package GUIs

import HanziToAnki.*
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.FileChooser
import java.util.*

class TabbedUIController {
    @FXML var outputSelector: ComboBox<String>? = null
    @FXML var inputText: TextArea? = null
    @FXML var inputFullFilename: TextField? = null
    @FXML var outputFullFilename: TextField? = null
    @FXML var fileSelectorInputButton: Button? = null
    @FXML var fileSelectorOutputButton: Button? = null
    @FXML var howToHandleHanzi: ComboBox<String>? = null
//    @FXML var pronunciationCheckBox: CheckBox? = null

    @FXML fun selectOutput(){
        val file = (FileChooser()).showSaveDialog(MainGUI.stage)
        if (file != null) {
            outputFullFilename?.text=file.absolutePath
        }
    }
    @FXML fun initialize() {}
    @FXML fun export() {
        val outputName:String?=outputFullFilename?.getText()
        Main.produceDeck(inputText?.getText()?.split("\n") as List<String>, ExportOptions(false, true, 0),outputName)
    }
}