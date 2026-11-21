import { headers } from 'next/headers';

/** Adresse du visiteur telle que vue par le proxy (premier maillon de X-Forwarded-For), relayée à l'API. */
export async function clientIp(): Promise<string | undefined> {
  const h = await headers();
  return h.get('x-forwarded-for')?.split(',')[0]?.trim() || h.get('x-real-ip') || undefined;
}
