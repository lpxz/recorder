package edu.hkust.leap.record.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class Util {

	public static String SRC_DIR ="src";
	public static String MAIN_THREAD_NAME = "leap-main";
	public static String getReplayDriverDirectory() 
	{
		String tempdir = System.getProperty("user.dir");
		tempdir=tempdir.replace("recorder", "replayer");
		
		if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
			tempdir = tempdir + System.getProperty("file.separator");
		}
		tempdir = tempdir+Util.SRC_DIR+System.getProperty("file.separator");
		
		File tempFile = new File(tempdir);
		if(!(tempFile.exists()))
			tempFile.mkdir();
			
		tempdir = tempdir+System.getProperty("file.separator");
		return tempdir;
	}
	public static String getOrderDataDirectory() 
	{
		String tempdir = System.getProperty("user.dir");
//		tempdir=tempdir.replace("recorder", "replayer");
		
//		if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
//			tempdir = tempdir + System.getProperty("file.separator");
//		}		
		
			
		tempdir = tempdir+System.getProperty("file.separator")+
		"OrderData"+ System.getProperty("file.separator");
		File tempFile = new File(tempdir);
		if(!(tempFile.exists()))
			tempFile.mkdir();
		
		return tempdir;
	}
    
	public static String getRecordArgFile() 
	{
		String tempdir = System.getProperty("user.dir");
				
		tempdir = tempdir+System.getProperty("file.separator");
		tempdir+= "leap.recorder.arg";
		return tempdir;
	}





    public static String writeArgLine(String argfileName , String towrite)
    {
    	String toret  = "";
		try{
		    // Open the file that is the first 
		    // command line parameter
			FileOutputStream fstream = new FileOutputStream(argfileName);
		    // Get the object of DataInputStream
			DataOutputStream in = new DataOutputStream(fstream);
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(in));
            
            br.write(towrite);
			br.flush();
			
		    //Close the input stream
		    in.close();
		    }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		    }
		    return toret;
    	
    }
    
    

    public static String getArgLine(String argfileName )
    {
    	String toret  = "";
		try{
		    // Open the file that is the first 
		    // command line parameter
		    FileInputStream fstream = new FileInputStream(argfileName);
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		    	if(strLine.startsWith("#"))
		    	{continue;}
		    	else if(strLine.isEmpty())
		    	{
		    		continue;
		    	}
		    	else
		    	{
		    		toret= strLine;
		    		break;
		    	}
		     // System.out.println (strLine);
		    }
		    //Close the input stream
		    in.close();
		    }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		    }
		    return toret;
    	
    }
    //
    private static Properties props;

    public static void loadProperties() {
         props = new Properties();
         String tempdir = System.getProperty("user.dir"); 		
		tempdir=tempdir.replace("recorder", "transformer");
 		tempdir = tempdir+System.getProperty("file.separator");
 		tempdir+= "leap.property";
         try {
              props.load(new FileInputStream(tempdir));
         } catch (Exception e) {
              e.printStackTrace();
         }
    }
    public static String getConfig(String key) {
        return props.getProperty(key);
    }

    public static void main(String[] args)
    {
         loadProperties();
         System.out.println(getConfig("vector"));
    }

    


}
