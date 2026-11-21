'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { requestOtp, revokeIdentitySession, sendContact, verifyOtp } from '@menewa/api-client';
import type { OtpRequestInput } from '@menewa/shared-types';
import { clientIp } from '@/lib/client-ip';
import { consumePendingCart } from '@/lib/pending-cart';
import { SESSION_COOKIE } from '@/lib/session';

export type LoginState = { step: 'phone' | 'code'; phone?: string; mode?: 'login' | 'register'; name?: string; role?: string; devCode?: string; error?: string };

export async function loginAction(_previous: LoginState, formData: FormData): Promise<LoginState> {
  const step = String(formData.get('step') ?? 'phone');
  const phone = String(formData.get('phone') ?? '').trim();
  const mode = String(formData.get('mode') ?? 'login') === 'register' ? 'register' : 'login';
  const name = String(formData.get('name') ?? '').trim().slice(0, 80);
  const role = String(formData.get('role') ?? 'donor');

  if (step === 'phone') {
    if (mode === 'register' && name.length < 2) return { step: 'phone', phone, mode, name, role, error: 'Veuillez indiquer votre nom.' };
    const input: OtpRequestInput = { phone, mode, ...(mode === 'register' ? { name, role } : {}) };
    try {
      const result = await requestOtp(input, await clientIp());
      return { step: 'code', phone, mode, name, role, devCode: result.devCode };
    } catch (error) {
      return { step: 'phone', phone, mode, name, role, error: error instanceof Error ? error.message : 'Demande de code impossible.' };
    }
  }

  let destination = '/espace';
  try {
    const session = await verifyOtp(phone, String(formData.get('code') ?? '').replace(/\D/g, ''), await clientIp());
    if (['admin', 'subadmin'].includes(session.user.role)) {
      await revokeIdentitySession(session.accessToken).catch(() => undefined);
      return { step: 'phone', phone, mode, error: 'Les comptes administrateurs se connectent sur la console d’administration.' };
    }
    (await cookies()).set(SESSION_COOKIE, session.accessToken, {
      httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production', path: '/', expires: new Date(session.expiresAt),
    });
    // Don choisi dans le simulateur avant la connexion : ajouté au panier maintenant.
    destination = (await consumePendingCart(session.accessToken, session.user.role)) ?? destination;
  } catch (error) {
    return { step: 'code', phone, mode, name, role, error: error instanceof Error ? error.message : 'Code invalide.' };
  }
  redirect(destination);
}

export async function contactAction(_p: { ok?: string; error?: string } | null, fd: FormData): Promise<{ ok?: string; error?: string }> {
  try {
    const res = await sendContact({
      name: String(fd.get('name') ?? '').trim(), email: String(fd.get('email') ?? '').trim() || undefined,
      phone: String(fd.get('phone') ?? '').trim() || undefined, subject: String(fd.get('subject') ?? '').trim(), message: String(fd.get('message') ?? '').trim(),
    });
    return { ok: res.ok };
  } catch (error) {
    return { error: error instanceof Error ? error.message : 'Envoi impossible.' };
  }
}
