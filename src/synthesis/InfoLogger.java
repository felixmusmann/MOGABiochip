package synthesis;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class InfoLogger {
    private static FileHandler txtFile;
    private static SimpleFormatter txtFormatter;

    private static int solutionCount;

    public static void initialize(String logFile) throws IOException {
        Logger logger = Logger.getGlobal();

        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (Handler handler : handlers) {
            logger.removeHandler(handler);
        }

        logger.setLevel(Level.INFO);

        int count = 0;
        String fileName = logFile + ".%d.log";
        File file;
        do  {
            count++;
            file = new File(String.format(fileName, count));
        } while (file.exists());

        txtFile = new FileHandler(fileName);
        txtFormatter = new SimpleFormatter();

        txtFile.setFormatter(txtFormatter);
        logger.addHandler(txtFile);
    }

    public static void incrementSolutions(int value) {
        if (value > 0) {
            solutionCount += value;
        }
    }

    public static int getSolutionCount() {
        return solutionCount;
    }
}
