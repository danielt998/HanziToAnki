import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
public class GUI extends JFrame implements ActionListener{
  public JTextArea textField;
  public JTextField outputName=new JTextField();
  public JButton createDeck;

  public JRadioButton theDefault;
  public JRadioButton singleChar;
  public JRadioButton wordList;
  public static void main(String[] args){
    GUI gui=new GUI();
    gui.setVisible(true);
  }
  public GUI(){
    initUI();
  }
  private void initUI(){
    setTitle("HanziToAnki");
    setSize(300, 200);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    addStuff();
    Extract.readInDictionary();//Possibly not the best place for this
  }
  private void addStuff(){
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
    getContentPane().add(panel);
    panel.add(new JLabel("Please enter your Chinese text below"));
    textField=new JTextArea(10,80);
    panel.add(textField);

    theDefault=new JRadioButton("Extract one,two and three letter words, ignoring single character"
                                +" words if they exist as part of another word in the text");
    singleChar=new JRadioButton("Extract into single characters");
    wordList=new JRadioButton(
                    "Each line contains a single word, extract these words individually");
    ButtonGroup group=new ButtonGroup();
    group.add(theDefault);
    group.add(singleChar);
    group.add(wordList);

    panel.add(theDefault);
    panel.add(singleChar);
    panel.add(wordList);

    panel.add(new JTextField("Please enter a file name (no extension):"));
    panel.add(outputName);
    createDeck=new JButton("Create Deck");
    panel.add(createDeck);
    createDeck.addActionListener(this);
    pack();
  }
  public void actionPerformed(ActionEvent e){
    if(e.getSource()==createDeck){
       Main.produceDeck(Arrays.asList(textField.getText().split("\\r?\\n")),new Options(wordList.isSelected(),theDefault.isSelected(),0),outputName.getText()+".csv");
    }
  }
}
