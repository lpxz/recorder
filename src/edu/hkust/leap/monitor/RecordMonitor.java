package edu.hkust.leap.monitor;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.zip.GZIPOutputStream;
import gnu.trove.list.linked.TLongLinkedList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
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




// 30115, 
public class RecordMonitor {
	
	/*              options:               */
	//28470
	public static boolean leap= false;//1163
	public static boolean stride= false;
	
	public static boolean myBasic= true;//  // no need for sync!
	public static boolean opt_reduce_write_seq = true;
	public static boolean opt_obj_sensitivity = true;
	
	public static boolean opt_reduce__local_read_seq_of_same_write=true; 
	public static boolean opt_reduce_read_of_local_write=true; 
	
	public static boolean opt_avoid_autoboxing= true;
	
	
	/*              constants               */
	private static final int COUNTER_BIT_SIZE = 48;
	private static final long MAGIC_NUMBER = ((long)1)<<COUNTER_BIT_SIZE;
	private static final int PARTITIONCOUNT = 1024;
	public static int readCount =0;
	public static int writeCount =0;
	public static int accessedLocSize = 1024;	
	public static int threadSize = 30;
	
	
	
	
	
	
	/*              data structures:               */
	public static boolean isCrashed = false;
	public static Throwable crashedException=null;
	public static HashMap<String,Long> threadNameToIdMap;		
	public static Vector[] accessVectorGroup;	// leap should apply sync(){} to enclose the access, rather than using the special sync provided by vector.
	
	public static Vector[] perThreadGroup;	// for stride, arraylist is not synchronized, vector is.
	// why does it report crash when we change vector to arraylist (without stride being executed)
	
