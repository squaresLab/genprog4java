package packageSimpleExample;

public class SimpleExample {

    public int mid(int x, int y, int z){

	final String noUse = "NoUse";
        int ret;
	ret = z;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		String s = null;
		String len = s.replaceAll("", noUse).toString();
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
/*
    public static void main(String[] args) {
        SimpleExample s = new SimpleExample();
	println("The middle value is: " + s.mid(2,3,1));
    }
*/
}
