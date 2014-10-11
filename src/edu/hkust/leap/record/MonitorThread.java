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

import AVdetect.edge.LockingCausalEdge;
import AVdetect.edge.abstractclass.CausalEdge;
import AVdetect.eventnode.LockingEvent;
import AVdetect.eventnode.SharedAccessEvent;
import AVdetect.eventnode.UnlockingEvent;
import AVdetect.eventnode.abstractclass.CriticalEvent;
import AVdetect.eventnode.abstractclass.LockReleEvent;
import AVdetect.graph.ARPair;
import AVdetect.graph.CausalGraphFlipping;
import AVdetect.graph.CausalGraphTraversal;
import AVdetect.manager.AtomicRegionManager;
import AVdetect.manager.ThreadManager;

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
	   
		edgeStatistic();
	  
	    Set<ARPair> arps = AtomicRegionManager.findARs(RecordMonitor.cg);
	   System.err.println("first run:");
	    boolean mark = true;
	    for (ARPair arPair : arps) 	
	    {
	    	if(mark)
	    	{
	    		CausalGraphTraversal.smartCycleDetection(RecordMonitor.cg,arPair );
	    		mark = false;
	    	}	    	
	    }
	    
//	    
	//	CausalGraphFlipping.flippable(RecordMonitor.cg);
	    
	    // task3:
	    
	    
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
	
	public void edgeStatistic()
	{
		int lo=  CausalGraphTraversal.getLockingEdges(RecordMonitor.cg).size();
		   int com = CausalGraphTraversal.getCommunicationEdges(RecordMonitor.cg).size();
		   int sha = CausalGraphTraversal.getSharedAccessEdges(RecordMonitor.cg).size();
		   int local = CausalGraphTraversal.getLocalEdges(RecordMonitor.cg).size();
		   int total = RecordMonitor.cg.coreG.edgeSet().size();
		   if(total != lo + com + sha + local)
		   {
			   System.err.println("waht is up!!");
			   System.err.println("lo:" +lo);
			   System.err.println("com:" +com);
			   System.err.println("sha:" +sha);
			   System.err.println("local:" +local);
			   System.err.println("total:" +total);
		   }
		   else {
			   System.err.println("lo:" +lo);
			   System.err.println("com:" +com);
			   System.err.println("sha:" +sha);
			   System.err.println("local:" +local);
			   System.err.println("total:" +total);
		   }
	}

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
