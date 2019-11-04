package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


/**
 * Implementation of Turbot Fish technique solver.
 */
public class TurbotFish implements IndirectHintProducer {

    @Override
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
		int[][] Sets = {
			// Skyscrapers
			{1, 1},
			{2, 2},
			// Two-string Kites
			{2, 1},
			// Turbot Fishes and Empty Rectangles
//Not needed as simpler techniques will always catch the same possible eliminations (e.g. box line elimination)
//			{0, 0},			
			{1, 0},
			{2, 0}
		};
		List<TurbotFishHint> hintsFinal = new ArrayList<TurbotFishHint>();
		List<TurbotFishHint> hintsStart;
        for (int i = 0; i < 5 ; i++) {	
			hintsStart = getHints(grid, Sets[i][0], Sets[i][1]);
			for (TurbotFishHint hint : hintsStart)
				hintsFinal.add(hint);
		}
		// Sort the result
		Collections.sort(hintsFinal, new Comparator<TurbotFishHint>() {
			public int compare(TurbotFishHint h1, TurbotFishHint h2) {
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
		for (TurbotFishHint hint : hintsFinal)
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
			else if (bridge2.getX() == bridge1Support.getX())
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
			else if (bridge2.getY() == bridge1Support.getY())
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
			else if (bridge2.getB() == bridge1Support.getB())
				return (Grid.Block)Grid.getRegionAt(0,bridge1.getIndex());		
        } 
		return null;
    }

    private List<TurbotFishHint> getHints(Grid grid, int base, int cover)
            /*throws InterruptedException*/ {
		List<TurbotFishHint> result = new ArrayList<TurbotFishHint>();
		int e1 = 0;
		int e2 = 0;
        for (int digit = 1; digit <= 9; digit++) {
			boolean coverEmptyRegion = false;
			boolean coverEmptyRegionBlades = false;	
            Grid.Region[] baseRegions = Grid.getRegions(base);
            Grid.Region[] coverRegions = Grid.getRegions(cover);
            for (int i1 = 0; i1 < baseRegions.length; i1++) {
				Grid.Region baseRegion = baseRegions[i1];
				BitSet baseRegionPotentials = baseRegion.getPotentialPositions(grid, digit);
				int baseRegionPotentialsCardinality = baseRegionPotentials.cardinality();
				if (baseRegionPotentialsCardinality >= 2){
					if (baseRegionPotentialsCardinality > 6 || (base == 0 && baseRegionPotentialsCardinality > 5))
						continue;
					boolean baseEmptyRegion = false;
					boolean baseEmptyRegionBlades = false;
					BitSet baseBlade1 = (BitSet)baseRegionPotentials.clone();
					BitSet baseBlade2 = (BitSet)baseRegionPotentials.clone();
					Cell[] heartCells= new Cell[2];
					if (baseRegionPotentialsCardinality > 2) {
						//Grouped Strong links in box have 15 configurations but only 9 (the ER configurations) would be useful in 2 strong links patterns
						for (e1 = 0; e1 < (base < 1 ? 9 : 3); e1++) {
							heartCells[0] = (base == 0 ? baseRegion.getCell(e1): null);
							BitSet baseEmptyArea = (BitSet)baseRegionPotentials.clone();
							baseBlade1 = (BitSet)baseRegionPotentials.clone();
							baseBlade2 = (BitSet)baseRegionPotentials.clone();
							if (base == 0) {
								//confirm if we have an empty rectangle
								//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
								//9 configurations for each block depending on "Heart" cell
								baseEmptyArea.and(baseRegion.Rectangle(e1));
							}
							else {
								baseEmptyArea.and(baseRegion.lineEmptyCells(e1));
							}
							if (baseEmptyArea.cardinality() == 0) {
								if (base == 0) {
									//confirm if we have an empty rectangle
									//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
									//9 configurations for each block depending on "Heart" cell
									baseBlade1.and(baseRegion.crossBlade1(e1));
									baseBlade2.and(baseRegion.crossBlade2(e1));
								}
								else {
									baseBlade1.and(baseRegion.lineBlade1(e1));
									baseBlade2.and(baseRegion.lineBlade2(e1));
								}			
								//Empty Rectangle configuration found
								//4 "Cross" cells are 2 "Blade1" Cells in a row and 2 "Blade2" Cells in a column
								//if Blade1 Cardinality or Blade2 Cardinality = 0 then configuration not useful
								if (baseBlade1.cardinality() > 0 && baseBlade2.cardinality() > 0)
									baseEmptyRegion = true;
								//There is only 1 useful configuration of Empty rectangle if baseRegionPotentialsCardinality > 2
								break;
							}
						}
						if (!baseEmptyRegion)
							continue;
					}
					// Strong link found
					// process cells of strong link to deliver a start and bridge cell
					int p1, p2;
					for (int baseGroupedLinkOrdinal = 0; baseGroupedLinkOrdinal < 2; baseGroupedLinkOrdinal++) {
						Cell[] cells = new Cell[8];
						// region 1
						//initialize as any cell
						//start cell supporting cell (if grouped)
						cells[4] = null;
						//bridge cell supporting cell (if grouped)
						cells[5] = null;
						// region 1
						if (baseEmptyRegion) {
							if (baseBlade1.cardinality() == 1 || baseBlade2.cardinality() == 1) {
								if (baseGroupedLinkOrdinal == 0)
									if (baseBlade1.cardinality() == 1) {
										baseEmptyRegionBlades = true;
										if (base == 0) {
											cells[0] = baseRegion.getCell(baseBlade1.nextSetBit(0));
											cells[1] = baseRegion.getCell(baseRegion.Heart(e1));
											cells[4] = null;
											cells[5] = baseRegion.getCell(baseBlade2.nextSetBit(0));
										}
										else {
											cells[0] = baseRegion.getCell(baseBlade1.nextSetBit(0));
											cells[1] = baseRegion.getCell(p1 = baseBlade2.nextSetBit(0));
											cells[4] = null;
											cells[5] = baseRegion.getCell(baseBlade2.nextSetBit(p1 + 1));
										}
									}
									else
										continue;
								if (baseGroupedLinkOrdinal == 1)
									if (baseBlade2.cardinality() == 1) {
										baseEmptyRegionBlades = true;
										if (base == 0) {
											cells[0] = baseRegion.getCell(baseBlade2.nextSetBit(0));
											cells[1] = baseRegion.getCell(baseRegion.Heart(e1));
											cells[4] = null;
											cells[5] = baseRegion.getCell(baseBlade1.nextSetBit(0));	
										}
										else {
											cells[0] = baseRegion.getCell(baseBlade2.nextSetBit(0));
											cells[1] = baseRegion.getCell(p2 = baseBlade1.nextSetBit(0));
											cells[4] = null;
											cells[5] = baseRegion.getCell(baseBlade1.nextSetBit(p2 + 1));
										}
									}
									else
										continue;
							}
							else {
								baseGroupedLinkOrdinal = 1;
								cells[0] = baseRegion.getCell(p2 = baseBlade1.nextSetBit(0));
								cells[4] = baseRegion.getCell(baseBlade1.nextSetBit(p2 + 1));
								cells[1] = baseRegion.getCell(p2 = baseBlade2.nextSetBit(0));
								cells[5] = baseRegion.getCell(baseBlade2.nextSetBit(p2 + 1));
							}									
						}
						else {
							baseGroupedLinkOrdinal = 1;
							cells[0] = baseRegion.getCell(p2 = baseRegionPotentials.nextSetBit(0));
							cells[1] = baseRegion.getCell(baseRegionPotentials.nextSetBit(p2 + 1));
						}
						for (int i2 = (base == cover ? i1+1 : 0); i2 < coverRegions.length; i2++) {
							// For each set in sets
							Grid.Region coverRegion = coverRegions[i2];
							BitSet coverRegionPotentials = coverRegion.getPotentialPositions(grid, digit);
							int coverRegionPotentialsCardinality = coverRegionPotentials.cardinality(); 
							// For a strong link Cardinality == 2 in region, for an empty rectangle region is a block 
							// with cardinality >2 (or it will be a strong link in a block i.e2 turbot fish ) 
							// and cardinality <6 (because we need 4 empty cells in the region.Rectangle cells)
							if (coverRegionPotentialsCardinality >= 2){
								if (coverRegionPotentialsCardinality > 6 || (cover == 0 && coverRegionPotentialsCardinality > 5))
									continue;
								coverEmptyRegion = false;
								coverEmptyRegionBlades = false;
								BitSet coverBlade1 = (BitSet)coverRegionPotentials.clone();
								BitSet coverBlade2 = (BitSet)coverRegionPotentials.clone();
								if (coverRegionPotentialsCardinality > 2) {
									for (e2 = 0; e2 < (cover < 1 ? 9 : 3); e2++) {
										heartCells[1] = (cover == 0 ? baseRegion.getCell(e2): null);
										BitSet coverEmptyArea = (BitSet)coverRegionPotentials.clone();
										coverBlade1 = (BitSet)coverRegionPotentials.clone();
										coverBlade2 = (BitSet)coverRegionPotentials.clone();
										if (cover == 0) {
											//confirm if we have an empty rectangle
											//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
											//9 configurations for each block depending on "Heart" cell
											coverEmptyArea.and(coverRegion.Rectangle(e2));
										}
										else {
											coverEmptyArea.and(coverRegion.lineEmptyCells(e2));
										}
										if (coverEmptyArea.cardinality() == 0) {
											if (cover == 0) {
												//confirm if we have an empty rectangle
												//block has 9 cells: 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
												//9 configurations for each block depending on "Heart" cell
												coverBlade1.and(coverRegion.crossBlade1(e2));
												coverBlade2.and(coverRegion.crossBlade2(e2));
											}
											else {
												coverBlade1.and(coverRegion.lineBlade1(e2));
												coverBlade2.and(coverRegion.lineBlade2(e2));
											}			
											//Empty Rectangle configuration found
											//4 "Cross" cells are 2 "Blade1" Cells in a row and 2 "Blade2" Cells in a column
											//if Blade1 Cardinality or Blade2 Cardinality = 0 then configuration not useful
											if (coverBlade1.cardinality() > 0 && coverBlade2.cardinality() > 0)
												coverEmptyRegion = true;
											//There is only 1 useful configuration of Empty rectangle if coverRegionPotentialsCardinality > 2
											break;
										}
									}
									if (!coverEmptyRegion)
										continue;
								}							
								// Strong link found
								// process to deliver bridge and end cells
								for (int coverGroupedLinkOrdinal = 0; coverGroupedLinkOrdinal < 2; coverGroupedLinkOrdinal++) {
									// region 2
									//initialize as any cell
									//bridge cell support cell if grouped
									cells[6] = null;
									//end cell support cell if grouped
									cells[7] = null;
									if (coverEmptyRegion) {
										if (coverBlade1.cardinality() == 1 || coverBlade2.cardinality() == 1) {
											if (coverGroupedLinkOrdinal == 0)
												if (coverBlade1.cardinality() == 1) {
													coverEmptyRegionBlades = true;
													if (cover == 0) {
														cells[2] = coverRegion.getCell(coverBlade1.nextSetBit(0));
														cells[3] = coverRegion.getCell(coverRegion.Heart(e2));
														cells[6] = null;
														cells[7] = coverRegion.getCell(coverBlade2.nextSetBit(0));
													}
													else {
														cells[2] = coverRegion.getCell(coverBlade1.nextSetBit(0));
														cells[3] = coverRegion.getCell(p2 = coverBlade2.nextSetBit(0));
														cells[6] = null;
														cells[7] = coverRegion.getCell(coverBlade2.nextSetBit(p2 + 1));
													}
												}
												else
													continue;
											if (coverGroupedLinkOrdinal == 1)
												if (coverBlade2.cardinality() == 1) {
													coverEmptyRegionBlades = true;
													if (cover == 0) {
														cells[2] = coverRegion.getCell(coverBlade2.nextSetBit(0));
														cells[3] = coverRegion.getCell(coverRegion.Heart(e2));
														cells[6] = null;
														cells[7] = coverRegion.getCell(coverBlade1.nextSetBit(0));	
													}
													else {
														cells[2] = coverRegion.getCell(coverBlade2.nextSetBit(0));
														cells[3] = coverRegion.getCell(p2 = coverBlade1.nextSetBit(0));
														cells[6] = null;
														cells[7] = coverRegion.getCell(coverBlade1.nextSetBit(p2 + 1));
													}
												}
												else
													continue;
										}
										else {
											coverGroupedLinkOrdinal = 1;
											cells[2] = coverRegion.getCell(p2 = coverBlade1.nextSetBit(0));
											cells[6] = coverRegion.getCell(coverBlade1.nextSetBit(p2 + 1));
											cells[3] = coverRegion.getCell(p2 = coverBlade2.nextSetBit(0));
											cells[7] = coverRegion.getCell(coverBlade2.nextSetBit(p2 + 1));
										}									
									}
									else {
										coverGroupedLinkOrdinal = 1;
										cells[2] = coverRegion.getCell(p2 = coverRegionPotentials.nextSetBit(0));
										cells[3] = coverRegion.getCell(coverRegionPotentials.nextSetBit(p2 + 1));
									}

									//baseRegion.getCell which hasCellPotentialValue can occur once in all base regions
									//For a strong link a region would have only 2 cells
									//For a grouped Strong link in box there would be a maximum of 5 cells
									//For a grouped strong link in a line there would be a maximum of 6 cells
									//No need to check for shared region cells as we have only one cover region
									Cell[] baseRegionsCells = new Cell[12];
									Cell[] emptyRegionCells = new Cell[12];
									int j = 0;
									int k = 0;
									for (int i = 0; i < 9 ; i++) {
										Cell digitCell = baseRegion.getCell(i);
										if (grid.hasCellPotentialValue(digitCell.getIndex(), digit)) {
											baseRegionsCells[j++] = digitCell;
											if (baseEmptyRegion)
												emptyRegionCells[k++] = digitCell;
										}
										digitCell = coverRegion.getCell(i);
										if (grid.hasCellPotentialValue(digitCell.getIndex(), digit)) {
											baseRegionsCells[j++] = digitCell;
											if (coverEmptyRegion)
												emptyRegionCells[k++] = digitCell;											
										}
									}
									boolean next = false;
									for (int i = 0; i < j - 1; i++) {
										for (k = i + 1; k < j; k++) {
											if (baseRegionsCells[i].equals(baseRegionsCells[k])) {
												next = true;
												break;
											}
										}
									}
									if (next) continue;
//===Check for redundancy Start //Previous check would appear to cover this too
/*									if (baseEmptyRegion) {
										if (base == 0)
											if (cells[1].getB() == cells[2].getB() || cells[1].getB() == cells[3].getB())
												break;
										if (base == 1)
											if (cells[1].getY() == cells[2].getY() || cells[1].getY() == cells[3].getY())
												break;
										if (base == 2)
											if (cells[1].getX() == cells[2].getX() || cells[1].getX() == cells[3].getX())
												break;
									}
									if (coverEmptyRegion) {
										if (cover == 0)
											if (cells[0].getB() == cells[3].getB() || cells[1].getB() == cells[3].getB())
												break;
										if (cover == 1)
											if (cells[0].getY() == cells[3].getY() || cells[1].getY() == cells[3].getY())
												break;
										if (cover == 2)
											if (cells[0].getX() == cells[3].getX() || cells[1].getX() == cells[3].getX())
												break;
									}*/
//===Check for redundancy End
									//Process to check shared region (weak link)
									Grid.Region shareRegion;
									Cell start, end, bridge1, bridge2, startSupport, endSupport;
									for (int i = 0; i < 2; i++) {
										for (j = 2; j < 4; j++) {
											if ((shareRegion = shareRegionOf(grid,
														bridge1 = cells[1 - i],
														cells[1 - i + 4],
														bridge2 = cells[j],
														cells[j + 4])) != null &&
													!shareRegion.equals(baseRegion) && !shareRegion.equals(coverRegion)) {
												// Turbot fish found
												start = cells[i];
												startSupport = cells[i + 4];
												end = cells[5 - j];
												endSupport = cells[5 - j + 4];
												TurbotFishHint hint = createHint(grid, digit, start, end, bridge1, bridge2,
														baseRegion, coverRegion, shareRegion, baseEmptyRegion, coverEmptyRegion, startSupport, endSupport, baseEmptyRegionBlades, coverEmptyRegionBlades, emptyRegionCells);
												if (hint.isWorth())
													result.add(hint);
											}
										} // for int j = 0..2
									} // for int i = 0..2
								}// for (int coverGroupedLinkOrdinal = 0
							} // if (coverRegionPotentialsCardinality >= 2)
						}//for (int i2
					}//for (int baseGroupedLinkOrdinal
				}//if (baseRegionPotentialsCardinality >= 2))
            }//for (int i1 = 0
        }
		return result;
    }

    private TurbotFishHint createHint(Grid grid, int value, Cell start, Cell end, Cell bridgeCell1, Cell bridgeCell2,
            Grid.Region baseSet, Grid.Region coverSet, Grid.Region shareRegion, boolean baseEmptyRegion, boolean coverEmptyRegion,  Cell startSupport, Cell endSupport, boolean baseEmptyRegionBlades, boolean coverEmptyRegionBlades, Cell[] emptyRegionCells) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
		int eliminationsTotal = 0;
        //Set<Cell> victims = new LinkedHashSet<>(start.getVisibleCells());
		CellSet victims = new CellSet(start.getVisibleCells());
        victims.retainAll(end.getVisibleCells());
		if (baseEmptyRegion && startSupport != null) {
			victims.retainAll(startSupport.getVisibleCells());
		}		
		if (coverEmptyRegion && endSupport != null) {
			victims.retainAll(endSupport.getVisibleCells());
		}
        victims.remove(start);
        victims.remove(end);
		//victims.removeAll(coverSet.getCellSet());
		//victims.removeAll(baseSet.getCellSet());
		victims.bits.andNot(coverSet.regionCellsBitSet);
		victims.bits.andNot(baseSet.regionCellsBitSet);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value)) {
				eliminationsTotal++;
                removablePotentials.put(cell, SingletonBitSet.create(value));
			}
        }
        // Create hint
        return new TurbotFishHint(this, removablePotentials,
                start, end, bridgeCell1, bridgeCell2, value, baseSet, coverSet, shareRegion, baseEmptyRegion, coverEmptyRegion, /*baseEmptyRegionBlades, coverEmptyRegionBlades,*/ emptyRegionCells, eliminationsTotal);
    }

    @Override
    public String toString() {
        return "Turbot Fishes";
    }
}
