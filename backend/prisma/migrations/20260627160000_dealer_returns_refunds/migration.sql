-- CreateEnum
CREATE TYPE "ReturnReason" AS ENUM ('DAMAGED_PRODUCT', 'EXPIRED_PRODUCT', 'WRONG_PRODUCT', 'QUALITY_ISSUE', 'EXCESS_QUANTITY', 'OTHER');
CREATE TYPE "ReturnRequestStatus" AS ENUM ('REQUESTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'RETURN_COMPLETED');
CREATE TYPE "RefundRequestStatus" AS ENUM ('PENDING', 'PROCESSING', 'REFUNDED', 'FAILED', 'REJECTED');
CREATE TYPE "RefundMethod" AS ENUM ('RAZORPAY', 'BANK_TRANSFER', 'UPI');

-- AlterEnum
ALTER TYPE "FinanceAuditAction" ADD VALUE IF NOT EXISTS 'RETURN_REQUESTED';
ALTER TYPE "FinanceAuditAction" ADD VALUE IF NOT EXISTS 'RETURN_APPROVED';
ALTER TYPE "FinanceAuditAction" ADD VALUE IF NOT EXISTS 'RETURN_REJECTED';
ALTER TYPE "FinanceAuditAction" ADD VALUE IF NOT EXISTS 'REFUND_REQUESTED';
ALTER TYPE "FinanceAuditAction" ADD VALUE IF NOT EXISTS 'REFUND_APPROVED';
ALTER TYPE "FinanceAuditAction" ADD VALUE IF NOT EXISTS 'REFUND_REJECTED';
ALTER TYPE "FinanceAuditAction" ADD VALUE IF NOT EXISTS 'REFUND_PROCESSED';

-- CreateTable
CREATE TABLE "ReturnRequest" (
    "id" TEXT NOT NULL,
    "companyId" TEXT,
    "returnCode" TEXT NOT NULL,
    "orderId" TEXT NOT NULL,
    "shopkeeperId" TEXT NOT NULL,
    "dealerId" TEXT NOT NULL,
    "reason" "ReturnReason" NOT NULL,
    "reasonText" TEXT,
    "comments" TEXT,
    "imageUrls" JSONB,
    "status" "ReturnRequestStatus" NOT NULL DEFAULT 'REQUESTED',
    "dealerRemarks" TEXT,
    "refundAmount" DECIMAL(12,2) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "approvedAt" TIMESTAMP(3),
    "rejectedAt" TIMESTAMP(3),
    "completedAt" TIMESTAMP(3),

    CONSTRAINT "ReturnRequest_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "ReturnRequestItem" (
    "id" TEXT NOT NULL,
    "returnRequestId" TEXT NOT NULL,
    "orderItemId" TEXT,
    "productId" TEXT NOT NULL,
    "productName" TEXT NOT NULL,
    "quantity" INTEGER NOT NULL,
    "unitAmount" DECIMAL(12,2) NOT NULL,
    "lineAmount" DECIMAL(12,2) NOT NULL,

    CONSTRAINT "ReturnRequestItem_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "RefundRequest" (
    "id" TEXT NOT NULL,
    "companyId" TEXT,
    "refundCode" TEXT NOT NULL,
    "returnRequestId" TEXT NOT NULL,
    "orderId" TEXT NOT NULL,
    "dealerId" TEXT NOT NULL,
    "shopkeeperId" TEXT NOT NULL,
    "amount" DECIMAL(12,2) NOT NULL,
    "status" "RefundRequestStatus" NOT NULL DEFAULT 'PENDING',
    "dealerRemarks" TEXT,
    "adminRemarks" TEXT,
    "refundMethod" "RefundMethod",
    "transactionReference" TEXT,
    "refundDate" TIMESTAMP(3),
    "processedById" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "RefundRequest_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "ReturnRequest_returnCode_key" ON "ReturnRequest"("returnCode");
CREATE INDEX "ReturnRequest_orderId_idx" ON "ReturnRequest"("orderId");
CREATE INDEX "ReturnRequest_shopkeeperId_idx" ON "ReturnRequest"("shopkeeperId");
CREATE INDEX "ReturnRequest_dealerId_idx" ON "ReturnRequest"("dealerId");
CREATE INDEX "ReturnRequest_status_idx" ON "ReturnRequest"("status");
CREATE INDEX "ReturnRequest_createdAt_idx" ON "ReturnRequest"("createdAt");

CREATE INDEX "ReturnRequestItem_returnRequestId_idx" ON "ReturnRequestItem"("returnRequestId");
CREATE INDEX "ReturnRequestItem_productId_idx" ON "ReturnRequestItem"("productId");

CREATE UNIQUE INDEX "RefundRequest_refundCode_key" ON "RefundRequest"("refundCode");
CREATE UNIQUE INDEX "RefundRequest_returnRequestId_key" ON "RefundRequest"("returnRequestId");
CREATE INDEX "RefundRequest_orderId_idx" ON "RefundRequest"("orderId");
CREATE INDEX "RefundRequest_dealerId_idx" ON "RefundRequest"("dealerId");
CREATE INDEX "RefundRequest_shopkeeperId_idx" ON "RefundRequest"("shopkeeperId");
CREATE INDEX "RefundRequest_status_idx" ON "RefundRequest"("status");
CREATE INDEX "RefundRequest_createdAt_idx" ON "RefundRequest"("createdAt");

ALTER TABLE "ReturnRequest" ADD CONSTRAINT "ReturnRequest_orderId_fkey" FOREIGN KEY ("orderId") REFERENCES "Order"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "ReturnRequest" ADD CONSTRAINT "ReturnRequest_shopkeeperId_fkey" FOREIGN KEY ("shopkeeperId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "ReturnRequest" ADD CONSTRAINT "ReturnRequest_dealerId_fkey" FOREIGN KEY ("dealerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE "ReturnRequestItem" ADD CONSTRAINT "ReturnRequestItem_returnRequestId_fkey" FOREIGN KEY ("returnRequestId") REFERENCES "ReturnRequest"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "ReturnRequestItem" ADD CONSTRAINT "ReturnRequestItem_productId_fkey" FOREIGN KEY ("productId") REFERENCES "Product"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE "RefundRequest" ADD CONSTRAINT "RefundRequest_returnRequestId_fkey" FOREIGN KEY ("returnRequestId") REFERENCES "ReturnRequest"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "RefundRequest" ADD CONSTRAINT "RefundRequest_orderId_fkey" FOREIGN KEY ("orderId") REFERENCES "Order"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "RefundRequest" ADD CONSTRAINT "RefundRequest_dealerId_fkey" FOREIGN KEY ("dealerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "RefundRequest" ADD CONSTRAINT "RefundRequest_processedById_fkey" FOREIGN KEY ("processedById") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;
