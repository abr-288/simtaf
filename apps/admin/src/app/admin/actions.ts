'use server';

import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';
import * as api from '@menewa/api-client';
import type { ActionState } from '@menewa/ui';
import { sessionToken } from '@/lib/session';

/* Toutes les mutations passent par l'API : aucune règle métier ni accès base ici. */

const str = (fd: FormData, k: string, max = 2000) => String(fd.get(k) ?? '').trim().slice(0, max);
const int = (fd: FormData, k: string, def = 0) => {
  const n = parseInt(String(fd.get(k) ?? '').replace(/\s/g, ''), 10);
  return Number.isFinite(n) ? n : def;
};
const optInt = (fd: FormData, k: string) => (str(fd, k) === '' ? undefined : int(fd, k));
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

/** Variante pour les formulaires sans retour d'état (boutons simples). */
async function fire(fn: (token: string) => Promise<unknown>) {
  const token = await sessionToken();
  if (!token) redirect('/login');
  try { await fn(token); } catch { /* l'état affiché après rechargement reflète le refus */ }
  revalidatePath('/', 'layout');
}

/* ───────── Demandes ───────── */
export const decideRequest = async (_p: ActionState, fd: FormData) =>
  run((t) => api.decideRequest(t, int(fd, 'requestId'), str(fd, 'decision'), str(fd, 'message', 1000), optInt(fd, 'newAmount')));
export const linkBeneficiary = async (_p: ActionState, fd: FormData) =>
  run((t) => api.linkBeneficiary(t, int(fd, 'requestId'), int(fd, 'beneficiaryId') || null));
export const updateFinance = async (_p: ActionState, fd: FormData) =>
  run((t) => api.updateRequestFinance(t, int(fd, 'requestId'), int(fd, 'executionBudget'), int(fd, 'otherFees')));
export const markInstallmentDone = async (fd: FormData) => fire((t) => api.markInstallmentDone(t, int(fd, 'id')));
export const markRequestCompleted = async (_p: ActionState, fd: FormData) => run((t) => api.markRequestCompleted(t, int(fd, 'requestId')));
export const closeRequest = async (_p: ActionState, fd: FormData) => run((t) => api.closeRequest(t, int(fd, 'requestId')));

/* ───────── Missions ───────── */
export const createMission = async (_p: ActionState, fd: FormData) =>
  run((t) => api.createMission(t, int(fd, 'requestId'), {
    title: str(fd, 'title', 200), instructions: str(fd, 'instructions', 1500), providerId: int(fd, 'providerId') || null,
    exceptionReason: str(fd, 'exceptionReason', 500), dueDate: str(fd, 'dueDate'), compensationMode: str(fd, 'compensationMode'),
    providerFee: int(fd, 'providerFee'), exchangeDescription: str(fd, 'exchangeDescription', 600),
  }));
export const cancelMission = async (_p: ActionState, fd: FormData) => run((t) => api.cancelMission(t, int(fd, 'missionId'), str(fd, 'reason', 500)));
export const reassignMission = async (_p: ActionState, fd: FormData) => run((t) => api.reassignMission(t, int(fd, 'missionId'), int(fd, 'providerId')));
export const validateMission = async (_p: ActionState, fd: FormData) => run((t) => api.validateMission(t, int(fd, 'missionId')));

/* ───────── Preuves ───────── */
export const reviewProof = async (_p: ActionState, fd: FormData) =>
  run((t) => api.reviewProof(t, int(fd, 'proofId'), str(fd, 'decision') === 'reject' ? 'reject' : 'validate', str(fd, 'reason', 500), !!fd.get('hide')));
export const uploadProof = async (_p: ActionState, fd: FormData) =>
  run((t) => {
    const form = new FormData();
    form.set('requestId', String(int(fd, 'requestId')));
    form.set('type', str(fd, 'type'));
    form.set('caption', str(fd, 'caption', 300));
    const file = fileOf(fd, 'file');
    if (!file) throw new Error('Sélectionnez un fichier.');
    form.set('file', file, file.name);
    return api.uploadProof(t, form);
  });

/* ───────── Paiements ───────── */
/* Reversement Mobile Money au prestataire (fournisseur simulé en zone de test). */
export const payoutPayment = async (_p: ActionState, fd: FormData) =>
  run(async (t) => (await api.payoutPayment(t, int(fd, 'paymentId'), str(fd, 'operator', 20), str(fd, 'phone', 30) || undefined)).message ?? 'Reversement lancé.');
export const simulatePayment = async (_p: ActionState, fd: FormData) =>
  run(async (t) => (await api.simulatePaymentTransaction(t, int(fd, 'transactionId'), str(fd, 'outcome') === 'success' ? 'success' : 'failure')).message ?? 'Enregistré.');
export const updatePayment = async (_p: ActionState, fd: FormData) =>
  run((t) => {
    const form = new FormData();
    for (const k of ['status', 'amountValidated', 'amountPaid', 'reference', 'note', 'exchangeStatus']) {
      const v = str(fd, k, 400);
      if (v !== '') form.set(k, v);
    }
    const receipt = fileOf(fd, 'receipt');
    if (receipt) form.set('receipt', receipt, receipt.name);
    return api.updatePayment(t, int(fd, 'paymentId'), form);
  });

/* ───────── Litiges ───────── */
export const updateDispute = async (_p: ActionState, fd: FormData) =>
  run((t) => api.updateDispute(t, int(fd, 'disputeId'), str(fd, 'status'), str(fd, 'decision', 2000), int(fd, 'responsibleId') || null));
