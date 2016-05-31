/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;


public class LSPR {

	public double LSSynthWRouting_CRM (Biochip biochip, ModuleLibrary mLib, CRMLibrary libCRM, DirectedGraph ftGraph, double deadline){
		CRMSyn synCRM = new CRMSyn(biochip); 
		ArrayList<Node> readyL = new ArrayList<Node>(ftGraph.nodes.size()); 
		ArrayList<Node> runningOpsL = new ArrayList<Node>(ftGraph.nodes.size()); 
		ArrayList<Node> tempOpsL = new ArrayList<Node>(); // for stores 

		/* CLEAN biochip, calculate the PRIORITIES and compute READY LIST*/
		this.cleanBiochipForNewUse(biochip); // TODO check if the parameters are trasmitted through reference
		ftGraph.calcCriticalityFactor(mLib); 
		computeInitialReadyList(ftGraph, readyL, biochip); // TODO check if the parameters are trasmitted through reference

		double crt_time = 0; 
		//for (int x = 0; x<10; x++){
		while (!readyL.isEmpty()){	
			crt_time +=0.01;
			//crt_time = syn.minStopTime(runningOpsL, crt_time);	/*TODO update time - skipping to the nearest time when an operations is finished */
			if (crt_time >= 1000) break; 
			/* check for finished operations and create stores for them in tempOpsL */
			synCRM.cleanBiochip(runningOpsL, ftGraph, crt_time, biochip, libCRM, tempOpsL);
			/* sort the nodes according to their priority */
			Collections.sort(readyL, new Comparator<Node>(){
				public int compare(Node o1, Node o2) {
					if (o1.critFactor < o2.critFactor)  return 1; 
					else 	if (o1.critFactor > o2.critFactor)	return -1; 
					else return 0; }});

			boolean stillReadyOps = true; 
			CRMModule bestMod =  null; Device bestDev = null; 
			while(stillReadyOps){
				stillReadyOps = false; 
				/* process the ready operations */
				ListIterator<Node> readyListIt = readyL.listIterator(); 
				while (readyListIt.hasNext()){ 
					Node v = readyListIt.next();
					if (crt_time >= v.data.start_t){ /* check for start new operations */
						//System.out.println("CRT node = " + v.toString() + " v_data_start = " + v.data.start_t); // + " start= "+ v.data.start_t + " stop=" + v.data.stop_t ); 
						boolean hasINsReady = hasInputsReadyForExecution(biochip, ftGraph, synCRM, crt_time, v);
						if (hasINsReady){
							synCRM.destroyStores(v, tempOpsL, ftGraph,crt_time); 
							if (v.isReconfig()){
								bestMod = synCRM.allocate(v,biochip, crt_time, libCRM);		
								if (bestMod!=null){
									synCRM.scheduleWRouting(v, crt_time, bestMod.t0_exe, ftGraph, libCRM);
									//System.out.println(" Op " + v.getName() + " " + v.data.start_t + " --- " + v.data.stop_t + " " +  bestMod.t0_exe + " CRM " + bestMod.id); 
									//System.out.println("\nAfter SCHEDULE" + libCRM); 
								}
							}

							if (!v.isReconfig()){
								bestDev = biochip.getFreeDevice(v, crt_time);
								if (bestDev != null){
									if (bestDev.isPlaced()) bookDevice(biochip, crt_time, v);
									else placeDeviceFirstTime(bestDev, crt_time, v, synCRM, ftGraph,  biochip);
									synCRM.scheduleWRouting(v, crt_time, bestDev.time, ftGraph, libCRM);
								}
							}

							if (bestMod == null || bestDev == null){ /* no space at the moment of the biochip -> need to store the input droplets */
								synCRM.createStores(v, tempOpsL, biochip, libCRM, crt_time, ftGraph);
								releaseBookedReservoirs(biochip, ftGraph, crt_time, v, hasINsReady);
							}
							/* update readyList and running list  */
							v.isVisited = true; 
							readyListIt.remove(); 
							runningOpsL.add(v);
							/* add the ready succs to the readyList */
							for (int i=0; i<v.succs.size(); i++){
								Node s = ftGraph.getNode(v.succs.get(i)); 
								if (ftGraph.isReady(s)){
									s.data.start_t = synCRM.getStartTime(s, ftGraph, crt_time); 
									readyListIt.add(s);
								}
							}
						}
					}
				}
			}
		}
		// calculate the maximum execution time of the running operations
		// add it to the crt_time to get the completion time 
		double max_time = 0; 
		for (int i=0; i<runningOpsL.size(); i++){
			if(
					(runningOpsL.get(i).getType().toString().compareTo("STORE")!=0) 
					&& 
					(runningOpsL.get(i).getName().toString().startsWith("R")==false)
			){
				double v_time = runningOpsL.get(i).data.stop_t - crt_time; //- runningOpsL.get(i).data.start_t;
				if (v_time>max_time) max_time = v_time; 
			}
		}
		// NOW time for some WCET estimation (for a maximum number of k faults) 
		
		return this.estimateFaultyScheduleLength(Main.K_MAX, ftGraph); 
	}
	
