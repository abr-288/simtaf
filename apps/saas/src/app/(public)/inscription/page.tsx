import { redirect } from 'next/navigation';
import { AuthForm } from '@/components/AuthForm';
import { getSession } from '@/lib/session';

export const dynamic = 'force-dynamic';
export const metadata = { title: 'Inscription — Menewa Naya' };

export default async function Page() {
  const session = await getSession().catch(() => null);
  if (session) redirect('/espace');
  return (
    <section className="bg-gradient-to-b from-cream to-white px-4 py-12">
      <div className="card mx-auto max-w-lg p-7">
        <h1 className="text-2xl font-bold text-brand-dark">Rejoindre Menewa Naya</h1>
        <p className="mb-5 mt-1 text-sm text-slate-600">Créez votre compte en quelques secondes. Aucun mot de passe : un code SMS suffit.</p>
        <AuthForm mode="register" />
        <p className="mt-4 text-xs text-slate-500">En vous inscrivant, vous acceptez les conditions d’utilisation et la politique de confidentialité.</p>
      </div>
    </section>
  );
}
