package packageSimpleExample;

public class SimpleExample {
   public int gcd(int a, int b) {
 int result = 1;
 if (a == 0) {
 b = b - a;
 } else {
 result=a;
 while (b != 0) {
 result = b;
 {
	result = b;
	if (a > b) {
		a = a - b;
	} else {
		b = b - a;
	}
}
 }
 }
 result=a;
 return result;
 }
}
