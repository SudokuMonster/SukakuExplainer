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
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
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

	private String sharedRegions(){
		if (Settings.getInstance().isVanilla())
			return "row, column or block";
		else {
			String res[] = new String[10];
			int i = 0;
			String finalRes = "row";
			if (Settings.getInstance().isVLatin())
				return "row or column";
			else
				res[i++]= "column";
			if (Settings.getInstance().isBlocks())
				res[i++]= "block";
			if (Settings.getInstance().isDG())
				res[i++]= "disjoint group";
			if (Settings.getInstance().isWindows())
				res[i++]= "window group";
			if (Settings.getInstance().isX())
				res[i++]= "diagonal";
			if (Settings.getInstance().isGirandola())
				res[i++]= "girandola group";
			if (Settings.getInstance().isAsterisk())
				res[i++]= "asterisk group";
			if (Settings.getInstance().isCD())
				res[i++]= "center dot group";
			i--;
			for (int j = 0; j < i; j++)
				finalRes += ", " + res[j];
			finalRes += " or " + res[i];
			return finalRes;
		}
	}

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, "UniqueLoopType4.html");
        String type = getTypeName();
        Cell[] cells = new Cell[loop.size()];
        loop.toArray(cells);
        String allCells = Cell.toString(cells);
        String cell1 = c1.toString();
        String cell2 = c2.toString();
        result = HtmlLoader.format(result, type, lockValue, remValue, allCells, cell1,
                cell2, region.toString(), sharedRegions());
        return result;
    }

}
