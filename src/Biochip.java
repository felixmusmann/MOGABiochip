import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

public class Biochip extends CellStructure {

    private ArrayList<Electrode> electrodes;
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

    public Biochip(int width, int height, ArrayList<Pair<Integer, Integer>> inactiveElectrodes, ArrayList<Device> devices) {
        super(width, height);
        this.electrodes = new ArrayList<>();
        this.devices = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                addElectrode(x, y);
            }
        }

        for (Pair<Integer, Integer> inactiveElectrodeCoordinates : inactiveElectrodes) {
            int x = inactiveElectrodeCoordinates.getKey();
            int y = inactiveElectrodeCoordinates.getValue();
            removeElectrode(x, y);
        }

        for (Device device : devices) {
            addDevice(device, device.getX(), device.getY());
        }
    }

    public Biochip(Biochip other) {
        super(other.getWidth(), other.getHeight());

        this.devices = new ArrayList<>();
        for (Device device : other.devices) {
            addDevice(new Device(device), device.getStartCell().getX(), device.getStartCell().getY());
        }

        this.electrodes = new ArrayList<>();
        for (Electrode electrode : other.electrodes) {
            addElectrode(electrode.getX(), electrode.getY());
        }
    }

    public float getCost() {
        // TODO: calculate cost
        float costDevices = 0;
        for (Device device : devices) {
            costDevices += device.getCost();
        }
        return electrodes.size() + costDevices;
    }

    public Electrode addElectrode(int x, int y) {
        if (getCell(x, y) != null) {
            return null;
        }

        Electrode electrode = new Electrode(x, y);
        setCell(x, y, electrode);
        electrodes.add(electrode);
        return electrode;
    }

    public Electrode removeElectrode(int x, int y) {
        if (!(getCell(x, y) instanceof Electrode)) {
            return null;
        }

        Electrode electrode = (Electrode) setCell(x, y, null);
        electrodes.remove(electrode);
        return electrode;
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public void addDevice(Device device, int startX, int startY) {
        device.resetDevice();
        // determine area for placement
        int leftBound = startX - device.getStartCell().getX();
        int rightBound = device.getWidth() - device.getStartCell().getX() + startX;
        int upperBound = startY - device.getStartCell().getY();
        int lowerBound = device.getHeight()  - device.getStartCell().getY() + startY;

        // increase size if bounds not in area
        if (leftBound < 0 || rightBound >= getWidth()) {
            // TODO: merge device horizontal
            return;
        } else if (upperBound < 0 || lowerBound >= getHeight()) {
            // TODO: merge device vertical
            return;
        }

        // check if area is in bounds
        boolean isInBounds = horizontalRangeCheck(leftBound) && horizontalRangeCheck(rightBound - 1)
                    && verticalRangeCheck(upperBound) && verticalRangeCheck(lowerBound - 1);
        if (!isInBounds) {
            throw new IndexOutOfBoundsException();
        }

        // check area for other devices
        for (int x = leftBound; x < rightBound; x++) {
            for (int y = upperBound; y < lowerBound; y++) {
                if (getCell(x, y) instanceof DeviceCell) {
                    throw new UnsupportedOperationException("There is another device in the area.");
                }
            }
        }

        // remove electrodes in area
        for (int x = leftBound; x < rightBound; x++) {
            for (int y = upperBound; y < lowerBound; y++) {
                removeElectrode(x, y);
            }
        }

        // add device
        insertCellStructure(leftBound, upperBound, device);
        device.setX(leftBound).setY(upperBound);
        devices.add(device);
    }

    public void removeDevice(Device device) {
        for (int x = device.getX(); x < device.getX() + device.getWidth(); x++) {
            for (int y = device.getY(); y < device.getY() + device.getHeight(); y++) {
                setCell(x, y, null);
            }
        }

        devices.remove(device);
    }

    /**
     * Removes all devices from the device list,
     * that are not placed on the chip.
     *
     * @return true if devices were removed
     */
    public boolean removeUnusedDevices() {
        HashSet<Device> usedDevices = new HashSet<>();

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Cell cell = getCell(x, y);
                if (cell instanceof DeviceCell) {
                    usedDevices.add(((DeviceCell) cell).getLinkedDevice());
                }
            }
        }

        return devices.retainAll(usedDevices);
    }

    /**
     * This method adds a full column of active electrodes to the architecture
     * at the specified index. The existing column and devices in it will be moved to the right.
     *
     * @param x    index of row at which the electrodes will be inserted
     */
    public void addColumnOfElectrodes(int x) {
        // collect devices in column, will be moved to right
        ArrayList<Device> devicesInArea = new ArrayList<>();
        for (int y = 0; y < getHeight() && devices.size() > 0; y++) {
            Cell cell = getCell(x, y);
            if (cell instanceof DeviceCell) {
                Device device = ((DeviceCell) cell).getLinkedDevice();
                devicesInArea.add(device);
                removeDevice(device);
            }
        }

        addColumn(x);
        for (int y = 0; y < getHeight(); y++) {
            addElectrode(x, y);
        }

        // add devices back to architecture, move to right
        for (Device device : devicesInArea) {
            int newX = device.getStartCell().getX() + 1;
            int newY = device.getStartCell().getY();
            addDevice(device, newX, newY);
        }
    }

    /**
     * This method removes a single column of electrodes on the biochip.
     * Devices in this column will be removed from the biochip.
     *
     * @param column   index of column, which will be removed
     */
    public void removeColumnOfElectrodes(int column) {
        removeColumnOfElectrodes(column, column+1);
    }

    /**
     * This method removes all columns of electrodes between the specified bounds
     * on the biochip. Devices in these columns will be removed.
     *
     * @param from  left bound of rows, which will be removed
     * @param to    right bound (excluded) of rows, which will be removed
     */
    public void removeColumnOfElectrodes(int from, int to) {
        for (int x = from; x < to; x++) {
            for (int y = 0; y < getHeight(); y++) {
                Cell cell = getCell(x, y);
                if (cell instanceof Electrode) {
                    removeElectrode(x, y);
                } else if (cell instanceof DeviceCell) {
                    removeDevice(((DeviceCell) cell).getLinkedDevice());
                }
            }
        }

        for (int x = from; x < to; x++) {
            removeColumn(from);
        }
    }

    /**
     * This method adds a full row of active electrodes to the biochip
     * at the specified index. The existing rows and devices in it will be moved downwards.
     *
     * @param y   index of row at which the electrodes will be inserted
     */
    public void addRowOfElectrodes(int y) {
        // collect devices in row, will be moved downwards
        ArrayList<Device> devicesInArea = new ArrayList<>();
        for (int x = 0; x < getWidth() && devices.size() > 0; x++) {
            Cell cell = getCell(x, y);
            if (cell instanceof DeviceCell) {
                Device device = ((DeviceCell) cell).getLinkedDevice();
                devicesInArea.add(device);
                removeDevice(device);
            }
        }

        addRow(y);
        for (int x = 0; x < getWidth(); x++) {
            addElectrode(x, y);
        }

        // add devices back to architecture, move to right
        for (Device device : devicesInArea) {
            int newX = device.getStartCell().getX();
            int newY = device.getStartCell().getY() + 1;
            addDevice(device, newX, newY);
        }
    }

    /**
     * This method removes a single row of electrodes on the biochip.
     *
     * @param row   index of row, which will be removed
     */
    public void removeRowOfElectrodes(int row) {
        removeRowOfElectrodes(row, row + 1);
    }

    /**
     * This method removes all rows of electrodes between the specified bounds
     * on the biochip.
     *
     * @param from  lower bound of rows, which will be removed
     * @param to    upper bound (excluded) of rows, which will be removed
     */
    public void removeRowOfElectrodes(int from, int to) {
        for (int y = from; y < to; y++) {
            for (int x = 0; x < getWidth(); x++) {
                Cell cell = getCell(x, y);
                if (cell instanceof Electrode) {
                    removeElectrode(x, y);
                } else if (cell instanceof DeviceCell) {
                    removeDevice(((DeviceCell) cell).getLinkedDevice());
                }
            }
        }

        for (int y = from; y < to; y++) {
            removeRow(from);
        }
    }

    /**
     * This method splits the biochip at a given column and
     * returns an array with two new biochips. At index 0 is the
     * left-hand part of the biochip including the specified column,
     * index 1 contains the right-hand part excluding the specified column.
     * Devices overlapping the split will be removed.
     *
     * @param column   Column at which the biochip will be split.
     * @return Array with two biochips, at index 0 left-hand part and index 1 right-hand part.
     */
    public Biochip[] splitAtColumn(int column) {
        Biochip[] splitBiochips = new Biochip[2];
        splitBiochips[0] = new Biochip(this);
        splitBiochips[0].removeColumnOfElectrodes(column, getWidth());
        splitBiochips[1] = new Biochip(this);
        splitBiochips[1].removeColumnOfElectrodes(0, column);
        return splitBiochips;
    }

    /**
     * This method splits the biochip at a given row and
     * returns an array with two new biochips. At index 0 is the
     * upper part of the biochip excluding the specified row,
     * index 1 contains the lower part excluding the specified row.
     * Devices overlapping the split will be removed.
     *
     * @param row   Row at which the biochip will be split.
     * @return Array with two biochips, at index 0 upper part and index 1 lower part.
     */
    public Biochip[] splitAtRow(int row) {
        Biochip[] splitBiochips = new Biochip[2];
        splitBiochips[0] = new Biochip(this);
        splitBiochips[0].removeRowOfElectrodes(row, getHeight());
        splitBiochips[1] = new Biochip(this);
        splitBiochips[1].removeRowOfElectrodes(0, row);
        return splitBiochips;
    }

    /**
     * This method merges two biochips vertically.
     * The first biochip will be positioned at the top and the second below.
     * The specified column of the first biochip will be aligned according
     * to the alignment option of the second biochip.
     *
     * @param first         top biochip
     * @param second        bottom biochip
     * @param columnFirst   column of the first biochip for alignment
     * @param columnSecond  column of the second biochip for alignment
     * @return new merged Biochip
     */
    public static Biochip mergeVertical(Biochip first, Biochip second, int columnFirst, int columnSecond) {
        Biochip mergedBiochip = new Biochip(first);
        Biochip secondCopy = new Biochip(second);

        int firstXShift = Math.max(0, columnSecond - columnFirst);
        int secondXShift = Math.max(0, columnFirst - columnSecond);
        int width = Math.max(columnFirst, columnSecond) + Math.max(first.getWidth() - columnFirst, second.getWidth() - columnSecond);
        int height = first.getHeight() + second.getHeight();

        for (int i = 0; i < firstXShift; i++) {
            mergedBiochip.addColumn(0);
        }

        while (mergedBiochip.getWidth() < width) {
            mergedBiochip.addColumn(mergedBiochip.getWidth());
        }

        while (mergedBiochip.getHeight() < height) {
            mergedBiochip.addRow(mergedBiochip.getHeight());
        }

        mergedBiochip.insertCellStructure(secondXShift, first.getHeight(), secondCopy);
        mergedBiochip.getDevices().addAll(secondCopy.getDevices());

        return mergedBiochip;
    }

    /**
     * This method merges two biochips horizontally.
     * The first biochip will be positioned at the left-hand side and the second to the right-hand side.
     * The specified row of the first biochip will be aligned according
     * to the alignment option of the second biochip.
     *
     * @param first     left-hand biochip
     * @param second    right-hand biochip
     * @param rowFirst       row of the first biochip for alignment
     * @param rowSecond alignment option for second biochip
     * @return new merged Biochip
     */
    public static Biochip mergeHorizontal(Biochip first, Biochip second, int rowFirst, int rowSecond) {
        Biochip mergedBiochip = new Biochip(first);
        Biochip secondCopy = new Biochip(second);

        int firstYShift = Math.max(0, rowSecond - rowFirst);
        int secondYShift = Math.max(0, rowFirst - rowSecond);
        int width = first.getWidth() + second.getWidth();
        int height = Math.max(rowFirst, rowSecond) + Math.max(first.getHeight() - rowFirst, second.getHeight() - rowSecond);

        for (int i = 0; i < firstYShift; i++) {
            mergedBiochip.addRow(0);
        }

        while (mergedBiochip.getWidth() < width) {
            mergedBiochip.addColumn(mergedBiochip.getWidth());
        }

        while (mergedBiochip.getHeight() < height) {
            mergedBiochip.addRow(mergedBiochip.getHeight());
        }

        mergedBiochip.insertCellStructure(first.getWidth(), secondYShift, secondCopy);
        mergedBiochip.getDevices().addAll(secondCopy.getDevices());

        return mergedBiochip;
    }

    /**
     * This method generates a random neighbor of the biochip.
     * Possible mutations are adding or removing of a single electrode,
     * row or column.
     *
     * @return neighbor with random mutation
     */
    public Biochip generateNeighbor() {
        Mutation mutation = Mutation.getRandom();

        switch (mutation) {
            case ADD_ELECTRODE:
                if (getFreeCells().size() < 1) {
                    return generateNeighbor();
                }
            case REMOVE_ELECTRODE: {
                if (electrodes.size() < 2) {
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
     * @param mutation  mutation that will be applied to biochip
     * @return neighbor with specific mutation
     */
    public Biochip generateNeighbor(Mutation mutation) {
        Biochip neighbor = new Biochip(this);
        Random random = new Random();

        switch (mutation) {
            case ADD_ELECTRODE: {
                ArrayList<Pair<Integer, Integer>> freeCells = neighbor.getFreeCells();
                int i = random.nextInt(freeCells.size());
                Pair<Integer, Integer> coordinates = freeCells.get(i);
                neighbor.addElectrode(coordinates.getKey(), coordinates.getValue());
                break;
            }
            case REMOVE_ELECTRODE: {
                int i = random.nextInt(neighbor.electrodes.size());
                Electrode electrode = neighbor.electrodes.get(i);
                neighbor.removeElectrode(electrode.getX(), electrode.getY());
                break;
            }
            case ADD_COLUMN: {
                int x = random.nextInt(getWidth());
                neighbor.addColumnOfElectrodes(x);
                break;
            }
            case REMOVE_COLUMN: {
                int x = random.nextInt(getWidth());
                neighbor.removeColumnOfElectrodes(x);
                break;
            }
            case ADD_ROW: {
                int y = random.nextInt(getHeight());
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
}
