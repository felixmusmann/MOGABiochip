package synthesis;

import org.uma.jmetal.operator.CrossoverOperator;
import synthesis.model.Biochip;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class BiochipCrossover implements CrossoverOperator<BiochipSolution> {

    private static final Logger LOGGER = Logger.getGlobal();

    @Override
    public List<BiochipSolution> execute(List<BiochipSolution> biochips) {
        ArrayList<BiochipSolution> offspring = new ArrayList<>();
        Random rnd = new Random();

        ArrayList<Biochip[]> splitBiochips = new ArrayList<>();
        for (Biochip biochip : biochips) {
            double decider;
            biochip.shrink();

            if (biochip.getWidth() < 2) {
                if (biochip.getHeight() < 2) {
                    LOGGER.severe("Biochip too small to split.");
                    continue;
                } else {
                    decider = 1;
                }
            } else if (biochip.getHeight() < 2) {
                decider = 0;
            } else {
                decider = rnd.nextDouble();
            }

            if (decider < 0.5) {
                int column = 1 + rnd.nextInt(biochip.getWidth() - 1);
                splitBiochips.add(biochip.splitAtColumn(column));
            } else {
                int row = 1 + rnd.nextInt(biochip.getHeight() - 1);
                splitBiochips.add(biochip.splitAtRow(row));
            }
        }

        // Do crossovers
        int decider;
        Biochip first, second;

        // First crossover horizontal
        decider = rnd.nextInt(2);
        first = splitBiochips.get(0)[decider];
        second = splitBiochips.get(1)[1 - decider];
        int rowFirst = rnd.nextInt(first.getHeight());
        int rowSecond = rnd.nextInt(second.getHeight());
        Biochip mergedBiochip = Biochip.mergeHorizontal(first, second, rowFirst, rowSecond);
        offspring.add(new BiochipSolution(mergedBiochip));

        // Second crossover vertical
        decider = rnd.nextInt(2);
        first = splitBiochips.get(0)[1 - decider];
        second = splitBiochips.get(1)[decider];
        int columnFirst = rnd.nextInt(first.getWidth());
        int columnSecond = rnd.nextInt(second.getWidth());
        mergedBiochip = Biochip.mergeVertical(first, second, columnFirst, columnSecond);
        offspring.add(new BiochipSolution(mergedBiochip));

        LogTool.incrementGeneratedArchitectures(2);
        return offspring;
    }
}
