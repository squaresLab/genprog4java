package packageTemplatePlaygroundTest;
import java.util.ArrayList;

public class TemplatePlaygroundTest {

	public int mid(int x, int y, int z){
		int ret = z;

		if(y<z){
			if(x<y){
				ret = y;
			}else if(x<z  && y > z){
				//ret = y; // bug, it should be ret = x;
				for(int i = 0; i < 5; i++) {
					ret = 5;
				}
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


