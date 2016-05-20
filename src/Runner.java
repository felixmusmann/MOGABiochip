import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.TournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.runner.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.List;

public class Runner extends AbstractAlgorithmRunner {

    public static void main(String[] args) {
        Problem<BiochipSolution> problem;
        Algorithm<List<BiochipSolution>> algorithm;
        CrossoverOperator<BiochipSolution> crossover;
        MutationOperator<BiochipSolution> mutation;
        SelectionOperator<List<BiochipSolution>, BiochipSolution> selection;

        problem = new SynthesisProblem();
        crossover = new BiochipCrossover();
        mutation = new BiochipMutation();
        selection = new TournamentSelection<>(new RankingAndCrowdingDistanceComparator<>(), 50);

        algorithm = new NSGAIIBuilder<>(problem, crossover, mutation)
                .setSelectionOperator(selection)
                .setMaxIterations(10)
                .setPopulationSize(100)
                .build();

        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

        List<BiochipSolution> population = algorithm.getResult();
        long computingTime = algorithmRunner.getComputingTime();

        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
        JMetalLogger.logger.info("Population size: " + population.size());

        for (int i = 0; i < 5 && i < population.size(); i++) {
            System.out.println(population.get(i));
        }
    }
}
