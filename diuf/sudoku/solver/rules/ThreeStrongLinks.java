package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


/**
 * Implementation of Turbot Fish technique solver.
 */
public class ThreeStrongLinks implements IndirectHintProducer {

    @Override
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
		int[][] Sets = {
			// 3 parallel strong links
			{1, 1, 1},
			{2, 2, 2},
			// 3 strong links in same Lines
			//with boxes
			{0, 0, 0},
			{0, 0, 1},
			{0, 0, 2},
			{0, 1, 1},
			{0, 2, 2},
			// 3 strong links with box(es)
			//mixed lines
			{0, 1, 2},
			{1, 1, 2},
			{1, 2, 2}
		};	
		List<ThreeStrongLinksHint> hintsFinal = new ArrayList<ThreeStrongLinksHint>();
		List<ThreeStrongLinksHint> hintsStart;
        for (int i = 0; i < 10 ; i++) {	
			hintsStart = getHints(grid, Sets[i][0], Sets[i][1], Sets[i][2]);
			for (ThreeStrongLinksHint hint : hintsStart)
				hintsFinal.add(hint);
		}
		// Sort the result
		Collections.sort(hintsFinal, new Comparator<ThreeStrongLinksHint>() {
			public int compare(ThreeStrongLinksHint h1, ThreeStrongLinksHint h2) {
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
		for (ThreeStrongLinksHint hint : hintsFinal)
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

    private boolean shareRegionsIntersection(Grid grid, Grid.Region Region1, Grid.Region Region2, int value, Cell[] cellIntersections) {						
		Cell[] sharedRegionsCells = new Cell[18];
		int j = 0;
		for (int i = 0; i < 9 ; i++) {
			Cell digitCell = Region1.getCell(i);
			if (grid.hasCellPotentialValue(digitCell.getIndex(), value)) {
				for (int l= 0; l < 3; l++) {
					if (digitCell.equals(cellIntersections[l]))
						break;
					if (l == 2)
						sharedRegionsCells[j++] = digitCell;
				}
			}
			digitCell = Region2.getCell(i);
			if (grid.hasCellPotentialValue(digitCell.getIndex(), value)) {
				for (int l= 0; l < 3; l++) {
					if (digitCell.equals(cellIntersections[l]))
						break;
					if (l == 2)
						sharedRegionsCells[j++] = digitCell;
				}
			}
		}
		boolean next = false;
		for (int i = 0; i < j - 1 && !next; i++) {
			for (int k = i + 1; k < j; k++) {
				if (sharedRegionsCells[i].equals(sharedRegionsCells[k])) {
					next = true;
					break;
				}
			}
		}
		if (next) return true;
		return false;
	}

	private boolean isSameLine (Cell lineCell1, Cell lineCell2) {
		if (lineCell1.getX() == lineCell2.getX() || lineCell1.getY() == lineCell2.getY())
			return true;
		return false;
	}

    private List<ThreeStrongLinksHint> getHints(Grid grid, int baseLink1, int baseLink2, int baseLink3)
            /*throws InterruptedException*/ {
		List<ThreeStrongLinksHint> result = new ArrayList<ThreeStrongLinksHint>();
        int p1, p2, p3, p4, p5, p6;
		p1 = p2 = p3 = p4 = p5 = p6 = 0;
		for (int digit = 1; digit <= 9; digit++) {
            Grid.Region[] baseLink1Regions = Grid.getRegions(baseLink1);
            Grid.Region[] baseLink2Regions = Grid.getRegions(baseLink2);
            Grid.Region[] baseLink3Regions = Grid.getRegions(baseLink3);
			for (int i1 = 0; i1 < baseLink1Regions.length; i1++) {
				Cell[] cells = new Cell[12];
				Cell[] heartCells= new Cell[3];
				boolean baseLink1EmptyRegion = false;
				//boolean baseLink1EmptyRegionBlades = false;
				Grid.Region baseLink1Region = baseLink1Regions[i1];
				BitSet baseLink1RegionPotentials = baseLink1Region.getPotentialPositions(grid, digit);
				int baseLink1RegionPotentialsC = baseLink1RegionPotentials.cardinality();
				int e1 = 0;
				if (baseLink1RegionPotentialsC > 1){
					if (baseLink1RegionPotentialsC > 6)
						continue;
					BitSet baseLink1Blade1 = (BitSet)baseLink1RegionPotentials.clone();
					BitSet baseLink1Blade2 = (BitSet)baseLink1RegionPotentials.clone();
					if (baseLink1RegionPotentialsC > 2) {
						//Grouped Strong links in box have 15 configurations but only 9 are ER 
						for (e1 = 0; e1 < (baseLink1 < 1 ? 15 : 3); e1++) {
							//there are equivalent strong links in box if e1 > 9 and baseLink1RegionPotentialsC < 4
							if (e1 > 8 && baseLink1RegionPotentialsC < 4)
								continue;
							//baseLink1EmptyRegion = false;
							heartCells[0] = (baseLink1 == 0 && e1 < 9 ? baseLink1Region.getCell(e1): null);
							BitSet baseLink1EmptyArea = (BitSet)baseLink1RegionPotentials.clone();
							baseLink1Blade1 = (BitSet)baseLink1RegionPotentials.clone();
							baseLink1Blade2 = (BitSet)baseLink1RegionPotentials.clone();
							if (baseLink1 == 0) {
								//confirm if we have an empty rectangle
								//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
								//9 configurations for each block depending on "Heart" cell
								baseLink1EmptyArea.and(baseLink1Region.Rectangle(e1));
							}
							else {
								baseLink1EmptyArea.and(baseLink1Region.lineEmptyCells(e1));
							}
							if (baseLink1EmptyArea.cardinality() == 0) {
								if (baseLink1 == 0) {
									//confirm if we have an empty rectangle
									//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
									//9 configurations for each block depending on "Heart" cell
									baseLink1Blade1.and(baseLink1Region.crossBlade1(e1));
									baseLink1Blade2.and(baseLink1Region.crossBlade2(e1));
								}
								else {
									baseLink1Blade1.and(baseLink1Region.lineBlade1(e1));
									baseLink1Blade2.and(baseLink1Region.lineBlade2(e1));
								}			
								//Empty Rectangle configuration found
								//4 "Cross" cells are 2 "Blade1" Cells in a row and 2 "Blade2" Cells in a column
								//if Blade1 Cardinality or Blade2 Cardinality = 0 then configuration not useful
								if (baseLink1Blade1.cardinality() > 0 && baseLink1Blade2.cardinality() > 0)
									baseLink1EmptyRegion = true;
								//There is only 1 useful configuration of Empty rectangle if baseLink1RegionPotentialsCardinality > 2
								//if (e1 < 9 || e1 > 12)
									break;
								//if e1 > 8 then equivalent e1 < 9 patterns exist if either Blade has <2 candiates
								//if (baseLink1EmptyRegion && (baseLink1Blade1.cardinality() < 2 || baseLink1Blade2.cardinality() < 2)) {
								//	e1 = (int)((e1 - 9) / 3) * 3 + 11;
								//	baseLink1EmptyRegion = false;
								//}
							}
						}
						if (!baseLink1EmptyRegion)
							continue;
					}
					if (!baseLink1EmptyRegion && baseLink1RegionPotentialsC > 2)
						continue;
					// Strong link found
					// process cells of strong link to deliver a start and bridge cell
					for (int baseLink1GroupedLinkOrdinal = 0; baseLink1GroupedLinkOrdinal < 2; baseLink1GroupedLinkOrdinal++) {
						// region 1
						//initialize as any cell
						//start cell supporting cell (if grouped)
						cells[6] = null;
						//bridge cell supporting cell (if grouped)
						cells[7] = null;
						boolean EmL1 = false;
						// region 1
						if (baseLink1EmptyRegion) {
							if (baseLink1Blade1.cardinality() == 1 || baseLink1Blade2.cardinality() == 1) {
								if (baseLink1GroupedLinkOrdinal == 0)
									if (baseLink1Blade1.cardinality() == 1) {
										//baseLink1EmptyRegionBlades = true;
										if (baseLink1 == 0) {
											cells[0] = baseLink1Region.getCell(baseLink1Blade1.nextSetBit(0));
											cells[7] = baseLink1Region.getCell(baseLink1Region.Heart(e1));
											cells[6] = null;
											cells[1] = baseLink1Region.getCell(baseLink1Blade2.nextSetBit(0));
										}
										else {
											cells[0] = baseLink1Region.getCell(baseLink1Blade1.nextSetBit(0));
											cells[1] = baseLink1Region.getCell(p1 = baseLink1Blade2.nextSetBit(0));
											cells[6] = null;
											cells[7] = baseLink1Region.getCell(baseLink1Blade2.nextSetBit(p1 + 1));
										}
									}
									else
										continue;
								if (baseLink1GroupedLinkOrdinal == 1)
									if (baseLink1Blade2.cardinality() == 1) {
										//baseLink1EmptyRegionBlades = true;
										if (baseLink1 == 0) {
											cells[0] = baseLink1Region.getCell(baseLink1Blade2.nextSetBit(0));
											cells[7] = baseLink1Region.getCell(baseLink1Region.Heart(e1));
											cells[6] = null;
											cells[1] = baseLink1Region.getCell(p2 = baseLink1Blade1.nextSetBit(0));
										}
										else {
											cells[0] = baseLink1Region.getCell(baseLink1Blade2.nextSetBit(0));
											cells[1] = baseLink1Region.getCell(p2 = baseLink1Blade1.nextSetBit(0));
											cells[6] = null;
											cells[7] = baseLink1Region.getCell(baseLink1Blade1.nextSetBit(p2 + 1));
										}
									}
									else
										continue;
							}
							else {
								baseLink1GroupedLinkOrdinal = 1;
								cells[0] = baseLink1Region.getCell(p1 = baseLink1Blade1.nextSetBit(0));
								cells[1] = baseLink1Region.getCell(p2 = baseLink1Blade2.nextSetBit(0));
								cells[6] = baseLink1Region.getCell(baseLink1Blade1.nextSetBit(p1 + 1));
								cells[7] = baseLink1Region.getCell(baseLink1Blade2.nextSetBit(p2 + 1));
								//The following is to extract the special case of reduced & equivalent EmL 3 , 4 (repeated for e2 and e3)
								if (e1 > 8 && baseLink1RegionPotentialsC == 4 && isSameLine(cells[6],cells[7]) && isSameLine(cells[0],cells[1]) ) {
									EmL1 = true;
								}
							}									
						}
						else {
							baseLink1GroupedLinkOrdinal = 1;
							cells[0] = baseLink1Region.getCell(p2 = baseLink1RegionPotentials.nextSetBit(0));
							cells[1] = baseLink1Region.getCell(baseLink1RegionPotentials.nextSetBit(p2 + 1));
							if (baseLink1 == 0 && isSameLine(cells[0],cells[1]))
								continue;
						}
						for (int i2 = (baseLink1 == baseLink2 ? i1+1 : 0); i2 < baseLink2Regions.length; i2++) {
							boolean baseLink2EmptyRegion = false;
							//boolean baseLink2EmptyRegionBlades = false;
							Grid.Region baseLink2Region = baseLink2Regions[i2];
							BitSet baseLink2RegionPotentials = baseLink2Region.getPotentialPositions(grid, digit);
							int baseLink2RegionPotentialsC = baseLink2RegionPotentials.cardinality();
							heartCells[1] = null;
							int e2 = 0;
							if (baseLink2RegionPotentialsC > 1){
								if (baseLink2RegionPotentialsC > 6)
									continue;
								BitSet baseLink2Blade1 = (BitSet)baseLink2RegionPotentials.clone();
								BitSet baseLink2Blade2 = (BitSet)baseLink2RegionPotentials.clone();
								if (baseLink2RegionPotentialsC > 2) {
									//Grouped Strong links in box have 15 configurations but only 9 are ER 
									for (e2 = 0; e2 < (baseLink2 < 1 ? 15 : 3); e2++) {
										if (e2 > 8 && baseLink2RegionPotentialsC < 4)
											continue;
										baseLink2EmptyRegion = false;
										heartCells[1] = (baseLink2 == 0 && e2 < 9 ? baseLink2Region.getCell(e2): null);
										BitSet baseLink2EmptyArea = (BitSet)baseLink2RegionPotentials.clone();
										baseLink2Blade1 = (BitSet)baseLink2RegionPotentials.clone();
										baseLink2Blade2 = (BitSet)baseLink2RegionPotentials.clone();
										if (baseLink2 == 0) {
											//confirm if we have an empty rectangle
											//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
											//9 configurations for each block depending on "Heart" cell
											baseLink2EmptyArea.and(baseLink2Region.Rectangle(e2));
										}
										else {
											baseLink2EmptyArea.and(baseLink2Region.lineEmptyCells(e2));
										}
										if (baseLink2EmptyArea.cardinality() == 0) {
											if (baseLink2 == 0) {
												//confirm if we have an empty rectangle
												//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
												//9 configurations for each block depending on "Heart" cell
												baseLink2Blade1.and(baseLink2Region.crossBlade1(e2));
												baseLink2Blade2.and(baseLink2Region.crossBlade2(e2));
											}
											else {
												baseLink2Blade1.and(baseLink2Region.lineBlade1(e2));
												baseLink2Blade2.and(baseLink2Region.lineBlade2(e2));
											}			
											//Empty Rectangle configuration found
											//4 "Cross" cells are 2 "Blade1" Cells in a row and 2 "Blade2" Cells in a column
											//if Blade1 Cardinality or Blade2 Cardinality = 0 then configuration not useful
											if (baseLink2Blade1.cardinality() > 0 && baseLink2Blade2.cardinality() > 0)
												baseLink2EmptyRegion = true;
											//There is only 1 useful configuration of Empty rectangle if baseLink1RegionPotentialsCardinality > 2
											if (e2 < 9)
												break;
											//if e2 > 8 then equivalent e2 < 9 patterns exist if either Blade has <2 candiates
											if (baseLink2EmptyRegion && (baseLink2Blade1.cardinality() < 2 || baseLink2Blade2.cardinality() < 2))
												baseLink2EmptyRegion = false;
										}
									}
									if (!baseLink2EmptyRegion)
										continue;
								}
								if (!baseLink2EmptyRegion && baseLink2RegionPotentialsC > 2)
									continue;
								// Strong link found
								// process cells of strong link to deliver a start and bridge cell
								for (int baseLink2GroupedLinkOrdinal = 0; baseLink2GroupedLinkOrdinal < 2; baseLink2GroupedLinkOrdinal++) {
									// region 2
									//initialize as any cell
									//start cell supporting cell (if grouped)
									cells[8] = null;
									//bridge cell supporting cell (if grouped)
									cells[9] = null;
									boolean EmL2 = false;
									// region 2
									if (baseLink2EmptyRegion) {
										if (baseLink2Blade1.cardinality() == 1 || baseLink2Blade2.cardinality() == 1) {
											if (baseLink2GroupedLinkOrdinal == 0)
												if (baseLink2Blade1.cardinality() == 1) {
													//baseLink2EmptyRegionBlades = true;
													if (baseLink2 == 0) {
														cells[2] = baseLink2Region.getCell(baseLink2Blade1.nextSetBit(0));
														cells[9] = baseLink2Region.getCell(baseLink2Region.Heart(e2));
														cells[8] = null;
														cells[3] = baseLink2Region.getCell(baseLink2Blade2.nextSetBit(0));
													}
													else {
														cells[2] = baseLink2Region.getCell(baseLink2Blade1.nextSetBit(0));
														cells[3] = baseLink2Region.getCell(p3 = baseLink2Blade2.nextSetBit(0));
														cells[8] = null;
														cells[9] = baseLink2Region.getCell(baseLink2Blade2.nextSetBit(p3 + 1));
													}
												}
												else
													continue;
											if (baseLink2GroupedLinkOrdinal == 1)
												if (baseLink2Blade2.cardinality() == 1) {
													//baseLink2EmptyRegionBlades = true;
													if (baseLink2 == 0) {
														cells[2] = baseLink2Region.getCell(baseLink2Blade2.nextSetBit(0));
														cells[9] = baseLink2Region.getCell(baseLink2Region.Heart(e2));
														cells[8] = null;
														cells[3] = baseLink2Region.getCell(baseLink2Blade1.nextSetBit(0));	
													}
													else {
														cells[2] = baseLink2Region.getCell(baseLink2Blade2.nextSetBit(0));
														cells[3] = baseLink2Region.getCell(p4 = baseLink2Blade1.nextSetBit(0));
														cells[8] = null;
														cells[9] = baseLink2Region.getCell(baseLink2Blade1.nextSetBit(p4 + 1));
													}
												}
												else
													continue;
										}
										else {
											baseLink2GroupedLinkOrdinal = 1;
											cells[2] = baseLink2Region.getCell(p3 = baseLink2Blade1.nextSetBit(0));
											cells[8] = baseLink2Region.getCell(baseLink2Blade1.nextSetBit(p3 + 1));
											cells[3] = baseLink2Region.getCell(p4 = baseLink2Blade2.nextSetBit(0));
											cells[9] = baseLink2Region.getCell(baseLink2Blade2.nextSetBit(p4 + 1));
											//The following is to extract the special case of reduced & equivalent EmL 3 , 4
											if (e2 > 8 && baseLink2RegionPotentialsC == 4 && isSameLine(cells[2],cells[3]) && isSameLine(cells[8],cells[9]) ) {
												EmL2 = true;
											}
										}									
									}
									else {
										baseLink2GroupedLinkOrdinal = 1;
										cells[2] = baseLink2Region.getCell(p4 = baseLink2RegionPotentials.nextSetBit(0));
										cells[3] = baseLink2Region.getCell(baseLink2RegionPotentials.nextSetBit(p4 + 1));
										if (baseLink2 == 0 && isSameLine(cells[2],cells[3]))
											continue;
									}
									for (int i3 = (baseLink2 == baseLink3 ? i2+1 : (baseLink1 == baseLink3 ? i1+1 : 0)); i3 < baseLink3Regions.length; i3++) {
										boolean baseLink3EmptyRegion = false;
										//boolean baseLink3EmptyRegionBlades = false;
										Grid.Region baseLink3Region = baseLink3Regions[i3];
										BitSet baseLink3RegionPotentials = baseLink3Region.getPotentialPositions(grid, digit);
										int baseLink3RegionPotentialsC = baseLink3RegionPotentials.cardinality();
										heartCells[2] = null;
										int e3 = 0;
										if (baseLink3RegionPotentialsC > 1){
											if (baseLink3RegionPotentialsC > 6)
												continue;
											BitSet baseLink3Blade1 = (BitSet)baseLink3RegionPotentials.clone();
											BitSet baseLink3Blade2 = (BitSet)baseLink3RegionPotentials.clone();
											if (baseLink3RegionPotentialsC > 2) {
												//Grouped Strong links in box have 15 configurations but only 9 are ER 
												for (e3 = 0; e3 < (baseLink3 < 1 ? 15 : 3); e3++) {
													if (e3 > 8 && baseLink3RegionPotentialsC < 4)
														continue;
													baseLink3EmptyRegion = false;
													heartCells[2] = (baseLink3 == 0 && e3 < 9 ? baseLink3Region.getCell(e3): null);
													BitSet baseLink3EmptyArea = (BitSet)baseLink3RegionPotentials.clone();
													baseLink3Blade1 = (BitSet)baseLink3RegionPotentials.clone();
													baseLink3Blade2 = (BitSet)baseLink3RegionPotentials.clone();
													if (baseLink3 == 0) {
														//confirm if we have an empty rectangle
														//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
														//9 configurations for each block depending on "Heart" cell
														baseLink3EmptyArea.and(baseLink3Region.Rectangle(e3));
													}
													else {
														baseLink3EmptyArea.and(baseLink3Region.lineEmptyCells(e3));
													}
													if (baseLink3EmptyArea.cardinality() == 0) {
														if (baseLink3 == 0) {
															//confirm if we have an empty rectangle
															//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
															//9 configurations for each block depending on "Heart" cell
															baseLink3Blade1.and(baseLink3Region.crossBlade1(e3));
															baseLink3Blade2.and(baseLink3Region.crossBlade2(e3));
														}
														else {
															baseLink3Blade1.and(baseLink3Region.lineBlade1(e3));
															baseLink3Blade2.and(baseLink3Region.lineBlade2(e3));
														}			
														//Empty Rectangle configuration found
														//4 "Cross" cells are 2 "Blade1" Cells in a row and 2 "Blade3" Cells in a column
														//if Blade1 Cardinality or Blade3 Cardinality = 0 then configuration not useful
														if (baseLink3Blade1.cardinality() > 0 && baseLink3Blade2.cardinality() > 0)
															baseLink3EmptyRegion = true;
														//There is only 1 useful configuration of Empty rectangle if baseLink1RegionPotentialsCardinality > 2
														if (e3 < 9)
															break;
														//if e3 > 8 then equivalent e3 < 9 patterns exist if either Blade has <2 candidates
														if (baseLink3EmptyRegion && (baseLink3Blade1.cardinality() < 2 || baseLink3Blade2.cardinality() < 2))
															baseLink3EmptyRegion = false;
													}
												}
												if (!baseLink3EmptyRegion)
													continue;
											}
											if (!baseLink3EmptyRegion && baseLink3RegionPotentialsC > 2)
												continue;
											// Strong link found
											// process cells of strong link to deliver a start and bridge cell
											for (int baseLink3GroupedLinkOrdinal = 0; baseLink3GroupedLinkOrdinal < 2; baseLink3GroupedLinkOrdinal++) {
												// region 3
												//initialize as any cell
												//start cell supporting cell (if grouped)
												cells[10] = null;
												//bridge cell supporting cell (if grouped)
												cells[11] = null;
												boolean EmL3 = false;
												// region 3
												if (baseLink3EmptyRegion) {
													if (baseLink3Blade1.cardinality() == 1 || baseLink3Blade2.cardinality() == 1) {
														if (baseLink3GroupedLinkOrdinal == 0)
															if (baseLink3Blade1.cardinality() == 1) {
																//baseLink3EmptyRegionBlades = true;
																if (baseLink3 == 0) {
																	cells[4] = baseLink3Region.getCell(baseLink3Blade1.nextSetBit(0));
																	cells[11] = baseLink3Region.getCell(baseLink3Region.Heart(e3));
																	cells[10] = null;
																	cells[5] = baseLink3Region.getCell(baseLink3Blade2.nextSetBit(0));
																}
																else {
																	cells[4] = baseLink3Region.getCell(baseLink3Blade1.nextSetBit(0));
																	cells[5] = baseLink3Region.getCell(p5 = baseLink3Blade2.nextSetBit(0));
																	cells[10] = null;
																	cells[11] = baseLink3Region.getCell(baseLink3Blade2.nextSetBit(p5 + 1));
																}
															}
															else
																continue;
														if (baseLink3GroupedLinkOrdinal == 1)
															if (baseLink3Blade2.cardinality() == 1) {
																//baseLink3EmptyRegionBlades = true;
																if (baseLink3 == 0) {
																	cells[4] = baseLink3Region.getCell(baseLink3Blade2.nextSetBit(0));
																	cells[11] = baseLink3Region.getCell(baseLink3Region.Heart(e3));
																	cells[10] = null;
																	cells[5] = baseLink3Region.getCell(baseLink3Blade1.nextSetBit(0));	
																}
																else {
																	cells[4] = baseLink3Region.getCell(baseLink3Blade2.nextSetBit(0));
																	cells[5] = baseLink3Region.getCell(p6 = baseLink3Blade1.nextSetBit(0));
																	cells[10] = null;
																	cells[11] = baseLink3Region.getCell(baseLink3Blade1.nextSetBit(p6 + 1));
																}
															}
															else
																continue;
													}
													else {
														baseLink3GroupedLinkOrdinal = 1;
														cells[4] = baseLink3Region.getCell(p5 = baseLink3Blade1.nextSetBit(0));
														cells[10] = baseLink3Region.getCell(baseLink3Blade1.nextSetBit(p5 + 1));
														cells[5] = baseLink3Region.getCell(p6 = baseLink3Blade2.nextSetBit(0));
														cells[11] = baseLink3Region.getCell(baseLink3Blade2.nextSetBit(p6 + 1));
														//The following is to extract the special case of reduced & equivalent EmL 3 , 4
														EmL3 = true;
														if (e3 > 8 && baseLink3RegionPotentialsC == 4 && isSameLine(cells[4],cells[5]) && isSameLine(cells[10],cells[11]) ) {
															EmL3 = true;
														}
													}									
												}
												else {
													baseLink3GroupedLinkOrdinal = 1;
													cells[4] = baseLink3Region.getCell(p6 = baseLink3RegionPotentials.nextSetBit(0));
													cells[5] = baseLink3Region.getCell(baseLink3RegionPotentials.nextSetBit(p6 + 1));
													if (baseLink3 == 0 && isSameLine(cells[4],cells[5]))
														continue;
												}
												//baseRegion.getCell which hasCellPotentialValue can occur once in all base regions
												//For a strong link a region would have only 2 cells
												//For a grouped Strong link in box there would be a maximum of 5 cells
												//For a grouped strong link in a line there would be a maximum of 6 cells
												//We have more than one shared region so we need to check for non reptition. heart cells can appear in more than
												//region shared region and would be the equivalent of a potential elimination therefore it is easier to omit them from the check
												//if they are kept in the check then it will generate a next = true 
												Cell[] baseRegionsCells = new Cell[18];
												Cell[] emptyRegionsCells = new Cell[18];
												int j = 0;
												int k = 0;
			//For further enhancement these checks may need to happen earlier	
												for (int i = 0; i < 9 ; i++) {
													Cell digitCell = baseLink1Region.getCell(i);
													if (grid.hasCellPotentialValue(digitCell.getIndex(), digit)) {
														baseRegionsCells[j++] = digitCell;
														if (baseLink1EmptyRegion)
															emptyRegionsCells[k++] = digitCell;
													}
													digitCell = baseLink2Region.getCell(i);
													if (grid.hasCellPotentialValue(digitCell.getIndex(), digit)) {
														baseRegionsCells[j++] = digitCell;
														if (baseLink2EmptyRegion)
															emptyRegionsCells[k++] = digitCell;											
													}
													digitCell = baseLink3Region.getCell(i);
													if (grid.hasCellPotentialValue(digitCell.getIndex(), digit)) {
														baseRegionsCells[j++] = digitCell;
														if (baseLink3EmptyRegion)
															emptyRegionsCells[k++] = digitCell;											
													}
												}
												boolean next = false;
												for (int i = 0; i < j - 1 && !next; i++) {
													for (k = i + 1; k < j; k++) {
														if (baseRegionsCells[i].equals(baseRegionsCells[k])) {
															next = true;
															break;
														}
													}
												}
												if (next) continue;
												Grid.Region shareRegion1, shareRegion2;
												Cell start1, bridge11, bridge12, end3, bridge21, bridge22, start1Support, end3Support;
												for (int i = 0; i < 2; i++) {
													/*if (i == 0 && baseLink1EmptyRegion)
														continue;*/
													for (j = 2; j < 4; j++) {
														/*if (j == 2 && baseLink2EmptyRegion)
															continue;*/
														for (k = 4; k < 6; k++) {
															/*if (k == 4 && baseLink3EmptyRegion)
																continue;*/								
															if ((shareRegion1 = shareRegionOf(grid,
																bridge11 = cells[1 - i],
																cells[1 - i + 6],
																bridge12 = cells[j],
																cells[j + 6]
																)) != null &&
																(shareRegion2 = shareRegionOf(grid,
																bridge21 = cells[5 - j],
																cells[5 - j + 6],
																bridge22 = cells[k],
																cells[k + 6]
																)) != null &&
																// Check if following 2 lines are redundant (recurs twice)					
																!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
																!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region)
																&& !shareRegionsIntersection(grid, shareRegion1, shareRegion2, digit, heartCells)
																) {
																// 3 strong-linked Turbot fish found
																start1 = cells[i];
																end3 = cells[9 - k];
																start1Support = cells[i + 6];
																end3Support = cells[9 - k + 6];
																ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
																		baseLink1Region, baseLink2Region, shareRegion1, end3, bridge21, bridge22, baseLink3Region, shareRegion2, baseLink1, baseLink2, baseLink3, baseLink1EmptyRegion, baseLink2EmptyRegion, baseLink3EmptyRegion, start1Support, end3Support, emptyRegionsCells, (e1 > 8 || e2 > 8 || e3 > 8 ? true : false));
																if (hint.isWorth())
																	result.add(hint);
															}
															if ((shareRegion1 = shareRegionOf(grid,
																bridge11 = cells[1 - i],
																cells[1 - i + 6],
																bridge12 = cells[k],
																cells[k + 6]
																)) != null &&
																(shareRegion2 = shareRegionOf(grid,
																bridge21 = cells[9 - k],
																cells[9 - k + 6],
																bridge22 = cells[j],
																cells[j + 6]
																)) != null &&
																!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
																!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region)
																&& !shareRegionsIntersection(grid, shareRegion1, shareRegion2, digit, heartCells)
																) {
																// 3 strong-linked Turbot fish found
																start1 = cells[i];
																end3 = cells[5 - j];
																start1Support = cells[i + 6];
																end3Support = cells[5 - j + 6];
																ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
																		baseLink1Region, baseLink3Region, shareRegion1, end3, bridge21, bridge22, baseLink2Region, shareRegion2, baseLink1, baseLink3, baseLink2, baseLink1EmptyRegion, baseLink3EmptyRegion, baseLink2EmptyRegion, start1Support, end3Support, emptyRegionsCells, (e1 > 8 || e2 > 8 || e3 > 8 ? true : false));
																if (hint.isWorth())
																	result.add(hint);
															}
															if ((shareRegion1 = shareRegionOf(grid,
																bridge11 = cells[5 - j],
																cells[5 - j + 6],
																bridge12 = cells[i],
																cells[i + 6]
																)) != null &&
																(shareRegion2 = shareRegionOf(grid,
																bridge21 = cells[1 - i],
																cells[1 - i + 6],
																bridge22 = cells[k],
																cells[k + 6]
																)) != null &&
																!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
																!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region) 
																&& !shareRegionsIntersection(grid, shareRegion1, shareRegion2, digit, heartCells)
																) {
																// 3 strong-linked Turbot fish found
																start1 = cells[j];
																end3 = cells[9 - k];
																start1Support = cells[j + 6];
																end3Support = cells[9 - k + 6];
																ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
																		baseLink2Region, baseLink1Region, shareRegion1, end3, bridge21, bridge22, baseLink3Region, shareRegion2, baseLink2, baseLink1, baseLink3, baseLink2EmptyRegion, baseLink1EmptyRegion, baseLink3EmptyRegion, start1Support, end3Support, emptyRegionsCells, (e1 > 8 || e2 > 8 || e3 > 8 ? true : false));
																if (hint.isWorth())
																	result.add(hint);
															} // if sharedRegion () && sharedRegion () && sharedRegion != bbaseLink regions										
															if (EmL3 && k == 5) {
																if (cells[5].equals(baseLink3Region.getCell(baseLink3Blade2.nextSetBit(0)))) {
																	cells[5] = baseLink3Region.getCell(baseLink3Blade1.nextSetBit(p5 + 1));
																	cells[10] = baseLink3Region.getCell(baseLink3Blade2.nextSetBit(0));
																	k = 3;
																}
																else {
																	cells[10] = baseLink3Region.getCell(baseLink3Blade1.nextSetBit(p5 + 1));
																	cells[5] = baseLink3Region.getCell(baseLink3Blade2.nextSetBit(0));																	
																}
															}
														} // for int k = 4
														if (EmL2 && j == 3) {
															if (cells[3].equals(baseLink2Region.getCell(baseLink2Blade2.nextSetBit(0)))) {
																cells[3] = baseLink2Region.getCell(baseLink2Blade1.nextSetBit(p3 + 1));
																cells[8] = baseLink2Region.getCell(baseLink2Blade2.nextSetBit(0));
																j = 1;
															}
															else {
																cells[8] = baseLink2Region.getCell(baseLink2Blade1.nextSetBit(p3 + 1));
																cells[3] = baseLink2Region.getCell(baseLink2Blade2.nextSetBit(0));																
															}
														}
													} // for int j = 2
													//The special case can infer a strong link in rows or columns
													if (EmL1 && i == 1) {
														if (cells[1].equals(baseLink1Region.getCell(baseLink1Blade2.nextSetBit(0)))) {
															cells[1] = baseLink1Region.getCell(baseLink1Blade1.nextSetBit(p1 + 1));
															cells[6] = baseLink1Region.getCell(baseLink1Blade2.nextSetBit(0));
															i = -1;
														}
														else {
															cells[6] = baseLink1Region.getCell(baseLink1Blade1.nextSetBit(p1 + 1));
															cells[1] = baseLink1Region.getCell(baseLink1Blade2.nextSetBit(0));
														}
													}
												} // for int i = 0..2
											}//for (int baseLink3GroupedLinkOrdinal
										}//if (baseLink3RegionPotentialsC >= 2){
										cells[4] = null;
										cells[5] = null;
									} //i3
								}//for (int baseLink2GroupedLinkOrdinal
							}//if (baseLink2RegionPotentialsC >= 2)
							cells[2] = null;
							cells[3] = null;
						} //i2
					}//for (int baseLink1GroupedLinkOrdinal
				}//if (baseLink1RegionPotentialsC >= 2)
				cells[0] = null;
				cells[1] = null;
            } //i1
        } //digit
		return result;
    }

    private ThreeStrongLinksHint createHint(Grid grid, int value, Cell start1, Cell bridgeCell11, Cell bridgeCell12,
            Grid.Region baseLink1Set, Grid.Region baseLink2Set, Grid.Region shareRegion1, Cell end3, Cell bridgeCell21, Cell bridgeCell22,
            Grid.Region baseLink3Set, Grid.Region shareRegion2, int baseLink1, int baseLink2, int baseLink3, boolean baseLink1EmptyRegion, boolean baseLink2EmptyRegion, boolean baseLink3EmptyRegion, Cell start1Support, Cell end3Support, Cell[] emptyRegionsCells, boolean EmL34) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
		int eliminationsTotal = 0;
        CellSet victims = new CellSet(start1.getVisibleCells());
        victims.retainAll(end3.getVisibleCells());
		if (baseLink1EmptyRegion && start1Support != null) {
			victims.retainAll(start1Support.getVisibleCells());
		}		
		if (baseLink3EmptyRegion && end3Support != null) {
			victims.retainAll(end3Support.getVisibleCells());
		}
        victims.remove(start1);
        victims.remove(end3);
		//victims.removeAll(baseLink1Set.getCellSet());
		//victims.removeAll(baseLink2Set.getCellSet());
		//victims.removeAll(baseLink3Set.getCellSet());
		//victims.removeAll(shareRegion1.getCellSet());
		//victims.removeAll(shareRegion2.getCellSet());
		victims.bits.andNot(baseLink1Set.regionCellsBitSet);
		victims.bits.andNot(baseLink2Set.regionCellsBitSet);
		victims.bits.andNot(baseLink3Set.regionCellsBitSet);
		victims.bits.andNot(shareRegion1.regionCellsBitSet);
		victims.bits.andNot(shareRegion2.regionCellsBitSet);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value)){
                removablePotentials.put(cell, SingletonBitSet.create(value));
				eliminationsTotal++;
			}
        }
