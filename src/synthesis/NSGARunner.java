package synthesis;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.runner.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSGARunner extends AbstractAlgorithmRunner {

    public static void main(String[] args) {
        // Configuration
        String pathToApp = "data/input/graph10.txt";
        String pathToLib = "data/input/TS_lib.txt";
        String pathToDevices = "data/input/devices.json";
        int minWidth = 5;
        int minHeight = 5;
        double mutationRate = 0.7;
        int maxIterations = 10;
        int populationSize = 20; // must be an even number

        // Read graph and create library of devices
        DeviceLibrary deviceLibrary = JSONParser.readDeviceLibrary(pathToDevices);

        // Initialize logger
        try {
            LogTool.initializeLogger(Level.WARNING, pathToApp);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem with creating log file: " + pathToApp);
        }
        LogTool.setInputFiles(pathToApp, pathToLib, pathToDevices);
        LogTool.setConfig(populationSize, maxIterations, mutationRate, minWidth, minHeight);

        // Define NSGA II
        Problem<BiochipSolution> problem = new SynthesisProblem(minWidth, minHeight, pathToApp, pathToLib, deviceLibrary);
        CrossoverOperator<BiochipSolution> crossover = new BiochipCrossover();
        MutationOperator<BiochipSolution> mutation = new BiochipMutation(mutationRate, null, null);

        NSGAII<BiochipSolution> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation)
                .setMaxIterations(maxIterations)
                .setPopulationSize(populationSize)
                .build();

        // Execute algorithm
        LogTool.setStartTime(System.currentTimeMillis());
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        LogTool.setEndTime(System.currentTimeMillis());

        // Save results
        LogTool.setSolutions(algorithm.getResult());
        try {
            LogTool.saveResults();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
