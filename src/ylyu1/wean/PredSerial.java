package ylyu1.wean;

import java.io.Serializable;
public class PredSerial implements Serializable
{
	public int line;
	public int serial;
	public String method;
	public String className;
	public String predicate;
	public int total=0;
	public int passed=0;
	public String location;
	public boolean posCover;
	public boolean negCover;
	public PredSerial(int serial1, String className1,String method1, String location1, String predicate1, boolean posCover1, boolean negCover1, int line1)
	{
		serial=serial1;
		className=className1;
		method=method1;
		location=location1;
		predicate=predicate1;
		posCover=posCover1;
		negCover=negCover1;
		line=line1;
	}

}
