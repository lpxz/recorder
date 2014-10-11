package edu.hkust.leap.monitor;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.zip.GZIPOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
//
//
//import edu.hkust.leap.record.*;
//import edu.hkust.leap.record.generator.*;
//import edu.hkust.leap.record.utils.Util;
//import edu.hkust.leap.record.utils.Serializer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import AVdetect.eventnode.JoinThreadEvent;
import AVdetect.eventnode.LockingEvent;
import AVdetect.eventnode.NotifyingEvent;
import AVdetect.eventnode.SharedAccessEvent;
import AVdetect.eventnode.StartThreadEvent;
import AVdetect.eventnode.ThreadBeginEvent;
import AVdetect.eventnode.ThreadEndEvent;
import AVdetect.eventnode.UnlockingEvent;
import AVdetect.eventnode.WaitingEvent;
import AVdetect.graph.CausalGraph;
import AVdetect.manager.MemoryManager;





public class Copy_2_of_RecordMonitor {
	public static CausalGraph cg = new CausalGraph();
	
	
	
	public static HashMap o2stmt = new HashMap();	
	public static int readCount =0;
	public static int writeCount =0;	
	public static int VECARRAYSIZE = 100;
	public static boolean isCrashed = false;
	public static Throwable crashedException=null;
	public static HashMap<String,Long> threadNameToIdMap;		
	public static Vector[] accessVector;	
	public static ReentrantReadWriteLock[] rws ;
	
	
	public static void initialize(int size)
	{
		cg = new CausalGraph();
		
		VECARRAYSIZE = size;
		accessVector = new Vector[VECARRAYSIZE];
		rws= new ReentrantReadWriteLock[VECARRAYSIZE];
		for(int i=0;i<VECARRAYSIZE;i++)
		{
			accessVector[i] = new Vector<Long>();//new MyAccessVector();
			rws[i] = new ReentrantReadWriteLock();
			
		}
		threadNameToIdMap = new HashMap<String,Long>();
	}
    
	public static String mainClass;
	public static String methodname;
    public static String[] mainargs;

	private static String mainthreadname;
    

   
	public synchronized static void crashed(Throwable crashedException) 
	{
		
		isCrashed = true;
		
//		System.err.println("--- program crashed! ---");
//		System.err.println("--- preparing for reproducing the crash ... ");
//		String traceFile_ = saveMonitorData();
//		System.err.println("--- generating the test driver program ... ");
//		generateTestDriver(traceFile_);
//		
		
		System.exit(-1);
	}
	
	public synchronized static long getNanoTime()
	{
		long nt = System.nanoTime();
		System.out.println("Nano Time: "+nt);
		return nt;
	}
	
//	public synchronized static void startThreadBefore() // not used in instrumentation
//	{ 
//		System.err.println("startThreadBefore");
//	}
	public static void mainThreadStartRun(long threadId,String methodName, String[] args)
	{
		ThreadBeginEvent e = new ThreadBeginEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(Thread.currentThread()));
        cg.addVertex_complete(e); 
        