	/** After I have done the scheduling, I need to traverse the graph and determine the critical path. How do I do that? */
	public ArrayList<Node> determineCriticalPath(DirectedGraph subGraph){		
		
		ArrayList<ArrayList<Node>> allPathsL = determineAllPathsInAgraph(subGraph);	
		double criticalPathLength = 0; 
		ArrayList<Node> criticalPath = new ArrayList<Node>(); 
		for (ArrayList<Node> path : allPathsL){
			double pathLength = calculateLengthOfExecutedPath(path); 
			if (pathLength > criticalPathLength){
				criticalPathLength = pathLength; 
				criticalPath = path; 		
			}
		}
		return criticalPath; 
	}
	
	public ArrayList<ArrayList<Node>> determineAllPathsInAgraph(DirectedGraph subGraph){
		
		ArrayList<ArrayList<Node>> allPathsL_old = new ArrayList<ArrayList<Node>>(); 
		ArrayList<ArrayList<Node>> allPathsL_new = new ArrayList<ArrayList<Node>>(); 
		ArrayDeque<Node> q_old = new ArrayDeque<Node>(subGraph.nodes.size());
		ArrayDeque<Node> q_new = new ArrayDeque<Node>(subGraph.nodes.size());

		// enqueue the childless nodes 
		for (Node n : subGraph.nodes){
			if (n.succs.size() == 0){
				q_old.offer(n); 
				ArrayList<Node> newPath = new ArrayList<Node>(); 
				newPath.add(n); 
				allPathsL_old.add(newPath); 
			}
		}
		do {
			while(!q_old.isEmpty()){
				Node v = q_old.poll();
				for (StringBuffer predName : v.preds){
					Node pred = subGraph.getNode(predName); 
					q_new.offer(pred); 	
					ArrayList<ArrayList<Node>> allPathsContainNode = determineAllPathThatContainNode(v, allPathsL_old); 
					for (ArrayList<Node> path : allPathsContainNode){
						ArrayList<Node> path_new = new ArrayList<Node>(path); 
						path_new.add(pred); 
						allPathsL_new.add(path_new); 
					}
				}
			}
			if (!q_new.isEmpty()){
				allPathsL_old = new ArrayList<ArrayList<Node>>(allPathsL_new);
				allPathsL_new = new ArrayList<ArrayList<Node>>(); 
			}
			q_old = q_new;
			q_new = new ArrayDeque<Node>();
		} while (!q_old.isEmpty()); 
		return allPathsL_old; 
	}
	
	public ArrayList<ArrayList<Node>> determineAllPathThatContainNode(Node v, ArrayList<ArrayList<Node>> allPathsL_old){
		ArrayList<ArrayList<Node>> restrictedPathsL = new  ArrayList<ArrayList<Node>>(); 
		for (ArrayList<Node> path : allPathsL_old){
			if (path.contains(v)) restrictedPathsL.add(path); 
		}
		return restrictedPathsL; 
	}
	
