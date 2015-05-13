package test;

public class NullCheckTest {

	
	private static class TEST{
		
		
		public void print(){
			System.out.println("This has printed");
		}
	}
	
	public static void main(String args[]){
		TEST t = new TEST();
		if(t != null)
			t.print();
	}
}
