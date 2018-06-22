package packageSimpleExample;

public class SimpleExample {

    public int mid(int x, int y, int z){

        int ret;
	ret = z;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		ret = z; // bug, it should be ret=x;
	   }
	}else{
	   if(x>y) {
		if (x < y) {
			ret = y;
		} else if (x < z) {
			ret = z;
		}
	} else if(x>z){
		ret = x;
	   }	
	}
	return ret;
    }
}