		mainthreadname = Thread.currentThread().getName();		
		threadNameToIdMap.put("leap-main",threadId);//Thread.currentThread().getName()
		mainargs = args;
		methodname = methodName;
		mainClass= methodName.substring(0, methodName.length()-5);// .main
		
	}
	
	public static void mainThreadStopRun(long threadId,String methodName, String[] args)
	{
		
//		ThreadEndEvent e = new ThreadEndEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(Thread.currentThread()));
//        cg.addVertex_complete(e); 	
	}
	
    public synchronized static void startRunThreadBefore(Thread t, long threadId)
    {	
//    	StartThreadEvent e = new StartThreadEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(t));
//        cg.addVertex_complete(e); 
    }
	public synchronized static void threadStartRun(long threadId)
	{
		
//		ThreadBeginEvent e = new ThreadBeginEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(Thread.currentThread()));
//        cg.addVertex_complete(e); 
		// give a fixed name please, otherwise the naming system would meet with a hazzard.
		threadNameToIdMap.put(Thread.currentThread().getName(),threadId);
	}
	// this is not injected originally
	public synchronized static void threadExitRun(long threadId)
	{
		
//		ThreadEndEvent e = new ThreadEndEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(Thread.currentThread()));
//        cg.addVertex_complete(e); 
	}
    public synchronized static void joinRunThreadAfter(Thread t,long threadId)
    {
//    	JoinThreadEvent e = new JoinThreadEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(t));
//        cg.addVertex_complete(e); 
    }


   
    public synchronized static void waitAfter(Object o, int index, long tid)
    {	
//    	WaitingEvent e = new WaitingEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o));
//        cg.addVertex_complete(e);  
    	
    }
    public synchronized static void notifyBefore(Object o, int index, long tid)
    {	
//    	NotifyingEvent e = new NotifyingEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o));
//        cg.addVertex_complete(e);  
    }
    public synchronized static void notifyAllBefore(Object o, int index, long tid)
    {	
//    	NotifyingEvent e = new NotifyingEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o));
//        cg.addVertex_complete(e);  
    	//accessSPE(index,tid);
    }

    
    public static void enterMonitorAfter(Object o, int iid,long id) {
//        LockingEvent e = new LockingEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o));
//        cg.addVertex_complete(e);    	
    }
    public static void exitMonitorBefore(Object o,int iid,long id) {
//        UnlockingEvent e = new UnlockingEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o));
//        cg.addVertex_complete(e);   
    }
    
   
    
    public static void readBeforeArrayElem(Object o, int index, long tid) {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o, index));
//        cg.addVertex_complete(e); 
    	System.err.println("xxx");
        accessSPE(index,tid, true);			
   }

    public static void writeBeforeArrayElem(Object o, int index, long tid)
    {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o, index));
//        cg.addVertex_complete(e); 
    	System.err.println("xxx");
    	accessSPE(index,tid, false);
    }
    
    public static void readBeforeInstance(Object o, int index, long tid) {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o, index));
//        cg.addVertex_complete(e); 
        
        
        accessSPE(index,tid,true);			
   }

    public static void writeBeforeInstance(Object o, int index, long tid)
    {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o, index));
//        cg.addVertex_complete(e); 
    	
    	accessSPE(index,tid, false);
    }
    
    public static void readBeforeFakedInstance(Object o, int index, long tid) {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o, index));
//        cg.addVertex_complete(e); 
        
        
        accessSPE(index,tid, true);			
   }

    public static void writeBeforeFakedInstance(Object o, int index, long tid)
    {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(o, index));
//        cg.addVertex_complete(e); 
        
        
    	accessSPE(index,tid, false);
    }
    public static void readBeforeStatic(int index,long tid) {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID(index));
//        cg.addVertex_complete(e); 
    	accessSPE(index,tid, true);
    	
    }
    public static void writeBeforeStatic(int index,long tid) {
//    	SharedAccessEvent e = new SharedAccessEvent(Thread.currentThread().getId(), cg.getMemoryManager().getMemID( index));
//        cg.addVertex_complete(e); 
        
        
    	accessSPE(index,tid,false);
    }
    
	public static void accessSPE(int index,long threadId, boolean read) {
		//GO TO THE RANDOM BUG INJECTION LIBRARY FIRST
//		String threadname = Thread.currentThread().getName();
//		if(mainthreadname.equals(threadname))
//			threadname = Parameters.MAIN_THREAD_NAME;
//		
//		index = edu.hkust.leap.random.RandomAccess.getRandomSPEIndex(threadname);
		
		
		if(read) readCount ++;
		else {
			writeCount ++;
		}
//        ReentrantReadWriteLock rw =rws[index];
//        Lock l = null;
//        if(read)
//        {
//        	l=rw.readLock();
//        }
//        else {
//			l=rw.writeLock();
//		}
//        l.lock();
		accessVector[index].add(threadId);
//		l.unlock();
		
//		int type = edu.hkust.leap.random.RandomAccess.getRandomAccessType(threadname);
//		Verifier.check(index,type,threadId);
	}
    
}
