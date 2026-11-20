import { randomUUID } from 'node:crypto';
import { getPublicSettings, getTargets } from '@menewa/api-client';
import { ActionForm, PageHeader, Section } from '@menewa/ui';
import { AnnexFields, ScheduleFields, TargetSelect } from '@/components/RequestFields';
import { requireRole } from '@/lib/session';
import { createCustomRequest } from '../../actions';

export default async function Page() {
  const { user: u } = await requireRole('donor', 'client');
  const [settings, targets] = await Promise.all([getPublicSettings(), getTargets()]);
  return (
    <>
      <PageHeader title="Don personnel / action ou visite" subtitle="Définissez librement votre don ou l’acte à poser (visite, remise de message, action de bienfaisance…). L’équipe pourra ajuster le budget lors de l’examen." />
      <Section title="Ma demande">
        <ActionForm action={createCustomRequest} submit="Envoyer la demande">
          <input type="hidden" name="idempotencyKey" value={randomUUID()} />
          <div className="grid gap-3 sm:grid-cols-2">
            <div><label className="label" htmlFor="kind">Nature</label><select id="kind" name="kind" className="input"><option value="personnalise">Don selon mes spécificités</option><option value="action">Acte à poser / visite à effectuer</option></select></div>
            <div><label className="label" htmlFor="title">Intitulé</label><input id="title" name="title" className="input" placeholder="Ex. Repas de fête pour l’orphelinat" required /></div>
          </div>
          <div><label className="label" htmlFor="description">Description détaillée</label><textarea id="description" name="description" rows={4} className="input" placeholder="Que souhaitez-vous offrir ou faire ? Pour qui ? Dans quelles conditions ?" /></div>
          <div className="grid gap-3 sm:grid-cols-3">
            <div><label className="label" htmlFor="targetName">Cible du don</label><TargetSelect id="targetName" name="targetName" targets={targets} allowCustom={u.role === 'donor'} required /></div>
            <div><label className="label" htmlFor="targetZone">Zone / ville</label><input id="targetZone" name="targetZone" className="input" /></div>
            <div><label className="label" htmlFor="amount">Budget par échéance (FCFA)</label><input id="amount" name="amount" type="number" min={500} step={500} className="input" required /></div>
          </div>
          <ScheduleFields />
          <AnnexFields prices={settings['annex.prices'] ?? {}} />
        </ActionForm>
      </Section>
    </>
  );
}
