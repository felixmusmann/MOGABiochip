import org.uma.jmetal.problem.Problem;

import java.util.Random;

public class SynthesisProblem implements Problem<BiochipSolution> {

    @Override
    public int getNumberOfVariables() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return 2;
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    @Override
    public String getName() {
        return "Biochip Synthesis Problem";
    }

    @Override
    public void evaluate(BiochipSolution solution) {
        solution.setObjective(0, solution.getCost());
        solution.setObjective(1, solution.getExecutionTime());
    }

    @Override
    public BiochipSolution createSolution() {
        Random rnd = new Random();
        // TODO: more sophisticated approach?
        int width = 10 + rnd.nextInt(91);
        int height = 10 + rnd.nextInt(91);

        return new BiochipSolution(width, height, null, null);
    }
}
