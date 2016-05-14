public class Shape {
    private int width;
    private int height;
    private int snapX;
    private int snapY;

    public Shape(int width, int height, int snapX, int snapY) {
        this.width = width;
        this.height = height;
        this.snapX = snapX;
        this.snapY = snapY;
    }

    public Shape(Shape other) {
        this.width = other.width;
        this.height = other.height;
        this.snapX = other.snapX;
        this.snapY = other.snapY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSnapX() {
        return snapX;
    }

    public int getSnapY() {
        return snapY;
    }
}
