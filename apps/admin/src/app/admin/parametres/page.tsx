import { getContactMessages, getSettings } from '@menewa/api-client';
import { ActionForm, fdatetime, PageHeader, Section } from '@menewa/ui';
import { requirePerm } from '@/lib/session';
import { saveSetting } from '../actions';

export default async function Page() {
  const { token } = await requirePerm('settings.manage');
  const [rows, msgs] = await Promise.all([getSettings(token), getContactMessages(token)]);
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
