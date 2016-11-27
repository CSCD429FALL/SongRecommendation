package songRecommendation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class FileOpener {

	public static InputStream openFile(String filePath) {

		InputStream is = FileOpener.class.getResourceAsStream(filePath);

		if (is == null)
			throw new NullPointerException("Path: " + filePath + " does not exist.");

		return is;

	}

	public static String[][] loadTable(String filePath, int numAttributes) throws IOException {

		InputStream is = FileOpener.openFile(filePath);
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));

		int fileLines = countFileLines(filePath);

		String[][] table = new String[fileLines][numAttributes];

		int curRow = 0;
		String line = "";

		while ((line = bf.readLine()) != null && curRow < fileLines) {

			// String[] quotationSplit = line.split("\"");
			// for(int i = 0; i < quotationSplit.length ; i++){
			// if(i != 0 && i%2 != 0){
			// quotationSplit[i] = quotationSplit[i].replaceAll(",", " ");
			//
			// }
			// }
			// line = "";
			// for(String s : quotationSplit){
			// line += s;
			// }

			String[] commaSplit = line.split(",");

			if (commaSplit.length != numAttributes)
				throw new IllegalArgumentException(
						"Number of attributes doesn't match with file." + " commsplit length: " + commaSplit.length
								+ " attr length: " + numAttributes + "rowNum: " + curRow + "\n" + line);

			for (int a = 0; a < numAttributes; a++) {
				table[curRow][a] = commaSplit[a];

				// System.out.print(commaSplit[a] + ", ");
			}
			// System.out.println();

			++curRow;
		}

		bf.close();
		is.close();
		return table;

	}


	/**
	 * @param db
	 *            The database path.
	 * @param table
	 *            The table in the database to get values from.
	 * @param column
	 *            The attribute from which to get the values from.
	 * @return HashMap with key as String of the value and the value as the
	 *         index.
	 */
	public static HashMap<String, Integer> getValues(String db, String table, String column) {

		HashMap<String, Integer> values = new HashMap<>();

		try {

			String sql = "SELECT " + column + " FROM " + table;

			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection(db);
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			int index = 0;

			while (rs.next()) {

				values.put(rs.getString(column), index);
				++index;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return values;
	}

	public static int countFileLines(String filePath) throws IOException {
		InputStream is = FileOpener.openFile(filePath);
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));
		int numLines = 0;
		while (bf.readLine() != null) {
			++numLines;
		}
		bf.close();
		is.close();

		return numLines;
	}

}
