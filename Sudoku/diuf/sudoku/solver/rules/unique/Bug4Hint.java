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


public class Bug4Hint extends BugHint implements Rule {

    private final Cell bugCell1;
    private final Cell bugCell2;
    private final Map<Cell, BitSet> extraValues;
    private final BitSet allExtraValues;
    private final int value; // removable value appearing on both cells
    private final Grid.Region region;


    public Bug4Hint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell bugCell1, Cell bugCell2, Map<Cell, BitSet> extraValues,
            BitSet allExtraValues, int value, Grid.Region region) {
        super(rule, removablePotentials);
        this.bugCell1 = bugCell1;
        this.bugCell2 = bugCell2;
        this.extraValues = extraValues;
        this.allExtraValues = allExtraValues;
        this.value = value;
        this.region = region;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {bugCell1, bugCell2};
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        BitSet b1 = (BitSet)extraValues.get(bugCell1).clone();
        b1.set(value); // orange
        result.put(bugCell1, b1);
        BitSet b2 = (BitSet)extraValues.get(bugCell2).clone();
        b2.set(value); // orange
        result.put(bugCell2, b2);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> removable = super.getRemovablePotentials();
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        for (Cell cell : removable.keySet()) {
            BitSet values = (BitSet)removable.get(cell).clone();
            values.set(value); // orange
            result.put(cell, values);
        }
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public Region[] getRegions() {
        return new Region[] {this.region};
    }

    public String getName() {
        return "BUG type 4";
    }

    public double getDifficulty() {
        return 5.7;
    }

    @Override
    public String toString() {
        return "BUG type 4: " + bugCell1.toString() + "," + bugCell2.toString() + " on " + value;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "BivalueUniversalGrave4.html");
        String bugValuesAnd = ValuesFormatter.formatValues(allExtraValues, " and ");
        String bugCellsAnd = ValuesFormatter.formatCells(new Cell[] {bugCell1, bugCell2}, " and ");
        String bugCellsOr = ValuesFormatter.formatCells(new Cell[] {bugCell1, bugCell2}, " or ");
        String bugValuesOr = ValuesFormatter.formatValues(allExtraValues, " or ");
        String lockedValue = Integer.toString(value);
        String regionName = region.toString();
        BitSet removable = new BitSet();
        for (BitSet r : getRemovablePotentials().values())
            removable.or(r);
        String removableValues = ValuesFormatter.formatValues(removable, " and ");
        return HtmlLoader.format(result, bugValuesAnd, bugCellsAnd, bugCellsOr, bugValuesOr,
                lockedValue, regionName, removableValues);
    }

}
