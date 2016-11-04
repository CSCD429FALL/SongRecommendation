import java.util.ArrayList;

public class Neuron {
	
	private ArrayList<Connection> outputWeights;
	private double outputVal;
	private int index;
	private double gradient;
	private static double eta = 0.15;
	private static double alpha = 0.5;
	
	public Neuron(int numOutputs, int index){
		
		outputWeights = new ArrayList<Connection>();
		this.index = index;
		
		for(int c = 0; c < numOutputs; ++c){
			Connection connection = new Connection(randomWeight());
			outputWeights.add(connection);
			
		}
	}
	
	public void setOutputVal(double v){
		this.outputVal = v;
	}
	
	public double getOutputVal(){
		return this.outputVal;
	}
	public void feedForward(final Layer pl){
		double sum = 0.0;
		
		// Sum the previous layer's outputs,
		// Including the bias node from the previous layer.
		
		for(int n = 0; n < pl.size(); ++n){
			sum += pl.get(n).getOutputVal() * pl.get(n).outputWeights.get(index).getWeight();
		}
		
		outputVal = transferFunction(sum);
	}
	
	public void calcOutputGradients(double targetVal){
		
		double d = targetVal  - outputVal;
		gradient = d * transferFunctionDerivative(outputVal);
	}
	
	public void calcHiddenGradients(final Layer nextLayer){
		
		double dow = sumDOW(nextLayer);
		gradient = dow * transferFunctionDerivative(outputVal);
	}
	
	public void updateInputWeights(Layer prevLayer){
		
		// The weights to be updated are in the connection container
		// in the neurons in the preceding layer.
		
		for(int n = 0; n < prevLayer.size(); ++n){
			Neuron neuron = prevLayer.get(n);
			double oldDWeight = neuron.outputWeights.get(index).getDWeight();
			
			double newDWeight = 
					// Individual input, magnified by the gradient and train rate.
					eta * 
					neuron.getOutputVal() * 
					gradient +
					// Also add momentum -> a fraction of the previous delta weight
					alpha *
					oldDWeight;
			
			neuron.outputWeights.get(index).setDWeight(newDWeight);
			neuron.outputWeights.get(index).setWeight(neuron.outputWeights.get(index).getWeight() + newDWeight);
		}
	}
	
	private static double randomWeight(){
		return Math.random();
	}
	
	// Non linear function.
	private static double transferFunction(double x){
		
		// tanh -> output range [-1.0, 1.0]
		return Math.tanh(x);
	}
	
	private static double transferFunctionDerivative(double x){
		
		// d(tanh) == 1 - tanh^2(x)
		return 1.0 - x * x;
	}
	
	private final double sumDOW(final Layer nextLayer){
		
		double sum = 0.0;
		
		// Sum contributions of the errors at the nodes fed
		
		for(int n = 0; n < nextLayer.size() - 1; ++n){
			sum += outputWeights.get(n).getWeight() * nextLayer.get(n).gradient;
		}
		
		return sum;
				
	}
	
}
