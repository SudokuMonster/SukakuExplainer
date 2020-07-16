/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


public class NakedSetGenHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final Cell[] cells;
    private final int[] values;
    private final Map<Cell, BitSet> highlightPotentials;
    private final Grid.Region region;

    
    public NakedSetGenHint(IndirectHintProducer rule, Cell[] cells,
            int[] values, Map<Cell, BitSet> highlightPotentials,
            Map<Cell, BitSet> removePotentials, Grid.Region region) {
        super(rule, removePotentials);
        this.cells = cells;
        this.values = values;
        this.highlightPotentials = highlightPotentials;
        this.region = region;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return cells;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        return highlightPotentials;
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
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {this.region};
    }

    public double getDifficulty() {
        int degree = values.length;
        if (degree == 2)
            return 3.0;
        else if (degree == 3)
            return 3.6;
        else if (degree == 4)
            return 5.0;
        else if (degree == 5)
            return 5.6;	//Genralized Naked Quintuple	
		return 6.5;//Genralized Naked Sextuple
    }

    public String getName() {
        final String[] groupNames = new String[] {"Pair", "Triplet", "Quad", "Quintuplet", "Sextuplet"};
		if (values.length < 7)
			return "Generalized Naked " + groupNames[values.length - 2];
		return "Generalized Naked Sets " + (values.length - 2);
    }

    public String getShortName() {
        final String[] groupNames = new String[] {"P", "T", "Q", "5", "6"};
        return "gN" + groupNames[values.length - 2];
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        BitSet myValues = new BitSet(10);
        for (int i = 0; i < values.length; i++)
            myValues.set(values[i]);
        for (Cell cell : this.cells) {
            Cell initialCell = Grid.getCell(cell.getX(), cell.getY());
            for (int value = 1; value <= 9; value++) {
                //if (initialCell.hasPotentialValue(value) && !myValues.get(value))
                if (initialGrid.hasCellPotentialValue(initialCell.getIndex(), value) && !myValues.get(value))
                    // This potential must go off before I can be applied
                    result.add(new Potential(cell, value, false));
            }
        }
        return result;
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " in the <b1>" + getRegions()[0].toFullString() + "</b1>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append(": ");
        if (cells.length <= 6)
            builder.append(Cell.toFullString(this.cells));
        else
            builder.append("Cells [...]");
        builder.append(": ");
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                builder.append(",");
            builder.append(Integer.toString(values[i]));
        }
        builder.append(" in ");
        builder.append(region.toString());
        return builder.toString();
    }

    @Override
    public String toHtml(Grid grid) {
        final String[] numberNames = new String[] {"two", "three", "four"};
        String result = HtmlLoader.loadHtml(this, "NakedSetGenHint.html");
        String counter = numberNames[values.length - 2];
        String cellList = HtmlLoader.formatList(cells);
        String valueList = HtmlLoader.formatValues(values);
        String regionName = region.toString();
        String ruleName = getName();
        return HtmlLoader.format(result, counter, cellList, valueList, regionName,
                ruleName);
    }

}
