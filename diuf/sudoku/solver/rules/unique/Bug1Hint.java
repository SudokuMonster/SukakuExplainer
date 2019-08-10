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
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        result.put(bugCell, bugValues);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
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

    @Override
    public String toString() {
        return "BUG type 1: " + bugCell.toString();
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "BivalueUniversalGrave1.html");
        String andExtra = ValuesFormatter.formatValues(bugValues, " and ");
        String orExtra = ValuesFormatter.formatValues(bugValues, " or ");
        BitSet removable = super.getRemovablePotentials().get(bugCell);
        String remList = ValuesFormatter.formatValues(removable, " and ");
        result = HtmlLoader.format(result, andExtra, bugCell, orExtra, remList);
        return result;
    }

}
