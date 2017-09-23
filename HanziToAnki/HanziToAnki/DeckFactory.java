package HanziToAnki;

import java.lang.Character;
import java.util.Set;

public class DeckFactory{
  private static final String DELIMITER="\t";
  private static final String CLOSING_HTML_TAG="</span>";
  public static Deck generateDeck(Set<Word> words){
    Deck deck=new Deck();
    Word firstWord = words.stream().findFirst().get(); // get first word, to see if it contains examples element

    if (firstWord.getContext()!=null){     // if context line is used (i.e. word size goes up by 1)
      for (Word word : words) {
        deck.addLine(getSimp(word) + DELIMITER + getDefinition(word) + DELIMITER + getPinyinWithHTML(word)
                + getSimpWithToneInfo(word) + DELIMITER + getContext(word) + DELIMITER);
      }
    }
    else {
      for (Word word : words) {
        deck.addLine(getSimp(word) + DELIMITER + getDefinition(word) + DELIMITER + getPinyinWithHTML(word)
                + getSimpWithToneInfo(word) + DELIMITER);
      }
    }
    return deck;
  }


  private static String getSimp(Word word){
    return word.getSimplifiedChinese();
  }

  private static String getPinyinWithHTML(Word word){
    String pinyin=word.getPinyinWithTones();
    String[] syllables=pinyin.split(" ");
    StringBuilder builder=new StringBuilder();
    for (String syllable:syllables){
      int tone=Integer.parseInt(""+syllable.charAt(syllable.length()-1));
      builder.append(getOpeningHTMLTag(tone) +getPinyinWithMarks(syllable) + CLOSING_HTML_TAG);
    }
    return builder.toString();
  }

  private static String getOpeningHTMLTag(int tone){
    return "<span class=\"tone"+tone+"\">";
  }
  private static String getPinyinWithMarks(String syllable){
    char letterForTone=getLetterForTone(syllable);
    int tone=Integer.parseInt(""+syllable.charAt(syllable.length()-1));
    char charWithToneMark=getCharWithTone(letterForTone, tone);
    if(letterForTone=='ü'){
      return syllable.replaceAll("u:", ""+charWithToneMark).substring(0,syllable.length()-1);
    }
    return syllable.replaceAll(""+letterForTone,""+charWithToneMark).substring(0,syllable.length()-1);
  }
  private static String getDefinition(Word word){
    return word.getDefinition();
  }

  private static String getContext(Word word){
    return word.getContext();
  }

  //FIX THESE
  private static char getLetterForTone(String syllable){
     //yes this is disgusting
     if(syllable.contains("E")){
      return 'E'; 
    }else if(syllable.contains("A")){
      return 'A';
    }else if(syllable.contains("Ou")){
      return 'O';
    }else{
      for(int i=syllable.length()-1; i>=0; i--){
        if(syllable.charAt(i)==':'){
          return 'ü';
        }
        if ("AEIOU".contains(""+syllable.charAt(i))){
          return syllable.charAt(i);
        }
      }
    }
   
    if(syllable.contains("e")){
      return 'e'; 
    }else if(syllable.contains("a")){
      return 'a';
    }else if(syllable.contains("ou")){
      return 'o';
    }else{
      for(int i=syllable.length()-1; i>=0; i--){
        if(syllable.charAt(i)==':'){
          return 'ü';
        }
        if ("aeiou".contains(""+syllable.charAt(i))){
          return syllable.charAt(i);
        }
      }
    }
    if(syllable.contains("r")){
      return 'e';//TODO:for now, but later we may want to handle this differently
    }
    System.out.println("Something went wrong, syllable:"+syllable);
    return '☃';
  }
  private static char getCharWithTone(char originalChar, int tone){
    //TODO: may be possible to use unicode modifiers to do this
    if (!"aeiouü".contains(""+originalChar)){
        return originalChar;
    }
    char[] tones=new char[]{};
    switch (Character.toLowerCase(originalChar)){
      case 'a': tones= new char[]{'ā','á','ǎ','à','a'};
                break;
      case 'e': tones= new char[]{'ē','é','ě','è','e'};
                break;
      case 'i': tones= new char[]{'ī','í','ǐ','ì','i'};
                break;
      case 'o': tones= new char[]{'ō','ó','ǒ','ò','o'};
                break;
      case 'u': tones= new char[]{'ū','ú','ǔ','ù','u'};
                break;
      case 'ü': tones= new char[]{'ǖ','ǘ','ǚ','ǜ','ü'};
                break;
      default : System.out.println("Unrecognised character:"+originalChar);
    }
    char newChar= tones[tone-1];
    if(Character.isUpperCase(originalChar)){
      newChar=Character.toUpperCase(newChar);
    }
    return newChar;
  }
  private static String getSimpWithToneInfo(Word word){
    return "";
  }
}
