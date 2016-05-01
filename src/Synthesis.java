import java.io.IOException;
import java.util.ArrayList;

public class Synthesis {

    public static void main(String[] args) {
        ArchitectureParser parser = new ArchitectureParser();
        Architecture arch = parser.readArchitecture("data/arch.json");

        ArrayList<Architecture> offspring = new ArrayList<>();
        offspring.add(arch.generateNeighbor());

        for (int i = 0; i < 5000; i = offspring.size()-1) {
            Architecture neighbor = offspring.get(i).generateNeighbor();
            offspring.add(neighbor);
        }

        try {
            parser.saveArchitecture(offspring.get(offspring.size()-1), "offspring.json");
            System.out.println(offspring.get(offspring.size()-1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
