import { defineConfig } from '@playwright/test';

/**
 * Tests de bout en bout dans un vrai navigateur.
 *   SAAS_URL / ADMIN_URL : applications testées (par défaut la zone de dev publique) ;
 *   DEMO_USER / DEMO_PASSWORD : mot de passe d'accès de la zone de dev (authentification HTTP), si elle est protégée.
 * La zone testée doit être en mode démonstration (code OTP affiché à l'écran).
 */
const httpCredentials = process.env.DEMO_PASSWORD
  ? { username: process.env.DEMO_USER ?? 'menewa', password: process.env.DEMO_PASSWORD }
  : undefined;

export default defineConfig({
  testDir: './tests',
  timeout: 90_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: { httpCredentials, locale: 'fr-FR', trace: 'retain-on-failure', screenshot: 'only-on-failure' },
  projects: [{ name: 'chromium', use: { browserName: 'chromium' } }],
});
