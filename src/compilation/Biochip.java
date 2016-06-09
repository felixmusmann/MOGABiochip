/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/ 
package compilation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;


public class Biochip {
	
	int width = 0; //no of columns 
	int height = 0; // no of rows 
	ArrayList<Cell> cells; // cells = electrodes - some of them might be inactive
	ArrayList<Device> devices; // stores all devices
	
	public Biochip(int w, int h){
		width = w; 
		height = h; 
		cells = new ArrayList<Cell>(w*h);
		for(int i=0; i<w*h; i++){
			cells.add(new Cell(true));
		}
		devices = new ArrayList<Device>(); 
		for (int i=0; i<cells.size(); i++){
			Cell c = cells.get(i);
			c.x = this.findColumn(c); 
			c.y = this.findRow(c);
		}
	}
	
	public Biochip(Biochip bio){
		width = bio.width; 
		height = bio.height; 
		cells = new ArrayList<Cell>(width*height);
		for(int i=0; i<width*height; i++){
			cells.add(new Cell(bio.cells.get(i)));
		}
		devices = new ArrayList<Device>(); 
		for(int i=0; i<bio.devices.size(); i++){
			devices.add(bio.devices.get(i)); 
		}
	}
	
	/** "Reads" the biochip architecture from input file 
	 * The input file has comment-lines with naming convention */
	public Biochip(String biochipArchFile) throws IOException{
		Scanner s = new Scanner(new File(biochipArchFile)); 
		/* the first line is for comments */
		String comment_line = s.nextLine(); 
		/* read the width and length of the biochip */
		String width = s.next(); 
		this.width = Integer.parseInt(width.substring(6)); 
		String height = s.next(); 
		this.height = Integer.parseInt(height.substring(7));
		/* initialize the cells on the biochip */
		cells = new ArrayList<Cell>(this.width*this.height);
		for(int i=0; i<this.width*this.height; i++){
			cells.add(new Cell());
		}
		devices = new ArrayList<Device>(); 	
		/* parse the next line to mark the inactive cells */
		comment_line = s.nextLine(); // this is comment-line, again
		s.nextLine(); // this line contains the index of inactive cells
		StringBuffer inactive_cells_indexes = new StringBuffer(s.nextLine());
		int stop = inactive_cells_indexes.indexOf(" "); 
		while(inactive_cells_indexes.length()>0){
			if (stop<=0) stop = inactive_cells_indexes.length();
			int index = Integer.parseInt(inactive_cells_indexes.substring(0,stop)); 
			inactive_cells_indexes.delete(0,stop+1);
			if (index >= 0) this.cells.get(index).setInactive(); 
			stop = inactive_cells_indexes.indexOf(" "); 
		}
		/* read the non-reconfigurable devices */
		comment_line = s.nextLine();
		try{
			while(s.hasNext()){
				String element = s.next(); 
				int w = s.nextInt();
				int h = s.nextInt();
				int t = s.nextInt();
				int number = s.nextInt();
				for (int i=1; i<=number; i++ ){
					String name = element.concat(Integer.toString(i)); 
					devices.add(new Device(name,element, w,h,t,number));
				}
			}
		}catch(Exception e) {
			   	e.printStackTrace();
		}
		s.close();
		//System.out.println("Biochip" + this.devices.toString()); 
	}

	/** Mark as busy/occupied the cells that correspond to the rectangle 
	 * No checking is done, the cells are occupied by force. Strongly 
	 * recommended to use this.hasSpace() before calling this.occupy() */
	public void occupy(Rectangle m){
		for (int i = this.height - m.y_bl - m.height; i<this.height - m.y_bl;i++){
			for (int j=m.x_bl; j< m.x_bl + m.width; j++)
					this.cells.get(i*width + j).setBusy(); 
		}
	}
	
	/** Marks the cells on the biochip, corresponding to rectangle m, as free. 
	 * Returns false if the biochip is to small, true otherwise
	 * TODO: do a check for rectangle with out-of-boundary coordinates */
	public boolean free(Rectangle m){
		/* check if the the biochip is big enough to accomodate the module */
		if (((this.width >= m.width) && (this.height >= m.height)) 
				 ||
		   ((this.width >= m.height) && (this.height >= m.width))){
			for (int i = this.height - m.y_bl - m.height; i<this.height - m.y_bl;i++){
				for (int j=m.x_bl; j< m.x_bl + m.width; j++){
					/* mark the corresponding cells as free */
					this.cells.get(i*width + j).setFree(); 
				}
			}
		} else return false; 
		return true; 
	}
	
	/** Resets the biochip by marking all cells as free. */
	public void clean(){
		for (int i=0; i<cells.size(); i++)
			cells.get(i).setFree(); 
	}
	
