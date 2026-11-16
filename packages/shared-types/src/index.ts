export type ApiError = { code: string; message: string };
export type HealthStatus = { status: 'UP' | 'DOWN'; service: string };
export type Project = {
  id: string;
  name: string;
  description: string | null;
  createdAt: string;
};
export type CreateProjectInput = Pick<Project, 'name' | 'description'>;
export type IdentityUser = { id: number; phone: string; name: string; role: string; permissions: string[] };
export type OtpRequestInput = { phone: string; mode: 'login' | 'register'; name?: string; role?: string };
export type OtpRequestResult = { ok: true; devCode?: string; error?: never };
export type AuthSession = { accessToken: string; tokenType: 'Bearer'; expiresAt: string; user: IdentityUser };
