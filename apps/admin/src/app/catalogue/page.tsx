import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { getIdentityPermissions, getIdentityUser, getManagedCatalog } from '@menewa/api-client';
import type { ManagedCatalogItem } from '@menewa/shared-types';
import { saveCatalogAction, toggleCatalogAction } from './actions';

function formatPrice(value: number) { return `${new Intl.NumberFormat('fr-FR').format(value)} FCFA`; }

function CatalogRow({ item }: { item: ManagedCatalogItem }) {
  return <article className={`managed-item ${item.active ? '' : 'managed-item--inactive'}`}>
    <div className="managed-item-main"><span className="managed-item-emoji">{item.emoji || '🎁'}</span><div><h3>{item.title}</h3><p>{item.category} · {item.kind} · {formatPrice(item.unitPrice)} / {item.unit}</p></div></div>
    <div className="managed-item-cost">Coût estimé<strong>{formatPrice(item.costEstimate)}</strong></div>
    <form action={toggleCatalogAction}><input type="hidden" name="id" value={item.id} /><input type="hidden" name="active" value={String(!item.active)} /><button className="catalog-toggle">{item.active ? 'Désactiver' : 'Activer'}</button></form>
  </article>;
}

export default async function CatalogAdminPage({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const token = (await cookies()).get('menewa_admin_session')?.value;
  if (!token) redirect('/login');
  let user;
  let permissions: string[];
  try {
    [user, permissions] = await Promise.all([getIdentityUser(token), getIdentityPermissions(token)]);
  } catch { redirect('/login?error=session'); }
  if (!['admin', 'subadmin'].includes(user.role)) redirect('/login?error=forbidden');
  if (!permissions.includes('catalog.manage')) redirect('/?error=forbidden');
  let items: ManagedCatalogItem[];
  try { items = await getManagedCatalog(token); }
  catch { redirect('/?error=api'); }
  const params = await searchParams;
  return <main className="admin-home catalog-admin">
    <a className="catalog-back" href="/">← Administration</a>
    <header><div><p className="eyebrow">CONFIGURATION DE LA PLATEFORME</p><h1>Catalogue & tarifs</h1><p>Les articles actifs sont visibles sur le SaaS public.</p></div><span className="catalog-total">{items.length} article{items.length === 1 ? '' : 's'}</span></header>
    {params.saved === '1' && <p className="catalog-feedback">Catalogue enregistré.</p>}
    {params.error === 'save' && <p className="catalog-feedback catalog-feedback--error">Enregistrement impossible. Vérifiez les valeurs et réessayez.</p>}
    <section className="catalog-admin-section"><h2>Ajouter un article ou une action</h2>
      <form action={saveCatalogAction} className="catalog-edit-form">
        <label>Icône<input name="emoji" defaultValue="🎁" maxLength={12} /></label>
        <label>Nom<input name="title" placeholder="Nom de l’article" maxLength={120} required /></label>
        <label>Catégorie<select name="category" defaultValue="nourriture"><option value="nourriture">Alimentation</option><option value="betail">Bétail</option><option value="volaille">Volaille</option><option value="habits">Vêtements</option><option value="fournitures">Fournitures</option><option value="equipements">Équipements</option><option value="action">Action solidaire</option><option value="autre">Autre</option></select></label>
        <label>Description<input name="description" maxLength={1000} /></label>
        <label>Prix FCFA<input name="unitPrice" type="number" min={0} required /></label>
        <label>Coût estimé<input name="costEstimate" type="number" min={0} defaultValue={0} /></label>
        <label>Unité<input name="unit" defaultValue="unité" maxLength={30} /></label>
        <label className="catalog-checkbox"><input name="active" type="checkbox" defaultChecked /> Publié</label>
        <button type="submit">Ajouter au catalogue</button>
      </form>
    </section>
    <section className="catalog-admin-section"><h2>Articles existants</h2>{items.length ? <div className="managed-list">{items.map((item) => <CatalogRow key={item.id} item={item} />)}</div> : <p>Aucun article. Importez les données legacy ou ajoutez le premier article.</p>}</section>
  </main>;
}
