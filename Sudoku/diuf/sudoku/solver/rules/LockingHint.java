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

/**
 * Locking hint (Pointing, Claiming, X-Wing, Swordfish or Jellyfish)
 */
public class LockingHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final Cell[] cells;
    private final int value;
    private final Map<Cell, BitSet> highlightPotentials;
    private final Grid.Region[] regions;

    public LockingHint(IndirectHintProducer rule, Cell[] cells,
            int value, Map<Cell, BitSet> highlightPotentials,
            Map<Cell, BitSet> removePotentials, Grid.Region... regions) {
        super(rule, removePotentials);
        this.cells = cells;
        this.value = value;
        this.highlightPotentials = highlightPotentials;
        this.regions = regions;
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
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        return highlightPotentials;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        return super.getRemovablePotentials();
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
        int degree = regions.length / 2;
        if (degree == 1) {
            if (regions[0] instanceof Grid.Block)
                return 2.6; // Pointing
            else
                return 2.8; // Claiming
        } else if (degree == 2)
            return 3.2; // X-Wing
        else if (degree == 3)
            return 3.8; // Swordfish
        else
            return 5.2; // Jellyfish
    }

    public String getName() {
        int degree = regions.length / 2;
        if (degree == 1) {
            if (regions[0] instanceof Grid.Block)
                return "Pointing";
            else
                return "Claiming";
        } else if (degree == 2) {
            if (regions[0] instanceof Grid.Block || regions[1] instanceof Grid.Block)
                return "Block X-Wing";
            else
                return "X-Wing";
        } else if (degree == 3) {
            return "Swordfish";
        } else if (degree == 4) {
            return "Jellyfish";
        }
        return null;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        // Add any potential of first region that are not in second region
        for (int i = 0; i < regions.length; i+= 2) {
            for (int pos1 = 0; pos1 < 9; pos1++) {
                Cell cell = regions[i].getCell(pos1);
                Cell initCell = initialGrid.getCell(cell.getX(), cell.getY());
                if (initCell.hasPotentialValue(value) && !cell.hasPotentialValue(value)) {
                    boolean isInRegion2 = false;
                    for (int j = 1; j < regions.length; j+= 2) {
                        for (int pos2 = 0; pos2 < 9; pos2++) {
                            Cell other = regions[j].getCell(pos2);
                            if (other.equals(cell))
                                isInRegion2 = true;
                        }
                    }
                    if (!isInRegion2)
                        result.add(new Potential(cell, value, false));
                }
            }
        }
        return result;
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
        if (regions != null) {
            if (regions.length == 2) {
                builder.append(" in ");
                builder.append(regions[0].toString());
                builder.append(" and ");
                builder.append(regions[1].toString());
            } else if (regions.length >= 4 && regions.length % 2 == 0) {
                builder.append(" in " + (regions.length / 2) + " ");
                builder.append(regions[0].toString());
                builder.append("s and " + (regions.length / 2) + " ");
                builder.append(regions[1].toString());
                builder.append("s");
            }
        }
        return builder.toString();
    }

    private String toHtml1() {
        String result = HtmlLoader.loadHtml(this, "SimpleLockingHint.html");
        String valueName = Integer.toString(value);
        String firstRegion = regions[0].toString();
        String secondRegion = regions[1].toString();
        String ruleName = getName();
        return HtmlLoader.format(result, valueName, firstRegion, secondRegion, ruleName);
    }

    @Override
    public String toHtml() {
        int degree = regions.length / 2;
        if (degree == 1)
            return toHtml1();
        final String[] numberNames = new String[] {"two", "three", "four", "five", "six",
                "seven", "eight"};
        String result = HtmlLoader.loadHtml(this, "LockingHint.html");
        String ruleName = getName();
        String valueName = Integer.toString(value);
        String degreeName = numberNames[degree - 2];
        String firstRegion = regions[0].toString();
        String secondRegion = regions[1].toString();
        return HtmlLoader.format(result, ruleName, valueName, degreeName, firstRegion,
                secondRegion);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LockingHint))
            return false;
        LockingHint other = (LockingHint)o;
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
