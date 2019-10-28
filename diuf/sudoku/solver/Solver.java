/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2009 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

import java.security.*;
import java.util.*;

import diuf.sudoku.*;
//import diuf.sudoku.Settings.*;
import diuf.sudoku.solver.checks.*;
import diuf.sudoku.solver.rules.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.solver.rules.unique.*;
import diuf.sudoku.test.serate;
import diuf.sudoku.tools.*;

import java.io.PrintWriter;

/**
 * The solver for Sudoku grids.
 * Used to:
 * <ul>
 * <li>Build or rebuild the potential values of empty cells of a grid
 * <li>Get all available hints, excluding those requiring chaining rules
 * <li>Get the next available hint that follows a given list of hints, in
 * increasing order of difficulty
 * <li>Solve a grid using brute-force
 * <li>Solve a grid using logical hints, and get a rating of the grid as well as a
 * list of the rules that were used.
 * <li>Check the validity of a grid
 * </ul>
 * In all cases, a validity check is automatically enforced as soon as an invalid grid
 * would cause performance loss or any other problems.
 * <p>
 * The solving techniques themselves are implemented in the various classes of the
 * packages {@link diuf.sudoku.solver.rules}, {@link diuf.sudoku.solver.rules.chaining}
 * and {@link diuf.sudoku.solver.rules.unique}. Checks for validity are
 * implemented in classes of the package {@link diuf.sudoku.solver.checks}.
 */
public class Solver {

    private static final String ADVANCED_WARNING1 =
        "This Sudoku seems to require advanced techniques\n" +
        "that may take a very long computing time.\n" +
        "Do you want to continue anyway?";
    private static final String ADVANCED_WARNING2 =
        "The next solving techniques are advanced ones\n" +
        "that may take a very long computing time.\n" +
        "Do you want to continue anyway?";

    public double difficulty;
    public double pearl;
    public double diamond;
    public String ERtN;
    public String EPtN;
    public String EDtN;
    public String shortERtN;
    public String shortEPtN;
    public String shortEDtN;
    public char want;

    private Grid grid;
    private List<HintProducer> directHintProducers;
    private List<IndirectHintProducer> indirectHintProducers;
    private List<WarningHintProducer> validatorHintProducers;
    private List<WarningHintProducer> warningHintProducers;
    private List<IndirectHintProducer> chainingHintProducers;
    private List<IndirectHintProducer> chainingHintProducers2;
    private List<IndirectHintProducer> advancedHintProducers;
    private List<IndirectHintProducer> experimentalHintProducers;

    private boolean isUsingAdvanced = false;

    public Grid getGrid() {
    	return this.grid;
    }
    
    private class DefaultHintsAccumulator implements HintsAccumulator {

        private final List<Hint> result;

        private DefaultHintsAccumulator(List<Hint> result) {
            super();
            this.result = result;
        }

        public void add(Hint hint) throws InterruptedException {
            if (!result.contains(hint))
                result.add(hint);
        }

    } // class DefaultHintsAccumulator

	// lksudoku: batch mode accumulator, accumulate until higher
	// rating is added
    private class SmallestHintsAccumulator implements HintsAccumulator {

        private final List<Hint> result;

		// dif is 0.0 at start, and changes to first added rating
		private double dif = 0.0;

        private SmallestHintsAccumulator(List<Hint> result) {
            super();
            this.result = result;
        }

        public void add(Hint hint) throws InterruptedException {
        	double newDifficulty = ((Rule)hint).getDifficulty();
        	int batchMode = Settings.getInstance().batchSolving();
			if(dif == 0.0) {
				dif = newDifficulty;
			} else if((newDifficulty != dif && batchMode == 1) || (newDifficulty > difficulty && newDifficulty != dif && batchMode == 2)) {
				throw new InterruptedException(); // this assumes calls are ordered strictly ascending by difficulty
			}
            if(!result.contains(hint))
                result.add(hint);
        }

    } // class SmallestHintsAccumulator

    private void addIfWorth(SolvingTechnique technique, Collection<HintProducer> coll, HintProducer producer) {
        if (Settings.getInstance().getTechniques().contains(technique))
            coll.add(producer);
    }

    private void addIfWorth(SolvingTechnique technique, Collection<IndirectHintProducer> coll, IndirectHintProducer producer) {
        if (Settings.getInstance().getTechniques().contains(technique))
            coll.add(producer);
    }

