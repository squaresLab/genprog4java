package packageOffByOne;

public class OffByOne {
	
	public int [] createFibonnaciArray(int n) {

		if (n <= 1){
			int[] array = new int [1];
			array[0] = 0;
			return array;
		} else {
			int[] array = new int[2];
			array[0] = 0;
			array[1] = 1;
			return array;
		}	
	}
}
