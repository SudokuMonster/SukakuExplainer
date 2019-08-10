/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

/**
 * Interface for rules that are able to produce direct hints.
 * @see diuf.sudoku.solver.DirectHint
 */
public interface DirectHintProducer extends HintProducer {

    public String toString();

}
