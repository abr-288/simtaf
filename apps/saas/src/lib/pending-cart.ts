// Module serveur uniquement (cookies) : importé par les actions et la connexion.
import { cookies } from 'next/headers';
import { addToCart } from '@menewa/api-client';

/** Don choisi dans le simulateur par un visiteur non connecté : ajouté au panier juste après sa connexion. */
const PENDING_CART_COOKIE = 'menewa_pending_cart';
export const CART_ROLES = ['donor', 'client'];

export type PendingCart = { itemId: number; quantity: number; monthly: boolean };

export function parsePendingCart(input: { itemId?: unknown; quantity?: unknown; freq?: unknown }): PendingCart | null {
  const itemId = Number(input.itemId);
  const quantity = Math.min(Math.max(Math.floor(Number(input.quantity)) || 1, 1), 500); // plafond du panier côté API
  if (!Number.isInteger(itemId) || itemId <= 0) return null;
  return { itemId, quantity, monthly: input.freq === 'monthly' };
}

export const cartUrl = (p: PendingCart) => `/espace/panier?ajout=simulateur${p.monthly ? '&freq=monthly' : ''}`;

export async function savePendingCart(p: PendingCart) {
  (await cookies()).set(PENDING_CART_COOKIE, JSON.stringify(p), {
    httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production', path: '/', maxAge: 30 * 60,
  });
}

/** Après connexion : ajoute le don en attente au panier. Renvoie l'adresse du panier, ou null s'il n'y avait rien à ajouter. */
export async function consumePendingCart(token: string, role: string): Promise<string | null> {
  const jar = await cookies();
  const raw = jar.get(PENDING_CART_COOKIE)?.value;
  if (!raw) return null;
  jar.delete(PENDING_CART_COOKIE);
  if (!CART_ROLES.includes(role)) return null;
  try {
    const pending = parsePendingCart(JSON.parse(raw));
    if (!pending) return null;
    await addToCart(token, pending.itemId, pending.quantity);
    return cartUrl(pending);
  } catch {
    return null; // article retiré du catalogue entre-temps, etc. : la connexion reste valide
  }
}
