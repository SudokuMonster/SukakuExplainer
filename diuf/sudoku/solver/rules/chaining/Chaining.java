/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.chaining;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ForkJoinWorkerThread;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
//import diuf.sudoku.Settings.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.*;
import diuf.sudoku.solver.rules.unique.BivalueUniversalGrave;
import diuf.sudoku.solver.rules.unique.UniqueLoops;
import diuf.sudoku.tools.*;

/**
 * Implementation of all solving techniques involving chains of implications.
 * This includes all types of Bidirectional Cycles and all types
 * of Forcing Chains.
 */
public class Chaining implements IndirectHintProducer {

    private final boolean isMultipleEnabled;
    private final boolean isDynamic;
    private final boolean isNisho;
    private final int level;
    private final boolean noParallel;
    private final int nestingLimit;
    private Grid saveGrid = new Grid();
    private List<IndirectHintProducer> otherRules;
    private Grid lastGrid = null;
    private Collection<ChainingHint> lastHints = null;


    /**
     * Create the engine for searching forcing chains.
     * @param isMultipleEnabled Whether multiple forcing chains (Cell and Region Forcing
     * Chains) are searched
     * @param isDynamic Whether dynamic forcing chains are included
     * @param isNishio Whether Nishio mode is activated
     * Only used if <tt>isDynamic</tt> and <tt>isMultiple</tt> are <tt>false</tt>.
     */
//    public Chaining(boolean isMultipleEnabled, boolean isDynamic, boolean isNishio, int level) {
//        this.isMultipleEnabled = isMultipleEnabled;
//        this.isDynamic = isDynamic;
//        this.isNisho = isNishio;
//        this.level = level;
//        this.noParallel = level < 3;
//        this.nestingLimit = 0;
//    }
    
    public Chaining(boolean isMultipleEnabled, boolean isDynamic, boolean isNishio, int level, boolean noParallel, int nestingLimit) {
        this.isMultipleEnabled = isMultipleEnabled;
        this.isDynamic = isDynamic;
        this.isNisho = isNishio;
        this.level = level;
        this.noParallel = noParallel;
        this.nestingLimit = nestingLimit;
    }

    boolean isDynamic() {
        return this.isDynamic;
    }

    boolean isNishio() {
        return this.isNisho;
    }

    boolean isMultiple() {
        return this.isMultipleEnabled;
    }

    public int getLevel() {
        return this.level;
    }

    double getDifficulty() {
//        if (level >= 2)
//            return 9.5 + 0.5 * (level - 2);
//        else 
    	if (level > 0)
            return 8.5 + 0.5 * level;
        else if (isNisho)
            return 7.5; // Nishio
        else if (isDynamic)
            return 8.5; // Dynamic chains
        else if (isMultipleEnabled)
            return 8.0; // Multiple chains
        else
            throw new IllegalStateException(); // Must compute by themselves
    }
    
    public class SortableChainingHint {
    	final ChainingHint hint;
    	final double difficulty;
    	final int complexity;
    	final int sortKey;
    	SortableChainingHint(ChainingHint hint) {
    		this.hint = hint;
    		this.difficulty = hint.getDifficulty();
    		this.complexity = hint.getComplexity();
    		this.sortKey = hint.getSortKey();
    	}
        public int compare(SortableChainingHint h1, SortableChainingHint h2) {
            double d1 = h1.difficulty;
            double d2 = h2.difficulty;
            if (d1 < d2)
                return -1;
            else if (d1 > d2)
                return 1;
            int l1 = h1.complexity;
            int l2 = h2.complexity;
            if (l1 == l2)
                return h1.sortKey - h2.sortKey;
            return l1 - l2;
        }
    }

    /**
     * Search for hints on the given grid
     * @param grid the grid on which to search fro hints
     * @return the hints found
     */
    protected List<ChainingHint> getHintList(Grid grid) {
        // TODO: implement an implications cache
        List<ChainingHint> result;
        if (isMultipleEnabled || isDynamic) {
            result = getMultipleChainsHintList(grid);
        } else {
            // Cycles with X-Links (Coloring / Fishy)
            List<ChainingHint> xLoops = getLoopHintList(grid, false, true);
            // Cycles with Y-Links
            List<ChainingHint> yLoops = getLoopHintList(grid, true, false);
            // Cycles with both
            List<ChainingHint> xyLoops = getLoopHintList(grid, true, true);
            result = xLoops;
            result.addAll(yLoops);
            result.addAll(xyLoops);
        }
        if(result.isEmpty()) {
        	return result;
        }
        /*
         * Sort the resulting hints. The hints with the shortest chain length
         * are returned first.
         */
        List<SortableChainingHint> sortableResult= new ArrayList<SortableChainingHint>();
        for(ChainingHint hint : result) {
        	sortableResult.add(new SortableChainingHint(hint));
        }
        Collections.sort(sortableResult, new Comparator<SortableChainingHint>() {
            public int compare(SortableChainingHint h1, SortableChainingHint h2) {
            	return h1.compare(h1, h2);
            }
        });
        result.clear();
        for(SortableChainingHint hint : sortableResult) {
        	result.add(hint.hint);
        }

        return result;
    }

    /**
     * Search for hints on the given grid
     * @param grid the grid on which to search for hints
     * @param isYChainEnabled whether Y-Links are used in "on to off" searches
     * @param isXChainEnabled whether X-Links are used in "off to on" searches
     * @return the hints found
     */
    private List<ChainingHint> getLoopHintList(Grid grid, boolean isYChainEnabled,
            boolean isXChainEnabled) {
        List<ChainingHint> result = new ArrayList<ChainingHint>();
        // Iterate on all empty cells
        for (int i = 0; i < 81; i++) {
            if (grid.getCellValue(i) == 0) { // the cell is empty
            	int cardinality = grid.getCellPotentialValues(i).cardinality();
                if (cardinality > 1) {
                    // Iterate on all potential values that are not alone
		            Cell cell = Grid.getCell(i);
                    for (int value = 1; value <= 9; value++) {
                        if (grid.hasCellPotentialValue(i, value)) {
                            Potential pOn = new Potential(cell, value, true);
                            doUnaryChaining(grid, pOn, result, isYChainEnabled, isXChainEnabled);
                        }
                    } 
                }
            } // if empty
         } // for i
        return result;
    }

