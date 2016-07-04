package synthesis;

import synthesis.model.*;

import java.util.*;

public class BiochipRepairConnectivity {

    private Pair<Integer, Integer>[][] predecessors;
    private ArrayList<Electrode> visitedElectrodes;
    private ArrayList<Electrode> unvisitedElectrodes;

    public BiochipRepairConnectivity() {
        visitedElectrodes = new ArrayList<>();
        unvisitedElectrodes = new ArrayList<>();
    }

    public BiochipSolution execute(BiochipSolution solution) {
        // TODO: connectivity only checks electrodes not devices
        if (solution.getElectrodes().size() < 2) {
            return solution;
        }

        if (!isConnected(solution)) {
            solution = repairConnectivity(solution);
            solution = execute(solution); // repeat if there are still multiple components
        }

        return solution;
    }

    public boolean isConnected(BiochipSolution solution) {
        visitedElectrodes.clear();
        unvisitedElectrodes.clear();
        unvisitedElectrodes.addAll(solution.getElectrodes());

        Electrode electrode = solution.getElectrodes().get(0);
        checkConnectivity(solution, electrode);

        return unvisitedElectrodes.size() == 0;
    }

    private void checkConnectivity(BiochipSolution solution, Electrode electrode) {
        if (visitedElectrodes.contains(electrode)) {
            return;
        }

        visitedElectrodes.add(electrode);
        unvisitedElectrodes.remove(electrode);
        int x = electrode.getX();
        int y = electrode.getY();

        if (solution.getCell(x, y - 1) != null && solution.getCell(x, y - 1) instanceof Electrode) {
            checkConnectivity(solution, (Electrode) solution.getCell(x, y - 1));
        }
        if (solution.getCell(x, y + 1) != null && solution.getCell(x, y + 1) instanceof Electrode) {
            checkConnectivity(solution, (Electrode) solution.getCell(x, y + 1));
        }
        if (solution.getCell(x - 1, y) != null && solution.getCell(x - 1, y) instanceof Electrode) {
            checkConnectivity(solution, (Electrode) solution.getCell(x - 1, y));
        }
        if (solution.getCell(x + 1, y) != null && solution.getCell(x + 1, y) instanceof Electrode) {
            checkConnectivity(solution, (Electrode) solution.getCell(x + 1, y));
        }
    }

    private BiochipSolution repairConnectivity(BiochipSolution solution) {
        Random rnd = new Random();
        int rndVisitedIndex = rnd.nextInt(visitedElectrodes.size());
        int rndUnvisitedIndex = rnd.nextInt(unvisitedElectrodes.size());
        Electrode visitedElectrode = visitedElectrodes.get(rndVisitedIndex);
        Electrode unvisitedElectrode = unvisitedElectrodes.get(rndUnvisitedIndex);

        // add buffer around chip so we can go around devices
        solution.addColumn(0);
        solution.addColumn(solution.getWidth());
        solution.addRow(0);
        solution.addRow(solution.getHeight());
        solution = buildPath(solution, visitedElectrode, unvisitedElectrode);
        solution.shrink();

        return solution;
    }

    private BiochipSolution buildPath(BiochipSolution solution, Electrode source, Electrode target) {
        predecessors = (Pair<Integer, Integer>[][]) new Pair[solution.getWidth()][solution.getHeight()];

        searchPath(solution, source.getX(), source.getY(), target.getX(), target.getY());

        int x = target.getX();
        int y = target.getY();
        Pair<Integer, Integer> predecessor;

        while (x != source.getX() || y != source.getY()) {
            predecessor = predecessors[x][y];
            if (predecessor == null) {
                System.out.println("That shouldn't happen!");
            }
            x = predecessor.fst;
            y = predecessor.snd;
            solution.addElectrode(x, y);
        }

        return solution;
    }

    private void searchPath(BiochipSolution solution, int sourceX, int sourceY, int targetX, int targetY) {
        LinkedList<Pair<Integer, Integer>> queue = new LinkedList<>();
        int[] deltaX = {1, -1, 0, 0};
        int[] deltaY = {0, 0, 1, -1};

        queue.add(new Pair<>(sourceX, sourceY));
        Pair<Integer, Integer> current;

        bfs:
        while (!queue.isEmpty()) {
            current = queue.poll();
            for (int i = 0; i < deltaX.length; i++) {
                // go to all neighbors
                int x = current.fst + deltaX[i];
                int y = current.snd + deltaY[i];
                if (validNeighbour(solution, x, y) && predecessors[x][y] == null) {
                    // set predecessor of neighbor
                    predecessors[x][y] = current;
                    queue.add(new Pair<>(x, y));
                    if (x == targetX && y == targetY) {
                        // we reached our target
                        break bfs;
                    }
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
