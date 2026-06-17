// Load .env before env() runs (Prisma does not inject DATABASE_URL into this file automatically).
import 'dotenv/config';
import { defineConfig, env } from 'prisma/config';

export default defineConfig({
  schema: 'prisma/schema.prisma',
  migrations: {
    path: 'prisma/migrations',
    seed:
      'prisma generate && ts-node --project prisma/tsconfig.seed.json prisma/seed.ts',
  },
  datasource: {
    url: env('DATABASE_URL'),
  },
});
