import type { ReactNode } from 'react';
import type { DisputeDetail } from '@menewa/shared-types';
import { PageHeader, Section, StatusBadge } from './components';
import { ActionForm, type ActionState } from './forms';
import { DISPUTE_STATUS, fdatetime } from './format';

/** Fil d'un litige (échanges, pièces, parties, décision), partagé par l'admin et l'espace membre. */
export function DisputeThread({ detail, viewerId, messageAction, children }: {
  detail: DisputeDetail; viewerId: number; messageAction: (prev: ActionState, fd: FormData) => Promise<ActionState>; children?: ReactNode;
}) {
  const d = detail.dispute;
  return (
    <>
      <PageHeader title={`${d.ref} — ${d.subject}`} subtitle={`Ouvert le ${fdatetime(d.createdAt)}${d.closedAt ? ` · clos le ${fdatetime(d.closedAt)}` : ''}`}
        actions={<StatusBadge meta={DISPUTE_STATUS} value={d.status} />} />
      <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
        <Section title="Échanges">
          <p className="mb-3 rounded-xl bg-slate-50 p-3 text-sm">{d.description}</p>
          <ul className="mb-4 space-y-2">
            {detail.messages.map(({ message: m, authorName }) => (
              <li key={m.id} className={`max-w-[88%] rounded-2xl px-3 py-2 text-sm ${m.authorId === viewerId ? 'ml-auto bg-mint' : 'bg-slate-100'}`}>
                <p className="text-[11px] font-semibold text-slate-500">{authorName} · {fdatetime(m.createdAt)}</p>
                <p>{m.body}</p>
                {m.attachmentName && <a href={`/files/dispute/${m.id}`} target="_blank" className="text-xs font-semibold text-brand underline">📎 {m.attachmentName}</a>}
              </li>
            ))}
          </ul>
          {d.status !== 'closed' && (
            <ActionForm action={messageAction} submit="Envoyer" btnClass="btn btn-outline btn-sm">
              <input type="hidden" name="disputeId" value={d.id} />
              <textarea name="body" rows={2} className="input" placeholder="Votre message" required />
              <input type="file" name="attachment" accept="image/*,application/pdf" className="input" aria-label="Pièce jointe" />
            </ActionForm>
          )}
        </Section>
        <div className="space-y-5">
          <Section title="Parties et décision">
            <dl className="space-y-1 text-sm">
              <div className="flex justify-between"><dt>Demandeur</dt><dd>{detail.openedByName ?? '—'}</dd></div>
              <div className="flex justify-between"><dt>Partie concernée</dt><dd>{detail.againstName ?? '—'}</dd></div>
              <div className="flex justify-between"><dt>Responsable</dt><dd>{detail.responsibleName ?? '—'}</dd></div>
            </dl>
            <p className="mt-3 rounded-xl bg-cream p-3 text-sm"><strong>Décision :</strong> {d.decision || 'En attente'}</p>
          </Section>
          {children}
        </div>
      </div>
    </>
  );
}
