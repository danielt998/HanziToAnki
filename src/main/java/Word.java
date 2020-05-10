public record Word(String traditional, String simplified, String pinyin, String pinyinTones, String definition) {

    public String getFileOutput() {
        return traditional + ' ' +
                simplified + ' ' +
                '[' + pinyinTones + ']' + ' ' +
                 '/' + definition;
    }

    //this allows us to run the sort command to sort the data ;)
    public String getSpecialOutput() {
        return pinyin + ' ' + traditional + ' ' + simplified + ' ' + '[' + pinyinTones + ']' + ' ' + '/' + definition;
    }
}

