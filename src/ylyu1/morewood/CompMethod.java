package ylyu1.morewood;

import clegoues.genprog4java.fitness.TestCase;
import java.util.*;

public class CompMethod implements Comparable<CompMethod> {

	public TestCase tc;
	public int match=0;
	public List<String> matched = new ArrayList<String>();
	
	
	public CompMethod(List<String> methods1, Set<String> methods2, TestCase t) {
		tc=t;
		for(String m : methods1) {
			if(methods2.contains(m)) {
				match++;
				matched.add(m);
			}
		}
	}
	
	@Override
	public int compareTo(CompMethod cm) {
		return cm.match-match;
	}

}
