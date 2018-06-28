package org;

import java.util.*;
import java.math.*;

public class MathStuff
{
	public int gcd(int a, int b)
	{
		int result = 0;
		if (a == 0) {
			result= b;
		}
		{
			while (b != 0) {
				if (a > b) {
					a = a - b;
				} else {
					b = b - a;
				}
			}
			result=a;
		}
		return a;
	}
}
