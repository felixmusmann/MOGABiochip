package compilation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Scanner;

public class CRMSyn {

	ArrayList<Node> ckpList = new ArrayList<Node>(); 
	CRMPlacer placer;

	public CRMSyn(Biochip biochip){
		placer = new CRMPlacer(biochip); 
		ckpList = new ArrayList<Node>();
	}

	public CRMSyn(){
		placer = new CRMPlacer(new Biochip(0,0));
		ckpList = new ArrayList<Node>();
	}



	public double getRoutingOverhead(Rectangle start_rect, Rectangle dest_rect){
		// the routing is considered as an overhead proportional with the manhattan distance 
		// between the start point and the destination point 
		// TODO need to check if the rects are inside the biochip /// this is manhattan distance
		return (Math.abs(start_rect.x_bl - dest_rect.x_bl) + Math.abs(start_rect.y_bl - dest_rect.y_bl)) * 0.01; 
	} 

	/**Use Lee algorithm to calculate the shortest path. Fill the grid until it reaches the destination.
	 * Stop when it reaches the destination. Do not retrieve the path, we are interested in the transportation
	 * overhead. Return the overhead (the value of destination *0.01 */
	public double getRoutingOverheadLee(Biochip grid, Rectangle source, Rectangle dest){
		//fill the grid - wave expansion
		Cell s_cell = grid.getCell(source.x_bl, source.y_bl);
		s_cell.value = 0;
		double crt_value = 0; 
		boolean done = false; 
		ArrayList<Cell> crt_cells = new ArrayList<Cell>();
		crt_cells.add(s_cell); 
		while(!done){
			crt_value++; 
			ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
			for (int k=0; k<crt_cells.size() && !done; k++){
				Cell crt_cell = crt_cells.get(k);
				ArrayList<Cell> neighbor_cells = this.fillNeighborsLee(grid, crt_cell, crt_value, dest.x_bl, dest.y_bl);
				if (neighbor_cells == null) return -1; 
				if (neighbor_cells.size()==0){
					done = true; 
					break;// only one path is returned this way
				}
				else new_filled_cells.addAll(neighbor_cells); 
			}
			if (new_filled_cells.size()==0) done = true; 
			crt_cells = new_filled_cells; 
		}

		// print filled biochip 
		grid.printGrid(); 

		return crt_value*0.01;
	}

