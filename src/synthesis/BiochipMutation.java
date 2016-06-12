package synthesis;

import org.uma.jmetal.operator.MutationOperator;
import synthesis.model.Biochip;
import synthesis.model.DeviceLibrary;
import synthesis.model.Electrode;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class BiochipMutation implements MutationOperator<BiochipSolution> {

    private Random rnd;
    private double mutationRate;
    private Set<String> requiredDeviceTypes;
    private DeviceLibrary deviceLibrary;

    public enum Type {
        ADD_ELECTRODE, REMOVE_ELECTRODE, ADD_COLUMN, REMOVE_COLUMN, ADD_ROW, REMOVE_ROW; // TODO: ADD_DEVICE;

        private static final Type[] VALUES = values();
        private static final int SIZE = VALUES.length;
        private static final Random RANDOM = new Random();

        public static Type getRandom()  {
            float weight = RANDOM.nextFloat();
            if (weight < 0.7) {
                return REMOVE_ELECTRODE;
            } else {
                return VALUES[RANDOM.nextInt(SIZE)];
            }
        }
    }

    public BiochipMutation(double mutationRate, DeviceLibrary deviceLibrary, Set<String> requiredDeviceTypes) {
        this.rnd = new Random();
        this.mutationRate = mutationRate;
        this.requiredDeviceTypes = requiredDeviceTypes;
        this.deviceLibrary = deviceLibrary;
    }

    @Override
    public BiochipSolution execute(BiochipSolution biochipSolution) {
        if (rnd.nextDouble() < mutationRate) {
            mutate(biochipSolution);
        }

        return biochipSolution;
    }

    /**
     * This method mutates the biochip with a random mutation.
     * Possible mutations are adding or removing of a single electrode,
     * row or column.
     *
     * @return mutated biochip
     */
    private Biochip mutate(Biochip biochip) {
        Type mutation = Type.getRandom();

        switch (mutation) {
            case ADD_ELECTRODE:
                if (biochip.getFreeCells().size() < 1) {
                    return mutate(biochip);
                }
                break;
            case REMOVE_ELECTRODE: {
                if (biochip.getElectrodes().size() < 2) {
                    return mutate(biochip);
                }
                break;
            }
            case REMOVE_COLUMN: {
                if (biochip.getWidth() < 2) {
                    return mutate(biochip);
                }
                break;
            }
            case REMOVE_ROW: {
                if (biochip.getHeight() < 2) {
                    return mutate(biochip);
                }
                break;
            }
        }

        return mutate(biochip, mutation);
    }

    /**
     * This method mutates the biochip with a specific mutation.
     *
     * @param mutation  mutation that will be applied to biochip
     * @return biochip with specific mutation
     */
    private Biochip mutate(Biochip biochip, Type mutation) {
        Random random = new Random();

        switch (mutation) {
            case ADD_ELECTRODE: {
                ArrayList<Pair<Integer, Integer>> freeCells = biochip.getFreeCells();
                int i = random.nextInt(freeCells.size());
                Pair<Integer, Integer> coordinates = freeCells.get(i);
                biochip.addElectrode(coordinates.fst, coordinates.snd);
                break;
            }
            case REMOVE_ELECTRODE: {
                int i = random.nextInt(biochip.getElectrodes().size());
                Electrode electrode = biochip.getElectrodes().get(i);
                biochip.removeElectrode(electrode.getX(), electrode.getY());
                break;
            }
            case ADD_COLUMN: {
                int x = random.nextInt(biochip.getWidth());
                biochip.addColumnOfElectrodes(x);
                break;
            }
            case REMOVE_COLUMN: {
                int x = random.nextInt(biochip.getWidth());
                biochip.removeColumnOfElectrodes(x);
                break;
            }
            case ADD_ROW: {
                int y = random.nextInt(biochip.getHeight());
                biochip.addRowOfElectrodes(y);
                break;
            }
            case REMOVE_ROW: {
                int y = random.nextInt(biochip.getHeight());
                biochip.removeRowOfElectrodes(y);
                break;
            }
        }

        return biochip;
    }
}
