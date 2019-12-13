/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Implementation of Pointing and Claiming solving techniques.
 */
public class Locking implements IndirectHintProducer {

    private final boolean isDirectMode;

    public Locking(boolean isDirectMode) {
        this.isDirectMode = isDirectMode;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        if (Settings.getInstance().isBlocks()) {
			getHints(grid, 0, 2, accu); //block, column
			getHints(grid, 0, 1, accu); //block, row
			getHints(grid, 2, 0, accu); //column, block
			getHints(grid, 1, 0, accu); //row, block
//@SudokuMonster: Added Variants
			if (Settings.getInstance().isDG()) {
				getHints(grid, 3, 2, accu); //DG, column
				getHints(grid, 3, 1, accu); //DG, row
				getHints(grid, 2, 3, accu); //column, DG
				getHints(grid, 1, 3, accu); //row, DG
				getHints(grid, 0, 3, accu); //block, DG
				getHints(grid, 3, 0, accu); //DG, block					
			}
			if (Settings.getInstance().isWindows()) {
				getHints(grid, 4, 2, accu); //window, column
				getHints(grid, 4, 1, accu); //window, row
				getHints(grid, 2, 4, accu); //column, window
				getHints(grid, 1, 4, accu); //row, window
				getHints(grid, 0, 4, accu); //block, window
				getHints(grid, 4, 0, accu); //window, block					
			}
			if (Settings.getInstance().isWindows() && Settings.getInstance().isDG()) {
				getHints(grid, 4, 3, accu); //window, DG
				getHints(grid, 3, 4, accu); //DG, window
			}
			
		}
    }

    /**
     * Given two part types, iterate on pairs of parts of both types that
     * are crossing. For each such pair (p1, p2), check if all the potential
     * positions of a value in p1 are also in p2.
     * <p>
     * Note: at least one of the two part type must be a
     * {@link Grid.Block 3x3 square}.
     * @param regionType1 the first part type
     * @param regionType2 the second part type
     */
    private void getHints(Grid grid, int regionType1Index, int regionType2Index,
            HintsAccumulator accu) throws InterruptedException {
        assert (regionType1Index == 0) != (regionType2Index == 0);

        // Iterate on values
        for (int value = 1; value <= 9; value++) {
	        // Iterate on pairs of parts
	        for (int i1 = 0; i1 < 9; i1++) {
	            Grid.Region region1 = Grid.getRegions(regionType1Index)[i1];
                // Get the potential positions of the value in part1
            	BitSet potentialPositions = region1.getPotentialPositions(grid, value);
                // Note: if cardinality == 1, this is Hidden Single in part1
                if (potentialPositions.cardinality() < 2) continue;
	            for (int i2 = 0; i2 < 9; i2++) {
	                Grid.Region region2 = Grid.getRegions(regionType2Index)[i2];
	                if(!region1.crosses(region2)) continue;
                    //CellSet region2Cells = region2.getCellSet();
                    boolean isInCommonSet = true;
                    // Test if all potential positions are also in part2
                    for(int i = potentialPositions.nextSetBit(0); i >= 0; i = potentialPositions.nextSetBit(i + 1)) {
                        Cell cell = region1.getCell(i);
                        //if (!region2Cells.containsCell(cell)) {
                        if (!region2.regionCellsBitSet.get(cell.getIndex())) {
                            isInCommonSet = false;
                            break;
                        }
                    }
                    if (isInCommonSet) {
                        if (isDirectMode) {
                            lookForFollowingHiddenSingles(grid, regionType1Index, accu, i1,
                                    region1, region2, value);
                        } else {
                            // Potential solution found
                            IndirectHint hint = createLockingHint(grid, region1, region2, null, value);
                            if (hint.isWorth())
                                accu.add(hint);
                        }
                    }
	            }
	        }
        } // for each value
    }

    private void lookForFollowingHiddenSingles(Grid grid,
            int regionType1Index, HintsAccumulator accu, int i1,
            Grid.Region region1, Grid.Region region2, int value) throws InterruptedException {
        // Look if the pointing / claiming induce a hidden single
        for(int i3 = 0; i3 < 9; i3++) {
            if (i3 == i1) continue;
            Grid.Region region3 = Grid.getRegions(regionType1Index)[i3];
            if (!region3.crosses(region2)) continue;
            // Region <> region1 but crosses region2
            //CellSet region2Cells = region2.getCellSet();
            BitSet potentialPositions3 = region3.getPotentialPositions(grid, value);
            if (potentialPositions3.cardinality() > 1) {
                int nbRemainInRegion3 = 0;
                Cell hcell = null;
                for (int i = 0; i < 9; i++) {
                    if (potentialPositions3.get(i)) {
                        Cell cell = region3.getCell(i);
                        //if (!region2Cells.containsCell(cell)) { // This position is not removed
                        if (!region2.regionCellsBitSet.get(cell.getIndex())) { // This position is not removed
                            nbRemainInRegion3++;
                            hcell = cell;
                        }
                    }
                }
                if (nbRemainInRegion3 == 1) {
                    IndirectHint hint = createLockingHint(grid, region1, region2, hcell, value);
                    if (hint.isWorth())
                        accu.add(hint);
                }
            }
        }
    }

    private IndirectHint createLockingHint(Grid grid, Grid.Region p1, Grid.Region p2, Cell hcell, int value) {
        // Build highlighted potentials
        Map<Cell,BitSet> cellPotentials = new HashMap<Cell,BitSet>();
        for (int i = 0; i < 9; i++) {
            Cell cell = p1.getCell(i);
            if (grid.hasCellPotentialValue(cell.getIndex(), value))
                cellPotentials.put(cell, SingletonBitSet.create(value));
        }
        // Build removable potentials
        Map<Cell,BitSet> cellRemovablePotentials = new HashMap<Cell,BitSet>();
        List<Cell> highlightedCells = new ArrayList<Cell>();
        //CellSet p1Cells = p1.getCellSet();
        for (int i = 0; i < 9; i++) {
            Cell cell = p2.getCell(i);
            //if (!p1Cells.containsCell(cell)) {
            if (!p1.regionCellsBitSet.get(cell.getIndex())) {
                if (grid.hasCellPotentialValue(cell.getIndex(), value))
                    cellRemovablePotentials.put(cell, SingletonBitSet.create(value));
            } else if (grid.hasCellPotentialValue(cell.getIndex(), value))
                highlightedCells.add(cell);
        }
        // Build list of cells
        Cell[] cells = new Cell[highlightedCells.size()];
        highlightedCells.toArray(cells);
        // Build hint
        if (isDirectMode)
            return new DirectLockingHint(this, cells, hcell, value, cellPotentials,
                    cellRemovablePotentials, p1, p2);
        else
            return new LockingHint(this, cells, value, cellPotentials,
                    cellRemovablePotentials, p1, p2);
    }

    @Override
    public String toString() {
        if (isDirectMode)
            return "Direct Intersections";
        else
            return "Intersections";
    }

}
