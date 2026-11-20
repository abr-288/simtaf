# Apps Next.js du monorepo (admin ou saas) : docker build --build-arg APP=admin -f deploy/dev/web.Dockerfile .
FROM node:22-alpine
ARG APP
ENV NEXT_TELEMETRY_DISABLED=1 CI=true
RUN corepack enable && corepack prepare pnpm@10.22.0 --activate
WORKDIR /repo
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc turbo.json ./
COPY packages packages
COPY apps/admin apps/admin
COPY apps/saas apps/saas
COPY e2e/package.json e2e/package.json
RUN pnpm install --frozen-lockfile
RUN pnpm --filter @menewa/${APP} build
ENV NODE_ENV=production APP=${APP}
EXPOSE 3000 3001
CMD ["sh", "-c", "pnpm --filter @menewa/${APP} start"]
