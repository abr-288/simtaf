import { getContactMessages, getSettings, getSmsOverview } from '@menewa/api-client';
import { ActionForm, fdatetime, PageHeader, Section, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { saveSetting } from '../actions';

const SMS_STATUS: Record<string, string> = { pending: 'En attente', sending: 'En cours', sent: 'Envoyé', failed: 'Échec' };

export default async function Page() {
  const { token } = await requirePerm('settings.manage');
  const [rows, msgs, sms] = await Promise.all([getSettings(token), getContactMessages(token), getSmsOverview(token)]);
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
