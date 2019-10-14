package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * Turbot Fish (with 3 Strong links) hints
 */
public class ThreeStrongLinksHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final int value;
	private final int baseLinkType1;
	private final int baseLinkType2;
	private final int baseLinkType3;
    private final Cell startCell;
    private final Cell bridgeCell11;
    private final Cell bridgeCell12;
    private final Cell bridgeCell21;
    private final Cell bridgeCell22;	
    private final Cell endCell;
    private final Grid.Region baseLink1Set;
    private final Grid.Region baseLink2Set;
    private final Grid.Region baseLink3Set;
    private final Grid.Region shareRegion1;
    private final Grid.Region shareRegion2;
	
    public ThreeStrongLinksHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell startCell, Cell bridgeCell11, Cell bridgeCell12,
            int value, Grid.Region baseLink1Set, Grid.Region baseLink2Set, Grid.Region shareRegion1, 
			Cell bridgeCell21, Cell bridgeCell22, Cell endCell, Grid.Region baseLink3Set, Grid.Region shareRegion2, int baseLinkType1, int baseLinkType2, int baseLinkType3) {
        super(rule, removablePotentials);
        this.value = value;
		this.baseLinkType1 = baseLinkType1;
		this.baseLinkType2 = baseLinkType2;
		this.baseLinkType3 = baseLinkType3;
        this.startCell = startCell;
        this.bridgeCell11 = bridgeCell11;
        this.bridgeCell12 = bridgeCell12;		
        this.bridgeCell21 = bridgeCell21;
        this.bridgeCell22 = bridgeCell22;			
        this.endCell = endCell;
        this.baseLink1Set = baseLink1Set;
        this.baseLink2Set = baseLink2Set;
        this.baseLink3Set = baseLink3Set;		
        this.shareRegion1 = shareRegion1;
        this.shareRegion2 = shareRegion2;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] { startCell, endCell };
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        BitSet fishDigitSet = SingletonBitSet.create(value);
        result.put(startCell, fishDigitSet); // orange
        result.put(bridgeCell11, fishDigitSet);
        result.put(bridgeCell12, fishDigitSet); // orange
        result.put(bridgeCell21, fishDigitSet);
        result.put(bridgeCell22, fishDigitSet); // orange
        result.put(endCell, fishDigitSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        BitSet fishDigitSet = SingletonBitSet.create(value);
        result.put(startCell, fishDigitSet);
        result.put(bridgeCell12, fishDigitSet);
        result.put(bridgeCell22, fishDigitSet);		
        return result;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<>();
        result.add(new Link(startCell, value, bridgeCell11, value));
		//result.add(new Link(bridgeCell11, value, bridgeCell12, value));
        result.add(new Link(bridgeCell12, value, bridgeCell21, value));
		//result.add(new Link(bridgeCell21, value, bridgeCell22, value));
		result.add(new Link(bridgeCell22, value, endCell, value));
        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
        //return new Grid.Region[] { shareRegion1 };
		        return null;
    }

    @Override
    public String toString() {
        return getName() +
                ": " +
                Cell.toFullString(startCell, bridgeCell11, bridgeCell12, bridgeCell21, bridgeCell22, endCell) +
                " on value " +
                value;
    }

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, "ThreeStrongLinksHint.html");
        String name = getName();
        String baseLink1Name = this.baseLink1Set.toFullString();
        String baseLink2Name = this.baseLink2Set.toFullString();
		String baseLink3Name = this.baseLink3Set.toFullString();
		String shared1 = this.shareRegion1.toFullString();
		String shared2 = this.shareRegion2.toFullString();
        String value = Integer.toString(this.value);
        String cell1 = startCell.toString();
        String cell2 = bridgeCell11.toString();
        String cell3 = bridgeCell12.toString();
        String cell4 = bridgeCell21.toString();
        String cell5 = bridgeCell22.toString();		
        String cell6 = endCell.toString();
        result = HtmlLoader.format(result, name, value, cell1, cell2, cell3, cell4, cell5,cell6, baseLink1Name, baseLink2Name, baseLink3Name, shared1, shared2);
        return result;
    }

	private String getSuffix() {
        String nameSuffix;
		if (baseLinkType1 > 0) {
			nameSuffix = "1";
		}
		else {
			nameSuffix = "0";
		}
		if ((baseLinkType2 > 0) && (baseLinkType1 == baseLinkType2 || nameSuffix == "0")) {
			nameSuffix +="1";
		}
		else {
			if (baseLinkType2 > 0) {
				nameSuffix +="2";
			}
			else {
				nameSuffix +="0";
			}
		}
		if ((baseLinkType3 > 0) && (nameSuffix.contains("00") || baseLinkType1 == baseLinkType3 || (baseLinkType2 != baseLinkType3 && nameSuffix.indexOf("2") == 1) || (baseLinkType2 == baseLinkType3 && nameSuffix.indexOf("1") == 1))) {
			 nameSuffix +="1";
		}
		else {
			if (baseLinkType3 > 0) {
				nameSuffix +="2";
			}
			else {
					nameSuffix +="0";
			}
		}
		if (nameSuffix.contains("122"))
			return "112";
		if (nameSuffix.contains("120"))
			return "012";		
		return nameSuffix;
    }


    @Override
    public String getName() {
        Class<? extends Grid.Region> region1 = baseLink1Set.getClass();
        Class<? extends Grid.Region> region2 = baseLink2Set.getClass();
		Class<? extends Grid.Region> region3 = baseLink3Set.getClass();
        String suffix = getSuffix();
        if (region1 == Grid.Row.class) {
            if (region2 == Grid.Row.class) {
				if (region3 == Grid.Row.class)	{			
					return "Skyscraper (with 3 Strong links)" + " " + suffix;
				}	
				else {
					if (region3 == Grid.Column.class) {
						return "Three-String Kite" + " " + suffix;
					}
					else {
						return "Turbot Fish (with 3 Strong links)" + " " + suffix;
					}
				}
			}
			else {
				if (region2 == Grid.Column.class)	{			
					return "Three-String Kite" + " " + suffix;
				}
				else {
					if (region3 == Grid.Column.class) {
						return "Three-String Kite" + " " + suffix;
					}
					else {
						return "Turbot Fish (with 3 Strong links)" + " " + suffix;
					}
				}
			}
		}
        if (region1 == Grid.Column.class) {
            if (region2 == Grid.Column.class) {
				if (region3 == Grid.Column.class)	{			
					return "Skyscraper (with 3 Strong links)" + " " + suffix;
				}	
				else {
					if (region3 == Grid.Row.class) {
						return "Three-String Kite" + " " + suffix;
					}
					else {
						return "Turbot Fish (with 3 Strong links)" + " " + suffix;
					}
				}
			}
			else {
				if (region2 == Grid.Row.class)	{			
					return "Three-String Kite" + " " + suffix;
				}
				else {
					if (region3 == Grid.Row.class) {
						return "Three-String Kite" + " " + suffix;
					}
					else {
						return "Turbot Fish (with 3 Strong links)" + " " + suffix;
					}
				}
			}
		}
		if (region1 == Grid.Block.class) {
			if (region2 == Grid.Row.class) {
				if (region3 == Grid.Column.class)	{			
					return "Three-String Kite" + " " + suffix;
				}	
				else {
					return "Turbot Fish (with 3 Strong links)" + " " + suffix;
				}
			}
			if (region2 == Grid.Column.class) {
				if (region3 == Grid.Row.class)	{			
					return "Three-String Kite" + " " + suffix;
				}	
				else {
					return "Turbot Fish (with 3 Strong links)" + " " + suffix;
				}
			}
			if (region2 == Grid.Block.class) {
				return "Turbot Fish (with 3 Strong links)" + " " + suffix;
			}
		}
		return "Turbot Fish (with 3 Strong links)" + " " + suffix;
    }	
	
    @Override
    public String getShortName() {
        Class<? extends Grid.Region> region1 = baseLink1Set.getClass();
        Class<? extends Grid.Region> region2 = baseLink2Set.getClass();
		Class<? extends Grid.Region> region3 = baseLink3Set.getClass();
        String suffix = getSuffix();
        if (region1 == Grid.Row.class) {
            if (region2 == Grid.Row.class) {
				if (region3 == Grid.Row.class)	{			
					return "3Sk" + " " + suffix;
				}	
				else {
					if (region3 == Grid.Column.class) {
						return "3SK" + " " + suffix;
					}
					else {
						return "3TF" + " " + suffix;
					}
				}
			}
			else {
				if (region2 == Grid.Column.class)	{			
					return "3SK" + " " + suffix;
				}
				else {
					if (region3 == Grid.Column.class) {
						return "3SK" + " " + suffix;
					}
					else {
						return "3TF" + " " + suffix;
					}
				}
			}
		}
        if (region1 == Grid.Column.class) {
            if (region2 == Grid.Column.class) {
				if (region3 == Grid.Column.class)	{			
					return "3Sk" + " " + suffix;
				}	
				else {
					if (region3 == Grid.Row.class) {
						return "3SK" + " " + suffix;
					}
					else {
						return "3TF" + " " + suffix;
					}
				}
			}
			else {
				if (region2 == Grid.Row.class)	{			
					return "3SK" + " " + suffix;
				}
				else {
					if (region3 == Grid.Row.class) {
						return "3SK" + " " + suffix;
					}
					else {
						return "3TF" + " " + suffix;
					}
				}
			}
		}
		if (region1 == Grid.Block.class) {
			if (region2 == Grid.Row.class) {
				if (region3 == Grid.Column.class)	{			
					return "3SK" + " " + suffix;
				}	
				else {
					return "3TF" + " " + suffix;
				}
			}
			if (region2 == Grid.Column.class) {
				if (region3 == Grid.Row.class)	{			
					return "3SK" + " " + suffix;
				}	
				else {
					return "3TF" + " " + suffix;
				}
			}
			if (region2 == Grid.Block.class) {
				return "3TF" + " " + suffix;
			}
		}
		return "3TF" + " " + suffix;
    }


    @Override
    public double getDifficulty() {
        String name = getName();
        if (name.contains("Skyscraper")) {
            return 5.4;
        } else if (name.contains("Three-String Kite")) {
            return 5.6;
        } else {
            return 5.5;
        }
    }

    @Override
    public int hashCode() {
        return startCell.hashCode() ^ endCell.hashCode() ^
                bridgeCell11.hashCode() ^ bridgeCell12.hashCode() ^ value;
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
        Cell bridgeCell11 = Grid.getCell(this.bridgeCell11.getIndex());
        Cell bridgeCell12 = Grid.getCell(this.bridgeCell12.getIndex());
        Cell bridgeCell21 = Grid.getCell(this.bridgeCell21.getIndex());
        Cell bridgeCell22 = Grid.getCell(this.bridgeCell22.getIndex());		
        if (initialGrid.hasCellPotentialValue(startCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.startCell.getIndex(), value))
            result.add(new Potential(this.startCell, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell11.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell11.getIndex(), value))
            result.add(new Potential(this.bridgeCell11, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell12.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell12.getIndex(), value))
            result.add(new Potential(this.bridgeCell12, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell21.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell21.getIndex(), value))
            result.add(new Potential(this.bridgeCell21, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell22.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell22.getIndex(), value))
            result.add(new Potential(this.bridgeCell22, value, false));
        if (initialGrid.hasCellPotentialValue(endCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.endCell.getIndex(), value))
            result.add(new Potential(this.endCell, value, false));
        return result;
    }
}
