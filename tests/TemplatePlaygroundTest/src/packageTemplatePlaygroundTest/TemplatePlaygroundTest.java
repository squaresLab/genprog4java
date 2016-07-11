package packageTemplatePlaygroundTest;
import java.util.ArrayList;

public class TemplatePlaygroundTest {

	public int mid(int x, int y, int z){
		int ret = z;
		ArrayList<Integer> newArrayList = new ArrayList<Integer>(5);
		if(y<z){
			if(x<y){
				ret = y;
			}else if(x<z  && y > z){
				newArrayList.add(0,5);
					ret = 5;
			}
		}else{
			if(x>y){
				ret = y;

			}else if(x>z){
				ret = x;

			}	
		}
		return ret;
	}
}


