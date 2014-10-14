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
import java.util.concurrent.ConcurrentHashMap;
//
//
//import edu.hkust.leap.record.*;
//import edu.hkust.leap.record.generator.*;
//import edu.hkust.leap.record.utils.Util;
//import edu.hkust.leap.record.utils.Serializer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;





public class RecordMonitor {
	//28470
	public static boolean leap= false;//1163
	public static boolean stride= false;
	
	public static boolean myBasic= true;//  752
	public static boolean opt_obj_sensitivity = false;
//	public static boolean opt_Reads_of_same_write=false; 
	
	
	public static int readCount =0;
	public static int writeCount =0;
	
	public static int accessedLocSize = 1000;
	
	public static int threadSize = 30;
	
	
	
	public static boolean isCrashed = false;
	public static Throwable crashedException=null;
	public static HashMap<String,Long> threadNameToIdMap;		
	public static Vector[] accessVectorGroup;	
	
	public static Vector[] perThreadGroup;	
	
	public static long[] instCounterGroup;	
	
//	public static long[] latestWritesTid;	//latest write's thread, lastest write's inst counter.
	public static long[] latestWritesInstCounter;
	public static Object[] locks4latestWrites;	//latest write's thread, lastest write's inst counter.
	
	public static long[][] writeTIDOfLastReadOfAccess;// first represent the TID index, second represents the access index.	
	public static long[][] writeCounterOfLastReadOfAccess;// first represent the TID index, second represents the access index.	
	
	
	
	public static ConcurrentHashMap mapping = new ConcurrentHashMap();
	public static Vector[] mappingsGroup;	
	
	public static ReentrantReadWriteLock[] rws ;
	public static void initialize(int size)
	{
		
		accessedLocSize = size;
		accessVectorGroup = new Vector[accessedLocSize];
		
		for(int i=0;i<accessedLocSize;i++)
		{
			accessVectorGroup[i] = new Vector<Long>();//new MyAccessVector();
			
		}
		
		
		perThreadGroup = new Vector[threadSize];// assume 100 threads maximally
		
		for(int i=0;i<threadSize;i++)
		{
			perThreadGroup[i] = new Vector<Long>();//new MyAccessVector();
			
		}
		
		instCounterGroup =new long[threadSize];
		for(int i=0;i<threadSize;i++)
		{
			instCounterGroup[i] = ((long)i)<<59;	// initialize		
		}
		
		
//		latestWritesTid = new long[accessedLocSize];
//		for(int i=0;i<accessedLocSize;i++)
//		{
//			latestWritesTid[i] = -1;	// initialize		
//		}
		
		latestWritesInstCounter = new long[accessedLocSize];
		for(int i=0;i<accessedLocSize;i++)
		{
			latestWritesInstCounter[i] = -1;	// initialize		
		}
		
		
		locks4latestWrites = new Object[accessedLocSize];
		for(int i=0;i<accessedLocSize;i++)
		{
			locks4latestWrites[i] = ""+i;
		}
		
		
        mappingsGroup = new Vector[accessedLocSize];// assume 100 threads maximally
		
		for(int i=0;i<accessedLocSize;i++)
		{
			mappingsGroup[i] = new Vector<Long>();//new MyAccessVector();
			
		}
		
		
		writeTIDOfLastReadOfAccess= new long[threadSize][accessedLocSize];
		for(int i=0 ; i< threadSize; i++)
		{
			for(int j=0; j< accessedLocSize; j++)
			{
				writeTIDOfLastReadOfAccess[i][j]=-1;
			}
		}
		
		writeCounterOfLastReadOfAccess= new long[threadSize][accessedLocSize];
		for(int i=0 ; i< threadSize; i++)
		{
			for(int j=0; j< accessedLocSize; j++)
			{
				writeCounterOfLastReadOfAccess[i][j]=-1;
			}
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
	
	
	public static void mainThreadStopRun(long threadId,String methodName, String[] args)
	{}
	

	//edu.hkust.leap.monitor.RecordMonitor.mainThreadStopRun(JLjava/lang/String;[Ljava/lang/String;)V
	//deprecated 
//    public static void readBeforeArrayElem(Object o, int index, long tid) {
//        accessSPE(index,tid, true);			
//   }
//
//    public static void writeBeforeArrayElem(Object o, int index, long tid)
//    {
//    	accessSPE(index,tid, false);
//    }
//    
//    public static void readBeforeInstance(Object o, int index, long tid) {
//        accessSPE(index,tid,true);			
//   }
//
//    public static void writeBeforeInstance(Object o, int index, long tid)
//    {
//    	
//    	accessSPE(index,tid, false);
//    }
//    
//    public static void readBeforeStatic(int index,long tid) {
//
//    	accessSPE(index,tid, true);
//    	
//    }
//    public static void writeBeforeStatic(int index,long tid) {
//
//    	accessSPE(index,tid,false);
//    }
    
    
	
	
	
	
	
	  public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,boolean value) {		
		  int objsensIndex = -1;
		  if(opt_obj_sensitivity){
		  objsensIndex=BitLibrary.compute( System.identityHashCode(o), arrayindex);
		  }
		  
		  accessSPE(iid,id, true, objsensIndex);		
		  if(stride){
			    perThreadGroup[(int)id].add(value);// value
				perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
		  }			
	  }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, boolean value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false,  objsensIndex);		
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,byte value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, byte value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(System.identityHashCode(o), arrayindex);
			  }
	    	 
				  accessSPE(iid,id, false, objsensIndex);		
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,char value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, char value) {
	    	int objsensIndex =   -1;
			  if(opt_obj_sensitivity){
				  objsensIndex=BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);		
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,double value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);		
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, double value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);		
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,float value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, float value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);		
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,int value) {		
	    	int objsensIndex = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex = BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, int value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);	
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,long value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, long value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);	
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,short value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, short value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);	
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,Object value) {		
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,  Object value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), arrayindex);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);	
	    }
	    
	    
	  
	    // deprecated
