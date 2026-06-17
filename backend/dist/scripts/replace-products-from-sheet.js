"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
require("dotenv/config");
const adapter_pg_1 = require("@prisma/adapter-pg");
const client_1 = require("@prisma/client");
const fs = __importStar(require("fs"));
const pg_1 = require("pg");
const path = __importStar(require("path"));
const XLSX = __importStar(require("xlsx"));
const companyId = 'demo-company';
const url = process.env.DATABASE_URL;
if (!url) {
    throw new Error('DATABASE_URL is required');
}
const pool = new pg_1.Pool({ connectionString: url });
const prisma = new client_1.PrismaClient({ adapter: new adapter_pg_1.PrismaPg(pool) });
function normalizeHeader(header) {
    return header.trim().toLowerCase().replace(/\s+/g, '');
}
function getString(row, key) {
    const value = row[key];
    if (value == null)
        return null;
    const text = String(value).trim();
    return text.length ? text : null;
}
function getNumber(row, key) {
    const raw = getString(row, key);
    if (raw == null)
        return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
}
function getBoolean(row, key, defaultValue) {
    const raw = getString(row, key);
    if (raw == null)
        return defaultValue;
    const text = raw.toLowerCase();
    if (['true', '1', 'yes', 'y'].includes(text))
        return true;
    if (['false', '0', 'no', 'n'].includes(text))
        return false;
    return defaultValue;
}
function toDecimal(value, fallback) {
    const safe = value ?? fallback;
    return new client_1.Prisma.Decimal(safe.toFixed(2));
}
function parseBrandType(value) {
    if (value?.toUpperCase() === 'OWN')
        return client_1.BrandType.OWN;
    return client_1.BrandType.OTHER;
}
function parseShelf(value) {
    if (!value)
        return client_1.ProductShelf.STAPLES;
    const normalized = value.toUpperCase();
    if (normalized in client_1.ProductShelf) {
        return client_1.ProductShelf[normalized];
    }
    return client_1.ProductShelf.STAPLES;
}
function parseRows(rows) {
    return rows
        .map((row) => {
        const name = getString(row, 'name');
        const brand = getString(row, 'brand');
        const weight = getString(row, 'weight');
        if (!name || !brand || !weight)
            return null;
        const mrp = getNumber(row, 'mrp');
        const dealerPriceFromSheet = getNumber(row, 'dealerprice');
        const dealerPrice = dealerPriceFromSheet ??
            (mrp != null ? Number((mrp * 0.9).toFixed(2)) : null);
        return {
            sku: getString(row, 'sku'),
            name,
            brand,
            manufacturer: getString(row, 'manufacturer'),
            brandType: parseBrandType(getString(row, 'brandtype')),
            shelf: parseShelf(getString(row, 'shelf')),
            weight,
            mrp,
            dealerPrice,
            caseQty: getNumber(row, 'caseqty'),
            gstRate: getNumber(row, 'gstrate') ?? 5,
            gstPercentage: getNumber(row, 'gstpercentage') ?? 5,
            dealerDiscount: getNumber(row, 'dealerdiscount') ?? 10,
            shopkeeperDiscount: getNumber(row, 'shopkeeperdiscount') ?? 5,
            isActive: getBoolean(row, 'isactive', true),
            imageUrl: getString(row, 'imageurl'),
        };
    })
        .filter((row) => row !== null);
}
async function main() {
    const inputPathArg = process.argv[2];
    if (!inputPathArg) {
        throw new Error('Usage: npm run products:replace-from-sheet -- "<path-to-xls/xlsx/csv>"\n' +
            'Or use the repo Excel template from Mart/backend:\n' +
            '  npm run products:replace-from-template-xls');
    }
    const inputPath = path.resolve(process.cwd(), inputPathArg);
    if (!fs.existsSync(inputPath)) {
        const hint = inputPathArg.includes('absolute/path') || inputPathArg.includes('path/to')
            ? '\nTip: "/absolute/path/to/..." was documentation only — use the real path to your .csv/.xlsx file.\n'
            : '';
        throw new Error(`File not found: ${inputPath}${hint}`);
    }
    const workbook = XLSX.readFile(inputPath, { cellDates: false });
    const firstSheetName = workbook.SheetNames[0];
    if (!firstSheetName) {
        throw new Error('No worksheet found in file');
    }
    const sheet = workbook.Sheets[firstSheetName];
    const rawRows = XLSX.utils.sheet_to_json(sheet, {
        defval: null,
    });
    const normalizedRows = rawRows.map((row) => Object.fromEntries(Object.entries(row).map(([k, v]) => [normalizeHeader(k), v])));
    const rows = parseRows(normalizedRows);
    if (rows.length === 0) {
        throw new Error('No valid product rows found. Ensure sheet has name/brand/weight columns.');
    }
    const uniqueBrands = new Map();
    for (const row of rows) {
        if (!uniqueBrands.has(row.brand)) {
            uniqueBrands.set(row.brand, { manufacturer: row.manufacturer });
        }
    }
    const company = await prisma.company.upsert({
        where: { id: companyId },
        update: { name: 'Mart Demo Distribution Pvt Ltd' },
        create: { id: companyId, name: 'Mart Demo Distribution Pvt Ltd' },
    });
    try {
        await prisma.$transaction(async (tx) => {
            await tx.payment.deleteMany({
                where: {
                    OR: [{ companyId: company.id }, { order: { companyId: company.id } }],
                },
            });
            await tx.invoice.deleteMany({
                where: {
                    OR: [{ companyId: company.id }, { order: { companyId: company.id } }],
                },
            });
            await tx.orderItem.deleteMany({
                where: { order: { companyId: company.id } },
            });
            await tx.order.deleteMany({ where: { companyId: company.id } });
            await tx.stock.deleteMany({ where: { companyId: company.id } });
            await tx.product.deleteMany({ where: { companyId: company.id } });
            const brandByName = new Map();
            for (const [brandName, meta] of uniqueBrands.entries()) {
                const brand = await tx.brand.upsert({
                    where: { name: brandName },
                    update: {
                        companyId: company.id,
                        manufacturer: meta.manufacturer,
                    },
                    create: {
                        name: brandName,
                        companyId: company.id,
                        manufacturer: meta.manufacturer,
                    },
                });
                brandByName.set(brandName, { id: brand.id });
            }
            for (const row of rows) {
                const brand = brandByName.get(row.brand);
                if (!brand)
                    continue;
                const fallbackBasePrice = row.dealerPrice ?? row.mrp ?? 0;
                await tx.product.create({
                    data: {
                        companyId: company.id,
                        sku: row.sku,
                        name: row.name,
                        brandId: brand.id,
                        imageUrl: row.imageUrl,
                        brandType: row.brandType,
                        shelf: row.shelf,
                        weight: row.weight,
                        mrp: row.mrp != null ? toDecimal(row.mrp, 0) : null,
                        dealerPrice: row.dealerPrice != null ? toDecimal(row.dealerPrice, 0) : null,
                        caseQty: row.caseQty,
                        gstRate: toDecimal(row.gstRate, 5),
                        isActive: row.isActive,
                        basePrice: toDecimal(fallbackBasePrice, 0),
                        gstPercentage: toDecimal(row.gstPercentage, 5),
                        dealerDiscount: toDecimal(row.dealerDiscount, 10),
                        shopkeeperDiscount: toDecimal(row.shopkeeperDiscount, 5),
                    },
                });
            }
            const defaultStockQty = 200;
            const dealers = await tx.user.findMany({
                where: { companyId: company.id, role: client_1.UserRole.DEALER },
                select: { id: true },
            });
            const productIds = await tx.product.findMany({
                where: { companyId: company.id },
                select: { id: true },
            });
            for (const { id: dealerId } of dealers) {
                for (const { id: productId } of productIds) {
                    await tx.stock.upsert({
                        where: {
                            dealerId_productId: { dealerId, productId },
                        },
                        update: { quantity: defaultStockQty },
                        create: {
                            companyId: company.id,
                            dealerId,
                            productId,
                            quantity: defaultStockQty,
                        },
                    });
                }
            }
        }, { timeout: 120_000 });
    }
    catch (e) {
        const code = typeof e === 'object' && e !== null && 'code' in e
            ? String(e.code)
            : '';
        if (code === 'P2022') {
            console.error('\nThe database `Product` table is missing columns (e.g. bulkShippingMinQty). ' +
                'Prisma migrate can say "No pending migrations" if `_prisma_migrations` is ahead of the real table (drift).\n\n' +
                'Repair (safe to re-run):\n  npm run db:repair-product-bulk-shipping\n\n' +
                'Do not paste ALTER TABLE into zsh — use the npm script or `psql` / `prisma db execute --file ...`.\n');
        }
        throw e;
    }
    const count = await prisma.product.count({ where: { companyId: company.id } });
    console.log(`Replaced products successfully from ${path.basename(inputPath)}. Total products: ${count}`);
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
//# sourceMappingURL=replace-products-from-sheet.js.map