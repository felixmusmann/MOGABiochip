package compilation;

public class Device extends Module {

		String name; // type_index ex: opt_1, opt_2
		String type; // general type of device ex: opt, sense, etc
		int no_total = 0; // total number of devices on the biochip TODO can be removed?
		int x_bl, y_bl; // the placement coordinates for bottom left corner
		double stop_t; // the stop time of the last usage, this is the time slot when it becomes free
		
		public Device(String name, int w, int h, int t, int no_total){
			super(w,h,t); 
			this.name = new String(name);
			this.type = new String(name);
			this.no_total = no_total;
			this.x_bl = this.y_bl = -1;
			this.stop_t = 0 - t; 
		}
		
		public Device(Device old_dev){
			super(old_dev.width, old_dev.height, old_dev.time); 
			this.name = old_dev.name; 
			this.type = old_dev.type; 
			this.no_total = old_dev.no_total; 
			this.x_bl = old_dev.x_bl;
			this.y_bl = old_dev.y_bl; 
			this.stop_t = old_dev.stop_t; 
		}
		
		public Device(String name, String type, int w, int h, int t, int no_total){
			super(w,h,t); 
			this.name = new String(name);
			this.type = type; 
			this.no_total = no_total;
			this.x_bl = this.y_bl = -1; 
			this.stop_t = 0 - t; 
		}
		
		public Device(String name, String type, int w, int h, int t){
			super(w,h,t); 
			this.name = new String(name);
			this.type = type; 
			this.no_total = 0;
			this.x_bl = this.y_bl = -1; 
			this.stop_t = 0 - t; 
		}

		
		public Device(String name, String type, int w, int h, int t, int x_bl, int y_bl){
			super(w,h,t); 
			this.name = new String(name);
			this.type = type; 
			this.x_bl = this.y_bl = -1; 
			this.stop_t = 0 - t; 
		}

		
		public Device(Node n, int time){
			super(n.module.width-2, n.module.height-2, time); 
			this.name = new String(n.module.name);
			this.type = new String(n.getType());
			this.x_bl = n.module.x_bl; 
			this.y_bl = n.module.y_bl; 
		}
		
		/** Returns true if the devices has already been placed.*/
		public boolean isPlaced(){
			if (this.x_bl != -1 && this.y_bl != -1)
				return true; 
			return false; 
		}
		
		public boolean equals(Object o){
			Device d = (Device) o; 
			if (d.name.toUpperCase().compareTo(this.name.toUpperCase()) == 0
					&& d.type.toUpperCase().compareTo(this.type.toUpperCase()) == 0
					&& d.x_bl == this.x_bl && d.y_bl == this.y_bl)
				return true; 
			return false; 
		}
		
		public String toString(){
			//
			return  String.format(this.type +  " (" + x_bl + "," + y_bl + ") %.2f", stop_t);  
			//return this.type + " " +  width + "x" + height + " " + time + "s" + " stop_t=" + stop_t ;  
			//return this.type + " " + time + "s" + " (" + x_bl + "," + y_bl + ")";// " stop_t=" + stop_t ;  
		}		
}
