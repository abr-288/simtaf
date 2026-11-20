import Link from 'next/link';
import { getAdminDashboard } from '@menewa/api-client';
import { Empty, fcfa, fdate, PageHeader, PERMISSIONS, REQUEST_STATUS, Section, StatCard, StatusBadge, type Perm } from '@menewa/ui';
import { requireAdmin } from '@/lib/session';

export default async function Page({ searchParams }: { searchParams: Promise<{ denied?: string }> }) {
  const { token } = await requireAdmin();
  const { denied } = await searchParams;
  const d = await getAdminDashboard(token);
  return (
    <>
      <PageHeader title="Tableau de bord" subtitle="Indicateurs globaux et éléments à traiter." />
      {denied && <p className="mb-4 rounded-xl bg-red-50 p-3 text-sm text-red-700">Accès refusé : votre rôle n’inclut pas la permission « {PERMISSIONS[denied as Perm] ?? denied} ».</p>}
      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <Link href="/admin/demandes?status=submitted"><StatCard label="Demandes à traiter" value={d.toTreat} tone="amber" hint={`${d.infoRequested} en attente d’infos`} /></Link>
        <Link href="/admin/missions"><StatCard label="Missions en retard" value={d.lateMissions} tone={d.lateMissions ? 'red' : 'green'} /></Link>
        <Link href="/admin/preuves"><StatCard label="Preuves à contrôler" value={d.proofsToReview} tone="blue" /></Link>
        <Link href="/admin/paiements"><StatCard label="Paiements à suivre" value={d.paymentsToFollow} tone="amber" /></Link>
        <Link href="/admin/litiges"><StatCard label="Litiges ouverts" value={d.openDisputes} tone={d.openDisputes ? 'red' : 'green'} /></Link>
        <StatCard label="Dons réalisés" value={d.completed} />
        <StatCard label="Montants encaissés" value={fcfa(d.collected)} />
        <StatCard label="Utilisateurs" value={d.users} />
      </div>
      <Section title="Demandes à traiter" actions={<Link href="/admin/demandes" className="text-xs font-semibold text-brand">Toutes les demandes</Link>}>
        {d.recent.length === 0 ? <Empty>Aucune demande en attente. 🎉</Empty> : (
          <ul className="divide-y divide-slate-100 text-sm">
            {d.recent.map((r) => (
              <li key={r.id} className="flex items-center justify-between gap-2 py-2.5">
                <Link href={`/admin/demandes/${r.id}`} className="font-medium hover:text-brand">{r.title}<br /><span className="text-xs font-normal text-slate-500">{r.ref} · {fcfa(r.totalAmount)} · {fdate(r.createdAt)}</span></Link>
                <StatusBadge meta={REQUEST_STATUS} value={r.status} />
              </li>
            ))}
          </ul>
        )}
      </Section>
    </>
  );
}
