package compilation;
/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/ 

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


public class Main {

	public static int K_MAX; 
	public static int DEADLINE; 
	public static double MINUS_CPU_TIME; 
	public static int WINDOW; 
	public static int RADIUS; 
	public static int COST_DisS; 
	public static int COST_DisR; 
	public static int COST_DisB; 	
	public static long timeForTS; // in seconds 
	
	public static void main(String[] args) throws IOException{
		/* Read command line parameters or use the DEFAULT ones. */
		boolean comm_line = true; 
		String appName, appFile, libFile, output_f;
		double deadline; 
		int window, radius; 

		if(comm_line){
			libFile = args[0];
			Main.K_MAX = Integer.parseInt(args[1]); 
			appName = args[2]; 
			deadline = Double.parseDouble(args[3]); 
			window = Integer.parseInt(args[4]);
			radius = Integer.parseInt(args[5]);
			Main.timeForTS = (long)Double.parseDouble(args[6]);
			appFile = "../input/" + appName + ".txt"; 
			output_f= "../output/"+ appName+ "_" + Main.K_MAX + "_" + Main.timeForTS  + "_TS.out";
		}else {	// all this arguments are hard-coded, they are default parameters
			appName = "protein"; 
			libFile = "input/TS_lib.txt"; 
			appFile = "input/" + appName+".txt";
			deadline = 100; // in seconds
			Main.K_MAX = 2; 
			Main.timeForTS = 60; 
			window = 3; 
			radius = 5; 
			output_f= "output/"+ appName+ "_" + Main.K_MAX + "_" + Main.timeForTS  + "_TS.out";
		}
		
		Main.WINDOW = window; 
		Main.RADIUS = radius; 
		Main.DEADLINE = (int)deadline; 
		//IOhandler.writeToLog( Calendar.getInstance().getTime().toString() + " *** Running " + appName.toUpperCase()); //TEST

	}  
	
	public static double compile(Arch inputArch, String appFile, String libFile, double deadline, int minWindow, int maxWindow, int minRadius, int maxRadius ) throws IOException{
		/* Initialize the ARCHITECTURE, the GRAPH and the MODULE LIBRARY*/
		DirectedGraph graph = new DirectedGraph(appFile);
		ModuleLibrary mLib = new ModuleLibrary(libFile);  // DO I need this for anything? 
		
		//System.out.println("Completion time is: " + "");
		
		/* Create the CRM library*/
		CRMSyn synCRM = new CRMSyn(inputArch.biochip); 
		CRMLibrary libCRM = new CRMLibrary();
		synCRM.placer.makeCRMLibrary(inputArch.biochip, libCRM, minWindow, maxWindow, minRadius, maxRadius);
		libCRM.mergeLib(); 
		
		LSPR LS = new LSPR(); 
		double sched_time = LS.LSSynthWRouting_CRM(inputArch.toBiochip(), mLib, libCRM, graph, deadline);
		
		return sched_time; 
	}
}
