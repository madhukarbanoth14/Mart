import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient } from '@prisma/client';
import { Pool } from 'pg';

const DATABASE_URL = process.env.DATABASE_URL;
if (!DATABASE_URL) {
  throw new Error('DATABASE_URL is required');
}
const BRAND_FILTER = (process.env.BRAND_FILTER || '')
  .split(',')
  .map((v) => v.trim())
  .filter(Boolean);
const MIN_SCORE = Number(process.env.MIN_SCORE || '4');

const pool = new Pool({ connectionString: DATABASE_URL });
const prisma = new PrismaClient({ adapter: new PrismaPg(pool) });

function normalize(value) {
  return (value || '')
    .toLowerCase()
    .replace(/[^a-z0-9\s]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function tokens(value) {
  return normalize(value)
    .split(' ')
    .filter((part) => part.length > 2);
}

function getWeightHint(weight) {
  if (!weight) return '';
  const norm = normalize(weight);
  const m = norm.match(/(\d+(\.\d+)?)\s*(kg|g|gm|ml|l)/);
  return m ? `${m[1]} ${m[3]}` : '';
}

function scoreCandidate(product, candidate) {
  const nameTokens = tokens(product.name);
  const brandTokens = tokens(product.brand?.name || '');
  const candName = normalize(candidate.product_name || '');
  const candBrands = normalize(candidate.brands || '');
  const candQty = normalize(candidate.quantity || '');

  let score = 0;

  for (const t of nameTokens) {
    if (candName.includes(t)) score += 2;
  }
  for (const t of brandTokens) {
    if (candBrands.includes(t) || candName.includes(t)) score += 3;
  }

  const weightHint = getWeightHint(product.weight);
  if (weightHint && (candQty.includes(weightHint) || candName.includes(weightHint))) {
    score += 2;
  }

  if (candidate.image_front_url || candidate.image_url) {
    score += 1;
  }

  return score;
}

async function findBestImage(product) {
  const brand = product.brand?.name || '';
  const name = product.name || '';
  const coreTokens = tokens(name).slice(0, 4).join(' ');
  const weightHint = getWeightHint(product.weight);
  const queries = [
    `${brand} ${name}`.trim(),
    `${brand} ${coreTokens}`.trim(),
    `${name}`.trim(),
    `${brand} ${coreTokens} ${weightHint}`.trim(),
  ].filter(Boolean);

  const allCandidates = [];
  const seenCodes = new Set();
  for (const query of queries) {
    const url =
      'https://world.openfoodfacts.org/cgi/search.pl' +
      `?search_terms=${encodeURIComponent(query)}` +
      '&search_simple=1&action=process&json=1&page_size=30' +
      '&fields=product_name,brands,quantity,image_front_url,image_url,code';

    const res = await fetch(url, {
      headers: { 'User-Agent': 'KNSR-Mart-ImageMapper/1.0 (demo)' },
    });
    if (!res.ok) continue;
    const json = await res.json();
    const candidates = Array.isArray(json.products) ? json.products : [];
    for (const c of candidates) {
      const key = c.code || `${c.product_name || ''}|${c.brands || ''}`;
      if (seenCodes.has(key)) continue;
      seenCodes.add(key);
      allCandidates.push(c);
    }
  }

  const candidates = allCandidates;
  if (!candidates.length) return null;

  let best = null;
  let bestScore = -1;
  for (const c of candidates) {
    const score = scoreCandidate(product, c);
    if (score > bestScore) {
      bestScore = score;
      best = c;
    }
  }

  if (!best) return null;
  const brandTokens = tokens(product.brand?.name || '');
  const bestName = normalize(best.product_name || '');
  const bestBrands = normalize(best.brands || '');
  const hasBrandHit = brandTokens.some(
    (t) => bestBrands.includes(t) || bestName.includes(t),
  );
  if (!hasBrandHit) return null;
  if (bestScore < MIN_SCORE) return null;

  return best.image_front_url || best.image_url || null;
}

async function main() {
  const products = await prisma.product.findMany({
    include: { brand: true },
    orderBy: { name: 'asc' },
  });
  const targetProducts =
    BRAND_FILTER.length === 0
      ? products
      : products.filter((p) => BRAND_FILTER.includes(p.brand?.name || ''));

  let matched = 0;
  let unmatched = 0;
  let failed = 0;

  for (const product of targetProducts) {
    if (product.imageUrl && !product.imageUrl.includes('dummyimage.com')) {
      continue;
    }
    try {
      const imageUrl = await findBestImage(product);
      if (!imageUrl) {
        unmatched += 1;
        continue;
      }
      await prisma.product.update({
        where: { id: product.id },
        data: { imageUrl },
      });
      matched += 1;
    } catch (err) {
      failed += 1;
      console.error(`Failed: ${product.name}`, err?.message || err);
    }
    await new Promise((r) => setTimeout(r, 120));
  }

  console.log({
    total: products.length,
    scanned: targetProducts.length,
    matched,
    unmatched,
    failed,
    minScore: MIN_SCORE,
    brandFilter: BRAND_FILTER,
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
