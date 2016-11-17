package decisionTree;

import java.util.ArrayList;
import java.util.HashMap;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.AttributeType;

public class DecisionTree {

	private Node<String[][]> root;//root node of the decision tree
	private String[][] D;//data set

	//attributeList contains index of its corresponding attribute value as the key
	private HashMap<Integer, String> attributeList;
	boolean[] discreteValued;//false if its continuous.

	/*
	 * seperatingValues
	 * each row is each attribute that is continuous,
	 * and each column for each row is a a split point
	 * for example:
	 * seperatingValues[SOMEATTRIBUTE][0] = 2
	 * seperatingValues[SOMEATTRIBUTE][1] = 4
	 * 
	 * that means for SOMEATTRIBUTE three branches will be created,
	 * the first with values less than 2
	 * the second with values => 2 && values <= 4
	 * the third with values > 4
	 */
	double[] seperatingValues;
	private final int classKey;

	public DecisionTree(String[][] D, HashMap<Integer, String> attributeList, int classKey,
			boolean[] discreteValued, double[] seperatingValues){
		this.D = D;
		this.attributeList = attributeList;
		this.classKey = classKey;
		this.discreteValued = discreteValued;
		this.seperatingValues = seperatingValues;
		root = generateDecisionTree(D, attributeList);
	}

	/*
	 * Prints the given tree with root node @param N.
	 */

	public void printTree(){
		printTree(root);
	}

	private void printTree(Node<String[][]> N){

		if(!N.isRoot() && !N.isLeaf())
			System.out.println(N.getParentLabel() + ":" + N.getLabelValue() + "->" + N.getLabel());
		else if(N.isLeaf())
			System.out.println(N.getParentLabel() + " leaf :" + N.getLabelValue() + "->" + N.getLabel());
		else
			System.out.println(N.getLabel());


		if(!N.isLeaf()){
			for(Node<String[][]> n: N.getChildren()){
				printTree(n);
			}
		}
		
	}
	
	public String predict(String[] tuple){
		return predict(tuple, root);
	}
	public String predict(String[] tuple, Node<String[][]> N){

		if(N.isLeaf()){
			return N.getLabel();
		}

		int curAttribute = -1;
		if(N.hasLabelIndex())
			curAttribute = N.getLabelIndex();
		else
			throw new IllegalStateException();


		Node<String[][]> curNode = null;


		for(Node<String[][]> child : N.getChildren()){
			if(child.getLabelValue().equals(tuple[curAttribute])){
				curNode = child;
				break;
			}
		}
		if(curNode == null){
			curNode = N.getChildren().get(0);
		}


		return predict(tuple, curNode);


	}

