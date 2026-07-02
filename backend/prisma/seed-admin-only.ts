/**
 * Production-safe: create/update ONE admin user only. Does NOT delete products/orders.
 * Links admin to the catalog company (`demo-company`) when products were imported via
 * `npm run products:replace-from-template-xls`.
 *
 * Usage: set SEED_ADMIN_EMAIL + SEED_ADMIN_PASSWORD in env, then:
 *   npx ts-node --project prisma/tsconfig.seed.json prisma/seed-admin-only.ts
 */
import 'dotenv/config';
import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient, UserRole, UserStatus } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { Pool } from 'pg';

const url = process.env.DATABASE_URL;
if (!url) {
  throw new Error('DATABASE_URL is required');
}

const pool = new Pool({ connectionString: url });
const prisma = new PrismaClient({ adapter: new PrismaPg(pool) });

/** Must match `scripts/replace-products-from-sheet.ts` catalog company id. */
const CATALOG_COMPANY_ID =
  (process.env.CATALOG_COMPANY_ID ?? 'demo-company').trim();

async function main(): Promise<void> {
  const adminEmail = (process.env.SEED_ADMIN_EMAIL ?? 'madhukar@techfylabs.com')
    .trim()
    .toLowerCase();
  const adminPasswordPlain = process.env.SEED_ADMIN_PASSWORD?.trim();
  if (!adminPasswordPlain || adminPasswordPlain.length < 8) {
    throw new Error('Set SEED_ADMIN_PASSWORD (min 8 chars) in .env.production');
  }
  const adminName = (process.env.SEED_ADMIN_NAME ?? 'FlashMart Admin').trim();
  const companyName =
    (process.env.SEED_COMPANY_NAME ?? 'FlashMart Distribution').trim();

  const adminPassword = await bcrypt.hash(adminPasswordPlain, 10);

  const existingAdmin = await prisma.user.findUnique({
    where: { email: adminEmail },
    include: { company: true },
  });

  const catalogProductCount = await prisma.product.count({
    where: { companyId: CATALOG_COMPANY_ID },
  });

  const company =
    catalogProductCount > 0
      ? await prisma.company.upsert({
          where: { id: CATALOG_COMPANY_ID },
          update: { name: companyName },
          create: { id: CATALOG_COMPANY_ID, name: companyName },
        })
      : existingAdmin?.company ??
        (await prisma.company.create({
          data: { name: companyName },
        }));

  if (catalogProductCount > 0) {
    console.log(
      `Catalog company: ${company.name} (${company.id}) — ${catalogProductCount} products`,
    );
    const realigned = await prisma.user.updateMany({
      where: { companyId: { not: company.id } },
      data: { companyId: company.id },
    });
    if (realigned.count > 0) {
      console.log(`Realigned ${realigned.count} user(s) to catalog company.`);
    }
  }

  const admin = await prisma.user.upsert({
    where: { email: adminEmail },
    update: {
      name: adminName,
      password: adminPassword,
      role: UserRole.ADMIN,
      companyId: company.id,
      status: UserStatus.ACTIVE,
    },
    create: {
      name: adminName,
      email: adminEmail,
      password: adminPassword,
      role: UserRole.ADMIN,
      companyId: company.id,
      status: UserStatus.ACTIVE,
    },
  });

  console.log('Admin ready:', admin.email, 'role:', admin.role, 'status:', admin.status);
  console.log('Company:', company.name, '(', company.id, ')');
}

void main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
    await pool.end();
  });
