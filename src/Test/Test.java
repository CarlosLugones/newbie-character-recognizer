package test;

import java.util.Random;
import rna.MultiLayerPerceptron;

/**
 * Test class
 * @author Carlos Ariel
 */
public class Test {
    
    /**
     * XOR example, demonstrating that our implementation of MLP works :-)
     */
    public static void xorTest() {
        MultiLayerPerceptron mlp = new MultiLayerPerceptron(2, 5, 1, 1, 1.0);        
        double[][] xor = new double[4][2];
	xor[0][0] = 0;
	xor[0][1] = 0;
	xor[1][0] = 1;
	xor[1][1] = 0;
	xor[2][0] = 0;
	xor[2][1] = 1;
	xor[3][0] = 1;
	xor[3][1] = 1;
	Random r = new Random();
        for(int i = 0; i <= 100000; i++){
            double[] input = xor[r.nextInt(4)];
            double[] target = new double[]{((int)input[0]+(int)input[1])%2};
            mlp.train(input, target);
	}
	for(int i = 0; i < 4; i++){
            System.out.println("Classifying "+xor[i][0]+","+xor[i][1]+". Output: "+mlp.calculate(xor[i])[0]);
	}
    }
    
    public static void main(String[] args) {
        xorTest();
    }
    
}
