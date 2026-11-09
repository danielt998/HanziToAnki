import { DictEntry, dictionaryService } from './dictionary';

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
 * Tokenizes text into words using native browser Intl.Segmenter.
 * Matches CC-CEDICT definitions and HSK levels.
 */
export function segmentText(
  text: string,
  options: {
    deconstructWords?: boolean; // also include individual single characters of multi-char words
    excludeHskMax?: number; // exclude words with HSK <= excludeHskMax (e.g. exclude 1..3)
  } = {},
): SegmentedWord[] {
  if (!text) return [];

  const results: SegmentedWord[] = [];
  const deconstruct = options.deconstructWords ?? false;
  const excludeMax = options.excludeHskMax ?? 0;

  // Use native Intl.Segmenter if available, otherwise regex fallback
  if (typeof Intl !== 'undefined' && 'Segmenter' in Intl) {
    const segmenter = new Intl.Segmenter('zh', { granularity: 'word' });
    const segments = segmenter.segment(text);

    for (const seg of segments) {
      const rawText = seg.segment.trim();
      if (!rawText) continue;

      const isChinese = isHanzi(rawText);
      const dictEntry = isChinese ? dictionaryService.lookup(rawText) : null;
      const hskLevel = isChinese
        ? dictEntry?.hskLevel || dictionaryService.getHskLevel(rawText)
        : 0;

      // Check HSK exclusion filter
      if (
        isChinese &&
        excludeMax > 0 &&
        hskLevel > 0 &&
        hskLevel <= excludeMax
      ) {
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
            const charHsk =
              charEntry?.hskLevel || dictionaryService.getHskLevel(char);
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
  } else {
    // Basic regex fallback
    const tokens = text.match(/[\u4e00-\u9fa5]+|[^\u4e00-\u9fa5\s]+/g) || [];
    for (const token of tokens) {
      const isChinese = isHanzi(token);
      const dictEntry = isChinese ? dictionaryService.lookup(token) : null;
      const hskLevel = isChinese
        ? dictEntry?.hskLevel || dictionaryService.getHskLevel(token)
        : 0;
      if (
        isChinese &&
        excludeMax > 0 &&
        hskLevel > 0 &&
        hskLevel <= excludeMax
      ) {
        continue;
      }
      results.push({
        text: token,
        isChinese,
        dictEntry,
        hskLevel,
      });
    }
  }

  return results;
}

/**
 * Tokenizes text specifically for the interactive Reader view,
 * keeping punctuation, spacing, and newlines intact for rendering paragraphs.
 */
export function tokenizeForReader(text: string): SegmentedWord[] {
  if (!text) return [];

  const segmenter =
    typeof Intl !== 'undefined' && 'Segmenter' in Intl
      ? new Intl.Segmenter('zh', { granularity: 'word' })
      : null;

  if (segmenter) {
    const segments = segmenter.segment(text);
    const tokens: SegmentedWord[] = [];
    for (const seg of segments) {
      const segText = seg.segment;
      const isChinese = isHanzi(segText);
      const dictEntry = isChinese ? dictionaryService.lookup(segText) : null;
      const hskLevel = isChinese
        ? dictEntry?.hskLevel || dictionaryService.getHskLevel(segText)
        : 0;

      tokens.push({
        text: segText,
        isChinese,
        dictEntry,
        hskLevel,
      });
    }
    return tokens;
  }

  // Fallback
  return text
    .split(/([ \t\r\n]+|[\u4e00-\u9fa5]+|[^\u4e00-\u9fa5\s]+)/g)
    .filter(Boolean)
    .map((part) => {
      const isChinese = isHanzi(part);
      const dictEntry = isChinese ? dictionaryService.lookup(part) : null;
      return {
        text: part,
        isChinese,
        dictEntry,
        hskLevel: isChinese
          ? dictEntry?.hskLevel || dictionaryService.getHskLevel(part)
          : 0,
      };
    });
}
