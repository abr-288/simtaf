import { exportStats } from '@menewa/api-client';
import { sessionToken } from '@/lib/session';

export const dynamic = 'force-dynamic';

/** Export CSV : l'API vérifie `stats.export` et trace l'export dans le journal d'audit. */
export async function GET(req: Request) {
  const token = await sessionToken();
  if (!token) return new Response('Non authentifié', { status: 401 });
  const sp = new URL(req.url).searchParams;
  const filters = Object.fromEntries(['from', 'to', 'status', 'category', 'provider', 'zone'].map((k) => [k, sp.get(k) ?? undefined]));
  try {
    const upstream = await exportStats(token, filters);
    return new Response(upstream.body, {
      headers: { 'content-type': 'text/csv; charset=utf-8', 'content-disposition': 'attachment; filename="menewa-rapport-demandes.csv"' },
    });
  } catch {
    return new Response('Accès refusé', { status: 403 });
  }
}
