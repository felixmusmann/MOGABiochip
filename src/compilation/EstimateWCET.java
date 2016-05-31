/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

import java.util.ArrayList;

/** This class is suposed to handle input/output files, i.e. reading/writing from/to the files*/
public class EstimateWCET {

	public EstimateWCET(){}
	
	public static CRMModule estimateWCET_MIX(ArrayList<Cell> CRM){
		/** CALCULATIONS FOR MIXING */
		double t0_exe = EstimateWCET.estimateCompletionTimeNoFaults(CRM); 
		double p0_cycle = 100 / (t0_exe / (0.01 * CRM.size())); 
		double p1_cycle = p0_cycle - 1 * 0.58 - 0.5; 
		double e1_cycle = CRM.size() - 1; // number of electrodes in a cycle (k=1 fault)
		double p2_cycle = (p0_cycle / 2) - (2*0.58) - 0.5; 
		double e2_cycle = Math.floor(CRM.size()/2) - 1; //no. of electrodes per cycle (k=2 faults)
		
		double t1_exe = calculateExecutionTime(p1_cycle, e1_cycle); 
		double t2_exe = calculateExecutionTime(p2_cycle, e2_cycle);  
		CRMModule CRM_new = new CRMModule(CRM, t0_exe, t1_exe, t2_exe, "MIX"); 

		return CRM_new; // in seconds , 0.01 the time needed for a move. hard coded 
	}
	
	public static CRMModule estimateWCET_DILUTION(ArrayList<Cell> CRM){
		/** CALCULATIONS FOR DILUTION */
		double t0_exe = EstimateWCET.estimateCompletionTimeNoFaults_DILUTION(CRM); 
		double p0_cycle = 100 / (t0_exe / (0.01 * CRM.size())); 
		double p1_cycle = p0_cycle - 1 * 0.32 - 0.5; 
		double e1_cycle = CRM.size() - 1; // number of electrodes in a cycle (k=1 fault)
		double p2_cycle = (p0_cycle / 2) - (2*0.32) - 0.5; 
		double e2_cycle = Math.floor(CRM.size()/2) - 1; //no. of electrodes per cycle (k=2 faults)
		
		double t1_exe = calculateExecutionTime(p1_cycle, e1_cycle); 
		double t2_exe = calculateExecutionTime(p2_cycle, e2_cycle);  
		CRMModule CRM_new = new CRMModule(CRM, t0_exe, t1_exe, t2_exe, "DILUTION"); 

		return CRM_new; // in seconds , 0.01 the time needed for a move. hard coded 
	}
	
	public static double calculateExecutionTime(double p_cycle, double e_cycle){
		return ((Math.abs(100/p_cycle) + 1) * e_cycle * 0.01); 
	}
	
	public static double estimateCompletionTimeNoFaults(ArrayList<Cell> CRM){
		ArrayList<Cell> route = new ArrayList<Cell>();
		double mix_p = 0; // mix percentage per cycle
		for (int i=0; i<CRM.size(); i++){
			Cell crt_cell = CRM.get(i);
			double crt_mix = calculateMixPercentage(route, crt_cell,route.size());
			mix_p += crt_mix; 
			route.add(crt_cell); 
		}
		if (mix_p <= 0) return 10000; // TODO: remove this line and fix the bug
		return ((Math.floor(100/mix_p) + 1) * (CRM.size() * 0.01));
	}
	
	public static double estimateCompletionTimeNoFaults_DILUTION(ArrayList<Cell> CRM){
		ArrayList<Cell> route = new ArrayList<Cell>();
		double mix_p = 0; // mix percentage per cycle
		for (int i=0; i<CRM.size(); i++){
			Cell crt_cell = CRM.get(i);
			double crt_mix = calculateMixPercentage_DILUTION(route, crt_cell,route.size());
			mix_p += crt_mix; 
			route.add(crt_cell); 
		}
		if (mix_p <= 0) return 10000; // TODO: remove this line and fix the bug
		return ((Math.floor(100/mix_p) + 1) * (CRM.size() * 0.01));
	}



