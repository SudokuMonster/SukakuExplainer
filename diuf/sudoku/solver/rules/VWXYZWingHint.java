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
    private final int zValue;
	private final int xValue;
	private final int biggestCardinality;
	private final int wingSize;
	private final boolean doubleLink;
	private final BitSet wingSet;
	private final int eliminationsTotal;

    public VWXYZWingHint(VWXYZWing rule, Map<Cell, BitSet> removablePotentials,
            Cell vwxyzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell, int zValue, int xValue, int biggestCardinality, int wingSize, boolean doubleLink, BitSet wingSet, int eliminationsTotal) {
        super(rule, removablePotentials);
        this.vwxyzCell = vwxyzCell;
        this.vzCell = vzCell;
        this.wzCell = wzCell;
        this.xzCell = xzCell;
        this.yzCell = yzCell;
        this.zValue = zValue;
		this.xValue = xValue;
		this.biggestCardinality = biggestCardinality;
		this.wingSize = wingSize;
		this.doubleLink = doubleLink;
		this.wingSet = wingSet;
		this.eliminationsTotal = eliminationsTotal;
    }

    private int getV(Grid grid) {
        //BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
		BitSet vwxyzPotentials = (BitSet)wingSet.clone();
        int v = vwxyzPotentials.nextSetBit(0);
        if (v == this.zValue || v == this.xValue)
            v = vwxyzPotentials.nextSetBit(v + 1);
        if (v == this.zValue || v == this.xValue)
            v = vwxyzPotentials.nextSetBit(v + 1);		
        return v;
    }

    private int getW(Grid grid) {
        //BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
		BitSet vwxyzPotentials = (BitSet)wingSet.clone();
        int v = getV(grid);
        int w = vwxyzPotentials.nextSetBit(v + 1);
        if (w == this.zValue || w == this.xValue)
            w = vwxyzPotentials.nextSetBit(w + 1);
        if (w == this.zValue || w == this.xValue)
            w = vwxyzPotentials.nextSetBit(w + 1);		
        return w;
    }

	private int getY(Grid grid) {
        //BitSet vwxyzPotentials = vwxyzCell.getPotentialValues();
		BitSet vwxyzPotentials = (BitSet)wingSet.clone();
        int w = getW(grid);
        int y = vwxyzPotentials.nextSetBit(w + 1);
        if (y == this.zValue || y == this.xValue)
            y = vwxyzPotentials.nextSetBit(y + 1);
        if (y == this.zValue || y == this.xValue)
            y = vwxyzPotentials.nextSetBit(y + 1);		
        return y;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
/*        // x, y and z of VWXYZ cell (orange)
        //result.put(vwxyzCell, vwxyzCell.getPotentialValues());
		result.put(vwxyzCell, grid.getCellPotentialValues(vwxyzCell.getIndex()));*/
        // z value (green)
		// All green if doubly linked
        BitSet zSet = SingletonBitSet.create(zValue);
		if (!doubleLink) {
			if (grid.hasCellPotentialValue(vzCell.getIndex(), zValue))
				result.put(vzCell, zSet);
			if (grid.hasCellPotentialValue(wzCell.getIndex(), zValue))
				result.put(wzCell, zSet);
			if (grid.hasCellPotentialValue(xzCell.getIndex(), zValue))
				result.put(xzCell, zSet);
			if (grid.hasCellPotentialValue(vwxyzCell.getIndex(), zValue))
				result.put(vwxyzCell, zSet);
			result.put(yzCell, zSet);
		}
		else {
				result.put(vzCell, grid.getCellPotentialValues(vzCell.getIndex()));			
				result.put(wzCell, grid.getCellPotentialValues(wzCell.getIndex()));			
				result.put(xzCell, grid.getCellPotentialValues(xzCell.getIndex()));	
				result.put(vwxyzCell, grid.getCellPotentialValues(vwxyzCell.getIndex()));	
				result.put(yzCell, grid.getCellPotentialValues(yzCell.getIndex()));
		}
		return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // Add x, y and z of VWXYZ cell (orange)
		/*if (!this.doubleLink) {
			BitSet vwxyz = new BitSet(10);
			vwxyz.set(this.xValue);
			vwxyz.set(getY(grid));
			vwxyz.set(getW(grid));
			vwxyz.set(getV(grid));
			result.put(vwxyzCell, vwxyz);
		}*/
        return result;
    }

    public int getEliminationsTotal() {
		return eliminationsTotal;
	}
	
    public double getDifficulty() {
		//double-link has no impact on rating
        double result = 6.2; //base rating
		int sizeDif = (5 + 2) / 2; //Avarage of possible pilot cell size Max 5 Min 2 Ave 3
		result += ((5-sizeDif)- Math.abs(sizeDif - biggestCardinality)) * 0.1;//Extremeties of size are easier than middle size
		return result;//Min difficulty 6.2 //Max difficulty 6.4
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getSuffix() {
		return ((doubleLink ? 2 : 1)) + "" + biggestCardinality + "" + wingSize;
	}
	
	public String getName() {
        return "VWXYZ-Wing " + getSuffix();
    }

    public String getShortName() {
		return "VXY" + getSuffix();
	}

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
		if (grid.hasCellPotentialValue(vwxyzCell.getIndex(), xValue)) {
			Link wLink = new Link(yzCell, xValue, vwxyzCell, xValue);
			result.add(wLink);
		}
		if (grid.hasCellPotentialValue(xzCell.getIndex(), xValue)) {
			Link xLink = new Link(yzCell, xValue, xzCell, xValue);
			result.add(xLink);
		}
		if (grid.hasCellPotentialValue(wzCell.getIndex(), xValue)) {
			Link yLink = new Link(yzCell, xValue, wzCell, xValue);
			result.add(yLink);
		}
		if (grid.hasCellPotentialValue(vzCell.getIndex(), xValue)) {
			Link vLink = new Link(yzCell, xValue, vzCell, xValue);
			result.add(vLink);
		}
		if (doubleLink) {
			if (grid.hasCellPotentialValue(vwxyzCell.getIndex(), zValue)) {
				Link wLink = new Link(yzCell, zValue, vwxyzCell, zValue);
				result.add(wLink);
			}
			if (grid.hasCellPotentialValue(xzCell.getIndex(), zValue)) {
				Link xLink = new Link(yzCell, zValue, xzCell, zValue);
				result.add(xLink);
			}
			if (grid.hasCellPotentialValue(wzCell.getIndex(), zValue)) {
				Link yLink = new Link(yzCell, zValue, wzCell, zValue);
				result.add(yLink);
			}			
			if (grid.hasCellPotentialValue(vzCell.getIndex(), zValue)) {
				Link vLink = new Link(yzCell, zValue, vzCell, zValue);
				result.add(vLink);
			}
		}
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
        Cell vwxyzCell = Grid.getCell(this.vwxyzCell.getIndex());
        Cell vzCell = Grid.getCell(this.vzCell.getIndex());
        Cell wzCell = Grid.getCell(this.wzCell.getIndex());
        Cell xzCell = Grid.getCell(this.xzCell.getIndex());
        Cell yzCell = Grid.getCell(this.yzCell.getIndex());
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
        if (this.vwxyzCell != other.vwxyzCell || this.zValue != other.zValue)
            return false;
        return this.vzCell == other.wzCell && this.wzCell == other.wzCell && this.xzCell == other.xzCell && this.yzCell == other.yzCell;
    }

    @Override
    public int hashCode() {
        return vwxyzCell.hashCode() ^ vzCell.hashCode() ^ wzCell.hashCode() ^ xzCell.hashCode() ^ yzCell.hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + getV(grid) + ", " + getW(grid) + ", " + getY(grid) + " and <b>" + xValue + zValue + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
 		if (!doubleLink)
			return getName() +
                ": " +
                Cell.toFullString(vwxyzCell, vzCell, wzCell, xzCell, yzCell) +
                " on value " +
                zValue;
		else
			return getName() +
                ": " +
                Cell.toFullString(vwxyzCell, vzCell, wzCell, xzCell, yzCell) +
                " on values " +
                xValue + "," + zValue;
    }

	private String sharedRegions(){
		if (Settings.getInstance().isVanilla())
			return "row, column or block";
		else {
			String res[] = new String[10];
			int i = 0;
			String finalRes = "row";
			if (Settings.getInstance().isVLatin())
				return "row or column";
			else
				res[i++]= "column";
			if (Settings.getInstance().isBlocks())
				res[i++]= "block";
			if (Settings.getInstance().isDG())
				res[i++]= "disjoint group";
			if (Settings.getInstance().isWindows())
				res[i++]= "window group";
			if (Settings.getInstance().isX())
				res[i++]= "diagonal";
			if (Settings.getInstance().isGirandola())
				res[i++]= "girandola group";
			if (Settings.getInstance().isAsterisk())
				res[i++]= "asterisk group";
			if (Settings.getInstance().isCD())
				res[i++]= "center dot group";
			i--;
			for (int j = 0; j < i; j++)
				finalRes += ", " + res[j];
			finalRes += " or " + res[i];
			return finalRes;
		}
	}

    @Override
    public String toHtml(Grid grid) {
		String result;
		if (!doubleLink)
			result = HtmlLoader.loadHtml(this, "VWXYZWingHint.html");
		else
			result = HtmlLoader.loadHtml(this, "VWXYZWing2Hint.html");
        String cell1 = vwxyzCell.toString();
        String cell2 = vzCell.toString();
		String cell3 = wzCell.toString();
        String cell4 = xzCell.toString();
        String cell5 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell2, cell3, cell4, cell5, zValue, getW(grid), getY(grid), getV(grid), xValue, biggestCardinality, wingSize, (doubleLink ? 2 : 1), sharedRegions());
        return result;
    }
}
