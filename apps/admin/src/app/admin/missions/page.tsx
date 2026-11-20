import Link from 'next/link';
import { getAdminMissions } from '@menewa/api-client';
import { ActionForm, Empty, fcfa, fdate, MISSION_STATUS, PageHeader, PROVIDER_CATEGORIES, StatusBadge, Table } from '@menewa/ui';
import { hasPerm, requirePerm } from '@/lib/session';
import { reassignMission, validateMission } from '../actions';

export default async function Page({ searchParams }: { searchParams: Promise<{ s?: string }> }) {
  const { token, user } = await requirePerm('missions.manage');
  const { s } = await searchParams;
  const { missions, providers } = await getAdminMissions(token, s);
  return (
    <>
      <PageHeader title="Missions" subtitle="Suivi des missions, retards, refus et réattributions." />
      <div className="mb-3 flex flex-wrap gap-2">
        <Link href="/admin/missions" className={`btn btn-sm ${!s ? 'btn-green' : 'btn-outline'}`}>Toutes</Link>
        {Object.entries(MISSION_STATUS).map(([k, v]) => <Link key={k} href={`/admin/missions?s=${k}`} className={`btn btn-sm ${s === k ? 'btn-green' : 'btn-outline'}`}>{v.label}</Link>)}
      </div>
      {missions.length === 0 ? <Empty>Aucune mission.</Empty> : (
        <Table>
          <thead><tr><th>Mission</th><th>Demande</th><th>Prestataire</th><th>Échéance</th><th>Coût</th><th>Statut</th><th>Actions</th></tr></thead>
          <tbody>
            {missions.map(({ mission: m, requestRef, providerName }) => (
              <tr key={m.id}>
                <td className="font-semibold">{m.title}{m.refusalReason && <span className="block text-xs font-normal text-red-600">Refus : {m.refusalReason}</span>}</td>
                <td><Link href={`/admin/demandes/${m.requestId}`} className="font-mono text-xs text-brand underline">{requestRef}</Link></td>
                <td>{providerName || <span className="text-xs text-amber-700">Exception admin.</span>}</td>
                <td>{fdate(m.dueDate)} {m.late && <span className="badge badge-red">Retard</span>}</td>
                <td>{m.compensationMode === 'exchange' ? 'Échange' : fcfa(m.providerFee)}</td>
                <td><StatusBadge meta={MISSION_STATUS} value={m.status} />{m.validatedAt && <span className="badge badge-green ml-1">Validée</span>}</td>
                <td className="min-w-[220px]">
                  {m.status === 'refused' && (
                    <ActionForm action={reassignMission} submit="Réattribuer" className="flex gap-1" btnClass="btn btn-outline btn-sm">
                      <input type="hidden" name="missionId" value={m.id} />
                      <select name="providerId" className="input">{providers.map((p) => <option key={p.id} value={p.id}>{p.name} · {PROVIDER_CATEGORIES[p.category ?? 'autre']}</option>)}</select>
                    </ActionForm>
                  )}
                  {m.status === 'completed' && !m.validatedAt && hasPerm(user, 'proofs.review') && (
                    <ActionForm action={validateMission} submit="Valider la mission" btnClass="btn btn-green btn-sm"><input type="hidden" name="missionId" value={m.id} /></ActionForm>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
    </>
  );
}
