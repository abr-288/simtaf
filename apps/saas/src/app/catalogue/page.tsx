import { getCatalog } from '@menewa/api-client';
import type { CatalogItem } from '@menewa/shared-types';

const categories: Record<string, string> = {
  nourriture: 'Alimentation', betail: 'Bétail', volaille: 'Volaille', habits: 'Vêtements',
  fournitures: 'Fournitures', equipements: 'Équipements', action: 'Actions solidaires', autre: 'Autres',
};

export default async function CatalogPage() {
  let data: Awaited<ReturnType<typeof getCatalog>> | null = null;
  try { data = await getCatalog(); } catch { /* Keep the public page useful while the API is starting. */ }
  const grouped = (data?.items ?? []).reduce<Record<string, CatalogItem[]>>((groups, item) => {
    (groups[item.category] ??= []).push(item);
    return groups;
  }, {});
  return <main className="catalog-page">
    <header className="catalog-header"><a className="catalog-brand" href="/">menewa</a><a className="catalog-login" href="/login">Se connecter →</a></header>
    <section className="catalog-intro"><p className="eyebrow">AGIR ENSEMBLE</p><h1>Un geste concret,<br />un impact réel.</h1><p>Choisissez une action solidaire et découvrez son coût transparent.</p></section>
    {!data ? <p className="catalog-unavailable" role="status">Le catalogue est momentanément indisponible. Réessayez dans un instant.</p> : data.items.length === 0 ? <p className="catalog-unavailable">Aucun article n’est publié pour le moment.</p> :
      Object.entries(grouped).map(([category, items]) => <section className="catalog-section" key={category}><div className="catalog-section-heading"><h2>{categories[category] ?? category}</h2><span>{items.length} option{items.length === 1 ? '' : 's'}</span></div><div className="catalog-grid">{items.map((item) => <article className="catalog-card" key={item.id}><div className="catalog-card-icon">{item.emoji || '🎁'}</div><p className="catalog-card-category">{categories[item.category] ?? item.category}</p><h3>{item.title}</h3><p className="catalog-card-description">{item.description}</p><div className="catalog-card-footer"><strong>{new Intl.NumberFormat('fr-FR').format(item.unitPrice)} FCFA</strong><span>par {item.unit}</span></div></article>)}</div></section>)}
    <footer className="catalog-footer">Menewa Naya · Ensemble, on fait la différence.</footer>
  </main>;
}
