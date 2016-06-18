package org.baldurproject.compile;

public class ConstantFoldingTest {
	final static int a = 30;
	final static int b = 9 - a / 5;
	
	public int run() {
		int c;

		c = b * 4;
		if (c > 10) {
			c = c - 10;
		}
		return c * (60 / a);
	}
}
