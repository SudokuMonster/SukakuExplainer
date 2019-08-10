/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

/**
 * Interface for rules that are able to produce
 * indirect hints.
 * @see diuf.sudoku.solver.IndirectHint
 */
public interface IndirectHintProducer extends HintProducer {

    public String toString();

}
