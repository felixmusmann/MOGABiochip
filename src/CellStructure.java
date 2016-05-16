import javafx.util.Pair;

import java.util.ArrayList;

public abstract class CellStructure {

    private int width;
    private int height;
    private ArrayList<ArrayList<Cell>> cells;

    public CellStructure() {}

    public CellStructure(int width, int height) {
        this.width = width;
        this.height = height;

        cells = new ArrayList<>(width);
        for (int x = 0; x < width; x++) {
            cells.add(x, new ArrayList<>(height));
            for (int y = 0; y < height; y++) {
                cells.get(x).add(null);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell setCell(int x, int y, Cell cell) {
        return cells.get(x).set(y, cell);
    }

    public Cell getCell(int x, int y) {
        return cells.get(x).get(y);
    }

    public ArrayList<Pair<Integer, Integer>> getFreeCells() {
        ArrayList<Pair<Integer, Integer>> freeCells = new ArrayList<>();

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (cells.get(x).get(y) == null) {
                    freeCells.add(new Pair<>(x, y));
                }
            }
        }

        return freeCells;
    }

    public void addColumn(int x) {
        cells.add(x, new ArrayList<>());
        for (int y = 0; y < getHeight(); y++) {
            cells.get(x).add(null);
        }
        width++;
        updateCoordinates(x + 1, 0);
    }

    public void removeColumn(int x) {
        cells.remove(x);
        width--;
        updateCoordinates(x, 0);
    }

    public void addRow(int y) {
        for (int x = 0; x < getWidth(); x++) {
            cells.get(x).add(y, null);
        }
        height++;
        updateCoordinates(0, y + 1);
    }

    public void removeRow(int y) {
        for (int x = 0; x < getWidth(); x++) {
            cells.get(x).remove(y);
        }
        height--;
        updateCoordinates(0, y);
    }

    public void insertCellStructure(int x, int y, CellStructure structure) {
        for (int iterX = x; iterX < structure.getWidth() + x; iterX++) {
            for (int iterY = y; iterY < structure.getHeight() + y; iterY++) {
                Cell cell = structure.getCell(iterX - x, iterY - y);
                if (cell != null) {
                    setCell(iterX, iterY, cell);
                    cell.setX(iterX).setY(iterY);
                }
            }
        }
    }

    private void updateCoordinates(int fromX, int fromY) {
        for (int x = fromX; x < getWidth(); x++) {
            for (int y = fromY; y < getHeight(); y++) {
                // TODO: update coordinates of devices, here or in Device?
                Cell cell = getCell(x, y);
                if (cell != null) {
                    cell.setX(x).setY(y);
                }
            }
        }
    }



    public boolean horizontalRangeCheck(int x) {
        return x >= 0 && x < getWidth();
    }

    public boolean verticalRangeCheck(int y) {
        return y >= 0 && y < getHeight();
    }

    @Override
    public String toString() {
        String output = "";

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Cell cell = getCell(x, y);
                if (cell == null) {
                    output += "  ";
                } else {
                    output += cell;
                }
            }
            output += "\n";
        }

        return output;
    }
}
