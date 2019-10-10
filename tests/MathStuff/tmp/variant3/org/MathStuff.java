package org;

import java.util.*;
import java.math.*;

public class MathStuff
{
	public int gcd(int a, int b)
	{
		int result = 1;
		if (a == 0) {
			{
				a = a - b;
			}
		}
		else
		{
			while (b != 0) {
				result = b;
				if (a > b) {
					a = a - b;
				} else {
					b = b - a;
				}
			}
		}
		result=a;	
		return result;
	}
}
