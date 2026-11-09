/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        paper: {
          light: '#FBF9F5',
          card: '#FFFFFF',
          dark: '#181A1D',
          cardDark: '#22262B',
          border: '#E7E3DC',
          borderDark: '#33383F',
        },
        chinese: {
          red: '#DC2626',
          redHover: '#B91C1C',
          gold: '#D97706',
          jade: '#059669',
          indigo: '#2563EB',
          ink: '#1F2937',
        },
      },
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'system-ui', 'sans-serif'],
        hanzi: ['-apple-system', 'BlinkMacSystemFont', '"PingFang SC"', '"Hiragino Sans GB"', '"Noto Sans SC"', '"Microsoft YaHei"', 'sans-serif'],
        display: ['"Ma Shan Zheng"', '"Noto Serif SC"', 'serif'],
      },
      boxShadow: {
        'seal': '0 0 0 2px #DC2626, inset 0 0 0 1px #DC2626',
        'soft': '0 4px 20px -2px rgba(0, 0, 0, 0.05)',
      },
    },
  },
  plugins: [],
};
