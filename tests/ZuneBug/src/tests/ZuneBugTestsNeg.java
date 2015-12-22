package tests;

import static org.junit.Assert.assertEquals;
import packageZuneBug.ZuneBug;

import org.junit.Test;

public class ZuneBugTestsNeg {

  @Test
  public void severalTestCasesToTestCurrentYear() {

	  ZuneBug tester = new ZuneBug();

    // assert statements
    assertEquals("Should be 2008", 2008, tester.CurrentYear(10593)); 
    assertEquals("Should be 2012", 2012, tester.CurrentYear(12054));
    assertEquals("Should be 1984", 1984, tester.CurrentYear(1827));
    assertEquals("Should be 1980", 1980, tester.CurrentYear(366));
 
   }

} 