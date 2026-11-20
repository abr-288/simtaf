import Link from 'next/link';
import { getAdminRequests } from '@menewa/api-client';
import { Empty, fcfa, fdate, KIND_LABEL, PageHeader, REQUEST_STATUS, StatusBadge, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';

export default async function Page({ searchParams }: { searchParams: Promise<{ status?: string; q?: string }> }) {
  const { token } = await requirePerm('requests.manage');
  const { status, q } = await searchParams;
  const rows = await getAdminRequests(token, status, q);
  return (
    <>
      <PageHeader title="Demandes" subtitle="Traitement des demandes : acceptation, refus motivé, demande d’informations." />
      <form className="mb-3 flex flex-wrap gap-2">
        <input name="q" defaultValue={q} className="input max-w-xs" placeholder="Référence, titre, cible…" />
        <select name="status" defaultValue={status ?? ''} className="input max-w-xs"><option value="">Tous statuts</option>{Object.entries(REQUEST_STATUS).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}</select>
        <button className="btn btn-green btn-sm">Filtrer</button>
      </form>
      {rows.length === 0 ? <Empty>Aucune demande.</Empty> : (
        <Table>
          <thead><tr><th>Réf.</th><th>Intitulé</th><th>Donateur</th><th>Type</th><th>Montant</th><th>Date</th><th>Statut</th></tr></thead>
          <tbody>
            {rows.map(({ request: r, donorName, donorPhone }) => (
              <tr key={r.id}>
                <td className="font-mono text-xs">{r.ref}</td>
                <td><Link href={`/admin/demandes/${r.id}`} className="font-semibold text-brand hover:underline">{r.title}</Link></td>
                <td>{donorName || donorPhone}</td><td>{KIND_LABEL[r.kind]}</td><td>{fcfa(r.totalAmount)}</td><td>{fdate(r.createdAt)}</td>
                <td><StatusBadge meta={REQUEST_STATUS} value={r.status} /></td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
    </>
  );
}
