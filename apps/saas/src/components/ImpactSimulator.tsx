"use client";

import { useEffect, useRef, useState } from "react";
import { useFormStatus } from "react-dom";
import Link from "next/link";
import { ArrowRight, Check } from "lucide-react";
import { addFromSimulator } from "@/app/actions/simulator";

export type SimItem = {
  id: number | string;
  title: string;
  description?: string | null;
  unit: string;
  unitPrice: number; // FCFA
  emoji?: string | null;
  imageUrl?: string | null;
};

/* Dons d'EXEMPLE (démo, sans base de données).
   Pour brancher le catalogue réel : <ImpactSimulator items={…} /> avec des SimItem. */
export const DEFAULT_ITEMS: SimItem[] = [
  { id: 1, title: "Pack alimentaire famille", description: "Riz, huile, haricots, farine, sel, tomate — pour une famille pendant un mois.", unit: "pack", unitPrice: 15000, emoji: "🍚" },
  { id: 2, title: "Pack repas enfants", description: "Repas équilibrés pour 10 enfants pendant une semaine.", unit: "pack", unitPrice: 8000, emoji: "🥣" },
  { id: 3, title: "Kit de fournitures scolaires", description: "Cahiers, stylos, ardoise, sac.", unit: "kit", unitPrice: 7500, emoji: "🎒" },
  { id: 4, title: "Lampe solaire", description: "Éclairage autonome pour étudier le soir.", unit: "unité", unitPrice: 6000, emoji: "💡" },
  { id: 5, title: "Habits pour enfant", description: "Tenue complète (haut, bas, sous-vêtements).", unit: "tenue", unitPrice: 12000, emoji: "👕" },
  { id: 6, title: "Visite à une personne", description: "Un bénévole rend visite à une personne âgée, malade ou isolée.", unit: "visite", unitPrice: 5000, emoji: "🤝" },
  { id: 7, title: "Lot de 5 volailles", description: "Cinq poulets pour démarrer un petit élevage ou partager un repas.", unit: "lot", unitPrice: 25000, emoji: "🐓" },
  { id: 8, title: "Matelas", description: "Matelas neuf pour une personne.", unit: "unité", unitPrice: 20000, emoji: "🛏️" },
  { id: 9, title: "Bélier", description: "Un bélier pour une fête ou une famille dans le besoin.", unit: "tête", unitPrice: 85000, emoji: "🐏" },
];

const CFG = { min: 1000, max: 500000, step: 1000, def: 50000, presets: [5000, 10000, 25000, 50000, 100000, 250000], months: 12 };
/* Aucun taux inventé : à renseigner une fois le régime fiscal validé. */
const TAX = { enabled: false, ratePct: 0, organizationEligible: false };

const fmt = (n: number) => new Intl.NumberFormat("fr-FR").format(n).replace(/\u202f|\u00a0/g, " ");

