/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.checks;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;

/**
 * Check for invalid sudoku based on the number of different values
 * that appear at least once.
 * If at least two values never appear, an appropriate warning hint
 * is produced.
 */
public class NumberOfValues implements WarningHintProducer {

    public void getHints(Grid grid, HintsAccumulator accu)
            throws InterruptedException {
        BitSet values = new BitSet(10);
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = grid.getCell(x, y);
                int value = cell.getValue();
                if (value != 0)
                    values.set(value);
            }
        }
        if (values.cardinality() < 8) {
            String missingValues = "";
            for (int v = 1; v <= 9; v++) {
                if (!values.get(v)) {
                    if (!missingValues.equals(""))
                        missingValues += ", ";
                    missingValues += v;
                }
            }
            WarningMessage message = new WarningMessage(this,
                    "Sudoku has multiple solutions",
                    "TooFewValues.html", missingValues);
            accu.add(message);
        }
    }

    @Override
    public String toString() {
        return "Number of different values";
    }

}
