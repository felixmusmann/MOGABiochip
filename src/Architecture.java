import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;

public class Architecture {

    private int width;
    private int height;

    private ArrayList<ArrayList<Electrode>> electrodes;
    private ArrayList<Device> devices;

    public Architecture(int width, int height, ArrayList<Pair<Integer, Integer>> inactiveElectrodes, ArrayList<Device> devices) {
        this.width = width;
        this.height = height;
        this.devices = devices;
        this.electrodes = new ArrayList<>(width);

        for (int x = 0; x < this.width; x++) {
            this.electrodes.add(x, new ArrayList<Electrode>(height));
            for (int y = 0; y < this.height; y++) {
                this.electrodes.get(x).add(y, new Electrode(x, y));
            }
        }

        for (Pair<Integer, Integer> inactiveElectrode : inactiveElectrodes) {
            int x = inactiveElectrode.fst;
            int y = inactiveElectrode.snd;
            electrodes.get(x).get(y).setActive(false);
        }
    }

    @Override
    public String toString() {
        String output = "";

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (this.electrodes.get(x).get(y).isActive())
                    output += "X ";
                else
                    output += "  ";
            }
            output += "\n";
        }

        return output;
    }
}
