import confetti from 'canvas-confetti';
import {
  Check,
  Copy,
  Download,
  Filter,
  RefreshCw,
  Trash2,
  Upload,
} from 'lucide-react';
import React, { useMemo, useState } from 'react';
import { downloadFile } from '../lib/ankiExporter';
import { filterDeck, parseDeck } from '../lib/deckTuner';
import { getHskBadgeClasses } from '../lib/hskHelper';

// Sample master deck for immediate interactive demo
const SAMPLE_DECK_CONTENT = `爱\tai4\tto love; affection\n北京\tbei3 jing1\tBeijing, capital of China\n帮助\tbang1 zhu4\tto help; assistance\n阿姨\ta1 yi2\tmaternal aunt; stepmother\n安全\tan1 quan2\tsafe; secure; safety\n爱惜\tai4 xi1\tto cherish; to treasure\n癌症\tai2 zheng4\tcancer\n图书馆\ttu2 shu1 guan3\tlibrary\n学习\txue2 xi2\tto study; to learn\n快乐\tkuai4 le4\thappy; merry\n朋友\tpeng2 you5\tfriend\n汉语\than4 yu3\tChinese language\n挑战\ttiao3 zhan4\tchallenge\n梦想\tmeng4 xiang3\tdream; ambition\n成功\tcheng2 gong1\tsuccess; to succeed`;

