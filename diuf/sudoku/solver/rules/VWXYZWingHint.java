package SudokuExplainer.solver.rules.wing;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.solver.rules.HasParentPotentialHint;
import SudokuExplainer.solver.rules.chaining.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;
import SudokuExplainer.units.Link;


/**
 * VWXYZ-Wing hints
 */
@SuppressWarnings("unused")
public class VWXYZWingHint extends IndirectHint implements Rule, HasParentPotentialHint {
    private final Cell vwxyzCell;
    private final Cell vzCell;
    private final Cell wzCell;
    private final Cell xzCell;
    private final Cell yzCell;
    private final int value;
    private final boolean isIncompletedPivot;

    public VWXYZWingHint(
            VWXYZWing rule, Map<Cell, BitSet> removablePotentials,
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

    private int getW() {
        BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
        int w = vwxyzPotentials.nextSetBit(0);
        if (w == this.value)
            w = vwxyzPotentials.nextSetBit(w + 1);
        return w;
    }

    private int getX() {
        BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
        int w = getW();
        int x = vwxyzPotentials.nextSetBit(w + 1);
        if (x == this.value)
            x = vwxyzPotentials.nextSetBit(x + 1);
        return x;
    }

    private int getY() {
        BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
        int x = getX();
        int y = vwxyzPotentials.nextSetBit(x + 1);
        if (y == this.value)
            y = vwxyzPotentials.nextSetBit(y + 1);
        return y;
    }

    private int getZ() {
        BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
        int y = getY();
        int z = vwxyzPotentials.nextSetBit(y + 1);
        if (z == this.value)
            z = vwxyzPotentials.nextSetBit(z + 1);
        return z;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        // w, x, y and z of VWXYZ cell (orange)
        result.put(vwxyzCell, vwxyzCell.getPotentialValues());
        // z value (green)
        BitSet zSet = SingletonBitSet.create(value);
        result.put(vzCell, zSet);
        result.put(wzCell, zSet);
        result.put(xzCell, zSet);
        result.put(yzCell, zSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // Add w, x, y and z of VWXYZ cell (orange)
        BitSet vwxyz = new BitSet(10);
        vwxyz.set(getW());
        vwxyz.set(getX());
        vwxyz.set(getY());
        vwxyz.set(getZ());
        result.put(vwxyzCell, vwxyz);
        return result;
    }

    public double getDifficulty() {
        return isIncompletedPivot ? 4.8: 5.0;
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getName() {
        return isIncompletedPivot ? "WXYZ-Wing Extension" : "VWXYZ-Wing";
    }

    private int getRemainingValue(Cell c) {
        BitSet result = (BitSet)c.getPotentialValues().clone();
        result.clear(value);
        return result.nextSetBit(0);
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        Collection<Link> result = new ArrayList<>();
        int vValue = getRemainingValue(vzCell);
        Link vLink = new Link(vwxyzCell, vValue, vzCell, vValue);
        result.add(vLink);
        int wValue = getRemainingValue(wzCell);
        Link wLink = new Link(vwxyzCell, wValue, wzCell, wValue);
        result.add(wLink);
        int xValue = getRemainingValue(xzCell);
        Link xLink = new Link(vwxyzCell, xValue, xzCell, xValue);
        result.add(xLink);
        int yValue = getRemainingValue(yzCell);
        Link yLink = new Link(vwxyzCell, yValue, yzCell, yValue);
        result.add(yLink);

        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
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
        Collection<Potential> result = new ArrayList<>();
        Cell vwxyzCell = initialGrid.getCell(this.vwxyzCell.getX(), this.vwxyzCell.getY());
        Cell vzCell = initialGrid.getCell(this.vzCell.getX(), this.vzCell.getY());
        Cell wzCell = initialGrid.getCell(this.wzCell.getX(), this.wzCell.getY());
        Cell xzCell = initialGrid.getCell(this.xzCell.getX(), this.xzCell.getY());
        Cell yzCell = initialGrid.getCell(this.yzCell.getX(), this.yzCell.getY());
        for (int p = 1; p <= 9; p++) {
            if (vwxyzCell.hasPotentialValue(p) && !this.vwxyzCell.hasPotentialValue(p))
                result.add(new Potential(this.vwxyzCell, p, false));
            if (vzCell.hasPotentialValue(p) && !this.vzCell.hasPotentialValue(p))
                result.add(new Potential(this.vzCell, p, false));
            if (wzCell.hasPotentialValue(p) && !this.wzCell.hasPotentialValue(p))
                result.add(new Potential(this.wzCell, p, false));
            if (xzCell.hasPotentialValue(p) && !this.xzCell.hasPotentialValue(p))
                result.add(new Potential(this.xzCell, p, false));
            if (yzCell.hasPotentialValue(p) && !this.yzCell.hasPotentialValue(p))
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

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + getW() + ", " + getX() + ", " +
                    getY() + ", " + getZ() + " and <b>" + value + "</b>";
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
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, isIncompletedPivot ? "WXYZWingExtensionHint.html" : "VWXYZWingHint.html");
        String cell1 = vwxyzCell.toString();
        String cell2 = vzCell.toString();
        String cell3 = wzCell.toString();
        String cell4 = xzCell.toString();
        String cell5 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell2, cell3, cell4, cell5, value, getW(), getX(), getY(), getZ());
        return result;
    }
}
