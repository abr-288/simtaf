import Link from 'next/link';
import { getAdminProofs } from '@menewa/api-client';
import { ActionForm, Empty, fdatetime, PageHeader, PROOF_STATUS, PROOF_TYPES, Section, StatusBadge } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { reviewProof } from '../actions';

export default async function Page() {
  const { token } = await requirePerm('proofs.review');
  const rows = await getAdminProofs(token);
  const pending = rows.filter((r) => r.proof.status === 'pending');
  const done = rows.filter((r) => r.proof.status !== 'pending');
  return (
    <>
      <PageHeader title="Contrôle des preuves" subtitle="Valider ou rejeter les preuves de remise (photos, reçus, factures, attestations)." />
      <Section title={`À contrôler (${pending.length})`}>
        {pending.length === 0 ? <Empty>Aucune preuve en attente.</Empty> : (
          <div className="grid gap-4 md:grid-cols-2">
            {pending.map(({ proof: p, requestRef, uploaderName }) => (
              <div key={p.id} className="rounded-xl border border-slate-200 p-3 text-sm">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                {p.mime.startsWith('image/') && <img src={`/files/proof/${p.id}`} alt={p.fileName} className="mb-2 h-44 w-full rounded-lg object-cover" />}
                <p className="font-semibold">{PROOF_TYPES[p.type]} — {p.caption || p.fileName}</p>
                <p className="text-xs text-slate-500"><Link href={`/admin/demandes/${p.requestId}`} className="text-brand underline">{requestRef}</Link> · par {uploaderName} · {fdatetime(p.createdAt)} · <a className="underline" target="_blank" href={`/files/proof/${p.id}`}>ouvrir</a></p>
                <ActionForm action={reviewProof} submit="Valider" className="mt-2 space-y-2" btnClass="btn btn-green btn-sm">
                  <input type="hidden" name="proofId" value={p.id} /><input type="hidden" name="decision" value="validate" />
                  <label className="flex items-center gap-2 text-xs"><input type="checkbox" name="hide" /> Ne pas montrer au donateur</label>
                </ActionForm>
                <ActionForm action={reviewProof} submit="Rejeter" className="mt-2 flex gap-2" btnClass="btn btn-danger btn-sm">
                  <input type="hidden" name="proofId" value={p.id} /><input type="hidden" name="decision" value="reject" />
                  <input name="reason" className="input" placeholder="Motif du rejet" required />
                </ActionForm>
              </div>
            ))}
          </div>
        )}
      </Section>
      <div className="mt-5">
        <Section title="Historique des contrôles">
          <ul className="divide-y divide-slate-100 text-sm">
            {done.map(({ proof: p, requestRef }) => <li key={p.id} className="flex items-center justify-between gap-2 py-2"><span>{requestRef} · {PROOF_TYPES[p.type]} — {p.fileName}{p.rejectionReason && <span className="text-xs text-red-600"> ({p.rejectionReason})</span>}</span><StatusBadge meta={PROOF_STATUS} value={p.status} /></li>)}
          </ul>
        </Section>
      </div>
    </>
  );
}
