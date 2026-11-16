'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { requestOtp, verifyOtp } from '@menewa/api-client';
import type { OtpRequestInput } from '@menewa/shared-types';

export type LoginState = { step: 'phone' | 'code'; phone?: string; mode?: 'login' | 'register'; name?: string; role?: string; devCode?: string; error?: string };

export async function loginAction(_previous: LoginState, formData: FormData): Promise<LoginState> {
  const step = String(formData.get('step') ?? 'phone');
  const phone = String(formData.get('phone') ?? '').trim();
  const mode = String(formData.get('mode') ?? 'login') === 'register' ? 'register' : 'login';
  const name = String(formData.get('name') ?? '').trim();
  const role = String(formData.get('role') ?? 'donor');

  if (step === 'phone') {
    const input: OtpRequestInput = { phone, mode, ...(mode === 'register' ? { name, role } : {}) };
    try {
      const result = await requestOtp(input);
      return { step: 'code', phone, mode, name, role, devCode: result.devCode };
    } catch (error) {
      return { step: 'phone', phone, mode, name, role, error: error instanceof Error ? error.message : 'Demande de code impossible.' };
    }
  }

  try {
    const session = await verifyOtp(phone, String(formData.get('code') ?? '').replace(/\D/g, ''));
    const jar = await cookies();
    jar.set('menewa_saas_session', session.accessToken, {
      httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production', path: '/',
      expires: new Date(session.expiresAt),
    });
  } catch (error) {
    return { step: 'code', phone, mode, name, role, error: error instanceof Error ? error.message : 'Code invalide.' };
  }
  redirect('/');
}
