import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.tools.javac.util.Pair;

import java.io.FileReader;
import java.util.ArrayList;

public class ArchitectureParser {

    public Architecture readArchitecture(String path) {
        int width;
        int height;
        ArrayList<Pair<Integer, Integer>> inactiveElectrodes = new ArrayList<>();
        ArrayList<Device> devices;

        try {
            final JsonParser parser = new JsonParser();
            final JsonElement jsonElement = parser.parse(new FileReader(path));
            final JsonObject archObject = jsonElement.getAsJsonObject();

            width = archObject.get("width").getAsInt();
            height = archObject.get("height").getAsInt();

            JsonArray inactiveElectrodesArray = archObject.get("inactiveElectrodes").getAsJsonArray();
            for (JsonElement element : inactiveElectrodesArray) {
                JsonObject electrodeCoordinates = element.getAsJsonObject();
                int x = electrodeCoordinates.get("x").getAsInt();
                int y = electrodeCoordinates.get("y").getAsInt();
                inactiveElectrodes.add(new Pair<>(x, y));
            }

            JsonArray deviceArray = archObject.get("devices").getAsJsonArray();
            devices = buildDevices(deviceArray);

            return new Architecture(width, height, inactiveElectrodes, devices);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void saveArchitecture(Architecture arch) {
        // TODO
    }

    private ArrayList<ArrayList<Electrode>> buildElectrodes(int width, int height, JsonArray inactiveCellsArray) {
        // List at index 0 contains active cells, list at index 1 contains inactive cells
        ArrayList<ArrayList<Electrode>> cells = new ArrayList<>(2);

        cells.add(0, new ArrayList<Electrode>(width * height));
        cells.add(1, new ArrayList<Electrode>(width * height));

        // Initialize active list with all cells
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells.get(0).add(new Electrode(x, y));
            }
        }

        // Remove cells from active list and add them to inactive list
        for (int i = 0; i < inactiveCellsArray.size(); i++) {
            JsonObject coordinates = inactiveCellsArray.get(i).getAsJsonObject();
            int x = coordinates.get("x").getAsInt();
            int y = coordinates.get("y").getAsInt();
            Electrode inactiveElectrode = cells.get(0).remove(y * width + x);
            cells.get(1).add(inactiveElectrode);
        }

        return cells;
    }

    private ArrayList<Device> buildDevices(JsonArray deviceArray) {
        ArrayList<Device> devices = new ArrayList<>();

        for (int i = 0; i < deviceArray.size(); i++) {
            JsonObject deviceObject = deviceArray.get(i).getAsJsonObject();

            int id = deviceObject.get("id").getAsInt();
            // int x = deviceObject.get("x").getAsInt();
            // int y = deviceObject.get("y").getAsInt();
            int executionTime = deviceObject.get("executionTime").getAsInt();

            String typeInJson = deviceObject.get("type").getAsString();
            Device.Type type = null;
            switch (typeInJson) {
                case "dispenserSample":
                    type = Device.Type.DISPENSER_SAMPLE;
                    break;
                case "dispenserBuffer":
                    type = Device.Type.DISPENSER_BUFFER;
                    break;
                case "dispenserReagent":
                    type = Device.Type.DISPENSER_REAGENT;
                    break;
                case "opticalDetector":
                    type = Device.Type.OPTICAL_DETECTOR;
                    break;
            }

            JsonObject shapeObject = deviceObject.get("shape").getAsJsonObject();
            Shape shape = new Shape();
            shape.width = shapeObject.get("width").getAsInt();
            shape.height = shapeObject.get("height").getAsInt();

            devices.add(new Device(type, id, executionTime, shape));
        }

        return devices;
    }
}
