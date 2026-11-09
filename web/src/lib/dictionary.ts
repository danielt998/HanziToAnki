import { formatPinyin } from './toneHelper';

export interface DictEntry {
  simplified: string;
  traditional: string;
  pinyin: string;
  pinyinAccented: string;
  definition: string;
  hskLevel: number; // 0 if untiered, 1-6/9
}

export interface DictStatus {
  isReady: boolean;
  isExtendedLoading: boolean;
  isFullLoaded: boolean;
  totalEntries: number;
}

// Compact raw entry structure: [traditional, pinyin, raw_defs, hsk_level]
type RawEntry = [string, string, string, number];

class DictionaryService {
  private dict: Map<string, DictEntry> = new Map();
  private hskMap: Map<string, number> = new Map();
  private isLoaded: boolean = false;
  private isFullLoaded: boolean = false;
  private isExtendedLoading: boolean = false;
  private loadPromise: Promise<void> | null = null;
  private fullLoadPromise: Promise<void> | null = null;
  private statusListeners: Set<(status: DictStatus) => void> = new Set();

  subscribeStatus(listener: (status: DictStatus) => void): () => void {
    this.statusListeners.add(listener);
    listener(this.getStatus());
    return () => {
      this.statusListeners.delete(listener);
    };
  }

  private notifyStatus() {
    const status = this.getStatus();
    for (const listener of this.statusListeners) {
      listener(status);
    }
  }

  getStatus(): DictStatus {
    return {
      isReady: this.isLoaded,
      isExtendedLoading: this.isExtendedLoading,
      isFullLoaded: this.isFullLoaded,
      totalEntries: this.dict.size,
    };
  }

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
          this.notifyStatus();
        }
      } catch (err) {
        console.error('Failed to load initial dictionary:', err);
      }
    })();

    return this.loadPromise;
  }

  /**
   * Lazily downloads the full 182,000-word CC-CEDICT dictionary (~4.2 MB gzipped).
   * Caches permanently in the browser Cache API to protect mobile data.
   */
  async loadExtendedDictionary(): Promise<void> {
    if (this.isFullLoaded) return;
    if (this.fullLoadPromise) return this.fullLoadPromise;

    // Respect user's Data Saver setting if enabled on mobile
    if (
      typeof navigator !== 'undefined' &&
      // biome-ignore lint/suspicious/noExplicitAny: network connection API
      (navigator as any).connection?.saveData
    ) {
      console.info(
        'Data Saver is enabled. Skipping automatic full CC-CEDICT download.',
      );
      return;
    }

    this.isExtendedLoading = true;
    this.notifyStatus();

    this.fullLoadPromise = (async () => {
      try {
        const baseUrl = import.meta.env.BASE_URL || './';
        const url = `${baseUrl}data/dictionary.json`;

        let rawText: string | null = null;
        const CACHE_NAME = 'hanzitoanki-dict-cache-v1';

        if (typeof window !== 'undefined' && 'caches' in window) {
          try {
            const cache = await caches.open(CACHE_NAME);
            const cachedRes = await cache.match(url);
            if (cachedRes) {
              rawText = await cachedRes.text();
            } else {
              const res = await fetch(url);
              if (res.ok) {
                cache.put(url, res.clone());
                rawText = await res.text();
              }
            }
          } catch {
            const res = await fetch(url);
            if (res.ok) rawText = await res.text();
          }
        } else {
          const res = await fetch(url);
          if (res.ok) rawText = await res.text();
        }

        if (rawText) {
          const fullRaw: Record<string, RawEntry> = JSON.parse(rawText);
          this.populateEntries(fullRaw);
          this.isFullLoaded = true;
        }
      } catch (e) {
        console.warn('Failed to load extended dictionary:', e);
      } finally {
        this.isExtendedLoading = false;
        this.notifyStatus();
      }
    })();

    return this.fullLoadPromise;
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

    // Lazily fetch extended CC-CEDICT if missing word is Chinese and full dictionary is not loaded
    if (
      !this.isFullLoaded &&
      !this.isExtendedLoading &&
      /[\u4e00-\u9fa5]/.test(word)
    ) {
      this.loadExtendedDictionary();
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
