package ylyu1.wean;

import java.io.*;
import java.util.*;

public class WeanParse
{
	public static String stuffToBeProcessed = null;
	public static ArrayList<PredGroup> allInvariants = null;
	public static void main(String[] args)
	{
	int total = 0;
		String fn = args[0];
                String debug = args[1];
		Scanner input = new Scanner(stuffToBeProcessed);
		ArrayList<PredGroup> classes = new ArrayList<PredGroup>();
		PredGroup current = null;
		while(input.hasNext())
		{
			String line = input.nextLine();
			if(line.equals("Exiting Daikon."))break;
			if(line.length()>=20&&line.substring(0,20).equals("===================="))
			{
				if(current!=null)classes.add(current);
				current = new PredGroup();
				line = input.nextLine();
				if(line.indexOf(":::EXIT")>=0)
				{
					current.location="EXIT";
					current.method=line.substring(0,line.indexOf(":::EXIT"));
					String s = line.substring(line.indexOf(":::EXIT")+7);
					if(!s.equals(""))current.line=Integer.parseInt(s);else total++;
				}
				else if(line.substring(line.length()-8).equals(":::ENTER"))
				{
					current.location="ENTER";
					current.method=line.substring(0,line.length()-8);
total++;
				}
				else if(line.substring(line.length()-9).equals(":::OBJECT"))
				{
					current.location="OBJECT";
					current.method=line.substring(0,line.length()-9);
				}
				else current = null;

			}
			else if(current!=null)
			{
				current.statements.add(line);
			}
		}
		System.out.println(total);
		if(current!=null)classes.add(current);
		//System.out.println(classes.size());
		/*
		if(debug.equals("DEBUG"))
		{
			PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( fn+".twt" )));
			for(PredGroup w : classes)
			{
				writer.println("Method: "+w.method);
				writer.println("Location: "+w.location);
				for(String s : w.statements)
				{
					writer.println(s);
				}
				writer.println("");
			}
			writer.close();
		}*/
		if(!classes.isEmpty())
		{
			allInvariants.addAll(classes);
			
		}


	}
	
	public static void make() {
		try {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("JUSTUSE.ywl"));
		oos.writeObject(allInvariants);
		oos.flush();
		oos.close();}catch(Exception e) {}
	}
}


