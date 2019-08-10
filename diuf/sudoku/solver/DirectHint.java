/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

import diuf.sudoku.*;

/**
 * Abstract class for hints that allow the direct placement of a value
 * in a cell of the sudoku grid.
 */
public abstract class DirectHint extends Hint {

    private DirectHintProducer rule; // The rule that produced this hint
    private Grid.Region region; // The concerned region, if any
    private Cell cell; // The cell that can be filled
    private int value; // The value that can be put in the cell


    /**
     * Create a new hint
     * @param rule the rule that discovered the hint
     * @param region the region for which the hint is applicable,
     * or <tt>null</tt> if irrelevent
     * @param cell the cell in which a value can be placed
     * @param value the value that can be placed in the cell
     */
    public DirectHint(DirectHintProducer rule, Grid.Region region, Cell cell, int value) {
        this.rule = rule;
        this.region = region;
        this.cell = cell;
        this.value = value;
    }

    @Override
    public DirectHintProducer getRule() {
        return this.rule;
    }

    protected Grid.Region getRegion() {
        return this.region;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {this.region};
    }

    @Override
    public Cell getCell() {
        return this.cell;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public void apply() {
        cell.setValueAndCancel(value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectHint))
            return false;
        DirectHint other = (DirectHint)o;
        return this.cell.equals(other.cell) && this.rule.equals(other.rule) &&
        this.value == other.value;
    }

    @Override
    public int hashCode() {
        return this.cell.hashCode() ^ this.rule.hashCode() ^ this.value;
    }

    @Override
    public String toString() {
        String result = cell.toString() + ": " + value;
        if (region != null)
            result += " in " + region.toString();
        return result;
    }

}
