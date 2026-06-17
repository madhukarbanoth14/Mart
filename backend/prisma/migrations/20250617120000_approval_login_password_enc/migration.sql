-- Employee-set login password (encrypted) until admin approval email is sent.
ALTER TABLE "User" ADD COLUMN IF NOT EXISTS "approval_login_password_enc" TEXT;
