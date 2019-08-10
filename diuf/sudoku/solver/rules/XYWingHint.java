/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * XW-Wing and XYZ-Wing hints
 */
public class XYWingHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final boolean isXYZ;
    private final Cell xyCell;
    private final Cell xzCell;
    private final Cell yzCell;
    private final int value;


    public XYWingHint(XYWing rule, Map<Cell,BitSet> removablePotentials,
            boolean isXYZ, Cell xyCell, Cell xzCell, Cell yzCell, int value) {
        super(rule, removablePotentials);
        this.isXYZ = isXYZ;
        this.xyCell = xyCell;
        this.xzCell = xzCell;
        this.yzCell = yzCell;
        this.value = value;
    }

    private int getX() {
        BitSet xyPotentials = xyCell.getPotentialValues();
        int x = xyPotentials.nextSetBit(0);
        if (x == this.value)
            x = xyPotentials.nextSetBit(x + 1);
        return x;
    }

    private int getY() {
        BitSet xyPotentials = xyCell.getPotentialValues();
        int x = getX();
        int y = xyPotentials.nextSetBit(x + 1);
        if (y == this.value)
            y = xyPotentials.nextSetBit(y + 1);
        return y;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        // x and y of XY cell (orange)
        result.put(xyCell, xyCell.getPotentialValues());
        // z value (green)
        BitSet zSet = SingletonBitSet.create(value);
        result.put(xzCell, zSet);
        result.put(yzCell, zSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>(super.getRemovablePotentials());
        // Add x and y of XY cell (orange)
        BitSet xy = new BitSet(10);
        xy.set(getX());
        xy.set(getY());
        result.put(xyCell, xy);
        return result;
    }

    public double getDifficulty() {
        if (isXYZ)
            return 4.4;
        else
            return 4.2;
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getName() {
        if (isXYZ)
            return "XYZ-Wing";
        else
            return "XY-Wing";
    }

    private int getRemainingValue(Cell c) {
        BitSet result = (BitSet)c.getPotentialValues().clone();
        result.clear(value);
        return result.nextSetBit(0);
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
        int xValue = getRemainingValue(xzCell);
        Link xLink = new Link(xyCell, xValue, xzCell, xValue);
        result.add(xLink);
        int yValue = getRemainingValue(yzCell);
        Link yLink = new Link(xyCell, yValue, yzCell, yValue);
        result.add(yLink);
        return result;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {xyCell, xzCell, yzCell};
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        Cell xyCell = initialGrid.getCell(this.xyCell.getX(), this.xyCell.getY());
        Cell xzCell = initialGrid.getCell(this.xzCell.getX(), this.xzCell.getY());
        Cell yzCell = initialGrid.getCell(this.yzCell.getX(), this.yzCell.getY());
        for (int p = 1; p <= 9; p++) {
            if (xyCell.hasPotentialValue(p) && !this.xyCell.hasPotentialValue(p))
                result.add(new Potential(this.xyCell, p, false));
            if (xzCell.hasPotentialValue(p) && !this.xzCell.hasPotentialValue(p))
                result.add(new Potential(this.xzCell, p, false));
            if (yzCell.hasPotentialValue(p) && !this.yzCell.hasPotentialValue(p))
                result.add(new Potential(this.yzCell, p, false));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof XYWingHint))
            return false;
        XYWingHint other = (XYWingHint)o;
        if (this.isXYZ != other.isXYZ)
            return false;
        if (this.xyCell != other.xyCell || this.value != other.value)
            return false;
        if (this.xzCell != other.xzCell && this.xzCell != other.yzCell)
            return false;
        if (this.yzCell != other.xzCell && this.yzCell != other.yzCell)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return xyCell.hashCode() ^ yzCell.hashCode() ^ xzCell.hashCode();
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
            " on the values " + getX() + ", " + getY() + " and <b>" + value + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append(": ");
        builder.append(Cell.toFullString(xyCell, xzCell, yzCell));
        builder.append(" on value ");
        builder.append(value);
        return builder.toString();
    }

    @Override
    public String toHtml() {
        String result;
        if (isXYZ)
            result = HtmlLoader.loadHtml(this, "XYZWingHint.html");
        else
            result = HtmlLoader.loadHtml(this, "XYWingHint.html");
        String cell1 = xyCell.toString();
        String cell2 = xzCell.toString();
        String cell3 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell2, cell3, value, getX(), getY());
        return result;
    }

}
