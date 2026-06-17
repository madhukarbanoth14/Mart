-- CreateEnum
CREATE TYPE "OrderKind" AS ENUM ('SHOPKEEPER', 'DEALER_RESTOCK');

-- AlterTable
ALTER TABLE "User" ADD COLUMN "passwordResetToken" TEXT,
ADD COLUMN "passwordResetExpires" TIMESTAMP(3);

-- CreateIndex
CREATE UNIQUE INDEX "User_passwordResetToken_key" ON "User"("passwordResetToken");

-- AlterTable
ALTER TABLE "Order" ADD COLUMN "kind" "OrderKind" NOT NULL DEFAULT 'SHOPKEEPER';

-- CreateIndex
CREATE INDEX "Order_kind_idx" ON "Order"("kind");
