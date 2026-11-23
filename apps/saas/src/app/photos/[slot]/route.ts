import { fetchHomePhoto } from '@menewa/api-client';

export const dynamic = 'force-dynamic';

/** Photos publiques de l'accueil, relayées depuis l'API (le navigateur n'accède pas directement au backend). */
export async function GET(_req: Request, { params }: { params: Promise<{ slot: string }> }) {
  const { slot } = await params;
  if (!/^[a-z]{2,20}$/.test(slot)) return new Response('Introuvable', { status: 404 });
  try {
    const upstream = await fetchHomePhoto(slot);
    const headers = new Headers({ 'cache-control': 'public, max-age=3600', 'x-content-type-options': 'nosniff' });
    const type = upstream.headers.get('content-type');
    if (type) headers.set('content-type', type);
    return new Response(upstream.body, { status: 200, headers });
  } catch {
    return new Response('Photo indisponible', { status: 404 });
  }
}
