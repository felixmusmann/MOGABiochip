package synthesis;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.runner.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;
import synthesis.model.DeviceLibrary;

import java.io.IOException;
import java.util.logging.Level;

public class NSGARunner extends AbstractAlgorithmRunner {

    public static void main(String[] args) {
        // Arguments: iterations population mutationRate minWidth minHeight app lib devices
        // Configuration
        int maxIterations = Integer.valueOf(args[0]);
        int populationSize = Integer.valueOf(args[1]); // must be an even number
        double mutationRate = Double.valueOf(args[2]);
        int minWidth = Integer.valueOf(args[3]);
        int minHeight = Integer.valueOf(args[4]);
        String pathToApp = args[5];
        String pathToLib = args[6];
        String pathToDevices = args[7];

        // Read graph and create library of devices
        DeviceLibrary deviceLibrary = JSONParser.readDeviceLibrary(pathToDevices);

        // Initialize logger
        try {
            LogTool.initializeLogger(Level.INFO, pathToApp);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem with creating log file: " + pathToApp);
        }
        LogTool.setInputFiles(pathToApp, pathToLib, pathToDevices);
        LogTool.setConfig(populationSize, maxIterations, mutationRate, minWidth, minHeight);

        // Define NSGA II
        Problem<BiochipSolution> problem = new SynthesisProblem(minWidth, minHeight, pathToApp, pathToLib, deviceLibrary);
        CrossoverOperator<BiochipSolution> crossover = new BiochipCrossover();
        MutationOperator<BiochipSolution> mutation = new BiochipMutation(mutationRate, deviceLibrary, null);

        NSGAII<BiochipSolution> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation)
                .setMaxIterations(maxIterations)
                .setPopulationSize(populationSize)
                .build();

        // Execute algorithm
        LogTool.setStartTime(System.currentTimeMillis());
        new AlgorithmRunner.Executor(algorithm).execute();
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
