import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TrainingData {
	
	private Scanner in;
	
	public TrainingData(final String filename) throws FileNotFoundException{
		in = new Scanner(new File(filename));
	}
	
	public boolean isEof(){
		return in.hasNext();
	}
	
	public void getTopology(ArrayList<Integer> topology){
		String line = "";
		String label = "";
		
		line = in.nextLine();
		
	}
	
	
	
}
