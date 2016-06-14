package packageTemplatePlaygroundTest;

public class TemplatePlaygroundTest {

	
    public int mid(int x, int y, int z){

    	int ret = z;
    	retTwo(2);
    	retThree();
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		 retOne(5);
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
    
    public void retOne(int foo){
	int i = 0;
	
	int e = 3;	
	double r = 5;
	String w = "Ello";
 	TemplatePlaygroundTest ww = new TemplatePlaygroundTest();
    }
    
    public void retTwo(int bar){
	int i = 0;
	
	int e = 3;	
	double r = 5;
	String w = "Ello";
 	TemplatePlaygroundTest ww = new TemplatePlaygroundTest();
    }
    public void retThree(){
	int i = 0;
	
	int e = 3;	
	double r = 5;
	String w = "Ello";
 	TemplatePlaygroundTest ww = new TemplatePlaygroundTest();
    }
}


