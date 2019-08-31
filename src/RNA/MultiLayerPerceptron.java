package rna;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Our Multi Layer Perceptron implementation using backpropagation algorithm.
 * @author Carlos Ariel
 */
public class MultiLayerPerceptron implements Serializable {
    
    private double learningRate;
    
    private List<Neuron> inputLayer;
    private List<List<Neuron>> hiddenLayer;
    private List<Neuron> outputLayer;

    /**
     * Constructs a MultiLayerPerceptron with the given params
     * @param numberOfInputNeurons The number of neurons in the input layer
     * @param numberOfHiddenNeurons The number of neurons in the hidden layer
     * @param numberOfOutputNeurons The number of neurons in the output layer
     * @param numberOfHiddenLayers The number of hidden layers
     * @param learningRate The learning rate
     */
    public MultiLayerPerceptron(int numberOfInputNeurons, int numberOfHiddenNeurons, int numberOfOutputNeurons, int numberOfHiddenLayers, double learningRate) {
        this.learningRate = learningRate;
        
        // Construct input layer       
        inputLayer = new LinkedList<>();
        for (int i=0; i<numberOfInputNeurons; i++) {
            inputLayer.add( new Neuron(false) );            
        }
        
        // Construct hidden layer
        hiddenLayer = new LinkedList<>();
        for (int i=0; i<numberOfHiddenLayers; i++) {
            List<Neuron> layer = new LinkedList<>();
            for (int j=0; j<numberOfHiddenNeurons; j++) {
                layer.add( new Neuron(true) );
            }
            hiddenLayer.add(layer);
        }
        
        // Construct output layer
        outputLayer = new LinkedList<>();
        for (int i=0; i<numberOfOutputNeurons; i++) {
            outputLayer.add( new Neuron(true) );
        }
        
        /*******************************************************************
         * Connect neurons and initializes the weights with random numbers *
         * *****************************************************************/
        
        // For each input neuron in the input layer, connect with the first
        // corresponding neuron in the first hidden layer
        for (Neuron n : inputLayer) {
            for(Neuron h : hiddenLayer.get(0)){
                n.connectTo(h, randWeight());
            }            
        }
        
        // For each hidden layer, connects every neuron with each other of the next hidden layer
        for (int i=1; i<hiddenLayer.size(); i++) {
            for (Neuron h : hiddenLayer.get(i-1)) {
                for (Neuron hTo : hiddenLayer.get(i)) {
                    h.connectTo(hTo, randWeight());
                }    
            }
        }
        
        // For each neuron in the last hidden layer, connects them with the neurons of the output layer
        for (Neuron h : hiddenLayer.get( hiddenLayer.size()-1 )) {
            for (Neuron o : outputLayer) {
                h.connectTo(o, randWeight());
            }
        }
    }
    
    /**
     * Trains the Multi Layer Perceptron using backpropagation algorithm
     * @param input The input vector
     * @param target The target vector of wich the network is needed to converge
     */
    public void train(double[] input, double[] target) {        
        // Sets the input for neurons in the input layer and propagate the signal
        for (int i=0; i<inputLayer.size(); i++) {            
            inputLayer.get(i).input( input[i] );            
        }
        // Backpropagate the error and adjust the values
        backpropagation(target);        
    }        
    
