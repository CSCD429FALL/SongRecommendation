package decisionTree;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * 
 * @author Sergio Ramirez
 *
 */

public class DecisionTree {

	private Node<String[][]> root;
	private String[][] D;
	private HashMap<Integer, String> attributeList;
	private boolean[] discreteValued;
	double[] seperatingValues;
	private final int classKey;

	/**
	 * @param D
	 *            Entire Data Set.
	 * @param attributeList
	 *            List of attributes to be used for tree splits.
	 * @param classKey
	 *            The index of the class attribute.
	 * @param discreteValued
	 *            Array where indices correspond to attributes in data set where
	 *            values indicate whether that attribute is discrete.
	 * @param seperatingValues
	 *            For continuous values this array indicates the split values.
	 */
	public DecisionTree(String[][] D, HashMap<Integer, String> attributeList, int classKey, boolean[] discreteValued,
			double[] seperatingValues) {
		this.D = D;
		this.classKey = classKey;
		this.attributeList = attributeList;
		this.discreteValued = discreteValued;
		this.seperatingValues = seperatingValues;
		root = generateDecisionTree(D, attributeList);
	}

	public void printTree() {
		printTree(root);
	}

	private void printTree(Node<String[][]> N) {

		if (!N.isRoot() && !N.isLeaf())
			System.out.println(N.getParentLabel() + ":" + N.getLabelValue() + "->" + N.getLabel());
		else if (N.isLeaf())
			System.out.println(N.getParentLabel() + " leaf :" + N.getLabelValue() + "->" + N.getLabel());
		else
			System.out.println(N.getLabel());

		if (!N.isLeaf()) {
			for (Node<String[][]> n : N.getChildren()) {
				printTree(n);
			}
		}

	}

	/**
	 * Prints the subsets in each leaf.
	 */
	public void printTreeLeafSubsets() {
		printTreeLeafSubsets(root, "");
	}

	private void printTreeLeafSubsets(Node<String[][]> N, String path) {

		if (N.isLeaf()) {
			String[][] subset = N.getData();

			for (String[] row : subset) {
				System.out.println(path + row[1] + ", " + row[2] + ", " + row[4] + ", " + row[9]);

			}

			System.out.println();
		}
		if(N.isRoot()){
			path += N.getLabel() + "->";
		}
		if (!N.isLeaf()) {
			for (Node<String[][]> n : N.getChildren()) {
				String newPath =  path;
				if(!n.isLeaf()){
					newPath += "[" + n.getLabelValue() + "]->" + n.getLabel();
				}
				else{
					newPath += "[" + n.getLabelValue() + "]->";
				}


				printTreeLeafSubsets(n, newPath);
			}
		}

	}

	/**
	 * Suggest an item in the data set based on the input tuple.
	 * 
	 */
	public String[][] suggestItems(String[] origin) {
		return suggestItems(origin, root);
	}

	private String[][] suggestItems(String[] origin, Node<String[][]> N) {

		if(N == null){
			return null;
		}
		if (N.isLeaf()) {
			String[][] subset = N.getData();



			return subset;
		}

		int curAttribute = -1;
		if (N.hasLabelIndex()) {
			curAttribute = N.getLabelIndex();
		} else {
			throw new IllegalStateException();
		}
		Node<String[][]> curNode = null;

		if(discreteValued[curAttribute]){
			for (Node<String[][]> child : N.getChildren()) {


				if (child.getLabelValue().equals(origin[curAttribute])) {
					curNode = child;
					break;
				}


			}
		}
		else{
			for (Node<String[][]> child : N.getChildren()) {

				if (child.getLabelValue().substring(0, 3).equals("le:")) {
					if(Double.parseDouble(origin[curAttribute]) <= seperatingValues[curAttribute]){
						curNode = child;
						break;
					}

				}
				else if (child.getLabelValue().substring(0, 3).equals("gt:")) {
					if(Double.parseDouble(origin[curAttribute]) > seperatingValues[curAttribute]){
						curNode = child;
						break;
					}

				}


			}
		}


		
		return suggestItems(origin, curNode);

	}

	/**
	 * Prints a friendly visual representation of the tree structure.
	 */
	public void printTreeStructure() {
		printTreeStructure(root, "");
	}

	private void printTreeStructure(Node<String[][]> N, String path) {

		if (N.isLeaf()) {

			System.out.println(path);

		}
		if(N.isRoot()){
			path += N.getLabel() + "->";
		}
		if (!N.isLeaf()) {
			for (Node<String[][]> n : N.getChildren()) {
				String newPath =  path;
				if(!n.isLeaf()){
					newPath += "[" + n.getLabelValue() + "]->" + n.getLabel();
				}
				else{
					newPath += "[" + n.getLabelValue() + "]->";
				}


				printTreeStructure(n, newPath);
			}
		}

	}

