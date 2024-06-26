//package GUIs;
//
//import hanziToAnki.DeckProducer;
//import hanziToAnki.ExportOptions;
//import hanziToAnki.FileUtils;
//import hanziToAnki.OutputFormat;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.stage.FileChooser;
//
//import javax.swing.*;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class MainGUIController {
    // I don't care about the GUI for now.
//    @FXML
//    ComboBox<String> outputSelector;
//    @FXML
//    TextArea inputText;
//    @FXML
//    TextField outputFullFilename;
//    @FXML
//    Button fileSelectorInputButton;
//    @FXML
//    Button fileSelectorOutputButton;
//    @FXML
//    ComboBox<String> howToHandleHanzi;
//    //    @FXML var pronunciationCheckBox: CheckBox? = null
//    private final List<File> files = new ArrayList<>();
//
//    @FXML
//    public void selectOutput() {
//        File file = (new FileChooser()).showSaveDialog(MainGUI.stage);
//
//        if (file != null) {
//            outputFullFilename.setText(file.getAbsolutePath());
//        }
//    }
//
//    @FXML
//    public void initialize() {
//    }
//
//    @FXML
//    public void export() {
//        String outputName = outputFullFilename.getText();
//
//        if (outputName.equals("")) {
//            showError("Please enter an output file name");
//            return;
//        }
//
//        List<String> allText = new ArrayList<>(Arrays.asList(inputText.getText().split("\n")));
//        for (File file : files) {
//            allText.addAll(FileUtils.fileToStringArray(file));
//        }
//
//        OutputFormat outputFormat = OutputFormat.ANKI; // default, just use for now...
//        ExportOptions options = new ExportOptions(false, true, 0, outputFormat);
//        DeckProducer.produceDeck(allText, options, outputName);
//        showGenerationCompleteAlert(outputName);
//    }
//
//    private void showGenerationCompleteAlert(String outputName) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Generation complete");
//        alert.setHeaderText("Flashcard generation complete");
//        alert.setContentText("Flashcard file " + outputName + " created");
//        alert.showAndWait();
//    }
//
//    @FXML
//    public void selectFiles() {
//        List<File> selectedFiles = new FileChooser().showOpenMultipleDialog(MainGUI.stage);
//    }
//
//    //TODO:update this to use JavaFx Alerts
//    public void showError(String title) {
//        JOptionPane.showMessageDialog(null, title, "Error", JOptionPane.ERROR_MESSAGE);
//    }
//}