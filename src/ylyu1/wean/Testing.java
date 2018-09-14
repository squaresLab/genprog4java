package ylyu1.wean;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class Testing {
	
	/*public static int count = 0;
	@Test
	public void test1()
	{
		count++;
		assertTrue(true);
	}
	@Test
	public void test2()
	{
		count++;
		assertTrue(true);
	}
	public static void main (String [] args) throws ClassNotFoundException
	{
		JUnitCore runner = new JUnitCore();
		Class[] claz = {Class.forName("ylyu1.wean.Testing")};
		Result r = runner.run(claz);
		System.out.println(count);
	}*/
	
	public static void main(String [] args) throws Exception
	{
		String[] argg = {"JUnitTestRunner","DEBUG"};
		WeanParse.main(argg);
		String[] aargg = {"egg","egg","DEBUG"};
		Modify.main(aargg);
		
	}

}
