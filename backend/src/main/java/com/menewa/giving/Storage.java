package com.menewa.giving;

import com.menewa.platform.Problems;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stockage privé des fichiers (preuves, reçus, pièces de litige) hors de tout webroot. Le type est
 * déterminé par la signature réelle du contenu ; seule une clé opaque est conservée en base.
 */
@Component
class Storage {
    static final long MAX_BYTES = 8L * 1024 * 1024;

    record Saved(String key, String name, String mime, long size, String sha256) {}

    private final Path root;

    Storage(@Value("${menewa.storage.dir:./storage}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    /** @return null si aucun fichier n'a été transmis. */
    Saved save(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > MAX_BYTES) throw Problems.bad("Fichier trop volumineux (8 Mo maximum).");
        try {
            byte[] bytes = file.getBytes();
            String mime = sniff(bytes);
            if (mime == null) throw Problems.bad("Format non autorisé (JPG, PNG, WEBP, PDF ou MP4).");
            String ext = switch (mime) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                case "application/pdf" -> "pdf";
                default -> "mp4";
            };
            Path dir = root.resolve(folder);
            Files.createDirectories(dir);
            String key = folder + "/" + UUID.randomUUID() + "." + ext;
            Files.write(root.resolve(key), bytes);
            String original = file.getOriginalFilename() == null ? "fichier." + ext : file.getOriginalFilename();
            String name = original.length() > 120 ? original.substring(0, 120) : original;
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            return new Saved(key, name, mime, bytes.length, hash);
        } catch (IOException | java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Enregistrement du fichier impossible", ex);
        }
    }

    byte[] read(String key) throws IOException {
        Path full = root.resolve(key).normalize();
        if (!full.startsWith(root)) throw new IOException("Chemin invalide");
        return Files.readAllBytes(full);
    }

    static String sniff(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
        if (b.length >= 8 && Arrays.equals(Arrays.copyOf(b, 8), new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A})) return "image/png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') return "application/pdf";
        if (b.length >= 12 && b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p') return "video/mp4";
        return null;
    }
}