    public Solver(Grid grid) {
        this.grid = grid;
        // These rules are not really solving techs. They check the validity of the puzzle
        validatorHintProducers = new ArrayList<WarningHintProducer>();
        validatorHintProducers.add(new NoDoubles());
        warningHintProducers = new ArrayList<WarningHintProducer>();
        warningHintProducers.add(new NumberOfFilledCells());
        warningHintProducers.add(new NumberOfValues());
        warningHintProducers.add(new BruteForceAnalysis(false));
        directHintProducers = new ArrayList<HintProducer>();
if (Settings.getInstance().revisedRating()==1) {
        addIfWorth(SolvingTechnique.HiddenSingle, directHintProducers, new HiddenSingle());
        addIfWorth(SolvingTechnique.NakedSingle, directHintProducers, new NakedSingle());
        addIfWorth(SolvingTechnique.DirectPointing, directHintProducers, new Locking(true));
        addIfWorth(SolvingTechnique.DirectHiddenPair, directHintProducers, new HiddenSet(2, true));
        indirectHintProducers = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.PointingClaiming, indirectHintProducers, new Locking(false));
        addIfWorth(SolvingTechnique.HiddenPair, indirectHintProducers, new HiddenSet(2, false));
        addIfWorth(SolvingTechnique.NakedPair, indirectHintProducers, new NakedSet(2));
        addIfWorth(SolvingTechnique.DirectHiddenPair, directHintProducers, new HiddenSet(3, true));
        addIfWorth(SolvingTechnique.XWing, indirectHintProducers, new Fisherman(2));
        addIfWorth(SolvingTechnique.NakedTriplet, indirectHintProducers, new NakedSet(3));
        addIfWorth(SolvingTechnique.HiddenTriplet, indirectHintProducers, new HiddenSet(3, false));
		addIfWorth(SolvingTechnique.TurbotFish, indirectHintProducers, new TurbotFish());
        addIfWorth(SolvingTechnique.Swordfish, indirectHintProducers, new Fisherman(3));
        addIfWorth(SolvingTechnique.XYWing, indirectHintProducers, new XYWing(false));
        addIfWorth(SolvingTechnique.XYZWing, indirectHintProducers, new XYWing(true));
//      addIfWorth(SolvingTechnique.WWing, indirectHintProducers, new WWing());		
        addIfWorth(SolvingTechnique.UniqueLoop, indirectHintProducers, new UniqueLoops());
        addIfWorth(SolvingTechnique.NakedQuad, indirectHintProducers, new NakedSet(4));
        addIfWorth(SolvingTechnique.HiddenQuad, indirectHintProducers, new HiddenSet(4, false));
        addIfWorth(SolvingTechnique.Jellyfish, indirectHintProducers, new Fisherman(4));
        addIfWorth(SolvingTechnique.WXYZWing, indirectHintProducers, new WXYZWing());
        addIfWorth(SolvingTechnique.BivalueUniversalGrave, indirectHintProducers, new BivalueUniversalGrave());
		addIfWorth(SolvingTechnique.VWXYZWing, indirectHintProducers, new VWXYZWing());
        addIfWorth(SolvingTechnique.AlignedPairExclusion, indirectHintProducers, new AlignedPairExclusion());
        chainingHintProducers = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.ForcingChainCycle, chainingHintProducers, new Chaining(false, false, false, 0, false, 0));
		addIfWorth(SolvingTechnique.AlignedTripletExclusion, chainingHintProducers, new AlignedExclusion(3));
        addIfWorth(SolvingTechnique.NishioForcingChain, chainingHintProducers, new Chaining(false, true, true, 0, false, 0));
        addIfWorth(SolvingTechnique.MultipleForcingChain, chainingHintProducers, new Chaining(true, false, false, 0, false, 0));
        addIfWorth(SolvingTechnique.DynamicForcingChain, chainingHintProducers, new Chaining(true, true, false, 0, false, 0));
        chainingHintProducers2 = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.DynamicForcingChainPlus, chainingHintProducers2, new Chaining(true, true, false, 1, false, 0));
        // These are very slow. We add them only as "rescue"
        advancedHintProducers = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.NestedForcingChain, advancedHintProducers, new Chaining(true, true, false, 2, false, 0));
        addIfWorth(SolvingTechnique.NestedForcingChain, advancedHintProducers, new Chaining(true, true, false, 3, false, 0));
        experimentalHintProducers = new ArrayList<IndirectHintProducer>(); // Two levels of nesting !?
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4, false, 0));
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4, false, 1));
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4, false, 2));
        //addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 5));
        //addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 6));
}
else {
        addIfWorth(SolvingTechnique.HiddenSingle, directHintProducers, new HiddenSingle());
        addIfWorth(SolvingTechnique.DirectPointing, directHintProducers, new Locking(true));
        addIfWorth(SolvingTechnique.DirectHiddenPair, directHintProducers, new HiddenSet(2, true));
        addIfWorth(SolvingTechnique.NakedSingle, directHintProducers, new NakedSingle());
        addIfWorth(SolvingTechnique.DirectHiddenTriplet, directHintProducers, new HiddenSet(3, true));
        indirectHintProducers = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.PointingClaiming, indirectHintProducers, new Locking(false));
        addIfWorth(SolvingTechnique.NakedPair, indirectHintProducers, new NakedSet(2));
        addIfWorth(SolvingTechnique.XWing, indirectHintProducers, new Fisherman(2));
        addIfWorth(SolvingTechnique.HiddenPair, indirectHintProducers, new HiddenSet(2, false));
        addIfWorth(SolvingTechnique.NakedTriplet, indirectHintProducers, new NakedSet(3));
        addIfWorth(SolvingTechnique.Swordfish, indirectHintProducers, new Fisherman(3));
        addIfWorth(SolvingTechnique.HiddenTriplet, indirectHintProducers, new HiddenSet(3, false));
		addIfWorth(SolvingTechnique.TurbotFish, indirectHintProducers, new TurbotFish());
        addIfWorth(SolvingTechnique.XYWing, indirectHintProducers, new XYWing(false));
        addIfWorth(SolvingTechnique.XYZWing, indirectHintProducers, new XYWing(true));
