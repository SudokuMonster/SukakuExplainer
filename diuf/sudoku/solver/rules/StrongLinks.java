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
		//This can deliver up to 8 strong links
		final int[][] setsAll ={
			{0,	0,	0,	0,	0,	0,	0,	0},
			{0,	0,	0,	0,	0,	0,	0,	1},
			{0,	0,	0,	0,	0,	0,	0,	2},
			{0,	0,	0,	0,	0,	0,	1,	1},
			{0,	0,	0,	0,	0,	0,	1,	2},
			{0,	0,	0,	0,	0,	0,	2,	2},
			{0,	0,	0,	0,	0,	1,	1,	1},
			{0,	0,	0,	0,	0,	1,	1,	2},
			{0,	0,	0,	0,	0,	1,	2,	2},
			{0,	0,	0,	0,	0,	2,	2,	2},
			{0,	0,	0,	0,	1,	1,	1,	1},
			{0,	0,	0,	0,	1,	1,	1,	2},
			{0,	0,	0,	0,	1,	1,	2,	2},
			{0,	0,	0,	0,	1,	2,	2,	2},
			{0,	0,	0,	0,	2,	2,	2,	2},
			{0,	0,	0,	1,	1,	1,	1,	1},
			{0,	0,	0,	1,	1,	1,	1,	2},
			{0,	0,	0,	1,	1,	1,	2,	2},
			{0,	0,	0,	1,	1,	2,	2,	2},
			{0,	0,	0,	1,	2,	2,	2,	2},
			{0,	0,	0,	2,	2,	2,	2,	2},
			{0,	0,	1,	1,	1,	1,	1,	1},
			{0,	0,	1,	1,	1,	1,	1,	2},
			{0,	0,	1,	1,	1,	1,	2,	2},
			{0,	0,	1,	1,	1,	2,	2,	2},
			{0,	0,	1,	1,	2,	2,	2,	2},
			{0,	0,	1,	2,	2,	2,	2,	2},
			{0,	0,	2,	2,	2,	2,	2,	2},
			{0,	1,	1,	1,	1,	1,	1,	1},
			{0,	1,	1,	1,	1,	1,	1,	2},
			{0,	1,	1,	1,	1,	1,	2,	2},
			{0,	1,	1,	1,	1,	2,	2,	2},
			{0,	1,	1,	1,	2,	2,	2,	2},
			{0,	1,	1,	2,	2,	2,	2,	2},
			{0,	1,	2,	2,	2,	2,	2,	2},
			{0,	2,	2,	2,	2,	2,	2,	2},
			{1,	1,	1,	1,	1,	1,	1,	1},
			{1,	1,	1,	1,	1,	1,	1,	2},
			{1,	1,	1,	1,	1,	1,	2,	2},
			{1,	1,	1,	1,	1,	2,	2,	2},
			{1,	1,	1,	1,	2,	2,	2,	2},
			{1,	1,	1,	2,	2,	2,	2,	2},
			{1,	1,	2,	2,	2,	2,	2,	2},
			{1,	2,	2,	2,	2,	2,	2,	2},
			{2,	2,	2,	2,	2,	2,	2,	2},
			{2,	2,	2,	2,	2,	2,	2,	2}
		};
		List<StrongLinksHint> hintsFinal = new ArrayList<StrongLinksHint>();
		List<StrongLinksHint> hintsStart;
		int Set[] = new int[degree];
		int linkNumber = 0;
		int setNumber = 0;
		while (linkNumber < degree) {
			Set[linkNumber] = setsAll[setNumber][8 - degree + linkNumber];
			linkNumber++;
			if (linkNumber == degree && (setNumber < 45 && (degree == 8 || setsAll[setNumber][8 - degree - 1] != 1)))  {
				hintsStart = getHints(grid, Set);
					for (StrongLinksHint hint : hintsStart)
						hintsFinal.add(hint);				
				linkNumber = 0;
				setNumber++;
			}
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
            Cell bridge1, Cell bridge1Support, Cell bridge2, Cell bridge2Support) {
        if (bridge1.getX() == bridge2.getX()) {
			if (bridge1Support == null && bridge2Support == null)
				return (Grid.Column)Grid.getRegionAt(2,bridge1.getIndex());
			else if (bridge1Support == null) {
				if (bridge1.getX() == bridge2Support.getX())
					return (Grid.Column)Grid.getRegionAt(2,bridge1.getIndex());
			}
			else if (bridge2Support == null) {
				if (bridge2.getX() == bridge1Support.getX())
					return (Grid.Column)Grid.getRegionAt(2,bridge1.getIndex());
			}
			else if (bridge2.getX() == bridge1Support.getX() && bridge1.getX() == bridge2Support.getX())
				return (Grid.Column)Grid.getRegionAt(2,bridge1.getIndex());		
        } 
		if (bridge1.getY() == bridge2.getY()) {
			if (bridge1Support == null && bridge2Support == null)
				return (Grid.Row)Grid.getRegionAt(1,bridge1.getIndex());
			else if (bridge1Support == null) {
				if (bridge1.getY() == bridge2Support.getY())
					return (Grid.Row)Grid.getRegionAt(1,bridge1.getIndex());
			}
			else if (bridge2Support == null) {
				if (bridge2.getY() == bridge1Support.getY())
					return (Grid.Row)Grid.getRegionAt(1,bridge1.getIndex());
			}
			else if (bridge2.getY() == bridge1Support.getY() && bridge1.getY() == bridge2Support.getY())
				return (Grid.Row)Grid.getRegionAt(1,bridge1.getIndex());		
        } 
		if (bridge1.getB() == bridge2.getB()) {
			if (bridge1Support == null && bridge2Support == null)
				return (Grid.Block)Grid.getRegionAt(0,bridge1.getIndex());
			else if (bridge1Support == null) {
				if (bridge1.getB() == bridge2Support.getB())
					return (Grid.Block)Grid.getRegionAt(0,bridge1.getIndex());
			}
			else if (bridge2Support == null) {
				if (bridge2.getB() == bridge1Support.getB())
					return (Grid.Block)Grid.getRegionAt(0,bridge1.getIndex());
			}
			else if (bridge2.getB() == bridge1Support.getB() && bridge1.getB() == bridge2Support.getB())
				return (Grid.Block)Grid.getRegionAt(0,bridge1.getIndex());		
        } 
		return null;
    }

	private boolean isSameLine (Cell lineCell1, Cell lineCell2) {
		if (lineCell1.getX() == lineCell2.getX() || lineCell1.getY() == lineCell2.getY())
			return true;
		return false;
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
					if (baseLinkRegionPotentialsC > 2) {
						//Grouped Strong links in box have 15 configurations but only 9 are ER 
						for (e[linksDepth] = 0; e[linksDepth] < (linkSet[linksDepth] < 1 ? 15 : 3); e[linksDepth]++) {
							//there are equivalent strong links in box if e[linksDepth] > 9 and baseLinkRegionPotentialsC < 4
							if (e[linksDepth] > 8 && baseLinkRegionPotentialsC < 4)
								continue;
							//baseLinkEmptyRegion[linksDepth] = false;
							heartCells[0] = (linkSet[linksDepth] == 0 && e[linksDepth] < 9 ? baseLinkRegion[linksDepth].getCell(e[linksDepth]): null);
							BitSet baseLinkEmptyArea = (BitSet)baseLinkRegionPotentials.clone();
							baseLinkBlade1 = (BitSet)baseLinkRegionPotentials.clone();
							baseLinkBlade2 = (BitSet)baseLinkRegionPotentials.clone();
							if (linkSet[linksDepth] == 0) {
								//confirm if we have an empty rectangle
								//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
								//9 configurations for each block depending on "Heart" cell
								baseLinkEmptyArea.and(baseLinkRegion[linksDepth].Rectangle(e[linksDepth]));
							}
							else {
								baseLinkEmptyArea.and(baseLinkRegion[linksDepth].lineEmptyCells(e[linksDepth]));
							}
							if (baseLinkEmptyArea.cardinality() == 0) {
								if (linkSet[linksDepth] == 0) {
									//confirm if we have an empty rectangle
									//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
									//9 configurations for each block depending on "Heart" cell
									baseLinkBlade1.and(baseLinkRegion[linksDepth].crossBlade1(e[linksDepth]));
									baseLinkBlade2.and(baseLinkRegion[linksDepth].crossBlade2(e[linksDepth]));
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
						//bridge cell supporting cell (if grouped)
						cells[linksDepth * 2 + linksNumber * 2 + 1] = null;
						boolean EmL = false;
						// region 1
						if (baseLinkEmptyRegion[linksDepth]) {
							if (baseLinkBlade1.cardinality() == 1 || baseLinkBlade2.cardinality() == 1) {
								if (baseLinkGroupedLinkOrdinal == 0)
									if (baseLinkBlade1.cardinality() == 1) {
										//baseLinkEmptyRegionBlades = true;
										if (linkSet[linksDepth] == 0) {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkRegion[linksDepth].Heart(e[linksDepth]));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0));
										}
										else {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(0));
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade2.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(p1 + 1));
										}
										//prevent duplication if both 	baseLinkBlade1.cardinality() and baseLinkBlade2.cardinality() = 1 in the middle (How?)
										if (baseLinkBlade2.cardinality() == 1) {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade1.nextSetBit(0));
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade2.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksNumber * 2 + linksDepth * 2 + 1] = null;
											baseLinkGroupedLinkOrdinal = 1;
										}
									}
									else
										continue;
								if (baseLinkGroupedLinkOrdinal == 1)
									if (baseLinkBlade2.cardinality() == 1) {
										//baseLinkEmptyRegionBlades = true;
										if (linkSet[linksDepth] == 0) {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkRegion[linksDepth].Heart(e[linksDepth]));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade1.nextSetBit(0));
										}
										else {
											cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0));
											cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade1.nextSetBit(0));
											cells[linksNumber * 2 + linksDepth * 2 + 0] = null;
											cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(p2 + 1));
										}
									}
									else
										continue;
							}
							else {
								cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(p1 = baseLinkBlade1.nextSetBit(0));
								cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkBlade2.nextSetBit(0));
								cells[linksNumber * 2 + linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(p1 + 1));
								cells[linksNumber * 2 + linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(p2 + 1));
								//The following is to extract the special case of reduced & equivalent EmL 3 , 4
								if (e[linksDepth] > 8 && baseLinkRegionPotentialsC == 4 && isSameLine(cells[linksNumber * 2 + linksDepth * 2 + 0],cells[linksNumber * 2 + linksDepth * 2 + 1]) && isSameLine(cells[linksDepth * 2 + 0],cells[linksDepth * 2 + 1]) )
									EmL = true;								
								if (EmL && baseLinkGroupedLinkOrdinal == 1)
									if (cells[linksDepth * 2 + 1].equals(baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0)))) {
										cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkBlade1.nextSetBit(p1 + 1));
										cells[linksNumber * 2 + linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(baseLinkBlade2.nextSetBit(0));
									}	
								if (!EmL)
									baseLinkGroupedLinkOrdinal = 1;							}									
						}
						else {
							baseLinkGroupedLinkOrdinal = 1;
							cells[linksDepth * 2 + 0] = baseLinkRegion[linksDepth].getCell(p2 = baseLinkRegionPotentials.nextSetBit(0));
							cells[linksDepth * 2 + 1] = baseLinkRegion[linksDepth].getCell(baseLinkRegionPotentials.nextSetBit(p2 + 1));
							cells[linksDepth * 2 + linksNumber * 2 + 0] = null;
							cells[linksDepth * 2 + linksNumber * 2 + 1] = null;
							if (linkSet[linksDepth] == 0 && isSameLine(cells[linksDepth * 2 + 0],cells[linksDepth * 2 + 1]))
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
								Cell bridge2[] = new Cell[linksNumber - 1];
								Cell end = null;
								Cell startSupport = null;
								Cell endSupport = null;
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
												cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],
												bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
												cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber]
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
											end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
											endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber];
											StrongLinksHint hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion);
											if (hint.isWorth())
												result.add(hint);
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
														cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],
														bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
														cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber]
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
													end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
													endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber];
													StrongLinksHint hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion);
													if (hint.isWorth())
														result.add(hint);
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
													cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],
													bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
													cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber]
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
												end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
												endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber];
												StrongLinksHint hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion);
												if (hint.isWorth())
													result.add(hint);
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
															cells[2 * q[w] + (1 - u[w]) + 2 * linksNumber],
															bridge2[w] = cells[2 * q[w + 1] + u[w + 1]],
															cells[2 * q[w + 1] + u[w + 1] + 2 * linksNumber]
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
														end = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1])];
														endSupport = cells[2 * q[linksNumber - 1] + (1 - u[linksNumber - 1]) + 2 * linksNumber];
														StrongLinksHint hint = createHint(grid, digit, start, end, startSupport, endSupport, emptyRegionsCells, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion);
														if (hint.isWorth())
															result.add(hint);
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
			Cell[] cells = new Cell[linkNumber * 4];
			Cell[] baseRegionsCells = new Cell[linkNumber * 6];
			Cell[] emptyRegionsCells = new Cell[linkNumber * 6];
			Grid.Region[] baseLinkRegion = new Grid.Region[linkNumber];
			boolean[] baseLinkEmptyRegion = new boolean[linkNumber];
			int[] e = new int [linkNumber];
			buildLinks(grid, digit, linkSet, linkNumber, 0, baseLinkRegions, 0, cells, baseRegionsCells, emptyRegionsCells, baseLinkRegion, baseLinkEmptyRegion, e, result);
        } //digit
		return result;
    }

    private StrongLinksHint createHint(Grid grid, int value, Cell start1, Cell end3, Cell start1Support, Cell end3Support, Cell[] emptyCells, Grid.Region[] baseLinkRegion, Grid.Region[] shareRegion, Cell[] bridge1, Cell[] bridge2, int[] q, int[] linkSet, boolean[] baseLinkEmptyRegion) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
		int eliminationsTotal = 0;
        CellSet victims = new CellSet(start1.getVisibleCells());
        victims.retainAll(end3.getVisibleCells());
		if (baseLinkEmptyRegion[q[0]] && start1Support != null) {
			victims.retainAll(start1Support.getVisibleCells());
		}		
		if (baseLinkEmptyRegion[q[linkSet.length - 1]] && end3Support != null) {
			victims.retainAll(end3Support.getVisibleCells());
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
			start1, value, end3, emptyCells, eliminationsTotal, baseLinkRegion, shareRegion, bridge1, bridge2, q, linkSet, baseLinkEmptyRegion);
	}

    @Override
    public String toString() {
        return degree + " Strong links";
    }
}
