public class Device {

    public enum Type {
        DISPENSER_SAMPLE, DISPENSER_BUFFER, DISPENSER_REAGENT, OPTICAL_DETECTOR
    }

    private Type type;
    private int id;
    private int x;
    private int y;
    private int executionTime;
    private int cost;
    private Shape shape;

    // TODO: representation as mini-arch?

    public Device(Type type, int id, int executionTime, int cost, Shape shape) {
        this.type = type;
        this.id = id;
        this.executionTime = executionTime;
        this.cost = cost;
        this.shape = shape;
    }

    public Device(Type type, int id, int x, int y, int executionTime, int cost, Shape shape) {
        this(type, id, executionTime, cost, shape);
        this.x = x;
        this.y = y;
    }

    public Device(Device other) {
        this.type = other.type;
        this.id = other.id;
        this.x = other.x;
        this.y = other.y;
        this.executionTime = other.id;
        this.cost = other.cost;
        this.shape = new Shape(other.shape);
    }

    public Type getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
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

    public Shape getShape() {
        return shape;
    }
}
