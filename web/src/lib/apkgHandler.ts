import * as fflate from 'fflate';
import initSqlJs from 'sql.js';
import { cleanHanziCandidate, resolveCardHsk, type TunerCard } from './deckTuner';
import { isHanzi } from './segmentation';

export interface ApkgContext {
  dbBytes: Uint8Array;
  mediaMap: Record<string, string>;
  mediaFiles: Record<string, Uint8Array>;
}

export interface ApkgParseResult {
  cards: TunerCard[];
  context: ApkgContext;
}

// biome-ignore lint/suspicious/noExplicitAny: sql.js instance
let sqlPromise: Promise<any> | null = null;

// biome-ignore lint/suspicious/noExplicitAny: sql.js instance
async function getSqlInstance(): Promise<any> {
  if (!sqlPromise) {
    const baseUrl = import.meta.env.BASE_URL || './';
    sqlPromise = initSqlJs({
      locateFile: (file: string) => `${baseUrl}${file}`,
    });
  }
  return sqlPromise;
}

/**
 * Parses an Anki .apkg zip buffer, extracting SQLite notes and associated media references.
 */
export async function parseApkg(buffer: ArrayBuffer): Promise<ApkgParseResult> {
  const zipData = new Uint8Array(buffer);
  const unzipped = fflate.unzipSync(zipData);

  const dbBytes = unzipped['collection.anki2'];
  if (!dbBytes) {
    throw new Error('Invalid .apkg: missing collection.anki2 database');
  }

  let mediaMap: Record<string, string> = {};
  if (unzipped.media) {
    try {
      mediaMap = JSON.parse(fflate.strFromU8(unzipped.media));
    } catch (e) {
      console.warn('Failed to parse apkg media map:', e);
    }
  }

  const mediaFiles: Record<string, Uint8Array> = {};
  for (const [filename, bytes] of Object.entries(unzipped)) {
    if (filename !== 'collection.anki2' && filename !== 'media') {
      mediaFiles[filename] = bytes;
    }
  }

  const SQL = await getSqlInstance();
  const db = new SQL.Database(dbBytes);

  const res = db.exec('SELECT id, flds FROM notes');
  const cards: TunerCard[] = [];

  if (res.length > 0 && res[0].values) {
    for (const row of res[0].values) {
      const noteId = row[0] as number | string;
      const rawFlds = (row[1] as string) || '';
      const cols = rawFlds.split('\x1f').map((c) => c.trim());

      // Identify Chinese headword
      const hanziCandidates: { text: string; length: number }[] = [];
      for (const col of cols) {
        const cleaned = cleanHanziCandidate(col);
        if (cleaned.length > 0 && isHanzi(cleaned)) {
          hanziCandidates.push({ text: cleaned, length: cleaned.length });
        }
      }

      let chineseWord = '';
      if (hanziCandidates.length > 0) {
        hanziCandidates.sort((a, b) => a.length - b.length);
        chineseWord = hanziCandidates[0].text;
      } else {
        chineseWord = cols.find((c) => /[\u4e00-\u9fa5]/.test(c)) || cols[0];
      }

      const hskLevel = resolveCardHsk(chineseWord);

      cards.push({
        noteId,
        chineseWord,
        hskLevel,
        columns: cols,
        originalLine: cols.join('\t'),
      });
    }
  }

  return {
    cards,
    context: {
      dbBytes,
      mediaMap,
      mediaFiles,
    },
  };
}

/**
 * Exports a tuned .apkg containing only notes matching retainedNoteIds and their audio files.
 */
export async function exportTunedApkg(
  context: ApkgContext,
  retainedNoteIds: Set<number | string>,
): Promise<Uint8Array> {
  const SQL = await getSqlInstance();
  const db = new SQL.Database(context.dbBytes);

  if (retainedNoteIds.size > 0) {
    const idList = Array.from(retainedNoteIds)
      .map((id) => (typeof id === 'number' ? id : `'${id}'`))
      .join(',');

    // 1. Prune excluded notes and cards
    db.run(`DELETE FROM notes WHERE id NOT IN (${idList})`);
    db.run(`DELETE FROM cards WHERE nid NOT IN (${idList})`);

    // 2. Add metadata tag to retained notes
    db.run("UPDATE notes SET tags = tags || ' hanziToAnki'");
  } else {
    db.run('DELETE FROM notes');
    db.run('DELETE FROM cards');
  }

  // 3. Retain only audio files referenced by surviving notes
  const notesRes = db.exec('SELECT flds FROM notes');
  const survivingFlds =
    notesRes.length > 0 && notesRes[0].values
      ? notesRes[0].values.map((r: unknown[]) => r[0] as string).join('\n')
      : '';

  const newMediaMap: Record<string, string> = {};
  const newMediaFiles: Record<string, Uint8Array> = {};

  for (const [key, filename] of Object.entries(context.mediaMap)) {
    if (survivingFlds.includes(filename)) {
      newMediaMap[key] = filename;
      if (context.mediaFiles[key]) {
        newMediaFiles[key] = context.mediaFiles[key];
      }
    }
  }

  const newDbBytes = db.export();

  const outZipFiles: Record<string, Uint8Array> = {
    'collection.anki2': newDbBytes,
    media: fflate.strToU8(JSON.stringify(newMediaMap)),
    ...newMediaFiles,
  };

  return fflate.zipSync(outZipFiles);
}
