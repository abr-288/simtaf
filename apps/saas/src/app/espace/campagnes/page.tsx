import { getPublishedListings } from '@menewa/api-client';
import { ActionForm, Empty, fcfa, fdate, LISTING_TYPES, PageHeader, PAY_METHODS } from '@menewa/ui';
import { requireUser } from '@/lib/session';
import { contribute, supportListingMena } from '../actions';

export default async function Page() {
  const { token } = await requireUser();
  const rows = await getPublishedListings(token);
  return (
    <>
      <PageHeader title="Campagnes & actions partenaires" subtitle="Financement participatif, dons et actions solidaires publiés par nos partenaires." />
      {rows.length === 0 ? <Empty>Aucune campagne publiée.</Empty> : (
        <div className="grid gap-4 md:grid-cols-2">
          {rows.map(({ listing: l, organization, ownerName }) => {
            const pct = l.goalAmount ? Math.min(100, Math.round((l.raisedAmount / l.goalAmount) * 100)) : 0;
            return (
              <div key={l.id} className="card">
                <div className="flex items-center justify-between"><span className="badge badge-amber">{LISTING_TYPES[l.type]}</span><span className="text-xs text-slate-500">{organization || ownerName}</span></div>
                <p className="mt-2 text-lg font-bold text-brand-dark">{l.title}</p>
                <p className="text-sm text-slate-600">{l.description}</p>
                <p className="mt-1 text-xs text-slate-500">{l.zone}{l.endsAt && ` · jusqu'au ${fdate(l.endsAt)}`}</p>
                {l.goalAmount > 0 && (
                  <div className="mt-3">
                    <div className="h-2.5 overflow-hidden rounded-full bg-slate-200"><div className="h-full rounded-full bg-brand" style={{ width: `${pct}%` }} /></div>
                    <p className="mt-1 text-xs"><strong>{fcfa(l.raisedAmount)}</strong> sur {fcfa(l.goalAmount)} ({pct} %) · {l.menaSupport} Mena offerts</p>
                  </div>
                )}
                {l.type === 'crowdfunding' && (
                  <details className="mt-3">
                    <summary className="btn btn-green btn-sm cursor-pointer">Contribuer</summary>
                    <ActionForm action={contribute} submit="Enregistrer ma contribution" className="mt-3 space-y-2" btnClass="btn btn-yellow btn-sm">
                      <input type="hidden" name="listingId" value={l.id} />
                      <input name="amount" type="number" min={500} step={500} className="input" placeholder="Montant (FCFA)" required />
                      <select name="method" className="input" aria-label="Moyen de paiement">{['mobile_money', 'card', 'bank', 'cash'].map((m) => <option key={m} value={m}>{PAY_METHODS[m]}</option>)}</select>
                      <input name="reference" className="input" placeholder="Référence de transaction" />
                    </ActionForm>
                    <ActionForm action={supportListingMena} submit="Offrir des Mena" className="mt-2 flex gap-2" btnClass="btn btn-outline btn-sm">
                      <input type="hidden" name="listingId" value={l.id} />
                      <input name="amount" type="number" min={1} className="input" placeholder="Mena" aria-label="Mena à offrir" />
                    </ActionForm>
                  </details>
                )}
              </div>
            );
          })}
        </div>
      )}
    </>
  );
}
