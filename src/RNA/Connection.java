package rna;

import java.io.Serializable;

/**
 * A connection from a neuron to another.
 * Wrappes the destination neuron (to) and the weight of the connection.
 * @author Carlos Ariel
 */
public class Connection implements Serializable {
    
    private Neuron to;
    private double weight;

    public Connection(Neuron to, double weight) {
        this.to = to;
        this.weight = weight;
    }

    public Neuron getTo() {
        return to;
    }

    public void setTo(Neuron to) {
        this.to = to;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }        
    
}
