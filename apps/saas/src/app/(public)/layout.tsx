import type { ReactNode } from 'react';
import { PublicFooter } from '@/components/PublicFooter';
import { PublicHeader } from '@/components/PublicHeader';
import { getSession } from '@/lib/session';

export const dynamic = 'force-dynamic';

export default async function PublicLayout({ children }: { children: ReactNode }) {
  const session = await getSession().catch(() => null);
  return (
    <>
      <PublicHeader userName={session ? session.user.name || session.user.phone : null} />
      <main>{children}</main>
      <PublicFooter />
    </>
  );
}
