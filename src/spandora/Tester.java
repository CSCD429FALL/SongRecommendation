package spandora;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.HashMap;
import main.Preprocessor;


import decisionTree.DecisionTree;

public class Tester {

	public static int TRACK_ID = 0, ARTIST_ID = 1, RELEASE = 2, ARTIST_FAMILIARITY = 3, ARTIST_HOTTNESS = 4, 
			YEAR = 5, TAG = 6, NUMBER_OF_ATTRIBUTES = 7;


	public static void main(String[] args) throws IOException {


		//Second argument defines how many tuples to read, defining a value larger than the number of total tuples
		//in data will default to max tuples in data.
		String[][] songs = loadTable("/CSV/test.csv",Integer.MAX_VALUE, new int[NUMBER_OF_ATTRIBUTES]);


		HashMap<Integer,String> attributeList = new HashMap<>();

		//Creating attribute list to be used in decision tree.
		attributeList.put(RELEASE, "Release");
		attributeList.put(ARTIST_ID, "Artist ID");
		attributeList.put(ARTIST_FAMILIARITY, "Artist Familiarity");
		attributeList.put(ARTIST_HOTTNESS, "Artist Hottness");
		attributeList.put(YEAR, "Year");
		attributeList.put(TAG, "Tag");

		//Setting the discrete valued attributes.
		boolean[] discreteValued = new boolean[NUMBER_OF_ATTRIBUTES]; 
		discreteValued[RELEASE] = true;
		discreteValued[YEAR] = true;
		discreteValued[TAG] = true;


		//Setting the separating values for the continuous attributes.
		double[] seperatingValues = new double[NUMBER_OF_ATTRIBUTES];
		seperatingValues[ARTIST_FAMILIARITY] = 0.5;
		seperatingValues[ARTIST_HOTTNESS] = 0.5;

		DecisionTree decisionTree = new DecisionTree(songs, attributeList, ARTIST_ID, discreteValued, seperatingValues);
		decisionTree.printTree();


		//		Preprocessor pre = new Preprocessor(false);
		//
		//		//ResultSet result = pre.getTable(pre.ARTISTS);
		//		
		//		String songsTableID = pre.SONGS + ".artist_id";
		//		String tagsTableID = pre.ARTIST_TAG + ".artist_id";
		//		String selectColumns = pre.SONGS + ".track_id, " + songsTableID + ", " + pre.SONGS + ".album" + ", " + 
		//				pre.SONGS + ".artist_familiarity, " + pre.SONGS + ".artist_hotness, " + pre.SONGS + ".year, " + pre.ARTIST_TAG + ".mbtag";
		//		
		//		pre.writeCSV("src/data/CSV/test.csv",pre.runQuery("SELECT " + selectColumns +  
		//		" from " + pre.SONGS  + " inner join " + pre.ARTIST_TAG + " on " + songsTableID + "=" + tagsTableID));
		//		

	}

	public static String[][] loadTable(String filePath, int lines, int... attributes) throws IOException{

		InputStream is = FileOpener.openFile(filePath);
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));

		int fileLines = countFileLines(filePath);
		if(lines < fileLines){
			fileLines = lines;
		}
		String[][] table = new String[fileLines][attributes.length];

		int curRow = 0;
		String line = bf.readLine();

		while((line = bf.readLine()) != null && curRow < fileLines){

//			String[] quotationSplit = line.split("\"");
//			for(int i = 0; i < quotationSplit.length ; i++){
//				if(i != 0 && i%2 != 0){
//					quotationSplit[i] = quotationSplit[i].replaceAll(",", " ");
//
//				}
//			}
//			line = "";
//			for(String s : quotationSplit){
//				line += s;
//			}

			String[] commaSplit = line.split(",");

			if(commaSplit.length != attributes.length)
				throw new IllegalArgumentException("Number of attributes doesn't match with file."
						+ " commsplit length: " + commaSplit.length + " attr length: " + attributes.length + "rowNum: " + curRow + "\n" + line);

			for(int a = 0; a < attributes.length; a++){				
				table[curRow][a] = commaSplit[a];
				//System.out.print(commaSplit[a] + ", ");
			}
			//System.out.println();

			++curRow;
		}

		bf.close();
		is.close();
		return table;

	}

	public static int countFileLines(String filePath) throws IOException{
		InputStream is = FileOpener.openFile(filePath);
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));
		int numLines = 0;
		while(bf.readLine() != null){
			++numLines;
		}
		bf.close();
		is.close();

		return numLines;
	}

}
