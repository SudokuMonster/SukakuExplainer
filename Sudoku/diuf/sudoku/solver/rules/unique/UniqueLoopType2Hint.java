/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.unique;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.tools.*;


public class UniqueLoopType2Hint extends UniqueLoopHint {

    private final Cell[] cells;
    private final int value;


    public UniqueLoopType2Hint(UniqueLoops rule, List<Cell> loop, int v1, int v2,
            Map<Cell, BitSet> removablePotentials, Cell[] cells, int value) {
        super(rule, loop, v1, v2, removablePotentials);
        this.cells = cells;
        this.value = value;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>(super.getRemovablePotentials());
        for (Cell c : cells)
            result.put(c, SingletonBitSet.create(value)); // orange
        return result;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>(super.getGreenPotentials(viewNum));
        for (Cell c : cells) {
            BitSet b = result.get(c);
            b.set(value); // orange
        }
        return result;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "UniqueLoopType2.html");
        String type = getTypeName();
        Cell[] loopCells = new Cell[loop.size()];
        loop.toArray(loopCells);
        String allCells = Cell.toString(loopCells);
        String extraCellsOr = ValuesFormatter.formatCells(cells, " or ");
        String extraCellsAnd = ValuesFormatter.formatCells(cells, " and ");
        result = HtmlLoader.format(result, type, v1, v2, allCells, extraCellsOr,
                extraCellsAnd, value);
        return result;
    }

}
