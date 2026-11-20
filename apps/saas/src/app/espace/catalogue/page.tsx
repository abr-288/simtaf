import Link from 'next/link';
import { getCatalog, getTargets } from '@menewa/api-client';
import { ActionForm, CATEGORIES, fcfa, PageHeader } from '@menewa/ui';
import { TargetSelect } from '@/components/RequestFields';
import { requireRole } from '@/lib/session';
import { addToCart } from '../actions';

export default async function Page({ searchParams }: { searchParams: Promise<{ cat?: string }> }) {
  const { user: u } = await requireRole('donor', 'client');
  const { cat } = await searchParams;
  const [{ items }, targets] = await Promise.all([getCatalog(), getTargets()]);
  const shown = cat ? items.filter((i) => i.category === cat) : items;
  return (
    <>
      <PageHeader title="Catalogue de dons" subtitle="Choisissez un don prédéfini, définissez la quantité et la cible, puis validez votre panier."
        actions={<Link href="/espace/panier" className="btn btn-outline btn-sm">🛒 Voir mon panier</Link>} />
      <div className="mb-5 flex flex-wrap gap-2">
        <Link href="/espace/catalogue" className={`btn btn-sm ${!cat ? 'btn-green' : 'btn-outline'}`}>Tous</Link>
        {Object.entries(CATEGORIES).filter(([k]) => k !== 'autre').map(([k, v]) => (
          <Link key={k} href={`/espace/catalogue?cat=${k}`} className={`btn btn-sm ${cat === k ? 'btn-green' : 'btn-outline'}`}>{v}</Link>
        ))}
      </div>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {shown.map((i) => (
          <div key={i.id} className="card flex flex-col">
            <div className="flex items-center gap-3">
              <span className="grid h-14 w-14 place-items-center rounded-2xl bg-mint text-3xl">{i.emoji}</span>
              <div><p className="font-bold text-brand-dark">{i.title}</p><p className="text-xs text-slate-500">{CATEGORIES[i.category]}</p></div>
            </div>
            <p className="mt-3 flex-1 text-sm text-slate-600">{i.description}</p>
            <p className="mt-2 text-lg font-bold text-brand">{fcfa(i.unitPrice)} <span className="text-xs font-normal text-slate-500">/ {i.unit}</span></p>
            <ActionForm action={addToCart} submit="Ajouter au panier" className="mt-3 space-y-2" btnClass="btn btn-green btn-sm w-full">
              <input type="hidden" name="catalogItemId" value={i.id} />
              <div className="grid grid-cols-[90px_1fr] gap-2">
                <input name="quantity" type="number" min={1} max={500} defaultValue={1} className="input" aria-label="Quantité" />
                <TargetSelect name="target" targets={targets} allowCustom={u.role === 'donor'} placeholder="Cible (facultatif)" />
              </div>
            </ActionForm>
          </div>
        ))}
      </div>
    </>
  );
}
