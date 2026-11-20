export const SAAS = process.env.SAAS_URL || 'https://menewa.169-58-93-234.sslip.io';
export const ADMIN = process.env.ADMIN_URL || 'https://menewa-admin.169-58-93-234.sslip.io';
export const ADMIN_PHONE = process.env.ADMIN_PHONE || '+22890000001';

/** Numéro unique par exécution (préfixe 99 réservé aux tests de bout en bout). */
export const uniquePhone = () => `+22899${String(Date.now()).slice(-6)}`;
