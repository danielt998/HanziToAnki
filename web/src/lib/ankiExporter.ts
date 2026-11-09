import { DictEntry } from './dictionary';

export type HanziExportType = 'SIMP' | 'TRAD' | 'SIMP_AND_TRAD';

export interface CardItem {
  simplified: string;
  traditional: string;
  pinyin: string;
  definition: string;
  hskLevel?: number;
}

/**
 * Converts a set of dictionary entries to Anki TSV file lines.
 */
export function generateAnkiTsv(
  words: (DictEntry | CardItem)[],
  hanziType: HanziExportType = 'SIMP_AND_TRAD',
): string {
  const lines: string[] = [];

  // Anki headers
  switch (hanziType) {
    case 'SIMP':
      lines.push('Simplified\tPinyin\tDefinition');
      for (const w of words) {
        lines.push(`${w.simplified}\t${w.pinyin}\t${w.definition}`);
      }
      break;
    case 'TRAD':
      lines.push('Traditional\tPinyin\tDefinition');
      for (const w of words) {
        lines.push(`${w.traditional}\t${w.pinyin}\t${w.definition}`);
      }
      break;
    default:
      lines.push('Simplified\tTraditional\tPinyin\tDefinition');
      for (const w of words) {
        lines.push(
          `${w.simplified}\t${w.traditional}\t${w.pinyin}\t${w.definition}`,
        );
      }
      break;
  }

  return lines.join('\n');
}

/**
 * Triggers a browser file download of the given text content.
 */
export function downloadFile(
  content: string,
  filename: string,
  mimeType: string = 'text/tab-separated-values;charset=utf-8',
) {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
