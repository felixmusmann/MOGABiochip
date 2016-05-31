/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

public class Operation{
	StringBuffer super_type; // a general operation like: IN, MIX
	StringBuffer type;// a specific device that the operation has to be performed on like: disB - the buffer dispenser
	StringBuffer name; // id
	double start_t = 0; 
	double stop_t = 0;
	boolean store = false; 
	
	public Operation(){
		this.super_type = new StringBuffer(); 
		this.type = new StringBuffer(); 
		this.name = new StringBuffer(); 
		this.store = false; 
	}
	
	public Operation(StringBuffer type, StringBuffer name){
		this.super_type = new StringBuffer(type); 
		this.type = new StringBuffer(type); 
		this.name = new StringBuffer(name); 
		this.start_t = stop_t = 0; 
		this.store = false; 
	}
	
	
	public Operation(StringBuffer super_type, StringBuffer type, StringBuffer name){
		this.super_type = new StringBuffer(super_type); 
		this.type = new StringBuffer(type); 
		this.name = new StringBuffer(name); 
		this.start_t = stop_t = 0; 
		this.store = false; 
	}
	
	public Operation(StringBuffer super_type, StringBuffer type, StringBuffer name, double start_t, double stop_t){
		this.super_type = super_type; 
		this.type.append(type); 
		this.name.append(name); 
		this.start_t = start_t; 
		this.stop_t = stop_t; 
		this.store = false; 
	}
	
	public boolean equals(Operation o){
		if ((o.name.lastIndexOf(this.name.toString()) == 0) && (o.name.length() == this.name.length())) {
				return true; 
		}
		return false; 
	}
	
	// TODO: SENSE how do we treat it 
	public boolean isReconfigurable(){
		if ((this.super_type.toString().compareTo("IN") == 0)
				|| (this.super_type.toString().compareTo("OPT") == 0)
				|| (this.super_type.toString().compareTo("SENSE") == 0)
			)
		return false;
		
		return true; 
	}
	public String getName(){
		return name.toString(); 
	}
	
	public String getType(){
		return type.toString(); 
	}
	
	public String toString(){ 
		return name + " " + type + " " + start_t + "-" + stop_t; 
	}
}