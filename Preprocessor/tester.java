package main;

import java.sql.ResultSet;

public class tester {
	
	private static Preprocessor backend = null;
	
	public static void main(String[] args) {
		
		backend = new Preprocessor();
		
		ResultSet temp = backend.runQuery("SELECT * FROM f16429team_music_recommend."+ backend.ARTIST_TAG);
		
		backend.printQuery(temp);
		
		backend.writeCSV("output.csv", temp);
		
		backend.close();
	}
	
	
}
