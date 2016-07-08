package synthesis;

import synthesis.model.*;

import java.util.*;

public class BiochipRepairConnectivity {

    private Pair<Integer, Integer>[][] predecessors;
    private ArrayList<Cell> visitedCells;
    private ArrayList<Cell> unvisitedCells;

    private int fails; // number of times we are unable to find a path

    public BiochipRepairConnectivity() {
        visitedCells = new ArrayList<>();
        unvisitedCells = new ArrayList<>();
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
        visitedCells.clear();
        unvisitedCells.clear();
        unvisitedCells.addAll(solution.getElectrodes());

        // add startCells from devices
        for (Device device : solution.getDevices()) {
            unvisitedCells.add(device.getStartCell());
        }

        Electrode electrode = solution.getElectrodes().get(0);
        isConnected(solution, electrode);

        return unvisitedCells.size() == 0;
    }

    private void isConnected(BiochipSolution solution, Cell cell) {
        if (visitedCells.contains(cell)) {
            // we already visited this electrode and
            // therefore called this function on its neighbours
            return;
        }

        visitedCells.add(cell);
        unvisitedCells.remove(cell);

        int x = cell.getX();
        int y = cell.getY();

        // call function on all its neighbouring electrodes or startCells
        if (validNeighbour(solution, x, y, x, y - 1, false)) {
            isConnected(solution, solution.getCell(x, y - 1));
        }
        if (validNeighbour(solution, x, y, x, y + 1, false)) {
            isConnected(solution, solution.getCell(x, y + 1));
        }
        if (validNeighbour(solution, x, y, x - 1, y, false)) {
            isConnected(solution, solution.getCell(x - 1, y));
        }
        if (validNeighbour(solution, x, y, x + 1, y, false)) {
            isConnected(solution, solution.getCell(x + 1, y));
        }
    }

    private BiochipSolution repairConnectivity(BiochipSolution solution) {
        Random rnd = new Random();

        checkListsForDeviceCells();

        // get random pair of electrodes from both sets and connect them
        int rndVisitedIndex = rnd.nextInt(visitedCells.size());
        int rndUnvisitedIndex = rnd.nextInt(unvisitedCells.size());
        Cell visitedCellOne = visitedCells.get(rndVisitedIndex);
        Cell unvisitedCellOne = unvisitedCells.get(rndUnvisitedIndex);

        // use a second pair of electrodes to create circle
        rndVisitedIndex = rnd.nextInt(visitedCells.size());
        rndUnvisitedIndex = rnd.nextInt(unvisitedCells.size());
        Cell visitedCellTwo = visitedCells.get(rndVisitedIndex);
        Cell unvisitedCellTwo = unvisitedCells.get(rndUnvisitedIndex);

        // add empty border around chip so we can go around devices
        solution.addColumn(0);
        solution.addColumn(solution.getWidth());
        solution.addRow(0);
        solution.addRow(solution.getHeight());
        solution = buildPath(solution, visitedCellOne, unvisitedCellOne);
        // TODO: connection second pair of electrodes rarely creates circle
        solution = buildPath(solution, visitedCellTwo, unvisitedCellTwo);
        solution.shrink();

        return solution;
    }

    private void checkListsForDeviceCells() {
        ArrayList<DeviceCell> deviceCells = new ArrayList<>();
        for (Cell cell : visitedCells) {
            if (cell instanceof DeviceCell) {
                deviceCells.add((DeviceCell) cell);
            }
        }

        if (deviceCells.size() != visitedCells.size()) {
            // prefer electrodes for selection
            visitedCells.removeAll(deviceCells);
        }

        deviceCells.clear();
        for (Cell cell : unvisitedCells) {
            if (cell instanceof DeviceCell) {
                deviceCells.add((DeviceCell) cell);
            }
        }

        if (deviceCells.size() != unvisitedCells.size()) {
            // prefer electrodes for selection
            unvisitedCells.removeAll(deviceCells);
        }
    }

    private BiochipSolution buildPath(BiochipSolution solution, Cell source, Cell target) {
        predecessors = (Pair<Integer, Integer>[][]) new Pair[solution.getWidth()][solution.getHeight()];

        searchPath(solution, source.getX(), source.getY(), target.getX(), target.getY());

        int x = target.getX();
        int y = target.getY();
        Pair<Integer, Integer> predecessor;

        while (x != source.getX() || y != source.getY()) {
            predecessor = predecessors[x][y];
            if (predecessor == null) {
                // there is no path, one component must be surrounded by devices
                fails++;
                if (fails <= 2) {
                    // lets try to remove redundant devices first
                    BiochipMutation mutation = new BiochipMutation(0);
                    solution = mutation.mutate(solution, BiochipMutation.Type.REMOVE_DEVICE);
                } else {
                    // remove unreachable electrodes and devices
                    List<Cell> unreachableCells = visitedCells.size() < unvisitedCells.size() ? visitedCells : unvisitedCells;
                    for (Cell cell : unreachableCells) {
                        if (cell instanceof Electrode) {
                            solution.removeElectrode(cell.getX(), cell.getY());
                        } else {
                            solution.removeDevice(((DeviceCell) cell).getLinkedDevice());
                        }
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
                if (validNeighbour(solution, current.fst, current.snd, x, y, true) && predecessors[x][y] == null) {
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
     * Path from DeviceCell to DeviceCell is not possible.
     *
     * @param solution
     * @param fromX
     * @param fromY
     * @param targetX
     * @param targetY
     * @return true, if Cell can be used for building path
     */
    private boolean validNeighbour(BiochipSolution solution, int fromX, int fromY, int targetX, int targetY, boolean allowToNull) {
        Cell sourceCell = solution.getCell(fromX, fromY);
        Cell targetCell = solution.getCell(targetX, targetY);

        boolean inBounds = targetX < solution.getWidth() && targetX >= 0 && targetY < solution.getHeight() && targetY >= 0;

        if (!inBounds) {
            return false;
        } else if (allowToNull && targetCell == null) {
            return true;
        } else if (sourceCell == null || sourceCell instanceof Electrode) {
            if (targetCell instanceof DeviceCell && ((DeviceCell) targetCell).isStartCell()) {
                return true;
            } else if (targetCell instanceof Electrode) {
                return true;
            }
        }

        return false;
    }
}
