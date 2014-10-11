package edu.hkust.leap.record.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;



import edu.hkust.leap.record.*;

public class Serializer {
	// Use XStream to serialize the stream

	static ObjectOutputStream outputStream = null;
	


	public static void storeObject(Object object, String outputfile) {		
		try {
			FileOutputStream fos= new FileOutputStream(outputfile);
			GZIPOutputStream gos = new  GZIPOutputStream(fos);
			outputStream = new ObjectOutputStream(gos);
			outputStream.writeObject(object);
			outputStream.flush();
            outputStream.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
