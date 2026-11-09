import { BookOpen, Layers, Moon, Settings, Sliders, Sun } from 'lucide-react';
import type React from 'react';

export type ActiveTab = 'text-to-deck' | 'tuner' | 'reader';

interface NavbarProps {
  activeTab: ActiveTab;
  setActiveTab: (tab: ActiveTab) => void;
  isDark: boolean;
  toggleDark: () => void;
  openSettings: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  isDark,
  toggleDark,
  openSettings,
}) => {
  return (
    <header className="sticky top-0 z-40 bg-paper-light/90 dark:bg-paper-dark/90 backdrop-blur-md border-b border-paper-border dark:border-paper-borderDark transition-colors">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        {/* Brand / Logo */}
        <div
          className="flex items-center gap-3 cursor-pointer"
          onClick={() => setActiveTab('text-to-deck')}
        >
          <div className="w-10 h-10 rounded-xl bg-chinese-red text-white flex items-center justify-center font-display text-2xl font-bold shadow-md shadow-chinese-red/20 border-2 border-amber-300">
            字
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-bold text-lg tracking-tight text-chinese-ink dark:text-gray-100">
                HanziToAnki
              </span>
              <span className="text-xs px-1.5 py-0.5 rounded bg-chinese-red/10 text-chinese-red font-medium border border-chinese-red/20">
                Web
              </span>
            </div>
            <p className="text-[10px] text-gray-700 dark:text-gray-400 font-medium">
              100% Client-Side Flashcard Suite
            </p>
          </div>
        </div>

        {/* Desktop Tabs */}
        <nav className="hidden md:flex items-center gap-1 bg-paper-border/40 dark:bg-paper-cardDark p-1 rounded-xl border border-paper-border dark:border-paper-borderDark">
          <button
            type="button"
            onClick={() => setActiveTab('text-to-deck')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
              activeTab === 'text-to-deck'
                ? 'bg-chinese-red text-white shadow-sm'
                : 'text-gray-600 dark:text-gray-300 hover:text-chinese-red dark:hover:text-white'
            }`}
          >
            <Layers className="w-4 h-4" />
            <span>Text to Deck</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('tuner')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
              activeTab === 'tuner'
                ? 'bg-chinese-red text-white shadow-sm'
                : 'text-gray-600 dark:text-gray-300 hover:text-chinese-red dark:hover:text-white'
            }`}
          >
            <Sliders className="w-4 h-4" />
            <span>Flashcard Tuner</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('reader')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
              activeTab === 'reader'
                ? 'bg-chinese-red text-white shadow-sm'
                : 'text-gray-600 dark:text-gray-300 hover:text-chinese-red dark:hover:text-white'
            }`}
          >
            <BookOpen className="w-4 h-4" />
            <span>Clip Reader</span>
          </button>
        </nav>

        {/* Global Action Icons */}
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={toggleDark}
            title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
            className="w-9 h-9 rounded-lg flex items-center justify-center text-gray-600 dark:text-gray-300 hover:bg-gray-200/60 dark:hover:bg-gray-800 transition"
          >
            {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4" />}
          </button>

          <button
            type="button"
            onClick={openSettings}
            title="Settings & Tone Palette"
            className="w-9 h-9 rounded-lg flex items-center justify-center text-gray-600 dark:text-gray-300 hover:bg-gray-200/60 dark:hover:bg-gray-800 transition"
          >
            <Settings className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Mobile Floating Bottom Dock */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-paper-card/95 dark:bg-paper-cardDark/95 backdrop-blur-lg border-t border-paper-border dark:border-paper-borderDark px-4 py-2 flex justify-around shadow-lg">
        <button
          type="button"
          onClick={() => setActiveTab('text-to-deck')}
          className={`flex flex-col items-center gap-1 text-xs font-medium py-1 px-3 rounded-lg transition ${
            activeTab === 'text-to-deck'
              ? 'text-chinese-red font-bold'
              : 'text-gray-500 dark:text-gray-400'
          }`}
        >
          <Layers className="w-5 h-5" />
          <span>Creator</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('tuner')}
          className={`flex flex-col items-center gap-1 text-xs font-medium py-1 px-3 rounded-lg transition ${
            activeTab === 'tuner'
              ? 'text-chinese-red font-bold'
              : 'text-gray-500 dark:text-gray-400'
          }`}
        >
          <Sliders className="w-5 h-5" />
          <span>Tuner</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('reader')}
          className={`flex flex-col items-center gap-1 text-xs font-medium py-1 px-3 rounded-lg transition ${
            activeTab === 'reader'
              ? 'text-chinese-red font-bold'
              : 'text-gray-500 dark:text-gray-400'
          }`}
        >
          <BookOpen className="w-5 h-5" />
          <span>Reader</span>
        </button>
      </div>
    </header>
  );
};
