// @@ JETC journal - Architecture Synthesis using placement of Circular-route modules 
package compilation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


//TODO fix this code to work for any k, not only for a hard-coded k
// look for hard-coded elements and remove them, fix them

public class ComputeWCET {
	//**Generate some mixing patterns. They are given by input by the designer.
	 /* The returned value is a vector that contains the order of the traversed cells while mixing (the routing)
	 /* We need the start_cell since the droplet can enter the module from any position. We can also simplify and say
	 /* that we simply move the module a few cells until it reaches a fixed start location (bottom left corner for instance)
	 * mix_percentage is the starting mixing percentage. */
	public ArrayList<ModuleElement> generateMixPattern(Module m, ModuleElement start_cell, String patternType, double mix_percentage){
		ArrayList<ModuleElement> pattern = new ArrayList<ModuleElement>(); 
		int i = start_cell.x;
		int j = start_cell.y; 
		boolean mixed = false; 
		pattern.add(start_cell); 
		// ROUNDTHECLOCK pattern (going round on the borders)
		if (patternType.compareTo("ROUNDTHECLOCK")== 0){
			if(mix_percentage>=100) mixed = true; 
			//System.out.println("mix = " + mix_percentage); 
			while(!mixed){
			//for (int kk=0; kk<1;kk++){
				// go to the right
				for (int k=0; k<m.width-1 && !mixed; k++){
					j++; 
					//System.out.println("("+i+","+j+")"); 
					pattern.add(new ModuleElement(i,j)); 
					mix_percentage = computeMix(pattern,mix_percentage); 
					//System.out.println("mix = " + mix_percentage); 
					if(mix_percentage>=100) mixed = true; 
				}
				// go down
				if (!mixed) for (int k=0; k<m.height-1 && !mixed; k++){
					i++; 
					//System.out.println("("+i+","+j+")"); 
					pattern.add(new ModuleElement(i,j)); 
					mix_percentage = computeMix(pattern,mix_percentage); 
					if(mix_percentage>=100) mixed = true; 
				}
				// go left 
				if (!mixed) for (int k=0; k<m.width-1 && !mixed; k++){
					j--; 
					//System.out.println("("+i+","+j+")"); 
					pattern.add(new ModuleElement(i,j)); 
					mix_percentage = computeMix(pattern,mix_percentage); 
					if(mix_percentage>=100) mixed = true; 
				}
				// go up
				if (!mixed) for (int k=0; k<m.height-1 && !mixed; k++){
					i--; 
					//System.out.println("("+i+","+j+")"); 
					pattern.add(new ModuleElement(i,j)); 
					mix_percentage = computeMix(pattern,mix_percentage); 
					if(mix_percentage>=100) mixed = true; 
				}
			}
			//System.out.println("Mix percentage: " + mix_percentage); 
		}
		// ZIGZAG pattern (back and forth on every row)
		// this one starts from one corner
		if (patternType.compareTo("ZIGZAG")== 0){
			while(!mixed){
				for (int k=0; k<m.height && !mixed; k++)
				{
					 if (k % 2 == 0)
						 for (int q = 0; q<m.width && !mixed; q++){
							 pattern.add(new ModuleElement(k,q)); 
							if(this.computeMix(pattern,mix_percentage)>=100) mixed = true; 
						 }
					  if (k % 2 != 0)
						 for (int q = m.width-1; q>=0 && !mixed; q--){
							 pattern.add(new ModuleElement(k,q));
							if(this.computeMix(pattern,mix_percentage)>=100) mixed = true; 
						 }
				}	
			}
		}
		// RANDOM pattern
		if (patternType.compareTo("RANDOM")== 0){
			while (!mixed){
				Random generator = new Random(); 
				int r = generator.nextInt(4); 
				switch(r){
					case 1: i++; 
					case 2: j++; 
					case 3: i--; 
					case 4: i++; 
					default:break; 
				}
				// test for out-of-boundaries results
				if (j>=m.width) j--; 
				if (j< 0) j++; 
				if (i>= m.height) i--; 
				if (i< 0) i++; 
				pattern.add(new ModuleElement(i,j)); 
				if(this.computeMix(pattern,mix_percentage)>=100) mixed = true; 
			}
		}
		
		return pattern; 
	}
	
	public double computeMix(ArrayList<ModuleElement> pattern, double mix_percentage){

		int i_k = -1; 
		int j_k = -1;
		int i_k_2 = -1; 
		int j_k_2 = -1;
		int i_k_3 = -1; 
		int j_k_3 = -1;
		int i_k_4 = -1; 
		int j_k_4 = -1;
		int i_k_5 = -1; 
		int j_k_5 = -1;

		if (pattern.size()>0){
			i_k = pattern.get(pattern.size()-1).x; 
			j_k = pattern.get(pattern.size()-1).y;
		}
		if (pattern.size()>1){
			i_k_2 = pattern.get(pattern.size()-2).x; 
			j_k_2 = pattern.get(pattern.size()-2).y;
		}
		if (pattern.size()>2){
			i_k_3 = pattern.get(pattern.size()-3).x; 
			j_k_3 = pattern.get(pattern.size()-3).y;
		}
		
		if (pattern.size()>3){
			i_k_4 = pattern.get(pattern.size()-4).x; 
			j_k_4 = pattern.get(pattern.size()-4).y;
		}
		if (pattern.size()>4){
			i_k_5 = pattern.get(pattern.size()-5).x; 
			j_k_5 = pattern.get(pattern.size()-5).y;
		}


		//System.out.println("initial " + mix_percentage + "%"); 

		// calculate degrees degrees 
		if ((i_k_2 == i_k) && (j_k_2 == j_k)) mix_percentage -= 0.5; 
		else if ((i_k_3 != i_k) && (j_k_3 != j_k)){
			mix_percentage += 0.1; //System.out.println("90");
			//mix_percentage += 0.08; //// For DILUTION

			//System.out.println("("+i_k_3 + ","+ j_k_3+ ")" + " (" + i_k_5 + ","+j_k_5+")"); 
			if(((i_k_5 != i_k_3) && (j_k_5 != j_k_3))
					&& 
					(((i_k_4 == i_k_3) && (i_k_3 == i_k_2))
							|| 
							((j_k_4 == j_k_3) && (j_k_3 == j_k_2))
					)
			){
				mix_percentage -= 0.29; //System.out.println("1 cell 0");
				//mix_percentage -= 0.11; //// For DILUTION

				// here I have to check if the previous was a one-cell 0 degree
			}
		}
		else mix_percentage += 0.58;
		//else mix_percentage += 0.31; // For DILUTION

		///else if ((i_k_4 == i_k_3)&&(i_k_3 == i_k_2)&&(i_k_2==i_k)) mix_percentage += 0.58;
		//else if ((i_k_2==i_k)) mix_percentage += 0.58;
		//else if ((j_k_4 == j_k_3)&&(j_k_3 == j_k_2)&&(j_k_2==j_k)) mix_percentage += 0.58;
		//else if ((j_k_2==j_k)) mix_percentage += 0.29;
		// else if ((j_k_2==j_k)) mix_percentage += 0.58;
		
		// !!! note: if only one 0 degree continous move, the the mix_p+= 0.29
		// but we hardcoded as we assume we will NOT use modules with only one 0 degrees move
		//TODO fix this so that 0.29 can be taken into account 

		//System.out.println("computed " + mix_percentage + "%"); 
		return mix_percentage; 
	}
	
