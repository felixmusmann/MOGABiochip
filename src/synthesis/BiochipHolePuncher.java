package synthesis;

import synthesis.model.Cell;
import synthesis.model.Electrode;

import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Logger;

public class BiochipHolePuncher {

    private final static Logger LOGGER = Logger.getGlobal();

    private int tries;

    public BiochipHolePuncher() {
        tries = 0;
    }

    public BiochipSolution execute(BiochipSolution solution, boolean checkConnectivity) {
        Random rnd = new Random();
        double filling = (double) solution.getElectrodes().size() / (solution.getWidth() * solution.getHeight());
        int maxWidth = (int) Math.floor(solution.getWidth() * filling / 2);
        int maxHeight = (int) Math.floor(solution.getHeight() * filling / 2);

        int rndElectrodeIndex = rnd.nextInt(solution.getElectrodes().size());
        Electrode electrode = solution.getElectrodes().get(rndElectrodeIndex);
        int startX = electrode.getX();
        int startY = electrode.getY();
        int radius = Math.max(maxHeight, maxWidth);
        boolean[][] removableElectrodes;

        if (tries > 10) {
            // we tried, lets remove all that are not necessary
            removableElectrodes = new boolean[solution.getWidth()][solution.getHeight()];
            for (Electrode electrodeI : solution.getElectrodes()) {
                int x = electrodeI.getX();
                int y = electrodeI.getY();
                removableElectrodes[x][y] = removableNeighbour(solution, x, y);
            }
        } else {
            removableElectrodes = labelElectrodes(solution, radius, startX, startY);
        }

        for (int x = 0; x < solution.getWidth(); x++) {
            for (int y = 0; y < solution.getHeight(); y++) {
                if (removableElectrodes[x][y]) {
                    solution.removeElectrode(x, y);
                }
            }
        }

//        LOGGER.fine(String.format("Punch hole: %dx%d from (%d, %d)", width, height, startX, startY));
//        for (int x = startX; x <= rightBound && x < solution.getWidth(); x++) {
//            for (int y = startY; y <= lowerBound && y < solution.getHeight(); y++) {
//                Cell cell = solution.getCell(x, y);
//                if (cell != null && cell instanceof Electrode) {
//                    solution.removeElectrode(x, y);
//                    if (checkConnectivity && !biochipConnectivity.isConnected(solution)) {
//                        // oh no, we destroyed the biochip
//                        solution.addElectrode(x, y);
//                    }
//                }
//            }
//        }

        // remove blank space around
        solution.shrink();
        tries++;

        return solution;
    }

    private boolean[][] labelElectrodes(BiochipSolution solution, int radius, int sourceX, int sourceY) {
        LinkedList<Pair<Integer, Integer>> queue = new LinkedList<>();
        boolean[][] removableElectrodes = new boolean[solution.getWidth()][solution.getHeight()];
        int[][] labels = new int[solution.getWidth()][solution.getHeight()];
        int[] deltaX = {-1, 1, -1, 1, 1, -1, 0, 0};
        int[] deltaY = {1, -1, -1, 1, 0, 0, 1, -1};

        if (tries <= 10 && !removableNeighbour(solution, sourceX, sourceY)) {
            return removableElectrodes;
        }

        labels[sourceX][sourceY] = 1;
        removableElectrodes[sourceX][sourceY] = removableNeighbour(solution, sourceX, sourceY);
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
                if (removableNeighbour(solution, x, y) && labels[x][y] == 0) {
                    // set label of neighbors
                    labels[x][y] = labelOfCurrent + 1;
                    removableElectrodes[x][y] = true;
                    queue.add(new Pair<>(x, y));
                }
            }
        }

        return removableElectrodes;
    }

    private boolean removableNeighbour(BiochipSolution solution, int x, int y) {
        boolean removable = true;
        int[] deltaX = {0, -1, 1, -1, 1, 1, -1, 0, 0};
        int[] deltaY = {0, 1, -1, -1, 1, 0, 0, 1, -1};

        for (int i = 0; i < deltaX.length; i++) {
            int curX = x + deltaX[i];
            int curY = y + deltaY[i];
            Cell cell = solution.getCell(curX, curY);
            boolean inBounds = curX < solution.getWidth() && curX >= 0 && curY < solution.getHeight() && curY >= 0;
            boolean isElectrode = cell != null && cell instanceof Electrode;

            if (!(inBounds && isElectrode)) {
                removable = false;
                break;
            }
        }

        return removable;
    }
}
