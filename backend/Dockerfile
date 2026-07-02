FROM node:22-slim AS builder
WORKDIR /app
ENV DATABASE_URL="postgresql://placeholder:placeholder@localhost:5432/placeholder?schema=public"

COPY package*.json ./
COPY prisma ./prisma
COPY prisma.config.ts ./prisma.config.ts
RUN npm ci

COPY . .
RUN npm run build
RUN npm prune --omit=dev

FROM node:22-slim AS runner
WORKDIR /app

ENV NODE_ENV=production

COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/public ./public
COPY --from=builder /app/prisma ./prisma
COPY --from=builder /app/prisma.config.ts ./prisma.config.ts
COPY --from=builder /app/package*.json ./

EXPOSE 8080
# The limited runtime DB user cannot run DDL, so migrations are applied
# out-of-band as the postgres owner via the Cloud SQL proxy:
#   ./scripts/prod-migrate-recover-via-proxy.sh   (recover a failed migration)
#   ./scripts/prod-db-setup-via-proxy.sh          (apply pending migrations)
CMD ["node", "dist/src/main.js"]
