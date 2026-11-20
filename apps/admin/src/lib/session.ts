import { cache } from 'react';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { ApiRequestError, getIdentityUser } from '@menewa/api-client';
import type { IdentityUser } from '@menewa/shared-types';
import { isAdminRole, type Perm } from '@menewa/ui/format';

export const SESSION_COOKIE = 'menewa_admin_session';

export async function sessionToken() {
  return (await cookies()).get(SESSION_COOKIE)?.value;
}

export type AdminSession = { token: string; user: IdentityUser };

/** Session courante (null si absente ou expirée) ; une API injoignable remonte à la page d'erreur. */
export const getSession = cache(async (): Promise<AdminSession | null> => {
  const token = await sessionToken();
  if (!token) return null;
  try {
    return { token, user: await getIdentityUser(token) };
  } catch (error) {
    if (error instanceof ApiRequestError && (error.status === 401 || error.status === 403)) return null;
    throw error;
  }
});

/** Confort d'affichage uniquement : chaque endpoint revérifie la permission côté backend. */
export function hasPerm(user: IdentityUser, perm: Perm) {
  if (user.role === 'admin') return true;
  return user.role === 'subadmin' && perm !== 'roles.manage' && user.permissions.includes(perm);
}

export async function requireAdmin(): Promise<AdminSession> {
  const session = await getSession();
  if (!session) redirect('/login');
  if (!isAdminRole(session.user.role)) redirect('/login?error=forbidden');
  return session;
}

export async function requirePerm(perm: Perm): Promise<AdminSession> {
  const session = await requireAdmin();
  if (!hasPerm(session.user, perm)) redirect(`/admin?denied=${encodeURIComponent(perm)}`);
  return session;
}
