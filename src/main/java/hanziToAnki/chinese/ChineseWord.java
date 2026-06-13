package hanziToAnki.chinese;

import hanziToAnki.Word;

// Value class (Project Valhalla) experiment — requires --enable-preview on Java 25+
public value class ChineseWord implements Word {
    private final String traditional;
    private final String simplified;
    private final String pinyin;
    private final String pinyinTones;
    private final String definition;

    public ChineseWord(String traditional, String simplified, String pinyin, String pinyinTones, String definition) {
        this.traditional = traditional;
        this.simplified = simplified;
        this.pinyin = pinyin;
        this.pinyinTones = pinyinTones;
        this.definition = definition;
    }

    public String traditional() { return traditional; }
    public String simplified() { return simplified; }
    public String pinyin() { return pinyin; }
    public String pinyinTones() { return pinyinTones; }
    public String definition() { return definition; }

    public String getSpecialOutput() {
        return pinyin + ' ' + traditional + ' ' + simplified + ' ' + '[' + pinyinTones + ']' + ' ' + '/' + definition;
    }

    @Override
    public String toString() {
        return pinyin();
    }
}

