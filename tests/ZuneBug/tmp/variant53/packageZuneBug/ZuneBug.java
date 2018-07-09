package packageZuneBug;

public class ZuneBug {

    public int CurrentYear(int days){

    	days -= 365;
    	while(days > 365){
    		if (year%4 == 0){
    			if (days > 366){
    				days -= 366;
    				year += 1;
    			}
    		}else{
    			days -= 365;
    			year += 1;
    		}
    	}
    	return year;
    }
}
