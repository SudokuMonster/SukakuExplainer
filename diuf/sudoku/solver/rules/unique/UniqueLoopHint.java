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


public abstract class UniqueLoopHint extends IndirectHint implements Rule {

    protected final List<Cell> loop;
    protected final int v1;
    protected final int v2;

    public UniqueLoopHint(UniqueLoops rule, List<Cell> loop, int v1, int v2,
            Map<Cell, BitSet> removablePotentials) {
        super(rule, removablePotentials);
        this.loop = loop;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public Cell[] getSelectedCells() {
        Cell[] cells = new Cell[loop.size()];
        loop.toArray(cells);
        return cells;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        for (Cell cell : loop) {
            BitSet commonValues = new BitSet(10);
            commonValues.set(v1);
            commonValues.set(v2);
            result.put(cell, commonValues);
        }
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
        for (int i = 0; i < loop.size(); i++) {
            Cell cell = loop.get(i);
            Cell next = loop.get((i + 1) % loop.size());
            result.add(new Link(cell, 0, next, 0));
        }
        return result;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    protected String getTypeName() {
        if (loop.size() == 4)
            return "Rectangle";
        else
            return "Loop";
    }

    public String getName() {
        if (loop.size() > 6)
            // Include size of the loop
            return "Unique " + getTypeName() + " " + loop.size() + " type " + getType();
        else
            return "Unique " + getTypeName() + " type " + getType();
    }

    public String getGroup() {
        return "Uniqueness tests";
    }

    public abstract int getType();

    public double getDifficulty() {
        double result = 4.5;
        if (loop.size() >= 10)
            result += 0.3;
        if (loop.size() >= 8)
            result += 0.2;
        else if (loop.size() >= 6)
            result += 0.1;
        return result; // 5.5 - 5.8
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
            " on the values <b>" + v1 + "</b> and <b>" + v2 + "</b>";
        } else {
            return "Look for a Unique Rectangle or Loop";
        }
    }

    @Override
    public String toString() {
        Cell[] cells = new Cell[loop.size()];
        loop.toArray(cells);
        return getName() + ": " + Cell.toFullString(cells) + " on " + v1 + ", " + v2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!o.getClass().equals(this.getClass()))
            return false;
        UniqueLoopHint other = (UniqueLoopHint)o;
        if (this.loop.size() != other.loop.size())
            return false;
        return this.loop.containsAll(other.loop);
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Cell cell : loop)
            result ^= cell.hashCode();
        return result;
    }

}
