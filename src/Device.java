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
}
