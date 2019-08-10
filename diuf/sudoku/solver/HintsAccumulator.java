/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

/**
 * Accumulator for hints.
 * The accumulator can choose to throw an <tt>InterruptedException</tt>
 * whenever it has gathered enough hints.
 */
public interface HintsAccumulator {

    /**
     * Add an hint to this accumulator
     * @param hint the hint to add
     * @throws InterruptedException if this accumulator want to
     * stop the gathering of hints. You must not catch this exception.
     */
    public void add(Hint hint) throws InterruptedException;

}
