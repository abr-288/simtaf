import Link from 'next/link';
import { randomUUID } from 'node:crypto';
import { getCart, getTargets } from '@menewa/api-client';
import { ActionForm, Empty, fcfa, PageHeader, Section } from '@menewa/ui';
import { AnnexFields, ScheduleFields, TargetSelect } from '@/components/RequestFields';
import { requireRole } from '@/lib/session';
import { checkoutCart, removeCartItem } from '../actions';

export default async function Page() {
  const { token, user: u } = await requireRole('donor', 'client');
  const [cart, targets] = await Promise.all([getCart(token), getTargets()]);
  // Cible déjà choisie dans le catalogue : la première renseignée parmi les articles du panier
  const cartTarget = cart.lines.find((l) => l.target)?.target ?? '';
  return (
    <>
      <PageHeader title="Mon panier" subtitle="Vérifiez vos dons, précisez la cible et validez : une demande sera créée et examinée par l’équipe." />
      {cart.lines.length === 0 ? (
        <Empty>Votre panier est vide. <Link href="/espace/catalogue" className="font-semibold text-brand underline">Parcourir le catalogue</Link></Empty>
      ) : (
        <div className="grid gap-5 lg:grid-cols-[1fr_1.2fr]">
          <Section title="Articles">
            <ul className="divide-y divide-slate-100 text-sm">
              {cart.lines.map((l) => (
                <li key={l.id} className="flex items-center justify-between gap-3 py-3">
                  <span className="text-2xl">{l.emoji}</span>
                  <span className="flex-1"><strong>{l.title}</strong><br /><span className="text-xs text-slate-500">{l.quantity} × {fcfa(l.unitPrice)}{l.target ? ` · cible : ${l.target}` : ''}</span></span>
                  <span className="font-semibold">{fcfa(l.quantity * l.unitPrice)}</span>
                  <form action={removeCartItem}><input type="hidden" name="id" value={l.id} /><button className="text-xs text-red-600 hover:underline" aria-label={`Retirer ${l.title}`}>Retirer</button></form>
                </li>
              ))}
            </ul>
            <p className="mt-3 flex justify-between border-t border-slate-200 pt-3 text-base font-bold text-brand-dark"><span>Total (hors annexes)</span><span>{fcfa(cart.total)}</span></p>
            <p className="mt-1 text-xs text-slate-500">Le montant définitif est recalculé par la plateforme à partir des tarifs en vigueur.</p>
          </Section>
          <Section title="Valider ma demande">
            <ActionForm action={checkoutCart} submit="Envoyer la demande">
              {/* Clé d'idempotence : un double envoi du formulaire ne crée qu'une seule demande. */}
              <input type="hidden" name="idempotencyKey" value={randomUUID()} />
              <div className="grid gap-3 sm:grid-cols-2">
                <div><label className="label" htmlFor="targetName">Cible du don</label><TargetSelect id="targetName" name="targetName" targets={targets} allowCustom={u.role === 'donor'} defaultValue={cartTarget} required /></div>
                <div><label className="label" htmlFor="targetZone">Zone / ville</label><input id="targetZone" name="targetZone" className="input" placeholder="Ex. Kpalimé" /></div>
              </div>
              <div><label className="label" htmlFor="description">Précisions</label><textarea id="description" name="description" rows={2} className="input" placeholder="Informations utiles pour la remise" /></div>
              <ScheduleFields />
              <AnnexFields prices={cart.annexPrices} />
            </ActionForm>
          </Section>
        </div>
      )}
    </>
  );
}
