/* @@ JETC journal - Architecture Synthesis using placement of Circular-route modules*/
package compilation;

public class Cell {

	boolean isFree = true; 
	boolean isActive = true;
	boolean isFaulty = false; 
	double value = -1; 
	int x = -1; 
	int y = -1; 

	public Cell(){ this.isFree = true; this.isActive = true;
	this.isFaulty = false; 
	this.value = -1;}

	public Cell(boolean isActive){ this.isFree = isActive;}

	public Cell(Cell old_c){
		this.isActive = old_c.isActive; 
		this.isFree = old_c.isFree; 
		this.value = old_c.value; 
		this.isFaulty = old_c.isFaulty; 
		this.x = old_c.x; 
		this.y = old_c.y; 
	}

	public void setFree(){
		this.isFree = true;
	}

	public void setBusy(){
		this.isFree = false; 
	}

	public void setInactive(){
		this.isActive = false; 
	}

	public void setCoordinates(int x, int y){
		this.x = x; 
		this.y = y; 
	}

	public boolean isUsable(){
		if (this.isActive && this.isFree)
			return true;
		return false; 
	}

	public String toString(){
		if (!this.isActive) return "X"; 
		if (this.isFree) return "0"; 
		return  "1";
	}

	public boolean equals(Cell c){
		if(this.x == c.x && this.y == c.y) return true; 
		return false; 
	}

	public boolean isContained(CRMModule CRM){
		for (int a = 0; a<CRM.cells.size(); a++){
			if (this.equals(CRM.cells.get(a)))
				return true; 
		}
		return false; 
	}
}