    private List<ChainingHint> getMultipleChainsHintListForCell(Grid grid, Cell cell, int cardinality) {
        List<ChainingHint> result = new ArrayList<ChainingHint>();
        // Prepare storage and accumulator for "Cell Reduction"
        Map<Integer, LinkedSet<Potential>> valueToOn =
            new HashMap<Integer, LinkedSet<Potential>>();
        Map<Integer, LinkedSet<Potential>> valueToOff =
            new HashMap<Integer, LinkedSet<Potential>>();
        LinkedSet<Potential> cellToOn = null;
        LinkedSet<Potential> cellToOff = null;

        // Iterate on all potential values that are not alone
        for (int value = 1; value <= 9; value++) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value)) {
                // Do Binary chaining (same potential either on or off)
                Potential pOn = new Potential(cell, value, true);
                Potential pOff = new Potential(cell, value, false);
                LinkedSet<Potential> onToOn = new LinkedSet<Potential>();
                LinkedSet<Potential> onToOff = new LinkedSet<Potential>();
                boolean doDouble = (cardinality >= 3 && !isNisho && isDynamic);
                boolean doContradiction = isDynamic || isNisho;
                doBinaryChaining(grid, pOn, pOff, result, onToOn, onToOff, doDouble, doContradiction);

                if (!isNisho) {
                    // Do region chaining
                    doRegionChainings(grid, result, cell, value, onToOn, onToOff);
                }

                // Collect results for cell chaining
                valueToOn.put(value, onToOn);
                valueToOff.put(value, onToOff);
                if (cellToOn == null) {
                    cellToOn = new LinkedSet<Potential>();
                    cellToOff = new LinkedSet<Potential>();
                    cellToOn.addAll(onToOn);
                    cellToOff.addAll(onToOff);
                } else {
                    cellToOn.retainAll(onToOn);
                    cellToOff.retainAll(onToOff);
                }
            }
        } // for value

        if (!isNisho) {
            // Do Cell reduction
            if (cardinality == 2 || (isMultipleEnabled && cardinality > 2)) {
                for (Potential p : cellToOn) {
                    CellChainingHint hint = createCellReductionHint(grid, cell, p, valueToOn);
                    if (hint.isWorth())
                        result.add(hint);
                }
                for (Potential p : cellToOff) {
                    CellChainingHint hint = createCellReductionHint(grid, cell, p, valueToOff);
                    if (hint.isWorth())
                        result.add(hint);
                }
            }
        }
    	return result;
    }
    
    private List<ChainingHint> getMultipleChainsHintList(Grid grid) {
        List<ChainingHint> result = new ArrayList<ChainingHint>();
        //boolean noParallel = true; //debug, hide the class member noParallel
        //boolean noParallel = false;
        boolean noParallel = this.noParallel || Settings.getInstance().getNumThreads() == 1;
        List<Cell> cellsToProcess = new ArrayList<Cell>();
        // Iterate on all empty cells
        for (int i = 0; i < 81; i++) {
            if (grid.getCellValue(i) == 0) { // the cell is empty
            	int cardinality = grid.getCellPotentialValues(i).cardinality();
                if (cardinality > 2 || (cardinality > 1 && isDynamic)) {
		            Cell cell = Grid.getCell(i);
                	if (noParallel) {
                		result.addAll(getMultipleChainsHintListForCell(grid, cell, cardinality));
                	}
                	else {
                		cellsToProcess.add(cell);
                	}
                } // Cardinality > 1
            } // if empty
        } // for i
        if (noParallel) {
        	return result;
        }
        //process the collected cells in parallel
        ConcurrentLinkedQueue<ChainingHint> parallelResult = new ConcurrentLinkedQueue<ChainingHint>();
        
//        //this approach wrongly distributes the work possibly due to java implementation bugs
//        cellsToProcess.parallelStream().forEach((cell) -> {
//           	int cardinality = cell.getPotentialValues().cardinality();
//           	Grid gridClone = new Grid();
//           	grid.copyTo(gridClone);
//           	Chaining chainingClone = new Chaining(isMultipleEnabled, isDynamic, isNisho, level, true, nestingLimit);
//           	parallelResult.addAll(chainingClone.getMultipleChainsHintListForCell(gridClone, gridClone.getCell(cell.getX(), cell.getY()), cardinality));
//        });
        
        List<MultipleChainsHintsCollector> threads = new ArrayList<MultipleChainsHintsCollector>();
        for(Cell cell : cellsToProcess) {
        	MultipleChainsHintsCollector t = new MultipleChainsHintsCollector(this, grid, cell, parallelResult);
        	threads.add(t);
        	t.start();
        }
        for(MultipleChainsHintsCollector t : threads) {
        	try {
        		t.join();
        	} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	finally {}
        }
        
        result.addAll(parallelResult);
        return result;
    }

    class MultipleChainsHintsCollector extends Thread {
    	private Chaining chaining;
    	private ConcurrentLinkedQueue<ChainingHint> accumulator;
    	private final Grid gridClone = new Grid();
    	private Cell cell;
    	MultipleChainsHintsCollector(Chaining caller, Grid grid, Cell cell, ConcurrentLinkedQueue<ChainingHint> result) {
    		chaining = new Chaining(caller.isMultipleEnabled, caller.isDynamic, caller.isNisho, caller.level, true, caller.nestingLimit);
    		grid.copyTo(gridClone);
    		accumulator = result;
    		this.cell = cell;
    	}
    	public void run() {
    		int cardinality = gridClone.getCellPotentialValues(cell.getIndex()).cardinality();
    		accumulator.addAll(chaining.getMultipleChainsHintListForCell(gridClone, cell, cardinality));
    	}
    }

    private Potential getReversedCycle(Potential org) {
        List<Potential> result = new LinkedList<Potential>();
        String explanations = null;
        while (org != null) {
            Potential rev = new Potential(org.cell, org.value, !org.isOn, org.cause, explanations);
            explanations = org.explanation;
            result.add(0, rev);
            if (!org.parents.isEmpty())
                org = org.parents.get(0);
            else
                org = null;
        }
        Potential prev = null;
        for (Potential rev : result) {
            if (prev != null)
                prev.parents.add(rev);
            prev = rev;
        }
        return result.get(0);
    }

    /**
     * Look for, and add single focring chains, and bidirectional cycles.
     * @param grid the sudoku grid
     * @param pOn the starting potential
     * @param result filled with the hints found
     * @param isYChainEnabled whether y-chain are enabled
     * @param isXChainEnabled whether x-chains are enabled
     */
    private void doUnaryChaining(Grid grid, final Potential pOn, List<ChainingHint> result,
            boolean isYChainEnabled, boolean isXChainEnabled) {

        if ((!isXChainEnabled) && grid.getCellPotentialValues(pOn.cell.getIndex()).cardinality() > 2)
            return; // Y-Cycles can only start if cell has 2 potential values

        final List<Potential> cycles = new ArrayList<Potential>();
        final List<Potential> chains = new ArrayList<Potential>();
        LinkedSet<Potential> onToOn = new LinkedSet<Potential>();
        LinkedSet<Potential> onToOff = new LinkedSet<Potential>();
        onToOn.add(pOn);
        doCycles(grid, onToOn, onToOff, isYChainEnabled, isXChainEnabled, cycles, pOn);
        if (isXChainEnabled) {
            // Forcing Y-Chains do not exist (length must be both odd and even)

            // Forcing chain with "off" implication
            onToOn = new LinkedSet<Potential>();
            onToOff = new LinkedSet<Potential>();
            onToOn.add(pOn);
            doForcingChains(grid, onToOn, onToOff, isYChainEnabled, chains, pOn);

            // Forcing chain with "on" implication
            final Potential pOff = new Potential(pOn.cell, pOn.value, false);
            onToOn = new LinkedSet<Potential>();
            onToOff = new LinkedSet<Potential>();
            onToOff.add(pOff);
            doForcingChains(grid, onToOn, onToOff, isYChainEnabled, chains, pOff);
        }
        for (Potential dstOn : cycles) {
            // Cycle found !!
            assert dstOn.isOn; // Cycles are only looked for from "on" potentials
            Potential dstOff = getReversedCycle(dstOn);
            ChainingHint hint = createCycleHint(grid, dstOn, dstOff, isYChainEnabled,
                    isXChainEnabled);
            if (hint.isWorth())
                result.add(hint);
        }
        for (Potential target : chains) {
            ChainingHint hint = createForcingChainHint(grid, target, isYChainEnabled, isXChainEnabled);
            if (hint.isWorth())
                result.add(hint);
        }

    }

    /**
     * From the potential <code>p</code>, compute the consequences from
     * both states.
     * <p>
     * More precisely, <code>p</code> is first assumed to be correct
     * ("on"), and then to be incorrect ("off"); and the following sets are
     * created:
     * <ul>
     * <li><b><code>onToOn</code></b> the set of potentials that must be "on"
     * when <code>p</code> is "on"
     * <li><b><code>onToOff</code></b> the set of potentials that must be "off"
     * when <code>p</code> is "on"
     * <li><b><code>offToOn</code></b> the set of potentials that must be "on"
     * when <code>p</code> is "off"
     * <li><b><code>offToOff</code></b> the set of potentials that must be "off"
     * when <code>p</code> is "off"
     * </ul>
     * Then the following rules are applied:
     * <ul>
     * <li>If a potential belongs to both <code>onToOn</code> and <code>onToOff</code>,
     * the potential <code>p</code> cannot be "on" because it would implie a potential
     * to be both "on" and "off", which is an absurd.
     * <li>If a potential belongs to both <code>offToOn</code> and <code>offToOff</code>,
     * the potential <code>p</code> cannot be "off" because it would implie a potential
     * to be both "on" and "off", which is an absurd.
     * <li>If a potential belongs to both <code>onToOn</code> and <code>offToOn</code>,
     * this potential must be "on", because it is implied to be "on" by the two possible
     * states of <code>p</code>.
     * <li>If a potential belongs to both <code>onToOff</code> and <code>offToOff</code>,
     * this potential must be "off", because it is implied to be "off" by the two possible
     * states of <code>p</code>.
     * </ul>
     * Note that if a potential belongs to all the four sets, the Sudoku has no solution.
     * This is not checked.
     * @param grid the grid
     * @param p the potential to gather hints from
     * @param accu the accumulator for hints
     * @param onToOn an empty set, filled with potentials that get on if the given
     * potential is on.
     * @param onToOff an empty set, filled with potentials that get off if the given
     * potential is on.
     * @throws InterruptedException
     */
    private void doBinaryChaining(Grid grid, Potential pOn, Potential pOff,
            List<ChainingHint> result, LinkedSet<Potential> onToOn,
            LinkedSet<Potential> onToOff, boolean doReduction, boolean doContradiction) {

        Potential[] absurdPotential = null;
        LinkedSet<Potential> offToOn = new LinkedSet<Potential>();
        LinkedSet<Potential> offToOff = new LinkedSet<Potential>();

        /*
         * Circular Forcing Chains (hypothesis implying its negation)
         * are already covered by Cell Forcing Chains, and are therefore
         * not checked for.
         */

        // Test p = "on"
        onToOn.add(pOn);
        absurdPotential = doChaining(grid, onToOn, onToOff);
        if (doContradiction && absurdPotential != null) {
            // p cannot hold its value, because else it would lead to a contradiction
            BinaryChainingHint hint = createChainingOffHint(absurdPotential[0], absurdPotential[1],
                    pOn, pOn, true);
            if (hint.isWorth())
                result.add(hint);
        }

        // Test p = "off"
        offToOff.add(pOff);
        absurdPotential = doChaining(grid, offToOn, offToOff);
        if (doContradiction && absurdPotential != null) {
            // p must hold its value, because else it would lead to a contradiction
            BinaryChainingHint hint = createChainingOnHint(grid, absurdPotential[0], absurdPotential[1],
                    pOff, pOff, true);
            if (hint.isWorth())
                result.add(hint);
        }

        if (doReduction) {
            // Check potentials that must be on in both case
            for (Potential pFromOn : onToOn) {
                Potential pFromOff = offToOn.get(pFromOn);
                if (pFromOff != null) {
                    BinaryChainingHint hint = createChainingOnHint(grid, pFromOn, pFromOff, pOn, pFromOn, false);
                    if (hint.isWorth())
                        result.add(hint);
                }
            }

            // Check potentials that must be off in both case
            for (Potential pFromOn : onToOff) {
                Potential pFromOff = offToOff.get(pFromOn);
                if (pFromOff != null) {
                    BinaryChainingHint hint = createChainingOffHint(pFromOn, pFromOff, pOff, pFromOff, false);
                    if (hint.isWorth())
                        result.add(hint);
                }
            }
        }

    }

    private void doRegionChainings(Grid grid, List<ChainingHint> result, Cell cell,
            int value, LinkedSet<Potential> onToOn, LinkedSet<Potential> onToOff) {
//@SudokuMonster: Changes for variants
        for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
        	if (!Settings.getInstance().isVLatin()) {
				if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
				if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
				if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
				if (Grid.cellRegions[cell.getIndex()][regionTypeIndex] < 0)
						continue;
			}
			Grid.Region region = Grid.getRegionAt(regionTypeIndex, cell.getIndex());
			BitSet potentialPositions = region.getPotentialPositions(grid, value);

            // Is this region worth ?
            int cardinality = potentialPositions.cardinality();
            if (cardinality == 2 || (isMultipleEnabled && cardinality > 2)) {
                int firstPos = potentialPositions.nextSetBit(0);
                Cell firstCell = region.getCell(firstPos);

                // Do we meet region for the first time ?
                if (firstCell.equals(cell)) {
                    Map<Integer, LinkedSet<Potential>> posToOn =
                        new HashMap<Integer, LinkedSet<Potential>>();
                    Map<Integer, LinkedSet<Potential>> posToOff =
                        new HashMap<Integer, LinkedSet<Potential>>();
                    LinkedSet<Potential> regionToOn = new LinkedSet<Potential>();
                    LinkedSet<Potential> regionToOff = new LinkedSet<Potential>();

                    // Iterate on potential positions within the region
                    for (int pos = potentialPositions.nextSetBit(0); pos >= 0;
                            pos = potentialPositions.nextSetBit(pos + 1)) {
                        Cell otherCell = region.getCell(pos);
                        if (otherCell.equals(cell)) {
                            posToOn.put(pos, onToOn);
                            posToOff.put(pos, onToOff);
                            regionToOn.addAll(onToOn);
                            regionToOff.addAll(onToOff);
                        } else {
                            Potential other = new Potential(otherCell, value, true);
                            LinkedSet<Potential> otherToOn = new LinkedSet<Potential>();
                            LinkedSet<Potential> otherToOff = new LinkedSet<Potential>();
                            otherToOn.add(other);
                            doChaining(grid, otherToOn, otherToOff);
                            posToOn.put(pos, otherToOn);
                            posToOff.put(pos, otherToOff);
                            regionToOn.retainAll(otherToOn);
                            regionToOff.retainAll(otherToOff);
                        }
                    }

                    // Gather results
                    for (Potential p : regionToOn) {
                        RegionChainingHint hint = createRegionReductionHint(grid, region, value,
                                p, posToOn);
                        if (hint.isWorth())
                            result.add(hint);
                    }
                    for (Potential p : regionToOff) {
                        RegionChainingHint hint = createRegionReductionHint(grid, region, value,
                                p, posToOff);
                        if (hint.isWorth())
                            result.add(hint);
                    }
                } // First meet
            } // cardinality >= 3
        } // for Region
    }

    /**
     * Get the set of all {@link Potential}s that cannot be valid (are "off") if the
     * given potential is "on" (i.e. if its value is the correct one for the cell).
     * 
     * @param grid the Sudoku grid
     * @param p    the potential that is assumed to be "on"
     * @return the set of potentials that must be "off"
     */
    private Set<Potential> getOnToOff(Grid grid, Potential p, boolean isYChainEnabled) {
        Set<Potential> result = new LinkedHashSet<Potential>();

        int potentialCellIndex = p.cell.getIndex();
        if (isYChainEnabled) { // This rule is not used with X-Chains
            // First rule: other potential values for this cell get off
            BitSet potentialValues = grid.getCellPotentialValues(potentialCellIndex);
            for (int value = potentialValues.nextSetBit(0); value >= 0; value = potentialValues.nextSetBit(value + 1)) {
                if (value != p.value)
                    result.add(new Potential(p.cell, value, false, p, Potential.Cause.NakedSingle,
                            "the cell can contain only one value"));
            }
        }

        boolean[] addedPotential = new boolean[81];
        addedPotential[potentialCellIndex] = true;

        // Second rule: other potential position for this value get off
        Grid.Region box = Grid.getRegionAt(0, potentialCellIndex);
        BitSet boxPositions = box.copyPotentialPositions(grid, p.value);
        boxPositions.clear(box.indexOf(p.cell));
        if (Settings.getInstance().isBlocks())
            for (int i = boxPositions.nextSetBit(0); i >= 0; i = boxPositions.nextSetBit(i + 1)) {
                Cell cell = box.getCell(i);
                if (!addedPotential[cell.getIndex()]) {
                    result.add(new Potential(cell, p.value, false, p, getRegionCause(0),
                            "the value can occur only once in the " + box.toString()));
                    addedPotential[cell.getIndex()] = true;
                }
            }
        Grid.Region row = Grid.getRegionAt(1, potentialCellIndex);
        BitSet rowPositions = row.copyPotentialPositions(grid, p.value);
        rowPositions.clear(row.indexOf(p.cell));
        for (int i = rowPositions.nextSetBit(0); i >= 0; i = rowPositions.nextSetBit(i + 1)) {
            Cell cell = row.getCell(i);
            if (!addedPotential[cell.getIndex()]) {
                result.add(new Potential(cell, p.value, false, p, getRegionCause(1),
                        "the value can occur only once in the " + row.toString()));
                addedPotential[cell.getIndex()] = true;
            }
        }
        Grid.Region col = Grid.getRegionAt(2, potentialCellIndex);
        BitSet colPositions = col.copyPotentialPositions(grid, p.value);
        colPositions.clear(col.indexOf(p.cell));
        for (int i = colPositions.nextSetBit(0); i >= 0; i = colPositions.nextSetBit(i + 1)) {
            Cell cell = col.getCell(i);
            if (!addedPotential[cell.getIndex()]) {
                result.add(new Potential(cell, p.value, false, p, getRegionCause(2),
                        "the value can occur only once in the " + col.toString()));
                addedPotential[cell.getIndex()] = true;
            }
        }
        // @SudokuMonster: Added Variants changes
        if (!Settings.getInstance().isVLatin()) {
            Grid.Region dg = null;
            Grid.Region window = null;
            Grid.Region md = null;
            Grid.Region ad = null;
            Grid.Region girandola = null;
            Grid.Region asterisk = null;
            Grid.Region cd = null;
            if (Settings.getInstance().isDG()) {
                dg = Grid.getRegionAt(3, potentialCellIndex);
                BitSet dgPositions = dg.copyPotentialPositions(grid, p.value);
                dgPositions.clear(dg.indexOf(p.cell));
                for (int i = dgPositions.nextSetBit(0); i >= 0; i = dgPositions.nextSetBit(i + 1)) {
                    Cell cell = dg.getCell(i);
                    if (!addedPotential[cell.getIndex()]) {
                        result.add(new Potential(cell, p.value, false, p, getRegionCause(3),
                                "the value can occur only once in the " + dg.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
            if (Settings.getInstance().isWindows()) {
                window = Grid.getRegionAt(4, potentialCellIndex);
                BitSet windowPositions = window.copyPotentialPositions(grid, p.value);
                windowPositions.clear(window.indexOf(p.cell));
                for (int i = windowPositions.nextSetBit(0); i >= 0; i = windowPositions.nextSetBit(i + 1)) {
                    Cell cell = window.getCell(i);
                    if (!addedPotential[cell.getIndex()]) {
                        result.add(new Potential(cell, p.value, false, p, getRegionCause(4),
                                "the value can occur only once in the " + window.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
            if (Settings.getInstance().isX() && Grid.cellRegions[potentialCellIndex][5] == 0) {
                md = Grid.getRegionAt(5, potentialCellIndex);
                BitSet mdPositions = md.copyPotentialPositions(grid, p.value);
                mdPositions.clear(md.indexOf(p.cell));
                for (int i = mdPositions.nextSetBit(0); i >= 0; i = mdPositions.nextSetBit(i + 1)) {
                    Cell cell = md.getCell(i);
                    if (!addedPotential[cell.getIndex()]) {
                        result.add(new Potential(cell, p.value, false, p, getRegionCause(5),
                                "the value can occur only once in the " + md.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
            if (Settings.getInstance().isX() && Grid.cellRegions[potentialCellIndex][6] == 0) {
                ad = Grid.getRegionAt(6, potentialCellIndex);
                BitSet adPositions = ad.copyPotentialPositions(grid, p.value);
                adPositions.clear(ad.indexOf(p.cell));
                for (int i = adPositions.nextSetBit(0); i >= 0; i = adPositions.nextSetBit(i + 1)) {
                    Cell cell = ad.getCell(i);
                    if (!addedPotential[cell.getIndex()]) {
                        result.add(new Potential(cell, p.value, false, p, getRegionCause(6),
                                "the value can occur only once in the " + ad.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
            if (Settings.getInstance().isGirandola() && Grid.cellRegions[potentialCellIndex][7] == 0) {
                girandola = Grid.getRegionAt(7, potentialCellIndex);
                BitSet girandolaPositions = girandola.copyPotentialPositions(grid, p.value);
                girandolaPositions.clear(girandola.indexOf(p.cell));
                for (int i = girandolaPositions.nextSetBit(0); i >= 0; i = girandolaPositions.nextSetBit(i + 1)) {
                    Cell cell = girandola.getCell(i);
                    if (!addedPotential[cell.getIndex()]) {
                        result.add(new Potential(cell, p.value, false, p, getRegionCause(7),
                                "the value can occur only once in the " + girandola.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
            if (Settings.getInstance().isAsterisk() && Grid.cellRegions[potentialCellIndex][8] == 0) {
                asterisk = Grid.getRegionAt(8, potentialCellIndex);
                BitSet asteriskPositions = asterisk.copyPotentialPositions(grid, p.value);
                asteriskPositions.clear(asterisk.indexOf(p.cell));
                for (int i = asteriskPositions.nextSetBit(0); i >= 0; i = asteriskPositions.nextSetBit(i + 1)) {
                    Cell cell = asterisk.getCell(i);
                    if (!addedPotential[cell.getIndex()]) {
                        result.add(new Potential(cell, p.value, false, p, getRegionCause(8),
                                "the value can occur only once in the " + asterisk.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
            if (Settings.getInstance().isCD() && Grid.cellRegions[potentialCellIndex][9] == 0) {
                cd = Grid.getRegionAt(9, potentialCellIndex);
                BitSet cdPositions = cd.copyPotentialPositions(grid, p.value);
                cdPositions.clear(cd.indexOf(p.cell));
                for (int i = cdPositions.nextSetBit(0); i >= 0; i = cdPositions.nextSetBit(i + 1)) {
                    Cell cell = cd.getCell(i);
                    if (!addedPotential[cell.getIndex()]) {
                        result.add(new Potential(cell, p.value, false, p, getRegionCause(9),
                                "the value can occur only once in the " + cd.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
        }

        // @Rangsk: Added anti-king
        if (Settings.getInstance().isAntiFerz()) {
            Cell centerCell = Grid.getCell(potentialCellIndex);
            int centerCellX = centerCell.getX();
            int centerCellY = centerCell.getY();
            for (int ferzOffsetIndex = 0; ferzOffsetIndex < Grid.ferzCellIndex.length; ferzOffsetIndex++) {
                int ferzOffsetX = Grid.ferzCellIndex[ferzOffsetIndex][0];
                int ferzOffsetY = Grid.ferzCellIndex[ferzOffsetIndex][1];
                int ferzCellX = centerCellX + ferzOffsetX;
                int ferzCellY = centerCellY + ferzOffsetY;
                if (ferzCellX >= 0 && ferzCellY >= 0 && ferzCellX < 9 && ferzCellY < 9) {
                    Cell cell = Grid.getCell(ferzCellX, ferzCellY);
                    if (!addedPotential[cell.getIndex()] && grid.hasCellPotentialValue(cell.getIndex(), p.value)) {
                        result.add(new Potential(cell, p.value, false, p, Potential.Cause.NakedSingle,
                                "anti-king prevents the value from being the same as " + centerCell.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
        }

        // @Rangsk: Added anti-knight
        if (Settings.getInstance().isAntiKnight()) {
            Cell centerCell = Grid.getCell(potentialCellIndex);
            int centerCellX = centerCell.getX();
            int centerCellY = centerCell.getY();
            for (int knightOffsetIndex = 0; knightOffsetIndex < Grid.knightCellIndex.length; knightOffsetIndex++) {
                int knightOffsetX = Grid.knightCellIndex[knightOffsetIndex][0];
                int knightOffsetY = Grid.knightCellIndex[knightOffsetIndex][1];
                int knightCellX = centerCellX + knightOffsetX;
                int knightCellY = centerCellY + knightOffsetY;
                if (knightCellX >= 0 && knightCellY >= 0 && knightCellX < 9 && knightCellY < 9) {
                    Cell cell = Grid.getCell(knightCellX, knightCellY);
                    if (!addedPotential[cell.getIndex()] && grid.hasCellPotentialValue(cell.getIndex(), p.value)) {
                        result.add(new Potential(cell, p.value, false, p, Potential.Cause.NakedSingle,
                                "anti-knight prevents the value from being the same as " + centerCell.toString()));
                        addedPotential[cell.getIndex()] = true;
                    }
                }
            }
        }

        // @Rangsk: Added non-consecutive
        if (Settings.getInstance().isForbiddenPairs() && Settings.getInstance().whichNC() > 0) {
            int statusNC = Settings.getInstance().whichNC();
            int i = potentialCellIndex;
            Cell centerCell = Grid.getCell(i);
            int value = p.value;
            boolean isWazir = statusNC == 1 || statusNC == 2;
            boolean isNCToroidal = statusNC == 2 || statusNC == 4;
            int[][] lookupCells = Settings.getInstance().isToroidal()
                    ? (isWazir ? Grid.wazirCellsToroidal : Grid.ferzCellsToroidal)
                    : (isWazir ? Grid.wazirCellsRegular : Grid.ferzCellsRegular);

            int j = lookupCells[i].length;
            for (int k = 0; k < j; k++) {
                int cellIndex = lookupCells[i][k];
                Cell cell = Grid.getCell(cellIndex);
                if (isNCToroidal || value < 9) {
                    int ncValue = value == 9 ? 1 : value + 1;
                    if (grid.hasCellPotentialValue(cell.getIndex(), ncValue)) {
                        result.add(new Potential(cell, ncValue, false, p, Potential.Cause.NakedSingle,
                                "The value is consecutive with " + centerCell.toString()));
                    }
                }
                if (isNCToroidal || value > 1) {
                    int ncValue = value == 1 ? 9 : value - 1;
                    if (grid.hasCellPotentialValue(cell.getIndex(), ncValue)) {
                        result.add(new Potential(cell, ncValue, false, p, Potential.Cause.NakedSingle,
                                "The value is consecutive with " + centerCell.toString()));
                    }
                }
            }
        }
        return result;
    }

    private void addHiddenParentsOfCell(Potential p, Grid grid, Grid source,
            LinkedSet<Potential> offPotentials) {
    	int i = p.cell.getIndex();
    	for (int value = 1; value <= 9; value++) {
            if (source.hasCellPotentialValue(i, value) && !grid.hasCellPotentialValue(i, value)) {
                // Add a hidden parent
                Potential parent = new Potential(p.cell, value, false);
                parent = offPotentials.get(parent); // Retrieve complete version
                if (parent == null)
                    throw new RuntimeException("Parent not found");
                p.parents.add(parent);
            }
        }
    }

    private void addHiddenParentsOfRegion(Potential p, Grid grid, Grid source,
            Grid.Region curRegion, LinkedSet<Potential> offPotentials) {
        //Grid.Region srcRegion = Grid.getRegionAt(curRegion.getRegionTypeIndex(), p.cell.getIndex());
        int value = p.value;
        BitSet curPositions = curRegion.copyPotentialPositions(grid, value);
        //BitSet srcPositions = srcRegion.copyPotentialPositions(source, value);
        BitSet srcPositions = curRegion.copyPotentialPositions(source, value);
        // Get positions of the potential value that have been removed
        srcPositions.andNot(curPositions);
        for (int i = srcPositions.nextSetBit(0); i >= 0; i = srcPositions.nextSetBit(i + 1)) {
            // Add a hidden parent
            Cell curCell = curRegion.getCell(i);
            Potential parent = new Potential(curCell, value, false);
            parent = offPotentials.get(parent); // Retrieve complete version
            if (parent == null)
                throw new RuntimeException("Parent not found");
            p.parents.add(parent);
        }
    }

    static Potential.Cause getRegionCause(Region region) { //still in use by collectRuleParents where for regionchaining region is used for repaint and stings
        if (region instanceof Block && Settings.getInstance().isBlocks())
            return Potential.Cause.HiddenBlock;
        else if (region instanceof Column)
            return Potential.Cause.HiddenColumn;
         else if (region instanceof Row)
            return Potential.Cause.HiddenRow;
//@SudokuMonster: Variants changes
         else if (region instanceof DG && Settings.getInstance().isDG())
            return Potential.Cause.HiddenDG;
         else if (region instanceof Window && Settings.getInstance().isWindows())
            return Potential.Cause.HiddenWindow;
         else if (region instanceof diagonalMain && Settings.getInstance().isX())
            return Potential.Cause.HiddenMD;
         else if (region instanceof diagonalAnti && Settings.getInstance().isX())
            return Potential.Cause.HiddenAD;
         else if (region instanceof Girandola && Settings.getInstance().isGirandola())
            return Potential.Cause.HiddenGirandola;
         else if (region instanceof Asterisk && Settings.getInstance().isAsterisk())
            return Potential.Cause.HiddenAsterisk;
         else if (region instanceof CD && Settings.getInstance().isCD())
            return Potential.Cause.HiddenCD;
//@SudokuMonster: Variants changes	Added to stop warning
		return Potential.Cause.HiddenRow;		
    }

//@SudokuMonster: Variants changes    
    static Potential.Cause regionCauses[] = {
    		Potential.Cause.HiddenBlock,
    		Potential.Cause.HiddenRow,
    		Potential.Cause.HiddenColumn,
			Potential.Cause.HiddenDG,
			Potential.Cause.HiddenWindow,
			Potential.Cause.HiddenMD,
			Potential.Cause.HiddenAD,
			Potential.Cause.HiddenGirandola,
			Potential.Cause.HiddenAsterisk,
			Potential.Cause.HiddenCD
    };
    
    static Potential.Cause getRegionCause(int regionTypeIndex) {
    	return regionCauses[regionTypeIndex];
    }

    /**
     * Get the set of all {@link Potential}s that must be
     * "on" (i.e. if their values are their correct cell's values)
     * if the given potential is not valid ("off").
     * @param grid the Sudoku grid
     * @param p the potential that is assumed to be "off"
     * @return the set of potentials that must be "on"
     */
    private Set<Potential> getOffToOn(Grid grid, Potential p, Grid source,
            LinkedSet<Potential> offPotentials, boolean isYChainEnabled,
            boolean isXChainEnabled) {
		Set<Potential> result = new LinkedHashSet<Potential>();
															  
    	int thisCellIndex = p.cell.getIndex();
        if (isYChainEnabled) {
            // First rule: if there is only two potentials in this cell, the other one gets on
            BitSet potentialValues = grid.getCellPotentialValues(thisCellIndex);
            if (potentialValues.cardinality() == 2) {
                int otherValue = potentialValues.nextSetBit(0);
                if (otherValue == p.value)
                    otherValue = potentialValues.nextSetBit(otherValue + 1);
                Potential pOn = new Potential(p.cell, otherValue, true, p,
                        Potential.Cause.NakedSingle, "only remaining possible value in the cell");
                addHiddenParentsOfCell(pOn, grid, source, offPotentials);
                result.add(pOn);
            }
        }

        if (isXChainEnabled) {
            // Second rule: if there are only two positions for this potential, the other one gets on
        	int thisValue = p.value;
        	//SudokuMonster: Variants changes
			for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
				if (!Settings.getInstance().isVLatin()) {
					if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
					if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
					if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
					if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
					if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
					if (Grid.cellRegions[thisCellIndex][regionTypeIndex] < 0)
						continue;
				}      		
				Region r = Grid.regions[regionTypeIndex][Grid.cellRegions[thisCellIndex][regionTypeIndex]];
				int otherPosition = -1;
	        	for(int regionCellIndex = 0; regionCellIndex < 9; regionCellIndex++) {
	        		int cellIndex = r.getCell(regionCellIndex).getIndex();
	        		if(cellIndex == thisCellIndex) continue;
	        		if(grid.hasCellPotentialValue(cellIndex, thisValue)) {
	        			if(otherPosition >= 0) { //third cell in a house has this candidate
	        				otherPosition = -1;
	        				break;
	        			}
	        			otherPosition = cellIndex;
	        		}
	        	} //region cells
	        	if(otherPosition >= 0) { //exactly one other position
                    Potential pOn = new Potential(Grid.getCell(otherPosition), thisValue, true, p,
                            getRegionCause(regionTypeIndex),
                            "only remaining possible position in the " + r.toString());
                    addHiddenParentsOfRegion(pOn, grid, source, r, offPotentials);						   
                    result.add(pOn);	   	  
	        	}
        	} // region types
        }

        return result;
    }

    /**
     * Whether <tt>parent</tt> is an ancestor of <tt>child</tt>.
     */
    private boolean isParent(Potential child, Potential parent) {
        Potential pTest = child;
        while (!pTest.parents.isEmpty()) {
            pTest = pTest.parents.get(0);
            if (pTest.equals(parent))
                return true;
        }
        return false;
    }

    private void doCycles(Grid grid, LinkedSet<Potential> toOn, LinkedSet<Potential> toOff, boolean isYChainEnabled,
            boolean isXChainEnabled, List<Potential> cycles, Potential source) {
        //List<Potential> pendingOn = new LinkedList<Potential>(toOn);
        //List<Potential> pendingOff = new LinkedList<Potential>(toOff);
        Queue<Potential> pendingOn = new LinkedList<Potential>(toOn);
        Queue<Potential> pendingOff = new LinkedList<Potential>(toOff);
        // Mind why this is a BFS and works. I learned that cycles are only found by DFS
        // Maybe we are missing loops

        int length = 0; // Cycle length
        while (!pendingOn.isEmpty() || !pendingOff.isEmpty()) {
            length++;
            //while (!pendingOn.isEmpty()) {
                //Potential p = pendingOn.remove(0);
            Potential p;
            while((p = pendingOn.poll()) != null) {
                Set<Potential> makeOff = getOnToOff(grid, p, isYChainEnabled);
                for (Potential pOff : makeOff) {
                    if (!isParent(p, pOff)) {
                        // Not processed yet
                        pendingOff.add(pOff);
                        toOff.add(pOff);
                    }
                }
            }
            length++;
            //while (!pendingOff.isEmpty()) {
                //Potential p = pendingOff.remove(0);
            while((p = pendingOff.poll()) != null) {
                Set<Potential> makeOn = getOffToOn(grid, p, saveGrid, toOff, isYChainEnabled, isXChainEnabled);
                for (Potential pOn : makeOn) {
                    if (length >= 4 && pOn.equals(source)) {
                        // Cycle found
                        cycles.add(pOn);
                    }
                    if (!toOn.contains(pOn)) {
                        // Not processed yet
                        pendingOn.add(pOn);
                        toOn.add(pOn);
                    }
                }
            }
        }
    }

    private void doForcingChains(Grid grid, LinkedSet<Potential> toOn,
            LinkedSet<Potential> toOff, boolean isYChainEnabled,
            List<Potential> chains, Potential source) {
        //List<Potential> pendingOn = new LinkedList<Potential>(toOn);
        //List<Potential> pendingOff = new LinkedList<Potential>(toOff);
        Queue<Potential> pendingOn = new LinkedList<Potential>(toOn);
        Queue<Potential> pendingOff = new LinkedList<Potential>(toOff);
        while (!pendingOn.isEmpty() || !pendingOff.isEmpty()) {
            //while (!pendingOn.isEmpty()) {
                //Potential p = pendingOn.remove(0);
        	Potential p;
        	while((p = pendingOn.poll()) != null) {
                Set<Potential> makeOff = getOnToOff(grid, p, isYChainEnabled);
                for (Potential pOff : makeOff) {
                    Potential pOn = new Potential(pOff.cell, pOff.value, true); // Conjugate
                    if (source.equals(pOn)) {
                        // Cyclic contradiction (forcing chain) found
                        if (!chains.contains(pOff))
                            chains.add(pOff);
                    }
                    if (!toOff.contains(pOff)) {
                        // Not processed yet
                        pendingOff.add(pOff);
                        toOff.add(pOff);
                    }
                }
            }
            //while (!pendingOff.isEmpty()) {
                //Potential p = pendingOff.remove(0);
        	while((p = pendingOff.poll()) != null) {
                Set<Potential> makeOn = getOffToOn(grid, p, saveGrid, toOff,
                        isYChainEnabled, true);
                for (Potential pOn : makeOn) {
                    Potential pOff = new Potential(pOn.cell, pOn.value, false); // Conjugate
                    if (source.equals(pOff)) {
                        // Cyclic contradiction (forcing chain) found
                        if (!chains.contains(pOn))
                            chains.add(pOn);
                    }
                    if (!toOn.contains(pOn)) {
                        // Not processed yet
                        pendingOn.add(pOn);
                        toOn.add(pOn);
                    }
                }
            }
        }
    }

    /**
     * Given the initial sets of potentials that are assumed to be "on" and "off",
     * complete the sets with all other potentials that must be "on"
     * or "off" as a result of the assumption.
     * Completion is done on the basis of direct eliminations for level 0.
     * For nested levels > 0 additional eliminations are included.
     * <p>
     * Both sets must be disjoint, and remain disjoint after this call.
     * @param grid the grid
     * @param toOn the potentials that are assumed to be "on"
     * @param toOff the potentials that are assumed to be "off"
     * @return <code>null</code> on success; the first potential that would have
     * to be both "on" and "off" else.
     */
    private Potential[] doChaining(Grid grid, LinkedSet<Potential> toOn, LinkedSet<Potential> toOff) {
    	//MD: Note that toOn potentials have higher precedence than toOff which can result in non-shortest contradiction chain finding.
        grid.copyTo(saveGrid);
        try {
            Queue<Potential> pendingOn = new LinkedList<Potential>(toOn);
            Queue<Potential> pendingOff = new LinkedList<Potential>(toOff);
            Potential p = null;
            do {
            	p = pendingOn.poll();
                if (p != null) {
                    Set<Potential> makeOff = getOnToOff(grid, p, !isNisho);
                    for (Potential pOff : makeOff) {
                        Potential pOn = new Potential(pOff.cell, pOff.value, true); // Conjugate
                        if (toOn.contains(pOn)) {
                            // Contradiction found
                            pOn = toOn.get(pOn); // Retrieve version of conjugate with parents
                            return new Potential[] {pOn, pOff}; // Cannot be both on and off at the same time
                        } else if (!toOff.contains(pOff)) {
                            // Not processed yet
                            toOff.add(pOff);
                            pendingOff.add(pOff);
                        }
                    }
                    continue;
                }
                p = pendingOff.poll();
                if (p != null) {
                    Set<Potential> makeOn = getOffToOn(grid, p, saveGrid, toOff, !isNisho, true);
                    if (isDynamic)
                        p.off(grid); // writes to grid
                    for (Potential pOn : makeOn) {
                        Potential pOff = new Potential(pOn.cell, pOn.value, false); // Conjugate
                        if (toOff.contains(pOff)) {
                            // Contradiction found
                            pOff = toOff.get(pOff); // Retrieve version of conjugate with parents
                            return new Potential[] {pOn, pOff}; // Cannot be both on and off at the same time
                        } else if (!toOn.contains(pOn)) {
                            // Not processed yet
                            toOn.add(pOn);
                            pendingOn.add(pOn);
                        }
                    }
                	continue;
                }
                if (level > 0) {
                    for (Potential pOff : getAdvancedPotentials(grid, saveGrid, toOff)) {
                        if (!toOff.contains(pOff)) {
                            // Not processed yet
                            toOff.add(pOff);
                            pendingOff.add(pOff);
                            p = pOff; //just a marker that the main loop should continue
                        }
                    }
                }
            } while(p != null);
            return null;
        } finally {
            saveGrid.copyTo(grid);
        }
    }

    /**
     * Get all non-trivial implications (involving fished, naked/hidden sets, etc).
     */
    private Collection<Potential> getAdvancedPotentials(final Grid grid, final Grid source,
            final LinkedSet<Potential> offPotentials) {
        final Collection<Potential> result = new ArrayList<Potential>();
        if (otherRules == null) {
            otherRules = new ArrayList<IndirectHintProducer>();
			if (Settings.getInstance().isVLatin()) {
				if (Settings.getInstance().isBlocks())
					otherRules.add(new Locking(false));
				otherRules.add(new HiddenSet(2, false));
				otherRules.add(new NakedSet(2));
				otherRules.add(new Fisherman(2));
	//@SudokuMonster: FCPlus will control non-trivial implications added
				if (Settings.getInstance().FCPlus() > 0) {
					otherRules.add(new TurbotFish());
					otherRules.add(new XYWing(false));
					otherRules.add(new XYWing(true));
				}
				if (Settings.getInstance().FCPlus() > 1) {
					otherRules.add(new HiddenSet(3, false));
					otherRules.add(new NakedSet(3));
					otherRules.add(new Fisherman(3));
					otherRules.add(new StrongLinks(3));
					otherRules.add(new WXYZWing());
					otherRules.add(new VWXYZWing());
					otherRules.add(new AlignedExclusion(3));
					otherRules.add(new UniqueLoops());
					otherRules.add(new BivalueUniversalGrave());
				}
	//            //otherRules.add(new HiddenSingle());
	//            //otherRules.add(new Locking(true));
	//            //otherRules.add(new HiddenSet(2, true));
	//            //otherRules.add(new NakedSingle());
	//            //otherRules.add(new HiddenSet(3, true));
	//            otherRules.add(new Locking(false));
	//            otherRules.add(new NakedSet(2));
	//            otherRules.add(new Fisherman(2));
	//            otherRules.add(new HiddenSet(2, false));
	//            otherRules.add(new NakedSet(3));
	//            otherRules.add(new Fisherman(3));
	//            otherRules.add(new HiddenSet(3, false));
	//            otherRules.add(new XYWing(false));
	//            otherRules.add(new XYWing(true));
	//            //otherRules.add(new UniqueLoops());
	//            otherRules.add(new NakedSet(4));
	//            otherRules.add(new Fisherman(4));
	//            otherRules.add(new HiddenSet(4, false));
	//            //otherRules.add(new BivalueUniversalGrave());
	//            //otherRules.add(new AlignedPairExclusion());
	//            //otherRules.add(new AlignedExclusion(3));
			}
			else {
				otherRules.add(new VLocking());
				otherRules.add(new HiddenSet(2, false));
				otherRules.add(new NakedSetGen(2));
				otherRules.add(new Fisherman(2));
	//@SudokuMonster: FCPlus will control non-trivial implications added
				if (Settings.getInstance().FCPlus() > 0) {
					otherRules.add(new TurbotFish());
					otherRules.add(new XYWing(false));
					otherRules.add(new XYWing(true));
				}
				if (Settings.getInstance().FCPlus() > 1) {
					otherRules.add(new HiddenSet(3, false));
					otherRules.add(new NakedSetGen(3));
					otherRules.add(new Fisherman(3));
					//otherRules.add(new StrongLinks(3));
					otherRules.add(new WXYZWing());
				}				
			}
            if (level < 4) {
                if (level >= 2)
                    otherRules.add(new Chaining(false, false, false, 0, true, 0)); // Forcing chains
                if (level >= 3)
                    otherRules.add(new Chaining(true, false, false, 0, true, 0)); // Multiple forcing chains
            } else {
//                // Dynamic Forcing Chains already cover Simple and Multiple Forcing Chains
//                if (level >= 4)
//                    otherRules.add(new Chaining(true, true, false, 0)); // Dynamic FC
//                if (level >= 5)
//                    otherRules.add(new Chaining(true, true, false, level - 3));
                otherRules.add(new Chaining(true, true, false, nestingLimit, true, 0)); // Dynamic FC
//                otherRules.add(new Chaining(true, true, false, 1, true)); // Dynamic FC+
//                otherRules.add(new Chaining(true, true, false, 2, true)); // Dynamic FC++
//                otherRules.add(new Chaining(true, true, false, 3, true)); // Dynamic FC+++
            }
        }
        int index = 0;
        while (index < otherRules.size() && result.isEmpty()) {
            IndirectHintProducer rule = otherRules.get(index);
            try {
                rule.getHints(grid, new HintsAccumulator() {
                    public void add(Hint hint0) {
                        IndirectHint hint = (IndirectHint)hint0;
                        Collection<Potential> parents =
                            ((HasParentPotentialHint)hint).getRuleParents(source, grid);
                        /*
                         * If no parent can be found, the rule probably already exists without
                         * the chain. Therefore it is useless to include it in the chain.
                         */
                        if (!parents.isEmpty()) {
                            ChainingHint nested = null;
                            if (hint instanceof ChainingHint)
                                nested = (ChainingHint)hint;
                            Map<Cell, BitSet> removable = hint.getRemovablePotentials();
                            assert !removable.isEmpty();
                            //for (Cell cell : removable.keySet()) {
                            for (Map.Entry<Cell, BitSet> entry : removable.entrySet()) {
                                //BitSet values = removable.get(cell);
                            	Cell cell = entry.getKey();
                                BitSet values = entry.getValue();
                               for (int value = values.nextSetBit(0); value != -1; value = values.nextSetBit(value + 1)) {
                                    //Potential.Cause cause = Potential.Cause.Advanced;
                                    Potential toOff = new Potential(cell, value, false, Potential.Cause.Advanced, hint.toString(), nested);
                                    for (Potential p : parents) {
                                        Potential real = offPotentials.get(p);
                                        assert real != null;
                                        toOff.parents.add(real);
                                    }
                                    result.add(toOff);
                                }
                            }
                        }
                    }
                });
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
            index++;
        }
        return result;
    }

    private CycleHint createCycleHint(Grid grid, Potential dstOn, Potential dstOff,
            boolean isYChain, boolean isXChain) {

        // Build list of cells in the chain
        Collection<Cell> cells = new LinkedHashSet<Cell>();
        Potential p = dstOn;
        while (!p.parents.isEmpty()) {
            assert p.parents.size() == 1;
            cells.add(p.cell);
            p = p.parents.get(0);
        }
        assert p.equals(dstOn); // dstOn should occur at begin and end

        // Build canceled potentials
        Collection<Potential> cancelForw = new LinkedHashSet<Potential>();
        Collection<Potential> cancelBack = new LinkedHashSet<Potential>();
        p = dstOn;
        while (!p.parents.isEmpty()) {
            assert p.parents.size() == 1;
            for (int cellIndex : p.cell.getVisibleCellIndexes()) {
            	Cell cell = Grid.getCell(cellIndex);
                if (!cells.contains(cell) && grid.hasCellPotentialValue(cellIndex, p.value)) {
                    if (p.isOn)
                        cancelForw.add(new Potential(cell, p.value, false));
                    else
                        cancelBack.add(new Potential(cell, p.value, false));
                }
            }
            p = p.parents.get(0);
        }
        assert p.equals(dstOn); // dstOn should occur at begin and end

        // Build removable potentials
        Collection<Potential> cancel = cancelForw;
        cancel.retainAll(cancelBack);
        Map<Cell,BitSet> removable = new HashMap<Cell,BitSet>();
        for (Potential rp : cancel) {
            BitSet values = removable.get(rp.cell);
            if (values == null)
                removable.put(rp.cell, SingletonBitSet.create(rp.value));
            else
                values.set(rp.value);
        }

        return new CycleHint(this, removable, isYChain, isXChain, dstOn, dstOff);
    }

    private ForcingChainHint createForcingChainHint(Grid grid, Potential target,
            boolean isYChain, boolean isXChain) {

        Map<Cell, BitSet> removable = new HashMap<Cell, BitSet>();
        if (!target.isOn)
            removable.put(target.cell, SingletonBitSet.create(target.value));
        else {
            BitSet values = new BitSet(10);
            for (int value = 1; value <= 9; value++) {
                //if (value != target.value && target.cell.hasPotentialValue(value))
                if (value != target.value && grid.hasCellPotentialValue(target.cell.getIndex(), value))
                    values.set(value);
            }
            removable.put(target.cell, values);
        }

        return new ForcingChainHint(this, removable, isYChain, isXChain, target);
    }

    private BinaryChainingHint createChainingOnHint(Grid grid, Potential dstOn, Potential dstOff,
            Potential source, Potential target, boolean isAbsurd) {

        // Build removable potentials (all values different that target value)
        Map<Cell,BitSet> cellRemovablePotentials = new HashMap<Cell,BitSet>();
        BitSet removable = (BitSet)grid.getCellPotentialValues(target.cell.getIndex()).clone();
        removable.set(target.value, false);
        if (!removable.isEmpty())
            cellRemovablePotentials.put(target.cell, removable);

        return new BinaryChainingHint(this, cellRemovablePotentials, source, dstOn, dstOff,
                isAbsurd, isNisho);
    }

    private BinaryChainingHint createChainingOffHint(Potential dstOn, Potential dstOff,
            Potential source, Potential target, boolean isAbsurd) {

        // Build removable potentials (target value)
        Map<Cell,BitSet> cellRemovablePotentials = new HashMap<Cell,BitSet>();
        cellRemovablePotentials.put(target.cell, SingletonBitSet.create(target.value));

        return new BinaryChainingHint(this, cellRemovablePotentials, source, dstOn, dstOff,
                isAbsurd, isNisho);
    }

    private CellChainingHint createCellReductionHint(Grid grid, Cell srcCell, Potential target,
            Map<Integer, LinkedSet<Potential>> outcomes) {

        // Build removable potentials
        Map<Cell,BitSet> cellRemovablePotentials = new HashMap<Cell,BitSet>();
        if (target.isOn) {
            BitSet removable = (BitSet)grid.getCellPotentialValues(target.cell.getIndex()).clone();
            removable.set(target.value, false);
            if (!removable.isEmpty())
                cellRemovablePotentials.put(target.cell, removable);
        } else {
            cellRemovablePotentials.put(target.cell, SingletonBitSet.create(target.value));
        }

        // Build chains
        LinkedHashMap<Integer, Potential> chains = new LinkedHashMap<Integer, Potential>();
        for (int value = 1; value <= 9; value++) {
            if (grid.hasCellPotentialValue(srcCell.getIndex(), value)) {
                // Get corresponding value with the matching parents
                Potential valueTarget = outcomes.get(value).get(target);
                chains.put(value, valueTarget);
            }
        }

        return new CellChainingHint(this, cellRemovablePotentials, srcCell, chains);
    }

    private RegionChainingHint createRegionReductionHint(Grid grid, Grid.Region region, int value,
            Potential target, Map<Integer, LinkedSet<Potential>> outcomes) {

        // Build removable potentials
        Map<Cell,BitSet> cellRemovablePotentials = new HashMap<Cell,BitSet>();
        if (target.isOn) {
            BitSet removable = (BitSet)grid.getCellPotentialValues(target.cell.getIndex()).clone();
            removable.set(target.value, false);
            if (!removable.isEmpty())
                cellRemovablePotentials.put(target.cell, removable);
        } else {
            cellRemovablePotentials.put(target.cell, SingletonBitSet.create(target.value));
        }

        // Build chains
        LinkedHashMap<Integer, Potential> chains = new LinkedHashMap<Integer, Potential>();
        BitSet potentialPositions = region.getPotentialPositions(grid, value);
        for (int pos = 0; pos < 9; pos++) {
            if (potentialPositions.get(pos)) {
                // Get corresponding value with the matching parents
                Potential posTarget = outcomes.get(pos).get(target);
                chains.put(pos, posTarget);
            }
        }

        return new RegionChainingHint(this, cellRemovablePotentials, region, value, chains);
    }

    public String getCommonName(ChainingHint hint) {
        if (!isDynamic && !isMultipleEnabled) {
            if (hint.isXChain) 
                return "X-Chain";
            else
                return "Y-Chain";
        }
        return null;
    }

    static String getNestedSuffix(int level) {
        if (level == 1)
            return " (+)";
        else if (level == 2)
            return " (+ Forcing Chains)";
        else if (level == 3)
            return " (+ Multiple Forcing Chains)";
        else if (level == 4)
            return " (+ Dynamic Forcing Chains)";
        else if (level >= 5)
            return " (+ Dynamic Forcing Chains" + getNestedSuffix(level - 3) + ")";
        return "";
    }

    static String getShortNestedSuffix(int level) {
        if (level == 1)
            return "+";
        else if (level == 2)
            return "+FC";
        else if (level == 3)
            return "+MFC";
        else if (level == 4)
            return "+DFC";
        else if (level >= 5)
            return "+DFC" + getShortNestedSuffix(level - 3);
        return "";
    }

    @Override
    public String toString() {
        if (isNisho)
            return "Nishio Forcing Chains";
        else if (isDynamic) {
            if (level == 0)
                return "Dynamic Forcing Chains";
            else
                return "Dynamic Forcing Chains" + getNestedSuffix(level);
        } else if (isMultipleEnabled)
            return "Multiple Forcing Chains";
        else
            return "Forcing Chains & Cycles";
    }

    private void getPreviousHints(HintsAccumulator accu) throws InterruptedException {
        for (ChainingHint hint : lastHints)
            accu.add(hint);
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        if (lastGrid != null && grid.equals(lastGrid)) {
            getPreviousHints(accu);
            return;
        }
        List<ChainingHint> result = getHintList(grid);
        lastGrid = new Grid();
        grid.copyTo(lastGrid);
        //if(Settings.getInstance().getBestHintOnly()) {
        if(accu instanceof SingleHintAccumulator) { 
            lastHints = new LinkedHashSet<ChainingHint>();
            if(! result.isEmpty()) {
            	lastHints.add(result.get(0));
            }
        }
        else {
	        // This filters hints that are equal:
	        lastHints = new LinkedHashSet<ChainingHint>(result);
        }
        for (IndirectHint hint : lastHints)
            accu.add(hint);
    }

}
