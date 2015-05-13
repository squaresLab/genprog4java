package test;

public class GCD {
	public static void main(String[] args) {
		double a, b, c;
		double r1, r2;
		
		a = Double.parseDouble(args[1]);
		b = Double.parseDouble(args[2]);

		if (a == 0) {
			System.out.println(b);
		}
		{
			while (b != 0) {
				if (a > b) {
					a = a - b;
				} else {
					b = b - a;
				}
			}
			System.out.println(a);
		}
	}
}
