-- Fix common misspellings of Warangal in area names.
UPDATE "Area"
SET name = 'Warangal'
WHERE LOWER(TRIM(name)) IN ('warrangal', 'wrangal', 'warangal');

UPDATE "Area"
SET name = 'Warangal Urban'
WHERE LOWER(TRIM(name)) IN ('warrangal urban', 'wrangal urban', 'warangal urban');

UPDATE "Area"
SET name = 'Warangal Rural'
WHERE LOWER(TRIM(name)) IN ('warrangal rural', 'wrangal rural', 'warangal rural');
