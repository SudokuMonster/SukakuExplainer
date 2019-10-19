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
        // Skyscrapers
        getHints(grid, accu, 1, 1);
        getHints(grid, accu, 2, 2);
        // Two-string Kites
        getHints(grid, accu, 2, 1);
        // Turbot Fishes
        getHints(grid, accu, 1, 0);
        getHints(grid, accu, 2, 0);
    }

    private Grid.Region shareRegionOf(Grid grid,
            Cell start, Cell bridge1, Cell bridge2, Cell end) {
        if (bridge1.getX() == bridge2.getX()) {
            return (Grid.Column)Grid.getRegionAt(2,bridge1.getIndex());
        } else if (bridge1.getY() == bridge2.getY()) {
            return (Grid.Row)Grid.getRegionAt(1,bridge1.getIndex());
        } else if (bridge1.getB() == bridge2.getB()) {
            return (Grid.Block)Grid.getRegionAt(0,bridge1.getIndex());
        } else return null;
    }

    private void getHints(Grid grid, HintsAccumulator accu,
            int base, int cover)
            throws InterruptedException {
        for (int digit = 1; digit <= 9; digit++) {
            Grid.Region[] baseRegions = grid.getRegions(base);
            Grid.Region[] coverRegions = grid.getRegions(cover);
            for (int i1 = 0; i1 < baseRegions.length; i1++) {
                for (int i2 = (base == cover ? i1+1 : 0); i2 < coverRegions.length; i2++) {
                    // For each set in sets
                    Grid.Region baseRegion = baseRegions[i1];
                    Grid.Region coverRegion = coverRegions[i2];

                    // Same region, skip
                    //if (baseRegion.getClass() == coverRegion.getClass() && i1 == i2)
                        //continue;
                    BitSet baseRegionPotentials = baseRegion.getPotentialPositions(grid, digit);
                    BitSet coverRegionPotentials = coverRegion.getPotentialPositions(grid, digit);
                    if (baseRegionPotentials.cardinality() == 2 && coverRegionPotentials.cardinality() == 2) {
                        // Strong links found (Conjugate pairs found)
                        // Check whether positions may in the same region or not (form a weak link)
                        int p1, p2;
                        Cell[] cells = new Cell[] {
                                // region 1
                                baseRegion.getCell(p1 = baseRegionPotentials.nextSetBit(0)),
                                baseRegion.getCell(baseRegionPotentials.nextSetBit(p1 + 1)),
                                // region 2
                                coverRegion.getCell(p2 = coverRegionPotentials.nextSetBit(0)),
                                coverRegion.getCell(coverRegionPotentials.nextSetBit(p2 + 1))
                        };

                        // Cells cannot be same
                        boolean next = false;
                        for (int i = 0; i < 3; i++) {
                            for (int j = i + 1; j < 4; j++) {
                                if (cells[i].equals(cells[j])) {
                                    next = true;
                                    break;
                                }
                            }
                        }
                        if (next) continue;

                        Grid.Region shareRegion;
                        Cell start, end, bridge1, bridge2;
                        for (int i = 0; i < 2; i++) {
                            for (int j = 2; j < 4; j++) {
                                if ((shareRegion = shareRegionOf(grid,
                                            start = cells[i],
                                            bridge1 = cells[1 - i],
                                            bridge2 = cells[j],
                                            end = cells[5 - j])) != null &&
                                        !shareRegion.equals(baseRegion) && !shareRegion.equals(coverRegion)) {
                                    // Turbot fish found
                                    TurbotFishHint hint = createHint(grid, digit, start, end, bridge1, bridge2,
                                            baseRegion, coverRegion, shareRegion);
                                    if (hint.isWorth())
                                        accu.add(hint);
                                }
                            } // for int j = 0..2
                        } // for int i = 0..2
                    } // if baseRegionPotentials.cardinality() == 2 && coverRegionPotentials.cardinality() == 2
                }
            }
        }
    }

    private TurbotFishHint createHint(Grid grid, int value, Cell start, Cell end, Cell bridgeCell1, Cell bridgeCell2,
            Grid.Region baseSet, Grid.Region coverSet, Grid.Region shareRegion) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
        Set<Cell> victims = new LinkedHashSet<>(start.getVisibleCells());
        victims.retainAll(end.getVisibleCells());
        victims.remove(start);
        victims.remove(end);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value))
                removablePotentials.put(cell, SingletonBitSet.create(value));
        }

        // Create hint
        return new TurbotFishHint(this, removablePotentials,
                start, end, bridgeCell1, bridgeCell2, value, baseSet, coverSet, shareRegion);
    }

    @Override
    public String toString() {
        return "Turbot Fishes";
    }
}
