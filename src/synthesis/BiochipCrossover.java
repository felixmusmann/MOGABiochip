package synthesis;

import org.uma.jmetal.operator.CrossoverOperator;
import synthesis.model.Biochip;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiochipCrossover implements CrossoverOperator<BiochipSolution> {

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

        for (int i = 0; i < splitBiochips.size() - 1; i++) {
            int rowFirst = rnd.nextInt(splitBiochips.get(i)[0].getHeight());
            int rowSecond = rnd.nextInt(splitBiochips.get(i+1)[1].getHeight());
            BiochipSolution mergedBiochip = new BiochipSolution(Biochip.mergeHorizontal(splitBiochips.get(i)[0], splitBiochips.get(i+1)[1], rowFirst, rowSecond));
            offspring.add(mergedBiochip);

            int columnFirst = rnd.nextInt(splitBiochips.get(i)[0].getWidth());
            int columnSecond = rnd.nextInt(splitBiochips.get(i+1)[1].getWidth());
            mergedBiochip = new BiochipSolution(Biochip.mergeVertical(splitBiochips.get(i)[0], splitBiochips.get(i+1)[1], columnFirst, columnSecond));
            offspring.add(mergedBiochip);
            InfoLogger.incrementSolutions(2);
        }

        return offspring;
    }
}