	/** Returns true if there is room on the biochip for accomodating rectangle r
	 * Returns false if the biochip is too small, or rect r has coordinates outside boundaries, or 
	 * the cells on the biochip are currently not free */
	 public boolean hasSpace(Rectangle m){
		/* check if the the biochip is big enough to accomodate the module */
		if (((this.width >= m.width) && (this.height >= m.height)) 
				 ||
		   ((this.width >= m.height) && (this.height >= m.width))){
			/* check if the coordinates are within biochip boundaries */ 
			if (((0 <= m.x_bl) && (m.x_bl < this.width)) 
				&& 
				((0 <= m.y_bl) && (m.y_bl < this.height))){
				for (int i = this.height - m.y_bl - m.height; i<this.height - m.y_bl;i++){
					for (int j=m.x_bl; j< m.x_bl + m.width; j++){
						/* check if the wanted cells are free */
						if (!this.cells.get(i*width + j).isUsable())
							return false;
				    }
				}
			}
		} else return false; 	
		return true; 
	}
	
	/** Returns the available device that can be placed, for node n. 
	 * Returns null if no available devices in library or if the any of the devices cannot be placed. 
	 * The returned device is marked as 'used', i.e. it is bound to node. The device stop_t is updated. 
	 * The stop_t indicates the next time when the device is free for use*/
	public Device getFreeDevice(Node n, double crt_time){
		ListIterator<Device> listIt = this.devices.listIterator(); 
		while(listIt.hasNext()){
			/* Search for a device that matches node n and can be placed on the biochip */
			Device crt_dev = listIt.next();
			//System.out.println(crt_dev.toString()); 

			if (crt_dev.type.compareTo(n.getType().toString())==0){ 
					//&& 	crt_dev.stop_t <= crt_time -crt_dev.time){
				Rectangle crt_rect = new Rectangle(crt_dev); 
				/* reservoirs are outside the biochip, they do not need to be placed */
				if (n.data.super_type.toString().compareTo("IN")==0 && 
						crt_dev.stop_t <= crt_time -crt_dev.time){ 
					n.module = crt_rect; 
					crt_dev.stop_t = crt_time; 
					//System.out.println("Using res: " + crt_dev.toString()); 
					return crt_dev; 
				}
				/* we found a free device, check if it can be placed
				 * it can be placed if is not placed yet (i.e. the coordinated are out of the boundary
				 * or if it has been placed (i.e. it has fixed coordinates) and the required cells are free */
				if (n.data.super_type.toString().compareTo("IN")!=0){
				if (this.hasSpace(crt_rect) || (crt_rect.isNotPlaced(this.width, this.height))){
					n.module = crt_rect; 
					//System.out.println("HERE"); 
					/* schedule the non-reconfigurable device as well :)
					 * I need this to avoid storage for IN operations like for disR in PRT assay (see Elena's thesis, page 48)
					 */
					//crt_dev.stop_t = crt_time + crt_dev.time; 
					return crt_dev; 
				}
				}
			}
		}
		return null;
	}
	
	public Device getFreeDevice(String type, double crt_time){
		ListIterator<Device> listIt = this.devices.listIterator(); 
		while(listIt.hasNext()){
			/* Search for a device that matches node n and can be placed on the biochip */
			Device crt_dev = listIt.next();
			//System.out.println(crt_dev.toString()); 

			if (crt_dev.type.compareTo(type)==0 && 
					crt_dev.stop_t <= crt_time -crt_dev.time){
				Rectangle crt_rect = new Rectangle(crt_dev); 
				/* reservoirs are outside the biochip, they do not need to be placed */
				if (type.compareTo("IN")==0){ 
					return null; 
				}
				/* we found a free device, check if it can be placed
				 * it can be placed if is not placed yet (i.e. the coordinated are out of the boundary
				 * or if it has been placed (i.e. it has fixed coordinates) and the required cells are free */
				if (this.hasSpace(crt_rect) || (crt_rect.isNotPlaced(this.width, this.height))){
					/* schedule the non-reconfigurable device as well :)
					 * I need this to avoid storage for IN operations like for disR in PRT assay (see Elena's thesis, page 48)
					 */
					return crt_dev; 
				}
			}
		}
		return null;
	}

	// returns the device with a specific placement
	public Device getDevice(Rectangle rect){
		ListIterator<Device> listIt = this.devices.listIterator(); 
		while(listIt.hasNext()){
			/* Search for a device that matches node n and can be placed on the biochip */
			Device crt_dev = listIt.next();
			if (crt_dev.width == rect.width-2  && crt_dev.height == rect.height-2 && 
					crt_dev.x_bl == rect.x_bl && crt_dev.y_bl == rect.y_bl){
					return crt_dev; 
			}
		}
		return null;
	}
 
