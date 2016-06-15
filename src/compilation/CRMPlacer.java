package compilation;

/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/ 

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;


public class CRMPlacer {
	public static boolean PRINT = false; 


	Biochip biochip;
	ArrayList<Rectangle> empty_rects; 

	public CRMPlacer(Biochip biochip){
		// the biochip - not a copy, a reference directly to the biochip
		// any modification done by the placer applies to the original biochip as well
		this.biochip = biochip; 

	}

	/**------------------THIS PART FROM HERE WAS ADDED FOR DTIP13 -------------------------------------**/
	/**This method places CRMs on the biochip. No cells need to be occupied, as we consider routing based
	 * synthesis - i.e. any cell can be used for any operation at any time.*/
	public boolean placeCRM(CRMModule CRM){
		if (CRM!=null){
			CRM.occupied++; 
			return true; 
		}
		return false; 
	}

	/** Discards space on CRM when an operation finishes.*/
	public boolean discardCRM(CRMModule CRM){
		if (CRM!=null){
			CRM.occupied--; 
			return true; 
		}
		return false; 
	}

	/**This method cuts the biochip into restricted rectangles and returns the list of their centroids.
	 * A restricted rectangle is a rectangle bordered either by the biochip border or by defects. 
	 * In our case the defects are the inactive cells. We use the cutting algorithm by Twisselmann, 1999.
	 * A centroid is the geometrical center, situated, for a rectangle, at the intersection of diagonals.
	 * Restricted rectangles are added to the library as CRMs of rectangular shapes.*/
	/*public ArrayList<Point> getCentroids(Biochip biochip, CRMLibrary lib){
		ArrayList<Point> cenList = new ArrayList<Point>(); 
		// start from each defect and expand on horizontal/vertical direction until it hits a border or a defect
		//System.out.println("Getting centroid for the following cells"); 
		for (int i=0; i<biochip.cells.size(); i++){
			Cell crt_cell = biochip.cells.get(i); 

			if (crt_cell.isActive && this.hasInactiveNeighbors(biochip, crt_cell) == true){ // start from a cell adjacent to a defect
				//System.out.print("\n"+  i + " "); 
				//TODO this can be optimized, not every inactive cell needs to be treated, cells can be grouped in rectangles and only their borders considered
				Rectangle rXY = this.expandHV(biochip, i); 
				CRMModule CRMxy = new CRMModule(rXY,biochip, "MIX");
				CRMxy.t0_exe = this.calculateCompletionTime(CRMxy.cells); 
				lib.addCRM(CRMxy); 

				Rectangle rYX = this.expandVH(biochip, i); 
				CRMModule CRMyx = new CRMModule(rYX,biochip, "MIX");
				CRMyx.t0_exe = this.calculateCompletionTime(CRMyx.cells); 
				lib.addCRM(CRMyx); 

				cenList.add(this.getCentroid(biochip, rXY));
				cenList.add(this.getCentroid(biochip, rYX));
			}
		}
		// merge the list of centroids 
		ArrayList<Point> tempCentroidList = new ArrayList<Point>(); 
		tempCentroidList.add(cenList.get(0)); 
		for (int i=1; i<cenList.size(); i++){
			Point p = cenList.get(i); 
			//System.out.println("\nWe compare" + p); 
			boolean contains = false;
			for (int j=0; j<tempCentroidList.size() && !contains; j++){
				Point q = tempCentroidList.get(j);
				//System.out.print(" to " + q); 
				if (p.equals(q)) {
					contains = true; 
				}
			}
			if (contains == false) {
				tempCentroidList.add(new Point(p.x, p.y)); 
			}
		}

		// check the merged list of centroids
		return tempCentroidList; 
	}
	 */

	/** Returns the list of rectangles instead of centroids */
	public ArrayList<Rectangle> getRRs(Biochip biochip, CRMLibrary lib){
		ArrayList<Rectangle> rrList = new ArrayList<Rectangle>(); 
		// start from each defect and expand on horizontal/vertical direction until it hits a border or a defect
		//System.out.println("Getting centroid for the following cells"); 
		for (int i=0; i<biochip.cells.size(); i++){
			Cell crt_cell = biochip.cells.get(i);
			if (crt_cell.isActive && this.hasInactiveNeighbors(biochip, crt_cell) == true){ 
				// start from a cell adjacent to a defect
				//TODO this can be optimized, not every inactive cell needs to be treated, cells can be grouped in rectangles and only their borders considered
				Rectangle rXY = this.expandHV(biochip, i); 
				rrList.add(rXY);
				CRMModule CRMxy = EstimateWCET.estimateWCET_MIX(transformRectangle(rXY, biochip));
				lib.addCRM(CRMxy); 
				
				CRMModule CRMxy_DLT = EstimateWCET.estimateWCET_DILUTION(transformRectangle(rXY, biochip));
				lib.addCRM(CRMxy_DLT); 

				Rectangle rYX = this.expandVH(biochip, i);
				rrList.add(rYX);
				CRMModule CRMyx = EstimateWCET.estimateWCET_MIX(transformRectangle(rXY, biochip));
				lib.addCRM(CRMyx); 
				
				CRMModule CRMyx_DLT = EstimateWCET.estimateWCET_DILUTION(transformRectangle(rXY, biochip));
				lib.addCRM(CRMyx_DLT); 
			}
		}

		if (rrList.size() == 0){
			if (CRMPlacer.PRINT) System.err.println("*!* We have a RECTANGULAR ARCH!"); 
			rrList.add(new Rectangle(biochip.width, biochip.height, 0, 0)); 
		}

		// merge the list of rectangles 
		ArrayList<Rectangle> tempCentroidList = new ArrayList<Rectangle>(); 
		tempCentroidList.add(rrList.get(0)); 
		for (int i=1; i<rrList.size(); i++){
			Rectangle p = rrList.get(i); 
			//System.out.println("\nWe compare" + p); 
			boolean contains = false;
			for (int j=0; j<tempCentroidList.size() && !contains; j++){
				Rectangle q = tempCentroidList.get(j);
				//System.out.print(" to " + q); 
				if (p.equals(q)) {
					contains = true; 
				}
			}
			if (contains == false) {
				tempCentroidList.add(new Rectangle(p)); 
			}
		}	

		// check the merged list of centroids
		return tempCentroidList; 
	}

	/** Calculate the mixing cost for rectangular CRMs. */
	public double calculateCompletionTime(Rectangle r){
		double move_t = 0.01; // in seconds, the time needed to move the droplet from one electrode to another
		// a formula to calculate for one-dimensional rectangles, as the method used in ComputeWCET does not work
		// TODO fix the method to work for one dimensional rectangles as well
		// TODO fix even the formula for cases when r= 1x2
		int length = 0; // the dimension that is not 1
		if (r.width == 1) length = r.height; 
		if (r.height == 1) length = r.width;
		if (length > 0){
			double time = 0; 
			double cycle_p; 
			if (length <=2){
				cycle_p = -0.5 + (length * 0.29); 
			}else cycle_p = -0.5 + ((length - 2) * 0.58); // percentage of mixing per cycle
			time = (Math.floor(100/cycle_p) + 1) * (length - 1) * move_t; 
			return time; 
		}
		ComputeWCET comp = new ComputeWCET(); // to calculate the cost for rectangle modules 
		ArrayList<ModuleElement> pattern = comp.generateMixPattern(new Module(r.width, r.height, 0), new ModuleElement(0, 0), "ROUNDTHECLOCK", 0); 
		//System.out.println(" w = " + r.width + " h =" + r.height +  " size = " + pattern.size()); 
		return pattern.size() * move_t; 
	}

	/**Checks if the current cell had inactive neighbors. Returns true if one of the neighbors are inactive, and false otherwise.
	 * Neighbors are in this case only the reachable electrodes (north, south, east, west), not the diagonal ones.*/
	public boolean hasInactiveNeighbors(Biochip biochip, Cell crt_cell){
		int x = biochip.findRow(crt_cell);
		int y = biochip.findColumn(crt_cell); 
		// check the border limits
		if (x>1 && biochip.getCell(x-1, y).isActive == false) return true; 
		if (x<biochip.height-1 && biochip.getCell(x+1, y).isActive == false) return true; 
		//if (x>1 && y<biochip.width-1 && biochip.getCell(x-1, y+1).isActive == false) return true; 
		if (y<biochip.width-1 && biochip.getCell(x, y+1).isActive == false) return true; 
		//if (x<biochip.height-1 && y<biochip.width-1 && biochip.getCell(x+1, y+1).isActive == false) return true; 
		//if (x>0 && y>0 && biochip.getCell(x-1, y-1).isActive == false) return true; 
		if (y>0 && biochip.getCell(x, y-1).isActive == false) return true; 
		//if (x<biochip.height-1 && y>0 && biochip.getCell(x+1, y-1).isActive == false) return true; 

		return false; 
	}


