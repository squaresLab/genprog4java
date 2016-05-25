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
    assertEquals("Should be 2", 1, tester.retOne());
  }

} 
