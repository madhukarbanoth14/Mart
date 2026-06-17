import 'dotenv/config';
import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient, UserRole } from '@prisma/client';
import { Pool } from 'pg';

const companyId = process.env.MART_COMPANY_ID ?? 'demo-company';
const defaultQty = Number.parseInt(process.env.MART_DEFAULT_STOCK_QTY ?? '200', 10);

async function main() {
  const pool = new Pool({ connectionString: process.env.DATABASE_URL });
  const prisma = new PrismaClient({ adapter: new PrismaPg(pool) });

  const dealers = await prisma.user.findMany({
    where: { companyId, role: UserRole.DEALER },
    select: { id: true, name: true },
  });
  const warehouseUsers = await prisma.user.findMany({
    where: { companyId, role: UserRole.ADMIN },
    select: { id: true, name: true },
  });
  const stockHolders = [...dealers, ...warehouseUsers];
  if (stockHolders.length === 0) {
    console.error(`No DEALER/ADMIN users for company ${companyId}. Run prisma seed first.`);
    process.exit(1);
  }

  const products = await prisma.product.findMany({
    where: { companyId },
    select: { id: true },
  });
  if (products.length === 0) {
    console.error(`No products for company ${companyId}. Import catalog first.`);
    process.exit(1);
  }

  let upserts = 0;
  for (const holder of stockHolders) {
    for (const product of products) {
      await prisma.stock.upsert({
        where: {
          dealerId_productId: {
            dealerId: holder.id,
            productId: product.id,
          },
        },
        update: { quantity: defaultQty },
        create: {
          companyId,
          dealerId: holder.id,
          productId: product.id,
          quantity: defaultQty,
        },
      });
      upserts += 1;
    }
  }

  console.log(
    `Stock seeded: ${upserts} rows (${products.length} SKUs × ${stockHolders.length} holders incl. warehouse admin, qty ${defaultQty} each).`,
  );
  await prisma.$disconnect();
  await pool.end();
}

void main().catch((e) => {
  console.error(e);
  process.exit(1);
});
