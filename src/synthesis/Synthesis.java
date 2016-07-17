package synthesis;

import compilation.Arch;
import compilation.DirectedGraph;
import synthesis.model.Biochip;
import synthesis.model.Device;
import synthesis.model.DeviceLibrary;
import synthesis.model.Shape;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Synthesis {

    private static final boolean DEMO_MODE = false;

    public static void main(String[] args) {
    	// Input files
    	String appName = "ivd";
    	String appFile = "data/input/graphs/" + appName + ".txt";
    	String libFile = "data/input/TS_lib.txt";
    	String devicesFile = "data/input/devices.json";
    	
    	// Architecture parameters
    	int minWidth = 10;
    	int minHeight = 10;
    	
    	// Compilation parameters
    	int deadline = 10;
    	int minWindow = 3;
    	int maxWindow = 3;
    	int minRadius = 10;
    	int maxRadius = 10;
    	
    	//DeviceLibrary deviceLib = JSONParser.readDeviceLibrary(devicesFile);
    	//SnthesisProblem problem = new SynthesisProblem(1, 1, minWidth, minHeight, appFile, libFile, deviceLib);
        Biochip solution;
        try {
            solution = JSONParser.readBiochip("data/arch.json").get(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        //problem.evaluate(solution);

        System.out.println(solution);
        System.out.println(solution.toCompileArchitecture());
        System.out.println(solution.getExecutionTime(appFile, libFile, deadline, minWindow, maxWindow, minRadius, maxRadius));
    }

    public static void waitForEnter() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
