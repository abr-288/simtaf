import type { ReactNode } from 'react';
import { Star } from 'lucide-react';
import type { StatusMeta } from './format';

/** Boutons ronds à icône (enregistrer ✓ / supprimer 🗑), alignés sur la hauteur des champs .input */
const ICON_BTN = 'inline-grid h-[42px] w-[42px] shrink-0 place-items-center rounded-full transition disabled:cursor-not-allowed';
export const ICON_SAVE = `${ICON_BTN} bg-brand text-white hover:bg-brand-dark disabled:opacity-50`;
export const ICON_DEL = `${ICON_BTN} border border-red-200 bg-white text-red-600 hover:bg-red-50 disabled:opacity-50`;
export const ICON_DEL_OFF = `${ICON_BTN} border border-slate-200 bg-white text-slate-300`;

export function StatusBadge({ meta, value }: { meta: StatusMeta; value: string }) {
  const m = meta[value] ?? { label: value, tone: 'slate' };
  return <span className={`badge badge-${m.tone}`}>{m.label}</span>;
}

export function PageHeader({ title, subtitle, actions }: { title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="h-title">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-slate-600">{subtitle}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
    </div>
  );
}

export function StatCard({ label, value, hint, tone = 'green' }: { label: string; value: ReactNode; hint?: string; tone?: 'green' | 'amber' | 'red' | 'blue' }) {
  const c = { green: 'text-brand', amber: 'text-amber-600', red: 'text-red-600', blue: 'text-sky-700' }[tone];
  return (
    <div className="card">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p>
      <p className={`mt-1 text-2xl font-bold ${c}`}>{value}</p>
      {hint && <p className="mt-0.5 text-xs text-slate-500">{hint}</p>}
    </div>
  );
}

export function Empty({ children }: { children: ReactNode }) {
  return <p className="rounded-xl border border-dashed border-slate-300 bg-slate-50 p-6 text-center text-sm text-slate-500">{children}</p>;
}

export function Stars({ value, size = 14 }: { value: number; size?: number }) {
  return (
    <span className="inline-flex items-center gap-0.5" aria-label={`${value.toFixed(1)} sur 5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <Star key={i} size={size} className={i <= Math.round(value) ? 'fill-sun text-sun' : 'text-slate-300'} />
      ))}
    </span>
  );
}

export function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div>
      <label className="label">{label}</label>
      {children}
    </div>
  );
}

export function Section({ title, children, actions }: { title: string; children: ReactNode; actions?: ReactNode }) {
  return (
    <section className="card">
      <div className="mb-3 flex items-center justify-between gap-2">
        <h2 className="text-base font-bold text-brand-dark">{title}</h2>
        {actions}
      </div>
      {children}
    </section>
  );
}

export function Table({ children }: { children: ReactNode }) {
  return (
    <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white">
      <table className="tbl">{children}</table>
    </div>
  );
}

/** Message affiché quand l'API ne répond pas ou refuse l'accès. */
export function ApiUnavailable({ message }: { message?: string }) {
  return (
    <p role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
      {message ?? 'Le service est momentanément indisponible. Vérifiez que l’API Menewa est démarrée puis réessayez.'}
    </p>
  );
}

export function LogoMark({ size = 56 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-hidden>
      <circle cx="20" cy="13" r="7" fill="#0b5a3c" />
      <path d="M3 30 C3 19 14 21 20 28 C23 38 19 52 30 59 C12 57 3 46 3 30Z" fill="#0b5a3c" />
      <circle cx="46" cy="12" r="7" fill="#f6b53b" />
      <path d="M44 59 C29 48 27 38 33 32 C37 28 43 30 45 36 C49 28 60 30 60 38 C60 46 52 52 44 59Z" fill="#f6b53b" />
    </svg>
  );
}

export function Logo({ light = false, size = 56 }: { light?: boolean; size?: number }) {
  return (
    <span className="flex items-center gap-2.5">
      <LogoMark size={size} />
      <span className="leading-none">
        <span className={`block text-[28px] font-extrabold tracking-tight ${light ? 'text-white' : 'text-brand'}`}>Menewa</span>
        <span className={`block pl-3 text-[19px] font-semibold ${light ? 'text-sun' : 'text-brand'}`}>Naya</span>
        <span className={`mt-0.5 block text-[10px] font-medium ${light ? 'text-white/80' : 'text-brand'}`}>Ensemble, on fait la différence.</span>
      </span>
    </span>
  );
}
