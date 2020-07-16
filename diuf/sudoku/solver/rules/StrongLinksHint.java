package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * Strong Links hints
 */
public class StrongLinksHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final int value;
	private final Cell startCell;
	private final Cell endCell;
    private final Cell[] emptyCells;
    private final int 	eliminationsTotal;
	private final Grid.Region[] baseLinkRegion;
	private final Grid.Region[] shareRegion;
	private final Cell[] bridge1;
	private final Cell[] bridge2;
	private final int[] q;
	private final int[] linkSet;
	private final boolean[] baseLinkEmptyRegion;
	private final int linksNumber;
	private final Grid.Region ringRegion;
	
	
    public StrongLinksHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell startCell, int value, Cell endCell, Cell[] emptyRegionCells, int eliminationsTotal, Grid.Region[] baseLinkRegion, Grid.Region[] shareRegion, Cell[] bridge1, Cell[] bridge2, int[] q, int[] linkSet, boolean[] baseLinkEmptyRegion, Grid.Region ringRegion) {
        super(rule, removablePotentials);
        this.value = value;
		this.startCell = startCell;
        this.endCell = endCell;
        this.emptyCells = emptyRegionCells.clone();
		this.eliminationsTotal = eliminationsTotal;
		this.baseLinkRegion = baseLinkRegion.clone();
		this.shareRegion = shareRegion.clone();
		this.bridge1 = bridge1.clone();
		this.bridge2 = bridge2.clone();
		this.q = q.clone();
		this.linkSet = linkSet.clone();
		this.baseLinkEmptyRegion = baseLinkEmptyRegion.clone();
		this.linksNumber = linkSet.length;
		this.ringRegion = ringRegion;
    }

	private boolean isLex(int[] intSet) {
		String intSetO = "";
		String intSetR = "";
		for (int i = 0; i < intSet.length; i++){
			intSetO += Integer.toString(intSet[i]);
			intSetR += Integer.toString(intSet[intSet.length - 1 - i]);
		}
		if (intSetO.compareTo(intSetR) > 0)
			return false;
		return true;
	}
	
	private int[] reverseIntegerArray(int[] Set) {
		int[] reversedArray = new int[Set.length];
		int[] currentArray = Set.clone();
		for (int i = 0; i < Set.length; i++)
			reversedArray [Set.length - i - 1] = currentArray[i];
		return reversedArray.clone();
	}

	private String rLineName(int[] Set) {
		String OriginalLinesNames = "";
		String LinesName = "";
		String ReverseSwap = "";
		String CurrentSwap = "";
		boolean isThereOne = false;
		for (int i = 0; i < Set.length; i++){
			if (Set[i] == 1) {
				isThereOne = true;
				ReverseSwap = "2" + ReverseSwap;
				CurrentSwap += "2";
			}
			OriginalLinesNames += Integer.toString(Set[i]);
			if (Set[i] == 2) {
				LinesName += "1";
				ReverseSwap = "1" + ReverseSwap;
				CurrentSwap += "1";
			}
			else {
				LinesName += Integer.toString(Set[i]);
				if (Set[i] < 1 || Set[i] > 2){
					ReverseSwap = Integer.toString(Set[i]) + ReverseSwap;
					CurrentSwap += Integer.toString(Set[i]);
				}
			}
		}
		if (ReverseSwap.compareTo(CurrentSwap) > 0)
			ReverseSwap = CurrentSwap;
		if (isThereOne)
			return (OriginalLinesNames.compareTo(ReverseSwap) > 0 ? ReverseSwap : OriginalLinesNames);
		return LinesName;
	}

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        for (int i = 0; i < linksNumber; i++)
			if (baseLinkEmptyRegion[i])
				return this.emptyCells;
		return new Cell[] {startCell, endCell};
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        BitSet fishDigitSet = SingletonBitSet.create(value);
		if (!baseLinkEmptyRegion[q[0]]) {
			result.put(startCell, fishDigitSet); // orange
			result.put(bridge1[0], fishDigitSet);
		}
		for (int i = 1; i < (linksNumber - 1); i++)
			if (!baseLinkEmptyRegion[q[i]]) {
				result.put(bridge2[i - 1], fishDigitSet); // orange
				result.put(bridge1[i], fishDigitSet);				
			}
		if (!baseLinkEmptyRegion[q[linksNumber - 1]]){
			result.put(bridge2[linksNumber - 2], fishDigitSet); // orange
			result.put(endCell, fishDigitSet);
		}
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        return result;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<>();
        result.add(new Link(startCell, value, bridge1[0], value));
		for (int i = 1; i < (linksNumber - 1); i++)
			result.add(new Link(bridge2[i - 1], value, bridge1[i], value));
		result.add(new Link(bridge2[linksNumber - 2], value, endCell, value));
        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
        Grid.Region[] finalRegions = new Grid.Region[linksNumber * 2];
		if (ringRegion == null)
			return shareRegion;
		else
			for (int i = 0; i < linksNumber; i++) {
				finalRegions[i * 2] = baseLinkRegion[i];
				if (i < (linksNumber -1))
					finalRegions[i * 2 + 1] = shareRegion[i];
				else
					finalRegions[i * 2 + 1] = ringRegion;
			}
		return finalRegions;
    }

    @Override
    public String toString() {
        String result = "";
		result = result + getName() + ": " + Cell.toFullString(startCell) + ",";
		for (int i = 0; i < (linksNumber - 1); i++)
                result += Cell.toString(bridge1[i], bridge2[i]) + ",";
		result += Cell.toString(endCell) + " on value " + value;
		return result;
    }

    private String shortBaseCover (Grid.Region [] sequence){
			String finalSequence = "";
			for (int i = 0; i < sequence.length; i++)
				finalSequence +=  sequence[i].toFullStringShort();
			return finalSequence;
	}
	
	private String fishRC() {
		return shortBaseCover(baseLinkRegion) + "\\" + shortBaseCover(shareRegion) + ringRegion.toFullStringShort();
	}
	
	private String fishShape() {
		String conf = fishRC();
		int halfLength = conf.length()/2;
		if (conf.indexOf("r") >= 0)
			if (
				(conf.indexOf("r") <= halfLength && conf.lastIndexOf("r") > halfLength) ||
				(conf.lastIndexOf("r") > halfLength && conf.lastIndexOf("c") > halfLength) ||
				(conf.indexOf("r") <= halfLength && conf.indexOf("c") <= halfLength && conf.indexOf("c") >= 0)
				)
				return "Mutant";
		if (conf.indexOf("c") >= 0)
			if (conf.indexOf("c") <= halfLength && conf.lastIndexOf("c") > halfLength)
				return "Mutant";
		if (conf.indexOf("p") >= 0)
				return "Mutant";
		if (conf.indexOf("w") >= 0)
				return "Mutant";
		if (conf.indexOf("d") >= 0)
				return "Mutant";
		if (conf.indexOf("g") >= 0)
				return "Mutant";
		if (conf.indexOf("a") >= 0)
				return "Mutant";
		if (conf.indexOf(".") >= 0)
				return "Mutant";
		if (conf.indexOf("b") >= 0)
				return "Franken";
		return "Basic";
	}
	
	@Override
    public String toHtml(Grid grid) {
		String result;
		String fishName = getFishName(linksNumber);
		String loopRegion = "";
		if (ringRegion != null) {
			result = HtmlLoader.loadHtml(this, "GroupedStrongLinksLoopHint.html");
			fishName = fishShape() + " " + getFishName(linksNumber) + " " + fishRC();
			loopRegion = ringRegion.toFullString();
		}
		else if (groupedLinks() >  0) {
			result = HtmlLoader.loadHtml(this, "GroupedStrongLinksHint.html");
			fishName = "Finned " + fishName;
		}
		else {
			result = HtmlLoader.loadHtml(this, "StrongLinksHint.html");
			fishName = "1 Finned Sashimi " + fishName;
			for (int i = 1; i < linksNumber; i++)
				fishName = "2x" + fishName;
		}
        String name = getName();
        String firstLinkName = this.baseLinkRegion[q[0]].toFullString();
		String lastLinkName = this.baseLinkRegion[q[linksNumber - 1]].toFullString();
		String middleLinksName = "";
		for (int i = 1; i < (linksNumber - 1); i++)
			middleLinksName +=  ", " + this.baseLinkRegion[q[i]].toFullString();
        String firstShared = this.shareRegion[0].toFullString();
		String lastShared = this.shareRegion[linksNumber - 2].toFullString();
		String middleShared = "";
		for (int i = 1; i < (linksNumber - 2); i++)
			middleShared += ", " +this.shareRegion[i].toFullString();		
		String numberOfStrongLinks = Integer.toString(linksNumber);
		String numberOfWeakLinks = Integer.toString(linksNumber - 1);
		String value = Integer.toString(this.value);
        String cell1 = startCell.toString();
        String cell2 = endCell.toString(); 
        result = HtmlLoader.format(result, name, value, firstLinkName + (middleLinksName == "" ? "": middleLinksName), lastLinkName, numberOfStrongLinks, numberOfWeakLinks, firstShared + (middleShared == "" ? "": middleShared), lastShared, cell1, cell2, fishName, firstLinkName, firstShared, loopRegion);
        return result;
    }

	static double baseRatings[] = {0,4.0,5.4,5.8,6.2,6.6,7.0,7.4};
	
	private String getFishName(int fishSize) {
		String[] fishNames = new String[] {"Cyclopsfish", "X-Wing", "Swordfish", "Jellyfish", "Starfish", "Whale", "Leviathan", "Octupus", "Enniafish"};
		return  fishNames[fishSize - 1];
    }	
	
	private int groupedLinks() {
		int groupedLinksNumber = 0;
		for (int i = 0; i < (linksNumber); i++)
			if (baseLinkEmptyRegion[i])
				groupedLinksNumber++;
		return groupedLinksNumber;
	}
		
	//The suffix is a string made of three numbers descibing the configuration of the 3 links
	//it aims to be min-lex to remain relatively constant even with isomorphism
	//0: Strong link in block 1: Strong Link in a line 2: Strong Link in a line (Different type to 1) 4: Empty rectangle
	public String getSuffix() {
		int[] setActual = new int[linksNumber];
		for (int i = 0; i < (linksNumber); i++)
			setActual[i] = linkSet[q[i]];
		String Suffix = "";
		Suffix += groupedLinks();
		if (isLex(setActual))
			Suffix += rLineName(setActual);
		else
			Suffix += rLineName(reverseIntegerArray(setActual));
		return  Suffix;
    }

	//if any ER the pattern is ER else if Mix of line types then is 3-String Kite else if any block then is 3-Turbot Fish else if 3 parallel lines the 3 Skyscrapers
    @Override
    public String getName() {
		String Suffix = getSuffix();
		String Name = "" + linksNumber;
		int gL = groupedLinks();
		if (ringRegion != null){
			Name = "(" + Name + " Strong Links) " + (gL > 0 ? "Grouped " : "") + "X-Loop";
		}
		else if (Suffix.indexOf("0",1) < 0 && Suffix.indexOf("2",1) < 0)
			if (linksNumber < 3)
				Name = "Skyscraper";
			else
				Name += " Skyscrapers";	
		else if (Suffix.indexOf("0",1) < 0 && linksNumber <4)
			Name += "-String Kite";
		else
			Name += " Strong links";
		Name = (gL > 0  && ringRegion == null ? "Grouped " : "") + Name + " " + Suffix;
		return  Name;
    }	
	
    @Override
    public String getShortName() {
		String Suffix = getSuffix();
		String Name = "" + linksNumber;
		int gL = groupedLinks();
		if (ringRegion != null)
			Name += "XL";
		else if (Suffix.indexOf("0",1) < 0 && Suffix.indexOf("2",1) < 0)
			if (linksNumber < 3)
				Name = "SS";
			else
				Name += "SS";	
		else if (Suffix.indexOf("0",1) >= 0 || linksNumber > 3)
			Name += "SL";
		else
			Name += "SK";
			
		Name = (gL > 0 ? "g" : "") + Name + Suffix;
		return  Name;
    }

	//Will return Total number of eliminations by this hint
	public int getEliminationsTotal() {
		return eliminationsTotal;
	}
	
    @Override
    public double getDifficulty() {
        String Suffix = getSuffix();
		if (groupedLinks() >  0 || Suffix.indexOf("3",1) > 0 || Suffix.indexOf("4",1) > 0 || Suffix.indexOf("5",1) > 0 || Suffix.indexOf("6",1) > 0 || Suffix.indexOf("7",1) > 0 || Suffix.indexOf("8",1) > 0 || Suffix.indexOf("9",1) > 0)
			return baseRatings[linksNumber - 1] + 0.3;
		if (Suffix.indexOf("0",1) < 0 && Suffix.indexOf("2",1) < 0) {
            return baseRatings[linksNumber - 1];
        } else if (Suffix.indexOf("0",1) > 0 && Suffix.indexOf("2",1) > 0) {
            return baseRatings[linksNumber - 1] + 0.2;
        } else {
            return baseRatings[linksNumber - 1] + 0.1;
        }
    }

    @Override
    public int hashCode() {
        return startCell.hashCode() ^ endCell.hashCode() ^ value;
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
        if (initialGrid.hasCellPotentialValue(startCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.startCell.getIndex(), value))
            result.add(new Potential(this.startCell, value, false));
        if (initialGrid.hasCellPotentialValue(endCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.endCell.getIndex(), value))
            result.add(new Potential(this.endCell, value, false));
        return result;
    }
}
