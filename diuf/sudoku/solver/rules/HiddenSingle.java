/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;


/**
 * Implementation of the Hidden Single solving technique.
 */
public class HiddenSingle implements DirectHintProducer {

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        // First alone cells (last empty cell in a region)
        if (Settings.getInstance().isBlocks())
			getHints(grid, 0, accu, true); //block
        getHints(grid, 2, accu, true); //column
        getHints(grid, 1, accu, true); //row
//SudokuMonster: Variants changes
		if (!Settings.getInstance().isVLatin()) {
			if (Settings.getInstance().isDG())
				getHints(grid, 3, accu, true); //DG
			if (Settings.getInstance().isWindows())
				getHints(grid, 4, accu, true); //Windows
			if (Settings.getInstance().isX()) {
				getHints(grid, 5, accu, true); //Main diagonal
				getHints(grid, 6, accu, true); //Anti diagonal
			}
			if (Settings.getInstance().isGirandola())
				getHints(grid, 7, accu, true); //Girandola
			if (Settings.getInstance().isAsterisk())
				getHints(grid, 8, accu, true); //Asterisk
			if (Settings.getInstance().isCD())
				getHints(grid, 9, accu, true); //Center Dot
		}
        // Then hidden cells
        if (Settings.getInstance().isBlocks())
			getHints(grid, 0, accu, false); //block
        getHints(grid, 2, accu, false); //column
        getHints(grid, 1, accu, false); //row
		if (!Settings.getInstance().isVLatin()) {
			if (Settings.getInstance().isDG())
				getHints(grid, 3, accu, false); //DG
			if (Settings.getInstance().isWindows())
				getHints(grid, 4, accu, false); //Windows
			if (Settings.getInstance().isX()) {
				getHints(grid, 5, accu, false); //Main diagonal
				getHints(grid, 6, accu, false); //Anti diagonal
			}
			if (Settings.getInstance().isGirandola())
				getHints(grid, 7, accu, false); //Girandola
			if (Settings.getInstance().isAsterisk())
				getHints(grid, 8, accu, false); //Asterisk
			if (Settings.getInstance().isCD())
				getHints(grid, 9, accu, false); //Center Dot
		}
    }

    /**
     * For each parts of the given type, check if a value has only one
     * possible potential position.
     * @param regionTypeIndex the type of the parts to check
     */  
    private void getHints(Grid grid, int regionTypeIndex,
            HintsAccumulator accu, boolean aloneOnly) throws InterruptedException {
        Grid.Region[] regions = Grid.getRegions(regionTypeIndex);
        // Iterate on parts
        for (Grid.Region region : regions) {
            // Iterate on values
            for (int value = 1; value <= 9; value++) {
                // Get value's potential position
                BitSet potentialIndexes = region.getPotentialPositions(grid, value);
                if (potentialIndexes.cardinality() == 1) {
                    // One potential position -> solution found
                    int uniqueIndex = potentialIndexes.nextSetBit(0);
                    Cell cell = region.getCell(uniqueIndex);
                    boolean isAlone = region.getEmptyCellCount(grid) == 1;
                    if (isAlone == aloneOnly)
                        accu.add(new HiddenSingleHint(this, region, cell, value, isAlone));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Hidden Singles";
    }

}
