import { notFound } from 'next/navigation';
import { ApiRequestError, getDispute } from '@menewa/api-client';
import { DisputeThread } from '@menewa/ui';
import { requireUser } from '@/lib/session';
import { postDisputeMessage } from '../../actions';

export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { token, user } = await requireUser();
  const id = parseInt((await params).id, 10);
  if (!Number.isFinite(id)) notFound();
  // L'API ne renvoie le litige qu'aux parties concernées.
  const detail = await getDispute(token, id).catch((e) => { if (e instanceof ApiRequestError && (e.status === 404 || e.status === 403)) notFound(); throw e; });
  return <DisputeThread detail={detail} viewerId={user.id} messageAction={postDisputeMessage} />;
}
