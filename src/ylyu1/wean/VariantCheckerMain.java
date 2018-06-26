package ylyu1.wean;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.FitnessValue;
import clegoues.genprog4java.fitness.JUnitTestRunner;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.main.Configuration;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

public class VariantCheckerMain
{
	public static ArrayList<Boolean> goodVariant = new ArrayList<Boolean>();
	public static void main(String [] args) throws Exception
	{
		/*int n = Integer.parseInt(args[0]);
		String orig = args[1];
		boolean daikon = false;
		if(args[2].equals("DAIKON"))daikon=true;
		String postest = args[3];
		String negtest = args[4];*/
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("mkdir opopop");
		pr.waitFor();
		/*if(daikon)
		{
			rt.exec("java -cp .:$CLASSPATH daikon.DynComp ");
		}
		for(int i = 0; i < n; i++)
		{
			
		}*/
	}
	
	public static int checkInvariant(int checked)
	{
		int begin = checked;
		while(checked<CachingRepresentation.sequence)
		{
			try
			{
				String libtrunc = Configuration.libs.substring(0, Configuration.libs.lastIndexOf(":"));
				CommandLine command1 = CommandLine.parse("cp -r "+Configuration.classTestFolder+"tests .");
				CommandLine command2 = CommandLine.parse("sh checker.sh "+Fitness.positiveTests.get(0)+" "+libtrunc+":.:tmp/variant"+checked+"/:/home/lvyiwei1/genprog4java-branch/genprog4java/target/classes/ variant"+checked+"pos NOTORIG");
				CommandLine command3 = CommandLine.parse("sh checker.sh "+Fitness.negativeTests.get(0)+" "+libtrunc+":.:tmp/variant"+checked+"/:/home/lvyiwei1/genprog4java-branch/genprog4java/target/classes/ variant"+checked+"neg NOTORIG");
				
				//System.out.println("command: " + command2.toString());
				ExecuteWatchdog watchdog = new ExecuteWatchdog(1000000);
				DefaultExecutor executor = new DefaultExecutor();
				String workingDirectory = System.getProperty("user.dir");
				executor.setWorkingDirectory(new File(workingDirectory));
				executor.setWatchdog(watchdog);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				executor.setExitValue(0);

				executor.setStreamHandler(new PumpStreamHandler(out));
				FitnessValue posFit = new FitnessValue();

				try {
					executor.execute(command1);
					executor.execute(command2);
					executor.execute(command3);
					out.flush();
					String output = out.toString();
					System.out.println(output);
					out.reset();
					goodVariant.add(true);
				} catch (ExecuteException exception) {
					//posFit.setAllPassed(false);
					out.flush();
					System.out.println(out.toString());
					throw exception;
					
				} catch (Exception e) {
				} finally {
					if (out != null)
						try {
							out.close();
						} catch (IOException e) {
						}
				}
			}
			catch(Exception e)
			{
				System.out.println("ERROR!!!!!! "+checked);
				goodVariant.add(false);
			}
			checked++;
		}
		try{System.out.println(Arrays.toString(analyzeResults(begin,checked)));}catch(Exception e) {}
		return checked;
	}
	
	public static int[] analyzeResults(int begin, int end) throws Exception
	{
		ArrayList<byte[]> templist = new ArrayList<byte[]>();
		int max = 0;
		byte[] b = tnsFetcher("origPos");
		templist.add(b);
		if(b.length>max)max=b.length;
		b = tnsFetcher("origNeg");
		templist.add(b);
		if(b.length>max)max=b.length;
		for(int i = begin; i < end; i++)
		{
			if(goodVariant.get(i))
			{
				b = tnsFetcher("variant"+i+"pos");
				templist.add(b);
				if(b.length>max)max=b.length;
				b = tnsFetcher("variant"+i+"neg");
				templist.add(b);
				if(b.length>max)max=b.length;
			}
		}
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		for(int i = 0; i < templist.size(); i+=2)
		{
			b = newByteArray2(2*max);
			for(int j = 0; j < templist.get(i).length; j++)
			{
				b[2*j]=templist.get(i)[j];
			}
			for(int j = 0; j < templist.get(i+1).length; j++)
			{
				b[2*j+1]=templist.get(i+1)[j];
			}
			System.out.println(Arrays.toString(b));
			list.add(b);
		}
		return Fitness.getStringDiffScore(list);
	}
	