	public double computeUnMixStep(ArrayList<ModuleElement> pattern, double mix_percentage){

		int i_k = -1; 
		int j_k = -1;
		int i_k_2 = -1; 
		int j_k_2 = -1;
		int i_k_3 = -1; 
		int j_k_3 = -1;
		int i_k_4 = -1; 
		int j_k_4 = -1;
		if (pattern.size()>0){
			i_k = pattern.get(pattern.size()-1).x; 
			j_k = pattern.get(pattern.size()-1).y;
		}
		if (pattern.size()>1){
			i_k_2 = pattern.get(pattern.size()-2).x; 
			j_k_2 = pattern.get(pattern.size()-2).y;
		}
		if (pattern.size()>2){
			i_k_3 = pattern.get(pattern.size()-3).x; 
			j_k_3 = pattern.get(pattern.size()-3).y;
		}
		
		if (pattern.size()>3){
			i_k_4 = pattern.get(pattern.size()-4).x; 
			j_k_4 = pattern.get(pattern.size()-4).y;
		}

		//System.out.println("initial " + mix_percentage + "%"); 

		// calculate degrees degrees 
		if ((i_k_2 == i_k) && (j_k_2 == j_k)) mix_percentage += 0.5; 
		else if ((i_k_3 != i_k) && (j_k_3 != j_k)){
			mix_percentage += 0.1; //System.out.println("90");
		}
		else if ((i_k_4 == i_k_3)&&(i_k_3 == i_k_2)&&(i_k_2==i_k)) mix_percentage -= 0.58;
		else if ((i_k_2==i_k)) mix_percentage -= 0.58;
		else if ((j_k_4 == j_k_3)&&(j_k_3 == j_k_2)&&(j_k_2==j_k)) mix_percentage -= 0.58;
		else if ((j_k_2==j_k)) mix_percentage -= 0.58;
		
		// TODO implement for 0.29 as well - only one continuos 0 degrees move
		//System.out.println("computed " + mix_percentage + "%"); 
		return mix_percentage; 
	}

	
	public double computeWCET(Module m,ArrayList<ModuleElement> pattern, String patternType){
		// hardcoded for k=3 maximum permanent faults 
		// get the all possibile scenarios (combination of up to 3 permanent faults)
		// the pattern is repetitive for ROUNDTHECLOCK and ZIG-ZAG so the number of possible combinations can be reduced
		double wcet = 0; 

		if (patternType.compareTo("ROUNDTHECLOCK")==0){
			// the fault cannot be on the start electroded of the pattern
			// this translates into the following: if the fault is on the gate (entrence) of the module, then 
			// the droplet is re-routed to another gate ... so the fault cannot be there
		
			double crt_wcet = 0;
			int patternLength = (m.height + m.width -1)*2; 
			ArrayList<ModuleElement> fault_scen; 
			//for (int x=8;x<patternLength;x++){

			for (int x=2;x<patternLength;x++){
				fault_scen = new ArrayList<ModuleElement>(); 
				fault_scen.add(pattern.get(x));
				
				//System.out.println("F_scen " + fault_scen.toString() ); 

				crt_wcet = this.computeEps(m, fault_scen, pattern); 
				//System.out.println("F_scen " + fault_scen.toString() + " wcet = " + crt_wcet); 

				if (wcet<crt_wcet) wcet = crt_wcet;
				if (crt_wcet>1) {
					//System.out.println("here"); 
					//System.exit(-1); 
				}

				//for (int y=x+1; y<x+3; y++){
				for (int y=x+1; y<patternLength-1; y++){
					fault_scen = new ArrayList<ModuleElement>(); 
					fault_scen.add(pattern.get(x));
					fault_scen.add(pattern.get(y));
					//System.out.print("F_scen " + fault_scen.toString() ); 
					crt_wcet = this.computeEps(m, fault_scen, pattern); 
					//System.out.println("F_scen " + fault_scen.toString() + " wcet = " + crt_wcet+" \n"); 
					if (wcet<crt_wcet) wcet = crt_wcet;
					if (crt_wcet>1) {
						//System.out.println("here"); 
						//System.exit(-1); 
					}

					//for (int z=y+1; z<y+4; z++){
					for (int z=y+1; z<patternLength-2; z++){
						fault_scen = new ArrayList<ModuleElement>(); 
						fault_scen.add(pattern.get(x));
						fault_scen.add(pattern.get(y));
						fault_scen.add(pattern.get(z));
						crt_wcet = this.computeEps(m, fault_scen, pattern); 
						if (wcet<crt_wcet) wcet = crt_wcet; 
						//System.out.println("F_scen " + fault_scen.toString() + " wcet = " + crt_wcet+" \n"); 
						if (crt_wcet>1) {
							//System.out.println("here"); 
							//System.exit(-1); 
						}

					}
				}
			}
		}
		
		return wcet; 
	}

