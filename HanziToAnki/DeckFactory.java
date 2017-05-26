import java.util.List;
import java.util.ArrayList;
import java.lang.Character;

public class DeckFactory{
  private final String DELIMITER=";";
  private final String CLOSING_HTML_TAG="</span>";
  public Deck generateDeck(List<Word> words){
    Deck deck=new Deck();
    for(Word word:words){
      deck.addLine(getSimp(word)+DELIMITER+getDefinition(word)+DELIMITER+getPinyinWithHTML(word)
                + getSimpWithToneInfo(word) + DELIMITER); 
    }
    return deck;
  }


  private String getSimp(Word word){
    return word.getSimplifiedChinese();
  }

  private String getPinyinWithHTML(Word word){
    String pinyin=word.getPinyinWithTones();
    String[] syllables=pinyin.split(" ");
    StringBuilder builder=new StringBuilder();
    for (String syllable:syllables){
      int tone=Integer.parseInt(""+syllable.charAt(syllable.length()-1));
      builder.append(getOpeningHTMLTag(tone) +getPinyinWithMarks(syllable) + CLOSING_HTML_TAG);
    }
    return builder.toString();
  }

  private String getOpeningHTMLTag(int tone){
    return "<span class=\"tone"+tone+"\">";
  }
  private String getPinyinWithMarks(String syllable){
    char letterForTone=getLetterForTone(syllable);
    int tone=Integer.parseInt(""+syllable.charAt(syllable.length()-1));
    char charWithToneMark=getCharWithTone(letterForTone, tone);
    if(letterForTone=='ü'){
      return syllable.replaceAll("u:", ""+charWithToneMark).substring(0,syllable.length()-1);
    }
    return syllable.replaceAll(""+letterForTone,""+charWithToneMark).substring(0,syllable.length()-1);
  }
  private String getDefinition(Word word){
    return word.getDefinition();
  }

  //FIX THESE
  private char getLetterForTone(String syllable){
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
    return '☃';
  }
  private char getCharWithTone(char originalChar, int tone){
          // may be possible to use unicode modifiers to do this
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
    }
    char newChar= tones[tone-1];
    if(Character.isUpperCase(originalChar)){
      newChar=Character.toUpperCase(newChar);
    }
    return newChar;
  }
  private String getSimpWithToneInfo(Word word){
    return "";
  }
  public static void main(String[] args){
    List<Word> list=new ArrayList<Word>();
    list.add(new Word("阿巴拉契亞","阿巴拉契亚","Aqiya", "A1 ba1 la1 qi4 ya4", "Some definition"));
    list.add(new Word("m","阿巴拉契亚","Aqiya", "A1 ba1 la1 qi4 ya4", "Some definition"));
    DeckFactory fact=new DeckFactory();
    Deck deck=fact.generateDeck(list);
    for(String s:deck.getLines()){
      System.out.println(s);
    }
  }
}
