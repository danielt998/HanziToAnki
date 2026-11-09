import React, { useEffect, useState } from 'react';
import { ClipboardReader } from './components/ClipboardReader';
import { FlashcardTuner } from './components/FlashcardTuner';
import { ActiveTab, Navbar } from './components/Navbar';
import { SettingsModal } from './components/SettingsModal';
import { TextToDeck } from './components/TextToDeck';
import { dictionaryService } from './lib/dictionary';
import { TonePalette } from './lib/toneHelper';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ActiveTab>('text-to-deck');
  const [isDark, setIsDark] = useState<boolean>(() => {
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  });
  const [isSettingsOpen, setIsSettingsOpen] = useState<boolean>(false);
  const [tonePalette, setTonePalette] = useState<TonePalette>('standard');
  const [colorHanziGlyphs, setColorHanziGlyphs] = useState<boolean>(false);
  const [hskStandard, setHskStandard] = useState<'HSK2' | 'HSK3'>('HSK3');
  const [dictReady, setDictReady] = useState<boolean>(false);

  // Initialize dark mode class on <html>
  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDark]);

  // Initialize dictionary on mount
  useEffect(() => {
    dictionaryService.init().then(() => {
      setDictReady(true);
    });
  }, []);

  return (
    <div className="min-h-screen flex flex-col bg-paper-light dark:bg-paper-dark text-chinese-ink dark:text-gray-100 transition-colors">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        isDark={isDark}
        toggleDark={() => setIsDark(!isDark)}
        openSettings={() => setIsSettingsOpen(true)}
      />

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-6 pb-24 md:pb-12">
        {!dictReady ? (
          <div className="flex flex-col items-center justify-center min-h-[300px] text-center">
            <div className="w-12 h-12 border-4 border-chinese-red border-t-transparent rounded-full animate-spin mb-4" />
            <p className="font-bold text-chinese-ink dark:text-white font-display text-lg">
              正在加载词典... Loading CC-CEDICT
            </p>
            <p className="text-xs text-gray-700 dark:text-gray-400 mt-1 font-medium">
              Indexing vocabulary for 100% offline client-side lookup
            </p>
          </div>
        ) : (
          <>
            {activeTab === 'text-to-deck' && (
              <TextToDeck
                tonePalette={tonePalette}
                colorHanziGlyphs={colorHanziGlyphs}
              />
            )}
            {activeTab === 'tuner' && <FlashcardTuner />}
            {activeTab === 'reader' && (
              <ClipboardReader
                tonePalette={tonePalette}
                colorHanziGlyphs={colorHanziGlyphs}
              />
            )}
          </>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-paper-border dark:border-paper-borderDark py-6 text-center text-xs text-gray-700 dark:text-gray-400 font-medium">
        <p className="flex items-center justify-center gap-1.5 font-medium flex-wrap">
          <span>🎋 HanziToAnki Web</span>
          <span>•</span>
          <span>
            Native <code className="text-chinese-red">Intl.Segmenter</code>
          </span>
          <span>•</span>
          <span>
            Dictionary data by{' '}
            <a
              href="https://cc-cedict.org/wiki/"
              target="_blank"
              rel="noreferrer"
              className="text-chinese-red underline hover:text-chinese-redHover font-semibold"
            >
              CC-CEDICT
            </a>{' '}
            (
            <a
              href="https://creativecommons.org/licenses/by-sa/3.0/"
              target="_blank"
              rel="noreferrer"
              className="underline hover:text-chinese-red"
            >
              CC BY-SA 3.0
            </a>
            )
          </span>
          <span>•</span>
          <span>100% Client-Side Sandbox</span>
        </p>
      </footer>

      {/* Settings Modal */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        tonePalette={tonePalette}
        setTonePalette={setTonePalette}
        colorHanziGlyphs={colorHanziGlyphs}
        setColorHanziGlyphs={setColorHanziGlyphs}
        hskStandard={hskStandard}
        setHskStandard={setHskStandard}
      />
    </div>
  );
};
