package hanziToAnki;

public record ExportOptions(boolean useWordList, boolean useAllWords, int hskLevelToExclude, OutputFormat outputFormat) {}