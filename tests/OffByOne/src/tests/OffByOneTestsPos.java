package tests;

import static org.junit.Assert.assertArrayEquals;
import packageOffByOne.OffByOne;

import org.junit.Test;

public class OffByOneTestsPos {

  @Test
  public void severalTestCasesToTestcreateFibonnaciArray() {

	  // OffByOne is tested
	    OffByOne tester = new OffByOne();
	   
	    
	    int[] expectedArray = {0};
	    assertArrayEquals(expectedArray, tester.createFibonnaciArray(0));
	    assertArrayEquals(expectedArray, tester.createFibonnaciArray(1));
	    int[] expectedArray2 = {0, 1};
	    assertArrayEquals(expectedArray2, tester.createFibonnaciArray(2));
	    int[] expectedArray3 = {0, 1, 1};
	    assertArrayEquals(expectedArray3, tester.createFibonnaciArray(3));
	    
//	    assertArrayEquals(new int[]{1,2,3},new int[]{1,2,3});
    
  }

} 