package ylyu1.wean;

import javassist.*;
import java.io.*;
import java.util.*;

public class Modify
{
	public static void main(String[] args) throws Exception
	{
		int varnum = 0;
		int prednum = 0;
		String fn = args[0];
		String vn = args[1];
		boolean debug = args[2].equals("DEBUG");
		ClassPool pool = ClassPool.getDefault();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("JUSTUSE.ywl"));
        ArrayList<PredGroup> classes = (ArrayList<PredGroup>)ois.readObject();
        ois.close();
		Hashtable<Integer,PredSerial> serials = new Hashtable<Integer,PredSerial>();
		CtClass c = null;
		String fullName = null;
		for(PredGroup w : classes)
		{
			if(w.location.equals("OBJECT"))continue;
			if(w.line>=0)continue;
			int paren = w.method.indexOf('(');
			String argf = w.method.substring(paren+1,w.method.length()-1);
			String[] argfs = parseargs(argf); 
			int id = w.method.substring(0,paren).lastIndexOf('.');
			if(id<0)continue;
			CtBehavior m = null;
			//prepares class and method
			try{
				if(c==null || w.method.substring(0,id) != fullName )
				{
				fullName = w.method.substring(0,id);
				if(c!=null)
				{
				c.writeFile();
				c.defrost();
				}
				if(fullName.equals(fn))c = pool.get(vn);
				else c = pool.get(fullName);
				}
				String properName = c.getName();
				int lastDot = properName.lastIndexOf('.');
				if(lastDot>=0){properName=properName.substring(lastDot+1);}
				if(w.method.substring(id+1,paren).equals(properName))m=c.getDeclaredConstructor(pool.get(argfs));
				else m = c.getDeclaredMethod(w.method.substring(id+1,paren),pool.get(argfs));
				if(w.line>-1){m.insertAt(w.line,"System.out.print(\"\\n124124124 \"+"+w.line+"+\" \");");
 				/*m.insertAt(w.line+1,"System.out.print(\"\\n124124125 \"+"+(w.line+1)+"+\" \");");*/}
			}catch(Exception e){if(debug)System.out.println("Skipped by invalid name: "+w.method+"  "+e.getMessage());/*System.out.println(argfs[0]);*/continue;}
			//runs through each statement
			for(String ss : w.statements)
			{
				//if(ss.indexOf("daikon.Quant")>=0)continue;
				if(ss.indexOf("Abstract")>=0)continue;
				prednum++;
				//statement conversions
				String s = changereturn(ss);
 				s=changearg(s);
				if(s.indexOf("getName()")>=0)continue;
				//Enter location
				if(w.location.equals("ENTER"))
				{

					try{m.insertBefore("try{ylyu1.wean.Flusher.flushIn(new Integer("+prednum+"),"+s+");}catch(Throwable e){ylyu1.wean.Flusher.flushIn(new Integer("+prednum+"),false);}");
					//try{m.insertBefore("{System.out.print(\"\\n417417417 \");System.out.print("+s+");System.out.print(\" "+prednum+" \");}");
						if(debug)System.out.println("good: "+s);serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"ENTER",s,w.posCover,w.negCover,w.line));}
					catch(Exception e){if(debug)System.out.println("Skipped by invalid syntax: "+s+" "+e.getMessage());}
				}
				//Exit location
				else
				{
					int origloc = s.indexOf("\\old(");
					//Without orig
					if(origloc<0){
					try{m.insertAfter("try{ylyu1.wean.Flusher.flushIn(new Integer("+prednum+"),"+s+");}catch(Throwable e){ylyu1.wean.Flusher.flushIn(new Integer("+prednum+"),false);}");
						//try{m.insertAfter("{System.out.print(\"\\n417417417 \");System.out.print("+s+");System.out.print(\" "+prednum+" \");}");
							if(debug)System.out.println("good: "+s);serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));}
						catch(Exception e){if(debug)System.out.println("Skipped by invalid syntax: "+s+" "+e.getMessage());}
					}
					//With orig
					else
					{
						varnum++;
						int origend = finder(s.substring(origloc+5))+origloc+5;
						String var = s.substring(origloc+5,origend);
						m.addLocalVariable("mts"+varnum,pool.get("ylyu1.wean.MultiTypeStorer"));
						String rep = "";
						try
						{
							m.insertBefore("try{mts"+varnum+"=new ylyu1.wean.MultiTypeStorer("+var+");}");
							rep = replacer(s, var, varnum);
							m.insertAfter("try{ylyu1.wean.Flusher.flushIn(new Integer("+prednum+"),"+rep+");}catch(Throwable e){ylyu1.wean.Flusher.flushIn(new Integer("+prednum+"),false);}");
							if(debug)System.out.println("good: "+s);serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));
						}
						catch(Exception e){if(debug)System.out.println("Skipped by invalid syntax: "+s+" "+e.getMessage());}
						
					}
				}
			}
		}
		if(c!=null)
		{
			if(debug) {System.out.println("bla");}
		c.writeFile();
		c.defrost();
		}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(vn+".pse"));
		oos.writeObject(new Integer(prednum));
		oos.flush();
		oos.close();
		allSerials=serials;
	}
	
	public static Hashtable<Integer,PredSerial> allSerials = null;


	public static String[] parseargs(String s)
	{
		int a = s.indexOf(',');
		ArrayList<String> result = new ArrayList<String>();
		while(a>=0)
		{
			String tmp = s.substring(0, a);
			result.add(tmp);
			s=s.substring(a+2);
			a=s.indexOf(',');
		}
		if(s.equals(""))return new String[0];
		result.add(s);
		return result.toArray(new String[result.size()]);
	}


	public static String replacer(String s, String var, int varnum)
	{
		int a = s.indexOf("\\old("+var+")");
		if(a<0)return s;
		return replacer(s.substring(0,a)+"mts"+varnum+".ret("+var+")"+s.substring(a+6+var.length()),var,varnum);
	}

