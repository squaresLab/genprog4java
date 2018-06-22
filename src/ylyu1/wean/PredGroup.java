package ylyu1.wean;

import java.util.*;
import java.io.*;
public class PredGroup implements Serializable
{
	public int line = -1;
	public String method="";
	public String location="";
	public boolean posCover = false;
	public boolean negCover = false;
	public ArrayList<String> statements = new ArrayList<String>();
}