	public static long[] instCounterGroup;	
//	public static long[] latestWritesTid;	//latest write's thread, lastest write's inst counter.
	public static long[] latestWritesInstCounter;
	public static Object[] locks4latestWrites;	//latest write's thread, lastest write's inst counter.
	
//	public static long[][] writeTIDOfLastReadOfAccess;// first represent the TID index, second represents the access index.	
	public static long[][] counterOfLastReadsWrite;// first represent the TID index, second represents the access index.	
    public static LinkedHashMap[][] myAccessVectorGroup;	
    public static TLongLinkedList[][] myAccessVectorGroup_Key;	
    public static TLongLinkedList[][] myAccessVectorGroup_Value;	
	
	
	
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
			instCounterGroup[i] = ((long)i)<<COUNTER_BIT_SIZE;	// initialize		
		}
		

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
		
		myAccessVectorGroup = new LinkedHashMap[threadSize][accessedLocSize];// assume 100 threads maximally
		 for(int i=0 ; i< threadSize; i++)
			{
				for(int j=0; j< accessedLocSize; j++)
				{
					myAccessVectorGroup[i][j]= new LinkedHashMap();
				}
			}
		 
		 
		myAccessVectorGroup_Key = new TLongLinkedList[threadSize][accessedLocSize];// assume 100 threads maximally
		 for(int i=0 ; i< threadSize; i++)
			{
				for(int j=0; j< accessedLocSize; j++)
				{
					myAccessVectorGroup_Key[i][j]= new TLongLinkedList();
				}
			}
		
		 
			myAccessVectorGroup_Value = new TLongLinkedList[threadSize][accessedLocSize];// assume 100 threads maximally
			 for(int i=0 ; i< threadSize; i++)
				{
					for(int j=0; j< accessedLocSize; j++)
					{
						myAccessVectorGroup_Value[i][j]= new TLongLinkedList();
					}
				}
			
       
		
		
		
		counterOfLastReadsWrite= new long[threadSize][accessedLocSize];
		for(int i=0 ; i< threadSize; i++)
		{
			for(int j=0; j< accessedLocSize; j++)
			{
				counterOfLastReadsWrite[i][j]=-1;
			}
		}
		
		
		
		
		threadNameToIdMap = new HashMap<String,Long>();
		start = System.currentTimeMillis();
		
	}
	
	public static long start = -1;
    
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
		  if(leap){			
				synchronized (accessVectorGroup[iid]) 
				{
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					perThreadGroup[(int)id].add(value);// value
					perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
			}else if(myBasic){
   			        accessSPE_array_index(iid,id, true, o, arrayindex);	
			}else {
				// future
			} 		
	  }
	  
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, boolean value) {
	    	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				accessSPE_array_index(iid,id, false, o, arrayindex);	
			}else {
				// future
			} 			
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,byte value) {	
	    	   if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);	
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, byte value) {
	    	
				  
				  if(leap){			
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}
					}else if(stride){
							synchronized (accessVectorGroup[iid]) {
								accessVectorGroup[iid].add(id);
							}				
					}else if(myBasic){
						  accessSPE_array_index(iid,id, false, o, arrayindex);		
					}else {
						// future
					} 		
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,char value) {	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);	
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, char value) {

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_array_index(iid,id, false, o, arrayindex);		
				}else {
					// future
				} 	
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,double value) {	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);	
				}else {
					// future
				} 			    	 
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, double value) {

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_array_index(iid,id, false, o, arrayindex);		
				}else {
					// future
				} 	
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,float value) {	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);	
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, float value) {

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_array_index(iid,id, false, o, arrayindex);		
				}else {
					// future
				} 	
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,int value) {		
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, int value) {

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_array_index(iid,id, false, o, arrayindex);			
				}else {
					// future
				} 
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,long value) {
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, long value) {
	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_array_index(iid,id, false, o, arrayindex);			
				}else {
					// future
				} 
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,short value) {	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);	
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex, short value) {

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_array_index(iid,id, false, o, arrayindex);			
				}else {
					// future
				} 
	    }
	    
	    public static void readBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,Object value) {		
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_array_index(iid,id, true, o, arrayindex);	
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeArrayElem(Object o, int iid,long id, String classname, int lineNO, int arrayindex,  Object value) {

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_array_index(iid,id, false, o, arrayindex);			
				}else {
					// future
				} 
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	  
	    // deprecated
//	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO) {		
//	    }
//	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO) {
//	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,boolean value) {		
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
			    	 accessSPE_object_field(iid,id, true,o, iid );
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, boolean value) {

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						synchronized (accessVectorGroup[iid]) {
							accessVectorGroup[iid].add(id);
						}				
				}else if(myBasic){
					  accessSPE_object_field(iid,id, false, o, iid);			
				}else {
					// future
				} 
	    }
	    
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,byte value) {		
	    	
	    	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o, iid);	
				}else {
					// future
				} 		
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, byte value) {

	   	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			  accessSPE_object_field(iid,id, false, o, iid);			
		}else {
			// future
		} 
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,char value) {		
	    	
	    	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o,iid);
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, char value) {

		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_object_field(iid,id, false, o, iid);			
			}else {
				// future
			} 
	    }
	    
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,double value) {	
	       	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o, iid);
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, double value) {

	   	 
		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_object_field(iid,id, false, o,iid);		
			}else {
				// future
			} 
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,float value) {	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o,iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, float value) {

		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_object_field(iid,id, false, o, iid);		
			}else {
				// future
			} 
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,int value) {
	    	
	    	 
	    	 
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o, iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, int value) {

		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_object_field(iid,id, false, o,iid);	
			}else {
				// future
			} 
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,long value) {
	    	

	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o,iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, long value) {

		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_object_field(iid,id, false, o,iid);	
			}else {
				// future
			} 
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,short value) {
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o,iid);		
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, short value) {

	 	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			  accessSPE_object_field(iid,id, false, o,iid);	
		}else {
			// future
		} 
	    }
	    
	    public static void readBeforeInstance(Object o, int iid,long id, String classname, int lineNO,Object value) {
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_object_field(iid,id, true, o,iid);	
				}else {
					// future
				} 	
	    }
	    
	    
	    public static void writeBeforeInstance(Object o, int iid,long id, String classname, int lineNO, Object value) {

	   	 
	   	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			  accessSPE_object_field(iid,id, false, o,iid);
		}else {
			// future
		} 
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    //deprecated
//	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO) {		
//	    }
//	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO) {
//	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,boolean value) {	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true,  iid);
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,boolean value) {

	   	 
	 	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
	    	  accessSPE_static_field(iid,id, false, iid);
		}else {
			// future
		} 
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,byte value) {		
	    	
	    	 
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,byte value) {

	   	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			  accessSPE_static_field(iid,id, false, iid);
		}else {
			// future
		} 
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,char value) {		
	    		
	    		
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,char value) {

	   	 
		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_static_field(iid,id, false, iid);
			}else {
				// future
			}
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,double value) {	
	    	
	    	 
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,double value) {
		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_static_field(iid,id, false, iid);
			}else {
				// future
			}
	    }
	    
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,float value) {	
	    	
	    	 	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,float value) {

	   	 
	   	 
		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
		    	  accessSPE_static_field(iid,id, false, iid);
			}else {
				// future
			}
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,int value) {		
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,int value) {

	   	 
	   	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			  accessSPE_static_field(iid,id, false, iid);
		}else {
			// future
		}
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,long value) {		
	    	 
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);		
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,long value) {

	   	 
		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_static_field(iid,id, false, iid);
			}else {
				// future
			}
	    }
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,short value) {
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,short value) {

	   	 
		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_static_field(iid,id, false, iid);
			}else {
				// future
			}
	    }
	    
	    
	    public static void readBeforeStatic(int iid,long id, String classname, int lineNO,Object value) {	
	    	
	    	 	
	    	 if(leap){			
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}
				}else if(stride){
						perThreadGroup[(int)id].add(value);// value
						perThreadGroup[(int)id].add(accessVectorGroup[iid].size());// index of current write.
				}else if(myBasic){
					  accessSPE_static_field(iid,id, true, iid);	
				}else {
					// future
				} 	
	    }
	    public static void writeBeforeStatic(int iid,long id, String classname, int lineNO,Object value) {

	   	 
		   	if(leap){			
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}
			}else if(stride){
					synchronized (accessVectorGroup[iid]) {
						accessVectorGroup[iid].add(id);
					}				
			}else if(myBasic){
				  accessSPE_static_field(iid,id, false, iid);
			}else {
				// future
			}
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
	    
	    

	 
	public static void accessSPE_array_index(int index,long threadId, boolean read, Object array, int arrayindex) {		
		long curCounter =incInsCounter(threadId);
		if(opt_obj_sensitivity)
		    index = arrayindex%PARTITIONCOUNT;
		
		if(!read)
		{		
			long oldLatestInstCounter=-1;
		    synchronized (locks4latestWrites[index])//1:3 3 are optimized.
		    {
		    	if(!opt_reduce_write_seq)
				{
		    		oldLatestInstCounter= latestWritesInstCounter[index];		
		    	}								
				latestWritesInstCounter[index] = curCounter;	
			}	
			if(!opt_reduce_write_seq){
				if(!sameThread(oldLatestInstCounter, curCounter))
					addOrder(threadId, index, oldLatestInstCounter, curCounter);
			}
		 }
		else//	if(read)
		{
			long counterOfTheWrite=-1;
			for(;;){
				counterOfTheWrite= latestWritesInstCounter[index];				
				
				if(latestWritesInstCounter[index]==counterOfTheWrite)
				{
					break;
				}
				// else loop back.
			}				
			// store the relation: latest write -> current read, if they belong to different threads.
			//opt_Reads_of_same_write&&
			if(opt_reduce__local_read_seq_of_same_write&& counterOfLastReadsWrite[(int)threadId][index]==counterOfTheWrite ) // opt: if I and the previous read read from the same write, skip me.
			{
				
			}else {
				if(opt_reduce_read_of_local_write&&sameThread(counterOfTheWrite, curCounter))//write and read from same thread. 4:26
				{
				
				}else {
					addOrder(threadId, index, counterOfTheWrite, curCounter);
					   counterOfLastReadsWrite[(int)threadId][index]=counterOfTheWrite ;
				}
			}
		}
		
	
   	}
	
	public static void accessSPE_object_field(int index,long threadId, boolean read, Object baseObject, int field) {
		if(opt_obj_sensitivity)
		      index = baseObject.hashCode()%PARTITIONCOUNT;
		long curCounter =incInsCounter(threadId);
		
		if(!read)
		{		
			long oldLatestInstCounter=-1;
		    synchronized (locks4latestWrites[index])//1:3 3 are optimized.
		    {
		    	if(!opt_reduce_write_seq){
		    		oldLatestInstCounter= latestWritesInstCounter[index];	
		    	}
				
				latestWritesInstCounter[index] = curCounter;	
			}	
		    if(!opt_reduce_write_seq){
		    	  if(!sameThread(oldLatestInstCounter, curCounter))
					   	addOrder(threadId, index, oldLatestInstCounter, curCounter);
		    }
		  
		 }
		else//	if(read)
		{
			long counterOfTheWrite=-1;
			for(;;){
				counterOfTheWrite= latestWritesInstCounter[index];				
				
				if(latestWritesInstCounter[index]==counterOfTheWrite)
				{
					break;
				}
				// else loop back.
			}				
			// store the relation: latest write -> current read, if they belong to different threads.
			//opt_Reads_of_same_write&&
			if(opt_reduce__local_read_seq_of_same_write&& counterOfLastReadsWrite[(int)threadId][index]==counterOfTheWrite ) // opt: if I and the previous read read from the same write, skip me.
			{
				
			}else {
				if(opt_reduce_read_of_local_write&&sameThread(counterOfTheWrite, curCounter))//write and read from same thread. 4:26
				{
					
				}else {
					addOrder(threadId, index, counterOfTheWrite, curCounter);
					   counterOfLastReadsWrite[(int)threadId][index]=counterOfTheWrite ;
				}
			}
		}
		
	
   	}
	
	
	
