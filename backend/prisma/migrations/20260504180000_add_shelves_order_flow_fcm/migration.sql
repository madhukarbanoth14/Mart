-- ProductShelf: new FMCG categories
ALTER TYPE "ProductShelf" ADD VALUE 'MILK_DRINKS';
ALTER TYPE "ProductShelf" ADD VALUE 'DETERGENTS';
ALTER TYPE "ProductShelf" ADD VALUE 'FEMININE_BABY_CARE';
ALTER TYPE "ProductShelf" ADD VALUE 'SHAMPOOS';
ALTER TYPE "ProductShelf" ADD VALUE 'GENERAL_CARE';
ALTER TYPE "ProductShelf" ADD VALUE 'ATTA';
ALTER TYPE "ProductShelf" ADD VALUE 'BISCUITS_COOKIES';
ALTER TYPE "ProductShelf" ADD VALUE 'NOODLES';
ALTER TYPE "ProductShelf" ADD VALUE 'CHOCOLATES';
ALTER TYPE "ProductShelf" ADD VALUE 'AGARBATTIS';
ALTER TYPE "ProductShelf" ADD VALUE 'SOAPS';
ALTER TYPE "ProductShelf" ADD VALUE 'STATIONERY';
ALTER TYPE "ProductShelf" ADD VALUE 'COFFEE';
ALTER TYPE "ProductShelf" ADD VALUE 'SAUCE';
ALTER TYPE "ProductShelf" ADD VALUE 'EDIBLE_OIL';
ALTER TYPE "ProductShelf" ADD VALUE 'NON_EDIBLE_OIL';

-- Order: track updates
ALTER TABLE "Order" ADD COLUMN IF NOT EXISTS "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- User: FCM + phone uniqueness (nullable unique allows multiple NULLs in Postgres)
ALTER TABLE "User" ADD COLUMN IF NOT EXISTS "fcmToken" TEXT;

DROP INDEX IF EXISTS "User_phone_key";
CREATE UNIQUE INDEX IF NOT EXISTS "User_phone_key" ON "User"("phone");

-- OrderStatus: replace ACCEPTED with richer lifecycle
CREATE TYPE "OrderStatus_new" AS ENUM ('PENDING', 'DEALER_CONFIRMED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED');

ALTER TABLE "Order" ALTER COLUMN "status" DROP DEFAULT;

ALTER TABLE "Order"
  ALTER COLUMN "status" TYPE "OrderStatus_new"
  USING (
    CASE "status"::text
      WHEN 'ACCEPTED' THEN 'DEALER_CONFIRMED'::"OrderStatus_new"
      WHEN 'PENDING' THEN 'PENDING'::"OrderStatus_new"
      WHEN 'DELIVERED' THEN 'DELIVERED'::"OrderStatus_new"
      ELSE 'PENDING'::"OrderStatus_new"
    END
  );

DROP TYPE "OrderStatus";
ALTER TYPE "OrderStatus_new" RENAME TO "OrderStatus";

ALTER TABLE "Order" ALTER COLUMN "status" SET DEFAULT 'PENDING'::"OrderStatus";
