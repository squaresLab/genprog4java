package packageTemplatePlaygroundTest;

import java.util.concurrent.ExecutionException;
import java.util.ArrayList;

public class TemplatePlaygroundTest {

	private int myfield;
	
    public TemplatePlaygroundTest(){
    }
    
    public int addOne(int i) {
    	return i + 1;
    }
    
    public int addTwo(int j) {
    	return j + 2;
    }
    public TemplatePlaygroundTest(int i){
    	if(i < 0){
    		throw new IllegalArgumentException("Final speed can not be less than zero");
    	}
    }

    public int mid(int x, int y, int z){
    	int foo = 0;
    	foo = addOne(foo);

    	int ret = z;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		ret = y; // bug, it should be ret = x;
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


