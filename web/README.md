# 🎋 HanziToAnki Web

> **100% Client-Side Flashcard Suite & Reader for Mandarin Chinese Learners**  
> Runs entirely in your browser with zero installation, zero tracking, and zero backend servers.

🌐 **Live GitHub Pages URL**: [https://danielt998.github.io/HanziToAnki/](https://danielt998.github.io/HanziToAnki/)

---

## ✨ Features

### 1. 🎴 Text to Deck (Creator)
- **Natural Language Word Segmentation**: Tokenizes Chinese articles, dialogues, and idioms into vocabulary words using native browser `Intl.Segmenter`.
- **Intelligent HSK Filtering**: Excludes known beginner vocabulary (e.g., skip HSK 1 or HSK 1–3) with singular/plural-aware counter pills.
- **Dynamic Difficulty Gradients**: Visually styles HSK badges relative to your selected base level:
  - 🟢 Lowest Included: Jade Green
  - 🟡 Base + 1: Amber / Warm Gold
  - 🟠 Base + 2: Orange
  - 🔴 Base + 3+: Chinese Red Seal
  - ⚪ Untiered: Neutral Grey
- **Instant Anki Export**: Generates compliant `.tsv` files with official Anki directives (`#separator:tab`, `#html:true`, `#tags:hanziToAnki`).

### 2. 🎛️ Flashcard Tuner (Deck Cleaner)
- **Direct `.apkg` & `.tsv` Ingestion**: Drag-and-drop existing Anki decks or paste TSV content.
- **Native Audio Preservation (Zero Data Loss)**: Ingests `.apkg` SQLite databases and zip media via client-side `sql.js` (WebAssembly) and `fflate`. Retains all native audio recordings (`[sound:...]`) and custom fields. Excluded cards and orphaned audio tracks are cleanly pruned upon re-export.
- **HSK Level Toggles**: Interactive checkboxes for HSK 1–6 and Untiered words to isolate vocabulary exactly at your current study level.
- **Standardized Export Naming**: Re-exports tuned decks as `<source>_tuned_hanziToAnki.apkg` or `.tsv`.

### 3. 📖 Interactive Clip Reader
- **Word Tokenization**: Reads Chinese text with tone-colored character chips.
- **Hover & Click Definitions**: Click any word to inspect Pinyin, CC-CEDICT definitions, and HSK tier in the side inspector.
- **Session Deck Accumulation**: Build an ad-hoc flashcard deck of unfamiliar words as you read, with 1-click TSV export.

### 4. ⚙️ Preferences & Tone Settings
- **Tone Color Palettes**: Choose from *Standard*, *Hanping / Trainchinese*, *Vibrant Rainbow*, or *Monochrome Minimal*.
- **5-Tone Swatches**: Full color dots for tones 1–5 (including neutral tone).
- **Hanzi Character Coloring**: Toggle coloring individual Chinese character glyphs directly.
- **HSK Curriculum**: Toggle between HSK 3.0 (9 Tiers) and HSK 2.0 (6 Tiers).
- **Dark Mode**: Complete light/dark theme support matching your system preferences.

---

## ⚡ Performance & Mobile Data Protection

- **Fast Initial Render**: Initial page load bundle is only **~112 KB gzipped**.
- **Starter Vocab Bundle**: All 4,991 HSK 1–6 words are pre-indexed in `starter_dict.json` (~170 KB gzipped) and `hsk_map.json` (~18 KB gzipped) for instant ~15ms startup.
- **Lazy Extended Dictionary**: The 182,000-word CC-CEDICT file (`dictionary.json`, ~4.2 MB) is loaded on demand only when untiered words are encountered.
- **Browser Cache Storage**: Heavy assets are cached permanently via `CacheStorage` (`caches.open`) so subsequent visits consume 0 bytes of network data. Respects mobile Data Saver (`navigator.connection.saveData`).

---

## 🛠️ Development & Tooling

```bash
cd web

# 1. Install dependencies (automatically sets up .githooks/pre-commit)
npm install

# 2. Start local development server
npm run dev

# 3. Production build
npm run build

# 4. Fast Biome check & linting (< 30ms)
npm run check
npm run lint

# 5. Auto-format and fix
npm run fix

# 6. Verify bundle size budget (< 400 KB gzip)
npm run check:size

# 7. Run Playwright E2E User Story Tests (MacBook Air & Pixel 8 Viewports)
npm run test:e2e
```

---

## 🧪 Testing Strategy

The test suite runs 5 end-to-end user stories against real device viewport resolutions:
- **Desktop**: Apple MacBook Air 13" (`1280 × 832`)
- **Mobile**: Google Pixel 8 (`393 × 873`)

- **Story 1**: Text-to-Deck vocabulary extraction and TSV export.
- **Story 2**: Flashcard Tuner filtering by HSK tiers with relative badge styling.
- **Story 3**: Interactive Clip Reader tokenization and session deck collection.
- **Story 4**: Global Settings modal, SVG icons, and tone palette switching.
- **Story 5**: Tuner `.apkg` audio cleanup using synthetic fixture (`minimal_hsk1_hsk2.apkg`) and binary export.

---

## 📄 License & Attribution

- Built as part of [HanziToAnki](https://github.com/danielt998/HanziToAnki).
- Dictionary data provided by [CC-CEDICT](https://cc-cedict.org/wiki/) licensed under [Creative Commons Attribution-ShareAlike 3.0](https://creativecommons.org/licenses/by-sa/3.0/).
