// Tone accent mapping following official Pinyin orthography rules
const VOWELS_TO_TONES: Record<string, string[]> = {
  a: ['a', 'ā', 'á', 'ǎ', 'à'],
  e: ['e', 'ē', 'é', 'ě', 'è'],
  o: ['o', 'ō', 'ó', 'ǒ', 'ò'],
  i: ['i', 'ī', 'í', 'ǐ', 'ì'],
  u: ['u', 'ū', 'ú', 'ǔ', 'ù'],
  v: ['ü', 'ǖ', 'ǘ', 'ǚ', 'ǜ'],
  ü: ['ü', 'ǖ', 'ǘ', 'ǚ', 'ǜ'],
  A: ['A', 'Ā', 'Á', 'Ǎ', 'À'],
  E: ['E', 'Ē', 'É', 'Ě', 'È'],
  O: ['O', 'Ō', 'Ó', 'Ǒ', 'Ò'],
  I: ['I', 'Ī', 'Í', 'Ǐ', 'Ì'],
  U: ['U', 'Ū', 'Ú', 'Ǔ', 'Ù'],
  V: ['Ü', 'Ǖ', 'Ǘ', 'Ǚ', 'Ǜ'],
  Ü: ['Ü', 'Ǖ', 'Ǘ', 'Ǚ', 'Ǜ'],
};

export type TonePalette = 'standard' | 'hanping' | 'rainbow' | 'monochrome';

export interface ToneColorDef {
  tone1: string;
  tone2: string;
  tone3: string;
  tone4: string;
  tone5: string;
}

export const TONE_PALETTES: Record<TonePalette, ToneColorDef> = {
  standard: {
    tone1: '#DC2626', // Red
    tone2: '#16A34A', // Green
    tone3: '#2563EB', // Blue
    tone4: '#9333EA', // Purple
    tone5: '#6B7280', // Grey
  },
  hanping: {
    tone1: '#2563EB', // Blue
    tone2: '#16A34A', // Green
    tone3: '#EA580C', // Orange
    tone4: '#DC2626', // Red
    tone5: '#6B7280', // Grey
  },
  rainbow: {
    tone1: '#EF4444', // Red
    tone2: '#F59E0B', // Amber
    tone3: '#10B981', // Emerald
    tone4: '#3B82F6', // Blue
    tone5: '#9CA3AF', // Grey
  },
  monochrome: {
    tone1: 'currentColor',
    tone2: 'currentColor',
    tone3: 'currentColor',
    tone4: 'currentColor',
    tone5: 'currentColor',
  },
};

/**
 * Converts a numbered pinyin syllable (e.g. "zhong1", "guo2", "lv3", "nu:3")
 * to standard accented pinyin (e.g. "zhōng", "guó", "lǚ", "nǚ").
 */
export function convertSyllableToAccented(syllable: string): string {
  if (!syllable) return '';
  const s = syllable.trim();

  // Match ending digit 1-5
  const match = s.match(/([a-zA-ZüÜ:]+)([1-5])?$/);
  if (!match) return s;

  const letters = match[1]
    .replace(/u:/g, 'ü')
    .replace(/U:/g, 'Ü')
    .replace(/v/g, 'ü')
    .replace(/V/g, 'Ü');
  const toneNum = match[2] ? parseInt(match[2], 10) : 5;

  if (toneNum === 5 || !match[2]) {
    return letters;
  }

  // Pinyin tone placement priority:
  // 1. 'a' or 'e' always gets the tone
  // 2. 'ou' gets the tone on 'o'
  // 3. Otherwise the final vowel gets the tone (covers 'iu' -> 'iú', 'ui' -> 'uì')
  let targetIndex = -1;
  const lower = letters.toLowerCase();

  if (lower.includes('a')) {
    targetIndex = lower.indexOf('a');
  } else if (lower.includes('e')) {
    targetIndex = lower.indexOf('e');
  } else if (lower.includes('ou')) {
    targetIndex = lower.indexOf('o');
  } else {
    // Find the last vowel
    for (let i = letters.length - 1; i >= 0; i--) {
      const char = lower[i];
      if ('aeiouvü'.includes(char)) {
        targetIndex = i;
        break;
      }
    }
  }

  if (targetIndex === -1) {
    return letters;
  }

  const vowelChar = letters[targetIndex];
  const accented = VOWELS_TO_TONES[vowelChar]?.[toneNum] || vowelChar;

  return letters.substring(0, targetIndex) + accented + letters.substring(targetIndex + 1);
}

/**
 * Converts space-separated or bracketed pinyin string:
 * e.g. "zhong1 guo2" -> "zhōng guó"
 * or "Ni3 hao3 [ni3 hao3]" -> "Nǐ hǎo [nǐ hǎo]"
 */
export function formatPinyin(pinyinStr: string): string {
  if (!pinyinStr) return '';
  return pinyinStr
    .split(/\s+/)
    .map((part) => {
      // Handle punctuation or brackets
      return part.replace(/([a-zA-ZüÜ:]+[1-5]?)/g, (match) => convertSyllableToAccented(match));
    })
    .join(' ');
}

/**
 * Returns tone number 1-5 from a pinyin syllable.
 */
export function getToneNumber(syllable: string): 1 | 2 | 3 | 4 | 5 {
  if (!syllable) return 5;
  const match = syllable.match(/[1-5]/);
  if (match) {
    return parseInt(match[0], 10) as 1 | 2 | 3 | 4 | 5;
  }
  // Check accented characters
  const accentedChars = syllable.normalize('NFD');
  if (accentedChars.includes('\u0304')) return 1; // macron
  if (accentedChars.includes('\u0301')) return 2; // acute
  if (accentedChars.includes('\u030C')) return 3; // caron
  if (accentedChars.includes('\u0300')) return 4; // grave
  return 5;
}

/**
 * Returns CSS color string for a given tone number and palette.
 */
export function getToneColor(tone: 1 | 2 | 3 | 4 | 5, palette: TonePalette = 'standard'): string {
  const p = TONE_PALETTES[palette] || TONE_PALETTES.standard;
  switch (tone) {
    case 1:
      return p.tone1;
    case 2:
      return p.tone2;
    case 3:
      return p.tone3;
    case 4:
      return p.tone4;
    default:
      return p.tone5;
  }
}
