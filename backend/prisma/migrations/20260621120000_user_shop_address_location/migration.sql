-- Shop/business name, postal address and GPS coordinates captured at onboarding.
ALTER TABLE "User" ADD COLUMN "shopName" TEXT;
ALTER TABLE "User" ADD COLUMN "address" TEXT;
ALTER TABLE "User" ADD COLUMN "latitude" DOUBLE PRECISION;
ALTER TABLE "User" ADD COLUMN "longitude" DOUBLE PRECISION;
