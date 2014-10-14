/**
 * 
 */
package edu.hkust.leap.monitor;

/**
 * @author Peng Liu from Purdue
 *
 * <lpxz.ust.hk@gmail.com>
 */
public class BitLibrary {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// address type, o, f
		
		int i=1;
		
		int j = 100;
		
		int k= (j<<20)+1;
//        System.out.println(k);
//        System.out.println(toBinary(keepNbits(k, 15)));
        System.out.println(((int)Math.pow(2, 5)-1));
        
		
		
//		System.out.println(toBinary(32767));
		
		

		
	}
	
	public static int keepNbits(int x , int Nbits)
	{
		int mask = (int)Math.pow(2, Nbits)-1;
		return x&mask;
	}
	
	public static int keep15bits(int x)
	{
		return x&32767;
	}
	
	public static int keep10bits(int x)
	{
		return x&1023;
	}
	
	public static int keep8bits(int x)
	{
		return x&255;
	}
	
	public static int keep5bits(int x)
	{
		return x&31;
	}
	
	
	public static int maskN(int Nbits)
	{
		return (int)Math.pow(2, Nbits)-1;
	}
	public static String toBinary(int x)
	{
		return Integer.toBinaryString(x);
	}

	/**

	 * @param o: base object or 0 for static
	 * @param arrayindex: field (spe id) or array
	 * @return
	 */
	
	public static int compute(int baseObject, int fieldorarrayIndex) {        				
//		int ret=  (keep5bits(baseObject)<<5) + keep5bits(fieldorarrayIndex);
		
		return 1;
	}
	
	

}