	public static byte[] newByteArray2(int size)
	{
		byte[] b = new byte[size];
		for(int i = 0; i < size; i++)
		{
			b[i]=2;
		}
		return b;
	}
	
	public static byte[] tnsFetcher(String s) throws Exception
	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s+".tns"));
		byte[] b = (byte[]) ois.readObject();
		ois.close();
		return b;
	}
	
	
	public static void checkInvariantOrig()
	{
		try
			{
				CommandLine command1 = CommandLine.parse("cp -r "+Configuration.classTestFolder+"tests .");
				CommandLine command2 = CommandLine.parse("sh checker.sh "+Fitness.positiveTests.get(0)+" "+Configuration.libs+":.:/home/lvyiwei1/genprog4java-branch/genprog4java/target/classes/ origPos ORIGPOS");
				CommandLine command3 = CommandLine.parse("sh checker.sh "+Fitness.negativeTests.get(0)+" "+Configuration.libs+":.:/home/lvyiwei1/genprog4java-branch/genprog4java/target/classes/ origNeg ORIGNEG");
				
				//System.out.println("command: " + command2.toString());
				ExecuteWatchdog watchdog = new ExecuteWatchdog(1000000);
				DefaultExecutor executor = new DefaultExecutor();
				String workingDirectory = System.getProperty("user.dir");
				executor.setWorkingDirectory(new File(workingDirectory));
				executor.setWatchdog(watchdog);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				executor.setExitValue(0);

				executor.setStreamHandler(new PumpStreamHandler(out));
				FitnessValue posFit = new FitnessValue();

				try {
					executor.execute(command1);
					executor.execute(command2);
					executor.execute(command3);
					out.flush();
					String output = out.toString();
					System.out.println(output);
					out.reset();

				} catch (ExecuteException exception) {
					//posFit.setAllPassed(false);
					out.flush();
					System.out.println(out.toString());
					throw exception;
					
				} catch (Exception e) {
				} finally {
					if (out != null)
						try {
							out.close();
						} catch (IOException e) {
						}
				}
			}
			catch(Exception e)
			{
				
			}
		
		
	}
	
	public static void runDaikon()
	{
		CommandLine command1 = CommandLine.parse("cp /home/lvyiwei1/genprog4java-branch/genprog4java/runDaikon.sh .");
		CommandLine command2 = CommandLine.parse("sh runDaikon.sh "+Fitness.positiveTests.get(0)+" "+Configuration.libs+":"+Configuration.classTestFolder+":"+Configuration.classSourceFolder+":/home/lvyiwei1/genprog4java-branch/genprog4java/target/classes/");
		CommandLine command3 = CommandLine.parse("cp /home/lvyiwei1/genprog4java-branch/genprog4java/checker.sh .");
		
		//System.out.println("command: " + command2.toString());
		ExecuteWatchdog watchdog = new ExecuteWatchdog(1000000);
		DefaultExecutor executor = new DefaultExecutor();
		String workingDirectory = System.getProperty("user.dir");
		executor.setWorkingDirectory(new File(workingDirectory));
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executor.setExitValue(0);

		executor.setStreamHandler(new PumpStreamHandler(out));
		FitnessValue posFit = new FitnessValue();

		try {
			executor.execute(command1);
			executor.execute(command2);
			executor.execute(command3);
			out.flush();
			String output = out.toString();
			System.out.println(output);
			out.reset();

		} catch (ExecuteException exception) {
			//posFit.setAllPassed(false);
			System.out.println(exception.toString());
			
		} catch (Exception e) {
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}

}