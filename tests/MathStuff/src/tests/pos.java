package tests;

import static org.junit.Assert.assertEquals;
import org.MathStuff;

import org.junit.Test;

public class pos {

  @Test(timeout=4000)
  public void postest() {

    // SimpleExample is tested
    MathStuff ms = new MathStuff();

    // assert statements
    assertEquals(ms.gcd(1,0),1); 
assertEquals(ms.gcd(2,2),2);
assertEquals(ms.gcd(2,0),2);
assertEquals(ms.gcd(4,6),2);
assertEquals(ms.gcd(25,10),5);
assertEquals(ms.gcd(144,196),4);
//assertEquals(ms.gcd(0,0),0);
assertEquals(ms.gcd(4,13),1);
assertEquals(ms.gcd(90,120),30);
assertEquals(ms.gcd(23,2000),1);
assertEquals(ms.gcd(20,1000),20);
assertEquals(ms.gcd(90,121),1);
assertEquals(ms.gcd(3,45),3);
   }

} 
