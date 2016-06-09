package synthesis;

import com.google.gson.JsonObject;
import compilation.DirectedGraph;
import synthesis.model.Biochip;
import synthesis.model.Device;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Synthesis {

    private static final boolean DEMO_MODE = false;

    public static void main(String[] args) {

//        Biochip arch = JSONParser.readBiochip("data/simpleArch.json");
//        HashMap<Integer, Device> library = parser.readDeviceLibrary("data/devices.json");
//
//        System.out.println("## Original architecture\n" + arch);
//
//        Biochip[] chips = arch.splitAtRow(2);
//        System.out.println(chips[0]);
//        System.out.println(chips[1]);
//
//        System.out.println("####");
//        System.out.println(Biochip.mergeVertical(chips[0], chips[1], 0, 9));
//        System.out.println("####");
//
//        System.out.println("####");
//        System.out.println(Biochip.mergeHorizontal(chips[0], chips[1], 0, 3));
//        System.out.println("####");

//        try {
//            FileWriter writer = new FileWriter("input/graph10.txt.json");
//            JsonObject graph = JSONParser.convertGraph("input/graph10.txt");
//            writer.write(graph.toString());
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void waitForEnter() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
