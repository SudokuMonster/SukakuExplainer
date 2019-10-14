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
        // 3 parallel strong links
        getHints(grid, accu, 1, 1, 1);
        getHints(grid, accu, 2, 2, 2);
		// 3 strong links in same Lines
		//with boxes
        getHints(grid, accu, 0, 0, 0); 
		getHints(grid, accu, 0, 0, 1);        
        getHints(grid, accu, 0, 0, 2);		
        getHints(grid, accu, 0, 1, 1);			
		getHints(grid, accu, 0, 2, 2);		
		// 3 strong links with box(es)
		//mixed lines
        getHints(grid, accu, 0, 1, 2);		
		getHints(grid, accu, 1, 1, 2);		
        getHints(grid, accu, 1, 2, 2);		
    }

    private Grid.Region shareRegionOf(Grid grid,
            Cell start, Cell bridge1, Cell bridge2, Cell end) {
        if (bridge1.getX() == bridge2.getX()) {
            return Grid.columns[bridge1.getX()];
        } else if (bridge1.getY() == bridge2.getY()) {
            return Grid.rows[bridge1.getY()];
        } else if (bridge1.getB() == bridge2.getB()) {
            return Grid.blocks[bridge1.getB()];
        } else return null;
    }
	
	private boolean isSamebox (Cell boxCell1, Cell boxCell2) {
		if (boxCell1.getB() == boxCell2.getB())
			return true;
		return false;
	}

    private void getHints(Grid grid, HintsAccumulator accu,
            int baseLink1, int baseLink2, int baseLink3)
            throws InterruptedException {
        Cell[] cells = new Cell[6];
		for (int digit = 1; digit <= 9; digit++) {
            Grid.Region[] baseLink1Regions = grid.getRegions(baseLink1);
            Grid.Region[] baseLink2Regions = grid.getRegions(baseLink2);
            Grid.Region[] baseLink3Regions = grid.getRegions(baseLink3);
            int p1, p2, p3;
			for (int i1 = 0; i1 < baseLink1Regions.length; i1++) {
				Grid.Region baseLink1Region = baseLink1Regions[i1];
				BitSet baseLink1RegionPotentials = baseLink1Region.getPotentialPositions(grid, digit);
				if (baseLink1RegionPotentials.cardinality() != 2)
					continue;				
				// region 1
				Cell regionCell1 = baseLink1Region.getCell(p1 = baseLink1RegionPotentials.nextSetBit(0));
				Cell regionCell2 = baseLink1Region.getCell(baseLink1RegionPotentials.nextSetBit(p1 + 1));
				if (baseLink1 > 0 && isSamebox(regionCell1,regionCell2))
					continue;
				cells[0] = regionCell1;
				cells[1] = regionCell2;
                for (int i2 = (baseLink1 == baseLink2 ? i1+1 : 0); i2 < baseLink2Regions.length; i2++) {
					Grid.Region baseLink2Region = baseLink2Regions[i2];
					BitSet baseLink2RegionPotentials = baseLink2Region.getPotentialPositions(grid, digit);
					if (baseLink2RegionPotentials.cardinality() != 2)
						continue;					
					// region 2
					regionCell1 = baseLink2Region.getCell(p2 = baseLink2RegionPotentials.nextSetBit(0));
					regionCell2 = baseLink2Region.getCell(baseLink2RegionPotentials.nextSetBit(p2 + 1));
					if (baseLink2 > 0 && isSamebox(regionCell1,regionCell2))
						continue;
					cells[2] = regionCell1;
					cells[3] = regionCell2;
					for (int i3 = (baseLink2 == baseLink3 ? i2+1 : (baseLink1 == baseLink3 ? i1+1 : 0)); i3 < baseLink3Regions.length; i3++) {
						Grid.Region baseLink3Region = baseLink3Regions[i3];
						BitSet baseLink3RegionPotentials = baseLink3Region.getPotentialPositions(grid, digit);
						if (baseLink3RegionPotentials.cardinality() != 2)
							continue;
						regionCell1 = baseLink3Region.getCell(p3 = baseLink3RegionPotentials.nextSetBit(0));
						regionCell2 = baseLink3Region.getCell(baseLink3RegionPotentials.nextSetBit(p3 + 1));					
						if (baseLink3 > 0 && isSamebox(regionCell1,regionCell2))
							continue;
						cells[4] = regionCell1;
						cells[5] = regionCell2;
						boolean next = false;
						for (int i = 0; i < 4; i++) {
							for (int j = i + 1; j < 5; j++) {
								for (int k = j + 1; k < 6; k++) {
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
						Grid.Region shareRegion1, shareRegion2;
						Cell start1, bridge11, bridge12, end2, bridge21, bridge22;
						for (int i = 0; i < 2; i++) {
							for (int j = 2; j < 4; j++) {
								for (int k = 4; k < 6; k++) {								
									if ((shareRegion1 = shareRegionOf(grid,
										start1 = cells[i],
										bridge11 = cells[1 - i],
										bridge12 = cells[j],
										bridge21 = cells[5 - j])) != null &&
										(shareRegion2 = shareRegionOf(grid,
										bridge12 = cells[j],
										bridge21 = cells[5 - j],
										bridge22 = cells[k],
										end2 = cells[9 - k])) != null &&
										!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
										!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region) ) {
										// 3 strong-linked Turbot fish found
										ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
												baseLink1Region, baseLink2Region, shareRegion1, end2, bridge21, bridge22, baseLink3Region, shareRegion2, baseLink1, baseLink2, baseLink3);
										if (hint.isWorth())
											accu.add(hint);
									}
									if ((shareRegion1 = shareRegionOf(grid,
										start1 = cells[i],
										bridge11 = cells[1 - i],
										bridge12 = cells[k],
										bridge21 = cells[9 - k])) != null &&
										(shareRegion2 = shareRegionOf(grid,
										bridge12 = cells[k],
										bridge21 = cells[9 - k],
										bridge22 = cells[j],
										end2 = cells[5 - j])) != null &&
										!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
										!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region) ) {
										// 3 strong-linked Turbot fish found
										ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
												baseLink1Region, baseLink3Region, shareRegion1, end2, bridge21, bridge22, baseLink2Region, shareRegion2, baseLink1, baseLink3, baseLink2);
										if (hint.isWorth())
											accu.add(hint);
									}
									if ((shareRegion1 = shareRegionOf(grid,
										start1 = cells[j],
										bridge11 = cells[5 - j],
										bridge12 = cells[i],
										bridge21 = cells[1 - i])) != null &&
										(shareRegion2 = shareRegionOf(grid,
										bridge12 = cells[i],
										bridge21 = cells[1 - i],
										bridge22 = cells[k],
										end2 = cells[9 - k])) != null &&
										!shareRegion1.equals(baseLink1Region) && !shareRegion1.equals(baseLink2Region) && !shareRegion1.equals(baseLink3Region) &&
										!shareRegion2.equals(baseLink1Region) && !shareRegion2.equals(baseLink2Region) && !shareRegion2.equals(baseLink3Region) ) {
										// 3 strong-linked Turbot fish found
										ThreeStrongLinksHint hint = createHint(grid, digit, start1, bridge11, bridge12,
												baseLink2Region, baseLink1Region, shareRegion1, end2, bridge21, bridge22, baseLink3Region, shareRegion2, baseLink2, baseLink1, baseLink3);
										if (hint.isWorth())
											accu.add(hint);
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
    }

    private ThreeStrongLinksHint createHint(Grid grid, int value, Cell start1, Cell bridgeCell11, Cell bridgeCell12,
            Grid.Region baseLink1Set, Grid.Region baseLink2Set, Grid.Region shareRegion1, Cell end2, Cell bridgeCell21, Cell bridgeCell22,
            Grid.Region baseLink3Set, Grid.Region shareRegion2, int baseLink1, int baseLink2, int baseLink3) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
        CellSet victims = new CellSet(start1.getVisibleCells());
        victims.retainAll(end2.getVisibleCells());
        victims.remove(start1);
        victims.remove(end2);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value))
                removablePotentials.put(cell, SingletonBitSet.create(value));
        }

        // Create hint
        return new ThreeStrongLinksHint(this, removablePotentials,
                start1, bridgeCell11, bridgeCell12, value, baseLink1Set, baseLink2Set, shareRegion1, bridgeCell21, bridgeCell22, end2, baseLink3Set, shareRegion2, baseLink1, baseLink2, baseLink3);
    }

    @Override
    public String toString() {
        return "3-link Turbot Fishes";
    }
}
