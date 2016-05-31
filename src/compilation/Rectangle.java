/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Rectangle{
	String name; 
	int width, height;
	int x_bl, y_bl; // the coordinates of the bottom left corner
	
	public Rectangle(){
		width = height = 0; 
		x_bl = y_bl=  -1; 
		this.name = "";
	}
	
	public Rectangle(int w, int h, int x, int y){
		width = w; height = h; 
		x_bl = x; y_bl = y; 
		this.name = new String(this.toString());
	}
	
	public Rectangle(Rectangle r){
		if (r!= null){
			width = r.width; height = r.height; 
			this.x_bl = r.x_bl; this.y_bl = r.y_bl;
			//System.out.println("ggu" + this.x_bl); 
		} else {width = height = x_bl= y_bl = -1;}
		this.name = new String(r.name); 
	}
	
	public Rectangle(Module m){
		//this.width = m.width + 2; 
		//this.height = m.height + 2; 
		this.width = m.width; 
		this.height = m.height; 
		this.x_bl = this.y_bl= -1; 
		this.name = new String(m.toString());
	}
	
	public Rectangle(Device d){
		this.width = d.width + 2; 
		this.height = d.height + 2; 
		this.x_bl = d.x_bl;
		this.y_bl= d.y_bl; 
		this.name = new String(d.name);
	}
	
	public int getArea(){
		return this.width * this.height;
	}
	
	public Rectangle intersect(Rectangle rect){
		ArrayList<Integer> i_list = new ArrayList<Integer>(); 
		ArrayList<Integer> j_list = new ArrayList<Integer>(); 

		for (int i=this.y_bl; i<this.y_bl + this.height; i++){
			for (int j=this.x_bl; j<this.x_bl+this.width; j++){
				if (rect.containsCell(j,i) == true) {
					//System.out.println("Common cell " + j + ","+ i);
					i_list.add(i);
					j_list.add(j); 
				}

			}
		}
		
		if (i_list.size()>0 && i_list.size()>0){
			// sort the lists 
			 Collections.sort(i_list, new Comparator<Integer>(){
	        	 public int compare(Integer o1, Integer o2) {
	            	if (o1 > o2)  return 1; 
	            	else if (o1 < o2) return -1; else return 0; }});
			 
			 Collections.sort(j_list, new Comparator<Integer>(){
	        	 public int compare(Integer o1, Integer o2) {
	            	if (o1 > o2)  return 1; 
	            	else if (o1 < o2) return -1; else return 0; }});
			 
			 // get the bl coordinates and the w and h
			 int h = i_list.get(i_list.size()-1) - i_list.get(0)+1;
			 int w = j_list.get(j_list.size()-1) - j_list.get(0)+1; 
			 int y_bl = i_list.get(0); 
			 int x_bl = j_list.get(0); 
	
			 Rectangle in_rect = new Rectangle(w,h,x_bl,y_bl);
			 return in_rect; 
		}
		return null; 


	}
	/** Return the rectangle situated at the intersection between this and rect
	 * or null if none found. I traverse the this.rectangle from left to right, bottom to up.
	 * Until I find a common cell, that would give me the coordinates for the bottom left corner.
	 * I continue traversing on the x-axis to find out the intersection width (temp_width++).
	 * I go up until I find a completely not-intersecting row (freeRow) or until the this.top_edge is reached.
	 * I can now compute the height of the intersection rectangle.*/
	public Rectangle intersect1(Rectangle rect){
		//System.out.println("intersection between " + this.toString() + " and " + rect.toString()); 
		Rectangle intersect_r = new Rectangle(); 
		boolean bl_corner = false; // bottom left corner of intersection rectangle
		int temp_width = 0; 
		boolean freeRow = false;
		boolean hasWidth = false; 
		int w =0 ;
		for (int i=this.y_bl; i<this.y_bl + this.height; i++){
			//System.out.println(bl_corner); 
			if (bl_corner == true){
				//intersect_r.width = temp_width;
				intersect_r.width = temp_width;
				//System.out.println("Here w " + w); 
				hasWidth = true; 
				// if I find a freeRow after I find a bl_corner it means that the rect.top_edge 
				// is inside this.top_edge, so I can stop, as I know the height of intersection rect
				if (freeRow == true){
					intersect_r.height = i - 2 - intersect_r.y_bl;
					return intersect_r; 
				}
			}
			else temp_width = 0; 
			freeRow = true; 
			w = 0; 
			for (int j=this.x_bl; j<this.x_bl+this.width; j++){
				//System.out.println("Look cell " + j + " , "+ i ); 
				
				if (rect.containsCell(j,i) == true) {
					System.out.println("Common cell " + j + ","+ i + " w " + w ); 
					w++; 
					// first intersection cell = bottom left corner cell
					freeRow = false; 
					if (bl_corner == false){
						//System.out.println("Gigi"); 
						intersect_r.x_bl = j; 
						intersect_r.y_bl = i;
						bl_corner = true; 
					}
					if (hasWidth == false){
						// the this.rect is traversed from left to rigth, from bottom->up, so when I found the 
						// bl_corner, I can increment the width, until I finish traversing the x-axis or I find a non-intersecting cell
						temp_width ++; 
					}
				} else {
					//System.out.println("FREE cell " + j + " , "+ i  + freeRow); 
				}
			}
			//System.out.println("Here2 w " + w); 
			intersect_r.width = w;

		}
		// case when a bl_corner is found, but the top edge of rect is outside this.top_edge
		if (bl_corner == true){
			//System.out.println("Hei " + intersect_r.width); 
			intersect_r.height = this.height + this.y_bl - intersect_r.y_bl; 
			return intersect_r; 
		}
		return null; 
	}
	
	/**Merges adjacent rectangles. Returns the merged rectangle if possible. 
	 * So, if the rectangle given as parameter is adjacent with this one, they are merged into a big rect.
	 * Otherwise a null rect is returned. */
	public Rectangle mergeAdjacent(Rectangle rect){
		if ((this.y_bl == rect.y_bl)
				&& (this.height == rect.height)){
			if((rect.x_bl <= this.x_bl && this.x_bl <= rect.x_bl+rect.width)
					|| 
				(this.x_bl<=rect.x_bl && rect.x_bl<= this.x_bl+this.width)){
				int mg_x = Math.min(this.x_bl, rect.x_bl); 
				int mg_y = this.y_bl; 
				int mg_h = this.height;
				int mg_w;
				if (this.x_bl>= rect.x_bl){
					mg_w = this.width + this.x_bl - rect.x_bl; 
				} else mg_w = rect.width + rect.x_bl - this.x_bl; 
				return new Rectangle(mg_w, mg_h, mg_x, mg_y); 
			}
		}
		
		if ((this.x_bl == rect.x_bl)
				&& (this.width == rect.width)){
			if((rect.y_bl <= this.y_bl && this.y_bl <= rect.y_bl+rect.height)
					|| 
				(this.y_bl<=rect.y_bl && rect.y_bl<= this.y_bl+this.height)){
				int mg_y = Math.min(this.y_bl, rect.y_bl); 
				int mg_x = this.x_bl; 
				int mg_w = this.width;
				int mg_h;
				if (this.y_bl>= rect.y_bl){
					mg_h = this.height + this.y_bl - rect.y_bl; 
				} else mg_h = rect.height + rect.y_bl - this.y_bl; 
				return new Rectangle(mg_w, mg_h, mg_x, mg_y); 
			}
		}

		
		return null; 
	}
	 
	public boolean hasCoordinatesInBoundaries(int width, int height){
		if (((0 <= this.x_bl) && (this.x_bl < width)) 
		&& 
		((0 <= this.y_bl) && (this.y_bl < height))){
			return true; 
		}
		return false; 
	}
	
	public boolean isNotPlaced(int biochip_w, int biochip_h){
		return (!this.hasCoordinatesInBoundaries(biochip_w, biochip_h));
	}
	
	/** Returns true if the current rectangle contains the rectangle rect given as parameter.
	 * Rect is contained by this if rect is placed inside this rectangle.*/
	public boolean contains(Rectangle rect){
		/*if ((rect.x_bl >= this.x_bl) && (rect.y_bl >= this.y_bl) && 
			(rect.x_bl <= this.x_bl + this.width) && (rect.y_bl <= this.y_bl + this.height) &&
			(rect.width <= this.width) && (rect.height <= this.height))*/
		/*if ((rect.x_bl - this.x_bl) < ((rect.width + this.width) * 0.5) && (rect.y_bl - this.y_bl) < ((rect.height + this.height) * 0.5) ||
				(this.x_bl - rect.x_bl) < ((rect.width + this.width) * 0.5) && (this.y_bl - rect.y_bl) < ((rect.height + this.height) * 0.5))*/
		if ((this.x_bl <= rect.x_bl) && (this.x_bl + this.width >= rect.x_bl + rect.width) &&
				(this.y_bl + this.height >= rect.y_bl + rect.height) && (this.y_bl <= rect.y_bl))
			return true; 	
		return false; 
	}
	
	/** Returns true if the cell with coordinates x=i, y=j is contained by this rect.
	 * Ox is the horizontal axis and Oy is the vertical axis.*/
	public boolean containsCell(int i, int j){
		if (i>=this.x_bl && i<this.x_bl+this.width && j>=this.y_bl && j<this.y_bl+this.height)
			return true; 
		return false; 
	}
	
	public void set(int w, int h, int x, int y){
		this.height = h; 
		this.width = w; 
		this.x_bl = x; 
		this.y_bl = y; 
	}
	
	public void set(Rectangle rect){
		this.height = rect.height; 
		this.width = rect.width; 
		this.x_bl = rect.x_bl; 
		this.y_bl = rect.y_bl; 
	}
	
	public boolean equals(Rectangle r){
		if (this.width == r.width && this.height == r.height && this.x_bl == r.x_bl && this.y_bl == r.y_bl)
			return true; 
		return false; 
	}
	
	public String toSVG(String fillColor){
		String svg_text = "Hello"; 
		// the coordinates are not correct
		//String svg_text =" \n <rect x=\""+x_bl+"\" y=\""+(y_bl-height)+" width=\""+width+"\" height=\""+height+"\" style=\"fill:"+fillColor+";stroke-width:1;stroke:rgb(0,0,0)\" />";       
		return svg_text; 
	}
	
	public String toString(){
		return this.width + "x"  + this.height + " (" + this.x_bl + "," + this.y_bl + ")";
	}
	
}

