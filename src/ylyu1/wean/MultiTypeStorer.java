package ylyu1.wean;



public class MultiTypeStorer
{
public Object storage=null;;
public int[] ia=null;
public double[] da=null;
public float[] fa=null;
public long[] la=null;
public short[] sa=null;
public byte[] bya=null;
public char[] ca=null;
public boolean[] ba=null;
public int type;
//public Class cls;
//public Object[] oa = null;
public MultiTypeStorer(int x)
{
storage=new Integer(x);
type=1;
}
public MultiTypeStorer(long x)
{
storage=new Long(x);
type=2;
}
public MultiTypeStorer(short x)
{
storage=new Short(x);
type=3;
}
public MultiTypeStorer(byte x)
{
storage=new Byte(x);
type=4;
}
public MultiTypeStorer(char x)
{
storage=new Character(x);
type=5;
}
public MultiTypeStorer(boolean x)
{
storage=new Boolean(x);
type=6;
}
public MultiTypeStorer(double x)
{
storage=new Double(x);
type=7;
}
public MultiTypeStorer(float x)
{
storage=new Float(x);
type=8;
}
public MultiTypeStorer(int[] x)
{
ia=x;
type=9;
}
public MultiTypeStorer(long[] x)
{
la=x;
type=10;
}
public MultiTypeStorer(short[] x)
{
sa=x;
type=11;
}
public MultiTypeStorer(byte[] x)
{
bya=x;
type=12;
}
public MultiTypeStorer(float[] x)
{
fa=x;
type=13;
}
public MultiTypeStorer(double[] x)
{
da=x;
type=14;
}
public MultiTypeStorer(boolean[] x)
{
ba=x;
type=15;
}
public MultiTypeStorer(char[] x)
{
ca=x;
type=16;
}
/*
public MultiTypeStorer(Object[] x)
{
oa=(Object[])x;
type=18;
}
*/
public MultiTypeStorer(Object x)
{
storage=(Object)x;
type=17;
//cls=x.getClass();
}

public int ret(int x)
{
return ((Integer)storage).intValue();
}

public int[] ret(int[] x)
{
return ia;
}

public double ret(double x)
{
return ((Double)storage).doubleValue();
}
public double[] ret(double[] x)
{
return da;
}
public float ret(float x)
{
return ((Float)storage).floatValue();
}
public float[] ret(float[] x)
{
return fa;
}
public boolean ret(boolean x)
{
return ((Boolean)storage).booleanValue();
}
public boolean[] ret(boolean[] x)
{
return ba;
}
public long ret(long x)
{
return ((Long)storage).longValue();
}
public long[] ret(long[] x)
{
return la;
}
public short ret(short x)
{
return ((Short)storage).shortValue();
}
public short[] ret(short[] x)
{
return sa;
}
public byte ret(byte x)
{
return ((Byte)storage).byteValue();
}
public byte[] ret(byte[] x)
{
return bya;
}
public char ret(char x)
{
return ((Character)storage).charValue();
}
public char[] ret(char[] x)
{
return ca;
}
/*
public Object[] ret (Object[] x)
{
return oa;
}
*/
public Object ret(Object x)
{
return storage;
}

} 
