/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Direct Hidden Set hint
 */
public class DirectHiddenSetHint extends IndirectHint implements Rule {

    private final Cell[] cells;
    private final int[] values;
    private final Cell cell; // Hidden single cell
    private final int value; // Hidden single value
    private final Map<Cell, BitSet> orangePotentials;
    private final Map<Cell, BitSet> redPotentials;
    private final Grid.Region region;

    public DirectHiddenSetHint(IndirectHintProducer rule, Cell[] cells,
            int[] values, Map<Cell, BitSet> orangePotentials,
            Map<Cell, BitSet> removePotentials, Grid.Region region,
            Cell cell, int value) {
        super(rule, getEmptyMap());
        this.cells = cells;
        this.values = values;
        this.cell = cell;
        this.value = value;
        this.orangePotentials = orangePotentials;
        this.redPotentials = removePotentials;
        this.region = region;
    }

    private static Map<Cell, BitSet> getEmptyMap() {
        return Collections.emptyMap();
    }

    @Override
    public Cell getCell() {
        return cell;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public boolean isWorth() {
        return true;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {cell};
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        result.putAll(orangePotentials);
        result.put(cell, SingletonBitSet.create(value));
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        result.putAll(orangePotentials);
        for (Cell cell : redPotentials.keySet()) {
            BitSet values = redPotentials.get(cell);
            if (result.containsKey(cell)) {
                BitSet nvalues = (BitSet)result.get(cell).clone();
                nvalues.or(values);
                result.put(cell, nvalues);
            } else
                result.put(cell, values);
        }
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {this.region};
    }

    public double getDifficulty() {
        int degree = values.length;
        if (degree == 2)
            return 2.0;
        else if (degree == 3)
            return 2.5;
        else
            return 4.3;
    }

    public String getName() {
        final String[] groupNames = new String[] {"Pair", "Triplet", "Quad"};
        int degree = values.length;
        return "Direct Hidden " + groupNames[degree - 2];
    }

    public String getClueHtml(boolean isBig) {
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
        if (cells.length <= 4)
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
    public String toHtml() {
        final String[] numberNames = new String[] {"two", "three", "four"};
        String result = HtmlLoader.loadHtml(this, "DirectHiddenSetHint.html");
        String counter = numberNames[values.length - 2];
        String cellList = HtmlLoader.formatList(cells);
        String valueList = HtmlLoader.formatValues(values);
        String regionName = region.toString();
        String ruleName = getName();
        String hcellName = cell.toString();
        String hvalueName = Integer.toString(value);
        return HtmlLoader.format(result, counter, cellList, valueList, regionName,
                ruleName, hcellName, hvalueName);
    }

}
