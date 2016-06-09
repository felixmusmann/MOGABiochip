package synthesis;

import synthesis.model.Device;

import java.util.*;

/**
 * This class allows accessing devices by type or id.
 */
public class DeviceLibrary {

    private Map<String, List<Device>> mapByType;
    private Map<Integer, Device> mapById;

    public DeviceLibrary() {
        mapByType = new HashMap<>();
        mapById = new HashMap<>();
    }

    public void add(List<Device> devices) {
        for (Device device : devices) {
            add(device);
        }
    }

    public void add(Device device) {
        if (mapByType.containsKey(device.getType())) {
            mapByType.get(device.getType()).add(device);
        } else {
            mapByType.put(device.getType(), new ArrayList<>());
            mapByType.get(device.getType()).add(device);
        }

        mapById.put(device.getId(), device);
    }

    public void remove(Device device) {
        mapById.remove(device.getId());

        mapByType.get(device.getType()).remove(device);
        if (mapByType.get(device.getType()).size() == 0) {
            mapByType.remove(device.getType());
        }
    }

    public List<Device> getDevicesByType(String type) {
        return mapByType.get(type);
    }

    public Device getDeviceById(int id) {
        return mapById.get(id);
    }

    public Collection<Device> values() {
        return mapById.values();
    }
}
