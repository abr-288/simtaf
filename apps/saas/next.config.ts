import type { NextConfig } from 'next';
const nextConfig: NextConfig = { transpilePackages: ['@menewa/api-client', '@menewa/shared-types'] };
export default nextConfig;
