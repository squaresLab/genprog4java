package packageTemplatePlaygroundTest;

import java.util.concurrent.ExecutionException;

public class TemplatePlaygroundTest {

    public TemplatePlaygroundTest(){
    	this(1);
    }
    
    public TemplatePlaygroundTest(int i){
    	if(1==1){
    		throw new IllegalArgumentException("Final speed can not be less than zero");
    	}
    }

    public int mid(int x, int y, int z){

        int ret;
	ret = z;
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


