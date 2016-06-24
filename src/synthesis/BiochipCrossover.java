package synthesis;

import org.uma.jmetal.operator.CrossoverOperator;
import synthesis.model.Biochip;
import synthesis.model.Electrode;

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

            int electrodeIndex = rnd.nextInt(biochip.getElectrodes().size());
            Electrode electrode = biochip.getElectrodes().get(electrodeIndex);

            decider = rnd.nextDouble();
            if (decider < 0.5) {
                int column = electrode.getX();
                splitBiochips.add(biochip.splitAtColumn(column));
            } else {
                int row = electrode.getY();
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
