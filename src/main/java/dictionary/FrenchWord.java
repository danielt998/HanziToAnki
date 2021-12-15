package dictionary;

public record FrenchWord(String french,
                         String english) implements IWord{

    //this allows us to run the sort command to sort the data ;)
    public String getSpecialOutput() {
        return french + ' ' + english;
    }
}

