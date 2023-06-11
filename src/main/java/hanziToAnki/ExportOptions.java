package hanziToAnki;

import hanziToAnki.chinese.ChineseWordFinder;

public record ExportOptions(boolean useWordList,
                            boolean useAllWords,
                            int hskLevelToExclude,
                            ChineseWordFinder.STRATEGY strategy,
                            OutputFormat outputFormat) {
}