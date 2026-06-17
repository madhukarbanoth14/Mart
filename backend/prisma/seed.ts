import 'dotenv/config';
import { PrismaPg } from '@prisma/adapter-pg';
import {
  BrandType,
  Prisma,
  PrismaClient,
  ProductShelf,
  UserRole,
  UserStatus,
} from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { Pool } from 'pg';

const url = process.env.DATABASE_URL;
if (!url) {
  throw new Error('DATABASE_URL is required to run the seed script');
}

const pool = new Pool({ connectionString: url });
const prisma = new PrismaClient({ adapter: new PrismaPg(pool) });

async function seedStockForCompany(
  companyId: string,
  opts: { dealerQty?: number; warehouseQty?: number } = {},
): Promise<{ productCount: number; holderCount: number; rowCount: number }> {
  const dealerQty = opts.dealerQty ?? 25;
  const warehouseQty = opts.warehouseQty ?? 500;

  const products = await prisma.product.findMany({
    where: { companyId },
    select: { id: true },
  });
  if (products.length === 0) {
    return { productCount: 0, holderCount: 0, rowCount: 0 };
  }

  const holders = await prisma.user.findMany({
    where: {
      companyId,
      role: { in: [UserRole.ADMIN, UserRole.DEALER] },
    },
    select: { id: true, role: true },
  });
  if (holders.length === 0) {
    return { productCount: products.length, holderCount: 0, rowCount: 0 };
  }

  let rowCount = 0;
  for (const holder of holders) {
    const qty = holder.role === UserRole.ADMIN ? warehouseQty : dealerQty;
    for (const product of products) {
      await prisma.stock.upsert({
        where: {
          dealerId_productId: { dealerId: holder.id, productId: product.id },
        },
        update: { quantity: qty },
        create: {
          companyId,
          dealerId: holder.id,
          productId: product.id,
          quantity: qty,
        },
      });
      rowCount += 1;
    }
  }
  return { productCount: products.length, holderCount: holders.length, rowCount };
}

/** Local dev: demo-company catalog + dealer login for simulator/APK testing. */
async function ensureLegacyDemoDealerWithStock(): Promise<void> {
  const legacyCompanyId = 'demo-company';
  const company = await prisma.company.findUnique({ where: { id: legacyCompanyId } });
  if (!company) return;

  const productCount = await prisma.product.count({ where: { companyId: legacyCompanyId } });
  if (productCount === 0) return;

  const demoPassword = await bcrypt.hash('Password@123', 10);

  await prisma.user.upsert({
    where: { email: 'admin@martdemo.com' },
    update: {
      companyId: legacyCompanyId,
      role: UserRole.ADMIN,
      status: UserStatus.ACTIVE,
      password: demoPassword,
    },
    create: {
      id: 'demo-user-admin',
      name: 'Demo Admin',
      email: 'admin@martdemo.com',
      password: demoPassword,
      role: UserRole.ADMIN,
      companyId: legacyCompanyId,
      status: UserStatus.ACTIVE,
    },
  });

  await prisma.user.upsert({
    where: { email: 'dealer@martdemo.com' },
    update: {
      companyId: legacyCompanyId,
      role: UserRole.DEALER,
      status: UserStatus.ACTIVE,
      password: demoPassword,
    },
    create: {
      id: 'demo-user-dealer',
      name: 'City Dealer',
      email: 'dealer@martdemo.com',
      password: demoPassword,
      role: UserRole.DEALER,
      companyId: legacyCompanyId,
      status: UserStatus.ACTIVE,
    },
  });

  const stock = await seedStockForCompany(legacyCompanyId);
  console.log(
    `Legacy demo-company stock: ${stock.rowCount} rows (${stock.productCount} SKUs × ${stock.holderCount} holders)`,
  );
}

/** Canonical manufacturer names (stored on Brand.manufacturer) */
const MANUFACTURERS = [
  'Hindustan Unilever Limited',
  'Procter & Gamble',
  'ITC Limited',
  'Nestle India Ltd',
  'Wipro Enterprises',
  'Lohiya Edible Oils Pvt Ltd',
  'Gemini Edibles & Fats India Limited',
  'Patanjali Foods Limited',
  'Sri Sainath Agro Oils Private Limited',
] as const;

