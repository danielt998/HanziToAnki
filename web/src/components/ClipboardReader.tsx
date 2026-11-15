import confetti from 'canvas-confetti';
import { Bookmark, Check, Download, GraduationCap, Plus, X } from 'lucide-react';
import type React from 'react';
import { Fragment, useEffect, useMemo, useRef, useState } from 'react';
import { downloadFile, generateAnkiTsv } from '../lib/ankiExporter';
import { type DictEntry, type DictStatus, dictionaryService } from '../lib/dictionary';
import {
  getHskBadgeClasses,
  getHskGradingSwatchClasses,
  getHskGradingTier,
  getHskReaderHighlightClasses,
} from '../lib/hskHelper';
import { type SegmentedWord, tokenizeForReader } from '../lib/segmentation';
import { getToneColor, getToneNumber, type TonePalette } from '../lib/toneHelper';

interface ClipboardReaderProps {
  tonePalette: TonePalette;
  colorHanziGlyphs: boolean;
}

type ReaderSegmentationMode = 'jieba' | 'intl';

const DEFAULT_ARTICLE = `中国古代有许多寓言故事。其中一个是“拔苗助长”。
古时候，宋国有一个农夫，他总是嫌自己田里的禾苗长得太慢。
有一天，他想出了一个主意，就跑到田里，把禾苗一棵一棵地往上拔高了一点。
回到家里，他十分得意地对妻子说：“今天可把我累坏了！我帮禾苗全都长高了！”
他的儿子听了很奇怪，第二天急忙跑去田里一看，结果所有的禾苗全都枯死了。
这个故事告诉我们：做事情不能急于求成，必须遵循事物的发展规律。`;

const DEFAULT_HIGHLIGHTED_HSK_LEVELS = [1, 2, 3, 4, 5, 6] as const;

const HSK_GRADING_OPTIONS: readonly { level: number; label: string }[] = [
  { level: 1, label: 'HSK 1' },
  { level: 2, label: 'HSK 2' },
  { level: 3, label: 'HSK 3' },
  { level: 4, label: 'HSK 4' },
  { level: 5, label: 'HSK 5' },
  { level: 6, label: 'HSK 6' },
  { level: 0, label: 'Untiered' },
];

function useIsDesktop(): boolean {
  const [isDesktop, setIsDesktop] = useState<boolean>(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return true;
    }
    return window.matchMedia('(min-width: 1024px)').matches;
  });

  useEffect(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return;
    }
    const mediaQuery = window.matchMedia('(min-width: 1024px)');
    setIsDesktop(mediaQuery.matches);

    const handleChange = (event: MediaQueryListEvent) => {
      setIsDesktop(event.matches);
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  return isDesktop;
}

interface WordInspectorCardProps {
  selectedWord: DictEntry | null;
  isInDeck: boolean;
  onClose: () => void;
  onToggleDeck: (word: DictEntry) => void;
}

