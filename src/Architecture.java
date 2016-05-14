import javafx.util.Pair;

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
            float weight = RANDOM.nextFloat();
            if (weight < 0.4) {
                return REMOVE_ELECTRODE;
            } else {
                return VALUES[RANDOM.nextInt(SIZE)];
            }
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
    
    public float getCost() {
        // TODO: calculate cost
        float costDevices = 0;
        for (Device device :
                this.devices) {
            costDevices += device.getCost();
        }
        return this.activeElectrodes.size() + costDevices;
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public boolean addDevice(Device device, int x, int y) {
        // TODO: add device
        return false;
    }

    public boolean removeDevice(Device device) {
        // TODO: remove device
        return false;
    }

    public ArrayList<Electrode> getInactiveElectrodes() {
        return inactiveElectrodes;
    }

    public Electrode getElectrode(int x, int y) {
        return this.electrodeGrid.get(x).get(y);
    }

    public boolean addElectrode(int x, int y) {
        boolean isInBounds = x < this.getWidth() && y < this.getHeight();
        boolean isAddable = !this.getElectrode(x, y).isActive() && !this.getElectrode(x, y).isBlocked();

        if (isInBounds && isAddable) {
            Electrode electrode = this.getElectrode(x, y);
            electrode.setActive(true);
            activeElectrodes.add(electrode);
            inactiveElectrodes.remove(electrode);
            return true;
        }

        return false;
    }

    public boolean removeElectrode(int x, int y) {
        boolean isInBounds = x < this.getWidth() && y < this.getHeight();
        boolean isRemovable = this.getElectrode(x, y).isActive() && !this.getElectrode(x, y).isBlocked();

        if (isInBounds && isRemovable) {
            Electrode electrode = this.getElectrode(x, y);
            electrode.setActive(false);
            activeElectrodes.remove(electrode);
            inactiveElectrodes.add(electrode);
            return true;
        }

        return false;
    }

    /**
     * This method adds a full column of active electrodes to the architecture
     * at the specified index. The existing column will be moved to the right.
     *
     * @param column    index of row at which the electrodes will be inserted
     */
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

    /**
     * This method removes a single column of electrodes on the architecture.
     *
     * @param column   index of column, which will be removed
     */
    public void removeColumnOfElectrodes(int column) {
        removeColumnOfElectrodes(column, column);
    }

    /**
     * This method removes all columns of electrodes between the specified bounds
     * on the architecture.
     *
     * @param from  left bound of rows, which will be removed
     * @param to    right bound (included) of rows, which will be removed
     */
    public void removeColumnOfElectrodes(int from, int to) {
        for (int column = from; column <= to; column++) {
            for (Electrode electrode : electrodeGrid.remove(from)) {
                if (electrode.isActive()) {
                    this.activeElectrodes.remove(electrode);
                } else {
                    this.inactiveElectrodes.remove(electrode);
                }
            }
        }

        // Adjust x coordinates of remaining electrodes to right of removed column
        for (int x = from; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                this.getElectrode(x, y).setX(x);
            }
        }
    }

    /**
     * This method adds a full row of active electrodes to the architecture
     * at the specified index. The existing rows will be moved downwards.
     *
     * @param row   index of row at which the electrodes will be inserted
     */
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

    /**
     * This method removes a single row of electrodes on the architecture.
     *
     * @param row   index of row, which will be removed
     */
    public void removeRowOfElectrodes(int row) {
        removeRowOfElectrodes(row, row);
    }

    /**
     * This method removes all rows of electrodes between the specified bounds
     * on the architecture.
     *
     * @param from  lower bound of rows, which will be removed
     * @param to    upper bound (included) of rows, which will be removed
     */
    public void removeRowOfElectrodes(int from, int to) {
        for (int x = 0; x < getWidth(); x++) {
            for (int row = from; row <= to; row++) {
                Electrode electrode = electrodeGrid.get(x).remove(from);
                if (electrode.isActive()) {
                    this.activeElectrodes.remove(electrode);
                } else {
                    this.inactiveElectrodes.remove(electrode);
                }
            }
        }

        // Adjust y coordinates of remaining electrodes below removed row
        for (int x = 0; x < getWidth(); x++) {
            for (int y = from; y < getHeight(); y++) {
                this.getElectrode(x, y).setY(y);
            }
        }

        System.out.print("");
    }

    /**
     * This method splits the architecture at a given column and
     * returns an array with two new architectures. At index 0 is the
     * left-hand part of the architecture including the specified column,
     * index 1 contains the right-hand part excluding the specified column.
     *
     * @param column   Column at which the architecture will be split.
     * @return Array with two architectures, at index 0 left-hand part and index 1 right-hand part.
     */
    public Architecture[] splitAtColumn(int column) {
        Architecture[] splitArchitectures = new Architecture[2];
        splitArchitectures[0] = new Architecture(this);
        splitArchitectures[0].removeColumnOfElectrodes(column + 1, splitArchitectures[0].getWidth()-1);
        splitArchitectures[1] = new Architecture(this);
        splitArchitectures[1].removeColumnOfElectrodes(0, column);
        return splitArchitectures;
    }

    /**
     * This method splits the architecture at a given row and
     * returns an array with two new architectures. At index 0 is the
     * upper part of the architecture including the specified row,
     * index 1 contains the lower part excluding the specified row.
     *
     * @param row   Row at which the architecture will be split.
     * @return Array with two architectures, at index 0 upper part and index 1 lower part.
     */
    public Architecture[] splitAtRow(int row) {
        Architecture[] splitArchitectures = new Architecture[2];
        splitArchitectures[0] = new Architecture(this);
        splitArchitectures[0].removeRowOfElectrodes(row + 1, splitArchitectures[0].getHeight()-1);
        splitArchitectures[1] = new Architecture(this);
        splitArchitectures[1].removeRowOfElectrodes(0, row);
        return splitArchitectures;
    }

    /**
     * This method merges two architectures vertically.
     * The first architecture will be positioned at the top and the second below.
     * The specified column of the first architecture will be aligned according
     * to the alignment option of the second architecture.
     *
     * @param first     top architecture
     * @param second    bottom architecture
     * @param column    column of the first architecture for alignment
     * @param alignment alignment option for second architecture
     * @return new merged Architecture
     */
    public static Architecture mergeVertical(Architecture first, Architecture second, int column, Alignment alignment) {
        Architecture mergedArchitecture;
        int width, height, secondColumn;

        if (alignment == Alignment.LEFT) {
            secondColumn = 0;
        } else if (alignment == Alignment.CENTER) {
            secondColumn = (int) Math.floor((second.getWidth() - 1) / 2);
        } else if (alignment == Alignment.RIGHT) {
            secondColumn = second.getWidth() - 1;
        } else {
            throw new IllegalArgumentException("Alignment must be LEFT, CENTER or RIGHT.");
        }

        int firstXShift = Math.max(0, secondColumn - column);
        int secondXShift = Math.max(0, column - secondColumn);

        width = Math.max(column, secondColumn) + Math.max(first.getWidth() - column, second.getWidth() - secondColumn);
        height = first.getHeight() + second.getHeight();

        // Copy inactive electrodes
        ArrayList<Pair<Integer, Integer>> inactiveElectrodes = new ArrayList<>();
        for (Electrode electrode : first.getInactiveElectrodes()) {
            int x = electrode.getX() + firstXShift;
            int y = electrode.getY();
            inactiveElectrodes.add(new Pair<>(x, y));
        }

        for (Electrode electrode : second.getInactiveElectrodes()) {
            int x = electrode.getX() + secondXShift;
            int y = electrode.getY() + first.getHeight();
            inactiveElectrodes.add(new Pair<>(x, y));
        }

        // Set corner electrodes inactive
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (y < first.getHeight()) {
                    if (x < firstXShift || x >= firstXShift + first.getWidth()) {
                        inactiveElectrodes.add(new Pair<>(x, y));
                    }
                } else {
                    if (x < secondXShift || x >= secondXShift + second.getWidth()) {
                        inactiveElectrodes.add(new Pair<>(x, y));
                    }
                }
            }
        }

        // TODO: merge devices

        mergedArchitecture = new Architecture(width, height, inactiveElectrodes, null);

        return mergedArchitecture;
    }

    /**
     * This method merges two architectures horizontally.
     * The first architecture will be positioned at the left-hand side and the second to the right-hand side.
     * The specified row of the first architecture will be aligned according
     * to the alignment option of the second architecture.
     *
     * @param first     left-hand architecture
     * @param second    right-hand architecture
     * @param row       row of the first architecture for alignment
     * @param alignment alignment option for second architecture
     * @return new merged Architecture
     */
    public static Architecture mergeHorizontal(Architecture first, Architecture second, int row, Alignment alignment) {
        Architecture mergedArchitecture;
        int width, height, secondRow;

        if (alignment == Alignment.TOP) {
            secondRow = 0;
        } else if (alignment == Alignment.CENTER) {
            secondRow = (int) Math.floor((second.getHeight() - 1) / 2);
        } else if (alignment == Alignment.BOTTOM) {
            secondRow = second.getHeight() - 1;
        } else {
            throw new IllegalArgumentException("Alignment must be TOP, CENTER or BOTTOM.");
        }

        int firstYShift = Math.max(0, secondRow - row);
        int secondYShift = Math.max(0, row - secondRow);

        width = first.getWidth() + second.getWidth();
        height = Math.max(row, secondRow) + Math.max(first.getHeight() - row, second.getHeight() - secondRow);

        // Copy inactive electrodes
        ArrayList<Pair<Integer, Integer>> inactiveElectrodes = new ArrayList<>();
        for (Electrode electrode : first.getInactiveElectrodes()) {
            int x = electrode.getX();
            int y = electrode.getY() + firstYShift;
            inactiveElectrodes.add(new Pair<>(x, y));
        }

        for (Electrode electrode : second.getInactiveElectrodes()) {
            int x = electrode.getX() + first.getWidth();
            int y = electrode.getY() + secondYShift;
            inactiveElectrodes.add(new Pair<>(x, y));
        }

        // Set corner electrodes inactive
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x < first.getWidth()) {
                    if (y < firstYShift || y >= firstYShift + first.getHeight()) {
                        inactiveElectrodes.add(new Pair<>(x, y));
                    }
                } else {
                    if (y < secondYShift || y >= secondYShift + second.getHeight()) {
                        inactiveElectrodes.add(new Pair<>(x, y));
                    }
                }
            }
        }


        // TODO: merge devices

        mergedArchitecture = new Architecture(width, height, inactiveElectrodes, null);

        return mergedArchitecture;
    }

    /**
     * This method generates a random neighbor of the architecture.
     * Possible mutations are adding or removing of a single electrode,
     * row or column.
     *
     * @return neighbor with random mutation
     */
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

    /**
     * This method generates a random neighbor with a specific mutation.
     *
     * @param mutation  mutation that will be applied to architecture
     * @return neighbor with specific mutation
     */
    public Architecture generateNeighbor(Mutation mutation) {
        Architecture neighbor = new Architecture(this);
        Random random = new Random();

       System.out.println(mutation);

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
