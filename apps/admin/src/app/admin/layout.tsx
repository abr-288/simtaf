import type { ReactNode } from 'react';
import { getUnreadCount } from '@menewa/api-client';
import { AppShell, ROLES, type NavItem, type Perm } from '@menewa/ui';
import { hasPerm, requireAdmin } from '@/lib/session';
import { logoutAction } from '../logout/actions';

export const dynamic = 'force-dynamic';

const adminNav: (NavItem & { perm?: Perm })[] = [
  { href: '/admin', label: 'Tableau de bord', icon: '📊' },
  { href: '/admin/demandes', label: 'Demandes', icon: '📋', perm: 'requests.manage' },
  { href: '/admin/missions', label: 'Missions', icon: '🧰', perm: 'missions.manage' },
  { href: '/admin/preuves', label: 'Preuves', icon: '📸', perm: 'proofs.review' },
  { href: '/admin/paiements', label: 'Paiements', icon: '💳', perm: 'payments.manage' },
  { href: '/admin/finances', label: 'Finances & marges', icon: '💰', perm: 'finances.view' },
  { href: '/admin/litiges', label: 'Litiges', icon: '⚖️', perm: 'disputes.manage' },
  { href: '/admin/utilisateurs', label: 'Utilisateurs', icon: '👥', perm: 'users.manage' },
  { href: '/admin/prestataires', label: 'Prestataires', icon: '🛠️', perm: 'providers.manage' },
  { href: '/admin/beneficiaires', label: 'Bénéficiaires', icon: '🫶', perm: 'beneficiaries.view' },
  { href: '/admin/publications', label: 'Publications partenaires', icon: '📣', perm: 'listings.moderate' },
  { href: '/admin/catalogue', label: 'Catalogue & tarifs', icon: '🎁', perm: 'catalog.manage' },
  { href: '/admin/cibles', label: 'Cibles', icon: '🎯', perm: 'catalog.manage' },
  { href: '/admin/tokens', label: 'Token Mena', icon: '🪙', perm: 'tokens.manage' },
  { href: '/admin/statistiques', label: 'Statistiques', icon: '📈', perm: 'stats.view' },
  { href: '/admin/roles', label: 'Rôles & permissions', icon: '🔐', perm: 'roles.manage' },
  { href: '/admin/audit', label: "Journal d'audit", icon: '🧾', perm: 'audit.view' },
  { href: '/admin/parametres', label: 'Paramètres', icon: '⚙️', perm: 'settings.manage' },
];

export default async function AdminLayout({ children }: { children: ReactNode }) {
  const { token, user } = await requireAdmin();
  const unread = await getUnreadCount(token).then((r) => r.count).catch(() => 0);
  const nav: NavItem[] = [
    ...adminNav.filter((i) => !i.perm || hasPerm(user, i.perm)),
    { href: '/admin/notifications', label: 'Notifications', icon: '🔔' },
    { href: '/admin/profil', label: 'Mon profil', icon: '👤' },
  ];
  return (
    <AppShell nav={nav} userName={user.name || user.phone} roleLabel={ROLES[user.role] ?? user.role} unread={unread} admin
      homeHref="/admin" notificationsHref="/admin/notifications" logoutAction={logoutAction}>
      {children}
    </AppShell>
  );
}
