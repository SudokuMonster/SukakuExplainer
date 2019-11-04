package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * Turbot Fish hints
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
	private final boolean emptyRegion1;
	private final boolean emptyRegion2;
	//private final boolean r1EmptyRegionBlades;
	//private final boolean r2EmptyRegionBlades;
	private final Cell[] emptyRegionCells;
    private final int eliminationsTotal;

    public TurbotFishHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell startCell, Cell endCell, Cell bridgeCell1, Cell bridgeCell2,
            int value, Grid.Region base, Grid.Region cover, Grid.Region shareRegion, boolean emptyRegion1, boolean emptyRegion2, /*boolean r1EmptyRegionBlades, boolean r2EmptyRegionBlades,*/ Cell[] emptyRegionCells, int eliminationsTotal) {
        super(rule, removablePotentials);
        this.value = value;
        this.startCell = startCell;
        this.endCell = endCell;
        this.bridgeCell1 = bridgeCell1;
        this.bridgeCell2 = bridgeCell2;
        this.baseSet = base;
        this.coverSet = cover;
        this.shareRegion = shareRegion;
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

    @Override
    public String toHtml(Grid grid) {
        String result;
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) || (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			result = HtmlLoader.loadHtml(this, "ERFishHint.html");
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
        result = HtmlLoader.format(result, name, value, cell1, cell2, cell3, cell4, base, cover, shared);
        return result;
    }
  
    static String hintNames[][][] = { //baseSetRegionTypeIndex, coverSetRegionTypeIndex, name/shortName
    			{ //baseSetRegionTypeIndex = 0 box
    				{ //coverSetRegionTypeIndex = 0 box
    					"Turbot Fish", "GXW"
    				},
    				{ //coverSetRegionTypeIndex = 1 row
    					"Turbot Fish", "TF"
    				},
    				{ //coverSetRegionTypeIndex = 2 column
    					"Turbot Fish", "TF"
    				}    				
    			},
    			{ //baseSetRegionTypeIndex = 1 row
    				{ //coverSetRegionTypeIndex = 0 box
    					"Turbot Fish", "TF"
    				},
    				{ //coverSetRegionTypeIndex = 1 row
    					"Skyscraper", "Sky"
    				},
    				{ //coverSetRegionTypeIndex = 2 column
    					"Two-string Kite", "2SK"
    				}    				    				
    			},
    			{ //baseSetRegionTypeIndex = 2 column
    				{ //coverSetRegionTypeIndex = 0 box
    					"Turbot Fish", "TF"
    				},
    				{ //coverSetRegionTypeIndex = 1 row
    					"Two-string Kite", "2SK"
    				},
    				{ //coverSetRegionTypeIndex = 2 column
    					"Skyscraper", "Sky"
    				}    								
    			}
	};

	public String getSuffix() {
		String SuffixNames[] = {"00", "01", "11"};
		return SuffixNames[(emptyRegion1 ? 1 : 0) + (emptyRegion2 ? 1 : 0)];
    }

    @Override
    public String getName() {
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) || (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			return "Empty Rectangle" + " " + getSuffix();
		if ((emptyRegion1 || emptyRegion2) && baseSet.getRegionTypeIndex() ==  coverSet.getRegionTypeIndex())
			return "Grouped Skyscraper" + " " + getSuffix();
		if (emptyRegion1 || emptyRegion2)
			return "Grouped 2-String Kite" + " " + getSuffix();
		return hintNames[baseSet.getRegionTypeIndex()][coverSet.getRegionTypeIndex()][0];

    }	
	
    @Override
    public String getShortName() {
		if ((emptyRegion1 && baseSet.getRegionTypeIndex() == 0) || (emptyRegion2 && coverSet.getRegionTypeIndex() == 0))
			return "ER" + getSuffix();
		if ((emptyRegion1 || emptyRegion2) && baseSet.getRegionTypeIndex() ==  coverSet.getRegionTypeIndex())
			return "gSky" + getSuffix();
		if (emptyRegion1 || emptyRegion2)
			return "g2SK" + getSuffix();		
		return hintNames[baseSet.getRegionTypeIndex()][coverSet.getRegionTypeIndex()][1];
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
		if (emptyRegion1 || emptyRegion2)
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
