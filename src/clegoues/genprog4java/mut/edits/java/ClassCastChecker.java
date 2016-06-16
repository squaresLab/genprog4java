package clegoues.genprog4java.mut.edits.java;

public class ClassCastChecker {
/*	[Class Cast Checker]
			B = buggy statements
			collect class-casting operators of B into collection C
			insert a if statement before B

			loop for all castees in C
			{
			 insert a conditional expression that checks whether a castee implements its casting type
			}
			concatenate conditions using AND

			if B include return statement
			{
			 negate the concatenated the conditional expression
			 insert a return statement that returns a default value into THEN section of the if statement
			 insert B after the if statement
			} else {
			 insert B into THEN section of the if statement
			}*/
}