//	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO) {		
//	    }
//	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO) {
//	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,boolean value) {		
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true,objsensIndex );
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, boolean value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,byte value) {		
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, byte value) {
	    	int objsensIndex = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex = BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,char value) {		
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, char value) {
	    	int objsensIndex = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex = BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,double value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, double value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,float value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, float value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,int value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, int value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,long value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, long value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,short value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, short value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,Object value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, Object value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    
	    
	    //deprecated
//	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO) {		
//	    }
//	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO) {
//	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,boolean value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,boolean value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,byte value) {		
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,byte value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,char value) {		
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,char value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,double value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }  
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,double value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,float value) {	
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,float value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,int value) {		
	    	int objsensIndex = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex = BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,int value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,long value) {		
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,long value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,short value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,short value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,Object value) {	
	    	int objsensIndex = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex = BitLibrary.compute(0,  iid);
			  }
	    	 accessSPE(iid,id, true, objsensIndex);	
	    	 if(stride){
				    perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			  }		
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,Object value) {
	    	int objsensIndex  = -1;
			  if(opt_obj_sensitivity){
				  objsensIndex= BitLibrary.compute(0,  iid);
			  }
	   	 accessSPE(iid,id, false, objsensIndex);
	    }
	    
	    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    //deleted
//    public static void readBeforeFakedInstance(Object o, int index, long tid) {
//        accessSPE(index,tid, true);			
//   }
//
//    public static void writeBeforeFakedInstance(Object o, int index, long tid)
//    {
//    	accessSPE(index,tid, false);
//    }

	    public static int rCount = 0;
	    public static int wCount = 0;
	    public static boolean fakedShared = true;
	public static void accessSPE(int index,long threadId, boolean read, int objSensIndex) {
//		System.out.println(threadId);
        

        
		if(leap){
			
			synchronized (accessVectorGroup[index]) {
				
	        	accessVectorGroup[index].add(threadId);
			}
		}else if(stride){
			if(!read){
				synchronized (accessVectorGroup[index]) {
		        	accessVectorGroup[index].add(threadId);
				}				
			}else {
				// record <value, version> per thread.
				// we record them consequentially, avoiding creating new objects.
				// put it into the call site.
			}
			
			
		}else if(myBasic){
			if(opt_obj_sensitivity)
			{ index = objSensIndex; }
			
			
			long instCounter =incInsCounter(threadId);
			
			long oldLatestInstCounter=-1;
			
			
			if(!read)
			{		


				
			    synchronized (locks4latestWrites[index])
			    {
					oldLatestInstCounter= latestWritesInstCounter[index];					
					latestWritesInstCounter[index] = instCounter;	
				}	
			    // store the relation: latest write -> current write, if they belong to different threads.
			    long oldLatestTID =oldLatestInstCounter>>59;
			    if( oldLatestTID!=threadId)
			    {	
			    	rCount++;
					synchronized (mappingsGroup[index]) {
						mappingsGroup[index].add(oldLatestInstCounter);						
						mappingsGroup[index].add(instCounter);
					}
				}
			    else {
					wCount++;
				}
			 }
			else//	if(read)
			{
				long oldLatestTID =-1;
				for(;;){
					oldLatestInstCounter= latestWritesInstCounter[index];
					oldLatestTID = oldLatestInstCounter >>59;
				
					boolean haha= fakedShared;
					
					
					if(((latestWritesInstCounter[index])>>59)==oldLatestTID && latestWritesInstCounter[index]==oldLatestInstCounter)
					{
						break;
					}
					// else loop back.
				}				
				// store the relation: latest write -> current read, if they belong to different threads.
				//opt_Reads_of_same_write&&
				if( writeCounterOfLastReadOfAccess[(int)threadId][index]==oldLatestInstCounter )
				{
					// last read (local) and this read read from same write. 
				}
				else 
				{
					if(oldLatestTID!=threadId)//write and read from same thread.
					{
						synchronized (mappingsGroup[index]) {
							mappingsGroup[index].add(oldLatestInstCounter);						
							mappingsGroup[index].add(instCounter);
						}
						
//						if(opt_Reads_of_same_write)
						{
						   writeTIDOfLastReadOfAccess[(int)threadId][index]=oldLatestTID ;
						   writeCounterOfLastReadOfAccess[(int)threadId][index]=oldLatestInstCounter ;
						}		
					}
				}
			}
			
		}else {
			// future
		}
   	}
    /**
	 * @param index
	 * @param threadId
	 * @param read
	 */
	




	/**
	 * @param threadId
	 */
	private static long incInsCounter(long threadId) {
		return ++instCounterGroup[(int)threadId];		
	}
	
