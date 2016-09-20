package packageTemplatePlaygroundTest;
import java.util.ArrayList;

public class TemplatePlaygroundTest {

   public int mid(int x, int y, int z){
        int ret;
	ret = z;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		ret = y; // bug, it should be ret = x;
		double 
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


