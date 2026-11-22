export type { PublicListing, PublicPartner } from '@menewa/shared-types';
import type {
  AdminDashboard, AdminDispute, AdminListings, AdminMissions, AdminPayments, AdminProof, AdminRequestDetail, AdminRequestRow, AdminUser,
  AppNotification, AuditEntry, AuthSession, Beneficiary, Cart, CatalogItem, CheckoutInput, ContactMessage, Created,
  CreateServiceOrderInput, CustomRequestInput, DisputeDetail, DonorDashboard, DonorRequestDetail, Finances, GiftRequest, HealthStatus,
  IdentityUser, ListingView, ManagedCatalogItem, Mission, MyProvider, MyRewards, Ok, OtpRequestInput, OtpRequestResult, PartnerDashboard,
  Payment, ProfileUpdate, PublicListing, PublicPartner, ProviderCard, ProviderDashboard, ProviderMissionDetail, ProviderOption, PublicSettings, ReceivedGift,
  RewardAccount, RewardsOverview, SmsOverview, SaveCatalogItemInput, ServiceOrderDetail, ServiceOrderView, Setting, StatFilters, Stats, Target,
  TargetUsage, UserOption, PaymentTransaction, PaymentTransactionView, OnlinePaymentConfig,
} from '@menewa/shared-types';

const apiBaseUrl = process.env.API_BASE_URL ?? process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080/api';

/** Messages utilisateur pour les codes d'erreur renvoyés par le module identity. */
const ERROR_MESSAGES: Record<string, string> = {
  account_exists: 'Ce numéro possède déjà un compte. Connectez-vous.',
  account_not_found: "Aucun compte pour ce numéro. Inscrivez-vous d'abord.",
  account_unavailable: "Ce compte est suspendu ou archivé. Contactez l'administration.",
  name_required: 'Veuillez indiquer votre nom.',
  otp_rate_limited: 'Trop de demandes de code. Réessayez dans quelques minutes.',
  otp_ip_rate_limited: 'Trop de tentatives depuis votre connexion. Réessayez dans une heure.',
  sms_unavailable: 'Le SMS n’a pas pu être envoyé. Vérifiez le numéro ou réessayez dans quelques minutes.',
  otp_expired_or_missing: 'Code expiré ou introuvable. Demandez un nouveau code.',
  otp_attempts_exceeded: 'Trop de tentatives. Demandez un nouveau code.',
  otp_invalid: 'Code incorrect.',
  invalid_phone: 'Numéro invalide. Exemple : +228 90 00 00 00',
  unauthenticated: 'Session expirée. Reconnectez-vous.',
  session_expired: 'Session expirée. Reconnectez-vous.',
  forbidden: 'Permission insuffisante pour cette action.',
  invalid_catalog_item: 'Intitulé et prix valides requis.',
  catalog_item_not_found: 'Article introuvable.',
};

export class ApiRequestError extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
  }
}

/** `clientIp` : adresse du visiteur, relayée à l'API (limite des demandes de code par IP) quand l'appel part du serveur Next.js. */
type RequestOptions = { token?: string; method?: string; body?: unknown; form?: FormData; raw?: boolean; clientIp?: string };

function describe(status: number, body: Record<string, unknown>): string {
  const detail = String(body.detail ?? body.message ?? body.error ?? '');
  if (ERROR_MESSAGES[detail]) return ERROR_MESSAGES[detail];
  if (detail && detail !== 'Invalid request content.' && !/^(Bad Request|Forbidden|Unauthorized|Not Found)$/.test(detail)) return detail;
  if (status === 401) return 'Session expirée. Reconnectez-vous.';
  if (status === 403) return 'Permission insuffisante pour cette action.';
  if (status === 404) return 'Élément introuvable.';
  if (status === 413) return 'Fichier trop volumineux (8 Mo maximum).';
  return status >= 500 ? 'Le service est momentanément indisponible. Réessayez.' : 'Requête invalide. Vérifiez les valeurs saisies.';
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (options.token) headers.Authorization = `Bearer ${options.token}`;
  if (options.clientIp) headers['X-Forwarded-For'] = options.clientIp;
  let body: BodyInit | undefined;
  if (options.form) body = options.form;
  else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(options.body);
  }
  const response = await fetch(`${apiBaseUrl}${path}`, { method: options.method ?? (body ? 'POST' : 'GET'), headers, body, cache: 'no-store' });
  if (options.raw) {
    if (!response.ok) throw new ApiRequestError(describe(response.status, {}), response.status);
    return response as unknown as T;
  }
  const text = await response.text();
  let parsed: unknown = {};
  try { parsed = text ? JSON.parse(text) : {}; } catch { parsed = { detail: text }; }
  if (!response.ok) throw new ApiRequestError(describe(response.status, parsed as Record<string, unknown>), response.status);
  return parsed as T;
}

