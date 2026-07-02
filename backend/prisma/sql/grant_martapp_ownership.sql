-- Run once in GCP Console → Cloud SQL → mart-pg → Cloud SQL Studio (as postgres).
-- Gives martapp ownership of all public tables so migrations can use DATABASE_URL (martapp)
-- instead of a separate postgres MIGRATE_DATABASE_URL.
--
-- After this, you may set MIGRATE_DATABASE_URL to the same value as DATABASE_URL.

DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT tablename
    FROM pg_tables
    WHERE schemaname = 'public'
  LOOP
    EXECUTE format('ALTER TABLE public.%I OWNER TO martapp', r.tablename);
  END LOOP;
END $$;

DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT sequence_name
    FROM information_schema.sequences
    WHERE sequence_schema = 'public'
  LOOP
    EXECUTE format('ALTER SEQUENCE public.%I OWNER TO martapp', r.sequence_name);
  END LOOP;
END $$;

ALTER SCHEMA public OWNER TO martapp;
