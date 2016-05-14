import java.io.IOException;

public class Synthesis {

    public static void main(String[] args) {
        ArchitectureParser parser = new ArchitectureParser();
        Architecture arch = parser.readArchitecture("data/arch.json");

        System.out.println("## Original architecture\n" + arch);
        waitForEnter();

        System.out.println("## Split architectures at row 3");
        Architecture[] splitArchsV = arch.splitAtRow(3);
        System.out.println("# Upper part\n" + splitArchsV[0]);
        System.out.println("# Lower part\n" + splitArchsV[1]);
        waitForEnter();

        System.out.println("## Split architectures at column 5");
        Architecture[] splitArchsH = arch.splitAtColumn(5);
        System.out.println("# Left-hand part\n" + splitArchsH[0]);
        System.out.println("# Right-hand part\n" + splitArchsH[1]);
        waitForEnter();

        System.out.println("# Merge vertically\n" + Architecture.mergeVertical(splitArchsH[0], splitArchsV[1], 3, Alignment.LEFT));
        waitForEnter();

        System.out.println("# Merge horizontally\n" + Architecture.mergeHorizontal(splitArchsV[1], splitArchsH[0], 0, Alignment.CENTER));

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < 1000; i++) {
//            arch = arch.generateNeighbor();
//            System.out.println(arch);
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            parser.saveArchitecture(arch, "offspring.json");
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