const WordInspectorCard: React.FC<WordInspectorCardProps> = ({
  selectedWord,
  isInDeck,
  onClose,
  onToggleDeck,
}) => {
  return (
    <>
      <div className="flex items-center justify-between pb-3 border-b border-paper-border dark:border-paper-borderDark mb-4">
        <h3 className="font-bold text-sm text-chinese-ink dark:text-white flex items-center gap-1.5">
          <span aria-hidden="true">🔍</span> Word Definition
        </h3>
        {selectedWord && (
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="p-1 rounded-lg text-gray-500 dark:text-gray-400 hover:text-chinese-ink dark:hover:text-white hover:bg-paper-light dark:hover:bg-paper-dark transition"
          >
            <X className="w-4 h-4" aria-hidden="true" />
          </button>
        )}
      </div>

      {selectedWord ? (
        <div className="space-y-4 animate-fade-in">
          {/* Large Word Display */}
          <div>
            <div className="flex items-baseline gap-3">
              <span className="font-hanzi text-4xl font-bold text-chinese-ink dark:text-white tracking-wider">
                {selectedWord.simplified}
              </span>
              {selectedWord.traditional !== selectedWord.simplified && (
                <span className="font-hanzi text-xl text-gray-700 dark:text-gray-400 font-medium">
                  [{selectedWord.traditional}]
                </span>
              )}
            </div>

            {/* Accented Pinyin */}
            <div className="text-lg font-bold text-chinese-red mt-1">
              {selectedWord.pinyinAccented}
            </div>
          </div>

          {/* HSK Tier Badge */}
          <div className="flex items-center gap-2">
            {selectedWord.hskLevel > 0 ? (
              <span
                className={`px-2.5 py-1 rounded text-xs font-bold ${getHskBadgeClasses(selectedWord.hskLevel, 1)}`}
              >
                HSK Level {selectedWord.hskLevel}
              </span>
            ) : (
              <span className="px-2 py-0.5 rounded text-xs font-medium bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400">
                Untiered / High Frequency
              </span>
            )}
          </div>

          {/* Definitions */}
          <div className="pt-3 border-t border-paper-border dark:border-paper-borderDark">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-700 dark:text-gray-400 block mb-1">
              English Definitions
            </span>
            <p className="text-sm text-chinese-ink dark:text-gray-200 leading-relaxed font-medium">
              {selectedWord.definition}
            </p>
          </div>

          {/* Add to Deck Button */}
          <button
            type="button"
            onClick={() => onToggleDeck(selectedWord)}
            className={`w-full py-2.5 rounded-xl text-xs font-bold flex items-center justify-center gap-2 shadow-sm transition ${
              isInDeck
                ? 'bg-emerald-600 hover:bg-emerald-700 text-white'
                : 'bg-chinese-red hover:bg-chinese-redHover text-white shadow-chinese-red/20'
            }`}
          >
            {isInDeck ? (
              <>
                <Check className="w-4 h-4" aria-hidden="true" />
                <span>Added to Session Deck</span>
              </>
            ) : (
              <>
                <Plus className="w-4 h-4" aria-hidden="true" />
                <span>Add to Session Deck</span>
              </>
            )}
          </button>
        </div>
      ) : (
        <div className="p-8 text-center text-gray-700 dark:text-gray-400">
          <div className="w-12 h-12 mx-auto rounded-full bg-paper-light dark:bg-paper-dark flex items-center justify-center text-2xl mb-2">
            👆
          </div>
          <p className="font-bold text-chinese-ink dark:text-white text-sm">No word selected</p>
          <p className="text-xs mt-1 font-medium">
            Click on any highlighted Chinese word in the reading pane to see its full definition.
          </p>
        </div>
      )}
    </>
  );
};

interface SessionDeckDrawerProps {
  sessionDeck: DictEntry[];
  onExport: () => void;
  onRemove: (word: DictEntry) => void;
}

const SessionDeckDrawer: React.FC<SessionDeckDrawerProps> = ({
  sessionDeck,
  onExport,
  onRemove,
}) => {
  if (sessionDeck.length === 0) return null;

  return (
    <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-4 shadow-soft">
      <div className="flex items-center justify-between pb-2 mb-2 border-b border-paper-border dark:border-paper-borderDark">
        <span className="text-xs font-bold text-chinese-ink dark:text-white flex items-center gap-1.5">
          <Bookmark className="w-3.5 h-3.5 text-chinese-red" aria-hidden="true" />
          Saved This Session ({sessionDeck.length})
        </span>
        <button
          type="button"
          onClick={onExport}
          className="text-[11px] font-bold text-chinese-red hover:underline"
        >
          Export Anki TSV
        </button>
      </div>

      <div className="flex flex-wrap gap-1.5 max-h-36 overflow-y-auto">
        {sessionDeck.map((w, idx) => (
          <span
            key={idx}
            className="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-lg bg-paper-light dark:bg-paper-dark border border-paper-border dark:border-paper-borderDark text-chinese-ink dark:text-white font-hanzi"
          >
            <span>{w.simplified}</span>
            <button
              type="button"
              onClick={() => onRemove(w)}
              aria-label={`Remove ${w.simplified} from session deck`}
              className="text-gray-400 hover:text-chinese-red ml-0.5"
            >
              ×
            </button>
          </span>
        ))}
      </div>
    </div>
  );
};

