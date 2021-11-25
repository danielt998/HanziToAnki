import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class MainGUI extends Application {
        public static Stage stage;

        public final void main(String args) {
            Application.launch(new String[]{args});
        }

        public final void initialiseDictionary() throws URISyntaxException {
            Extract.readInDictionary();
        }

        public void start(Stage givenStage) throws URISyntaxException {
            stage = givenStage;
            this.initialiseDictionary();
            FXMLLoader loader = new FXMLLoader();
            InputStream fxmlStream = this.getClass().getResourceAsStream("/GUI/MainGUI.fxml");
            Object tabPane = loader.load(fxmlStream);

            TabPane root = (TabPane)tabPane;
            Scene scene = new Scene((Parent)root);
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
