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
    assertEquals("Should be 2", 2, tester.mid(1,2,3));
    assertEquals("Should be 2", 2, tester.mid(3,2,1));
    assertEquals("Should be 2", 2, tester.mid(2,3,1));
    assertEquals("Should be 1", 1, tester.mid(1,1,1));
    assertEquals("Should be 1", 1, tester.mid(2,1,1));
    assertEquals("Should be 2", 2, tester.mid(2,2,1));
    assertEquals("Should be 2", 2, tester.mid(1,3,2));
    assertEquals("Should be 2", 2, tester.mid(3,1,2));
  }

} 