/* Contrats JSON de l'API Menewa (backend Spring). Dates : chaînes ISO 8601. Montants : FCFA (XOF) entiers.
 * Types de réponse vérifiés à la compilation contre le contrat généré (contract.check.ts ← openapi.generated.ts). */
export type { components as ApiComponents, paths as ApiPaths } from './openapi.generated';

export type ApiError = { code: string; message: string };
export type HealthStatus = { status: 'UP' | 'DOWN'; service: string };
export type Ok = { ok: string };

/* ───────── Identité ───────── */
export type Role = 'donor' | 'client' | 'provider' | 'partner' | 'beneficiary' | 'subadmin' | 'admin';
export type NotificationPreferences = { inapp: boolean; sms: boolean; email: boolean };
export type IdentityUser = {
  id: number; phone: string; name: string; role: string; permissions: string[];
  status?: string; email?: string | null; zone?: string | null; country?: string | null; organization?: string | null; bio?: string | null;
  notifPrefs?: NotificationPreferences; createdAt?: string;
};
export type OtpRequestInput = { phone: string; mode: 'login' | 'register'; name?: string; role?: string };
export type OtpRequestResult = { ok: true; devCode?: string; error?: never };
export type AuthSession = { accessToken: string; tokenType: 'Bearer'; expiresAt: string; user: IdentityUser };
export type ProfileUpdate = {
  name: string; email?: string | null; zone?: string | null; organization?: string | null; bio?: string | null;
  notifInapp: boolean; notifSms: boolean; notifEmail: boolean;
};
export type AdminUser = {
  id: number; phone: string; name: string; role: string; status: string; permissions: string[]; zone: string | null;
  organization: string | null; createdAt: string; lastLoginAt: string | null;
};
export type UserOption = { id: number; name: string; phone: string; role: string };

/* ───────── Catalogue ───────── */
export type CatalogItem = { id: number; slug: string; title: string; category: string; kind: string; description: string | null; unitPrice: number; unit: string; emoji: string | null };
export type ManagedCatalogItem = CatalogItem & { costEstimate: number; active: boolean; createdAt: string };
export type SaveCatalogItemInput = Omit<ManagedCatalogItem, 'id' | 'slug' | 'createdAt' | 'kind'> & { kind?: string };
export type Target = { id: string; label: string; description: string; active: boolean };
export type TargetUsage = { target: string; count: number };

/* ───────── Plateforme ───────── */
export type AppNotification = { id: number; type: string; title: string; body: string | null; link: string | null; readAt: string | null; createdAt: string };
export type Setting = { key: string; value: unknown; description: string | null; updatedBy: number | null; updatedAt: string };
export type AuditEntry = {
  id: number; actorId: number | null; actorRole: string | null; actorName: string | null; action: string; entity: string | null;
  entityId: string | null; details: string | null; ip: string | null; createdAt: string;
};
export type ContactMessage = { id: number; name: string; email: string | null; phone: string | null; subject: string; message: string; handled: boolean; createdAt: string };
export type PublicSettings = {
  'annex.prices'?: Record<string, number>; 'feature.token_transfer'?: boolean; 'feature.token_conversion'?: boolean;
  'tokens.level_thresholds'?: Record<string, number>; 'public.stats'?: Record<string, string>; 'finance.currency'?: string;
};

