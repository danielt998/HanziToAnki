import confetti from 'canvas-confetti';
import { Bookmark, Check, Download, Plus, X } from 'lucide-react';
import React, { useMemo, useState } from 'react';
import { downloadFile, generateAnkiTsv } from '../lib/ankiExporter';
import { DictEntry } from '../lib/dictionary';
import { getHskBadgeClasses } from '../lib/hskHelper';
import { tokenizeForReader } from '../lib/segmentation';
import { getToneColor, getToneNumber, TonePalette } from '../lib/toneHelper';

interface ClipboardReaderProps {
  tonePalette: TonePalette;
  colorHanziGlyphs: boolean;
}

const DEFAULT_ARTICLE = `中国古代有许多寓言故事。其中一个是“拔苗助长”。
古时候，宋国有一个农夫，他总是嫌自己田里的禾苗长得太慢。
有一天，他想出了一个主意，就跑到田里，把禾苗一棵一棵地往上拔高了一点。
回到家里，他十分得意地对妻子说：“今天可把我累坏了！我帮禾苗全都长高了！”
他的儿子听了很奇怪，第二天急忙跑去田里一看，结果所有的禾苗全都枯死了。
这个故事告诉我们：做事情不能急于求成，必须遵循事物的发展规律。`;

export const ClipboardReader: React.FC<ClipboardReaderProps> = ({
  tonePalette,
  colorHanziGlyphs,
}) => {
  const [inputText, setInputText] = useState<string>(DEFAULT_ARTICLE);
  const [selectedWord, setSelectedWord] = useState<DictEntry | null>(null);
  const [sessionDeck, setSessionDeck] = useState<DictEntry[]>([]);
  const [_copied, _setCopied] = useState<boolean>(false);

  // Tokenize text for reading
  const tokens = useMemo(() => {
    return tokenizeForReader(inputText);
  }, [inputText]);

  // Check if a word is in session deck
  const isInSessionDeck = (word: DictEntry | null) => {
    if (!word) return false;
    return sessionDeck.some((w) => w.simplified === word.simplified);
  };

  const toggleAddToSessionDeck = (word: DictEntry | null) => {
    if (!word) return;
    if (isInSessionDeck(word)) {
      setSessionDeck(
        sessionDeck.filter((w) => w.simplified !== word.simplified),
      );
    } else {
      setSessionDeck([...sessionDeck, word]);
    }
  };

  const handleExportSession = () => {
    if (sessionDeck.length === 0) return;
    const tsv = generateAnkiTsv(sessionDeck, 'SIMP_AND_TRAD');
    downloadFile(tsv, 'Reading_Session_Deck.tsv');
    confetti({
      particleCount: 70,
      spread: 60,
      origin: { y: 0.6 },
    });
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Header Card */}
      <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-3">
          <div>
            <h2 className="text-xl font-bold font-display text-chinese-ink dark:text-white flex items-center gap-2">
              <span className="text-chinese-jade text-2xl">📖</span> Interactive
              Clip Reader
            </h2>
            <p className="text-xs text-gray-700 dark:text-gray-400 mt-0.5 font-medium">
              Click or tap any Chinese word to view instant definitions, pinyin
              with tone coloring, and HSK level. Save unfamiliar words directly
              to your session deck.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-gray-700 dark:text-gray-400 font-medium">
              Session Deck:
            </span>
            <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-chinese-red text-white">
              {sessionDeck.length} saved
            </span>
            {sessionDeck.length > 0 && (
              <button
                onClick={handleExportSession}
                className="flex items-center gap-1.5 px-3 py-1 rounded-xl text-xs font-bold bg-chinese-jade text-white hover:bg-emerald-700 shadow-sm transition"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Export</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Main Reader View Grid: Text on Left, Live Inspector on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Reading Article Pane */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft min-h-[420px]">
            <div className="flex items-center justify-between pb-3 mb-4 border-b border-paper-border dark:border-paper-borderDark">
              <span className="text-xs font-bold uppercase tracking-wider text-chinese-ink dark:text-gray-300">
                Reading Pane (Click words for definitions)
              </span>
              <button
                onClick={() => setInputText('')}
                className="text-xs text-gray-700 dark:text-gray-400 hover:text-chinese-red transition font-medium"
              >
                Clear Text
              </button>
            </div>

            {/* Interactive Tokenized Text */}
            {tokens.length === 0 ? (
              <div className="py-16 text-center text-gray-500">
                <p className="text-3xl mb-2">📜</p>
                <p className="font-bold text-chinese-ink dark:text-white">
                  Reading Pane is Empty
                </p>
                <p className="text-xs mt-1">
                  Paste your own Chinese article below, or restore the sample
                  story.
                </p>
                <button
                  onClick={() => setInputText(DEFAULT_ARTICLE)}
                  className="mt-3 px-3 py-1.5 rounded-xl text-xs font-semibold bg-chinese-red text-white hover:bg-chinese-redHover transition"
                >
                  Load Sample Story
                </button>
              </div>
            ) : (
              <div className="font-hanzi text-lg leading-loose tracking-wide text-chinese-ink dark:text-gray-100 selection:bg-chinese-red/20">
                {tokens.map((token, i) => {
                  if (!token.isChinese || !token.dictEntry) {
                    return (
                      <span
                        key={i}
                        className="text-gray-600 dark:text-gray-400"
                      >
                        {token.text}
                      </span>
                    );
                  }

                  const isSelected =
                    selectedWord?.simplified === token.dictEntry.simplified;
                  const inDeck = isInSessionDeck(token.dictEntry);

                  // Compute tone color if enabled
                  const toneNum = getToneNumber(token.dictEntry.pinyin);
                  const toneStyle = colorHanziGlyphs
                    ? { color: getToneColor(toneNum, tonePalette) }
                    : {};

                  return (
                    <span
                      key={i}
                      onClick={() => setSelectedWord(token.dictEntry)}
                      style={toneStyle}
                      className={`inline-block px-1 py-0.5 mx-0.5 rounded-lg cursor-pointer transition select-none ${
                        isSelected
                          ? 'bg-chinese-red text-white shadow-sm ring-2 ring-chinese-red'
                          : inDeck
                            ? 'bg-emerald-500/15 border-b-2 border-emerald-500 hover:bg-emerald-500/25'
                            : 'hover:bg-amber-400/20 hover:text-chinese-red'
                      }`}
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

        {/* Live Word Inspector & Tooltip Drawer (Right Column) */}
        <div className="space-y-4">
          <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft sticky top-24">
            <div className="flex items-center justify-between pb-3 border-b border-paper-border dark:border-paper-borderDark mb-4">
              <h3 className="font-bold text-sm text-chinese-ink dark:text-white flex items-center gap-1.5">
                <span>🔍</span> Word Definition
              </h3>
              {selectedWord && (
                <button
                  onClick={() => setSelectedWord(null)}
                  className="text-gray-400 hover:text-chinese-ink dark:hover:text-white"
                >
                  <X className="w-4 h-4" />
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
                    <span className="px-2 py-0.5 rounded text-xs font-medium bg-gray-100 dark:bg-gray-800 text-gray-500">
                      Untiered / High Frequency
                    </span>
                  )}
                </div>

                {/* Definitions */}
                <div className="pt-3 border-t border-paper-border dark:border-paper-borderDark">
                  <label className="text-[11px] font-bold uppercase tracking-wider text-gray-700 dark:text-gray-400 block mb-1 font-medium">
                    English Definitions
                  </label>
                  <p className="text-sm text-chinese-ink dark:text-gray-200 leading-relaxed font-medium">
                    {selectedWord.definition}
                  </p>
                </div>

                {/* Add to Deck Button */}
                <button
                  onClick={() => toggleAddToSessionDeck(selectedWord)}
                  className={`w-full py-2.5 rounded-xl text-xs font-bold flex items-center justify-center gap-2 shadow-sm transition ${
                    isInSessionDeck(selectedWord)
                      ? 'bg-emerald-600 hover:bg-emerald-700 text-white'
                      : 'bg-chinese-red hover:bg-chinese-redHover text-white shadow-chinese-red/20'
                  }`}
                >
                  {isInSessionDeck(selectedWord) ? (
                    <>
                      <Check className="w-4 h-4" />
                      <span>Added to Session Deck</span>
                    </>
                  ) : (
                    <>
                      <Plus className="w-4 h-4" />
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
                <p className="font-bold text-chinese-ink dark:text-white text-sm">
                  No word selected
                </p>
                <p className="text-xs mt-1 font-medium">
                  Click on any highlighted Chinese word in the reading pane to
                  see its full definition.
                </p>
              </div>
            )}
          </div>

          {/* Session Deck Quick Drawer */}
          {sessionDeck.length > 0 && (
            <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-4 shadow-soft">
              <div className="flex items-center justify-between pb-2 mb-2 border-b border-paper-border dark:border-paper-borderDark">
                <span className="text-xs font-bold text-chinese-ink dark:text-white flex items-center gap-1.5">
                  <Bookmark className="w-3.5 h-3.5 text-chinese-red" />
                  Saved This Session ({sessionDeck.length})
                </span>
                <button
                  onClick={handleExportSession}
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
                      onClick={() => toggleAddToSessionDeck(w)}
                      className="text-gray-400 hover:text-chinese-red ml-0.5"
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
