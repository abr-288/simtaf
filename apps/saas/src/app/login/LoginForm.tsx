'use client';

import { useActionState } from 'react';
import { loginAction, type LoginState } from './actions';

const initialState: LoginState = { step: 'phone', mode: 'login', role: 'donor' };

export function LoginForm() {
  const [state, action, pending] = useActionState(loginAction, initialState);
  return <form action={action} className="login-form">
    <h1>{state.step === 'phone' ? 'Connexion à Menewa' : 'Vérifiez votre numéro'}</h1>
    {state.step === 'phone' ? <>
      <label>Numéro de téléphone<input name="phone" type="tel" placeholder="+228 90 00 00 00" defaultValue={state.phone} required /></label>
      <label>Action<select name="mode" defaultValue={state.mode}><option value="login">Se connecter</option><option value="register">Créer un compte</option></select></label>
      <label>Nom (pour une inscription)<input name="name" placeholder="Votre nom" defaultValue={state.name} /></label>
      <label>Profil<select name="role" defaultValue={state.role}><option value="donor">Donateur</option><option value="client">Client</option><option value="provider">Prestataire</option><option value="partner">Partenaire</option><option value="beneficiary">Bénéficiaire</option></select></label>
      <button name="step" value="phone" disabled={pending}>Recevoir un code</button>
    </> : <>
      <input type="hidden" name="phone" value={state.phone ?? ''} />
      <input type="hidden" name="mode" value={state.mode ?? 'login'} />
      <input type="hidden" name="name" value={state.name ?? ''} />
      <input type="hidden" name="role" value={state.role ?? 'donor'} />
      <p>Code envoyé au {state.phone}. {state.devCode && <strong>Code de démonstration : {state.devCode}</strong>}</p>
      <label>Code à 6 chiffres<input name="code" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} autoComplete="one-time-code" required /></label>
      <button name="step" value="verify" disabled={pending}>Vérifier et continuer</button>
      <button className="secondary" name="step" value="phone">Changer de numéro</button>
    </>}
    {state.error && <p role="alert" className="login-error">{state.error}</p>}
  </form>;
}
