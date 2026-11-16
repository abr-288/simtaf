import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { getIdentityPermissions, getIdentityUser } from '@menewa/api-client';
import { logoutAction } from './logout/actions';

export default async function HomePage() {
  const token = (await cookies()).get('menewa_admin_session')?.value;
  if (!token) redirect('/login');
  let user;
  let permissions: string[] = [];
  try {
    user = await getIdentityUser(token);
    permissions = await getIdentityPermissions(token);
  } catch {
    redirect('/login?error=session');
  }
  if (!['admin', 'subadmin'].includes(user.role)) redirect('/login?error=forbidden');

  return <main className="admin-home">
    <header><div><p className="eyebrow">CONSOLE D’ADMINISTRATION</p><h1>Bonjour, {user.name || user.phone}</h1><p>Rôle : {user.role === 'admin' ? 'Administrateur principal' : 'Administrateur secondaire'}</p></div><form action={logoutAction}><button className="secondary">Se déconnecter</button></form></header>
    <section><h2>Permissions actives</h2><p>Ces droits sont fournis et vérifiés par l’API Identity.</p><ul>{permissions.map((permission) => <li key={permission}>{permission}</li>)}</ul></section>
  </main>;
}
