package synthesis;

import com.google.gson.*;
import synthesis.model.Biochip;
import synthesis.model.Device;
import synthesis.model.DeviceLibrary;
import synthesis.model.Shape;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JSONParser {

    public static List<Biochip> readBiochip(String path) throws FileNotFoundException {
        final JsonParser parser = new JsonParser();
        final JsonElement jsonElement = parser.parse(new FileReader(path));
        List<Biochip> biochips = new ArrayList<>();

        if (jsonElement.isJsonArray()) {
            for (JsonElement biochip : jsonElement.getAsJsonArray()) {
                biochips.add(buildBiochip(biochip.getAsJsonObject()));
            }
        } else if (jsonElement.isJsonObject()) {
            biochips.add(buildBiochip(jsonElement.getAsJsonObject()));
        }

        return biochips;
    }

    public static Biochip buildBiochip(JsonObject archObject) {
        int width;
        int height;
        ArrayList<Pair<Integer, Integer>> inactiveElectrodes = new ArrayList<>();
        ArrayList<Device> devices;

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

        return new Biochip(width, height, inactiveElectrodes, devices);
    }

    public static void saveBiochip(Biochip arch, String filename) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter out = new FileWriter(filename);

        JsonObject archObject = new JsonObject();
        archObject.addProperty("width", arch.getWidth());
        archObject.addProperty("height", arch.getHeight());

        JsonArray inactiveElectrodeArray = new JsonArray();
        for (Pair<Integer, Integer> freeCell : arch.getFreeCells()) {
            JsonArray coordinates = new JsonArray();
            coordinates.add(freeCell.fst);
            coordinates.add(freeCell.snd);
            inactiveElectrodeArray.add(coordinates);
        }
        archObject.add("inactiveElectrodes", inactiveElectrodeArray);

        JsonArray deviceArray = new JsonArray();
        // TODO: save devices
        /*for (synthesis.model.Device device : arch.getDevices()) {
            deviceArray.add(gson.toJsonTree(device));
        }*/
        archObject.add("devices", deviceArray);

        out.write(gson.toJson(archObject));
        out.flush();
        out.close();
    }

    public static DeviceLibrary readDeviceLibrary(String path) {
        DeviceLibrary deviceLibrary = new DeviceLibrary();

        try {
            final JsonParser parser = new JsonParser();
            final JsonElement jsonElement = parser.parse(new FileReader(path));
            final JsonArray jsonDevices = jsonElement.getAsJsonArray();

            ArrayList<Device> deviceArrayList = buildDevices(jsonDevices);
            for (Device device : deviceArrayList) {
                deviceLibrary.add(device);
            }

            return deviceLibrary;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static Device buildDevice(JsonObject device) {
        int id = device.get("id").getAsInt();
        int x = device.get("x").getAsInt();
        int y = device.get("y").getAsInt();
        int executionTime = device.get("executionTime").getAsInt();
        int cost = device.get("cost").getAsInt();

        String type = device.get("type").getAsString();
        if (device.has("metadata") && device.get("metadata").getAsJsonObject().has("fluid")) {
            type += device.get("metadata").getAsJsonObject().get("fluid").getAsString();
        }

        // Parse shape
        JsonObject shapeObject = device.get("shape").getAsJsonObject();
        int shapeWidth = shapeObject.get("width").getAsInt();
        int shapeHeight = shapeObject.get("height").getAsInt();
        int startX = shapeObject.get("startX").getAsInt();
        int startY = shapeObject.get("startY").getAsInt();
        Shape shape = new Shape(shapeWidth, shapeHeight, startX, startY);

        return new Device(type, id, x, y, executionTime, cost, shape);
    }

    private static ArrayList<Device> buildDevices(JsonArray deviceArray) {
        ArrayList<Device> devices = new ArrayList<>();

        for (int i = 0; i < deviceArray.size(); i++) {
            JsonObject deviceObject = deviceArray.get(i).getAsJsonObject();
            devices.add(buildDevice(deviceObject));
        }

        return devices;
    }

    public static JsonObject convertGraph(String path) throws FileNotFoundException {
        JsonObject root = new JsonObject();
        JsonObject graphJson = new JsonObject();
        JsonArray nodesJson = new JsonArray();
        JsonArray edgesJson = new JsonArray();

        root.add("graph", graphJson);
        graphJson.addProperty("type", "bioprotocol");
        graphJson.addProperty("directed", true);
        graphJson.add("nodes", nodesJson);
        graphJson.add("edges", edgesJson);

        // read from old format
        Scanner scanner = new Scanner(new FileReader(path));
        while(scanner.hasNext()) {
            String element = scanner.next();
            if (element.equalsIgnoreCase("node")){
                JsonObject nodeJson = new JsonObject();
                JsonObject metadata = new JsonObject();
                nodeJson.addProperty("id", scanner.next());
                nodeJson.addProperty("label", scanner.next());
                nodeJson.addProperty("type", scanner.next().toLowerCase());
                metadata.addProperty("fluid", scanner.next());
                if (!metadata.get("fluid").getAsString()
                        .equalsIgnoreCase(nodeJson.get("type").getAsString())) {
                    nodeJson.add("metadata", metadata);
                }
                nodesJson.add(nodeJson);
            } else if (element.equalsIgnoreCase("edge")) {
                JsonObject edgeJson = new JsonObject();
                JsonObject metadata = new JsonObject();
                metadata.addProperty("label", scanner.next());
                edgeJson.addProperty("source", scanner.next());
                edgeJson.addProperty("target", scanner.next());
                edgeJson.add("metadata", metadata);
                edgesJson.add(edgeJson);
            }
        }
        scanner.close();

        return root;
    }
}