/*		int j = 0;
		Cell[] emptyRectangleCells= new Cell[15];
		if (baseLink1EmptyRegion){
			for (int i = 0; i < 9 ; i++) {
				Cell CrossCell = baseLink1Set.getCell(i);
				if (grid.hasCellPotentialValue(CrossCell.getIndex(), value))
					emptyRectangleCells[j++] = CrossCell;
			}
		}
		if (baseLink2EmptyRegion){
			for (int i = 0; i < 9 ; i++) {
				Cell CrossCell = baseLink2Set.getCell(i);
				if (grid.hasCellPotentialValue(CrossCell.getIndex(), value))
					emptyRectangleCells[j++] = CrossCell;
			}
		}
		if (baseLink3EmptyRegion){
			for (int i = 0; i < 9 ; i++) {
				Cell CrossCell = baseLink3Set.getCell(i);
				if (grid.hasCellPotentialValue(CrossCell.getIndex(), value))
					emptyRectangleCells[j++] = CrossCell;
			}
		}*/
        // Create hint
        return new ThreeStrongLinksHint(this, removablePotentials,
                start1, bridgeCell11, bridgeCell12, value, baseLink1Set, baseLink2Set, shareRegion1, bridgeCell21, bridgeCell22, end3, baseLink3Set, shareRegion2, baseLink1, baseLink2, baseLink3, baseLink1EmptyRegion, baseLink2EmptyRegion, baseLink3EmptyRegion, emptyRegionsCells, eliminationsTotal, EmL34);
    }

    @Override
    public String toString() {
        return "3-link Turbot Fishes";
    }
}
