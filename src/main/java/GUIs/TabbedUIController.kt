package GUIs

import HanziToAnki.*
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.FileChooser
import java.io.File
import java.util.*
import javax.swing.JOptionPane

class TabbedUIController {
    @FXML var outputSelector: ComboBox<String>? = null
    @FXML var inputText: TextArea? = null
    @FXML var outputFullFilename: TextField? = null
    @FXML var fileSelectorInputButton: Button? = null
    @FXML var fileSelectorOutputButton: Button? = null
    @FXML var howToHandleHanzi: ComboBox<String>? = null
//    @FXML var pronunciationCheckBox: CheckBox? = null
    var files:List<File> = ArrayList<File>()

    @FXML fun selectOutput(){
        val file = (FileChooser()).showSaveDialog(MainGUI.stage)
        if (file != null) {
            outputFullFilename!!.text =file.absolutePath
        }
    }
    @FXML fun initialize() {}
    @FXML fun export() {
        val outputName:String?= outputFullFilename!!.getText()
        val allText:ArrayList<String> = ArrayList<String>()
        if(outputName.equals("")){
            showError("Please enter an output file name")
            return
        }
        allText.addAll(inputText!!.getText().split("\n") as List<String>)
        for(file:File in files){
            allText.addAll(FileUtils.fileToStringArray(file))
        }

        var outputFormat = OutputFormat.ANKI // default, just use for now...
        Main.produceDeck(allText, ExportOptions(false, true, 0, outputFormat), outputName)
    }
    @FXML fun selectFiles(){
        val selectedFiles:List<File>?=FileChooser().showOpenMultipleDialog(MainGUI.stage)
        selectedFiles?.let{files=selectedFiles}
    }
    fun showError(title:String){
        //using swing instead of JavaFx due to reasons of Java version
        //TODO:update this to use JavaFx Alerts
        JOptionPane.showMessageDialog(null, title, "Error", JOptionPane.ERROR_MESSAGE)
    }
}