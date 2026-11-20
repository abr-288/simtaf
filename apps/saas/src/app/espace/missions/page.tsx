import Link from 'next/link';
import { getMyMissions } from '@menewa/api-client';
import { Empty, fcfa, fdate, MISSION_STATUS, PageHeader, StatusBadge, Table } from '@menewa/ui';
import { requireRole } from '@/lib/session';

const TABS: [string, string, string[]][] = [
  ['', 'Toutes', []],
  ['proposed', 'Proposées', ['proposed']],
  ['accepted', 'Acceptées', ['accepted']],
  ['in_progress', 'En cours', ['in_progress']],
  ['done', 'Terminées / historique', ['completed', 'refused', 'cancelled']],
];

export default async function Page({ searchParams }: { searchParams: Promise<{ s?: string }> }) {
  const { token } = await requireRole('provider');
  const { s } = await searchParams;
  const all = await getMyMissions(token);
  const f = TABS.find((t) => t[0] === (s ?? ''))?.[2] ?? [];
  const rows = f.length ? all.filter((m) => f.includes(m.status)) : all;
  return (
    <>
      <PageHeader title="Mes missions" subtitle="Vous ne voyez que les missions qui vous sont attribuées." />
      <div className="mb-4 flex flex-wrap gap-2">
        {TABS.map(([k, l]) => <Link key={k} href={k ? `/espace/missions?s=${k}` : '/espace/missions'} className={`btn btn-sm ${(s ?? '') === k ? 'btn-green' : 'btn-outline'}`}>{l}</Link>)}
      </div>
      {rows.length === 0 ? <Empty>Aucune mission.</Empty> : (
        <Table>
          <thead><tr><th>Mission</th><th>Échéance</th><th>Rémunération</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            {rows.map((m) => (
              <tr key={m.id}>
                <td className="font-semibold">{m.title}</td>
                <td>{fdate(m.dueDate)} {m.late && <span className="badge badge-red">En retard</span>}</td>
                <td>{m.compensationMode === 'exchange' ? 'Échange de services' : fcfa(m.providerFee)}</td>
                <td><StatusBadge meta={MISSION_STATUS} value={m.status} />{m.validatedAt && <span className="badge badge-green ml-1">Validée</span>}</td>
                <td><Link href={`/espace/missions/${m.id}`} className="btn btn-outline btn-sm">Détail</Link></td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
    </>
  );
}
