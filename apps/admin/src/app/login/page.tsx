import { Logo } from '@menewa/ui';
import { LoginForm } from './LoginForm';

const ERRORS: Record<string, string> = {
  forbidden: 'Ce compte ne possède pas de droits administrateur.',
  session: 'Votre session a expiré. Reconnectez-vous.',
};

export default async function AdminLoginPage({ searchParams }: { searchParams: Promise<{ error?: string }> }) {
  const { error } = await searchParams;
  return (
    <main className="grid min-h-screen place-items-center bg-gradient-to-b from-mint to-white px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-6 flex justify-center"><Logo /></div>
        {error && ERRORS[error] && <p role="alert" className="mb-4 rounded-xl bg-red-50 p-3 text-sm text-red-700">{ERRORS[error]}</p>}
        <LoginForm />
      </div>
    </main>
  );
}