/* ───────── Demandes de dons ───────── */
export type Annex = { packaging?: boolean; officiant?: boolean; photos?: boolean; video?: boolean; packaging_notes?: string; other?: string };
export type AnnexInput = { packaging: boolean; officiant: boolean; photos: boolean; video: boolean; packagingNotes?: string; other?: string };
export type ScheduleInput = { recurrence: string; occurrences?: number; startDate?: string; paymentMode: string };
export type GiftRequest = {
  id: number; ref: string; userId: number; kind: string; title: string; description: string | null; category: string;
  targetName: string | null; targetZone: string | null; beneficiaryId: number | null; partnerId: number | null; status: string;
  rejectionReason: string | null; infoRequest: string | null; amountPerOccurrence: number; occurrences: number; totalAmount: number;
  paymentMode: string; recurrence: string; startDate: string | null; annexServices: Annex | null; annexCost: number;
  executionBudget: number; otherFees: number; createdAt: string; updatedAt: string; completedAt: string | null; closedAt: string | null;
};
export type RequestItem = { id: number; requestId: number; catalogItemId: number | null; label: string; quantity: number; unitPrice: number; costEstimate: number; target: string | null };
export type HistoryEntry = { id: number; requestId: number; status: string; note: string | null; actorId: number | null; createdAt: string };
export type Installment = { id: number; requestId: number; seq: number; dueDate: string; amount: number; status: string; notifiedAt: string | null };
export type Mission = {
  id: number; requestId: number; providerId: number | null; title: string; instructions: string | null; status: string; refusalReason: string | null;
  dueDate: string | null; providerFee: number; compensationMode: string; exchangeDescription: string | null; exchangeStatus: string | null;
  exceptionReason: string | null; reassignedFrom: number | null; createdBy: number | null; createdAt: string; updatedAt: string;
  completedAt: string | null; validatedAt: string | null; late: boolean;
};
export type Proof = {
  id: number; requestId: number; missionId: number | null; type: string; fileName: string; mime: string; size: number; caption: string | null;
  uploadedBy: number; status: string; rejectionReason: string | null; reviewedBy: number | null; reviewedAt: string | null; visibleToDonor: boolean; createdAt: string;
};
export type Payment = {
  id: number; requestId: number | null; missionId: number | null; serviceOrderId: number | null; installmentId: number | null; userId: number;
  direction: 'in' | 'out'; kind: string; mode: string; method: string | null; amountPlanned: number; amountValidated: number | null; amountPaid: number;
  status: string; reference: string | null; receiptName: string | null; exchangeDescription: string | null; exchangeStatus: string | null;
  dueDate: string | null; note: string | null; createdAt: string; paidAt: string | null; hasReceipt: boolean; remaining: number;
};
export type Dispute = {
  id: number; ref: string; requestId: number | null; missionId: number | null; openedBy: number; againstUserId: number | null; subject: string;
  description: string | null; status: string; responsibleId: number | null; decision: string | null; createdAt: string; closedAt: string | null;
};
export type DisputeMessage = { id: number; disputeId: number; authorId: number; body: string; attachmentName: string | null; createdAt: string };
export type DisputeDetail = {
  dispute: Dispute; messages: { message: DisputeMessage; authorName: string | null }[];
  openedByName: string | null; againstName: string | null; responsibleName: string | null;
};
export type Beneficiary = {
  id: number; fullName: string; phone: string | null; zone: string | null; address: string | null; needs: string | null; notes: string | null;
  consent: boolean; userId: number | null; createdBy: number | null; archivedAt: string | null; createdAt: string;
};
export type Created = { id: number; ref: string };

export type CartLine = { id: number; catalogItemId: number; title: string; emoji: string | null; category: string; quantity: number; unitPrice: number; target: string | null };
export type Cart = { lines: CartLine[]; total: number; annexPrices: Record<string, number> };
export type CheckoutInput = { targetName: string; targetZone?: string; description?: string; schedule: ScheduleInput; annex: AnnexInput; idempotencyKey?: string };
export type CustomRequestInput = {
  kind: string; title: string; description?: string; targetName: string; targetZone?: string; amount: number;
  schedule: ScheduleInput; annex: AnnexInput; idempotencyKey?: string;
};

export type DonorDashboard = { pending: number; ongoing: number; done: number; proofsAvailable: number; recent: GiftRequest[] };
export type MissionSummary = { id: number; title: string; status: string; dueDate: string | null; providerName: string | null; validatedAt: string | null };
export type DonorRequestDetail = {
  request: GiftRequest; items: RequestItem[]; history: HistoryEntry[]; installments: Installment[]; payments: Payment[];
  proofs: Proof[]; missions: MissionSummary[]; disputes: Dispute[]; canReview: boolean;
};
export type ProviderDashboard = { proposed: number; accepted: number; inProgress: number; completed: number; validated: number; proposals: Mission[] };
export type ProviderMissionDetail = {
  mission: Mission;
  request: { id: number; ref: string; title: string; targetName: string | null; targetZone: string | null; annexServices: Annex | null };
  items: RequestItem[]; proofs: Proof[];
  beneficiary: { fullName: string; phone: string | null; address: string | null; zone: string | null } | null;
};
export type ReceivedGift = { id: number; title: string; status: string; createdAt: string };

/* ───────── Administration des dons ───────── */
export type AdminDashboard = {
  toTreat: number; infoRequested: number; lateMissions: number; proofsToReview: number; paymentsToFollow: number; openDisputes: number;
  users: number; completed: number; collected: number; recent: GiftRequest[];
};
export type AdminRequestRow = { request: GiftRequest; donorName: string | null; donorPhone: string | null };
export type ProviderOption = { id: number; name: string; category: string };
export type AdminMission = { mission: Mission; providerName: string | null; requestRef: string | null };
export type Margin = { fees: number; cost: number; gross: number; pct: number };
export type CompletionCheck = { ok: boolean; label: string; help: string | null };
export type AdminRequestDetail = {
  request: GiftRequest; donorName: string | null; donorPhone: string | null; items: RequestItem[]; history: HistoryEntry[];
  installments: Installment[]; payments: Payment[]; proofs: Proof[]; missions: AdminMission[]; disputes: Dispute[];
  beneficiary: Beneficiary | null; beneficiaries: Beneficiary[]; margin: Margin | null; targetMarginPct: number;
  checks: CompletionCheck[]; providers: ProviderOption[];
};
export type AdminMissions = { missions: AdminMission[]; providers: ProviderOption[] };
export type AdminProof = { proof: Proof; requestRef: string | null; uploaderName: string | null };
export type AdminPayments = { payments: { payment: Payment; userName: string | null; userPhone: string | null }[]; planned: number; paid: number };
export type FinanceRow = { id: number; ref: string; title: string; totalAmount: number; executionBudget: number; otherFees: number; partnerId: number | null; actors: number; margin: Margin };
export type Finances = { rows: FinanceRow[]; revenue: number; cost: number; gross: number; targetMarginPct: number };
export type AdminDispute = { dispute: Dispute; openedByName: string | null };
export type StatFilters = { from?: string; to?: string; status?: string; category?: string; provider?: string; zone?: string };
export type Kpi = {
  received: number; accepted: number; rejected: number; realized: number; realizationRate: number; acceptanceRate: number;
  avgDays: number; documented: number; late: number; amount: number;
};
export type Stats = { rows: { request: GiftRequest; providers: string; late: boolean }[]; kpi: Kpi };

