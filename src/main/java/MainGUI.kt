package HanziToAnki

import Extract
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.TabPane
import javafx.stage.Stage
import java.io.FileInputStream

class MainGUI : Application() {
    companion object {
        //may not be the best solution... but this treats it like a Java static variable
        @JvmStatic
        var stage: Stage? = null
    }

    fun main(args: String) {
        launch(args)
    }

    fun initialiseDictionary() {
        Extract.readInDictionary()//Possibly not the best place for this
    }

    override fun start(givenStage: Stage) {
        stage = givenStage
        initialiseDictionary()
        val loader: FXMLLoader = FXMLLoader()
        val fxmlDocPath: String = "../GUIs/TabbedUI.fxml"
        val fxmlStream: FileInputStream = FileInputStream(fxmlDocPath)
        val root: TabPane = loader.load(fxmlStream) as TabPane
        val scene: Scene = Scene(root)
        givenStage.scene = scene
        givenStage.title = "HanziToAnki"
        givenStage.show()
    }
}

/**
 * Created by james on 07/06/17.
 */
