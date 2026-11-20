import { redirect } from 'next/navigation';

/** Le catalogue public est présenté sur la page Social Kitchen. */
export default function CatalogPage() {
  redirect('/social-kitchen');
}
