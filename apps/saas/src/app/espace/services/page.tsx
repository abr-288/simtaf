import Link from 'next/link';
import { getMyServiceOrders, searchProviders } from '@menewa/api-client';
import { ActionForm, Empty, fcfa, fdate, ORDER_STATUS, PageHeader, PROVIDER_CATEGORIES, Section, Stars, StatusBadge, Table } from '@menewa/ui';
import { requireUser } from '@/lib/session';
import { createServiceOrder } from '../actions';

export default async function Page({ searchParams }: { searchParams: Promise<{ q?: string; cat?: string; zone?: string }> }) {
  const { token, user: u } = await requireUser();
  const { q, cat, zone } = await searchParams;
  const [orders, provs] = await Promise.all([getMyServiceOrders(token), searchProviders(token, q, cat, zone)]);
  return (
    <>
      <PageHeader title="Services communautaires" subtitle="Trouvez un prestataire local, négociez le prix ou un échange de services, suivez la prestation et notez." />
      <Section title="Mes demandes de services">
        {orders.length === 0 ? <Empty>Aucune demande de service.</Empty> : (
          <Table>
            <thead><tr><th>Service</th><th>{u.role === 'provider' ? 'Client' : 'Prestataire'}</th><th>Prix</th><th>Date</th><th>Statut</th><th></th></tr></thead>
            <tbody>
              {orders.map(({ order: o, clientName, providerName, iAmProvider }) => (
                <tr key={o.id}>
                  <td className="font-semibold">{o.title}</td>
                  <td>{iAmProvider ? clientName : providerName}</td>
                  <td>{o.compensationMode === 'exchange' ? 'Échange' : fcfa(o.agreedPrice ?? o.proposedPrice)}</td>
                  <td>{fdate(o.createdAt)}</td>
                  <td><StatusBadge meta={ORDER_STATUS} value={o.status} /></td>
                  <td><Link href={`/espace/services/${o.id}`} className="btn btn-outline btn-sm">Ouvrir</Link></td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
      </Section>
      <div className="mt-6">
        <h2 className="mb-3 text-lg font-bold text-brand-dark">Rechercher un prestataire</h2>
        <form className="mb-4 flex flex-wrap gap-2">
          <input name="q" defaultValue={q} placeholder="Mot-clé…" className="input max-w-xs" aria-label="Mot-clé" />
          <select name="cat" defaultValue={cat ?? ''} className="input max-w-xs" aria-label="Catégorie"><option value="">Toutes catégories</option>{Object.entries(PROVIDER_CATEGORIES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select>
          <input name="zone" defaultValue={zone} placeholder="Zone" className="input max-w-[160px]" aria-label="Zone" />
          <button className="btn btn-green">Rechercher</button>
        </form>
        <div className="grid gap-4 md:grid-cols-2">
          {provs.map((p) => (
            <div key={p.id} className="card">
              <div className="flex items-start justify-between">
                <div><p className="font-bold text-brand-dark">{p.name} {p.verified && <span className="badge badge-green">Vérifié</span>}</p><p className="text-xs text-slate-500">{PROVIDER_CATEGORIES[p.category]} · {p.zone}</p></div>
                <div className="text-right"><Stars value={p.rating} /><p className="text-[11px] text-slate-500">{p.reviews} avis</p></div>
              </div>
              <p className="mt-2 text-sm text-slate-600">{p.description}</p>
              <ul className="mt-2 text-xs text-slate-600">{p.services.map((s) => <li key={s.id}>• {s.label} — {fcfa(s.unitCost)} / {s.unit}</li>)}</ul>
              <details className="mt-3">
                <summary className="btn btn-yellow btn-sm cursor-pointer">Demander ce prestataire</summary>
                <ActionForm action={createServiceOrder} submit="Envoyer la demande" className="mt-3 space-y-2" btnClass="btn btn-green btn-sm">
                  <input type="hidden" name="providerId" value={p.id} />
                  <input name="title" className="input" placeholder="Service demandé" required />
                  <textarea name="description" rows={2} className="input" placeholder="Détails" />
                  <div className="grid grid-cols-2 gap-2">
                    <select name="compensationMode" className="input" aria-label="Rémunération"><option value="money">Paiement monétaire</option><option value="exchange">Échange de services</option></select>
                    <input name="price" type="number" min={0} step={500} className="input" placeholder="Prix proposé (FCFA)" />
                  </div>
                  <input name="exchangeDescription" className="input" placeholder="Si échange : engagements réciproques" />
                  <input name="zone" className="input" placeholder="Lieu / zone" />
                </ActionForm>
              </details>
            </div>
          ))}
          {provs.length === 0 && <p className="text-sm text-slate-500">Aucun prestataire trouvé.</p>}
        </div>
      </div>
    </>
  );
}