	// this path is already executed, so each node has an start and finish time
	// the length of the path is the latest finish time among all operations
	public double calculateLengthOfExecutedPath(ArrayList<Node> path){
		double pathLength = 0; 
		for (Node op : path){
			if (op.data.stop_t > pathLength)
				pathLength = op.data.stop_t; 
		}
		return pathLength; 
	}
	
	/** This method distributes the k_max faults to the operations in a greedy way. *//*
	public double calculateIncreaseTimeBecauseOfFaults(int k_max, ArrayList<Node> criticalPath){
		double increaseInTime = 0; 
		ArrayList<Node> faultyL = new ArrayList<Node>(criticalPath.size()); 
		
		for (int k_crt=0; k_crt<k_max; k_crt++){
			double t_max = 0; 
			Node op_faulty= new Node(); 
			// determine the faulty operation from critical path
			for (Node op_crit: criticalPath){
				int k_assigned = Collections.frequency(faultyL, op_crit); 
				CRMModule CRM_crt = op_crt.CRM; 
				double t_crt = 0;
				if (k_assigned == 0)
					t_crt = CRM_crt.t1_exe; 
				else if (k_assigned == 1)
					t_crt = CRM_crt.t2_exe; 
				else System.err.println("More than 2 permanent faults assigned to one operation");
				if (t_crt > t_max){
					t_max = t_crt; 
					op_max = new Node(op_crt); 
				}		
			}
			faultyL.add(op_max); 
			increaseInTime += t_max; 
		}
		
		return increaseInTime; 
	}*/

	/** This method distributes the k_max faults to the operations in a greedy way. */
	public double estimateFaultyScheduleLength(int k_max, DirectedGraph graph){
		for (int k_crt=0; k_crt<k_max; k_crt++){
			ArrayList<Node> criticalExePath = this.determineCriticalPath(graph); 
			StringBuffer op_faulty_name = getOperationWithWorstImpact(criticalExePath, graph);
			updateSchedule(op_faulty_name, graph); 
		}
		return calculateScheduleLength(graph); 
	}
	
	public StringBuffer getOperationWithWorstImpact(ArrayList<Node> criticalPath, DirectedGraph graph){
		StringBuffer op_faultyName = new StringBuffer(); 
		double t_faulty = 0; 
		for (Node op : criticalPath){
			double t_op = estimateFaultyCompletionTime(op.getName(), graph);
			if (t_op > t_faulty){
				t_faulty = t_op;
				op_faultyName = op.getName(); 
			}
		}
		return op_faultyName; 
	}
	
	public double estimateFaultyCompletionTime(StringBuffer opName, DirectedGraph graph){
		// get a copy of the original, non-faulty (with the current fault) graph
		DirectedGraph graph_faulty = new DirectedGraph(graph);
		Node op_faulty = graph_faulty.getNode(opName); 
		double diff = getDifferenceInExeTimes(op_faulty); 
		if (diff > 0){	
			ArrayDeque<Node> q = new ArrayDeque<Node>(); 
			q.offer(op_faulty);
			while(!q.isEmpty()){
				Node v = q.poll();
				v.data.stop_t += diff;
				// add the successors 
				for (StringBuffer succ : v.succs)
					q.offer(graph_faulty.getNode(succ)); 
				// add the operations that have conflictual use of the same module 
				for (Node op_j : graph_faulty.nodes){
					if ((op_j.CRM.id.compareTo(v.CRM.id) == 0) && 
							(op_j.data.start_t < v.data.stop_t) && 
					        (op_j.getName().toString().compareTo(v.getName().toString())!=0)){
						q.offer(op_j); 
					}
				}
			}
			
		}// TODO: get a check for the corner case when diff <= 0 (should not happen)
		return this.calculateScheduleLength(graph_faulty); 
	}
	
