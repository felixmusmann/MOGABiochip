/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;

public class DirectedGraph {
	
	ArrayList<Node> nodes = new ArrayList<>();
	ArrayList<Edge> edges = new ArrayList<>();
	
	public DirectedGraph(){
		nodes = new ArrayList<>();
		edges = new ArrayList<>();
	}
	
	/** Creates a new DirectedGraph object that is a copy of the argument.*/
	public DirectedGraph(DirectedGraph g){
		this.nodes = new ArrayList<>(g.size());
		this.edges = new ArrayList<>(g.edges.size());
		for (int i = 0; i<g.nodes.size(); i++){
			this.nodes.add(new Node(g.nodes.get(i))); 
		}
		for (int i = 0; i<g.edges.size(); i++){
			this.edges.add(new Edge(g.edges.get(i).src,g.edges.get(i).dest)); 
		}
	}
	
	// for the sake of using STORE efficiently, we need to clear all waste operations 
	// TODO ignore any waste operation (go through the graph and clean it of waste operations)
	/** Creates a new DirectedGraph object with the nodes and edges described in appFile.
	 * Go to readme.txt for the notation convention.*/
	public DirectedGraph(String appFile) 
	throws IOException{
		// "read" the application graph from input file 
		Scanner s = new Scanner(new File(appFile)); 
		try{
			while(s.hasNext()){
				String element = s.next(); 
				if (element.equals("node")){
					int id = s.nextInt();
					StringBuffer name = new StringBuffer(s.next()); 
					StringBuffer super_type = new StringBuffer(s.next()); 
					StringBuffer type = new StringBuffer(s.next()); 
					nodes.add(new Node(new Operation(super_type, type, name)));
					//System.out.println((new Node(id,name,type)).toString()); 
				}else {
					if (element.equals("edge")){
						String eID = s.next();
						StringBuffer sourceID = new StringBuffer(s.next());
						StringBuffer targetID = new StringBuffer(s.next()); 
						
						Node sourceNode = this.getNode(sourceID);
						Node targetNode = this.getNode(targetID);
						//System.out.println("target" + targetID.toString() +"source" + sourceID.toString()); 
						if (sourceNode!= null && targetNode!=null)
							this.addEdge(sourceNode, targetNode);
							sourceNode.addSucc(targetNode.data.name); 
							targetNode.addPred(sourceNode.data.name); 
					}
				}
			}
		} catch(Exception e) {
			   	e.printStackTrace();
		}
		s.close();
	}

	public static DirectedGraph readFromJson(String path) throws FileNotFoundException {
		DirectedGraph graph = new DirectedGraph();

        JsonParser jsonParser = new JsonParser();
		JsonObject rootObject = jsonParser.parse(new FileReader(path)).getAsJsonObject();
        JsonObject graphJson = rootObject.getAsJsonObject("graph");
        JsonArray nodesJson = graphJson.getAsJsonArray("nodes");
        JsonArray edgesJson = graphJson.getAsJsonArray("edges");

        // Add nodes to graph
        for (JsonElement element : nodesJson) {
            JsonObject nodeJson = element.getAsJsonObject();
            String name = nodeJson.get("label").getAsString();
            String super_type = nodeJson.get("type").getAsString();
            String type = super_type;
            if (super_type.equalsIgnoreCase("in")) {
                type = nodeJson.getAsJsonObject("metadata").get("subtype").getAsString();
            }

            graph.nodes.add(new Node(
                    new Operation(
                            new StringBuffer(super_type),
                            new StringBuffer(type),
                            new StringBuffer(name)
                    )
            ));
        }

        // Connect nodes with edges
        for (JsonElement element : edgesJson) {
            JsonObject edgeJson = element.getAsJsonObject();
            String source = edgeJson.get("source").getAsString();
            String target = edgeJson.get("target").getAsString();

            Node sourceNode = graph.getNode(new StringBuffer(source));
            Node targetNode = graph.getNode(new StringBuffer(target));
            graph.addEdge(sourceNode, targetNode);
        }

        return graph;
	}
	
