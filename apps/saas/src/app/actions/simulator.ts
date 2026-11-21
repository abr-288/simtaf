'use server';

import { redirect } from 'next/navigation';
import { addToCart } from '@menewa/api-client';
import { CART_ROLES, cartUrl, parsePendingCart, savePendingCart } from '@/lib/pending-cart';
import { getSession } from '@/lib/session';

/** Simulateur d'impact : ajoute le don choisi au panier, ou le garde en attente le temps de la connexion. */
export async function addFromSimulator(fd: FormData) {
  const pending = parsePendingCart({ itemId: fd.get('itemId'), quantity: fd.get('quantity'), freq: fd.get('freq') });
  if (!pending) redirect('/social-kitchen');
  const session = await getSession();
  if (!session) {
    await savePendingCart(pending);
    redirect('/connexion?raison=simulateur');
  }
  if (!CART_ROLES.includes(session.user.role)) redirect('/espace');
  const added = await addToCart(session.token, pending.itemId, pending.quantity).then(() => true, () => false);
  redirect(added ? cartUrl(pending) : '/espace/catalogue'); // don retiré du catalogue entre-temps : choisir dans le catalogue
}
