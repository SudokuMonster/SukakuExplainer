package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * TUVWXYZ-Wing hints
 */
public class TUVWXYZWingHint extends IndirectHint implements Rule, HasParentPotentialHint {
    private final Cell TUVWXYZCell;
    private final Cell tzCell;
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

    public TUVWXYZWingHint(TUVWXYZWing rule, Map<Cell, BitSet> removablePotentials,
            Cell TUVWXYZCell, Cell tzCell, Cell uzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell, int zValue, int xValue, int biggestCardinality, int wingSize, boolean doubleLink, BitSet wingSet, int eliminationsTotal) {
        super(rule, removablePotentials);
        this.TUVWXYZCell = TUVWXYZCell;
        this.tzCell = tzCell;
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

    private int getT(Grid grid) {
        //BitSet TUVWXYZPotentials = TUVWXYZCell.getPotentialValues();
		BitSet TUVWXYZPotentials = (BitSet)wingSet.clone();
        int t = TUVWXYZPotentials.nextSetBit(0);
        if (t == this.zValue || t == this.xValue)
            t = TUVWXYZPotentials.nextSetBit(t + 1);
        if (t == this.zValue || t == this.xValue)
            t = TUVWXYZPotentials.nextSetBit(t + 1);		
        return t;
    }
    private int getU(Grid grid) {
        //BitSet TUVWXYZPotentials = TUVWXYZCell.getPotentialValues();
		BitSet TUVWXYZPotentials = (BitSet)wingSet.clone();
        int t = getT(grid);
		int u = TUVWXYZPotentials.nextSetBit(t + 1);
        if (u == this.zValue || u == this.xValue)
            u = TUVWXYZPotentials.nextSetBit(u + 1);
        if (u == this.zValue || u == this.xValue)
            u = TUVWXYZPotentials.nextSetBit(u + 1);		
        return u;
    }

    private int getV(Grid grid) {
        //BitSet TUVWXYZPotentials = TUVWXYZCell.getPotentialValues();
		BitSet TUVWXYZPotentials = (BitSet)wingSet.clone();
        int u = getU(grid);
        int v = TUVWXYZPotentials.nextSetBit(u + 1);
        if (v == this.zValue || v == this.xValue)
            v = TUVWXYZPotentials.nextSetBit(v + 1);
        if (v == this.zValue || v == this.xValue)
            v = TUVWXYZPotentials.nextSetBit(v + 1);		
        return v;
    }

    private int getW(Grid grid) {
        //BitSet TUVWXYZPotentials = TUVWXYZCell.getPotentialValues();
		BitSet TUVWXYZPotentials = (BitSet)wingSet.clone();
        int v = getV(grid);
        int w = TUVWXYZPotentials.nextSetBit(v + 1);
        if (w == this.zValue || w == this.xValue)
            w = TUVWXYZPotentials.nextSetBit(w + 1);
        if (w == this.zValue || w == this.xValue)
            w = TUVWXYZPotentials.nextSetBit(w + 1);		
        return w;
    }

	private int getY(Grid grid) {
        //BitSet TUVWXYZPotentials = TUVWXYZCell.getPotentialValues();
		BitSet TUVWXYZPotentials = (BitSet)wingSet.clone();
        int w = getW(grid);
        int y = TUVWXYZPotentials.nextSetBit(w + 1);
        if (y == this.zValue || y == this.xValue)
            y = TUVWXYZPotentials.nextSetBit(y + 1);
        if (y == this.zValue || y == this.xValue)
            y = TUVWXYZPotentials.nextSetBit(y + 1);		
        return y;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
/*        // x, y and z of TUVWXYZ cell (orange)
        //result.put(TUVWXYZCell, TUVWXYZCell.getPotentialValues());
		result.put(TUVWXYZCell, grid.getCellPotentialValues(TUVWXYZCell.getIndex()));*/
        // z value (green)
		// All green if doubly linked
        BitSet zSet = SingletonBitSet.create(zValue);
		if (!doubleLink) {
			if (grid.hasCellPotentialValue(tzCell.getIndex(), zValue))
				result.put(tzCell, zSet);
			if (grid.hasCellPotentialValue(uzCell.getIndex(), zValue))
				result.put(uzCell, zSet);
			if (grid.hasCellPotentialValue(vzCell.getIndex(), zValue))
				result.put(vzCell, zSet);
			if (grid.hasCellPotentialValue(wzCell.getIndex(), zValue))
				result.put(wzCell, zSet);
			if (grid.hasCellPotentialValue(xzCell.getIndex(), zValue))
				result.put(xzCell, zSet);
			if (grid.hasCellPotentialValue(TUVWXYZCell.getIndex(), zValue))
				result.put(TUVWXYZCell, zSet);
			result.put(yzCell, zSet);
		}
		else {
				result.put(tzCell, grid.getCellPotentialValues(tzCell.getIndex()));			
				result.put(uzCell, grid.getCellPotentialValues(uzCell.getIndex()));			
				result.put(vzCell, grid.getCellPotentialValues(vzCell.getIndex()));			
				result.put(wzCell, grid.getCellPotentialValues(wzCell.getIndex()));			
				result.put(xzCell, grid.getCellPotentialValues(xzCell.getIndex()));	
				result.put(TUVWXYZCell, grid.getCellPotentialValues(TUVWXYZCell.getIndex()));	
				result.put(yzCell, grid.getCellPotentialValues(yzCell.getIndex()));
		}
		return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // Add x, y and z of TUVWXYZ cell (orange)
		/*if (!this.doubleLink) {
			BitSet TUVWXYZ = new BitSet(10);
			TUVWXYZ.set(this.xValue);
			TUVWXYZ.set(getY(grid));
			TUVWXYZ.set(getW(grid));
			TUVWXYZ.set(getV(grid));
			TUVWXYZ.set(getU(grid));
			result.put(TUVWXYZCell, TUVWXYZ);
		}*/
        return result;
    }

    public int getEliminationsTotal() {
		return eliminationsTotal;
	}
	
    public double getDifficulty() {
		//double-link has no impact on rating
        double result = 7.5; //base rating
		//int sizeDif = (7 + 2) / 2; //Avarage of possible pilot cell size
		//result += ((7-sizeDif)- Math.abs(sizeDif - biggestCardinality)) * 0.1;//Extremeties of size are easier than middle size
		return result;//difficulty 7.5  (Fixed at base rating)
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getSuffix() {
		return ((doubleLink ? 2 : 1)) + "" + biggestCardinality + "" + wingSize;
	}
	
	public String getName() {
        return "TUVWXYZ-Wing " + getSuffix();
    }

    public String getShortName() {
		return "TXY" + getSuffix();
	}

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
		if (grid.hasCellPotentialValue(TUVWXYZCell.getIndex(), xValue)) {
			Link wLink = new Link(yzCell, xValue, TUVWXYZCell, xValue);
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
		if (grid.hasCellPotentialValue(tzCell.getIndex(), xValue)) {
			Link tLink = new Link(yzCell, xValue, tzCell, xValue);
			result.add(tLink);
		}
		if (doubleLink) {
			if (grid.hasCellPotentialValue(TUVWXYZCell.getIndex(), zValue)) {
				Link wLink = new Link(yzCell, zValue, TUVWXYZCell, zValue);
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
			if (grid.hasCellPotentialValue(tzCell.getIndex(), zValue)) {
				Link tLink = new Link(yzCell, zValue, tzCell, zValue);
				result.add(tLink);
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
		return new Cell[] {TUVWXYZCell, tzCell, uzCell, vzCell, wzCell, xzCell, yzCell};
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<Potential>();
        Cell TUVWXYZCell = Grid.getCell(this.TUVWXYZCell.getIndex());
        Cell tzCell = Grid.getCell(this.tzCell.getIndex());
        Cell uzCell = Grid.getCell(this.uzCell.getIndex());
        Cell vzCell = Grid.getCell(this.vzCell.getIndex());
        Cell wzCell = Grid.getCell(this.wzCell.getIndex());
        Cell xzCell = Grid.getCell(this.xzCell.getIndex());
        Cell yzCell = Grid.getCell(this.yzCell.getIndex());
        for (int p = 1; p <= 9; p++) {
            //if (TUVWXYZCell.hasPotentialValue(p) && !this.TUVWXYZCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(TUVWXYZCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.TUVWXYZCell.getIndex(), p))
                result.add(new Potential(this.TUVWXYZCell, p, false));
            //if (tzCell.hasPotentialValue(p) && !this.tzCell.hasPotentialValue(p))
			if (initialGrid.hasCellPotentialValue(tzCell.getIndex(), p) && !currentGrid.hasCellPotentialValue(this.tzCell.getIndex(), p))
                result.add(new Potential(this.tzCell, p, false));
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
        if (!(o instanceof TUVWXYZWingHint))
            return false;
        TUVWXYZWingHint other = (TUVWXYZWingHint)o;
        if (this.TUVWXYZCell != other.TUVWXYZCell || this.zValue != other.zValue)
            return false;
        return this.tzCell == other.tzCell &&  this.uzCell == other.uzCell && this.vzCell == other.vzCell && this.wzCell == other.wzCell && this.xzCell == other.xzCell && this.yzCell == other.yzCell;
    }

    @Override
    public int hashCode() {
        return TUVWXYZCell.hashCode() ^ tzCell.hashCode() ^ uzCell.hashCode() ^ vzCell.hashCode() ^ wzCell.hashCode() ^ xzCell.hashCode() ^ yzCell.hashCode();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + getT(grid) + ", " + getU(grid) + ", " + getV(grid) + ", " + getW(grid) + ", " + getY(grid) + " and <b>" + xValue + zValue + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
 		if (!doubleLink)
			return getName() +
                ": " +
                Cell.toFullString(TUVWXYZCell, tzCell, uzCell, vzCell, wzCell, xzCell, yzCell) +
                " on value " +
                zValue;
		else
			return getName() +
                ": " +
                Cell.toFullString(TUVWXYZCell, tzCell, uzCell, vzCell, wzCell, xzCell, yzCell) +
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
			result = HtmlLoader.loadHtml(this, "TUVWXYZWingHint.html");
		else
			result = HtmlLoader.loadHtml(this, "TUVWXYZWing2Hint.html");
        String cell1 = TUVWXYZCell.toString();
        String cell2 = tzCell.toString();
        String cell3 = uzCell.toString();
        String cell4 = vzCell.toString();
		String cell5 = wzCell.toString();
        String cell6 = xzCell.toString();
        String cell7 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell4, cell5, cell6, cell7, zValue, getW(grid), getY(grid), getV(grid), xValue, biggestCardinality, wingSize, (doubleLink ? 2 : 1), sharedRegions(), cell3, cell2 );
        return result;
    }
}
