import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		
//		TrainingData trainData = new TrainingData("trainingData.txt");
//		
//		
//		
//		//Defining input variables
//		ArrayList<Integer> topology = new ArrayList<Integer>();
//		ArrayList<Double> inputVals = new ArrayList<Double>();
//		ArrayList<Double> targetVals = new ArrayList<Double>();
//		ArrayList<Double> resultVals = new ArrayList<Double>();	
//		
//		trainData.getTopology(topology);
//
//		
//		Net myNet = new Net(topology);
//		int trainingPass = 0;
//		
//		while(!trainData.isEof()){
//			++trainingPass;
//			System.out.println();
//			System.out.print("Pass " + trainingPass);
//			
//			// Get new input data and feed it forward.
//			if(trainData.getNextInputs(inputVals) != topology.get(0)){
//				break;
//			}
//			
//			showVectorVals(": Inputs:", inputVals);
//			myNet.feedForward(inputVals);
//			
//			// Collect the net's actual results.
//			myNet.getResults(resultVals);
//			showVectorVals("Outputs:", resultVals);
//			
//			// Train the net with what the outputs should have been.
//			//trainData.getTargetOutputs(targetVals);
//			showVectorVals("Targets:", targetVals);
//			if(targetVals.size() == topology.get(topology.size() - 1)){
//				myNet.backProp(targetVals);
//			}
//			else{
//				throw new IllegalStateException("Error");
//			}
//			
//			// Report how well the training is working.
//			System.out.println("Net recent average error: " + myNet.getRecentAverageError());
//			System.out.println();
//			System.out.println("Done");
//		}
//		
		
		

	}
	
	public static void showVectorVals(String label, ArrayList<Double> v){
		
		System.out.print(label + " ");
		for(int i = 0; i < v.size(); ++i){
			System.out.print(v.get(i) + " ");
		}
		
		System.out.println();
	}

}