	public double computeWCETSmallerModules(Module m,ArrayList<ModuleElement> pattern, String patternType){
		// hardcoded for k=3 maximum permanent faults 
		// get the all possibile scenarios (combination of up to 3 permanent faults)
		// the pattern is repetitive for ROUNDTHECLOCK and ZIG-ZAG so the number of possible combinations can be reduced
		double wcet = 0; 
		if (patternType.compareTo("ROUNDTHECLOCK")==0){
			// the fault cannot be on the start electroded of the pattern
			// this translates into the following: if the fault is on the gate (entrence) of the module, then 
			// the droplet is re-routed to another gate ... so the fault cannot be there
		
			double crt_wcet = 0;
			int patternLength = (m.height + m.width -1)*2; // the lenght of a cycle
			ArrayList<ModuleElement> fault_scen; 
			// take the k=1 combinations first 
			double max_1F_wcet = 0; 
			//for (int x=8;x<patternLength;x++){
			for (int x=2;x<patternLength;x++){
				fault_scen = new ArrayList<ModuleElement>(); 
				fault_scen.add(pattern.get(x));
				//System.out.println("F_scen " + fault_scen.toString() ); 
				crt_wcet = this.computeEps(m, fault_scen, pattern); 
				//System.out.println("F_scen " + fault_scen.toString() + " wcet = " + crt_wcet); 
				if (max_1F_wcet<crt_wcet) max_1F_wcet = crt_wcet;
			}
			// take the k=2 combinations  
			double max_2F_wcet =0; 
			for (int x=2;x<patternLength;x++){
				//for (int y=x+1; y<x+3; y++){
				for (int y=x+1; y<patternLength-1; y++){
					fault_scen = new ArrayList<ModuleElement>(); 
					fault_scen.add(pattern.get(x));
					fault_scen.add(pattern.get(y));
					//System.out.print("F_scen " + fault_scen.toString() ); 
					crt_wcet = this.computeEps(m, fault_scen, pattern); 
					//System.out.println("F_scen " + fault_scen.toString() + " wcet = " + crt_wcet+" \n"); 
					if (max_2F_wcet<crt_wcet) max_2F_wcet = crt_wcet;
				}
			}
			// take the k=3 combinations  
			double max_3F_wcet =0; 
			for (int x=2;x<patternLength;x++){
				for (int y=x+1; y<patternLength-1; y++){
					//for (int z=y+1; z<y+4; z++){
					for (int z=y+1; z<patternLength-2; z++){
						fault_scen = new ArrayList<ModuleElement>(); 
						fault_scen.add(pattern.get(x));
						fault_scen.add(pattern.get(y));
						fault_scen.add(pattern.get(z));
						crt_wcet = this.computeEps(m, fault_scen, pattern); 
						if (max_3F_wcet<crt_wcet) max_3F_wcet = crt_wcet; 
						//System.out.println("F_scen " + fault_scen.toString() + " wcet = " + crt_wcet+" \n"); 
					}
				}
			}
			// case k=1
			max_1F_wcet += pattern.size()*0.01; 
			max_2F_wcet += pattern.size()*0.01; 
			max_3F_wcet += pattern.size()*0.01; 
			double smaller_m_wcet = 0; 

			wcet = Math.max(this.nonFWCET(m.width-1, m.height),this.nonFWCET(m.width, m.height-1));
			wcet = Math.min(max_1F_wcet,wcet); 
			System.out.println("max_1F_wcet=" + max_1F_wcet + " wcet = " + wcet); 
		
			// case k=2 
			System.out.println(" k = 2 "); 
			if(m.width==4 || m.height ==4){
				if (max_2F_wcet > max_1F_wcet)
					smaller_m_wcet = max_1F_wcet; 
				else smaller_m_wcet = max_2F_wcet; 
			}
			else{
				for(int i=2; i<(int)Math.floor(m.height/2)+1;i++){
					if (smaller_m_wcet < this.nonFWCET(m.width, m.height-i))
						smaller_m_wcet = this.nonFWCET(m.width, m.height-i); 
				}
				for(int i=2; i<(int)Math.floor(m.width/2)+1;i++){
					if (smaller_m_wcet < this.nonFWCET(m.width-i, m.height))
						smaller_m_wcet = this.nonFWCET(m.width-i, m.height); 
				}
				if (smaller_m_wcet < this.nonFWCET(m.width-1, m.height-1))
					smaller_m_wcet = this.nonFWCET(m.width-1, m.height-1); 			
			}
			wcet = Math.max(wcet, Math.min(max_2F_wcet, smaller_m_wcet)); 
			System.out.println("max_2F_wcet=" + max_2F_wcet + " wcet = " + wcet + " smaller = " + smaller_m_wcet); 

		   // case k=3
			// I have to use placement here - generate all possible combinations even inside the module 
			smaller_m_wcet = this.minPlacedRectWCET(m, max_2F_wcet); 
			System.out.println("max_2F_wcet=" + max_2F_wcet +" max_3F_wcet=" + max_3F_wcet + " wcet = " + wcet + " smaller = " + smaller_m_wcet); 

			//wcet = Math.max(wcet, Math.min(max_3F_wcet, smaller_m_wcet)); 


			
		}
		
		return wcet; 
	}
	
