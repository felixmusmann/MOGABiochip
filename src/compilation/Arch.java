package compilation;
import java.io.FileReader;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import synthesis.Pair;

public class Arch{ 
	int n_disB; 
	int n_disR; 
	int n_disS; 
	int n_det; 

	Biochip biochip;

	ArrayList<Device> dev; 
	
	public Arch (int width, int height, ArrayList<Pair<Integer,Integer>> coordinates, ArrayList<Device> dev){
		  this.biochip = new Biochip(width,height) ;
		  for (Pair<Integer, Integer> element : coordinates) {
              int x = element.fst;
              int y = element.snd;
              this.biochip.cells.get(y*width + x).isActive = false; 
          }
		  this.dev = dev;
		  biochip.devices = dev;
	}
	
	 public Arch (String json_file) {
		    int width; 
		    int height; 
	        try {
	            final JsonParser parser = new JsonParser();
	            final JsonElement jsonElement = parser.parse(new FileReader(json_file));
	            final JsonObject archObject = jsonElement.getAsJsonObject();

	            width = archObject.get("width").getAsInt(); 
	            height = archObject.get("height").getAsInt(); 
	            this.biochip = new Biochip(width,height) ;
	           
	            JsonArray inactiveElectrodesArray = archObject.get("inactiveElectrodes").getAsJsonArray();
	            for (JsonElement element : inactiveElectrodesArray) {
	                JsonArray electrodeCoordinates = element.getAsJsonArray();
	                int x = electrodeCoordinates.get(0).getAsInt();
	                int y = electrodeCoordinates.get(1).getAsInt();
	                this.biochip.cells.get(y*width + x).isActive = false; 
	            }
	            this.dev = new ArrayList<Device>();
	            JsonArray deviceArray = archObject.get("devices").getAsJsonArray();
	            for (int i = 0; i < deviceArray.size(); i++) {
	                JsonObject deviceObject = deviceArray.get(i).getAsJsonObject();

	                int id = deviceObject.get("id").getAsInt();
	                int x = deviceObject.get("x").getAsInt();
	                int y = deviceObject.get("y").getAsInt();
	                int executionTime = deviceObject.get("executionTime").getAsInt();
	                String type = deviceObject.get("type").getAsString();
	                this.dev.add(new Device (Integer.toString(id), type, x, y, executionTime, 1)); 
	            }
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }
	    }

	public Arch(Arch new_arch){
		this.biochip= new Biochip(new_arch.biochip.width, new_arch.biochip.height);
		this.n_det = new_arch.n_det; 
		this.n_disB = new_arch.n_disB; 
		this.n_disR = new_arch.n_disR; 
		this.n_disS = new_arch.n_disS; 

		this.dev = new ArrayList<Device>();
		for (int i=0; i<new_arch.dev.size(); i++){
			this.dev.add(new Device(new_arch.dev.get(i)));
		}
		// copy the electrodes as well
		this.biochip.cells = new ArrayList<Cell>(new_arch.biochip.cells.size());
		for (int i=0; i<new_arch.biochip.cells.size(); i++){
			this.biochip.cells.add(new Cell(new_arch.biochip.cells.get(i)));
		}
	}

	public Arch(int w, int h, int dB, int dS, int dR, int nDet, ArrayList<Device> new_dev){
		this.n_det = nDet; 
		this.n_disB = dB; 
		this.n_disR = dR; 
		this.n_disS = dS; 

		this.dev = new ArrayList<Device>();
		for (int i=0; i<new_dev.size(); i++){
			this.dev.add(new_dev.get(i));
		}
		// initialiaze the Biochip, rectangular, all cells are active
		biochip = new Biochip(w,h);
	}

	public void removeDev(String dev_type) {
		for (Device dev:this.dev){
			if (dev.type.compareTo(dev_type) == 0){
				this.dev.remove(dev); 
				return; 
			}
		}
	}
	public Biochip toBiochip(){
		Biochip b = new Biochip(this.biochip.width, this.biochip.height);

		// copy the cells
		b.cells = new ArrayList<Cell>(b.height * b.width);
		for (int i=0; i<this.biochip.cells.size(); i++){
			b.cells.add(this.biochip.cells.get(i)); 
		}

		for (int i=0; i<this.dev.size(); i++){
			b.devices.add(this.dev.get(i)); 
		}

		for (int i=0; i<this.n_disB; i++){
			//b.devices.add(new Device("dev1", "disB", 1,1,2));
			// for comparison with TS
			String name = new String("disB" + Integer.toString(i)); 
			b.devices.add(new Device(name, "disB", 1,1,2)); 

		}

		for (int i=0; i<this.n_disR; i++){
			//b.devices.add(new Device("dev1", "disR", 1,1,2)); 
			// for comparison with TS 
			b.devices.add(new Device("dev1", "disR", 1,1,2)); 

		}

		for (int i=0; i<this.n_disS; i++){
			//this.dev.add(new Device("dev1", "disS1", 1,1,7)); 
			//this.dev.add(new Device("dev1", "disS2", 1,1,7)); 
			//this.dev.add(new Device("dev1", "disS3", 1,1,7)); 
			//b.devices.add(new Device("dev1", "disS", 1,1,2)); 
			// for comparison with TS 
			b.devices.add(new Device("dev1", "disS", 1,1,2)); 

		}

		return b; 
	}
	
	public Biochip getBiochip(){
		return this.biochip; 
	}

	public int cost(){
		int cost_electrodes = (this.n_disB + this.n_disR + this.n_disS)*3 + (biochip.width*biochip.height); 
		return this.n_det + cost_electrodes;  
	}
	public String toString(){
		return "\n\tBiochip " + biochip.width + "x" + biochip.height + " disB = " + this.n_disB + " disS = " + this.n_disS + " disR = " + this.n_disR + " opt = " + this.n_det + " \n\t" + this.dev.toString() + "\n";  
	}
}
