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
 * Implementation of Generalized Intersectio  technique by Tarek Maani (@SudokuMonster).
 */
public class VLocking implements IndirectHintProducer {
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        if (Settings.getInstance().isBlocks())
			if (Settings.getInstance().isBlocks())
				getHints(grid, 0, accu); //block
			getHints(grid, 1, accu); //row
			getHints(grid, 2, accu); //column
//@SudokuMonster: Added Variants
			if (Settings.getInstance().isDG()) {
				getHints(grid, 3, accu); //DG				
			}
			if (Settings.getInstance().isWindows()) {
				getHints(grid, 4, accu); //window				
			}
			if (Settings.getInstance().isX()) {
				getHints(grid, 5, accu); //Main diagonal
				getHints(grid, 6, accu); //Anti diagonal
			}
			if (Settings.getInstance().isGirandola())
				getHints(grid, 7, accu); //Girandola			
			if (Settings.getInstance().isAsterisk())
				getHints(grid, 8, accu); //Asterisk			
			if (Settings.getInstance().isCD())
				getHints(grid, 9, accu); //CD			
    }

    /**
     * Given a region with 2 - 6 cells with a potential value
	 * locked candidates > 4 rarely result in eliminations ouside
	 * region so limited to 6
     * The potential value is locked in that region
     * Any cell with that value outside the region that sees all
     * the region cells with the potential value are targets
     * The technique subsumes locked candidates (intersection, pointing, claiming)
     * designed to handle variant regions interactions.
     * Useless in Vanilla or Latin square only	 
     */
    private void getHints(Grid grid, int regionType1Index,
            HintsAccumulator accu) throws InterruptedException {
        // Iterate on values
        for (int value = 1; value <= 9; value++) {
	        // Iterate on pairs of parts
			int regionsNumber = Grid.getRegions(regionType1Index).length;
	        for (int i1 = 0; i1 < regionsNumber; i1++) {
	            Grid.Region region1 = Grid.getRegions(regionType1Index)[i1];
                // Get the potential positions of the value in part1
            	BitSet potentialPositions = region1.getPotentialPositions(grid, value);
                // Note: if cardinality == 1, this is Hidden Single in part1 & if cardinality = 9 then it is definitely not useful
                int positionsCardinality = potentialPositions.cardinality();
				if (positionsCardinality < 2 || positionsCardinality > 6) continue;
				int setPosition = 0;
				Cell[] regionCells = new Cell[positionsCardinality];
				for(int i = potentialPositions.nextSetBit(0); i >= 0; i = potentialPositions.nextSetBit(i + 1))
					regionCells[setPosition++] = region1.getCell(i);
				// Potential solution found
				IndirectHint hint = createVLockingHint(grid, region1, regionCells, value);
				if (hint.isWorth())
					accu.add(hint);
	        }
        } // for each value
    }

    private IndirectHint createVLockingHint(Grid grid, Grid.Region p1, Cell[] hcell, int value) {
        // Build removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
        CellSet victims = new CellSet (hcell[0].getVisibleCells());
		for (int i = 1; i < hcell.length; i++) {
			victims.retainAll(hcell[i].getVisibleCells());
			victims.remove(hcell[i]);
		}
        int eliminationsTotal = 0;
		for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value)) {
					eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(value));
            }
        }
        // Build hint
        return new VLockingHint(this, hcell, value,
                    removablePotentials, p1, eliminationsTotal);
    }
    @Override
    public String toString() {
        return "Generalized Intersections";
    }

}
