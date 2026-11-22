import Link from 'next/link';
import { getAdminPayments, getOnlinePaymentConfig, getPaymentTransactionsAdmin } from '@menewa/api-client';
import type { PaymentTransaction } from '@menewa/shared-types';
import { ActionForm, Empty, fcfa, fdate, PageHeader, PAY_METHODS, PAYMENT_STATUS, StatusBadge } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { payoutPayment, simulatePayment, updatePayment } from '../actions';

const OPERATORS: Record<string, string> = { tmoney: 'Mixx by Yas (T-Money)', flooz: 'Flooz (Moov Africa)' };
const TX_STATUS: Record<string, string> = { pending: 'en attente', succeeded: 'confirmé', failed: 'échoué' };

export default async function Page({ searchParams }: { searchParams: Promise<{ s?: string; d?: string }> }) {
  const { token } = await requirePerm('payments.manage');
  const { s, d } = await searchParams;
  const [{ payments, planned, paid }, txs, payConfig] = await Promise.all([getAdminPayments(token, s, d), getPaymentTransactionsAdmin(token), getOnlinePaymentConfig()]);
  const txOf = (id: number) => txs.filter((t: PaymentTransaction) => t.paymentId === id);
  return (
    <>
      <PageHeader title="Paiements & échanges de services" subtitle="Montants prévus, validés, payés et restant dus ; justificatifs et références de transaction." />
      <div className="mb-4 flex flex-wrap gap-2">
        <Link href="/admin/paiements" className={`btn btn-sm ${!s && !d ? 'btn-green' : 'btn-outline'}`}>Tous</Link>
        <Link href="/admin/paiements?d=in" className={`btn btn-sm ${d === 'in' ? 'btn-green' : 'btn-outline'}`}>Encaissements</Link>
        <Link href="/admin/paiements?d=out" className={`btn btn-sm ${d === 'out' ? 'btn-green' : 'btn-outline'}`}>Rémunérations</Link>
        {['declared', 'validated', 'partial', 'paid'].map((k) => <Link key={k} href={`/admin/paiements?s=${k}`} className={`btn btn-sm ${s === k ? 'btn-green' : 'btn-outline'}`}>{PAYMENT_STATUS[k].label}</Link>)}
      </div>
      <p className="mb-3 text-sm text-slate-600">Prévu : <strong>{fcfa(planned)}</strong> · Payé : <strong>{fcfa(paid)}</strong></p>
      {payConfig.simulated && <p className="mb-3 rounded-xl border border-amber-300 bg-amber-50 p-3 text-sm text-amber-900">Paiement Mobile Money <strong>simulé</strong> (zone de test) : aucun argent ne circule. L’issue de chaque transaction se choisit avec les boutons de test.</p>}
      {payments.length === 0 ? <Empty>Aucun paiement.</Empty> : (
        <div className="space-y-3">
          {payments.map(({ payment: p, userName, userPhone }) => (
            <details key={p.id} className="card p-4">
              <summary className="flex cursor-pointer flex-wrap items-center justify-between gap-2 text-sm">
                <span><strong>{p.direction === 'in' ? '↘' : '↗'} {p.note || p.kind}</strong> · {userName || userPhone} · {p.mode === 'exchange' ? 'Échange de services' : `${fcfa(p.amountPlanned)} prévu`}{p.requestId && <> · <Link href={`/admin/demandes/${p.requestId}`} className="text-brand underline">demande</Link></>}</span>
                <span className="flex items-center gap-2"><span className="text-xs text-slate-500">Éch. {fdate(p.dueDate)} · payé {fcfa(p.amountPaid)}</span><StatusBadge meta={PAYMENT_STATUS} value={p.status} /></span>
              </summary>
              <div className="mt-3 text-xs text-slate-500">Moyen : {PAY_METHODS[p.method ?? ''] ?? '—'} · Réf. : {p.reference || '—'} {p.hasReceipt && <a href={`/files/receipt/${p.id}`} target="_blank" className="font-semibold text-brand underline">· Justificatif</a>}{p.exchangeDescription && <p>Échange : {p.exchangeDescription} ({p.exchangeStatus})</p>}</div>
              {(() => {
                const list = txOf(p.id);
                const pending = list.find((t) => t.status === 'pending');
                const canPayout = p.direction === 'out' && p.mode === 'money' && ['validated', 'partial'].includes(p.status) && p.remaining > 0 && !pending;
                return (
                  <div className="mt-3 space-y-2 text-sm">
                    {list.length > 0 && <ul className="space-y-0.5 text-xs text-slate-500">{list.map((t) => <li key={t.id}>{t.direction === 'in' ? 'Encaissement' : 'Reversement'} Mobile Money · {fcfa(t.amount)} · {OPERATORS[t.operator] ?? t.operator} {t.phone} · <strong>{TX_STATUS[t.status]}</strong>{t.providerRef && ` · réf. ${t.providerRef}`}</li>)}</ul>}
                    {pending && payConfig.simulated && (
                      <div className="flex flex-wrap gap-2">
                        <ActionForm action={simulatePayment} submit="Simuler : confirmé" btnClass="btn btn-green btn-sm" className="contents"><input type="hidden" name="transactionId" value={pending.id} /><input type="hidden" name="outcome" value="success" /></ActionForm>
                        <ActionForm action={simulatePayment} submit="Simuler : échec" btnClass="btn btn-outline btn-sm" className="contents"><input type="hidden" name="transactionId" value={pending.id} /><input type="hidden" name="outcome" value="failure" /></ActionForm>
                      </div>
                    )}
                    {canPayout && (
                      <ActionForm action={payoutPayment} submit={`Verser ${fcfa(p.remaining)} par Mobile Money`} className="grid gap-2 rounded-xl bg-slate-50 p-3 sm:grid-cols-3" btnClass="btn btn-green btn-sm">
                        <input type="hidden" name="paymentId" value={p.id} />
                        <select name="operator" className="input" aria-label="Opérateur">{payConfig.operators.map((o) => <option key={o} value={o}>{OPERATORS[o] ?? o}</option>)}</select>
                        <input name="phone" defaultValue={userPhone ?? ''} className="input" aria-label="Numéro Mobile Money du prestataire" />
                      </ActionForm>
                    )}
                  </div>
                );
              })()}
              <ActionForm action={updatePayment} submit="Enregistrer" className="mt-3 grid gap-2 sm:grid-cols-3" btnClass="btn btn-green btn-sm">
                <input type="hidden" name="paymentId" value={p.id} />
                <div><label className="label">Montant validé</label><input name="amountValidated" type="number" min={0} defaultValue={p.amountValidated ?? p.amountPlanned} className="input" /></div>
                <div><label className="label">Montant payé</label><input name="amountPaid" type="number" min={0} defaultValue={p.amountPaid} className="input" /></div>
                <div><label className="label">Statut</label><select name="status" defaultValue={p.status} className="input">{Object.entries(PAYMENT_STATUS).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}</select></div>
                <div><label className="label">Référence</label><input name="reference" defaultValue={p.reference ?? ''} className="input" /></div>
                {p.mode === 'exchange' && <div><label className="label">Exécution de l’échange</label><select name="exchangeStatus" defaultValue={p.exchangeStatus ?? 'pending'} className="input"><option value="pending">À faire</option><option value="in_progress">En cours</option><option value="done">Réalisé</option></select></div>}
                <div><label className="label">Facture / reçu</label><input name="receipt" type="file" accept="image/*,application/pdf" className="input" /></div>
                <div className="sm:col-span-3"><label className="label">Note</label><input name="note" defaultValue={p.note ?? ''} className="input" /></div>
              </ActionForm>
            </details>
          ))}
        </div>
      )}
    </>
  );
}
