package synthesis;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.runner.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class NSGARunner extends AbstractAlgorithmRunner {

    private final static Logger LOGGER = Logger.getGlobal();

    public static void main(String[] args) {
        // Configuration
        String pathToApp = "input/graph10.txt";
        String pathToLib = "input/TS_lib.txt";
        String pathToDevices = "input/devices.json";
        int minWidth = 5;
        int minHeight = 5;
        double mutationRate = 0.7;
        int maxIterations = 100;
        int populationSize = 40; // must be an even number

        // Read graph and create library of devices
        DeviceLibrary deviceLibrary = JSONParser.readDeviceLibrary(pathToDevices);

        // Initialize logger
        try {
            InfoLogger.initialize(pathToApp);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem with creating log file: " + pathToApp);
        }

        // Define NSGA II
        Problem<BiochipSolution> problem = new SynthesisProblem(minWidth, minHeight, pathToApp, pathToLib, deviceLibrary);
        CrossoverOperator<BiochipSolution> crossover = new BiochipCrossover();
        MutationOperator<BiochipSolution> mutation = new BiochipMutation(mutationRate, null, null);

        NSGAII<BiochipSolution> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation)
                .setMaxIterations(maxIterations)
                .setPopulationSize(populationSize)
                .build();

        // Execute algorithm
        LOGGER.info("Starting algorithm for " + maxIterations + " iterations with population of " + populationSize);
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

        // Get results and print solutions
        List<BiochipSolution> population = algorithm.getResult();
        long computingTime = algorithmRunner.getComputingTime();

        String conclusion = "Total computing time: " + TimeUnit.MILLISECONDS.toMinutes(computingTime) + " min " + TimeUnit.MILLISECONDS.toSeconds(computingTime) + " s" +
                "\nGenerated " + InfoLogger.getSolutionCount() + " biochips." +
                "\nFound " + population.size() + " non-dominated solutions.";
        LOGGER.info(conclusion);

        for (BiochipSolution solution : population) {
            System.out.println(solution);
        }
    }

    private static void saveResults(List<BiochipSolution> population, long computingTime) {
        // TODO: implement saveResults
    }
}
