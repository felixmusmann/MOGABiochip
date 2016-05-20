import org.uma.jmetal.operator.MutationOperator;

public class BiochipMutation implements MutationOperator<BiochipSolution> {

    @Override
    public BiochipSolution execute(BiochipSolution biochipSolution) {
        // TODO: Add devices
        Biochip neighbor = biochipSolution.generateNeighbor();
        return new BiochipSolution(neighbor);
    }
}
