package clegoues.genprog4java.Search;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedHole;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.util.Pair;

@SuppressWarnings("rawtypes")
public class BruteForce<G extends EditOperation> extends Search<G> {

	public BruteForce(Fitness<G> engine) {
		super(engine);
	}
	private boolean doWork(Representation<G> rep, Representation<G> original,
			Mutation mut, Location first, EditHole fixCode) {
		rep.performEdit(mut, first, fixCode);
		if (fitnessEngine.testToFirstFailure(rep, false)) {
			this.noteSuccess(rep, original, 1);
			if (!Search.continueSearch) {
				return true;
			}
		}
		return false;
	}

	
	private TreeSet<Location> rescaleLocations(TreeSet<Location> items) {
		double fullSum = 0.0;
		TreeSet<Location> retVal = new TreeSet<Location>();
		for (Location item : items) {
			fullSum += item.getWeight();
		}
		double scale = 1.0 / fullSum;
		for (Location item : items) { 
			Location newItem = item;
			newItem.setLocation(item.getLocation());
			newItem.setWeight(item.getWeight() * scale);
			retVal.add(newItem);
		}
		return retVal;
	}
	private TreeSet<WeightedHole> rescaleAtomPairs(TreeSet<WeightedHole> items) {
		double fullSum = 0.0;
		TreeSet<WeightedHole> retVal = new TreeSet<WeightedHole>();
		for (WeightedHole item : items) {
			fullSum += item.getWeight();
		}
		double scale = 1.0 / fullSum;
		for (WeightedHole item : items) { 
			WeightedHole newItem = item;
			newItem.setWeight(item.getWeight() * scale);
			retVal.add(newItem);
		}
		return retVal;
	}



	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException {
		return null;
	}

	// FIXME: this thing is such a mess.
	@Override
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation)
			throws RepairFoundException {
		original.reduceSearchSpace();

		int count = 0;
		TreeSet<Location> allFaultyLocations = new TreeSet<Location>(
				original.getFaultyLocations());

		for (Location faultyLocation : allFaultyLocations) {

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

		TreeSet<Location> rescaledAtoms = rescaleLocations(allFaultyLocations);

		for (Location faultyLocation : rescaledAtoms) {
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
			TreeSet<Pair<Mutation, Double>> availableMutations = original
					.availableMutations(faultyLocation);
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
				logger.info(faultyLocation.getWeight() + " " + prob);
				
				switch(mut) {
				case DELETE:
					Representation<G> delRep = original.copy();
					if (this.doWork(delRep, original, mut, faultyLocation, null)) {
						wins++;
						repairFound = true;
					}
					break;
				case APPEND:
				case REPLACE:
					TreeSet<EditHole> sources1 = new TreeSet<EditHole>();
					// FIXME: HACK: fix this hard-coding of the hole name, and below, too
					for(WeightedHole hole : this.rescaleAtomPairs(original
							.editSources(faultyLocation, mut))) {
						sources1.add(hole.getHole());
					}
					for (EditHole append : sources1) {
						Representation<G> rep = original.copy();
						if (this.doWork(rep, original, mut, faultyLocation,
								append)) {
							wins++;
							repairFound = true;
						}
					}
					break;
				case SWAP:
					TreeSet<EditHole> sources = new TreeSet<EditHole>();
					for(WeightedHole hole : this.rescaleAtomPairs(original
							.editSources(faultyLocation, mut))) {
						sources.add(hole.getHole());
					}
					for (EditHole append : sources) {
						Representation<G> rep = original.copy();
						if (this.doWork(rep, original, mut, faultyLocation, append)) {
							wins++;
							repairFound = true;
						}
					}
					break;
				case OFFBYONE: 
				default:
					logger.fatal("FATAL: unhandled template type in bruteForceOne.  Add handling (probably by adding a case either to the DELETE case or the other one); and try again");
					break;
				}


			}
			// FIXME: debug output System.out.printf("\t variant " + wins +
			// "/" + sofar + "/" + count + "(w: " + probs +")" +
			// rep.getName());
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
