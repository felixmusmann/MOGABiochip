package synthesis;

import synthesis.model.Device;
import synthesis.model.DeviceLibrary;
import synthesis.model.Electrode;

import java.util.*;
import java.util.logging.Logger;

public class BiochipRepairDevices {

    private enum Orientation {
        TOP, RIGHT, BOTTOM, LEFT
    }

    private final Logger LOGGER = Logger.getGlobal();

    private DeviceLibrary deviceLibrary;
    private Set<String> requiredDeviceTypes;

    public BiochipRepairDevices(DeviceLibrary deviceLibrary, Set<String> requiredDeviceTypes) {
        this.deviceLibrary = deviceLibrary;
        this.requiredDeviceTypes = requiredDeviceTypes;
    }

    public BiochipSolution execute(BiochipSolution solution) {
        List<Device> devices = solution.getDevices();
        for (String type : requiredDeviceTypes) {
            boolean foundType = false;

            for (Device device : devices) {
                foundType = type.equals(device.getType());
                if (foundType) {
                    break;
                }
            }

            if (!foundType) {
                // this device type is required but was not found on biochip
                solution = placeDevice(solution, type);
            }
        }

        return solution;
    }

    private BiochipSolution placeDevice(BiochipSolution solution, String type) {
        List<Device> possibleDevices = deviceLibrary.getDevicesByType(type);
        Collections.shuffle(possibleDevices);

        // try to place any of the possible devices and rotation
        boolean placedDevice = false;
        loopDevices:
        for (Device device : possibleDevices) {
            for (int i = 0; i < 4; i++) {
                if (i != 0) {
                    device = device.getRotatedCopy();
                }

                ArrayList<Orientation> orientations = new ArrayList<>(4);

                // determine possible orientations for device
                if (device.getStartCell().getY() == 0) {
                    orientations.add(Orientation.TOP);
                }
                if (device.getStartCell().getX() == device.getWidth() - 1) {
                    orientations.add(Orientation.RIGHT);
                }
                if (device.getStartCell().getY() == device.getHeight() - 1) {
                    orientations.add(Orientation.BOTTOM);
                }
                if (device.getStartCell().getX() == 0) {
                    orientations.add(Orientation.LEFT);
                }

                Electrode electrode = null;
                Orientation orientation = null;

                // try every orientation of this device
                for (Orientation currentOrientation : orientations) {
                    electrode = findConnectingElectrode(solution, device, currentOrientation);
                    if (electrode != null) {
                        orientation = currentOrientation;
                        break;
                    }
                }

                // we found a place
                if (electrode != null) {
                    Pair<Integer, Integer> coordinates = getCoordinatesForPlacement(electrode, orientation);
                    solution.addDevice(device, coordinates.fst, coordinates.snd);
                    placedDevice = true;
                    break loopDevices;
                }
            }
        }

        if (!placedDevice) {
            LOGGER.warning("Device of type " + type + " could not be placed.");
            // TODO: place device anywhere and connect (connection may not be possible)
        }

        return solution;
    }

    private Electrode findConnectingElectrode(BiochipSolution solution, Device device, Orientation orientation) {
        int startX = device.getStartCell().getX();
        int startY = device.getStartCell().getY();

        for (Electrode electrode : solution.getElectrodes()) {
            boolean foundElectrode = true;
            int leftBound = electrode.getX() - startX;
            int upperBound = electrode.getY() - startY;
            int rightBound;
            int lowerBound;


            switch (orientation) {
                case TOP:
                    upperBound = electrode.getY() + 1;
                    break;
                case RIGHT:
                    leftBound = electrode.getX() - device.getWidth();
                    break;
                case BOTTOM:
                    upperBound = electrode.getY() - device.getHeight();
                    break;
                case LEFT:
                    leftBound = electrode.getX() + 1;
                    break;
            }

            rightBound = leftBound + device.getWidth();
            lowerBound = upperBound + device.getHeight();

            loop:
            for (int x = leftBound; x < rightBound; x++) {
                for (int y = upperBound; y < lowerBound; y++) {
                    if (solution.getCell(x, y) != null) {
                        foundElectrode = false;
                        break loop;
                    }
                }
            }

            if (foundElectrode) {
                return electrode;
            }
        }

        return null;
    }

    private Pair<Integer, Integer> getCoordinatesForPlacement(Electrode electrode, Orientation orientation) {
        int x = electrode.getX();
        int y = electrode.getY();

        switch (orientation) {
            case TOP:
                y += 1;
                break;
            case RIGHT:
                x -= 1;
                break;
            case BOTTOM:
                y -= 1;
                break;
            case LEFT:
                x += 1;
        }

        return new Pair<>(x, y);
    }
}