export const ClipboardReader: React.FC<ClipboardReaderProps> = ({
  tonePalette,
  colorHanziGlyphs,
}) => {
  const [inputText, setInputText] = useState<string>(DEFAULT_ARTICLE);
  const [selectedWord, setSelectedWord] = useState<DictEntry | null>(null);
  const [sessionDeck, setSessionDeck] = useState<DictEntry[]>([]);
  const [segmentationMode, setSegmentationMode] = useState<ReaderSegmentationMode>('jieba');
  const [hskGradingEnabled, setHskGradingEnabled] = useState<boolean>(false);
  const [highlightedHskLevels, setHighlightedHskLevels] = useState<Set<number>>(
    () => new Set(DEFAULT_HIGHLIGHTED_HSK_LEVELS),
  );
  const [dictStatus, setDictStatus] = useState<DictStatus>(() => dictionaryService.getStatus());

  const isDesktop = useIsDesktop();
  const isPopoutOpen = !isDesktop && selectedWord !== null;
  const popoutCardRef = useRef<HTMLDivElement | null>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);
  const wasPopoutOpenRef = useRef<boolean>(false);

  // Re-tokenize when background dictionary finishes loading additional entries
  useEffect(() => {
    return dictionaryService.subscribeStatus(setDictStatus);
  }, []);

  // Manage focus entry/return when mobile popout dialog opens or closes
  useEffect(() => {
    if (isPopoutOpen && !wasPopoutOpenRef.current) {
      previousFocusRef.current =
        document.activeElement instanceof HTMLElement ? document.activeElement : null;
      popoutCardRef.current?.focus();
    } else if (!isPopoutOpen && wasPopoutOpenRef.current) {
      const prev = previousFocusRef.current;
      previousFocusRef.current = null;
      if (prev?.isConnected) {
        prev.focus();
      }
    }
    wasPopoutOpenRef.current = isPopoutOpen;
  }, [isPopoutOpen]);

  // Dismiss mobile popout on Escape key
  useEffect(() => {
    if (!isPopoutOpen) return;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setSelectedWord(null);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isPopoutOpen]);

  // Tokenize text for reading (compatible both before and after PR 2 merges)
  const tokens = useMemo(() => {
    void dictStatus.totalEntries;
    const tokenizeReader = tokenizeForReader as (
      text: string,
      mode?: ReaderSegmentationMode,
    ) => SegmentedWord[];
    return tokenizeReader(inputText, segmentationMode);
  }, [inputText, segmentationMode, dictStatus.totalEntries]);

  const isInSessionDeck = (word: DictEntry | null) => {
    if (!word) return false;
    return sessionDeck.some((w) => w.simplified === word.simplified);
  };

  const toggleAddToSessionDeck = (word: DictEntry | null) => {
    if (!word) return;
    setSessionDeck((prev) =>
      prev.some((w) => w.simplified === word.simplified)
        ? prev.filter((w) => w.simplified !== word.simplified)
        : [...prev, word],
    );
  };

  const toggleHskLevel = (level: number) => {
    setHighlightedHskLevels((prev) => {
      const next = new Set(prev);
      if (next.has(level)) {
        next.delete(level);
      } else {
        next.add(level);
      }
      return next;
    });
  };

  const selectAllHskLevels = () => {
    setHighlightedHskLevels(new Set(HSK_GRADING_OPTIONS.map((option) => option.level)));
  };

  const clearAllHskLevels = () => {
    setHighlightedHskLevels(new Set());
  };

  const handleExportSession = () => {
    if (sessionDeck.length === 0) return;
    const tsv = generateAnkiTsv(sessionDeck, 'SIMP_AND_TRAD');
    downloadFile(tsv, 'reading_session_hanziToAnki.tsv');
    confetti({
      particleCount: 70,
      spread: 60,
      origin: { y: 0.6 },
    });
  };

  const handleDialogKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {
    if (event.key !== 'Tab' || !popoutCardRef.current) return;
    const focusables = Array.from(
      popoutCardRef.current.querySelectorAll<HTMLElement>(
        'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      ),
    );
    if (focusables.length === 0) {
      event.preventDefault();
      return;
    }
    const first = focusables[0];
    const last = focusables[focusables.length - 1];
    const active = document.activeElement;

    if (event.shiftKey) {
      if (active === first || active === popoutCardRef.current) {
        event.preventDefault();
        last.focus();
      }
    } else if (active === last) {
      event.preventDefault();
      first.focus();
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Header Card */}
      <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-3">
          <div>
            <h2 className="text-xl font-bold font-display text-chinese-ink dark:text-white flex items-center gap-2">
              <span className="text-chinese-jade text-2xl">📖</span> Interactive Clip Reader
            </h2>
            <p className="text-xs text-gray-700 dark:text-gray-400 mt-0.5 font-medium">
              Click or tap any Chinese word to view instant definitions, pinyin with tone coloring,
              and HSK level. Save unfamiliar words directly to your session deck.
            </p>
          </div>

          <div className="flex items-center gap-2 shrink-0 flex-wrap">
            <span className="text-xs font-bold text-gray-700 dark:text-gray-400 whitespace-nowrap">
              Session Deck:
            </span>
            <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-chinese-red text-white whitespace-nowrap shrink-0">
              {sessionDeck.length} saved
            </span>
            {sessionDeck.length > 0 && (
              <button
                type="button"
                onClick={handleExportSession}
                className="flex items-center gap-1.5 px-3 py-1 rounded-xl text-xs font-bold bg-chinese-jade text-white hover:bg-emerald-700 shadow-sm transition whitespace-nowrap shrink-0"
              >
                <Download className="w-3.5 h-3.5" aria-hidden="true" />
                <span>Export</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Main Reader View Grid: Text on Left, Live Inspector on Right (Desktop) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Reading Article Pane */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-4 sm:p-6 shadow-soft min-h-[420px]">
            <div className="flex flex-wrap items-center justify-between gap-2.5 pb-3 mb-4 border-b border-paper-border dark:border-paper-borderDark">
              <span className="text-xs font-bold uppercase tracking-wider text-chinese-ink dark:text-gray-300">
                Reading Pane (Click words for definitions)
              </span>

              <div className="flex flex-wrap items-center gap-2">
                {/* Segmentation Engine Switcher */}
                <fieldset
                  aria-label="Segmentation Engine"
                  className="inline-flex items-center rounded-xl border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark p-0.5"
                >
                  <button
                    type="button"
                    onClick={() => setSegmentationMode('jieba')}
                    aria-pressed={segmentationMode === 'jieba'}
                    title="Statistical Jieba word segmentation"
                    className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition ${
                      segmentationMode === 'jieba'
                        ? 'bg-chinese-red text-white shadow-sm'
                        : 'text-gray-700 dark:text-gray-300 hover:text-chinese-red dark:hover:text-white'
                    }`}
                  >
                    Jieba
                  </button>
                  <button
                    type="button"
                    onClick={() => setSegmentationMode('intl')}
                    aria-pressed={segmentationMode === 'intl'}
                    title="Native browser Intl.Segmenter"
                    className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition ${
                      segmentationMode === 'intl'
                        ? 'bg-chinese-red text-white shadow-sm'
                        : 'text-gray-700 dark:text-gray-300 hover:text-chinese-red dark:hover:text-white'
                    }`}
                  >
                    Browser Intl
                  </button>
                </fieldset>

                {/* HSK Grading Mode Toggle */}
                <button
                  type="button"
                  onClick={() => setHskGradingEnabled((prev) => !prev)}
                  aria-pressed={hskGradingEnabled}
                  className={`inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl text-xs font-semibold border transition ${
                    hskGradingEnabled
                      ? 'bg-chinese-red/10 text-chinese-red border-chinese-red/40 ring-1 ring-chinese-red/30 dark:bg-chinese-red/20 dark:text-rose-300'
                      : 'border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark text-gray-700 dark:text-gray-300 hover:border-chinese-red hover:text-chinese-red'
                  }`}
                >
                  <GraduationCap className="w-3.5 h-3.5" aria-hidden="true" />
                  <span>HSK Grading</span>
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setInputText('');
                    setSelectedWord(null);
                  }}
                  className="px-2 py-1.5 text-xs text-gray-700 dark:text-gray-400 hover:text-chinese-red transition font-medium"
                >
                  Clear Text
                </button>
              </div>
            </div>

            {/* HSK Grading Level Filter Bar */}
            {hskGradingEnabled && (
              <div className="mb-4 p-3 rounded-xl bg-paper-light dark:bg-paper-dark border border-paper-border dark:border-paper-borderDark space-y-2 animate-fade-in">
                <div className="flex items-center justify-between gap-2 flex-wrap">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-gray-700 dark:text-gray-300">
                    Highlight HSK Tiers
                  </span>
                  <div className="flex items-center gap-1.5">
                    <button
                      type="button"
                      onClick={selectAllHskLevels}
                      className="text-[11px] font-semibold px-2 py-0.5 rounded-md bg-paper-border/60 dark:bg-gray-800 text-chinese-ink dark:text-gray-300 hover:bg-chinese-red hover:text-white transition"
                    >
                      All
                    </button>
                    <button
                      type="button"
                      onClick={clearAllHskLevels}
                      className="text-[11px] font-semibold px-2 py-0.5 rounded-md bg-paper-border/60 dark:bg-gray-800 text-chinese-ink dark:text-gray-300 hover:bg-chinese-red hover:text-white transition"
                    >
                      None
                    </button>
                  </div>
                </div>

                <div className="flex flex-wrap gap-1.5">
                  {HSK_GRADING_OPTIONS.map((option) => {
                    const checked = highlightedHskLevels.has(option.level);
                    const inputId = `reader-hsk-level-${option.level}`;
                    return (
                      <label
                        key={option.level}
                        htmlFor={inputId}
                        className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg border text-xs font-semibold cursor-pointer select-none transition ${
                          checked
                            ? 'border-chinese-ink/25 dark:border-white/25 bg-paper-card dark:bg-paper-cardDark text-chinese-ink dark:text-white shadow-sm'
                            : 'border-paper-border dark:border-paper-borderDark bg-transparent text-gray-600 dark:text-gray-400 opacity-75 hover:opacity-100'
                        }`}
                      >
                        <input
                          id={inputId}
                          type="checkbox"
                          checked={checked}
                          onChange={() => toggleHskLevel(option.level)}
                          className="w-3.5 h-3.5 accent-chinese-red rounded cursor-pointer"
                        />
                        <span
                          aria-hidden="true"
                          className={`w-2.5 h-2.5 rounded-full shrink-0 ${getHskGradingSwatchClasses(option.level)}`}
                        />
                        <span>{option.label}</span>
                      </label>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Interactive Tokenized Text */}
            {tokens.length === 0 ? (
              <div className="py-16 text-center text-gray-500">
                <p className="text-3xl mb-2">📜</p>
                <p className="font-bold text-chinese-ink dark:text-white">Reading Pane is Empty</p>
                <p className="text-xs mt-1">
                  Paste your own Chinese article below, or restore the sample story.
                </p>
                <button
                  type="button"
                  onClick={() => setInputText(DEFAULT_ARTICLE)}
                  className="mt-3 px-3 py-1.5 rounded-xl text-xs font-semibold bg-chinese-red text-white hover:bg-chinese-redHover transition"
                >
                  Load Sample Story
                </button>
              </div>
            ) : (
              <div className="font-hanzi text-lg sm:text-xl leading-[2.1] sm:leading-loose whitespace-pre-wrap text-chinese-ink dark:text-gray-100 selection:bg-chinese-red/20">
                {tokens.map((token, i) => {
                  if (!token.isChinese || !token.dictEntry) {
                    if (!token.isChinese && !/\S/.test(token.text)) {
                      return <Fragment key={i}>{token.text}</Fragment>;
                    }
                    return (
                      <span
                        key={i}
                        className={token.isChinese ? '' : 'text-gray-600 dark:text-gray-400'}
                      >
                        {token.text}
                      </span>
                    );
                  }

                  const entry = token.dictEntry;
                  const isSelected = selectedWord?.simplified === entry.simplified;
                  const inDeck = isInSessionDeck(entry);
                  const gradingLevel = getHskGradingTier(token.hskLevel);
                  const isHskHighlighted =
                    hskGradingEnabled && highlightedHskLevels.has(gradingLevel);

                  // Compute tone color if enabled
                  const toneNum = getToneNumber(entry.pinyin);
                  const toneStyle =
                    colorHanziGlyphs && !isSelected
                      ? { color: getToneColor(toneNum, tonePalette) }
                      : {};

                  const tokenStateClass = isSelected
                    ? 'bg-chinese-red text-white shadow-sm'
                    : inDeck
                      ? 'bg-emerald-500/15 border-b-2 border-emerald-500 hover:bg-emerald-500/25'
                      : isHskHighlighted
                        ? `${getHskReaderHighlightClasses(token.hskLevel)} hover:brightness-95 dark:hover:brightness-110`
                        : 'hover:bg-amber-400/20 hover:text-chinese-red';

                  const tokenPaddingClass =
                    isSelected || inDeck || isHskHighlighted ? 'px-0.5' : 'px-[0.5px]';

                  return (
                    <span
                      key={i}
                      onClick={() => setSelectedWord(entry)}
                      style={toneStyle}
                      className={`inline ${tokenPaddingClass} py-0.5 rounded cursor-pointer transition-colors select-none ${tokenStateClass}`}
                    >
                      {token.text}
                    </span>
                  );
                })}
              </div>
            )}
          </div>

          {/* Collapsible Source Editor */}
          <details className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-xl p-3 text-xs">
            <summary className="font-semibold text-chinese-ink dark:text-gray-300 cursor-pointer select-none">
              ✏️ Edit or Paste Custom Article Text
            </summary>
            <textarea
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              rows={4}
              className="w-full mt-3 p-3 rounded-lg border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark text-chinese-ink dark:text-white font-hanzi text-sm outline-none focus:ring-1 focus:ring-chinese-red"
              placeholder="Paste custom Chinese text here..."
            />
          </details>
        </div>

        {/* Live Word Inspector (Desktop Only) & Session Deck Drawer */}
        {(isDesktop || sessionDeck.length > 0) && (
          <div className="space-y-4">
            {isDesktop && (
              <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft sticky top-24">
                <WordInspectorCard
                  selectedWord={selectedWord}
                  isInDeck={isInSessionDeck(selectedWord)}
                  onClose={() => setSelectedWord(null)}
                  onToggleDeck={toggleAddToSessionDeck}
                />
              </div>
            )}

            <SessionDeckDrawer
              sessionDeck={sessionDeck}
              onExport={handleExportSession}
              onRemove={toggleAddToSessionDeck}
            />
          </div>
        )}
      </div>

      {/* Mobile Word Definition Popout Dialog */}
      {isPopoutOpen && (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Word Definition"
          className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/45 backdrop-blur-xs"
          onClick={() => setSelectedWord(null)}
        >
          <div
            ref={popoutCardRef}
            tabIndex={-1}
            onClick={(e) => e.stopPropagation()}
            onKeyDown={handleDialogKeyDown}
            className="w-full max-w-md max-h-[75dvh] overflow-y-auto bg-paper-card dark:bg-paper-cardDark border-t sm:border border-paper-border dark:border-paper-borderDark rounded-t-2xl sm:rounded-2xl p-5 pb-[calc(1.25rem+env(safe-area-inset-bottom))] sm:pb-5 shadow-2xl outline-none"
          >
            <div className="sm:hidden w-10 h-1 rounded-full bg-gray-300 dark:bg-gray-600 mx-auto mb-3" />
            <WordInspectorCard
              selectedWord={selectedWord}
              isInDeck={isInSessionDeck(selectedWord)}
              onClose={() => setSelectedWord(null)}
              onToggleDeck={toggleAddToSessionDeck}
            />
          </div>
        </div>
      )}
    </div>
  );
};
