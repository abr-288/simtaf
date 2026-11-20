import { ActionForm, PageHeader, ROLES, Section } from '@menewa/ui';
import { requireAdmin } from '@/lib/session';
import { updateProfile } from '../actions';

export default async function Page() {
  const { user: u } = await requireAdmin();
  const prefs = u.notifPrefs ?? { inapp: true, sms: true, email: false };
  return (
    <>
      <PageHeader title="Mon profil" subtitle={`${ROLES[u.role]} · ${u.phone}`} />
      <div className="max-w-2xl">
        <Section title="Informations personnelles">
          <ActionForm action={updateProfile}>
            <div><label className="label" htmlFor="name">Nom complet</label><input id="name" name="name" defaultValue={u.name} className="input" required /></div>
            <div className="grid gap-3 sm:grid-cols-2">
              <div><label className="label" htmlFor="email">E-mail</label><input id="email" name="email" type="email" defaultValue={u.email ?? ''} className="input" /></div>
              <div><label className="label" htmlFor="zone">Zone / ville</label><input id="zone" name="zone" defaultValue={u.zone ?? ''} className="input" /></div>
            </div>
            <div><label className="label" htmlFor="bio">À propos</label><textarea id="bio" name="bio" defaultValue={u.bio ?? ''} rows={2} className="input" /></div>
            <fieldset className="rounded-xl border border-slate-200 p-3">
              <legend className="px-1 text-xs font-bold uppercase text-slate-500">Canaux de notification</legend>
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="inapp" defaultChecked={prefs.inapp} className="accent-[#0b5a3c]" /> Dans l’application</label>
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="sms" defaultChecked={prefs.sms} className="accent-[#0b5a3c]" /> SMS (événements importants)</label>
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="email_pref" defaultChecked={prefs.email} className="accent-[#0b5a3c]" /> E-mail (canal à brancher)</label>
            </fieldset>
          </ActionForm>
        </Section>
      </div>
    </>
  );
}
