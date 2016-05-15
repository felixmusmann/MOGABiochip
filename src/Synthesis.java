import java.io.IOException;

public class Synthesis {

    public static void main(String[] args) {
        ArchitectureParser parser = new ArchitectureParser();
        Biochip arch = parser.readBiochip("data/simpleArch.json");

        System.out.println("## Original architecture\n" + arch);
        //waitForEnter();

        Biochip[] chips = arch.splitAtRow(2);
        System.out.println(chips[0]);
        System.out.println(chips[1]);

        System.out.println("####");
        System.out.println(Biochip.mergeVertical(chips[0], chips[1], 0, 9));
        System.out.println("####");

        System.out.println("####");
        System.out.println(Biochip.mergeHorizontal(chips[0], chips[1], 0, 3));
        System.out.println("####");

        try {
            parser.saveBiochip(chips[1], "data/test.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
//            parser.saveBiochip(arch, "offspring.json");
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
