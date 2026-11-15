/**
 * Helper to determine badge styling for HSK levels based on relative difficulty.
 * - Lowest included tier: Green (accessible / baseline)
 * - +1 tier above: Yellow (moderate)
 * - +2 tiers above: Orange (challenging)
 * - +3 or more tiers above: Red (stretch / advanced)
 * - Untiered / Level 0: Neutral gray
 */
export function getHskBadgeClasses(level: number, baseLevel: number = 1): string {
  if (level <= 0) {
    return 'bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 border border-gray-200 dark:border-gray-700';
  }

  const delta = level - baseLevel;
  if (delta <= 0) {
    // Lowest included / Baseline: Emerald Green
    return 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 shadow-sm';
  } else if (delta === 1) {
    // 1 level above: Amber / Warm Yellow
    return 'bg-amber-400/15 text-amber-600 dark:text-amber-400 border border-amber-400/40 shadow-sm';
  } else if (delta === 2) {
    // 2 levels above: Orange
    return 'bg-orange-500/10 text-orange-600 dark:text-orange-400 border border-orange-500/30 shadow-sm';
  } else {
    // 3 or more levels above: Classic Chinese Seal Red
    return 'bg-chinese-red/10 text-chinese-red border border-chinese-red/30 shadow-seal';
  }
}

const READER_HIGHLIGHT_MAP: Record<number, string> = {
  0: 'bg-slate-500/15 dark:bg-slate-400/20 border-b border-slate-500/40',
  1: 'bg-emerald-500/15 dark:bg-emerald-400/20 border-b border-emerald-500/40',
  2: 'bg-sky-500/15 dark:bg-sky-400/20 border-b border-sky-500/40',
  3: 'bg-amber-400/20 dark:bg-amber-400/20 border-b border-amber-500/40',
  4: 'bg-orange-500/15 dark:bg-orange-400/20 border-b border-orange-500/40',
  5: 'bg-rose-500/15 dark:bg-rose-400/20 border-b border-rose-500/40',
  6: 'bg-purple-500/15 dark:bg-purple-400/20 border-b border-purple-500/40',
};

const GRADING_SWATCH_MAP: Record<number, string> = {
  0: 'bg-slate-500 dark:bg-slate-400',
  1: 'bg-emerald-500 dark:bg-emerald-400',
  2: 'bg-sky-500 dark:bg-sky-400',
  3: 'bg-amber-400 dark:bg-amber-400',
  4: 'bg-orange-500 dark:bg-orange-400',
  5: 'bg-rose-500 dark:bg-rose-400',
  6: 'bg-purple-500 dark:bg-purple-400',
};

function normalizeGradingLevel(level: number): number {
  if (level <= 0) return 0;
  return Math.min(level, 6);
}

/**
 * Normalizes raw HSK level into a Clip Reader grading bucket (0 for Untiered, 1-5, or 6 for 6+).
 */
export function getHskGradingTier(level: number): number {
  return normalizeGradingLevel(level);
}

/**
 * Faint, high-legibility background highlight classes for Clip Reader HSK Grading mode.
 */
export function getHskReaderHighlightClasses(level: number): string {
  return READER_HIGHLIGHT_MAP[normalizeGradingLevel(level)] ?? READER_HIGHLIGHT_MAP[0];
}

/**
 * Color swatch classes matching HSK Grading filter checkboxes to reader highlight hues.
 */
export function getHskGradingSwatchClasses(level: number): string {
  return GRADING_SWATCH_MAP[normalizeGradingLevel(level)] ?? GRADING_SWATCH_MAP[0];
}
