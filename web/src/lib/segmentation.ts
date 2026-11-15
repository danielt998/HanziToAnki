import { type DictEntry, dictionaryService } from './dictionary';

export type SegmentationMode = 'jieba' | 'intl';
export const SEGMENTATION_ENGINE_VERSION = 'jieba-wasm';

const MAX_DICT_WORD_LEN = 8;

export interface SegmentedWord {
  text: string;
  isChinese: boolean;
  dictEntry: DictEntry | null;
  hskLevel: number;
}

export interface SegmentOptions {
  deconstructWords?: boolean; // also include individual single characters of multi-char words
  excludeHskMax?: number; // exclude words with HSK <= excludeHskMax (e.g. exclude 1..3)
  mode?: SegmentationMode;
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
 * Statistical unigram log-probability DAG fallback used when WASM Jieba is unavailable.
 * Scores multi-character dictionary/HSK words strictly higher than isolated single characters.
 */
function segmentHanziDag(run: string): string[] {
  const n = run.length;
  if (n <= 1) return n === 1 ? [run] : [];

  const bestScore = new Float64Array(n + 1).fill(Number.NEGATIVE_INFINITY);
  const nextIdx = new Int32Array(n + 1).fill(n);
  bestScore[n] = 0;

  for (let i = n - 1; i >= 0; i--) {
    const maxLen = Math.min(MAX_DICT_WORD_LEN, n - i);
    for (let len = 1; len <= maxLen; len++) {
      const candidate = run.slice(i, i + len);
      const entry = dictionaryService.peek(candidate);
      if (len >= 2 && !entry) continue;

      let wordLogProb: number;
      if (len === 1) {
        wordLogProb = entry ? -5.5 : -7.5;
      } else {
        const hskBonus = entry && entry.hskLevel > 0 ? (7 - entry.hskLevel) * 0.15 : 0;
        wordLogProb = -4.8 + len * 0.5 + hskBonus;
      }

      const candidateScore = wordLogProb + bestScore[i + len];
      if (candidateScore > bestScore[i]) {
        bestScore[i] = candidateScore;
        nextIdx[i] = i + len;
      }
    }
  }

  const tokens: string[] = [];
  let idx = 0;
  while (idx < n) {
    const end = nextIdx[idx];
    tokens.push(run.slice(idx, end));
    idx = end;
  }
  return tokens;
}

/**
 * Segments text using statistical Jieba (WASM DAG + HMM) when ready,
 * falling back to the built-in statistical log-probability DAG segmenter.
 */
export function segmentJieba(text: string): string[] {
  if (!text) return [];

  const wasmTokens = dictionaryService.cutWithJieba(text, true);
  if (wasmTokens !== null) {
    return wasmTokens;
  }

  const chunks = text.match(/[\u4e00-\u9fa5]+|\r\n|\s+|[^\u4e00-\u9fa5\s]+/g) || [];
  const results: string[] = [];
  for (const chunk of chunks) {
    if (isPureHanzi(chunk)) {
      results.push(...segmentHanziDag(chunk));
    } else {
      results.push(chunk);
    }
  }
  return results;
}

function segmentIntl(text: string): string[] {
  if (!text) return [];
  if (typeof Intl !== 'undefined' && 'Segmenter' in Intl) {
    const segmenter = new Intl.Segmenter('zh', { granularity: 'word' });
    return Array.from(segmenter.segment(text), (seg) => seg.segment);
  }
  return text.match(/[\u4e00-\u9fa5]+|\r\n|\s+|[^\u4e00-\u9fa5\s]+/g) || [];
}

function tokenizeRaw(text: string, mode: SegmentationMode): string[] {
  return mode === 'jieba' ? segmentJieba(text) : segmentIntl(text);
}

/**
 * Breaks any Hanzi token lacking a direct dictionary entry into longest-match
 * known dictionary substrings (down to 1-char Hanzi) so Reader/Deck entries remain valid.
 */
export function subsegmentToKnownWords(token: string): string[] {
  if (!token) return [];
  if (!isHanzi(token) || dictionaryService.lookup(token)) {
    return [token];
  }

  const parts: string[] = [];
  let i = 0;
  while (i < token.length) {
    if (!isHanzi(token[i])) {
      let j = i + 1;
      while (j < token.length && !isHanzi(token[j])) {
        j++;
      }
      parts.push(token.slice(i, j));
      i = j;
      continue;
    }

    const maxLen = Math.min(MAX_DICT_WORD_LEN, token.length - i);
    let matchedLen = 1;
    for (let len = maxLen; len >= 2; len--) {
      const candidate = token.slice(i, i + len);
      if (dictionaryService.peek(candidate)) {
        matchedLen = len;
        break;
      }
    }
    parts.push(token.slice(i, i + matchedLen));
    i += matchedLen;
  }

  return parts;
}

function buildSegmentedWord(token: string): SegmentedWord {
  const isChinese = isHanzi(token);
  const dictEntry = isChinese ? dictionaryService.lookup(token) : null;
  const hskLevel = isChinese ? dictEntry?.hskLevel || dictionaryService.getHskLevel(token) : 0;
  return {
    text: token,
    isChinese,
    dictEntry,
    hskLevel,
  };
}

/**
 * Tokenizes text into words using Jieba (default) or native browser Intl.Segmenter.
 * Matches CC-CEDICT definitions and HSK levels.
 */
export function segmentText(text: string, options: SegmentOptions = {}): SegmentedWord[] {
  if (!text) return [];

  const results: SegmentedWord[] = [];
  const deconstruct = options.deconstructWords ?? false;
  const excludeMax = options.excludeHskMax ?? 0;
  const mode: SegmentationMode = options.mode ?? 'jieba';

  const rawTokens = tokenizeRaw(text, mode);
  for (const rawToken of rawTokens) {
    const trimmed = rawToken.trim();
    if (!trimmed) continue;

    const subTokens = isHanzi(trimmed) ? subsegmentToKnownWords(trimmed) : [trimmed];
    for (const token of subTokens) {
      const seg = buildSegmentedWord(token);
      if (seg.isChinese && excludeMax > 0 && seg.hskLevel > 0 && seg.hskLevel <= excludeMax) {
        continue;
      }
      results.push(seg);

      // Optionally break down multi-character words into sub-characters
      if (deconstruct && seg.isChinese && seg.text.length > 1) {
        for (const char of seg.text) {
          if (isPureHanzi(char)) {
            const charSeg = buildSegmentedWord(char);
            if (excludeMax > 0 && charSeg.hskLevel > 0 && charSeg.hskLevel <= excludeMax) {
              continue;
            }
            results.push(charSeg);
          }
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
export function tokenizeForReader(text: string, mode: SegmentationMode = 'jieba'): SegmentedWord[] {
  if (!text) return [];

  const tokens: SegmentedWord[] = [];
  const rawTokens = tokenizeRaw(text, mode);

  for (const rawToken of rawTokens) {
    if (!isHanzi(rawToken)) {
      tokens.push({
        text: rawToken,
        isChinese: false,
        dictEntry: null,
        hskLevel: 0,
      });
      continue;
    }

    for (const subToken of subsegmentToKnownWords(rawToken)) {
      tokens.push(buildSegmentedWord(subToken));
    }
  }

  return tokens;
}
