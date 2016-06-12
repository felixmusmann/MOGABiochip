package synthesis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;
import synthesis.model.Device;
import synthesis.model.DeviceLibrary;
import synthesis.model.Electrode;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Logger;

public class SynthesisProblem implements Problem<BiochipSolution> {

    private final static Logger LOGGER = Logger.getGlobal();

    private OverallConstraintViolation<BiochipSolution> overallConstraintViolation;
    private NumberOfViolatedConstraints<BiochipSolution> numberOfViolatedConstraints;

    private DeviceLibrary deviceLibrary;
    private Set<String> requiredDeviceTypes;

    private int minWidth;
    private int minHeight;

    private String pathToApp;
    private String pathToLib;

    public SynthesisProblem(int minWidth, int minHeight, String pathToApp, String pathToLib, DeviceLibrary deviceLibrary) {
        this.overallConstraintViolation = new OverallConstraintViolation<>();
        this.numberOfViolatedConstraints = new NumberOfViolatedConstraints<>();
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.pathToApp = pathToApp;
        this.pathToLib = pathToLib;
        this.deviceLibrary = deviceLibrary;

        try {
            this.requiredDeviceTypes = getRequiredDeviceTypes(pathToApp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getNumberOfVariables() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return 2;
    }

    @Override
    public int getNumberOfConstraints() {
        return 4;
    }

    @Override
    public String getName() {
        return "Biochip Synthesis Problem";
    }

    @Override
    public void evaluate(BiochipSolution solution) {
        long startTime, duration;

        startTime = System.currentTimeMillis();
        solution.setObjective(0, solution.getCost());
        duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Calculated cost in " + duration + " ms");

        double deadline = 10;
        int window = 3;
        int radius = 5;

        startTime = System.currentTimeMillis();
        solution.setObjective(1, solution.getExecutionTime(pathToApp, pathToLib, deadline, window, window, radius, radius));
        duration = System.currentTimeMillis() - startTime;
        if (duration > 300) {
            LOGGER.warning("Calculated execution time in " + duration + " ms\n" + solution);
        } else {
            LOGGER.info("Calculated execution time in " + duration + " ms");
        }

        evaluateConstraints(solution);
    }

    public void evaluateConstraints(BiochipSolution solution) {
        double overallConstraintViolation = 0;
        int violatedConstraints = 0;
        long startTime, duration;

        // filling constraint
        startTime = System.currentTimeMillis();
        double maxFreeCells = solution.getWidth() * solution.getHeight() * 0.3;
        double numberOfFreeCells = solution.getFreeCells().size();
        if (numberOfFreeCells > maxFreeCells) {
            overallConstraintViolation -= numberOfFreeCells / maxFreeCells;
            violatedConstraints++;
        }
        duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Filling constraint " + duration + " ms");

        // min size constraint
        startTime = System.currentTimeMillis();
        if (solution.getWidth() < minWidth || solution.getHeight() < minHeight) {
            overallConstraintViolation -= 10;
            violatedConstraints++;
        }
        duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Minimum size constraint " + duration + " ms");

        // required devices constraint
        startTime = System.currentTimeMillis();
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
                // TODO: implement repair mechanism?
                overallConstraintViolation -= 100;
                violatedConstraints++;
                break;
            }
        }
        duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Required devices constraint " + duration + " ms");

        // connectivity constraint
        startTime = System.currentTimeMillis();
        if (!solution.isConnected()) {
            overallConstraintViolation -= 100;
            violatedConstraints++;
        }
        duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Connectivity constraint " + duration + " ms");

        this.overallConstraintViolation.setAttribute(solution, overallConstraintViolation);
        this.numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
    }

    @Override
    public BiochipSolution createSolution() {
        long startTime = System.currentTimeMillis();
        Random rnd = new Random();
        int width = minWidth + rnd.nextInt(20);
        int height = minHeight + rnd.nextInt(20);

        BiochipSolution solution = new BiochipSolution(width, height, null, null);
        ArrayList<ArrayList<Electrode>> electrodeReferences = new ArrayList<>(width);
        for (int x = 0; x < width; x++) {
            ArrayList<Electrode> column = new ArrayList<>(height);
            for (int y = 0; y < height; y++) {
                column.add((Electrode) solution.getCell(x, y));
            }
            electrodeReferences.add(column);
        }

        int leftY = 0;
        int rightY = 0;
        int topX = 0;
        int bottomX = 0;

        for (String type : requiredDeviceTypes){
            List<Device> devices = deviceLibrary.getDevicesByType(type);
            Device device = devices.get(rnd.nextInt(devices.size()));
            int startX = device.getStartCell().getX();
            int startY = device.getStartCell().getY();

            // Determine where startX and startY are located
            boolean top = startY == 0;
            boolean right = startX == device.getWidth() - 1;
            boolean bottom = startY == device.getHeight() - 1;
            boolean left = startX == 0;

            // Determine x and y for placing
            int x, y;
            if (right && (leftY + startY) < height) {
                x = electrodeReferences.get(0).get(leftY + startY).getX() - 1;
                y = electrodeReferences.get(0).get(leftY + startY).getY();
                leftY += device.getHeight();
            } else if (left && (rightY + startY) < height) {
                x = electrodeReferences.get(width - 1).get(rightY + startY).getX() + 1;
                y = electrodeReferences.get(width - 1).get(rightY + startY).getY();
                rightY += device.getHeight();
            } else if (top && (bottomX + startX) < width) {
                x = electrodeReferences.get(bottomX + startX).get(height - 1).getX();
                y = electrodeReferences.get(bottomX + startX).get(height - 1).getY() + 1;
                bottomX += device.getWidth();
            } else if (bottom && (topX + startX) < width) {
                x = electrodeReferences.get(topX + startX).get(0).getX();
                y = electrodeReferences.get(topX + startX).get(0).getY() - 1;
                topX += device.getWidth();
            } else {
                LOGGER.warning("Unable to fit all devices on biochip!");
                return createSolution();
            }

            // Place device
            solution.addDevice(new Device(device), x, y);
        }

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Created solution " + duration + " ms");
        LogTool.incrementGeneratedArchitectures(1);
        return solution;
    }

    private static Set<String> getRequiredDeviceTypes(String path) throws FileNotFoundException {
        Set<String> requiredDeviceTypes = new TreeSet<>();
        // JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = JSONParser.convertGraph(path); //jsonParser.parse(path);
        JsonObject graph = rootElement.getAsJsonObject().getAsJsonObject("graph");
        JsonArray nodes = graph.getAsJsonArray("nodes");

        for (JsonElement element : nodes) {
            JsonObject node = element.getAsJsonObject();
            String type = node.get("type").getAsString();
            if (node.has("metadata")) {
                JsonObject metadata = node.getAsJsonObject("metadata");
                if (metadata.has("fluid")) {
                    String fluid = metadata.get("fluid").getAsString();
                    requiredDeviceTypes.add(type + fluid);
                }
            } else if (type.equalsIgnoreCase("opt")) {
                requiredDeviceTypes.add(type);
            }
        }

        return requiredDeviceTypes;
    }
}