	/** Inserts node n in the graph after node loc (between loc and its successors). */
	public void insertNode(Node n, Node loc){
		if ((n!= null) && (loc!=null)) {
			if (nodes.contains(loc)){	
				//delete edges: loc->succ; add edges: n->loc_succ
				if (loc.succs.size()>0){
					ListIterator<StringBuffer> listIt = loc.succs.listIterator(); 
					while (listIt.hasNext()){ 
						Node succ = getNode(listIt.next());
						ListIterator<StringBuffer> lIt = succ.preds.listIterator(); 
						while(lIt.hasNext()){
							StringBuffer p = lIt.next(); 
							if (p.toString().compareTo(loc.data.name.toString()) == 0){
								lIt.remove(); 
								lIt.add(n.data.name); 
							}
						}
						n.addSucc(succ.data.name); 
						edges.add(new Edge(n,succ));
						removeEdge(loc,succ); 
						listIt.remove(); 
					}
				}		
				// add edge loc->n
				edges.add(new Edge(loc, n));
				loc.addSucc(n.data.name); 
				n.addPred(loc.data.name); 
				// add node n
				nodes.add(n);
				
			} else System.out.println("\n ERR: Inserting node not successfull, location not found \n"); 
		}
			else {System.out.println("\n ERR: Adding node not successfull, NULL node \n");
			 System.exit(-1);}; 
	}
	
	/** Adds node n in the graph, with no other nodes connected to it. */
	public void addNode(Node n){
		if ((n!= null) && (!nodes.contains(n))) nodes.add(n); 
			else if (n==null) {System.out.println("\n ERR: Adding node not successfull, NULL node \n");
				System.exit(1);}; 
	}
	
	/** Removes node n from the graph, together with all the the edges that contain it. 
	 * The nodes connected to n are not removed from the graph. */
	public void removeNode(Node n){
		if (n!= null){
			//remove from nodes
			ListIterator<Node> nodesIt = nodes.listIterator();
			boolean finish = false; 
			while(nodesIt.hasNext() && (finish == false)){
				Node crt_n = nodesIt.next(); 
				if (crt_n.getName().toString().compareTo(n.getName().toString())== 0){
					nodesIt.remove(); 
					finish = true; 
				}
			}

			//remove all edges connected to it 
			ListIterator<Edge> listIt = edges.listIterator(); 
			while (listIt.hasNext()){ 
				Edge e = listIt.next(); 
				if (e.src.equals(n)) {
					e.dest.removePred(n.data.name); 
					listIt.remove(); 
				} else if (e.dest.equals(n)){
					e.src.removeSucc(n.data.name); 
					listIt.remove();
				}
			}
		} else System.out.println("\n ERR: Removing node not successfull, NULL node \n"); 
	}
	
	/** The node is extracted from the graph. The predecessors of n are connected to successor of n.*/
	public void extractNode(Node n){
		if (n!= null){
			removeNode(n); 
			// reconnect the edges
			for (int i=0; i<n.preds.size(); i++){
				for (int j=0; j<n.succs.size(); j++){
					Node p = getNode(n.preds.get(i));
					Node s = getNode(n.succs.get(i)); 
					addEdge(p,s); 
				}
			}
		} else System.out.println("\n ERR: Removing node not successfull, NULL node \n"); 
	}

	
	/** Adds edge src -> dest to the graph. Nodes src or dest are also added to the graph, 
	in case they are not already contained by the graph.  */ 
	public void addEdge(Node src, Node dest){
		if ((src!= null) && (dest!= null)){
			edges.add(new Edge(src,dest));
			if (!nodes.contains(src)) nodes.add(src);
			if (!nodes.contains(dest)) nodes.add(dest); 
			src.addSucc(dest.data.name);
			dest.addPred(src.data.name); 
			} else if (src == null ){
						System.out.println("\n ERR: Adding edge not successfull, NULL source node \n"); 
					}else System.out.println("\n ERR: Adding edge not successfull, NULL destination node \n"); 
	}
	
