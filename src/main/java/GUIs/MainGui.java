package GUIs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainGui extends Application {
    public static Stage stage;

    public final void main(String args) {
        Application.launch(args);
    }

    public final void initialiseDictionary() throws URISyntaxException {
//        ChineseDictionaryExtractor.readInDictionary();
    }

    public void start(Stage givenStage) throws URISyntaxException, IOException {
        stage = givenStage;
        this.initialiseDictionary();
        FXMLLoader loader = new FXMLLoader();
        InputStream fxmlStream = this.getClass().getResourceAsStream("/GUI/MainGUI.fxml");
        Object tabPane = loader.load(fxmlStream);

        TabPane root = (TabPane) tabPane;
        Scene scene = new Scene(root);
        givenStage.setScene(scene);
        givenStage.setTitle("HanziToAnki");
        givenStage.show();
    }

    public final Stage getStage() {
        return stage;
    }

    public final void setStage(Stage newStage) {
        stage = newStage;
    }
}