	/**Calculated the mixing percentage for a cell and returns it. 
	 * The CRM is needed as input, since the mix impact depends on previous moves. */
	//TODO: does it matter if we calculate for dilution or mixing? proportionally, how are they? 
	public static double calculateMixPercentage(ArrayList<Cell> CRM, Cell cell, int crt_index){
		double mix_percentage; 
		int i_k = -1; 
		int j_k = -1;
		int i_k_2 = -1; 
		int j_k_2 = -1;
		int i_k_3 = -1; 
		int j_k_3 = -1;
		int i_k_4 = -1; 
		int j_k_4 = -1;

		i_k = cell.x; 
		j_k = cell.y;


		if (crt_index>0){
			i_k_2 = CRM.get(crt_index-1).x; 
			j_k_2 = CRM.get(CRM.size()-1).y;
		}
		if (crt_index>1){
			i_k_3 = CRM.get(crt_index-2).x; 
			j_k_3 = CRM.get(crt_index-2).y;
		}

		if (crt_index>2){
			i_k_4 = CRM.get(crt_index-3).x; 
			j_k_4 = CRM.get(crt_index-3).y;
		}

		mix_percentage = 0; 

		//System.out.println(i_k_4 + "," + j_k_4 + " " + i_k_3 + "," + j_k_3 + " " + i_k_2 + "," + j_k_2 + " " + i_k + "," + j_k); 

		// calculate degrees degrees 
		if ((i_k_3 == i_k) && (j_k_3 == j_k)) {
			mix_percentage = -0.5; // 180 degrees
			return mix_percentage; 
		}

		if (EstimateWCET.is90degrees(i_k_3, j_k_3, i_k, j_k)) mix_percentage = 0.1; // 90 degrees

		// one 0 degrees 
		if (EstimateWCET.is90degrees(i_k_4, j_k_4, i_k_2, j_k_2) && EstimateWCET.is0degrees(i_k_3, j_k_3, i_k_2, j_k_2) && EstimateWCET.is0degrees(i_k_2, j_k_2, i_k, j_k)
				&& EstimateWCET.is0degrees(i_k_3, j_k_3, i_k, j_k))
			mix_percentage = 0.29; 

		// two or more 0 degrees 
		if (EstimateWCET.is0degrees(i_k_4, j_k_4, i_k_3, j_k_3) && EstimateWCET.is0degrees(i_k_3, j_k_3, i_k_2, j_k_2) && EstimateWCET.is0degrees(i_k_2, j_k_2, i_k, j_k)
				&& EstimateWCET.is0degrees(i_k_3, j_k_3, i_k, j_k) && EstimateWCET.is0degrees(i_k_4, j_k_4, i_k_2, j_k_2))
			mix_percentage = 0.58; 

		//System.out.println("For cell "+ cell.x + " " + cell.y + " computed " + mix_percentage + "%"); 
		return mix_percentage;

	}
	
