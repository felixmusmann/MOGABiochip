import org.uma.jmetal.operator.CrossoverOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiochipCrossover implements CrossoverOperator<BiochipSolution> {

    @Override
    public List<BiochipSolution> execute(List<BiochipSolution> biochips) {
        ArrayList<BiochipSolution> offspring = new ArrayList<>();
        //offspring.add(biochips.get(0).copy());
        //offspring.add(biochips.get(1).copy());

        Random rnd = new Random();

        ArrayList<Biochip[]> splitBiochips = new ArrayList<>();
        for (Biochip biochip : biochips) {
            if (rnd.nextDouble() >= 0.5) {
                int column = (int) (rnd.nextDouble() * biochip.getWidth());
                splitBiochips.add(biochip.splitAtColumn(column));
            } else {
                int row = (int) (rnd.nextDouble() * biochip.getHeight());
                splitBiochips.add(biochip.splitAtRow(row));
            }
        }

        for (int i = 0; i < splitBiochips.size() - 1; i++) {
            int rowFirst = (int) (rnd.nextDouble() * splitBiochips.get(i)[0].getHeight());
            int rowSecond = (int) (rnd.nextDouble() * splitBiochips.get(i+1)[1].getHeight());
            BiochipSolution mergedBiochip = new BiochipSolution(Biochip.mergeHorizontal(splitBiochips.get(i)[0], splitBiochips.get(i+1)[1], rowFirst, rowSecond));
            offspring.add(mergedBiochip);

            int columnFirst = (int) (rnd.nextDouble() * splitBiochips.get(i)[0].getWidth());
            int columnSecond = (int) (rnd.nextDouble() * splitBiochips.get(i+1)[1].getWidth());
            mergedBiochip = new BiochipSolution(Biochip.mergeVertical(splitBiochips.get(i)[0], splitBiochips.get(i+1)[1], columnFirst, columnSecond));
            offspring.add(mergedBiochip);
        }

        return offspring;
    }
}
