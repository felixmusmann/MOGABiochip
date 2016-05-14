public class Electrode {

    private boolean active;

    private int x;
    private int y;

    private Device linkedDevice;

    public Electrode(int x, int y) {
        this.x = x;
        this.y = y;
        this.active = true;
    }

    public Electrode(Electrode other) {
        this.x = other.x;
        this.y = other.y;
        this.active = other.active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setLinkedDevice(Device linkedDevice) {
        if (linkedDevice == null) {
            this.linkedDevice = null;
        } else {
            this.linkedDevice = linkedDevice;
        }

    }

    public Device getLinkedDevice() {
        return linkedDevice;
    }

    public boolean isBlocked() {
        return linkedDevice != null;
    }
}
