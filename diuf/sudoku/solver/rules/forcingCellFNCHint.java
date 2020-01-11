/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * forcingCellFNC hints
 */
public class forcingCellFNCHint extends IndirectHint implements Rule, HasParentPotentialHint {
    
	private final Cell forcingCell;
    private final int[] Values;

    public forcingCellFNCHint(forcingCellFNC rule, Map<Cell, BitSet> removablePotentials, Cell forcingCell, int[] Values) {
        super(rule, removablePotentials);
        this.forcingCell = forcingCell;
        this.Values = Values;
	}

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
		Map<Cell, BitSet> result = new HashMap<>();
		return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
		Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        return result;
    }
	
	public double getDifficulty() {
		return 2.4;
    }

    public String getGroup() {
        return "NC_Techniques";
    }

    public String getName() {
        return "Non-Consecutive Forcing Cell";
    }

    public String getShortName() {
		return "kNC";
	}

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
        return result;
    }

    @Override
    public Region[] getRegions() {
		return null;
    }

    public Cell[] getSelectedCells() {
        Cell[] result = {this.forcingCell};
		return result;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        //BitSet myPositions = new BitSet(9);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof forcingCellFNCHint))
            return false;
        forcingCellFNCHint other = (forcingCellFNCHint)o;
        if (this.forcingCell != other.forcingCell)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return forcingCell.hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " eliminations by observing the values in "+ forcingCell.toFullString() + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
 		StringBuilder builder = new StringBuilder();
        builder.append(Cell.toFullString(forcingCell));
        builder.append(" on value(s) ");
        for (int i = 0; i < Values.length; i++) {
            if (i > 0)
                builder.append(",");
            builder.append(Integer.toString(Values[i]));
        }
        return builder.toString();
    }

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, "forcingCellNC.html");
        String valueList = HtmlLoader.formatValues(Values);
        String cellName = forcingCell.toString();
        String ruleName = getName();
        return HtmlLoader.format(result, cellName, valueList,
                ruleName);
    }
}
