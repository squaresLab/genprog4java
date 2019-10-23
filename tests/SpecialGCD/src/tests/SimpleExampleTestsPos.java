package tests;

import static org.junit.Assert.assertEquals;
import packageSimpleExample.SimpleExample;

import org.junit.Test;

public class SimpleExampleTestsPos {

  @Test
  public void severalTestCasesToTestMid() {

    // SimpleExample is tested
    SimpleExample tester = new SimpleExample();

    // assert statements
    assertEquals( 15, tester.gcd(15,15));
    assertEquals( 1, tester.gcd(6,7));
    assertEquals( 5, tester.gcd(10,15));
    assertEquals( 6, tester.gcd(6,0));
    assertEquals( 2, tester.gcd(2,0));
    assertEquals( 3, tester.gcd(3,123));
    assertEquals( 0, tester.gcd(0,0));

  }

} 
