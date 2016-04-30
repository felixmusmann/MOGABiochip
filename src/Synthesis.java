import java.util.ArrayList;

public class Synthesis {

    public static void main(String[] args) {
        ArchitectureParser parser = new ArchitectureParser();
        Architecture arch = parser.readArchitecture("data/arch.json");

        ArrayList<Architecture> offspring = new ArrayList<>();
        offspring.add(arch.generateNeighbor());

        for (int i = 0; i < 50; i = offspring.size()-1) {
            Architecture neighbor = offspring.get(i).generateNeighbor();
            if (neighbor.getWidth() > 0 && neighbor.getHeight() > 0) {
               offspring.add(neighbor);
            } else {
                System.out.println("Generated fruitless offspring.");
            }
        }

        System.out.println(arch);
        System.out.println(offspring.get(offspring.size()-1));
    }
}
