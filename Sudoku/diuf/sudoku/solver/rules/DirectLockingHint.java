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


public class DirectLockingHint extends IndirectHint implements Rule {

    private final Cell[] cells;
    private final Cell cell;
    private final int value;
    private final Map<Cell, BitSet> redPotentials;
    private final Map<Cell, BitSet> orangePotentials;
    private final Grid.Region[] regions;


    public DirectLockingHint(IndirectHintProducer rule, Cell[] cells, Cell cell,
            int value, Map<Cell, BitSet> highlightPotentials,
            Map<Cell, BitSet> removePotentials, Grid.Region... regions) {
        super(rule, getEmptyMap());
        this.cells = cells;
        this.cell = cell;
        this.value = value;
        this.redPotentials = removePotentials;
        this.orangePotentials = highlightPotentials;
        this.regions = regions;
    }

    private static Map<Cell, BitSet> getEmptyMap() {
        return Collections.emptyMap();
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
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        result.putAll(orangePotentials);
        result.put(cell, SingletonBitSet.create(value));
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        result.putAll(redPotentials);
        result.putAll(orangePotentials);
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public Grid.Region[] getRegions() {
        return this.regions;
    }

    public double getDifficulty() {
        if (regions[0] instanceof Grid.Block)
            return 1.7; // Pointing
        else
            return 1.9; // Claiming
    }

    public String getName() {
        if (regions[0] instanceof Grid.Block)
            return "Direct Pointing";
        else
            return "Direct Claiming";
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the value <b>" + value + "<b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append(": ");
        builder.append(Cell.toFullString(this.cells));
        builder.append(": ");
        builder.append(value);
        builder.append(" of ");
        builder.append(regions[0].toString());
        builder.append(" in ");
        builder.append(regions[1].toString());
        return builder.toString();
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "DirectLockingHint.html");
        String valueName = Integer.toString(value);
        String firstRegion = regions[0].toString();
        String secondRegion = regions[1].toString();
        String ruleName = getName();
        String cellName = cell.toString();
        return HtmlLoader.format(result, valueName, firstRegion, secondRegion, ruleName,
                cellName);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectLockingHint))
            return false;
        DirectLockingHint other = (DirectLockingHint)o;
        if (this.value != other.value)
            return false;
        if (this.cells.length != other.cells.length)
            return false;
        return Arrays.asList(this.cells).containsAll(Arrays.asList(other.cells));
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Cell cell : cells)
            result ^= cell.hashCode();
        result ^= value;
        return result;
    }

}
