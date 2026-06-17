# Database setup & troubleshooting

## Prisma `P1001` (can’t reach database) right after `docker compose up -d`

The container may **not be running**. Check:

```bash
cd Mart/backend
npm run db:ps
# or if you use the 5435 override file:
npm run db:ps:5435
npm run db:logs
```

- **STATUS should be `running`**, not `Restarting` or `Exited`.
- If logs show **`exec format error`**: the Postgres image **CPU arch** does not match what Docker can run (wrong `platform`, bad cache, or emulation off).

**Default in this repo:** `docker-compose.yml` uses **`platform: linux/amd64`** so the same image runs on **Intel Mac**, **Linux CI**, and **Apple Silicon** (via Docker’s x86 emulation).

**Apple Silicon:** turn on **Docker Desktop → Settings → General → “Use Rosetta for x86/amd64 emulation on Apple Silicon”**, then:

```bash
cd Mart/backend
docker compose -f docker-compose.yml -f docker-compose.port5435.yml down
docker rmi postgres:16-alpine 2>/dev/null || true
npm run db:up:5435
npm run db:logs:5435
```

**Prefer native ARM Postgres on Apple Silicon** (no x86 emulation): `npm run db:up:5435:arm64` (merges `docker-compose.platform.arm64.yml`).

**Still broken after `docker rmi`?** Wipe the compose volume (deletes DB data):  
`docker compose -f docker-compose.yml -f docker-compose.port5435.yml down -v` then `npm run db:up:5435` again.

---

## Docker: `commit failed` / `overlayfs/metadata.db` / `input/output error`

That message comes from **Docker Desktop’s storage**, not from this repo.

1. **Free disk space** on your Mac (Docker + images need several GB; low disk causes I/O errors). Check with `df -h`.
2. **Restart Docker Desktop** (fully quit from the menu bar, open again).
3. If it still fails: Docker Desktop → **Troubleshoot** (bug icon) → **Clean / Purge data** or **Reset to factory defaults** (this removes local images/containers/volumes).
4. From `Mart/backend` run again:
   ```bash
   docker compose down -v
   docker compose up -d
   ```
5. If **port 5432 is already used** by Homebrew Postgres, use the alternate port (see below).

---

## Prisma `P1010` / access denied

Your `.env` `DATABASE_URL` does not match a working Postgres user/password, or the `mart` database does not exist.

- With **Docker** (default compose): user `postgres`, password `postgres`, database `mart`.
- With **Homebrew Postgres**, typical URL (trust auth, your macOS username):

  ```bash
  createdb mart   # once
  ```

  Then in `.env`:

  ```env
  DATABASE_URL="postgresql://YOUR_MAC_USERNAME@localhost:5432/mart?schema=public"
  ```

  Replace `YOUR_MAC_USERNAME` with the output of `whoami`.

---

## Use Docker on port **5435** (when 5432 is taken)

```bash
cd Mart/backend
docker compose -f docker-compose.yml -f docker-compose.port5435.yml up -d
```

Set in `.env`:

```env
DATABASE_URL="postgresql://postgres:postgres@localhost:5435/mart?schema=public"
```

Or: `npm run db:up:5435`

### Prisma `P1001` still says `localhost:5432` after `db:up:5435`

Compose only changes which **host** port maps to Postgres. **Prisma reads `DATABASE_URL` from `.env`**, which probably still uses `5432`.

**Fix (pick one):**

1. Edit `Mart/backend/.env` and set port **5435** in `DATABASE_URL`, then run `npx prisma migrate deploy` as usual.

2. Or run migrations/seed with an inline URL (no `.env` edit):

   ```bash
   npm run prisma:migrate:deploy:docker5435
   npm run prisma:seed:docker5435
   ```

   For `npm run start:dev` the app still needs `.env` updated to `5435`, unless you always use the default `docker compose up -d` on port 5432.

---

## After Postgres is reachable

```bash
cd Mart/backend
npx prisma migrate deploy
npm run prisma:seed
```