	public String predict(String[] tuple) {
		return predict(tuple, root);
	}

	public String predict(String[] tuple, Node<String[][]> N) {

		if (N.isLeaf()) {
			return N.getLabel();
		}

		int curAttribute = -1;
		if (N.hasLabelIndex()) {
			curAttribute = N.getLabelIndex();
		} else {
			throw new IllegalStateException();
		}
		Node<String[][]> curNode = null;

		for (Node<String[][]> child : N.getChildren()) {
			if (child.getLabelValue().equals(tuple[curAttribute])) {
				curNode = child;
				break;
			}
		}
		if (curNode == null) {
			curNode = N.getChildren().get(0);
		}

		return predict(tuple, curNode);

	}

	// Stepping through the Inducing a decision tree algorithm as shown in 8.2
	// from book.
	private Node<String[][]> generateDecisionTree(String[][] D, HashMap<Integer, String> attribute_list) {

		// Creating a new node N.
		Node<String[][]> N = new Node<String[][]>(D);

		/*
		 * FIRST TERMINATING CONDITION If tuples in root are all of the same
		 * class.
		 */
		String classLabel = sameClass(D);
		if (classLabel != null) {

			String mc = getMajorityClass(D);

			N.setLabel(mc);
			return N;
		}

		/*
		 * SECOND TERMINATING CONDITION If attribute_list is empty
		 */

		if (attribute_list.isEmpty()) {

			String mc = getMajorityClass(D);
			N.setLabel(mc);

			return N;
		}

		// Applying attribute selection method: information gain.
		HashMap.Entry<Integer, String> splitCriterion = info(N.getData(), attribute_list);
		// System.out.println(attribute_list.size() + " " + splitCriterion);
		N.setLabel(splitCriterion.getValue());
		N.setLabelIndex(splitCriterion.getKey());
		// System.out.println("Split label:" + splitCriterion.getValue());

		// for each outcome of the splitting_criterion
		// partition the tuples and grow subtrees for each partition.

		HashMap<String, Integer> splitValues = null;

		// Checking if split attribute is discrete or continuous.
		if (discreteValued[splitCriterion.getKey()]) {
			splitValues = getValues(N.getData(), splitCriterion.getKey());
			HashMap<Integer, String> newAttList = new HashMap<Integer, String>(attribute_list);
			newAttList.remove(splitCriterion.getKey());



			for (HashMap.Entry<String, Integer> e : splitValues.entrySet()) {

			
				String[][] subset = getSubset(D, splitCriterion.getKey(), e.getKey());
				/*
				 * THIRD TERMINATING CONDITION If subset is empty then attach a
				 * leaf node labeled with the majority class of D.
				 */
				if (subset.length == 0) {
					String mc = getMajorityClass(D);

					Node<String[][]> leaf = new Node<>(getSubset(D, classKey, mc));
					leaf.setLabel(mc);

					N.addChild(leaf, e.getKey());


				} else {
					N.addChild(generateDecisionTree(subset, newAttList), e.getKey());
				}
			}
		} else {
			HashMap<Integer, String> newAttList = new HashMap<Integer, String>(attribute_list);
			newAttList.remove(splitCriterion.getKey());

			// going through all 3 branching criteria
			// 0 -> retrieve items less than or equal to seperatingValue
			// 1 -> retrieve items greater than seperatingValue
			for (int i = 0; i < 2; i++) {
				double seperatingValue = seperatingValues[splitCriterion.getKey()];
				String[][] subset = getSubset(D, splitCriterion.getKey(), seperatingValues[splitCriterion.getKey()], i);

				/*
				 * THIRD TERMINATING CONDITION If subset is empty then attach a
				 * leaf node labeled with the majority class of D.
				 */
				String label = "";
				if (i == 0) {
					label = "le:";
				} else {
					label = "gt:";
				}
				if (subset.length == 0) {
					String mc = getMajorityClass(D);

					Node<String[][]> leaf = new Node<>(getSubset(D, classKey, mc));
					leaf.setLabel(mc);
					// System.out.println("subset length 0:" +
					// getMajorityClass(D));
					N.addChild(leaf, label + seperatingValue);

				} else {

					N.addChild(generateDecisionTree(subset, newAttList), label+ seperatingValue);
				}
			}

		}

		return N;

	}

	/*
	 * Returns a subset of the data set.
	 * 
	 * @param attr: The global static index that points to the attribute from
	 * where all values will be checked.
	 * 
	 * @param value: The String value that will be checked for every tuple. If a
	 * tuple has this value at the given attribute then that tuple will be added
	 * to the sub set.
	 */
	public static String[][] getSubset(String[][] D, int attr, String value) {

		ArrayList<String[]> subsetList = new ArrayList<>();

		for (int i = 0; i < D.length; i++) {

			if (D[i][attr].equals(value)) {
				subsetList.add(D[i]);
			}
		}

		String[][] subset = new String[subsetList.size()][];

		subset = subsetList.toArray(subset);

		return subset;
	}

