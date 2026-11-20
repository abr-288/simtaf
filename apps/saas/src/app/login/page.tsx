import { redirect } from 'next/navigation';

/** Ancienne adresse : la connexion se fait sur /connexion (comme sur Menewa Naya). */
export default function LoginPage() {
  redirect('/connexion');
}
