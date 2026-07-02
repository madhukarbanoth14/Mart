/** Canonical spellings for known territory names (case-insensitive keys). */
const AREA_NAME_ALIASES: Record<string, string> = {
  warrangal: 'Warangal',
  wrangal: 'Warangal',
  warangal: 'Warangal',
  'warangal urban': 'Warangal Urban',
  'warangal rural': 'Warangal Rural',
};

export function normalizeAreaName(raw: string): string {
  const trimmed = raw.trim().replace(/\s+/g, ' ');
  if (!trimmed) {
    return trimmed;
  }
  const alias = AREA_NAME_ALIASES[trimmed.toLowerCase()];
  return alias ?? trimmed;
}
