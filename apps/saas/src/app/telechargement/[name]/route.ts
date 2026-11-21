import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import path from 'node:path';
import { Readable } from 'node:stream';

export const dynamic = 'force-dynamic';

/**
 * Téléchargement des versions de test de l'application mobile (APK Android), déposées par deploy/dev/build-apk.sh
 * dans DOWNLOADS_DIR. En zone de dev, l'accès est déjà protégé par le mot de passe d'accès du proxy.
 */
const DIR = process.env.DOWNLOADS_DIR ?? '/data/downloads';

export async function GET(_req: Request, { params }: { params: Promise<{ name: string }> }) {
  const { name } = await params;
  // Nom simple uniquement : aucun chemin, aucune autre extension.
  if (!/^[a-z0-9][a-z0-9._-]{0,80}\.apk$/.test(name)) return new Response('Introuvable', { status: 404 });
  const file = path.join(DIR, name);
  try {
    const info = await stat(file);
    if (!info.isFile()) return new Response('Introuvable', { status: 404 });
    const body = Readable.toWeb(createReadStream(file)) as ReadableStream;
    return new Response(body, {
      headers: {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': String(info.size),
        'Content-Disposition': `attachment; filename="${name}"`,
        'Cache-Control': 'no-store',
        'X-Content-Type-Options': 'nosniff',
      },
    });
  } catch {
    return new Response('Introuvable', { status: 404 });
  }
}
