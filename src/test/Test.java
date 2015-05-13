package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.ScopeInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.genprog4java.rep.WeightedAtom;

public class Test {

	
	public static void main(String args[]) throws IOException, UnexpectedCoverageResultException{
		System.out.println(args[0]);
		
		Configuration.setProperties(args[0]);
		File workDir = new File(Configuration.outputDir);
		if(!workDir.exists())
			workDir.mkdir();
		System.out.println("Configuration file loaded");
		
		Representation rep = new JavaRepresentation();
		Fitness fitnessEngine = new Fitness<JavaEditOperation>();
		Search searchEngine = new Search<JavaEditOperation>(fitnessEngine);
		//computes localization, and parses the file into an AST.
		
		rep.load(Configuration.targetClassName);
		
		ArrayList<WeightedAtom> faultyAtoms = rep.getFaultyAtoms();
		ArrayList<Integer> posAtom = new ArrayList<Integer>();
		
		for(WeightedAtom watom : faultyAtoms){
			if(rep.replaceSources(watom.getAtom()).size() > 0)
				posAtom.add(watom.getAtom());
		}
		
		if(posAtom.size() > 0){
			TreeSet<WeightedAtom> sources = rep.replaceSources(posAtom.get(0));
			System.out.println((new ArrayList<WeightedAtom>(sources)).get(0).getAtom());
			System.out.println(posAtom.get(5));
			rep.append(posAtom.get(0), (new ArrayList<WeightedAtom>(sources)).get(5).getAtom());
			
			String s = CachingRepresentation.newVariant();
			rep.compile(s, s);
		}
		

	}
	
}
