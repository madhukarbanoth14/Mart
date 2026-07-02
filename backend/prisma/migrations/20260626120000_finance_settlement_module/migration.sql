-- Finance, commission, settlement & audit module

CREATE TYPE "CommissionRuleType" AS ENUM ('GLOBAL', 'DEALER', 'PRODUCT');
CREATE TYPE "SettlementStatus" AS ENUM ('PENDING', 'PARTIALLY_SETTLED', 'SETTLED');
CREATE TYPE "SettlementPaymentMethod" AS ENUM ('BANK_TRANSFER', 'UPI', 'NEFT', 'RTGS', 'OTHER');
CREATE TYPE "FinanceAuditAction" AS ENUM (
  'COMMISSION_CALCULATED',
  'SETTLEMENT_CREATED',
  'SETTLEMENT_APPROVED',
  'SETTLEMENT_PAYMENT',
  'SETTLEMENT_MODIFIED',
  'COMMISSION_RULE_UPDATED',
  'REPORT_DOWNLOADED',
  'PAYMENT_UPDATED'
);

CREATE TABLE "CommissionRule" (
  "id" TEXT NOT NULL,
  "companyId" TEXT,
  "ruleType" "CommissionRuleType" NOT NULL,
  "rate" DECIMAL(5,2) NOT NULL,
  "dealerId" TEXT,
  "productId" TEXT,
  "active" BOOLEAN NOT NULL DEFAULT true,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "CommissionRule_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "PlatformRevenue" (
  "id" TEXT NOT NULL,
  "companyId" TEXT,
  "orderId" TEXT NOT NULL,
  "paymentId" TEXT,
  "dealerId" TEXT NOT NULL,
  "grossAmount" DECIMAL(12,2) NOT NULL,
  "gstAmount" DECIMAL(12,2) NOT NULL,
  "commissionAmount" DECIMAL(12,2) NOT NULL,
  "dealerPayable" DECIMAL(12,2) NOT NULL,
  "commissionRate" DECIMAL(5,2) NOT NULL,
  "settlementId" TEXT,
  "settlementStatus" "SettlementStatus" NOT NULL DEFAULT 'PENDING',
  "revenueDate" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "PlatformRevenue_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "DealerSettlement" (
  "id" TEXT NOT NULL,
  "settlementCode" TEXT NOT NULL,
  "companyId" TEXT,
  "dealerId" TEXT NOT NULL,
  "settlementStartDate" TIMESTAMP(3) NOT NULL,
  "settlementEndDate" TIMESTAMP(3) NOT NULL,
  "totalOrders" INTEGER NOT NULL DEFAULT 0,
  "totalQuantity" INTEGER NOT NULL DEFAULT 0,
  "grossSales" DECIMAL(14,2) NOT NULL,
  "gstAmount" DECIMAL(14,2) NOT NULL,
  "commissionAmount" DECIMAL(14,2) NOT NULL,
  "dealerPayable" DECIMAL(14,2) NOT NULL,
  "settledAmount" DECIMAL(14,2) NOT NULL DEFAULT 0,
  "balanceAmount" DECIMAL(14,2) NOT NULL,
  "settlementStatus" "SettlementStatus" NOT NULL DEFAULT 'PENDING',
  "paymentMethod" "SettlementPaymentMethod",
  "transactionReference" TEXT,
  "utrNumber" TEXT,
  "paymentDate" TIMESTAMP(3),
  "remarks" TEXT,
  "createdById" TEXT,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "DealerSettlement_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "DealerPaymentHistory" (
  "id" TEXT NOT NULL,
  "settlementId" TEXT NOT NULL,
  "dealerId" TEXT NOT NULL,
  "amount" DECIMAL(14,2) NOT NULL,
  "paymentMethod" "SettlementPaymentMethod" NOT NULL,
  "transactionReference" TEXT,
  "utrNumber" TEXT,
  "paymentDate" TIMESTAMP(3) NOT NULL,
  "remarks" TEXT,
  "proofUrl" TEXT,
  "createdById" TEXT,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "DealerPaymentHistory_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "FinanceAuditLog" (
  "id" TEXT NOT NULL,
  "companyId" TEXT,
  "action" "FinanceAuditAction" NOT NULL,
  "entityType" TEXT NOT NULL,
  "entityId" TEXT NOT NULL,
  "actorId" TEXT,
  "details" JSONB,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "FinanceAuditLog_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "PlatformRevenue_orderId_key" ON "PlatformRevenue"("orderId");
CREATE UNIQUE INDEX "DealerSettlement_settlementCode_key" ON "DealerSettlement"("settlementCode");

CREATE INDEX "CommissionRule_companyId_idx" ON "CommissionRule"("companyId");
CREATE INDEX "CommissionRule_ruleType_idx" ON "CommissionRule"("ruleType");
CREATE INDEX "CommissionRule_dealerId_idx" ON "CommissionRule"("dealerId");
CREATE INDEX "CommissionRule_productId_idx" ON "CommissionRule"("productId");

CREATE INDEX "PlatformRevenue_companyId_idx" ON "PlatformRevenue"("companyId");
CREATE INDEX "PlatformRevenue_dealerId_idx" ON "PlatformRevenue"("dealerId");
CREATE INDEX "PlatformRevenue_settlementId_idx" ON "PlatformRevenue"("settlementId");
CREATE INDEX "PlatformRevenue_settlementStatus_idx" ON "PlatformRevenue"("settlementStatus");
CREATE INDEX "PlatformRevenue_revenueDate_idx" ON "PlatformRevenue"("revenueDate");

CREATE INDEX "DealerSettlement_companyId_idx" ON "DealerSettlement"("companyId");
CREATE INDEX "DealerSettlement_dealerId_idx" ON "DealerSettlement"("dealerId");
CREATE INDEX "DealerSettlement_settlementStatus_idx" ON "DealerSettlement"("settlementStatus");
CREATE INDEX "DealerSettlement_settlementStartDate_settlementEndDate_idx" ON "DealerSettlement"("settlementStartDate", "settlementEndDate");

CREATE INDEX "DealerPaymentHistory_settlementId_idx" ON "DealerPaymentHistory"("settlementId");
CREATE INDEX "DealerPaymentHistory_dealerId_idx" ON "DealerPaymentHistory"("dealerId");
CREATE INDEX "DealerPaymentHistory_paymentDate_idx" ON "DealerPaymentHistory"("paymentDate");

CREATE INDEX "FinanceAuditLog_companyId_idx" ON "FinanceAuditLog"("companyId");
CREATE INDEX "FinanceAuditLog_action_idx" ON "FinanceAuditLog"("action");
CREATE INDEX "FinanceAuditLog_entityType_entityId_idx" ON "FinanceAuditLog"("entityType", "entityId");
CREATE INDEX "FinanceAuditLog_createdAt_idx" ON "FinanceAuditLog"("createdAt");

ALTER TABLE "CommissionRule" ADD CONSTRAINT "CommissionRule_companyId_fkey" FOREIGN KEY ("companyId") REFERENCES "Company"("id") ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE "CommissionRule" ADD CONSTRAINT "CommissionRule_dealerId_fkey" FOREIGN KEY ("dealerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "CommissionRule" ADD CONSTRAINT "CommissionRule_productId_fkey" FOREIGN KEY ("productId") REFERENCES "Product"("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "PlatformRevenue" ADD CONSTRAINT "PlatformRevenue_companyId_fkey" FOREIGN KEY ("companyId") REFERENCES "Company"("id") ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE "PlatformRevenue" ADD CONSTRAINT "PlatformRevenue_orderId_fkey" FOREIGN KEY ("orderId") REFERENCES "Order"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "PlatformRevenue" ADD CONSTRAINT "PlatformRevenue_paymentId_fkey" FOREIGN KEY ("paymentId") REFERENCES "Payment"("id") ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE "PlatformRevenue" ADD CONSTRAINT "PlatformRevenue_dealerId_fkey" FOREIGN KEY ("dealerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "PlatformRevenue" ADD CONSTRAINT "PlatformRevenue_settlementId_fkey" FOREIGN KEY ("settlementId") REFERENCES "DealerSettlement"("id") ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "DealerSettlement" ADD CONSTRAINT "DealerSettlement_companyId_fkey" FOREIGN KEY ("companyId") REFERENCES "Company"("id") ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE "DealerSettlement" ADD CONSTRAINT "DealerSettlement_dealerId_fkey" FOREIGN KEY ("dealerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "DealerSettlement" ADD CONSTRAINT "DealerSettlement_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "DealerPaymentHistory" ADD CONSTRAINT "DealerPaymentHistory_settlementId_fkey" FOREIGN KEY ("settlementId") REFERENCES "DealerSettlement"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "DealerPaymentHistory" ADD CONSTRAINT "DealerPaymentHistory_dealerId_fkey" FOREIGN KEY ("dealerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "DealerPaymentHistory" ADD CONSTRAINT "DealerPaymentHistory_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "FinanceAuditLog" ADD CONSTRAINT "FinanceAuditLog_actorId_fkey" FOREIGN KEY ("actorId") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;
