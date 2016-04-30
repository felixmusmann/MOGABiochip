import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Random;

public class Architecture {

    private ArrayList<ArrayList<Electrode>> electrodes;
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
        this.electrodes = new ArrayList<>(width);

        for (int x = 0; x < width; x++) {
            this.electrodes.add(x, new ArrayList<>(height));
            for (int y = 0; y < height; y++) {
                this.electrodes.get(x).add(y, new Electrode(x, y));
            }
        }

        for (Pair<Integer, Integer> inactiveElectrode : inactiveElectrodes) {
            int x = inactiveElectrode.getKey();
            int y = inactiveElectrode.getValue();
            electrodes.get(x).get(y).setActive(false);
        }
    }

    public Architecture(Architecture other) {
        this.devices = new ArrayList<>();
        for (Device device : other.devices) {
            this.devices.add(new Device(device));
        }

        this.electrodes = new ArrayList<>();
        for (ArrayList<Electrode> column :
                other.electrodes) {
            ArrayList<Electrode> columnCopy = new ArrayList<>();
            for (Electrode electrode :
                    column) {
                columnCopy.add(new Electrode(electrode));
            }
            this.electrodes.add(columnCopy);
        }
    }

    public int getWidth() {
        return electrodes.size();
    }

    public int getHeight() {
        if (electrodes.size() > 0) {
            return electrodes.get(0).size();
        } else {
            return 0;
        }
    }

    public Electrode getElectrode(int x, int y) {
        return this.electrodes.get(x).get(y);
    }

    public void addElectrode(int x, int y) {
        this.getElectrode(x, y).setActive(true);
    }

    public void removeElectrode(int x, int y) {
        this.getElectrode(x, y).setActive(false);
    }

    public void addColumnOfElectrodes(int column) {
        ArrayList<Electrode> newColumn = new ArrayList<>(getHeight());
        for (int i = 0; i < getHeight(); i++) {
            newColumn.add(i, new Electrode(column, i));
        }

        this.electrodes.add(column, newColumn);

        for (int x = column + 1; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                this.getElectrode(x, y).setX(x);
            }
        }
    }

    public void removeColumnOfElectrodes(int column) {
        electrodes.remove(column);
        for (int x = column; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                this.getElectrode(x, y).setX(x);
            }
        }
    }

    public void addRowOfElectrodes(int row) {
        for (int x = 0; x < getWidth(); x++) {
            electrodes.get(x).add(row, new Electrode(x, row));
            for (int y = row + 1; y < getHeight(); y++) {
                this.getElectrode(x, y).setY(y);
            }
        }
    }

    public void removeRowOfElectrodes(int row) {
        for (int x = 0; x < getWidth(); x++) {
            electrodes.get(x).remove(row);
        }

        for (int x = 0; x < getWidth(); x++) {
            for (int y = row; y < getHeight(); y++) {
                this.getElectrode(x, y).setY(y);
            }
        }
    }

    public Architecture generateNeighbor() {
        Architecture neighbor = new Architecture(this);
        Mutation mutation = Mutation.getRandom();
        Random random = new Random();
        int x = -1;
        int y = -1;

        switch (mutation) {
            case ADD_ELECTRODE:
                x = random.nextInt(getWidth());
                y = random.nextInt(getHeight());
                neighbor.addElectrode(x, y);
                break;
            case REMOVE_ELECTRODE:
                x = random.nextInt(getWidth());
                y = random.nextInt(getHeight());
                neighbor.removeElectrode(x, y);
                break;
            case ADD_COLUMN:
                x = random.nextInt(getWidth()+1);
                neighbor.addColumnOfElectrodes(x);
                break;
            case REMOVE_COLUMN:
                x = random.nextInt(getWidth());
                neighbor.removeColumnOfElectrodes(x);
                break;
            case ADD_ROW:
                y = random.nextInt(getHeight()+1);
                neighbor.addRowOfElectrodes(y);
                break;
            case REMOVE_ROW:
                y = random.nextInt(getHeight());
                neighbor.removeRowOfElectrodes(y);
                break;
        }

        System.out.println("Neighbor generation: " + mutation + " x: "  + x + "y: " + y);

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
