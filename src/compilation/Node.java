/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

import java.util.ArrayList;
import java.util.ListIterator;

public class Node{
	
	Operation data = new Operation(); 
	Rectangle module = new Rectangle(); 
	CRMModule CRM = new CRMModule(); 
	boolean isVisited = false; 
	double err = 0; 
	double critFactor = 0; // the criticality factor - how important is the node
	ArrayList<StringBuffer> succs = new ArrayList<StringBuffer>(); // stores the ids of the successor nodes
	ArrayList<StringBuffer> preds = new ArrayList<StringBuffer>(); // stores the IDs of the predecessors nodes 
	
	public Node(){}
	
	public Node(Operation data, boolean isVisited){
		this.data = new Operation(data.type, data.name); 
		this.isVisited = isVisited; 
	}
	
	public Node(Operation data){
		this.data = new Operation(data.super_type, data.type, data.name); 
		this.isVisited = false; 
	}
	
	public Node(String type, String name){
		this.data = new Operation(new StringBuffer(type), new StringBuffer(name)); 
		this.isVisited = false;
	}
	
	public Node(StringBuffer type, StringBuffer name){
		this.data = new Operation(type,name); 
		this.isVisited = false;
	}
	
	public Node(Node n){
		this.data = new Operation(new StringBuffer(n.data.type), new StringBuffer(n.data.name)); 
		this.isVisited = false;	
		this.data.store = n.data.store;
		for (int i=0; i<n.preds.size(); i++)
			this.preds.add(n.preds.get(i)); 
		for (int i=0; i<n.succs.size(); i++)
			this.succs.add(n.succs.get(i)); 
		this.data.start_t = n.data.start_t; 
		this.data.stop_t = n.data.stop_t; 
		this.module = new Rectangle(n.module.width,n.module.height,n.module.x_bl, n.module.y_bl); 
	}
	
	public Node(Node n, String newName){
		this.data = new Operation(new StringBuffer(n.data.type), new StringBuffer(newName)); 
		this.isVisited = false;	
		this.data.store = n.data.store;
		for (int i=0; i<n.preds.size(); i++)
			this.preds.add(n.preds.get(i)); 
		for (int i=0; i<n.succs.size(); i++)
			this.succs.add(n.succs.get(i)); 
	
	}
	
	public void setData(Operation op){
		data = new Operation(op.super_type, op.type, op.name, op.start_t, op.stop_t); 
	}
	
	public void setSuccs(ArrayList<StringBuffer> succsNew){
		for (int i=0; i<succsNew.size(); i++){
			this.succs.add(new StringBuffer(succsNew.get(i)));
		}
	};
	
	public void setPreds(ArrayList<StringBuffer> predsNew){
		for (int i=0; i<predsNew.size(); i++){
			this.preds.add(new StringBuffer(predsNew.get(i)));
		}
	};
	
	public void removePred(StringBuffer predName){
		if (this.preds.size()>0){
			ListIterator<StringBuffer> lIt = this.preds.listIterator(); 
			while(lIt.hasNext()){
				StringBuffer p = lIt.next(); 
				if (p.toString().compareTo(predName.toString()) == 0){
					lIt.remove(); 
				}
			}
		}
	}
	
	public void removeSucc(StringBuffer succName){
		if (this.succs.size()>0){
			ListIterator<StringBuffer> lIt = this.succs.listIterator(); 
			while(lIt.hasNext()){
				StringBuffer s = lIt.next(); 
				if (s.toString().compareTo(succName.toString()) == 0){
					lIt.remove(); 
				}
			}
		}
	}
	
	public void removeAllSuccs(){
		ListIterator<StringBuffer> lIt = this.succs.listIterator(); 
		while(lIt.hasNext()){
			StringBuffer s = lIt.next(); 
			lIt.remove();
		}

	}
	
	public void removeAllPreds(){
		ListIterator<StringBuffer> lIt = this.preds.listIterator(); 
		while(lIt.hasNext()){
			StringBuffer p = lIt.next(); 
			lIt.remove();
		}

	}
	
   public boolean hasSuccConjNode(){
		for (int i=0; i<this.succs.size(); i++){
			//System.out.println(this.succs.get(i));
			if (this.succs.get(i).length()>5){
				String succ_type = this.succs.get(i).substring(0,4); 
				if (succ_type.compareTo("CONJ")==0) return true;
			}
		}
		return false; 
	}
	

	
	public void addSucc(StringBuffer succName){
		if(!isSucc(succName))
			this.succs.add(succName); 
	}
	
	public void addPred(StringBuffer predName){
		if(!isPred(predName))
			this.preds.add(predName); 
	}
	
	public boolean isPred(StringBuffer predName){
		for(int i=0; i<this.preds.size(); i++){
			if (this.preds.get(i).toString().compareTo(predName.toString()) ==0)
				return true; 
		}
		return false; 
	}
	
	public boolean isSucc(StringBuffer succName){
		for(int i=0; i<this.succs.size(); i++){
			if (this.succs.get(i).toString().compareTo(succName.toString()) ==0)
				return true; 
		}
		return false; 
	}
	
	public boolean isReconfig(){
		return this.data.isReconfigurable();
	}
	
	public Operation getOp(){
		return this.data;
	}
	public StringBuffer getName(){
		return this.data.name; 
	}
	
	public StringBuffer getType(){
		return this.data.type; 
	}
	
	public double getExeTime(){
		return this.data.stop_t - this.data.start_t; 
	}
	
	public void setName(StringBuffer new_name){
		this.data.name = new_name; 
	} 
	
	public void setType(StringBuffer new_type){
		this.data.type = new_type; 
	}
	
	public boolean equals(Node n){
		if (n.data.equals(this.data)) return true; 
		return false; 
	}
	
	public String toString(){
		if (this.getType().toString().compareTo("store") == 0) return (this.getName() + " " + this.module.toString());
		if (data != null) return data.toString(); 
		return "null"; 
	}
}

