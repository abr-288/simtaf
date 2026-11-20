import { cache } from 'react';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { ApiRequestError, getIdentityUser } from '@menewa/api-client';
import type { IdentityUser } from '@menewa/shared-types';
import { isAdminRole } from '@menewa/ui/format';

export const SESSION_COOKIE = 'menewa_saas_session';

export async function sessionToken() {
  return (await cookies()).get(SESSION_COOKIE)?.value;
}

export type MemberSession = { token: string; user: IdentityUser };

/** Session courante (null si absente ou expirée) ; une API injoignable remonte à la page d'erreur. */
export const getSession = cache(async (): Promise<MemberSession | null> => {
  const token = await sessionToken();
  if (!token) return null;
  try {
    return { token, user: await getIdentityUser(token) };
  } catch (error) {
    if (error instanceof ApiRequestError && (error.status === 401 || error.status === 403)) return null;
    throw error;
  }
});

/** Espace membre : les administrateurs utilisent la console d'administration. */
export async function requireUser(): Promise<MemberSession> {
  const session = await getSession();
  if (!session) redirect('/connexion');
  if (isAdminRole(session.user.role)) redirect(process.env.ADMIN_APP_URL ?? 'http://localhost:3001');
  return session;
}

/** Garde de navigation (confort) : le backend revérifie le rôle sur chaque endpoint. */
export async function requireRole(...roles: string[]): Promise<MemberSession> {
  const session = await requireUser();
  if (!roles.includes(session.user.role)) redirect('/espace');
  return session;
}
