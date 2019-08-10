/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.unique;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.tools.*;


public class UniqueLoopType1Hint extends UniqueLoopHint {

    private final Cell target;


    public UniqueLoopType1Hint(UniqueLoops rule, List<Cell> loop, int v1, int v2,
            Map<Cell, BitSet> removablePotentials, Cell target) {
        super(rule, loop, v1, v2, removablePotentials);
        this.target = target;
    }


    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        BitSet removable = new BitSet(10);
        removable.set(v1);
        removable.set(v2);
        return Collections.singletonMap(target, removable);
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "UniqueLoopType1.html");
        String type = getTypeName();
        String cellName = target.toString();
        Cell[] cells = new Cell[loop.size()];
        loop.toArray(cells);
        String allCells = Cell.toString(cells);
        result = HtmlLoader.format(result, type, v1, v2, allCells, cellName);
        return result;
    }

}