	public static String[][] getSubset(String[][] D, int attr, double seperatingValue, int operation) {

		ArrayList<String[]> subsetList = new ArrayList<>();

		for (int i = 0; i < D.length; i++) {

			if (operation == 0) {
				if (Double.parseDouble(D[i][attr]) <= seperatingValue) {
					subsetList.add(D[i]);
				}
			} else if (operation == 1) {
				if (Double.parseDouble(D[i][attr]) > seperatingValue) {
					subsetList.add(D[i]);
				}
			}

		}

		String[][] subset = new String[subsetList.size()][];

		subset = subsetList.toArray(subset);

		return subset;
	}

	public HashMap.Entry<Integer, String> info(String[][] D, HashMap<Integer, String> attribute_list) {

		double info_D = 0;
		double P_i = 0;

		HashMap<String, Integer> classValues = getValues(D, classKey);

		for (HashMap.Entry<String, Integer> e : classValues.entrySet()) {
			P_i = e.getValue() / D.length;
			info_D += P_i * Math.log10(P_i) / Math.log10(2);

		}
		info_D = info_D * -1;

		HashMap.Entry<Integer, String> splitAttribute = null;
		double maxInfo = Integer.MAX_VALUE * -1;

		for (HashMap.Entry<Integer, String> e : attribute_list.entrySet()) {

			if (e.getKey() != classKey) {
				splitAttribute = e;
				HashMap<String, Integer> curAttribute = new HashMap<>();
				if (discreteValued[e.getKey()]) {
					getValues(D, e.getKey());
				} else {
					getValues(D, e.getKey(), seperatingValues[e.getKey()]);
				}

				double D_i = 0;
				double d = curAttribute.size();
				double info_Da = 0;

				for (HashMap.Entry<String, Integer> a : curAttribute.entrySet()) {
					D_i = a.getValue();
					info_Da += (D_i / d) * info_D;

				}

				if ((info_D - info_Da) > maxInfo) {
					maxInfo = info_D - info_Da;
					splitAttribute = e;
				}
			}
		}

		return splitAttribute;
	}

	/*
	 * Returns the values in the data set of the attribute given. Returns a
	 * Hashmap with the value at the given attribute as the key, and the amount
	 * of times that value is present as the Hashmap value.
	 */
	public static HashMap<String, Integer> getValues(String[][] D, int v) {

		HashMap<String, Integer> values = new HashMap<>();

		for (int tuple = 0; tuple < D.length; tuple++) {

			String value = D[tuple][v];

			if (values.containsKey(value)) {
				values.put(value, values.get(value) + 1);
			} else {
				values.put(value, 1);
			}
		}

		return values;
	}

	public static HashMap<String, Integer> getValues(String[][] D, int attr, double seperatingValue) {

		HashMap<String, Integer> values = new HashMap<>();
		String key = "";

		for (int i = 0; i < D.length; i++) {

			if (Double.parseDouble(D[i][attr]) <= seperatingValue) {
				key = "<= " + seperatingValue;
			} else if (Double.parseDouble(D[i][attr]) > seperatingValue) {
				key = "> " + seperatingValue;
			}

			if (values.containsKey(key)) {
				values.put(key, values.get(key) + 1);
			} else {
				values.put(key, 1);
			}

		}

		return values;
	}

	/*
	 * Returns the majority class in the data set, as a String.
	 */
	public String getMajorityClass(String[][] D) {

		HashMap<String, Integer> classValues = new HashMap<>();

		for (int tuple = 0; tuple < D.length; tuple++) {

			String classValue = D[tuple][classKey];

			if (classValues.containsKey(classValue)) {
				classValues.put(classValue, classValues.get(classValue) + 1);
			} else {
				classValues.put(classValue, 1);
			}

		}

		String majorityClass = "";
		int max = 0;

		for (HashMap.Entry<String, Integer> e : classValues.entrySet()) {
			int curCount = e.getValue();

			if (curCount > max) {
				majorityClass = e.getKey();
			}

		}

		return majorityClass;

	}

	/*
	 * Searches the input dataset's class attribute and determines if all tuples
	 * have the same class. If all tuples have the same class it returns the
	 * class name. Otherwise it returns null.
	 *
	 */
	public String sameClass(String[][] D) {

		String curClassValue = D[0][classKey];

		for (int tuple = 1; tuple < D.length; tuple++) {

			String nextClassValue = D[tuple][classKey];
			if (!curClassValue.equals(nextClassValue))
				return null;

			curClassValue = nextClassValue;

		}

		return curClassValue;
	}

}