	/** Adds edge e to the graph. The edge e is of type Edge and already contains the source and destination. */ 
	public void addEdge(Edge e){
		if (e!= null) edges.add(e);
			else System.out.println("\n ERR: Adding edge not successfull, NULL edge \n"); 

	}
	
	/** Removes edge src -> dest from the graph. The nodes src, dest are not removed from the graph. */
	public void removeEdge(Node src, Node dest){
		if ((src!= null) && (dest!= null)){
			ListIterator<Edge> listIt = edges.listIterator(); 
			while (listIt.hasNext()){ 
				Edge e = listIt.next(); 
				if (e.src.equals(src) && e.dest.equals(dest)) {
					listIt.remove(); 
				}
			}
		}else if (src == null ){
			System.out.println("\n ERR: Removing edge not successfull, NULL source node \n"); 
		}else System.out.println("\n ERR: Removing edge not successfull, NULL destination node \n"); 
	}
	
	/** Checks if the graph contains the edge source -> dest.
	 *  Returns true if the specified edge exists in the graph and false otherwise.*/
	public boolean isEdge(Node source, Node dest){
		if (source!= null && dest!=null){
			ListIterator<Edge> listIt = edges.listIterator(); 
			while (listIt.hasNext()){ 
				Edge e = listIt.next(); 
				if (e.src.equals(source) && e.dest.equals(dest)) {
					return true; 
				}
			}
		}else if (source == null ){
			System.out.println("\n ERR: Removing edge not successfull, NULL source node \n"); 
		}else System.out.println("\n ERR: Removing edge not successfull, NULL destination node \n"); 

		return false; 
	}

	/** Returns true if the node is not visited and all its predecessors have been visited */
	public boolean isReady(Node v){
		//System.out.println("Check is ready for node " + v.toString()); 
		if (v.data.super_type.toString().compareTo("IN") == 0) return false; 
		if (v.isVisited) return false; 
		if (!v.isVisited){
			for (int i =0; i<v.preds.size(); i++){
				Node p = getNode(v.preds.get(i)); 
				//System.out.println("Pred " + p.getName() + p.isVisited);
				if (!p.isVisited && p.data.super_type.toString().compareTo("IN") != 0) return false; 
			}
		}
		return true; 
	}
	
	/** Returns true if the node is not visited and all its predecessors have been visited 
	 * Should be moved to SynMechanism*/
	public boolean isReady(Node v, double crt_time, Biochip biochip){
		//System.out.println("Check is ready for node " + v.toString()); 
		if (v.data.super_type.toString().compareTo("IN") == 0) return false; 
		if (v.isVisited) return false; 
		double max_t = 0; 
		if (!v.isVisited){
			for (int i =0; i<v.preds.size(); i++){
				Node p = getNode(v.preds.get(i)); 
				//System.out.println("Pred " + p.getName() + p.getType());
				if (!p.isVisited && p.data.super_type.toString().compareTo("IN") != 0) return false; 
				if (!p.isReconfig()){
					if (max_t < biochip.getMaxTime(p.getType().toString())){
						max_t = biochip.getMaxTime(p.getType().toString()); 
					}
				}
			}
		}
		//System.out.println(v.toString() + "max_t = " + max_t); 
		v.data.start_t = crt_time + max_t;
		return true; 
	}
	

