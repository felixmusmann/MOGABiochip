public class DeviceCell extends Cell {

    private Device linkedDevice;
    private boolean isStartCell;

    public DeviceCell(int x, int y, Device linkedDevice) {
        super(x, y, true);
        this.linkedDevice = linkedDevice;
        isStartCell = false;
    }

    public DeviceCell(int x, int y, Device linkedDevice, boolean isStartCell) {
        super(x, y, true);
        this.linkedDevice = linkedDevice;
        this.isStartCell = isStartCell;
    }

    public Device getLinkedDevice() {
        return linkedDevice;
    }

    public void setStartCell(boolean isStartCell) {
        this.isStartCell = isStartCell;
        setBlocked(false);
    }

    public boolean isStartCell() {
        return isStartCell;
    }

    @Override
    public String toString() {
        if (isStartCell()) {
            return "+ ";
        } else {
            return "# ";
        }
    }
}
