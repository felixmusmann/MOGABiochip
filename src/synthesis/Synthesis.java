package synthesis;

import compilation.Arch;
import compilation.DirectedGraph;
import synthesis.model.Biochip;
import synthesis.model.Device;
import synthesis.model.DeviceLibrary;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Synthesis {

    private static final boolean DEMO_MODE = false;

    public static void main(String[] args) {
    	// Input files
    	String appName = "interp_dil";
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
    	
    	DeviceLibrary deviceLib = JSONParser.readDeviceLibrary(devicesFile);
    	SynthesisProblem problem = new SynthesisProblem(minWidth, minHeight, appFile, libFile, deviceLib);
    	BiochipSolution solution = problem.createSolution();
    	
    	Arch my_arch = solution.toCompileArchitecture(); 
    	System.out.println(my_arch.toString()); 
    	
    	try {
    		long cpu_start_t = System.currentTimeMillis(); 
			double time = compilation.Main.compile(solution.toCompileArchitecture(), appFile, libFile, deadline, minWindow, maxWindow, minRadius, maxRadius);
			long cpu_stop_t = System.currentTimeMillis(); 
			System.out.println("CPU time = " + (cpu_stop_t - cpu_start_t) + " ms");
			System.out.println("app completes in " +  time + " s" );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static void waitForEnter() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
