/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.tools;

import java.util.*;

import diuf.sudoku.*;

/**
 * Formatter for arrays and bitsets.
 */
public class ValuesFormatter {

    public static String formatValues(int[] values, String lastSep) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0 && i == values.length - 1)
                result.append(lastSep);
            else if (i > 0)
                result.append(", ");
            result.append(values[i]);
        }
        return result.toString();
    }

    public static String formatValues(BitSet values, String lastSep) {
        int[] array = new int[values.cardinality()];
        int index = 0;
        for (int v = values.nextSetBit(0); v >= 0; v = values.nextSetBit(v + 1))
            array[index++] = v;
        return formatValues(array, lastSep);
    }

    public static String formatCells(Cell[] cells, String lastSep) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0 && i == cells.length - 1)
                result.append(lastSep);
            else if (i > 0)
                result.append(", ");
            result.append(cells[i].toString());
        }
        return result.toString();
    }

}
