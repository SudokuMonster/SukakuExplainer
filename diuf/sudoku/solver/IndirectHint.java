/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

import java.util.*;

import diuf.sudoku.*;

/**
 * Abstract class for hints that do not directly allow the placement
 * of a value in a cell, but allow the removal of one or more potential
 * values of one or more cells of the sudoku grid.
 */
public abstract class IndirectHint extends Hint {

    private final IndirectHintProducer rule;
    private final Map<Cell,BitSet> removablePotentials;


    /**
     * Create a new indirect hint.
     * @param rule the rule that discovered the hint
     * @param removablePotentials the potential values that can be removed
     * in cells of the sudoku grid by applying this hint.
     */
    protected IndirectHint(IndirectHintProducer rule,
            Map<Cell, BitSet> removablePotentials) {
        this.rule = rule;
        this.removablePotentials = removablePotentials;
    }

    /**
     * Get the rule that discovered this hint
     */
    @Override
    public IndirectHintProducer getRule() {
        return this.rule;
    }

    /**
     * Get the potential values that can be removed from cells of the sudoku
     * grid by applying this hint. The keys are cells of the grid, and the
     * values are the bit set of values that can be removed from the corresponding
     * cell. Note that the bit sets can only contain values ranging between 1 and 9.
     * @return the potential values that can be removed from cells of the sudoku
     * grid by applying this hint
     */
    public Map<Cell,BitSet> getRemovablePotentials() {
        return this.removablePotentials;
    }

    /**
     * Test is this hint is worth, that is, if it really allows one to remove
     * at least one potential value from a cell of the grid.
     * <p>
     * This method is used, because it is frequent that a solving pattern is
     * dicovered, but does not allow any progress in the current grid. But this
     * fact is hard to discover before the pattern itself.
     * @return whether this hint allows some progress in the solving process
     */
    public boolean isWorth() {
        return !removablePotentials.isEmpty();
    }

    /**
     * Apply this hint on the grid (passed to the constructor).
     */
    @Override
    public void apply() {
        for (Cell cell : removablePotentials.keySet()) {
            BitSet cellRemovablePotentials = removablePotentials.get(cell);
            cell.removePotentialValues(cellRemovablePotentials);
        }
        if (getCell() != null)
            getCell().setValueAndCancel(getValue());
    }

//  Visual representation

    /**
     * Get the number of views that are required to make a visual
     * representation of this hint.
     */
    public abstract int getViewCount();

    /**
     * Get the cells that must be highlighted in a visual representation
     * of this hint.
     * @return the cells that must be highlighted in a visual representation, or
     * <tt>null</tt> if none
     * 
     */
    public abstract Cell[] getSelectedCells();

    /**
     * Get the cell potential values that must be highlighted in green
     * (or in a positive-sounding color) in a visual representation of this
     * hint.
     * <p>
     * Note that potential values that must be highlighted both in green
     * and in red (according to {@link #getRedPotentials(int)}, will be
     * highlighted in orange.
     * @param viewNum the view number, zero-based.
     * @return the cell potential values to highlight in green, or
     * <tt>null</tt> if none
     * @see #getViewCount()
     */
    public abstract Map<Cell, BitSet> getGreenPotentials(int viewNum);

    /**
     * Get the cell potential values that must be highlighted in red
     * (or in a negative-sounding color) in a visual representation of this
     * hint.
     * @param viewNum the view number, zero-based
     * @return the cell potential values to highlight in red, or
     * <tt>null</tt> if none
     * @see #getViewCount()
     */
    public abstract Map<Cell, BitSet> getRedPotentials(int viewNum);

    public Map<Cell, BitSet> getBluePotentials(Grid grid, int viewNum) {
        return Collections.emptyMap();
    }

    /**
     * Get the links to draw in a visual representation of this hint.
     * @param viewNum the vien number, zero-based
     * @return the links to draw, or <tt>null</tt> if none.
     * @see #getViewCount()
     * @see Link
     */
    public abstract Collection<Link> getLinks(int viewNum);

}
