import { expect, test, type Page } from '@playwright/test';
import { ADMIN, ADMIN_PHONE, SAAS, uniquePhone } from './env';

/** Lit le code de démonstration affiché à l'écran après la demande d'OTP. */
async function readDemoCode(page: Page) {
  const notice = page.getByText(/code\s*:/i).filter({ has: page.locator('strong') }).first();
  await expect(notice).toBeVisible();
  return (await notice.locator('strong').innerText()).replace(/\D/g, '');
}

test('un donateur s’inscrit, fait un don et l’administration l’accepte', async ({ browser }) => {
  // 1. Inscription du donateur (code SMS affiché en mode démonstration)
  const donor = await (await browser.newContext()).newPage();
  await donor.goto(SAAS + '/inscription');
  await donor.getByLabel('Nom complet').fill('Donateur E2E');
  await donor.getByLabel('Je m’inscris en tant que').selectOption('donor');
  await donor.getByLabel('Numéro de téléphone').fill(uniquePhone());
  await donor.getByRole('button', { name: 'Recevoir mon code' }).click();
  await donor.getByLabel('Code à 6 chiffres').fill(await readDemoCode(donor));
  await donor.getByRole('button', { name: 'Valider et accéder' }).click();
  await expect(donor).toHaveURL(/\/espace$/);
  await expect(donor.getByRole('heading', { level: 1 })).toContainText('Bonjour Donateur E2E');

  // 2. Don depuis le catalogue
  await donor.goto(SAAS + '/espace/catalogue');
  await donor.getByRole('button', { name: 'Ajouter au panier' }).first().click();
  await expect(donor.getByText('ajouté au panier').first()).toBeVisible();
  await donor.goto(SAAS + '/espace/panier');
  await donor.locator('#targetName').selectOption({ index: 1 });
  await donor.getByLabel('Zone / ville').fill('Lomé');
  await donor.getByRole('button', { name: 'Envoyer la demande' }).click();
  await expect(donor).toHaveURL(/\/espace\/demandes\/\d+$/);
  const subtitle = await donor.locator('h1 + p').innerText();
  const ref = subtitle.match(/MN-\d{4}-\d{5}/)?.[0];
  expect(ref, 'référence de la demande').toBeTruthy();
  await expect(donor.getByText('Soumise').first()).toBeVisible();

  // 3. L'administration accepte la demande
  const admin = await (await browser.newContext()).newPage();
  await admin.goto(ADMIN + '/login');
  await admin.getByLabel('Téléphone administrateur').fill(ADMIN_PHONE);
  await admin.getByRole('button', { name: 'Recevoir un code' }).click();
  await admin.getByLabel('Code à 6 chiffres').fill(await readDemoCode(admin));
  await admin.getByRole('button', { name: 'Vérifier' }).click();
  await expect(admin).toHaveURL(/\/admin$/);
  await admin.goto(ADMIN + `/admin/demandes?q=${ref}`);
  await admin.getByRole('link', { name: /×/ }).first().click();
  await admin.getByRole('button', { name: 'Accepter la demande' }).click();
  // Le formulaire de décision disparaît une fois la demande acceptée : on vérifie le suivi.
  await expect(admin.getByRole('region', { name: 'Suivi de la demande' }).getByRole('listitem').filter({ hasText: 'Acceptée' }))
    .toContainText('étape actuelle');

  // 4. Le donateur voit la décision et la notification
  await donor.reload();
  await expect(donor.getByText('Acceptée').first()).toBeVisible();
  await donor.goto(SAAS + '/espace/notifications');
  await expect(donor.getByText(`Votre demande ${ref} a été acceptée`)).toBeVisible();
});
