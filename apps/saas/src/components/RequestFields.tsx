'use client';

import { useState } from 'react';
import type { Target } from '@menewa/shared-types';
import { ANNEX_LABELS, fcfa, PAYMENT_MODE, RECURRENCE } from '@menewa/ui';

export function ScheduleFields() {
  return (
    <fieldset className="space-y-3 rounded-xl border border-slate-200 p-4">
      <legend className="px-2 text-sm font-bold text-brand-dark">Échéancier et paiement</legend>
      <div className="grid gap-3 sm:grid-cols-3">
        <div><label className="label">Fréquence</label><select name="recurrence" className="input" defaultValue="none">{Object.entries(RECURRENCE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select></div>
        <div><label className="label">Nombre d’échéances</label><input name="occurrences" type="number" min={2} max={60} defaultValue={3} className="input" /></div>
        <div><label className="label">Date de début</label><input name="startDate" type="date" className="input" /></div>
      </div>
      <div>
        <label className="label">Mode de paiement</label>
        <select name="paymentMode" className="input" defaultValue="immediate">{Object.entries(PAYMENT_MODE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select>
        <p className="mt-1 text-xs text-slate-500">Don ponctuel : paiement immédiat. Engagement récurrent : préfinancement (tout payé d’avance) ou paiement à chaque échéance. Le règlement s’effectue via Mobile Money / virement et est validé par l’administration (intégration automatique à venir).</p>
      </div>
    </fieldset>
  );
}

export function AnnexFields({ prices }: { prices: Record<string, number> }) {
  return (
    <fieldset className="space-y-3 rounded-xl border border-slate-200 p-4">
      <legend className="px-2 text-sm font-bold text-brand-dark">Services annexes (optionnels)</legend>
      <div className="grid gap-2 sm:grid-cols-2">
        {Object.entries(ANNEX_LABELS).map(([k, l]) => (
          <label key={k} className="flex items-center gap-2 rounded-lg bg-slate-50 px-3 py-2 text-sm">
            <input type="checkbox" name={`annex_${k}`} className="h-4 w-4 accent-[#0b5a3c]" />
            <span className="flex-1">{l}</span>
            <span className="text-xs font-semibold text-brand">+{fcfa(prices[k] ?? 0)}</span>
          </label>
        ))}
      </div>
      <div><label className="label">Critères de personnalisation du packaging</label><input name="annex_packaging_notes" className="input" placeholder="Couleurs, message, emballage cadeau…" /></div>
      <div><label className="label">Autre spécificité à demander</label><input name="annex_other" className="input" placeholder="Toute autre demande particulière" /></div>
      <p className="text-xs text-slate-500">Tarifs provisoires, configurables par l’administration.</p>
    </fieldset>
  );
}

const CUSTOM = '__custom__';

/** Liste des cibles définies par l'administration ; `allowCustom` ouvre un champ libre (donateurs). */
export function TargetSelect({ name, targets, allowCustom = false, required = false, id, placeholder = 'Choisir une cible', customPlaceholder = 'Précisez la cible (ex. Famille Mensah)', defaultValue = '' }: {
  name: string; targets: Target[]; allowCustom?: boolean; required?: boolean; id?: string; placeholder?: string; customPlaceholder?: string; defaultValue?: string;
}) {
  const known = targets.some((t) => t.label === defaultValue);
  const [value, setValue] = useState(known ? defaultValue : defaultValue && allowCustom ? CUSTOM : '');
  const custom = value === CUSTOM;
  return (
    <div>
      <select id={id} name={custom ? undefined : name} value={value} onChange={(e) => setValue(e.target.value)} required={required} className="input" aria-label={id ? undefined : 'Cible du don'}>
        <option value="">{placeholder}</option>
        {targets.map((t) => <option key={t.id} value={t.label} title={t.description || undefined}>{t.label}</option>)}
        {allowCustom && <option value={CUSTOM}>Autre cible (personnalisée)…</option>}
      </select>
      {custom && <input name={name} defaultValue={known ? '' : defaultValue} className="input mt-2" placeholder={customPlaceholder} required={required} minLength={2} maxLength={150} autoFocus aria-label="Cible personnalisée" />}
    </div>
  );
}
