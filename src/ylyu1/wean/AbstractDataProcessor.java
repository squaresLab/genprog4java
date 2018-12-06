package ylyu1.wean;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import clegoues.genprog4java.main.Configuration;

public abstract class AbstractDataProcessor {
	public static boolean repair = false;
	
	public abstract void storeError(String err);
	public abstract void storeNormal();
}
