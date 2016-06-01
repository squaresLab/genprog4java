package packageTemplatePlaygroundTest;

import java.io.IOException;

public class TemplatePlaygroundTest {

    public int mid(int x, int y, int z){
    	int ret = z;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		//ret = y; // bug, it should be ret = x;
		try{
		retOne();
		}catch(IOException e){
			e.printStackTrace();
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
    public void retOne() throws IOException{
	int i = 0;

		

	int e = 3;	
	int r = 5;
	int w = e-r;
 	int ww = e+r;
throw new IOException();
    }
}


