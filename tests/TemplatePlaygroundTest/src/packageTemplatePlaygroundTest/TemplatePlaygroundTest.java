package packageTemplatePlaygroundTest;

public class TemplatePlaygroundTest {

    public int mid(int x, int y, int z){
    	int ret = z;
    	int[] foo = new int[5];

	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z  && y > z){
		//ret = y; // bug, it should be ret = x;
		   if(z > x) {
		   retOne(5);
		   }
	   }
	}else{
	   if(x>y){
		ret = y;
		
	   }else if(x>z){
		ret = x;
		
	   }	
	}
	retOne(6);
	return ret;
    }
    public void retOne(int foo){
	int i = 0;
	
	int e = 3;	
	double r = 5;
	String w = "Ello";
 	TemplatePlaygroundTest ww = new TemplatePlaygroundTest();
    }
    
    public void retTwo(int foo){
    	int i = 0;
    	
    	int e = 3;	
    	double r = 5;
    	String w = "Ello";
     	TemplatePlaygroundTest ww = new TemplatePlaygroundTest();
        }
}