public static void accessSPE_static_field(int index,long threadId, boolean read, int staticfield) {
	if(opt_obj_sensitivity)
	    index = staticfield;// can hardly be beyond 1024
	
		long curCounter =incInsCounter(threadId);
		
		if(!read)
		{		
			long oldLatestInstCounter=-1;
		    synchronized (locks4latestWrites[index])//1:3 3 are optimized.
		    {
		    	if(!opt_reduce_write_seq){
		    		oldLatestInstCounter= latestWritesInstCounter[index];	
		    	}
				
				latestWritesInstCounter[index] = curCounter;	
			}	
		    if(!opt_reduce_write_seq){
		    	 if(!sameThread(oldLatestInstCounter, curCounter))
					   	addOrder(threadId, index, oldLatestInstCounter, curCounter);
		    }
		   
		 }
		else//	if(read)
		{
			long counterOfTheWrite=-1;
			for(;;){
				counterOfTheWrite= latestWritesInstCounter[index];				
				
				if(latestWritesInstCounter[index]==counterOfTheWrite)
				{
					break;
				}
				// else loop back.
			}				
			// store the relation: latest write -> current read, if they belong to different threads.
			//opt_Reads_of_same_write&&
			if( opt_reduce__local_read_seq_of_same_write&&counterOfLastReadsWrite[(int)threadId][index]==counterOfTheWrite ) // opt: if I and the previous read read from the same write, skip me.
			{
				
			}else {
				if(opt_reduce_read_of_local_write&&sameThread(counterOfTheWrite, curCounter))//write and read from same thread. 4:26
				{
					
				}else {
					addOrder(threadId, index, counterOfTheWrite, curCounter);
					counterOfLastReadsWrite[(int)threadId][index]=counterOfTheWrite ;
				}
			}
		}
		
	
   	}
	
	
    /**
	 * @param index
	 * @param threadId
	 * @param read
	 */
	




	/**
	 * @param oldLatestInstCounter
	 * @param instCounter
	 * @return
	 */
	private static boolean sameThread(long oldLatestInstCounter, long instCounter) {		
		return ((oldLatestInstCounter^instCounter) < MAGIC_NUMBER);// 1<<48
	}

	/**
	 * @param index
	 * @param oldLatestInstCounter
	 * @param instCounter
	 */
	
	private static void addOrder(long threadid, int index, long oldLatestInstCounter, long instCounter) {
// no need for sync!
		if(opt_avoid_autoboxing){
			// do not use hashmap for two reasons: 
			// (1) it is slow due to the call of valueOf() in the objectWrapping. 
			// (2) it needs to resolve conflict, which is not useful for our case.
			myAccessVectorGroup_Key[(int)threadid][index].add(instCounter);
			myAccessVectorGroup_Value[(int)threadid][index].add(oldLatestInstCounter);
		}
		else {
			myAccessVectorGroup[(int)threadid][index].put(instCounter, oldLatestInstCounter);
		}		
	}

	/**
	 * @param threadId
	 */
	// a more safe way is to check whether it exceeds the bit scope of the counter.
	private static long incInsCounter(long threadId) {
//		instCounterXXX++;
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
    
    // old-school methods:
    public static void enterMonitorAfter( int iid,long id) {
	   	if(leap){			
			synchronized (accessVectorGroup[iid]) 
			{
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    public static void exitMonitorBefore(int iid,long id) {
    	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    public static void enterMonitorBefore( int iid,long id) {
    	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    public static void exitMonitorAfter(int iid,long id) {
    	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    public static void enterMonitorBefore(Object o, int iid,long id) {
    	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				if(opt_obj_sensitivity)
					iid = o.hashCode()%PARTITIONCOUNT;
				
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    public static void enterMonitorAfter(Object o, int iid,long id) {
    	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				if(opt_obj_sensitivity)
					iid = o.hashCode()%PARTITIONCOUNT;
				
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    public static void exitMonitorBefore(Object o,int iid,long id) {
    	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				if(opt_obj_sensitivity)
					iid = o.hashCode()%PARTITIONCOUNT;
				
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    public static void exitMonitorAfter(Object o,int iid,long id) {
    	if(leap){			
			synchronized (accessVectorGroup[iid]) {
				accessVectorGroup[iid].add(id);
			}
		}else if(stride){
				synchronized (accessVectorGroup[iid]) {
					accessVectorGroup[iid].add(id);
				}				
		}else if(myBasic){
			synchronized (accessVectorGroup[iid]) {
				if(opt_obj_sensitivity)
					iid = o.hashCode()%PARTITIONCOUNT;
				
				accessVectorGroup[iid].add(id);
			}	
		}else {
			// future
		}
    }
    
    
}
