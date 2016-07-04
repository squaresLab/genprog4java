package tests;

import static org.junit.Assert.assertEquals;
import packageTemplatePlaygroundTest.TemplatePlaygroundTest;

import org.junit.Test;

public class TemplatePlaygroundTestsNeg {

  @Test
  public void severalTestCasesToTestMid() {

    // TemplatePlaygroundTest is tested
     TemplatePlaygroundTest tester1 = new TemplatePlaygroundTest();
    TemplatePlaygroundTest tester2 = null;
    
    // assert statements
    tester1.add(0);
    tester1.add(1);
    tester1.add(2);
    assertEquals("", 0, tester1.get(0));
    tester1.remove(0);
    assertEquals("", 1, tester1.get(0));
    assertEquals("", false, tester1.equals(1));
    assertEquals("Here is the bug", false, tester1.equals(tester2));
    
    
 
   }

} 