//        addIfWorth(SolvingTechnique.WWing, indirectHintProducers, new WWing());
        addIfWorth(SolvingTechnique.UniqueLoop, indirectHintProducers, new UniqueLoops());
        addIfWorth(SolvingTechnique.NakedQuad, indirectHintProducers, new NakedSet(4));
        addIfWorth(SolvingTechnique.Jellyfish, indirectHintProducers, new Fisherman(4));
        addIfWorth(SolvingTechnique.HiddenQuad, indirectHintProducers, new HiddenSet(4, false));
        addIfWorth(SolvingTechnique.ThreeStrongLinks, indirectHintProducers, new ThreeStrongLinks());
		addIfWorth(SolvingTechnique.WXYZWing, indirectHintProducers, new WXYZWing());
        //addIfWorth(SolvingTechnique.VWXYZWing4, indirectHintProducers, new VWXYZWing(true));
		//addIfWorth(SolvingTechnique.VWXYZWing5, indirectHintProducers, new VWXYZWing(false));
        addIfWorth(SolvingTechnique.BivalueUniversalGrave, indirectHintProducers, new BivalueUniversalGrave());
        addIfWorth(SolvingTechnique.VWXYZWing, indirectHintProducers, new VWXYZWing());
        addIfWorth(SolvingTechnique.AlignedPairExclusion, indirectHintProducers, new AlignedPairExclusion());
        chainingHintProducers = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.ForcingChainCycle, chainingHintProducers, new Chaining(false, false, false, 0, false, 0));
        addIfWorth(SolvingTechnique.AlignedTripletExclusion, chainingHintProducers, new AlignedExclusion(3));
        addIfWorth(SolvingTechnique.NishioForcingChain, chainingHintProducers, new Chaining(false, true, true, 0, false, 0));
        addIfWorth(SolvingTechnique.MultipleForcingChain, chainingHintProducers, new Chaining(true, false, false, 0, false, 0));
        addIfWorth(SolvingTechnique.DynamicForcingChain, chainingHintProducers, new Chaining(true, true, false, 0, false, 0));
        chainingHintProducers2 = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.DynamicForcingChainPlus, chainingHintProducers2, new Chaining(true, true, false, 1, false, 0));
        // These are very slow. We add them only as "rescue"
        advancedHintProducers = new ArrayList<IndirectHintProducer>();
        addIfWorth(SolvingTechnique.NestedForcingChain, advancedHintProducers, new Chaining(true, true, false, 2, false, 0));
        addIfWorth(SolvingTechnique.NestedForcingChain, advancedHintProducers, new Chaining(true, true, false, 3, false, 0));
        experimentalHintProducers = new ArrayList<IndirectHintProducer>(); // Two levels of nesting !?
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4, false, 0));
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4, false, 1));
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4, false, 2));
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4, false, 3)); //MD: highly experimental
        //addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 5));
        //addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 6));
}
	}
