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
 * Implementation of the Naked Single solving techniques.
 */
public class NakedSingle implements DirectHintProducer {

    /**
     * Check if a cell has only one potential value, and accumulate
     * corresponding hints
     */
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
//        Grid.Region[] parts = grid.getRegions(Grid.Row.class);
//        // Iterate on parts
//        for (Grid.Region part : parts) {
//            // Iterate on cells
//            for (int index = 0; index < 9; index++) {
//                Cell cell = part.getCell(index);
//                // Get the cell's potential values
//                BitSet potentialValues = cell.getPotentialValues();
//                if (potentialValues.cardinality() == 1) {
//                    // One potential value -> solution found
//                    int uniqueValue = potentialValues.nextSetBit(0);
//                    accu.add(new NakedSingleHint(this, null, cell, uniqueValue));
//                }
//            }
//        }
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                Cell cell = grid.getCell(x, y);
                //if(cell.getValue() != 0) continue;
                if(grid.getCellValue(x, y) != 0) continue;
                //BitSet potentialValues = cell.getPotentialValues();
                BitSet potentialValues = grid.getCellPotentialValues(cell);
                if (potentialValues.cardinality() == 1) {
                    // One potential value -> solution found
                    int uniqueValue = potentialValues.nextSetBit(0);
                    accu.add(new NakedSingleHint(this, null, cell, uniqueValue));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Naked Singles";
    }

}
