package ylyu1.wean;

public class Typer
{
public static int typer(double[] x)
{
return 7;
}
public static int typer(int[] x)
{
return 8;
}
public static int typer(long[] x)
{
return 10;
}
public static int typer(short[] x)
{
return 11;
}
public static int typer(byte[] x)
{
return 12;
}
public static int typer(char[] x)
{
return 13;
}
public static int typer(boolean[] x)
{
return 14;
}
public static int typer(float[] x)
{
return 15;
}
public static int typer(short x)
{
return 16;
}

public static int typer(Object x)
{
return 0;
}
public static int typer(long x)
{
return 1;
}
public static int typer(double x)
{
return 2;
}
public static void main(String[] args)
{
double[] d = {1.3,1.3,1.3};
System.out.println(typer(d));
}
public static int typer(int x)
{
return 3;
}
public static int typer(char x)
{
return 4;
}
public static int typer(float x)
{
return 9;
}
public static int typer(boolean x)
{
return 5;
}
public static int typer(byte x)
{
return 6;
}
}
