import Link from 'next/link';
import { getAudit } from '@menewa/api-client';
import { fdatetime, PageHeader, Table } from '@menewa/ui';
import { requirePerm } from '@/lib/session';

/** Familles d'actions (préfixe du code technique). */
const FAMILIES: Record<string, string> = {
  user: 'Comptes et connexions',
  request: 'Demandes',
  mission: 'Missions',
  proof: 'Preuves',
  payment: 'Paiements',
  installment: 'Échéances',
  beneficiary: 'Bénéficiaires',
  dispute: 'Litiges',
  catalog: 'Catalogue',
  target: 'Cibles',
  listing: 'Publications',
  service: 'Services communautaires',
  tokens: 'Tokens Mena',
  settings: 'Paramètres',
  report: 'Exports',
  file: 'Téléchargements de fichiers',
};

/** Libellés lisibles des actions ; le code technique reste affiché en dessous. */
const ACTIONS: Record<string, string> = {
  'user.register': 'Inscription', 'user.login': 'Connexion', 'user.logout': 'Déconnexion',
  'user.role': 'Changement de rôle', 'user.status': 'Changement de statut du compte',
  'request.create': 'Demande créée', 'request.accept': 'Demande acceptée', 'request.reject': 'Demande refusée',
  'request.info': 'Complément demandé', 'request.cancel': 'Demande annulée', 'request.completed': 'Demande réalisée',
  'request.closed': 'Demande clôturée', 'request.finance_update': 'Finances de la demande modifiées',
  'request.link_beneficiary': 'Bénéficiaire rattaché',
  'mission.create': 'Mission créée', 'mission.accept': 'Mission acceptée', 'mission.refuse': 'Mission refusée',
  'mission.complete': 'Mission terminée', 'mission.validate': 'Mission validée', 'mission.cancel': 'Mission annulée',
  'mission.reassign': 'Mission réattribuée',
  'proof.upload': 'Preuve déposée', 'proof.validate': 'Preuve validée', 'proof.reject': 'Preuve rejetée',
  'payment.declare': 'Paiement déclaré', 'payment.update': 'Paiement mis à jour', 'installment.done': 'Échéance réglée',
  'beneficiary.create': 'Bénéficiaire créé', 'beneficiary.view': 'Fiche bénéficiaire consultée',
  'beneficiary.list_view': 'Liste des bénéficiaires consultée',
  'dispute.open': 'Litige ouvert', 'dispute.update': 'Litige mis à jour',
  'catalog.create': 'Article créé', 'catalog.update': 'Article modifié', 'catalog.toggle': 'Article activé / désactivé',
  'target.create': 'Cible créée', 'target.update': 'Cible modifiée', 'target.delete': 'Cible supprimée',
  'listing.create': 'Publication créée', 'listing.published': 'Publication publiée', 'listing.closed': 'Publication clôturée', 'listing.rejected': 'Publication refusée',
  'service.order': 'Service commandé', 'service.agree': 'Service accepté',
  'tokens.adjust': 'Ajustement de tokens', 'tokens.transfer': 'Transfert de tokens', 'tokens.rule_create': 'Règle de tokens créée',
  'tokens.rule_update': 'Règle de tokens modifiée', 'tokens.rule_delete': 'Règle de tokens supprimée',
  'settings.update': 'Paramètre modifié', 'settings.home_photo': 'Photo de l’accueil modifiée', 'settings.home_photo_remove': 'Photo de l’accueil retirée',
  'payment.online_start': 'Paiement Mobile Money lancé', 'payment.online_paid': 'Paiement Mobile Money reçu', 'payment.online_failed': 'Paiement Mobile Money échoué',
  'payment.payout_start': 'Reversement lancé', 'payment.payout_paid': 'Reversement effectué', 'payment.payout_failed': 'Reversement échoué', 'report.export': 'Export de rapport', 'file.download': 'Fichier téléchargé',
};

type Search = { q?: string; action?: string; actorName?: string; from?: string; to?: string };

export default async function Page({ searchParams }: { searchParams: Promise<Search> }) {
  const { token } = await requirePerm('audit.view');
  const f = await searchParams;
  const rows = await getAudit(token, f);
  const filtered = Boolean(f.q || f.action || f.actorName || f.from || f.to);
  return (
    <>
      <PageHeader title="Journal d’audit" subtitle="Actions critiques et décisions administratives, horodatées et non modifiables depuis l’interface." />
      <form className="mb-2 grid gap-2 sm:grid-cols-2 lg:grid-cols-6">
        <label className="text-xs text-slate-600 lg:col-span-2">Recherche
          <input name="q" defaultValue={f.q} className="input" placeholder="Mot, n° d’objet (#12), référence…" />
        </label>
        <label className="text-xs text-slate-600">Type d’action
          <select name="action" defaultValue={f.action ?? ''} className="input">
            <option value="">Toutes</option>
            {Object.entries(FAMILIES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </label>
        <label className="text-xs text-slate-600">Auteur
          <input name="actorName" defaultValue={f.actorName} className="input" placeholder="Nom ou téléphone" />
        </label>
        <label className="text-xs text-slate-600">Du<input name="from" type="date" defaultValue={f.from} className="input" /></label>
        <label className="text-xs text-slate-600">Au<input name="to" type="date" defaultValue={f.to} className="input" /></label>
        <div className="flex items-center gap-3 sm:col-span-2 lg:col-span-6">
          <button className="btn btn-green btn-sm">Filtrer</button>
          {filtered && <Link href="/admin/audit" className="text-sm text-brand underline">Effacer les filtres</Link>}
        </div>
      </form>
      <p className="mb-3 text-xs text-slate-500">
        La recherche porte sur l’action, l’objet et son numéro, les détails et le rôle de l’auteur.
        {' '}{rows.length >= 300 ? '300 entrées affichées, les plus récentes : affinez les filtres pour remonter plus loin.' : `${rows.length} entrée${rows.length > 1 ? 's' : ''}.`}
      </p>
      <Table>
        <thead><tr><th>Date</th><th>Auteur</th><th>Action</th><th>Objet</th><th>Détails</th><th>IP</th></tr></thead>
        <tbody>
          {rows.length === 0 && <tr><td colSpan={6} className="text-center text-sm text-slate-500">Aucune entrée pour ces filtres.</td></tr>}
          {rows.map((a) => (
            <tr key={a.id}>
              <td className="whitespace-nowrap text-xs">{fdatetime(a.createdAt)}</td>
              <td>{a.actorName || 'système'}<br /><span className="text-xs text-slate-500">{a.actorRole}</span></td>
              <td><span className="text-sm font-semibold">{ACTIONS[a.action] ?? a.action}</span><br /><span className="font-mono text-xs text-slate-500">{a.action}</span></td>
              <td className="text-xs">{a.entity} {a.entityId && `#${a.entityId}`}</td>
              <td className="max-w-xs break-words text-xs text-slate-600">{a.details}</td>
              <td className="text-xs">{a.ip}</td>
            </tr>
          ))}
        </tbody>
      </Table>
    </>
  );
}
