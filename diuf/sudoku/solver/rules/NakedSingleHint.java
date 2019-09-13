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
//import diuf.sudoku.Settings.*;


public class NakedSingleHint extends DirectHint implements Rule {

    public NakedSingleHint(DirectHintProducer rule, Region region, Cell cell, int value) {
        super(rule, region, cell, value);
    }

    public double getDifficulty() {
if (Settings.getInstance().newRating())
		return 1.6;//New rating
else
        return 2.3;//Original rating
    }

    public String getName() {
        return "Naked Single";
    }
    public String getShortName() {
        return "NS";
    }

    public String getClueHtml(Grid grid, boolean isBig) {
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
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, "NakedSingleHint.html");
        return HtmlLoader.format(result, Integer.toString(super.getValue()),
                super.getCell().toString());
    }

}