/*public static String typechanger(String s)
{
if(s.equals("int"))return "CtClass.intType";
if(s.equals("double"))return "CtClass.doubleType";
if(s.equals("float"))return "CtClass.floatType";
if(s.equals("char"))return "CtClass.charType";
if(s.equals("long"))return "CtClass.longType";
if(s.equals("short"))return "CtClass.shortType";
if(s.equals("byte"))return "CtClass.byteType";
if(s.equals("boolean"))return "CtClass.booleanType";
if(s.equals("void"))return "CtClass.voidType";
if(s.equals(""))return "CtClass.doubleType";
if(s.equals("double"))return "CtClass.doubleType";
if(s.equals("double"))return "CtClass.doubleType";
return s;
}*/

	public static int finder(String s)
	{
		int stack = 0;
		for(int i = 0; i < s.length(); i++)
		{
			if(s.charAt(i)=='(')stack++;
			else if(s.charAt(i)==')')
			{
				if(stack==0)return i;
				else stack--;
			}
		}
		return -1;
	}

	public static String changearg(String s)
	{
		int a = s.indexOf("arg");
		if(a<0)return s;
		try{return changearg(s.substring(0,a)+"$"+(Integer.parseInt(s.substring(a+3,a+4))+1)+s.substring(a+4));}catch(Exception e){return s;}
	}
	public static String changereturn(String s)
	{
		int a = s.indexOf("\\result");
		if(a>=0)
		{
			return changereturn(s.substring(0,a)+"$_"+s.substring(a+7));
		}
		else return s;
	}
}



