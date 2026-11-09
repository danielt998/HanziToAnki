# AGENTS.md — HanziToAnki Web Application

Guide and architectural reference for AI coding agents and human contributors working within `web/`.

---

## 1. Project Overview & Architecture

`web/` is a **100% client-side, zero-install, zero-cost** Chinese flashcard suite for **HanziToAnki**. It requires no backend servers, database, or external API keys, running entirely inside the user's browser.

- **Hosting & CI/CD**: Static deployment to **GitHub Pages** at `https://danielt998.github.io/HanziToAnki/` via `.github/workflows/deploy-web.yml`.
- **Base URL Portability**: Configured with `base: './'` in `vite.config.ts` and `import.meta.env.BASE_URL` in dynamic data fetches to seamlessly function on GitHub Pages subpaths or custom domains.
- **NLP & Segmentation**: Uses browser-native `Intl.Segmenter('zh', { granularity: 'word' })` for Chinese word tokenization.
- **Offline CC-CEDICT**: Loads `data/starter_dict.json` (<500 KB) for instant initial render, then streams the full dictionary (`data/dictionary.json`, ~33 MB) asynchronously in the background.

---

## 2. Target Device Resolutions & UX/UI Guidelines

When designing, testing, or updating UI components, evaluate against these **real-world physical target devices**:

| Device | Type | CSS Viewport | Aspect Ratio | Key Layout Constraints |
| :--- | :--- | :---: | :---: | :--- |
| **Apple MacBook Air 13"** | Desktop | **`1280 × 832`** | 16:10 | Sticky top navigation bar, 2-column reader canvas, max-w-6xl container. |
| **Google Pixel 8** | Mobile | **`393 × 873`** | ~20:9 | Fixed bottom navigation dock (64px), cards collapse to single column, touch-friendly tap targets ($\ge 44\text{px}$). |

### Design System Principles (Neo-Chinoiserie)
- **Palette**: "Rice paper" ivory background (`#FBF9F5` light, `#121212` dark), paired with Imperial Chinese Red accents (`#DC2626`).
- **HSK Difficulty Badges**: Relative difficulty gradient:
  - Baseline / Lowest Included: **Emerald Green**
  - +1 Tier Above: **Amber / Warm Yellow**
  - +2 Tiers Above: **Orange**
  - +3+ Tiers Above: **Chinese Red Seal** (`印章`)
  - Untiered: **Neutral Gray**
- **Mascot ("Bao the Scholar")**: Encouraging dumpling mascot banner. Stats badges use subtle pill tags (`bg-chinese-red/10 text-chinese-red border border-chinese-red/25`), never loud solid blobs.
- **Terminology Rule**: Use **"Standard"** for the default tone palette and **"Clip Reader"** for the reading canvas. Avoid referencing third-party proprietary app names.

---

## 3. Tooling & Key Commands

We use **Biome** for blazing-fast (single-binary, Rust-powered) linting and formatting, replacing ESLint and Prettier.

| Task | Command | Description |
| :--- | :--- | :--- |
| **Dev Server** | `npm run dev` | Starts Vite local development server on `http://localhost:5173`. |
| **Format & Lint Check** | `npm run check` | Runs Biome over all source files. Fails if formatting or lint rules are violated. |
| **Auto-Format** | `npm run format` | Auto-formats code using Biome's fast formatter. |
| **Linter Only** | `npm run lint` | Runs Biome linter diagnostics. |
| **Production Build** | `npm run build` | Compiles TypeScript (`tsc`) and bundles static assets with Vite into `dist/`. |
| **E2E Tests** | `npm run test:e2e` | Runs Playwright tests, capturing dual MacBook Air and Pixel 8 screenshots. |

---

## 4. Pull Request Requirements & CI Pipeline

All pull requests touching `web/` must pass the automated GitHub Actions CI checks (`.github/workflows/web-ci.yml`):
1. **Biome Check**: `npm run check` must pass with zero errors.
2. **Typecheck & Build**: `npm run build` must complete cleanly with zero TypeScript errors.
3. **Playwright E2E**: `npm run test:e2e` must pass all stories and generate updated screenshots.
4. **Visual Comparison Table**: Embed the dual-column comparison table (Desktop vs Mobile) in the PR description using the template in `.github/pull_request_template.md`.
