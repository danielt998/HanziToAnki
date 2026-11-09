import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import * as fflate from 'fflate';
import initSqlJs from 'sql.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export async function generateTestApkg() {
  const SQL = await initSqlJs();
  const db = new SQL.Database();

  // Create standard Anki database schema
  db.run(`
    CREATE TABLE col (
      id integer primary key, crt integer, mod integer, scm integer, ver integer,
      dty integer, usn integer, ls integer, conf text, models text, decks text, dconf text, tags text
    );
    CREATE TABLE notes (
      id integer primary key, guid text, mid integer, mod integer, usn integer,
      tags text, flds text, sfld integer, csum integer, flags integer, data text
    );
    CREATE TABLE cards (
      id integer primary key, nid integer, did integer, ord integer, mod integer,
      usn integer, type integer, queue integer, due integer, ivl integer, factor integer,
      reps integer, lapses integer, left integer, odue integer, odid integer, flags integer, data text
    );
  `);

  const now = 1619146800;
  const nowMs = 1619146800000;

  db.run(
    'INSERT INTO col VALUES (1, ?, ?, ?, 11, 0, 0, 0, "{}", "{}", "{}", "{}", "{}")',
    [now, nowMs, nowMs],
  );

  const note1Id = 1001; // HSK 1: 朋友 (Friend)
  const note2Id = 1002; // HSK 2: 帮助 (Help)

  db.run(
    'INSERT INTO notes VALUES (?, "guid1", 1, ?, -1, "", ?, 0, 0, 0, "")',
    [note1Id, now, 'Friend\x1f朋友\x1fpéng you\x1f[sound:pengyou.mp3]'],
  );
  db.run(
    'INSERT INTO notes VALUES (?, "guid2", 1, ?, -1, "", ?, 0, 0, 0, "")',
    [note2Id, now, 'Help\x1f帮助\x1fbāng zhù\x1f[sound:bangzhu.mp3]'],
  );

  db.run(
    'INSERT INTO cards VALUES (1, ?, 1, 0, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "")',
    [note1Id, now],
  );
  db.run(
    'INSERT INTO cards VALUES (2, ?, 1, 0, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "")',
    [note2Id, now],
  );

  const dbBytes = db.export();

  // Lightweight 24-byte silent MP3 payload
  const silentMp3 = new Uint8Array([
    0xff, 0xfb, 0x90, 0x64, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
  ]);

  const mediaJson = JSON.stringify({
    0: 'pengyou.mp3',
    1: 'bangzhu.mp3',
  });

  const zipFiles = {
    'collection.anki2': dbBytes,
    media: fflate.strToU8(mediaJson),
    0: silentMp3,
    1: silentMp3,
  };

  const zipped = fflate.zipSync(zipFiles);
  const outPath = path.join(__dirname, 'minimal_hsk1_hsk2.apkg');
  fs.writeFileSync(outPath, Buffer.from(zipped));
  console.log(`Generated fixture: ${outPath} (${zipped.length} bytes)`);
  return outPath;
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  generateTestApkg();
}
