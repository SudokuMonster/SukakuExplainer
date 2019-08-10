/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver;

import java.util.*;

import diuf.sudoku.*;

/**
 * A hint that is not really a hint for solving a sudoku, but rather
 * to give an information on the sudoku, such as the fact that the sudoku
 * is not valid.
 */
public abstract class WarningHint extends IndirectHint {

    public WarningHint(WarningHintProducer rule) {
        super(rule, new HashMap<Cell,BitSet>());
    }

    @Override
    public void apply() {
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        return Collections.emptyMap();
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        return Collections.emptyMap();
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return null;
    }

    public Collection<Cell> getRedCells() {
        return Collections.emptyList();
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public boolean isWorth() {
        return true;
    }

}
