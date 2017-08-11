package clegoues.genprog4java.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import clegoues.genprog4java.localization.UnexpectedCoverageResultException;

public class Run {
	
	public static void main(String[] args) throws FileNotFoundException{
		if (args.length < 4) {
			System.err.println(
					"<projectName> <iteration in projectID> <Path to defect4j projects> <Path to where output will be stored>");
			return;
		}
		String projectName = args[0];
		int limit = Integer.parseInt(args[1]);
		//examplePath goes to the directory with projects checked out by Defects4j
		//outputPath goes to the directory where output results are appended
		String examplePath = args[2];
		String outputPath = args[3]; 
		
		int i = limit;
		//for(int i=1;i<limit+1;i++){
			String[] arg = {examplePath + "/"+ projectName.toLowerCase()+ Integer.toString(i)+"Buggy/defects4j.config"};
			PrintStream out = new PrintStream(new FileOutputStream(outputPath+ "/" +
																	projectName + Integer.toString(i) + "_Probability.txt"));
			
			//Saves results from EntropyLocalization into txt file
			//System.setOut(out); //Can be commented when debugging
			try {
				Main.main(arg);
			} catch (Exception e) {
				System.err.println("Error completed");
			}
			System.err.println("Finished iter: " + Integer.toString(i));
			System.err.println(limit+1);
		//}
	}
}
























