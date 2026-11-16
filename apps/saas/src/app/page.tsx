import { getProjects } from '@menewa/api-client';
import type { Project } from '@menewa/shared-types';
import { createProjectAction } from './projects/actions';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { getIdentityUser } from '@menewa/api-client';
import { logoutAction } from './logout/actions';

type HomePageProps = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

function ProjectCard({ project }: { project: Project }) {
  return (
    <article className="project-card">
      <div className="project-card__icon" aria-hidden="true">{project.name.slice(0, 1).toUpperCase()}</div>
      <div className="project-card__content">
        <h3>{project.name}</h3>
        <p>{project.description || 'Aucune description pour le moment.'}</p>
      </div>
      <time dateTime={project.createdAt}>
        {new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(new Date(project.createdAt))}
      </time>
    </article>
  );
}

export default async function HomePage({ searchParams }: HomePageProps) {
  const params = await searchParams;
  const token = (await cookies()).get('menewa_saas_session')?.value;
  if (!token) redirect('/login');
  let user;
  let projects: Project[] = [];
  let apiAvailable = true;

  try {
    user = await getIdentityUser(token);
    projects = await getProjects(token);
  } catch {
    apiAvailable = false;
  }
  if (!user) redirect('/login?error=session');

  return (
    <main className="dashboard">
      <aside className="sidebar">
        <a className="brand" href="/" aria-label="Menewa, accueil"><span className="brand__mark">m</span><span>menewa</span></a>
        <p className="sidebar__label">Espace de travail</p>
        <a className="sidebar__link sidebar__link--active" href="/"><span aria-hidden="true">▦</span> Projets</a>
        <div className="sidebar__footer"><span className={`connection-dot ${apiAvailable ? 'is-online' : ''}`} />{apiAvailable ? 'API connectée' : 'API indisponible'}</div>
      </aside>

      <section className="workspace">
        <header className="topbar"><span>Mon espace · {user.name || user.phone}</span><form action={logoutAction}><button className="secondary">Se déconnecter</button></form><div className="avatar" aria-label={`Compte ${user.name || user.phone}`}>{(user.name || 'M').slice(0, 1).toUpperCase()}</div></header>
        <div className="content">
          <div className="page-heading">
            <div><p className="eyebrow">VOTRE ACTIVITÉ</p><h1>Projets</h1><p className="page-description">Organisez et suivez vos projets au même endroit.</p></div>
            <span className="project-count">{projects.length} projet{projects.length > 1 ? 's' : ''}</span>
          </div>

          {params.created === '1' && <p className="notice notice--success" role="status">Le projet a été créé.</p>}
          {params.error === 'api' && <p className="notice notice--error" role="alert">Impossible de contacter l’API. Vérifiez que le backend est démarré.</p>}
          {!apiAvailable && <p className="notice notice--error" role="status">Démarrez le backend sur <code>localhost:8080</code> pour charger et enregistrer les projets.</p>}

          <div className="project-layout">
            <section className="project-list" aria-labelledby="project-list-title">
              <div className="section-heading"><div><h2 id="project-list-title">Tous les projets</h2><p>La liste est synchronisée avec votre espace Menewa.</p></div></div>
              {projects.length > 0 ? (
                <div className="project-cards">{projects.map((project) => <ProjectCard key={project.id} project={project} />)}</div>
              ) : (
                <div className="empty-state"><span className="empty-state__icon" aria-hidden="true">✳</span><h3>{apiAvailable ? 'Votre premier projet commence ici' : 'Vos projets apparaîtront ici'}</h3><p>{apiAvailable ? 'Créez un projet avec le formulaire pour le voir apparaître dans cette liste.' : 'La liste se chargera dès que l’API sera disponible.'}</p></div>
              )}
            </section>

            <aside className="create-panel">
              <div className="create-panel__heading"><span className="create-panel__icon" aria-hidden="true">＋</span><div><h2>Nouveau projet</h2><p>Ajoutez un projet à votre espace.</p></div></div>
              <form action={createProjectAction} className="project-form">
                <label htmlFor="name">Nom du projet</label>
                <input id="name" name="name" placeholder="Ex. Refonte du site" maxLength={120} required />
                <label htmlFor="description">Description <span>(facultatif)</span></label>
                <textarea id="description" name="description" placeholder="Quel est l’objectif de ce projet ?" rows={4} maxLength={1000} />
                <button type="submit">Créer le projet <span aria-hidden="true">→</span></button>
              </form>
            </aside>
          </div>
        </div>
      </section>
    </main>
  );
}
