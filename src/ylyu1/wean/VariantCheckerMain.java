package ylyu1.wean;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.FitnessValue;
import clegoues.genprog4java.fitness.JUnitTestRunner;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.Search.GeneticProgramming;
import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.main.Main;

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
	public static int turn = 0;
	public final static boolean cinnamon = true;
	//public static ArrayList<Boolean> goodVariant = new ArrayList<Boolean>();
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
	
	public static void checkInvariant(Population<? extends EditOperation> pop)
	{
		for(Representation<? extends EditOperation> rep : pop)
		{
			rep.vf = rep.getVariantFolder();
			try
			{
				
				if(rep.vf.equals(""))rep.vf=rep.getAlreadyCompiled().getValue();
				System.out.println("VF Value: "+rep.vf);
				
				//String libtrunc = Configuration.libs.substring(0, Configuration.libs.lastIndexOf(":")); //this line is causing problems: Configuration.libs.lastIndexOf(":") is returning -1, : isn't always in the string
				int lastIndexOfColonInLibs = Configuration.libs.lastIndexOf(":");
				//String libtrunc = lastIndexOfColonInLibs == -1 ? Configuration.libs : Configuration.libs.substring(0, lastIndexOfColonInLibs);
				String libtrunc = Configuration.libs; //no truncation for now
				
				CommandLine command1 = CommandLine.parse("cp -r "+Configuration.classTestFolder+" .");
				System.err.println("THIS IS COMMAND 2: " + "sh checker.sh "+Fitness.positiveTests.get(0)+" "+libtrunc+":.:tmp/"+rep.vf+"/:"+Main.GP4J_HOME+"/target/classes/ "+ "d_" + rep.getVariantFolder()+"pos NOTORIG" + " " 
							+ Main.GP4J_HOME + " " + Main.JAVA8_HOME + " " + Main.DAIKON_HOME);
				CommandLine command2 = CommandLine.parse("sh checker.sh "+Fitness.positiveTests.get(0)+" "+libtrunc+":.:tmp/"+rep.vf+"/:"+Main.GP4J_HOME+"/target/classes/ "+ "d_" + rep.getVariantFolder()+"pos NOTORIG" + " " 
							+ Main.GP4J_HOME + " " + Main.JAVA8_HOME + " " + Main.DAIKON_HOME);
				CommandLine command3 = CommandLine.parse("sh checker.sh "+Fitness.negativeTests.get(0)+" "+libtrunc+":.:tmp/"+rep.vf+"/:"+Main.GP4J_HOME+"/target/classes/ "+ "d_" +rep.getVariantFolder()+"neg NOTORIG" + " " 
							+ Main.GP4J_HOME + " " + Main.JAVA8_HOME + " " + Main.DAIKON_HOME);
				
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
					//goodVariant.add(true);
					rep.isGoodForCheck=true;
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
				System.out.println("ERROR!!!!!! "+rep.vf);
				e.printStackTrace();
				//goodVariant.add(false);
				rep.isGoodForCheck=false;
				if(!rep.vf.equals(""))Fitness.invariantCache.put(rep.hashCode(), null);
			}
		}
		
		try{System.out.println(Arrays.toString(analyzeResults(pop)));}catch(Exception e) {System.out.println(e.toString());}
		//return checked;
	}
	
	public static int[] analyzeResults(Population<? extends EditOperation> pop) throws Exception
	{
		ArrayList<byte[]> templist = new ArrayList<byte[]>();
		ArrayList<Representation<? extends EditOperation>> repstorer = new ArrayList<Representation<? extends EditOperation>>();
		int max = 0;
		byte[] b = null;
		System.out.println("Entering first loop");
		for(Representation<? extends EditOperation> rep : pop)
		{
			if((!rep.getVariantFolder().equals(""))&&rep.isGoodForCheck)
			{
				try{b = tnsFetcher(rep.vf+"pos");}catch(Exception e) {System.out.println("BAD: "+rep.vf);}
				templist.add(b);
				if(b.length>max)max=b.length;
				try{b = tnsFetcher(rep.vf+"neg");}catch(Exception e) {System.out.println("BAD: "+rep.vf);}
				templist.add(b);
				if(b.length>max)max=b.length;
				repstorer.add(rep);
			}
			else
			{
				rep.setFitness(0.0);
			}
		}
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		System.out.println("Entering second loop: templist length: " + templist.size()+" max: "+max);
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
			
			Fitness.invariantCache.put(repstorer.get(i/2).hashCode(),b);
		}
		repstorer = new ArrayList<Representation<? extends EditOperation>>();
		System.out.println("Entering third loop");
		for(Representation<? extends EditOperation> rep : pop)
		{
			if(Fitness.invariantCache.containsKey(rep.hashCode()))
			{
				b=Fitness.invariantCache.get(rep.hashCode());
				
				if(b!=null)
				{
					repstorer.add(rep);
					System.out.println(Arrays.toString(b));
					list.add(b);
				}
			}
			else
			{
				System.out.println("Should not happen");
			}
		}
		int[] diffScores =  Fitness.getStringDiffScore(list);
		int max1 = 0;
		for(int a: diffScores)
		{
			if(a>max1)max1=a;
		}
		if(max1==0)return diffScores;
		for(int i = 0; i < repstorer.size();i++)
		{
			if(GeneticProgramming.mode==2)
			{	
				repstorer.get(i).setFitness(((double)diffScores[i])/((double)max1)/10*(11-turn)+repstorer.get(i).getFitness()/10*(turn-1)); 
			}
			else
			{
				repstorer.get(i).setFitness(((double)diffScores[i])/((double)max1));
			}
		}
		return diffScores;
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
				CommandLine command1 = CommandLine.parse("cp -r "+Configuration.classTestFolder+" .");
				CommandLine command2 = CommandLine.parse("sh checker.sh "+Fitness.positiveTests.get(0)+" "+Configuration.libs+":.:"+Main.GP4J_HOME+"/target/classes/ origPos ORIGPOS"
							+ " " + Main.GP4J_HOME + " " + Main.JAVA8_HOME + " " + Main.DAIKON_HOME);
				CommandLine command3 = CommandLine.parse("sh checker.sh "+Fitness.negativeTests.get(0)+" "+Configuration.libs+":.:"+Main.GP4J_HOME+"/target/classes/ origNeg ORIGNEG"
						 	+ " " + Main.GP4J_HOME + " " + Main.JAVA8_HOME + " " + Main.DAIKON_HOME);
				
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
		CommandLine command1 = CommandLine.parse("cp "+Main.GP4J_HOME+"/runDaikon.sh .");
		System.out.println("sh runDaikon.sh "+Fitness.positiveTests.get(0)+" "+Configuration.libs+":"+Configuration.classTestFolder+":"+Configuration.classSourceFolder+":"+Configuration.testClassPath+":"+Main.GP4J_HOME+"/target/classes/"
					+ " " + Main.GP4J_HOME + " " + Main.JAVA8_HOME + " " + Main.DAIKON_HOME);
		CommandLine command2 = CommandLine.parse("sh runDaikon.sh "+Fitness.positiveTests.get(0)+" "+Configuration.libs+":"+Configuration.classTestFolder+":"+Configuration.classSourceFolder+":"+Configuration.testClassPath+":"+Main.GP4J_HOME+"/target/classes/"
					+ " " + Main.GP4J_HOME + " " + Main.JAVA8_HOME + " " + Main.DAIKON_HOME);
		CommandLine command3 = CommandLine.parse("cp "+Main.GP4J_HOME+"/checker.sh .");
		
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
					out.flush();
					out.close();
				} catch (IOException e) {
				}
		}
	}

}