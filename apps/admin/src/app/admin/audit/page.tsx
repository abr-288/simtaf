import { getAudit } from '@menewa/api-client';
import { fdatetime, PageHeader, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';

export default async function Page({ searchParams }: { searchParams: Promise<{ q?: string }> }) {
  const { token } = await requirePerm('audit.view');
  const { q } = await searchParams;
  const rows = await getAudit(token, q);
  return (
    <>
      <PageHeader title="Journal d’audit" subtitle="Actions critiques et décisions administratives, horodatées et non modifiables depuis l’interface." />
      <form className="mb-3 flex gap-2"><input name="q" defaultValue={q} className="input max-w-xs" placeholder="Action (ex. request.reject)" /><button className="btn btn-green btn-sm">Filtrer</button></form>
      <Table>
        <thead><tr><th>Date</th><th>Acteur</th><th>Action</th><th>Objet</th><th>Détails</th><th>IP</th></tr></thead>
        <tbody>
          {rows.map((a) => (
            <tr key={a.id}>
              <td className="whitespace-nowrap text-xs">{fdatetime(a.createdAt)}</td>
              <td>{a.actorName || 'système'}<br /><span className="text-xs text-slate-500">{a.actorRole}</span></td>
              <td className="font-mono text-xs font-semibold">{a.action}</td>
              <td className="text-xs">{a.entity} {a.entityId && `#${a.entityId}`}</td>
              <td className="max-w-xs break-words text-xs text-slate-600">{a.details}</td>
              <td className="text-xs">{a.ip}</td>
            </tr>
          ))}
        </tbody>
      </Table>
    </>
  );
}
