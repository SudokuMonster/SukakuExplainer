/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.unique;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


public class Bug1Hint extends BugHint implements Rule {

    protected final Cell bugCell;
    protected final BitSet bugValues;

    public Bug1Hint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell bugCell, BitSet bugValues) {
        super(rule, removablePotentials);
        this.bugCell = bugCell;
        this.bugValues = bugValues;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        return null;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        result.put(bugCell, bugValues);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        return super.getRemovablePotentials();
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {bugCell};
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    public double getDifficulty() {
        return 5.6;
    }

    public String getName() {
        return "BUG type 1";
    }

    public String getShortName() {
        return "BUG1";
    }

    @Override
    public String toString() {
        return "BUG type 1: " + bugCell.toString();
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
        String result = HtmlLoader.loadHtml(this, "BivalueUniversalGrave1.html");
        String andExtra = ValuesFormatter.formatValues(bugValues, " and ");
        String orExtra = ValuesFormatter.formatValues(bugValues, " or ");
        BitSet removable = super.getRemovablePotentials().get(bugCell);
        String remList = ValuesFormatter.formatValues(removable, " and ");
        result = HtmlLoader.format(result, andExtra, bugCell, orExtra, remList, sharedRegions());
        return result;
    }

}
