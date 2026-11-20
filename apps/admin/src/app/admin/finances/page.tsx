import Link from 'next/link';
import { getFinances, getProviderDirectory } from '@menewa/api-client';
import { fcfa, PageHeader, PROVIDER_CATEGORIES, Section, StatCard, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';

export default async function Page() {
  const { token } = await requirePerm('finances.view');
  const [f, dir] = await Promise.all([getFinances(token), getProviderDirectory(token)]);
  return (
    <>
      <PageHeader title="Finances & marges" subtitle="Marge brute = contribution − (budget d’exécution + frais prestataires + autres frais)." />
      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Contributions" value={fcfa(f.revenue)} />
        <StatCard label="Coûts totaux" value={fcfa(f.cost)} tone="amber" />
        <StatCard label="Marge brute" value={fcfa(f.gross)} tone={f.gross < 0 ? 'red' : 'green'} />
        <StatCard label="Taux de marge" value={`${f.revenue ? ((f.gross / f.revenue) * 100).toFixed(1) : 0} %`} hint={`Cible provisoire : ${f.targetMarginPct} %`} tone="blue" />
      </div>
      <Table>
        <thead><tr><th>Demande</th><th>Contribution</th><th>Budget d’exéc.</th><th>Frais prestataires</th><th>Autres</th><th>Marge brute</th><th>%</th><th>Acteurs</th></tr></thead>
        <tbody>
          {f.rows.map((r) => (
            <tr key={r.id}>
              <td><Link href={`/admin/demandes/${r.id}`} className="font-semibold text-brand hover:underline">{r.ref}</Link><span className="block text-xs text-slate-500">{r.title}</span></td>
              <td>{fcfa(r.totalAmount)}</td><td>{fcfa(r.executionBudget)}</td><td>{fcfa(r.margin.fees)}</td><td>{fcfa(r.otherFees)}</td>
              <td className={r.margin.gross < 0 ? 'font-bold text-red-600' : 'font-bold text-brand'}>{fcfa(r.margin.gross)}</td>
              <td className={r.margin.pct < f.targetMarginPct ? 'text-amber-600' : ''}>{r.margin.pct.toFixed(1)} %</td>
              <td className="text-xs">Donateur + {r.actors} prestataire(s){r.partnerId ? ' + partenaire' : ''}</td>
            </tr>
          ))}
        </tbody>
      </Table>
      <div className="mt-6">
        <Section title="Répertoire des prestataires, partenaires et coûts">
          <Table>
            <thead><tr><th>Prestataire</th><th>Catégorie</th><th>Services et coûts unitaires</th></tr></thead>
            <tbody>
              {dir.map((p) => (
                <tr key={p.id}><td className="font-semibold">{p.name}</td><td>{PROVIDER_CATEGORIES[p.category]}</td>
                  <td className="text-xs">{p.services.map((s) => `${s.label} — ${fcfa(s.unitCost)}/${s.unit}`).join(' · ') || '—'}</td></tr>
              ))}
            </tbody>
          </Table>
        </Section>
      </div>
    </>
  );
}
