package hanziToAnki.french;

import hanziToAnki.Word;

public record FrenchWord(String french,
                         String english) implements Word {

    //this allows us to run the sort command to sort the data ;)
    public String getSpecialOutput() {
        return french + ' ' + english;
    }
}

