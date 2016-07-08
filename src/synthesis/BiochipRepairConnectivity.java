package synthesis;

import synthesis.model.*;

import java.util.*;

public class BiochipRepairConnectivity {

    private Pair<Integer, Integer>[][] predecessors;
    private ArrayList<Electrode> visitedElectrodes;
    private ArrayList<Electrode> unvisitedElectrodes;

    private int fails; // number of times we are unable to find a path

    public BiochipRepairConnectivity() {
        visitedElectrodes = new ArrayList<>();
        unvisitedElectrodes = new ArrayList<>();
        fails = 0;
    }

    public BiochipSolution execute(BiochipSolution solution) {
        if (solution.getElectrodes().size() < 2) {
            // well, if there is only one electrode...
            return solution;
        }

        fails = 0;

        // repeat as long as there are multiple components
        while (!isConnected(solution)) {
            solution = repairConnectivity(solution);
        }

        return solution;
    }

    public boolean isConnected(BiochipSolution solution) {
        visitedElectrodes.clear();
        unvisitedElectrodes.clear();
        unvisitedElectrodes.addAll(solution.getElectrodes());

        Electrode electrode = solution.getElectrodes().get(0);
        isConnected(solution, electrode);

        return unvisitedElectrodes.size() == 0;
    }

    private void isConnected(BiochipSolution solution, Electrode electrode) {
        if (visitedElectrodes.contains(electrode)) {
            // we already visited this electrode and
            // therefore called this function on its neighbours
            return;
        }

        visitedElectrodes.add(electrode);
        unvisitedElectrodes.remove(electrode);

        int x = electrode.getX();
        int y = electrode.getY();

        // call function on all its neighbouring electrodes
        if (solution.getCell(x, y - 1) != null && solution.getCell(x, y - 1) instanceof Electrode) {
            isConnected(solution, (Electrode) solution.getCell(x, y - 1));
        }
        if (solution.getCell(x, y + 1) != null && solution.getCell(x, y + 1) instanceof Electrode) {
            isConnected(solution, (Electrode) solution.getCell(x, y + 1));
        }
        if (solution.getCell(x - 1, y) != null && solution.getCell(x - 1, y) instanceof Electrode) {
            isConnected(solution, (Electrode) solution.getCell(x - 1, y));
        }
        if (solution.getCell(x + 1, y) != null && solution.getCell(x + 1, y) instanceof Electrode) {
            isConnected(solution, (Electrode) solution.getCell(x + 1, y));
        }
    }

    private BiochipSolution repairConnectivity(BiochipSolution solution) {
        Random rnd = new Random();

        // get random pair of electrodes from both sets and connect them
        int rndVisitedIndex = rnd.nextInt(visitedElectrodes.size());
        int rndUnvisitedIndex = rnd.nextInt(unvisitedElectrodes.size());
        Electrode visitedElectrodeOne = visitedElectrodes.get(rndVisitedIndex);
        Electrode unvisitedElectrodeOne = unvisitedElectrodes.get(rndUnvisitedIndex);

        // use a second pair of electrodes to create circle
        rndVisitedIndex = rnd.nextInt(visitedElectrodes.size());
        rndUnvisitedIndex = rnd.nextInt(unvisitedElectrodes.size());
        Electrode visitedElectrodeTwo = visitedElectrodes.get(rndVisitedIndex);
        Electrode unvisitedElectrodeTwo = unvisitedElectrodes.get(rndUnvisitedIndex);

        // add empty border around chip so we can go around devices
        solution.addColumn(0);
        solution.addColumn(solution.getWidth());
        solution.addRow(0);
        solution.addRow(solution.getHeight());
        solution = buildPath(solution, visitedElectrodeOne, unvisitedElectrodeOne);
        // TODO: connection second pair of electrodes rarely creates circle
        solution = buildPath(solution, visitedElectrodeTwo, unvisitedElectrodeTwo);
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
                // one component must be surrounded by devices
                fails++;
                if (fails <= 2) {
                    // lets try to remove redundant devices first
                    BiochipMutation mutation = new BiochipMutation(0);
                    solution = mutation.mutate(solution, BiochipMutation.Type.REMOVE_DEVICE);
                } else {
                    // remove unreachable electrodes
                    List<Electrode> unreachableElectrodes = visitedElectrodes.size() < unvisitedElectrodes.size() ? visitedElectrodes : unvisitedElectrodes;
                    for (Electrode electrode : unreachableElectrodes) {
                        solution.removeElectrode(electrode.getX(), electrode.getY());
                    }
                }
                break;
            } else {
                x = predecessor.fst;
                y = predecessor.snd;
                solution.addElectrode(x, y);
            }
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

    /**
     * This method checks if the specified cell can be used for building a path.
     *
     * @param solution biochip to perform check on
     * @param x column of electrode
     * @param y row of electrode
     * @return true, if cell is in bounds and free or an electrode
     */
    private boolean validNeighbour(BiochipSolution solution, int x, int y) {
        Cell cell = solution.getCell(x, y);
        boolean inBounds = x < solution.getWidth() && x >= 0 && y < solution.getHeight() && y >= 0;
        boolean isElectrodeOrNull = cell == null || cell instanceof Electrode;
        return inBounds && isElectrodeOrNull;
    }
}
