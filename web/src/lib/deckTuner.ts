import { dictionaryService } from './dictionary';
import { isHanzi } from './segmentation';

export interface TunerCard {
  originalLine: string;
  chineseWord: string;
  hskLevel: number; // 0 for untiered, 1-6
  columns: string[];
  noteId?: number | string;
}

export interface TunerResult {
  totalCards: number;
  filteredCards: TunerCard[];
  hskCounts: Record<number, number>; // counts per level
  excludedCount: number;
}

export function cleanHanziCandidate(raw: string): string {
  let text = raw
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/\[sound:[^\]]+\]/g, '');
  text = text.replace(/\+[^+]+\+/g, ' '); // Remove grammar template markers like +adjective+
  return text.trim();
}

export function resolveCardHsk(text: string): number {
  const onlyHanzi = text.replace(/[^\u4e00-\u9fa5]/g, '');
  if (!onlyHanzi) return 0;

  // 1. Direct match on full stripped headword
  const direct = dictionaryService.getHskLevel(onlyHanzi);
  if (direct > 0) return direct;

  // 2. Segment phrase/sentence using native Intl.Segmenter and find max HSK among words
  try {
    const segmenter = new Intl.Segmenter('zh', { granularity: 'word' });
    const segments = Array.from(segmenter.segment(text))
      .map((s) => s.segment.replace(/[^\u4e00-\u9fa5]/g, ''))
      .filter((w) => w.length > 0);

    let maxHsk = 0;
    for (const word of segments) {
      const lvl = dictionaryService.getHskLevel(word);
      if (lvl > maxHsk) maxHsk = lvl;
    }
    return maxHsk;
  } catch {
    return 0;
  }
}

/**
 * Parses raw deck text (TSV or CSV) and extracts Chinese headwords and their HSK level.
 */
export function parseDeck(rawContent: string): TunerCard[] {
  const lines = rawContent
    .split(/\r?\n/)
    .filter((line) => line.trim().length > 0);
  const cards: TunerCard[] = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    // Skip comment lines or obvious headers
    if (
      line.startsWith('#') ||
      (i === 0 &&
        (line.includes('Simplified') ||
          line.includes('Front') ||
          line.includes('Expression')))
    ) {
      continue;
    }

    const delimiter = line.includes('\t')
      ? '\t'
      : line.includes(',')
        ? ','
        : ';';
    const cols = line
      .split(delimiter)
      .map((c) => c.trim().replace(/^"|"$/g, ''));

    // Find all columns containing Chinese characters
    const hanziCandidates: {
      text: string;
      length: number;
      colIndex: number;
    }[] = [];
    for (let cIdx = 0; cIdx < cols.length; cIdx++) {
      const col = cols[cIdx];
      const cleaned = cleanHanziCandidate(col);
      if (cleaned.length > 0 && isHanzi(cleaned)) {
        hanziCandidates.push({
          text: cleaned,
          length: cleaned.length,
          colIndex: cIdx,
        });
      }
    }

    // Pick the best headword candidate: prefer short vocabulary (<= 6 chars) over long sentences
    let chineseCol = '';
    if (hanziCandidates.length > 0) {
      const vocabCandidates = hanziCandidates.filter((c) => c.length <= 6);
      if (vocabCandidates.length > 0) {
        chineseCol = vocabCandidates[0].text;
      } else {
        hanziCandidates.sort((a, b) => a.length - b.length);
        chineseCol = hanziCandidates[0].text;
      }
    }

    if (chineseCol) {
      const hsk = resolveCardHsk(chineseCol);
      cards.push({
        originalLine: line,
        chineseWord: chineseCol,
        hskLevel: hsk,
        columns: cols,
      });
    }
  }

  return cards;
}

/**
 * Filters parsed cards based on which HSK levels are enabled.
 * enabledLevels is a Set containing numbers 1..6 (and 0 for untiered/other words).
 */
export function filterDeck(
  cards: TunerCard[],
  enabledLevels: Set<number>,
): TunerResult {
  const hskCounts: Record<number, number> = {
    0: 0,
    1: 0,
    2: 0,
    3: 0,
    4: 0,
    5: 0,
    6: 0,
  };
  const filtered: TunerCard[] = [];

  for (const card of cards) {
    const lvl = card.hskLevel;
    hskCounts[lvl] = (hskCounts[lvl] || 0) + 1;
    if (enabledLevels.has(lvl)) {
      filtered.push(card);
    }
  }

  return {
    totalCards: cards.length,
    filteredCards: filtered,
    hskCounts,
    excludedCount: cards.length - filtered.length,
  };
}