function useCountUp(target: number) {
  const [v, setV] = useState(target);
  const cur = useRef(target);
  useEffect(() => {
    const from = cur.current;
    const dur = window.matchMedia("(prefers-reduced-motion: reduce)").matches ? 0 : 350;
    const t0 = performance.now();
    let raf = 0;
    const tick = (t: number) => {
      const p = dur === 0 ? 1 : Math.min(1, (t - t0) / dur);
      const val = Math.round(from + (target - from) * (1 - Math.pow(1 - p, 3)));
      cur.current = val;
      setV(val);
      if (p < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [target]);
  return v;
}

function Card({ it, units, monthlyUnits, ratio, partial, missing, monthly, selected, onSelect }: {
  it: SimItem; units: number; monthlyUnits: number; ratio: number; partial: number; missing: number; monthly: boolean; selected: boolean; onSelect: () => void;
}) {
  const shown = useCountUp(units);
  const can = units > 0;
  const [broken, setBroken] = useState(false);
  return (
    <li>
      <button
        type="button" onClick={onSelect} disabled={!can} aria-pressed={selected}
        className={`w-full rounded-2xl border bg-white p-4 text-left transition duration-300 disabled:cursor-default ${
          selected ? "border-brand bg-[#eaf6ee] ring-2 ring-brand" : can ? "border-brand/25 shadow-sm hover:-translate-y-px hover:border-brand" : "border-slate-200 opacity-70"
        }`}
      >
        <div className="flex items-start gap-3">
          <span className={`min-w-[3.4rem] text-[32px] font-extrabold leading-none tabular-nums ${can ? "text-brand" : "text-slate-400"}`}>{fmt(shown)}</span>
          {it.imageUrl && !broken ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={it.imageUrl} alt="" onError={() => setBroken(true)} className="h-10 w-10 shrink-0 rounded-xl object-cover" />
          ) : (
            <span className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-mint text-xl" aria-hidden>{it.emoji || "🎁"}</span>
          )}
          <span className="min-w-0 flex-1">
            <span className="block text-[15px] font-bold leading-tight text-brand-dark">{it.title}</span>
            {it.description && <span className="mt-0.5 line-clamp-2 block text-[12px] leading-snug text-slate-500">{it.description}</span>}
          </span>
          <span className="shrink-0 pl-1 text-[11px] text-slate-500">{fmt(it.unitPrice)} FCFA / {it.unit}</span>
          {selected && <span className="grid h-5 w-5 shrink-0 place-items-center rounded-full bg-brand text-white"><Check size={12} strokeWidth={3} /></span>}
        </div>
        <div className="mt-2.5 flex items-center gap-3 text-[11px] text-slate-500">
          <span className="h-[7px] flex-1 overflow-hidden rounded-full bg-mint" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.round((can ? ratio : partial) * 100)}>
            <span className={`block h-full rounded-full transition-[width] duration-500 ${can ? "bg-gradient-to-r from-sun to-sun-dark" : "bg-slate-300"}`} style={{ width: `${(can ? ratio : partial) * 100}%` }} />
          </span>
          <span className="shrink-0">
            {can ? (monthly ? (monthlyUnits > 0 ? `sur 12 mois · ${fmt(monthlyUnits)} / mois` : "sur 12 mois (cumul)") : `${it.unit}${units > 1 ? "s" : ""} finançable${units > 1 ? "s" : ""}`) : `encore ${fmt(missing)} FCFA`}
          </span>
        </div>
      </button>
    </li>
  );
}

function AddButton({ disabled, children }: { disabled: boolean; children: React.ReactNode }) {
  const { pending } = useFormStatus();
  return (
    <button type="submit" disabled={disabled || pending} className="btn btn-green px-7 py-3.5 text-sm disabled:opacity-50">
      {pending ? "Ajout au panier…" : children} <ArrowRight size={16} />
    </button>
  );
}

export default function ImpactSimulator({ items = DEFAULT_ITEMS }: { items?: SimItem[] }) {
  const [amount, setAmount] = useState(CFG.def);
  const [monthly, setMonthly] = useState(false);
  const [selId, setSelId] = useState<SimItem["id"] | null>(null);
  const months = monthly ? CFG.months : 1;
  const total = amount * months;

  const rows = items.filter((i) => i.unitPrice > 0).map((it) => ({
    it,
    units: Math.floor(total / it.unitPrice),
    monthlyUnits: Math.floor(amount / it.unitPrice),
    partial: Math.min(1, total / it.unitPrice),
    missing: Math.max(0, it.unitPrice - total),
  }));
  const maxUnits = Math.max(1, ...rows.map((r) => r.units));
  const sel = rows.find((r) => r.it.id === selId && r.units > 0) ?? null;
  const taxOn = TAX.enabled && TAX.ratePct > 0 && TAX.organizationEligible;
  const reduction = taxOn ? Math.round((total * TAX.ratePct) / 100) : 0;
  const pct = Math.min(100, Math.max(0, ((Math.min(amount, CFG.max) - CFG.min) / (CFG.max - CFG.min)) * 100));
  const big = useCountUp(amount);
  const href = `/espace/catalogue?amount=${amount}&freq=${monthly ? "monthly" : "once"}`;
  // Quantité ajoutée au panier : sur l'ensemble pour un don ponctuel, par mois pour un don mensuel.
  const selQty = sel ? (monthly ? sel.monthlyUnits : sel.units) : 0;

  return (
    <div className="relative overflow-hidden rounded-[28px] bg-[#e9f3ed] p-5 sm:p-8 lg:p-10">
      <div className="grid gap-8 lg:grid-cols-[1fr_1.05fr] lg:gap-12">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-brand-dark">Concrètement</p>
          <h2 className="mt-2 text-[30px] font-extrabold leading-[1.08] text-brand-dark sm:text-[42px]">
            Déplacez le curseur,{" "}
            <em className="bg-[linear-gradient(transparent_62%,rgba(246,181,59,.55)_62%)] not-italic text-brand">voyez ce que ça change.</em>
          </h2>

          <div className="mt-6 flex items-end gap-2.5" aria-live="polite">
            <span className="text-[52px] font-extrabold leading-none tracking-tight tabular-nums text-brand-dark sm:text-[76px]">{fmt(big)}</span>
            <span className="pb-2 text-[13px] text-slate-500"><b className="mr-2 text-[22px] text-brand">FCFA</b>{monthly ? "par mois" : "en une fois"}</span>
          </div>
          {monthly && <p className="text-[13px] font-semibold text-brand">= {fmt(total)} FCFA sur {CFG.months} mois</p>}

          <input
            type="range" aria-label="Montant du don" min={CFG.min} max={CFG.max} step={CFG.step}
            value={Math.min(Math.max(amount, CFG.min), CFG.max)} onChange={(e) => setAmount(Number(e.target.value))}
            className="mt-5 h-2 w-full cursor-pointer appearance-none rounded-full accent-[#0b5a3c]"
            style={{ background: `linear-gradient(to right,#f6b53b ${pct}%,#c5d3ca ${pct}%)` }}
          />

          <div className="mt-4 flex flex-wrap gap-2">
            {CFG.presets.map((p) => (
              <button key={p} type="button" onClick={() => setAmount(p)} aria-pressed={amount === p}
                className={`rounded-full border px-3.5 py-1.5 text-xs font-semibold transition ${amount === p ? "border-brand bg-brand text-white" : "border-brand/30 bg-white text-brand hover:bg-mint"}`}>
                {fmt(p)} FCFA
              </button>
            ))}
          </div>

          <label className="mt-4 flex max-w-[300px] items-center gap-2 rounded-xl border border-slate-200 bg-white px-3.5 py-2 focus-within:border-brand focus-within:ring-2 focus-within:ring-brand/20">
            <span className="text-xs text-slate-500">Autre montant</span>
            <input inputMode="numeric" aria-label="Montant saisi manuellement" value={amount === 0 ? "" : amount}
              onChange={(e) => setAmount(Math.min(Number(e.target.value.replace(/\D/g, "")) || 0, 100_000_000))}
              onBlur={() => setAmount((a) => (a < CFG.min ? CFG.min : a))}
              className="min-w-0 flex-1 bg-transparent text-right text-[15px] font-semibold outline-none" />
            <span className="text-xs text-slate-500">FCFA</span>
          </label>

          <div className="mt-5 flex flex-wrap items-center justify-between gap-3 border-t border-brand/15 pt-4">
            <button type="button" role="switch" aria-checked={monthly} onClick={() => setMonthly((m) => !m)} className="flex items-center gap-2.5 text-sm font-medium">
              <span className={`relative h-6 w-11 rounded-full transition ${monthly ? "bg-brand" : "bg-[#b9c9bf]"}`}>
                <span className={`absolute left-0.5 top-0.5 h-5 w-5 rounded-full bg-white shadow transition-transform ${monthly ? "translate-x-5" : ""}`} />
              </span>
              Don mensuel <span className="text-xs font-normal text-slate-500">(×{CFG.months})</span>
            </button>
            <span className="text-xs text-slate-500">
              {taxOn ? <>Après déduction fiscale : <b className="text-brand">{fmt(total - reduction)} FCFA</b> de votre poche.</> : "Aucune estimation fiscale configurée."}
            </span>
          </div>
          {monthly && (
            <p className="mt-3 rounded-lg bg-[#fff3d6] px-3 py-2 text-xs text-[#6b4a07]">
              Don récurrent : {fmt(amount)} FCFA chaque mois, soit {fmt(total)} FCFA sur {CFG.months} mois. Les chiffres de droite sont des projections sur {CFG.months} mois.
            </p>
          )}
          {taxOn && (
            <dl className="mt-3 grid grid-cols-2 gap-2 rounded-xl bg-white px-3.5 py-2.5 text-xs sm:grid-cols-4">
              {[["Don", `${fmt(total)} FCFA`], ["Taux", `${TAX.ratePct} %`], ["Réduction estimée", `${fmt(reduction)} FCFA`], ["Coût réel", `${fmt(total - reduction)} FCFA`]].map(([k, v]) => (
                <div key={k}><dt className="text-slate-500">{k}</dt><dd className="font-bold text-brand-dark">{v}</dd></div>
              ))}
            </dl>
          )}

          <div className="mt-5">
            {sel ? (
              // Don choisi : ajouté au panier (quantité par mois pour un don mensuel, plafonnée à 500 comme le panier).
              <form action={addFromSimulator}>
                <input type="hidden" name="itemId" value={String(sel.it.id)} />
                <input type="hidden" name="quantity" value={Math.min(selQty, 500)} />
                <input type="hidden" name="freq" value={monthly ? "monthly" : "once"} />
                <AddButton disabled={selQty < 1}>
                  {`Financer ${fmt(Math.min(selQty, 500))} × ${sel.it.title}${monthly ? " / mois" : ""}`}
                </AddButton>
              </form>
            ) : (
              <Link href={href} aria-disabled={amount < 1} className={`btn btn-green px-7 py-3.5 text-sm ${amount < 1 ? "pointer-events-none opacity-50" : ""}`}>
                {`Faire un don de ${fmt(amount)} FCFA${monthly ? " / mois" : ""}`} <ArrowRight size={16} />
              </Link>
            )}
            <p className="mt-3 max-w-md text-[11px] leading-relaxed text-slate-500">
              {sel
                ? selQty < 1
                  ? `Ce don coûte ${fmt(sel.it.unitPrice)} FCFA : augmentez le montant mensuel pour le financer chaque mois. `
                  : `Le don choisi sera ajouté à votre panier${monthly ? " (fréquence mensuelle sur 12 mois proposée)" : ""}, à valider ensuite. `
                : "Touchez un don à droite pour le financer en particulier. "}
              Simulation indicative : aucun paiement n’est effectué ici, et chaque résultat est indépendant des autres.
            </p>
          </div>
        </div>

        <div>
          {rows.length === 0 ? (
            <p className="rounded-2xl border border-dashed border-slate-300 bg-white p-6 text-sm text-slate-500">Aucun don n’est disponible pour le moment.</p>
          ) : (
            <ul className="max-h-[650px] space-y-2.5 overflow-y-auto pr-2">
              {rows.map((r) => (
                <Card key={r.it.id} {...r} ratio={r.units / maxUnits} monthly={monthly} selected={sel?.it.id === r.it.id}
                  onSelect={() => setSelId((c) => (c === r.it.id ? null : r.it.id))} />
              ))}
            </ul>
          )}
        </div>
      </div>
      <div className="pointer-events-none absolute bottom-0 right-0 h-20 w-28" aria-hidden>
        <div className="absolute inset-0 bg-sun [clip-path:polygon(100%_0,100%_100%,0_100%)]" />
        <div className="pattern-strip absolute inset-0 [clip-path:polygon(100%_28%,100%_100%,28%_100%)]" />
      </div>
    </div>
  );
}