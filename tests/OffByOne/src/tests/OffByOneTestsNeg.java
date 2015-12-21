package tests;

import static org.junit.Assert.assertArrayEquals;
import packageOffByOne.OffByOne;
import org.junit.Test;

public class OffByOneTestsNeg {

  @Test
  public void severalTestCasesToTestcreateFibonnaciArray() {

	  // OffByOne is tested
	    OffByOne tester = new OffByOne();

	    int[] resultArray = tester.createFibonnaciArray(4);
	    int[] expectedArray4 = {0, 1, 1, 2};
	    assertArrayEquals(expectedArray4, resultArray);
	    
	    resultArray = tester.createFibonnaciArray(5);
	    int[] expectedArray5 = {0, 1, 1, 2, 3};
	    assertArrayEquals(expectedArray5, resultArray);
	    
	    resultArray = tester.createFibonnaciArray(6);
	    int[] expectedArray6 = {0, 1, 1, 2, 3, 5};
	    assertArrayEquals(expectedArray6, resultArray);
	    
	    resultArray = tester.createFibonnaciArray(7);
	    int[] expectedArray7 = {0, 1, 1, 2, 3, 5, 8};
	    assertArrayEquals(expectedArray7, resultArray);
	    
	    resultArray = tester.createFibonnaciArray(8);
	    int[] expectedArray8 = {0, 1, 1, 2, 3, 5, 8, 13};
	    assertArrayEquals(expectedArray8, resultArray);
	  
	//  assertArrayEquals(new int[]{1,2,3},new int[]{1,2,3,4});
    
  }

} 