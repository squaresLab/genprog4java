package tests;

import static org.junit.Assert.assertEquals;
import packageTemplatePlaygroundTest.TemplatePlaygroundTest;

import org.junit.Test;

public class TemplatePlaygroundTestsPos {

  @Test
  public void severalTestCasesToTestMid() {

    // TemplatePlaygroundTest is tested
    TemplatePlaygroundTest tester1 = new TemplatePlaygroundTest();
    TemplatePlaygroundTest tester2 = new TemplatePlaygroundTest();
    
    // assert statements
    tester1.add(0);
    tester1.add(1);
    tester1.add(2);
    tester1.add(3);
    assertEquals("", 0, tester1.get(0));
    tester1.remove(0);
    assertEquals("", 1, tester1.get(0));
    assertEquals("", "size: 3, elements: [1, 2, 3]", tester1.toString());
    assertEquals("", "size: 3, elements: [1, 2, 3]", tester1.toStringUsingStringBuffer());
    System.out.println("Retulst so far: " + tester1.size());
    System.out.println("Retulst so far: " + tester2.size());
    assertEquals("", false, tester1.equals(tester2));
    tester2.add(1);
    assertEquals("", false, tester1.equals(tester2));
    tester2.add(2);
    assertEquals("", false, tester1.equals(tester2));
    tester2.add(3);
    assertEquals("", true, tester1.equals(tester2));
   
  }

} 
