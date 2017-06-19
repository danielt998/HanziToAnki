package GUIs

import HanziToAnki.*
import javafx.fxml.FXML
import javafx.scene.control.*
import java.util.*

class TabbedUIController {
    @FXML var outputSelector: ComboBox<String>? = null
    @FXML var inputText: TextArea? = null
    @FXML var inputFullFilename: TextField? = null
    @FXML var outputFullFilename: TextField? = null
    @FXML var fileSelectorButton: Button? = null
    @FXML var howToHandleHanzi: ComboBox<String>? = null
//    @FXML var pronunciationCheckBox: CheckBox? = null

    constructor() {}

    @FXML fun initialize() {}
    @FXML fun export() {
        System.out.println("hi")
        val outputName:String?=outputFullFilename?.getText()
        Main.produceDeck(inputText?.getText()?.split("\n") as List<String>, ExportOptions(false, true, 0),outputName)
System.out.println("did stuff")
    }
}