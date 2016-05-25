package tests;

import static org.junit.Assert.assertEquals;
import packageTemplatePlaygroundTest.TemplatePlaygroundTest;

import org.junit.Test;

public class TemplatePlaygroundTestsNeg {

  @Test
  public void severalTestCasesToTestMid() {

    // TemplatePlaygroundTest is tested
    TemplatePlaygroundTest tester = new TemplatePlaygroundTest();

    // assert statements
    assertEquals("Should be 2", 2, tester.retOne()); 
 
   }

} 
