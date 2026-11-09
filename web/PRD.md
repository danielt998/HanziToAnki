# Product Requirements Document (PRD)
## HanziToAnki Web — Client-Side Chinese Learning & Flashcard Suite

---

## 1. Executive Summary

### 1.1 Mission
Transform **HanziToAnki** from a Java-dependent CLI and server into a **100% client-side, zero-install, zero-cost web application** hosted on GitHub Pages. 

### 1.2 Core Value Proposition
- **Zero Install & Frictionless**: Works instantly in any modern browser on desktop or mobile. No Java runtime, no Python, no Gradle, and no command-line flags required.
- **Total Privacy & Client-Side Sandbox**: User text, personal notes, and uploaded decks never leave the user's browser. Safe from server-side vulnerabilities (e.g. zip bombs, malformed files, or backend DDoS).
- **Modern Chinese NLP (`Intl.Segmenter`)**: Replaces legacy 3-gram heuristics with native ECMAScript internationalization word-boundary models, properly extracting **4-character idioms (成语)**, compound terms, and arbitrary-length vocabulary without multimegabyte WASM overhead.
- **HSK 3.0 First**: Built from the ground up for the official 9-tier HSK curriculum (11,092 words), with backward-compatibility for HSK 2.0.

---

## 2. Information Architecture & Navigation

The application consists of a single-page app (SPA) with a top navigation bar, a theme/settings toggle, and three dedicated functional tabs:

```
+-------------------------------------------------------------------------------+
|  🌱 HanziToAnki Web        [Text to Deck]  [Flashcard Tuner]  [Reader]    ⚙️ 🌙 |
+-------------------------------------------------------------------------------+
|                                                                               |
|                               ACTIVE TAB CONTENT                              |
|                                                                               |
+-------------------------------------------------------------------------------+
```

---

## 3. Detailed Feature Specifications

### Tab 1: Text to Deck (Flashcard Creator)
*Recreates and modernizes the original HanziToAnki CLI / API functionality.*

1. **Input Interface**:
   - Large text paste area with drag-and-drop file upload (`.txt`, `.pdf`, `.docx`).
   - Clean sample text buttons (e.g., "Beginner Story", "News Excerpt") for immediate test drives.
2. **Segmentation & Dictionary Extraction**:
   - Native `Intl.Segmenter('zh', { granularity: 'word' })` tokenizes the input text into semantic words.
   - Looks up definitions, traditional/simplified mappings, and pinyin in client-cached CC-CEDICT.
   - Optional **Deconstruct Words** toggle: generates cards for both composite words (e.g. `图书馆`) and their individual characters (`图书`, `馆`).
3. **Filtering Controls**:
   - **HSK Level Exclusion**: Exclude known words up to level $N$ (e.g. exclude HSK 1–3 to focus only on unfamiliar words).
   - **Character Set**: Output Simplified, Traditional, or Both (4-column format).
4. **Preview & Export**:
   - Interactive table preview of extracted cards showing Hanzi, Pinyin (tone-colored), definitions, and HSK level badge.
   - One-click export to:
     - **Anki TSV** (tab-delimited text ready for `File > Import` in Anki desktop or AnkiMobile).
     - **Anki `.apkg`** (direct binary import with embedded CSS styles).
     - **Standard Bookmark List** (tab-delimited for flashcards).

---

### Tab 2: Flashcard Tuner (Deck Curator) + Simple Guide
*Enables learners to customize giant, unwieldy public decks to their exact proficiency level.*

1. **Input & Deck Ingestion**:
   - Drag & drop existing flashcard decks (`.tsv`, `.txt`, `.csv`, `.apkg`).
   - Client-side parser extracts the Chinese character column automatically.
2. **Bandpass HSK Filter Matrix**:
   - Interactive checkbox matrix with live count statistics:
     ```
     [ ] HSK 1 (Beginner)     — 1,420 cards
     [ ] HSK 2 (Elementary)   —   950 cards
     [ ] HSK 3 (Intermediate) —   820 cards
     [x] HSK 4 (Target Level) —   510 cards  <-- SELECTED
     [ ] HSK 5 (Advanced)     —   340 cards
     [ ] HSK 6 (Proficiency)  —   220 cards
     [ ] HSK 7-9 (Advanced)   —   180 cards
     [ ] Non-HSK / Idioms     —   120 cards
     ```
   - Real-time counter badge: *"Original: 4,560 cards ➔ Tuned: 510 cards (88.8% reduction)"*.
3. **Embedded Tuner Guide**:
   - A clean, collapsible onboarding guide explaining the **$i+1$ Comprehensible Input Hypothesis**:
     - Why giant 5,000-word decks burn learners out.
     - How to isolate your current target HSK band to study high-yield vocabulary without getting bogged down by basics or paralyzed by rare literary terms.
4. **Export**:
   - Export filtered cards while preserving original user tags, note types, and supplementary fields.

---

### Tab 3: Interactive Clip Reader
*A responsive in-browser reading companion for immersion and reading practice.*

1. **Interactive Reading Canvas**:
   - Users paste articles, news stories, social media posts, or song lyrics.
   - The reader automatically tokenizes the text into clickable word chips.
2. **Word Tooltip & Popover**:
   - Clicking or hovering over any word opens a rich, informative definition card:
     - **Headwords**: Large Simplified and Traditional glyphs.
     - **Pronunciation**: Pinyin with standard tone marks and tone-colored accents.
     - **HSK Level**: Badge indicating HSK 1–9 tier or 成语 (Idiom) status.
     - **Definition**: Full CC-CEDICT definitions and measure words (CL / 词量).