type BrandDef = {
  name: string;
  manufacturer: string;
  category: string;
};

const BRANDS: BrandDef[] = [
  { name: 'Horlicks', manufacturer: 'Hindustan Unilever Limited', category: 'Milk drinks' },
  { name: 'Boost', manufacturer: 'Hindustan Unilever Limited', category: 'Milk drinks' },
  { name: 'Tide', manufacturer: 'Procter & Gamble', category: 'Detergents' },
  { name: 'Ariel', manufacturer: 'Procter & Gamble', category: 'Detergents' },
  { name: 'Whisper', manufacturer: 'Procter & Gamble', category: 'Feminine & baby care' },
  { name: 'Pampers', manufacturer: 'Procter & Gamble', category: 'Feminine & baby care' },
  { name: 'Head & Shoulders', manufacturer: 'Procter & Gamble', category: 'Shampoos' },
  { name: 'Pantene', manufacturer: 'Procter & Gamble', category: 'Shampoos' },
  { name: 'Vicks', manufacturer: 'Procter & Gamble', category: 'General care' },
  { name: 'Gillette', manufacturer: 'Procter & Gamble', category: 'General care' },
  { name: 'Aashirvaad', manufacturer: 'ITC Limited', category: 'Atta' },
  { name: 'Sunfeast', manufacturer: 'ITC Limited', category: 'Biscuits & snacks' },
  { name: 'Mangaldeep', manufacturer: 'ITC Limited', category: 'Agarbatti' },
  { name: 'Vivel', manufacturer: 'ITC Limited', category: 'Soaps' },
  { name: 'Classmate', manufacturer: 'ITC Limited', category: 'Stationery' },
  { name: 'KitKat', manufacturer: 'Nestle India Ltd', category: 'Chocolates' },
  { name: 'Nescafe', manufacturer: 'Nestle India Ltd', category: 'Coffee' },
  { name: 'Maggi', manufacturer: 'Nestle India Ltd', category: 'Sauce & noodles' },
  { name: 'Santoor', manufacturer: 'Wipro Enterprises', category: 'Soaps' },
  { name: 'Chandrika', manufacturer: 'Wipro Enterprises', category: 'Soaps' },
  { name: 'Gold Drop', manufacturer: 'Lohiya Edible Oils Pvt Ltd', category: 'Edible oil' },
  { name: 'Freedom', manufacturer: 'Gemini Edibles & Fats India Limited', category: 'Edible oil' },
  { name: 'Ruchi Gold', manufacturer: 'Patanjali Foods Limited', category: 'Edible oil' },
  { name: 'Vijaya', manufacturer: '', category: 'Edible oil' },
  { name: 'Mahalaxmi', manufacturer: 'Sri Sainath Agro Oils Private Limited', category: 'Non edible oil' },
];

type ProductRow = {
  sku: string;
  name: string;
  brand: string;
  shelf: ProductShelf;
  weight: string;
  mrp: number;
  dealerPrice: number;
  caseQty: number;
  imageUrl?: string | null;
};

/**
 * 37 client SKUs (22–58). Global: gst 5%, dealerDisc 10%, shopDisc 5%, isActive true, BrandType OTHER.
 */
