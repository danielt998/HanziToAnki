import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;
import javax.swing.JOptionPane;

public class MainGUIController {
    @FXML ComboBox<String> outputSelector;
    @FXML TextArea inputText;
    @FXML TextField outputFullFilename;
    @FXML Button fileSelectorInputButton;
    @FXML Button fileSelectorOutputButton;
    @FXML ComboBox<String> howToHandleHanzi;
//    @FXML var pronunciationCheckBox: CheckBox? = null
    private List<File> files = new ArrayList<File>();

    @FXML
    public void selectOutput() {
        File file = (new FileChooser()).showSaveDialog(MainGUI.stage);

        if (file != null) {
            outputFullFilename.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void initialize() {}

    @FXML
    public void export() {
        String outputName = outputFullFilename.getText();
        List<String> allText = new ArrayList<>();

        if (outputName.equals("")) {
            showError("Please enter an output file name");
            return;
        }

        allText.addAll(Arrays.asList(inputText.getText().split("\n")));
        for (File file : files) {
            allText.addAll(FileUtils.fileToStringArray(file));
        }

        OutputFormat outputFormat = OutputFormat.ANKI; // default, just use for now...
        ExportOptions options = new ExportOptions(false, true, 0, outputFormat);
        Main.produceDeck(allText, options, outputName);
    }

    @FXML
    public void selectFiles() {
        List<File> selectedFiles = new FileChooser().showOpenMultipleDialog(MainGUI.stage);
    }

    //TODO:update this to use JavaFx Alerts
    public void showError(String title) {
        JOptionPane.showMessageDialog(null, title, "Error", JOptionPane.ERROR_MESSAGE);
    }
}