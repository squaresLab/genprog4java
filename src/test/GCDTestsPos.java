package test;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
public class GCDTestsPos {
	private ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//	
//	
//	public static void main(String[] args) throws Exception {                    
//	       JUnitCore.main(
//	         "com.stackoverflow.MyTestSuite");            
//	}
	
	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	}
	
	@Test
	public void testLargeGCD(){
		String[] args = new String[3];
		args[1] = "1024";
		args[2] = "512";
		GCD.main(args);
		assertEquals(outContent.toString(), "512.0\n");
	}

}



