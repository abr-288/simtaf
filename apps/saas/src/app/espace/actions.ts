'use server';

import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';
import * as api from '@menewa/api-client';
import type { AnnexInput, ScheduleInput } from '@menewa/shared-types';
import type { ActionState } from '@menewa/ui';
import { sessionToken } from '@/lib/session';

/* Mutations de l'espace membre : toutes passent par l'API (prix, droits et transitions vérifiés côté backend). */

const str = (fd: FormData, k: string, max = 2000) => String(fd.get(k) ?? '').trim().slice(0, max);
const int = (fd: FormData, k: string, def = 0) => {
  const n = parseInt(String(fd.get(k) ?? '').replace(/\s/g, ''), 10);
  return Number.isFinite(n) ? n : def;
};
const fileOf = (fd: FormData, k: string) => {
  const f = fd.get(k);
  return f instanceof File && f.size > 0 ? f : null;
};

async function run(fn: (token: string) => Promise<{ ok?: string } | string | void>): Promise<ActionState> {
  const token = await sessionToken();
  if (!token) return { error: 'Session expirée. Reconnectez-vous.' };
  try {
    const result = await fn(token);
    revalidatePath('/', 'layout');
    return { ok: typeof result === 'string' ? result : result?.ok ?? 'Enregistré.' };
  } catch (error) {
    return { error: error instanceof Error ? error.message : 'Erreur inattendue.' };
  }
}

async function fire(fn: (token: string) => Promise<unknown>) {
  const token = await sessionToken();
  if (!token) redirect('/login');
  try { await fn(token); } catch { /* l'état rechargé reflète le refus */ }
  revalidatePath('/', 'layout');
}

const schedule = (fd: FormData): ScheduleInput => ({
  recurrence: str(fd, 'recurrence') || 'none', occurrences: int(fd, 'occurrences', 2), startDate: str(fd, 'startDate') || undefined,
  paymentMode: str(fd, 'paymentMode') || 'immediate',
});
const annex = (fd: FormData): AnnexInput => ({
  packaging: !!fd.get('annex_packaging'), officiant: !!fd.get('annex_officiant'), photos: !!fd.get('annex_photos'), video: !!fd.get('annex_video'),
  packagingNotes: str(fd, 'annex_packaging_notes', 500), other: str(fd, 'annex_other', 500),
});

/* ───────── Panier & demandes ───────── */
export const addToCart = async (_p: ActionState, fd: FormData) =>
  run((t) => api.addToCart(t, int(fd, 'catalogItemId'), Math.min(Math.max(int(fd, 'quantity', 1), 1), 500), str(fd, 'target', 150) || undefined));
export const removeCartItem = async (fd: FormData) => fire((t) => api.removeCartItem(t, int(fd, 'id')));

export async function checkoutCart(_p: ActionState, fd: FormData): Promise<ActionState> {
  const token = await sessionToken();
  if (!token) return { error: 'Session expirée. Reconnectez-vous.' };
  let created;
  try {
    created = await api.checkoutCart(token, {
      targetName: str(fd, 'targetName', 150), targetZone: str(fd, 'targetZone', 100), description: str(fd, 'description', 1500),
      schedule: schedule(fd), annex: annex(fd), idempotencyKey: str(fd, 'idempotencyKey', 80) || undefined,
    });
  } catch (error) {
    return { error: error instanceof Error ? error.message : 'Erreur inattendue.' };
  }
  revalidatePath('/', 'layout');
  redirect(`/espace/demandes/${created.id}`);
}

export async function createCustomRequest(_p: ActionState, fd: FormData): Promise<ActionState> {
  const token = await sessionToken();
  if (!token) return { error: 'Session expirée. Reconnectez-vous.' };
  let created;
  try {
    created = await api.createCustomRequest(token, {
      kind: str(fd, 'kind'), title: str(fd, 'title', 150), description: str(fd, 'description', 2000), targetName: str(fd, 'targetName', 150),
      targetZone: str(fd, 'targetZone', 100), amount: int(fd, 'amount'), schedule: schedule(fd), annex: annex(fd),
      idempotencyKey: str(fd, 'idempotencyKey', 80) || undefined,
    });
  } catch (error) {
    return { error: error instanceof Error ? error.message : 'Erreur inattendue.' };
  }
  revalidatePath('/', 'layout');
  redirect(`/espace/demandes/${created.id}`);
}

export const cancelRequest = async (_p: ActionState, fd: FormData) => run((t) => api.cancelRequest(t, int(fd, 'requestId')));
export const answerInfoRequest = async (_p: ActionState, fd: FormData) => run((t) => api.answerInfoRequest(t, int(fd, 'requestId'), str(fd, 'answer', 1500)));
export const rateMission = async (_p: ActionState, fd: FormData) => run((t) => api.rateMission(t, int(fd, 'requestId'), int(fd, 'rating'), str(fd, 'comment', 800)));
export const openDispute = async (_p: ActionState, fd: FormData) =>
  run((t) => api.openDispute(t, int(fd, 'requestId'), str(fd, 'subject', 150), str(fd, 'description', 2000)));
export const postDisputeMessage = async (_p: ActionState, fd: FormData) =>
  run((t) => {
    const form = new FormData();
    form.set('body', str(fd, 'body', 2000));
    const file = fileOf(fd, 'attachment');
    if (file) form.set('attachment', file, file.name);
    return api.postDisputeMessage(t, int(fd, 'disputeId'), form);
  });
