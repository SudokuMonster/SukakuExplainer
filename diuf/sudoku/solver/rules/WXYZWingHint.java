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
    private final int zValue;
	private final int xValue;
	private final int biggestCardinality;
	private final int wingSize;
	private final boolean doubleLink;
	private final BitSet wingSet;
	private final int eliminationsTotal;

    public WXYZWingHint(WXYZWing rule, Map<Cell, BitSet> removablePotentials,
            Cell wxyzCell, Cell wzCell, Cell xzCell, Cell yzCell, int zValue, int xValue, int biggestCardinality, int wingSize, boolean doubleLink, BitSet wingSet, int eliminationsTotal) {
        super(rule, removablePotentials);
        this.wxyzCell = wxyzCell;
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

    private int getX(Grid grid) {
        //BitSet wxyzPotentials = wxyzCell.getPotentialValues();
		BitSet wxyzPotentials = (BitSet)wingSet.clone();
        int x = wxyzPotentials.nextSetBit(0);
        if (x == this.zValue)
            x = wxyzPotentials.nextSetBit(x + 1);
        return x;
    }

    private int getY(Grid grid) {
        //BitSet wxyzPotentials = wxyzCell.getPotentialValues();
		BitSet wxyzPotentials = (BitSet)wingSet.clone();
        int x = getX(grid);
        int y = wxyzPotentials.nextSetBit(x + 1);
        if (y == this.zValue)
            y = wxyzPotentials.nextSetBit(y + 1);
        return y;
    }

    private int getZ(Grid grid) {
        //BitSet wxyzPotentials = wxyzCell.getPotentialValues();
		BitSet wxyzPotentials = (BitSet)wingSet.clone();
        int y = getY(grid);
        int z = wxyzPotentials.nextSetBit(y + 1);
        if (z == this.zValue)
            z = wxyzPotentials.nextSetBit(z + 1);
        return z;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
/*        // x, y and z of WXYZ cell (orange)
        //result.put(wxyzCell, wxyzCell.getPotentialValues());
		result.put(wxyzCell, grid.getCellPotentialValues(wxyzCell.getIndex()));*/
        // z value (green)
		// All green if doubly linked
        BitSet zSet = SingletonBitSet.create(zValue);
		if (!doubleLink) {
			if (grid.hasCellPotentialValue(wzCell.getIndex(), zValue))
				result.put(wzCell, zSet);
			if (grid.hasCellPotentialValue(xzCell.getIndex(), zValue))
				result.put(xzCell, zSet);
			if (grid.hasCellPotentialValue(wxyzCell.getIndex(), zValue))
				result.put(wxyzCell, zSet);
			result.put(yzCell, zSet);
		}
		else {
				result.put(wzCell, grid.getCellPotentialValues(wzCell.getIndex()));			
				result.put(xzCell, grid.getCellPotentialValues(xzCell.getIndex()));	
				result.put(wxyzCell, grid.getCellPotentialValues(wxyzCell.getIndex()));	
				result.put(yzCell, grid.getCellPotentialValues(yzCell.getIndex()));
		}
		return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
/*        // Add x, y and z of WXYZ cell (orange)
        BitSet wxyz = new BitSet(10);
        wxyz.set(getX(grid));
        wxyz.set(getY(grid));
        wxyz.set(getZ(grid));
        result.put(wxyzCell, wxyz);*/
        return result;
    }
 
    public int getEliminationsTotal() {
		return eliminationsTotal;
	}
	
	public double getDifficulty() {
		//double-link has no impact on rating
        double result = 5.5; //base rating
		int sizeDif = (4 + 2) / 2; //Avarage of possible pilot cell size Max 4 Min 2 Ave 3
		result += ((4-sizeDif)- Math.abs(sizeDif - biggestCardinality)) * 0.1;//Extremeties of size are easier than middle size
		return result;//Min difficulty 5.5 //Max difficulty 5.6
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getSuffix() {
		return ((doubleLink ? 2 : 1)) + "" + biggestCardinality + "" + wingSize;
	}
	
	public String getName() {
        return "WXYZ-Wing " + getSuffix();
    }

    public String getShortName() {
		return "WXY" + getSuffix();
	}

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<Link>();
		if (grid.hasCellPotentialValue(wxyzCell.getIndex(), xValue)) {
			Link wLink = new Link(yzCell, xValue, wxyzCell, xValue);
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
		if (doubleLink) {
			if (grid.hasCellPotentialValue(wxyzCell.getIndex(), zValue)) {
				Link wLink = new Link(yzCell, zValue, wxyzCell, zValue);
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
		}
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
        if (this.wxyzCell != other.wxyzCell || this.zValue != other.zValue)
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
                    " on the values " + getX(grid) + ", " + getY(grid) + ", " + getZ(grid) + " and <b>" + zValue + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
 		if (!doubleLink)
			return getName() +
                ": " +
                Cell.toFullString(wxyzCell, wzCell, xzCell, yzCell) +
                " on value " +
                zValue;
		else
			return getName() +
                ": " +
                Cell.toFullString(wxyzCell, wzCell, xzCell, yzCell) +
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
			result = HtmlLoader.loadHtml(this, "WXYZWingHint.html");
		else
			result = HtmlLoader.loadHtml(this, "WXYZWing2Hint.html");
        String cell1 = wxyzCell.toString();
        String cell2 = wzCell.toString();
        String cell3 = xzCell.toString();
        String cell4 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell2, cell3, cell4, zValue, getX(grid), getY(grid), getZ(grid), xValue, biggestCardinality, wingSize, (doubleLink ? 2 : 1), sharedRegions());
        return result;
    }
}