	// the extension is done according the matrix notation, where the horizontal coord is the number of row
	// and the vertical coordinate is the number of column. The rectangle is returned by considering the orthogonal notation
	// where the bottom left corner is the (0,0) point and the x is horizontal, and y vertical
	public Rectangle expandHV(Biochip biochip, int i){
		int start_h_coord = biochip.findRow(i); 
		int start_v_coord = biochip.findColumn(i); 

		// expand on HV direction, first on horizontal, then on vertical
		// expansion to the right
		boolean stop = false; 
		int v_coord = start_v_coord; 
		while (!stop){
			v_coord++; 
			// hit border of the biochip
			if (v_coord == biochip.width){
				stop = true; 
				break;
			}
			// hit the defect of the biochip
			if (biochip.getCell(start_h_coord, v_coord).isActive == false)
				stop = true; 
		}
		int v_right = v_coord - 1; 
		// expansion to the left 
		stop = false; 
		v_coord = start_v_coord; 
		while (!stop){
			v_coord--; 
			// hit border of the biochip
			if (v_coord == -1){
				stop = true; 
				break; 
			}
			// hit the defect of the biochip
			if (biochip.getCell(start_h_coord, v_coord).isActive == false)
				stop = true; 
		}
		int v_left = v_coord + 1; 
		// expand on Y direction - rows can go upper and downer
		// extension down
		int h_coord = start_h_coord; 
		boolean stop_down = false; 
		while(!stop_down){
			h_coord--;
			// hit the border of the biochip
			if (h_coord == -1) {
				stop_down = true; 
			} else {
				// hit a defect
				for (int j=v_left; j<=v_right; j++)
					if (biochip.getCell(h_coord, j).isActive == false)
						stop_down = true; 
			}
		}
		int h_down = h_coord +1; 
		// extension up
		boolean stop_up = false; 
		h_coord = start_h_coord; 
		while(!stop_up){
			h_coord++;
			// hit the border of the biochip
			if (h_coord == biochip.height) {
				stop_up = true; 
				break;
			}
			// hit a defect
			for (int j=v_left; j<=v_right; j++)
				if (biochip.getCell(h_coord, j).isActive == false)
					stop_up = true; 
		}
		int h_up = h_coord -1; 
		// return the XY rectangle 
		Rectangle rXY = new Rectangle(v_right-v_left+1, h_up-h_down+1,v_left,biochip.height-h_up-1); 
		return rXY; 
	}


	public Rectangle expandVH(Biochip biochip, int i){
		int start_h_coord = biochip.findRow(i); 
		int start_v_coord = biochip.findColumn(i); 

		// expand on VH direction, first on vertical, then on horizontal
		// expansion up
		boolean stop = false; 
		int h_coord = start_h_coord; 
		while (!stop){
			h_coord++; 
			// hit border of the biochip
			if (h_coord == biochip.height){
				stop = true; 
				break;
			}
			// hit the defect of the biochip
			if (biochip.getCell(h_coord, start_v_coord).isActive == false)
				stop = true; 
		}
		int h_up = h_coord - 1; 
		// expansion down
		stop = false; 
		h_coord = start_h_coord; 
		while (!stop){
			h_coord--; 
			// hit border of the biochip
			if (h_coord == -1){
				stop = true; 
				break; 
			}
			// hit the defect of the biochip
			if (biochip.getCell(h_coord, start_v_coord).isActive == false)
				stop = true; 
		}
		int h_down = h_coord + 1; 
		//System.out.println("limits on vertical " + h_down + " " + h_up); 
		// expand on horizontal direction - rows can to the left or to the right
		// extension left
		int v_coord = start_v_coord; 
		boolean stop_down = false; 
		while(!stop_down){
			v_coord--;
			// hit the border of the biochip
			if (v_coord == -1) {
				stop_down = true; 
			} else {
				// hit a defect
				for (int j=h_down; j<=h_up; j++)
					if (biochip.getCell(j,v_coord).isActive == false)
						stop_down = true; 
			}
		}
		int v_left = v_coord +1; 
		// extension right
		boolean stop_up = false; 
		v_coord = start_v_coord; 
		while(!stop_up){
			v_coord++;
			// hit the border of the biochip
			if (v_coord == biochip.width) {
				stop_up = true; 
				break;
			}
			// hit a defect
			for (int j=h_down; j<=h_up; j++)
				if (biochip.getCell(j, v_coord).isActive == false)
					stop_up = true; 
		}
		int v_right = v_coord -1; 
		//System.out.println("limits on horizontal " + v_left + " " + v_right); 

		// return the XY rectangle 
		Rectangle rXY = new Rectangle(v_right-v_left+1, h_up-h_down+1,v_left,biochip.height-h_up-1); 
		return rXY;  
	}


	/**Returns the centroid of a Rectangle (in matrix notations, (0,0) is the top right corner. */
	public Point getCentroid(Biochip biochip, Rectangle r){
		double x; 
		double y; 
		if (r.height%2 == 0)
			x = r.height/2+0.5; 
		else x = Math.floor(r.height/2) + 1; 
		if (r.width%2 == 0)
			y = r.width/2 + 0.5; 
		else y = Math.floor(r.width/2) + 1; 
		//System.out.println(" x, y " + x + " " + y); 
		double row = biochip.height - (r.y_bl + x); 
		double column = r.x_bl + y -1; 
		return new Point(row, column); 
	}

	/**Filling the grid with a rectangular pattern starting from a previously calculated centroid. 
	 * In case of a (non) integer centroid, that point is marked with 0 and a rectangular area around it is being filled with 1.*/

	public Biochip Fill(Biochip grid, Point centroid){
		//System.out.println("Filling from " + centroid); 
		Biochip filledGrid = new Biochip(grid);
		/* The initialization of filling - there are several case, see bellow.*/
		int last_value = 0; 
		// case 1: both x and y coordinates of centroid are non-integers
		if (Math.floor(centroid.x) != centroid.x && Math.floor(centroid.y) != centroid.y){
			int x_int = (int)centroid.x; 
			int y_int = (int)centroid.y; 
			filledGrid.getCell(x_int, y_int).value = 1; 
			filledGrid.getCell(x_int+1, y_int).value = 1; 
			filledGrid.getCell(x_int, y_int+1).value = 1; 
			filledGrid.getCell(x_int+1, y_int+1).value = 1; 
			for (int w = 0; w<filledGrid.cells.size(); w++ )
				//	if (filledGrid.cells.get(w).value > -1) System.out.print(w + " "); 
				last_value = 1; 
		}

		// case 2: only x coordinate of centroid is non-integers
		if (Math.floor(centroid.x) != centroid.x && Math.floor(centroid.y) == centroid.y){
			int x_int = (int)centroid.x; 
			int y_int = (int)centroid.y; 
			if (filledGrid.getCell(x_int, y_int) != null) filledGrid.getCell(x_int, y_int).value = 1; 
			if (filledGrid.getCell(x_int+1, y_int) != null)filledGrid.getCell(x_int+1, y_int).value = 1; 
			if (filledGrid.getCell(x_int, y_int-1) != null) filledGrid.getCell(x_int, y_int-1).value = 1; 
			if (filledGrid.getCell(x_int+1, y_int-1) != null) filledGrid.getCell(x_int+1, y_int-1).value = 1;
			if (filledGrid.getCell(x_int, y_int+1) != null) filledGrid.getCell(x_int, y_int+1).value = 1; 
			if (filledGrid.getCell(x_int+1, y_int+1) != null) filledGrid.getCell(x_int+1, y_int+1).value = 1;
			last_value = 1; 
		}

		// case 3: only y coordinate of centroid is non-integers
		if (Math.floor(centroid.x) == centroid.x && Math.floor(centroid.y) != centroid.y){
			int x_int = (int)centroid.x; 
			int y_int = (int)centroid.y; 
			filledGrid.getCell(x_int, y_int).value = 1; 
			if (x_int+1 < filledGrid.height)filledGrid.getCell(x_int+1, y_int).value = 1; 
			if (x_int+2 < filledGrid.height) filledGrid.getCell(x_int+2, y_int).value = 1; 
			if (y_int+1 < filledGrid.width) filledGrid.getCell(x_int, y_int+1).value = 1;
			if (y_int+1 < filledGrid.width && x_int+1 < filledGrid.height) filledGrid.getCell(x_int+1, y_int+1).value = 1; 
			if (y_int+1 < filledGrid.width && x_int+2 < filledGrid.height)  filledGrid.getCell(x_int+2, y_int+1).value = 1;
			last_value = 1; 
		}

		//TODO check if this case is needed 
		// case 4: both x and y coordinates of centroid are integers
		if (Math.floor(centroid.x) == centroid.x && Math.floor(centroid.y) == centroid.y){
			int x_int = (int)centroid.x; 
			int y_int = (int)centroid.y; 
			filledGrid.getCell(x_int, y_int).value = 0; 
			// last_value = 0; // redundant, last_value has been initialized with 0
		}


		// fill the whole grid
		boolean stop = false; 
		while(stop == false){
			int total_filled_neighbors = 0; 
			for (int i=0; i<filledGrid.cells.size(); i++){
				Cell crt_cell = filledGrid.cells.get(i);
				stop = true; 
				if (crt_cell.value == last_value){
					int x = filledGrid.findRow(crt_cell);
					int y = filledGrid.findColumn(crt_cell); 
					// check for borders
					if (x>0 && this.fillCell(filledGrid.getCell(x-1, y), last_value+1)) total_filled_neighbors++; 
					if (y>0 && this.fillCell(filledGrid.getCell(x, y-1), last_value+1)) total_filled_neighbors++; 

					if (x<filledGrid.height -1 && this.fillCell(filledGrid.getCell(x+1, y), last_value+1)) total_filled_neighbors++; 
					if (y<filledGrid.width -1 && this.fillCell(filledGrid.getCell(x, y+1), last_value+1)) total_filled_neighbors++; 

					if (y>0 && x>0 && this.fillCell(filledGrid.getCell(x-1, y-1), last_value+1)) total_filled_neighbors++; 
					if (y<filledGrid.width -1 && x<filledGrid.height -1 && this.fillCell(filledGrid.getCell(x+1, y+1), last_value+1)) total_filled_neighbors++; 
					if (y<filledGrid.width -1 && x>0 && this.fillCell(filledGrid.getCell(x-1, y+1), last_value+1)) total_filled_neighbors++; 
					if (x<filledGrid.height -1 && y>0 && this.fillCell(filledGrid.getCell(x+1, y-1), last_value+1)) total_filled_neighbors++; 
				}
			}
			// stop the filling if there are no more neighbors to be filled 
			if (total_filled_neighbors != 0){
				stop = false; 
				last_value++; 
			}
		}

		return filledGrid; 
	}


