package ylyu1.wean;

import javassist.*;
import java.io.*;
import java.util.*;

public class Modify {
	public static void main(String[] args) throws Exception {
		int varnum = 0;
		int prednum = 0;
		String fn = args[0];
		String vn = args[1];
		boolean debug = args[2].equals("DEBUG");
		ClassPool pool = ClassPool.getDefault();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("JUSTUSE.ywl"));
		ArrayList<PredGroup> classes = (ArrayList<PredGroup>) ois.readObject();
		ois.close();
		Hashtable<Integer, PredSerial> serials = new Hashtable<Integer, PredSerial>();
		CtClass c = null;
		String fullName = null;
		for (PredGroup w : classes) {
			if (w.location.equals("OBJECT"))
				continue;
			if (w.line >= 0)
				continue;
			int paren = w.method.indexOf('(');
			String argf = w.method.substring(paren + 1, w.method.length() - 1);
			String[] argfs = parseargs(argf);
			int id = w.method.substring(0, paren).lastIndexOf('.');
			if (id < 0)
				continue;
			CtBehavior m = null;
			// prepares class and method
			try {
				if (c == null || w.method.substring(0, id) != fullName) {
					fullName = w.method.substring(0, id);
					if (c != null) {
						c.writeFile();
						c.defrost();
					}
					if (fullName.indexOf("ylyu1.wean") >= 0)
						continue;
					if (fullName.equals(fn))
						c = pool.get(vn);
					else
						c = pool.get(fullName);
				}
				String properName = c.getName();
				int lastDot = properName.lastIndexOf('.');
				if (lastDot >= 0) {
					properName = properName.substring(lastDot + 1);
				}
				if (w.method.substring(id + 1, paren).equals(properName))
					m = c.getDeclaredConstructor(pool.get(argfs));
				else
					m = c.getDeclaredMethod(w.method.substring(id + 1, paren), pool.get(argfs));
			} catch (Exception e) {
				if (debug)
					System.out.println("Skipped by invalid name: " + w.method + "  " + e.getMessage());
				continue;
			}
			// runs through each statement
			for (String ss : w.statements) {
				if (ss.indexOf("Abstract") >= 0)
					continue;
				prednum++;
				// statement conversions
				String s = changereturn(ss);
				s = changearg(s);
				if (s.indexOf("getName()") >= 0)
					continue;
				if (s.indexOf("Flusher") >= 0)
					continue;
				// Enter location
				if (w.location.equals("ENTER")) {

					try {
						m.insertBefore("try{ylyu1.wean.Flusher.flushIn(new Integer(" + prednum + ")," + s
								+ ");}catch(Throwable e){ylyu1.wean.Flusher.flushIn(new Integer(" + prednum
								+ "),false);}");
						if (debug)
							System.out.println("good: " + s);
						serials.put(prednum, new PredSerial(prednum, c.getName(), m.getName(), "ENTER", s, w.posCover,
								w.negCover, w.line));
					} catch (Exception e) {
						if (debug)
							System.out.println("Skipped by invalid syntax: " + s + " " + e.getMessage());
					}
				}
				// Exit location
				else {
					int origloc = s.indexOf("\\old(");
					// Without orig
					if (origloc < 0) {
						try {
							m.insertAfter("try{ylyu1.wean.Flusher.flushIn(new Integer(" + prednum + ")," + s
									+ ");}catch(Throwable e){ylyu1.wean.Flusher.flushIn(new Integer(" + prednum
									+ "),false);}");
							if (debug)
								System.out.println("good: " + s);
							serials.put(prednum, new PredSerial(prednum, c.getName(), m.getName(), "EXIT", s,
									w.posCover, w.negCover, w.line));
						} catch (Exception e) {
							if (debug)
								System.out.println("Skipped by invalid syntax: " + s + " " + e.getMessage());
						}
					}
					// With orig
					else {
						varnum++;
						int origend = finder(s.substring(origloc + 5)) + origloc + 5;
						String var = s.substring(origloc + 5, origend);
						m.addLocalVariable("mts" + varnum, pool.get("ylyu1.wean.MultiTypeStorer"));
						String rep = "";
						try {
							m.insertBefore("try{mts" + varnum + "=new ylyu1.wean.MultiTypeStorer(" + var + ");}");
							rep = replacer(s, var, varnum);
							m.insertAfter("try{ylyu1.wean.Flusher.flushIn(new Integer(" + prednum + ")," + rep
									+ ");}catch(Throwable e){ylyu1.wean.Flusher.flushIn(new Integer(" + prednum
									+ "),false);}");
							if (debug)
								System.out.println("good: " + s);
							serials.put(prednum, new PredSerial(prednum, c.getName(), m.getName(), "EXIT", s,
									w.posCover, w.negCover, w.line));
						} catch (Exception e) {
							if (debug)
								System.out.println("Skipped by invalid syntax: " + s + " " + e.getMessage());
						}

					}
				}
			}
		}
		if (c != null) {
			if (debug) {
				System.out.println("bla");
			}
			c.writeFile();
			c.defrost();
		}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(vn + ".pse"));
		oos.writeObject(new Integer(prednum));
		oos.flush();
		oos.close();
		allSerials = serials;
	}

	public static Hashtable<Integer, PredSerial> allSerials = null;

	public static String[] parseargs(String s) {
		int a = s.indexOf(',');
		ArrayList<String> result = new ArrayList<String>();
		while (a >= 0) {
			String tmp = s.substring(0, a);
			result.add(tmp);
			s = s.substring(a + 2);
			a = s.indexOf(',');
		}
		if (s.equals(""))
			return new String[0];
		result.add(s);
		return result.toArray(new String[result.size()]);
	}

	public static String replacer(String s, String var, int varnum) {
		int a = s.indexOf("\\old(" + var + ")");
		if (a < 0)
			return s;
		return replacer(s.substring(0, a) + "mts" + varnum + ".ret(" + var + ")" + s.substring(a + 6 + var.length()),
				var, varnum);
	}

	public static int finder(String s) {
		int stack = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '(')
				stack++;
			else if (s.charAt(i) == ')') {
				if (stack == 0)
					return i;
				else
					stack--;
			}
		}
		return -1;
	}

	public static String changearg(String s) {
		int a = s.indexOf("arg");
		if (a < 0)
			return s;
		try {
			return changearg(
					s.substring(0, a) + "$" + (Integer.parseInt(s.substring(a + 3, a + 4)) + 1) + s.substring(a + 4));
		} catch (Exception e) {
			return s;
		}
	}

	public static String changereturn(String s) {
		int a = s.indexOf("\\result");
		if (a >= 0) {
			return changereturn(s.substring(0, a) + "$_" + s.substring(a + 7));
		} else
			return s;
	}
}
