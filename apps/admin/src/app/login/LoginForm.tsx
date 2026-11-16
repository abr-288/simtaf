'use client';

import { useActionState } from 'react';
import { adminLoginAction, type AdminLoginState } from './actions';

export function LoginForm() {
  const [state, action, pending] = useActionState<AdminLoginState, FormData>(adminLoginAction, { step: 'phone' });
  return <form action={action} className="login-form">
    <h1>{state.step === 'phone' ? 'Administration Menewa' : 'Vérifiez votre numéro'}</h1>
    {state.step === 'phone' ? <>
      <label>Téléphone administrateur<input name="phone" type="tel" placeholder="+228 90 00 00 00" defaultValue={state.phone} required /></label>
      <button name="step" value="phone" disabled={pending}>Recevoir un code</button>
    </> : <>
      <input type="hidden" name="phone" value={state.phone ?? ''} />
      <input type="hidden" name="step" value="verify" />
      <p>Code envoyé au {state.phone}. {state.devCode && <strong>Code de démonstration : {state.devCode}</strong>}</p>
      <label>Code à 6 chiffres<input name="code" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} autoComplete="one-time-code" required /></label>
      <button disabled={pending}>Vérifier</button>
    </>}
    {state.error && <p role="alert" className="login-error">{state.error}</p>}
  </form>;
}