const qs = (params: Record<string, string | number | undefined | null>) => {
  const entries = Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '');
  return entries.length ? `?${new URLSearchParams(entries.map(([k, v]) => [k, String(v)])).toString()}` : '';
};

/* ───────── Santé ───────── */
export const getHealth = () => request<HealthStatus>('/health');

/* ───────── Identité ───────── */
export const requestOtp = (input: OtpRequestInput, clientIp?: string) => request<OtpRequestResult>('/auth/otp/request', { body: input, clientIp });
export const verifyOtp = (phone: string, code: string, clientIp?: string) => request<AuthSession>('/auth/otp/verify', { body: { phone, code }, clientIp });
export const getIdentityUser = (token: string) => request<IdentityUser>('/auth/me', { token });
export const updateProfile = (token: string, input: ProfileUpdate) => request<IdentityUser>('/auth/me', { token, method: 'PUT', body: input });
export const getIdentityPermissions = (token: string) => request<string[]>('/auth/permissions', { token });
export const revokeIdentitySession = (token: string) => request<void>('/auth/logout', { token, method: 'POST' });
export const getAdminUsers = (token: string, q?: string, role?: string) => request<AdminUser[]>(`/v1/admin/users${qs({ q, role })}`, { token });
export const getUserOptions = (token: string, roles: string[] = []) =>
  request<UserOption[]>(`/v1/admin/users/options${roles.length ? `?${roles.map((r) => `role=${encodeURIComponent(r)}`).join('&')}` : ''}`, { token });
export const setUserStatus = (token: string, id: number, status: string) => request<AdminUser>(`/v1/admin/users/${id}/status`, { token, method: 'PATCH', body: { status } });
export const setUserRole = (token: string, id: number, role: string, permissions: string[]) =>
  request<AdminUser>(`/v1/admin/users/${id}/role`, { token, method: 'PUT', body: { role, permissions } });

/* ───────── Catalogue & cibles ───────── */
export const getCatalog = () => request<{ currency: string; items: CatalogItem[] }>('/catalog');
export const getManagedCatalog = (token: string) => request<ManagedCatalogItem[]>('/admin/catalog', { token });
export const saveCatalogItem = (token: string, input: SaveCatalogItemInput, id?: number) =>
  request<ManagedCatalogItem>(id ? `/admin/catalog/${id}` : '/admin/catalog', { token, method: id ? 'PUT' : 'POST', body: input });
export const setCatalogItemActive = (token: string, id: number, active: boolean) =>
  request<ManagedCatalogItem>(`/admin/catalog/${id}/active`, { token, method: 'PATCH', body: { active } });
export const getTargets = () => request<Target[]>('/v1/catalog/targets');
export const getAllTargets = (token: string) => request<Target[]>('/v1/admin/catalog/targets', { token });
export const saveTarget = (token: string, input: Omit<Target, 'id'>, id?: string) =>
  request<Target>(id ? `/v1/admin/catalog/targets/${encodeURIComponent(id)}` : '/v1/admin/catalog/targets', { token, method: id ? 'PUT' : 'POST', body: input });
export const deleteTarget = (token: string, id: string) => request<Ok>(`/v1/admin/catalog/targets/${encodeURIComponent(id)}`, { token, method: 'DELETE' });
export const getTargetUsage = (token: string) => request<TargetUsage[]>('/v1/admin/requests/target-usage', { token });

