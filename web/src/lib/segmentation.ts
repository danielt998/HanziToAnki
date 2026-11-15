import { type DictEntry, dictionaryService } from './dictionary';

export type SegmentationMode = 'intl' | 'cedict-greedy';

export interface SegmentedWord {
  text: string;
  isChinese: boolean;
  dictEntry: DictEntry | null;
  hskLevel: number;
}

// Check if string contains at least one Chinese character (Han script)
export function isHanzi(str: string): boolean {
  return /[\u4e00-\u9fa5]/.test(str);
}

// Clean non-hanzi characters for pure vocabulary generation
export function isPureHanzi(str: string): boolean {
  return /^[\u4e00-\u9fa5]+$/.test(str);
}

/**
 * Bi-directional Maximum Matching (BiMM) segmenter using in-memory CC-CEDICT vocabulary.
 * Produces compound/greedy words (Jieba-like) without large external binary dependencies.
 */
export function segmentCedictGreedy(text: string, maxWordLen = 8): string[] {
  if (!text) return [];

  // Break text into Hanzi and non-Hanzi segments (preserving punctuation, spaces, etc.)
  const chunks = text.match(/[\u4e00-\u9fa5]+|[^\u4e00-\u9fa5]+/g) || [];
  const result: string[] = [];

  for (const chunk of chunks) {
    if (!isHanzi(chunk)) {
      result.push(chunk);
      continue;
    }

    // Forward Maximum Matching (FMM)
    const fmm: string[] = [];
    let i = 0;
    while (i < chunk.length) {
      let matched = false;
      for (let len = Math.min(maxWordLen, chunk.length - i); len > 1; len--) {
        const sub = chunk.slice(i, i + len);
        if (dictionaryService.lookup(sub)) {
          fmm.push(sub);
          i += len;
          matched = true;
          break;
        }
      }
      if (!matched) {
        fmm.push(chunk[i]);
        i++;
      }
    }

    // Reverse Maximum Matching (RMM)
    const rmm: string[] = [];
    let j = chunk.length;
    while (j > 0) {
      let matched = false;
      for (let len = Math.min(maxWordLen, j); len > 1; len--) {
        const sub = chunk.slice(j - len, j);
        if (dictionaryService.lookup(sub)) {
          rmm.unshift(sub);
          j -= len;
          matched = true;
          break;
        }
      }
      if (!matched) {
        rmm.unshift(chunk[j - 1]);
        j--;
      }
    }

    // Disambiguation between FMM and RMM:
    // 1. Prefer tokenization with fewer total tokens
    // 2. If equal, prefer tokenization with fewer single-character tokens
    if (fmm.length < rmm.length) {
      result.push(...fmm);
    } else if (rmm.length < fmm.length) {
      result.push(...rmm);
    } else {
      const fmmSingles = fmm.filter((w) => w.length === 1).length;
      const rmmSingles = rmm.filter((w) => w.length === 1).length;
      if (rmmSingles < fmmSingles) {
        result.push(...rmm);
      } else {
        result.push(...fmm);
      }
    }
  }

  return result;
}

/**
 * Tokenizes text into words using either browser native Intl.Segmenter
 * or dictionary-driven CC-CEDICT Bi-directional Maximum Matching (Jieba-like).
 * Matches CC-CEDICT definitions and HSK levels.
 */
export function segmentText(
  text: string,
  options: {
    mode?: SegmentationMode;
    deconstructWords?: boolean; // also include individual single characters of multi-char words
    excludeHskMax?: number; // exclude words with HSK <= excludeHskMax (e.g. exclude 1..3)
  } = {},
): SegmentedWord[] {
  if (!text) return [];

  const results: SegmentedWord[] = [];
  const mode = options.mode ?? 'intl';
  const deconstruct = options.deconstructWords ?? false;
  const excludeMax = options.excludeHskMax ?? 0;

  let rawTokens: string[] = [];

  if (mode === 'cedict-greedy') {
    rawTokens = segmentCedictGreedy(text);
  } else if (typeof Intl !== 'undefined' && 'Segmenter' in Intl) {
    const segmenter = new Intl.Segmenter('zh', { granularity: 'word' });
    rawTokens = Array.from(segmenter.segment(text)).map((s) => s.segment);
  } else {
    // Regex fallback
    rawTokens = text.match(/[\u4e00-\u9fa5]+|[^\u4e00-\u9fa5\s]+/g) || [];
  }

  for (const rawToken of rawTokens) {
    const rawText = rawToken.trim();
    if (!rawText) continue;

    const isChinese = isHanzi(rawText);
    const dictEntry = isChinese ? dictionaryService.lookup(rawText) : null;
    const hskLevel = isChinese ? dictEntry?.hskLevel || dictionaryService.getHskLevel(rawText) : 0;

    // Check HSK exclusion filter
    if (isChinese && excludeMax > 0 && hskLevel > 0 && hskLevel <= excludeMax) {
      continue;
    }

    results.push({
      text: rawText,
      isChinese,
      dictEntry,
      hskLevel,
    });

    // Optionally break down multi-character words into sub-characters
    if (deconstruct && isChinese && rawText.length > 1) {
      for (const char of rawText) {
        if (isPureHanzi(char)) {
          const charEntry = dictionaryService.lookup(char);
          const charHsk = charEntry?.hskLevel || dictionaryService.getHskLevel(char);
          if (excludeMax > 0 && charHsk > 0 && charHsk <= excludeMax) {
            continue;
          }
          results.push({
            text: char,
            isChinese: true,
            dictEntry: charEntry,
            hskLevel: charHsk,
          });
        }
      }
    }
  }

  return results;
}

/**
 * Tokenizes text specifically for the interactive Reader view,
 * keeping punctuation, spacing, and newlines intact for rendering paragraphs.
 */
export function tokenizeForReader(text: string, mode: SegmentationMode = 'intl'): SegmentedWord[] {
  if (!text) return [];

  let rawTokens: string[] = [];

  if (mode === 'cedict-greedy') {
    rawTokens = segmentCedictGreedy(text);
  } else if (typeof Intl !== 'undefined' && 'Segmenter' in Intl) {
    const segmenter = new Intl.Segmenter('zh', { granularity: 'word' });
    rawTokens = Array.from(segmenter.segment(text)).map((s) => s.segment);
  } else {
    rawTokens = text.split(/([ \t\r\n]+|[\u4e00-\u9fa5]+|[^\u4e00-\u9fa5\s]+)/g).filter(Boolean);
  }

  return rawTokens.map((part) => {
    const isChinese = isHanzi(part);
    const dictEntry = isChinese ? dictionaryService.lookup(part) : null;
    return {
      text: part,
      isChinese,
      dictEntry,
      hskLevel: isChinese ? dictEntry?.hskLevel || dictionaryService.getHskLevel(part) : 0,
    };
  });
}