	public double nonFWCET(int width, int height){
		Module crt_mod = new Module(width, height, 0); 
		ModuleElement cell = new ModuleElement(0,0); 
		ArrayList<ModuleElement> pattern = this.generateMixPattern(crt_mod, cell, "ROUNDTHECLOCK", 0); 
		
		System.out.println("Module "+crt_mod.toString() + " wcet = " + (pattern.size()*0.01)); 
		
		return pattern.size()*0.01; 
	}
	
	 //Returns the minimum wcet of the rectangle that can be placed on the module with faults 
	public double minPlacedRectWCET(Module m, double max_2F_wcet){
		// build the grid 
		Biochip grid = new Biochip(m.width,m.height);

		double max_wcet = 0; 
		for (int x=0;x<grid.cells.size();x++){
		//for (int x=0;x<grid.cells.size();x++){
			//for (int y=x+1; y<4; y++){
			for (int y=x+1; y<grid.cells.size()-1; y++){
			//	for (int z=y+1; z<5; z++){
					
				for (int z=y+1; z<grid.cells.size()-2; z++){
					double min_wcet = 1000; 
					ArrayList<ModuleElement> fault_scen = new ArrayList<ModuleElement>(); 
					fault_scen.add(new ModuleElement(grid.findRow(grid.cells.get(x)),grid.findColumn(grid.cells.get(x))));
					fault_scen.add(new ModuleElement(grid.findRow(grid.cells.get(y)),grid.findColumn(grid.cells.get(y))));
					fault_scen.add(new ModuleElement(grid.findRow(grid.cells.get(z)),grid.findColumn(grid.cells.get(z))));
					System.out.println("\nFault scenario: " + fault_scen.toString()); 

					// mark the faulty cells as inactive
					for (int k=0; k<fault_scen.size();k++){
						ModuleElement f_cell = fault_scen.get(k);
						grid.getCell(f_cell.x, f_cell.y).isActive = false; 
					}
					
					System.out.println(grid.toString()); 
	// calculate the min wcet 
					for (int k=0; k<fault_scen.size();k++){
						ModuleElement f_cell = fault_scen.get(k);
						System.out.println("crt cell = (" + f_cell.x + " , " + f_cell.y + ")" ); 
						// go around the fault - RIGHT 
						if (f_cell.y+1<grid.width){
							ModuleElement right_n = new ModuleElement(f_cell.x, f_cell.y+1); 
							if(!this.findFault(right_n, fault_scen)){
								Rectangle right_r = new Rectangle(1,1,right_n.y, grid.height - 1 - right_n.x);
								//System.out.println("Start rect = " + right_r.toString()); 

								// get the scaled ER
								this.shiftRight(right_r,grid); 
								//System.out.println(" After right right rect = " + right_r.toString()); 

								this.shiftBottom(right_r,grid);
								//System.out.println(" After BOTTOM right rect = " + right_r.toString()); 

								this.shiftTop(right_r,grid); 
								//System.out.println(" After up right rect = " + right_r.toString()); 

								if (right_r.height > 1 && right_r.width >1){

								if(min_wcet>this.nonFWCET(right_r.width, right_r.height))
									min_wcet = this.nonFWCET(right_r.width, right_r.height); 
								//System.out.println("Right rect = " + right_r.toString()); 
								}
							}
						}
						// LEFT
						if (f_cell.y-1 >= 0){
							ModuleElement left_n = new ModuleElement(f_cell.x, f_cell.y-1); 
							if(!this.findFault(left_n, fault_scen)){
								Rectangle left_r = new Rectangle(1,1,left_n.y, grid.height - 1 - left_n.x);
								// get the scaled ER
								this.shiftLeft(left_r,grid); 

								this.shiftBottom(left_r,grid); 

								this.shiftTop(left_r,grid); 
								if (left_r.height > 1 && left_r.width >1){

								if(min_wcet>this.nonFWCET(left_r.width, left_r.height))
									min_wcet = this.nonFWCET(left_r.width, left_r.height); 
								//System.out.println("Left rect = " + left_r.toString()); 
								}

							}
						}

						// UP
						if (f_cell.x-1>=0){
							ModuleElement up_n = new ModuleElement(f_cell.x-1, f_cell.y); 
							if(!this.findFault(up_n, fault_scen)){
								Rectangle up_r = new Rectangle(1,1,up_n.y, grid.height - 1 - up_n.x);
								// get the scaled ER
								this.shiftLeft(up_r, grid); 
								this.shiftRight(up_r, grid); 
								this.shiftTop(up_r, grid); 
								if (up_r.height > 1 && up_r.width >1){
									if(min_wcet>this.nonFWCET(up_r.width, up_r.height))
										min_wcet = this.nonFWCET(up_r.width, up_r.height); 
								}
								//System.out.println("UP rect = " + up_r.toString()); 


							}
						}
						// DOWN
						if (f_cell.x+1<grid.height){

							ModuleElement down_n = new ModuleElement(f_cell.x+1, f_cell.y);
							//System.out.println("Down cell = " + down_n.toString()); 
							if(!this.findFault(down_n, fault_scen)){
								Rectangle down_r = new Rectangle(1,1,down_n.y, grid.height - 1 - down_n.x);
								//System.out.println("Start rect = " + down_r.toString()); 
								// get the scaled ER
								this.shiftLeft(down_r,grid);
								//System.out.println(" After Left Down rect = " + down_r.toString()); 
								this.shiftRight(down_r,grid);
								//System.out.println(" After Right Down rect = " + down_r.toString()); 
								this.shiftBottom(down_r,grid); 
								//System.out.println(" After Down Down rect = " + down_r.toString()); 
								if (down_r.height > 1 && down_r.width >1){

								if(min_wcet>this.nonFWCET(down_r.width, down_r.height))
									min_wcet = this.nonFWCET(down_r.width, down_r.height); 
								//System.out.println("Down rect = " + down_r.toString()); 
								}

							}
						}
					}
					System.out.println("local minimum = " + min_wcet + " max_wcet = " + max_wcet);
					if (min_wcet > max_2F_wcet) min_wcet = max_2F_wcet; 
					//if(max_wcet > 4) System.exit(1); 
					if (max_wcet < min_wcet) max_wcet = min_wcet; 
					// clean the marked cells 
					for (int k=0; k<fault_scen.size();k++){
						ModuleElement f_cell = fault_scen.get(k);
						grid.getCell(f_cell.x, f_cell.y).isActive = true; 
					}

				}
			}
		}
	
		
		return max_wcet; 
	}
 
