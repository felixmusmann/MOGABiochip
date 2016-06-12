package compilation;

import java.util.ArrayList;

class CRMModule{
	String id; // name of the CRM
	ArrayList<Cell> cells;
	int occupied = 0;
	int capacity = 1;
	double t0_exe = 0; // execution time in seconds
	double t1_exe = 0; // execution time in seconds
	double t2_exe = 0; // execution time in seconds

	String operation; // this is the type of operations, in case we consider CRMs that are specific to an operation
	ArrayList<Node> opList = new ArrayList<Node>(); // this is the list of operations that are currently executed on CRM
	//TODO some kind of check that the operation list size does not exceed the capacity of the CRM

	public CRMModule(){
		this.cells = new ArrayList<Cell>();
	}

	public CRMModule(ArrayList<Cell> CRM, double cost, double t1_exe, double t2_exe, String op){
		this.cells = new ArrayList<Cell>();
		for (int a=0; a<CRM.size(); a++){
			this.cells.add(CRM.get(a));
		}
		this.t0_exe = cost;
		this.t1_exe = t1_exe;
		this.t2_exe = t2_exe;
		this.capacity = 1;
		this.occupied = 0;
		this.operation = new String(op);
		this.opList = new ArrayList<Node>();
	}

	public CRMModule(Rectangle r, Biochip biochip, String op){
		this.cells = new ArrayList<Cell>();
		for (int i = biochip.height - r.y_bl - r.height; i<biochip.height - r.y_bl;i++){
			for (int j=r.x_bl; j< r.x_bl + r.width; j++){
				this.cells.add(biochip.cells.get(i*biochip.width + j));
			}
		}
		this.capacity = 1;
		this.occupied = 0;
		this.operation = new String(op);
		this.opList = new ArrayList<Node>();
		//System.out.println(" XXXX Operation " + this.operation);
	}

	public CRMModule(ArrayList<Cell> CRM, double cost, int capacity, String operation){
		this.cells = new ArrayList<Cell>();
		for (int a=0; a<CRM.size(); a++){
			this.cells.add(CRM.get(a));
		}
		this.t0_exe = cost;
		this.capacity = capacity;
		this.occupied = 0;
		this.operation = new String(operation);
	}

	public CRMModule(CRMModule CRM){
		this.id = new String(CRM.id);
		this.capacity = CRM.capacity;
		this.t0_exe = CRM.t0_exe;
		this.t1_exe = CRM.t1_exe;
		this.t2_exe = CRM.t2_exe;
		this.occupied = CRM.occupied;
		this.operation = new String(CRM.operation);
		this.cells = new ArrayList<Cell>(CRM.cells.size());
		for (int a=0; a<CRM.cells.size(); a++){
			this.cells.add(new Cell(CRM.cells.get(a)));
		}
		this.opList = new ArrayList<Node>();
		//System.out.println(" XXXX Operation " + this.operation);
	}

	// area aproximated to the circumsribing (bounding) rectangle
	public int getArea(){
		// find the limits on the coordinates
		int min_x = this.cells.get(0).x;
		int max_x = this.cells.get(0).x;
		int min_y = this.cells.get(0).y;
		int max_y = this.cells.get(0).y;
		for (int a=0; a<this.cells.size(); a++){
			Cell c = this.cells.get(a);
			if (c.x < min_x) min_x = c.x;
			if (c.x > max_x) max_x = c.x;
			if (c.y < min_y) min_y = c.y;
			if (c.y > max_y) max_y = c.y;
		}
		return ((max_x - min_x) * (max_y - min_y));
	}

	public void setID(String id){
		this.id = new String(id);
	}

	public boolean hasFreeSpace(){
		if ( this.capacity - this.occupied > 0)
			return true;
		return false;
	}

	/** For comparison reasons. Checks if 2 CRMs are equal.*/
	public boolean equals(CRMModule p){
		if (this.capacity == p.capacity && this.operation.compareTo(p.operation)==0 && this.t0_exe == p.t0_exe
				&& this.cells.size() == p.cells.size()){
			boolean found_dif = false;
			for (int a=0; a<this.cells.size() && (!found_dif); a++){
				if (!this.cells.get(a).equals(p.cells.get(a))) found_dif = true;
			}
			if (found_dif == false) return true;
		}
		return false;
	}



	/** Returns true if it intersects at least in one point the CRM given as paramenter*/
	public boolean intersects(CRMModule CRM){
		/*System.out.println("Intersect crm" + this.cost + " ("+ this.cells.get(0).x + " " + this.cells.get(0).y + ") with" + CRM.cost  );
		System.out.println();
		for (int a=0; a<this.cells.size(); a++){
			System.out.print(this.cells.get(a).x + " " + this.cells.get(a).y + " ; ");
		}
		System.out.println();
		for (int a=0; a<this.cells.size(); a++){
			System.out.print(CRM.cells.get(a).x + " " + CRM.cells.get(a).y + " ; ");
		}
		System.out.println();
*/
		for (int a=0; a<this.cells.size(); a++){
			//System.out.println(this.cells.get(a).x + " " +this.cells.get(a).y );
			if (this.cells.get(a).isContained(CRM)){
				//System.out.println("CONTAINS");
				return true;
			}
		}
		return false;
	}



	/** Updates the overhead for operation list, overhead is necessary to avoid merging when 2 CRMS are intersecting*/
	public void updateOverhead(double crt_time){

		for (int b=0; b<this.opList.size(); b++){
			double overhead = ((-crt_time + this.opList.get(b).data.stop_t)/this.cells.size()) * 3;
			this.opList.get(b).data.stop_t += overhead;
			//System.out.println("Update overhead for " + this.opList.get(b).toString() + " overhead = " + overhead + "crt_time=" + crt_time + " stop_t=" + this.opList.get(b).data.stop_t);
		}
	}


	public String toString(){
		//String s = new String("\nCRM: Cost " + this.t0_exe + " size " + this.cells.size() + " occ=" + this.occupied + " cap=" + this.capacity + " start=(" + this.cells.get(0).x + "," + this.cells.get(0).y + ")\n" );
		String s = new String("\nCRM: " + this.id + "Cost " + this.t0_exe + " occ=" + this.occupied + " cap=" + this.capacity);
		System.out.print(s);
		for (int a=0; a<this.cells.size(); a++){
			//System.out.print(this.cells.get(a).x + " " + this.cells.get(a).y + " ; ");
		}
		return s;
	}

	public String printOpList(){
		StringBuffer s = new StringBuffer();
		this.toString();
		s.append(" NODES : ");
		for (int a=0; a<this.opList.size(); a++){
			s.append(" " + this.opList.get(a).data.getName());
		}

		return s.toString();
	}

}
