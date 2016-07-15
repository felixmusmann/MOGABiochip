package compilation;

import java.util.ArrayList;

/**This class represents the library of circular route modules (CRM). A circular route module is basically a route, that goes in circle, with no restrictions on shape.
 * The modules considered in the past were of rectangular shape. CRMs have irregular shapes.
 * The CRMLibrary is a class that stores the CRMs, which have been previously determined. It contains the necessary methods to handle the CRMs.*/
public class CRMLibrary {

	ArrayList<CRMModule> CRMList; 
	
	// constructor
	public CRMLibrary(){
		// TODO: would this work? Do I need to initialize the ArrayList<Cell>() as well? Check the memory reqs in Java. 
		this.CRMList = new ArrayList<CRMModule>(); 
	}
	
	/**This method adds a new CRM in the library.*/
	public void addCRM(CRMModule CRM){
		// add a copy (not shallow, deep) to avoid memory reference issues
		CRMModule cpyCRM = new CRMModule(); 
		for (int i=0; i<CRM.cells.size(); i++){
			cpyCRM.cells.add(CRM.cells.get(i)); 
		}
		cpyCRM.capacity = CRM.capacity;
		cpyCRM.occupied = CRM.occupied; 
		cpyCRM.t0_exe = CRM.t0_exe;
		cpyCRM.t1_exe = CRM.t1_exe; 
		cpyCRM.t2_exe = CRM.t2_exe; 
		cpyCRM.operation = CRM.operation; 
		this.CRMList.add(cpyCRM); 
	}
	
	/** Merge the components of the library (eliminate duplicates).*/
	public void mergeLib(){
		
		ArrayList<CRMModule> tempCentroidList = new ArrayList<CRMModule>(); 
		tempCentroidList.add(this.CRMList.get(0)); 
		for (int i=1; i<this.CRMList.size(); i++){
			CRMModule m = this.CRMList.get(i); 
			boolean contains = false;
			for (int j=0; j<tempCentroidList.size() && !contains; j++){
				CRMModule q = tempCentroidList.get(j);
				
				if (m.equals(q)) {
					contains = true; 
				}
			}
			if (contains == false) {
				tempCentroidList.add(new CRMModule(m)); 
			}
		}
		// now the merged CRMS are in temporary list 
		this.CRMList = new ArrayList<CRMModule>(tempCentroidList.size());
		for (int b =0; b<tempCentroidList.size(); b++){
			this.CRMList.add(tempCentroidList.get(b)); 
		}
		
		
	}
	
	
	
	public String toString(){
		StringBuffer s = new StringBuffer(); 
		for (int c=0; c<this.CRMList.size(); c++){
			//System.out.print( "\n" + c + " "); 
			s.append(this.CRMList.get(c).toString()); 
		}
		return s.toString(); 
	}
	
}