	public void shiftBottom(Rectangle crt_r, Biochip biochip){
		for (int y = crt_r.y_bl; y>0; y--){
			int x = crt_r.x_bl; 
			int column = x ; 
			int row = biochip.height - y-1;
			//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column); 
			if (!biochip.getCell(row,column).isUsable()){
				crt_r.height += crt_r.y_bl - y -1; 
				crt_r.y_bl = y +1;
				System.out.println("crt_r = " + crt_r.toString()); 
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
	
	public void shiftLeft(Rectangle crt_r, Biochip biochip){
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
					if (!biochip.getCell(row+k,column).isUsable()){
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
	
	public void shiftTop(Rectangle crt_r, Biochip biochip){
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
	
	public void shiftRight(Rectangle crt_r, Biochip biochip){
		for (int x = crt_r.x_bl + crt_r.width; x<biochip.width; x++){
			int y = crt_r.y_bl; 
			int column = x; 
			int row = biochip.height - y -1; 
			//System.out.println(" x, y = "+ x+ "," + y + " row, column = " + row + "," + column); 
			if (!biochip.getCell(row,column).isUsable()){
				crt_r.width = x - crt_r.x_bl; 
				return; 
			} else for (int k=0; k< crt_r.height; k++){
				if (!biochip.getCell(row+k,column).isUsable()){
					crt_r.width = x - crt_r.x_bl; 
					return; 
			}
				}
			
		}
		// I got to the right edge of the biochip
		crt_r.width = biochip.width - crt_r.x_bl; 
	}
	

	public boolean findFault(ModuleElement cell, ArrayList<ModuleElement> fault_scen){
		for (int k=0; k<fault_scen.size();k++){
			ModuleElement f_cell = fault_scen.get(k);
			if(f_cell.x==cell.x && f_cell.y == cell.y)
				return true; 
		}

		return false; 
	}
	public double computeEps(Module m, ArrayList<ModuleElement> fault_scen, ArrayList<ModuleElement> pattern){

		double mix_p = 100; 
		double no_cycles = pattern.size()/(2*(m.height + m.width -2)); 
		int epsilon_cells = 0; 
		double unmix_p =0; // this is a percentage that is obtained by avoiding to go back

		for (int q=0; q<fault_scen.size();q++){
					
			// for each fault determine the source and the destination (the cell before and after the faulty one)
			int source_i = pattern.indexOf(fault_scen.get(q)); //source index in the pattern 
			int dest_i = source_i; 
			//System.out.println("source_i = " + source_i); 

			boolean source_f = false; // source found 
			boolean dest_f = false; // dest found
			boolean skip = false; 
			// special case if the previous cell in the pattern is a corner (if the fault comes after a corner
			// we try to avoid negative mixing, that it why we treat it as a special case 

			if (this.isCorner(m, pattern.get(source_i-1))){
				//System.out.println(" Corner on  "+ pattern.get(source_i-1).x + " " + pattern.get(source_i-1).y); 
				ArrayList<ModuleElement> unmix_pattern = new ArrayList<ModuleElement>(); 
				if (source_i>1) unmix_pattern.add(pattern.get(source_i-1)); 
				if (source_i>2) unmix_pattern.add(pattern.get(source_i-2)); 
				if (source_i>3) unmix_pattern.add(pattern.get(source_i-3)); 
				unmix_p = this.computeUnMixStep(unmix_pattern, unmix_p); 
				epsilon_cells = epsilon_cells -1 * (int)no_cycles ; 
				source_i--; 
			}
			while(!source_f || !dest_f){
				if (!source_f){
					// too many things here ... the fault_scen is ordered, only the dest can be faulty
					source_i --; 
					// test if this one is not faulty as well
					ModuleElement s = pattern.get(source_i); 
					if (fault_scen.indexOf(s)==-1)
						source_f = true; 
				}
				if (!dest_f){
					//ModuleElement source_a = pattern.get(source_i);
					//ModuleElement dest_a = pattern.get(dest_i);
					//System.out.println("\tsource=" + source_a.toString() + " dest=" + dest_a.toString()); 

					dest_i ++; 
					// test if this one is not faulty as well
					//if (dest_i<pattern.size()) 
						ModuleElement d = pattern.get(dest_i); 
					if (fault_scen.indexOf(d)==-1){
						if (dest_i+1<pattern.size() && fault_scen.indexOf(pattern.get(dest_i+1))==-1){
							if (!this.isCorner(m,pattern.get(dest_i))){
								// test for corners
								dest_f = true; // at least 2 spaces between faults like xoox
							}
						}
						else {
							dest_i++; 
							q++; // disregard the next fault - xox pattern
						}
					}else{
						// mark the faulty neighbor as visited - the fault_scen is ordered
						// only the dest can be faulty
						skip = true;  // skip to the next fault; pattern oxxo
						dest_f = true; 
					}
				}
			}
			
			if (!skip){
				ModuleElement source = pattern.get(source_i);
				ModuleElement dest = pattern.get(dest_i);
			//	System.out.println("\tsource=" + source.toString() + " dest=" + dest.toString()); 
				
				// calculate mix percentage for non_faulty
				double non_f_mix = 0; 
				ArrayList<ModuleElement> non_f_route = new ArrayList<ModuleElement>(); 
				if (source_i>1) non_f_route.add(pattern.get(source_i-2)); // in order to calculate the mix percentage better
				if (source_i>0) non_f_route.add(pattern.get(source_i-1));
				non_f_route.add(pattern.get(source_i));
				for (int k=1; k<=dest_i-source_i; k++){
					non_f_route.add(pattern.get(source_i+k));
					non_f_mix = this.computeMix(non_f_route, non_f_mix); 
				}
				
				//System.out.println("non_f_route " + non_f_route.toString() + " mix_p = " + non_f_mix); 
				
				// calculate mix percentage for faulty
				Biochip grid = this.waveExpansionAll(m,source,dest,fault_scen);
				// get all shortest routes - I pick  the route with the max mix
				double max_mix = 0; 
				ArrayList<ArrayList<ModuleElement>> route_list = this.getAllShortestRoutes(grid, dest);

				for (int p=0; p<route_list.size(); p++){
					double f_mix = 0; 
					ArrayList<ModuleElement> shortest_route = route_list.get(p); 
					ArrayList<ModuleElement> f_route = new ArrayList<ModuleElement>(); 
					if (pattern.size()>1 && source_i>0) f_route.add(pattern.get(source_i-1));
					f_route.add(pattern.get(source_i));
					for (int k=shortest_route.size()-2; k>=0; k--){
						f_route.add(shortest_route.get(k));
						f_mix = this.computeMix(f_route, f_mix); 
						//System.out.println("add mix ="+ f_mix); 
					}
					//System.out.println("sh_route = " + shortest_route.toString()); 

					//System.out.println("f_route = " + f_route.toString()); 
					if (max_mix<f_mix) max_mix = f_mix;
				}
				
				//System.out.println("max_mix="+ max_mix); 
			
				// calculate total mix percentage
				//System.out.println("cycles: " + no_cycles + " non_f_mix=" + non_f_mix + " f_mix=" + max_mix + " unmix_p="+unmix_p);
				mix_p = mix_p + (unmix_p * no_cycles)- (non_f_mix * no_cycles) + (max_mix*no_cycles); 
				epsilon_cells = epsilon_cells - ((dest_i - source_i)*(int)no_cycles) + (int)(grid.getCell(dest.x, dest.y).value * no_cycles);
				// epsilon_cell = epsilon transitions (moving the droplet, not no of cells)
				//System.out.println("mix_p="+ mix_p + " eps_cells = " + epsilon_cells); 
			}
		}
		
		// continue the mixing until reaches 100%
		ModuleElement start_cell = pattern.get(pattern.size()-1);
		// mixing completion route
		ArrayList<ModuleElement> comp_route = this.generateMixPattern(m, start_cell, new String("ROUNDTHECLOCK"), mix_p); 
		// calculate the ummix number of steps - we allow a mixing between 100 and 101 as good (one step back can get a mix<100 and 
		// we want to make sure that the mix is done, so if the mix_p is >100 and <101, then it is fine
		int unmix_steps = this.computeUnMixRoute(pattern, mix_p); 
		// calculate the final time 
		double droplet_trans_t = 0.01; // hardcoded 
		//System.out.println("comp_route = " + (comp_route.size()-1)); 
		return  (epsilon_cells - unmix_steps +comp_route.size()-1) * droplet_trans_t; 		
		
	}
	
	public int computeUnMixRoute(ArrayList<ModuleElement> pattern,double mix_percentage){
		int unmix_steps = 0; 
		while(mix_percentage > 101){
			ArrayList<ModuleElement> unmix_pattern = new ArrayList<ModuleElement>(); 
			for (int x=unmix_steps; x<unmix_steps+4;x++)
				unmix_pattern.add(pattern.get(pattern.size()-x-1)); 
			mix_percentage = this.computeUnMixStep(unmix_pattern, mix_percentage); 
			unmix_steps++;
		}
		
		//System.out.println("unmix_steps " + unmix_steps); 
		return unmix_steps; 		
	}
	
	/**Use Lee algorithm to calculate the shortest path. Fill the grid until it reaches the destination.
	 * Backtrack the route. See wikipedia. */
	public Biochip waveExpansionOne(Module m, ModuleElement source, ModuleElement dest, ArrayList<ModuleElement> fault_scen){
		// view the module as a grid to use Lee algorithm
		Biochip grid = new Biochip(m.width,m.height);
		Cell s_cell = grid.getCell(source.x, source.y);
		// mark the faulty cells
		for (int k=0; k<fault_scen.size();k++){
			ModuleElement f_cell = fault_scen.get(k);
			grid.getCell(f_cell.x, f_cell.y).isFaulty = true; 
		}
		
		//this.printGrid(grid);
		
		//fill the grid - wave expansion
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
				ArrayList<Cell> neighbor_cells = this.fillNeighbors(grid, crt_cell, crt_value, dest.x, dest.y);
				if (neighbor_cells.size()==0){
					done = true; 
					break;// only one path is returned this way
				}
				else new_filled_cells.addAll(neighbor_cells); 
			}
			if (new_filled_cells.size()==0) done = true; 
			crt_cells = new_filled_cells; 
		}
		
		this.printGrid(grid);

		return grid;
}
	
	/** The difference is that this one expands the wave so that all the short paths are marked*/
	public Biochip waveExpansionAll(Module m, ModuleElement source, ModuleElement dest, ArrayList<ModuleElement> fault_scen){
		// view the module as a grid to use Lee algorithm
		Biochip grid = new Biochip(m.width,m.height);
		Cell s_cell = grid.getCell(source.x, source.y);
		// mark the faulty cells
		for (int k=0; k<fault_scen.size();k++){
			ModuleElement f_cell = fault_scen.get(k);
			grid.getCell(f_cell.x, f_cell.y).isFaulty = true; 
		}
		//this.printGrid(grid);
		
		//fill the grid - wave expansion
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
				ArrayList<Cell> neighbor_cells = this.fillNeighborsAll(grid, crt_cell, crt_value, dest.x, dest.y);
				if (grid.findColumn(crt_cell)==dest.y && grid.findRow(crt_cell)==dest.x){
					done = true; 
					break;
				}
				else new_filled_cells.addAll(neighbor_cells); 
			}
			if (new_filled_cells.size()==0) done = true; 
			crt_cells = new_filled_cells; 
		}
		return grid;
}
	
	public ArrayList<Cell> fillNeighborsAll(Biochip grid, Cell cell, double value, int dest_x, int dest_y){
		int i = grid.findRow(cell); 
		int j = grid.findColumn(cell); 
		ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
		//System.out.println("fill for : " + i + " " + j); 

		// right neighbor - only if it has one 
		if (j+1<grid.width){
			Cell right_n = grid.getCell(i, j+1);
			if (right_n.isFaulty==false && right_n.value<0){
					right_n.value = value; 
					new_filled_cells.add(right_n);
			}
		}
		// left neighbor - only if it has one
		if (j-1>=0){
			Cell left_n = grid.getCell(i, j-1);
			if (left_n.isFaulty==false && left_n.value<0){
				left_n.value = value; 
				new_filled_cells.add(left_n);
			}
		}
		// up neighbor
		if (i-1>=0){
			Cell up_n = grid.getCell(i-1, j);
			if (up_n.isFaulty==false && up_n.value<0){
				up_n.value = value;
				new_filled_cells.add(up_n);
			}
		}
		// down neighbor
		if (i+1<grid.height){
			Cell down_n = grid.getCell(i+1, j);
			if (down_n.isFaulty==false && down_n.value<0){
				down_n.value = value; 
				new_filled_cells.add(down_n);
			}
		}
		//	this.printGrid(grid);
		return new_filled_cells; 
	}
	


	// returns the list of the new marked cells, if the destination is reached, then the list has 0 elements
	// also if not cell was further marked, the list has 0 elements
	public ArrayList<Cell> fillNeighbors(Biochip grid, Cell cell, double value, int dest_x, int dest_y){
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
		
	//	this.printGrid(grid);
		return new_filled_cells; 

	}
	
	public ArrayList<ArrayList<ModuleElement>> getAllShortestRoutes(Biochip grid, ModuleElement dest){
		//backtrack all shortest routes
		ArrayList<ArrayList<ModuleElement>> route_list = new ArrayList<ArrayList<ModuleElement>>(); 
		ArrayList<ModuleElement> crt_route = new ArrayList<ModuleElement>();
		double crt_value = grid.getCell(dest.x, dest.y).value; 
		crt_route.add(dest);
		route_list.add(crt_route);
		//this.printGrid(grid);
		//System.out.println(grid.getCell(dest.x, dest.y).value); 
		//System.out.println("Get sh_route for dest = " + dest.x + dest.y + "crt_value=" + crt_value); 

		while(crt_value>0){
			ArrayList<ArrayList<ModuleElement>> new_route_list = new ArrayList<ArrayList<ModuleElement>>(); 
			crt_value --; 
			for (int k=0; k<route_list.size();k++){
				//System.out.println("k=" + k); 
				crt_route = route_list.get(k); 
				int i = crt_route.get(crt_route.size()-1).x;
				int j = crt_route.get(crt_route.size()-1).y;
				// neighbors
				if (j+1<grid.width){
					Cell right_n = grid.getCell(i, j+1);
					ArrayList<ModuleElement> r_route = this.createNewRoute(grid, crt_route, right_n, crt_value); 
					if (r_route!=null) new_route_list.add(r_route); 	
				}
				if (j-1>=0) {
					Cell left_n = grid.getCell(i, j-1);
					ArrayList<ModuleElement> l_route = this.createNewRoute(grid, crt_route, left_n, crt_value); 
					if (l_route!=null) new_route_list.add(l_route); 
				} 
				if (i-1>=0) {
					Cell up_n = grid.getCell(i-1, j);
					ArrayList<ModuleElement> u_route = this.createNewRoute(grid, crt_route, up_n, crt_value); 
					if (u_route!=null) new_route_list.add(u_route); 
				} 
				if (i+1<grid.height) {
					Cell down_n = grid.getCell(i+1, j);
					ArrayList<ModuleElement> d_route = this.createNewRoute(grid, crt_route, down_n, crt_value); 
					if (d_route!=null) new_route_list.add(d_route); 	
				} 
				//System.out.println("new_route_list = " + new_route_list); 
			}
			route_list = new_route_list; 
		}
		
		return route_list; 
	}
	
	public ArrayList<ModuleElement> createNewRoute(Biochip grid, ArrayList<ModuleElement> crt_route, Cell right_n, double crt_value){
		if (right_n.value == crt_value){
			ArrayList<ModuleElement> new_route = new ArrayList<ModuleElement>();
			for (int p=0; p<crt_route.size(); p++)
				new_route.add(crt_route.get(p)); 
			new_route.add(new ModuleElement(grid.findRow(right_n),grid.findColumn(right_n)));
			return new_route; 
		}
		return null; 
	}
	
	public boolean isCorner(Module m, ModuleElement cell){
		if ((cell.x == 0 && cell.y == 0)
			|| (cell.y==m.width-1 && cell.x==m.height-1) 
			|| (cell.x == m.height-1 && cell.y == 0) 
			|| (cell.y == m.width-1 && cell.x == 0))
			return true;
		return false; 
	}
	
	public ModuleLibrary adjustWCET(ModuleLibrary mLib){
		ArrayList<Module> modList = mLib.getModuleList("mix"); 
		for (int k=0; k<modList.size(); k++){
			Module crt_mod = modList.get(k); 
			//System.out.println(crt_mod.toString()); 
			ModuleElement cell = new ModuleElement(0,0); 
			crt_mod.time += this.computeWCET(crt_mod, this.generateMixPattern(crt_mod, cell, "ROUNDTHECLOCK", 0),"ROUNDTHECLOCK"); 		
			//System.out.println(crt_mod.toString()); 
		}
		ArrayList<Module> modList_dlt = mLib.getModuleList("dilution"); 
		for (int k=0; k<modList_dlt.size(); k++){
			Module crt_mod = modList_dlt.get(k); 
			//System.out.println(crt_mod.toString()); 
			ModuleElement cell = new ModuleElement(0,0); 
			crt_mod.time += this.computeWCET(crt_mod, this.generateMixPattern(crt_mod, cell, "ROUNDTHECLOCK", 0),"ROUNDTHECLOCK"); 		
			//System.out.println(crt_mod.toString()); 
		}

		return mLib; 
	}
	
	public void printGrid(Biochip grid){
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
		System.out.println(" Grid " + s); 
	}
	
	// returns true if there are at least k disjoint paths between source and destination
	// 2 disjoint paths have no electrode in comun, apart from the source and destination 
	public boolean hasDisjointPaths(Module m, ModuleElement source, ModuleElement dest, int k_paths){
		// view the module as a grid to use Lee algorithm
		Biochip grid = new Biochip(m.width,m.height);
		Cell s_cell = grid.getCell(source.x, source.y);
		
		//fill the grid - wave expansion
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
				ArrayList<Cell> neighbor_cells = this.fillNeighborsAll(grid, crt_cell, crt_value, dest.x, dest.y);
				if (grid.findColumn(crt_cell)==dest.y && grid.findRow(crt_cell)==dest.x){
					done = true; 
					break;
				}
				else new_filled_cells.addAll(neighbor_cells); 
			}
			if (new_filled_cells.size()<k_paths) return false; 
			//if (new_filled_cells.size()==0) done = true; 
			crt_cells = new_filled_cells; 
		}
		
		this.printGrid(grid);

		return true; 
	}
	
	
	// we implement the algorithm of Shimon Even 
	public boolean isRoutable(Biochip biochip, int k_conn){
		
		// step1
		for (int i=0; i<k_conn; i++)
			for (int j=i+1;j<=k_conn;j++){	
				ModuleElement source = new ModuleElement(biochip.findRow(biochip.cells.get(i)), biochip.findColumn(biochip.cells.get(i))); 
				ModuleElement dest = new ModuleElement(biochip.findRow(biochip.cells.get(j)), biochip.findColumn(biochip.cells.get(j))); 
				if (!this.hasDisjointPaths(new Module(biochip.width, biochip.height,0), source, dest, k_conn)){
					return false; 
				}
			}
		
		// step2 
		
		
		return true; 
	} 
	
