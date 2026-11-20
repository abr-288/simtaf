import Link from 'next/link';
import { getAdminDisputes } from '@menewa/api-client';
import { DISPUTE_STATUS, Empty, fdate, PageHeader, StatusBadge, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';

export default async function Page() {
  const { token } = await requirePerm('disputes.manage');
  const rows = await getAdminDisputes(token);
  return (
    <>
      <PageHeader title="Litiges" subtitle="Parties, objet, pièces, responsable, échanges, décision et clôture." />
      {rows.length === 0 ? <Empty>Aucun litige.</Empty> : (
        <Table>
          <thead><tr><th>Réf.</th><th>Objet</th><th>Ouvert par</th><th>Date</th><th>Clôture</th><th>Statut</th></tr></thead>
          <tbody>{rows.map(({ dispute: d, openedByName }) => <tr key={d.id}><td className="font-mono text-xs">{d.ref}</td><td><Link href={`/admin/litiges/${d.id}`} className="font-semibold text-brand hover:underline">{d.subject}</Link></td><td>{openedByName}</td><td>{fdate(d.createdAt)}</td><td>{fdate(d.closedAt)}</td><td><StatusBadge meta={DISPUTE_STATUS} value={d.status} /></td></tr>)}</tbody>
        </Table>
      )}
    </>
  );
}
