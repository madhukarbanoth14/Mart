-- Idempotent completion of 20250606120000_dealer_restock_password_reset
-- (production may have OrderKind enum from a partial failed run).

DO $$ BEGIN
  CREATE TYPE "OrderKind" AS ENUM ('SHOPKEEPER', 'DEALER_RESTOCK');
EXCEPTION
  WHEN duplicate_object THEN NULL;
END $$;

ALTER TABLE "User" ADD COLUMN IF NOT EXISTS "passwordResetToken" TEXT;
ALTER TABLE "User" ADD COLUMN IF NOT EXISTS "passwordResetExpires" TIMESTAMP(3);

CREATE UNIQUE INDEX IF NOT EXISTS "User_passwordResetToken_key"
  ON "User"("passwordResetToken");

DO $$ BEGIN
  ALTER TABLE "Order" ADD COLUMN "kind" "OrderKind" NOT NULL DEFAULT 'SHOPKEEPER';
EXCEPTION
  WHEN duplicate_column THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS "Order_kind_idx" ON "Order"("kind");