    /**
     * Backpropagation algorithm
     * @param target The target vector of wich the network is needed to converge
     */
    private void backpropagation(double[] target) {
        //System.out.println();
        //System.out.print("Running backpropagation algorithm...");
        double[] error = new double[ outputLayer.size() ];
        int i=0;
        // Calculate error in output layer
        for (Neuron o : outputLayer) {            
            double output = o.getOutput();
            error[i] = (target[outputLayer.indexOf(o)] - output) * activationFunctionDerivate(output);
            i++;
        }
        // Update weights in connections from final hidden layer to output layer
        for (Neuron h : hiddenLayer.get( hiddenLayer.size()-1 )) {            
            for (Connection c : h.getConnections()) {
                double weight = c.getWeight();
                double weightVariationAdjust = learningRate * error[outputLayer.indexOf(c.getTo())] * h.getOutput();
                c.setWeight( weight + weightVariationAdjust );
            }
        }
        
        double[] outputError = error.clone();
        error = new double[hiddenLayer.get(0).size()];
        
        // Hidden -> hidden
        for (i=hiddenLayer.size()-1; i>0; i--) { // for each hidden layer from last to first (right to left)
            int j=0;            
            for (Neuron h : hiddenLayer.get(i)) { // for each neuron in the current hidden layer                
                double derivate = activationFunctionDerivate(h.getOutput());
                double k = 0;
                for (Connection c : h.getConnections()) { // for each connection of the current neuron                    
                    if (i == hiddenLayer.size()-1) { // if it is the last hidden layer
                        k += outputError[ outputLayer.indexOf(c.getTo()) ] * c.getWeight();
                    } else {
                        k += error[ hiddenLayer.get(i+1).indexOf(c.getTo()) ] * c.getWeight();
                    }
                }
                error[j] = k * derivate;
                j++;
            }
            for (Neuron h : hiddenLayer.get(i-1)) { // for each neuron in the previous hidden layer                
                for (Connection c : h.getConnections()) { // for each connection of the current neuron                    
                    double weight = c.getWeight();
                    double weightVariationAdjust = learningRate * error[hiddenLayer.get(i).indexOf(c.getTo())] * h.getInput();
                    c.setWeight( weight + weightVariationAdjust );
                }
            }
        }
        
        // Input -> hidden
        i=0;
        double[] t = error.clone();
        for (Neuron h : hiddenLayer.get(0)) { // for each neuron in the first hidden layer            
            double derivate = activationFunctionDerivate(h.getOutput());
            double k = 0;
            for (Connection c : h.getConnections()) { // for each connection in the current neuron                
                if (hiddenLayer.size() == 1) {
                    k += c.getWeight() * outputError[outputLayer.indexOf(c.getTo())];
                } else {
                    k += c.getWeight() * error[hiddenLayer.get(1).indexOf(c.getTo())];
                }
            }
            t[i] = k * derivate;
            i++;
        }
        for (Neuron n : inputLayer) { // for each neuron in the input layer            
            for (Connection c : n.getConnections()) { // for each connection of the current neuron                
                double weight = c.getWeight();
                double weightVariationAdjust = learningRate * t[hiddenLayer.get(0).indexOf(c.getTo())] * n.getInput();
                c.setWeight( weight + weightVariationAdjust );
            }
        }
        //System.out.print(" Finished.");
    }
    
    /**
     * Calculates the output corresponding to the given input. (Classify the input)     
     * Recomended: Use this method only once the MLP has been trained.
     * @param input The given input
     * @return The output (classification)
     */
    public double[] calculate(double[] input) {
        
        // Transfer input
        for (int i=0; i<input.length; i++) {
            inputLayer.get(i).input( input[i] );
        }
        
        // Take output
        double[] result = new double[ outputLayer.size() ];
        for (int i=0; i<result.length; i++) {
            result[i] = outputLayer.get(i).getOutput();
        }
        
        return result;
    }
    
    /**
     * Gets a random number for weight initialization
     * @return the random number
     */
    private double randWeight() {
        return Math.random() * (Math.random() > 0.5 ? 1 : -1);
    }
    
    /**
     * Logsig (sigmoidal) activation function.
     * @param n
     * @return 
     */
    public static double activationFunction(double n) {
	return 1.0 / ( 1 + Math.pow(Math.E, -n) );
    }
    
    /**
     * The derivate of logsin (sigmoidal) function: f'(n) = f(n) * (1 - f(n))
     * @param n
     * @return 
     */
    private double activationFunctionDerivate(double n) {
        return n * (1-n);
    }
    
