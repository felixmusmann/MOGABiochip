package synthesis;

import synthesis.model.Cell;
import synthesis.model.Electrode;

import java.util.Random;

public class BiochipHolePuncher {

    public BiochipSolution execute(BiochipSolution solution) {
        solution.shrink();
        Random rnd = new Random();
        int maxWidth = solution.getWidth() / 2;
        int maxHeight = solution.getHeight() / 2;

        int rndElectrodeIndex = rnd.nextInt(solution.getElectrodes().size());
        Electrode electrode = solution.getElectrodes().get(rndElectrodeIndex);
        int startX = electrode.getX();
        int startY = electrode.getY();
        int width = 1 + rnd.nextInt(maxWidth);
        int height = 1 + rnd.nextInt(maxHeight);
        int rightBound = width + startX;
        int lowerBound = height + startY;

        System.out.println(String.format("Punch hole: %dx%d from (%d, %d)", width, height, startX, startY));
        System.out.println("Before: \n" + solution);
        for (int x = startX; x <= rightBound && x < solution.getWidth(); x++) {
            for (int y = startY; y <= lowerBound && y < solution.getHeight(); y++) {
                Cell cell = solution.getCell(x, y);
                if (cell != null && cell instanceof Electrode) {
                    solution.removeElectrode(x, y);
                }
            }
        }

        System.out.println("After: \n" + solution);

        return solution;
    }
}
