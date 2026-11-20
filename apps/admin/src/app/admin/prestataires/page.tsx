import { getAdminProviders } from '@menewa/api-client';
import { fcfa, PageHeader, PROVIDER_CATEGORIES, Stars, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { verifyProvider } from '../actions';

export default async function Page() {
  const { token } = await requirePerm('providers.manage');
  const rows = await getAdminProviders(token);
  return (
    <>
      <PageHeader title="Prestataires" subtitle="Identification des prestataires, services proposés, coûts et notation. La vérification est décidée par l’administration." />
      <Table>
        <thead><tr><th>Prestataire</th><th>Catégorie</th><th>Zone</th><th>Services & coûts</th><th>Note</th><th>Vérifié</th></tr></thead>
        <tbody>
          {rows.map((p) => (
            <tr key={p.id}>
              <td className="font-semibold">{p.name}<br /><span className="font-mono text-xs font-normal text-slate-500">{p.phone}</span></td>
              <td>{PROVIDER_CATEGORIES[p.category]}</td><td>{p.zone}</td>
              <td className="text-xs">{p.services.map((s) => <div key={s.id}>{s.label} — {fcfa(s.unitCost)}/{s.unit}</div>)}</td>
              <td><Stars value={p.rating} /> <span className="text-xs text-slate-500">({p.reviews})</span></td>
              <td><form action={verifyProvider}><input type="hidden" name="userId" value={p.id} /><input type="hidden" name="verified" value={p.verified ? '0' : '1'} /><button className={`btn btn-sm ${p.verified ? 'btn-green' : 'btn-outline'}`}>{p.verified ? '✔ Vérifié' : 'Vérifier'}</button></form></td>
            </tr>
          ))}
        </tbody>
      </Table>
    </>
  );
}
