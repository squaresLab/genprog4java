package clegoues.genprog4java.java;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.treelm.SymbolTable;

public class JavaLMSymbolTable implements SymbolTable {
	private Location babbleScope;
	
	public JavaLMSymbolTable (Location startingPoint) {
		this.babbleScope = startingPoint;
	}
	
	private static int identifier;
	private String allocFreeNameSupplier(String type, int index) {
		Set<String> classScope = new HashSet<String>(JavaSemanticInfo.classScopeMap.get(index));
		Set<String> methodScope = JavaSemanticInfo.methodScopeMap.get(index);
		classScope.addAll(methodScope);
		String newName = "newVar" + identifier++;
		while(classScope.contains(newName)) {
			newName = "newVar" + identifier++;
		}
		methodScope.add(newName);
		return newName;
	}
	

	//FIXME/question for Dorn: should this actually create the variable, or just come up with a unique name?
	@Override
	public Supplier<String> allocFreeName(String type) {
		int index = babbleScope.getId();
		return () -> allocFreeNameSupplier(type, index);
	}
	
	// FIXME: this won't do the right thing for int x = [] + 1; (that is, it might return x)
	private String getNameForTypeSupplier(String type) {
		if(JavaSemanticInfo.inverseVarDataTypeMap.containsKey(type)) {
			Set<String> possibleNames = JavaSemanticInfo.inverseVarDataTypeMap.get(type);
			int num = Configuration.randomizer.nextInt(possibleNames.size());
			for(String poss : possibleNames) {
				if(--num < 0) return poss;
			}
		}
		return null;
	}
	@Override
	public Supplier<String> getNameForType(String type) {
		return () -> getNameForTypeSupplier(type);
	}


	@Override
	public void enter(int nodeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leave(int nodeType) {
		// TODO Auto-generated method stub
		
	}

}
