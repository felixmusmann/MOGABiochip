package synthesis.model;

public class Device extends CellStructure {

    private int id;
    private String type;
    private int x;
    private int y;
    private int executionTime;
    private int cost;

    private DeviceCell startCell;

    public Device(String type, int id, int executionTime, int cost, Shape shape) {
        super(shape.getWidth(), shape.getHeight());
        this.type = type;
        this.id = id;
        this.executionTime = executionTime;
        this.cost = cost;

        // fill with device cells
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                DeviceCell deviceCell = new DeviceCell(x, y, this);
                setCell(x, y, deviceCell);
            }
        }

        startCell = (DeviceCell) getCell(shape.getStartX(), shape.getStartY());
        startCell.setStartCell(true);
    }

    public Device(String type, int id, int x, int y, int executionTime, int cost, Shape shape) {
        super(shape.getWidth(), shape.getHeight());
        this.x = x;
        this.y = y;
        this.type = type;
        this.id = id;
        this.executionTime = executionTime;
        this.cost = cost;

        // fill with device cells
        for (x = 0; x < getWidth(); x++) {
            for (y = 0; y < getHeight(); y++) {
                DeviceCell deviceCell = new DeviceCell(x + this.x, y + this.y, this);
                setCell(x, y, deviceCell);
            }
        }

        startCell = (DeviceCell) getCell(shape.getStartX() - this.x, shape.getStartY() - this.y);
        startCell.setStartCell(true);
    }

    public Device(Device other) {
        super(other.getWidth(), other.getHeight());

        // fill with device cells
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                DeviceCell cell = new DeviceCell(x, y, this);
                if (((DeviceCell) other.getCell(x, y)).isStartCell()) {
                    cell.setStartCell(true);
                    startCell = cell;
                }
                setCell(x, y, cell);
            }
        }

        this.type = other.type;
        this.id = other.id;
        this.x = other.x;
        this.y = other.y;
        this.executionTime = other.executionTime;
        this.cost = other.cost;
    }

    public void resetDevice() {
        x = 0;
        y = 0;

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                getCell(x, y).setX(x).setY(y);
            }
        }
    }

    public String getType() {
        return type;
    }

    public Device setX(int x) {
        this.x = x;
        return this;
    }

    public int getX() {
        return x;
    }

    public Device setY(int y) {
        this.y = y;
        return this;
    }

    public int getY() {
        return y;
    }

    public DeviceCell getStartCell() {
        return startCell;
    }

    public int getId() {
        return id;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public int getCost() {
        return cost;
    }

    public Device getRotatedCopy() {
        int startX = this.getHeight() - this.getStartCell().getY() - 1;
        int startY = this.getStartCell().getX();
        Shape shape = new Shape(this.getHeight(), this.getWidth(), startX, startY);
        return new Device(this.type, this.id, this.executionTime, this.cost, shape);
    }

    @Override
    void updateOperations() {}
}
