package ylyu1.wean;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;

public class Flusher implements Serializable {

	public static Hashtable<Integer, Flusher> counter = null;
	public static int count = 0;
	public static boolean changed = false;
	public static void initialize()
	{
		System.out.println("initializing...");
		counter = new Hashtable<Integer, Flusher>();
		count = 0;
		System.out.println("initialized!");
	}
	public static void flushIn(Integer i, boolean b)
	{
		if(counter==null)System.out.println("counter is null??");
		if (!counter.containsKey(i))
		{
			counter.put(i, new Flusher());
		}
		counter.get(i).in(b);
		count++;
	}
	public static void flushOut() throws FileNotFoundException, IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Temp.all"));
		oos.writeObject(counter);
		oos.flush();
		oos.close();
		changed = false;
	}
	public boolean passing = true;
	public void in(boolean passed)
	{
		if(passing && !passed)
		{
			changed = true;
		}
		passing = passing && passed;
	}
}
