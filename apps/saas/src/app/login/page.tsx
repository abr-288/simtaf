import { LoginForm } from './LoginForm';

export default function LoginPage() {
  return <main className="login-shell"><LoginForm /><a href="/catalogue" className="catalog-login-link">Voir le catalogue public</a></main>;
}
