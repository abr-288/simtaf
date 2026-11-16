'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { revokeIdentitySession } from '@menewa/api-client';

export async function logoutAction() {
  const jar = await cookies();
  const token = jar.get('menewa_admin_session')?.value;
  if (token) await revokeIdentitySession(token).catch(() => undefined);
  jar.delete('menewa_admin_session');
  redirect('/login');
}