const PRODUCTS: ProductRow[] = [
  {
    sku: '22',
    name: 'Horlicks Classic Malt',
    brand: 'Horlicks',
    shelf: ProductShelf.MILK_DRINKS,
    weight: '200g',
    mrp: 99,
    dealerPrice: 89.92,
    caseQty: 48,
    imageUrl:
      'https://encrypted-tbn2.gstatic.com/shopping?q=tbn:ANd9GcQz38F7LT4jfQlaY8nLnMZcD-EWp7LADxR2HKkoDQv7AI4Nq5BjDS5x6TT9rzJFT0t3wb8h8TJtp_hO9juTiP3crA6ffRvMUMLyMDhws_sJUr9qVi_hyA0dgAgl8Ag65h44Pwr_lJ8&usqp=Cac',
  },
  {
    sku: '23',
    name: 'Boost 3x More Stamina Malt Powder',
    brand: 'Boost',
    shelf: ProductShelf.MILK_DRINKS,
    weight: '200g',
    mrp: 110,
    dealerPrice: 101.95,
    caseQty: 48,
    imageUrl:
      'https://encrypted-tbn2.gstatic.com/shopping?q=tbn:ANd9GcTAa2bGhm3q5lSvhRSj5iaiYdkbCtS6WKfw-VqB5KKl8RDHfAD8Hxqzu3ClISitH0HDsRmsK4Ut7vgrXXpWT4xHTzXQCqXYTmaiYUF6GKEvS8EbrJPdrjGNELRWf1YD20W4M8-Cw2Y&usqp=CAc',
  },
  {
    sku: '24',
    name: 'Tide Plus Double Power Detergent Powder, Jasmine & Rose',
    brand: 'Tide',
    shelf: ProductShelf.DETERGENTS,
    weight: '',
    mrp: 10,
    dealerPrice: 8.17,
    caseQty: 60,
  },
  {
    sku: '25',
    name: 'Ariel Perfect Wash Detergent Powder',
    brand: 'Ariel',
    shelf: ProductShelf.DETERGENTS,
    weight: '45g',
    mrp: 10,
    dealerPrice: 8.17,
    caseQty: 60,
  },
  {
    sku: '26',
    name: 'Whisper Choice Ultra Sanitary Pads with Wings, XL',
    brand: 'Whisper',
    shelf: ProductShelf.FEMININE_BABY_CARE,
    weight: '6pcs',
    mrp: 50,
    dealerPrice: 39.77,
    caseQty: 64,
  },
  {
    sku: '27',
    name: 'Pampers Happy Skin Pants Medium (7-12kg)',
    brand: 'Pampers',
    shelf: ProductShelf.FEMININE_BABY_CARE,
    weight: '2pcs',
    mrp: 23,
    dealerPrice: 18.43,
    caseQty: 192,
  },
  {
    sku: '28',
    name: 'Pampers Happy Skin Pants Small (4-8kg)',
    brand: 'Pampers',
    shelf: ProductShelf.FEMININE_BABY_CARE,
    weight: '2pcs',
    mrp: 18,
    dealerPrice: 14.42,
    caseQty: 192,
  },
  {
    sku: '29',
    name: 'Pampers Happy Skin Pants Medium (9-14kg)',
    brand: 'Pampers',
    shelf: ProductShelf.FEMININE_BABY_CARE,
    weight: '2pcs',
    mrp: 30,
    dealerPrice: 24.04,
    caseQty: 192,
  },
  {
    sku: '30',
    name: 'Head & Shoulders Anti Dandruff Shampoo, Daily Clean',
    brand: 'Head & Shoulders',
    shelf: ProductShelf.SHAMPOOS,
    weight: '5ml',
    mrp: 2,
    dealerPrice: 1.43,
    caseQty: 1200,
  },
  {
    sku: '31',
    name: 'Head & Shoulders Anti Dandruff Shampoo, Basic Cool',
    brand: 'Head & Shoulders',
    shelf: ProductShelf.SHAMPOOS,
    weight: '5ml',
    mrp: 2,
    dealerPrice: 1.39,
    caseQty: 1200,
  },
  {
    sku: '32',
    name: 'Pantene Pro-V Advanced Hairfall Solution Shampoo, Silky Smooth Care',
    brand: 'Pantene',
    shelf: ProductShelf.SHAMPOOS,
    weight: '75ml',
    mrp: 75,
    dealerPrice: 63.47,
    caseQty: 48,
  },
  {
    sku: '33',
    name: 'Vicks Double Power Cough Drops, Ginger, Honey & Menthol',
    brand: 'Vicks',
    shelf: ProductShelf.GENERAL_CARE,
    weight: '310.5g',
    mrp: 230,
    dealerPrice: 192.13,
    caseQty: 32,
  },
  {
    sku: '34',
    name: 'Gillette Guard Shaving Razor',
    brand: 'Gillette',
    shelf: ProductShelf.GENERAL_CARE,
    weight: '1pc',
    mrp: 28,
    dealerPrice: 22.19,
    caseQty: 540,
  },
  {
    sku: '35',
    name: 'Gillette Guard Cartridges',
    brand: 'Gillette',
    shelf: ProductShelf.GENERAL_CARE,
    weight: '4pcs',
    mrp: 52,
    dealerPrice: 43.38,
    caseQty: 480,
  },
  {
    sku: '36',
    name: 'Gillette Shaving Foam, Regular',
    brand: 'Gillette',
    shelf: ProductShelf.GENERAL_CARE,
    weight: '50g',
    mrp: 95,
    dealerPrice: 76.42,
    caseQty: 48,
  },
  {
    sku: '37',
    name: 'Aashirvaad Shudh Chakki Atta',
    brand: 'Aashirvaad',
    shelf: ProductShelf.ATTA,
    weight: '5kg',
    mrp: 263,
    dealerPrice: 243.0,
    caseQty: 6,
  },
  {
    sku: '38',
    name: 'Aashirvaad Atta with Multigrains',
    brand: 'Aashirvaad',
    shelf: ProductShelf.ATTA,
    weight: '1kg',
    mrp: 84,
    dealerPrice: 72.18,
    caseQty: 30,
  },
  {
    sku: '39',
    name: "Sunfeast Mom's Magic Cookies, Cashew & Almond",
    brand: 'Sunfeast',
    shelf: ProductShelf.BISCUITS_COOKIES,
    weight: '28g',
    mrp: 5,
    dealerPrice: 4.53,
    caseQty: 180,
  },
  {
    sku: '40',
    name: 'Sunfeast Yippee! Mood Masala Noodles',
    brand: 'Sunfeast',
    shelf: ProductShelf.NOODLES,
    weight: '70g',
    mrp: 25,
    dealerPrice: 22.3,
    caseQty: 144,
  },
  {
    sku: '41',
    name: 'Sunfeast Dark Fantasy Choco Rolls',
    brand: 'Sunfeast',
    shelf: ProductShelf.CHOCOLATES,
    weight: '180g',
    mrp: 177,
    dealerPrice: 161.07,
    caseQty: 24,
  },
  {
    sku: '42',
    name: 'Mangaldeep Sadhvi Puja Agarbattis',
    brand: 'Mangaldeep',
    shelf: ProductShelf.AGARBATTIS,
    weight: '100pcs',
    mrp: 35,
    dealerPrice: 25.64,
    caseQty: 120,
  },
  {
    sku: '43',
    name: 'Vivel BThing Soap, Aloe Vera',
    brand: 'Vivel',
    shelf: ProductShelf.SOAPS,
    weight: '100g',
    mrp: 26,
    dealerPrice: 22.77,
    caseQty: 72,
  },
  {
    sku: '44',
    name: 'Classmate Long Single Line Exercise Notebook, 84 pages',
    brand: 'Classmate',
    shelf: ProductShelf.STATIONERY,
    weight: '1pc',
    mrp: 34,
    dealerPrice: 25.53,
    caseQty: 108,
  },
  {
    sku: '45',
    name: 'Nestle KitKat',
    brand: 'KitKat',
    shelf: ProductShelf.CHOCOLATES,
    weight: '13.2g',
    mrp: 10,
    dealerPrice: 8.97,
    caseQty: 1008,
  },
  {
    sku: '46',
    name: 'Nescafe Sunrise Coffee',
    brand: 'Nescafe',
    shelf: ProductShelf.COFFEE,
    weight: '1.3g',
    mrp: 2,
    dealerPrice: 1.81,
    caseQty: 4680,
  },
  {
    sku: '47',
    name: 'Maggi Pazzta, Cheezy Tomato Twist',
    brand: 'Maggi',
    shelf: ProductShelf.SAUCE,
    weight: '64g',
    mrp: 35,
    dealerPrice: 32.14,
    caseQty: 80,
    imageUrl: 'https://i.ebayimg.com/images/g/MGsAAOSwIDNj7jli/s-l1200.jpg',
  },
  {
    sku: '48',
    name: 'Maggi Rich Tomato Ketchup',
    brand: 'Maggi',
    shelf: ProductShelf.SAUCE,
    weight: '200g',
    mrp: 70,
    dealerPrice: 64.31,
    caseQty: 24,
    imageUrl: 'https://m.media-amazon.com/images/I/31IOmbt2RLL._SY300_SX300_QL70_FMwebp_.jpg',
  },
  {
    sku: '49',
    name: 'Nescafe Classic Coffee',
    brand: 'Nescafe',
    shelf: ProductShelf.COFFEE,
    weight: '45g',
    mrp: 235,
    dealerPrice: 216.8,
    caseQty: 30,
    imageUrl: 'https://imgwlns.gumlet.io/images/products/262991-1.jpg',
  },
  {
    sku: '50',
    name: 'Santoor Skin Moisturising Bathing Bar (4pcs*125g)',
    brand: 'Santoor',
    shelf: ProductShelf.SOAPS,
    weight: '500g',
    mrp: 199,
    dealerPrice: 180.33,
    caseQty: 30,
    imageUrl:
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQABWAukuBl5RBBOd3f5MXFsI3YZSovnMkvEA&s',
  },
  {
    sku: '51',
    name: 'Chandrika Ayurvedic Bathing Bar',
    brand: 'Chandrika',
    shelf: ProductShelf.SOAPS,
    weight: '75g',
    mrp: 33,
    dealerPrice: 29.91,
    caseQty: 216,
    imageUrl:
      'https://images.apollo247.in/pub/media/catalog/product/C/H/CHA0019_1-JULY23_1.jpg',
  },
  {
    sku: '52',
    name: 'Gold Drop Refined Sunflower Oil',
    brand: 'Gold Drop',
    shelf: ProductShelf.EDIBLE_OIL,
    weight: '5L',
    mrp: 1188,
    dealerPrice: 883.0,
    caseQty: 4,
    imageUrl:
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQwtm4PLya1ZH5PdEMyGqfJiR6HvHFNGJ8uXw&s',
  },
  {
    sku: '53',
    name: 'Freedom Refined Sunflower Oil',
    brand: 'Freedom',
    shelf: ProductShelf.EDIBLE_OIL,
    weight: '1L',
    mrp: 215,
    dealerPrice: 171.12,
    caseQty: 16,
    imageUrl:
      'https://5.imimg.com/data5/GLADMIN/Default/2022/6/QW/UF/BS/106185266/freedom-refined-sunflower-oil.jpeg',
  },
  {
    sku: '54',
    name: 'Freedom Double Filtered Groundnut Oil',
    brand: 'Freedom',
    shelf: ProductShelf.EDIBLE_OIL,
    weight: '1L',
    mrp: 230,
    dealerPrice: 179.06,
    caseQty: 10,
    imageUrl:
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnQ6D_s5zEvpwQqrGJ5y9aPHn0G2D8dEpyxQ&s',
  },
  {
    sku: '55',
    name: 'Ruchi Gold Refined Palmolein Oil',
    brand: 'Ruchi Gold',
    shelf: ProductShelf.EDIBLE_OIL,
    weight: '750g',
    mrp: 172,
    dealerPrice: 122.5,
    caseQty: 10,
    imageUrl:
      'https://www.jiomart.com/images/product/original/494574562/ruchi-gold-refined-palmolein-oil-750g-product-images-o494574562-p612654654-0-202510171656.jpg?im=Resize=(1000,1000)',
  },
  {
    sku: '56',
    name: 'Vijaya Double Filtered Groundnut Oil',
    brand: 'Vijaya',
    shelf: ProductShelf.EDIBLE_OIL,
    weight: '1L',
    mrp: 218,
    dealerPrice: 175.75,
    caseQty: 16,
    imageUrl:
      'https://www.jiomart.com/images/product/original/490000064/vijaya-filtered-groundnut-oil-1-l-product-images-o490000064-p490000064-0-202203151517.jpg',
  },
  {
    sku: '57',
    name: 'Vijaya Deepam Oil',
    brand: 'Vijaya',
    shelf: ProductShelf.NON_EDIBLE_OIL,
    weight: '1L',
    mrp: 211,
    dealerPrice: 160.77,
    caseQty: 12,
    imageUrl:
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQhRHY78ncX-E1LbJk1RT0QEnxfxzf23RrzfQ&s',
  },
  {
    sku: '58',
    name: 'Mahalaxmi Deepam Oil',
    brand: 'Mahalaxmi',
    shelf: ProductShelf.NON_EDIBLE_OIL,
    weight: '400ml',
    mrp: 92,
    dealerPrice: 63.14,
    caseQty: 32,
    imageUrl:
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQYHjGp3j4GpvJkGV5ByZcpOxX-w7GGAJbSIw&s',
  },
];