	//Stepping through the Inducing a decision tree algorithm as shown in 8.2 from book.
	private Node<String[][]> generateDecisionTree(String[][] D, HashMap<Integer, String> attribute_list){

		//Creating a new node N.
		Node<String[][]> N = new Node<String[][]>(D);


		/*
		 *  FIRST TERMINATING CONDITION
		 *  If tuples in root are all of the same class.
		 */
		String classLabel = sameClass(D);
		if(classLabel != null){

			N.setLabel(classLabel);
			return N;
		}

		/*
		 * SECOND TERMINATING CONDITION
		 * If attribute_list is empty
		 */

		if(attribute_list.isEmpty() || (attribute_list.size() == 1 && attribute_list.containsKey(classKey))){
			N.setLabel(getMajorityClass(D));
			//System.out.println("attr list empty class label:" + getMajorityClass(D));
			return N;
		}

		//Applying attribute selection method: information gain.
		HashMap.Entry<Integer, String> splitCriterion = info(N.getData(), attribute_list);
		//System.out.println(attribute_list.size() + " " + splitCriterion);
		N.setLabel(splitCriterion.getValue());
		N.setLabelIndex(splitCriterion.getKey());
		//System.out.println("Split label:" + splitCriterion.getValue());

		//for each outcome of the splitting_criterion
		//partition the tuples and grow subtrees for each partition.

		HashMap<String, Integer> splitValues = null;

		//Checking if split attribute is discrete or continuous.
		if(discreteValued[splitCriterion.getKey()]){
			splitValues = getValues(N.getData(), splitCriterion.getKey());
			HashMap<Integer, String> newAttList = attribute_list;
			newAttList.remove(splitCriterion.getKey());

			for (HashMap.Entry<String, Integer> e : splitValues.entrySet())
			{	
				String[][] subset = getSubset(D, splitCriterion.getKey(), e.getKey());
				/*
				 * THIRD TERMINATING CONDITION
				 * If subset is empty then attach a leaf node labeled with the majority class of D.
				 */
				if(subset.length == 0){
					Node<String[][]> leaf = new Node<String[][]>(new String[0][0]);
					leaf.setLabel(getMajorityClass(D));
					//System.out.println("subset length 0:" + getMajorityClass(D));
					N.addChild(leaf);
				}
				else{
					N.addChild(generateDecisionTree(subset, newAttList), e.getKey());
				}
			}
		}
		else{
			HashMap<Integer, String> newAttList = attribute_list;
			newAttList.remove(splitCriterion.getKey());

			//going through all 3 branching criteria
			//0 -> retrieve items less than or equal to seperatingValue
			//1 -> retrieve items greater than seperatingValue
			for(int i = 0; i < 2; i++){
				double seperatingValue = seperatingValues[splitCriterion.getKey()];
				String[][] subset = getSubset(D, splitCriterion.getKey(), seperatingValues[splitCriterion.getKey()], i);

				/*
				 * THIRD TERMINATING CONDITION
				 * If subset is empty then attach a leaf node labeled with the majority class of D.
				 */
				if(subset.length == 0){
					Node<String[][]> leaf = new Node<String[][]>(new String[0][0]);
					leaf.setLabel(getMajorityClass(D));
					//System.out.println("subset length 0:" + getMajorityClass(D));
					N.addChild(leaf);
				}
				else{
					String label = ""; 
					if(i == 0){
						label = "<=";
					}
					else{
						label = ">";
					}
					N.addChild(generateDecisionTree(subset, newAttList), label + " " + seperatingValue);
				}
			}

		}

		return N;

	}

	/*
	 * Returns a subset of the data set.
	 * @param attr: The global static index that points to the attribute
	 * from where all values will be checked.
	 * @param value: The String value that will be checked for every
	 * tuple. If a tuple has this value at the given attribute then that
	 * tuple will be added to the sub set.
	 */
	public static String[][] getSubset(String[][] D, int attr, String value){

		ArrayList<String[]> subsetList = new ArrayList<>();

		for(int i = 0; i < D.length; i++){

			if(D[i][attr].equals(value)){
				subsetList.add(D[i]);
			}
		}

		String[][] subset = new String[subsetList.size()][];

		subset = subsetList.toArray(subset);


		return subset;
	}

	public static String[][] getSubset(String[][] D, int attr, double seperatingValue, int operation){

		ArrayList<String[]> subsetList = new ArrayList<>();

		for(int i = 0; i < D.length; i++){

			if(operation == 0){
				if(Double.parseDouble(D[i][attr]) <= seperatingValue){
					subsetList.add(D[i]);
				}
			}
			else if(operation == 1){
				if(Double.parseDouble(D[i][attr]) > seperatingValue){
					subsetList.add(D[i]);
				}
			}

		}

		String[][] subset = new String[subsetList.size()][];

		subset = subsetList.toArray(subset);


		return subset;
	}

