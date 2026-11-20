import Link from 'next/link';
import { notFound } from 'next/navigation';
import { ApiRequestError, getDispute, getUserOptions } from '@menewa/api-client';
import { ActionForm, DISPUTE_STATUS, DisputeThread, Section } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { postDisputeMessage, updateDispute } from '../../actions';

export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { token, user } = await requirePerm('disputes.manage');
  const id = parseInt((await params).id, 10);
  if (!Number.isFinite(id)) notFound();
  const [detail, admins] = await Promise.all([
    getDispute(token, id).catch((e) => { if (e instanceof ApiRequestError && e.status === 404) notFound(); throw e; }),
    getUserOptions(token, ['admin', 'subadmin']),
  ]);
  const d = detail.dispute;
  return (
    <DisputeThread detail={detail} viewerId={user.id} messageAction={postDisputeMessage}>
      <Section title="Traitement">
        {d.requestId && <p className="mb-2 text-sm"><Link className="text-brand underline" href={`/admin/demandes/${d.requestId}`}>Voir la demande associée</Link></p>}
        <ActionForm action={updateDispute} submit="Enregistrer">
          <input type="hidden" name="disputeId" value={id} />
          <select name="status" defaultValue={d.status} className="input" aria-label="Statut">{Object.entries(DISPUTE_STATUS).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}</select>
          <select name="responsibleId" defaultValue={d.responsibleId ?? 0} className="input" aria-label="Responsable"><option value="0">Responsable : —</option>{admins.map((a) => <option key={a.id} value={a.id}>{a.name || a.phone}</option>)}</select>
          <textarea name="decision" rows={3} defaultValue={d.decision ?? ''} className="input" placeholder="Décision motivée (obligatoire pour résoudre / clore)" />
        </ActionForm>
      </Section>
    </DisputeThread>
  );
}