function toDecimal(value: number): Prisma.Decimal {
  return new Prisma.Decimal(value.toFixed(2));
}

function placeholderImage(brand: string, name: string): string {
  const label = encodeURIComponent(`${brand} ${name}`.slice(0, 48));
  return `https://dummyimage.com/600x600/1f2937/f9fafb.png&text=${label}`;
}

async function purgeLegacyDemoAccounts(): Promise<void> {
  const legacyEmails = [
    'admin@martdemo.com',
    'employee@martdemo.com',
    'dealer@martdemo.com',
    'shop1@martdemo.com',
    'shop2@martdemo.com',
    'krishna.wholesale@knsr.demo',
    'ravi.kirana@knsr.demo',
  ];
  const legacyIds = [
    'demo-user-admin',
    'demo-user-employee',
    'demo-user-dealer',
    'demo-user-shop1',
    'demo-user-shop2',
  ];

  await prisma.onboardingDocument.deleteMany({
    where: {
      user: {
        OR: [{ email: { in: legacyEmails } }, { id: { in: legacyIds } }],
      },
    },
  });
  await prisma.orderItem.deleteMany({
    where: {
      order: {
        OR: [
          { shopkeeper: { email: { in: legacyEmails } } },
          { dealer: { email: { in: legacyEmails } } },
        ],
      },
    },
  });
  await prisma.payment.deleteMany({
    where: { order: { shopkeeper: { email: { in: legacyEmails } } } },
  });
  await prisma.invoice.deleteMany({
    where: { order: { shopkeeper: { email: { in: legacyEmails } } } },
  });
  await prisma.order.deleteMany({
    where: {
      OR: [
        { shopkeeper: { email: { in: legacyEmails } } },
        { dealer: { email: { in: legacyEmails } } },
      ],
    },
  });
  await prisma.stock.deleteMany({
    where: { dealer: { email: { in: legacyEmails } } },
  });
  await prisma.area.updateMany({
    where: { dealer: { email: { in: legacyEmails } } },
    data: { dealerId: null },
  });
  await prisma.user.deleteMany({
    where: {
      OR: [{ email: { in: legacyEmails } }, { id: { in: legacyIds } }],
    },
  });
}

