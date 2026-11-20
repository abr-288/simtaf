import Link from "next/link";
import { getPublicProviders } from "@menewa/api-client";
import { PageHero } from "@/components/PageHero";
import { PROVIDER_CATEGORIES, Stars } from "@menewa/ui";

export const dynamic = "force-dynamic";
export const metadata = { title: "Social Network — Menewa Naya" };

export default async function Page({ searchParams }: { searchParams: Promise<{ q?: string; cat?: string }> }) {
  const { q, cat } = await searchParams;
  const rows = await getPublicProviders(q, cat).catch(() => []);
  return (
    <>
      <PageHero eyebrow="Menewa Social Network" title="Vos compétences, au service des autres">
        Trouvez un prestataire local et mettez-vous en relation. Connectez-vous pour demander un service et négocier le prix.
      </PageHero>
      <div className="mx-auto max-w-[1100px] px-4 py-8 lg:px-9">
        <form className="mb-6 flex flex-wrap gap-2">
          <input name="q" defaultValue={q} placeholder="Rechercher (nom, zone, compétence)…" className="input max-w-sm" />
          <select name="cat" defaultValue={cat ?? ""} className="input max-w-xs">
            <option value="">Toutes les catégories</option>
            {Object.entries(PROVIDER_CATEGORIES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <button className="btn btn-green">Rechercher</button>
        </form>
        <div className="grid gap-4 md:grid-cols-2">
          {rows.map((r) => (
            <div key={r.id} className="card">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="font-bold text-brand-dark">{r.name || "Prestataire"} {r.verified && <span className="badge badge-green ml-1">Vérifié</span>}</p>
                  <p className="text-xs text-slate-500">{PROVIDER_CATEGORIES[r.category]} · {r.zone ?? "—"}</p>
                </div>
                <div className="text-right"><Stars value={r.rating} /><p className="text-[11px] text-slate-500">{r.reviews} avis</p></div>
              </div>
              <p className="mt-2 text-sm text-slate-600">{r.description}</p>
              <ul className="mt-2 list-disc pl-5 text-xs text-slate-600">
                {r.services.map((s) => <li key={s.id}>{s.label}</li>)}
              </ul>
            </div>
          ))}
          {rows.length === 0 && <p className="text-sm text-slate-500">Aucun prestataire ne correspond à votre recherche.</p>}
        </div>
        <div className="mt-8 text-center"><Link href="/espace/services" className="btn btn-yellow px-8 py-3">Demander un service</Link></div>
      </div>
    </>
  );
}
