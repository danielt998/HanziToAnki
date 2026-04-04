import hanziToAnki.Main;
import server.Application;
import java.net.URISyntaxException;

/**
 * Launcher that detects whether to run as API server or CLI tool based on arguments.
 * 
 * Usage:
 *   java -jar HanziToAnki-1.0.0.jar --api        # Start API server
 *   java -jar HanziToAnki-1.0.0.jar input.txt    # Run CLI with file
 */
public class Launcher {
    public static void main(String[] args) throws URISyntaxException {
        if (args.length > 0 && args[0].equals("--api")) {
            // Start API server (no args passed to Spring Boot)
            String[] springArgs = new String[args.length - 1];
            System.arraycopy(args, 1, springArgs, 0, args.length - 1);
            Application.main(springArgs);
        } else if (args.length == 0) {
            // Default to API server if no args
            Application.main(args);
        } else {
            // Run CLI tool
            Main.main(args);
        }
    }
}
