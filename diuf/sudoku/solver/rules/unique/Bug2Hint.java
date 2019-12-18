/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.unique;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


public class Bug2Hint extends BugHint implements Rule {

    private final Cell[] bugCells;
    private final int bugValue;


    public Bug2Hint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell[] bugCells, int bugValue) {
        super(rule, removablePotentials);
        this.bugCells = bugCells;
        this.bugValue = bugValue;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        for (Cell cell : bugCells)
            result.put(cell, SingletonBitSet.create(bugValue));
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        return super.getRemovablePotentials();
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return bugCells;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public double getDifficulty() {
        return 5.7;
    }

    public String getName() {
        return "BUG type 2";
    }

    public String getShortName() {
        return "BUG2";
    }

    @Override
    public String toString() {
        return "BUG type 2: " + Cell.toString(bugCells) + " on " + bugValue;
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
        String result = HtmlLoader.loadHtml(this, "BivalueUniversalGrave2.html");
        String andBugCells = ValuesFormatter.formatCells(bugCells, " and ");
        return HtmlLoader.format(result, bugValue, andBugCells, sharedRegions());
    }

}
