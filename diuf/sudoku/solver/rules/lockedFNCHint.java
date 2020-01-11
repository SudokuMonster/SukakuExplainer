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
 * lockedFNC hints
 */
public class lockedFNCHint extends IndirectHint implements Rule, HasParentPotentialHint {
    
	private final Cell[] lockedCells;
    private final int[] Values;
	private final Region region;
	private final int value;

    public lockedFNCHint(lockedFNC rule, Map<Cell, BitSet> removablePotentials, Cell[] lockedCells, int[] Values, Region region, int value) {
        super(rule, removablePotentials);
        this.lockedCells = lockedCells;
        this.Values = Values;
		this.region = region;
		this.value = value;
	}

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
		Map<Cell, BitSet> result = new HashMap<>();
		BitSet zSet = SingletonBitSet.create(this.value);
		for (Cell cell : lockedCells) {
			if (grid.hasCellPotentialValue(cell.getIndex(), this.value))
				result.put(cell, zSet);
		}
		return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
		Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        return result;
    }
	
	public double getDifficulty() {
		return 2.5;
    }

    public String getGroup() {
        return "NC_Techniques";
    }

    public String getName() {
        return "Locked Non Consecutive";
    }

    public String getShortName() {
		return "lNC";
	}

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
        return result;
    }

    @Override
    public Region[] getRegions() {
		return new Grid.Region[] {this.region};
    }

    public Cell[] getSelectedCells() {
        Cell[] result = this.lockedCells;
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
        if (!(o instanceof lockedFNCHint))
            return false;
        lockedFNCHint other = (lockedFNCHint)o;
        if (this.lockedCells != other.lockedCells)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return lockedCells.hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " eliminations by observing the values " + this.value + " in "+ region.toFullString() + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
 		StringBuilder builder = new StringBuilder();
		builder.append(this.value + ": ");
        builder.append(Cell.toFullString(lockedCells));
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
        String result = HtmlLoader.loadHtml(this, "lockedNC.html");
        String valueList = HtmlLoader.formatValues(Values);
		String cellList = HtmlLoader.formatList(lockedCells);
		String mainValue = "" + this.value;
		String regionName = region.toFullString();;
        String ruleName = getName();
        return HtmlLoader.format(result, cellList, valueList,
                ruleName, mainValue, regionName);
    }
}