/* ───────── Plateforme ───────── */
export const getNotifications = (token: string, limit = 100) => request<AppNotification[]>(`/v1/notifications?limit=${limit}`, { token });
export const getSmsOverview = (token: string) => request<SmsOverview>('/v1/admin/sms', { token });
export const getUnreadCount = (token: string) => request<{ count: number }>('/v1/notifications/unread-count', { token });
export const markNotificationsRead = (token: string) => request<{ ok: boolean }>('/v1/notifications/read-all', { token, method: 'POST' });
export const getPublicSettings = () => request<PublicSettings>('/v1/settings/public');
export const getSettings = (token: string) => request<Setting[]>('/v1/admin/settings', { token });
export const saveSetting = (token: string, key: string, value: unknown) =>
  request<Ok>(`/v1/admin/settings/${encodeURIComponent(key)}`, { token, method: 'PUT', body: { value } });
export type AuditFilters = { q?: string; action?: string; actorName?: string; from?: string; to?: string };
export const getAudit = (token: string, filters: AuditFilters = {}) => request<AuditEntry[]>(`/v1/admin/audit${qs(filters)}`, { token });
export const sendContact = (input: { name: string; email?: string; phone?: string; subject: string; message: string }) => request<Ok>('/v1/contact', { body: input });
export const getContactMessages = (token: string) => request<ContactMessage[]>('/v1/admin/contact-messages', { token });

/* ───────── Donateur / client ───────── */
export const getCart = (token: string) => request<Cart>('/v1/cart', { token });
export const addToCart = (token: string, catalogItemId: number, quantity: number, target?: string) =>
  request<Ok>('/v1/cart', { token, body: { catalogItemId, quantity, target } });
export const removeCartItem = (token: string, id: number) => request<Ok>(`/v1/cart/${id}`, { token, method: 'DELETE' });
export const checkoutCart = (token: string, input: CheckoutInput) => request<Created>('/v1/cart/checkout', { token, body: input });
export const createCustomRequest = (token: string, input: CustomRequestInput) => request<Created>('/v1/requests', { token, body: input });
export const getMyRequests = (token: string, status?: string) => request<GiftRequest[]>(`/v1/requests${qs({ status })}`, { token });
export const getDonorDashboard = (token: string) => request<DonorDashboard>('/v1/requests/dashboard', { token });
export const getMyRequest = (token: string, id: number) => request<DonorRequestDetail>(`/v1/requests/${id}`, { token });
export const cancelRequest = (token: string, id: number) => request<Ok>(`/v1/requests/${id}/cancel`, { token, method: 'POST' });
export const answerInfoRequest = (token: string, id: number, answer: string) => request<Ok>(`/v1/requests/${id}/answer`, { token, body: { answer } });
export const rateMission = (token: string, id: number, rating: number, comment?: string) => request<Ok>(`/v1/requests/${id}/review`, { token, body: { rating, comment } });
export const openDispute = (token: string, requestId: number, subject: string, description: string) =>
  request<Ok>(`/v1/requests/${requestId}/disputes`, { token, body: { subject, description } });
export const getMyPayments = (token: string) => request<Payment[]>('/v1/payments/mine', { token });
/** `form` : method, reference, receipt (fichier facultatif). */
export const declarePayment = (token: string, id: number, form: FormData) => request<Ok>(`/v1/payments/${id}/declare`, { token, form });
/* Paiement en ligne Mobile Money (fournisseur simulé tant qu'aucun agrégateur n'est branché). */
export const getOnlinePaymentConfig = () => request<OnlinePaymentConfig>('/v1/payments/online/config');
export const payOnline = (token: string, id: number, phone: string, operator: string) =>
  request<PaymentTransactionView>(`/v1/payments/${id}/pay`, { token, body: { phone, operator } });
