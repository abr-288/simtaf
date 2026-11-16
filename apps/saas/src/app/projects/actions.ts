'use server';

import { createProject } from '@menewa/api-client';
import { redirect } from 'next/navigation';
import { cookies } from 'next/headers';

export async function createProjectAction(formData: FormData) {
  const name = String(formData.get('name') ?? '').trim();
  const description = String(formData.get('description') ?? '').trim();

  if (!name) redirect('/?error=api');
  const token = (await cookies()).get('menewa_saas_session')?.value;
  if (!token) redirect('/login');

  try {
    await createProject({ name, description: description || null }, token);
  } catch {
    redirect('/?error=api');
  }

  redirect('/?created=1');
}
