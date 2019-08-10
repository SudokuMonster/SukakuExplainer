/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

/**
 * Hints accumulator, that accumulates a single hint (the first that
 * is received) and then stops.
 */
public class SingleHintAccumulator implements HintsAccumulator {

    private Hint result = null;

    public SingleHintAccumulator() {
        super();
    }

    public void add(Hint hint) throws InterruptedException {
        if (!hint.equals(result)) {
            result = hint;
            throw new InterruptedException();
        }
    }

    /**
     * Get the only hint that has been accumulated, or <tt>null</tt> if no
     * hint has been received at all.
     * @return the only hint that has been collected
     */
    public Hint getHint() {
        return result;
    }

}