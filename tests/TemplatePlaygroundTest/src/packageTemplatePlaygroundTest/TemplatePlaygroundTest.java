package packageTemplatePlaygroundTest;

public class TemplatePlaygroundTest {

	
    public int mid(int x, int y, int z){

    	int[] foo = new int[5];
    	int ret = z;
    	int bar = 2;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		   foo[bar] = 0;
		ret = y; // bug, it should be ret = x;
		// retOne();
		
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
    
    public void retOne(){
	int i = 0;
	
	int e = 3;	
	double r = 5;
	String w = "Ello";
 	TemplatePlaygroundTest ww = new TemplatePlaygroundTest();
    }
}


