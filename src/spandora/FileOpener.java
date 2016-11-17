package spandora;

import java.io.InputStream;

public class FileOpener {
	
	public static InputStream openFile(String filePath){
		
		InputStream is = FileOpener.class.getResourceAsStream(filePath);
		
		if(is == null)
			throw new NullPointerException("Path: " + filePath + "does not exist.");
		
		return is;
		
	}

}
