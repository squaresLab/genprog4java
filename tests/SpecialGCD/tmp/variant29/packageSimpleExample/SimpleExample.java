package packageSimpleExample;

public class SimpleExample {
   public int gcd(int a, int b) {
 int result = 1;
 if (a == 0) {
 b = b - a;
 } else {
 {
}
 while (b != 0) {
 result = b;
 if (a > b) {
 {
	a = a - b;
	result = a;
}
 } else {
 b = b - a;
 }
 }
 }
 result=a;
 return result;
 }
}
