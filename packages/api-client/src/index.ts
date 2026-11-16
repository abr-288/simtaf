import type { CreateProjectInput, HealthStatus, Project } from '@menewa/shared-types';

const apiBaseUrl = process.env.API_BASE_URL ?? process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080/api';

export async function getHealth(): Promise<HealthStatus> {
  const response = await fetch(`${apiBaseUrl}/health`, { cache: 'no-store' });
  if (!response.ok) throw new Error(`API health request failed: ${response.status}`);
  return response.json() as Promise<HealthStatus>;
}

export async function getProjects(): Promise<Project[]> {
  const response = await fetch(`${apiBaseUrl}/projects`, { cache: 'no-store' });
  if (!response.ok) throw new Error(`Project list request failed: ${response.status}`);
  return response.json() as Promise<Project[]>;
}

export async function createProject(input: CreateProjectInput): Promise<Project> {
  const response = await fetch(`${apiBaseUrl}/projects`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
    cache: 'no-store',
  });
  if (!response.ok) throw new Error(`Project creation failed: ${response.status}`);
  return response.json() as Promise<Project>;
}