	public boolean fillCell(Cell cell, int value){
		if (cell.value < 0){
			cell.value = value; 
			return true;
		}
		return false; 
	}


	/** Determine a Circular Route Module (CRM), starting from the determined centroids.
	 * The route is determined by greedy search, which is directed by a given window (max and min value).
	 * The are also limit values for radius (minR and maxR), which depend on the architecture (biochip) itself. 
	 * I store the CRM as a list of cells, sorted in the order they need to be traversed.*/
	public void makeCRMLibrary(Biochip biochip, CRMLibrary lib, int minW, int maxW, int minR, int maxR){
	    // get the list of restricted rectangles 
		ArrayList<Rectangle> rrList = this.getRRs(biochip, lib); 
		int a =0; 
		for (a=0; a<rrList.size(); a++){
			Rectangle r = rrList.get(a); 
			// fill the grid 
			Biochip filledGrid = this.Fill(biochip, this.getCentroid(biochip, r));
			// determine the corresponding CRMs and add them to library
			maxR = Math.max(r.width, r.height); 
			minR = (int)(Math.min(r.height, r.width)/2);
			if (CRMPlacer.PRINT) {
				filledGrid.printFilledGrid(); 
				//System.out.print("RR = " + r.toString() + " centroid = " + this.getCentroid(biochip, r).toString()); 
				//System.out.println(" window = " + maxW + " radius = <" +minR + "," + maxR + ">"); 
			}
			this.getCRM(filledGrid, lib, minW, maxW, minR, maxR); 
			ArrayList<Cell> CRM_rect = transformRectToCRM(r);
			lib.addCRM(EstimateWCET.estimateWCET_MIX(CRM_rect));
			lib.addCRM(EstimateWCET.estimateWCET_DILUTION(CRM_rect));
		}
		// give names/id to each crm stored in library
		for (int x=0; x<lib.CRMList.size(); x++){
			lib.CRMList.get(x).setID("CRM" + x); 
		} 
	}
	
	public ArrayList<Cell> transformRectToCRM(Rectangle r){
		ArrayList<Cell> crm_route = new ArrayList<Cell>();
		int start_x = r.x_bl; // new coordinate system
		int start_y = r.y_bl; 
		// add left side |
		for (int i=0; i<r.height; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x, start_y+i);
		    crm_route.add(c); 
		}
		
