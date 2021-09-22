package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Implementation of Strong Links technique solver.
 */
public class StrongLinks implements IndirectHintProducer {

    private final int degree;


    public StrongLinks(int degree) {
        this.degree = degree;
    }

    @Override	
	public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
		int[] variantsArray = new int[10];
		int j = 0;
		if (Settings.getInstance().isBlocks())
			variantsArray[j++] = 0;
		variantsArray[j++] = 1;
		variantsArray[j++] = 2;
		if (!Settings.getInstance().isVLatin()){
			if (Settings.getInstance().isDG())
				variantsArray[j++] = 3;
			if (Settings.getInstance().isWindows())
				variantsArray[j++] = 4;
			if (Settings.getInstance().isX()){
				variantsArray[j++] = 5;
				variantsArray[j++] = 6;
			}
			if (Settings.getInstance().isGirandola())
				variantsArray[j++] = 7;
			if (Settings.getInstance().isAsterisk())
				variantsArray[j++] = 8;
			if (Settings.getInstance().isCD())
				variantsArray[j++] = 9;
		}
		List<StrongLinksHint> hintsFinal = new ArrayList<StrongLinksHint>();
		List<StrongLinksHint> hintsStart;
		Permutations perm = new Permutations(degree, j * degree);
        while (perm.hasNext()) {
            int[] indexes = perm.nextBitNums();
            assert indexes.length == degree;
			int[] Set = new int[degree];
			int i = 0;
			for (i = 0; i < degree; i++)
				if (indexes[i] % degree != i)
					break;
				else
					Set[i] = variantsArray[indexes[i] / degree] ;
			if (i < degree)
				continue;
			hintsStart = getHints(grid, Set);
			for (StrongLinksHint hint : hintsStart)
				hintsFinal.add(hint);
		}			
		// Sort the result
		Collections.sort(hintsFinal, new Comparator<StrongLinksHint>() {
			public int compare(StrongLinksHint h1, StrongLinksHint h2) {
				double d1 = h1.getDifficulty();
				double d2 = h2.getDifficulty();
				int e1 = h1.getEliminationsTotal();
				int e2 = h2.getEliminationsTotal();
				String s1 = h1.getSuffix();
				String s2 = h2.getSuffix();
				//sort according to difficulty in ascending order
				if (d1 < d2)
					return -1;
				else if (d1 > d2)
					return 1;
				//sort according to number of eliminations in descending order
				if ((e2 - e1) != 0)
					return e2 - e1;
				//sort according to suffix in lexographic order
				return s1.compareTo(s2);
			}
		});
		for (StrongLinksHint hint : hintsFinal)
			accu.add(hint);
    }

    private Grid.Region shareRegionOf(Grid grid,
            Cell bridge1, Cell bridge1Support, Cell bridge2, Cell bridge2Support, Cell bridge1Support2, Cell bridge2Support2) {
		boolean sameRegionCounter = true;
		Cell bridge1True = (bridge1 != null ? bridge1 : bridge1Support);
		Cell bridge2True = (bridge2 != null ? bridge2 : bridge2Support);
		
		if (sameRegionCounter = bridge1True.getX() == bridge2True.getX()) {
			if (bridge1Support != null && sameRegionCounter) {
				sameRegionCounter = bridge1Support.getX() == bridge1True.getX();
				if (bridge1Support2 != null && sameRegionCounter)
					sameRegionCounter = bridge1Support2.getX() == bridge1True.getX();
			}
			if (bridge2Support != null && sameRegionCounter) {
				sameRegionCounter = bridge2Support.getX() == bridge1True.getX();
				if (bridge2Support2 != null && sameRegionCounter)
					sameRegionCounter = bridge2Support2.getX() == bridge1True.getX();
			}
			if (sameRegionCounter)
				return (Grid.Column)Grid.getRegionAt(2,bridge1True.getIndex());
		}
		if (sameRegionCounter = bridge1True.getY() == bridge2True.getY()) {
			if (bridge1Support != null && sameRegionCounter) {
				sameRegionCounter = bridge1Support.getY() == bridge1True.getY();
				if (bridge1Support2 != null && sameRegionCounter)
					sameRegionCounter = bridge1Support2.getY() == bridge1True.getY();
			}
			if (bridge2Support != null && sameRegionCounter) {
				sameRegionCounter = bridge2Support.getY() == bridge1True.getY();
				if (bridge2Support2 != null && sameRegionCounter)
					sameRegionCounter = bridge2Support2.getY() == bridge1True.getY();
			}
			if (sameRegionCounter)
				return (Grid.Row)Grid.getRegionAt(1,bridge1True.getIndex());
		}
		if (Settings.getInstance().isBlocks())
			if (sameRegionCounter = bridge1True.getB() == bridge2True.getB()) {
				if (bridge1Support != null && sameRegionCounter) {
					sameRegionCounter = bridge1Support.getB() == bridge1True.getB();
					if (bridge1Support2 != null && sameRegionCounter)
						sameRegionCounter = bridge1Support2.getB() == bridge1True.getB();
				}
				if (bridge2Support != null && sameRegionCounter) {
					sameRegionCounter = bridge2Support.getB() == bridge1True.getB();
					if (bridge2Support2 != null && sameRegionCounter)
						sameRegionCounter = bridge2Support2.getB() == bridge1True.getB();
				}
				if (sameRegionCounter)
					return (Grid.Block)Grid.getRegionAt(0,bridge1True.getIndex());
			}
		if (!Settings.getInstance().isVLatin()){
			if (Settings.getInstance().isDG())
				if (sameRegionCounter = bridge1True.getD() == bridge2True.getD()) {
					if (bridge1Support != null && sameRegionCounter) {
						sameRegionCounter = bridge1Support.getD() == bridge1True.getD();
						if (bridge1Support2 != null && sameRegionCounter)
							sameRegionCounter = bridge1Support2.getD() == bridge1True.getD();
					}
					if (bridge2Support != null && sameRegionCounter) {
						sameRegionCounter = bridge2Support.getD() == bridge1True.getD();
						if (bridge2Support2 != null && sameRegionCounter)
							sameRegionCounter = bridge2Support2.getD() == bridge1True.getD();
					}
					if (sameRegionCounter)
						return (Grid.DG)Grid.getRegionAt(3,bridge1True.getIndex());
				}
			if (Settings.getInstance().isWindows())
				if (sameRegionCounter = bridge1True.getW() == bridge2True.getW()) {
					if (bridge1Support != null && sameRegionCounter) {
						sameRegionCounter = bridge1Support.getW() == bridge1True.getW();
						if (bridge1Support2 != null && sameRegionCounter)
							sameRegionCounter = bridge1Support2.getW() == bridge1True.getW();
					}
					if (bridge2Support != null && sameRegionCounter) {
						sameRegionCounter = bridge2Support.getW() == bridge1True.getW();
						if (bridge2Support2 != null && sameRegionCounter)
							sameRegionCounter = bridge2Support2.getW() == bridge1True.getW();
					}
					if (sameRegionCounter)
						return (Grid.Window)Grid.getRegionAt(4,bridge1True.getIndex());
				}
			if (Settings.getInstance().isGirandola())
				if (sameRegionCounter = ((bridge1True.getG() * bridge2True.getG()) == 1)) {
					if (bridge1Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge1Support.getG() * bridge1True.getG()) == 1;
						if (bridge1Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge1Support2.getG() * bridge1True.getG()) == 1;
					}
					if (bridge2Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge2Support.getG() * bridge1True.getG()) == 1;
						if (bridge2Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge2Support2.getG() * bridge1True.getG()) == 1;
					}
					if (sameRegionCounter)
						return (Grid.Girandola)Grid.getRegionAt(7,bridge1True.getIndex());
				}
			if (Settings.getInstance().isAsterisk())
				if (sameRegionCounter = ((bridge1True.getA() * bridge2True.getA()) == 1)) {
					if (bridge1Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge1Support.getA() * bridge1True.getA()) == 1;
						if (bridge1Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge1Support2.getA() * bridge1True.getA()) == 1;
					}
					if (bridge2Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge2Support.getA() * bridge1True.getA()) == 1;
						if (bridge2Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge2Support2.getA() * bridge1True.getA()) == 1;
					}
					if (sameRegionCounter)
						return (Grid.Asterisk)Grid.getRegionAt(8,bridge1True.getIndex());
				}
			if (Settings.getInstance().isCD())
				if (sameRegionCounter = ((bridge1True.getCD() * bridge2True.getCD()) == 1)) {
					if (bridge1Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge1Support.getCD() * bridge1True.getCD()) == 1;
						if (bridge1Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge1Support2.getCD() * bridge1True.getCD()) == 1;
					}
					if (bridge2Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge2Support.getCD() * bridge1True.getCD()) == 1;
						if (bridge2Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge2Support2.getCD() * bridge1True.getCD()) == 1;
					}
					if (sameRegionCounter)
						return (Grid.CD)Grid.getRegionAt(9,bridge1True.getIndex());
				}
			if (Settings.getInstance().isX()){
				if (sameRegionCounter = ((bridge1True.getMD() * bridge2True.getMD()) == 1)) {
					if (bridge1Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge1Support.getMD() * bridge1True.getMD()) == 1;
						if (bridge1Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge1Support2.getMD() * bridge1True.getMD()) == 1;
					}
					if (bridge2Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge2Support.getMD() * bridge1True.getMD()) == 1;
						if (bridge2Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge2Support2.getMD() * bridge1True.getMD()) == 1;
					}
					if (sameRegionCounter)
						return (Grid.diagonalMain)Grid.getRegionAt(5,bridge1True.getIndex());
				}
				if (sameRegionCounter = ((bridge1True.getAD() * bridge2True.getAD()) == 1)) {
					if (bridge1Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge1Support.getAD() * bridge1True.getAD()) == 1;
						if (bridge1Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge1Support2.getAD() * bridge1True.getAD()) == 1;
					}
					if (bridge2Support != null && sameRegionCounter) {
						sameRegionCounter = (bridge2Support.getAD() * bridge1True.getAD()) == 1;
						if (bridge2Support2 != null && sameRegionCounter)
							sameRegionCounter = (bridge2Support2.getAD() * bridge1True.getAD()) == 1;
					}
					if (sameRegionCounter)
						return (Grid.diagonalAnti)Grid.getRegionAt(6,bridge1True.getIndex());
				}			
			}
		}
		return null;
    }

	private boolean isSameLine (Cell lineCell1, Cell lineCell2) {
		if (lineCell1.getX() == lineCell2.getX() || lineCell1.getY() == lineCell2.getY())
			return true;
		return false;
	}

	private boolean isRegionMinLex(Grid.Region [] sequence, int [] order){
			int[] origin =  new int[sequence.length];
			int[] temp =  new int[sequence.length];
			for (int i = 0; i < sequence.length; i++){
				temp[i] = sequence[i].toFullNumber();
				origin[i] =  sequence[i].toFullNumber();
			}
			Arrays.sort(temp);
			return origin[order[0]] == temp [0];
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

	private void buildLinks(Grid grid, int digit, int[] linkSet, int linksNumber, int linksDepth, Grid.Region[][] baseLinkRegions, int x, Cell[] cells, Cell[] baseRegionsCells, Cell[] emptyRegionsCells, Grid.Region[] baseLinkRegion, boolean[] baseLinkEmptyRegion, int[] e, List<StrongLinksHint> result) {
		int p1, p2;
		p1 = p2 = 0;
		if (linksDepth < linksNumber)
			for (int i = (linksDepth > 0 && linkSet[linksDepth - 1] == linkSet[linksDepth] ? x + 1 : 0); i < baseLinkRegions[linksDepth].length; i++){
				//find potential link
				//Cell[] cells = new Cell[12];
				Cell[] heartCells= new Cell[3];
				baseLinkEmptyRegion[linksDepth] = false;
				//boolean baseLinkEmptyRegionBlades = false;
				baseLinkRegion[linksDepth] = baseLinkRegions[linksDepth][i];
				BitSet baseLinkRegionPotentials = baseLinkRegion[linksDepth].getPotentialPositions(grid, digit);
				int baseLinkRegionPotentialsC = baseLinkRegionPotentials.cardinality();
				e[linksDepth] = 0;
				if (baseLinkRegionPotentialsC > 1){
					if (baseLinkRegionPotentialsC > 6)
						continue;
					BitSet baseLinkBlade1 = (BitSet)baseLinkRegionPotentials.clone();
					BitSet baseLinkBlade2 = (BitSet)baseLinkRegionPotentials.clone();
						//@SudokuMonster #111 bug fixattempt
						BitSet beatingHeart = (BitSet)baseLinkRegionPotentials.clone();
					
					if (baseLinkRegionPotentialsC > 2) {
						//Grouped Strong links in box have 15 configurations but only 9 are ER 
						for (e[linksDepth] = 0; e[linksDepth] < (linkSet[linksDepth] < 1 ? 15 : 3); e[linksDepth]++) {
							//there are equivalent strong links in box if e[linksDepth] > 9 and baseLinkRegionPotentialsC < 4
							if (e[linksDepth] > 8 && baseLinkRegionPotentialsC < 4)
								continue;
							//baseLinkEmptyRegion[linksDepth] = false;
							heartCells[0] = ((linkSet[linksDepth] == 0 || linkSet[linksDepth] == 3 || linkSet[linksDepth] == 4) && e[linksDepth] < 9 ? baseLinkRegion[linksDepth].getCell(e[linksDepth]): null);
							BitSet baseLinkEmptyArea = (BitSet)baseLinkRegionPotentials.clone();
							baseLinkBlade1 = (BitSet)baseLinkRegionPotentials.clone();
							baseLinkBlade2 = (BitSet)baseLinkRegionPotentials.clone();
								//@SudokuMonster #111 bug fixattempt
								beatingHeart = (BitSet)baseLinkRegionPotentials.clone();
							if (linkSet[linksDepth] == 0 || linkSet[linksDepth] == 3 || linkSet[linksDepth] == 4) {
								//confirm if we have an empty rectangle
								//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
								//9 configurations for each block depending on "Heart" cell
								baseLinkEmptyArea.and(baseLinkRegion[linksDepth].Rectangle(e[linksDepth]));
							}
							else {
								baseLinkEmptyArea.and(baseLinkRegion[linksDepth].lineEmptyCells(e[linksDepth]));
							}
							if (baseLinkEmptyArea.cardinality() == 0) {
								if (linkSet[linksDepth] == 0 || linkSet[linksDepth] == 3 || linkSet[linksDepth] == 4) {
									//confirm if we have an empty rectangle
									//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
									//9 configurations for each block depending on "Heart" cell
									baseLinkBlade1.and(baseLinkRegion[linksDepth].crossBlade1(e[linksDepth]));
									baseLinkBlade2.and(baseLinkRegion[linksDepth].crossBlade2(e[linksDepth]));
										//@SudokuMonster #111 bug fixattempt
										beatingHeart.and(baseLinkRegion[linksDepth].crossHeart(e[linksDepth]));
								}
								else {
									baseLinkBlade1.and(baseLinkRegion[linksDepth].lineBlade1(e[linksDepth]));
									baseLinkBlade2.and(baseLinkRegion[linksDepth].lineBlade2(e[linksDepth]));
								}			
								//Empty Rectangle configuration found
								//4 "Cross" cells are 2 "Blade1" Cells in a row and 2 "Blade2" Cells in a column
								//if Blade1 Cardinality or Blade2 Cardinality = 0 then configuration not useful
								if (baseLinkBlade1.cardinality() > 0 && baseLinkBlade2.cardinality() > 0)
									baseLinkEmptyRegion[linksDepth] = true;
								//There is only 1 useful configuration of Empty rectangle if baseLinkRegionPotentialsCardinality > 2
								//if (e[linksDepth] < 9 || e[linksDepth] > 12)
									break;
							}
						}
						if (!baseLinkEmptyRegion[linksDepth])
							continue;
					}
					if (!baseLinkEmptyRegion[linksDepth] && baseLinkRegionPotentialsC > 2)
						continue;
					// Strong link found
					// process cells of strong link to deliver a start and bridge cell
					for (int baseLinkGroupedLinkOrdinal = 0; baseLinkGroupedLinkOrdinal < 2; baseLinkGroupedLinkOrdinal++) {
						// region 1
						//initialize as any cell
						//start cell supporting cell (if grouped)
						cells[linksDepth * 2 + linksNumber * 2 + 0] = null;
						//2nd start cell supporting cell (if grouped)
						cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
						//bridge cell supporting cell (if grouped)
						cells[linksDepth * 2 + linksNumber * 2 + 1] = null;
						//2nd bridge cell supporting cell (if grouped)
						cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
						boolean EmL = false;
						// region 1
						if (baseLinkEmptyRegion[linksDepth]) {
							if (baseLinkBlade1.cardinality() == 1 || baseLinkBlade2.cardinality() == 1) {
								if (baseLinkGroupedLinkOrdinal == 0)
									if (baseLinkBlade1.cardinality() == 1) {
										//baseLinkEmptyRegionBlades = true;
										if (linkSet[linksDepth] == 0 || linkSet[linksDepth] == 3 || linkSet[linksDepth] == 4) {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkRegion[linksDepth].Heart(e[linksDepth]));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade2.nextSetBit(0));
											if (baseLinkBlade2.cardinality() > 1)
												cells[linksDepth * 2 + linksNumber * 4 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(p1 + 1));												
										}
										else {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(0));
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade2.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade2.nextSetBit(p1 + 1));
											if (baseLinkBlade2.cardinality() > 2)
												cells[linksDepth * 2 + linksNumber * 4 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(p2 + 1));												
										}

//SudokuMonster: check the following for deletion
										//prevent duplication if both 	baseLinkBlade1.cardinality() and baseLinkBlade2.cardinality() = 1 in the middle (How?)
										if (baseLinkBlade2.cardinality() == 1) {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade1.nextSetBit(0));
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade2.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 1] = null;
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
											baseLinkGroupedLinkOrdinal = 1;
										}
									}
									else
										continue;
								if (baseLinkGroupedLinkOrdinal == 1)
									if (baseLinkBlade2.cardinality() == 1) {
										//baseLinkEmptyRegionBlades = true;
										if (linkSet[linksDepth] == 0 || linkSet[linksDepth] == 3 || linkSet[linksDepth] == 4) {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkRegion[linksDepth].Heart(e[linksDepth]));
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade1.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
											if (baseLinkBlade1.cardinality() > 1)
												cells[linksDepth * 2 + linksNumber * 4 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(p1 + 1));												
										}
										else {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0));
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade1.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade1.nextSetBit(p2 + 1));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
											cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
											if (baseLinkBlade1.cardinality() > 2)
												cells[linksDepth * 2 + linksNumber * 4 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(p1 + 1));												
										}
									}
									else
										continue;
							}
							else {
								int p3,p4;
								cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
								cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
								cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade1.nextSetBit(0));
								cells[linksNumber * 2 + linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(p3 = baseLinkBlade1.nextSetBit(p1 + 1));
								if (baseLinkBlade1.cardinality() > 2)
									cells[linksDepth * 2 + linksNumber * 4 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(p3 + 1));												
								cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade2.nextSetBit(0));
								cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p4 = baseLinkBlade2.nextSetBit(p2 + 1));
								if (baseLinkBlade2.cardinality() > 2)
									cells[linksDepth * 2 + linksNumber * 4 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(p4 + 1));												
								//The following is to extract the special case of reduced & equivalent EmL 3 , 4
								if (e[linksDepth] > 8 && baseLinkRegionPotentialsC == 4 && isSameLine(cells[linksNumber * 2 + linksDepth * 2 + 0],cells[linksNumber * 2 + linksDepth * 2 + 1]) && isSameLine(cells[linksDepth * 2 + 0],cells[linksDepth * 2 + 1]) ){
									EmL = true;
								}
								if (EmL && baseLinkGroupedLinkOrdinal == 1)
									if (cells[linksDepth * 2 + 1].equals(baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0)))) {
										cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(p1 + 1));
										cells[linksNumber * 2 + linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0));
									}	
								if (!EmL) {
									baseLinkGroupedLinkOrdinal = 1;
									//@SudokuMonster #111 bug fixattempt
										if (e[linksDepth] < 9){
												if (baseLinkBlade1.cardinality() == 2 && beatingHeart.cardinality() == 1)
													cells[linksDepth * 2 + linksNumber * 4 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkRegion[linksDepth].Heart(e[linksDepth]));
												if (baseLinkBlade2.cardinality() == 2 && beatingHeart.cardinality() == 1)
													cells[linksDepth * 2 + linksNumber * 4 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkRegion[linksDepth].Heart(e[linksDepth]));
										}
								}
							}									
						}
						else {
							baseLinkGroupedLinkOrdinal = 1;
							cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkRegionPotentials.nextSetBit(0));
							cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkRegionPotentials.nextSetBit(p2 + 1));
							cells[linksDepth * 2 + linksNumber * 2 + 0] = null;
							cells[linksDepth * 2 + linksNumber * 2 + 1] = null;
							cells[linksDepth * 2 + linksNumber * 4 + 0] = null;
							cells[linksDepth * 2 + linksNumber * 4 + 1] = null;
							if ((linkSet[linksDepth] == 0 || linkSet[linksDepth] == 3 || linkSet[linksDepth] == 4) && isSameLine(cells[linksDepth * 2 + 0],cells[linksDepth * 2 + 1]))
								continue;
						}
						int j, k ,l, m;
						j = k = m = 0;
						while (baseRegionsCells[j] != null)
							j++;
						while (emptyRegionsCells[k] != null)
							k++;
						int n = j;
						for (l = 0; l < 9 ; l++) {
							Cell digitCell = baseLinkRegion[linksDepth].getCell(l);
							if (grid.hasCellPotentialValue(digitCell.getIndex(), digit)) {
								baseRegionsCells[j++] = digitCell;
								if (baseLinkEmptyRegion[linksDepth])
								emptyRegionsCells[k++] = digitCell;
							}
						}
						//No same base region exists no same base cells (endofins) exist
						boolean next = false;
						for (l = 0; l < n && !next; l++) {
							for (m = n; m < j; m++) {
								if (baseRegionsCells[l].equals(baseRegionsCells[m])) {
									next = true;
									break;
								}
							}
						}
						m = n;
						if (!next) {
							buildLinks(grid, digit, linkSet, linksNumber, linksDepth + 1, baseLinkRegions, i, cells, baseRegionsCells, emptyRegionsCells, baseLinkRegion, baseLinkEmptyRegion, e, result);					
							if (linksDepth == (linksNumber - 1)) {
								// required potential strong links found build more
								//Permutation algorithm based on code by Phillip Paul Fuchs https://quickperm.org/01example.php
								//isLex() to reduce repitition of reverse arrangement only the min Lex n!/2 permutations allowed
								int[] q = new int[linksNumber];
								int[] r = new int[linksNumber+1];// target array and index control array 
								int s, t, tmp; // Upper Index s; Lower Index t
								Grid.Region shareRegion[] = new Grid.Region[linksNumber - 1];
								Cell start = null;
								Cell bridge1[] = new Cell[linksNumber - 1];
								Cell bridge1Support[] = new Cell[linksNumber - 1];
								Cell bridge1Support2[] = new Cell[linksNumber - 1];
								Cell bridge2[] = new Cell[linksNumber - 1];
								Cell bridge2Support[] = new Cell[linksNumber - 1];
								Cell bridge2Support2[] = new Cell[linksNumber - 1];
								Cell end = null;
								Cell startSupport = null;
								Cell startSupport2 = null;
								Cell endSupport = null;
								Cell endSupport2 = null;
								Grid.Region ringRegion = null;
								StrongLinksHint hint = null;
								int w = 0;
								for(s = 0; s < linksNumber; s++)   // initialize arrays; q[linksNumber] can be any type
								{
								  q[s] = s;   // q[s] value is not revealed and can be arbitrary
								  r[s] = s;
								}
								r[linksNumber] = linksNumber; // r[linksNumber] > 0 controls iteration and the index boundary for s
								int[] u = new int[linksNumber];
								int iterMax = 1;//2 directions for linking
								int v = 0;
								if (isLex(q)) {
									//display(q, 0, 0);   // remove comment to display array q[]
									//2 directions for each link so 2^Number of links posibilities
									while (v < linksNumber)
										u[v++] = 0;   // initialize arrays; u[linksNumber] can be any type
									//if (isLex(u)) {
										//Display u
										//Shared regions = baseLinks number - 1
										next = true;
										for (w = 0; w < (linksNumber - 1); w++)
											if ((shareRegion[w] = shareRegionOf(grid,
												bridge1[w] = cells[2 * q[w] + (1 - u[w])],
												bridge1Support[w] = cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],												
												bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
												bridge2Support[w] = cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber],
												bridge1Support2[w] = cells[2 * q[w] + (1 - u[w]) + 4 * linksNumber],
												bridge2Support2[w] = cells[2 * q[w + 1] + u[w + 1] + 4 * linksNumber]
												)) == null) {
												next = false;
												break;
											}
										if (next) {
											//Found Strong links
											//The iterative algorithms designed to handle 2 >= linksNumber >= 8 means that this happens at 4 points
											//What to do with them
											start = cells[2 * q[0] + u[0]];
											startSupport = cells[2 * q[0] + u[0] + 2 * linksNumber];
											startSupport2 = cells[2 * q[0] + u[0] + 4 * linksNumber];
											end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
											endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber];
											endSupport2 = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 4 * linksNumber];
											if ((ringRegion = shareRegionOf(grid,
												start,
												startSupport,												
												end,
												endSupport,
												startSupport2,
												endSupport2
												)) != null) {
											//We have a ring
												if (isRegionMinLex(baseLinkRegion, q) && u[0] == 0) {
													hint = createHint1(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion, new Cell[][] {bridge1, bridge1Support, bridge1Support2, bridge2, bridge2Support, bridge2Support2});
													if (hint.isWorth())
													result.add(hint);
												}
											}
											else {
												hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion);
												if (hint.isWorth())
													result.add(hint);
											}
										}
									//}
									v--;
									while (v >= 0) {
										if (u[v] < iterMax) {
											u[v]++;
											v++;
											while (v < linksNumber)
											   u[v++] = 0;
											//if (isLex(u)) {
												//Display u
												//Shared regions = baseLinks number - 1
												next = true;
												for (w = 0; w < (linksNumber - 1); w++)
													if ((shareRegion[w] = shareRegionOf(grid,
														bridge1[w] = cells[2 * q[w] + (1 - u[w])],
														bridge1Support[w] = cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],
														bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
														bridge2Support[w] = cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber],
														bridge1Support2[w] = cells[2 * q[w] + (1 - u[w]) + 4 * linksNumber],
														bridge2Support2[w] = cells[2 * q[w + 1] + u[w + 1] + 4 * linksNumber]
														)) == null) {
														next = false;
														break;
													}
												if (next) {
													//Found Strong links
													//The iterative algorithms designed to handle 2 >= linksNumber >= 8 means that this happens at 4 points
													//What to do with them
													start = cells[2 * q[0] + u[0]];
													startSupport = cells[2 * q[0] + u[0] + 2 * linksNumber];
													startSupport2 = cells[2 * q[0] + u[0] + 4 * linksNumber];
													end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
													endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber]; 
													endSupport2 = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 4 * linksNumber]; 
													if ((ringRegion = shareRegionOf(grid,
														start,
														startSupport,												
														end,
														endSupport,
														startSupport2,
														endSupport2
														)) != null) {
													//We have a ring
														if (isRegionMinLex(baseLinkRegion, q) && u[0] == 0) {
															hint = createHint1(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion, new Cell[][] {bridge1, bridge1Support, bridge1Support2, bridge2, bridge2Support, bridge2Support2});
															if (hint.isWorth())
															result.add(hint);
														}
													}
													else {
														hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion);
														if (hint.isWorth())
															result.add(hint);
													}
												}
											//}
											v--;
										}
										else
											v--;
									} 
								}
								s = 1;   // setup first swap points to be 1 and 0 respectively (s & t)
								while(s < linksNumber)
								{
									r[s]--;             // decrease index "weight" for s by one
									t = s % 2 * r[s];   // IF s is odd then t = r[s] otherwise t = 0
									tmp = q[t];         // swap(q[t], q[s])
									q[t] = q[s];
									q[s] = tmp;
									if (isLex(q)) {
										//display(q, t, s); // remove comment to display target array q[]
										//2 directions for each link so 2^Number of links posibilities
										u = new int[linksNumber];
										iterMax = 1;//2 directions for linking
										v = 0;
										while (v < linksNumber)
											u[v++] = 0;   // initialize arrays; u[linksNumber] can be any type
										//if (isLex(u)) {
											//Display u
											next = true;
											for (w = 0; w < (linksNumber - 1); w++)
												if ((shareRegion[w] = shareRegionOf(grid,
													bridge1[w] = cells[2 * q[w] + (1 - u[w])],
													bridge1Support[w] = cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],
													bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
													bridge2Support[w] = cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber],
													bridge1Support2[w] = cells[2 * q[w] + (1 - u[w]) + 4 * linksNumber],
													bridge2Support2[w] = cells[2 * q[w + 1] + u[w + 1] + 4 * linksNumber]
													)) == null) {
													next = false;
													break;
												}
											if (next) {
												//Found Strong links
												//The iterative algorithms designed to handle 2 >= linksNumber >= 8 means that this happens at 4 points
												//What to do with them
												start = cells[2 * q[0] + u[0]];
												startSupport = cells[2 * q[0] + u[0] + 2 * linksNumber];
												startSupport2 = cells[2 * q[0] + u[0] + 4 * linksNumber];
												end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
												endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber];
												endSupport2 = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 4 * linksNumber];
												if ((ringRegion = shareRegionOf(grid,
													start,
													startSupport,												
													end,
													endSupport,
													startSupport2,
													endSupport2
													)) != null) {
													//We have a ring
													if (isRegionMinLex(baseLinkRegion, q) && u[0] == 0) {
														hint = createHint1(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion, new Cell[][] {bridge1, bridge1Support, bridge1Support2, bridge2, bridge2Support, bridge2Support2});
														if (hint.isWorth())
														result.add(hint);
													}
												}
												else {
													hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion);
													if (hint.isWorth())
														result.add(hint);
												}
											}
										//}
										v--;
										while (v >= 0) {
											if (u[v] < iterMax) {
												u[v]++;
												v++;
												while (v < linksNumber)
												   u[v++] = 0;
												//if (isLex(u)) {
													//Display u
													next = true;
													for (w = 0; w < (linksNumber - 1); w++)
														if ((shareRegion[w] = shareRegionOf(grid,
															bridge1[w] = cells[2 * q[w] + (1 - u[w])],
															bridge1Support[w] = cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],
															bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
															bridge2Support[w] = cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber],
															bridge1Support2[w] = cells[2 * q[w] + (1 - u[w]) + 4 * linksNumber],
															bridge2Support2[w] = cells[2 * q[w + 1] + u[w + 1] + 4 * linksNumber]
															)) == null) {
															next = false;
															break;
														}
													if (next) {
														//Found Strong links
														//The iterative algorithms designed to handle 2 >= linksNumber >= 8 means that this happens at 4 points
														//What to do with them
														start = cells[2 * q[0] + u[0]];
														startSupport = cells[2 * q[0] + u[0] + 2 * linksNumber];
														startSupport2 = cells[2 * q[0] + u[0] + 4 * linksNumber];
														end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
														endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber];
														endSupport2 = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 4 * linksNumber];
														if ((ringRegion = shareRegionOf(grid,
															start,
															startSupport,												
															end,
															endSupport,
															startSupport2,
															endSupport2
															)) != null) {
															//We have a ring
															if (isRegionMinLex(baseLinkRegion, q) && u[0] == 0) {
																hint = createHint1(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion, new Cell[][] {bridge1, bridge1Support, bridge1Support2, bridge2, bridge2Support, bridge2Support2});
																if (hint.isWorth())
																result.add(hint);
															}
														}
														else {
															hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, startSupport2, endSupport2, ringRegion);
															if (hint.isWorth())
																result.add(hint);
														}
													}
												//}
												v--;
											}
											else
												v--;
										} 
									}
									s = 1;              // reset index s to 1 (assumed)
									while (r[s] == 0)   // while (r[s] == 0)
									{
									 r[s] = s;        // reset r[s] zero value
									 s++;             // set new index value for s (increase by one)
									} // while(!r[s])
							   } // while(s < linksNumber)   
	   
							}
						}
						//From here is the finish
						for (l = 0; l < 9; l++) {
							Cell digitCell = baseLinkRegion[linksDepth].getCell(l);
							if (grid.hasCellPotentialValue(digitCell.getIndex(), digit)) {
								baseRegionsCells[--j] = null;
								if (baseLinkEmptyRegion[linksDepth])
								emptyRegionsCells[--k] = null;
							}
						}
					}
				}
			}
	}

    private List<StrongLinksHint> getHints(Grid grid, int[] linkSet)
            /*throws InterruptedException*/ {
		List<StrongLinksHint> result = new ArrayList<StrongLinksHint>();
		int linkNumber = linkSet.length;
		Grid.Region[][] baseLinkRegions = new Grid.Region[linkNumber][9];
		for (int i = 0; i < linkNumber; i++)
			baseLinkRegions[i] = Grid.getRegions(linkSet[i]);
		for (int digit = 1; digit <= 9; digit++) {
			Cell[] cells = new Cell[linkNumber * 6];//ZSudokuMonster: 6 to allow bridge1Support2 & bridge2Support2
			Cell[] baseRegionsCells = new Cell[linkNumber * 6];
			Cell[] emptyRegionsCells = new Cell[linkNumber * 6];
			Grid.Region[] baseLinkRegion = new Grid.Region[linkNumber];
			boolean[] baseLinkEmptyRegion = new boolean[linkNumber];
			int[] e = new int [linkNumber];
			buildLinks(grid, digit, linkSet, linkNumber, 0, baseLinkRegions, 0, cells, baseRegionsCells, emptyRegionsCells, baseLinkRegion, baseLinkEmptyRegion, e, result);
        } //digit
		return result;
    }

    private StrongLinksHint createHint(Grid grid, int value, Cell start1, Cell end3, Cell start1Support, Cell end3Support, Cell[] emptyCells, Grid.Region[] baseLinkRegion, Grid.Region[] shareRegion, Cell[] bridge1, Cell[] bridge2, int[] q, int[] linkSet, boolean[] baseLinkEmptyRegion, Cell start1Support2, Cell end3Support2, Grid.Region ringRegion) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
		int eliminationsTotal = 0;
        CellSet victims = new CellSet(start1.getVisibleCells());
        victims.retainAll(end3.getVisibleCells());
		if (baseLinkEmptyRegion[q[0]] && start1Support != null) {
			victims.retainAll(start1Support.getVisibleCells());
			if (start1Support2 != null)
				victims.retainAll(start1Support2.getVisibleCells());
		}		
		if (baseLinkEmptyRegion[q[linkSet.length - 1]] && end3Support != null) {
			victims.retainAll(end3Support.getVisibleCells());
			if (end3Support2 != null)
				victims.retainAll(end3Support2.getVisibleCells());
		}
        victims.remove(start1);
        victims.remove(end3);
		for (int i = 0; i < linkSet.length; i++) {
			victims.bits.andNot(baseLinkRegion[q[i]].regionCellsBitSet);
		}
		for (int i = 0; i < (linkSet.length - 1); i++) {
			victims.bits.andNot(shareRegion[i].regionCellsBitSet);
		}		
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value)){
                removablePotentials.put(cell, SingletonBitSet.create(value));
				eliminationsTotal++;
			}
        }
        // Create hint

        return new StrongLinksHint(this, removablePotentials,
			start1, value, end3, emptyCells, eliminationsTotal, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, ringRegion);
	}

    private StrongLinksHint createHint1(Grid grid, int value, Cell start1, Cell end3, Cell start1Support, Cell end3Support, Cell[] emptyCells, Grid.Region[] baseLinkRegion, Grid.Region[] shareRegion, Cell[] bridge1, Cell[] bridge2, int[] q, int[] linkSet, boolean[] baseLinkEmptyRegion, Cell start1Support2, Cell end3Support2, Grid.Region ringRegion, Cell[][] ringRegionCells) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
		int eliminationsTotal = 0;
        CellSet victims = new CellSet(start1.getVisibleCells());
        victims.retainAll(end3.getVisibleCells());
		if (baseLinkEmptyRegion[q[0]] && start1Support != null) {
			victims.retainAll(start1Support.getVisibleCells());
			if (start1Support2 != null)
				victims.retainAll(start1Support2.getVisibleCells());
		}		
		if (baseLinkEmptyRegion[q[linkSet.length - 1]] && end3Support != null) {
			victims.retainAll(end3Support.getVisibleCells());
			if (end3Support2 != null)
				victims.retainAll(end3Support2.getVisibleCells());
		}
        victims.remove(start1);
        victims.remove(end3);
		for (int i = 0; i < linkSet.length; i++) {
			victims.bits.andNot(baseLinkRegion[q[i]].regionCellsBitSet);
		}
		//for (int i = 0; i < (linkSet.length - 1); i++) {
			//victims.bits.andNot(shareRegion[i].regionCellsBitSet);
		//}		
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value)){
                removablePotentials.put(cell, SingletonBitSet.create(value));
				eliminationsTotal++;
			}
        }
		for (int w = 0; w < (linkSet.length - 1); w++) {
			victims = new CellSet(ringRegionCells[0][w].getVisibleCells());
			victims.retainAll(ringRegionCells[3][w].getVisibleCells());
			if (ringRegionCells[1][w] != null) {
				victims.retainAll(ringRegionCells[1][w].getVisibleCells());
				if (ringRegionCells[2][w] != null)
					victims.retainAll(ringRegionCells[2][w].getVisibleCells());	
			}
			if (ringRegionCells[4][w] != null) {
				victims.retainAll(ringRegionCells[4][w].getVisibleCells());
				if (ringRegionCells[5][w] != null)
					victims.retainAll(ringRegionCells[5][w].getVisibleCells());
			}
			victims.remove(ringRegionCells[0][w]);
			victims.remove(ringRegionCells[3][w]);
			for (int i = 0; i < linkSet.length; i++) {
				victims.bits.andNot(baseLinkRegion[q[i]].regionCellsBitSet);
			}
			//for (int i = 0; i < (linkSet.length - 1); i++) {
				//victims.bits.andNot(shareRegion[i].regionCellsBitSet);
			//}		
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), value)){
					removablePotentials.put(cell, SingletonBitSet.create(value));
					eliminationsTotal++;
				}
			}
		}
        // Create hint

        return new StrongLinksHint(this, removablePotentials,
			start1, value, end3, emptyCells, eliminationsTotal, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion, ringRegion);
	}

    @Override
    public String toString() {
        return degree + " Strong links";
    }
}
