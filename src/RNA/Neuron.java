package rna;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Neuron knows how many inputs has to receive and what neuron(s) are next to it.
 * Neuron can't access to the previous neurons, only to the next.
 * @author Carlos Ariel */
public class Neuron implements Serializable {
    
    private boolean activated;
    
    // Input stuff
    private double realInput   = 0; // the real input receibed in each time
    private double totalInput  = 0; // the final input when all sources has contributed (sum of inputs)
    private int inputTriggered = 0; // the count of times that the neuron has received an input   
    private int inputAmount    = 0; // the amout of inputs that the neuron has to receive in order to propagate signal    
    
    // Output stuff
    private double output;    
    
    private List<Connection> connections;       

    public Neuron(boolean activated) {
        this.activated = activated;
        this.connections = new LinkedList<>();
    }
    
    /**
     * Sets the input from an specific source.
     * This makes a sumatory input to input.
     * The neuron activates when all the inputs has completed.
     * @param input
     */
    public void input(double input) {
        inputTriggered++;
        totalInput += input;
        if (inputTriggered >= inputAmount ) {
            realInput = totalInput;
            propagateSignal();
        }        
    }        
    
    public void propagateSignal() {
        
        for (Connection c : connections) {
            if (activated) {
                c.getTo().input( MultiLayerPerceptron.activationFunction(totalInput) * c.getWeight() );
            } else {
                c.getTo().input( totalInput * c.getWeight() );
            }
        }
        
        if (activated) {
            output = MultiLayerPerceptron.activationFunction(totalInput);
        } else {
            output = totalInput;
        }
        
        // Reset neuron
        totalInput = 0;
        inputTriggered = 0;
        
    }
    
    public void connectTo(Neuron neuron, double weight) {
        Connection c = new Connection(neuron, weight);
        connections.add(c);
        neuron.notificate();
    }
    
    public void notificate() {
        inputAmount++;
    }
    
    public double getInput() {
        return realInput;
    }

    public double getOutput() {
        return output;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    @Override
    public String toString() {
        return totalInput + "";
    }   
    
}
