/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

import diuf.sudoku.*;

/**
 * Interface for solving techniques that are able to produce hints.
 * @see diuf.sudoku.solver.Hint
 */
public interface HintProducer {

    /**
     * Get all the hints that applicable of the given grid according to
     * this solving technique.
     * @param grid the sudoku grid
     * @param accu the accumulator in which to add hints
     * @throws InterruptedException if the search for hints has been interrupted.
     * This exception might be thrown by the accumulator and you must not try
     * to catch it.
     */
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException;

}
