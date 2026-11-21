import { expect, test } from '@playwright/test';
import { SAAS, uniquePhone } from './env';

test('un don choisi dans le simulateur arrive dans le panier après inscription', async ({ page }) => {
  await page.goto(SAAS + '/#simulateur');
  const simulator = page.locator('#simulateur');
  await simulator.getByRole('button', { name: '50 000 FCFA', exact: true }).click();
  // Premier don finançable de la liste
  const card = simulator.locator('li button[aria-pressed]:not([disabled])').first();
  const title = (await card.locator('span.font-bold').first().innerText()).trim();
  await card.click();
  await simulator.getByRole('button', { name: /^Financer / }).click();

  // Visiteur non connecté : connexion, puis inscription ; le don reste en attente.
  await expect(page).toHaveURL(/\/connexion\?raison=simulateur$/);
  await page.getByRole('link', { name: 'Inscrivez-vous' }).click();
  await page.getByLabel('Nom complet').fill('Donateur Simulateur');
  await page.getByLabel('Je m’inscris en tant que').selectOption('donor');
  await page.getByLabel('Numéro de téléphone').fill(uniquePhone());
  await page.getByRole('button', { name: 'Recevoir mon code' }).click();
  const notice = page.getByText(/code\s*:/i).filter({ has: page.locator('strong') }).first();
  await page.getByLabel('Code à 6 chiffres').fill((await notice.locator('strong').innerText()).replace(/\D/g, ''));
  await page.getByRole('button', { name: 'Valider et accéder' }).click();

  await expect(page).toHaveURL(/\/espace\/panier\?ajout=simulateur$/);
  await expect(page.getByRole('status')).toContainText('ajouté à votre panier');
  await expect(page.getByText(title, { exact: true })).toBeVisible();
});
