package spandora;


import java.io.PrintWriter;

public class FileWriter {
	
	public static void write(String filePath, String[][] data){
		
		try{
			PrintWriter pw = new PrintWriter(filePath);
			
			for(int row = 0; row < data.length; row++){
				for(int col = 0; col < data[row].length; col++){
					if(col < data[row].length - 1){						
						pw.print(data[row][col] + ",");
					}
					else{
						pw.print(data[row][col]);
					}
				}
				pw.println();
			}
			
			pw.close();
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
}
