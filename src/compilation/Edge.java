/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

/**This class implements and edge object for a directed graph.
 * Each edge is directed, src->dest, therefore is has a source node @src and a destination node @dest. 
 * The edge dest->src is a different edge than src->dest. */

public class Edge{
	
	Node  src; // source  
	Node  dest; // destination
	
	/**Constructors */
	public Edge(){
		src = new Node(); 
		dest = new Node(); 
	}
	
	public Edge(Node s, Node d){ 
		src = new Node(s.data, s.isVisited);
		dest = new Node(d.data, d.isVisited); 
	}
	
	/** This method sets the current source of the edge to the new value @newSrc given as parameter */
	public void setSource(Node newSrc){
		this.src = new Node(newSrc.data, newSrc.isVisited);
	}
	/** This method sets the current destination of the edge to the new value @newDest given as parameter */
	public void setDest(Node newDest){
		this.dest = new Node(newDest.data, newDest.isVisited); 
	}
	
	/**Method that returns the source node of the current edge. The return value is a @Node object.*/
	public Node getSource(){
		Node op = new Node (src.data, src.isVisited); 
		op.data.start_t = src.data.start_t; 
		op.data.stop_t = src.data.stop_t; 
		return op; 
	}
	
	/**Method that returns TRUE is the edge contains the Node @n given as parameter.
	 * In order to be contained by the current edge, the Node @n has to be either the source, or the dest.
	 * If @n is not contained by the edge, than FALSE is returned.*/
	public boolean containsNode(Node n){
		if (src.equals(n)) return true; 
		if (dest.equals(n)) return true; 
		return false; 
	}
	
	/**Overrides the Object.equals method. Returns TRUE if two edges are equals, and FALSE otherwise.
	 * For two edges to be equal they must have the same source nodes and the same destination nodes.*/
	public boolean equals(Edge e){
		if (src.equals(e.src) && dest.equals(e.dest))
			return true; 
		return false; 
	}
	
	/**Prints the propertied of an edge to a String object.*/
	public String toString(){
		return  src.data.name + "->" + dest.data.name; 
	}
	
}