	/**Attaches the subgraph recSub to the node n from the graph. A new conjunctive node
	 * is connecting the subgraph to the graph. A conjunctive node is activated when one of the 
	 * inputs has arrived (like an OR node). The CONJ node is inserted in the graph after node n.
	 * The subgraph recSub is connected to the CONJ node.*/
	public void attachSubWithConjNode(DirectedGraph recSub, Node n){
		ArrayDeque<Node> q = new ArrayDeque<Node>(recSub.size());
		StringBuffer name = new StringBuffer("CONJ"+ (this.size()+1)); 
		Node conj_n = new Node(new Operation(new StringBuffer("DUMMY"), new StringBuffer("conj"), name), false); 
		this.insertNode(conj_n,n); 
				
		// enqueue the childless nodes 
		for (int i=0; i<recSub.nodes.size(); i++){
			Node v = recSub.nodes.get(i); 
			if (v.succs.size() == 0){
				String rec_name = "R" + v.data.name.toString() + "_O"+ (this.size()+1); 
				Node rec_n = new Node(v,rec_name); 
				q.offer(rec_n); 
				addEdge(rec_n,conj_n);
			}
		}
		
		while(!q.isEmpty()){
		//for (int x=0; x<14;x++){
			Node rec_n = q.poll(); 
			// add the node to the graph
			addNode(rec_n); 
			if (rec_n.preds.size()==0){} // TODO - figure out if we need this
			// add the preds->v edges
			for (int i=0; i<rec_n.preds.size(); i++){
					//System.out.println("This Node" + recSub.getNode(rec_n.preds.get(i)).data.name);
					Node p = new Node(recSub.getNode(rec_n.preds.get(i))); 
					String rec_name = "R" + p.data.name.toString() + "_O"+ (this.size()+1); 
					Node rec_p = new Node(p,rec_name); 
					rec_n.preds.set(i,rec_p.data.name);
					addEdge(rec_p,rec_n);
					q.offer(rec_p); 
			}
		}	
	}
	
	/**Attaches the subgraph recSub to the node n from the graph. A new dummy node
	 * is connecting the subgraph to the graph.The dummy node is inserted in the graph after node n.
	 * The subgraph recSub is connected to the dummy node.*/
	
	public void attachSub1(DirectedGraph recSub, Node n){	
		ArrayDeque<Node> q = new ArrayDeque<Node>(recSub.size());
		StringBuffer name = new StringBuffer("D"+ (size()+1)); 
		Node dummy = new Node(new Operation(new StringBuffer("DUMMY"), new StringBuffer("dummy"), name), false); 
		insertNode(dummy,n); 
				
		// enqueue the childless nodes 
		for (int i=0; i<recSub.nodes.size(); i++){
			Node v = recSub.nodes.get(i); 
			String rec_name = "R" + v.data.name.toString() + "_O"+ (this.size()+1); 
			v.setName(new StringBuffer(rec_name)); 
			if (v.succs.size() == 0){
				//Node rec_n = new Node(v,rec_name); 
				//q.offer(rec_n); 
				q.offer(v); 
				// connect them to the dummy node 
				addEdge(v,dummy);
			}
		}
	}
	
	public void insertSub(DirectedGraph subG, Node loc){
		Node src_dummy = new Node("DUMMY", "O_" + (this.size()+1));
		this.addNode(src_dummy);
		Node sink_dummy = new Node("DUMMY", "O_" + (this.size()+1));
		this.addNode(sink_dummy);
		
		ArrayDeque<Node> q = new ArrayDeque<Node>(subG.size());
		for (int i=0; i<subG.nodes.size(); i++){
			Node v = subG.nodes.get(i); 
			if (v.succs.size() == 0){
				String rec_name = "R" + v.data.name.toString() + "_O"+ (this.size()+1); 
				Node rec_n = new Node(v,rec_name); 
				q.offer(rec_n); 
				// connect them to the dummy node 
				addEdge(rec_n,sink_dummy);
			}
		}
		//System.out.println("subG to be inserted " + subG.toString() ); 
		for (int i=0; i<subG.size(); i++){
			Node v = subG.nodes.get(i); 
			//System.out.println(v.toString());
			for (int x=0; x<v.preds.size(); x++){
				//System.out.println("Pred " + v.preds.get(x)); 
			}
		}
		while(!q.isEmpty()){
			//for (int x=0; x<14;x++){
				Node rec_n = q.poll(); 
				System.out.println(rec_n); 
				// add the node to the graph
				addNode(rec_n); 
				// add the preds->v edges
				for (int i=0; i<rec_n.preds.size(); i++){
					    System.out.println("pred " + rec_n.preds.get(i)); 
						Node p = new Node(subG.getNode(rec_n.preds.get(i))); 
						String rec_name = "R" + p.data.name.toString() + "_O"+ (this.size()+1); 
						Node rec_p = new Node(p,rec_name); 
						rec_n.preds.set(i,rec_p.data.name);
						rec_p.removeAllSuccs(); 
						//System.out.println("Add edge " + rec_p.toString() + " -> " + rec_n.toString()); 
						addEdge(rec_p,rec_n);
						q.offer(rec_p); 
				}
				if (rec_n.preds.size()==0){
					addEdge(src_dummy,rec_n);
				} // TODO figure out if I need this
				
		}	
		
		for (int i=0; i<loc.succs.size(); i++){
			Node succ = new Node(this.getNode(loc.succs.get(i))); 
			addEdge(sink_dummy, succ); 
			removeEdge(loc,succ); 
		}
		
		loc.removeAllSuccs(); 
		
		addEdge(loc,src_dummy);
		
		

	}
	
