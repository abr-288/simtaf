'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { saveCatalogItem, setCatalogItemActive } from '@menewa/api-client';
import type { SaveCatalogItemInput } from '@menewa/shared-types';

export async function saveCatalogAction(formData: FormData) {
  const token = (await cookies()).get('menewa_admin_session')?.value;
  if (!token) redirect('/login');
  const category = String(formData.get('category') ?? 'autre');
  const input: SaveCatalogItemInput = {
    title: String(formData.get('title') ?? '').trim(), category,
    kind: category === 'action' ? 'action' : 'don',
    description: String(formData.get('description') ?? '').trim() || null,
    unitPrice: Math.max(0, Number(formData.get('unitPrice') ?? 0)),
    costEstimate: Math.max(0, Number(formData.get('costEstimate') ?? 0)),
    unit: String(formData.get('unit') ?? 'unité').trim() || 'unité',
    emoji: String(formData.get('emoji') ?? '🎁').trim() || '🎁',
    active: formData.get('active') === 'on',
  };
  try {
    const id = Number(formData.get('id') ?? 0);
    await saveCatalogItem(token, input, id || undefined);
  } catch {
    redirect('/catalogue?error=save');
  }
  redirect('/catalogue?saved=1');
}

export async function toggleCatalogAction(formData: FormData) {
  const token = (await cookies()).get('menewa_admin_session')?.value;
  if (!token) redirect('/login');
  try {
    await setCatalogItemActive(token, Number(formData.get('id')), formData.get('active') === 'true');
  } catch {
    redirect('/catalogue?error=save');
  }
  redirect('/catalogue?saved=1');
}
