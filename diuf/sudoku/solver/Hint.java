/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

import diuf.sudoku.*;

/**
 * Abstract class for a hint.
 * <p>
 * A hint is (usually) capable of advancing one step in the solving
 * process of a sudoku. Warnings and info messages are also implemented
 * as hints, see {@link WarningHint} subclass.
 * @see DirectHint
 * @see IndirectHint
 */
public abstract class Hint {

    /**
     * Get the solving technique that discovered this hint
     * @return the solving technique that discovered this hint
     */
    public abstract HintProducer getRule();

    /**
     * Get the cell that can be filled, if any,
     * by applying this hint
     * @return the cell that can be filled
     */
    public Cell getCell() {
        return null;
    }

    /**
     * Get the value that can be placed in the cell,
     * if any.
     * @return the value that can be placed in the cell
     * @see #getCell()
     */
    public int getValue() {
        return 0;
    }

    /**
     * Apply this hint on the current sudoku grid.
     */
    public abstract void apply();

    /**
     * Get the regions concerned by this hint.
     * <tt>null</tt> can be returned if this hint does
     * not depend on regions.
     * @return the regions concerned by this hint
     */
    public abstract Grid.Region[] getRegions();

    /**
     * Get a string representation of this hint.
     * <p>
     * The string returned by this method is displayed in the hint tree.
     * @return a string representation of this hint
     */
    @Override
    public abstract String toString();

    /**
     * Get an HTML explanation of this hint, understandable by human beings.
     * @return an HTML explanation of this hint
     */
    public abstract String toHtml();

}
