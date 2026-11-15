import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import zlib from 'node:zlib';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const distAssets = path.resolve(__dirname, '../dist/assets');

if (!fs.existsSync(distAssets)) {
  console.error('❌ dist/assets not found. Run npm run build first.');
  process.exit(1);
}

const files = fs.readdirSync(distAssets);
let maxJsGzip = 0;
let totalJsGzip = 0;

for (const file of files) {
  if (file.endsWith('.js')) {
    const content = fs.readFileSync(path.join(distAssets, file));
    const gzipped = zlib.gzipSync(content);
    totalJsGzip += gzipped.length;
    if (gzipped.length > maxJsGzip) maxJsGzip = gzipped.length;
    console.log(
      `📦 ${file}: ${(content.length / 1024).toFixed(1)} KB (gzip: ${(gzipped.length / 1024).toFixed(1)} KB)`,
    );
  }
}

const MAX_ALLOWED_GZIP_KB = 400;
const totalGzipKb = totalJsGzip / 1024;

if (totalGzipKb > MAX_ALLOWED_GZIP_KB) {
  console.error(
    `❌ Bundle budget exceeded! Total JS gzip: ${totalGzipKb.toFixed(1)} KB > limit of ${MAX_ALLOWED_GZIP_KB} KB`,
  );
  process.exit(1);
} else {
  console.log(
    `✅ Bundle budget passed! Total JS gzip: ${totalGzipKb.toFixed(1)} KB / limit: ${MAX_ALLOWED_GZIP_KB} KB`,
  );
}
