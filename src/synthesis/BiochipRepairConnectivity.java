package synthesis;

import synthesis.model.Device;
import synthesis.model.DeviceCell;
import synthesis.model.Electrode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiochipRepairConnectivity {

    private static final int MAX_TRIES = 6; // number of tries to find path before deleting (probably redundant) devices on path
    private int tries;

    private ArrayList<Electrode> visitedElectrodes;
    private ArrayList<Electrode> unvisitedElectrodes;

    public BiochipRepairConnectivity() {
        tries = 0;
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
            solution = execute(solution);
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

        Electrode source = visitedElectrode;
        Electrode target = unvisitedElectrode;

        if (isDeviceOnPath(solution, source, target)) {
            // try different path from target to source
            source = unvisitedElectrode;
            target = visitedElectrode;
        }

        if (isDeviceOnPath(solution, source, target)) {
            // try with new electrodes
            return repairConnectivity(solution);
        }

        return connectElectrodes(solution, source, target);
    }

    private boolean isDeviceOnPath(BiochipSolution solution, Electrode source, Electrode target) {
        tries++;
        int x = source.getX();
        int y = source.getY();

        // Check for devices on path
        while (target.getX() - x != 0) {
            if (solution.getCell(x, y) != null && solution.getCell(x, y) instanceof DeviceCell) {
                if (tries > MAX_TRIES) {
                    Device device = ((DeviceCell) solution.getCell(x, y)).getLinkedDevice();
                    solution.removeDevice(device);
                } else {
                    return true;
                }
            }

            if (target.getX() > x) {
                x++;
            } else {
                x--;
            }
        }

        while (target.getY() - y != 0) {
            if (solution.getCell(x, y) != null && solution.getCell(x, y) instanceof DeviceCell) {
                if (tries > MAX_TRIES) {
                    Device device = ((DeviceCell) solution.getCell(x, y)).getLinkedDevice();
                    solution.removeDevice(device);
                } else {
                    return true;
                }
            }

            if (target.getY() > y) {
                y++;
            } else {
                y--;
            }
        }

        return false;
    }

    private BiochipSolution connectElectrodes(BiochipSolution solution, Electrode source, Electrode target) {
        int x = source.getX();
        int y = source.getY();

        // Add path of electrodes
        while (target.getX() - x != 0) {
            if (solution.getCell(x, y) == null) {
                solution.addElectrode(x, y);
            }

            if (target.getX() > x) {
                x++;
            } else {
                x--;
            }
        }

        while (target.getY() - y != 0) {
            if (solution.getCell(x, y) == null) {
                solution.addElectrode(x, y);
            }

            if (target.getY() > y) {
                y++;
            } else {
                y--;
            }
        }

        return solution;
    }
}
