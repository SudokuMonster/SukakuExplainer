/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.unique;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;


public abstract class BugHint extends IndirectHint implements Rule {

    public BugHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials) {
        super(rule, removablePotentials);
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName();
        } else {
            return "Look for a Bivalue Universal Grave (BUG)";
        }
    }

}
