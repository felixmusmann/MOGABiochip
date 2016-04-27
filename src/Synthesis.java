public class Synthesis {

    public static void main(String[] args) {
        ArchitectureParser parser = new ArchitectureParser();
        Architecture arch = parser.readArchitecture("data/arch.json");
        System.out.println(arch);
    }
}