//    /**
//     * This is the basic Sudoku rule: If a cell contains a value,
//     * that value can be removed from the potential values of
//     * all cells in the same block, row or column.
//     * @param partType the Class of the part to cancel in
//     * (block, row or column)
//     */
//    private void cancelBy(int partTypeIndex) {
//        Grid.Region[] parts = Grid.getRegions(partTypeIndex);
//        for (Grid.Region part : parts) {
//            for (int i = 0; i < 9; i++) {
//                Cell cell = part.getCell(i);
//                int value = grid.getCellValue(cell.getIndex());
//                if (value != 0) {
//                    // Remove the cell value from the potential values of other cells
//                    for (int j = 0; j < 9; j++)
//                    	grid.removeCellPotentialValue(part.getCell(j).getIndex(), value);
//                }
//            }
//        }
//    }

    /**
     * Rebuild, for each empty cell, the set of potential values.
     */
    public void rebuildPotentialValues() {
        for (int i = 0; i < 81; i++) {
            if (grid.getCellValue(i) == 0) {
                for (int value = 1; value <= 9; value++)
                	grid.addCellPotentialValue(i, value);
            }
        }
        cancelPotentialValues();
    }

    /**
     * Remove all illegal potential values according
     * to the current values of the cells.
     * Can be invoked after a new cell gets a value.
     */
    public void cancelPotentialValues() {
        for(int i = 0; i < 81; i++) {
        	int value = grid.getCellValue(i);
            if(value == 0) continue;
        	grid.clearCellPotentialValues(i);
        	for(int visible : Grid.visibleCellIndex[i]) {
        		grid.removeCellPotentialValue(visible, value);
        	}
        }
        //cancelBy(0); //block
        //cancelBy(1); //row
        //cancelBy(2); //column
    }

    /**
     * Lower the current thread's priority.
     * @return the previous thread's priority
     */
    private int lowerPriority() {
        try {
            int result = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY * 2) / 3);
            return result;
        } catch (AccessControlException ex) {}
        return 0;
    }

    /**
     * Reset the current thread's priority to the given value.
     * Typically, the given value is the value returned by
     * {@link #lowerPriority()}.
     * @param priority the new priority
     */
    private void normalPriority(int priority) {
        try {
            Thread.currentThread().setPriority(priority);
        } catch (AccessControlException ex) {}
    }

    /**
     * Get the first available validity warning hint.
     * This can be used to check the validity of a
     * Sudoku grid. If the sudoku is valid, <code>null</code>
     * is returned; else, a warning hint.
     * @return a warning hint if the sudoku is invalid, <code>null</code>
     * if the sudoku is valid.
     */
    public Hint checkValidity() {
        int oldPriority = lowerPriority();
        SingleHintAccumulator accu = new SingleHintAccumulator();
        try {
            for (WarningHintProducer producer : validatorHintProducers)
                producer.getHints(grid, accu);
            for (WarningHintProducer producer : warningHintProducers)
                producer.getHints(grid, accu);
        } catch (InterruptedException willProbablyHappen) {}
        normalPriority(oldPriority);
        return accu.getHint();
    }

    private void gatherProducer(List<Hint> previousHints, List<Hint> curHints,
            HintsAccumulator accu, HintProducer producer) throws InterruptedException {
        // Get last hint producer. Because the last producer may not have produced
        // all its hints, we will need to restart from scratch with it.
        HintProducer lastProducer = null;
        if (!previousHints.isEmpty())
            lastProducer = previousHints.get(previousHints.size() - 1).getRule();

        if (curHints.size() < previousHints.size() && producer != lastProducer) {
            // Reuse previously computed hints of this producer
            Hint hint = null;
            hint = previousHints.get(curHints.size());
            while (hint.getRule() == producer) {
                accu.add(hint);
                hint = previousHints.get(curHints.size());
            }
        } else
            // Compute now
            producer.getHints(grid, accu);
    }

    public void gatherHints(List<Hint> previousHints, final List<Hint> result,
            HintsAccumulator accu, Asker asker) {

        int oldPriority = lowerPriority();
        boolean isAdvanced = false;
        try {
            for (HintProducer producer : directHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            for (HintProducer producer : indirectHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            for (HintProducer producer : validatorHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            if (result.isEmpty()) {
                for (HintProducer producer : warningHintProducers)
                    gatherProducer(previousHints, result, accu, producer);
            }
            for (HintProducer producer : chainingHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            for (HintProducer producer : chainingHintProducers2)
                gatherProducer(previousHints, result, accu, producer);
            boolean hasWarning = false;
            for (Hint hint : result) {
                if (hint instanceof WarningHint)
                    hasWarning = true;
            }
            // We have not been interrupted yet. So no rule has been found yet
            if (!hasWarning &&
                    !(advancedHintProducers.isEmpty() && experimentalHintProducers.isEmpty()) &&
                    (isUsingAdvanced || asker.ask(ADVANCED_WARNING2))) {
                isAdvanced = true;
                isUsingAdvanced = true;
                for (HintProducer producer : advancedHintProducers)
                    gatherProducer(previousHints, result, accu, producer);
                for (HintProducer producer : experimentalHintProducers) {
                    if (result.isEmpty() && Settings.getInstance().isUsingAllTechniques())
                        gatherProducer(previousHints, result, accu, producer);
                }
            }
        } catch (InterruptedException willProbablyHappen) {}
        if (!isAdvanced)
            isUsingAdvanced = false;
        normalPriority(oldPriority);
    }

    public List<Hint> getAllHints(Asker asker) {
        int oldPriority = lowerPriority();
        List<Hint> result = new ArrayList<Hint>();
        HintsAccumulator accu = new DefaultHintsAccumulator(result);
        try {
            for (HintProducer producer : directHintProducers)
                producer.getHints(grid, accu);
            for (IndirectHintProducer producer : indirectHintProducers)
                producer.getHints(grid, accu);
            for (WarningHintProducer producer : validatorHintProducers)
                producer.getHints(grid, accu);
            if (result.isEmpty()) {
                for (WarningHintProducer producer : warningHintProducers)
                    producer.getHints(grid, accu);
            }
            if (result.isEmpty()) {
                for (IndirectHintProducer producer : chainingHintProducers)
                    producer.getHints(grid, accu);
            }
            if (result.isEmpty()) {
                for (IndirectHintProducer producer : chainingHintProducers2)
                    producer.getHints(grid, accu);
            }
            if (result.isEmpty() &&
                    !(advancedHintProducers.isEmpty() && experimentalHintProducers.isEmpty()) &&
                    (isUsingAdvanced || asker.ask(ADVANCED_WARNING2))) {
                isUsingAdvanced = true;
                for (IndirectHintProducer producer : advancedHintProducers) {
                    if (result.isEmpty())
                        producer.getHints(grid, accu);
                }
                for (IndirectHintProducer producer : experimentalHintProducers) {
                    if (result.isEmpty() && Settings.getInstance().isUsingAllTechniques())
                        producer.getHints(grid, accu);
                }
            }
        } catch (InterruptedException cannotHappen) {}
        normalPriority(oldPriority);
        return result;
    }

    private boolean isSolved() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (grid.getCellValue(x, y) == 0)
                    return false;
            }
        }
        return true;
    }

    private class RuleComparer implements Comparator<Rule> {

        public int compare(Rule r1, Rule r2) {
            double d1 = r1.getDifficulty();
            double d2 = r2.getDifficulty();
            if (d1 < d2)
                return -1;
            else if (d1 > d2)
                return 1;
            else
                return r1.getName().compareTo(r2.getName());
        }

    }

    /**
     * Solve the Sudoku passed to the constructor.
     * <p>
     * Returns a sorted map between the rules that were used and
     * their frequency. Rules are sorted by difficulty.
     * @return the map between used rules and their frequency
     * @throws UnsupportedOperationException if the Sudoku cannot
     * be solved without recursive guessing (brute-force).
     */
    public Map<Rule,Integer> solve(Asker asker) {
        int oldPriority = lowerPriority();
        // rebuildPotentialValues();
        Map<Rule,Integer> usedRules = new TreeMap<Rule,Integer>(new RuleComparer());
        boolean isUsingAdvanced = false;
        while (!isSolved()) {
            SingleHintAccumulator accu = new SingleHintAccumulator();
            try {
                for (HintProducer producer : directHintProducers)
                    producer.getHints(grid, accu);
                for (IndirectHintProducer producer : indirectHintProducers)
                    producer.getHints(grid, accu);
                for (IndirectHintProducer producer : chainingHintProducers)
                    producer.getHints(grid, accu);
                for (IndirectHintProducer producer : chainingHintProducers2)
                    producer.getHints(grid, accu);
                if (!(advancedHintProducers.isEmpty() && experimentalHintProducers.isEmpty()) &&
                        (asker == null || isUsingAdvanced || asker.ask(ADVANCED_WARNING1))) {
                    isUsingAdvanced = true;
                    for (IndirectHintProducer producer : advancedHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : experimentalHintProducers) {
                        if (Settings.getInstance().isUsingAllTechniques())
                            producer.getHints(grid, accu);
                    }
                }
            } catch (InterruptedException willHappen) {}
            Hint hint = accu.getHint();
            if (hint == null)
                throw new UnsupportedOperationException("Failed to solve this Sudoku");
            assert hint instanceof Rule;
            Rule rule = (Rule)hint;
            if (usedRules.containsKey(rule))
                usedRules.put(rule, usedRules.get(rule) + 1);
            else
                usedRules.put(rule, 1);
            hint.apply(grid);
        }
        normalPriority(oldPriority);
        return usedRules;
    }

    /**
     * Get whether the grid's difficulty is between the two
     * bounds or not. If yes, return the actual difficulty.
     * If no, return a value less than <tt>min</tt> if the
     * grid is less difficult than <tt>min</tt> and a value
     * greater than <tt>max</tt> if the grid is more
     * difficult than <tt>max</tt>.
     * @param min the minimal difficulty (inclusive)
     * @param max the maximal difficulty (inclusive)
     * @return The actual difficulty if it is between the
     * given bounds. An arbitrary out-of-bounds value else.
     */
    public double analyseDifficulty(double min, double max, double include1, double include2, double include3, double exclude1, double exclude2, double exclude3, double notMax1, double notMax2, double notMax3, String excludeT1, String excludeT2, String excludeT3, String includeT1, String includeT2, String includeT3, String notMaxT1, String notMaxT2, String notMaxT3, String oneOf3_1, String oneOf3_2, String oneOf3_3) {
        int oldPriority = lowerPriority();
        try {
            double difficulty = 0; //Double.NEGATIVE_INFINITY;
            boolean notMaxCounter = false;
			int inRateCounter = 0;
			boolean oneOfThreeCounter = false;
			if (include1 == 0.0) inRateCounter++;
			if (include2 == 0.0) inRateCounter++;
			if (include3 == 0.0) inRateCounter++;
			int inTechCounter = 0;
			if (Objects.equals(includeT1,"")) inTechCounter++;
			if (Objects.equals(includeT2,"")) inTechCounter++;
			if (Objects.equals(includeT3,"")) inTechCounter++;
			while (!isSolved()) {
                SingleHintAccumulator accu = new SingleHintAccumulator();
                try {
                    for (HintProducer producer : directHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : indirectHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers2)
                        producer.getHints(grid, accu);
                    // Only used for generator. Ignore advanced/experimental techniques
                } catch (InterruptedException willHappen) {}
                Hint hint = accu.getHint();
                if (hint == null) {
                    System.err.println("Failed to solve:\n" + grid.toString());
                    return Double.MAX_VALUE;
                }
                assert hint instanceof Rule;
                Rule rule = (Rule)hint;
                double ruleDiff = rule.getDifficulty();
				String ruleName = rule.getName();
                if (ruleDiff == exclude1 || ruleDiff == exclude2 || ruleDiff == exclude3 || (ruleName.contains(excludeT1) && (!Objects.equals(excludeT1,""))) || (ruleName.contains(excludeT2) && (!Objects.equals(excludeT2,""))) || (ruleName.contains(excludeT3) && (!Objects.equals(excludeT3,""))))
					return 0.0;
				if (inRateCounter < 3 && (ruleDiff == include1 || ruleDiff == include2 || ruleDiff == include3)) inRateCounter++;
				if (inTechCounter < 3 && ((ruleName.contains(includeT1) && (!Objects.equals(includeT1,""))) || (ruleName.contains(includeT2) && (!Objects.equals(includeT2,""))) || (ruleName.contains(includeT3) && (!Objects.equals(includeT3,""))))) inTechCounter++;
				if (!oneOfThreeCounter && (ruleName.contains(oneOf3_1) || ruleName.contains(oneOf3_2) || ruleName.contains(oneOf3_3)))
					oneOfThreeCounter = true;
				if (ruleDiff > difficulty) {
					if (notMax1 == ruleDiff || notMax2 == ruleDiff || notMax3 == ruleDiff || (ruleName.contains(notMaxT1) && (!Objects.equals(notMaxT1,""))) || (ruleName.contains(notMaxT2) && (!Objects.equals(notMaxT2,""))) || (ruleName.contains(notMaxT3) && (!Objects.equals(notMaxT3,""))))
							notMaxCounter = true;
					else
							notMaxCounter = false;
					difficulty = ruleDiff;
				}
                if (difficulty >= min && max >= 11.0)
                    break;
                if (difficulty > max)
                    break;
                hint.apply(grid);
            }
			if (!oneOfThreeCounter || notMaxCounter || inRateCounter < 3 || inTechCounter < 3)
				return 0.0;
			return difficulty;
        } finally {
            normalPriority(oldPriority);
        }
    }
        
    private Hint getSingleHint() {
        SingleHintAccumulator accu = new SingleHintAccumulator();
        try {
            for (HintProducer producer : directHintProducers)
                producer.getHints(grid, accu);
            for (IndirectHintProducer producer : indirectHintProducers)
                producer.getHints(grid, accu);
            for (IndirectHintProducer producer : chainingHintProducers)
                producer.getHints(grid, accu);
            for (IndirectHintProducer producer : chainingHintProducers2)
                producer.getHints(grid, accu);
            for (IndirectHintProducer producer : advancedHintProducers)
                producer.getHints(grid, accu);
            for (IndirectHintProducer producer : experimentalHintProducers)
                producer.getHints(grid, accu);
        } catch (InterruptedException willHappen) {}
        return accu.getHint();
    }
    public void getDifficulty(serate.Formatter formatter) {
        Grid backup = new Grid();
        grid.copyTo(backup);
		boolean logStep = Settings.getInstance().isLog();
		PrintWriter logWriter = Settings.getInstance().getLogWriter();
		int stepCount = 0;
        try {
            difficulty = 0; //Double.NEGATIVE_INFINITY;
            pearl = 0.0;
            diamond = 0.0;
			ERtN ="No solution";
			EPtN ="No solution";
			EDtN ="No solution";
			shortERtN ="O";
			shortEPtN ="O";
			shortEDtN ="O";			
        	formatter.beforePuzzle(this);
            while (!isSolved()) {
            	formatter.beforeHint(this);
            	Hint hint = null;
            	try {
            		hint = getSingleHint();
            		if(hint != null) {
		                assert hint instanceof Rule;
		                Rule rule = (Rule)hint;
		                double ruleDiff = rule.getDifficulty();
						String ruleName = rule.getName();
						String ruleNameShort = rule.getShortName();						
						//lksudoku, log steps
						if (logStep) {
							++stepCount;
							logWriter.println("Step "+stepCount+": rate "+ruleDiff);
							logWriter.println(rule.toString());
						}
		                if (ruleDiff > difficulty) {
		                    difficulty = ruleDiff;
							ERtN = ruleName;
							shortERtN = ruleNameShort;
		                }
            		}
            	}
                catch (UnsupportedOperationException ex) {
                    difficulty = pearl = diamond = 0.0;
					ERtN = EPtN = EDtN = "No solution";
					shortERtN = shortEPtN = shortEDtN = "O";
                }
                if (hint == null) {
                    difficulty = 20.0;
					ERtN = "Beyond solver";
					shortERtN = "xx";
                    break;
                }
                hint.apply(grid);
            	formatter.afterHint(this, hint);
                if (pearl == 0.0) {
                    if (diamond == 0.0){
                        diamond = difficulty;
						EDtN = ERtN;
						shortEDtN = shortERtN;
					}
                    if (hint.getCell() != null) {
                        if (want == 'd' && difficulty > diamond) {
                            difficulty = 20.0;
							ERtN = "Beyond solver";
							shortERtN = "xx";
                           break;
                        }
                        pearl = difficulty;
						EPtN = ERtN;
						shortEPtN = shortERtN;
                    }
                }
                else if (want != 0 && difficulty > pearl) {
                    difficulty = 20.0;
					ERtN = "Beyond solver";
					shortERtN = "xx";
                    break;
                }
            }
        	formatter.afterPuzzle(this);
        } finally {
            backup.copyTo(grid);
        }
    }

    public void getHintsHint() {
        Grid backup = new Grid();
        grid.copyTo(backup);
        try {
            difficulty = 0; //Double.NEGATIVE_INFINITY;
            pearl = 0.0;
            diamond = 0.0;
			ERtN ="No solution";
			EPtN ="No solution";
			EDtN ="No solution";
			shortERtN ="O";
			shortEPtN ="O";
			shortEDtN ="O";			
            while (!isSolved()) {
                SingleHintAccumulator accu = new SingleHintAccumulator();
                try {
                    for (HintProducer producer : directHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : indirectHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers2)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : advancedHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : experimentalHintProducers)
                        producer.getHints(grid, accu);
                } catch (InterruptedException willHappen) {}
                Hint hint = accu.getHint();
                if (hint == null) {
					ERtN = "Beyond solver";
					shortERtN = "xx";
                    break;
                }
                assert hint instanceof Rule;
                Rule rule = (Rule)hint;
                double ruleDiff = rule.getDifficulty();
				String ruleName = rule.getName();
				String ruleNameShort = rule.getShortName();						
                if (ruleDiff > difficulty) {
                    difficulty = ruleDiff;
					ERtN = ruleName;
					shortERtN = ruleNameShort;
				}
                hint.apply(grid);

                String s = "";
                for (int i = 0; i < 81; i++) {
                    int n = grid.getCellValue(i % 9, i / 9);
                    s += (n==0)?".":n;
                }
                s += " ";
                int w = (int)((ruleDiff + 0.05) * 10);
                int p = w % 10;
                w /= 10;
                s += w + "." + p;
                s += ", " + hint.toString();
                System.out.println(s);
                System.out.flush();

                if (pearl == 0.0) {
                    if (diamond == 0.0){
                        diamond = difficulty;
						EDtN = ERtN;
						shortEDtN = shortERtN;
					}
                    if (hint.getCell() != null) {
                        if (want == 'd' && difficulty > diamond) {
                            difficulty = 20.0;
							ERtN = "Beyond solver";
							shortERtN = "xx";
                            break;
                        }
                        pearl = difficulty;
						EPtN = ERtN;
						shortEPtN = shortERtN;
                    }
                }
                else if (want != 0 && difficulty > pearl) {
                    difficulty = 20.0;
					ERtN = "Beyond solver";
					shortERtN = "xx";
                    break;
                }
            }
        } finally {
            backup.copyTo(grid);
        }
    }

