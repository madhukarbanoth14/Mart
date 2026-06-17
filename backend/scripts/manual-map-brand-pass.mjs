import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient } from '@prisma/client';
import { Pool } from 'pg';
import { writeFileSync } from 'fs';

const DATABASE_URL = process.env.DATABASE_URL;
if (!DATABASE_URL) throw new Error('DATABASE_URL is required');

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
    .filter((t) => t.length > 2);
}

function isPlaceholder(url) {
  return !url || url.includes('dummyimage.com');
}

function score(product, candidate) {
  const pName = tokens(product.name);
  const pBrand = tokens(product.brand?.name || '');
  const cName = normalize(candidate.product_name || '');
  const cBrand = normalize(candidate.brands || '');
  let s = 0;
  for (const t of pBrand) {
    if (cBrand.includes(t) || cName.includes(t)) s += 4;
  }
  for (const t of pName) {
    if (cName.includes(t)) s += 2;
  }
  if (candidate.image_front_url || candidate.image_url) s += 1;
  return s;
}

async function bestFor(product) {
  const brand = product.brand?.name || '';
  const name = product.name || '';
  const queries = [
    `${brand} ${name}`.trim(),
    `${brand} ${tokens(name).slice(0, 3).join(' ')}`.trim(),
  ];

  const all = [];
  const seen = new Set();
  for (const q of queries) {
    const url =
      'https://world.openfoodfacts.org/cgi/search.pl' +
      `?search_terms=${encodeURIComponent(q)}` +
      '&search_simple=1&action=process&json=1&page_size=25' +
      '&fields=product_name,brands,image_front_url,image_url,code';
    let data = null;
    for (let attempt = 0; attempt < 3; attempt += 1) {
      try {
        const res = await fetch(url, {
          headers: { 'User-Agent': 'KNSR-Mart-ManualPass/1.0' },
        });
        if (!res.ok) break;
        data = await res.json();
        break;
      } catch (_err) {
        await new Promise((r) => setTimeout(r, 600 * (attempt + 1)));
      }
    }
    if (!data) continue;
    const products = Array.isArray(data.products) ? data.products : [];
    for (const p of products) {
      const key = p.code || `${p.product_name || ''}|${p.brands || ''}`;
      if (seen.has(key)) continue;
      seen.add(key);
      all.push(p);
    }
  }
  if (!all.length) return null;

  let best = null;
  let bestScore = -1;
  for (const c of all) {
    const s = score(product, c);
    if (s > bestScore) {
      bestScore = s;
      best = c;
    }
  }
  if (!best) return null;

  const brandTokens = tokens(product.brand?.name || '');
  const bestName = normalize(best.product_name || '');
  const bestBrands = normalize(best.brands || '');
  const brandMatch = brandTokens.some(
    (t) => bestBrands.includes(t) || bestName.includes(t),
  );
  if (!brandMatch) return null;
  if (bestScore < 5) return null;

  return {
    imageUrl: best.image_front_url || best.image_url || null,
    matchedName: best.product_name || '',
    matchedBrands: best.brands || '',
    score: bestScore,
  };
}

async function main() {
  const rows = await prisma.product.findMany({
    where: {
      OR: [{ imageUrl: null }, { imageUrl: { contains: 'dummyimage.com' } }],
    },
    include: { brand: true },
    orderBy: { name: 'asc' },
  });

  const applied = [];
  let updated = 0;
  for (const row of rows) {
    if (!isPlaceholder(row.imageUrl)) continue;
    const best = await bestFor(row);
    if (!best?.imageUrl) continue;
    await prisma.product.update({
      where: { id: row.id },
      data: { imageUrl: best.imageUrl },
    });
    updated += 1;
    applied.push({
      productId: row.id,
      brand: row.brand?.name || '',
      name: row.name,
      imageUrl: best.imageUrl,
      matchedName: best.matchedName,
      matchedBrands: best.matchedBrands,
      score: best.score,
    });
    await new Promise((r) => setTimeout(r, 120));
  }

  const header =
    'productId,brand,name,imageUrl,matchedName,matchedBrands,score\n';
  const csv = applied
    .map((r) =>
      [
        r.productId,
        r.brand,
        r.name,
        r.imageUrl,
        r.matchedName,
        r.matchedBrands,
        String(r.score),
      ]
        .map((v) => `"${String(v).replace(/"/g, '""')}"`)
        .join(','),
    )
    .join('\n');
  writeFileSync(
    '../docs/manual_product_image_map.csv',
    header + (csv ? csv + '\n' : ''),
    'utf8',
  );

  console.log({ scanned: rows.length, updated, mapFile: 'docs/manual_product_image_map.csv' });
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
