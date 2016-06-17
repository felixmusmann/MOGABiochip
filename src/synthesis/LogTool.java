package synthesis;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

public class LogTool {
    private static FileHandler txtFile;
    private static SimpleFormatter txtFormatter;

    private static long startTime;
    private static long endTime;

    // timer stuff
    private static ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    private static long timer;

    // input files
    private static String graphFile;
    private static String libraryFile;
    private static String devicesFile;

    // config parameters
    private static int populationSize;
    private static int maxIterations;
    private static double mutationRate;
    private static int minWidth;
    private static int minHeight;

    // solutions
    private static int generatedArchitecturesCount;
    private static BiochipSolution[] solutions;

    public static void initializeLogger(Level logLevel, String logFile) throws IOException {
        final String path = "data/logs/";
        Logger logger = Logger.getGlobal();
        logger.setLevel(logLevel);
        logger.setUseParentHandlers(false);

        String filename = logFile + ".%d.log";
        int indexOfLastSlash = filename.lastIndexOf("/");
        if (indexOfLastSlash != -1) {
            filename = filename.substring(indexOfLastSlash + 1);
        }

        File file;
        int count = 0;
        do  {
            count++;
            file = new File(path + String.format(filename, count));
        } while (file.exists());

        txtFile = new FileHandler(path + String.format(filename, count));
        txtFormatter = new SimpleFormatter();

        txtFile.setFormatter(txtFormatter);
        logger.addHandler(txtFile);
    }

    public static void saveResults() throws IOException {
        final String path = "data/results/";
        String filename = graphFile + "_results.json";
        int indexOfLastSlash = filename.lastIndexOf("/");
        if (indexOfLastSlash != -1) {
            filename = filename.substring(indexOfLastSlash + 1);
        }

        JsonObject sessionResults = new JsonObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        Date date = new Date(startTime);
        sessionResults.addProperty("start-time", sdf.format(date));
        long seconds = (endTime - startTime) / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        String time = days + ":" + hours % 24 + ":" + minutes % 60 + ":" + seconds % 60;
        sessionResults.addProperty("total-time", time);

        JsonObject input = new JsonObject();
        input.addProperty("graph", graphFile);
        input.addProperty("library", libraryFile);
        input.addProperty("device", devicesFile);
        sessionResults.add("input", input);

        JsonObject config = new JsonObject();
        config.addProperty("population-size", populationSize);
        config.addProperty("max-iterations", maxIterations);
        config.addProperty("mutation-rate", mutationRate);
        config.addProperty("min-width", minWidth);
        config.addProperty("min-height", minHeight);
        sessionResults.add("config", config);

        sessionResults.addProperty("generated-architectures-count", generatedArchitecturesCount);
        sessionResults.addProperty("result-count", solutions.length);

        JsonArray results = new JsonArray();
        for (BiochipSolution solution : solutions) {
            JsonObject result = new JsonObject();
            result.addProperty("cost", solution.getObjective(0));
            result.addProperty("execution-time", solution.getObjective(1));
            result.addProperty("architecture", solution.toString());
            results.add(result);
        }
        sessionResults.add("results", results);


        File file = new File(path + filename);
        JsonArray content = new JsonArray();
        if (file.exists()) {
            JsonParser parser = new JsonParser();
            JsonElement previousContent = parser.parse(new FileReader(file));
            content = previousContent.getAsJsonArray();
        }
        content.add(sessionResults);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter writer = new FileWriter(file);
        writer.write(gson.toJson(content));
        writer.close();
    }

    public static void incrementGeneratedArchitectures(int value) {
        if (value > 0) {
            generatedArchitecturesCount += value;
        }
    }

    public static void setStartTime(long startTime) {
        LogTool.startTime = startTime;
    }

    public static void setEndTime(long endTime) {
        LogTool.endTime = endTime;
    }

    public static void setInputFiles(String graphFile, String libraryFile, String devicesFile) {
        LogTool.graphFile = graphFile;
        LogTool.libraryFile = libraryFile;
        LogTool.devicesFile = devicesFile;
    }

    public static void setConfig(int populationSize, int maxIterations, double mutationRate, int minWidth, int minHeight) {
        LogTool.populationSize = populationSize;
        LogTool.maxIterations = maxIterations;
        LogTool.mutationRate = mutationRate;
        LogTool.minWidth = minWidth;
        LogTool.minHeight = minHeight;
    }

    public static void setSolutions(List<BiochipSolution> solutions) {
        LogTool.solutions = new BiochipSolution[solutions.size()];
        solutions.toArray(LogTool.solutions);
        // sort by cost in ascending order
        Arrays.sort(LogTool.solutions, new Comparator<BiochipSolution>() {
            @Override
            public int compare(BiochipSolution o1, BiochipSolution o2) {
                if (o1.getObjective(0) < o2.getObjective(0)) {
                    return -1;
                } else if (o1.getObjective(0) > o2.getObjective(0)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    public static void startTimer() {
        timer = mxBean.getCurrentThreadCpuTime();
    }

    public static long getTimerMillis() {
        return (mxBean.getCurrentThreadCpuTime() - timer) / 1000000;
    }
}
