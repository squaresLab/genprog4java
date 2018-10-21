package ylyu1.wean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;

import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.Search.Population;

public class PopTester {
	public static void main(String [] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Temp.all"));
		Population<EditOperation> pop = (Population<EditOperation>) ois.readObject();
		ois.close();
		for(Representation<EditOperation> rep : pop) {
			System.out.println(rep.diversity);
		}
	}

}
