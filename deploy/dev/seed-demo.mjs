// Demande d'exemple de Menewa Naya (seed.ts) créée via l'API, après scripts/seed-demo.sql.
// Usage (réseau Docker interne) : API=http://backend:8080/api node seed-demo.mjs — sans effet si le donateur a déjà une demande.
const API = process.env.API ?? 'http://localhost:8080/api';

async function api(path, { token, method, body } = {}) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const res = await fetch(API + path, { method: method ?? (body !== undefined ? 'POST' : 'GET'), headers, body: body !== undefined ? JSON.stringify(body) : undefined });
  const text = await res.text();
  const json = text ? JSON.parse(text) : null;
  if (!res.ok) throw new Error(`${path} → ${res.status} ${json?.detail ?? text}`);
  return json;
}

async function login(phone) {
  const { devCode } = await api('/auth/otp/request', { body: { phone, mode: 'login' } });
  return (await api('/auth/otp/verify', { body: { phone, code: devCode } })).accessToken;
}

const admin = await login('+22890000001');
const donor = await login('+22890000002');
if ((await api('/v1/requests', { token: donor })).length > 0) {
  console.log('Demande de démonstration déjà présente.');
  process.exit(0);
}
const item = (await api('/catalog')).items.find((i) => i.slug === 'pack-alimentaire-famille');
await api('/v1/cart', { token: donor, body: { catalogItemId: item.id, quantity: 2, target: 'Famille' } });
const created = await api('/v1/cart/checkout', {
  token: donor,
  body: { targetName: 'Famille Mensah', targetZone: 'Kpalimé', schedule: { recurrence: 'none', paymentMode: 'immediate' }, annex: {}, idempotencyKey: 'seed-demo' },
});
await api(`/v1/admin/requests/${created.id}/decision`, { token: admin, body: { decision: 'accept' } });
const ben = (await api('/v1/admin/beneficiaries', { token: admin })).find((b) => b.fullName === 'Famille Mensah');
if (ben) await api(`/v1/admin/requests/${created.id}/beneficiary`, { token: admin, method: 'PUT', body: { beneficiaryId: ben.id } });
const yao = (await api('/v1/admin/providers/options', { token: admin })).find((p) => p.name.startsWith('Yao'));
const due = new Date(Date.now() + 5 * 86400000).toISOString().slice(0, 10);
await api(`/v1/admin/requests/${created.id}/missions`, {
  token: admin,
  body: {
    title: 'Livrer 2 packs alimentaires à la famille Mensah', providerId: yao.id, dueDate: due, compensationMode: 'money', providerFee: 8000,
    instructions: 'Remise en main propre, photo avec la famille (avec consentement) et reçu signé.',
  },
});
console.log(`Demande ${created.ref} créée, acceptée et missionnée.`);
