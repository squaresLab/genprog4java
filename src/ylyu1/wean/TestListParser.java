package ylyu1.wean;

import java.util.*;
import java.io.*;
public class TestListParser
{
public static void main(String [] args) throws Exception
{
Scanner input = new Scanner(new File(args[0]));
String orgdir = args[1];
String testdir = args[2];
ArrayList<String> dirlist = new ArrayList<String>();
ArrayList<String> claslist = new ArrayList<String>();
int limit = Integer.parseInt(args[3]);
PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("Mainy.java")));
//pw.println("public class Mainy \n{ \npublic static void main(String[] args)throws Exception\n{");
pw.println("import org.junit.runner.*;\nimport org.junit.runners.*;@RunWith(Suite.class)\n@Suite.SuiteClasses({");
int i = 0;
while(input.hasNextLine())
{
i++;
String line = input.nextLine();
int paren1 = line.indexOf('(');
int paren2 = line.indexOf(')');
String func = line.substring(0,paren1);
String clas = line.substring(paren1+1,paren2);
int splitter = clas.lastIndexOf('.');
String clasn = clas.substring(splitter+1);
if(clasn.indexOf('$')>=0)continue;
String dir = clas.substring(0,splitter);
dir=changetoslash(dir);
if(!isin(dir,dirlist))dirlist.add(dir);
if(!isin(clas,claslist))claslist.add(clas);
//String code = "try{TimeLimitedCodeBlock.runwithtime(new Runnable(){public void run(){try{"+clas+" c = new "+clas+"();c."+func+"();}catch(Throwable e){}}},"+limit+");}catch(Throwable e){}System.out.println(\"Test "+i+"\");";
//String code = "try{"+clas+" c = new "+clas+"();c."+func+"();}catch(Throwable e){}System.out.println(\"Test "+i+"\");";
//pw.println(code);
}
for(int j = 0; j < claslist.size();j++){if(j>0)pw.print(",");pw.println(claslist.get(j)+".class");}
//pw.println("\n}\n}");
pw.println("\n})\npublic class Mainy{}");
pw.close();

PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter("CP.sh")));
p.print("#!/usr/lib/bin\nexport RELPATH=.");
for(String s: dirlist)
{
p.print(":"+orgdir+"/"+s+":"+testdir+"/"+s);
}
p.close();
}
public static boolean isin(String s, ArrayList<String> ss)
{
for(String x:ss)
{
if(s.equals(x))return true;
}
return false;
}
public static String changetoslash(String s)
{
int a = s.indexOf('.');
if(a>=0)return changetoslash(s.substring(0,a)+"/"+s.substring(a+1));
return s;
}
}

