import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  transpilePackages: ['@menewa/api-client', '@menewa/shared-types', '@menewa/ui'],
  experimental: { serverActions: { bodySizeLimit: '10mb' } },
};

export default nextConfig;
