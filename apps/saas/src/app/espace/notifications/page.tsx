import Link from 'next/link';
import { getNotifications } from '@menewa/api-client';
import { Empty, fdatetime, PageHeader } from '@menewa/ui';
import { requireUser } from '@/lib/session';
import { markNotificationsRead } from '../actions';

/** Dans l'espace membre, seuls les liens /espace/* sont pertinents. */
const memberLink = (link: string | null) => (link && link.startsWith('/espace') ? link : null);

export default async function Page() {
  const { token } = await requireUser();
  const rows = await getNotifications(token);
  return (
    <>
      <PageHeader title="Notifications" subtitle="Nouvelles demandes, décisions, missions, échéances, preuves, clôtures…"
        actions={<form action={markNotificationsRead}><button className="btn btn-outline btn-sm">Tout marquer comme lu</button></form>} />
      {rows.length === 0 ? <Empty>Aucune notification.</Empty> : (
        <ul className="space-y-2">
          {rows.map((n) => (
            <li key={n.id} className={`card flex items-start justify-between gap-3 p-4 ${n.readAt ? '' : 'border-l-4 border-l-sun bg-cream/40'}`}>
              <div>
                <p className="text-sm font-semibold">{n.title}</p>
                {n.body && <p className="text-sm text-slate-600">{n.body}</p>}
                <p className="mt-1 text-xs text-slate-500">{fdatetime(n.createdAt)}</p>
              </div>
              {memberLink(n.link) && <Link href={memberLink(n.link)!} className="btn btn-outline btn-sm">Voir</Link>}
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
