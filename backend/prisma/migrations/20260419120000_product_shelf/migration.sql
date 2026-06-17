-- FMCG shelf for catalog grouping (shopkeeper UX).

CREATE TYPE "ProductShelf" AS ENUM (
  'STAPLES',
  'OILS_GHEE',
  'SUGAR_SALT_BASICS',
  'BEVERAGES',
  'SNACKS_BISCUITS',
  'HOME_CARE',
  'PERSONAL_CARE'
);

ALTER TABLE "Product" ADD COLUMN "shelf" "ProductShelf" NOT NULL DEFAULT 'STAPLES';

CREATE INDEX "Product_shelf_idx" ON "Product"("shelf");
