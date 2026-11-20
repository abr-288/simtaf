/**
 * Vérification à la compilation : chaque type de réponse utilisé par les fronts correspond au schéma du contrat
 * OpenAPI généré depuis le backend (openapi.generated.ts). Tout champ doit exister côté backend avec le même type
 * de base (texte, nombre, booléen, objet, tableau) ; la nullabilité, plus fine côté front, n'est pas comparée.
 * Un renommage, une suppression ou un changement de type côté backend fait échouer `tsc`.
 */
import type { components } from './openapi.generated';
import type * as T from './index';

type S = components['schemas'];
type Widen<V> = V extends string ? string : V extends number ? number : V extends boolean ? boolean
  : V extends readonly unknown[] ? 'array' : V extends object ? 'object' : V;
type Same<A, B> = [A] extends [B] ? ([B] extends [A] ? true : false) : false;
type Fields<H, G> = { [K in keyof H]-?: K extends keyof G ? Same<Widen<NonNullable<H[K]>>, Widen<NonNullable<G[K]>>> : false }[keyof H];
type Matches<H, G> = false extends Fields<H, G> ? false : true;
/** En cas d'échec, survolez `Mismatch` pour voir les champs en écart. */
type Mismatch<H, G> = { [K in keyof H as K extends keyof G ? (Same<Widen<NonNullable<H[K]>>, Widen<NonNullable<G[K]>>> extends true ? never : K) : K]: H[K] };
type Expect<_ extends true> = true;

export type ContractChecks = [
  Expect<Matches<T.IdentityUser, S['IdentityUserResponse']>>,
  Expect<Matches<T.AuthSession, S['SessionResponse']>>,
  Expect<Matches<T.AdminUser, S['AdminUserView']>>,
  Expect<Matches<T.UserOption, S['UserOption']>>,
  Expect<Matches<T.CatalogItem, S['CatalogItemResponse']>>,
  Expect<Matches<T.ManagedCatalogItem, S['ManagedCatalogItemResponse']>>,
  Expect<Matches<T.Target, S['Target']>>,
  Expect<Matches<T.TargetUsage, S['TargetUsage']>>,
  Expect<Matches<T.AppNotification, S['NotificationView']>>,
  Expect<Matches<Omit<T.Setting, 'value'>, Omit<S['SettingView'], 'value'>>>,
  Expect<Matches<T.AuditEntry, S['AuditView']>>,
  Expect<Matches<T.ContactMessage, S['ContactView']>>,
  Expect<Matches<T.GiftRequest, S['RequestRow']>>,
  Expect<Matches<T.RequestItem, S['ItemRow']>>,
  Expect<Matches<T.HistoryEntry, S['HistoryRow']>>,
  Expect<Matches<T.Installment, S['InstallmentRow']>>,
  Expect<Matches<T.Mission, S['MissionRow']>>,
  Expect<Matches<T.Proof, S['ProofRow']>>,
  Expect<Matches<T.Payment, S['PaymentRow']>>,
  Expect<Matches<T.Dispute, S['DisputeRow']>>,
  Expect<Matches<T.DisputeMessage, S['DisputeMessageRow']>>,
  Expect<Matches<T.DisputeDetail, S['DisputeDetail']>>,
  Expect<Matches<T.Beneficiary, S['BeneficiaryRow']>>,
  Expect<Matches<T.Created, S['Created']>>,
  Expect<Matches<T.CartLine, S['CartLine']>>,
  Expect<Matches<T.Cart, S['Cart']>>,
  Expect<Matches<T.DonorDashboard, S['DonorDashboard']>>,
  Expect<Matches<T.MissionSummary, S['MissionSummary']>>,
  Expect<Matches<T.DonorRequestDetail, S['DonorRequestDetail']>>,
  Expect<Matches<T.ProviderDashboard, S['ProviderDashboard']>>,
  Expect<Matches<T.ProviderMissionDetail, S['ProviderMissionDetail']>>,
  Expect<Matches<T.ProviderMissionDetail['request'], S['MissionRequestInfo']>>,
  Expect<Matches<T.ReceivedGift, S['ReceivedGift']>>,
  Expect<Matches<T.AdminDashboard, S['AdminDashboard']>>,
  Expect<Matches<T.AdminRequestRow, S['AdminRequestRow']>>,
  Expect<Matches<T.ProviderOption, S['ProviderOption']>>,
  Expect<Matches<T.AdminMission, S['AdminMission']>>,
  Expect<Matches<T.Margin, S['Margin']>>,
  Expect<Matches<T.CompletionCheck, S['Check']>>,
  Expect<Matches<T.AdminRequestDetail, S['AdminRequestDetail']>>,
  Expect<Matches<T.AdminMissions, S['AdminMissions']>>,
  Expect<Matches<T.AdminProof, S['AdminProof']>>,
  Expect<Matches<T.AdminPayments, S['AdminPayments']>>,
  Expect<Matches<T.AdminPayments['payments'][number], S['AdminPayment']>>,
  Expect<Matches<T.FinanceRow, S['FinanceRow']>>,
  Expect<Matches<T.Finances, S['Finances']>>,
  Expect<Matches<T.AdminDispute, S['AdminDispute']>>,
  Expect<Matches<T.Kpi, S['Kpi']>>,
  Expect<Matches<T.Stats, S['Stats']>>,
  Expect<Matches<T.ProviderService, S['Service']>>,
  Expect<Matches<T.ProviderProfile, S['Profile']>>,
  Expect<Matches<T.Review, S['Review']>>,
  Expect<Matches<T.MyProvider, S['MyProvider']>>,
  Expect<Matches<T.ProviderCard, S['ProviderCard']>>,
  Expect<Matches<T.Level, S['Level']>>,
  Expect<Matches<T.TokenTransaction, S['TransactionView']>>,
  Expect<Matches<T.Badge, S['BadgeView']>>,
  Expect<Matches<T.MyRewards, S['MyRewards']>>,
  Expect<Matches<T.TokenRule, S['Rule']>>,
  Expect<Matches<T.RewardsOverview, S['AdminOverview']>>,
  Expect<Matches<T.RewardsOverview['transactions'][number], S['AdminTransaction']>>,
  Expect<Matches<T.RewardAccount, S['AccountView']>>,
  Expect<Matches<T.ServiceOrder, S['Order']>>,
  Expect<Matches<T.ServiceOrderView, S['OrderView']>>,
  Expect<Matches<T.ServiceOrderDetail, S['OrderDetail']>>,
  Expect<Matches<T.ServiceOrderDetail['messages'][number], S['ServiceMessageView']>>,
  Expect<Matches<T.ServiceOrderDetail['payments'][number], S['ServicePayment']>>,
  Expect<Matches<T.Listing, S['Listing']>>,
  Expect<Matches<T.ListingView, S['ListingView']>>,
  Expect<Matches<T.Contribution, S['Contribution']>>,
  Expect<Matches<T.AdminListings, S['AdminListings']>>,
  Expect<Matches<T.PartnerDashboard, S['PartnerDashboard']>>,
  Expect<Matches<T.PublicPartner, S['PublicPartner']>>,
  Expect<Matches<T.PublicListing, S['PublicListing']>>,
];

/** Utilitaire de diagnostic : `type X = Mismatch<T.GiftRequest, S['RequestRow']>` liste les champs en écart. */
export type { Mismatch };
