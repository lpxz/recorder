package edu.hkust.leap.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import edu.hkust.leap.monitor.RecordMonitor;
import edu.hkust.leap.record.generator.CrashTestCaseGenerator;
import edu.hkust.leap.record.utils.Serializer;
import edu.hkust.leap.record.utils.Util;

public class MonitorThread extends Thread
{
	
	MonitorThread()
	{
		super("MonitorThread");
	}
	public void run()
	{

		if(RecordMonitor.isCrashed)
		{
			System.err.println("--- program crashed! ---");
			System.err.println("--- preparing for reproducing the crash ... ");
			String traceFile_ = saveMonitorData();
			System.err.println("--- generating the test driver program ... ");
			generateTestDriver(traceFile_);
		}
		else
		{
			generateTestDriver(saveMonitorData());
		}
	   
		
	    
	    
	}
	
//	public void ARflipPractice()
//	{
//		 Set<ARPair> arps = AtomicRegionManager.findARs(RecordMonitor.cg);
//	    for (ARPair arPair : arps) 	    	
//    	{  		 
//    		 CausalGraphFlipping.reverseInnerEdgesLocally(RecordMonitor.cg, arPair);
//    		 RecordMonitor.cg.exportCausalGraph("/home/lpxz/eclipse/workspace/leap/recorder/test.dot");
//			 CausalGraphFlipping.reverseInnerEdgesBackLocally(RecordMonitor.cg, arPair);			
//    	}
//	}
	


//	public void sharedMemStatistic()
//	{// shared variables
//		   List<CausalEdge> edges =CausalGraphTraversal.getSharedAccessEdges(RecordMonitor.cg);
//		   Set sharedMems = new HashSet();
//		   for(CausalEdge edge:edges)
//		   {
//			   CriticalEvent sourcEvent = (CriticalEvent)edge.getSource();
////			   CriticalEvent tgtEvent = (CriticalEvent)edge.getTarget();
//			   sharedMems.add(sourcEvent.getMem());
//			  
//		   }
//		   System.err.println("shared memory NO.:" + sharedMems.size());
//	}
//	
//	public void lockFlipPractice()
//	{
//		List<CausalEdge> lockEdges =CausalGraphTraversal.getLockingEdges(RecordMonitor.cg);
//        for(CausalEdge edge : lockEdges)
//        {
//        	RecordMonitor.cg.exportCausalGraph("/home/lpxz/eclipse/workspace/leap/recorder/test1.dot");
//          LockingCausalEdge flipped= CausalGraphFlipping.flipLockEdge(RecordMonitor.cg, (LockingCausalEdge)edge);
//            RecordMonitor.cg.exportCausalGraph("/home/lpxz/eclipse/workspace/leap/recorder/test2.dot");
//            CausalGraphFlipping.flipLockEdgeBack(RecordMonitor.cg, flipped);
//        }
//	}
//	
	
	 public static String saveMonitorData()
	    {
	    	String traceFile_=null;
			File traceFile_monitordata = null;
			File traceFile_threadNameToIdMap= null;
//			File traceFile_nanoTimeDataVec = null;
//			File traceFile_nanoTimeThreadVec = null;
//			
			OutputStreamWriter fw_monitordata;
			OutputStreamWriter fw_threadNameToIdMap;
//			OutputStreamWriter fw_nanoTimeDataVec;
//			OutputStreamWriter fw_nanoTimeThreadVec;
//			
			//SAVE Runtime Information
			try 
			{
				traceFile_monitordata = File.createTempFile("Leap", "_accessVector.trace.gz", new File(
						Util.getOrderDataDirectory()));
				
				String traceFileName = traceFile_monitordata.getAbsolutePath();
				int index  =traceFileName.indexOf("_accessVector");
				traceFile_ = traceFileName.substring(0, index);
				
				traceFile_threadNameToIdMap = new File(traceFile_+"_threadNameToIdMap.trace.gz");
				String traceMapFileName = traceFile_threadNameToIdMap.getAbsolutePath();

//				
				
				assert (traceFile_monitordata != null && traceFile_threadNameToIdMap != null);



//				
				Serializer.storeObject(RecordMonitor.accessVector, traceFileName);
				Serializer.storeObject(RecordMonitor.threadNameToIdMap, traceMapFileName);

//				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return traceFile_;
	    }
	    public static void generateTestDriver(String traceFile_)
	    {
			//GENERATE Test Driver		
			try {
				CrashTestCaseGenerator.main(new String[] { traceFile_,
						Util.getReplayDriverDirectory() });
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
}	
