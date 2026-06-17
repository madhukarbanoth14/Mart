-- Areas can exist before a dealer is onboarded (territory assigned on dealer approval).
ALTER TABLE "Area" ALTER COLUMN "dealerId" DROP NOT NULL;
ALTER TABLE "Area" DROP CONSTRAINT IF EXISTS "Area_dealerId_fkey";
ALTER TABLE "Area" ADD CONSTRAINT "Area_dealerId_fkey" FOREIGN KEY ("dealerId") REFERENCES "User"("id") ON DELETE SET NULL ON UPDATE CASCADE;
