package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * WXYZ-Wing hints
 */
public class VLockingHint extends IndirectHint implements Rule, HasParentPotentialHint {
    
	private final Cell[] regionCells;
    private final int Value;
	private final Region lockedRegion;
	private final int eliminationsTotal;

    public VLockingHint(VLocking rule, Cell[] regionCells, int Value, Map<Cell, BitSet> removablePotentials, Region lockedRegion, int eliminationsTotal) {
        super(rule, removablePotentials);
        this.regionCells = regionCells;
        this.Value = Value;
        this.lockedRegion = lockedRegion;
        this.eliminationsTotal = eliminationsTotal;
	}

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        BitSet vSet = SingletonBitSet.create(Value);
		Map<Cell, BitSet> result = new HashMap<>();
        for (int i = 0; i < regionCells.length; i++) {
			result.put(regionCells[i], vSet);
		}
		return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        return result;
    }
 
    public int getEliminationsTotal() {
		return eliminationsTotal;
	}
	
	public double getDifficulty() {
		return 2.9;
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getName() {
        return "Generalized Intersections";
    }

    public String getShortName() {
		return "gI";
	}

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
        return result;
    }

    @Override
    public Region[] getRegions() {
		return new Grid.Region[] {this.lockedRegion};
    }

    public Cell[] getSelectedCells() {
        return this.regionCells;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        BitSet myPositions = new BitSet(9);
        myPositions.or(lockedRegion.getPotentialPositions(currentGrid, Value));
        for (int i = 0; i < 9; i++) {
            if (!myPositions.get(i)) {
                Cell cell = lockedRegion.getCell(i);
                if (initialGrid.hasCellPotentialValue(cell.getIndex(), Value))
                        result.add(new Potential(cell, Value, false));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VLockingHint))
            return false;
        VLockingHint other = (VLockingHint)o;
        if (this.regionCells != other.regionCells)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return regionCells[0].hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + Value + " in "+ lockedRegion.toFullString() + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
 		return Cell.toFullString(regionCells) +
                " on value " +
                Value + " in " + lockedRegion.toFullString();
    }

    @Override
    public String toHtml(Grid grid) {
		String result;
		result = HtmlLoader.loadHtml(this, "VLocking.html");
        String region = lockedRegion.toFullString();
		String ruleName = getName();
        result = HtmlLoader.format(result, region, Value, ruleName);
        return result;
    }
}
