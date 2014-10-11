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





public class CopyOfRecordMonitor {
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
	
	public synchronized static void startThreadBefore()
	{ 
	}
	public static void threadStartRun(long threadId)
	{
		// give a fixed name please, otherwise the naming system would meet with a hazzard.
		threadNameToIdMap.put(Thread.currentThread().getName(),threadId);
	}
	public synchronized static void threadExitRun(long threadId)
	{
		
	}
    public synchronized static void startRunThreadBefore(Thread t, long threadId)
    {	
    	
    }
    public synchronized static void joinRunThreadAfter(Thread t,long threadId)
    {
    	
    }
	public static void mainThreadStartRun(long threadId,String methodName, String[] args)
	{
		mainthreadname = Thread.currentThread().getName();		
		threadNameToIdMap.put("leap-main",threadId);//Thread.currentThread().getName()
		mainargs = args;
		methodname = methodName;
		mainClass= methodName.substring(0, methodName.length()-5);// .main
	}

    public static void readBeforeArrayElem(Object o, int index, long tid) {
        accessSPE(index,tid, true);			
   }

    public static void writeBeforeArrayElem(Object o, int index, long tid)
    {
    	accessSPE(index,tid, false);
    }
    
    public static void readBeforeInstance(Object o, int index, long tid) {
        accessSPE(index,tid,true);			
   }

    public static void writeBeforeInstance(Object o, int index, long tid)
    {
    	
    	accessSPE(index,tid, false);
    }
    
    public static void readBeforeFakedInstance(Object o, int index, long tid) {
        accessSPE(index,tid, true);			
   }

    public static void writeBeforeFakedInstance(Object o, int index, long tid)
    {
    	accessSPE(index,tid, false);
    }
    public static void readBeforeStatic(int index,long tid) {

    	accessSPE(index,tid, true);
    	
    }
    public static void writeBeforeStatic(int index,long tid) {

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
    public synchronized static void waitAfter(Object o, int index, long tid)
    {	
    	//accessSPE(index,tid);
    }
    public synchronized static void notifyBefore(Object o, int index, long tid)
    {	
    	//accessSPE(index,tid);
    }
    public synchronized static void notifyAllBefore(Object o, int index, long tid)
    {	
    	//accessSPE(index,tid);
    }
    public static void enterMonitorAfter( int iid,long id) {
    }
    public static void exitMonitorBefore(int iid,long id) {
    }
    public static void enterMonitorBefore( int iid,long id) {
    }
    public static void exitMonitorAfter(int iid,long id) {
    }
    public static void enterMonitorBefore(Object o, int iid,long id) {
    }
    public static void enterMonitorAfter(Object o, int iid,long id) {
    }
    public static void exitMonitorBefore(Object o,int iid,long id) {
    }
    public static void exitMonitorAfter(Object o,int iid,long id) {
    }
    
    
}
