import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient } from '@prisma/client';
import { Pool } from 'pg';

const DATABASE_URL = process.env.DATABASE_URL;
if (!DATABASE_URL) throw new Error('DATABASE_URL is required');

const pool = new Pool({ connectionString: DATABASE_URL });
const prisma = new PrismaClient({ adapter: new PrismaPg(pool) });

const EXPLICIT_MAPPINGS = [
  {
    key: 'britannia cake gobbles fruity fun',
    imageUrl:
      'https://images.openfoodfacts.org/images/products/890/106/336/3748/front_en.4.400.jpg',
  },
  {
    key: 'britannia good day butter cookies',
    imageUrl:
      'https://images.openfoodfacts.org/images/products/890/106/309/2822/front_en.4.400.jpg',
  },
  {
    key: 'britannia good day pista badam cookies',
    imageUrl:
      'https://images.openfoodfacts.org/images/products/890/106/309/4253/front_en.4.400.jpg',
  },
  {
    key: 'britannia nutrichoice digestive biscuits',
    imageUrl:
      'https://images.openfoodfacts.org/images/products/890/106/313/6403/front_en.4.400.jpg',
  },
  {
    key: 'britannia tiger krunch chocochips biscuits',
    imageUrl:
      'https://images.openfoodfacts.org/images/products/890/106/315/5534/front_en.4.400.jpg',
  },
];

function normalize(value) {
  return (value || '')
    .toLowerCase()
    .replace(/[^a-z0-9\s]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function isPlaceholder(url) {
  return !url || url.includes('dummyimage.com');
}

function familyKey(name) {
  const stop = new Set([
    'mrp',
    'pack',
    'sachet',
    'jar',
    'box',
    'pouch',
    'bottle',
    'g',
    'gm',
    'kg',
    'ml',
    'l',
  ]);
  const tokens = normalize(name)
    .split(' ')
    .filter((t) => t.length > 2 && !stop.has(t) && !/^\d+(\.\d+)?$/.test(t));
  return tokens.slice(0, 4).join(' ');
}

async function main() {
  const products = await prisma.product.findMany({
    include: { brand: true },
    orderBy: { name: 'asc' },
  });

  let explicitUpdates = 0;
  for (const p of products) {
    if (!isPlaceholder(p.imageUrl)) continue;
    const combined = normalize(`${p.brand?.name || ''} ${p.name}`);
    for (const map of EXPLICIT_MAPPINGS) {
      const keyTokens = map.key.split(' ');
      if (keyTokens.every((t) => combined.includes(t))) {
        await prisma.product.update({
          where: { id: p.id },
          data: { imageUrl: map.imageUrl },
        });
        explicitUpdates += 1;
        break;
      }
    }
  }

  const refreshed = await prisma.product.findMany({
    include: { brand: true },
    orderBy: { name: 'asc' },
  });

  const familyImage = new Map();
  for (const p of refreshed) {
    if (isPlaceholder(p.imageUrl)) continue;
    const brand = p.brand?.name || '';
    const key = `${brand}::${familyKey(p.name)}`;
    if (!familyImage.has(key)) {
      familyImage.set(key, p.imageUrl);
    }
  }

  let propagated = 0;
  for (const p of refreshed) {
    if (!isPlaceholder(p.imageUrl)) continue;
    const brand = p.brand?.name || '';
    const key = `${brand}::${familyKey(p.name)}`;
    const img = familyImage.get(key);
    if (!img) continue;
    await prisma.product.update({
      where: { id: p.id },
      data: { imageUrl: img },
    });
    propagated += 1;
  }

  console.log({
    total: products.length,
    explicitUpdates,
    propagated,
    totalUpdated: explicitUpdates + propagated,
  });
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
    await pool.end();
  });
