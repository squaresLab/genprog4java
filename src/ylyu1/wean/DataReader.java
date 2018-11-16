package ylyu1.wean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;

public class DataReader {
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("DATAOFSEED0Gen4.ddd"));
        Population<EditOperation> pop = (Population<EditOperation>)ois.readObject();
        ois.close();
        for(Representation<EditOperation> rep : pop) {
        	System.out.println(rep.diversity);
        }
	}

}
