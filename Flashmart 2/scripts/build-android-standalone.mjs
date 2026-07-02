#!/usr/bin/env node
/**
 * Bundle Flashmart 2/Flashmart Android.html into a single portable HTML file.
 * Output: Mart/Flashmart Android (standalone).html
 */
import fs from 'fs';
import path from 'path';
import zlib from 'zlib';
import { randomUUID } from 'crypto';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const FM_ROOT = path.resolve(__dirname, '..');
const ENTRY = path.join(FM_ROOT, 'Flashmart Android.html');
const OUT_MAIN = path.resolve(FM_ROOT, '..', 'Flashmart Android (standalone).html');
const OUT_COPY = path.join(FM_ROOT, 'Flashmart Android (standalone).html');

const LOADER_JS = fs.readFileSync(path.join(__dirname, 'standalone-loader.js'), 'utf8');
const SHELL_CSS = fs.readFileSync(path.join(__dirname, 'standalone-shell.css'), 'utf8');
const SHELL_BODY = fs.readFileSync(path.join(__dirname, 'standalone-shell.html'), 'utf8');

const manifest = {};
const urlToUuid = new Map();

function mimeFor(url, buf) {
  if (url.includes('.css') || url.includes('fonts.googleapis.com')) return 'text/css';
  if (url.endsWith('.jsx')) return 'application/javascript';
  if (url.includes('babel')) return 'text/javascript';
  if (url.includes('react-dom') || url.includes('react@')) return 'text/javascript';
  if (buf[0] === 0x77 && buf[1] === 0x4f) return 'font/woff2';
  if (url.endsWith('.js')) return 'text/javascript';
  return 'application/octet-stream';
}

function scriptTypeFor(url) {
  if (url.endsWith('.jsx')) return 'text/babel';
  return '';
}

async function loadBytes(url, baseDir) {
  if (url.startsWith('http://') || url.startsWith('https://')) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status} for ${url}`);
    return Buffer.from(await res.arrayBuffer());
  }
  const filePath = path.resolve(baseDir, url);
  return fs.readFileSync(filePath);
}

async function bundleUrl(url, baseDir) {
  const key = url;
  if (urlToUuid.has(key)) return urlToUuid.get(key);

  let buf = await loadBytes(url, baseDir);
  let mime = mimeFor(url, buf);

  if (mime === 'text/css' || url.includes('fonts.googleapis.com')) {
    let css = buf.toString('utf8');
    const refs = [...css.matchAll(/url\(([^)]+)\)/g)];
    for (const ref of refs) {
      let raw = ref[1].trim().replace(/^['"]|['"]$/g, '');
      if (raw.startsWith('data:')) continue;
      const resolved = raw.startsWith('http')
        ? raw
        : new URL(raw, url.startsWith('http') ? url : `file://${path.resolve(baseDir, url)}`).href;
      const fontUuid = await bundleUrl(resolved, baseDir);
      css = css.split(ref[0]).join(`url("${fontUuid}")`);
    }
    buf = Buffer.from(css, 'utf8');
    mime = 'text/css';
  }

  const uuid = randomUUID();
  urlToUuid.set(key, uuid);
  manifest[uuid] = {
    mime,
    compressed: true,
    data: zlib.gzipSync(buf).toString('base64'),
  };
  return uuid;
}

function extractAssets(html) {
  const assets = [];
  for (const m of html.matchAll(/<link[^>]+href=["']([^"']+)["'][^>]*>/gi)) {
    const tag = m[0];
    const url = m[1];
    if (tag.includes('rel="preconnect"') || tag.includes("rel='preconnect'")) continue;
    if (!url.endsWith('.css') && !url.includes('fonts.googleapis.com/css')) continue;
    assets.push({ url });
  }
  for (const m of html.matchAll(/<script[^>]+src=["']([^"']+)["'][^>]*>/gi)) {
    assets.push({ url: m[1] });
  }
  return assets;
}

async function build() {
  const entryHtml = fs.readFileSync(ENTRY, 'utf8');
  const baseDir = FM_ROOT;
  const assets = extractAssets(entryHtml);

  for (const asset of assets) {
    await bundleUrl(asset.url, baseDir);
  }

  let template = entryHtml;
  const sorted = [...urlToUuid.entries()].sort((a, b) => b[0].length - a[0].length);
  for (const [url, uuid] of sorted) {
    template = template.split(url).join(uuid);
  }

  const shell = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>FlashMart — Android Material 3 Gallery</title>
  <style>${SHELL_CSS}</style>
  <noscript>
    <style>#__bundler_loading { display: none; }</style>
    <div class="fm-noscript">This gallery requires JavaScript. Open in Chrome or Safari.</div>
  </noscript>
</head>
<body>
${SHELL_BODY}
  <script>${LOADER_JS}</script>
  <script type="__bundler/manifest">
${JSON.stringify(manifest)}
  </script>
  <script type="__bundler/template">
${JSON.stringify(template)}
  </script>
  <script type="__bundler/ext_resources">[]</script>
</body>
</html>`;

  fs.writeFileSync(OUT_MAIN, shell);
  fs.writeFileSync(OUT_COPY, shell);
  console.log(`Wrote ${OUT_MAIN}`);
  console.log(`Wrote ${OUT_COPY}`);
  console.log(`Assets: ${Object.keys(manifest).length}`);
}

build().catch((err) => {
  console.error(err);
  process.exit(1);
});