	public void attachSub(DirectedGraph recSub, Node n){	
		ArrayDeque<Node> q = new ArrayDeque<Node>(recSub.size());
		StringBuffer name = new StringBuffer("D"+ (size()+1)); 
		Node dummy = new Node(new Operation(new StringBuffer("DUMMY"), new StringBuffer("dummy"), name), false); 
		insertNode(dummy,n); 
				
		// enqueue the childless nodes 
		for (int i=0; i<recSub.nodes.size(); i++){
			Node v = recSub.nodes.get(i); 
			if (v.succs.size() == 0){
				String rec_name = "R" + v.data.name.toString() + "_O"+ (this.size()+1); 
				Node rec_n = new Node(v,rec_name); 
				q.offer(rec_n); 
				// connect them to the dummy node 
				addEdge(rec_n,dummy);
			}
		}
		
		while(!q.isEmpty()){
		//for (int x=0; x<14;x++){
			Node rec_n = q.poll(); 
			// add the node to the graph
			addNode(rec_n); 
			if (rec_n.preds.size()==0){} // TODO figure out if I need this
			// add the preds->v edges
			for (int i=0; i<rec_n.preds.size(); i++){
				    //System.out.println(rec_n.preds.get(i)); 
					Node p = new Node(recSub.getNode(rec_n.preds.get(i))); 
					String rec_name = "R" + p.data.name.toString() + "_O"+ (this.size()+1); 
					Node rec_p = new Node(p,rec_name); 
					rec_n.preds.set(i,rec_p.data.name);
					rec_p.removeAllSuccs(); 
					//System.out.println("Add edge " + rec_p.toString() + " -> " + rec_n.toString()); 
					addEdge(rec_p,rec_n);
					q.offer(rec_p); 
			}
		}	
	}
	
	/** Discards the all the nodes from the graph that are on the paths that stop at stop_n 
	 * (all the predecessors until the root). The node stop_n is replaced in the graph with
	 * node replace_n.*/
	public void prune(Node stop_n, Node replace_n){
		this.prune(stop_n); 
		// replace stop_n  with replace_n
		replace_n.removeAllSuccs(); 
		replace_n.removeAllPreds(); 
		// connect replace_n -> stop_n.succ
		for (int i=0; i<stop_n.succs.size(); i++){
			Node succ = this.getNode(stop_n.succs.get(i)); 
			//System.out.println(stop_n.getName().toString() ); //+ "   "+  succ.getName().toString()); 
			if (succ!=null){
			succ.removePred(stop_n.getName());
			this.addEdge(replace_n,succ); 
			}
		}
	}
	
