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
        getHints(grid, Grid.Block.class, accu, true);
        getHints(grid, Grid.Column.class, accu, true);
        getHints(grid, Grid.Row.class, accu, true);
        // Then hidden cells
        getHints(grid, Grid.Block.class, accu, false);
        getHints(grid, Grid.Column.class, accu, false);
        getHints(grid, Grid.Row.class, accu, false);
    }

    /**
     * For each parts of the given type, check if a value has only one
     * possible potential position.
     * @param regionType the type of the parts to check
     */  
    private <T extends Grid.Region> void getHints(Grid grid, Class<T> regionType,
            HintsAccumulator accu, boolean aloneOnly) throws InterruptedException {
        Grid.Region[] regions = grid.getRegions(regionType);
        // Iterate on parts
        for (Grid.Region region : regions) {
            // Iterate on values
            for (int value = 1; value <= 9; value++) {
                // Get value's potential position
                BitSet potentialIndexes = region.getPotentialPositions(value);
                if (potentialIndexes.cardinality() == 1) {
                    // One potential position -> solution found
                    int uniqueIndex = potentialIndexes.nextSetBit(0);
                    Cell cell = region.getCell(uniqueIndex);
                    boolean isAlone = region.getEmptyCellCount() == 1;
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
