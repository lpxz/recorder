/**
 * 
 */
package edu.hkust.leap.monitor;

/**
 * @author Peng Liu from Purdue
 *
 * <lpxz.ust.hk@gmail.com>
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		long i=4;
		long j=20;
		
		long k= i<<(59)+1;
		System.out.println(k);
		
		System.out.println(k>>>59);
		
		
	}

}