export const getPaymentTransactions = (token: string, id: number) => request<PaymentTransaction[]>(`/v1/payments/${id}/transactions`, { token });
export const simulatePaymentTransaction = (token: string, txId: number, outcome: 'success' | 'failure') =>
  request<PaymentTransactionView>(`/v1/payments/transactions/${txId}/simulate`, { token, body: { outcome } });
export const getDispute = (token: string, id: number) => request<DisputeDetail>(`/v1/disputes/${id}`, { token });
/** `form` : body, attachment (fichier facultatif). */
export const postDisputeMessage = (token: string, id: number, form: FormData) => request<Ok>(`/v1/disputes/${id}/messages`, { token, form });

/* ───────── Prestataire ───────── */
export const getMyMissions = (token: string) => request<Mission[]>('/v1/missions/mine', { token });
export const getProviderDashboard = (token: string) => request<ProviderDashboard>('/v1/missions/dashboard', { token });
export const getMyMission = (token: string, id: number) => request<ProviderMissionDetail>(`/v1/missions/${id}`, { token });
export const respondMission = (token: string, id: number, decision: 'accept' | 'refuse', reason?: string) =>
  request<Ok>(`/v1/missions/${id}/respond`, { token, body: { decision, reason } });
export const startMission = (token: string, id: number) => request<Ok>(`/v1/missions/${id}/start`, { token, method: 'POST' });
export const completeMission = (token: string, id: number) => request<Ok>(`/v1/missions/${id}/complete`, { token, method: 'POST' });
/** `form` : missionId ou requestId, type, caption, hidden, file. */
export const uploadProof = (token: string, form: FormData) => request<Ok>('/v1/proofs', { token, form });
export const getMyProvider = (token: string) => request<MyProvider>('/v1/providers/me', { token });
export const updateMyProvider = (token: string, input: { category: string; description?: string; zone?: string; available: boolean }) =>
  request<Ok>('/v1/providers/me', { token, method: 'PUT', body: input });
export const addProviderService = (token: string, input: { label: string; category: string; unitCost: number; unit: string; description?: string }) =>
  request<Ok>('/v1/providers/me/services', { token, body: input });
export const removeProviderService = (token: string, id: number) => request<Ok>(`/v1/providers/me/services/${id}`, { token, method: 'DELETE' });
export const searchProviders = (token: string, q?: string, cat?: string, zone?: string) => request<ProviderCard[]>(`/v1/providers/search${qs({ q, cat, zone })}`, { token });

/* ───────── Bénéficiaire ───────── */
export const getReceivedGifts = (token: string) => request<ReceivedGift[]>('/v1/beneficiary/requests', { token });

/* ───────── Token Mena ───────── */
export const getMyRewards = (token: string) => request<MyRewards>('/v1/rewards/me', { token });
export const transferTokens = (token: string, phone: string, amount: number) => request<Ok>('/v1/rewards/transfer', { token, body: { phone, amount } });
export const requestConversion = (token: string, amount: number) => request<Ok>('/v1/rewards/conversion', { token, body: { amount } });
export const getRewardsOverview = (token: string) => request<RewardsOverview>('/v1/admin/rewards', { token });
export const getRewardAccounts = (token: string) => request<RewardAccount[]>('/v1/admin/rewards/accounts', { token });
export const saveTokenRule = (token: string, id: number, input: { label: string; amount: number; dailyCap: number; active: boolean }) =>
  request<Ok>(`/v1/admin/rewards/rules/${id}`, { token, method: 'PUT', body: input });
export const createTokenRule = (token: string, input: { eventKey: string; label: string; amount: number; dailyCap: number; active: boolean }) =>
  request<Ok>('/v1/admin/rewards/rules', { token, body: input });
export const deleteTokenRule = (token: string, id: number) => request<Ok>(`/v1/admin/rewards/rules/${id}`, { token, method: 'DELETE' });
export const adjustTokens = (token: string, userId: number, amount: number, reason: string) =>
  request<Ok>('/v1/admin/rewards/adjust', { token, body: { userId, amount, reason } });
export const decideTokenTransaction = (token: string, id: number, approve: boolean) =>
  request<Ok>(`/v1/admin/rewards/transactions/${id}/decision`, { token, body: { approve } });