	public void updateSchedule(StringBuffer opName, DirectedGraph graph){
		// get a copy of the original, non-faulty (with the current fault) graph
		Node op_faulty = graph.getNode(opName); 
		double diff = getDifferenceInExeTimes(op_faulty); 
		if (diff > 0){	
			ArrayDeque<Node> q = new ArrayDeque<Node>(); 
			q.offer(op_faulty);
			while(!q.isEmpty()){
				Node v = q.poll();
				v.data.stop_t += diff;
				// add the successors 
				for (StringBuffer succ : v.succs)
					q.offer(graph.getNode(succ)); 
				// add the operations that have conflictual use of the same module 
				for (Node op_j : graph.nodes){
					if ((op_j.CRM.id!= null && op_j.CRM.id.compareTo(v.CRM.id) == 0) && 
							(op_j.data.start_t < v.data.stop_t) && (op_j.data.start_t > v.data.stop_t - diff) && 
					        (op_j.getName().toString().compareTo(v.getName().toString())!=0)){
						q.offer(op_j); 
					}
				}
			}
			
		}// TODO: get a check for the corner case when diff <= 0 (should not happen)
	}
	
	// just upgrade the current number of assigned faults to the next one
	// if already two faults assigned, then return 0 (note: it should not get into that situation)
	public double getDifferenceInExeTimes(Node op_faulty){
		CRMModule crm_mod = op_faulty.CRM; 
		double exe_time = op_faulty.getExeTime(); 
		if (exe_time == crm_mod.t0_exe)
			return crm_mod.t1_exe - exe_time; 
		if (exe_time == crm_mod.t1_exe)
			return crm_mod.t2_exe - exe_time; 	
		return 0; 
	}

	public double calculateScheduleLength(DirectedGraph graph){
		double scheduleLength = 0; 
		for (Node n: graph.nodes)
			if (n.data.stop_t > scheduleLength)
				scheduleLength = n.data.stop_t; 
		return scheduleLength; 
	}

	public void bookDevice(Biochip biochip, double crt_time, Node v) {
		/* the device has had assigned coordinates before- place the device- I should use the placer*/
		biochip.occupy(v.module);
		/* schedule the device*/
		Device dev = biochip.getDevice(v.module); 
		if (dev!=null) {
			dev.stop_t = crt_time + dev.time; 
		}
	} 
	public void releaseBookedReservoirs(Biochip biochip, DirectedGraph ftGraph, double crt_time, Node v, boolean hasINsReady) {
		/* release the corresponding non-reconfigurable reservoirs*/
		for (int i =0; i<v.preds.size() && hasINsReady; i++){
			Node p = ftGraph.getNode(v.preds.get(i)); 
			if (p.data.super_type.toString().compareTo("IN") == 0){
				biochip.releaseDevice(p.data.type.toString(), crt_time); 
			}
		}
	}

	public void placeDeviceFirstTime(Device dev, double crt_time, Node v, CRMSyn synCRM, DirectedGraph ftGraph, Biochip biochip) {
		Module bestMod = new Module(dev.width, dev.height, dev.time); 
		Rectangle r = synCRM.placer.placeKAMER(bestMod, v, ftGraph); 
		v.module = new Rectangle(r);
		dev.x_bl = v.module.x_bl; 
		dev.y_bl = v.module.y_bl;
		dev.stop_t = crt_time + dev.time; 
		/* the device has had assigned coordinates before- place the device- I should use the placer*/
		biochip.occupy(v.module);
	}

	public boolean hasInputsReadyForExecution(Biochip biochip,
											  DirectedGraph ftGraph, CRMSyn synCRM, double crt_time, Node v) {
		boolean hasINsReady = true; // has all predecesessors that are dispensing operations schedulable 
		for (int i =0; i<v.preds.size() && hasINsReady; i++){
			Node p = ftGraph.getNode(v.preds.get(i)); 
			if (p.data.super_type.toString().compareTo("IN") == 0){
				//System.out.println(p.toString()); 
				Device in_dev = synCRM.allocateDispensingReservoirs(p, biochip, crt_time); 
				if (in_dev == null) hasINsReady = false; 
			}
		}
		return hasINsReady;
	}

	public void computeInitialReadyList(DirectedGraph ftGraph, ArrayList<Node> readyL, Biochip biochip) {
		/* add all the parentless nodes (i.e. ready operations) to the ready list */
		for (int i=0; i<ftGraph.nodes.size(); i++){
			Node n = ftGraph.nodes.get(i); 
			n.isVisited = false; 
			if (n.preds.size() == 0 && n.data.super_type.toString().compareTo("IN")!= 0){
				readyL.add(n); 
				n.data.start_t = 0; 
				n.data.stop_t = -1;
			}else {
				int crt_t = 0; 
				if (ftGraph.isReady(n, crt_t, biochip)){
					readyL.add(n);
				}
			}
		}
	}

