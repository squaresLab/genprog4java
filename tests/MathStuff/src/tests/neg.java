package tests;

import static org.junit.Assert.assertEquals;
import org.MathStuff;

import org.junit.Test;

public class neg {

  @Test(timeout=1000)
  public void negTest() {

    // SimpleExample is tested
    MathStuff tester = new MathStuff();
assertEquals(tester.gcd(0,8),8);
assertEquals(tester.gcd(0,3),3);
  }

} 
