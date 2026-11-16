'use server';

import { createProject } from '@menewa/api-client';
import { redirect } from 'next/navigation';

export async function createProjectAction(formData: FormData) {
  const name = String(formData.get('name') ?? '').trim();
  const description = String(formData.get('description') ?? '').trim();

  if (!name) redirect('/?error=api');

  try {
    await createProject({ name, description: description || null });
  } catch {
    redirect('/?error=api');
  }

  redirect('/?created=1');
}
