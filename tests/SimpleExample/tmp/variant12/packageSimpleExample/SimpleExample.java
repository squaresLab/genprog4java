package packageSimpleExample;

public class SimpleExample {

    public int mid(int x, int y, int z){

        int ret;
	ret = z;
	if(y<z){
	   if(x<y){
		ret = y;
	   }else if(x<z){
		ret = y;
	   }
	}else{
	   {
		ret = y;
	}	
	}
	return ret;
    }
}
