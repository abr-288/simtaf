import { getAdminListings } from '@menewa/api-client';
import { ActionForm, Empty, fcfa, fdate, LISTING_STATUS, LISTING_TYPES, PageHeader, PAY_METHODS, Section, StatusBadge } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { decideContribution, moderateListing } from '../actions';

export default async function Page() {
  const { token } = await requirePerm('listings.moderate');
  const { listings, pendingContributions } = await getAdminListings(token);
  return (
    <>
      <PageHeader title="Publications partenaires & campagnes" subtitle="Modération des dons, actions solidaires et campagnes de financement participatif ; confirmation des contributions." />
      <Section title={`Contributions à confirmer (${pendingContributions.length})`}>
        {pendingContributions.length === 0 ? <Empty>Aucune contribution en attente.</Empty> : (
          <ul className="divide-y divide-slate-100 text-sm">
            {pendingContributions.map(({ contribution: c, userName, listingTitle }) => (
              <li key={c.id} className="flex flex-wrap items-center justify-between gap-2 py-2.5">
                <span><strong>{fcfa(c.amount)}</strong> — {userName} → {listingTitle}<br /><span className="text-xs text-slate-500">{PAY_METHODS[c.method ?? '']} · réf. {c.reference || '—'} · {fdate(c.createdAt)}</span></span>
                <span className="flex gap-2">
                  <ActionForm action={decideContribution} submit="Confirmer" btnClass="btn btn-green btn-sm" className=""><input type="hidden" name="id" value={c.id} /><input type="hidden" name="decision" value="confirm" /></ActionForm>
                  <ActionForm action={decideContribution} submit="Rejeter" btnClass="btn btn-danger btn-sm" className=""><input type="hidden" name="id" value={c.id} /><input type="hidden" name="decision" value="reject" /></ActionForm>
                </span>
              </li>
            ))}
          </ul>
        )}
      </Section>
      <div className="mt-5 space-y-3">
        {listings.map(({ listing: l, ownerName, organization }) => (
          <div key={l.id} className="card">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div><span className="badge badge-amber mr-2">{LISTING_TYPES[l.type]}</span><strong>{l.title}</strong> <span className="text-xs text-slate-500">· {organization || ownerName}</span></div>
              <StatusBadge meta={LISTING_STATUS} value={l.status} />
            </div>
            <p className="mt-1 text-sm text-slate-600">{l.description}</p>
            <p className="text-xs text-slate-500">Objectif {fcfa(l.goalAmount)} · collecté {fcfa(l.raisedAmount)}</p>
            <div className="mt-2 flex flex-wrap gap-2">
              {l.status !== 'published' && <ActionForm action={moderateListing} submit="Publier" btnClass="btn btn-green btn-sm" className=""><input type="hidden" name="id" value={l.id} /><input type="hidden" name="decision" value="publish" /></ActionForm>}
              {l.status === 'published' && <ActionForm action={moderateListing} submit="Clore" btnClass="btn btn-outline btn-sm" className=""><input type="hidden" name="id" value={l.id} /><input type="hidden" name="decision" value="close" /></ActionForm>}
              {l.status === 'pending' && (
                <ActionForm action={moderateListing} submit="Refuser" btnClass="btn btn-danger btn-sm" className="flex gap-2"><input type="hidden" name="id" value={l.id} /><input type="hidden" name="decision" value="reject" /><input name="reason" className="input" placeholder="Motif" /></ActionForm>
              )}
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
