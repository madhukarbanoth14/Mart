/**
 * Refuse to start the API against a production database when NODE_ENV is not production.
 * Prevents accidental writes during local dev / test.
 */
export function assertSafeDatabaseUrl(
  databaseUrl: string,
  nodeEnv: string,
): void {
  if (nodeEnv === 'production') {
    return;
  }
  if (process.env.ALLOW_PRODUCTION_DATABASE === 'true') {
    return;
  }

  const url = databaseUrl.toLowerCase();
  const markers = (
    process.env.PRODUCTION_DATABASE_HOSTS ??
    '34.93.71.44,cloudsql,/cloudsql/'
  )
    .split(',')
    .map((h) => h.trim().toLowerCase())
    .filter(Boolean);

  for (const marker of markers) {
    if (url.includes(marker)) {
      throw new Error(
        `DATABASE_URL looks like production (${marker}) but NODE_ENV=${nodeEnv}. ` +
          'Use the local test database (mart_test on localhost). ' +
          'Set ALLOW_PRODUCTION_DATABASE=true only if you intentionally need prod access.',
      );
    }
  }
}
