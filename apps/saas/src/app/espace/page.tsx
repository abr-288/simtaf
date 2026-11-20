import Link from 'next/link';
import type { ReactNode } from 'react';
import {
  getDonorDashboard, getMyRewards, getNotifications, getPartnerDashboard, getProviderDashboard, getReceivedGifts,
} from '@menewa/api-client';
import { Empty, fcfa, fdate, fdatetime, PageHeader, REQUEST_STATUS, Section, StatCard, StatusBadge } from '@menewa/ui';
import { requireUser } from '@/lib/session';

export default async function Dashboard() {
  const { token, user: u } = await requireUser();
  const notifs = await getNotifications(token, 5);
  const header = <PageHeader title={`Bonjour ${u.name || ''} 👋`} subtitle="Voici un aperçu de votre activité sur Menewa Naya." />;
  const notifBlock: ReactNode = (
    <Section title="Dernières notifications" actions={<Link href="/espace/notifications" className="text-xs font-semibold text-brand">Tout voir</Link>}>
      {notifs.length === 0 ? <Empty>Aucune notification.</Empty> : (
        <ul className="divide-y divide-slate-100 text-sm">
          {notifs.map((n) => (
            <li key={n.id} className="py-2">
              <Link href={n.link?.startsWith('/espace') ? n.link : '/espace/notifications'} className="font-medium hover:text-brand">{!n.readAt && '● '}{n.title}</Link>
              <p className="text-xs text-slate-500">{fdatetime(n.createdAt)}</p>
            </li>
          ))}
        </ul>
      )}
    </Section>
  );

  if (u.role === 'provider') {
    const d = await getProviderDashboard(token);
    return (
      <>
        {header}
        <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
          <StatCard label="Proposées" value={d.proposed} tone="amber" />
          <StatCard label="Acceptées" value={d.accepted} tone="blue" />
          <StatCard label="En cours" value={d.inProgress} />
          <StatCard label="Terminées" value={d.completed} hint={`${d.validated} validées`} />
        </div>
        <div className="grid gap-5 lg:grid-cols-2">
          <Section title="Missions proposées" actions={<Link href="/espace/missions" className="text-xs font-semibold text-brand">Toutes mes missions</Link>}>
            {d.proposals.length === 0 ? <Empty>Aucune proposition en attente.</Empty> : (
              <ul className="divide-y divide-slate-100 text-sm">
                {d.proposals.map((m) => (
                  <li key={m.id} className="flex items-center justify-between gap-2 py-2">
                    <span>{m.title}<br /><span className="text-xs text-slate-500">Échéance {fdate(m.dueDate)}</span></span>
                    <Link href={`/espace/missions/${m.id}`} className="btn btn-yellow btn-sm">Répondre</Link>
                  </li>
                ))}
              </ul>
            )}
          </Section>
          {notifBlock}
        </div>
      </>
    );
  }

  if (u.role === 'partner') {
    const d = await getPartnerDashboard(token);
    return (
      <>
        {header}
        <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
          <StatCard label="Publications" value={d.total} />
          <StatCard label="Publiées" value={d.published} />
          <StatCard label="En modération" value={d.pending} tone="amber" />
          <StatCard label="Fonds collectés" value={fcfa(d.raised)} />
        </div>
        <div className="grid gap-5 lg:grid-cols-2">
          <Section title="Actions rapides"><Link href="/espace/publications" className="btn btn-green">Publier un don, une action ou une campagne</Link></Section>
          {notifBlock}
        </div>
      </>
    );
  }

  if (u.role === 'beneficiary') {
    const rows = await getReceivedGifts(token);
    return (
      <>
        <PageHeader title={`Bonjour ${u.name || ''} 👋`} subtitle="Les dons qui vous sont destinés. Les informations des donateurs restent confidentielles." />
        <Section title="Dons reçus">
          {rows.length === 0 ? <Empty>Aucun don pour le moment.</Empty> : (
            <ul className="divide-y divide-slate-100 text-sm">
              {rows.map((r) => (
                <li key={r.id} className="flex items-center justify-between py-2.5"><span><strong>{r.title}</strong><br /><span className="text-xs text-slate-500">{fdate(r.createdAt)}</span></span><StatusBadge meta={REQUEST_STATUS} value={r.status} /></li>
              ))}
            </ul>
          )}
        </Section>
        <div className="mt-5">{notifBlock}</div>
      </>
    );
  }

  const [d, rewards] = await Promise.all([getDonorDashboard(token), getMyRewards(token)]);
  return (
    <>
      {header}
      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="En attente" value={d.pending} tone="amber" />
        <StatCard label="En cours" value={d.ongoing} tone="blue" />
        <StatCard label="Réalisées / clôturées" value={d.done} />
        <StatCard label="Preuves disponibles" value={d.proofsAvailable} />
      </div>
      <div className="mb-6 flex flex-wrap items-center gap-4 rounded-2xl bg-gradient-to-r from-brand to-brand-light p-5 text-white">
        <span className="text-4xl">🪙</span>
        <div className="flex-1">
          <p className="text-lg font-bold">{rewards.balance} Mena · niveau {rewards.level.label}</p>
          <p className="text-sm text-white/80">{rewards.next ? `Encore ${rewards.next.min - rewards.earned} Mena gagnés pour atteindre ${rewards.next.label}` : 'Niveau maximal atteint'}</p>
        </div>
        <Link href="/espace/catalogue" className="btn btn-yellow">Faire un don</Link>
      </div>
      <div className="grid gap-5 lg:grid-cols-2">
        <Section title="Mes dernières demandes" actions={<Link href="/espace/demandes" className="text-xs font-semibold text-brand">Tout voir</Link>}>
          {d.recent.length === 0 ? <Empty>Vous n’avez pas encore de demande. <Link href="/espace/catalogue" className="font-semibold text-brand underline">Parcourir le catalogue</Link></Empty> : (
            <ul className="divide-y divide-slate-100 text-sm">
              {d.recent.map((r) => (
                <li key={r.id} className="flex items-center justify-between gap-2 py-2.5">
                  <Link href={`/espace/demandes/${r.id}`} className="font-medium hover:text-brand">{r.title}<br /><span className="text-xs font-normal text-slate-500">{r.ref} · {fcfa(r.totalAmount)}</span></Link>
                  <StatusBadge meta={REQUEST_STATUS} value={r.status} />
                </li>
              ))}
            </ul>
          )}
        </Section>
        {notifBlock}
      </div>
    </>
  );
}
