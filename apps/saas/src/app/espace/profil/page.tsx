import { getMyProvider } from '@menewa/api-client';
import { ActionForm, Empty, fcfa, PageHeader, PROVIDER_CATEGORIES, ROLES, Section } from '@menewa/ui';
import { requireUser } from '@/lib/session';
import { addProviderService, removeProviderService, updateProfile } from '../actions';

export default async function Page() {
  const { token, user: u } = await requireUser();
  const provider = u.role === 'provider' ? await getMyProvider(token) : null;
  const pp = provider?.profile;
  const prefs = u.notifPrefs ?? { inapp: true, sms: true, email: false };
  return (
    <>
      <PageHeader title="Profil & paramètres du compte" subtitle={`${ROLES[u.role]} · ${u.phone}`} />
      <div className="grid gap-5 lg:grid-cols-2">
        <Section title="Informations personnelles">
          <ActionForm action={updateProfile}>
            <div><label className="label" htmlFor="name">Nom complet</label><input id="name" name="name" defaultValue={u.name} className="input" required /></div>
            <div className="grid gap-3 sm:grid-cols-2">
              <div><label className="label" htmlFor="email">E-mail</label><input id="email" name="email" type="email" defaultValue={u.email ?? ''} className="input" /></div>
              <div><label className="label" htmlFor="zone">Zone / ville</label><input id="zone" name="zone" defaultValue={u.zone ?? ''} className="input" /></div>
            </div>
            {u.role === 'partner' && <div><label className="label" htmlFor="organization">Organisation</label><input id="organization" name="organization" defaultValue={u.organization ?? ''} className="input" /></div>}
            {pp && (
              <>
                <input type="hidden" name="providerProfile" value="1" />
                <div><label className="label" htmlFor="category">Catégorie de prestataire</label><select id="category" name="category" defaultValue={pp.category} className="input">{Object.entries(PROVIDER_CATEGORIES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select></div>
                <div><label className="label" htmlFor="description">Présentation de vos services</label><textarea id="description" name="description" defaultValue={pp.description ?? ''} rows={3} className="input" /></div>
                <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="available" defaultChecked={pp.available} className="accent-[#0b5a3c]" /> Disponible pour de nouvelles missions</label>
              </>
            )}
            <div><label className="label" htmlFor="bio">À propos</label><textarea id="bio" name="bio" defaultValue={u.bio ?? ''} rows={2} className="input" /></div>
            <fieldset className="rounded-xl border border-slate-200 p-3">
              <legend className="px-1 text-xs font-bold uppercase text-slate-500">Canaux de notification</legend>
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="inapp" defaultChecked={prefs.inapp} className="accent-[#0b5a3c]" /> Dans l’application</label>
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="sms" defaultChecked={prefs.sms} className="accent-[#0b5a3c]" /> SMS (événements importants)</label>
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="email_pref" defaultChecked={prefs.email} className="accent-[#0b5a3c]" /> E-mail (canal à brancher)</label>
            </fieldset>
          </ActionForm>
        </Section>
        {provider && (
          <div className="space-y-5">
            <Section title="Mes services et tarifs">
              {provider.services.length === 0 ? <Empty>Aucun service renseigné.</Empty> : (
                <ul className="divide-y divide-slate-100 text-sm">
                  {provider.services.map((s) => (
                    <li key={s.id} className="flex items-center justify-between py-2"><span>{s.label}<br /><span className="text-xs text-slate-500">{fcfa(s.unitCost)} / {s.unit}</span></span>
                      <form action={removeProviderService}><input type="hidden" name="id" value={s.id} /><button className="text-xs text-red-600 hover:underline">Retirer</button></form></li>
                  ))}
                </ul>
              )}
              <ActionForm action={addProviderService} submit="Ajouter" className="mt-4 space-y-2" btnClass="btn btn-yellow btn-sm">
                <input name="label" className="input" placeholder="Intitulé du service" required />
                <div className="grid grid-cols-3 gap-2"><select name="category" className="input" aria-label="Catégorie">{Object.entries(PROVIDER_CATEGORIES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select><input name="unitCost" type="number" min={0} className="input" placeholder="Coût" /><input name="unit" className="input" placeholder="Unité" /></div>
              </ActionForm>
            </Section>
            <Section title={`Avis reçus (${provider.reviewCount})`}>
              {provider.reviews.length === 0 ? <Empty>Pas encore d’avis.</Empty> : <ul className="space-y-2 text-sm">{provider.reviews.map((r) => <li key={r.id}>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)} — {r.comment}</li>)}</ul>}
            </Section>
          </div>
        )}
      </div>
    </>
  );
}