3. **In-Reading Card Creator**:
   - A quick **"+" (Add to Deck)** button inside the tooltip saves that specific word to a "Current Session Deck" drawer.
   - At the end of a reading session, the user exports their curated session deck directly to Anki.

---

### Global Settings Modal (⚙️ Header Icon)

Accessible from any tab, persisted in `localStorage`:

1. **Tone Color Palette**:
   - **Standard Mandarin** (Default): Tone 1 Red, Tone 2 Green, Tone 3 Blue, Tone 4 Purple, Tone 5 Grey.
   - **Hanping / Trainchinese**: Tone 1 Blue, Tone 2 Green, Tone 3 Orange, Tone 4 Red.
   - **Custom Palette**: User-selectable color picker for each tone.
2. **Tone Coloring Target**:
   - Color Pinyin syllables only (Default).
   - Color Hanzi glyphs directly in text and cards.
3. **HSK Standard Selection**:
   - **HSK 3.0 (9 Levels)** — Default for modern study.
   - **HSK 2.0 (Classic 6 Levels)** — Backward compatibility for legacy tests.
4. **Display Options**:
   - Toggle Ruby / Furigana Pinyin annotations above characters in the Reader.
   - Font size, font family (KaiTi, SongTi, HeiTi, PingFang), and line height controls.
   - Dark / Light mode toggle.

---

## 4. Technical Architecture & Constraints

```
web/
├── index.html
├── package.json
├── vite.config.ts
├── tailwind.config.ts
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── components/
│   │   ├── Navbar.tsx
│   │   ├── SettingsModal.tsx
│   │   ├── TextToDeck/
│   │   ├── FlashcardTuner/
│   │   └── Reader/
│   ├── lib/
│   │   ├── segmentation.ts     <-- Intl.Segmenter wrapper with fallback
│   │   ├── dictionary.ts       <-- IndexedDB cache + CEDICT lookup
│   │   ├── toneHelper.ts       <-- Pinyin conversion & tone colorizer
│   │   ├── hskGrader.ts        <-- HSK 2.0 & HSK 3.0 tier classifier
│   │   └── ankiExporter.ts     <-- TSV generator and .apkg bundler
│   └── public/
│       ├── dict/
│       │   └── cedict_compact.json.gz
│       └── hsk/
│           ├── hsk3_vocab.json
│           └── hsk2_vocab.json
```

### 4.1 Performance Targets
- **Initial Bundle Size**: < 200 KB gzipped (excluding dictionary).
- **Dictionary Load**: Pre-indexed dictionary (~2.5 MB gzipped) fetched asynchronously and stored permanently in browser `IndexedDB`.
- **Segmentation Throughput**: > 50,000 characters/second using native `Intl.Segmenter`.
- **Zero Server Overhead**: 100% static hosting on GitHub Pages with HTTPS and CDN distribution.

---

## 5. UI/UX & Visual Design: Modern Playful Neo-Chinoiserie

### 5.1 The Aesthetic Vibe
A delightful blend of **modern, high-polish minimalism** with **playful traditional Chinese visual culture**:
- **Warm & Organic Paper Tones**:
  - Background: Warm Rice Paper white (`#FBF9F5` in light mode, deep Charcoal Tea `#1A1D20` in dark mode).
  - Primary Brand Accent: Vermilion / Lacquer Red (`#DC2626`).
  - Secondary Accents: Imperial Amber (`#D97706`), Jade Green (`#059669`), and Porcelain Indigo (`#2563EB`).
- **Traditional Motifs Reimagined**:
  - **Seal Stamp Badges (`印章`)**: HSK levels displayed inside subtle vermilion square seals with rounded corners and authentic border styling (e.g. `[ HSK 4 ]` stamped badge).
  - **Bao the Scholar Mascot**: A cute, encouraging dumpling character that appears on empty states and congratulates users (*"Bao filtered out 2,400 words! 320 spicy new words ready for Anki."*).
  - **Lantern & Cloud Micro-Details**: Subtle wave or cloud separators on card borders without cluttering the clean modern typography.

### 5.2 Responsive & Adaptive Layouts
- **Mobile-First Experience (< 768px)**:
  - **Bottom Navigation Bar**: Ergonomic thumb-friendly bottom dock with lively bouncy icon tabs (🏮 Text to Deck, 🎛️ Tuner, 📖 Reader, ⚙️ Settings).
  - **Large Touch Targets**: Checkboxes and tuner chips have generous hit areas ($\ge 48\text{px}$) with haptic-like micro-animations on touch.
  - **Full-Screen Reader**: Tap any word to summon a sleek bottom-sheet definition drawer.
- **Desktop Experience ($\ge 768px$)**:
  - **Top Navigation Bar**: Slim floating header with quick-switch tabs, tone palette selector, and dark mode toggle.
  - **Split-View Interactive Reader**: Left side for paste/source text; right side for live interactive clip reader with hovering tooltips and persistent sidebar vocabulary inspector.
  - **Keyboard Navigation**: Standard Anki shortcuts (`Space` to flip card preview, `1-4` to rate familiarity, `Cmd/Ctrl + Enter` to export).

### 5.3 Typography & Readability
- **Hanzi Character Stack**: Optimized for stroke clarity and aesthetic balance:
  `font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Noto Sans SC", "Microsoft YaHei", sans-serif;`
- **Calligraphic Accents**: `Ma Shan Zheng` / `Noto Serif SC` for decorative headers, seal stamps, and proverb banners.
- **Comfortable Reading Metrics**: 1.8x line height on Chinese paragraphs with generous character spacing (`tracking-wide`) to prevent glyph crowding.