//	private static long getInsCounter(long threadId) {
//		return instCounterGroup[(int)threadId];		
//	}

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
    	int objsensIndex  = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex= BitLibrary.compute(0,  iid);
		  }
    	 accessSPE(iid,id, false, objsensIndex);
    }
    public static void exitMonitorBefore(int iid,long id) {
    	int objsensIndex  = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex= BitLibrary.compute(0,  iid);
		  }
    	 accessSPE(iid,id, false, objsensIndex);
    }
    public static void enterMonitorBefore( int iid,long id) {
    	int objsensIndex  = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex= BitLibrary.compute(0,  iid);
		  }
    	 accessSPE(iid,id, false,objsensIndex);
    }
    public static void exitMonitorAfter(int iid,long id) {
    	int objsensIndex  = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex= BitLibrary.compute(0,  iid);
		  }
    	 accessSPE(iid,id, false, objsensIndex);
    }
    public static void enterMonitorBefore(Object o, int iid,long id) {
    	int objsensIndex  = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
		  }
    	 accessSPE(iid,id, false, objsensIndex);
    }
    public static void enterMonitorAfter(Object o, int iid,long id) {
    	int objsensIndex = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex = BitLibrary.compute( System.identityHashCode(o), iid);
		  }
    	 accessSPE(iid,id, false, objsensIndex);
    }
    public static void exitMonitorBefore(Object o,int iid,long id) {
    	int objsensIndex  = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
		  }
    	 accessSPE(iid,id, false, objsensIndex);
    }
    public static void exitMonitorAfter(Object o,int iid,long id) {
    	int objsensIndex  = -1;
		  if(opt_obj_sensitivity){
			  objsensIndex= BitLibrary.compute( System.identityHashCode(o), iid);
		  }
    	 accessSPE(iid,id, false, objsensIndex);
    }
    
    
}
