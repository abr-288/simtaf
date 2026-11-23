import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import path from 'node:path';
import { Readable } from 'node:stream';

export const dynamic = 'force-dynamic';

/**
 * Téléchargement des versions de test de l'application mobile (APK Android), déposées par deploy/dev/build-apk.sh
 * dans DOWNLOADS_DIR, et fiche de la dernière version (menewa-test.json) lue par l'application pour se mettre à jour.
 */
const DIR = process.env.DOWNLOADS_DIR ?? '/data/downloads';

export async function GET(_req: Request, { params }: { params: Promise<{ name: string }> }) {
  const { name } = await params;
  // Nom simple uniquement : aucun chemin, aucune autre extension.
  if (!/^[a-z0-9][a-z0-9._-]{0,80}\.(apk|json)$/.test(name)) return new Response('Introuvable', { status: 404 });
  const json = name.endsWith('.json');
  const file = path.join(DIR, name);
  try {
    const info = await stat(file);
    if (!info.isFile()) return new Response('Introuvable', { status: 404 });
    const body = Readable.toWeb(createReadStream(file)) as ReadableStream;
    return new Response(body, {
      headers: {
        'Content-Type': json ? 'application/json; charset=utf-8' : 'application/vnd.android.package-archive',
        'Content-Length': String(info.size),
        ...(json ? {} : { 'Content-Disposition': `attachment; filename="${name}"` }),
        'Cache-Control': 'no-store',
        'X-Content-Type-Options': 'nosniff',
      },
    });
  } catch {
    return new Response('Introuvable', { status: 404 });
  }
}
