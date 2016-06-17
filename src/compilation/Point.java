package compilation;

/**
 * Created by Felix on 30/05/16.
 */
class Point{
	public double x;
	public double y;

	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}

	public boolean equals(Point q){
		if(this.x == q.x && this.y == q.y)
			return true;
		return false;
	}

	public String toString(){
		return "("+this.x+"," + this.y + ") ";
	}
}