const SEED_TERRITORIES = [
  'North Zone',
  'South Zone',
  'Central Zone',
  'East Zone',
  'West Zone',
];

async function main(): Promise<void> {
  const adminEmail = (process.env.SEED_ADMIN_EMAIL ?? 'madhukar@techfylabs.com')
    .trim()
    .toLowerCase();
  const adminPasswordPlain = process.env.SEED_ADMIN_PASSWORD?.trim();
  if (!adminPasswordPlain || adminPasswordPlain.length < 8) {
    throw new Error(
      'Set SEED_ADMIN_PASSWORD (min 8 chars) in .env before running seed — only one admin account is created.',
    );
  }
  const adminName = (process.env.SEED_ADMIN_NAME ?? 'FlashMart Admin').trim();
  const companyName =
    (process.env.SEED_COMPANY_NAME ?? 'FlashMart Distribution').trim();

  await purgeLegacyDemoAccounts();

  const adminPassword = await bcrypt.hash(adminPasswordPlain, 10);

  const existingAdmin = await prisma.user.findUnique({
    where: { email: adminEmail },
    include: { company: true },
  });

  const company =
    existingAdmin?.company ??
    (await prisma.company.create({
      data: { name: companyName },
    }));
  console.log(`Seed company.id: ${company.id}`);

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
  console.log(`Seed admin: ${admin.email} (id: ${admin.id})`);

  const areaCount = await prisma.area.count({ where: { companyId: company.id } });
  if (areaCount === 0) {
    for (const name of SEED_TERRITORIES) {
      await prisma.area.create({
        data: { name, companyId: company.id },
      });
    }
    console.log(`Seed territories: ${SEED_TERRITORIES.length}`);
  }

  console.log('Manufacturers (reference):', MANUFACTURERS);
  await prisma.orderItem.deleteMany({
    where: { order: { companyId: company.id } },
  });
  await prisma.payment.deleteMany({
    where: { companyId: company.id },
  });
  await prisma.invoice.deleteMany({
    where: { companyId: company.id },
  });
  await prisma.order.deleteMany({ where: { companyId: company.id } });
  await prisma.stock.deleteMany({ where: { companyId: company.id } });
  await prisma.product.deleteMany({ where: { companyId: company.id } });
  await prisma.brand.deleteMany({ where: { companyId: company.id } });

  const brands = await Promise.all(
    BRANDS.map((b) =>
      prisma.brand.upsert({
        where: { name: b.name },
        update: {
          manufacturer: b.manufacturer || null,
          category: b.category,
          companyId: company.id,
        },
        create: {
          name: b.name,
          manufacturer: b.manufacturer || null,
          category: b.category,
          companyId: company.id,
        },
      }),
    ),
  );
  const brandByName = new Map(brands.map((b) => [b.name, b]));

  const createdProducts = await Promise.all(
    PRODUCTS.map(async (row) => {
      const brand = brandByName.get(row.brand);
      if (!brand) {
        throw new Error(`Brand not in seed map: ${row.brand}`);
      }
      const img = row.imageUrl?.trim() || placeholderImage(row.brand, row.name);
      const weight = row.weight;
      const mrp = toDecimal(row.mrp);
      const dealerPrice = toDecimal(row.dealerPrice);

      return prisma.product.upsert({
        where: { sku: row.sku },
        update: {
          name: row.name,
          brandId: brand.id,
          companyId: company.id,
          brandType: BrandType.OTHER,
          shelf: row.shelf,
          weight,
          mrp,
          dealerPrice,
          caseQty: row.caseQty,
          gstRate: toDecimal(5),
          gstPercentage: toDecimal(5),
          dealerDiscount: toDecimal(10),
          shopkeeperDiscount: toDecimal(5),
          isActive: true,
          basePrice: dealerPrice,
          imageUrl: img,
          bulkShippingFee: toDecimal(35),
          bulkShippingMinQty: 10,
        },
        create: {
          sku: row.sku,
          name: row.name,
          brandId: brand.id,
          companyId: company.id,
          brandType: BrandType.OTHER,
          shelf: row.shelf,
          weight,
          mrp,
          dealerPrice,
          caseQty: row.caseQty,
          gstRate: toDecimal(5),
          gstPercentage: toDecimal(5),
          dealerDiscount: toDecimal(10),
          shopkeeperDiscount: toDecimal(5),
          isActive: true,
          basePrice: dealerPrice,
          imageUrl: img,
          bulkShippingFee: toDecimal(35),
          bulkShippingMinQty: 10,
        },
      });
    }),
  );

  await prisma.product.updateMany({
    where: { companyId: company.id, imageUrl: null },
    data: {
      imageUrl: 'https://dummyimage.com/600x600/111827/f9fafb.png&text=KNSR+Mart',
    },
  });

  const allProducts = await prisma.product.findMany({
    where: { companyId: company.id },
    select: { id: true },
  });

  const primaryStock = await seedStockForCompany(company.id);
  console.log(
    `Primary company stock: ${primaryStock.rowCount} rows (${primaryStock.productCount} SKUs × ${primaryStock.holderCount} holders)`,
  );

  const companies = await prisma.company.findMany({ select: { id: true, name: true } });
  let totalStockRows = primaryStock.rowCount;
  for (const c of companies) {
    if (c.id === company.id) continue;
    const r = await seedStockForCompany(c.id);
    if (r.rowCount > 0) {
      console.log(`Stock for ${c.name ?? c.id}: ${r.rowCount} rows`);
      totalStockRows += r.rowCount;
    }
  }

  await ensureLegacyDemoDealerWithStock();

  console.log({
    seeded: true,
    companyId: company.id,
    adminEmail: admin.email,
    manufacturers: MANUFACTURERS.length,
    brands: brands.length,
    products: createdProducts.length,
    territories: SEED_TERRITORIES.length,
    stockRows: totalStockRows,
    note: 'Demo dealer: dealer@martdemo.com / Password@123 on demo-company when legacy catalog exists.',
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