/* ───────── Prestataires ───────── */
export type ProviderService = { id: number; providerId: number; label: string; category: string; unitCost: number; unit: string; description: string | null; active: boolean };
export type ProviderProfile = { userId: number; category: string; description: string | null; zone: string | null; available: boolean; verified: boolean };
export type Review = { id: number; kind: string; refId: number; fromUserId: number; toUserId: number; rating: number; comment: string | null; createdAt: string };
export type MyProvider = { profile: ProviderProfile; services: ProviderService[]; reviews: Review[]; reviewCount: number };
export type ProviderCard = {
  id: number; name: string; phone: string | null; zone: string | null; category: string; description: string | null; available: boolean;
  verified: boolean; rating: number; reviews: number; services: ProviderService[];
};

/* ───────── Token Mena ───────── */
export type Level = { key: string; label: string; min: number };
export type TokenTransaction = { id: number; userId: number; amount: number; type: string; eventKey: string | null; reason: string | null; status: string; createdBy: number | null; createdAt: string };
export type Badge = { code: string; label: string; emoji: string; desc: string; earned: boolean };
export type MyRewards = {
  balance: number; earned: number; level: Level; next: Level | null; levels: Level[]; transactions: TokenTransaction[]; badges: Badge[];
  transferEnabled: boolean; conversionEnabled: boolean;
};
export type TokenRule = { id: number; eventKey: string; label: string; amount: number; dailyCap: number; active: boolean };
export type RewardsOverview = {
  rules: TokenRule[]; circulating: number; manualAdjustMax: number;
  transactions: { id: number; userId: number; userName: string; amount: number; type: string; reason: string | null; status: string; createdBy: number | null; createdAt: string }[];
  events: { key: string; label: string; who: string; base: boolean }[];
};
export type RewardAccount = { userId: number; balance: number; earned: number };

/* ───────── Communauté ───────── */
export type ServiceOrder = {
  id: number; clientId: number; providerId: number; offerId: number | null; title: string; description: string | null; zone: string | null;
  proposedPrice: number; agreedPrice: number | null; compensationMode: string; exchangeDescription: string | null; status: string; createdAt: string; updatedAt: string;
};
export type ServiceOrderView = { order: ServiceOrder; clientName: string; providerName: string; iAmProvider: boolean };
export type ServiceOrderDetail = ServiceOrderView & {
  messages: { message: { id: number; orderId: number; authorId: number; body: string; price: number | null; createdAt: string }; authorName: string }[];
  payments: { id: number; status: string; mode: string; amountPlanned: number; amountPaid: number; exchangeStatus: string | null }[];
};
export type CreateServiceOrderInput = { providerId: number; title: string; description?: string; zone?: string; compensationMode: string; price: number; exchangeDescription?: string };
export type Listing = {
  id: number; ownerId: number; type: string; title: string; description: string | null; zone: string | null; goalAmount: number; raisedAmount: number;
  menaSupport: number; endsAt: string | null; status: string; rejectionReason: string | null; createdAt: string;
};
export type ListingView = { listing: Listing; ownerName: string; organization: string | null; confirmedContributions: number };
export type Contribution = { id: number; listingId: number; userId: number; amount: number; method: string | null; reference: string | null; status: string; createdAt: string };
export type AdminListings = { listings: ListingView[]; pendingContributions: { contribution: Contribution; userName: string; listingTitle: string }[] };
export type PartnerDashboard = { total: number; published: number; pending: number; raised: number };

/* ───────── Site public ───────── */
export type PublicPartner = { id: number; name: string; organization: string | null; zone: string | null };
export type PublicListing = {
  id: number; type: string; title: string; description: string | null; zone: string | null; goalAmount: number; raisedAmount: number;
  menaSupport: number; endsAt: string | null; organization: string | null;
};
