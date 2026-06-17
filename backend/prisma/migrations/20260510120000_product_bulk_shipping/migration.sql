-- AlterTable
ALTER TABLE "Product" ADD COLUMN "bulkShippingFee" DECIMAL(12,2),
ADD COLUMN "bulkShippingMinQty" INTEGER NOT NULL DEFAULT 10;
