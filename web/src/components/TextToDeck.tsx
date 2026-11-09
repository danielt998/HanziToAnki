import confetti from 'canvas-confetti';
import { Check, Copy, Download, Trash2 } from 'lucide-react';
import React, { useMemo, useState } from 'react';
import {
  downloadFile,
  generateAnkiTsv,
  HanziExportType,
} from '../lib/ankiExporter';
import { getHskBadgeClasses } from '../lib/hskHelper';
import { segmentText } from '../lib/segmentation';
import { TonePalette } from '../lib/toneHelper';

interface TextToDeckProps {
  tonePalette: TonePalette;
  colorHanziGlyphs: boolean;
}

const SAMPLE_TEXTS = {
  dialogue:
    '你好！今天天气真好。我们一起去图书馆看书，好吗？我非常喜欢学习中文。',
  story:
    '从前有一只聪明的小兔子，它住在美丽的大森林里。有一天，兔子遇到了一只饥饿的老虎。老虎想吃兔子，但是兔子想出了一个好办法。',
  idioms:
    '成语是中国文化的宝藏。例如：一心一意、画蛇添足、望梅止渴、守株待兔。学习成语能让汉语更加生动有趣。',
};

export const TextToDeck: React.FC<TextToDeckProps> = ({
  tonePalette,
  colorHanziGlyphs,
}) => {
  const [inputText, setInputText] = useState(SAMPLE_TEXTS.dialogue);
  const [hanziType, setHanziType] = useState<HanziExportType>('SIMP_AND_TRAD');
  const [excludeHskMax, setExcludeHskMax] = useState<number>(1);
  const [deconstructWords, setDeconstructWords] = useState<boolean>(false);
  const [copied, setCopied] = useState<boolean>(false);

  // Segment text and extract vocabulary
  const extractedWords = useMemo(() => {
    if (!inputText.trim()) return [];
    const segments = segmentText(inputText, {
      deconstructWords,
      excludeHskMax,
    });

    // Deduplicate by simplified word
    const seen = new Set<string>();
    const unique = [];
    for (const seg of segments) {
      if (
        seg.isChinese &&
        seg.dictEntry &&
        !seen.has(seg.dictEntry.simplified)
      ) {
        seen.add(seg.dictEntry.simplified);
        unique.push(seg.dictEntry);
      }
    }
    return unique;
  }, [inputText, deconstructWords, excludeHskMax]);

  const handleExport = () => {
    if (extractedWords.length === 0) return;
    const tsv = generateAnkiTsv(extractedWords, hanziType);
    downloadFile(tsv, 'HanziToAnki_Deck.tsv');
    confetti({
      particleCount: 80,
      spread: 70,
      origin: { y: 0.6 },
      colors: ['#DC2626', '#F59E0B', '#10B981', '#3B82F6'],
    });
  };

  const handleCopy = () => {
    if (extractedWords.length === 0) return;
    const tsv = generateAnkiTsv(extractedWords, hanziType);
    navigator.clipboard.writeText(tsv);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Top Hero Card */}
      <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
          <div>
            <h2 className="text-xl font-bold font-display text-chinese-ink dark:text-white flex items-center gap-2">
              <span className="text-chinese-red text-2xl">🏮</span> Text to Anki
              Deck
            </h2>
            <p className="text-xs text-gray-700 dark:text-gray-400 mt-0.5 font-medium">
              Paste Chinese articles, dialogues, or sentences. Uses native{' '}
              <code className="bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-chinese-red">
                Intl.Segmenter
              </code>{' '}
              NLP to extract vocabulary.
            </p>
          </div>

          {/* Sample buttons */}
          <div className="flex items-center gap-2 flex-wrap">
            <span className="text-xs text-gray-700 dark:text-gray-400 font-medium">
              Try Sample:
            </span>
            <button
              onClick={() => setInputText(SAMPLE_TEXTS.dialogue)}
              className="text-xs px-2.5 py-1 rounded-lg bg-paper-border/60 dark:bg-gray-800 hover:bg-chinese-red hover:text-white transition font-medium text-chinese-ink dark:text-gray-200"
            >
              Dialogue
            </button>
            <button
              onClick={() => setInputText(SAMPLE_TEXTS.story)}
              className="text-xs px-2.5 py-1 rounded-lg bg-paper-border/60 dark:bg-gray-800 hover:bg-chinese-red hover:text-white transition font-medium text-chinese-ink dark:text-gray-200"
            >
              Short Story
            </button>
            <button
              onClick={() => setInputText(SAMPLE_TEXTS.idioms)}
              className="text-xs px-2.5 py-1 rounded-lg bg-paper-border/60 dark:bg-gray-800 hover:bg-chinese-red hover:text-white transition font-medium text-chinese-ink dark:text-gray-200"
            >
              成语 Idioms
            </button>
          </div>
        </div>

        {/* Input Textarea */}
        <div className="relative">
          <textarea
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            rows={4}
            placeholder="粘贴中文文本 / Paste Chinese text here..."
            className="w-full p-4 rounded-xl border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark text-chinese-ink dark:text-white font-hanzi text-base leading-relaxed focus:ring-2 focus:ring-chinese-red focus:border-transparent outline-none transition"
          />
          {inputText && (
            <button
              onClick={() => setInputText('')}
              className="absolute top-3 right-3 p-1.5 text-gray-400 hover:text-chinese-red rounded-lg transition"
              title="Clear input"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Controls Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-4 pt-4 border-t border-paper-border dark:border-paper-borderDark">
          {/* HSK Exclusion */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-gray-700 dark:text-gray-400 block mb-1.5 font-medium">
              Filter Known HSK Words
            </label>
            <select
              value={excludeHskMax}
              onChange={(e) => setExcludeHskMax(parseInt(e.target.value, 10))}
              className="w-full p-2.5 rounded-xl border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark text-xs font-semibold text-chinese-ink dark:text-white outline-none cursor-pointer focus:ring-1 focus:ring-chinese-red"
            >
              <option value={0}>Keep All Words (No Exclusion)</option>
              <option value={1}>Exclude HSK 1 (Very Beginner)</option>
              <option value={2}>Exclude HSK 1 - 2</option>
              <option value={3}>Exclude HSK 1 - 3 (Elementary)</option>
              <option value={4}>Exclude HSK 1 - 4 (Intermediate)</option>
              <option value={5}>Exclude HSK 1 - 5 (Advanced)</option>
            </select>
          </div>

          {/* Character Mode */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-gray-700 dark:text-gray-400 block mb-1.5 font-medium">
              Output Columns
            </label>
            <select
              value={hanziType}
              onChange={(e) => setHanziType(e.target.value as HanziExportType)}
              className="w-full p-2.5 rounded-xl border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark text-xs font-semibold text-chinese-ink dark:text-white outline-none cursor-pointer focus:ring-1 focus:ring-chinese-red"
            >
              <option value="SIMP_AND_TRAD">
                Simplified + Traditional (4 cols)
              </option>
              <option value="SIMP">Simplified Only</option>
              <option value="TRAD">Traditional Only</option>
            </select>
          </div>

          {/* Deconstruct Checkbox */}
          <div className="flex items-end">
            <label className="flex items-center gap-2.5 p-2.5 rounded-xl border border-paper-border dark:border-paper-borderDark bg-paper-light dark:bg-paper-dark w-full cursor-pointer hover:border-chinese-red transition">
              <input
                type="checkbox"
                checked={deconstructWords}
                onChange={(e) => setDeconstructWords(e.target.checked)}
                className="w-4 h-4 accent-chinese-red rounded cursor-pointer"
              />
              <div>
                <div className="text-xs font-bold text-chinese-ink dark:text-white">
                  Deconstruct Words
                </div>
                <div className="text-[10px] text-gray-700 dark:text-gray-400 font-medium">
                  Also create cards for single characters
                </div>
              </div>
            </label>
          </div>
        </div>
      </div>

      {/* Mascot & Stats Banner */}
      <div className="bg-gradient-to-r from-amber-500/10 via-paper-card to-emerald-500/10 dark:from-amber-950/20 dark:via-paper-cardDark dark:to-emerald-950/20 border border-amber-500/20 rounded-2xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-soft">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 shrink-0 rounded-full bg-white dark:bg-gray-800 shadow-sm border border-amber-300/80 flex items-center justify-center text-xl">
            🥟
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-sm font-bold text-chinese-ink dark:text-white">
                Bao the Scholar says:
              </span>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-chinese-red/10 text-chinese-red border border-chinese-red/25">
                {extractedWords.length}{' '}
                {extractedWords.length === 1 ? 'word' : 'words'} found
              </span>
            </div>
            <p className="text-xs text-gray-700 dark:text-gray-400 mt-1 font-medium leading-relaxed">
              {excludeHskMax === 1
                ? 'Filtered out known HSK 1 vocabulary. Remaining words are ready for your deck.'
                : excludeHskMax > 1
                  ? `Filtered out known HSK 1–${excludeHskMax} vocabulary. Remaining words are ready for your deck.`
                  : 'All words extracted without tier exclusion.'}
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
          <button
            onClick={handleCopy}
            disabled={extractedWords.length === 0}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold border border-paper-border dark:border-paper-borderDark bg-paper-card dark:bg-paper-cardDark hover:border-chinese-red text-chinese-ink dark:text-white disabled:opacity-40 transition"
          >
            {copied ? (
              <Check className="w-3.5 h-3.5 text-emerald-500" />
            ) : (
              <Copy className="w-3.5 h-3.5" />
            )}
            <span className="hidden sm:inline">
              {copied ? 'Copied!' : 'Copy TSV'}
            </span>
          </button>

          <button
            onClick={handleExport}
            disabled={extractedWords.length === 0}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold bg-chinese-red hover:bg-chinese-redHover text-white shadow-md shadow-chinese-red/20 disabled:opacity-40 transition"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Export Anki Deck</span>
          </button>
        </div>
      </div>

      {/* Results Table */}
      <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl overflow-hidden shadow-soft">
        <div className="p-4 border-b border-paper-border dark:border-paper-borderDark flex items-center justify-between">
          <h3 className="font-bold text-sm text-chinese-ink dark:text-white flex items-center gap-2">
            <span>🎴</span> Generated Flashcards Preview (
            {extractedWords.length})
          </h3>
          <span className="text-xs text-gray-700 dark:text-gray-400 font-medium">
            Click row or copy for Anki TSV import
          </span>
        </div>

        {extractedWords.length === 0 ? (
          <div className="p-12 text-center text-gray-700 dark:text-gray-400">
            <p className="text-3xl mb-2">🎋</p>
            <p className="font-bold text-chinese-ink dark:text-white">
              No words to display
            </p>
            <p className="text-xs mt-1 font-medium">
              Type or paste Chinese text above, or lower the HSK exclusion tier.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto max-h-[500px]">
            <table className="w-full text-left text-xs">
              <thead className="bg-paper-light dark:bg-paper-dark text-gray-700 dark:text-gray-400 sticky top-0 uppercase tracking-wider font-bold">
                <tr>
                  <th className="py-3 px-4">Hanzi (Simp / Trad)</th>
                  <th className="py-3 px-4">Pinyin</th>
                  <th className="py-3 px-4">Definition</th>
                  <th className="py-3 px-4 text-center">HSK Tier</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-paper-border dark:divide-paper-borderDark font-medium">
                {extractedWords.map((w, idx) => (
                  <tr
                    key={idx}
                    className="hover:bg-chinese-red/5 dark:hover:bg-chinese-red/10 transition"
                  >
                    <td className="py-3 px-4 font-hanzi text-base font-bold text-chinese-ink dark:text-white">
                      <span>{w.simplified}</span>
                      {w.traditional !== w.simplified && (
                        <span className="text-xs text-gray-700 dark:text-gray-400 ml-2 font-normal font-medium">
                          ({w.traditional})
                        </span>
                      )}
                    </td>
                    <td className="py-3 px-4 font-medium text-chinese-ink dark:text-gray-200">
                      {w.pinyinAccented}
                    </td>
                    <td
                      className="py-3 px-4 text-gray-600 dark:text-gray-300 max-w-md truncate"
                      title={w.definition}
                    >
                      {w.definition}
                    </td>
                    <td className="py-3 px-4 text-center">
                      {w.hskLevel > 0 ? (
                        <span
                          className={`inline-block px-2 py-0.5 rounded text-[11px] font-bold ${getHskBadgeClasses(w.hskLevel, excludeHskMax > 0 ? excludeHskMax + 1 : 1)}`}
                        >
                          HSK {w.hskLevel}
                        </span>
                      ) : (
                        <span className="inline-block px-2 py-0.5 rounded text-[10px] font-medium bg-gray-100 dark:bg-gray-800 text-gray-500">
                          Untiered
                        </span>
                      )}
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