/* ───────── Communauté ───────── */
export const getMyServiceOrders = (token: string) => request<ServiceOrderView[]>('/v1/services/orders', { token });
export const createServiceOrder = (token: string, input: CreateServiceOrderInput) => request<Ok & { id: number }>('/v1/services/orders', { token, body: input });
export const getServiceOrder = (token: string, id: number) => request<ServiceOrderDetail>(`/v1/services/orders/${id}`, { token });
export const postServiceMessage = (token: string, id: number, body: string, price?: number) =>
  request<Ok>(`/v1/services/orders/${id}/messages`, { token, body: { body, price } });
export const serviceTransition = (token: string, id: number, action: string, rating?: number, comment?: string) =>
  request<Ok>(`/v1/services/orders/${id}/transition`, { token, body: { action, rating, comment } });
export const getPublishedListings = (token: string) => request<ListingView[]>('/v1/listings/published', { token });
export const getMyListings = (token: string) => request<ListingView[]>('/v1/listings/mine', { token });
export const getPartnerDashboard = (token: string) => request<PartnerDashboard>('/v1/listings/dashboard', { token });
export const createListing = (token: string, input: { type: string; title: string; description?: string; zone?: string; goalAmount: number; endsAt?: string }) =>
  request<Ok>('/v1/listings', { token, body: input });
export const contributeToListing = (token: string, id: number, amount: number, method: string, reference?: string) =>
  request<Ok>(`/v1/listings/${id}/contributions`, { token, body: { amount, method, reference } });
export const supportListingWithMena = (token: string, id: number, amount: number) => request<Ok>(`/v1/listings/${id}/mena`, { token, body: { amount } });
export const getAdminListings = (token: string) => request<AdminListings>('/v1/admin/listings', { token });
export const moderateListing = (token: string, id: number, decision: string, reason?: string) =>
  request<Ok>(`/v1/admin/listings/${id}/moderate`, { token, body: { decision, reason } });
export const decideContribution = (token: string, id: number, decision: 'confirm' | 'reject') =>
  request<Ok>(`/v1/admin/contributions/${id}/decision`, { token, body: { decision } });

/* ───────── Administration des dons ───────── */
export const getAdminDashboard = (token: string) => request<AdminDashboard>('/v1/admin/dashboard', { token });
export const getAdminRequests = (token: string, status?: string, q?: string) => request<AdminRequestRow[]>(`/v1/admin/requests${qs({ status, q })}`, { token });
export const getAdminRequest = (token: string, id: number) => request<AdminRequestDetail>(`/v1/admin/requests/${id}`, { token });
export const decideRequest = (token: string, id: number, decision: string, message?: string, newAmount?: number) =>
  request<Ok>(`/v1/admin/requests/${id}/decision`, { token, body: { decision, message, newAmount } });
export const linkBeneficiary = (token: string, id: number, beneficiaryId: number | null) =>
  request<Ok>(`/v1/admin/requests/${id}/beneficiary`, { token, method: 'PUT', body: { beneficiaryId } });
export const updateRequestFinance = (token: string, id: number, executionBudget: number, otherFees: number) =>
  request<Ok>(`/v1/admin/requests/${id}/finance`, { token, method: 'PUT', body: { executionBudget, otherFees } });
