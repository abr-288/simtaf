import Link from 'next/link';
import { notFound } from 'next/navigation';
import { CheckCircle2, XCircle } from 'lucide-react';
import { ApiRequestError, getAdminRequest } from '@menewa/api-client';
import {
  ActionForm, ANNEX_LABELS, DISPUTE_STATUS, fcfa, fdate, fdatetime, KIND_LABEL, MISSION_STATUS, PageHeader, PAYMENT_MODE, PAYMENT_STATUS,
  PROOF_STATUS, PROOF_TYPES, PROVIDER_CATEGORIES, RECURRENCE, REQUEST_STATUS, RequestTracker, Section, StatusBadge,
} from '@menewa/ui';
import { hasPerm, requirePerm } from '@/lib/session';
import {
  cancelMission, closeRequest, createMission, decideRequest, linkBeneficiary, markInstallmentDone, markRequestCompleted, reassignMission,
  updateFinance, uploadProof,
} from '../../actions';

export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { token, user: u } = await requirePerm('requests.manage');
  const id = parseInt((await params).id, 10);
  if (!Number.isFinite(id)) notFound();
  const d = await getAdminRequest(token, id).catch((e) => { if (e instanceof ApiRequestError && e.status === 404) notFound(); throw e; });
  const r = d.request;
  const annex = r.annexServices ?? {};
  const annexKeys = Object.keys(ANNEX_LABELS).filter((k) => annex[k as keyof typeof annex]);
  const canBen = hasPerm(u, 'beneficiaries.view');
  // Conditions de clôture et textes d'aide calculés par l'API (règle métier côté backend).
  const checks = d.checks;
  const ready = checks.every((c) => c.ok);
  const provs = d.providers;

  return (
    <>
      <PageHeader title={r.title} subtitle={`${r.ref} · ${KIND_LABEL[r.kind]} · ${fdatetime(r.createdAt)}`} />
      <RequestTracker status={r.status} createdAt={r.createdAt} updatedAt={r.updatedAt} history={d.history} />
      <div className="grid gap-5 xl:grid-cols-[1.5fr_1fr]">
        <div className="space-y-5">
          <Section title="Demande">
            <dl className="grid gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
              <div><dt className="text-xs uppercase text-slate-500">Donateur</dt><dd>{d.donorName || '—'} · {d.donorPhone}</dd></div>
              <div><dt className="text-xs uppercase text-slate-500">Cible déclarée</dt><dd>{r.targetName} {r.targetZone && `(${r.targetZone})`}</dd></div>
              <div><dt className="text-xs uppercase text-slate-500">Montant</dt><dd className="font-bold text-brand">{fcfa(r.totalAmount)} <span className="text-xs font-normal text-slate-500">({fcfa(r.amountPerOccurrence)} × {r.occurrences})</span></dd></div>
              <div><dt className="text-xs uppercase text-slate-500">Paiement / fréquence</dt><dd>{PAYMENT_MODE[r.paymentMode]} · {RECURRENCE[r.recurrence]}</dd></div>
            </dl>
            {r.description && <p className="mt-3 rounded-xl bg-slate-50 p-3 text-sm">{r.description}</p>}
            <ul className="mt-3 list-disc pl-5 text-sm">{d.items.map((i) => <li key={i.id}>{i.quantity} × {i.label} ({fcfa(i.unitPrice)}){i.target ? ` — ${i.target}` : ''}</li>)}</ul>
            {annexKeys.length > 0 && <p className="mt-2 text-sm"><strong>Annexes :</strong> {annexKeys.map((k) => ANNEX_LABELS[k]).join(', ')} {annex.packaging_notes ? `· ${annex.packaging_notes}` : ''} {annex.other ? `· ${annex.other}` : ''}</p>}
            {r.rejectionReason && <p className="mt-2 text-sm text-red-700">Motif de refus : {r.rejectionReason}</p>}
            {r.infoRequest && r.status === 'info_requested' && <p className="mt-2 text-sm text-amber-800">Information demandée : {r.infoRequest}</p>}
          </Section>

          <Section title="Missions">
            {d.missions.length === 0 && <p className="text-sm text-slate-500">Aucune mission.</p>}
            <ul className="mb-4 space-y-2 text-sm">
              {d.missions.map(({ mission: m, providerName }) => (
                <li key={m.id} className="rounded-xl border border-slate-200 p-3">
                  <div className="flex flex-wrap items-center justify-between gap-2"><span className="font-semibold">{m.title}</span><StatusBadge meta={MISSION_STATUS} value={m.status} /></div>
                  <p className="text-xs text-slate-500">{providerName || `Aucun prestataire (exception : ${m.exceptionReason})`} · échéance {fdate(m.dueDate)} · {m.compensationMode === 'exchange' ? `échange : ${m.exchangeDescription}` : fcfa(m.providerFee)}</p>
                  {m.refusalReason && <p className="text-xs text-red-600">Refus : {m.refusalReason}</p>}
                  {['refused', 'proposed'].includes(m.status) && hasPerm(u, 'missions.manage') && (
                    <ActionForm action={reassignMission} submit="Réattribuer" className="mt-2 flex gap-2" btnClass="btn btn-outline btn-sm">
                      <input type="hidden" name="missionId" value={m.id} />
                      <select name="providerId" className="input">{provs.map((p) => <option key={p.id} value={p.id}>{p.name} ({PROVIDER_CATEGORIES[p.category ?? 'autre']})</option>)}</select>
                    </ActionForm>
                  )}
                  {['proposed', 'accepted', 'in_progress'].includes(m.status) && hasPerm(u, 'missions.manage') && (
                    <details className="mt-2">
                      <summary className="cursor-pointer text-xs font-semibold text-red-600">Annuler la mission</summary>
                      <ActionForm action={cancelMission} submit="Confirmer l’annulation" className="mt-2 flex flex-wrap gap-2" btnClass="btn btn-danger btn-sm">
                        <input type="hidden" name="missionId" value={m.id} />
                        <input name="reason" className="input flex-1" placeholder="Motif (obligatoire)" required minLength={5} aria-label="Motif de l’annulation" />
                      </ActionForm>
                    </details>
                  )}
                </li>
              ))}
            </ul>
            {['accepted', 'in_progress'].includes(r.status) && hasPerm(u, 'missions.manage') && (
              <details>
                <summary className="btn btn-yellow btn-sm cursor-pointer">+ Créer une mission</summary>
                <ActionForm action={createMission} submit="Créer la mission" className="mt-3 space-y-2">
                  <input type="hidden" name="requestId" value={id} />
                  <input name="title" className="input" placeholder="Intitulé de la mission *" required minLength={4} />
                  <textarea name="instructions" rows={2} className="input" placeholder="Instructions d’exécution" />
                  <select name="providerId" className="input"><option value="0">— Aucun (exception administrative documentée) —</option>{provs.map((p) => <option key={p.id} value={p.id}>{p.name} ({PROVIDER_CATEGORIES[p.category ?? 'autre']})</option>)}</select>
                  <input name="exceptionReason" className="input" placeholder="Si aucun prestataire : justification de l’exception" />
                  <div className="grid gap-2 sm:grid-cols-3">
                    <label className="text-xs text-slate-600">Échéance *<input name="dueDate" type="date" className="input" min={new Date().toISOString().slice(0, 10)} required /></label>
                    <label className="text-xs text-slate-600">Rémunération<select name="compensationMode" className="input"><option value="money">Paiement monétaire</option><option value="exchange">Échange de services</option></select></label>
                    <label className="text-xs text-slate-600">Coût de la prestation (FCFA) *<input name="providerFee" type="number" min={0} className="input" placeholder="ex. 5000" required /></label>
                  </div>
                  <p className="text-xs text-slate-500">* obligatoire. En paiement monétaire, le coût doit être supérieur à 0 ; en échange de services, mettez 0 et décrivez les engagements.</p>
                  <input name="exchangeDescription" className="input" placeholder="Si échange : engagements réciproques" />
                </ActionForm>
              </details>
            )}
          </Section>

          <Section title="Preuves de remise">
            {d.proofs.length === 0 ? <p className="text-sm text-slate-500">Aucune preuve déposée.</p> : (
              <ul className="space-y-1 text-sm">{d.proofs.map((p) => <li key={p.id} className="flex items-center justify-between gap-2"><a className="text-brand underline" target="_blank" href={`/files/proof/${p.id}`}>{PROOF_TYPES[p.type]} — {p.caption || p.fileName}</a><StatusBadge meta={PROOF_STATUS} value={p.status} /></li>)}</ul>
            )}
            <p className="mt-2 text-xs"><Link href="/admin/preuves" className="font-semibold text-brand underline">Contrôler les preuves →</Link></p>
            {hasPerm(u, 'proofs.review') && (
              <details className="mt-3">
                <summary className="cursor-pointer text-sm font-semibold text-brand">Déposer une preuve (exception administrative)</summary>
                <ActionForm action={uploadProof} submit="Déposer" className="mt-2 space-y-2" btnClass="btn btn-outline btn-sm">
                  <input type="hidden" name="requestId" value={id} />
                  <select name="type" className="input">{Object.entries(PROOF_TYPES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select>
                  <input name="caption" className="input" placeholder="Légende" /><input name="file" type="file" required className="input" accept="image/*,application/pdf,video/mp4" />
                </ActionForm>
              </details>
            )}
          </Section>

          {hasPerm(u, 'payments.manage') && (
            <Section title="Paiements liés" actions={<Link href="/admin/paiements" className="text-xs font-semibold text-brand">Gérer</Link>}>
              <ul className="space-y-1 text-sm">
                {d.payments.map((p) => <li key={p.id} className="flex items-center justify-between gap-2"><span>{p.direction === 'in' ? '↘ Encaissement' : '↗ Rémunération'} · {p.note} · {p.mode === 'exchange' ? 'échange de services' : fcfa(p.amountPlanned)}</span><StatusBadge meta={PAYMENT_STATUS} value={p.status} /></li>)}
              </ul>
            </Section>
          )}
        </div>

        <div className="space-y-5">
          {r.status === 'submitted' && (
            <Section title="Décision administrative">
              <ActionForm action={decideRequest} submit="Accepter la demande" className="space-y-2">
                <input type="hidden" name="requestId" value={id} /><input type="hidden" name="decision" value="accept" />
                <input name="newAmount" type="number" min={0} className="input" placeholder={`Ajuster le montant par échéance (actuel : ${r.amountPerOccurrence})`} />
                <input name="message" className="input" placeholder="Commentaire (facultatif)" />
              </ActionForm>
              <hr className="my-3" />
              <ActionForm action={decideRequest} submit="Demander des informations" btnClass="btn btn-outline btn-sm">
                <input type="hidden" name="requestId" value={id} /><input type="hidden" name="decision" value="info" />
                <textarea name="message" rows={2} className="input" placeholder="Information demandée au donateur" required />
              </ActionForm>
              <hr className="my-3" />
              <ActionForm action={decideRequest} submit="Refuser" btnClass="btn btn-danger btn-sm">
                <input type="hidden" name="requestId" value={id} /><input type="hidden" name="decision" value="reject" />
                <textarea name="message" rows={2} className="input" placeholder="Motif du refus (obligatoire, consultable par le donateur)" required />
              </ActionForm>
            </Section>
          )}

          {['accepted', 'in_progress'].includes(r.status) && (
            <Section title="Déclarer le don réalisé">
              <ul className="mb-3 space-y-2 text-sm">
                {checks.map((c) => (
                  <li key={c.label} className="flex gap-2">
                    {c.ok ? <CheckCircle2 size={18} className="mt-0.5 shrink-0 text-emerald-600" aria-label="Condition remplie" /> : <XCircle size={18} className="mt-0.5 shrink-0 text-red-600" aria-label="Condition non remplie" />}
                    <span><span className={c.ok ? 'text-slate-700' : 'font-semibold text-red-700'}>{c.label}</span>{c.help && <span className="block text-xs text-slate-500">{c.help}</span>}</span>
                  </li>
                ))}
              </ul>
              <ActionForm action={markRequestCompleted} submit="Déclarer réalisé" hideSubmit={!ready}>
                <input type="hidden" name="requestId" value={id} />
                {!ready && <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-900">Le bouton « Déclarer réalisé » apparaîtra quand toutes les conditions seront remplies. Ensuite, vous pourrez clôturer le don.</p>}
              </ActionForm>
            </Section>
          )}
          {r.status === 'completed' && (
            <Section title="Clôture"><ActionForm action={closeRequest} submit="Clôturer le don"><input type="hidden" name="requestId" value={id} /></ActionForm></Section>
          )}

          {d.installments.length > 0 && (
            <Section title="Échéancier">
              <ul className="space-y-1 text-sm">
                {d.installments.map((i) => (
                  <li key={i.id} className="flex items-center justify-between gap-2"><span>#{i.seq} · {fdate(i.dueDate)} · {fcfa(i.amount)}</span>
                    {i.status === 'done' ? <span>✅</span> : ['accepted', 'in_progress'].includes(r.status) ? <form action={markInstallmentDone}><input type="hidden" name="id" value={i.id} /><button className="btn btn-outline btn-sm">Réalisée</button></form> : <span>⏳</span>}</li>
                ))}
              </ul>
            </Section>
          )}

          {d.margin && (
            <Section title="Finances & marge brute">
              <dl className="space-y-1 text-sm">
                <div className="flex justify-between"><dt>Contribution du donateur</dt><dd>{fcfa(r.totalAmount)}</dd></div>
                <div className="flex justify-between"><dt>− Budget d’exécution</dt><dd>{fcfa(r.executionBudget)}</dd></div>
                <div className="flex justify-between"><dt>− Frais prestataires</dt><dd>{fcfa(d.margin.fees)}</dd></div>
                <div className="flex justify-between"><dt>− Autres frais</dt><dd>{fcfa(r.otherFees)}</dd></div>
                <div className={`flex justify-between border-t pt-1 font-bold ${d.margin.gross < 0 ? 'text-red-600' : 'text-brand'}`}><dt>Marge brute</dt><dd>{fcfa(d.margin.gross)} ({d.margin.pct.toFixed(1)} %)</dd></div>
                <p className="text-xs text-slate-500">Cible provisoire : {d.targetMarginPct} %</p>
              </dl>
              {hasPerm(u, 'payments.manage') && (
                <ActionForm action={updateFinance} submit="Mettre à jour" className="mt-3 grid grid-cols-2 gap-2" btnClass="btn btn-outline btn-sm">
                  <input type="hidden" name="requestId" value={id} />
                  <input name="executionBudget" type="number" min={0} defaultValue={r.executionBudget} className="input" aria-label="Budget d'exécution" />
                  <input name="otherFees" type="number" min={0} defaultValue={r.otherFees} className="input" aria-label="Autres frais" />
                </ActionForm>
              )}
            </Section>
          )}

          <Section title="Bénéficiaire">
            {!canBen ? <p className="text-sm text-slate-500">Données personnelles masquées (permission requise).</p> : (
              <>
                {d.beneficiary ? <p className="mb-2 text-sm"><strong>{d.beneficiary.fullName}</strong> · {d.beneficiary.phone} · {d.beneficiary.zone}</p> : <p className="mb-2 text-sm text-slate-500">Aucun bénéficiaire associé.</p>}
                <ActionForm action={linkBeneficiary} submit="Associer" className="flex gap-2" btnClass="btn btn-outline btn-sm">
                  <input type="hidden" name="requestId" value={id} />
                  <select name="beneficiaryId" defaultValue={r.beneficiaryId ?? 0} className="input"><option value="0">—</option>{d.beneficiaries.map((b) => <option key={b.id} value={b.id}>{b.fullName} ({b.zone})</option>)}</select>
                </ActionForm>
              </>
            )}
          </Section>

          {d.disputes.length > 0 && (
            <Section title="Litiges">{d.disputes.map((x) => <Link key={x.id} href={`/admin/litiges/${x.id}`} className="mb-1 flex items-center justify-between rounded-lg bg-slate-50 p-2 text-sm"><span>{x.ref} — {x.subject}</span><StatusBadge meta={DISPUTE_STATUS} value={x.status} /></Link>)}</Section>
          )}

          <Section title="Historique">
            <ol className="space-y-2 border-l-2 border-mint pl-4 text-sm">
              {d.history.map((h) => <li key={h.id}><StatusBadge meta={REQUEST_STATUS} value={h.status} /> <span className="text-xs text-slate-500">{fdatetime(h.createdAt)}</span><p>{h.note}</p></li>)}
            </ol>
          </Section>
        </div>
      </div>
    </>
  );
}
