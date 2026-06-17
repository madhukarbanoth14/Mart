-- Idempotent repair: use when `_prisma_migrations` lists bulk-shipping as applied
-- but `Product` is missing columns (drift), or you pasted SQL in zsh by mistake.
-- Run: npm run db:repair-product-bulk-shipping

ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "bulkShippingFee" DECIMAL(12,2);

ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "bulkShippingMinQty" INTEGER NOT NULL DEFAULT 10;
