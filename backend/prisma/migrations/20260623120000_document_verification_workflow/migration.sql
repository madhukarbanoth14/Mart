-- Document verification workflow + self-registration support

CREATE TYPE "UserDocumentStatus" AS ENUM ('NOT_UPLOADED', 'PENDING_VERIFICATION', 'VERIFIED', 'REJECTED');
CREATE TYPE "DocumentVerificationStatus" AS ENUM ('PENDING_VERIFICATION', 'VERIFIED', 'REJECTED');
CREATE TYPE "BusinessDocumentType" AS ENUM ('AADHAAR', 'PAN', 'GST', 'TRADE_LICENSE');

ALTER TABLE "User" ADD COLUMN "state" TEXT;
ALTER TABLE "User" ADD COLUMN "district" TEXT;
ALTER TABLE "User" ADD COLUMN "referralCode" TEXT;
ALTER TABLE "User" ADD COLUMN "documentUploaded" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "User" ADD COLUMN "canPlaceOrders" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "User" ADD COLUMN "documentStatus" "UserDocumentStatus" NOT NULL DEFAULT 'NOT_UPLOADED';
ALTER TABLE "User" ADD COLUMN "lastFollowUpAt" TIMESTAMP(3);

CREATE UNIQUE INDEX "User_referralCode_key" ON "User"("referralCode");
CREATE INDEX "User_documentStatus_idx" ON "User"("documentStatus");

ALTER TABLE "OnboardingDocument" ADD COLUMN "documentType" "BusinessDocumentType";
ALTER TABLE "OnboardingDocument" ADD COLUMN "verificationStatus" "DocumentVerificationStatus" NOT NULL DEFAULT 'PENDING_VERIFICATION';
ALTER TABLE "OnboardingDocument" ADD COLUMN "verifiedById" TEXT;
ALTER TABLE "OnboardingDocument" ADD COLUMN "verifiedAt" TIMESTAMP(3);
ALTER TABLE "OnboardingDocument" ADD COLUMN "rejectionReason" TEXT;

CREATE INDEX "OnboardingDocument_verificationStatus_idx" ON "OnboardingDocument"("verificationStatus");

ALTER TABLE "OnboardingDocument" ADD CONSTRAINT "OnboardingDocument_verifiedById_fkey"
  FOREIGN KEY ("verifiedById") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "Area" ADD COLUMN "state" TEXT;
ALTER TABLE "Area" ADD COLUMN "district" TEXT;
ALTER TABLE "Area" ADD COLUMN "employeeId" TEXT;

CREATE INDEX "Area_employeeId_idx" ON "Area"("employeeId");

ALTER TABLE "Area" ADD CONSTRAINT "Area_employeeId_fkey"
  FOREIGN KEY ("employeeId") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;

CREATE TABLE "PhoneOtp" (
  "id" TEXT NOT NULL,
  "phone" TEXT NOT NULL,
  "code" TEXT NOT NULL,
  "purpose" TEXT NOT NULL DEFAULT 'REGISTER',
  "expiresAt" TIMESTAMP(3) NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "PhoneOtp_pkey" PRIMARY KEY ("id")
);

CREATE INDEX "PhoneOtp_phone_purpose_idx" ON "PhoneOtp"("phone", "purpose");

-- Backfill document flags for existing users with uploaded documents
UPDATE "User" u
SET
  "documentUploaded" = true,
  "documentStatus" = 'PENDING_VERIFICATION',
  "canPlaceOrders" = (u."status" = 'ACTIVE')
WHERE EXISTS (
  SELECT 1 FROM "OnboardingDocument" d
  WHERE d."userId" = u.id
    AND d."verificationStatus" IN ('PENDING_VERIFICATION', 'VERIFIED')
);
