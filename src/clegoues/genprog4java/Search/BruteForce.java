package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.util.Pair;

public class BruteForce<G extends EditOperation> extends Search<G> {

	public BruteForce(Fitness<G> engine) {
		super(engine);
	}

	private boolean doWork(Representation<G> rep, Representation<G> original,
			Mutation mut, int first, int second) {
		rep.performEdit(mut, first, second);
		if (fitnessEngine.testToFirstFailure(rep, false)) {
			this.noteSuccess(rep, original, 1);
			if (!Search.continueSearch) {
				return true;
			}
		}
		return false;
	}
	private TreeSet<WeightedAtom> rescaleAtomPairs(ArrayList<WeightedAtom> arrayList) {
		double fullSum = 0.0;
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for (Pair<?, Double> item : arrayList) {
			fullSum += item.getSecond();
		}
		double scale = 1.0 / fullSum;
		for (WeightedAtom item : arrayList) {
			WeightedAtom newItem = new WeightedAtom(item.getAtom(),
					item.getWeight() * scale);
			retVal.add(newItem);
		}
		return retVal;
	}



	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException {
		return null;
	}

	@Override
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation)
			throws RepairFoundException {
		original.reduceFixSpace();

		int count = 0;
		ArrayList<WeightedAtom> allFaultyAtoms = new ArrayList<WeightedAtom>(
				original.getFaultyAtoms());

		for (WeightedAtom faultyAtom : allFaultyAtoms) {
			int faultyLocation = faultyAtom.getAtom();

			for(Map.Entry mutation : availableMutations.entrySet()) {
				Mutation key = (Mutation) mutation.getKey();
				Double prob = (Double) mutation.getValue();
				if(prob > 0.0) {
					count += original.editSources(faultyLocation, key).size(); 
				}
			}

		}
		logger.info("search: bruteForce: " + count + " mutants in search space\n");

		int wins = 0;
		int sofar = 1;
		boolean repairFound = false;

		TreeSet<WeightedAtom> rescaledAtoms = rescaleAtomPairs(allFaultyAtoms);

		for (WeightedAtom faultyAtom : rescaledAtoms) {
			int stmt = faultyAtom.getAtom();
			double weight = faultyAtom.getWeight();
			Comparator<Pair<Mutation, Double>> descendingMutations = new Comparator<Pair<Mutation, Double>>() {
				@Override
				public int compare(Pair<Mutation, Double> one,
						Pair<Mutation, Double> two) {
					return (new Double(two.getSecond())).compareTo((new Double(
							one.getSecond())));
				}
			};
			// wouldn't real polymorphism be the actual legitimate best right
			// here?
			TreeSet<clegoues.util.Pair<Mutation, Double>> availableMutations = original
					.availableMutations(stmt);
			TreeSet<Pair<Mutation, Double>> rescaledMutations = new TreeSet<Pair<Mutation, Double>>(
					descendingMutations);
			double sumMutScale = 0.0;
			for (Pair<Mutation, Double> item : availableMutations) {
				sumMutScale += item.getSecond();
			}
			double mutScale = 1 / sumMutScale;
			for (Pair<Mutation, Double> item : availableMutations) {
				rescaledMutations.add(new Pair<Mutation, Double>(item
						.getFirst(), item.getSecond() * mutScale));
			}

			// rescaled Mutations gives us the mutation,weight pairs available
			// at this atom
			// which itself has its own weight
			Comparator<WeightedAtom> descendingAtom = new Comparator<WeightedAtom>() {
				@Override
				public int compare(WeightedAtom one, WeightedAtom two) {
					return (new Double(two.getWeight())).compareTo((new Double(
							one.getWeight())));
				}
			};
			for (Pair<Mutation, Double> mutation : rescaledMutations) {
				Mutation mut = mutation.getFirst();
				double prob = mutation.getSecond();
				logger.info(weight + " " + prob);
				switch(mut) {
				case DELETE:
					Representation<G> delRep = original.copy();
					if (this.doWork(delRep, original, mut, stmt, stmt)) {
						wins++;
						repairFound = true;
					}
					break;
				case APPEND:
				case REPLACE:
				case OFFBYONE:
					TreeSet<WeightedAtom> sources1 = new TreeSet<WeightedAtom>(
							descendingAtom);
					sources1.addAll(this.rescaleAtomPairs(original
							.editSources(stmt, mut)));
					for (WeightedAtom append : sources1) {
						Representation<G> rep = original.copy();
						if (this.doWork(rep, original, mut, stmt,
								append.getAtom())) {
							wins++;
							repairFound = true;
						}
					}
					break;
				case SWAP:
					TreeSet<WeightedAtom> sources = new TreeSet<WeightedAtom>(
							descendingAtom);
					sources.addAll(this.rescaleAtomPairs(original
							.editSources(stmt, mut)));
					for (WeightedAtom append : sources) {
						Representation<G> rep = original.copy();
						if (this.doWork(rep, original, mut, stmt,
								append.getAtom())) {
							wins++;
							repairFound = true;
						}
					}
					break;
				default:
					logger.fatal("FATAL: unhandled template type in bruteForceOne.  Add handling (probably by adding a case either to the DELETE case or the other one); and try again");
					break;
				}
			}

			sofar++;
			if (repairFound && !Search.continueSearch) {
				throw new RepairFoundException();
			}
		}
		logger.info("search: brute_force_1 ends\n");
		if(repairFound) 
			throw new RepairFoundException();		
	}
}
