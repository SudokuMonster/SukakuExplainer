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
            Cell bridge1, Cell bridge2) {
        if (bridge1.getX() == bridge2.getX()) {
            return (Grid.Column)Grid.getRegionAt(2,bridge1.getIndex());
        } else if (bridge1.getY() == bridge2.getY()) {
            return (Grid.Row)Grid.getRegionAt(1,bridge1.getIndex());
        } else if (bridge1.getB() == bridge2.getB()) {
            return (Grid.Block)Grid.getRegionAt(0,bridge1.getIndex());
        } else return null;
    }


	private boolean isSameLine (Cell lineCell1, Cell lineCell2) {
		if (lineCell1.getX() == lineCell2.getX() || lineCell1.getY() == lineCell2.getY())
			return true;
		return false;
	}

    private List<ThreeStrongLinksHint> getHints(Grid grid, int baseLink1, int baseLink2, int baseLink3)
            /*throws InterruptedException*/ {
		List<ThreeStrongLinksHint> result = new ArrayList<ThreeStrongLinksHint>();
        Cell[] cells = new Cell[6];
		for (int digit = 1; digit <= 9; digit++) {
            Grid.Region[] baseLink1Regions = grid.getRegions(baseLink1);
            Grid.Region[] baseLink2Regions = grid.getRegions(baseLink2);
            Grid.Region[] baseLink3Regions = grid.getRegions(baseLink3);
            int p1, p2, p3;
			int e1 = 0;
			int e2 = 0;
			int e3 = 0;
			Cell regionCell1, regionCell2;
			for (int i1 = 0; i1 < baseLink1Regions.length; i1++) {
				boolean emptyRectangle1 = false;
				Grid.Region baseLink1Region = baseLink1Regions[i1];
				BitSet baseLink1RegionPotentials = baseLink1Region.getPotentialPositions(grid, digit);
				int baseLink1RegionPotentialsC = baseLink1RegionPotentials.cardinality();
				if (baseLink1RegionPotentialsC < 2)
					continue;	
				if (baseLink1RegionPotentialsC != 2 && (baseLink1 > 0 || baseLink1RegionPotentialsC > 5))
					continue;
					if (baseLink1RegionPotentialsC > 2) {
						for (e1 = 0; e1 < 9; e1++) {
							BitSet rectangle = (BitSet)baseLink1RegionPotentials.clone();
							//BitSet cross = (BitSet)baseLink1RegionPotentials.clone();
							rectangle.and(baseLink1Region.Rectangle(e1));
							//cross.and(baseLink1Region.Cross(e1));
							if (rectangle.cardinality() == 0 /*&& cross.cardinality() > 2*/ ) {
								emptyRectangle1 = true;
								break;
							}
						}
						if (!emptyRectangle1)
							continue;
					}	
				// region 1
				if (emptyRectangle1) {
					regionCell1 = baseLink1Region.getCell(baseLink1Region.Heart(e1));
					regionCell2 = baseLink1Region.getCell(baseLink1Region.Heart(e1));										
				}
				else {
					regionCell1 = baseLink1Region.getCell(p1 = baseLink1RegionPotentials.nextSetBit(0));
					regionCell2 = baseLink1Region.getCell(baseLink1RegionPotentials.nextSetBit(p1 + 1));
					if (baseLink1 == 0 && isSameLine(regionCell1,regionCell2))
						continue;
				}
				cells[0] = regionCell1;
				cells[1] = regionCell2;
                for (int i2 = (baseLink1 == baseLink2 ? i1+1 : 0); i2 < baseLink2Regions.length; i2++) {
					boolean emptyRectangle2 = false;
					Grid.Region baseLink2Region = baseLink2Regions[i2];
					BitSet baseLink2RegionPotentials = baseLink2Region.getPotentialPositions(grid, digit);
					int baseLink2RegionPotentialsC = baseLink2RegionPotentials.cardinality();
					if (baseLink2RegionPotentialsC < 2)
						continue;
					// For a strong link Cardinality == 2 in region, for an empty rectangle region is a block 
					// with cardinality >2 (or it will be a strong link in a block i.e turbot fish ) 
					// and cardinality <6 (because we need 4 empty cells in the region.Rectangle cells)
					if (baseLink2RegionPotentialsC != 2 && (baseLink2 > 0 || baseLink2RegionPotentialsC > 5))
						continue;
						if (baseLink2RegionPotentialsC > 2) {
							for (e2 = 0; e2 < 9; e2++) {
								BitSet rectangle = (BitSet)baseLink2RegionPotentials.clone();
								rectangle.and(baseLink2Region.Rectangle(e2));
								//confirm if we have an empty rectangle
								//block has 9 cells 4 "Cross" cells, 4 "Rectangle" cells and 1 "Heart" cell
								//9 configurations for each block depending on "Heart" cell
								if (rectangle.cardinality() == 0) {
									emptyRectangle2 = true;
									break;
								}
							}
							if (!emptyRectangle2)
								continue;
						}	
					// region 2
					if (emptyRectangle2) {
						regionCell1 = baseLink2Region.getCell(baseLink2Region.Heart(e2));
						regionCell2 = baseLink2Region.getCell(baseLink2Region.Heart(e2));										
					}
					else {
						regionCell1 = baseLink2Region.getCell(p2 = baseLink2RegionPotentials.nextSetBit(0));
						regionCell2 = baseLink2Region.getCell(baseLink2RegionPotentials.nextSetBit(p2 + 1));
						if (baseLink2 == 0 && isSameLine(regionCell1,regionCell2))
							continue;
					}
					cells[2] = regionCell1;
					cells[3] = regionCell2;
					for (int i3 = (baseLink2 == baseLink3 ? i2+1 : (baseLink1 == baseLink3 ? i1+1 : 0)); i3 < baseLink3Regions.length; i3++) {
						boolean emptyRectangle3 = false;
						Grid.Region baseLink3Region = baseLink3Regions[i3];
						BitSet baseLink3RegionPotentials = baseLink3Region.getPotentialPositions(grid, digit);
						int baseLink3RegionPotentialsC = baseLink3RegionPotentials.cardinality();
						if (baseLink3RegionPotentialsC < 2)
							continue;
						if (baseLink3RegionPotentialsC != 2 && (baseLink3 > 0 || baseLink3RegionPotentialsC > 5))
							continue;
							if (baseLink3RegionPotentialsC > 2) {
								for (e3 = 0; e3 < 9; e3++) {
									BitSet rectangle = (BitSet)baseLink3RegionPotentials.clone();
									//BitSet cross = (BitSet)baseLink3RegionPotentials.clone();
									rectangle.and(baseLink3Region.Rectangle(e3));
									//cross.and(baseLink3Region.Cross(e3));
									if (rectangle.cardinality() == 0 /*&& cross.cardinality() > 2*/ ) {
										emptyRectangle3 = true;
										break;
									}
								}
								if (!emptyRectangle3)
									continue;
							}	
						// region 3
						if (emptyRectangle3) {
							regionCell1 = baseLink3Region.getCell(baseLink3Region.Heart(e3));
							regionCell2 = baseLink3Region.getCell(baseLink3Region.Heart(e3));										
						}
						else {
							regionCell1 = baseLink3Region.getCell(p3 = baseLink3RegionPotentials.nextSetBit(0));
							regionCell2 = baseLink3Region.getCell(baseLink3RegionPotentials.nextSetBit(p3 + 1));
							if (baseLink3 == 0 && isSameLine(regionCell1,regionCell2))
							continue;
						}
						cells[4] = regionCell1;
						cells[5] = regionCell2;
						boolean next = false;
						for (int i = 0; i < 4; i++) {
							if ((i == 1 && emptyRectangle1) || (i == 3 && emptyRectangle2))
								continue;
							for (int j = i + 1; j < 5; j++) {
								if ((j == 1 && emptyRectangle1) || (j == 3 && emptyRectangle2))
									continue;
								for (int k = j + 1; k < 6; k++) {
									if ((k == 3 && emptyRectangle2) || (k == 5 && emptyRectangle3))
										continue;
									if (cells[i].equals(cells[j]) || cells[i].equals(cells[k]) || cells[j].equals(cells[k])) {
										next = true;
										break;
									}
								}
							}
						}
						if (next) {
							cells[4] = null;
							cells[5] = null;
							continue;
						}
						if (emptyRectangle1)
							if (cells[0].getB() == cells[2].getB() || cells[0].getB() == cells[3].getB() || cells[0].getB() == cells[4].getB() || cells[0].getB() == cells[5].getB())
								continue;
						if (emptyRectangle2)
							if (cells[2].getB() == cells[0].getB() || cells[2].getB() == cells[1].getB() || cells[2].getB() == cells[4].getB() || cells[2].getB() == cells[5].getB())
								continue;
						if (emptyRectangle3)
							if (cells[4].getB() == cells[0].getB() || cells[4].getB() == cells[1].getB() || cells[4].getB() == cells[2].getB() || cells[4].getB() == cells[3].getB())
								continue;						
						Grid.Region shareRegion1, shareRegion2;
						Cell start1, bridge11, bridge12, end2, bridge21, bridge22;
						for (int i = 0; i < 2; i++) {
							if (i == 0 && emptyRectangle1)
								continue;
							for (int j = 2; j < 4; j++) {
								if (j == 2 && emptyRectangle2)
									continue;
								for (int k = 4; k < 6; k++) {
									if (k == 4 && emptyRectangle3)
										continue;									
									if ((shareRegion1 = shareRegionOf(grid,
										bridge11 = cells[1 - i],
										bridge12 = cells[j]
										)) != null &&
										(shareRegion2 = shareRegionOf(grid,
										bridge21 = cells[5 - j],
										bridge22 = cells[k]
										)) != null &&
										!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
										!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region) ) {
										// 3 strong-linked Turbot fish found
										start1 = cells[i];
										end2 = cells[9 - k];
										ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
												baseLink1Region, baseLink2Region, shareRegion1, end2, bridge21, bridge22, baseLink3Region, shareRegion2, baseLink1, baseLink2, baseLink3, emptyRectangle1, emptyRectangle2, emptyRectangle3);
										if (hint.isWorth())
											result.add(hint);
									}
									if ((shareRegion1 = shareRegionOf(grid,
										bridge11 = cells[1 - i],
										bridge12 = cells[k]
										)) != null &&
										(shareRegion2 = shareRegionOf(grid,
										bridge21 = cells[9 - k],
										bridge22 = cells[j]
										)) != null &&
										!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
										!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region) ) {
										// 3 strong-linked Turbot fish found
										start1 = cells[i];
										end2 = cells[5 - j];
										ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
												baseLink1Region, baseLink3Region, shareRegion1, end2, bridge21, bridge22, baseLink2Region, shareRegion2, baseLink1, baseLink3, baseLink2, emptyRectangle1, emptyRectangle3, emptyRectangle2);
										if (hint.isWorth())
											result.add(hint);
									}
									if ((shareRegion1 = shareRegionOf(grid,
										bridge11 = cells[5 - j],
										bridge12 = cells[i]
										)) != null &&
										(shareRegion2 = shareRegionOf(grid,
										bridge21 = cells[1 - i],
										bridge22 = cells[k]
										)) != null &&
										!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
										!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region) ) {
										// 3 strong-linked Turbot fish found
										start1 = cells[j];
										end2 = cells[9 - k];
										ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
												baseLink2Region, baseLink1Region, shareRegion1, end2, bridge21, bridge22, baseLink3Region, shareRegion2, baseLink2, baseLink1, baseLink3, emptyRectangle2, emptyRectangle1, emptyRectangle3);
										if (hint.isWorth())
											result.add(hint);
									} // if sharedRegion () && sharedRegion () && sharedRegion != bbaseLink regions										
								} // for int k = 0..2
							} // for int j = 0..2
						} // for int i = 0..2
						cells[4] = null;
						cells[5] = null;
					} //i3
					cells[2] = null;
					cells[3] = null;
				} //i2
				cells[0] = null;
				cells[1] = null;
            } //i1
        } //digit
		return result;
    }

    private ThreeStrongLinksHint createHint(Grid grid, int value, Cell start1, Cell bridgeCell11, Cell bridgeCell12,
            Grid.Region baseLink1Set, Grid.Region baseLink2Set, Grid.Region shareRegion1, Cell end2, Cell bridgeCell21, Cell bridgeCell22,
            Grid.Region baseLink3Set, Grid.Region shareRegion2, int baseLink1, int baseLink2, int baseLink3, boolean emptyRectangle1, boolean emptyRectangle2, boolean emptyRectangle3) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
		int eliminationsTotal = 0;
        CellSet victims = new CellSet(start1.getVisibleCells());
        victims.retainAll(end2.getVisibleCells());
        victims.remove(start1);
        victims.remove(end2);
		victims.removeAll(baseLink1Set.getCellSet());
		victims.removeAll(baseLink2Set.getCellSet());
		victims.removeAll(baseLink3Set.getCellSet());
		victims.removeAll(shareRegion1.getCellSet());
		victims.removeAll(shareRegion2.getCellSet());
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value)){
                removablePotentials.put(cell, SingletonBitSet.create(value));
				eliminationsTotal++;
			}
        }
		int j = 0;
		Cell[] emptyRectangleCells= new Cell[15];
		if (emptyRectangle1){
			for (int i = 0; i < 9 ; i++) {
				Cell CrossCell = baseLink1Set.getCell(i);
				if (grid.hasCellPotentialValue(CrossCell.getIndex(), value))
					emptyRectangleCells[j++] = CrossCell;
			}
		}
		if (emptyRectangle2){
			for (int i = 0; i < 9 ; i++) {
				Cell CrossCell = baseLink2Set.getCell(i);
				if (grid.hasCellPotentialValue(CrossCell.getIndex(), value))
					emptyRectangleCells[j++] = CrossCell;
			}
		}
		if (emptyRectangle3){
			for (int i = 0; i < 9 ; i++) {
				Cell CrossCell = baseLink3Set.getCell(i);
				if (grid.hasCellPotentialValue(CrossCell.getIndex(), value))
					emptyRectangleCells[j++] = CrossCell;
			}
		}
        // Create hint
        return new ThreeStrongLinksHint(this, removablePotentials,
                start1, bridgeCell11, bridgeCell12, value, baseLink1Set, baseLink2Set, shareRegion1, bridgeCell21, bridgeCell22, end2, baseLink3Set, shareRegion2, baseLink1, baseLink2, baseLink3, emptyRectangle1, emptyRectangle2, emptyRectangle3, emptyRectangleCells, eliminationsTotal);
    }

    @Override
    public String toString() {
        return "3-link Turbot Fishes";
    }
}
