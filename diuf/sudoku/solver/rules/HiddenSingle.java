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
        getHints(grid, 0, accu, true); //block
        getHints(grid, 2, accu, true); //column
        getHints(grid, 1, accu, true); //row
        // Then hidden cells
        getHints(grid, 0, accu, false); //block
        getHints(grid, 2, accu, false); //column
        getHints(grid, 1, accu, false); //row
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
