public class Shape {
    private int width;
    private int height;

    public Shape(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Shape(Shape other) {
        this.width = other.width;
        this.height = other.height;
    }
}
