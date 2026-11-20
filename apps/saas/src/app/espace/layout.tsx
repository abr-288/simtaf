import type { ReactNode } from 'react';
import { getUnreadCount } from '@menewa/api-client';
import { AppShell, ROLES, type NavItem } from '@menewa/ui';
import { requireUser } from '@/lib/session';
import { logoutAction } from '../logout/actions';

export const dynamic = 'force-dynamic';

const common: NavItem[] = [
  { href: '/espace/notifications', label: 'Notifications', icon: '🔔' },
  { href: '/espace/profil', label: 'Profil & paramètres', icon: '⚙️' },
  { href: '/espace/aide', label: 'Aide', icon: '❓' },
];

const requester: NavItem[] = [
  { href: '/espace', label: 'Tableau de bord', icon: '🏠' },
  { href: '/espace/catalogue', label: 'Catalogue de dons', icon: '🎁' },
  { href: '/espace/panier', label: 'Mon panier', icon: '🛒' },
  { href: '/espace/demandes/nouvelle', label: 'Don personnel / action', icon: '✍️' },
  { href: '/espace/demandes', label: 'Mes demandes', icon: '📋' },
  { href: '/espace/paiements', label: 'Mes paiements', icon: '💳' },
  { href: '/espace/services', label: 'Services communautaires', icon: '🤝' },
  { href: '/espace/campagnes', label: 'Campagnes & partenaires', icon: '📣' },
  { href: '/espace/tokens', label: 'Token Mena', icon: '🪙' },
];

const provider: NavItem[] = [
  { href: '/espace', label: 'Tableau de bord', icon: '🏠' },
  { href: '/espace/missions', label: 'Mes missions', icon: '🧰' },
  { href: '/espace/services', label: 'Demandes de services', icon: '🤝' },
  { href: '/espace/paiements', label: 'Rémunérations', icon: '💳' },
  { href: '/espace/tokens', label: 'Token Mena', icon: '🪙' },
];

const partner: NavItem[] = [
  { href: '/espace', label: 'Tableau de bord', icon: '🏠' },
  { href: '/espace/publications', label: 'Mes publications', icon: '📣' },
  { href: '/espace/campagnes', label: 'Campagnes', icon: '🌍' },
  { href: '/espace/tokens', label: 'Token Mena', icon: '🪙' },
];

const beneficiary: NavItem[] = [{ href: '/espace', label: 'Dons reçus', icon: '🏠' }];

export default async function EspaceLayout({ children }: { children: ReactNode }) {
  const { token, user } = await requireUser();
  const unread = await getUnreadCount(token).then((r) => r.count).catch(() => 0);
  const base = user.role === 'provider' ? provider : user.role === 'partner' ? partner : user.role === 'beneficiary' ? beneficiary : requester;
  return (
    <AppShell nav={[...base, ...common]} userName={user.name || user.phone} roleLabel={ROLES[user.role] ?? user.role} unread={unread} admin={false}
      homeHref="/espace" notificationsHref="/espace/notifications" logoutAction={logoutAction}>
      {children}
    </AppShell>
  );
}
