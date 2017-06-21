package HanziToAnki

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.FileInputStream
import javafx.fxml.FXMLLoader
import javafx.scene.control.TabPane

class MainGUI(): Application() {
    companion object {//may not be the best solution... but this treats it like a Java static variable
        @JvmStatic var stage: Stage? = null
    }
    fun main(args:String){
        launch(args)
    }
    fun initialiseDictionary(){
        Extract.readInDictionary()//Possibly not the best place for this
    }
    override fun start(givenStage: Stage){
        stage=givenStage
        initialiseDictionary()
        val loader: FXMLLoader = FXMLLoader()
        val fxmlDocPath:String = "../GUIs/TabbedUI.fxml"
        val fxmlStream: FileInputStream = FileInputStream(fxmlDocPath)
        val root: TabPane = loader.load(fxmlStream) as TabPane
        val scene: Scene = Scene(root)
        givenStage.setScene(scene)
        givenStage.setTitle("HanziToAnki")
        givenStage.show()
    }
}

/**
 * Created by james on 07/06/17.
 */
