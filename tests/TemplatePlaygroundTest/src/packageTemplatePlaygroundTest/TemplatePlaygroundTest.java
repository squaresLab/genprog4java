package packageTemplatePlaygroundTest;

public class TemplatePlaygroundTest {

    public int mid(int x, int y, int z){

        int ret;
	ret = z;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		String s = "Hello";
		String s1 = null;
		s1 = s.replaceAll("o", "").toString();
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
