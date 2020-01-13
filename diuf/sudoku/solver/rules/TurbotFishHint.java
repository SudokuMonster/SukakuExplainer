package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * Turbot Crane hints
 */
public class TurbotFishHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final int value;
    private final Cell startCell;
    private final Cell endCell;
    private final Cell bridgeCell1;
    private final Cell bridgeCell2;
    private final Grid.Region baseSet;
    private final Grid.Region coverSet;
    private final Grid.Region shareRegion;
	private final Grid.Region ringRegion;
	private final boolean emptyRegion1;
	private final boolean emptyRegion2;
	//private final boolean r1EmptyRegionBlades;
	//private final boolean r2EmptyRegionBlades;
	private final Cell[] emptyRegionCells;
    private final int eliminationsTotal;

    public TurbotFishHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell startCell, Cell endCell, Cell bridgeCell1, Cell bridgeCell2,
            int value, Grid.Region base, Grid.Region cover, Grid.Region shareRegion, boolean emptyRegion1, boolean emptyRegion2, /*boolean r1EmptyRegionBlades, boolean r2EmptyRegionBlades,*/ Cell[] emptyRegionCells, int eliminationsTotal, Grid.Region ringRegion) {
        super(rule, removablePotentials);
        this.value = value;
        this.startCell = startCell;
        this.endCell = endCell;
        this.bridgeCell1 = bridgeCell1;
        this.bridgeCell2 = bridgeCell2;
        this.baseSet = base;
        this.coverSet = cover;
        this.shareRegion = shareRegion;
        this.ringRegion = ringRegion;
		this.emptyRegion1 = emptyRegion1;
		this.emptyRegion2 = emptyRegion2;
		//this.r1EmptyRegionBlades = r1EmptyRegionBlades;
		//this.r2EmptyRegionBlades = r2EmptyRegionBlades;
		this.emptyRegionCells = emptyRegionCells;
		this.eliminationsTotal = eliminationsTotal; 
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
		if (emptyRegion1 || emptyRegion2)
			return emptyRegionCells;
		else
			return new Cell[] { startCell, endCell };
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        BitSet fishDigitSet = SingletonBitSet.create(value);
    if (!emptyRegion1) {
		result.put(startCell, fishDigitSet); // orange
        result.put(bridgeCell1, fishDigitSet);
	}
	else if (Settings.getInstance().isDG() || (Settings.getInstance().isDG() && emptyRegion2))
		for (Cell cell : emptyRegionCells)
			result.put(cell, fishDigitSet); // orange
	if (!emptyRegion2) {
        result.put(bridgeCell2, fishDigitSet); // orange
        result.put(endCell, fishDigitSet);
	}
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        //BitSet fishDigitSet = SingletonBitSet.create(value);
        //result.put(startCell, fishDigitSet);
        //result.put(bridgeCell2, fishDigitSet);
        return result;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<>();
        result.add(new Link(startCell, value, bridgeCell1, value));
		//if (!emptyRegion1) {
			result.add(new Link(bridgeCell1, value, bridgeCell2, value));
			result.add(new Link(bridgeCell2, value, endCell, value));
		//}

        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] { baseSet, shareRegion, coverSet};
		        //return null;
    }

    @Override
    public String toString() {
        return getName() +
                ": " +
                Cell.toFullString(startCell, bridgeCell1, bridgeCell2, endCell) +
                " on value " +
                value;
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
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) || (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			result = HtmlLoader.loadHtml(this, "GroupedTCFishHint.html");
		else if (emptyRegion1 || emptyRegion2)
			result = HtmlLoader.loadHtml(this, "Grouped2LinksFishHint.html");
		else
			result = HtmlLoader.loadHtml(this, "TurbotFishHint.html");
        String name = getName();
        String base = this.baseSet.toFullString();
        String cover = this.coverSet.toFullString();
		String shared = this.shareRegion.toFullString();
        String value = Integer.toString(this.value);
        String cell1 = startCell.toString();
        String cell2 = bridgeCell1.toString();
        String cell3 = bridgeCell2.toString();
        String cell4 = endCell.toString();
        result = HtmlLoader.format(result, name, value, cell1, cell2, cell3, cell4, base, cover, shared, sharedRegions());
        return result;
    }
  
    static String hintNames[][][] = { //baseSetRegionTypeIndex, coverSetRegionTypeIndex, name/shortName
    			{ //baseSetRegionTypeIndex = 0 box
    				{ //coverSetRegionTypeIndex = 0 box
    					"Turbot Crane", "2SL"
    				},
    				{ //coverSetRegionTypeIndex = 1 row
    					"Turbot Crane", "TC"
    				},
    				{ //coverSetRegionTypeIndex = 2 column
    					"Turbot Crane", "TC"
    				}    				
    			},
    			{ //baseSetRegionTypeIndex = 1 row
    				{ //coverSetRegionTypeIndex = 0 box
    					"Turbot Crane", "TC"
    				},
    				{ //coverSetRegionTypeIndex = 1 row
    					"Skyscraper", "SS"
    				},
    				{ //coverSetRegionTypeIndex = 2 column
    					"Two-string Kite", "2SK"
    				}    				    				
    			},
    			{ //baseSetRegionTypeIndex = 2 column
    				{ //coverSetRegionTypeIndex = 0 box
    					"Turbot Crane", "TC"
    				},
    				{ //coverSetRegionTypeIndex = 1 row
    					"Two-string Kite", "2SK"
    				},
    				{ //coverSetRegionTypeIndex = 2 column
    					"Skyscraper", "SS"
    				}    								
    			}
	};

	public String getSuffix() {
		String SuffixNames[] = {"00", "01", "11"};
		return SuffixNames[(emptyRegion1 ? 1 : 0) + (emptyRegion2 ? 1 : 0)];
    }

    @Override
    public String getName() {
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) ^ (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			return "Grouped Turbot Crane" + (ringRegion == null ? "" : " X-Loop") + " " + getSuffix();
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) && (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			return "Grouped 2 strong links" + (ringRegion == null ? "" : " X-Loop") + " " + getSuffix();
		if ((emptyRegion1 || emptyRegion2) && baseSet.getRegionTypeIndex() ==  coverSet.getRegionTypeIndex())
			return "Grouped Skyscraper" +(ringRegion == null ? "" : " X-Loop") + " " + getSuffix();
		if (emptyRegion1 || emptyRegion2)
			return "Grouped 2-String Kite" + (ringRegion == null ? "" : " X-Loop") + " " + getSuffix();
		if (baseSet.getRegionTypeIndex() > 2 || coverSet.getRegionTypeIndex() > 2)
			return "Grouped 2 strong links" + (ringRegion == null ? "" : " X-Loop") + " " + getSuffix();
		return hintNames[baseSet.getRegionTypeIndex()][coverSet.getRegionTypeIndex()][0] + (ringRegion == null ? "" : " X-Loop");

    }	
	
    @Override
    public String getShortName() {
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) ^ (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			return "gTC" + (ringRegion == null ? "" : "r") + getSuffix();
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) && (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			return "g2SL" + (ringRegion == null ? "" : "r") + getSuffix();
		if ((emptyRegion1 || emptyRegion2) && baseSet.getRegionTypeIndex() ==  coverSet.getRegionTypeIndex())
			return "gSS" + (ringRegion == null ? "" : "r") + getSuffix();
		if (emptyRegion1 || emptyRegion2)
			return "g2SK" + (ringRegion == null ? "" : "r") + getSuffix();
		if (baseSet.getRegionTypeIndex() > 2 || coverSet.getRegionTypeIndex() > 2)
			return "g2SL" + (ringRegion == null ? "" : "r") + getSuffix();		
		return hintNames[baseSet.getRegionTypeIndex()][coverSet.getRegionTypeIndex()][1] + (ringRegion == null ? "" : "r");
    }

	public int getEliminationsTotal() {
		return eliminationsTotal;
	}


    static double difficulties[][] = //baseSetRegionTypeIndex, coverSetRegionTypeIndex

    	{
    			{ //baseSetRegionTypeIndex = 0 box
    				4.2, 4.2, 4.2 //coverSetRegionTypeIndex = box, row, column
    			},

    			{ //baseSetRegionTypeIndex = 1 row
    				4.2, 4.0, 4.1 //coverSetRegionTypeIndex = box, row, column
    			},

    			{ //baseSetRegionTypeIndex = 2 column
    				4.2, 4.1, 4.0 //coverSetRegionTypeIndex = box, row, column
    			}
		};

    @Override
    public double getDifficulty() {
		if (emptyRegion1 || emptyRegion2 || baseSet.getRegionTypeIndex() > 2 || coverSet.getRegionTypeIndex() > 2) 
			return 4.3;
    	return difficulties[baseSet.getRegionTypeIndex()][coverSet.getRegionTypeIndex()];
    }

    @Override
    public int hashCode() {
        return startCell.hashCode() ^ endCell.hashCode() ^
                bridgeCell1.hashCode() ^ bridgeCell2.hashCode() ^ value;
    }


    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() + " on the value " + value;
        } else {
            return "Look for a " + getName();
        }
    }



    @Override
    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<>();
        Cell startCell = Grid.getCell(this.startCell.getIndex());
        Cell endCell = Grid.getCell(this.endCell.getIndex());
        Cell bridgeCell1 = Grid.getCell(this.bridgeCell1.getIndex());
        Cell bridgeCell2 = Grid.getCell(this.bridgeCell2.getIndex());
        if (initialGrid.hasCellPotentialValue(startCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.startCell.getIndex(), value))
            result.add(new Potential(this.startCell, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell1.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell1.getIndex(), value))
            result.add(new Potential(this.bridgeCell1, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell2.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell2.getIndex(), value))
            result.add(new Potential(this.bridgeCell2, value, false));
        if (initialGrid.hasCellPotentialValue(endCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.endCell.getIndex(), value))
            result.add(new Potential(this.endCell, value, false));
        return result;
    }
}
