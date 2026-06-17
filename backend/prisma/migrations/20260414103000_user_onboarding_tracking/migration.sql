-- Track which employee/admin onboarded a user; optional notes (e.g. document file names).
ALTER TABLE "User" ADD COLUMN "onboardingNotes" TEXT;
ALTER TABLE "User" ADD COLUMN "onboardedById" TEXT;

CREATE INDEX "User_onboardedById_idx" ON "User"("onboardedById");

ALTER TABLE "User" ADD CONSTRAINT "User_onboardedById_fkey" FOREIGN KEY ("onboardedById") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;
