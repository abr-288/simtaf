import { expect, test } from '@playwright/test';
import { SAAS } from './env';

test.describe('Site public', () => {
  test('accueil, Social Kitchen et connexion sont accessibles', async ({ page }) => {
    await page.goto(SAAS + '/');
    await expect(page.getByRole('heading', { level: 1 })).toContainText('on fait la différence');
    await page.goto(SAAS + '/social-kitchen');
    await expect(page.getByText('Le don qui devient action')).toBeVisible();
    await expect(page.locator('.card').first()).toBeVisible();
    await page.goto(SAAS + '/connexion');
    await expect(page.getByRole('heading', { name: 'Se connecter' })).toBeVisible();
  });

  test('les espaces membres exigent une connexion', async ({ page }) => {
    await page.goto(SAAS + '/espace/demandes');
    await expect(page).toHaveURL(/\/connexion$/);
  });
});