	public void releaseDevice(String name, double crt_time){
		//this is used when the device is aquired for an operation that cannot be placed
		// the dispensed droplet cannot be used at that time
		// so the resource has to be released for another operation that maybe can be placed
		ListIterator<Device> listIt = this.devices.listIterator(); 
		while(listIt.hasNext()){
			/* Search for a device that matches node n and can be placed on the biochip */
			Device crt_dev = listIt.next();
			//System.out.println("Hello " + crt_dev.name + " " + crt_dev.stop_t);
			if(crt_dev.type.compareTo(name)==0 && crt_dev.stop_t == crt_time){
				//System.out.println("GIGI"); 
				crt_dev.stop_t = crt_time - crt_dev.time; 
				return; 
			}
		}
	}
	
	public double getMaxTime(String type){
		double t = 0; 
		for (int i=0; i<this.devices.size(); i++ ){
			Device crt_d = this.devices.get(i); 
			if (crt_d.type.compareTo(type) == 0){
				if (t < crt_d.time) t = crt_d.time; 
			}
		}
		return t;
	}
	
	/** Aproximates the area of a CRM to the circumscribed (bounding) rectangle. */
	public int getArea(ArrayList<Cell> CRM){
		// find the limits on the coordinates
		int min_x = CRM.get(0).x;
		int max_x = CRM.get(0).x; 
		int min_y = CRM.get(0).y; 
		int max_y = CRM.get(0).y; 
		for (int a=1; a<CRM.size(); a++){
			Cell c = CRM.get(a);
			if (c.x < min_x) min_x = c.x; 
			if (c.x > max_x) max_x = c.x; 
			if (c.y < min_y) min_y = c.y; 
			if (c.y > max_y) max_y = c.y; 
		}
		return ((max_x - min_x) * (max_y - min_y));
	}
	
	public int countActiveCells(){
		int count = 0; 
		for (int a=0; a<this.cells.size(); a++)
			 if (this.cells.get(a).isActive) count++; 
		return count; 
	}
	
	public Cell getCell(int i, int j){
		if (i*width + j<cells.size() &&  i*width + j>=0)
			return cells.get(i*width + j); 
		return null; 
	}
	
	public void setCell(int i, int j, Cell c){
		cells.set(i*width+j,c); 
	}
	
	public int findColumn(Cell c){
		int col = 0; 
		int index = cells.indexOf(c); 
		while(index % width != 0){
			col++; 
			index --; 
		}
		return col; 
	}
	
	public int findColumn(int index_cell){
		return this.findColumn(cells.get(index_cell));
	}
	
	public int findRow(Cell c){
		int index = cells.indexOf(c); 
		return (index - findColumn(c))/width; 
	}
	
	public int findRow(int index_cell){
		return this.findRow(cells.get(index_cell));
	}
	public String toString(){
		System.out.println(cells.toString()); 
		StringBuffer s = new StringBuffer(" "); 
		for (int i=0; i<this.height *this.width; i++){
			if (i % (this.width) != 0){
				s.append(new String(" " + cells.get(i).toString()));
			} else {
				s.append("\n" + " " + cells.get(i).toString()); 
			}	
		}
		return s.toString(); 
	}
	
	public void removeColumn (int index_col){
		// copy all elements apart from the ones on index_col in the new array new_cells
		ArrayList<Cell> new_cells = new ArrayList<Cell>();
		for (Cell c:cells){
			int cell_col = findColumn(c); 
			if (cell_col != index_col){
				new_cells.add(c); 
			}
		}
		// update the current cells with the new copied elements
		cells.clear(); 
		for (Cell new_c: new_cells){
			cells.add(new_c); 
		}
		this.width --; 		
	}
	
	
	public StringBuffer printGrid(){
		//System.out.println(grid.cells.toString()); 
		StringBuffer s = new StringBuffer(" "); 
		for (int i=0; i<this.height *this.width; i++){
			if (i % (this.width) != 0){
				if (this.cells.get(i).isActive) s.append(" " + cells.get(i).toString());
					else s.append(new String("  "));
			} else {
				if (this.cells.get(i).isActive) s.append(new String("\n" + " " + cells.get(i).toString()));
				else s.append(new String("\n "));
			}	
		}
		System.out.println(" Grid " + s); 
		return s; 
	}
	
	public StringBuffer printFilledGrid(){
		//System.out.println(grid.cells.toString()); 
		StringBuffer s = new StringBuffer(" "); 
		for (int i=0; i<this.height *this.width; i++){
			if (i % (this.width) != 0){
				if (this.cells.get(i).isActive) s.append(" " + cells.get(i).value);
					else s.append(new String("  "));
			} else {
				if (this.cells.get(i).isActive) s.append(new String("\n" + " " + cells.get(i).value));
				else s.append(new String("\n "));
			}	
		}
		System.out.println(" Grid " + s); 
		return s; 
	}

	public boolean containsCell(ArrayList<Cell> CRM, Cell c){
		for (int a=0; a<=CRM.size()-1; a++){
			if (CRM.get(a).x == c.x && CRM.get(a).y == c.y)
				return true; 
		}
		return false; 
	} 
}
