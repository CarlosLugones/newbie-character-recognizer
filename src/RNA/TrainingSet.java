package rna;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A training set is a set of inputs with the corresponding outputs.
 * @author Carlos Ariel
 */
public class TrainingSet implements Serializable {
    
    private List<double[]> inputs;
    private List<double[]> outputs;
    private int numberOfInputs;
    private int numberOfOutputs;

    public TrainingSet(int numberOfInputs, int numberOfOutputs) {
        inputs = new LinkedList<>();
        outputs = new LinkedList<>();
        this.numberOfOutputs = numberOfOutputs;
        this.numberOfInputs = numberOfInputs;
    }
    
    /**
     * The size of the training set
     * @return 
     */
    public int size() {
        return inputs.size();
    }

    public void add(int[] input, int[] output) {
        inputs.add( transformToArrayDouble(input) );
        outputs.add( transformToArrayDouble(output) );        
    }
    
    public static double[] transformToArrayDouble(int[] x) {
        double[] aux = new double[x.length];
        for (int i=0; i<x.length; i++) {
            aux[i] = x[i];
        }
        return aux;
    }

    public int getNumberOfInputs() {
        return numberOfInputs;
    }

    public int getNumberOfOutputs() {
        return numberOfOutputs;
    }

    public List<double[]> getInputs() {
        return inputs;
    }

    public List<double[]> getOutputs() {
        return outputs;
    }        
    
    public void clear() {
        inputs.clear();
        outputs.clear();
    }
    
    public String[] names() {
        int total = numberOfInputs + numberOfOutputs;
        String[] aux = new String[ total ];
        int outputNumber = 0;
        for (int i=0; i<total; i++) {
            int j = i+1;
            aux[i] = "x" + j;
            if (i > numberOfInputs-1) {
                outputNumber++;
                aux[i] = "y" + outputNumber;
            }
        }
        return aux;
    }

    @Override
    public String toString() {
        String aux = "";
        String[] n = names();
        for (int i=0; i<inputs.size(); i++) {
            int j=0;
            for (j=0; j<inputs.get(i).length; j++) {
                aux += n[j] + "=" + inputs.get(i)[j] + ", ";
            }
            for (int q=0; q<outputs.get(i).length; q++) {
                aux += n[j] + "=" + outputs.get(i)[q] + ", ";
                j++;
            }
            aux += "\r\n";
        }
        return aux;
    }        
    
}