export const postDisputeMessage = async (_p: ActionState, fd: FormData) =>
  run((t) => {
    const form = new FormData();
    form.set('body', str(fd, 'body', 2000));
    const file = fileOf(fd, 'attachment');
    if (file) form.set('attachment', file, file.name);
    return api.postDisputeMessage(t, int(fd, 'disputeId'), form);
  });

/* ───────── Utilisateurs, rôles, prestataires ───────── */
export const setUserStatus = async (_p: ActionState, fd: FormData) =>
  run(async (t) => { await api.setUserStatus(t, int(fd, 'userId'), str(fd, 'status')); return 'Statut mis à jour (archivage : aucune suppression définitive).'; });
export const setUserRole = async (_p: ActionState, fd: FormData) =>
  run(async (t) => {
    await api.setUserRole(t, int(fd, 'userId'), str(fd, 'role'), fd.getAll('perm').map(String));
    return 'Rôle et permissions enregistrés.';
  });
export const verifyProvider = async (fd: FormData) => fire((t) => api.setProviderVerified(t, int(fd, 'userId'), fd.get('verified') === '1'));

/* ───────── Catalogue & cibles ───────── */
export const saveCatalogItem = async (_p: ActionState, fd: FormData) =>
  run(async (t) => {
    const id = int(fd, 'id');
    await api.saveCatalogItem(t, {
      title: str(fd, 'title', 120), category: str(fd, 'category') || 'autre', description: str(fd, 'description', 1000) || null,
      unitPrice: Math.max(int(fd, 'unitPrice'), 0), costEstimate: Math.max(int(fd, 'costEstimate'), 0), unit: str(fd, 'unit', 30) || 'unité',
      emoji: str(fd, 'emoji', 12) || '🎁', active: fd.get('active') !== 'off',
    }, id || undefined);
    return 'Catalogue mis à jour.';
  });
export const saveTarget = async (_p: ActionState, fd: FormData) =>
  run(async (t) => {
    const id = str(fd, 'id', 80);
    await api.saveTarget(t, { label: str(fd, 'label', 60), description: str(fd, 'description', 200), active: fd.get('active') !== 'off' }, id || undefined);
    return id ? 'Cible enregistrée.' : 'Cible ajoutée.';
  });
export const deleteTarget = async (_p: ActionState, fd: FormData) => run((t) => api.deleteTarget(t, str(fd, 'id', 80)));

/* ───────── Token Mena ───────── */
export const saveTokenRule = async (_p: ActionState, fd: FormData) =>
  run((t) => api.saveTokenRule(t, int(fd, 'id'), { label: str(fd, 'label', 120), amount: int(fd, 'amount'), dailyCap: int(fd, 'dailyCap'), active: !!fd.get('active') }));
export const createTokenRule = async (_p: ActionState, fd: FormData) =>
  run((t) => api.createTokenRule(t, { eventKey: str(fd, 'eventKey'), label: str(fd, 'label', 120), amount: int(fd, 'amount'), dailyCap: int(fd, 'dailyCap'), active: !!fd.get('active') }));
export const deleteTokenRule = async (_p: ActionState, fd: FormData) => run((t) => api.deleteTokenRule(t, int(fd, 'id')));
export const adjustTokens = async (_p: ActionState, fd: FormData) => run((t) => api.adjustTokens(t, int(fd, 'userId'), int(fd, 'amount'), str(fd, 'reason', 300)));
export const decideTokenTx = async (fd: FormData) => fire((t) => api.decideTokenTransaction(t, int(fd, 'id'), fd.get('decision') === 'approve'));

/* ───────── Paramètres ───────── */
export const saveSetting = async (_p: ActionState, fd: FormData) =>
  run((t) => {
    let value: unknown;
    try { value = JSON.parse(str(fd, 'value', 8000)); } catch { throw new Error('Valeur JSON invalide (ex. 15, true, "texte", {"a":1}).'); }
    return api.saveSetting(t, str(fd, 'key'), value);
  });

/* ───────── Publications partenaires ───────── */
export const moderateListing = async (_p: ActionState, fd: FormData) => run((t) => api.moderateListing(t, int(fd, 'id'), str(fd, 'decision'), str(fd, 'reason', 400)));
export const decideContribution = async (_p: ActionState, fd: FormData) =>
  run((t) => api.decideContribution(t, int(fd, 'id'), str(fd, 'decision') === 'confirm' ? 'confirm' : 'reject'));

/* ───────── Bénéficiaires ───────── */
export const saveBeneficiary = async (_p: ActionState, fd: FormData) =>
  run((t) => api.createBeneficiary(t, {
    fullName: str(fd, 'fullName', 120), phone: str(fd, 'phone', 30), zone: str(fd, 'zone', 100), address: str(fd, 'address', 200),
    needs: str(fd, 'needs', 500), consent: !!fd.get('consent'),
  }));
export const archiveBeneficiary = async (fd: FormData) => fire((t) => api.archiveBeneficiary(t, int(fd, 'id'), !!fd.get('restore')));

/* ───────── Compte ───────── */
export const markNotificationsRead = async () => fire((t) => api.markNotificationsRead(t));
export const updateProfile = async (_p: ActionState, fd: FormData) =>
  run(async (t) => {
    await api.updateProfile(t, {
      name: str(fd, 'name', 80), email: str(fd, 'email', 120), zone: str(fd, 'zone', 100), organization: str(fd, 'organization', 120), bio: str(fd, 'bio', 600),
      notifInapp: !!fd.get('inapp'), notifSms: !!fd.get('sms'), notifEmail: !!fd.get('email_pref'),
    });
    return 'Profil mis à jour.';
  });

export async function goTo(fd: FormData) {
  redirect(str(fd, 'to') || '/admin');
}
