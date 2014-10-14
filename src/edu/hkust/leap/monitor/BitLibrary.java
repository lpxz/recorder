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
        System.out.println(k);
        System.out.println(toBinary(keepNbits(k, 15)));
        
        
		
		
		

		
	}
	
	public static int keepNbits(int x , int Nbits)
	{
		int mask = (int)Math.pow(2, Nbits)-1;
		return x&mask;
	}
	public static int maskN(int Nbits)
	{
		return (int)Math.pow(2, Nbits)-1;
	}
	public static String toBinary(int x)
	{
		return Integer.toBinaryString(x);
	}
	
	

}
