package dictionary;

public record ChineseWord(String traditional,
                          String simplified,
                          String pinyin,
                          String pinyinTones,
                          String definition) implements Word {

    //this allows us to run the sort command to sort the data ;)
    public String getSpecialOutput() {
        return pinyin + ' ' + traditional + ' ' + simplified + ' ' + '[' + pinyinTones + ']' + ' ' + '/' + definition;
    }

    // so we can sort Set<ChineseWord> to be alphabetically ordered
    @Override
    public String toString() {
        return pinyin();
    }
}

