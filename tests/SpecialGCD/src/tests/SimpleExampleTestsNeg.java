package tests;

import static org.junit.Assert.assertEquals;
import packageSimpleExample.SimpleExample;

import org.junit.Test;

public class SimpleExampleTestsNeg {

  @Test
  public void severalTestCasesToTestMid() {

    // SimpleExample is tested
    SimpleExample tester = new SimpleExample();

    // assert statements

    assertEquals( 2, tester.gcd(2,4));
    assertEquals( 15, tester.gcd(0,15));
    assertEquals( 6, tester.gcd(0,6));
 
   }

} 
