/** Formats d'affichage et libellés communs. Les libellés viennent du référentiel du backend (GET /api/v1/reference),
 * généré dans reference.generated.ts par `pnpm contract` : ne pas les recopier ici. */
import { REFERENCE } from './reference.generated';

export const fcfa = (n: number | null | undefined) =>
  `${new Intl.NumberFormat('fr-FR').format(n ?? 0).replace(/ | /g, ' ')} FCFA`;

export const fdate = (d: Date | string | null | undefined) =>
  d ? new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';

export const fdatetime = (d: Date | string | null | undefined) =>
  d ? new Date(d).toLocaleString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—';

export type StatusMeta = Record<string, { label: string; tone: string }>;
type Labels = Record<string, string>;

const statuses = REFERENCE.statuses;
const labels = REFERENCE.labels;

export const REQUEST_STATUS: StatusMeta = statuses.request;
export const MISSION_STATUS: StatusMeta = statuses.mission;
export const PROOF_STATUS: StatusMeta = statuses.proof;
export const PAYMENT_STATUS: StatusMeta = statuses.payment;
export const DISPUTE_STATUS: StatusMeta = statuses.dispute;
export const ORDER_STATUS: StatusMeta = statuses.order;
export const LISTING_STATUS: StatusMeta = statuses.listing;
export const TOKEN_STATUS: StatusMeta = statuses.token;
export const USER_STATUS: StatusMeta = statuses.user;

export const KIND_LABEL: Labels = labels.requestKind;
export const PAYMENT_MODE: Labels = labels.paymentMode;
export const RECURRENCE: Labels = labels.recurrence;
export const CATEGORIES: Labels = labels.category;
export const PROVIDER_CATEGORIES: Labels = labels.providerCategory;
export const PROOF_TYPES: Labels = labels.proofType;
export const PAY_METHODS: Labels = labels.payMethod;
export const LISTING_TYPES: Labels = labels.listingType;
export const ANNEX_LABELS: Labels = labels.annex;
export const ROLES: Labels = labels.role;
export const SELF_REGISTER_ROLES = REFERENCE.selfRegisterRoles;

export const PERMISSIONS = labels.permission;
export type Perm = keyof typeof PERMISSIONS;

export const isAdminRole = (role: string) => role === 'admin' || role === 'subadmin';
export const isRequester = (role: string) => role === 'donor' || role === 'client';
