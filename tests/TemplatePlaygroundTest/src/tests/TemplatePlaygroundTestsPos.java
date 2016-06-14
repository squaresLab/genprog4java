package tests;

import static org.junit.Assert.assertEquals;
import packageTemplatePlaygroundTest.TemplatePlaygroundTest;

import org.junit.Test;

public class TemplatePlaygroundTestsPos {

  @Test
  public void severalTestCasesToTestMid() {

    // TemplatePlaygroundTest is tested
    TemplatePlaygroundTest tester = new TemplatePlaygroundTest();

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