	public static double calculateMixPercentage_DILUTION(ArrayList<Cell> CRM, Cell cell, int crt_index){
		double mix_percentage; 
		int i_k = -1; 
		int j_k = -1;
		int i_k_2 = -1; 
		int j_k_2 = -1;
		int i_k_3 = -1; 
		int j_k_3 = -1;
		int i_k_4 = -1; 
		int j_k_4 = -1;

		i_k = cell.x; 
		j_k = cell.y;


		if (crt_index>0){
			i_k_2 = CRM.get(crt_index-1).x; 
			j_k_2 = CRM.get(CRM.size()-1).y;
		}
		if (crt_index>1){
			i_k_3 = CRM.get(crt_index-2).x; 
			j_k_3 = CRM.get(crt_index-2).y;
		}

		if (crt_index>2){
			i_k_4 = CRM.get(crt_index-3).x; 
			j_k_4 = CRM.get(crt_index-3).y;
		}

		mix_percentage = 0; 

		//System.out.println(i_k_4 + "," + j_k_4 + " " + i_k_3 + "," + j_k_3 + " " + i_k_2 + "," + j_k_2 + " " + i_k + "," + j_k); 

		// calculate degrees degrees 
		if ((i_k_3 == i_k) && (j_k_3 == j_k)) {
			mix_percentage = -0.5; // 180 degrees
			return mix_percentage; 
		}

		if (EstimateWCET.is90degrees(i_k_3, j_k_3, i_k, j_k)) mix_percentage = 0.08; // 90 degrees

		// one 0 degrees 
		if (EstimateWCET.is90degrees(i_k_4, j_k_4, i_k_2, j_k_2) && EstimateWCET.is0degrees(i_k_3, j_k_3, i_k_2, j_k_2) && EstimateWCET.is0degrees(i_k_2, j_k_2, i_k, j_k)
				&& EstimateWCET.is0degrees(i_k_3, j_k_3, i_k, j_k))
			mix_percentage = 0.21; 

		// two or more 0 degrees 
		if (EstimateWCET.is0degrees(i_k_4, j_k_4, i_k_3, j_k_3) && EstimateWCET.is0degrees(i_k_3, j_k_3, i_k_2, j_k_2) && EstimateWCET.is0degrees(i_k_2, j_k_2, i_k, j_k)
				&& EstimateWCET.is0degrees(i_k_3, j_k_3, i_k, j_k) && EstimateWCET.is0degrees(i_k_4, j_k_4, i_k_2, j_k_2))
			mix_percentage = 0.32; 

		//System.out.println("For cell "+ cell.x + " " + cell.y + " computed " + mix_percentage + "%"); 
		return mix_percentage;

	}
	
	/** Checks if there are 90 degrees between point1 (x1, y1) and point2 (x2, y2)*/
	public static boolean is90degrees(int x1, int y1, int x2, int y2){
		if ((x1 != x2) && (y1 != y2)) return true; 
		return false; 
	}

	/** Checks if there are 0 degrees between point1 (x1, y1) and point2 (x2, y2)*/
	public static boolean is0degrees(int x1, int y1, int x2, int y2){
		if ((x1 == x2) || (y1 == y2)) return true; 
		return false; 
	}
	
	public static CRMModule calculateWCET_PRECISE(ArrayList<Cell> CRM){
		double mix_p = 0; 
		//int no_moves = 0; 
		boolean not_mixed = true; 
		ArrayList<Cell> route = new ArrayList<Cell>();
		double p_cycle = 0; // the percentage of mixing obtained in a cycle 
		double t_cycle = 0; // the number of eletrodes traversed in a cycle 
		double no_cycle = 0; 
		while (not_mixed){
			no_cycle ++; 
			for (int i=0; i<CRM.size() && not_mixed; i++){
				Cell crt_cell = CRM.get(i);
				double crt_mix = EstimateWCET.calculateMixPercentage(route, crt_cell,route.size());
				mix_p += crt_mix; 
				route.add(crt_cell); 
				//System.out.print(" " + crt_cell.x + "," + crt_cell.y + " = " + crt_mix); 
				//no_moves ++; 
				if (mix_p >= 100) not_mixed = false; 
			}
			if (no_cycle == 1){
				p_cycle = mix_p; 
				t_cycle = route.size(); 
			}
		}
		double t0_exe = (route.size()*0.01); 
		double t1_exe = (Math.floor(100/(p_cycle-0.5)) + 1) * ((t_cycle - 1) * 0.01); 
		double t2_exe = (Math.floor(100/(p_cycle/2-0.5)) + 1) * ((t_cycle/2 - 1) * 0.01); 
		CRMModule CRM_new = new CRMModule(CRM, t0_exe, t1_exe, t2_exe, "MIX"); 

		return CRM_new; // in seconds , 0.01 the time needed for a move. hard coded 
	}

		
}