	public ArrayList<Cell> fillGrid(Biochip grid, Cell cell, double value){
		int i = grid.findRow(cell); 
		int j = grid.findColumn(cell); 
		ArrayList<Cell> new_filled_cells = new ArrayList<Cell>();
		//System.out.println("fill for : " + i + " " + j); 

		// right neighbor - only if it has one 
		if (j+1<grid.width){
			Cell right_n = grid.getCell(i, j+1);
			if (right_n.isFaulty==false && right_n.value<0){
					right_n.value = value; 
					new_filled_cells.add(right_n);
			}
		}
		// left neighbor - only if it has one
		if (j-1>=0){
			Cell left_n = grid.getCell(i, j-1);
			if (left_n.isFaulty==false && left_n.value<0){
				left_n.value = value; 
				new_filled_cells.add(left_n);
			}
		}
		// up neighbor
		if (i-1>=0){
			Cell up_n = grid.getCell(i-1, j);
			if (up_n.isFaulty==false && up_n.value<0){
				up_n.value = value;
				new_filled_cells.add(up_n);
			}
		}
		// down neighbor
		if (i+1<grid.height){
			Cell down_n = grid.getCell(i+1, j);
			if (down_n.isFaulty==false && down_n.value<0){
				down_n.value = value; 
				new_filled_cells.add(down_n);
			}
		}
		
	//	this.printGrid(grid);
		return new_filled_cells; 

	}

