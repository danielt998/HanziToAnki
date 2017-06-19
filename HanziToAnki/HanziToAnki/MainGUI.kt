package HanziToAnki

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.FileInputStream
import javafx.fxml.FXMLLoader
import javafx.event.ActionEvent
import javafx.scene.control.TabPane

class MainGUI(): Application(){
    fun export (event: ActionEvent){}
    fun main(args:String){
        //val controller:HanziToAnki.MainGUI=HanziToAnki.MainGUI(ComboBox())
        launch(args)
    }
    fun initialiseDictionary(){
        Extract.readInDictionary()//Possibly not the best place for this
    }
    override fun start(stage: Stage){
        initialiseDictionary()
        val loader: FXMLLoader = FXMLLoader()
        val fxmlDocPath:String = "../GUIs/TabbedUI.fxml"
        val fxmlStream: FileInputStream = FileInputStream(fxmlDocPath)
        val root: TabPane = loader.load(fxmlStream) as TabPane
        val scene: Scene = Scene(root);
        stage.setScene(scene)
        stage.setTitle("HanziToAnki")
        stage.show()
    }
}

/**
 * Created by james on 07/06/17.
 */
