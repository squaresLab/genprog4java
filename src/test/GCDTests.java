package test;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
public class GCDTests {
	private ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	
	
	public static void main(String[] args) throws Exception {                    
	       JUnitCore.main(
	         "com.stackoverflow.MyTestSuite");            
	}
	
	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	}
	
	@Test
	public void testGCD(){
		String[] args = new String[3];
		args[1] = "63";
		args[2] = "7";
		GCD.main(args);
		assertEquals(outContent.toString(), "7.0\n");
		
		args[1] = "-1.0";
		args[2] = "-2.0";
		GCD.main(args);
		assertEquals(outContent.toString(), "1.0\n");
		
	}

}



