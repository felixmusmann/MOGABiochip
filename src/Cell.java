public abstract class Cell {

    private boolean isBlocked;
    private int x;
    private int y;

    public Cell(int x, int y, boolean isBlocked) {
        this.x = x;
        this.y = y;
        this.isBlocked = isBlocked;
    }

    public int getX() {
        return x;
    }

    public Cell setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public Cell setY(int y) {
        this.y = y;
        return this;
    }

    public Cell setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
        return this;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    @Override
    public String toString() {
        if (isBlocked()) {
            return "B ";
        } else {
            return "  ";
        }
    }
}
