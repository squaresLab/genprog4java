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
    assertEquals("Should be 2", 2, tester.mid(2,1,3)); 
 
   }

} 