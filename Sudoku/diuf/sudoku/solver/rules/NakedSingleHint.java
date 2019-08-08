/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


public class NakedSingleHint extends DirectHint implements Rule {

    public NakedSingleHint(DirectHintProducer rule, Region region, Cell cell, int value) {
        super(rule, region, cell, value);
    }

    public double getDifficulty() {
        return 2.3;
    }

    public String getName() {
        return "Naked Single";
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " in the cell <b>" + getCell().toString() + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() + ": " + super.toString();
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "NakedSingleHint.html");
        return HtmlLoader.format(result, Integer.toString(super.getValue()),
                super.getCell().toString());
    }

}