	/*
	 * Computes the informational gain of the given data set.
	 * @param attribute_list: The input attribute list of the
	 * data set.
	 * Returns a HasMap.Entry that contains the attribute's static
	 * global index that points to that attribute in the data set.
	 * The global index is the key in the Hashmap.Entry return
	 * object, and it also returns a String representation of the
	 * name of the attribute as the value of the Hashmap.Entry.
	 */
	public HashMap.Entry<Integer, String> info(String[][] D, HashMap<Integer, String> attribute_list){

		double info_D = 0;
		double P_i = 0;

		//find
		HashMap<String, Integer> classValues = getValues(D, classKey);


		for (HashMap.Entry<String, Integer> e : classValues.entrySet())
		{	    
			P_i = e.getValue()/D.length;
			info_D += P_i * Math.log10(P_i)/Math.log10(2);

		}		
		info_D = info_D * -1;

		HashMap.Entry<Integer, String> splitAttribute = null;
		double maxInfo = Integer.MAX_VALUE * -1;

		for (HashMap.Entry<Integer, String> e : attribute_list.entrySet())
		{	

			if(e.getKey() != classKey){
				splitAttribute = e;
				HashMap<String, Integer> curAttribute = new HashMap<>();
				if(discreteValued[e.getKey()]){
					getValues(D, e.getKey());
				}
				else{
					getValues(D, e.getKey(), seperatingValues[e.getKey()]);
				}


				double D_i = 0;
				double d = curAttribute.size();
				double info_Da = 0;

				for (HashMap.Entry<String, Integer> a : curAttribute.entrySet())
				{	    				 				
					D_i = a.getValue();
					info_Da += (D_i/d) * info_D;

				}

				if((info_D - info_Da) > maxInfo){
					maxInfo = info_D - info_Da;
					splitAttribute = e;
				}
			}
		}

		return splitAttribute;
	}

	/*
	 * Returns the values in the data set of the attribute given.
	 * Returns a Hashmap with the value at the given attribute as
	 * the key, and the amount of times that value is present as the
	 * Hashmap value.
	 */
	public static HashMap<String, Integer> getValues(String[][] D, int v){

		HashMap<String, Integer> values = new HashMap<>();

		for(int tuple = 0; tuple < D.length; tuple++){

			String value = D[tuple][v];

			if(values.containsKey(value)){
				values.put(value, values.get(value) + 1);
			}
			else{
				values.put(value, 1);
			}
		}


		return values;
	}

	public static HashMap<String, Integer> getValues(String[][] D, int attr, double seperatingValue){

		HashMap<String, Integer> values = new HashMap<>();
		String key = "";

		for(int i = 0; i < D.length; i++){

			if(Double.parseDouble(D[i][attr]) <= seperatingValue){
				key = "<= " + seperatingValue;
			}
			else if(Double.parseDouble(D[i][attr]) > seperatingValue){
				key = "> " + seperatingValue;			
			}

			if(values.containsKey(key)){
				values.put(key, values.get(key) + 1);
			}
			else{
				values.put(key, 1);
			}

		}

		return values;
	}




	/*
	 * Returns the majority class in the data set, as a String.
	 */
	public String getMajorityClass(String[][] D){

		HashMap<String, Integer> classValues = new HashMap<>();

		for(int tuple = 0; tuple < D.length; tuple++){

			String classValue = D[tuple][classKey];

			if(classValues.containsKey(classValue)){
				classValues.put(classValue, classValues.get(classValue) + 1);
			}
			else{
				classValues.put(classValue, 1);
			}



		}

		String majorityClass = "";
		int max = 0;

		for (HashMap.Entry<String, Integer> e : classValues.entrySet())
		{	    
			int curCount = e.getValue();

			if(curCount > max){
				majorityClass = e.getKey();
			}

		}

		return majorityClass;

	}

	/*
	 * Searches the input dataset's class attribute and
	 * determines if all tuples have the same class.
	 * If all tuples have the same class it returns the class name.
	 * Otherwise it returns null. 
	 *
	 */
	public String sameClass(String[][] D){

		String curClassValue = D[0][classKey];

		for(int tuple = 1; tuple < D.length; tuple++){

			String nextClassValue = D[tuple][classKey];
			if(!curClassValue.equals(nextClassValue))
				return null;

			curClassValue = nextClassValue;

		}


		return curClassValue;
	}

}
