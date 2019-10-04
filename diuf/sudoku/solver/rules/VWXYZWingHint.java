package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;





/**
 * VWXYZ-Wing hints
 */

public class VWXYZWingHint extends IndirectHint implements Rule, HasParentPotentialHint {
    private final Cell vwxyzCell;
    private final Cell vzCell;
    private final Cell wzCell;
    private final Cell xzCell;
    private final Cell yzCell;
    private final int value;
    private final boolean isIncompletedPivot;

    
	public VWXYZWingHint(VWXYZWing rule, Map<Cell, BitSet> removablePotentials,
            Cell vwxyzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            int value, boolean isIncompletedPivot) {
        super(rule, removablePotentials);
        this.vwxyzCell = vwxyzCell;
        this.vzCell = vzCell;
        this.wzCell = wzCell;
        this.xzCell = xzCell;
        this.yzCell = yzCell;
        this.value = value;
        this.isIncompletedPivot = isIncompletedPivot;
    }

    private int getW(Grid grid) {
        //BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
        BitSet vwxyzPotentials = grid.getCellPotentialValues(vwxyzCell.getIndex());
		int w = vwxyzPotentials.nextSetBit(0);
        if (w == this.value)
            w = vwxyzPotentials.nextSetBit(w + 1);
        return w;
    }

    private int getX(Grid grid) {
        //BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
        BitSet vwxyzPotentials = grid.getCellPotentialValues(vwxyzCell.getIndex());
		int w = getW(grid);
        int x = vwxyzPotentials.nextSetBit(w + 1);
        if (x == this.value)
            x = vwxyzPotentials.nextSetBit(x + 1);
        return x;
    }

    private int getY(Grid grid) {
        //BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
		BitSet vwxyzPotentials = grid.getCellPotentialValues(vwxyzCell.getIndex());																   
        int x = getX(grid);
        int y = vwxyzPotentials.nextSetBit(x + 1);
        if (y == this.value)
            y = vwxyzPotentials.nextSetBit(y + 1);
        return y;
    }

    private int getZ(Grid grid) {
        //BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
		BitSet vwxyzPotentials = grid.getCellPotentialValues(vwxyzCell.getIndex());																   
        int y = getY(grid);
        int z = vwxyzPotentials.nextSetBit(y + 1);
        if (z == this.value)
            z = vwxyzPotentials.nextSetBit(z + 1);
        return z;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        // w, x, y and z of VWXYZ cell (orange)
        //result.put(vwxyzCell, vwxyzCell.getPotentialValues());
		result.put(vwxyzCell, grid.getCellPotentialValues(vwxyzCell.getIndex()));																		 
        // z value (green)
        BitSet zSet = SingletonBitSet.create(value);
        result.put(vzCell, zSet);
        result.put(wzCell, zSet);
        result.put(xzCell, zSet);
        result.put(yzCell, zSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // Add w, x, y and z of VWXYZ cell (orange)
        BitSet vwxyz = new BitSet(10);
        vwxyz.set(getW(grid));
        vwxyz.set(getX(grid));
        vwxyz.set(getY(grid));
        vwxyz.set(getZ(grid));
        result.put(vwxyzCell, vwxyz);
        return result;
    }

    public double getDifficulty() {
        return isIncompletedPivot ? 5.4: 5.5;
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getName() {
        return isIncompletedPivot ? "WXYZ-Wing (4 candidate pilot)" : "WXYZ-Wing (5 candidate pilot)";
    }

    public String getShortName() {
		return isIncompletedPivot ? "VWXY4" : "VWXY5";
	}
	
    private int getRemainingValue(Grid grid, Cell c) {
        BitSet result = (BitSet)grid.getCellPotentialValues(c.getIndex()).clone();
        result.clear(value);
        return result.nextSetBit(0);
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
        int vValue = getRemainingValue(grid, vzCell);
        Link vLink = new Link(vwxyzCell, vValue, vzCell, vValue);
        result.add(vLink);
        int wValue = getRemainingValue(grid, wzCell);
        Link wLink = new Link(vwxyzCell, wValue, wzCell, wValue);
        result.add(wLink);
        int xValue = getRemainingValue(grid, xzCell);
        Link xLink = new Link(vwxyzCell, xValue, xzCell, xValue);
        result.add(xLink);
        int yValue = getRemainingValue(grid, yzCell);
        Link yLink = new Link(vwxyzCell, yValue, yzCell, yValue);
        result.add(yLink);

        return result;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {vwxyzCell, vzCell, wzCell, xzCell, yzCell};
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        Cell vwxyzCell = Grid.getCell(this.vwxyzCell.getX(), this.vwxyzCell.getY());
        Cell vzCell = Grid.getCell(this.vzCell.getX(), this.vzCell.getY());
        Cell wzCell = Grid.getCell(this.wzCell.getX(), this.wzCell.getY());
        Cell xzCell = Grid.getCell(this.xzCell.getX(), this.xzCell.getY());
        Cell yzCell = Grid.getCell(this.yzCell.getX(), this.yzCell.getY());
        for (int p = 1; p <= 9; p++) {
            //if (vwxyzCell.hasPotentialValue(p) && !this.vwxyzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(vwxyzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.vwxyzCell.getIndex(), p))
                result.add(new Potential(this.vwxyzCell, p, false));
            //if (vzCell.hasPotentialValue(p) && !this.vzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(vzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.vzCell.getIndex(), p))
                result.add(new Potential(this.vzCell, p, false));
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
        if (!(o instanceof VWXYZWingHint))
            return false;
        VWXYZWingHint other = (VWXYZWingHint)o;
        if (this.vwxyzCell != other.vwxyzCell || this.value != other.value)
            return false;
        return this.vzCell == other.vzCell && this.wzCell == other.wzCell &&
                this.xzCell == other.xzCell && this.yzCell == other.yzCell;
    }

    @Override
    public int hashCode() {
        return vwxyzCell.hashCode() ^
                vzCell.hashCode() ^ wzCell.hashCode() ^ xzCell.hashCode() ^ yzCell.hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + getW(grid) + ", " + getX(grid) + ", " +
                    getY(grid) + ", " + getZ(grid) + " and <b>" + value + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() +
                ": " +
                Cell.toFullString(vwxyzCell, vzCell, wzCell, xzCell, yzCell) +
                " on value " +
                value;
    }

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, isIncompletedPivot ? "VWXYZWing4Hint.html" : "VWXYZWing5Hint.html");
        String cell1 = vwxyzCell.toString();
        String cell2 = vzCell.toString();
        String cell3 = wzCell.toString();
        String cell4 = xzCell.toString();
        String cell5 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell2, cell3, cell4, cell5, value, getW(grid), getX(grid), getY(grid), getZ(grid));
        return result;
    }
}
