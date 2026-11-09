import { formatPinyin } from './toneHelper';

export interface DictEntry {
  simplified: string;
  traditional: string;
  pinyin: string;
  pinyinAccented: string;
  definition: string;
  hskLevel: number; // 0 if untiered, 1-6/9
}

// Compact raw entry structure: [traditional, pinyin, raw_defs, hsk_level]
type RawEntry = [string, string, string, number];

class DictionaryService {
  private dict: Map<string, DictEntry> = new Map();
  private hskMap: Map<string, number> = new Map();
  private isLoaded: boolean = false;
  private isFullLoaded: boolean = false;
  private loadPromise: Promise<void> | null = null;

  async init(): Promise<void> {
    if (this.isLoaded) return;
    if (this.loadPromise) return this.loadPromise;

    this.loadPromise = (async () => {
      try {
        const baseUrl = import.meta.env.BASE_URL || './';
        const [hskRes, starterRes] = await Promise.all([
          fetch(`${baseUrl}data/hsk_map.json`),
          fetch(`${baseUrl}data/starter_dict.json`),
        ]);

        if (hskRes.ok) {
          const rawHsk: Record<string, number> = await hskRes.json();
          for (const [k, v] of Object.entries(rawHsk)) {
            this.hskMap.set(k, v);
          }
        }

        if (starterRes.ok) {
          const rawStarter: Record<string, RawEntry> = await starterRes.json();
          this.populateEntries(rawStarter);
          this.isLoaded = true;
        }

        // 2. Stream full dictionary in the background without blocking UI
        this.loadFullDictionaryInBackground();
      } catch (err) {
        console.error('Failed to load initial dictionary:', err);
      }
    })();

    return this.loadPromise;
  }

  private async loadFullDictionaryInBackground(): Promise<void> {
    try {
      const baseUrl = import.meta.env.BASE_URL || './';
      const res = await fetch(`${baseUrl}data/dictionary.json`);
      if (res.ok) {
        const fullRaw: Record<string, RawEntry> = await res.json();
        this.populateEntries(fullRaw);
        this.isFullLoaded = true;
      }
    } catch (e) {
      console.warn(
        'Background dictionary load failed (starter dict still active):',
        e,
      );
    }
  }

  private populateEntries(data: Record<string, RawEntry>) {
    for (const [key, val] of Object.entries(data)) {
      const [tradOrSimp, rawPinyin, rawDefs, hskLevel] = val;
      const hsk = hskLevel || this.hskMap.get(key) || 0;

      const entry: DictEntry = {
        simplified: key,
        traditional: tradOrSimp || key,
        pinyin: rawPinyin,
        pinyinAccented: formatPinyin(rawPinyin),
        definition: rawDefs.replace(/\//g, '; '),
        hskLevel: hsk,
      };

      this.dict.set(key, entry);
      if (tradOrSimp && !this.dict.has(tradOrSimp)) {
        this.dict.set(tradOrSimp, {
          ...entry,
          simplified: key,
          traditional: tradOrSimp,
        });
      }
    }
  }

  lookup(word: string): DictEntry | null {
    if (!word) return null;
    const direct = this.dict.get(word);
    if (direct) return direct;

    // Er-hua fallback (e.g. 哪儿 -> 哪)
    if (word.endsWith('儿') && word.length > 1) {
      const stripped = word.slice(0, -1);
      const erhuaMatch = this.dict.get(stripped);
      if (erhuaMatch) return erhuaMatch;
    }

    return null;
  }

  getHskLevel(word: string): number {
    return this.hskMap.get(word) || this.dict.get(word)?.hskLevel || 0;
  }

  isReady(): boolean {
    return this.isLoaded;
  }

  hasFullDictionary(): boolean {
    return this.isFullLoaded;
  }
}

export const dictionaryService = new DictionaryService();
