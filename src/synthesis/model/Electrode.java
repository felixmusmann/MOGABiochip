package synthesis.model;

public class Electrode extends Cell {

    public Electrode(int x, int y) {
        super(x, y, false);
    }

    public Electrode(Electrode other) {
        super(other.getX(), other.getY(), other.isBlocked());
    }

    @Override
    public String toString() {
        if (isBlocked()) {
            return super.toString();
        } else {
            return "X ";
        }
    }
}