export const FlashcardTuner: React.FC = () => {
  const [deckText, setDeckText] = useState<string>(SAMPLE_DECK_CONTENT);
  const [enabledLevels, setEnabledLevels] = useState<Set<number>>(new Set([4])); // default: tuner set to HSK 4!
  const [copied, setCopied] = useState<boolean>(false);

  // Parse raw deck
  const parsedCards = useMemo(() => {
    return parseDeck(deckText);
  }, [deckText]);

  // Filter deck based on enabled HSK levels
  const tunerResult = useMemo(() => {
    return filterDeck(parsedCards, enabledLevels);
  }, [parsedCards, enabledLevels]);

  const toggleLevel = (lvl: number) => {
    const next = new Set(enabledLevels);
    if (next.has(lvl)) {
      next.delete(lvl);
    } else {
      next.add(lvl);
    }
    setEnabledLevels(next);
  };

  const selectAll = () => {
    setEnabledLevels(new Set([0, 1, 2, 3, 4, 5, 6]));
  };

  const clearAll = () => {
    setEnabledLevels(new Set());
  };

  const handleExport = () => {
    if (tunerResult.filteredCards.length === 0) return;
    const outputLines = tunerResult.filteredCards
      .map((c) => c.originalLine)
      .join('\n');
    downloadFile(outputLines, 'Tuned_Flashcards.tsv');
    confetti({
      particleCount: 80,
      spread: 70,
      origin: { y: 0.6 },
      colors: ['#DC2626', '#F59E0B', '#10B981', '#3B82F6'],
    });
  };

  const handleCopy = () => {
    if (tunerResult.filteredCards.length === 0) return;
    const outputLines = tunerResult.filteredCards
      .map((c) => c.originalLine)
      .join('\n');
    navigator.clipboard.writeText(outputLines);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      if (text) setDeckText(text);
    };
    reader.readAsText(file);
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header Info Card */}
      <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
          <div>
            <h2 className="text-xl font-bold font-display text-chinese-ink dark:text-white flex items-center gap-2">
              <span className="text-amber-500 text-2xl">🎛️</span> Flashcard Tuner
            </h2>
            <p className="text-xs text-gray-700 dark:text-gray-400 mt-0.5 font-medium">
              Upload or paste existing flashcard decks. Toggle HSK levels to
              keep only the cards you want to study. Runs 100% locally in your
              browser.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <label className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark hover:border-chinese-red text-chinese-ink dark:text-white cursor-pointer transition">
              <Upload className="w-3.5 h-3.5" />
              <span>Upload .tsv / .txt</span>
              <input
                type="file"
                accept=".tsv,.txt,.csv"
                onChange={handleFileUpload}
                className="hidden"
              />
            </label>

            <button
              onClick={() => setDeckText(SAMPLE_DECK_CONTENT)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark hover:border-chinese-red text-chinese-ink dark:text-white transition"
              title="Reset to sample deck"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Reset Sample</span>
            </button>
          </div>
        </div>

        {/* Input Textarea */}
        <div className="relative">
          <textarea
            value={deckText}
            onChange={(e) => setDeckText(e.target.value)}
            rows={3}
            placeholder="粘贴现有的Anki卡组TSV数据 / Paste existing deck TSV here..."
            className="w-full p-4 rounded-xl border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark text-chinese-ink dark:text-white font-mono text-xs leading-relaxed focus:ring-2 focus:ring-chinese-red focus:border-transparent outline-none transition"
          />
          {deckText && (
            <button
              onClick={() => setDeckText('')}
              className="absolute top-3 right-3 p-1.5 text-gray-400 hover:text-chinese-red rounded-lg transition"
              title="Clear input"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* HSK Level Selection Controls */}
        <div className="mt-5 pt-4 border-t border-paper-border dark:border-paper-borderDark">
          <div className="flex items-center justify-between mb-3 flex-wrap gap-2">
            <span className="text-xs font-bold uppercase tracking-wider text-chinese-ink dark:text-gray-300 flex items-center gap-1.5">
              <Filter className="w-3.5 h-3.5 text-chinese-red" />
              Select HSK Levels to Keep:
            </span>

            <div className="flex items-center gap-1.5">
              <button
                onClick={selectAll}
                className="text-[11px] font-semibold px-2.5 py-1 rounded-lg bg-paper-border/60 dark:bg-gray-800 text-chinese-ink dark:text-gray-300 hover:bg-chinese-red hover:text-white transition"
              >
                Select All
              </button>
              <button
                onClick={clearAll}
                className="text-[11px] font-semibold px-2.5 py-1 rounded-lg bg-paper-border/60 dark:bg-gray-800 text-chinese-ink dark:text-gray-300 hover:bg-chinese-red hover:text-white transition"
              >
                Clear
              </button>
            </div>
          </div>

          {/* Interactive Checkbox Chips for HSK 1-6 + Untiered */}
          <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-7 gap-2.5">
            {[1, 2, 3, 4, 5, 6, 0].map((lvl) => {
              const isChecked = enabledLevels.has(lvl);
              const count = tunerResult.hskCounts[lvl] || 0;
              const label = lvl === 0 ? 'Other / Untiered' : `HSK ${lvl}`;

              return (
                <label
                  key={lvl}
                  className={`flex flex-col items-center justify-center p-3 rounded-xl border cursor-pointer select-none transition ${
                    isChecked
                      ? 'border-chinese-red bg-chinese-red/5 dark:bg-chinese-red/15 ring-2 ring-chinese-red shadow-sm'
                      : 'border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark opacity-60 hover:opacity-100'
                  }`}
                >
                  <div className="flex items-center gap-1.5 font-bold text-xs text-chinese-ink dark:text-white">
                    <input
                      type="checkbox"
                      checked={isChecked}
                      onChange={() => toggleLevel(lvl)}
                      className="w-4 h-4 accent-chinese-red rounded cursor-pointer"
                    />
                    <span>{label}</span>
                  </div>
                  <span className="text-[10px] text-gray-700 dark:text-gray-400 mt-1 font-medium">
                    {count} {count === 1 ? 'card' : 'cards'}
                  </span>
                </label>
              );
            })}
          </div>
        </div>
      </div>

      {/* Mascot Tuning Summary Banner */}
      <div className="bg-gradient-to-r from-amber-500/10 via-paper-card to-indigo-500/10 dark:from-amber-950/20 dark:via-paper-cardDark dark:to-indigo-950/20 border border-amber-500/20 rounded-2xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-soft">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 shrink-0 rounded-full bg-white dark:bg-gray-800 shadow-sm border border-amber-300/80 flex items-center justify-center text-xl">
            🥟
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-sm font-bold text-chinese-ink dark:text-white">
                Bao says:
              </span>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/25">
                {tunerResult.filteredCards.length} Cards Kept
              </span>
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 border border-gray-200 dark:border-gray-700">
                {tunerResult.excludedCount} Filtered Out
              </span>
            </div>
            <p className="text-xs text-gray-700 dark:text-gray-400 mt-1 font-medium leading-relaxed">
              Target deck is tuned to your exact level. Ready for direct
              re-export into Anki.
            </p>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
          <button
            onClick={handleCopy}
            disabled={tunerResult.filteredCards.length === 0}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold border border-paper-border dark:border-paper-borderDark bg-paper-card dark:bg-paper-cardDark hover:border-chinese-red text-chinese-ink dark:text-white disabled:opacity-40 transition"
          >
            {copied ? (
              <Check className="w-3.5 h-3.5 text-emerald-500" />
            ) : (
              <Copy className="w-3.5 h-3.5" />
            )}
            <span className="hidden sm:inline">
              {copied ? 'Copied!' : 'Copy Tuned TSV'}
            </span>
          </button>

          <button
            onClick={handleExport}
            disabled={tunerResult.filteredCards.length === 0}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold bg-chinese-red hover:bg-chinese-redHover text-white shadow-md shadow-chinese-red/20 disabled:opacity-40 transition"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Export Tuned Deck</span>
          </button>
        </div>
      </div>

      {/* Tuned Results Table */}
      <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl overflow-hidden shadow-soft">
        <div className="p-4 border-b border-paper-border dark:border-paper-borderDark flex items-center justify-between">
          <h3 className="font-bold text-sm text-chinese-ink dark:text-white flex items-center gap-2">
            <span>🎯</span> Kept Flashcards ({tunerResult.filteredCards.length})
          </h3>
          <span className="text-xs text-gray-700 dark:text-gray-400 font-medium">
            Filtered according to active checkboxes
          </span>
        </div>

        {tunerResult.filteredCards.length === 0 ? (
          <div className="p-12 text-center text-gray-700 dark:text-gray-400">
            <p className="text-3xl mb-2">🎋</p>
            <p className="font-bold text-chinese-ink dark:text-white">
              All cards filtered out!
            </p>
            <p className="text-xs mt-1 font-medium">
              Check one or more HSK level boxes above to include vocabulary in
              your tuned deck.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto max-h-[500px]">
            <table className="w-full text-left text-xs">
              <thead className="bg-paper-light dark:bg-paper-dark text-gray-700 dark:text-gray-400 sticky top-0 uppercase tracking-wider font-bold">
                <tr>
                  <th className="py-3 px-4">Headword</th>
                  <th className="py-3 px-4 text-center">HSK Level</th>
                  <th className="py-3 px-4">Deck Columns Preview</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-paper-border dark:divide-paper-borderDark font-medium">
                {tunerResult.filteredCards.map((card, idx) => (
                  <tr
                    key={idx}
                    className="hover:bg-chinese-red/5 dark:hover:bg-chinese-red/10 transition"
                  >
                    <td className="py-3 px-4 font-hanzi text-base font-bold text-chinese-ink dark:text-white">
                      {card.chineseWord}
                    </td>
                    <td className="py-3 px-4 text-center">
                      {card.hskLevel > 0 ? (
                        <span
                          className={`inline-block px-2 py-0.5 rounded text-[11px] font-bold ${getHskBadgeClasses(card.hskLevel, Math.min(...Array.from(enabledLevels).filter((l) => l > 0)) || 1)}`}
                        >
                          HSK {card.hskLevel}
                        </span>
                      ) : (
                        <span className="inline-block px-2 py-0.5 rounded text-[10px] font-medium bg-gray-100 dark:bg-gray-800 text-gray-500">
                          Untiered
                        </span>
                      )}
                    </td>
                    <td
                      className="py-3 px-4 text-gray-600 dark:text-gray-300 font-mono text-xs max-w-lg truncate"
                      title={card.originalLine}
                    >
                      {card.originalLine}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