export const markInstallmentDone = (token: string, id: number) => request<Ok>(`/v1/admin/installments/${id}/done`, { token, method: 'POST' });
export const markRequestCompleted = (token: string, id: number) => request<Ok>(`/v1/admin/requests/${id}/complete`, { token, method: 'POST' });
export const closeRequest = (token: string, id: number) => request<Ok>(`/v1/admin/requests/${id}/close`, { token, method: 'POST' });
export const getAdminMissions = (token: string, status?: string) => request<AdminMissions>(`/v1/admin/missions${qs({ status })}`, { token });
export const createMission = (token: string, requestId: number, input: {
  title: string; instructions?: string; providerId?: number | null; exceptionReason?: string; dueDate?: string;
  compensationMode: string; providerFee: number; exchangeDescription?: string;
}) => request<Ok>(`/v1/admin/requests/${requestId}/missions`, { token, body: input });
export const cancelMission = (token: string, id: number, reason: string) => request<Ok>(`/v1/admin/missions/${id}/cancel`, { token, body: { reason } });
export const reassignMission = (token: string, id: number, providerId: number) => request<Ok>(`/v1/admin/missions/${id}/reassign`, { token, body: { providerId } });
export const validateMission = (token: string, id: number) => request<Ok>(`/v1/admin/missions/${id}/validate`, { token, method: 'POST' });
export const getAdminProofs = (token: string) => request<AdminProof[]>('/v1/admin/proofs', { token });
export const reviewProof = (token: string, id: number, decision: 'validate' | 'reject', reason?: string, hide = false) =>
  request<Ok>(`/v1/admin/proofs/${id}/review`, { token, body: { decision, reason, hide } });
export const getAdminPayments = (token: string, status?: string, direction?: string) => request<AdminPayments>(`/v1/admin/payments${qs({ status, direction })}`, { token });
export const payoutPayment = (token: string, id: number, operator: string, phone?: string) =>
  request<PaymentTransactionView>(`/v1/admin/payments/${id}/payout`, { token, body: { operator, phone } });
export const getPaymentTransactionsAdmin = (token: string) => request<PaymentTransaction[]>('/v1/admin/payment-transactions', { token });
/** `form` : status, amountValidated, amountPaid, reference, note, exchangeStatus, receipt. */
export const updatePayment = (token: string, id: number, form: FormData) => request<Ok>(`/v1/admin/payments/${id}`, { token, method: 'PUT', form });
export const getFinances = (token: string) => request<Finances>('/v1/admin/finances', { token });
export const getProviderDirectory = (token: string) => request<ProviderCard[]>('/v1/admin/providers/directory', { token });
export const getAdminDisputes = (token: string) => request<AdminDispute[]>('/v1/admin/disputes', { token });
export const updateDispute = (token: string, id: number, status: string, decision?: string, responsibleId?: number | null) =>
  request<Ok>(`/v1/admin/disputes/${id}`, { token, method: 'PUT', body: { status, decision, responsibleId } });
export const getBeneficiaries = (token: string) => request<Beneficiary[]>('/v1/admin/beneficiaries', { token });
export const createBeneficiary = (token: string, input: { fullName: string; phone?: string; zone?: string; address?: string; needs?: string; consent: boolean }) =>
  request<Ok>('/v1/admin/beneficiaries', { token, body: input });
export const archiveBeneficiary = (token: string, id: number, restore: boolean) => request<Ok>(`/v1/admin/beneficiaries/${id}/archive`, { token, body: { restore } });
export const getStats = (token: string, filters: StatFilters) => request<Stats>(`/v1/admin/stats${qs(filters)}`, { token });
export const exportStats = (token: string, filters: StatFilters) => request<Response>(`/v1/admin/stats/export${qs(filters)}`, { token, raw: true });
export const getAdminProviders = (token: string) => request<ProviderCard[]>('/v1/admin/providers', { token });
export const getProviderOptions = (token: string) => request<ProviderOption[]>('/v1/admin/providers/options', { token });
export const setProviderVerified = (token: string, id: number, verified: boolean) =>
  request<Ok>(`/v1/admin/providers/${id}/verified`, { token, method: 'PATCH', body: { verified } });

/* ───────── Site public (sans connexion) ───────── */
export const getPublicPartners = () => request<PublicPartner[]>('/v1/public/partners');
export const getPublicListings = () => request<PublicListing[]>('/v1/public/listings');
export const getPublicProviders = (q?: string, cat?: string) => request<ProviderCard[]>(`/v1/public/providers${qs({ q, cat })}`);

/* ───────── Fichiers protégés ───────── */
export const fetchProtectedFile = (token: string, kind: string, id: number) => request<Response>(`/v1/files/${kind}/${id}`, { token, raw: true });
