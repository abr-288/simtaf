import Link from "next/link";
import { getPublicPartners } from "@menewa/api-client";
import { PageHero } from "@/components/PageHero";

export const dynamic = "force-dynamic";
export const metadata = { title: "Partenaires — Menewa Naya" };

export default async function Page() {
  const partners = await getPublicPartners().catch(() => []);
  return (
    <>
      <PageHero eyebrow="Nos partenaires" title="Ensemble pour un plus grand impact">
        Associations, ONG, entreprises, institutions et mécènes partagent notre vision et publient dons, actions et campagnes sur la plateforme.
      </PageHero>
      <div className="mx-auto max-w-[1100px] px-4 py-10 lg:px-9">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {partners.map((p) => (
            <div key={p.id} className="card">
              <p className="text-lg font-bold text-brand-dark">{p.organization || p.name}</p>
              <p className="text-sm text-slate-500">{p.zone ?? "Togo"}</p>
            </div>
          ))}
        </div>
        <div className="mt-10 rounded-2xl bg-mint p-6 text-center">
          <h2 className="text-xl font-bold text-brand-dark">Devenir partenaire</h2>
          <p className="mx-auto mt-2 max-w-xl text-sm">Inscrivez votre organisation, publiez vos campagnes de financement participatif et suivez les contributions.</p>
          <Link href="/inscription" className="btn btn-green mt-4">S’inscrire comme partenaire</Link>
        </div>
      </div>
    </>
  );
}
