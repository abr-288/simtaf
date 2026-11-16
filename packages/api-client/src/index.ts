import type { AuthSession, CreateProjectInput, HealthStatus, IdentityUser, ManagedOtpRequestInput, OtpRequestResult, Project, SaveCatalogItemInput } from '@menewa/shared-types';

const apiBaseUrl = process.env.API_BASE_URL ?? process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080/api';

export async function getHealth(): Promise<HealthStatus> {
  const response = await fetch(`${apiBaseUrl}/health`, { cache: 'no-store' });
  if (!response.ok) throw new Error(`API health request failed: ${response.status}`);
  return response.json() as Promise<HealthStatus>;
}

export async function getProjects(accessToken?: string): Promise<Project[]> {
  const response = await fetch(`${apiBaseUrl}/projects`, { cache: 'no-store', headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {} });
  if (!response.ok) throw new Error(`Project list request failed: ${response.status}`);
  return response.json() as Promise<Project[]>;
}

export async function createProject(input: CreateProjectInput, accessToken?: string): Promise<Project> {
  const response = await fetch(`${apiBaseUrl}/projects`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}) },
    body: JSON.stringify(input),
    cache: 'no-store',
  });
  if (!response.ok) throw new Error(`Project creation failed: ${response.status}`);
  return response.json() as Promise<Project>;
}

async function identityRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${apiBaseUrl}/auth/${path}`, { ...init, cache: 'no-store' });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const detail = body.detail ?? body.message ?? body.error ?? `HTTP ${response.status}`;
    throw new Error(String(detail));
  }
  return body as T;
}

export function requestOtp(input: OtpRequestInput): Promise<OtpRequestResult> {
  return identityRequest('otp/request', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(input) });
}

export function verifyOtp(phone: string, code: string): Promise<AuthSession> {
  return identityRequest('otp/verify', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ phone, code }) });
}

export function getIdentityUser(accessToken: string): Promise<IdentityUser> {
  return identityRequest('me', { headers: { Authorization: `Bearer ${accessToken}` } });
}

export function getIdentityPermissions(accessToken: string): Promise<string[]> {
  return identityRequest('permissions', { headers: { Authorization: `Bearer ${accessToken}` } });
}

export function revokeIdentitySession(accessToken: string): Promise<void> {
  return identityRequest('logout', { method: 'POST', headers: { Authorization: `Bearer ${accessToken}` } });
}
