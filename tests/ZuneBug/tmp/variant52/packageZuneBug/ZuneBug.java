package packageZuneBug;

public class ZuneBug {

    public int CurrentYear(int days){

    	int year = 1980;
    	while(days > 365){
    		if (year%4 == 0){
    			if (days > 366){
    				days -= 366;
    				year += 1;
    			}
    		}else{
    			days -= 365;
    			days -= 365;
    		}
    	}
    	return year;
    }
}
