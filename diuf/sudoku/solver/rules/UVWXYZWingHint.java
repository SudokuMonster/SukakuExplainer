package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * UVWXYZ-Wing hints
 */
public class UVWXYZWingHint extends IndirectHint implements Rule, HasParentPotentialHint {
    private final Cell UVWXYZCell;
    private final Cell uzCell;
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

    public UVWXYZWingHint(UVWXYZWing rule, Map<Cell, BitSet> removablePotentials,
            Cell UVWXYZCell, Cell uzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell, int zValue, int xValue, int biggestCardinality, int wingSize, boolean doubleLink, BitSet wingSet, int eliminationsTotal) {
        super(rule, removablePotentials);
        this.UVWXYZCell = UVWXYZCell;
        this.uzCell = uzCell;
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

    private int getU(Grid grid) {
        //BitSet UVWXYZPotentials = UVWXYZCell.getPotentialValues();
		BitSet UVWXYZPotentials = (BitSet)wingSet.clone();
        int u = UVWXYZPotentials.nextSetBit(0);
        if (u == this.zValue || u == this.xValue)
            u = UVWXYZPotentials.nextSetBit(u + 1);
        if (u == this.zValue || u == this.xValue)
            u = UVWXYZPotentials.nextSetBit(u + 1);		
        return u;
    }

    private int getV(Grid grid) {
        //BitSet UVWXYZPotentials = UVWXYZCell.getPotentialValues();
		BitSet UVWXYZPotentials = (BitSet)wingSet.clone();
        int u = getU(grid);
        int v = UVWXYZPotentials.nextSetBit(u + 1);
        if (v == this.zValue || v == this.xValue)
            v = UVWXYZPotentials.nextSetBit(v + 1);
        if (v == this.zValue || v == this.xValue)
            v = UVWXYZPotentials.nextSetBit(v + 1);		
        return v;
    }

    private int getW(Grid grid) {
        //BitSet UVWXYZPotentials = UVWXYZCell.getPotentialValues();
		BitSet UVWXYZPotentials = (BitSet)wingSet.clone();
        int v = getV(grid);
        int w = UVWXYZPotentials.nextSetBit(v + 1);
        if (w == this.zValue || w == this.xValue)
            w = UVWXYZPotentials.nextSetBit(w + 1);
        if (w == this.zValue || w == this.xValue)
            w = UVWXYZPotentials.nextSetBit(w + 1);		
        return w;
    }

	private int getY(Grid grid) {
        //BitSet UVWXYZPotentials = UVWXYZCell.getPotentialValues();
		BitSet UVWXYZPotentials = (BitSet)wingSet.clone();
        int w = getW(grid);
        int y = UVWXYZPotentials.nextSetBit(w + 1);
        if (y == this.zValue || y == this.xValue)
            y = UVWXYZPotentials.nextSetBit(y + 1);
        if (y == this.zValue || y == this.xValue)
            y = UVWXYZPotentials.nextSetBit(y + 1);		
        return y;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
/*        // x, y and z of UVWXYZ cell (orange)
        //result.put(UVWXYZCell, UVWXYZCell.getPotentialValues());
		result.put(UVWXYZCell, grid.getCellPotentialValues(UVWXYZCell.getIndex()));*/
        // z value (green)
		// All green if doubly linked
        BitSet zSet = SingletonBitSet.create(zValue);
		if (!doubleLink) {
			if (grid.hasCellPotentialValue(uzCell.getIndex(), zValue))
				result.put(uzCell, zSet);
			if (grid.hasCellPotentialValue(vzCell.getIndex(), zValue))
				result.put(vzCell, zSet);
			if (grid.hasCellPotentialValue(wzCell.getIndex(), zValue))
				result.put(wzCell, zSet);
			if (grid.hasCellPotentialValue(xzCell.getIndex(), zValue))
				result.put(xzCell, zSet);
			if (grid.hasCellPotentialValue(UVWXYZCell.getIndex(), zValue))
				result.put(UVWXYZCell, zSet);
			result.put(yzCell, zSet);
		}
		else {
				result.put(uzCell, grid.getCellPotentialValues(uzCell.getIndex()));			
				result.put(vzCell, grid.getCellPotentialValues(vzCell.getIndex()));			
				result.put(wzCell, grid.getCellPotentialValues(wzCell.getIndex()));			
				result.put(xzCell, grid.getCellPotentialValues(xzCell.getIndex()));	
				result.put(UVWXYZCell, grid.getCellPotentialValues(UVWXYZCell.getIndex()));	
				result.put(yzCell, grid.getCellPotentialValues(yzCell.getIndex()));
		}
		return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // Add x, y and z of UVWXYZ cell (orange)
		/*if (!this.doubleLink) {
			BitSet UVWXYZ = new BitSet(10);
			UVWXYZ.set(this.xValue);
			UVWXYZ.set(getY(grid));
			UVWXYZ.set(getW(grid));
			UVWXYZ.set(getV(grid));
			UVWXYZ.set(getU(grid));
			result.put(UVWXYZCell, UVWXYZ);
		}*/
        return result;
    }

    public int getEliminationsTotal() {
		return eliminationsTotal;
	}
	
    public double getDifficulty() {
		//double-link has no impact on rating
        double result = 6.6; //base rating
		//int sizeDif = (6 + 2) / 2; //Avarage of possible pilot cell size
		//result += ((6-sizeDif)- Math.abs(sizeDif - biggestCardinality)) * 0.1;//Extremeties of size are easier than middle size
		return result;//difficulty 6.6 (Fixed at base rating)
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getSuffix() {
		return ((doubleLink ? 2 : 1)) + "" + biggestCardinality + "" + wingSize;
	}
	
	public String getName() {
        return "UVWXYZ-Wing " + getSuffix();
    }

    public String getShortName() {
		return "UXY" + getSuffix();
	}

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
		if (grid.hasCellPotentialValue(UVWXYZCell.getIndex(), xValue)) {
			Link wLink = new Link(yzCell, xValue, UVWXYZCell, xValue);
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
		if (grid.hasCellPotentialValue(uzCell.getIndex(), xValue)) {
			Link uLink = new Link(yzCell, xValue, uzCell, xValue);
			result.add(uLink);
		}
		if (doubleLink) {
			if (grid.hasCellPotentialValue(UVWXYZCell.getIndex(), zValue)) {
				Link wLink = new Link(yzCell, zValue, UVWXYZCell, zValue);
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
			if (grid.hasCellPotentialValue(uzCell.getIndex(), zValue)) {
				Link uLink = new Link(yzCell, zValue, uzCell, zValue);
				result.add(uLink);
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
		return new Cell[] {UVWXYZCell, uzCell, vzCell, wzCell, xzCell, yzCell};
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        Cell UVWXYZCell = Grid.getCell(this.UVWXYZCell.getIndex());
        Cell uzCell = Grid.getCell(this.uzCell.getIndex());
        Cell vzCell = Grid.getCell(this.vzCell.getIndex());
        Cell wzCell = Grid.getCell(this.wzCell.getIndex());
        Cell xzCell = Grid.getCell(this.xzCell.getIndex());
        Cell yzCell = Grid.getCell(this.yzCell.getIndex());
        for (int p = 1; p <= 9; p++) {
            //if (UVWXYZCell.hasPotentialValue(p) && !this.UVWXYZCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(UVWXYZCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.UVWXYZCell.getIndex(), p))
                result.add(new Potential(this.UVWXYZCell, p, false));
            //if (uzCell.hasPotentialValue(p) && !this.uzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(uzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.uzCell.getIndex(), p))
                result.add(new Potential(this.uzCell, p, false));
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
        if (!(o instanceof UVWXYZWingHint))
            return false;
        UVWXYZWingHint other = (UVWXYZWingHint)o;
        if (this.UVWXYZCell != other.UVWXYZCell || this.zValue != other.zValue)
            return false;
        return this.uzCell == other.uzCell && this.vzCell == other.vzCell && this.wzCell == other.wzCell && this.xzCell == other.xzCell && this.yzCell == other.yzCell;
    }

    @Override
    public int hashCode() {
        return UVWXYZCell.hashCode() ^ uzCell.hashCode() ^ vzCell.hashCode() ^ wzCell.hashCode() ^ xzCell.hashCode() ^ yzCell.hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + getU(grid) + ", " + getV(grid) + ", " + getW(grid) + ", " + getY(grid) + " and <b>" + xValue + zValue + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
 		if (!doubleLink)
			return getName() +
                ": " +
                Cell.toFullString(UVWXYZCell, uzCell, vzCell, wzCell, xzCell, yzCell) +
                " on value " +
                zValue;
		else
			return getName() +
                ": " +
                Cell.toFullString(UVWXYZCell, uzCell, vzCell, wzCell, xzCell, yzCell) +
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
			result = HtmlLoader.loadHtml(this, "UVWXYZWingHint.html");
		else
			result = HtmlLoader.loadHtml(this, "UVWXYZWing2Hint.html");
        String cell1 = UVWXYZCell.toString();
        String cell2 = uzCell.toString();
        String cell3 = vzCell.toString();
		String cell4 = wzCell.toString();
        String cell5 = xzCell.toString();
        String cell6 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell3, cell4, cell5, cell6, zValue, getW(grid), getY(grid), getV(grid), xValue, biggestCardinality, wingSize, (doubleLink ? 2 : 1), sharedRegions(), cell2);
        return result;
    }
}
