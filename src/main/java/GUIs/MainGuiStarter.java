package GUIs;

public class MainGuiStarter {

    /**
     * GUIs.MainGUI extends application, and doesn't load javafx well
     * (even with jvm args for javafx set in build.gradle)
     * So this helper class loads javafx, then starts the javafx GUI
     *
     * @param args can be empty
     */
    public static void main(String[] args) {
        MainGui gui = new MainGui();
        gui.main("");
    }
}
