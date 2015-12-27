package tests;

import static org.junit.Assert.assertEquals;
import packageZuneBug.ZuneBug;

import org.junit.Test;

public class ZuneBugTestsPos {

  @Test(timeout=100)
  public void severalTestCasesToTestCurrentYear() {

	  ZuneBug tester = new ZuneBug();

    // assert statements
	assertEquals("Should be 1980", 1980, tester.CurrentYear(-366));
    assertEquals("Should be 1980", 1980, tester.CurrentYear(-100));
    assertEquals("Should be 1980", 1980, tester.CurrentYear(0));
    assertEquals("Should be 1980", 1980, tester.CurrentYear(365));
    assertEquals("Should be 1981", 1981, tester.CurrentYear(367));
    assertEquals("Should be 1982", 1982, tester.CurrentYear(1000));
    assertEquals("Should be 1984", 1984, tester.CurrentYear(1826));
    assertEquals("Should be 1985", 1985, tester.CurrentYear(1828));
    assertEquals("Should be 1985", 1985, tester.CurrentYear(2000));
    assertEquals("Should be 1988", 1988, tester.CurrentYear(3000));
    assertEquals("Should be 1990", 1990, tester.CurrentYear(4000));
    assertEquals("Should be 1993", 1993, tester.CurrentYear(5000));
    assertEquals("Should be 2008", 2008, tester.CurrentYear(10592));
    assertEquals("Should be 2009", 2009, tester.CurrentYear(10594));
    assertEquals("Should be 2012", 2012, tester.CurrentYear(12053));
    assertEquals("Should be 2013", 2013, tester.CurrentYear(12055));
    assertEquals("Should be 2013", 2013, tester.CurrentYear(12213));
    assertEquals("Should be 2013", 2013, tester.CurrentYear(12419));
    assertEquals("Should be 2014", 2014, tester.CurrentYear(12420));
//    assertEquals("Should be 275765", 275765, tester.CurrentYear(100000000));
    

  }

} 