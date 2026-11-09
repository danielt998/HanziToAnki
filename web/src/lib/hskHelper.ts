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
