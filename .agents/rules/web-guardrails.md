# Web Architecture & Code Guardrails

## 1. Zero-Regression Vibe Coding Principles
- **Keep It Simple (KISS)**: Favor clean TypeScript, functional React components, and Tailwind utility classes over heavy state management libraries.
- **Fast Linting & Formatting**: Biome is the sole linter and formatter. Run `npm run check` before any commit. Zero TypeScript `any` types allowed.
- **Bundle Budget**: Initial JS bundle must not exceed 400 KB gzipped. Enforced via `npm run check:size`.

## 2. Asset Loading & Performance
- **Starter Bundle**: `starter_dict.json` (~170 KB gzipped) and `hsk_map.json` (~18 KB gzipped) cover 100% of HSK 1–6 vocabulary and must load synchronously in < 25ms.
- **Lazy Loading**: `dictionary.json` (~4.2 MB gzipped) and `sql-wasm-browser.wasm` (~669 KB) must be loaded lazily on demand, never blocking the critical render path.
- **Browser Caching**: Cache heavy assets using the browser Cache API / CacheStorage so repeat visits use 0 KB of network data.

## 3. Brand & License Integrity
- **Proprietary Trademarks**: Never mention or reference proprietary apps (e.g. Pleco). Always use "Standard" for tone palettes and "Clip Reader" for Tab 3.
- **Attribution**: Always maintain CC-CEDICT license attribution in the application footer.
- **Author Identity**: All git commits by James must use `--author="James Clark <james.sw.clark@gmail.com>"`.
