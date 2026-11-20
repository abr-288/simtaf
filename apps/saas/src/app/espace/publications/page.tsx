import { getMyListings } from '@menewa/api-client';
import { ActionForm, Empty, fcfa, LISTING_STATUS, LISTING_TYPES, PageHeader, Section, StatusBadge, Table } from '@menewa/ui';
import { requireRole } from '@/lib/session';
import { createListing } from '../actions';

export default async function Page() {
  const { token } = await requireRole('partner');
  const rows = await getMyListings(token);
  return (
    <>
      <PageHeader title="Mes publications" subtitle="Publiez des dons, des actions solidaires ou des campagnes de financement participatif. Chaque publication est modérée avant mise en ligne." />
      <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
        <Section title="Publications">
          {rows.length === 0 ? <Empty>Aucune publication.</Empty> : (
            <Table>
              <thead><tr><th>Titre</th><th>Type</th><th>Objectif</th><th>Collecté</th><th>Statut</th></tr></thead>
              <tbody>
                {rows.map(({ listing: l, confirmedContributions }) => (
                  <tr key={l.id}>
                    <td className="font-semibold">{l.title}{l.rejectionReason && <span className="block text-xs text-red-600">Refus : {l.rejectionReason}</span>}</td>
                    <td>{LISTING_TYPES[l.type]}</td><td>{l.goalAmount ? fcfa(l.goalAmount) : '—'}</td>
                    <td>{fcfa(l.raisedAmount)} <span className="text-xs text-slate-500">({confirmedContributions} contrib.)</span></td>
                    <td><StatusBadge meta={LISTING_STATUS} value={l.status} /></td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Section>
        <Section title="Nouvelle publication">
          <ActionForm action={createListing} submit="Soumettre à modération">
            <select name="type" className="input" aria-label="Type"><option value="crowdfunding">Financement participatif</option><option value="action">Action solidaire</option><option value="don">Don</option></select>
            <input name="title" className="input" placeholder="Titre" required />
            <textarea name="description" rows={3} className="input" placeholder="Description" />
            <div className="grid grid-cols-2 gap-2"><input name="zone" className="input" placeholder="Zone" /><input name="goalAmount" type="number" min={0} className="input" placeholder="Objectif (FCFA)" /></div>
            <div><label className="label" htmlFor="endsAt">Date de fin (facultatif)</label><input id="endsAt" name="endsAt" type="date" className="input" /></div>
          </ActionForm>
        </Section>
      </div>
    </>
  );
}
