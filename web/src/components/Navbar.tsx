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

interface NavTabConfig {
  id: ActiveTab;
  desktopLabel: string;
  mobileLabel: string;
  icon: typeof Layers;
}

const NAV_TABS: NavTabConfig[] = [
  { id: 'text-to-deck', desktopLabel: 'Text to Deck', mobileLabel: 'Creator', icon: Layers },
  { id: 'tuner', desktopLabel: 'Flashcard Tuner', mobileLabel: 'Tuner', icon: Sliders },
  { id: 'reader', desktopLabel: 'Clip Reader', mobileLabel: 'Reader', icon: BookOpen },
];

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  isDark,
  toggleDark,
  openSettings,
}) => {
  const themeLabel = isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode';

  return (
    <>
      <header className="sticky top-0 z-40 bg-paper-light/90 dark:bg-paper-dark/90 backdrop-blur-md border-b border-paper-border dark:border-paper-borderDark transition-colors">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-2">
          {/* Brand / Logo */}
          <button
            type="button"
            onClick={() => setActiveTab('text-to-deck')}
            className="flex items-center gap-2.5 sm:gap-3 cursor-pointer text-left shrink-0 bg-transparent border-0 p-0"
          >
            <div
              aria-hidden="true"
              className="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-chinese-red text-white flex items-center justify-center font-display text-xl sm:text-2xl font-bold shadow-md shadow-chinese-red/20 border-2 border-amber-300 shrink-0"
            >
              字
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-bold text-base sm:text-lg tracking-tight text-chinese-ink dark:text-gray-100 whitespace-nowrap">
                  HanziToAnki
                </span>
                <span className="text-[11px] sm:text-xs px-1.5 py-0.5 rounded bg-chinese-red/10 text-chinese-red font-medium border border-chinese-red/20 whitespace-nowrap">
                  Web
                </span>
              </div>
              <p className="hidden sm:block text-[10px] text-gray-700 dark:text-gray-400 font-medium whitespace-nowrap">
                100% Client-Side Flashcard Suite
              </p>
            </div>
          </button>

          {/* Desktop Tabs */}
          <nav
            aria-label="Primary"
            className="hidden md:flex items-center gap-1 bg-paper-border/40 dark:bg-paper-cardDark p-1 rounded-xl border border-paper-border dark:border-paper-borderDark shrink-0"
          >
            {NAV_TABS.map(({ id, desktopLabel, icon: Icon }) => {
              const isActive = activeTab === id;
              return (
                <button
                  key={id}
                  type="button"
                  onClick={() => setActiveTab(id)}
                  aria-current={isActive ? 'page' : undefined}
                  className={`flex items-center gap-2 px-3 lg:px-4 py-2 rounded-lg text-sm font-semibold whitespace-nowrap transition-all cursor-pointer ${
                    isActive
                      ? 'bg-chinese-red text-white shadow-sm'
                      : 'text-gray-700 dark:text-gray-300 hover:text-chinese-red dark:hover:text-white'
                  }`}
                >
                  <Icon className="w-4 h-4 shrink-0" aria-hidden="true" />
                  <span>{desktopLabel}</span>
                </button>
              );
            })}
          </nav>

          {/* Global Action Icons */}
          <div className="flex items-center gap-2 shrink-0">
            <button
              type="button"
              onClick={toggleDark}
              title={themeLabel}
              aria-label={themeLabel}
              className="h-10 px-3 rounded-full flex items-center justify-center gap-1.5 text-xs font-semibold border border-paper-border dark:border-paper-borderDark bg-paper-card dark:bg-paper-cardDark text-chinese-ink dark:text-gray-100 hover:border-chinese-red/50 dark:hover:border-chinese-red/50 shadow-xs transition cursor-pointer shrink-0"
            >
              {isDark ? (
                <>
                  <Sun className="w-4 h-4 text-amber-400 shrink-0" aria-hidden="true" />
                  <span>Light</span>
                </>
              ) : (
                <>
                  <Moon className="w-4 h-4 text-gray-700 shrink-0" aria-hidden="true" />
                  <span>Dark</span>
                </>
              )}
            </button>

            <button
              type="button"
              onClick={openSettings}
              title="Settings & Tone Palette"
              aria-label="Settings & Tone Palette"
              className="w-10 h-10 rounded-full flex items-center justify-center border border-paper-border dark:border-paper-borderDark bg-paper-card dark:bg-paper-cardDark text-gray-700 dark:text-gray-200 hover:border-chinese-red/50 hover:text-chinese-red dark:hover:text-white shadow-xs transition cursor-pointer shrink-0"
            >
              <Settings className="w-4 h-4" aria-hidden="true" />
            </button>
          </div>
        </div>
      </header>

      {/* Mobile Floating Bottom Dock */}
      <nav
        aria-label="Mobile Navigation"
        className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-paper-card/95 dark:bg-paper-cardDark/95 backdrop-blur-lg border-t border-paper-border dark:border-paper-borderDark px-4 pt-2 pb-[calc(0.5rem+env(safe-area-inset-bottom))] flex justify-around shadow-lg"
      >
        {NAV_TABS.map(({ id, mobileLabel, icon: Icon }) => {
          const isActive = activeTab === id;
          return (
            <button
              key={id}
              type="button"
              onClick={() => setActiveTab(id)}
              aria-current={isActive ? 'page' : undefined}
              className={`flex flex-col items-center justify-center gap-1 min-w-[64px] min-h-[44px] text-xs py-1 px-3 rounded-lg transition cursor-pointer ${
                isActive
                  ? 'text-chinese-red font-bold'
                  : 'text-gray-600 dark:text-gray-300 font-medium'
              }`}
            >
              <Icon className="w-5 h-5 shrink-0" aria-hidden="true" />
              <span>{mobileLabel}</span>
            </button>
          );
        })}
      </nav>
    </>
  );
};
