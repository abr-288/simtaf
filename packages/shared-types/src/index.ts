export type ApiError = { code: string; message: string };
export type HealthStatus = { status: 'UP' | 'DOWN'; service: string };
export type Project = {
  id: string;
  name: string;
  description: string | null;
  createdAt: string;
};
export type CreateProjectInput = Pick<Project, 'name' | 'description'>;
