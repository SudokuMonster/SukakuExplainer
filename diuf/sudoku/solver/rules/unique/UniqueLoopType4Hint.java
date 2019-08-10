/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.unique;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.tools.*;


public class UniqueLoopType4Hint extends UniqueLoopHint {

    private final Cell c1;
    private final Cell c2;
    private final int lockValue;
    private final int remValue;
    private final Grid.Region region;


    public UniqueLoopType4Hint(UniqueLoops rule, List<Cell> loop, int lockValue,
            int remValue, Map<Cell, BitSet> removablePotentials, Cell c1, Cell c2,
            Grid.Region region) {
        super(rule, loop, lockValue, remValue, removablePotentials);
        this.c1 = c1;
        this.c2 = c2;
        this.lockValue = lockValue;
        this.remValue = remValue;
        this.region = region;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        return super.getRemovablePotentials();
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {region};
    }

    @Override
    public int getType() {
        return 4;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "UniqueLoopType4.html");
        String type = getTypeName();
        Cell[] cells = new Cell[loop.size()];
        loop.toArray(cells);
        String allCells = Cell.toString(cells);
        String cell1 = c1.toString();
        String cell2 = c2.toString();
        result = HtmlLoader.format(result, type, lockValue, remValue, allCells, cell1,
                cell2, region.toString());
        return result;
    }

}
