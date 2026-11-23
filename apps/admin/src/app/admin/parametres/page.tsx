import { getContactMessages, getHomePhotos, getSettings, getSmsOverview } from '@menewa/api-client';
import { ActionForm, fdatetime, PageHeader, Section, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { deleteHomePhoto, saveSetting, uploadHomePhoto } from '../actions';

const SMS_STATUS: Record<string, string> = { pending: 'En attente', sending: 'En cours', sent: 'Envoyé', failed: 'Échec' };

export default async function Page() {
  const { token } = await requirePerm('settings.manage');
  const [rows, msgs, sms, photos] = await Promise.all([getSettings(token), getContactMessages(token), getSmsOverview(token), getHomePhotos()]);
  return (
    <>
      <PageHeader title="Paramètres de la plateforme" subtitle="Valeurs provisoires configurables : décisions métier et réglementaires à valider. Chaque modification est auditée." />
      <div className="space-y-3">
        {rows.map((s) => (
          <div key={s.key} className="card p-4">
            <p className="font-mono text-sm font-bold text-brand-dark">{s.key}</p>
            <p className="mb-2 text-xs text-slate-500">{s.description}</p>
            <ActionForm action={saveSetting} submit="Enregistrer" className="flex flex-wrap gap-2" btnClass="btn btn-outline btn-sm">
              <input type="hidden" name="key" value={s.key} />
              <input name="value" defaultValue={JSON.stringify(s.value)} className="input flex-1 font-mono text-xs" aria-label={`Valeur de ${s.key}`} />
            </ActionForm>
          </div>
        ))}
      </div>
      <div className="mt-6">
        <Section title="Photos de l’accueil">
          <p className="mb-3 text-sm text-slate-600">Affichées sur la page d’accueil du site et, pour le bandeau principal, dans l’application mobile. JPG, PNG ou WEBP, 8 Mo maximum. Utilisez des photos dont vous avez les droits et le consentement des personnes.</p>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {photos.map((p) => (
              <div key={p.slot} className="rounded-xl border border-slate-200 p-3">
                <p className="mb-2 text-sm font-semibold text-brand-dark">{p.label}</p>
                {p.available
                  ? <img src={`/photos/${p.slot}?v=${p.version ?? 0}`} alt={p.alt ?? p.label} className="mb-2 h-32 w-full rounded-lg object-cover" />
                  : <div className="mb-2 grid h-32 place-items-center rounded-lg bg-slate-100 text-xs text-slate-500">Aucune photo</div>}
                <ActionForm action={uploadHomePhoto} submit={p.available ? 'Remplacer' : 'Téléverser'} className="space-y-2" btnClass="btn btn-green btn-sm">
                  <input type="hidden" name="slot" value={p.slot} />
                  <input name="file" type="file" accept="image/jpeg,image/png,image/webp" required className="input" aria-label={`Photo : ${p.label}`} />
                  <input name="alt" defaultValue={p.alt ?? ''} className="input" placeholder="Description (lecteurs d’écran)" aria-label="Description de la photo" />
                </ActionForm>
                {p.available && (
                  <ActionForm action={deleteHomePhoto} submit="Retirer" className="mt-2" btnClass="btn btn-outline btn-sm">
                    <input type="hidden" name="slot" value={p.slot} />
                  </ActionForm>
                )}
              </div>
            ))}
          </div>
        </Section>
      </div>
      <div className="mt-6">
        <Section title="Envois SMS">
          <p className="mb-3 text-sm text-slate-600">
            Les SMS de notification partent en arrière-plan et sont réessayés en cas d’échec (jusqu’à 5 fois).
            <span className="ml-2 font-semibold">{sms.summary.pending} en attente · {sms.summary.sent} envoyés · <span className={sms.summary.failed > 0 ? 'text-red-600' : ''}>{sms.summary.failed} en échec</span></span>
          </p>
          {sms.recent.length === 0 ? <p className="text-sm text-slate-500">Aucun SMS pour le moment.</p> : (
            <Table>
              <thead><tr><th>Créé</th><th>Destinataire</th><th>Type</th><th>Statut</th><th>Essais</th><th>Dernière erreur</th></tr></thead>
              <tbody>
                {sms.recent.map((r) => (
                  <tr key={r.id}>
                    <td className="text-xs">{fdatetime(r.createdAt)}</td><td className="font-mono text-xs">{r.phone}</td><td className="text-xs">{r.notificationType ?? '—'}</td>
                    <td><span className={`badge ${r.status === 'sent' ? 'badge-green' : r.status === 'failed' ? 'badge-red' : 'badge-amber'}`}>{SMS_STATUS[r.status] ?? r.status}</span></td>
                    <td>{r.attempts}</td><td className="max-w-xs truncate text-xs text-slate-500" title={r.lastError ?? ''}>{r.lastError ?? ''}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Section>
      </div>
      <div className="mt-6">
        <Section title="Messages de contact reçus">
          {msgs.length === 0 ? <p className="text-sm text-slate-500">Aucun message.</p> : (
            <ul className="divide-y divide-slate-100 text-sm">
              {msgs.map((m) => <li key={m.id} className="py-2"><strong>{m.subject}</strong> — {m.name} ({m.phone || m.email || '—'}) <span className="text-xs text-slate-500">{fdatetime(m.createdAt)}</span><p className="text-slate-600">{m.message}</p></li>)}
            </ul>
          )}
        </Section>
      </div>
    </>
  );
}
