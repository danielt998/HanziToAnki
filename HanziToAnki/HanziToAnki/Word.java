package HanziToAnki;

public class Word{
  public Word(){}
  public Word(String givenTrad, String givenSimp,String givenPinyin, String givenPinTones, String givenDef){
    trad=givenTrad;
    simp=givenSimp;
    pinyin=givenPinyin;
    pinTones=givenPinTones;
    def=givenDef;
  }
  public Word(String givenTrad, String givenSimp,String givenPinyin, String givenPinTones, String givenDef, String givenContext){
    trad=givenTrad;
    simp=givenSimp;
    pinyin=givenPinyin;
    pinTones=givenPinTones;
    def=givenDef;
    context=givenContext;
  }
  private String trad,simp,pinyin,pinTones,def,context;
  private final int LARGE_PRIME_NUMBER=65729;

  public String getTraditionalChinese(){
    return trad;
  }
  public String getSimplifiedChinese(){
    return simp;
  }
  public String getPinyinNoTones(){
    return pinyin;
  }
  public String getPinyinWithTones(){
    return pinTones;
  }
  public String getDefinition(){
    return def;
  }
  public String getContext(){
    return context;
  }
  public void setTraditionalChinese (String given){
    trad=given;
  }
  public void setSimplifiedChinese (String given){
    simp=given;
  }
  public void setPinyinNoTones (String given){
    pinyin=given;
  }
  public void setPinyinWithTones (String given){
    pinTones=given;
  }
  public void setDefinition (String given){
    def=given;
  }
  public void setContext (String given){
    context=given;
  }

  public String getFileOutput(){
    return trad + ' ' + simp + ' ' + '[' + pinTones + ']' + ' '+ '/' + def;
  }
  //this allows us to run the sort command to sort the data ;)
  public String getSpecialOutput(){
    return pinyin+ ' ' + trad + ' ' + simp + ' ' + '[' + pinTones + ']' + ' '+ '/' + def;
  }
 //not sure this makes any difference 
  @Override
  public int hashCode(){
  /*  int hash=7;
    for (int i=0;i<simp.length();i++){
      hash=hash*LARGE_PRIME_NUMBER + ((int)simp.charAt(i));
    }
    return hash;*/
    return simp.hashCode();
  }
}

