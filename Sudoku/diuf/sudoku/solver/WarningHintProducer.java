/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

import diuf.sudoku.*;

/**
 * Interface for techniques that are able to produce warnings and informations.
 * Typically implemented by classes that check the validity of a sudoku.
 */
public interface WarningHintProducer extends IndirectHintProducer {

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException;

    public String toString();

}
