package synthesis;

import synthesis.model.Cell;
import synthesis.model.Electrode;

import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Logger;

public class BiochipHolePuncher {

    private final static Logger LOGGER = Logger.getGlobal();

    private BiochipRepairConnectivity biochipConnectivity;

    public BiochipHolePuncher() {
        biochipConnectivity = new BiochipRepairConnectivity();
    }

    public BiochipSolution execute(BiochipSolution solution, boolean checkConnectivity) {
        Random rnd = new Random();
        double filling = (double) solution.getElectrodes().size() / (solution.getWidth() * solution.getHeight());
        int maxWidth = (int) Math.floor(solution.getWidth() * filling / 2);
        int maxHeight = (int) Math.floor(solution.getHeight() * filling / 2);

        int rndElectrodeIndex = rnd.nextInt(solution.getElectrodes().size());
        Electrode electrode = solution.getElectrodes().get(rndElectrodeIndex);
        int width = 2 + rnd.nextInt(maxWidth);
        int height = 2 + rnd.nextInt(maxHeight);
        int startX = electrode.getX();
        int startY = electrode.getY();
        int rightBound = width + startX;
        int lowerBound = height + startY;

        LOGGER.fine(String.format("Punch hole: %dx%d from (%d, %d)", width, height, startX, startY));
        for (int x = startX; x <= rightBound && x < solution.getWidth(); x++) {
            for (int y = startY; y <= lowerBound && y < solution.getHeight(); y++) {
                Cell cell = solution.getCell(x, y);
                if (cell != null && cell instanceof Electrode) {
                    solution.removeElectrode(x, y);
                    if (checkConnectivity && !biochipConnectivity.isConnected(solution)) {
                        // oh no, we destroyed the biochip
                        solution.addElectrode(x, y);
                    }
                }
            }
        }

        // remove blank space around
        solution.shrink();

        return solution;
    }

    private void labelElectrodes(BiochipSolution solution, int radius, int sourceX, int sourceY) {
        LinkedList<Pair<Integer, Integer>> queue = new LinkedList<>();
        int[] deltaX = {1, -1, 0, 0};
        int[] deltaY = {0, 0, 1, -1};
        int[][] labels = new int[solution.getWidth()][solution.getHeight()];

        labels[sourceX][sourceY] = 1;
        queue.add(new Pair<>(sourceX, sourceY));
        Pair<Integer, Integer> current;

        while (!queue.isEmpty()) {
            current = queue.poll();
            int labelOfCurrent = labels[current.fst][current.snd];

            if (labelOfCurrent == radius) {
                break;
            }

            for (int i = 0; i < deltaX.length; i++) {
                // go to all neighbors
                int x = current.fst + deltaX[i];
                int y = current.snd + deltaY[i];
                if (validNeighbour(solution, x, y) && labels[x][y] == 0) {
                    // set label of neighbors
                    labels[x][y] = labelOfCurrent + 1;
                    queue.add(new Pair<>(x, y));
                }
            }
        }
    }

    private boolean validNeighbour(BiochipSolution solution, int x, int y) {
        Cell cell = solution.getCell(x, y);
        boolean inBounds = x < solution.getWidth() && x >= 0 && y < solution.getHeight() && y >= 0;
        boolean isElectrodeOrNull = cell == null || cell instanceof Electrode;
        return inBounds && isElectrodeOrNull;
    }
}
