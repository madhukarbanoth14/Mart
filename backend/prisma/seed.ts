import 'dotenv/config';
import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient, BrandType, Prisma, UserRole } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { Pool } from 'pg';

const url = process.env.DATABASE_URL;
if (!url) {
  throw new Error('DATABASE_URL is required to run the seed script');
}

const pool = new Pool({ connectionString: url });
const prisma = new PrismaClient({ adapter: new PrismaPg(pool) });

async function main(): Promise<void> {
  const defaultPassword = await bcrypt.hash('Password@123', 10);

  const company = await prisma.company.upsert({
    where: { id: 'demo-company' },
    update: {},
    create: {
      id: 'demo-company',
      name: 'Mart Demo Distribution Pvt Ltd',
    },
  });

  await prisma.user.upsert({
    where: { email: 'admin@martdemo.com' },
    update: {},
    create: {
      name: 'Super Admin',
      email: 'admin@martdemo.com',
      phone: '9000000001',
      password: defaultPassword,
      role: UserRole.ADMIN,
      companyId: company.id,
    },
  });

  await prisma.user.upsert({
    where: { email: 'employee@martdemo.com' },
    update: {},
    create: {
      name: 'Field Employee',
      email: 'employee@martdemo.com',
      phone: '9000000002',
      password: defaultPassword,
      role: UserRole.EMPLOYEE,
      companyId: company.id,
    },
  });

  const dealer = await prisma.user.upsert({
    where: { email: 'dealer@martdemo.com' },
    update: {},
    create: {
      name: 'City Dealer',
      email: 'dealer@martdemo.com',
      phone: '9000000003',
      password: defaultPassword,
      role: UserRole.DEALER,
      companyId: company.id,
    },
  });

  const area = await prisma.area.upsert({
    where: { id: 'area-central' },
    update: { dealerId: dealer.id },
    create: {
      id: 'area-central',
      name: 'Central Zone',
      dealerId: dealer.id,
      companyId: company.id,
    },
  });

  await prisma.user.upsert({
    where: { email: 'shop1@martdemo.com' },
    update: { areaId: area.id },
    create: {
      name: 'Shopkeeper One',
      email: 'shop1@martdemo.com',
      phone: '9000000004',
      password: defaultPassword,
      role: UserRole.SHOPKEEPER,
      areaId: area.id,
      companyId: company.id,
    },
  });

  await prisma.user.upsert({
    where: { email: 'shop2@martdemo.com' },
    update: { areaId: area.id },
    create: {
      name: 'Shopkeeper Two',
      email: 'shop2@martdemo.com',
      phone: '9000000005',
      password: defaultPassword,
      role: UserRole.SHOPKEEPER,
      areaId: area.id,
      companyId: company.id,
    },
  });

  const productSeed = Array.from({ length: 10 }).map((_, index) => {
    const isOwn = index < 6;
    return {
      id: `demo-product-${index + 1}`,
      name: `Product ${index + 1}`,
      brandType: isOwn ? BrandType.OWN : BrandType.OTHER,
      basePrice: new Prisma.Decimal(80 + index * 12),
      gstPercentage: new Prisma.Decimal(18),
      dealerDiscount: new Prisma.Decimal(10),
      shopkeeperDiscount: new Prisma.Decimal(5),
    };
  });

  const products = await Promise.all(
    productSeed.map((product) =>
      prisma.product.upsert({
        where: { id: product.id },
        update: {},
        create: {
          ...product,
          companyId: company.id,
        },
      }),
    ),
  );

  await Promise.all(
    products.map((product, idx) =>
      prisma.stock.upsert({
        where: {
          dealerId_productId: {
            dealerId: dealer.id,
            productId: product.id,
          },
        },
        update: {
          quantity: 60 + idx * 5,
        },
        create: {
          companyId: company.id,
          dealerId: dealer.id,
          productId: product.id,
          quantity: 60 + idx * 5,
        },
      }),
    ),
  );

  console.log({
    seeded: true,
    logins: {
      admin: 'admin@martdemo.com',
      employee: 'employee@martdemo.com',
      dealer: 'dealer@martdemo.com',
      shopkeepers: ['shop1@martdemo.com', 'shop2@martdemo.com'],
      password: 'Password@123',
    },
    productIds: products.map((p) => p.id),
  });
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
