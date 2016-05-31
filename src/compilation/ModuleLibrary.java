
/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/ 
package compilation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Scanner;

public class ModuleLibrary {
	
	HashMap<String,ArrayList<Module>> modLib = new HashMap<String, ArrayList<Module>>(); 

	/** Creates a ModuleLibrary objects from the file libFile. 
	 * Go to readme.txt for notation convention. */
	public ModuleLibrary(String libFile) throws FileNotFoundException{
		Scanner s = new Scanner(new File(libFile)); 
		try{
			while(s.hasNext()){
					String operation = s.next(); 
					int length = s.nextInt();
					int width = s.nextInt(); 
					double time = s.nextDouble(); 
					this.addModule(operation, length, width, time); 
				}
			}catch(Exception e) {
			   	e.printStackTrace();
		}
		s.close();
	}
	
	/** Adds a module (a rectangular area of length and width) into the library.
	 *  Modules are specific for every operation. 
	 *  The time needed for the operation to finish within the given are is also a parameter.
	 *  Example of module: mix 2x5 3 . */
	public void addModule(String operation, int width, int height, double time ){

		Module mod = new Module(width, height, time); 
		ArrayList<Module> modList = (ArrayList<Module>)modLib.get(operation); 
		// if operation is already in the library, add the module to the existing list
		 if (modList != null){
			 modList.add(new Module(width, height ,time)); 
		 } else {
			 //new operation, create an entry in the library for it
			 modList = new ArrayList<Module>(); 
			 modList.add(mod);
			 modLib.put(operation, modList); 
		 }	 
	 }
	
	/** Returns all the modules available in the library for the operation given as argument. */
	public ArrayList<Module> getModuleList(String operation){
		//System.out.println("look for modules for operation: " + operation);
		return (ArrayList<Module>)modLib.get(operation); 
	}
	
	/** Returns only the modules that can finish the operation in less than maxTime and occupy less than maxArea.*/
	public ArrayList<Module> getModuleList(String operation, int maxArea, int maxTime){
		ArrayList<Module> modList = (ArrayList<Module>)modLib.get(operation); 
		ArrayList<Module> resList = new ArrayList<Module>(); 
		ListIterator<Module> listIt = modList.listIterator(); 
		while (listIt.hasNext()){ 
			Module m = listIt.next(); 
			if ((m.time <= maxTime) && (m.getArea() <= maxArea)) resList.add(m); 
		}
		
		return resList; 
	}
	
	
	/** Return the maximum time needed for an operation of type operationType
	 * to be executed. All the available modules are processed and the maximum time 
	 * is returned. If no available modules are found in the library, than 0 is returned.*/
	public double getMaxTime(String operationType){
		ArrayList<Module> modList =  this.getModuleList(operationType);
		//System.out.println("Modules for " + operationType); 
		if ((modList!= null) && (modList.size()>0)){
			double maxTime = modList.get(0).time; 
			for (int i=1; i<modList.size()-1; i++)
				if (maxTime < modList.get(i).time)
					maxTime = modList.get(i).time;
			if (maxTime<0) maxTime =0; // negative value is for N/A time, for ex. for a STORE operation
			return maxTime; 
		}
		else return 0; 
	}
	
	/**For operation op, return the time in which op is executed on an area
	 * of w x h. Returns -1 if no available modules are found in the library.*/
	public double getTime(int w, int h, Operation op){
		ArrayList<Module> modList = (ArrayList<Module>)modLib.get(op.type.toString()); 
		for (int i =0; i<modList.size(); i++){
			Module crtMod = modList.get(i); 
			if (((crtMod.height == h) && (crtMod.width == w)) 
				||
			((crtMod.height == w) && (crtMod.width == h))){
				return crtMod.time; 
			}
		}	
		return -1; 
	}
	
	/** Returns a string with the content of module library*/
	public String toString(){
		return  "\n" + modLib.keySet().toString() + "\n" + modLib.values().toString();
	}
	
	/**This following methods are related to mixing pattern and adjusting execution times to tolerate
	 * permanent faults. This means that the delay (epsilon) corresponding to avoiding the faulty cells is 
	 * calculated and added to the executing time. The droplet-aware synthesis model and mixing estimations are
	 * taken from Elena's thesis.*/
	
	}


class Module{
	int width = 0; 
	int height = 0; 
	double time = 0;
	
	public Module(int width, int height, double time){
		this.width = width;
		this.height = height; 
		this.time = time; 
	}
	
	public Module(Module m){
		this.width = m.width; 
		this.height = m.height; 
		this.time = m.time; 
	}
	
	public String toString(){
		return width + "x" + height + " " + time + "s";  
	}
	
	public int getArea(){
		return this.width * this.height; 
	}
}

/**Not a very inspired name. It is used for calculating the delay when tolerating k permanent faults. 
 * It defines a module element, the x and y coordinates inside a  module. The bottom left corner is 0,0
 * The x axis has a maxim of module.width and the y axis of module.height*/ 
class ModuleElement{
	int x; 
	int y; 
	//boolean marked; 
	ModuleElement(int x, int y){
		this.x = x; this.y=y; 
		//marked = false; 
	}
	public String toString(){
		return "("+ x+ "," + y + ") "; 
	}
}