		// add top side _
		for (int i=1; i<r.width; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x+i, start_y+r.height-1);
		    crm_route.add(c); 
		}
		
		// add right side |
		for (int i=1; i<r.height; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x + r.width-1, start_y+r.height-1-i);
		    crm_route.add(c); 
		}
		
		// add bottom side -
		for (int i=1; i<r.width-1; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x +r.width - 1 -i, start_y);
		    crm_route.add(c); 
		}
			
		return crm_route; 
	}
	
	public ArrayList<Cell> transformRectToCRM_Matrix(Rectangle r){
		ArrayList<Cell> crm_route = new ArrayList<Cell>();
		int start_x = r.height - 1 - r.y_bl; // new coordinate system
		int start_y = r.x_bl; 
		// add left side |
		for (int i=0; i<r.height; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x-i, start_y);
		    crm_route.add(c); 
		}
		
		// add top side _
		for (int i=1; i<r.width; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x-r.height+1, start_y+i);
		    crm_route.add(c); 
		}
		
		// add right side |
		for (int i=1; i<r.height; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x-r.height+1+i, start_y+r.width-1);
		    crm_route.add(c); 
		}
		
		// add bottom side -
		for (int i=1; i<r.width-1; i++){
			Cell c = new Cell();
		    c.setCoordinates(start_x, start_y+r.width-1-i);
		    crm_route.add(c); 
		}
		System.out.println("\n �����^cells : "); 
		for (Cell ce : crm_route)
			System.out.print(" " + ce.x + "," + ce.y + " ");
		
		return crm_route; 
	}
	public void getCRM(Biochip filledGrid, CRMLibrary lib, int minW, int maxW, int minR, int maxR){

		ArrayList<Cell> CRM = new ArrayList<Cell>(); // best in time execution
		ArrayList<Cell> CRM_area = new ArrayList<Cell>();// best in area occupied

		double completion_time = 10000; // a great number (in seconds)
		int area = 10000; // in electrodes //TODO make it area, as it is now perimeter

		// minim and max for Window 
		minW = 1; 
		//shift within the given window
		for (int radius = maxR; radius>=minR; radius--){
			maxW = radius -1; 
			for (int win = minW; win<=maxW; win++){ 
				//System.out.println("Current values for win = " + win + " radius = " + radius); 
				ArrayList<Cell> crtRoute = this.getGRASPRoute(filledGrid, win, radius);
				// update best-so-far CRM, if exists 
				if (crtRoute != null){
					double crtCompletionTime = EstimateWCET.estimateCompletionTimeNoFaults(crtRoute); 
					//System.out.println("\n Cost = " + crtCompletionTime); 
					if (crtCompletionTime < completion_time){
						completion_time = crtCompletionTime; 
						CRM = new ArrayList<Cell>();
						for (int a = 0; a<crtRoute.size(); a++){
							CRM.add(new Cell(crtRoute.get(a)));
						} 
						//System.out.println("BEST ROUTE: " + IOhandler.printCRM(filledGrid, CRM)); 
					}			
					int crt_area = biochip.getArea(crtRoute); 
					if (crt_area <area) {
						area = crt_area; 
						CRM_area = new ArrayList<Cell>();
						for (int a = 0; a<crtRoute.size(); a++){
							CRM_area.add(new Cell(crtRoute.get(a)));
						} 
						//System.out.println("BEST AREA ROUTE: " + IOhandler.printCRM(filledGrid, CRM_area)); 
					}
				}	
			}
		}
		// add the CRM to the library
		if (CRM != null) lib.addCRM(EstimateWCET.estimateWCET_MIX(CRM)); 
		if (CRM != null) lib.addCRM(EstimateWCET.estimateWCET_DILUTION(CRM)); 
		
		if (CRM_area != null) lib.addCRM(EstimateWCET.estimateWCET_MIX(CRM_area)); 
		if (CRM_area != null) lib.addCRM(EstimateWCET.estimateWCET_DILUTION(CRM_area)); 
	}

	/** GRASP method.*/
	public ArrayList<Cell> getGRASPRoute(Biochip filledGrid, int win, int radius){
		if (radius > filledGrid.height/2 && radius > filledGrid.width/2){
			if (CRMPlacer.PRINT) System.err.print("*ERR!* Radius is too large for biochip size!");
			return null; 
		} else if (CRMPlacer.PRINT) System.out.println("\nradius = " + radius + " window = " + win); 
		
		ArrayList<Cell> CRM = new ArrayList<Cell>();
		double cost = 10000; // a great number (in seconds, as it refers to completion time)
		int max_size = filledGrid.countActiveCells(); // we set a maximum size for the CRM
		for (int i=0; i<filledGrid.cells.size(); i++){
			// start a new CRM
			Cell start_cell = filledGrid.cells.get(i);
			//System.out.print("START: " + start_cell.x + " , " + start_cell.y);
			if (start_cell.isActive && start_cell.value == radius ){
				ArrayList<Cell> crt_CRM = new ArrayList<Cell>();
				crt_CRM.add(start_cell); 
				//System.out.print("\nSTART: " + start_cell.x + "," + start_cell.y); 
				boolean stop = false; 
				while (!stop){
					// pick the last cell added to the Circular Route Module
					Cell crt_cell = crt_CRM.get(crt_CRM.size()-1);
					//System.out.println("crt_cell: " + crt_cell.x + " , " + crt_cell.y); 
					// select the next cell to be part of the CRM
					Cell n = this.selectNeighbor(crt_cell, filledGrid, crt_CRM, win, radius);
					// check if we got a valid neighbor
					if (n == null) {
						//System.out.println("STOP"); 
						crt_CRM.clear(); 
						stop = true; 
						break; 
					}else {
						// we set a maximum size for the CRM
						if (crt_CRM.size()>max_size){
							crt_CRM.clear(); 
							stop = true; 
							break; 
						}
						// check if the CRM is not complete
						if (n.x == start_cell.x && n.y == start_cell.y){
							stop = true; 
						}else { // add the selected neighbor to the CRM
							//System.out.print(" " + n.x + "," + n.y); 
							crt_CRM.add(n);
						}
					}
				}
				if (crt_CRM.size()>0){
					// evaluate the new obtained CRM
					double crt_cost = EstimateWCET.estimateCompletionTimeNoFaults(crt_CRM);
					// print the new CRM
					if (crt_cost<cost) {
						cost = crt_cost; 
						CRM = new ArrayList<Cell>();
						for (int a = 0; a<crt_CRM.size(); a++){
							CRM.add(new Cell(crt_CRM.get(a)));
						}	
					}
				}	
			}
		}
		//System.out.println("\nNEW ROUTE :" + IOhandler.printCRM(filledGrid, CRM)); 
		//System.err.println("COST = " + cost); 
		if (cost < 10000) return CRM; 
		return null; 
	}

	
	public ArrayList<Cell> transformRectangle(Rectangle r, Biochip biochip){
		ArrayList<Cell> arrayOfCells = new ArrayList<Cell>();
		for (int i = biochip.height - r.y_bl - r.height; i<biochip.height - r.y_bl;i++){
			for (int j=r.x_bl; j< r.x_bl + r.width; j++){
				arrayOfCells.add(biochip.cells.get(i*biochip.width + j)); 
			}
		}	
		return arrayOfCells; 
	}

	//TODO: check if the I respected the orthogonal system used (the matrix one, not mathematical)

	public Cell selectNeighbor(Cell crt_cell, Biochip filledGrid, ArrayList<Cell> CRM, int win, int radius){
		// form the neighbor list; neighbors are the 4 electrodes that can be reached through EWOD (not diagonally)
		int x = crt_cell.y; 
		int y = crt_cell.x; 
		//System.out.println(" x = " + x + " y=" + y); 
		Cell neigh1;
		if (x < filledGrid.height-1) {
			if (filledGrid.getCell(x+1, y)!=null){
			if (filledGrid.getCell(x+1, y).isActive && this.isWithinLimits(filledGrid.getCell(x+1, y), win, radius))
				neigh1 = filledGrid.getCell(x+1, y);  
			else neigh1 = null;
			}else neigh1 = null;
		}
		else neigh1 = null;

		Cell neigh2;
		if (x>0) {
			if (filledGrid.getCell(x-1, y)!= null && filledGrid.getCell(x-1, y).isActive &&  this.isWithinLimits(filledGrid.getCell(x-1, y), win, radius))
				neigh2 = filledGrid.getCell(x-1, y); 
			else neigh2 = null; 
		}else neigh2 = null; 


		Cell neigh3;
		if (y<filledGrid.width-1) {
			if (filledGrid.getCell(x, y+1)!=null && filledGrid.getCell(x, y+1).isActive && this.isWithinLimits(filledGrid.getCell(x, y+1), win, radius))
				neigh3 = filledGrid.getCell(x, y+1); else neigh3 = null;
		}else neigh3 = null; 


		Cell neigh4;
		if (y>0) {
			if (filledGrid.getCell(x, y-1)!=null && filledGrid.getCell(x, y-1).isActive && this.isWithinLimits(filledGrid.getCell(x, y-1), win, radius))
				neigh4 = filledGrid.getCell(x, y-1); else neigh4 = null;
		}else neigh4 = null; 

		// avoid going back 
		int prev_x = -1; 
		int prev_y = -1; 
		if (CRM.size()>1) prev_x = CRM.get(CRM.size()-2).x; 
		if (CRM.size()>1) prev_y = CRM.get(CRM.size()-2).y; 

		// return the neighbor that delivers best mixing percentage and respects the conditions
		// TODO: it hurts because it is not optimized :P
		double best_mix_p = 0; 
		double mix_p1, mix_p2, mix_p3, mix_p4; 
		mix_p1 = 0; mix_p2 = 0; mix_p3 = 0; mix_p4 = 0; 
		Cell the_neigh = new Cell();
		if (neigh1!= null){ // && neigh1.isActive && this.isWithinLimits(neigh1, win, radius)){
			// this condition is for not going allowing backward moves, remove it otherwise
			if (neigh1.x!= prev_x || neigh1.y!=prev_y){
				//System.out.println("neihg " + neigh1.x + " " + neigh1.y); 
				mix_p1 = EstimateWCET.calculateMixPercentage(CRM, neigh1, CRM.size());
				if (mix_p1 > best_mix_p) {
					best_mix_p = mix_p1; 
					the_neigh = new Cell(neigh1);
				}
			}
		}

		if (neigh2!= null){// && neigh2.isActive && this.isWithinLimits(neigh2, win, radius)){
			// this condition is for not going allowing backward moves, remove it otherwise
			if (neigh2.x!= prev_x || neigh2.y!=prev_y){
				mix_p2 = EstimateWCET.calculateMixPercentage(CRM, neigh2, CRM.size());
				if (mix_p2 > best_mix_p) {
					best_mix_p = mix_p2; 
					the_neigh = new Cell(neigh2);
				}
			}
		}

		if (neigh3!= null){// && neigh3.isActive && this.isWithinLimits(neigh3, win, radius)){
			// this condition is for not going allowing backward moves, remove it otherwise
			if (neigh3.x!= prev_x || neigh3.y!=prev_y){
				mix_p3 = EstimateWCET.calculateMixPercentage(CRM, neigh3, CRM.size());
				if (mix_p3 > best_mix_p) {
					best_mix_p = mix_p3; 
					the_neigh = new Cell(neigh3);
				}
			}
		}

		if (neigh4!= null){// && neigh4.isActive && this.isWithinLimits(neigh4, win, radius)){
			// this condition is for not going allowing backward moves, remove it otherwise
			if (neigh4.x!= prev_x || neigh4.y!=prev_y){
				mix_p4 = EstimateWCET.calculateMixPercentage(CRM, neigh4, CRM.size());
				if (mix_p4 > best_mix_p) {
					best_mix_p = mix_p4; 
					the_neigh = new Cell(neigh4);
				}
			}
		}

		//System.out.println(" neigh " + the_neigh.x + " " + the_neigh.y + " " + best_mix_p); 

		if (best_mix_p > 0 ) {

			// find out if there is the_other_neigh - a neighbor that has the same mix_p but different coordinates
			Cell the_other_neigh = new Cell();
			if (neigh1 != null){
				if ( mix_p1 == best_mix_p && (neigh1.x!= the_neigh.x || neigh1.y!= the_neigh.y)){
					//System.out.println("other neigh1 + " + neigh1.x + " " + neigh1.y + " mix = " + mix_p1); 
					the_other_neigh = new Cell(neigh1);
				} 
			}

			if (neigh2 != null){
				if ( mix_p2 == best_mix_p && (neigh2.x!= the_neigh.x || neigh2.y!= the_neigh.y)){
					//System.out.println("other neigh2 + " + neigh2.x + " " + neigh2.y + " mix = " + mix_p2); 
					the_other_neigh = new Cell(neigh2);
				}
			}

			if (neigh3 != null){
				if ( mix_p3 == best_mix_p && (neigh3.x!= the_neigh.x || neigh3.y!= the_neigh.y)){
					//System.out.println("other neigh3 + " + neigh3.x + " " + neigh3.y + " mix = " + mix_p3); 
					the_other_neigh = new Cell(neigh3);
				} 
			}

			if (neigh4 != null){
				if ( mix_p4 == best_mix_p && (neigh4.x!= the_neigh.x || neigh4.y!= the_neigh.y)){
					//System.out.println("other neigh + " + neigh4.x + " " + neigh4.y + " mix = " + mix_p4); 
					the_other_neigh = new Cell(neigh4);
				}  	
			}


			if (the_other_neigh.x>0){ // equivalent to != null 
				//System.out.println("here best_mix " + best_mix_p + " the other neigh " + the_other_neigh.x + " " + the_other_neigh.y); 
				Random randomGenerator = new Random();
				int randInt = randomGenerator.nextInt(100);
				if (randInt % 2 == 0) return the_other_neigh; 
			}
			return the_neigh; 
		}
		return null; 
	}

	/**Checks if the current cell (crt_cell) has the value within the required limits.*/
	public boolean isWithinLimits(Cell cell, int win, int radius){
		if (cell.isActive && (radius-win<=cell.value) && (cell.value<=radius))
			return true; 
		return false; 
	}

	
	


	/** This is for storage. I need to be able to place a 3x3 module, which is equivalent with finding a restricted rectangle of max 3 width or height.*/
	public Rectangle getRRMax(Biochip biochip, int maxW, int maxH){

		// start from each defect and expand on horizontal/vertical direction until it hits a border or a defect
		// this algorithm is modified to find storage places, the cells used by crt CRMs have been marked as inactive in a previous step
		for (int i=0; i<biochip.cells.size(); i++){
			Cell crt_cell = biochip.cells.get(i);

			if (crt_cell.isActive && this.hasInactiveNeighbors(biochip, crt_cell) == true){ // start from a cell adjacent to a defect
				//System.out.print("\n"+  i + " "); 
				//TODO this can be optimized, not every inactive cell needs to be treated, cells can be grouped in rectangles and only their borders considered
				Rectangle rXY = this.expandHV(biochip, i); 
				Rectangle rYX = this.expandVH(biochip, i); 
				// check if the rectangle is big-enough to store a droplet
				if (rXY.width >=maxW && rXY.height>=maxH)
				{
					// we found a storage place - stop the search 
					Rectangle r = new Rectangle(maxW, maxH, rXY.x_bl, rXY.y_bl);
					return r; 
				}
				if (rYX.width >=maxW && rYX.height>=maxH)
				{
					// we found a storage place - stop the search 
					Rectangle r = new Rectangle(maxW, maxH, rYX.x_bl, rYX.y_bl);
					return r; 
				}
			}
		}
		return null; 
	}


	/**------------------ END OF PART ADDED FOR DTIP13 -------------------------------------**/



	public void clean(){
		Rectangle bigChip= new Rectangle(biochip.width, biochip.height, 0,0);
		ListIterator<Rectangle> listIt = empty_rects.listIterator(); 
		while(listIt.hasNext()){
			Rectangle crt_r = listIt.next(); 
			listIt.remove(); 
		}
		listIt.add(bigChip); 
		biochip.clean(); 
		System.out.println(" The chip has one big empty rect " + empty_rects.size()); 

	}

	public Rectangle transCoord(Rectangle old_r, Biochip grid){
		// from biochip mode, like the cartagian system to matrix mode 
		return new Rectangle(old_r.width, old_r.height, (grid.height-old_r.y_bl-1), old_r.x_bl); 
	}

	/* return the time needed to route a droplet from source to destionation on the shortest path*/
	public double getHadlockRoute(Biochip grid, Rectangle source, Rectangle dest){

		// TODO solve this bug when a rect has zero area 
		if (source.getArea()==0) return this.getManhD(source.x_bl, source.y_bl, dest.x_bl, dest.y_bl)*0.01; 
		// view the module as a grid to use Lee algorithm

		// first the trabsformation from cartagian (math) to matrix
		//source = this.transCoord(source, grid);
		//dest = this.transCoord(dest,grid);

		//System.out.println("source = " + source.toString()+ " dest = " + dest.toString() ); 

		int s_x = grid.height - source.y_bl - source.height + (int)Math.floor(source.height/2);
		int s_y = source.x_bl + (int)Math.floor(source.width/2);
		int d_x = grid.height  - dest.y_bl - dest.height + (int)Math.floor(dest.height/2);
		int d_y = dest.x_bl+ (int)Math.floor(dest.width/2);

		//System.out.println(" Source " + s_x + ", " + s_y + " Dest " + d_x + ", " + d_y); 

		Cell s_cell = grid.getCell(s_x, s_y);
		Cell d_cell = grid.getCell(d_x, d_y);


		//fill the grid - wave expansion
		s_cell.value = 0;
		//double crt_value = 0; 
		boolean done = false; 
		ArrayList<Cell> crt_cells = new ArrayList<Cell>();
		crt_cells.add(s_cell); 
		//for (int q=0; q<2;q++){
		while(!done){
			//crt_value++; 
			ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
			for (int k=0; k<crt_cells.size() && !done; k++){
				Cell crt_cell = crt_cells.get(k);
				ArrayList<Cell> neighbor_cells = this.fillNeighborsHadlock(grid, crt_cell, d_cell);
				//this.printGrid(grid); 
				if(neighbor_cells!=null){
					if (neighbor_cells.size()==0)
						done = true; 
					else new_filled_cells.addAll(neighbor_cells); 
				}
			}
			if (new_filled_cells.size()==0) {
				//System.out.println("No new cells"); 
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
			//System.out.println("This is the case"); 
			return -1; 
		}

		// remember the detour before cleaning the biochip :P 
		double detour = d_cell.value; 
		//System.out.println("detour = " + detour + " manhattan = " + this.getManhD(s_x, s_y, d_x, d_y)); 
		//clean the biochip 
		for(int i=0; i<grid.cells.size(); i++){
			grid.cells.get(i).value = -1; 
		}

		//System.out.println("time for route = " + (this.getManhD(s_x, s_y, d_x, d_y) + 2*detour)*0.01);


		return (this.getManhD(s_x, s_y, d_x, d_y) + 2*detour)*0.01;
	}

	/* return the time needed to route a droplet from source to destionation on the shortest path
	 * THe route is calculated between two CRM (circular-route modules) between their start cells */

	public double getHadlockRoute(Biochip grid, CRMModule source, CRMModule dest){

		int s_x = source.cells.get(0).y; 
		int s_y = source.cells.get(0).x; 
		int d_x = dest.cells.get(0).y; 
		int d_y = dest.cells.get(0).x; 

		Cell s_cell = grid.getCell(s_x, s_y);
		Cell d_cell = grid.getCell(d_x, d_y);
		
		if (s_cell == null || d_cell == null) return ((grid.height + grid.width)*0.01); // high value to make the architecture invalid

		//System.out.println(" Source " + s_cell.toString() + "  " + " Dest " + d_cell.toString()); 


		//fill the grid - wave expansion
		s_cell.value = 0;
		//double crt_value = 0; 
		boolean done = false; 
		ArrayList<Cell> crt_cells = new ArrayList<Cell>();
		crt_cells.add(s_cell); 
		//for (int q=0; q<2;q++){
		while(!done){
			//crt_value++; 
			ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
			for (int k=0; k<crt_cells.size() && !done; k++){
				Cell crt_cell = crt_cells.get(k);
				ArrayList<Cell> neighbor_cells = this.fillNeighborsHadlock(grid, crt_cell, d_cell);
				//this.printGrid(grid); 
				if(neighbor_cells!=null){
					if (neighbor_cells.size()==0)
						done = true; 
					else new_filled_cells.addAll(neighbor_cells); 
				}
			}
			if (new_filled_cells.size()==0) {
				//System.out.println("No new cells"); 
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
			//System.out.println("This is the case"); 
			return -1; 
		}

		// remember the detour before cleaning the biochip :P 
		double detour = d_cell.value; 
		//System.out.println("detour = " + detour + " manhattan = " + this.getManhD(s_x, s_y, d_x, d_y)); 
		//clean the biochip 
		for(int i=0; i<grid.cells.size(); i++){
			grid.cells.get(i).value = -1; 
		}

		//System.out.println("time for route = " + (this.getManhD(s_x, s_y, d_x, d_y) + 2*detour)*0.01);

		return (this.getManhD(s_x, s_y, d_x, d_y) + 2*detour)*0.01;
	}

	public ArrayList<Cell> fillNeighborsHadlock(Biochip grid, Cell cell, Cell dest){
		int i = grid.findRow(cell); 
		int j = grid.findColumn(cell); 
		int d_i = grid.findRow(dest);
		int d_j = grid.findColumn(dest);
		ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
		int cell_manhD= this.getManhD(i, j, d_i, d_j); 
		//System.out.println("fill for : " + i + " " + j); 

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

	public StringBuffer printGrid(Biochip grid){
		//System.out.println(grid.cells.toString()); 
		StringBuffer s = new StringBuffer(" "); 
		for (int i=0; i<grid.height *grid.width; i++){
			if (i % (grid.width) != 0){
				if (grid.cells.get(i).isFaulty) s.append(new String("  X"));
				else
					if (grid.cells.get(i).value>=0)
						s.append(new String("  " + (int)grid.cells.get(i).value));
				//else s.append(new String(" " + (int)grid.cells.get(i).value));
					else s.append(new String("   "));

			} else {
				if (grid.cells.get(i).isFaulty) s.append(new String("\n X"));
				else 
					if (grid.cells.get(i).value>=0)
						s.append(new String("\n " + (int)grid.cells.get(i).value));
					else s.append(new String("\n" + (int)grid.cells.get(i).value));

			}	
		}
		System.out.println("\n Grid " + s); 
		return s; 
	}


	/** Fast Template Placement according to [1] Bargazan .. etc 
	 * This method (KAMER) keeps all maximum empty rectangles (even overlapping) */ 
	public Rectangle placeKAMER(Module m, Node n, DirectedGraph graph){

		Rectangle crt_r = new Rectangle(m); 

		//System.out.println("ERs before KAMER  are: " + this.empty_rects.toString());
		/* GET BEST CANDIDATE */	
		ArrayList<Rectangle> cand_rects = getCandidates(crt_r, n, graph); 
		//System.out.println("Candidates :" + cand_rects.toString()); 

		Rectangle cand =  new Rectangle(selectCandidate(cand_rects, crt_r,n,graph));	
		// rotate the module if the candidate accomodates the rotated version of the module
		//System.out.println(" cand " + cand.toString()); 
		if ((cand.width < crt_r.width) || (cand.height < crt_r.height)){
			int aux = m.width; 
			m.width = m.height; 
			m.height = aux; 
			crt_r.width = m.height; 
			crt_r.height = m.width; 	
		}

		/* PLACE - OCUPPY CELLS */
		//System.out.println("To be placed :" + cand.toString()); 

		crt_r.x_bl = cand.x_bl; 
		crt_r.y_bl = cand.y_bl; 
		// FOR MODULE BASED 
		//crt_r.width = m.width + 2;  // update the width and height in case the module is rotated 
		//crt_r.height = m.height + 2; 
		// FOR DROPLET BASED SYNTH
		crt_r.width = m.width;  // update the width and height in case the module is rotated 
		crt_r.height = m.height; 

		//System.out.println("To be placed :" + crt_r.toString()); 

		this.biochip.occupy(crt_r);

		//System.out.println("Place " + n.data.name + " module" + crt_r.toString() + this.biochip.toString()); 


		/*HARDCODED FOR TEST TODO: REMOVE*/
		/*	this.biochip.free(crt_r);
		Rectangle crt_r1= new Rectangle(3,3,3,2);  
		Rectangle crt_r2= new Rectangle(6,3,0,5);  
		for (int kk=0; kk<2; kk++){
			if (kk==0)crt_r = new Rectangle(crt_r1); 
			if (kk==1)crt_r = new Rectangle(crt_r2); 
		//System.out.println("KAMER for "+crt_r.toString());
		//System.out.println("ERs before are: " + this.empty_rects.toString());
		this.biochip.occupy(crt_r);
		 */
		/* DIVDE FREE SPACE
		 * First, we devide the free space obtaining the maximum empty rectangles on each empty rectangle
		 * that has to 'host' module m
		 * The empty_rects list is traversed backwards, because all the new rects are added at the end of the list
		 * That way, the adding of new elements (i.e. empty_rects is modified) will not interfere with the traversal of initial empty_rects */
		for (int i= this.empty_rects.size()-1; i>=0; i--){
			Rectangle crt_ER = this.empty_rects.get(i); 
			Rectangle top_r = new Rectangle(); 
			Rectangle bottom_r = new Rectangle();
			Rectangle right_r = new Rectangle();
			Rectangle left_r = new Rectangle();
			if (crt_ER.contains(crt_r)){
				//System.out.println(" ER that contains is " + crt_ER.toString()); 
				top_r = this.divideTop(crt_ER, crt_r); 
				//System.out.println(" TOP is " + top_r.toString()); 

				bottom_r = this.divideBottom(crt_ER, crt_r); 
				//System.out.println(" BOTTOM is " + bottom_r.toString()); 

				right_r = this.divideRight(crt_ER, crt_r); 
				//System.out.println(" RIGTH is " + right_r.toString()); 

				left_r = this.divideLeft(crt_ER, crt_r); 
				//System.out.println(" LEFT is " + left_r.toString()); 
				if (left_r.getArea()==0 && top_r.getArea()==0 && bottom_r.getArea()==0
						&& right_r.getArea()==0){
					// artificiu: instead of deleting add a non-usefull rect
					crt_ER.set(new Rectangle(0,0,0,0)); 
				}


			} else {
				// if intersects 
				Rectangle intersect_r = crt_ER.intersect(crt_r);
				if (intersect_r != null){
					//System.out.println(" ER that intersects is " + crt_ER.toString()); 
					//System.out.println(" Intersection R is " + intersect_r.toString()); 
					//System.out.println(" ER that intersects is " + crt_ER.toString()); 
					top_r = this.divideTop(crt_ER, intersect_r); 
					//System.out.println(" TOP is " + top_r.toString()); 
					bottom_r = this.divideBottom(crt_ER, intersect_r); 
					//System.out.println(" BOTTOM is " + bottom_r.toString()); 

					right_r = this.divideRight(crt_ER, intersect_r);
					//System.out.println(" RIGTH is " + right_r.toString()); 

					left_r = this.divideLeft(crt_ER, intersect_r); 
					//System.out.println(" LEFT is " + left_r.toString()); 

				}
			}

			// instead of deleting crt_ER - which is defficult because we are also adding to the list
			// I just replace it with the first valid rect
			boolean isReplaced = false; 
			if (top_r.getArea()>0) {
				crt_ER.set(top_r);
				isReplaced = true; 
			}
			if (bottom_r.getArea()>0) {
				if (isReplaced == false){
					crt_ER.set(bottom_r);
					isReplaced = true; 
				} 
				else 
					empty_rects.add(bottom_r); 
			}
			if (right_r.getArea()>0) {
				if (isReplaced == false){
					crt_ER.set(right_r);
					isReplaced = true; 
				} 
				else 
					empty_rects.add(right_r); 
			}
			if (left_r.getArea()>0) {
				if (isReplaced == false){
					crt_ER.set(left_r);
					isReplaced = true; 
				} 
				else 
					empty_rects.add(left_r); 
			}	
		}
		/* MERGE TO GET MERs
		 * Second, we merge all the ERs created by dividing the free space, so that we get only the maximum ones (MERs)
		 * For that, we sort the ER list by the area, and then traverse it. If one rectangle is contained by a bigger one,
		 * then we delete it from the list */
		Collections.sort(this.empty_rects, new Comparator<Rectangle>(){
			public int compare(Rectangle o1, Rectangle o2) {
				if (o1.getArea() > o2.getArea())  return 1; 
				else 	if (o1.getArea() < o2.getArea())	return -1; 
				else return 0; }});
		ListIterator<Rectangle> listIt = empty_rects.listIterator();
		int index = -1; 
		while (listIt.hasNext()){ 
			Rectangle crt_ER = listIt.next(); 
			index++; 
			boolean found = false; 
			for (int i=index+1; i<this.empty_rects.size() && found == false; i++){
				Rectangle max_rect = this.empty_rects.get(i); 
				if (max_rect.contains(crt_ER)){
					//System.out.println( " ER " + crt_ER.toString() + " is contained in " + max_rect.toString()); 
					found = true; 
					index--; 
				}
			}				
			if (found) listIt.remove(); 
		}

		/* Then we merge with adjacent rectangles to create Maximum size*/
		// the list is checked several times - until it is completely traversed and no new neighbor is found
		ArrayList<Rectangle> copy_ER = new ArrayList<Rectangle>(empty_rects.size()); 
		for (int i=0; i<this.empty_rects.size(); i++){
			copy_ER.add(empty_rects.get(i)); 
		}

		// System.out.println("ERs  before ADJACENT: " + this.empty_rects.toString());
		for (int i=0; i<copy_ER.size(); i++){
			this.mergeAdjacent(copy_ER.get(i), empty_rects); 
		}	 
		// THIS IS THE LAST MINUTE ADD OF MERGING RECTS - to be removed if it means trouble 
		for (int i=0; i<copy_ER.size(); i++){
			this.mergeIntersectAdjacent(copy_ER.get(i), empty_rects); 
		}
		/* Remove duplicates */
		//System.out.println("ERs  are: " + this.empty_rects.toString());

		HashSet<Rectangle> hs = new HashSet<Rectangle>();
		hs.addAll(empty_rects);
		Set<Rectangle> s = new HashSet<Rectangle>(empty_rects);

		empty_rects.clear();
		empty_rects.addAll(s);	 
		// System.out.println("ERs (merged) after KAMER  are: " + this.empty_rects.toString());
		return crt_r; 
	}



	public ArrayList<Rectangle> getCandidates(Rectangle r, Node n, DirectedGraph graph){
		//System.out.println("Allocate for " + n.toString() +  biochip.toString() + "ER is "  + empty_rects.toString());
		//System.out.println("Allocate for " + n.toString() + "rect = " + r.toString());
		if (((biochip.width >= r.width) && (biochip.height >= r.height)) 
				||
				((biochip.width >= r.height) && (biochip.height >= r.width))){
			// the biochip is big enough to accomodate the module 
			ArrayList<Rectangle> cands = new ArrayList<Rectangle>(); 
			ListIterator<Rectangle> listIt = empty_rects.listIterator(); 
			//System.out.println("ER = " + empty_rects.toString());
			while (listIt.hasNext()){  
				Rectangle r_cand = listIt.next(); 
				//System.out.println("r_cand = " + r_cand.toString() + " r = " + r.toString()); 
				if  (((r_cand.width >= r.width) && (r_cand.height >= r.height)) 
						||
						((r_cand.width >= r.height) && (r_cand.height >= r.width)))
				{
					//System.out.println("cand  to fit in biochip: "  + r_cand.toString()); 
					// check for a route
					if (n.preds.size() == 0) {
						cands.add(r_cand); 
						//System.out.println("cand  to fit in biochip: "  + r_cand.toString()); 
					}
					else{
						//System.out.println("gigi"); 

						double crt_route_t = -1;
						for (int k=0; k<n.preds.size(); k++){
							Node p = graph.getNode(n.preds.get(k)); 

							//System.out.println("p=" + p.toString() + " p.mod=" + p.module.toString()); 

							// to test this part of the code 
							// we look for a route from each predecessor to the rectangle - note the rotating situation 
							if ((r_cand.width >= r.width) && (r_cand.height >= r.height)){
								Rectangle crt_cand = new Rectangle(r.width, r.height, r_cand.x_bl, r_cand.y_bl); 
								//System.out.println("Cautam ruta de la "+ p.toString() + p.module.toString() + " to " + crt_cand.toString()); 	

								crt_route_t = this.getHadlockRoute(biochip, p.module, new Rectangle(r.width, r.height, r_cand.x_bl, r_cand.y_bl));

							}
							else {
								Rectangle crt_cand = new Rectangle(r.height, r.width, r_cand.x_bl, r_cand.y_bl); 
								//System.out.println("Cautam ruta de la "+ p.toString()+p.module.toString() + " to " + crt_cand.toString()); 

								crt_route_t = this.getHadlockRoute(biochip, p.module, new Rectangle(r.height, r.width, r_cand.x_bl, r_cand.y_bl));

							}

							if(crt_route_t<0){
								// test
								Rectangle crt_cand = new Rectangle(r.height, r.width, r_cand.x_bl, r_cand.y_bl); 
								//System.out.println("No route de la  "+ p.module.toString() + " to " + crt_cand.toString() + "crt+route = " + crt_route_t); 
								//System.exit(-1);
								// _end_test

								break;
							}
						}
						if(crt_route_t>=0){ // a route exists
							//System.out.println("Hei"); 
							cands.add(r_cand); 
						}
						//}
					}
				}
			}
			//System.out.println("cands =" + cands.toString()); 

			return cands;
		} else {
			//System.out.println(" Error: the biochip " + biochip.width +"x"+ biochip.height + "is NOT big enough to place Module" + r.toString());
			return null;
		}

	}



	public Rectangle selectCandidate(ArrayList<Rectangle> cands, Rectangle m, Node n, DirectedGraph graph){
		if (cands.size()>0){
			int min_cost = biochip.height * biochip.width + biochip.width; 
			Rectangle cand = new Rectangle(); 
			//TODO Poate fi imbunatatit algoritmul pentru candidati cu costuri egale
			for (int i=0; i<cands.size(); i++){
				int c = calculateCost(biochip, cands.get(i)); 
				//System.out.println("c =" + c + " min_cost = " + min_cost ); 

				if (c <= min_cost){
					min_cost = c;
					cand = cands.get(i); 
					//System.out.println("cand = " + cand); 
				}
			}
			// update the routing time for the operation 
			/*	double route_t = 0; 
			Rectangle r_m = new Rectangle(m.width, m.height, cand.x_bl, cand.y_bl); 
			for (int k=0; k<n.preds.size(); k++){
				Node p = graph.getNode(n.preds.get(k)); 
				double crt_route_t = this.getHadlockRoute(biochip,p.module, r_m); 
				if (route_t < crt_route_t) route_t = crt_route_t; 
			}
			n.data.stop_t += route_t; */

			return cand; 
		} else return null; 
	}

	public ArrayList<Rectangle> getER(){
		// this method returns the little ERs obtained when the biochip has inactive Cells 
		ArrayList<Rectangle> ER = new ArrayList<Rectangle>(); 
		boolean rectangle_arch = true; 

		for (int i=0; i<this.biochip.cells.size(); i++){
			Cell crt_cell = this.biochip.cells.get(i);
			if (!crt_cell.isActive){
				rectangle_arch = false; 
				ModuleElement f_cell = new ModuleElement(this.biochip.findRow(crt_cell), this.biochip.findColumn(crt_cell));
				//System.out.println("crt cell = (" + f_cell.x + " , " + f_cell.y + ")" ); 
				// go around the fault - RIGHT 
				if (f_cell.y+1<this.biochip.width){
					ModuleElement right_n = new ModuleElement(f_cell.x, f_cell.y+1); 
					if(this.biochip.getCell(f_cell.x, f_cell.y+1).isActive){	
						Rectangle right_r = new Rectangle(1,1,right_n.y, this.biochip.height - 1 - right_n.x);
						//System.out.println("Start rect = " + right_r.toString()); 
						// get the scaled ER
						this.shiftRightNEW(right_r,this.biochip); 
						//System.out.println(" After right right rect = " + right_r.toString()); 
						this.shiftBottomNEW(right_r,this.biochip);
						//System.out.println(" After BOTTOM right rect = " + right_r.toString()); 
						this.shiftTopNEW(right_r,this.biochip); 
						//System.out.println(" After up right rect = " + right_r.toString()); 
						if (right_r.height >= 1 && right_r.width >=1){
							ER.add(right_r); 
							//System.out.print(" right_rect "+ right_r); 
						}
					}
				}

				// LEFT
				if (f_cell.y-1 >= 0){
					ModuleElement left_n = new ModuleElement(f_cell.x, f_cell.y-1); 
					if(this.biochip.getCell(f_cell.x, f_cell.y-1).isActive){	
						Rectangle left_r = new Rectangle(1,1,left_n.y, this.biochip.height - 1 - left_n.x);
						// get the scaled ER
						this.shiftLeftNEW(left_r,this.biochip); 
						this.shiftBottomNEW(left_r,this.biochip); 
						this.shiftTopNEW(left_r,this.biochip); 
						if (left_r.height >= 1 && left_r.width >=1){
							ER.add(left_r);
							//System.out.print(" left_r = "+ left_r); 

						}
					}

				}


				// UP
				if (f_cell.x-1>=0){
					ModuleElement up_n = new ModuleElement(f_cell.x-1, f_cell.y); 
					if(this.biochip.getCell(f_cell.x-1, f_cell.y).isActive){	
						Rectangle up_r = new Rectangle(1,1,up_n.y, this.biochip.height - 1 - up_n.x);
						// get the scaled ER
						this.shiftLeftNEW(up_r, this.biochip); 
						this.shiftRightNEW(up_r, this.biochip); 
						this.shiftTopNEW(up_r, this.biochip); 
						if (up_r.height >=1 && up_r.width >=1){
							ER.add(up_r);
							//System.out.print(" up_r =  "+ up_r); 

						}
						//System.out.println("UP rect = " + up_r.toString()); 
					}
				}
				// DOWN
				if (f_cell.x+1<this.biochip.height){

					ModuleElement down_n = new ModuleElement(f_cell.x+1, f_cell.y);
					//System.out.println("Down cell = " + down_n.toString()); 
					if(this.biochip.getCell(f_cell.x+1, f_cell.y).isActive){	
						Rectangle down_r = new Rectangle(1,1,down_n.y, this.biochip.height - 1 - down_n.x);
						//System.out.println("Start rect = " + down_r.toString()); 
						// get the scaled ER
						this.shiftLeftNEW(down_r,this.biochip);
						//System.out.println(" After Left Down rect = " + down_r.toString()); 
						this.shiftRightNEW(down_r,this.biochip);
						//System.out.println(" After Right Down rect = " + down_r.toString()); 
						this.shiftBottomNEW(down_r,this.biochip); 
						//System.out.println(" After Down Down rect = " + down_r.toString()); 
						if (down_r.height >= 1 && down_r.width >=1){
							ER.add(down_r);
							//System.out.println(" down_r = "+ down_r); 

						}

					}
				}
			}
		}

		if (rectangle_arch){
			Rectangle r= new Rectangle(biochip.width,biochip.height, 0, 0); 
			ER.add(r); 
		}
		return ER; 

	}

	/*This new ones know how to avoid the inactive cells. The previous ones are good with rectangles
	 * and regular shapes. TODO: I should check if this new ones are good for the others as well*/
	public void shiftBottomNEW(Rectangle crt_r, Biochip biochip){
		for (int y = crt_r.y_bl; y>0; y--){
			int x = crt_r.x_bl; 
			int column = x ; 
			int row = biochip.height - y-1;
			//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column); 
			if (!biochip.getCell(row,column).isUsable()){
				crt_r.height += crt_r.y_bl - y -1; 
				crt_r.y_bl = y +1;
				//System.out.println("crt_r = " + crt_r.toString()); 
				return; 
			}else 
				for (int k=0; k< crt_r.width; k++){
					//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column+k); 

					if (!biochip.getCell(row,column+k).isUsable()){
						crt_r.height += crt_r.y_bl - y; 
						crt_r.y_bl = y;
						return; 
					}
				}
		}

		// I got to the bottom edge of the biochip 
		crt_r.height += crt_r.y_bl; 
		crt_r.y_bl = 0;
	}

	public void shiftLeftNEW(Rectangle crt_r, Biochip biochip){
		for (int x = crt_r.x_bl; x>0; x--){
			int y = crt_r.y_bl; 
			int column = x - 1; 
			int row = biochip.height - y -1; 
			//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column); 
			if (!biochip.getCell(row,column).isUsable()){
				crt_r.width = crt_r.width + crt_r.x_bl - x;
				crt_r.x_bl = x; 
				return; 
			}else 
				for (int k=0; k< crt_r.height; k++){
					if (!biochip.getCell(row-k,column).isUsable()){
						crt_r.width = crt_r.width + crt_r.x_bl - x;
						crt_r.x_bl = x; 
						return; 

					}
				}
		}
		// I got to the left edge of the biochip 
		crt_r.width = crt_r.width + crt_r.x_bl;
		crt_r.x_bl = 0; 
	}

	public void shiftTopNEW(Rectangle crt_r, Biochip biochip){
		for (int y = crt_r.y_bl + crt_r.height; y<biochip.height; y++){
			int x = crt_r.x_bl; 
			int column = x; 
			int row = biochip.height - y -1; 
			//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column); 
			if (!biochip.getCell(row,column).isUsable()){
				crt_r.height = y - crt_r.y_bl; 
				return; 
			}else {
				for (int k=0; k< crt_r.width; k++){
					//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column+k); 
					if (!biochip.getCell(row,column+k).isUsable()){
						crt_r.height = y - crt_r.y_bl; 
						return; 
					}
				}

			}
		}
		// I got to the top edge of the biochip
		crt_r.height = biochip.height - crt_r.y_bl; 
	}

	public void shiftRightNEW(Rectangle crt_r, Biochip biochip){
		for (int x = crt_r.x_bl + crt_r.width; x<biochip.width; x++){
			int y = crt_r.y_bl; 
			int column = x; 
			int row = biochip.height - y -1; 
			//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column); 
			if (!biochip.getCell(row,column).isUsable()){
				crt_r.width = x - crt_r.x_bl; 
				return; 
			} else for (int k=0; k< crt_r.height; k++){
				if (!biochip.getCell(row-k,column).isUsable()){
					crt_r.width = x - crt_r.x_bl; 
					return; 
				}
			}

		}
		// I got to the right edge of the biochip
		crt_r.width = biochip.width - crt_r.x_bl; 
	}



	public int calculateCost(Biochip biochip, Rectangle r){
		return ((r.y_bl * biochip.width) + r.x_bl); 
	}

	public Rectangle divideBottom(Rectangle ER, Rectangle m){
		int x = ER.x_bl; 
		int y = ER.y_bl; 
		int w = ER.width; 
		int h = m.y_bl - ER.y_bl; 		
		return new Rectangle(w,h,x,y); 
	}

	public Rectangle divideTop(Rectangle ER, Rectangle m){
		int x = ER.x_bl; 
		int y = m.y_bl + m.height; 
		int w = ER.width; 
		int h = ER.y_bl + ER.height - m.height - m.y_bl; 
		//	System.out.println(" m= " + m.y_bl + " " + m.height + " h = " + h); 

		//		System.out.println(" m= " + m.toString() + " h = " + h); 
		return new Rectangle(w,h,x,y); 
	}

	public Rectangle divideLeft(Rectangle ER, Rectangle m){
		int x = ER.x_bl; 
		int y = ER.y_bl; 
		int w = m.x_bl - ER.x_bl; 
		int h = ER.height; 
		//System.out.println("ER = " + ER.toString() + " m="+ m.toString());
		return new Rectangle(w,h,x,y); 
	}

	public Rectangle divideRight(Rectangle ER, Rectangle m){
		int x = m.x_bl + m.width; 
		int y = ER.y_bl; 
		int w = ER.x_bl + ER.width - m.x_bl - m.width; 
		int h = ER.height; 		
		return new Rectangle(w,h,x,y); 
	}

	public void mergeAdjacent(Rectangle m, ArrayList<Rectangle> empty_rects){
		boolean isDone = false; 
		while (!isDone){
			isDone = true; 
			//System.out.println("New round"); 
			//System.out.println("M rect ? " + m.width + "x" + m.height + " " + m.x_bl + " - " + m.y_bl); 

			ListIterator<Rectangle> listIt = empty_rects.listIterator(); 
			while (listIt.hasNext()){ 
				Rectangle r = listIt.next(); 
				//System.out.println("Empty rect neighbor? " + r.width + "x" + r.height + " " + r.x_bl + " - " + r.y_bl); 

				boolean rIsNeighbor = true; 
				// RIGHT 
				if ((r.height == m.height) && (r.y_bl == m.y_bl) && (r.x_bl == m.x_bl + m.width)) {
					m = new Rectangle(r.width+m.width, m.height, m.x_bl, m.y_bl);
					//System.out.println("RIGHT"); 
				}
				else 
					// TOP
					if (( r.width == m.width) && (r.x_bl == m.x_bl) && (r.y_bl == m.y_bl + m.height)) {
						m = new Rectangle(m.width, m.height + r.height, m.x_bl, m.y_bl);
						//System.out.println("TOP"); 
					}
					else 
						// LEFT 
						if ((r.height == m.height) && (r.y_bl == m.y_bl) && (r.x_bl == m.x_bl - r.width)) 
							m = new Rectangle(r.width + m.width, m.height, r.x_bl, m.y_bl);
						else 
							// BOTTOM
							if (( r.width == m.width) && (r.x_bl == m.x_bl) && (r.y_bl == m.y_bl - r.height)) 
								m = new Rectangle(m.width, r.height + m.height, m.x_bl, r.y_bl); 
							else 
								rIsNeighbor = false; 

				// remove the merged rectangle 
				if (rIsNeighbor) {
					listIt.remove(); 
					isDone = false; 
				}
			}
		}
		// add the big merged empty rectangle to the list
		if (m.hasCoordinatesInBoundaries(biochip.width, biochip.height))
			empty_rects.add(m); 

	}


	/**Merges rects that are intersecting but they are adjacent
	 * They have the same height, but they overlap in width, or the other way around*/
	public void mergeIntersectAdjacent(Rectangle m, ArrayList<Rectangle> empty_rects){
		boolean isDone = false; 
		while (!isDone){
			isDone = true; 
			//System.out.println("New round"); 
			//System.out.println("M rect ? " + m.width + "x" + m.height + " " + m.x_bl + " - " + m.y_bl); 

			ListIterator<Rectangle> listIt = empty_rects.listIterator(); 
			while (listIt.hasNext()){ 
				Rectangle r = listIt.next(); 
				//System.out.println("Empty rect neighbor? " + r.width + "x" + r.height + " " + r.x_bl + " - " + r.y_bl); 
				Rectangle mg_r = m.mergeAdjacent(r);

				// remove the merged rectangle 
				if (mg_r!=null) {
					m = mg_r; 
					listIt.remove(); 
					isDone = false; 
				}
			}
		}
		// add the big merged empty rectangle to the list
		if (m.hasCoordinatesInBoundaries(biochip.width, biochip.height))
			empty_rects.add(m); 

	}




}

