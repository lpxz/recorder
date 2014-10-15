package edu.hkust.leap.record;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;



import edu.hkust.leap.monitor.RecordMonitor;
import edu.hkust.leap.record.utils.Util;

public class RecordMain {

	// Note that to change the buildpath!!
	// NOTE to change the CrashTestCaseGenerator. invoke main(): LP version
	public static void main(String[] args) {
//		String argfileName= Util.getRecordArgFile();
//		String argline= Util.getArgLine(argfileName);
//		String[]  arglineItems = argline.split(" ");
		

		
		List<String>  arg = new LinkedList(Arrays.asList(args));
		int len = arg.size();
		if(len==0)
		{
			System.err.println("please specify SPE size, the main class, and parameters... ");
		}
		else 
		{
			process(arg);
		}
	}
			
	private static void process(List<String> args)
	{
		int index=0;

			RecordMonitor.initialize(Integer.valueOf(args.get(0)));
			run(args.subList(++index, args.size()));
	}
	

	private static void run(List<String> args)
	{
		try 
		{
			MonitorThread monThread = new MonitorThread();
			Runtime.getRuntime().addShutdownHook(monThread);
			String[] mainArgs = {};
			// format: 1 appclass appclass_main_argments
		
			String appname = args.get(0);
			Class<?> c = Class.forName(appname);
		    Class[] argTypes = new Class[] { String[].class };
		    Method main = c.getDeclaredMethod("main", argTypes);
		    if(!main.isAccessible())
		    {
		    	main.setAccessible(true);
		    }
		   

		    if(args.size()>1)
		    {
		    	mainArgs = new String[args.size()-1];
		    	for(int k=0;k<args.size()-1;k++)
		    		mainArgs[k] = args.get(k+1);
		    }
		    
		    main.invoke(null, (Object)mainArgs);
		  // analysis here:
		    
		    	    

		    
			// production code should handle these exceptions more gracefully
			} catch (Exception x) {            
			    x.printStackTrace();
			}
	}

}
