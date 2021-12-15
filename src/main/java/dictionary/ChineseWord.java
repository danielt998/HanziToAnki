package dictionary;

public record ChineseWord(String traditional,
                          String simplified,
                          String pinyin,
                          String pinyinTones,
                          String definition) implements IWord{

    //this allows us to run the sort command to sort the data ;)
    public String getSpecialOutput() {
        return pinyin + ' ' + traditional + ' ' + simplified + ' ' + '[' + pinyinTones + ']' + ' ' + '/' + definition;
    }
}

