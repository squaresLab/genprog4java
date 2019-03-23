package ylyu1.morewood;

import java.util.*;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.localization.UnexpectedCoverageResultException;
import clegoues.genprog4java.main.*;
import clegoues.genprog4java.rep.Representation;

import java.io.*;

import weka.clusterers.*;
import weka.core.*;


public class MethodTracker {
	
	public static Map<TestCase,Set<String>> mcov = new HashMap<TestCase,Set<String>>();
	
	public static void printmcov() {
		for(TestCase tc : mcov.keySet()) {
			System.out.println("Test Case: "+ tc.getTestName());
			System.out.println("MCOUNT: "+mcov.get(tc).size());
			for(String s : mcov.get(tc)) {
				System.out.println(s);
			}
		}
	}
	
	public static Set<TestCase> selectTests(int m, int t) {
		List<List<String>> clusters = null;
		try {
			clusters = kmeansOut(m,t);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		Set<TestCase> selected = new HashSet<TestCase>();
		for(List<String> cluster : clusters) {
			//sort testcases based on how many methods visited in cluster
			CompMethod[] cms = new CompMethod[poslist.size()];
			for(int i = 0; i < cms.length; i++) {
				cms[i] = new CompMethod(cluster, mcov.get(poslist.get(i)),poslist.get(i));
			}
			Arrays.sort(cms);
			
			//greedily takes methods
			Map<String, Integer> tcount = new HashMap<String, Integer>();
			for(String s : cluster) {
				tcount.put(s, 0);
			}
			for(int i = 0; i < cms.length; i++) {
				if(cms[i].match==0)break;
				selected.add(cms[i].tc);
				for(String s : cms[i].matched) {
					tcount.put(s, tcount.get(s)+1);
				}
				if(tclear(tcount,t))break;
			}
		}
		return selected;
	}
	
	public static boolean tclear(Map<String, Integer> m, int t) {
		for(Integer i : m.values()) {
			if(i<t)return false;
		}
		return true;
	}
	
	public static ArrayList<TestCase> poslist = null;
	
	public static List<List<String>> kmeansOut(int m, int t) throws Exception {
		//build kmeans
		Map<String, List<Integer>> vectors = vectorize();
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setNumClusters(vectors.keySet().size()/m);
		ArrayList<Attribute> att = new ArrayList<Attribute>();
		for(int i = 0; i < poslist.size();i++) {
			att.add(new Attribute("POSTEST"+i));
		}
		Instances dataa = new Instances("SOMENAME",att,vectors.keySet().size());
		Map<Instance,String> positionmap = new HashMap<Instance, String>();
		for(String method : vectors.keySet()) {
			Instance inst = new DenseInstance(poslist.size());
			for(int i = 0; i < poslist.size(); i++) {
				inst.setValue(att.get(i), vectors.get(method).get(i));
			}
			dataa.add(inst);
			positionmap.put(inst, method);
		}
		kmeans.buildClusterer(dataa);
		
		//split into clusters
		Map<Integer,List<Instance>> clusters = new HashMap<Integer,List<Instance>>();
		for(Instance inst : dataa) {
			int cl = kmeans.clusterInstance(inst);
			if(!clusters.containsKey(cl)) {
				clusters.put(cl, new ArrayList<Instance>());
			}
			clusters.get(cl).add(inst);
		}
		
		//split clusters of size over m into size under m
		List<List<String>> mclusters = new ArrayList<List<String>>();
		Random rand = new Random(Configuration.seed);
		for(List<Instance> cluster : clusters.values()) {
			List<List<Instance>> splitted = new ArrayList<List<Instance>>();
			List<Instance> current = new ArrayList<Instance>();
			current.addAll(cluster);
			while(true) {
				if(current.size()<=m) {
					splitted.add(current);
					break;
				}
				else {
					List<Instance> split = new ArrayList<Instance>();
					for(int i = 0; i < m; i++) {
						int index = rand.nextInt(current.size());
						split.add(current.get(index));
						current.remove(index);
					}
					splitted.add(split);
				}
			}
			for(List<Instance> c : splitted) {
				mclusters.add(i2s(c,positionmap));
			}
		}
		
		return mclusters;
	}
	
	public static List<String> i2s(List<Instance> inst, Map<Instance,String> m) {
		ArrayList<String> arr = new ArrayList<String>();
		for(Instance i : inst) {
			arr.add(m.get(i));
		}
		return arr;
	}
	
	public static Map<String, List<Integer>> vectorize(){
		Map<String, List<Integer>> vectors = new HashMap<String, List<Integer>>();
		poslist = new ArrayList<TestCase>();
		for(TestCase tc : mcov.keySet()) {
			if(Fitness.negativeTests.contains(tc)) {
				for(String method : mcov.get(tc)) {
					if(!vectors.containsKey(method)) {
						vectors.put(method, new ArrayList<Integer>());
					}
				}
			}
			else {
				poslist.add(tc);
			}
		}
		for(TestCase tc : poslist) {
			for(String method : vectors.keySet()) {
				if(mcov.get(tc).contains(method)) {
					vectors.get(method).add(1);
				}
				else {
					vectors.get(method).add(0);
				}
			}
		}
		return vectors;
	}
}
