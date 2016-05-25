package packageTemplatePlaygroundTest;

public class TemplatePlaygroundTest {

    public void mid(int x, int y, int z){
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
	return ;
    }
    public int retOne(){
	return 1;
    }
}


