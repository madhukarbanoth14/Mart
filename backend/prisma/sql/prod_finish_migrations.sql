-- Idempotent completion of 20260505061022 + 20260510120000 on production (partial apply).

ALTER TABLE "Brand" ADD COLUMN IF NOT EXISTS "category" TEXT;
ALTER TABLE "Brand" ADD COLUMN IF NOT EXISTS "manufacturer" TEXT;

DO $$ BEGIN
  ALTER TABLE "Order" ADD COLUMN "paymentStatus" "OrderPaymentStatus" NOT NULL DEFAULT 'UNPAID';
EXCEPTION WHEN duplicate_column THEN NULL;
END $$;

ALTER TABLE "Order" ALTER COLUMN "updatedAt" DROP DEFAULT;

ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "caseQty" INTEGER;
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "dealerPrice" DECIMAL(12,2);
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "gstRate" DECIMAL(5,2) NOT NULL DEFAULT 5;
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "imageUrl" TEXT;
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "isActive" BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "mrp" DECIMAL(12,2);
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "sku" TEXT;
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "weight" TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS "Payment" (
    "id" TEXT NOT NULL,
    "companyId" TEXT,
    "orderId" TEXT NOT NULL,
    "provider" "PaymentProvider" NOT NULL,
    "status" "PaymentStatus" NOT NULL DEFAULT 'INITIATED',
    "amount" DECIMAL(12,2) NOT NULL,
    "currency" TEXT NOT NULL DEFAULT 'INR',
    "gatewayOrderId" TEXT,
    "gatewayPaymentId" TEXT,
    "gatewaySignature" TEXT,
    "failureReason" TEXT,
    "metadata" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    CONSTRAINT "Payment_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS "Payment_gatewayOrderId_key" ON "Payment"("gatewayOrderId");
CREATE INDEX IF NOT EXISTS "Payment_companyId_idx" ON "Payment"("companyId");
CREATE INDEX IF NOT EXISTS "Payment_orderId_idx" ON "Payment"("orderId");
CREATE INDEX IF NOT EXISTS "Payment_provider_status_idx" ON "Payment"("provider", "status");
CREATE INDEX IF NOT EXISTS "Order_paymentStatus_idx" ON "Order"("paymentStatus");
CREATE UNIQUE INDEX IF NOT EXISTS "Product_sku_key" ON "Product"("sku");
CREATE UNIQUE INDEX IF NOT EXISTS "Product_name_weight_key" ON "Product"("name", "weight");

DO $$ BEGIN
  ALTER TABLE "Payment" ADD CONSTRAINT "Payment_companyId_fkey"
    FOREIGN KEY ("companyId") REFERENCES "Company"("id") ON DELETE SET NULL ON UPDATE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE "Payment" ADD CONSTRAINT "Payment_orderId_fkey"
    FOREIGN KEY ("orderId") REFERENCES "Order"("id") ON DELETE CASCADE ON UPDATE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "bulkShippingFee" DECIMAL(12,2);
ALTER TABLE "Product" ADD COLUMN IF NOT EXISTS "bulkShippingMinQty" INTEGER NOT NULL DEFAULT 10;
