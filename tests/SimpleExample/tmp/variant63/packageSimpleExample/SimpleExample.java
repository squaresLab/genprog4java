package packageSimpleExample;

public class SimpleExample {

    public int mid(int x, int y, int z){

        int ret;
	ret = z;
	{
		if (x < y) {
			ret = y;
		} else if (x < z) {
			ret = z;
		}
	}
	return ret;
    }
}