    public void render(Graphics  g) {
    
        final int padding = 50;
        final int globeSize = 100;        
        final Color inputColor = Color.blue;
        final Color hiddenColor = Color.black;
        final Color outputColor = Color.darkGray;
        final Color textColor = Color.white;
        
        int x = padding;
        int y = padding;
        
        g.setColor(inputColor);
        g.fillOval(x, y, globeSize, globeSize);
        g.drawString("Input", x+40, y);
        g.setColor(textColor);
        g.drawString(inputLayer.size()+"", x+padding, y+padding);        
        
        x += globeSize + padding;
        for (int i=0; i<hiddenLayer.size(); i++) {
            g.setColor(hiddenColor);
            g.fillOval(x, y, globeSize, globeSize);
            g.drawString("Hidden", x+40, y);
            g.setColor(textColor);     
            g.drawString(hiddenLayer.get(i).size()+"", x+padding, y+padding);           
            if (hiddenLayer.size() > 1) {
                x += padding*2;
            }
        }
        
        x += globeSize + padding;
        g.setColor(outputColor);
        g.fillOval(x, y, globeSize, globeSize);
        g.drawString("Output", x+40, y);
        g.setColor(textColor);     
        g.drawString(outputLayer.size()+"", x+padding, y+padding);
        
    }
    
    public void oldRender(Graphics g) {       
        //Some draw parameters
        final int padding = 10;
        final int neuronWidth = 10;
        final int adjust = padding/2;
        final Color neuronColor = Color.blue;
        final Color connectionColor = Color.black;
        
        /*********************************
         * Calculate position of neurons *
         *********************************/
        HashMap<Neuron,Point> pos = new HashMap<>();   
        int x = padding;
        int y = padding;                             
        
        // Input layer
        for (int i=0; i<inputLayer.size(); i++) {
            pos.put(inputLayer.get(i), new Point(x,y));
            y += neuronWidth + padding;
        }
        
        // Hidden layers
        x += padding * 4;
        for (int i=0; i<hiddenLayer.size(); i++) {
            y = padding;
            for (int j=0; j<hiddenLayer.get(i).size(); j++) {
                pos.put(hiddenLayer.get(i).get(j), new Point(x,y));
                y += neuronWidth + padding;
            }
            if (hiddenLayer.size() > 1) {
                x += padding*2;
            }
        }
        
        // Output layer
        x += padding * 4;
        y = padding;
        for (int i=0; i<outputLayer.size(); i++) {
            pos.put(outputLayer.get(i), new Point(x,y));            
            y += neuronWidth + padding;
        }
        
        /********************
         * DRAW CONNECTIONS *
         ********************/
        
        // Clear area
        g.setColor(Color.white);
        g.fillRect(0, 0, 1000, 1000);
        g.setColor(connectionColor);
        
        // Input layer
        for (Neuron n : inputLayer) {
            for (Connection c : n.getConnections()) {
                Point from = pos.get(n);
                Point to = pos.get(c.getTo());               
                g.drawLine(from.x + adjust, from.y + adjust, to.x + adjust, to.y + adjust);
            }
        }
        
        // Hidden layers        
        for (int i=0; i<hiddenLayer.size(); i++) {
            for (Neuron n : hiddenLayer.get(i)) {
                for (Connection c : n.getConnections()) {
                    Point from = pos.get(n);
                    Point to = pos.get(c.getTo());                
                    g.drawLine(from.x + adjust, from.y + adjust, to.x + adjust, to.y + adjust);
                }
            }
        }
       
        /****************
         * DRAW NEURONS *
         ****************/
        
        g.setColor(neuronColor);
        
        // Draw neurons
        for (Point point : pos.values()) {
            g.fillOval(point.x, point.y, neuronWidth, neuronWidth);
        }
        
    }        

    @Override
    public String toString() {
        return "MultiLayerPerceptron{\r\n" + 
               "inputLayer=" + inputLayer + ", \r\n" +
               "hiddenLayer=" + hiddenLayer + ", \r\n" +
               "outputLayer=" + outputLayer +
               "\r\n}";
    }   
    
}