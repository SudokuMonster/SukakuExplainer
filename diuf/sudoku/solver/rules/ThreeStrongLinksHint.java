package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * 3-Turbot Fish hints
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
	private final boolean emptyRegion1;
	private final boolean emptyRegion2;
	private final boolean emptyRegion3;
	private final Cell[] emptyRegionCells;
    private final int 	eliminationsTotal;
	private final boolean EmL34;
	
    public ThreeStrongLinksHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell startCell, Cell bridgeCell11, Cell bridgeCell12,
            int value, Grid.Region baseLink1Set, Grid.Region baseLink2Set, Grid.Region shareRegion1, 
			Cell bridgeCell21, Cell bridgeCell22, Cell endCell, Grid.Region baseLink3Set, Grid.Region shareRegion2, int baseLinkType1, int baseLinkType2, int baseLinkType3, boolean emptyRegion1, boolean emptyRegion2, boolean emptyRegion3, Cell[] emptyRegionCells, int eliminationsTotal, boolean EmL34) {
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
		this.emptyRegion1 = emptyRegion1;
		this.emptyRegion2 = emptyRegion2;
		this.emptyRegion3 = emptyRegion3;
		this.emptyRegionCells = emptyRegionCells;
		this.eliminationsTotal = eliminationsTotal;
		this.EmL34 = EmL34; 
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        if (emptyRegion1 || emptyRegion2 || emptyRegion3)
			return emptyRegionCells;
		else
			return new Cell[] { startCell, endCell };
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        BitSet fishDigitSet = SingletonBitSet.create(value);
		if (!emptyRegion1){
			result.put(startCell, fishDigitSet); // orange
			result.put(bridgeCell11, fishDigitSet);
		}
		if (!emptyRegion2){
			result.put(bridgeCell12, fishDigitSet); // orange      
			result.put(bridgeCell21, fishDigitSet);
		}
		if (!emptyRegion3){
			result.put(bridgeCell22, fishDigitSet); // orange
			result.put(endCell, fishDigitSet);
		}
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        //BitSet fishDigitSet = SingletonBitSet.create(value);
        //result.put(startCell, fishDigitSet);
        //result.put(bridgeCell12, fishDigitSet);
        //result.put(bridgeCell22, fishDigitSet);		
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
        return new Grid.Region[] { shareRegion1, shareRegion2};
		        //return null;
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
		String result;
		if (emptyRegion1 || emptyRegion2 || emptyRegion3)
			result = HtmlLoader.loadHtml(this, "Grouped3LinksFishHint.html");
		else
			result = HtmlLoader.loadHtml(this, "ThreeStrongLinksHint.html");
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

	static String hintNames[][][][] = {
		{
			{
				{"3 Box Strong Links", "3TF", "000"},
				{"3-Turbot Crane", "3TC", "001"},
				{"3-Turbot Crane", "3TC", "001"},
				{"", "","004"}
			},
			{
				{"3-Turbot Crane", "3TC", "010"},
				{"3-Turbot Crane", "3TC", "011"},
				{"3-Turbot Crane", "3TC", "012"},
				{"", "","014"}
			},
			{
				{"3-Turbot Crane", "3TC", "010"},
				{"3-Turbot Crane", "3TC", "012"},
				{"3-Turbot Crane", "3TC", "011"},
				{"", "","014"}
			},
			{
				{"", "","040"},
				{"", "","041"},
				{"", "","041"},
				{"", "","044"}
			}
		},
		{
			{
				{"3-Turbot Crane", "3TC", "001"},
				{"3-Turbot Crane", "3TC", "101"},
				{"3-Turbot Crane", "3TC", "102"},
				{"", "","104"}
			},
			{
				{"3-Turbot Crane", "3TC", "011"},
				{"3 Skyscrapers", "3Sky", "111"},
				{"3-String Kite", "3SK", "112"},
				{"", "","114"}
			},
			{
				{"3-Turbot Crane", "3TC", "012"},
				{"3-String Kite", "3SK", "121"},
				{"3-String Kite", "3SK", "112"},
				{"", "","124"}
			},
			{
				{"", "","041"},
				{"", "","141"},
				{"", "","142"},
				{"", "","144"}
			}
		},
		{
			{
				{"3-Turbot Crane", "3TC", "001"},
				{"3-Turbot Crane", "3TC", "102"},
				{"3-Turbot Crane", "3TC", "101"},
				{"", "","104"}
			},
			{
				{"3-Turbot Crane", "3TC", "012"},
				{"3-String Kite", "3SK", "112"},
				{"3-String Kite", "3SK", "121"},
				{"", "","124"}
			},
			{
				{"3-Turbot Crane", "3TC", "011"},
				{"3-String Kite", "3SK", "112"},
				{"3 Skyscrapers", "3Sky", "111"},
				{"", "","114"}
			},
			{
				{"", "","041"},
				{"", "","142"},
				{"", "","141"},
				{"", "","144"}
			}
		},
		{
			{
				{"", "","004"},
				{"", "","104"},
				{"", "","104"},
				{"", "","404"}
			},
			{
				{"", "","014"},
				{"", "","114"},
				{"", "","124"},
				{"", "","414"}
			},
			{
				{"", "","014"},
				{"", "","124"},
				{"", "","114"},
				{"", "","414"}
			},
			{
				{"", "","044"},
				{"", "","144"},
				{"", "","144"},
				{"", "","444"}
			}
		}
	};

	//The suffix is a string made of three numbers descibing the configuration of the 3 links
	//it aims to be min-lex to remain relatively constant even with isomorphism
	//0: Strong link in block 1: Strong Link in a line 2: Strong Link in a line (Different type to 1) 4: Empty rectangle
	public String getSuffix() {
		return  ((emptyRegion1 ? 1 : 0) + (emptyRegion2	? 1 : 0) + (emptyRegion3 ? 1 : 0)) + hintNames[baseLinkType1][baseLinkType2][baseLinkType3][2];
    }

	//if any ER the pattern is ER else if Mix of line types then is 3-String Kite else if any block then is 3-Turbot Fish else if 3 parallel lines the 3 Skyscrapers
    @Override
    public String getName() {
		if (emptyRegion1 || emptyRegion2 || emptyRegion3)
			return hintNames[baseLinkType1][baseLinkType2][baseLinkType3][0] + " " + getSuffix() + " (" + ((emptyRegion1 ? 1 : 0) + (emptyRegion2	? 1 : 0) + (emptyRegion3 ? 1 : 0)) + " grouped Strong links" + (EmL34 ? " including EmL c/d" : "") + ")";
		return hintNames[baseLinkType1][baseLinkType2][baseLinkType3][0] + " " + getSuffix();
    }	
	
    @Override
    public String getShortName() {
        if (emptyRegion1 || emptyRegion2 || emptyRegion3)
			return "g" + hintNames[baseLinkType1][baseLinkType2][baseLinkType3][1] + getSuffix();
		return hintNames[baseLinkType1][baseLinkType2][baseLinkType3][1]+ getSuffix();
    }

	//Will return Total number of eliminations by this hint
	public int getEliminationsTotal() {
		return eliminationsTotal;
	}
	
    @Override
    public double getDifficulty() {
		if (emptyRegion1 || emptyRegion2 || emptyRegion3)
			return 5.7;
        String name = getName();
		if (name.contains("Skyscraper")) {
            return 5.4;
        } else if (name.contains("3-String Kite")) {
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
