package spandora;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;


public class FileOpener {

	public static InputStream openFile(String filePath){

		InputStream is = FileOpener.class.getResourceAsStream(filePath);



		if(is == null)
			throw new NullPointerException("Path: " + filePath + " does not exist.");

		return is;

	}

	public static String[][] loadTable(String filePath, int lines, int... attributes) throws IOException{

		InputStream is = FileOpener.openFile(filePath);
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));

		int fileLines = countFileLines(filePath) - 1;
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

	/**
	 * 
	 * @param filePath Path where file is located.
	 * @param numberOfAttributes Number of attributes in the file.
	 * @param primaryKey Index of the primary key in the end result;
	 * @param multiValuedAttributes HashMap that contains the index and the values of attributes
	 * 								with multiple values.
	 * @return The data set as String[][] with multiple values converted to a single binary value. 
	 * @throws IOException
	 */
	public static String[][] loadTable(String filePath, int numberOfAttributes, int primaryKey, 
			HashMap<Integer, HashMap<String, Integer>> multiValuedAttributes) throws IOException{

		InputStream is = FileOpener.openFile(filePath);
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));

		int fileLines = 1000000;
		String[][] table = new String[338047][numberOfAttributes];
		String line = "";
		String key = "";
		String previousKey = "";
		int curRow = 0;
		int keyRow = 0;
		int misses = 0;

		ArrayList<MultiValuedAttribute> multiAttributes = new ArrayList<>();

		for (HashMap.Entry<Integer, HashMap<String, Integer>> e : multiValuedAttributes.entrySet()) {	    			
			
			multiAttributes.add(new MultiValuedAttribute(e.getValue().size(), e.getKey()));

		}
		long startTime = System.nanoTime();
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

			if(commaSplit.length != numberOfAttributes){

				throw new IllegalArgumentException("Number of attributes doesn't match with file."
						+ " commsplit length: " + commaSplit.length + " attr length: " + numberOfAttributes + "rowNum: " + curRow + "\n" + line);
			}

			key = commaSplit[primaryKey];
			if(previousKey.equals(key)){
				for(MultiValuedAttribute a : multiAttributes){
					
					int mvIndex = a.getIndex();
					String attrValueKey = commaSplit[mvIndex];
					
					HashMap<String, Integer> attrValues = multiValuedAttributes.get(mvIndex);
					
					if(attrValues.containsKey(attrValueKey)){
						a.setValue(true, attrValues.get(attrValueKey));
						
					}
					else{
						++misses;
					}

				}
			}
			else{

				if(keyRow > 0){
					
					for(MultiValuedAttribute a : multiAttributes){
						String singleValueRepresentation = a.toString();
						table[keyRow - 1][a.getIndex()] = singleValueRepresentation;
						//System.out.println(singleValueRepresentation);
					}
					
				

					//System.out.println(keyRow + ": 1st: " + table.get(keyRow)[9] + "\n2nd: " + table.get(keyRow)[10] + "\n");


				}
				
				for(int a = 0; a < numberOfAttributes; ++a){

					if(!multiValuedAttributes.containsKey(a)){
						table[keyRow][a] = commaSplit[a];				
						
					}				

				}
				//System.out.println(keyRow);
				
				++keyRow;
				
			}
			

			previousKey = key;

			++curRow;
		}
		
		
		for(MultiValuedAttribute a : multiAttributes){
			String singleValueRepresentation = a.toString();
			table[keyRow - 1][a.getIndex()] = singleValueRepresentation;
		}

		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000;//execution time for this method in milliseconds
		System.out.println(duration + ", " + keyRow + ", misses: " + misses);

		bf.close();
		is.close();

		return table;

	}

	/**
	 * @param db The database path.
	 * @param table The table in the database to get values from.		
	 * @param column The attribute from which to get the values from.
	 * @return 	HashMap with key as String of the value and
	 * 			the value as the index.
	 */
	public static HashMap<String, Integer> getValues(String db, String table, String column){

		HashMap<String, Integer> values = new HashMap<>();


		try {

			String sql = 	"SELECT " + column + 
					" FROM " + table;

			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection(db);
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			int index = 0;

			while(rs.next()){

				values.put(rs.getString(column), index);
				++index;

			}

		}
		catch(Exception e) {
			e.printStackTrace(); 
		}


		return values;
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