/*

						//int origend = s.substring(origloc+5).indexOf(')')+origloc+5;
						try{   
							varnum++;
							String var = s.substring(origloc+5,origend);
							try{
								String rep = s.substring(0, origloc)+"var"+varnum+s.substring(origend+1);
								m.addLocalVariable("var"+varnum,pool.get("java.lang.Object"));
								//m.insertBefore("try{var"+varnum+"=(Typer.typer("+var+")==0)?(Object)"+var+":null;}catch(Throwable e){}");
								m.insertBefore("var"+varnum+"=(Typer.typer("+var+")==0)?(Object)"+var+":null;");
								//m.insertAfter("try{System.out.print(\"\\n417417417 \");System.out.print("+rep+");System.out.print(\" "+prednum+" \");}catch(Throwable e){}");
								m.insertAfter("{System.out.print(\"\\n417417417 \");System.out.print("+rep+");System.out.print(\" "+prednum+" \");}");
								System.out.println("good: "+s);
								serials.put(prednum,new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));
							}
							catch(Exception e)
							{
								try{
									String repint = s.substring(0, origloc)+"lt"+varnum+".longValue()"+s.substring(origend+1);
									m.addLocalVariable("lt"+varnum,pool.get("java.lang.Long"));
									//m.insertBefore("try{lt"+varnum+"=(Typer.typer("+var+")==1)?new Long("+var+"):null;}catch(Throwable e){}");
									m.insertBefore("lt"+varnum+"=(Typer.typer("+var+")==1)?new Long("+var+"):null;");
									//m.insertAfter("try{if(lt"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}catch(Throwable e){}");
									m.insertAfter("{if(lt"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}");
									System.out.println("good: "+s);
									serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));
								}
								catch(Exception e1)
								{
									try{
										//String repint = s.substring(0, origloc)+"it"+varnum+".intValue()"+s.substring(origend+1);
										String repint = s.substring(0, origloc)+"it"+varnum+s.substring(origend+1);
										m.addLocalVariable("it"+varnum,pool.get("int"));
										m.insertBefore("{it"+varnum+"=(Typer.typer("+var+")==3)?"+var+":0;}");
										//m.insertBefore("{it"+varnum+"=(Typer.typer("+var+")==3)?new Integer("+var+"):null;}");
										m.insertAfter("{if(Typer.typer("+var+")==3){try{System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}catch(Throwable e){System.out.print(\"\\n417417417 \");System.out.print(\"false\");System.out.print(\" "+prednum+" \");}}}");
										//m.insertAfter("{if(it"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}");
										System.out.println("good: "+s);
										serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));
									}
									catch(Exception e2)
									{
								System.out.println("Semi-Skipped by invalid syntax: "+s+" "+e2.getMessage());
										try{
											String repint = s.substring(0, origloc)+"ct"+varnum+".charValue()"+s.substring(origend+1);
											m.addLocalVariable("ct"+varnum,pool.get("java.lang.Character"));
											//m.insertBefore("try{ct"+varnum+"=(Typer.typer("+var+")==4)?new Character("+var+"):null;}catch(Throwable e){}");
											m.insertBefore("{ct"+varnum+"=(Typer.typer("+var+")==4)?new Character("+var+"):null;}");
											//m.insertAfter("try{if(ct"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}catch(Throwable e){}");
											m.insertAfter("{if(ct"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}");
											System.out.println("good: "+s);
											serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));
										}
										catch(Exception e3)
										{
											try{
												String repint = s.substring(0, origloc)+"byt"+varnum+".byteValue()"+s.substring(origend+1);
												m.addLocalVariable("byt"+varnum,pool.get("java.lang.Byte"));
												//m.insertBefore("try{byt"+varnum+"=(Typer.typer("+var+")==6)?new Byte("+var+"):null;}catch(Throwable e){}");
												m.insertBefore("{byt"+varnum+"=(Typer.typer("+var+")==6)?new Byte("+var+"):null;}");
												//m.insertAfter("try{if(byt"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}catch(Throwable e){}");
												m.insertAfter("{if(byt"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}");
												System.out.println("good: "+s);
												serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));
											}
											catch(Exception e4)
											{
try{
												String repint = s.substring(0, origloc)+"bt"+varnum+".booleanValue()"+s.substring(origend+1);
												m.addLocalVariable("bt"+varnum,pool.get("java.lang.Boolean"));
												//m.insertBefore("try{bt"+varnum+"=(Typer.typer("+var+")==5)?new Boolean("+var+"):null;}catch(Throwable e){}");
												m.insertBefore("{bt"+varnum+"=(Typer.typer("+var+")==5)?new Boolean("+var+"):null;}");
												//m.insertAfter("try{if(bt"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}catch(Throwable e){}");
												m.insertAfter("{if(bt"+varnum+"!=null){System.out.print(\"\\n417417417 \");System.out.print("+repint+");System.out.print(\" "+prednum+" \");}}");
												System.out.println("good: "+s);
												serials.put(prednum, new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));
}
catch(Exception e5)
{
								String rep = s.substring(0, origloc)+"(double[])var"+varnum+s.substring(origend+1);
                                                                m.addLocalVariable("var"+varnum,pool.get("java.lang.Object"));
                                                                //m.insertBefore("try{var"+varnum+"=(Typer.typer("+var+")==0)?(Object)"+var+":null;}catch(Throwable e){}");
                                                                m.insertBefore("var"+varnum+"=(Typer.typer("+var+")==7)?(Object)"+var+":null;");
                                                                //m.insertAfter("try{System.out.print(\"\\n417417417 \");System.out.print("+rep+");System.out.print(\" "+prednum+" \");}catch(Throwable e){}");
                                                                m.insertAfter("{System.out.print(\"\\n417417417 \");System.out.print("+rep+");System.out.print(\" "+prednum+" \");}");
                                                                System.out.println("good: "+s);
                                                                serials.put(prednum,new PredSerial(prednum,c.getName(),m.getName(),"EXIT",s,w.posCover,w.negCover,w.line));

}

											}}}}}
						}
						catch(Exception e){System.out.println("Skipped by invalid syntax: "+s+" "+e.getMessage());}
*/