	/** Discards al the nodes from the graph that are on the paths that stop at stop_n 
	 * (all the predecessors until the root). The */
	public void prune(Node stop_n){	
		//stop_n.removeAllPreds();
		this.removeNode(stop_n); 
		//System.out.println("After removing " + stop_n + this.toString());

		// remove all stop_n preds from the graph
		ArrayDeque<Node> q = new ArrayDeque<Node>(this.size());
		q.offer(stop_n); 
		while(!q.isEmpty()){
			Node v = q.poll(); 
			this.removeNode(v); 
			//System.out.println("After removing " + v + this.toString());
			for (int i=0; i<v.preds.size(); i++){
				//System.out.println(g.toString()); 
				Node p = new Node(this.getNode(v.preds.get(i))); 
				q.offer(p); 
			}
			

		}
		/*for (int i=0; i<stop_n.preds.size(); i++){
			Node pred = this.getNode(stop_n.preds.get(i)); 
			
			this.removeNode(pred); 
			System.out.println("After removing " + pred + this.toString());

		}
*/		
	}
	
	
	/** Returns the Node object that has the identifier #name#. 
	 * If none of the nodes in the graph corresponds, the method returns null. */
	public Node getNode(StringBuffer name){
		ListIterator<Node> listIt = nodes.listIterator(); 
		boolean notFound = true; 
		while (listIt.hasNext() && notFound){ 
			Node n = listIt.next(); 
			if (n.data.name.toString().equals(name.toString())) {
				notFound = false; 
				return n;
			}
		}
		
		return null; 
	}
	
	/** we calculate the CF by doing a reverse BFD traversal of the graph
	 we have not decided on a schedule yet, so we do not know what MIX module from library would be used, therefore 
	 I do not know the time, I take the max MIX time value */
	
	public void calcCriticalityFactor(ModuleLibrary mLib){
		ArrayDeque<Node> q = new ArrayDeque<Node>(this.nodes.size());
		// enqueue the childless nodes 
		for (int i=0; i<this.nodes.size(); i++){
			Node n = this.nodes.get(i); 
			if (n.succs.size() == 0){
				q.offer(n); 
				n.critFactor = mLib.getMaxTime(n.data.type.toString());
			}
		}
		
		while(!q.isEmpty()){
			Node v = q.poll();
			for (int i =0; i<v.preds.size(); i++){
				Node pred = this.getNode(v.preds.get(i)); 
				//System.out.println("Here" + v.toString() + " cf =" + v.critFactor); 
				//System.out.println("pred " + v.preds.get(i).toString()); 
				//System.out.println(pred.toString()+ " pred cf " + pred.critFactor ); 
				double critFactor = v.critFactor + mLib.getMaxTime(pred.data.type.toString()); 
				if (critFactor > pred.critFactor) pred.critFactor = critFactor; 
				q.offer(pred); 
			}
		}
		
		//for (int i=0; i<this.nodes.size(); i++){
			//System.out.println("Node "+ nodes.get(i).data.name + " crtF=" +  nodes.get(i).critFactor); 
		//}	
	}
	
	/** Returns the number of nodes in the graph.*/
	public int size(){
		return nodes.size(); 
	}
	
	/** Prints information about the size, nodes and edges of the graph. */
	public String toString(){
		return " Nodes " + nodes.toString() + "\n Edges " + edges.toString(); 
	}
	
	/** Prints the graph to a .dot file, that can be visualised with GraphViz. */
	public void toDOT(String dotFile) 
	throws FileNotFoundException{
		FileOutputStream log_f = new FileOutputStream(dotFile+".dot"); // declare a file output object
        PrintStream p = new PrintStream(log_f); // declare a print stream object
	    // print the graph into a DOT file (see DOT notation)	
        p.println("digraph G {");
        ListIterator<Edge> listIt = edges.listIterator(); 
		while (listIt.hasNext()){ 
			Edge e = listIt.next(); 
			p.println(e.src.data.name + "_" + e.src.data.type +
					//"_"+ g.getSource(edge).getCost() +
					//"_"+ g.getSource(edge).getStartT() +
					" -> " + e.dest.data.name+ "_" + e.dest.data.type 
					//+ "_"+ g.getDest(edge).getCost()
					//+ "_"+ g.getDest(edge).getStartT()
					);
		}
		// print also the singular nodes (nodes that are not connected to any other node)
		p.println("}");
	}// end graphToDOT
			
}


