package synthesis;

import javafx.util.Pair;
import org.uma.jmetal.solution.DoubleSolution;
import synthesis.model.Biochip;
import synthesis.model.Device;

import java.util.ArrayList;
import java.util.HashMap;

public class BiochipSolution extends Biochip implements DoubleSolution {

    private ArrayList<Double> objectives;
    private HashMap<Object, Object> attributes;

    public BiochipSolution(int width, int height, ArrayList<Pair<Integer, Integer>> inactiveElectrodes, ArrayList<Device> devices) {
        super(width, height, inactiveElectrodes, devices);
        objectives = new ArrayList<>(getNumberOfObjectives());
        attributes = new HashMap<>();
    }

    public BiochipSolution(Biochip other) {
        super(other);
        objectives = new ArrayList<>(getNumberOfObjectives());
        attributes = new HashMap<>();
    }

    public BiochipSolution(BiochipSolution other) {
        super(other);
        objectives = new ArrayList<>(getNumberOfObjectives());
        objectives.addAll(other.objectives);
        attributes = new HashMap<>();
    }

    @Override
    public Double getLowerBound(int i) {
        return null;
    }

    @Override
    public Double getUpperBound(int i) {
        return null;
    }

    @Override
    public void setObjective(int i, double v) {
        if (objectives.size() > i && objectives.get(i) != null) {
            objectives.set(i, v);
        } else {
            objectives.add(i, v);
        }
    }

    @Override
    public double getObjective(int i) {
        return objectives.get(i);
    }

    @Override
    public Double getVariableValue(int i) {
        return null;
    }

    @Override
    public void setVariableValue(int i, Double aDouble) {
        throw new UnsupportedOperationException("Variables can not be set.");
    }

    @Override
    public String getVariableValueString(int i) {
        return null;
    }

    @Override
    public int getNumberOfVariables() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return 2;
    }

    @Override
    public BiochipSolution copy() {
        return new BiochipSolution(this);
    }

    @Override
    public void setAttribute(Object id, Object value) {
        attributes.put(id, value);
    }

    @Override
    public Object getAttribute(Object id) {
        return attributes.get(id);
    }

    @Override
    public String toString() {
        String out = super.toString();
        out += "Cost\t\t" + getObjective(0) + "\n";
        out += "Exec. time\t" + getObjective(1) + "\n";
        return out;
    }
}