	// returns the list of the new marked cells, if the destination is reached, then the list has 0 elements
	// also if not cell was further marked, the list has 0 elements
	public ArrayList<Cell> fillNeighborsLee(Biochip grid, Cell cell, double value, int dest_x, int dest_y){
		int i = grid.findRow(cell); 
		int j = grid.findColumn(cell); 
		ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
		//System.out.println("fill for : " + i + " " + j); 

		// right neighbor - only if it has one 
		if (j+1<grid.width){
			Cell right_n = grid.getCell(i, j+1);
			if (right_n.isFaulty==false && right_n.value<0){
				right_n.value = value; 
				if (grid.findColumn(right_n)==dest_y && grid.findRow(right_n)==dest_x){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(right_n);
			}
		}
		// left neighbor - only if it has one
		if (j-1>=0){
			Cell left_n = grid.getCell(i, j-1);
			if (left_n.isFaulty==false && left_n.value<0){
				left_n.value = value; 
				if (grid.findColumn(left_n)==dest_y && grid.findRow(left_n)==dest_x){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(left_n);
			}
		}
		// up neighbor
		if (i-1>=0){
			Cell up_n = grid.getCell(i-1, j);
			if (up_n.isFaulty==false && up_n.value<0){
				up_n.value = value;
				if (grid.findColumn(up_n)==dest_y && grid.findRow(up_n)==dest_x){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(up_n);
			}
		}
		// down neighbor
		if (i+1<grid.height){
			Cell down_n = grid.getCell(i+1, j);
			if (down_n.isFaulty==false && down_n.value<0){
				down_n.value = value; 
				if (grid.findColumn(down_n)==dest_y && grid.findRow(down_n)==dest_x){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(down_n);
			}
		}

		if (new_filled_cells.size()==0)
			return null; 

		//	this.printGrid(grid);
		return new_filled_cells; 

	}




	public void cleanBiochip(ArrayList<Node> runningOpsList, DirectedGraph graph, double crt_time, Biochip biochip, CRMLibrary lib, ArrayList<Node> tempOpsL){
		// check for finished operations and free the space on the biochip 
		ListIterator<Node> listIt = runningOpsList.listIterator(); // these are the operations currently running 
		while(listIt.hasNext()){
			Node v = listIt.next(); 
			if (v.data.stop_t >= crt_time){
				// we free the corresponding CRMModule or device on the biochip
				this.placer.discardCRM(v.CRM); 
				// and unbind the operation - remove it from the CRM's list of current operations 
				v.CRM.opList.remove(v); 
				//System.out.println("Finish " + v.data.name + " CRM");
				//v.CRM.toString(); 

				// remove the finished node from the list
				listIt.remove();
				/** STORE part*/ 
				// create a store operation for each successor 
				// the split/dilution will have to store 2 droplets
				for (int x = 0; x<v.succs.size(); x++){
					//int graph_size = 1; // TODO I do not care about graph size now, as I do not insert the store in the graph
					// but when I will insert the store in the graph, I need to know the graph_size
					Node st_n = new Node("store", "ST_"+v.getName());;// I do not care about graph size now, as I do not insert the store in the graph
					// allocate and bound and place it
					Rectangle storeRR = this.allocateStore(st_n, graph, biochip, lib, crt_time); // we assume there is space for storage since we just freed some
					//System.out.println("Allocate store " + bestMod.toString()); 

					// TODO checking for corner case such as bestMod == null (I forget to add it to mLib). 
					// check if there is space for the store
					if (storeRR == null || storeRR.getArea() == 0){ 
						//System.out.println("Not enough space for storing " + st_n.toString()); 
						return;
						//System.exit(1); 
					}
					// add the store to the temporary operations list
					tempOpsL.add(st_n); 
					//System.out.println("1Create store = " + st_n.getName() + " " + st_n.module.toString()); 
				}

			}
		}
	}

	public Rectangle allocateStore(Node st_n, DirectedGraph graph, Biochip biochip, CRMLibrary lib, double crt_time){
		// copy the grid (biochip) into a temporary variable to not apply modifications 
		Biochip cpyGrid = new Biochip(biochip);
		// mark the cells used by the current CRMs as inactive
		for (int a=0; a<lib.CRMList.size(); a++){
			CRMModule crtCRM = lib.CRMList.get(a);
			if (crtCRM.occupied > 0){
				// mark CRM cells as not free
				for (int b=0; b<crtCRM.cells.size(); b++){
					Cell crtCell = crtCRM.cells.get(b);
					if (cpyGrid.getCell(crtCell.y, crtCell.x)!=null)
						cpyGrid.getCell(crtCell.y, crtCell.x).isActive = false; // to match the algorithm that finds an RR of 3x3 
				}		
			}
		}
		// get a restricted rectangle that has an area of 3x3
		int maxW = 3; 
		int maxH = 3; // for a store, we allocate 3x3 cells TODO can be optimized
		return this.placer.getRRMax(biochip, maxW, maxH);
	}


	// A null value means that either the biochip is too small, or there are no modules in the library 
	// for that specific operation.
	public CRMModule allocate(Node v, Biochip biochip, double crt_time, CRMLibrary lib){
		if ((v.isReconfig()) && lib == null) System.out.println("library is null"); 
		else {
			if (v.isReconfig())
				return allocateReconfig(v,biochip,lib, crt_time); 
		}
		return null;
	}

	/**Done for CRM. Each CRM has a capacity that denotes the number of operations that can execute 
	 * at the same on that specific CRM. */
	public CRMModule allocateReconfig(Node v, Biochip biochip, CRMLibrary lib, double crt_time){
		CRMModule bestM = new CRMModule(); 
		if (v.data.super_type.toString().compareTo("MIX") == 0 || v.data.super_type.toString().compareTo("DILUTION") == 0){
			bestM.t0_exe = 10000; 
			//System.out.println("\nAlocate " + lib);  
			for (int i=0; i<lib.CRMList.size(); i++){
				CRMModule crtCRM = lib.CRMList.get(i); 
				//System.out.println(crtCRM.toString()); 
				// check is it is the right operation and it is not too overloaded
				if (crtCRM.operation.compareTo(v.data.getType().toUpperCase())==0 && crtCRM.hasFreeSpace()){	

					// check if no storage overlap 
					// TODO: this can be optimized in the following way: if a CRM is in use (occupied >0) then it implies it does not overlap a storage since the store locations 
					// are selected so that they avoid the cells currently used by active CRMs
					// now, when a droplet is stored, the corresponding cells are marked as busy .. so all we have to do is to check if the cells are busy
					boolean stop = false; 
					for (int a=0; (a<crtCRM.cells.size() && !stop); a++){
						if (crtCRM.cells.get(a).isFree == false){
							stop = true; 
						}
					}

					if (!stop && (bestM.t0_exe > crtCRM.t0_exe) && (crtCRM.t0_exe >0)){
						// update best-so-far
						bestM = crtCRM; 
					}
				}
			}
			if (bestM.t0_exe < 10000) {
				bestM.occupied++; // allocating - the CRM becomes busy
				bestM.opList.add(v); // binding  
				v.CRM = bestM; // binding

				return bestM; 
			}
			return null;
		} else {
			// for other operations than MIX
			// mark it as busy 
			bestM.occupied++; 
			v.CRM = bestM; 
			//System.out.println( "BestMod is " + bestM.toString() ); 
			return bestM; 
		}
	}

	/** The assumption is that if the device exists in the architecture, then it is place-able (i.e., there is 
	 * for sure space on the biochip to place it. The method gets the free devices, and it updates also the usage time of the device.*/
	public Module allocateNonReconfig(Node v, DirectedGraph graph, Biochip biochip, double crt_time){
		Device dev = biochip.getFreeDevice(v, crt_time);
		return dev; 
	}
	  

	/* return the time needed to route a droplet from source to destionation on the shortest path*/
	public double getHadlockRoute(Biochip grid, Rectangle source, Rectangle dest){
		// view the module as a grid to use Lee algorithm
		int s_x = grid.height -1 - source.y_bl - source.height + (int)Math.floor(source.height/2);
		int s_y = source.x_bl + (int)Math.floor(source.width/2);
		int d_x = grid.height -1 - dest.y_bl - dest.height + (int)Math.floor(dest.height/2);
		int d_y = dest.x_bl+ (int)Math.floor(dest.width/2);
		Cell s_cell = grid.getCell(s_x, s_y);
		Cell d_cell = grid.getCell(d_x, d_y);

		//System.out.println(" Source " + s_x + ", " + s_y + " Dest " + d_x + ", " + d_y); 

		//fill the grid - wave expansion
		s_cell.value = 0;
		//double crt_value = 0; 
		boolean done = false; 
		ArrayList<Cell> crt_cells = new ArrayList<Cell>();
		crt_cells.add(s_cell); 
		while(!done){
			//crt_value++; 
			ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
			for (int k=0; k<crt_cells.size() && !done; k++){
				Cell crt_cell = crt_cells.get(k);
				ArrayList<Cell> neighbor_cells = this.fillNeighborsHadlock(grid, crt_cell, d_cell);
				if(neighbor_cells!=null){
					if (neighbor_cells.size()==0)
						done = true; 
					else new_filled_cells.addAll(neighbor_cells); 
				}
			}
			if (new_filled_cells.size()==0) {
				System.out.println("No new cells"); 
				done = true; 
			}
			crt_cells = new_filled_cells; 
			// sort ascendently the new_filled_cells as the ones with a lower value have to be processed earlier
			Collections.sort(crt_cells, new Comparator<Cell>(){
				public int compare(Cell o1, Cell o2) {
					if (o1.value > o2.value)  return 1; 
					else if (o1.value < o2.value) return -1; else return 0; }});

		}

		//this.printGrid(grid);
		if(d_cell.value<0){ 
			// NO ROUTE
			System.out.println("This is the case"); 
			return -1; 
		}

		return (this.getManhD(s_x, s_y, d_x, d_y) + 2*d_cell.value)*0.01;
	}

	public ArrayList<Cell> fillNeighborsHadlock(Biochip grid, Cell cell, Cell dest){
		int i = grid.findRow(cell); 
		int j = grid.findColumn(cell); 
		int d_i = grid.findRow(dest);
		int d_j = grid.findColumn(dest);
		ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
		int cell_manhD= this.getManhD(i, j, d_i, d_j); 
		System.out.println("fill for : " + i + " " + j); 

		// right neighbor - only if it has one (i,j+1)
		if (j+1<grid.width){
			Cell right_n = grid.getCell(i, j+1);
			if (right_n.isActive && right_n.value<0){
				int right_manhD = this.getManhD(i, j+1, d_i, d_j); 
				if (right_manhD<cell_manhD)	
					right_n.value = cell.value; 
				else right_n.value = cell.value+1; 
				if (i==d_i && j+1==d_j){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(right_n);
			}
		}
		// left neighbor - only if it has one (i,j-1)
		if (j-1>=0){
			Cell left_n = grid.getCell(i, j-1);
			if (left_n.isActive && left_n.value<0){
				int left_manhD = this.getManhD(i, j-1, d_i, d_j); 
				if (left_manhD<cell_manhD)	
					left_n.value = cell.value; 
				else left_n.value = cell.value+1; 
				if (i==d_i && j-1==d_j){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(left_n);
			}
		}
		// up neighbor (i-1, j)
		if (i-1>=0){
			Cell up_n = grid.getCell(i-1, j);
			if (up_n.isActive && up_n.value<0){
				int up_manhD = this.getManhD(i-1, j, d_i, d_j); 
				if (up_manhD<cell_manhD)	
					up_n.value = cell.value; 
				else up_n.value = cell.value+1; 
				if (i-1==d_i && j==d_j){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(up_n);
			}
		}
		// down neighbor (i+1,j)
		if (i+1<grid.height){
			Cell down_n = grid.getCell(i+1, j);
			if (down_n.isActive && down_n.value<0){
				int down_manhD = this.getManhD(i+1, j, d_i, d_j); 
				if (down_manhD<cell_manhD)	
					down_n.value = cell.value; 
				else down_n.value = cell.value+1; 
				if (i+1==d_i && j==d_j){
					return new ArrayList<Cell>();
				}
				else new_filled_cells.add(down_n);
			}
		}

		//this.printGrid(grid);
		if (new_filled_cells.size()==0) return null; 
		return new_filled_cells; 

	}

	public int getManhD(int x1, int y1, int x2, int y2){
		return Math.abs(x1-x2)+Math.abs(y1-y2); 
	}


	// goes through the runningOpsL list and returns the closest stop time
	// if the list is empty, increments the current time 
	public double minStopTime(ArrayList<Node> runningOpsL, double crt_time){
		double minStopT = 1000; 
		if (runningOpsL.size()==0) {
			return crt_time+0.01; 
		}
		for (int i=0; i< runningOpsL.size(); i++){
			Node run_n = runningOpsL.get(i);
			if (run_n.data.stop_t < minStopT)
				minStopT = run_n.data.stop_t; 
		}

		return minStopT; 
	}

	// goes through the readyOp list and returns the closest start time
	// if the list is empty, increments the current time 
	public double minStartTime(ArrayList<Node> readyL, double crt_time){
		double minStopT = 1000; 
		if (readyL.size()==0) {
			return crt_time+1; 
		}
		for (int i=0; i< readyL.size(); i++){
			if (readyL.get(i).data.start_t < minStopT)
				minStopT = readyL.get(i).data.start_t; 
		}

		return minStopT; 
	}

	public void schedule(Node v, Module bestMod, double crt_time){
		// set the start and the stop time for each operation
		v.data.start_t = crt_time; 
		if (v.getOp().getType().toString().compareTo("store") != 0){
			v.data.stop_t = crt_time + bestMod.time; 		
		}
	} 


	public void scheduleWRouting(Node v, double crt_time, double ex_time, DirectedGraph graph, CRMLibrary lib){
		// set the start and the stop time for each operation
		v.data.start_t = crt_time; 
		if (v.getOp().getType().toString().compareTo("store") != 0){
			v.data.stop_t = v.data.start_t + ex_time; 

			// add also the routing time 
			double route_t = 0; 
			for (int i=0; i<v.preds.size(); i++){
				Node p = graph.getNode(v.preds.get(i)); 
				// double crt_route_t = this.getRoutingOverhead(p.module, v.module); 
				// use hadlock instead of manhattan
				if(p.CRM.cells.size()>0 && p.CRM.cells.size()>0){
					double crt_route_t = placer.getHadlockRoute(placer.biochip, p.CRM, v.CRM); 
					//System.out.println("Pred p = " + p.toString() + " " + p.module.toString() + " dist =" + crt_route_t); 
					if (route_t < crt_route_t) route_t = crt_route_t; 
				}
			}
			v.data.stop_t += route_t;
			
			// update overhead if intersecting routes
			CRMModule bestM = v.CRM;
			for (int i=0; i<lib.CRMList.size(); i++){
				CRMModule crtCRM = lib.CRMList.get(i);
				if (crtCRM.occupied>0 && (crtCRM.equals(bestM)==false)) // is being used at the moment
				{
					//crtCRM.toString(); 
					//bestM.toString(); 
					//System.out.println("HERE"); 
					if (bestM.intersects(crtCRM)){
						//System.out.println("INTERSECTION"); 
						if (crtCRM.t0_exe >= bestM.t0_exe){
							// always add the overhead to the slow route so that the fast one would be freed faster
							crtCRM.updateOverhead(crt_time); 
						} else {
							bestM.updateOverhead(crt_time); 
						}
					}
				}
			}
		}
	} 


	public void determineStoresForSense(DirectedGraph graph){
		ArrayList<Node> stores = new ArrayList<Node>(); 
		for (int i=0; i<graph.nodes.size(); i++){
			Node v = graph.nodes.get(i); 
			if (v.getType().toString().compareTo("STORE")==0){
				stores.add(v); 
			}

			if (v.getType().toString().compareTo("SENSE")==0){
				if (v instanceof SenseNode){
					SenseNode sense_n = (SenseNode)v; 
					sense_n.copyStore(stores); 
					stores = new ArrayList<Node>(); 
				}

			}
		}
	}		

	public void discardOldStores(SenseNode sense_n, ArrayList<Node> runningOpsL, int crt_time){
		for (int i=0; i<runningOpsL.size(); i++){
			Node run_n = runningOpsL.get(i);
			if (run_n.getType().toString().compareTo("STORE")==0){
				String run_name = run_n.getName().toString(); 
				for (int j=0; j<sense_n.stores.size(); j++){
					String old_store_name = sense_n.stores.get(j).getName().toString(); 
					if (run_name.compareTo(old_store_name)==0){
						run_n.data.stop_t = crt_time; 
					}
				}
			}

		}
	}

	public double getStartTime(Node n, DirectedGraph graph, double crt_time){
		// node n cannot start until all the predecessors have finished executing 
		// the maximum stop time of n_preds
		double max_time = 0; 
		for (int i=0; i<n.preds.size(); i++){
			double t; 
			if (graph.getNode(n.preds.get(i)).getType().toString().compareTo("STORE")!=0){
				t = graph.getNode(n.preds.get(i)).data.stop_t;
			} else t = crt_time; 
			if (t>max_time) max_time = t; 
		}
		return max_time; 
	}
	

	public Node createStoreNode(Node crt_n){
		Node st_n = new Node("store", "ST_"+crt_n.getName()); //+ "_" + (crt_graph_size+1)); 
		return st_n; 
	}

	public void destroyStores(Node crt_n, ArrayList<Node> tempOpsL, DirectedGraph graph, double crt_time){
		// destroy the stores of the input operations to make more space available on the biochip
		for (int i=0; i<crt_n.preds.size(); i++){
			// look in the tempOpsL and remove the stores for the inputs 
			String pred_name = crt_n.preds.get(i).toString();
			//System.out.println(pred_name + " " + tempOpsL.toString()); 
			String st_name = "ST_"+ pred_name; 
			ListIterator<Node> listIt = tempOpsL.listIterator();
			// some operations (split) have 2 product droplets stored 
			// we need to destroy only one store for each input
			boolean no_store_found = true; 
			while(listIt.hasNext() && no_store_found){
				Node run_st = listIt.next(); 
				if (run_st.getName().toString().compareTo(st_name) == 0){
					// TODO THIS IS RECENTLY ADDED - this is for routing from the storage place, not from the module 
					// this is valid for operations that have finished a while ago
					//System.out.println("Found store " + run_st.toString()); 
					Node p = graph.getNode(crt_n.preds.get(i)); 
					if(p.data.stop_t<crt_time){
						p.module = run_st.module; 		
					}
					//discard (unmark (mark as free) the cells used for storage

					placer.biochip.free(run_st.module);
					no_store_found = false; // stops iteration
					//System.out.println("destroy store " + run_st.toString());
					//System.out.println("MERs after discarding store:"  +this.placer.empty_rects.toString()); 
					listIt.remove(); 

				}
			}
		}
	}

	public void createStores(Node crt_n, ArrayList<Node> tempOpsL, Biochip biochip, CRMLibrary lib, double crt_t, DirectedGraph graph){
		// destroy the stores of the input operations to make more space available on the biochip
		//System.out.println(crt_n); 
		for (int i=0; i<crt_n.preds.size(); i++){
			// allocate stores for all inputs except for IN (dispensing operations)
			Node p = graph.getNode(crt_n.preds.get(i)); 
			if (p.data.super_type.toString().compareTo("IN") != 0){
				Node st_n = new Node("store", "ST_"+crt_n.preds.get(i));			
				// allocate store
				Rectangle bestMod = this.allocateStore(st_n, graph, biochip, lib, crt_t); // we assume there is space for storage since we just freed some
				if (bestMod!=null){
				st_n.module = bestMod; 
				// mark the space on biochip as not free so that no other CRM will use it
				biochip.occupy(bestMod); 
				// add the store to the temporary operations list
				tempOpsL.add(st_n); 
				//System.out.println("Add store " + st_n.toString());
				}
			}
			//System.out.println("2Create store = " + st_n.getName() + " " + st_n.module.toString()); 
		}
	}



	public  void printToFile(String outFile,String msg) 
	throws FileNotFoundException{
		FileOutputStream log_f = new FileOutputStream(outFile,true); // declare a file output object
		PrintStream p = new PrintStream(log_f); // declare a print stream object
		p.print(msg);
	}// end graphToDOT



	/** Transforms the graphs generated with TGFF (synthetic benchmarks) into my format (.txt) */
	public void TGFFtoTXT(String tgff_file) throws IOException{
		// "read" the application graph from input file 
		Scanner s = new Scanner(new File(tgff_file)); 
		StringBuffer out_msg = new StringBuffer(); 
		int node_index = 0; 
		int edge_index = 0; 
		try{
			while(s.hasNext()){
				String element = s.next();
				if (element.indexOf("ARC") == -1){
					// this is node 
					node_index ++; 
					out_msg.append("node " + node_index + " O"+ node_index + " ");
					String super_type = s.next(); 
					if (super_type.compareTo("dis")==0) out_msg.append("IN ");
					else if (super_type.compareTo("dlt")==0)  out_msg.append("DILUTION ");
					else out_msg.append(super_type.toUpperCase() + " "); // super_type with upper case (IN)
					String type = s.next(); // type of operation (like disS/disR)
					if (type.compareTo("dlt")==0)  out_msg.append("dilution\n");
					else out_msg.append(type + "\n"); 
				}else {
					// this is an edge
					edge_index ++; 
					element = s.next(); // the edge identifier 
					element = s.next(); // FROM
					element = s.next(); // the source node t1_source
					String source_n = "O"+ (Integer.parseInt(element.substring(3))+1);
					element = s.next(); // TO
					element = s.next(); // the destination node t1_destination
					String dest_n = "O"+ (Integer.parseInt(element.substring(3))+1);
					element = s.next(); // TYPE
					element = s.next(); // 1
					// write it in my .txt format
					out_msg.append("edge " + "M" + edge_index + " " + source_n + " " + dest_n + "\n"); 		
				}		
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		s.close();

		System.out.print(out_msg); 
		String out_f = tgff_file + ".ONS" ; 
		this.printToFile(out_f, out_msg.toString()); 
	}

	public Device allocateDispensingReservoirs(Node v, Biochip biochip, double crt_time){
		//System.out.println("look for non-reconfigurable devices for" + v.toString() + " in " + biochip.devices.toString() + " at t = " + crt_time);
		return biochip.getFreeDevice(v, crt_time);
	}


}