//    public void getPencilMarks() {
//        Grid backup = new Grid();
//        grid.copyTo(backup);
//        try {
//            difficulty = 0; //Double.NEGATIVE_INFINITY;
//            pearl = 0.0;
//            diamond = 0.0;
//			ERtN ="No solution";
//			EPtN ="No solution";
//			EDtN ="No solution";
//			shortERtN ="O";
//			shortEPtN ="O";
//			shortEDtN ="O";			
//            while (!isSolved()) {
//                String s = "";
//
//                int crd = 1;
//                for (int i = 0; i < 81; i++) {
//                    int n = grid.getCellPotentialValues(i).cardinality();
//                    if ( n > crd ) { crd = n; }
//                }
//                if ( crd > 1 )
//                {
//                    for (int i=0; i<3; i++ ) {
//                        s = "+";
//                        for (int j=0; j<3; j++ ) {
//                            for (int k=0; k<3; k++ ) { s += "-";
//                                for (int l=0; l<crd; l++ ) { s += "-";
//                                }
//                            }
//                            s += "-+";
//                        }
//                        System.out.println(s);
//                        System.out.flush();
//
//                        for (int j=0; j<3; j++ ) {
//                            s = "|";
//                            for (int k=0; k<3; k++ ) {
//                                for (int l=0; l<3; l++ ) {
//                                    s += " ";
//                                    int cnt = 0;
//                                    int c = ((((i*3)+j)*3)+k)*3+l;
//                                    Cell cell = Grid.getCell(c % 9, c / 9);
//                                    int n = grid.getCellValue(c % 9, c / 9);
//                                    if ( n != 0 ) {
//                                        s += n;
//                                        cnt += 1;
//                                    }
//                                    if ( n == 0 ) {
//                                        for (int pv=1; pv<=9; pv++ ) {
//                                            if ( grid.hasCellPotentialValue(cell.getIndex(), pv) ) {
//                                                s += pv;
//                                                cnt += 1;
//                                            }
//                                        }
//                                    }
//                                    for (int pad=cnt; pad<crd; pad++ ) { s += " ";
//                                    }
//                                }
//                                s += " |";
//                            }
//                            System.out.println(s);
//                            System.out.flush();
//                        }
//                    }
//
//                    s = "+";
//                    for (int j=0; j<3; j++ ) {
//                        for (int k=0; k<3; k++ ) { s += "-";
//                            for (int l=0; l<crd; l++ ) { s += "-";
//                            }
//                        }
//                        s += "-+";
//                    }
//                    System.out.println(s);
//                    System.out.flush();
//                }
//
//                SingleHintAccumulator accu = new SingleHintAccumulator();
//                try {
//                    for (HintProducer producer : directHintProducers)
//                        producer.getHints(grid, accu);
//                    for (IndirectHintProducer producer : indirectHintProducers)
//                        producer.getHints(grid, accu);
//                    for (IndirectHintProducer producer : chainingHintProducers)
//                        producer.getHints(grid, accu);
//                    for (IndirectHintProducer producer : chainingHintProducers2)
//                        producer.getHints(grid, accu);
//                    for (IndirectHintProducer producer : advancedHintProducers)
//                        producer.getHints(grid, accu);
//                    for (IndirectHintProducer producer : experimentalHintProducers)
//                        producer.getHints(grid, accu);
//                } catch (InterruptedException willHappen) {}
//                Hint hint = accu.getHint();
//                if (hint == null) {
//                    difficulty = 20.0;
//					ERtN = "Beyond solver";
//					shortERtN = "xx";
//                    break;
//                }
//                assert hint instanceof Rule;
//                Rule rule = (Rule)hint;
//                double ruleDiff = rule.getDifficulty();
//				String ruleName = rule.getName();
//				String ruleNameShort = rule.getShortName();
//                if (ruleDiff > difficulty){
//                    difficulty = ruleDiff;
//					ERtN = ruleName;
//					shortERtN = ruleNameShort;
//				}
//                hint.apply(grid);
//
//                s = "";
//                for (int i = 0; i < 81; i++) {
//                    int n = grid.getCellValue(i % 9, i / 9);
//                    s += (n==0)?".":n;
//                }
//                s += " ";
//                int w = (int)((ruleDiff + 0.05) * 10);
//                int p = w % 10;
//                w /= 10;
//                s += w + "." + p;
//                s += ", " + hint.toString();
//                System.out.println(s);
//                System.out.flush();
//
//                if (pearl == 0.0) {
//                    if (diamond == 0.0) {
//                        diamond = difficulty;
//						EDtN = ERtN;
//						shortEDtN = shortERtN;
//					}
//                    if (hint.getCell() != null) {
//                        if (want == 'd' && difficulty > diamond) {
//                            difficulty = 20.0;
//							ERtN = "Beyond solver";
//							shortERtN = "xx";
//                            break;
//                        }
//                        pearl = difficulty;
//						EPtN = ERtN;
//						shortEPtN = shortERtN;
//                    }
//                }
//                else if (want != 0 && difficulty > pearl) {
//                    difficulty = 20.0;
//					ERtN = "Beyond solver";
//					shortERtN = "xx";
//                    break;
//                }
//            }
//        } finally {
//            backup.copyTo(grid);
//        }
//    }

	// lksudoku added batch rating ability
	// apply all concurrent moves of lowest rating
    public void getBatchDifficulty(serate.Formatter formatter) {
        Grid backup = new Grid();
        grid.copyTo(backup);
		boolean logStep = Settings.getInstance().isLog();
		PrintWriter logWriter = Settings.getInstance().getLogWriter();
		int batchCount = 0;
        try {
            difficulty = 0.0;
            pearl = 0.0;
            diamond = 0.0;
			ERtN ="No solution";
			EPtN ="No solution";
			EDtN ="No solution";
			shortERtN ="O";
			shortEPtN ="O";
			shortEDtN ="O";			
            formatter.beforePuzzle(this);
			while (!isSolved()) {
				formatter.beforeHint(this);
            	//Hint hint = null;
				List<Hint> result = new ArrayList<Hint>();
				SmallestHintsAccumulator accu = new SmallestHintsAccumulator(result);
                try {
                    for (HintProducer producer : directHintProducers) {
                        producer.getHints(grid, accu);
						if (!result.isEmpty()) {
							throw new InterruptedException();
						}
                    }
                    for (IndirectHintProducer producer : indirectHintProducers) {
                        producer.getHints(grid, accu);
						if (!result.isEmpty()) {
							throw new InterruptedException();
						}
                    }
                    for (IndirectHintProducer producer : chainingHintProducers) {
                        producer.getHints(grid, accu);
						if (!result.isEmpty()) {
							throw new InterruptedException();
						}
                    }
                    for (IndirectHintProducer producer : chainingHintProducers2) {
                        producer.getHints(grid, accu);
						if (!result.isEmpty()) {
							throw new InterruptedException();
						}
                    }
                    for (IndirectHintProducer producer : advancedHintProducers) {
                        producer.getHints(grid, accu);
						if (!result.isEmpty()) {
							throw new InterruptedException();
						}
                    }
                    for (IndirectHintProducer producer : experimentalHintProducers) {
                        producer.getHints(grid, accu);
						if (!result.isEmpty()) {
							throw new InterruptedException();
						}
                    }
                }
				catch (InterruptedException willHappen) {}				
                if (result.isEmpty()) {
                    difficulty = 20.0;
					ERtN = "Beyond solver";
					shortERtN = "xx";
                    break;
                }
				//lksudoku, log steps
				if (logStep) {
					++batchCount;
				}
				int batchSubStep = 0;
				// apply hints of same rating
				for (Hint hint: result)
				{
	                assert hint instanceof Rule;
	                Rule rule = (Rule)hint;
	                double ruleDiff = rule.getDifficulty();
					String ruleName = rule.getName();
					String ruleNameShort = rule.getShortName();
					if (logStep) {
						if (++batchSubStep == 1) {
							logWriter.println("Batch "+batchCount+": rate "+ruleDiff);
						}
						logWriter.println("Step "+batchCount+"."+batchSubStep+", "+rule.toString());
					}
	                if (ruleDiff > difficulty) {
		                    difficulty = ruleDiff;
							ERtN = ruleName;
							shortERtN = ruleNameShort;
					}
	                hint.apply(grid);
					formatter.afterHint(this, hint);
					if (pearl == 0.0) {
						if (diamond == 0.0){
							diamond = difficulty;
							EDtN = ERtN;
							shortEDtN = shortERtN;
						}
						if (hint.getCell() != null) {
							if (want == 'd' && difficulty > diamond) {
								difficulty = 20.0;
								ERtN = "Beyond solver";
								shortERtN = "xx";
							   break;
							}
							pearl = difficulty;
							EPtN = ERtN;
							shortEPtN = shortERtN;
						}
					}
					else if (want != 0 && difficulty > pearl) {
						difficulty = 20.0;
						ERtN = "Beyond solver";
						shortERtN = "xx";
						break;
					}
				}
				if ( difficulty == 20.0 ) {
					break;
				}
           	}
			formatter.afterPuzzle(this);
		}
		finally {
            backup.copyTo(grid);
        }
    }

    public Map<String, Integer> toNamedList(Map<Rule, Integer> rules) {
        Map<String, Integer> hints = new LinkedHashMap<String, Integer>();
        for (Map.Entry<Rule, Integer> entry : rules.entrySet()) {
        	Rule rule = entry.getKey();
        	int count = entry.getValue();
            String name = rule.getName();
            if (hints.containsKey(name))
                hints.put(name, hints.get(name) + count);
            else
                hints.put(name, count);
        }
        return hints;
    }

    public Hint analyse(Asker asker) {
        Grid copy = new Grid();
        grid.copyTo(copy);
        try {
            SingleHintAccumulator accu = new SingleHintAccumulator();
            try {
                for (WarningHintProducer producer : validatorHintProducers)
                    producer.getHints(grid, accu);
                for (WarningHintProducer producer : warningHintProducers)
                    producer.getHints(grid, accu);
                Analyser engine = new Analyser(this, asker);
                engine.getHints(grid, accu);
            } catch (InterruptedException willProbablyHappen) {}
            return accu.getHint();
        } finally {
            copy.copyTo(grid);
        }
    }

    public Hint bruteForceSolve() {
        SingleHintAccumulator accu = new SingleHintAccumulator();
        try {
            for (WarningHintProducer producer : validatorHintProducers)
                producer.getHints(grid, accu);
            Solution engine = new Solution();
            engine.getHints(grid, accu);
        } catch (InterruptedException willProbablyHappen) {}
        return accu.getHint();
    }

}
