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
public class WXYZWingHint extends IndirectHint implements Rule, HasParentPotentialHint {
    private final Cell wxyzCell;
    private final Cell wzCell;
    private final Cell xzCell;
    private final Cell yzCell;
    private final int value;
    private final boolean isIncompletedPivot;

    public WXYZWingHint(WXYZWing rule, Map<Cell, BitSet> removablePotentials,
            Cell wxyzCell, Cell wzCell, Cell xzCell, Cell yzCell, int value, boolean isIncompletedPivot) {
        super(rule, removablePotentials);
        this.wxyzCell = wxyzCell;
        this.wzCell = wzCell;
        this.xzCell = xzCell;
        this.yzCell = yzCell;
        this.value = value;
        this.isIncompletedPivot = isIncompletedPivot;
    }

    private int getX(Grid grid) {
        //BitSet wxyzPotentials = wxyzCell.getPotentialValues();
		BitSet wxyzPotentials = grid.getCellPotentialValues(wxyzCell.getIndex());
        int x = wxyzPotentials.nextSetBit(0);
        if (x == this.value)
            x = wxyzPotentials.nextSetBit(x + 1);
        return x;
    }

    private int getY(Grid grid) {
        //BitSet wxyzPotentials = wxyzCell.getPotentialValues();
		BitSet wxyzPotentials = grid.getCellPotentialValues(wxyzCell.getIndex());
        int x = getX(grid);
        int y = wxyzPotentials.nextSetBit(x + 1);
        if (y == this.value)
            y = wxyzPotentials.nextSetBit(y + 1);
        return y;
    }

    private int getZ(Grid grid) {
        //BitSet wxyzPotentials = wxyzCell.getPotentialValues();
		BitSet wxyzPotentials = grid.getCellPotentialValues(wxyzCell.getIndex());
        int y = getY(grid);
        int z = wxyzPotentials.nextSetBit(y + 1);
        if (z == this.value)
            z = wxyzPotentials.nextSetBit(z + 1);
        return z;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        // x, y and z of WXYZ cell (orange)
        //result.put(wxyzCell, wxyzCell.getPotentialValues());
		result.put(wxyzCell, grid.getCellPotentialValues(wxyzCell.getIndex()));
        // z value (green)
        BitSet zSet = SingletonBitSet.create(value);
        result.put(wzCell, zSet);
        result.put(xzCell, zSet);
        result.put(yzCell, zSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // Add x, y and z of WXYZ cell (orange)
        BitSet wxyz = new BitSet(10);
        wxyz.set(getX(grid));
        wxyz.set(getY(grid));
        wxyz.set(getZ(grid));
        result.put(wxyzCell, wxyz);
        return result;
    }

    public double getDifficulty() {
        return isIncompletedPivot ? 4.5 : 4.6;
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getName() {
        return isIncompletedPivot ? "WXYZ-Wing B" : "WXYZ-Wing A";
    }

    public String getShortName() {
		return isIncompletedPivot ? "WXYB" : "WXYA";
	}

    private int getRemainingValue(Grid grid, Cell c) {
        BitSet result = (BitSet)grid.getCellPotentialValues(c.getIndex()).clone();
        result.clear(value);
        return result.nextSetBit(0);
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
        int wValue = getRemainingValue(grid, wzCell);
        Link wLink = new Link(wxyzCell, wValue, wzCell, wValue);
        result.add(wLink);
        int xValue = getRemainingValue(grid, xzCell);
        Link xLink = new Link(wxyzCell, xValue, xzCell, xValue);
        result.add(xLink);
        int yValue = getRemainingValue(grid, yzCell);
        Link yLink = new Link(wxyzCell, yValue, yzCell, yValue);
        result.add(yLink);

        return result;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {wxyzCell, wzCell, xzCell, yzCell};
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        Cell wxyzCell = Grid.getCell(this.wxyzCell.getIndex());
        Cell wzCell = Grid.getCell(this.wzCell.getIndex());
        Cell xzCell = Grid.getCell(this.xzCell.getIndex());
        Cell yzCell = Grid.getCell(this.yzCell.getIndex());
        for (int p = 1; p <= 9; p++) {
            //if (wxyzCell.hasPotentialValue(p) && !this.wxyzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(wxyzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.wxyzCell.getIndex(), p))
                result.add(new Potential(this.wxyzCell, p, false));
            //if (wzCell.hasPotentialValue(p) && !this.wzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(wzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.wzCell.getIndex(), p))
                result.add(new Potential(this.wzCell, p, false));
            //if (xzCell.hasPotentialValue(p) && !this.xzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(xzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.xzCell.getIndex(), p))
                result.add(new Potential(this.xzCell, p, false));
            //if (yzCell.hasPotentialValue(p) && !this.yzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(yzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.yzCell.getIndex(), p))
                result.add(new Potential(this.yzCell, p, false));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WXYZWingHint))
            return false;
        WXYZWingHint other = (WXYZWingHint)o;
        if (this.wxyzCell != other.wxyzCell || this.value != other.value)
            return false;
        return this.wzCell == other.wzCell && this.xzCell == other.xzCell && this.yzCell == other.yzCell;
    }

    @Override
    public int hashCode() {
        return wxyzCell.hashCode() ^ wzCell.hashCode() ^ xzCell.hashCode() ^ yzCell.hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + getX(grid) + ", " + getY(grid) + ", " + getZ(grid) + " and <b>" + value + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() +
                ": " +
                Cell.toFullString(wxyzCell, wzCell, xzCell, yzCell) +
                " on value " +
                value;
    }

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, isIncompletedPivot ? "XYZExtensionHint.html" : "WXYZWingHint.html");
        String cell1 = wxyzCell.toString();
        String cell2 = wzCell.toString();
        String cell3 = xzCell.toString();
        String cell4 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell2, cell3, cell4, value, getX(grid), getY(grid), getZ(grid));
        return result;
    }
}
