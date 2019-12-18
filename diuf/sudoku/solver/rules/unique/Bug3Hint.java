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


public class Bug3Hint extends BugHint implements Rule {

    private final Cell[] bugCells;
    private final Cell[] nakedCells;
    private final Map<Cell, BitSet> extraValues;
    private final BitSet allExtraValues;
    private final BitSet nakedSet;
    private final Grid.Region region;


    public Bug3Hint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell[] bugCells, Cell[] nakedCells, Map<Cell, BitSet> extraValues,
            BitSet allExtraValues, BitSet nakedSet, Grid.Region region) {
        super(rule, removablePotentials);
        this.bugCells = bugCells;
        this.extraValues = extraValues;
        this.allExtraValues = allExtraValues;
        this.nakedCells = nakedCells;
        this.nakedSet = nakedSet;
        this.region = region;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return bugCells;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        for (Cell cell : bugCells) {
            BitSet innerNaked = (BitSet)nakedSet.clone();
            innerNaked.and(extraValues.get(cell));
            result.put(cell, innerNaked); // green
        }
        BitSet outerNaked = (BitSet)nakedSet.clone();
        outerNaked.and(allExtraValues);
        for (Cell cell : nakedCells)
            result.put(cell, nakedSet); // orange
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>(super.getRemovablePotentials());
        for (Cell cell : nakedCells)
            result.put(cell, nakedSet); // orange
        return result;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        return null;
    }

    public String getName() {
        return "BUG type 3";
    }

    public String getShortName() {
        return "BUG3";
    }

    public double getDifficulty() {
        double toAdd = (nakedSet.cardinality() - 1) * 0.1; // Pair=0.1, Quad=0.3
        return 5.7 + toAdd;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {this.region};
    }

    @Override
    public String toString() {
        return "BUG type 3: " + Cell.toString(bugCells) + " on "
        + ValuesFormatter.formatValues(nakedSet, ", ");
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
        String result = HtmlLoader.loadHtml(this, "BivalueUniversalGrave3.html");
        String andExtraValues = ValuesFormatter.formatValues(allExtraValues, " and ");
        String andBugCells = ValuesFormatter.formatCells(bugCells, " and ");
        String orBugCells = ValuesFormatter.formatCells(bugCells, " or ");
        String orExtraValues = ValuesFormatter.formatValues(allExtraValues, " or ");
        final String[] setNames = new String[] {"Pair", "Triplet", "Quad", "Set (5)",
                "Set (6)", "Set (7)"};
        String setName = setNames[nakedSet.cardinality() - 2];
        String andOtherCells = ValuesFormatter.formatCells(nakedCells, " and ");
        String andNakedValues = ValuesFormatter.formatValues(nakedSet, " and ");
        String regionName = region.toString();
        return HtmlLoader.format(result, andExtraValues, andBugCells, orBugCells,
                orExtraValues, setName, andOtherCells, andNakedValues, regionName, sharedRegions());
    }

}
