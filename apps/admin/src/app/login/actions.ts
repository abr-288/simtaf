'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { requestOtp, revokeIdentitySession, verifyOtp } from '@menewa/api-client';

export type AdminLoginState = { step: 'phone' | 'code'; phone?: string; devCode?: string; error?: string };

export async function adminLoginAction(_previous: AdminLoginState, formData: FormData): Promise<AdminLoginState> {
  const step = String(formData.get('step') ?? 'phone');
  const phone = String(formData.get('phone') ?? '').trim();
  if (step === 'phone') {
    try {
      const result = await requestOtp({ phone, mode: 'login' });
      return { step: 'code', phone, devCode: result.devCode };
    } catch (error) {
      return { step: 'phone', phone, error: error instanceof Error ? error.message : 'Demande de code impossible.' };
    }
  }

  try {
    const session = await verifyOtp(phone, String(formData.get('code') ?? '').replace(/\D/g, ''));
    if (!['admin', 'subadmin'].includes(session.user.role)) {
      await revokeIdentitySession(session.accessToken);
      return { step: 'phone', phone, error: 'Ce compte ne possède pas de droits administrateur.' };
    }
    (await cookies()).set('menewa_admin_session', session.accessToken, {
      httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production', path: '/',
      expires: new Date(session.expiresAt),
    });
  } catch (error) {
    return { step: 'code', phone, error: error instanceof Error ? error.message : 'Code invalide.' };
  }
  redirect('/');
}