	// return the time needed to route a droplet from source to destionation on the shortest path
	public double getHadlockRoute(Biochip grid, Rectangle source, Rectangle dest){
		// view the module as a grid to use Lee algorithm
		int s_x = grid.height -1 - source.y_bl - source.height + (int)Math.floor(source.height/2);
		int s_y = source.x_bl + (int)Math.floor(source.width/2);
		int d_x = grid.height -1 - dest.y_bl - dest.height + (int)Math.floor(dest.height/2);
		int d_y = dest.x_bl+ (int)Math.floor(dest.width/2);
		Cell s_cell = grid.getCell(s_x, s_y);
		Cell d_cell = grid.getCell(d_x, d_y);
		
		System.out.println(" Source " + s_x + ", " + s_y + " Dest " + d_x + ", " + d_y); 

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
		
		this.printGrid(grid);
		if (new_filled_cells.size()==0) return null; 
		return new_filled_cells; 

	}

	public int getManhD(int x1, int y1, int x2, int y2){
		return Math.abs(x1-x2)+Math.abs(y1-y2); 
	}

	
	public static void main(String[] args) throws IOException{
		System.out.println("Start testing the ComputeWCET algorithm"); 
		int width = 4; int height = 8; 
		
		Module crt_mod = new Module(width, height, 0); 
		ModuleElement cell = new ModuleElement(0,0); 

		ComputeWCET obj = new ComputeWCET();
		ArrayList<ModuleElement> pattern = obj.generateMixPattern(crt_mod, cell, "ROUNDTHECLOCK", 0); 
		double eps_time = obj.computeWCETSmallerModules(crt_mod, pattern,"ROUNDTHECLOCK"); 
	    ArrayList<ModuleElement> time = obj.generateMixPattern(new Module(4 , 8, 0), new ModuleElement(0, 0), "ROUNDTHECLOCK", 0);
		
		System.out.println("Module " + crt_mod.toString() + "Pattern (RTC) takes " + pattern.size()*0.01 + " new = " + time.size()); 
		System.out.println("Eps time = " + eps_time + " WCET = " + (pattern.size()*0.01 - eps_time)); 
	} 
	


}
