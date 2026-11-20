package com.menewa.platform;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Erreurs métier renvoyées au client (ProblemDetail, champ `detail` lisible par l'utilisateur). */
public final class Problems {
    private Problems() {}

    public static ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    public static ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    public static ResponseStatusException forbidden(String message) { return new ResponseStatusException(HttpStatus.FORBIDDEN, message); }
    public static ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
    public static ResponseStatusException forbidden() { return forbidden("Accès refusé."); }

    public static String trim(String value, int max) {
        if (value == null) return "";
        String t = value.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    public static String trimToNull(String value, int max) {
        String t = trim(value, max);
        return t.isEmpty() ? null : t;
    }
}
