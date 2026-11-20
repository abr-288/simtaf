'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { revokeIdentitySession } from '@menewa/api-client';
import { SESSION_COOKIE } from '@/lib/session';

export async function logoutAction() {
  const jar = await cookies();
  const token = jar.get(SESSION_COOKIE)?.value;
  if (token) await revokeIdentitySession(token).catch(() => undefined);
  jar.delete(SESSION_COOKIE);
  redirect('/login');
}
