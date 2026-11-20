import { getMyRewards } from '@menewa/api-client';
import { ActionForm, Empty, fdatetime, PageHeader, Section, StatCard, StatusBadge, Table, TOKEN_STATUS } from '@menewa/ui';
import { requireUser } from '@/lib/session';
import { requestConversion, transferTokens } from '../actions';

export default async function Page() {
  const { token } = await requireUser();
  const r = await getMyRewards(token);
  const pct = r.next ? Math.round(((r.earned - r.level.min) / (r.next.min - r.level.min)) * 100) : 100;
  return (
    <>
      <PageHeader title="Token Mena" subtitle="Unité de récompense virtuelle : gagnez des Mena en rendant service et en participant." />
      <div className="mb-5 grid gap-4 sm:grid-cols-3">
        <StatCard label="Solde" value={`${r.balance} Mena`} />
        <StatCard label="Total gagné" value={`${r.earned} Mena`} tone="blue" />
        <StatCard label="Niveau" value={r.level.label} tone="amber" hint={r.next ? `Prochain : ${r.next.label} (${r.next.min} Mena)` : 'Niveau maximal'} />
      </div>
      <div className="card mb-5">
        <div className="h-2.5 overflow-hidden rounded-full bg-slate-200"><div className="h-full rounded-full bg-sun" style={{ width: `${pct}%` }} /></div>
        <p className="mt-2 text-xs text-slate-500">Niveaux : {r.levels.map((l) => `${l.label} (${l.min}+)`).join(' · ')}</p>
      </div>
      <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
        <Section title="Historique">
          {r.transactions.length === 0 ? <Empty>Aucune opération.</Empty> : (
            <Table>
              <thead><tr><th>Date</th><th>Motif</th><th>Mena</th><th>Statut</th></tr></thead>
              <tbody>{r.transactions.map((t) => (
                <tr key={t.id}><td className="whitespace-nowrap text-xs">{fdatetime(t.createdAt)}</td><td>{t.reason}</td>
                  <td className={`font-bold ${t.amount > 0 ? 'text-emerald-700' : 'text-red-600'}`}>{t.amount > 0 ? '+' : ''}{t.amount}</td><td><StatusBadge meta={TOKEN_STATUS} value={t.status} /></td></tr>
              ))}</tbody>
            </Table>
          )}
        </Section>
        <div className="space-y-5">
          <Section title="Badges">
            <div className="grid grid-cols-2 gap-2">
              {r.badges.map((b) => <div key={b.code} className={`rounded-xl border p-2 text-center text-xs ${b.earned ? 'border-sun bg-cream' : 'border-slate-200 opacity-50'}`}><p className="text-lg">{b.emoji}</p><p className="font-semibold">{b.label}</p><p className="text-slate-500">{b.desc}</p></div>)}
            </div>
          </Section>
          <Section title="Utiliser mes Mena">
            <p className="mb-3 text-sm text-slate-600">Offrez vos Mena en soutien symbolique à une campagne (page « Campagnes »). Les opérations ci-dessous sont soumises à validation juridique :</p>
            <fieldset disabled={!r.transferEnabled} className={!r.transferEnabled ? 'opacity-60' : ''}>
              <p className="mb-1 text-sm font-semibold">Transférer à un membre {!r.transferEnabled && <span className="badge badge-slate ml-1">Désactivé — validation juridique en attente</span>}</p>
              <ActionForm action={transferTokens} submit="Transférer" className="space-y-2" btnClass="btn btn-outline btn-sm">
                <input name="phone" className="input" placeholder="+228…" aria-label="Téléphone du destinataire" /><input name="amount" type="number" min={1} className="input" placeholder="Mena" aria-label="Mena" />
              </ActionForm>
            </fieldset>
            <fieldset disabled={!r.conversionEnabled} className={`mt-4 ${!r.conversionEnabled ? 'opacity-60' : ''}`}>
              <p className="mb-1 text-sm font-semibold">Convertir en monnaie {!r.conversionEnabled && <span className="badge badge-slate ml-1">Désactivé — validation réglementaire en attente</span>}</p>
              <ActionForm action={requestConversion} submit="Demander la conversion" className="space-y-2" btnClass="btn btn-outline btn-sm">
                <input name="amount" type="number" min={1} className="input" placeholder="Mena" aria-label="Mena à convertir" />
              </ActionForm>
            </fieldset>
          </Section>
        </div>
      </div>
    </>
  );
}
