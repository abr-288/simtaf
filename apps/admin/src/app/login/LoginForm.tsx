'use client';

import { useActionState } from 'react';
import { adminLoginAction, type AdminLoginState } from './actions';

export function LoginForm() {
  const [state, action, pending] = useActionState<AdminLoginState, FormData>(adminLoginAction, { step: 'phone' });
  return (
    <form action={action} className="card space-y-4">
      <div>
        <p className="text-[10px] font-semibold uppercase tracking-widest text-brand">Console d’administration</p>
        <h1 className="h-title mt-1">{state.step === 'phone' ? 'Connexion administrateur' : 'Vérifiez votre numéro'}</h1>
      </div>
      {state.step === 'phone' ? (
        <>
          <div><label className="label" htmlFor="phone">Téléphone administrateur</label><input id="phone" name="phone" type="tel" className="input" placeholder="+228 90 00 00 00" defaultValue={state.phone} required /></div>
          <button name="step" value="phone" disabled={pending} className="btn btn-green w-full">{pending ? 'Envoi…' : 'Recevoir un code'}</button>
        </>
      ) : (
        <>
          <input type="hidden" name="phone" value={state.phone ?? ''} />
          <input type="hidden" name="step" value="verify" />
          <p className="text-sm text-slate-600">Code envoyé au <strong>{state.phone}</strong>.</p>
          {state.devCode && <p className="rounded-xl bg-cream p-3 text-sm">Mode démonstration — code : <strong className="font-mono text-lg">{state.devCode}</strong></p>}
          <div><label className="label" htmlFor="code">Code à 6 chiffres</label><input id="code" name="code" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} autoComplete="one-time-code" className="input text-center font-mono text-lg tracking-[0.4em]" required /></div>
          <button disabled={pending} className="btn btn-green w-full">{pending ? 'Vérification…' : 'Vérifier'}</button>
        </>
      )}
      {state.error && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{state.error}</p>}
    </form>
  );
}
