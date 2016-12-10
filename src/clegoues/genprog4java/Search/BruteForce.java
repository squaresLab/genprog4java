package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedHole;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;

@SuppressWarnings("rawtypes")
public class BruteForce<G extends EditOperation> extends Search<G> {

	public BruteForce(Fitness engine) {
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

	
	private List<Location> rescaleLocations(List<Location> items) {
		double fullSum = 0.0;
		List<Location> retVal = new LinkedList<Location>();
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
	private List<WeightedHole> rescaleAtomPairs(List<WeightedHole> items) {
		double fullSum = 0.0;
		List<WeightedHole> retVal = new ArrayList<WeightedHole>();
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

	// FIXME: this thing is such a mess.  And I think it's broken.  But we don't use it anyway, so who cares?
	@Override
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation)
			throws RepairFoundException, GiveUpException {
		original.reduceSearchSpace();
		Localization localization = original.getLocalization();

		int count = 0;
		List<Location> allFaultyLocations = localization.getFaultLocalization();

		for (Location faultyLocation : allFaultyLocations) {

			for(WeightedMutation mutation : availableMutations) {
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

		List<Location> rescaledAtoms = rescaleLocations(allFaultyLocations);

		for (Location faultyLocation : rescaledAtoms) {
			Comparator<Pair<Mutation, Double>> descendingMutations = new Comparator<Pair<Mutation, Double>>() {
				@Override
				public int compare(Pair<Mutation, Double> one,
						Pair<Mutation, Double> two) {
					return (new Double(two.getRight())).compareTo((new Double(
							one.getRight())));
				}
			};
			// wouldn't real polymorphism be the actual legitimate best right
			// here?
			List<WeightedMutation> availableMutations = original
					.availableMutations(faultyLocation);
			List<WeightedMutation> rescaledMutations = new LinkedList<WeightedMutation>();
			double sumMutScale = 0.0;
			for (Pair<Mutation, Double> item : availableMutations) {
				sumMutScale += item.getRight();
			}
			double mutScale = 1 / sumMutScale;
			for (Pair<Mutation, Double> item : availableMutations) {
				rescaledMutations.add(new WeightedMutation(item
						.getLeft(), item.getRight() * mutScale));
			}

			Collections.sort(rescaledMutations, descendingMutations);
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
				Mutation mut = mutation.getLeft();
				double prob = mutation.getRight();
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
					List<EditHole> sources1 = new ArrayList<EditHole>();
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
