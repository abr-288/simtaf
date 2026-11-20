import { fetchProtectedFile } from '@menewa/api-client';
import { sessionToken } from '@/lib/session';

export const dynamic = 'force-dynamic';

/** Proxy des fichiers protégés : le jeton reste côté serveur, l'API contrôle les droits. */
export async function GET(_req: Request, { params }: { params: Promise<{ kind: string; id: string }> }) {
  const token = await sessionToken();
  if (!token) return new Response('Non authentifié', { status: 401 });
  const { kind, id } = await params;
  if (!['proof', 'receipt', 'dispute'].includes(kind) || !/^\d+$/.test(id)) return new Response('Introuvable', { status: 404 });
  try {
    const upstream = await fetchProtectedFile(token, kind, Number(id));
    const headers = new Headers();
    for (const h of ['content-type', 'content-disposition', 'cache-control', 'x-content-type-options']) {
      const v = upstream.headers.get(h);
      if (v) headers.set(h, v);
    }
    return new Response(upstream.body, { status: 200, headers });
  } catch (error) {
    const status = typeof error === 'object' && error && 'status' in error ? Number((error as { status: number }).status) : 502;
    return new Response(status === 403 ? 'Accès refusé' : 'Fichier indisponible', { status });
  }
}
