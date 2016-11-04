import java.util.ArrayList;

public class Net {
	
	private ArrayList<Layer> layers;// layers[layerNum][neuronNum]
	private double error;
	private double recentAverageError;
	private double recentAverageSmoothingFactor;
	
	public Net(final ArrayList<Integer> topology){
		
		layers = new ArrayList<Layer>();
		int numLayers = topology.size();
		
		for(int layerIndex = 0; layerIndex < numLayers; ++layerIndex){
			layers.add(new Layer());
			int numOutputs = layerIndex == topology.size() - 1? 0 : topology.get(layerIndex + 1);
			
			for(int neuronIndex = 0; neuronIndex <= topology.get(layerIndex); ++neuronIndex){
				layers.get(layers.size() - 1).add(new Neuron(numOutputs, neuronIndex));
				System.out.println("Neuron");
			}
		}
		
		// Force the bias node's output value to 1.0. Its the last neuron created above.
		Layer layer = layers.get(layers.size() - 1);
		Neuron neuron = layer.get(layer.size() - 1);
		
		neuron.setOutputVal(1.0);
		
	}
	
	public void feedForward(final ArrayList<Double> inputVals){
		
		if(inputVals.size() == layers.get(0).size() - 1){
			
			// Assign the input values into the input neurons.
			for(int i = 0; i < inputVals.size(); ++i){
				layers.get(0).get(i).setOutputVal(inputVals.get(i));
			}
			
			// Forward propagate
			for(int l = 1; l < layers.size(); ++l){
				
				Layer prevLayer = layers.get(l - 1);
				for(int n = 0; n < layers.get(l).size() - 1; ++n){
					layers.get(l).get(n).feedForward(prevLayer);
				}
			}
			
		}
		else{
		
			throw new IllegalStateException("Error!");
		}
	}
	
	public void backProp(final ArrayList<Double> targetVals){
		
		// Calculate overall net error RMS of output neuron errors
		
		Layer outputLayer = layers.get(layers.size() - 1);
		error = 0.0;
		
		for(int n = 0; n < outputLayer.size() - 1; ++n){
			double d = targetVals.get(n) - outputLayer.get(n).getOutputVal();
			error += d *d;
		}
		
		error /= outputLayer.size() - 1;// Average error squared
		error = Math.sqrt(error); // RMS
		
		// Implement a recent average measurement:
		
		recentAverageError = (recentAverageError * recentAverageSmoothingFactor + error) / (recentAverageSmoothingFactor + 1.0);
			
		// Calculate output layer gradients
		
		for(int n = 0; n < outputLayer.size() - 1; ++n){
			outputLayer.get(n).calcOutputGradients(targetVals.get(n));
		}
			
		// Calculate gradients on hidden layers
		
		for(int ln = layers.size() - 2; ln > 0; --ln){
			Layer hiddenLayer = layers.get(ln);
			Layer nextLayer = layers.get(ln + 1);
			
			for(int n = 0; n < hiddenLayer.size(); ++n){
				hiddenLayer.get(n).calcHiddenGradients(nextLayer);
			}
		}
		
		// For all layers from outputs to first hidden layer.
		// Update connection weights
		
		for(int ln = layers.size() - 1; ln > 0; --ln){
			Layer layer = layers.get(ln);
			Layer prevLayer = layers.get(ln - 1);
			
			for(int n = 0; n < layer.size() - 1; ++n){
				layer.get(n).updateInputWeights(prevLayer);
			}
		}
	}
	
	//Function can't modify the class.
	public final void getResults(ArrayList<Double> resultVals){
		
		resultVals.clear();
		
		for(int n = 0; n < layers.get(layers.size() - 1).size() - 1; ++n){
			resultVals.add(layers.get(layers.size() - 1).get(n).getOutputVal());
		}
	}
	
	public double getRecentAverageError(){
		return recentAverageError;
	}
}
