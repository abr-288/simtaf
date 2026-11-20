#!/usr/bin/env node
// Contrat front ↔ back. Source de vérité : le backend, exporté par `./gradlew test` (ContractExportTests) dans docs/api/.
//   1. types TypeScript des réponses        → packages/shared-types/src/openapi.generated.ts
//   2. référentiel des libellés (web)        → packages/ui/src/reference.generated.ts
//   3. référentiel des libellés (mobile)     → apps/mobile/lib/core/reference.g.dart
//   4. contrôle : chaque appel des clients TypeScript et Dart (chemin + méthode) existe dans le contrat.
// Usage : pnpm contract            (génère puis contrôle)
//         pnpm contract --check    (contrôle seulement, échoue si un fichier généré n'est pas à jour — CI)
import { readFileSync, writeFileSync, readdirSync, statSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const checkOnly = process.argv.includes('--check');
const read = (p) => readFileSync(join(root, p), 'utf8');
const openapi = JSON.parse(read('docs/api/openapi.json'));
const reference = JSON.parse(read('docs/api/reference.json'));
let failures = 0;

function emit(path, content) {
  const full = join(root, path);
  let current = null;
  try { current = readFileSync(full, 'utf8'); } catch { /* nouveau fichier */ }
  if (current === content) return console.log(`= ${path}`);
  if (checkOnly) { failures++; return console.error(`✗ ${path} n'est pas à jour : lancez « pnpm contract ».`); }
  writeFileSync(full, content);
  console.log(`✓ ${path}`);
}

/* 1. Types TypeScript générés depuis OpenAPI */
const otsDir = join(root, 'packages/shared-types/node_modules/openapi-typescript/dist/index.mjs');
const { default: openapiTS, astToString } = await import(pathToFileURL(otsDir).href);
const ast = await openapiTS(openapi, { alphabetize: true });
emit('packages/shared-types/src/openapi.generated.ts',
  `/* eslint-disable */\n// Généré par scripts/contract.mjs depuis docs/api/openapi.json — ne pas modifier à la main.\n\n${astToString(ast)}`);

/* 2. Référentiel (web) */
emit('packages/ui/src/reference.generated.ts',
  `// Généré par scripts/contract.mjs depuis docs/api/reference.json (GET /api/v1/reference) — ne pas modifier à la main.\n\n` +
  `export const REFERENCE = ${JSON.stringify(reference, null, 2)} as const;\n`);

/* 3. Référentiel (mobile) */
const dart = (s) => `'${String(s).replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\$/g, '\\$')}'`;
const dartStatuses = Object.entries(reference.statuses).map(([group, values]) =>
  `  ${dart(group)}: {\n${Object.entries(values).map(([k, v]) => `    ${dart(k)}: (label: ${dart(v.label)}, tone: ${dart(v.tone)}),`).join('\n')}\n  },`).join('\n');
const dartLabels = Object.entries(reference.labels).map(([group, values]) =>
  `  ${dart(group)}: {\n${Object.entries(values).map(([k, v]) => `    ${dart(k)}: ${dart(v)},`).join('\n')}\n  },`).join('\n');
emit('apps/mobile/lib/core/reference.g.dart',
  `// Généré par scripts/contract.mjs depuis docs/api/reference.json (GET /api/v1/reference) — ne pas modifier à la main.\n\n` +
  `/// Statuts métier : libellé et tonalité (green, amber, red, blue, indigo, slate).\n` +
  `const referenceStatuses = <String, Map<String, ({String label, String tone})>>{\n${dartStatuses}\n};\n\n` +
  `/// Libellés des codes métier (catégories, rôles, moyens de paiement…).\n` +
  `const referenceLabels = <String, Map<String, String>>{\n${dartLabels}\n};\n\n` +
  `const selfRegisterRoles = <String>[${reference.selfRegisterRoles.map(dart).join(', ')}];\n`);

/* 4. Contrôle des appels clients */
const contract = new Set();
for (const [path, ops] of Object.entries(openapi.paths)) {
  for (const method of Object.keys(ops)) contract.add(`${method.toUpperCase()} ${path.replace(/\{[^}]+\}/g, '{}')}`);
}

/** Extrait l'argument chaîne (quote ou template) qui commence à `i`, en gérant `${…}` imbriqués. */
function readStringLiteral(src, i) {
  const quote = src[i];
  let out = '', depth = 0;
  for (let j = i + 1; j < src.length; j++) {
    const c = src[j];
    if (depth === 0 && c === quote) return { value: out, end: j + 1 };
    if (c === '$' && src[j + 1] === '{') { depth++; out += '${'; j++; continue; }   // gabarit TS ou chaîne Dart
    if (depth > 0 && c === '}') { depth--; out += '}'; continue; }
    out += c;
  }
  throw new Error('Chaîne non terminée');
}

/** Remplace chaque interpolation `${…}` (accolades équilibrées) : paramètre de chemin → `{}`, chaîne de requête → supprimée. */
function normalizePath(raw) {
  let out = '';
  for (let i = 0; i < raw.length; i++) {
    if (raw[i] === '$' && raw[i + 1] === '{') {
      let depth = 1, j = i + 2;
      for (; j < raw.length && depth > 0; j++) { if (raw[j] === '{') depth++; else if (raw[j] === '}') depth--; }
      const inner = raw.slice(i + 2, j - 1);
      out += /^qs\(|\?/.test(inner) ? '' : '{}';
      i = j - 1;
    } else if (raw[i] === '$' && /[A-Za-z_]/.test(raw[i + 1] ?? '')) {   // interpolation Dart simple : $id
      let j = i + 1;
      while (j < raw.length && /\w/.test(raw[j])) j++;
      out += '{}';
      i = j - 1;
    } else out += raw[i];
  }
  return '/api' + out.replace(/\?.*$/, '');
}

const calls = [];
const tsSrc = read('packages/api-client/src/index.ts');
for (const m of tsSrc.matchAll(/request<[^(]*?>\(\s*(['`])/g)) {
  const { value, end } = readStringLiteral(tsSrc, m.index + m[0].length - 1);
  const rest = tsSrc.slice(end, tsSrc.indexOf('\n', end) + 1);
  const explicit = rest.match(/method:\s*'([A-Z]+)'/);
  const method = explicit ? explicit[1] : /\b(body|form)\b/.test(rest) ? 'POST' : 'GET';
  calls.push({ client: 'TypeScript', call: `${method} ${normalizePath(value)}` });
}

const dartFiles = [];
(function walk(dir) {
  for (const f of readdirSync(dir)) {
    const p = join(dir, f);
    if (statSync(p).isDirectory()) walk(p); else if (p.endsWith('.dart') && !p.endsWith('.g.dart')) dartFiles.push(p);
  }
})(join(root, 'apps/mobile/lib'));
for (const file of dartFiles) {
  const src = readFileSync(file, 'utf8');
  for (const m of src.matchAll(/\.(get|post|put|delete|multipart)\(\s*'\//g)) {
    const { value } = readStringLiteral(src, m.index + m[0].length - 2);
    calls.push({ client: 'Dart', call: `${m[1] === 'multipart' ? 'POST' : m[1].toUpperCase()} ${normalizePath(value)}` });
  }
}

const missing = calls.filter((c) => !contract.has(c.call));
for (const c of missing) console.error(`✗ appel ${c.client} absent du contrat : ${c.call}`);
failures += missing.length;
const used = new Set(calls.map((c) => c.call));
console.log(`Contrat : ${contract.size} opérations · appels vérifiés : ${calls.length} (TypeScript ${calls.filter((c) => c.client === 'TypeScript').length}, Dart ${calls.filter((c) => c.client === 'Dart').length}) · opérations utilisées : ${[...contract].filter((c) => used.has(c)).length}`);
if (failures) { console.error(`${failures} problème(s).`); process.exit(1); }
console.log('Contrat respecté.');
