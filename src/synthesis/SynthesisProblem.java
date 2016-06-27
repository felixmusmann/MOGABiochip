package synthesis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;
import synthesis.model.Biochip;
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
    private float highestCost; // gets initialized during creation of initial solutions
    private float highestCostOffspring;

    private DeviceLibrary deviceLibrary;
    private Set<String> requiredDeviceTypes;

    private JsonObject appGraph;

    private int populationSize; // used for iteration estimation
    private int populationIterator;

    private int minWidth;
    private int minHeight;

    private String pathToApp;
    private String pathToLib;

    public SynthesisProblem(int populationSize, int minWidth, int minHeight, String pathToApp, String pathToLib, DeviceLibrary deviceLibrary) {
        this.overallConstraintViolation = new OverallConstraintViolation<>();
        this.numberOfViolatedConstraints = new NumberOfViolatedConstraints<>();
        this.highestCostOffspring = 0;

        this.populationSize = populationSize;
        this.populationIterator = 0;

        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.pathToApp = pathToApp;
        this.pathToLib = pathToLib;
        this.deviceLibrary = deviceLibrary;

        try {
            JsonElement rootElement = JSONParser.convertGraph(pathToApp);
            this.appGraph = rootElement.getAsJsonObject().getAsJsonObject("graph");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.requiredDeviceTypes = getRequiredDeviceTypes();
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
        double overallConstraintViolation = 0;
        int violatedConstraints = 0;
        boolean isConnected = true;
        long duration;

        // REPAIR hole puncher
        BiochipHolePuncher holePuncher = new BiochipHolePuncher();
        // TODO: inject limiting factor
        while (solution.getCost() > highestCost * 1.1) {
            System.out.println(String.format("Solution %.2f over budget.", highestCost * 1.1 - solution.getCost()));
            solution = holePuncher.execute(solution);
        }

        // REPAIR connectivity
        BiochipConnectivity connectivity = new BiochipConnectivity();
        solution = connectivity.execute(solution);

        // CONSTRAINT required devices
        LogTool.startTimer();
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
        duration = LogTool.getTimerMillis();
        LOGGER.finer("Required devices constraint " + duration + " ms");

        // CONSTRAINT filling
        LogTool.startTimer();
        double maxFreeCells = solution.getWidth() * solution.getHeight() * 0.3;
        double numberOfFreeCells = solution.getFreeCells().size();
        if (numberOfFreeCells > maxFreeCells) {
            overallConstraintViolation -= numberOfFreeCells / maxFreeCells;
            violatedConstraints++;
        }
        duration = LogTool.getTimerMillis();
        LOGGER.finer("Filling constraint " + duration + " ms");

        // CONSTRAINT min size
        LogTool.startTimer();
        if (solution.getWidth() < minWidth || solution.getHeight() < minHeight) {
            overallConstraintViolation -= 10;
            violatedConstraints++;
        }
        duration = LogTool.getTimerMillis();
        LOGGER.finer("Minimum size constraint " + duration + " ms");

        // OBJECTIVE cost
        float cost = solution.getCost();
        solution.setObjective(0, cost);
        if (cost > highestCostOffspring) {
            highestCostOffspring = cost;
        }

        // OBJECTIVE execution time
        if (isConnected) {
            double deadline = 10;
            int window = 3;
            int radius = 5;

            LogTool.startTimer();
            solution.setObjective(1, solution.getExecutionTime(pathToApp, pathToLib, deadline, window, window, radius, radius));
            duration = LogTool.getTimerMillis();
            if (duration > 3000) {
                String message = String.format("Calculation of execution time\n\tApp completes in %.2f s\n\tCPU time %d ms", solution.getObjective(1), duration);
                LOGGER.info(message + solution);
            }
        } else {
            solution.setObjective(1, Double.MAX_VALUE);
        }

        this.overallConstraintViolation.setAttribute(solution, overallConstraintViolation);
        this.numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);

        populationIterator++;
        if (populationIterator == populationSize) {
            // all solutions of the offspring got evaluated
            if (highestCostOffspring > highestCost) {
                highestCost = highestCostOffspring;
            }
        }
    }

    @Override
    public BiochipSolution createSolution() {
        long startTime = System.currentTimeMillis();
        BiochipSolution solution = createSolutionFast();
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.finer("Created solution " + duration + " ms");
        LogTool.incrementGeneratedArchitectures(1);

        float cost = solution.getCost();
        if (highestCost < cost) {
            highestCost = cost;
        }

        return solution;
    }

    /*private BiochipSolution createSolutionGood() {
        BiochipSolution solution = new BiochipSolution();
        return solution;
    }*/

    private BiochipSolution createSolutionFast() {
        Random rnd = new Random();
        int noIns = getCountOfOperationType("in");
        int noOpt = getCountOfOperationType("opt");
        int noMix = getCountOfOperationType("mix");

        int moduleArea = 3 * 5;
        int sizeFactor = noOpt > 0 ? noOpt * 2 : noIns > 0 ? noIns / 2 : noMix;
        int size = (int) Math.sqrt(moduleArea * sizeFactor) + 1;

        int rndFactor = rnd.nextInt(11) - 5;
        if (rndFactor > 0 || (rndFactor < 0 && size - 2 > Math.abs(rndFactor))) {
            size += rndFactor;
        }

        BiochipSolution solution = new BiochipSolution(size, size, null, null);
        solution = placeDevices(solution);

        return solution;
    }

    private BiochipSolution createSolutionWorst() {
        Random rnd = new Random();
        int width = minWidth + rnd.nextInt(20);
        int height = minHeight + rnd.nextInt(20);

        BiochipSolution solution = new BiochipSolution(width, height, null, null);
        solution = placeDevices(solution);

        return solution;
    }

    private BiochipSolution placeDevices(BiochipSolution solution) {
        int width = solution.getWidth();
        int height = solution.getHeight();

        // Generate references for electrodes for lookup
        ArrayList<ArrayList<Electrode>> electrodeReferences = new ArrayList<>(width);
        for (int x = 0; x < width; x++) {
            ArrayList<Electrode> column = new ArrayList<>(height);
            for (int y = 0; y < height; y++) {
                column.add((Electrode) solution.getCell(x, y));
            }
            electrodeReferences.add(column);
        }

        // Initialize variables for current position on sides
        int leftY = 0;
        int rightY = 0;
        int topX = 0;
        int bottomX = 0;

        Random rnd = new Random();
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

        return solution;
    }

    private int getCountOfOperationType(String... types) {
        Set<String> typeSet = new TreeSet<>();
        Collections.addAll(typeSet, types);
        int counter = 0;

        for (JsonElement element : appGraph.getAsJsonArray("nodes")) {
            JsonObject node = element.getAsJsonObject();
            String nodeType = node.get("type").getAsString();
            if (typeSet.contains(nodeType)) {
                counter++;
            }
        }

        return counter;
    }

    public Set<String> getRequiredDeviceTypes() {
        Set<String> requiredDeviceTypes = new TreeSet<>();

        for (JsonElement element : appGraph.getAsJsonArray("nodes")) {
            JsonObject node = element.getAsJsonObject();
            String type = node.get("type").getAsString();
            if (node.has("metadata")) {
                JsonObject metadata = node.getAsJsonObject("metadata");
                if (metadata.has("fluid")) {
                    String fluid = metadata.get("fluid").getAsString();
                    requiredDeviceTypes.add(/*type +*/ fluid);
                }
            } else if (type.equalsIgnoreCase("opt")) {
                requiredDeviceTypes.add(type);
            }
        }

        return requiredDeviceTypes;
    }
}
