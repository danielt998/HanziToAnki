import { Check, Settings, X } from 'lucide-react';
import type React from 'react';
import { TONE_PALETTES, type TonePalette } from '../lib/toneHelper';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  tonePalette: TonePalette;
  setTonePalette: (palette: TonePalette) => void;
  colorHanziGlyphs: boolean;
  setColorHanziGlyphs: (color: boolean) => void;
  hskStandard: 'HSK2' | 'HSK3';
  setHskStandard: (std: 'HSK2' | 'HSK3') => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen,
  onClose,
  tonePalette,
  setTonePalette,
  colorHanziGlyphs,
  setColorHanziGlyphs,
  hskStandard,
  setHskStandard,
}) => {
  if (!isOpen) return null;

  const palettes: { key: TonePalette; name: string }[] = [
    {
      key: 'standard',
      name: 'Standard',
    },
    {
      key: 'hanping',
      name: 'Hanping / Trainchinese',
    },
    {
      key: 'rainbow',
      name: 'Vibrant Rainbow',
    },
    {
      key: 'monochrome',
      name: 'Monochrome Minimal',
    },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-fade-in">
      <div className="bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl max-w-lg w-full p-6 shadow-2xl relative">
        <button
          type="button"
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-700 dark:text-gray-400 hover:text-chinese-ink dark:hover:text-white"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-2.5 mb-1">
          <Settings className="w-5 h-5 text-chinese-red shrink-0" />
          <h3 className="text-lg font-bold text-chinese-ink dark:text-white">
            Preferences & Tone Settings
          </h3>
        </div>
        <p className="text-xs text-gray-700 dark:text-gray-400 mb-6 font-medium">
          Personalize colors for tones 1–5, HSK standards, and reading preferences.
        </p>

        {/* Tone Palettes */}
        <div className="space-y-3 mb-6">
          <span className="text-xs font-bold uppercase tracking-wider text-chinese-ink dark:text-gray-300 block">
            Tone Color Palette (Colors for Tones 1–5)
          </span>

          <div className="grid grid-cols-1 gap-2">
            {palettes.map((p) => {
              const colors = TONE_PALETTES[p.key];
              const isSelected = tonePalette === p.key;

              return (
                <div
                  key={p.key}
                  onClick={() => setTonePalette(p.key)}
                  className={`flex items-center justify-between p-3 rounded-xl border cursor-pointer transition ${
                    isSelected
                      ? 'border-chinese-red bg-chinese-red/5 dark:bg-chinese-red/10 ring-1 ring-chinese-red'
                      : 'border-paper-border dark:border-paper-borderDark hover:border-gray-400 bg-paper-light dark:bg-paper-dark'
                  }`}
                >
                  <div className="font-semibold text-sm text-chinese-ink dark:text-gray-100 flex items-center gap-2">
                    {p.name}
                    {isSelected && <Check className="w-4 h-4 text-chinese-red" />}
                  </div>

                  {/* Palette color preview swatches (Tones 1–5) */}
                  <div className="flex items-center gap-1.5" title="Tones 1–5">
                    {[colors.tone1, colors.tone2, colors.tone3, colors.tone4, colors.tone5].map(
                      (c, i) => (
                        <span
                          key={i}
                          className="w-4 h-4 rounded-full border border-black/10 dark:border-white/10"
                          style={{
                            backgroundColor: c === 'currentColor' ? '#6B7280' : c,
                          }}
                          title={`Tone ${i + 1}`}
                        />
                      ),
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Tone Coloring Target */}
        <div className="space-y-4 mb-6 pt-4 border-t border-paper-border dark:border-paper-borderDark">
          <span className="text-xs font-bold uppercase tracking-wider text-chinese-ink dark:text-gray-300 block">
            Color Target
          </span>
          <label className="flex items-center justify-between cursor-pointer p-3 rounded-xl border border-paper-border dark:border-paper-borderDark hover:bg-gray-50 dark:hover:bg-gray-800/40">
            <div>
              <div className="text-sm font-semibold text-chinese-ink dark:text-gray-100">
                Color Hanzi Characters Directly
              </div>
              <div className="text-xs text-gray-700 dark:text-gray-400 font-medium">
                Color individual Chinese characters with tone hues
              </div>
            </div>
            <input
              type="checkbox"
              checked={colorHanziGlyphs}
              onChange={(e) => setColorHanziGlyphs(e.target.checked)}
              className="w-5 h-5 accent-chinese-red rounded cursor-pointer"
            />
          </label>
        </div>

        {/* HSK Standard */}
        <div className="space-y-4 mb-6 pt-4 border-t border-paper-border dark:border-paper-borderDark">
          <span className="text-xs font-bold uppercase tracking-wider text-chinese-ink dark:text-gray-300 block">
            HSK Standard
          </span>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setHskStandard('HSK3')}
              className={`p-3 rounded-xl border text-left transition ${
                hskStandard === 'HSK3'
                  ? 'border-chinese-red bg-chinese-red/5 dark:bg-chinese-red/10 ring-1 ring-chinese-red'
                  : 'border-paper-border dark:border-paper-borderDark hover:border-gray-400'
              }`}
            >
              <div className="font-bold text-sm text-chinese-ink dark:text-gray-100">
                HSK 3.0 (9 Tiers)
              </div>
              <div className="text-xs text-gray-700 dark:text-gray-400 mt-1 font-medium">
                Official modern 11,092 word curriculum
              </div>
            </button>

            <button
              type="button"
              onClick={() => setHskStandard('HSK2')}
              className={`p-3 rounded-xl border text-left transition ${
                hskStandard === 'HSK2'
                  ? 'border-chinese-red bg-chinese-red/5 dark:bg-chinese-red/10 ring-1 ring-chinese-red'
                  : 'border-paper-border dark:border-paper-borderDark hover:border-gray-400'
              }`}
            >
              <div className="font-bold text-sm text-chinese-ink dark:text-gray-100">
                HSK 2.0 (6 Tiers)
              </div>
              <div className="text-xs text-gray-700 dark:text-gray-400 mt-1 font-medium">
                Classic 5,000 word curriculum
              </div>
            </button>
          </div>
        </div>

        <button
          type="button"
          onClick={onClose}
          className="w-full py-2.5 bg-chinese-red hover:bg-chinese-redHover text-white rounded-xl font-semibold text-sm shadow-md shadow-chinese-red/20 transition"
        >
          Save & Close
        </button>
      </div>
    </div>
  );
};
