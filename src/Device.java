public class Device {

    public enum Type {
        DISPENSER_SAMPLE, DISPENSER_BUFFER, DISPENSER_REAGENT, OPTICAL_DETECTOR
    }

    private Type type;
    private int id;
    private int executionTime;
    private Shape shape;

    public Device(Type type, int id, int executionTime, Shape shape) {
        this.type = type;
        this.id = id;
        this.executionTime = executionTime;
        this.shape = shape;
    }

    public Device(Device other) {
        this.type = other.type;
        this.id = other.id;
        this.executionTime = other.id;
        this.shape = new Shape(other.shape);
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public Shape getShape() {
        return shape;
    }
}