	public void cleanBiochipForNewUse(Biochip biochip) {
		// clean biochip and graph
		biochip.clean(); 
		for (int i=0; i<biochip.devices.size(); i++){
			biochip.devices.get(i).stop_t = 0 - biochip.devices.get(i).time; 
			System.out.println("Dev " + biochip.devices.get(i).toString()); 
		}
	}
	
	

	/*public double LSSynthWRouting(Biochip  biochip, ModuleLibrary mLib, DirectedGraph ftGraph, boolean KAMER, double deadline){
		SynMechanism syn = new SynMechanism(biochip); 
		ArrayList<Node> readyL = new ArrayList<Node>(ftGraph.nodes.size()); 
		ArrayList<Node> runningOpsL = new ArrayList<Node>(ftGraph.nodes.size()); 
		ArrayList<Node> tempOpsL = new ArrayList<Node>(); // for stores 
		// clean biochip and graph
		biochip.clean(); 
		for (int i=0; i<biochip.devices.size(); i++){
			biochip.devices.get(i).stop_t = 0 - biochip.devices.get(i).time; 
		}

		 calculate the priorities based on critical path
		ftGraph.calcCriticalityFactor(mLib); 

		 add all the parentless nodes (i.e. ready operations) to the ready list 
		for (int i=0; i<ftGraph.nodes.size(); i++){
			Node n = ftGraph.nodes.get(i); 
			n.isVisited = false; 
			if (n.preds.size() == 0 && n.data.super_type.toString().compareTo("IN")!= 0){
				readyL.add(n); 
				n.data.start_t = 0; 
				n.data.stop_t = -1;
			}else {
				int crt_t = 0; 
				if (ftGraph.isReady(n, crt_t, syn.placer.biochip)){
					readyL.add(n);
				}
			}
		}

		 initialize current time
		double crt_time = -0.01; 
		//double crt_time = 0; 
		//for (int x = 0; x<10; x++){
		while (!readyL.isEmpty()){		
			TODO update time - skipping to the nearest time when an operations is finished 
			crt_time +=1;
			//crt_time = syn.minStopTime(runningOpsL, crt_time);
			if (crt_time >= 1000) {
					break; 
			}
			 check for finished operations and create stores for them in tempOpsL 
			syn.cleanBiochip(runningOpsL, ftGraph, crt_time, biochip, mLib, tempOpsL, KAMER);
			 sort the nodes according to their priority 
			Collections.sort(readyL, new Comparator<Node>(){
				public int compare(Node o1, Node o2) {
					if (o1.critFactor < o2.critFactor)  return 1; 
					else 	if (o1.critFactor > o2.critFactor)	return -1; 
					else return 0; }});

			boolean stillReadyOps = true; 
			while(stillReadyOps){
				stillReadyOps = false; 
				 process the ready operations 
				ListIterator<Node> readyListIt = readyL.listIterator(); 
				while (readyListIt.hasNext()){ 
					Node v = readyListIt.next();
					 check for start new operations 
					if (crt_time >= v.data.start_t){
						boolean hasINsReady = true; // has all predecesessors that are dispensing operations schedulable 
						for (int i =0; i<v.preds.size() && hasINsReady; i++){
							Node p = ftGraph.getNode(v.preds.get(i)); 
							if (p.data.super_type.toString().compareTo("IN") == 0){
								ArrayList<Module> modList = mLib.getModuleList(p.data.type.toString());
								Module in_dev = syn.allocate(p, ftGraph, biochip, modList, crt_time - mLib.getMaxTime("IN"));
								if (in_dev == null) {
									hasINsReady = false; 
								} else {
								}
							}; 
						}

						if (hasINsReady){
							 allocation of place-able and route-able modules 
							syn.destroyStores(v, tempOpsL, ftGraph,crt_time); // make more room 	
							ArrayList<Module> modList = mLib.getModuleList(v.data.type.toString());
							Module bestMod = syn.allocate(v,ftGraph,  biochip, modList, crt_time);
							if (bestMod == null && v.isReconfig()){
								 either no modules defined in library, or the operations requires a bigger biochip 
								System.out.println("\n\tERR allocation = NULL");
								System.exit(-1); 
								break; 
							}
							if (bestMod!=null){
								if (bestMod.time < 0){
									System.out.println("ERR - module in library with time -1"+ bestMod.toString()); 
									break;  
								}

								if (bestMod.time == 1000){
									 no space at the moment of the biochip -> need to store the input droplets 
									syn.createStores(v, tempOpsL, biochip, mLib, crt_time, ftGraph,KAMER);
									 release the corresponding non-reconfigurable reservoirs
									for (int i =0; i<v.preds.size() && hasINsReady; i++){
										Node p = ftGraph.getNode(v.preds.get(i)); 
										if (p.data.super_type.toString().compareTo("IN") == 0){
											biochip.releaseDevice(p.data.type.toString(), crt_time); 
										}
									}
								}

								 For zero-time operations NO placement, NO binding, only scheduling 
								if (bestMod.time != 1000){
									if ((bestMod.time > 0) && (bestMod.getArea()>0)){
										if (v.isReconfig()){
											 placement with routing check inside 
											Rectangle r; 
											if (KAMER) 	r = syn.placer.placeKAMER(bestMod, v, ftGraph); 
											else  r = syn.placer.placeModule(bestMod, "BER", v, ftGraph); 
											 binding 
											v.module = new Rectangle(r); 

										} 
										if (!v.isReconfig()){
											 the device is already bound  
											if (v.module.isNotPlaced(biochip.width, biochip.height)) {
												Rectangle r; 
												 first time use - we need to place the device, i.e. 
	 * to establish some coordinates that will remain fixed afterwards 
												if (KAMER) 	r = syn.placer.placeKAMER(bestMod,v,ftGraph); 
												else r = syn.placer.placeModule(bestMod, "BER", v, ftGraph);
												v.module = new Rectangle(r);
												Device dev = biochip.getFreeDevice(v.getType().toString(), crt_time+bestMod.time);
												if (dev!=null){
													dev.x_bl = v.module.x_bl; 
													dev.y_bl = v.module.y_bl;
													dev.stop_t = crt_time + dev.time; 
												}
											} else {
												 the device has had assigned coordinates before- place the device- I should use the placer
												biochip.occupy(v.module);
												 schedule the device
												Device dev = biochip.getDevice(v.module); 
												if (dev!=null) {
													dev.stop_t = crt_time + dev.time; 
												}
											}
										}
									}
									 schedule   
									syn.schedule(v, bestMod, crt_time); 
									//syn.scheduleWRouting(v, bestMod, crt_time,ftGraph);
									 update readyList 
									v.isVisited = true; 
									readyListIt.remove(); 							

									 update running list 
									if (bestMod.time == 0){ // time-zero operations
										stillReadyOps = true; 
									} else runningOpsL.add(v);

									 add the ready succs to the readyList 
									for (int i=0; i<v.succs.size(); i++){
										Node s = ftGraph.getNode(v.succs.get(i)); 
										if (ftGraph.isReady(s)){
											s.data.start_t = syn.getStartTime(s, ftGraph, crt_time); 
											readyListIt.add(s);
										}
									}

								}
							}

						}
					}
				}
			}			
		}	
		// calculate the maximum execution time of the running operations
		// add it to the crt_time to get the completion time 
		double max_time = 0; 
		for (int i=0; i<runningOpsL.size(); i++){
			if(
					(runningOpsL.get(i).getType().toString().compareTo("STORE")!=0) 
					&& 
					(runningOpsL.get(i).getName().toString().startsWith("R")==false)
			){
				double v_time = runningOpsL.get(i).data.stop_t - crt_time; //- runningOpsL.get(i).data.start_t;
				if (v_time>max_time) max_time = v_time; 
			}
		}
		return crt_time + max_time; 
	}*/

}
