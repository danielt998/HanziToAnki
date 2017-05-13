import java.awt.*;
import javax.swing.*;
public class GUI extends JFrame{
  public JTextArea textField;
  public JButton createDeck;
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
  }
  private void addStuff(){
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
    getContentPane().add(panel);
    panel.add(new JLabel("Please enter your Chinese text below"));
    textField=new JTextArea(10,80);
    panel.add(textField);

    JRadioButton theDefault=new JRadioButton("Extract one,two and three letter words");
    JRadioButton singleChar=new JRadioButton("Extract into single characters");
    JRadioButton wordList=new JRadioButton("Each line contains a single word, extract these words individually");
    ButtonGroup group=new ButtonGroup();
    group.add(theDefault);
    group.add(singleChar);
    group.add(wordList);

    panel.add(theDefault);
    panel.add(singleChar);
    panel.add(wordList);

    createDeck=new JButton("Create Deck");
    panel.add(createDeck);
    pack();
  }
}