export const declarePayment = async (_p: ActionState, fd: FormData) =>
  run((t) => {
    const form = new FormData();
    form.set('method', str(fd, 'method'));
    form.set('reference', str(fd, 'reference', 100));
    const receipt = fileOf(fd, 'receipt');
    if (receipt) form.set('receipt', receipt, receipt.name);
    return api.declarePayment(t, int(fd, 'paymentId'), form);
  });

/* ───────── Prestataire ───────── */
export const respondMission = async (_p: ActionState, fd: FormData) =>
  run((t) => api.respondMission(t, int(fd, 'missionId'), str(fd, 'decision') === 'refuse' ? 'refuse' : 'accept', str(fd, 'reason', 600)));
export const startMission = async (_p: ActionState, fd: FormData) => run((t) => api.startMission(t, int(fd, 'missionId')));
export const completeMission = async (_p: ActionState, fd: FormData) => run((t) => api.completeMission(t, int(fd, 'missionId')));
export const uploadProof = async (_p: ActionState, fd: FormData) =>
  run((t) => {
    const form = new FormData();
    form.set('missionId', String(int(fd, 'missionId')));
    form.set('type', str(fd, 'type'));
    form.set('caption', str(fd, 'caption', 300));
    form.set('hidden', fd.get('hidden') ? 'true' : 'false');
    const file = fileOf(fd, 'file');
    if (!file) throw new Error('Sélectionnez un fichier.');
    form.set('file', file, file.name);
    return api.uploadProof(t, form);
  });
export const addProviderService = async (_p: ActionState, fd: FormData) =>
  run((t) => api.addProviderService(t, { label: str(fd, 'label', 150), category: str(fd, 'category') || 'autre', unitCost: Math.max(int(fd, 'unitCost'), 0), unit: str(fd, 'unit', 40) || 'prestation', description: str(fd, 'description', 400) }));
export const removeProviderService = async (fd: FormData) => fire((t) => api.removeProviderService(t, int(fd, 'id')));

/* ───────── Services communautaires ───────── */
export async function createServiceOrder(_p: ActionState, fd: FormData): Promise<ActionState> {
  return run((t) => api.createServiceOrder(t, {
    providerId: int(fd, 'providerId'), title: str(fd, 'title', 150), description: str(fd, 'description', 1500), zone: str(fd, 'zone', 100),
    compensationMode: str(fd, 'compensationMode') === 'exchange' ? 'exchange' : 'money', price: Math.max(int(fd, 'price'), 0),
    exchangeDescription: str(fd, 'exchangeDescription', 600),
  }));
}
export const postServiceMessage = async (_p: ActionState, fd: FormData) =>
  run((t) => api.postServiceMessage(t, int(fd, 'orderId'), str(fd, 'body', 1000), str(fd, 'price') === '' ? undefined : int(fd, 'price')));
export const serviceTransition = async (_p: ActionState, fd: FormData) =>
  run((t) => api.serviceTransition(t, int(fd, 'orderId'), str(fd, 'action'), str(fd, 'rating') === '' ? undefined : int(fd, 'rating'), str(fd, 'comment', 800)));

/* ───────── Campagnes & publications ───────── */
export const createListing = async (_p: ActionState, fd: FormData) =>
  run((t) => api.createListing(t, { type: str(fd, 'type'), title: str(fd, 'title', 150), description: str(fd, 'description', 2000), zone: str(fd, 'zone', 100), goalAmount: int(fd, 'goalAmount'), endsAt: str(fd, 'endsAt') || undefined }));
export const contribute = async (_p: ActionState, fd: FormData) =>
  run((t) => api.contributeToListing(t, int(fd, 'listingId'), int(fd, 'amount'), str(fd, 'method'), str(fd, 'reference', 100)));
export const supportListingMena = async (_p: ActionState, fd: FormData) => run((t) => api.supportListingWithMena(t, int(fd, 'listingId'), int(fd, 'amount')));

/* ───────── Token Mena ───────── */
export const transferTokens = async (_p: ActionState, fd: FormData) => run((t) => api.transferTokens(t, str(fd, 'phone', 20), int(fd, 'amount')));
export const requestConversion = async (_p: ActionState, fd: FormData) => run((t) => api.requestConversion(t, int(fd, 'amount')));

/* ───────── Profil & notifications ───────── */
export const updateProfile = async (_p: ActionState, fd: FormData) =>
  run(async (t) => {
    await api.updateProfile(t, {
      name: str(fd, 'name', 80), email: str(fd, 'email', 120), zone: str(fd, 'zone', 100), organization: str(fd, 'organization', 120), bio: str(fd, 'bio', 600),
      notifInapp: !!fd.get('inapp'), notifSms: !!fd.get('sms'), notifEmail: !!fd.get('email_pref'),
    });
    if (fd.get('providerProfile')) {
      await api.updateMyProvider(t, { category: str(fd, 'category') || 'autre', description: str(fd, 'description', 800), zone: str(fd, 'zone', 100), available: !!fd.get('available') });
    }
    return 'Profil mis à jour.';
  });
export const markNotificationsRead = async () => fire((t) => api.markNotificationsRead(t));
