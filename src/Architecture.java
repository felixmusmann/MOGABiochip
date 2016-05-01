import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class Architecture {

    private ArrayList<ArrayList<Electrode>> electrodeGrid;
    private ArrayList<Electrode> activeElectrodes;
    private ArrayList<Electrode> inactiveElectrodes;
    private ArrayList<Device> devices;

    public enum Mutation {
        ADD_ELECTRODE, REMOVE_ELECTRODE, ADD_COLUMN, REMOVE_COLUMN, ADD_ROW, REMOVE_ROW;

        private static final Mutation[] VALUES = values();
        private static final int SIZE = VALUES.length;
        private static final Random RANDOM = new Random();

        public static Mutation getRandom()  {
            return VALUES[RANDOM.nextInt(SIZE)];
        }
    }

    public Architecture(int width, int height, ArrayList<Pair<Integer, Integer>> inactiveElectrodes, ArrayList<Device> devices) {
        this.devices = devices;
        this.activeElectrodes = new ArrayList<>();
        this.inactiveElectrodes = new ArrayList<>();
        this.electrodeGrid = new ArrayList<>(width);

        for (int x = 0; x < width; x++) {
            this.electrodeGrid.add(x, new ArrayList<>(height));
            for (int y = 0; y < height; y++) {
                Electrode electrode = new Electrode(x, y);
                this.electrodeGrid.get(x).add(y, electrode);
                this.activeElectrodes.add(electrode);
            }
        }

        for (Pair<Integer, Integer> inactiveElectrodeCoordinates : inactiveElectrodes) {
            int x = inactiveElectrodeCoordinates.getKey();
            int y = inactiveElectrodeCoordinates.getValue();
            this.removeElectrode(x, y);
        }
    }

    public Architecture(Architecture other) {
        this.devices = new ArrayList<>();
        for (Device device : other.devices) {
            this.devices.add(new Device(device));
        }

        this.activeElectrodes = new ArrayList<>();
        this.inactiveElectrodes = new ArrayList<>();
        this.electrodeGrid = new ArrayList<>();
        for (ArrayList<Electrode> column : other.electrodeGrid) {
            ArrayList<Electrode> columnCopy = new ArrayList<>();
            for (Electrode electrode : column) {
                Electrode electrodeCopy = new Electrode(electrode);
                columnCopy.add(electrodeCopy);
                if (electrode.isActive()) {
                    this.activeElectrodes.add(electrodeCopy);
                } else {
                    this.inactiveElectrodes.add(electrodeCopy);
                }
            }
            this.electrodeGrid.add(columnCopy);
        }
    }

    public int getWidth() {
        return electrodeGrid.size();
    }

    public int getHeight() {
        if (electrodeGrid.size() > 0) {
            return electrodeGrid.get(0).size();
        } else {
            return 0;
        }
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public ArrayList<Electrode> getInactiveElectrodes() {
        return inactiveElectrodes;
    }

    public Electrode getElectrode(int x, int y) {
        return this.electrodeGrid.get(x).get(y);
    }

    public boolean addElectrode(int x, int y) {
        if (this.getElectrode(x, y).isActive()) {
            return false;
        } else {
            Electrode electrode = this.getElectrode(x, y);
            electrode.setActive(true);
            activeElectrodes.add(electrode);
            inactiveElectrodes.remove(electrode);
            return true;
        }
    }

    public boolean removeElectrode(int x, int y) {
        if (this.getElectrode(x, y).isActive()) {
            Electrode electrode = this.getElectrode(x, y);
            electrode.setActive(false);
            activeElectrodes.remove(electrode);
            inactiveElectrodes.add(electrode);
            return true;
        } else {
            return false;
        }
    }

    public void addColumnOfElectrodes(int column) {
        ArrayList<Electrode> newColumn = new ArrayList<>(getHeight());
        for (int i = 0; i < getHeight(); i++) {
            Electrode electrode = new Electrode(column, i);
            this.activeElectrodes.add(electrode);
            newColumn.add(i, electrode);
        }

        this.electrodeGrid.add(column, newColumn);

        // Adjust x coordinates of electrodes to right of new column
        for (int x = column + 1; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                this.getElectrode(x, y).setX(x);
            }
        }
    }

    public void removeColumnOfElectrodes(int column) {
        for (Electrode electrode : electrodeGrid.remove(column)) {
            if (electrode.isActive()) {
                this.activeElectrodes.remove(electrode);
            } else {
                this.inactiveElectrodes.remove(electrode);
            }
        }

        // Adjust x coordinates of remaining electrodes to right of removed column
        for (int x = column; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                this.getElectrode(x, y).setX(x);
            }
        }
    }

    public void addRowOfElectrodes(int row) {
        for (int x = 0; x < getWidth(); x++) {
            Electrode electrode = new Electrode(x, row);
            this.activeElectrodes.add(electrode);
            electrodeGrid.get(x).add(row, electrode);

            // Adjust y coordinates of electrodes below new row
            for (int y = row + 1; y < getHeight(); y++) {
                this.getElectrode(x, y).setY(y);
            }
        }
    }

    public void removeRowOfElectrodes(int row) {
        for (int x = 0; x < getWidth(); x++) {
            Electrode electrode = electrodeGrid.get(x).remove(row);
            if (electrode.isActive()) {
                this.activeElectrodes.remove(electrode);
            } else {
                this.inactiveElectrodes.remove(electrode);
            }
        }

        // Adjust y coordinates of remaining electrodes below removed row
        for (int x = 0; x < getWidth(); x++) {
            for (int y = row; y < getHeight(); y++) {
                this.getElectrode(x, y).setY(y);
            }
        }

        System.out.print("");
    }

    public Architecture generateNeighbor() {
        Mutation mutation = Mutation.getRandom();

        switch (mutation) {
            case ADD_ELECTRODE:
                if (inactiveElectrodes.size() < 1) {
                    return generateNeighbor();
                }
            case REMOVE_ELECTRODE: {
                if (activeElectrodes.size() < 2) {
                    return generateNeighbor();
                }
            }
            case REMOVE_COLUMN: {
                if (getWidth() < 2) {
                    return generateNeighbor();
                }
            }
            case REMOVE_ROW: {
                if (getHeight() < 2) {
                    return generateNeighbor();
                }
            }
        }

        return generateNeighbor(mutation);
    }

    public Architecture generateNeighbor(Mutation mutation) {
        Architecture neighbor = new Architecture(this);
        Random random = new Random();

       System.out.printf("Width %s\tHeight %d\t %-18s\n", this.getWidth(), this.getHeight(), mutation);

        switch (mutation) {
            case ADD_ELECTRODE: {
                int i = random.nextInt(neighbor.inactiveElectrodes.size());
                Electrode electrode = neighbor.inactiveElectrodes.get(i);
                neighbor.addElectrode(electrode.getX(), electrode.getY());
                break;
            }
            case REMOVE_ELECTRODE: {
                int i = random.nextInt(neighbor.activeElectrodes.size());
                Electrode electrode = neighbor.activeElectrodes.get(i);
                neighbor.removeElectrode(electrode.getX(), electrode.getY());
                break;
            }
            case ADD_COLUMN: {
                int x = random.nextInt(getWidth() + 1);
                neighbor.addColumnOfElectrodes(x);
                break;
            }
            case REMOVE_COLUMN: {
                int x = random.nextInt(getWidth());
                neighbor.removeColumnOfElectrodes(x);
                break;
            }
            case ADD_ROW: {
                int y = random.nextInt(getHeight() + 1);
                neighbor.addRowOfElectrodes(y);
                break;
            }
            case REMOVE_ROW: {
                int y = random.nextInt(getHeight());
                neighbor.removeRowOfElectrodes(y);
                break;
            }
        }

        return neighbor;
    }

    @Override
    public String toString() {
        String output = "";

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Electrode electrode = this.getElectrode(x, y);
                if (electrode.isActive())
                    if (electrode.getX() == x && electrode.getY() == y) {
                        output += "X ";
                    } else {
                        output += "O ";
                    }
                else
                    output += "  ";
            }
            output += "\n";
        }

        return output;
    }
}
