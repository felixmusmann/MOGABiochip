public class Electrode {

    private boolean active;
    private int x;
    private int y;

    public Electrode(int x, int y) {
        this.x = x;
        this.y = y;
        this.active = true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
