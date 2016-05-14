import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import javafx.util.Pair;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
                JsonArray electrodeCoordinates = element.getAsJsonArray();
                int x = electrodeCoordinates.get(0).getAsInt();
                int y = electrodeCoordinates.get(1).getAsInt();
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

    public void saveArchitecture(Architecture arch, String filename) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter out = new FileWriter(filename);

        JsonObject archObject = new JsonObject();
        archObject.addProperty("width", arch.getWidth());
        archObject.addProperty("height", arch.getHeight());

        JsonArray inactiveElectrodeArray = new JsonArray();
        for (Electrode inactiveElectrode : arch.getInactiveElectrodes()) {
            JsonArray coordinates = new JsonArray();
            coordinates.add(inactiveElectrode.getX());
            coordinates.add(inactiveElectrode.getY());
            inactiveElectrodeArray.add(coordinates);
        }
        archObject.add("inactiveElectrodes", inactiveElectrodeArray);

        JsonArray deviceArray = new JsonArray();
        for (Device device : arch.getDevices()) {
            deviceArray.add(gson.toJsonTree(device));
        }
        archObject.add("devices", deviceArray);

        out.write(gson.toJson(archObject));
        out.flush();
        out.close();
    }

    private ArrayList<Device> buildDevices(JsonArray deviceArray) {
        ArrayList<Device> devices = new ArrayList<>();

        for (int i = 0; i < deviceArray.size(); i++) {
            JsonObject deviceObject = deviceArray.get(i).getAsJsonObject();

            int id = deviceObject.get("id").getAsInt();
            int x = deviceObject.get("x").getAsInt();
            int y = deviceObject.get("y").getAsInt();
            int executionTime = deviceObject.get("executionTime").getAsInt();
            int cost = deviceObject.get("cost").getAsInt();

            String typeInJson = deviceObject.get("type").getAsString();
            Device.Type type = null;
            switch (typeInJson) {
                case "DISPENSER_SAMPLE":
                    type = Device.Type.DISPENSER_SAMPLE;
                    break;
                case "DISPENSER_BUFFER":
                    type = Device.Type.DISPENSER_BUFFER;
                    break;
                case "DISPENSER_REAGENT":
                    type = Device.Type.DISPENSER_REAGENT;
                    break;
                case "OPTICAL_DETECTOR":
                    type = Device.Type.OPTICAL_DETECTOR;
                    break;
            }

            // Parse shape
            JsonObject shapeObject = deviceObject.get("shape").getAsJsonObject();
            int shapeWidth = shapeObject.get("width").getAsInt();
            int shapeHeight = shapeObject.get("height").getAsInt();
            int snapX = shapeObject.get("snapX").getAsInt();
            int snapY = shapeObject.get("snapY").getAsInt();
            Shape shape = new Shape(shapeWidth, shapeHeight, snapX, snapY);

            devices.add(new Device(type, id, x, y, executionTime, cost, shape));
        }

        return devices;
    }


}
