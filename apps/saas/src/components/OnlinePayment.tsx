import type { OnlinePaymentConfig, Payment, PaymentTransaction } from '@menewa/shared-types';
import { ActionForm, fcfa, fdatetime } from '@menewa/ui';
import { payOnline, simulatePayment } from '@/app/espace/actions';

export const OPERATORS: Record<string, string> = { tmoney: 'Mixx by Yas (T-Money)', flooz: 'Flooz (Moov Africa)' };
const TX_STATUS: Record<string, string> = { pending: 'En attente de confirmation', succeeded: 'Confirmé', failed: 'Échoué' };

/** Paiement Mobile Money d'un paiement du membre, avec l'historique de ses tentatives. */
export function OnlinePayment({ payment: p, transactions, config, phone }: {
  payment: Payment; transactions: PaymentTransaction[]; config: OnlinePaymentConfig; phone: string;
}) {
  const pending = transactions.find((t) => t.status === 'pending');
  const payable = p.mode === 'money' && p.remaining > 0 && ['planned', 'declared', 'validated', 'partial'].includes(p.status);
  return (
    <div className="mt-3 space-y-2">
      {pending ? (
        <div className="rounded-xl border border-amber-300 bg-amber-50 p-3 text-sm text-amber-900">
          <p><strong>{fcfa(pending.amount)}</strong> en attente : confirmez le paiement sur le téléphone {pending.phone} ({OPERATORS[pending.operator] ?? pending.operator}).</p>
          {config.simulated && (
            <div className="mt-2 flex flex-wrap gap-2">
              <span className="w-full text-xs">Zone de test : aucun argent ne circule, choisissez l’issue.</span>
              <ActionForm action={simulatePayment} submit="Simuler : paiement confirmé" btnClass="btn btn-green btn-sm" className="contents">
                <input type="hidden" name="transactionId" value={pending.id} /><input type="hidden" name="outcome" value="success" />
              </ActionForm>
              <ActionForm action={simulatePayment} submit="Simuler : échec" btnClass="btn btn-outline btn-sm" className="contents">
                <input type="hidden" name="transactionId" value={pending.id} /><input type="hidden" name="outcome" value="failure" />
              </ActionForm>
            </div>
          )}
        </div>
      ) : payable && (
        <ActionForm action={payOnline} submit={`Payer ${fcfa(p.remaining)} par Mobile Money`} className="grid gap-2 sm:grid-cols-2" btnClass="btn btn-green btn-sm sm:col-span-2">
          <input type="hidden" name="paymentId" value={p.id} />
          <select name="operator" className="input" aria-label="Opérateur">{config.operators.map((o) => <option key={o} value={o}>{OPERATORS[o] ?? o}</option>)}</select>
          <input name="phone" defaultValue={phone} inputMode="tel" className="input" aria-label="Numéro Mobile Money" placeholder="+228 90 00 00 00" required />
          {config.simulated && <p className="text-xs text-slate-500 sm:col-span-2">Paiement simulé (zone de test) : aucun argent n’est débité.</p>}
        </ActionForm>
      )}
      {transactions.length > 0 && (
        <ul className="space-y-0.5 text-xs text-slate-500">
          {transactions.map((t) => (
            <li key={t.id}>{fdatetime(t.createdAt)} · {fcfa(t.amount)} · {OPERATORS[t.operator] ?? t.operator} {t.phone} · <strong>{TX_STATUS[t.status]}</strong>{t.providerRef ? ` · réf. ${t.providerRef}` : ''}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
