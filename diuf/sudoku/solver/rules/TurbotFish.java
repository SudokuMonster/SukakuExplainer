package SudokuExplainer.solver.rules.chaining.aic;

import java.util.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.*;


/**
 * Implementation of Turbot Fish technique solver.
 */
public class TurbotFish implements IndirectHintProducer {

    @Override
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        // Skyscrapers
        getHints(grid, accu, Grid.Row.class, Grid.Row.class);
        getHints(grid, accu, Grid.Column.class, Grid.Column.class);
        // Two-string Kites
        getHints(grid, accu, Grid.Column.class, Grid.Row.class);
        getHints(grid, accu, Grid.Row.class, Grid.Column.class);
        // Turbot Fishes
        getHints(grid, accu, Grid.Row.class, Grid.Block.class);
        getHints(grid, accu, Grid.Column.class, Grid.Block.class);
        getHints(grid, accu, Grid.Block.class, Grid.Row.class);
        getHints(grid, accu, Grid.Block.class, Grid.Column.class);
        // Generalized X-Wing...
        getHints(grid, accu, Grid.Block.class, Grid.Block.class);
    }

    private Grid.Region shareRegionOf(Grid grid,
            Cell start, Cell bridge1, Cell bridge2, Cell end) {
        if (bridge1.getRowNum() == bridge2.getRowNum()) {
            return grid.getRow(bridge1.getRowNum());
        } else if (bridge1.getColumnNum() == bridge2.getColumnNum()) {
            return grid.getColumn(bridge1.getColumnNum());
        } else if (bridge1.getBlockNum() == bridge2.getBlockNum()) {
            return grid.getBlock(bridge1.getBlockNum());
        } else return null;
    }

    private void getHints(Grid grid, HintsAccumulator accu,
            Class<? extends Grid.Region> base, Class<? extends Grid.Region> cover)
            throws InterruptedException {
        for (int digit = 1; digit <= 9; digit++) {
            Grid.Region[] baseRegions = grid.getRegions(base);
            Grid.Region[] coverRegions = grid.getRegions(cover);
            for (int i1 = 0; i1 < baseRegions.length; i1++) {
                for (int i2 = 0; i2 < coverRegions.length; i2++) {
                    // For each set in sets
                    Grid.Region baseRegion = baseRegions[i1];
                    Grid.Region coverRegion = coverRegions[i2];

                    // Same region, skip
                    if (baseRegion.getClass() == coverRegion.getClass() && i1 == i2)
                        continue;

                    BitSet baseRegionPotentials = baseRegion.getPotentialPositions(digit);
                    BitSet coverRegionPotentials = coverRegion.getPotentialPositions(digit);
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
                                    TurbotFishHint hint = createHint(digit, start, end, bridge1, bridge2,
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

    private TurbotFishHint createHint(int value, Cell start, Cell end, Cell bridgeCell1, Cell bridgeCell2,
            Grid.Region baseSet, Grid.Region coverSet, Grid.Region shareRegion) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
        Set<Cell> victims = new LinkedHashSet<>(start.getHouseCells());
        victims.retainAll(end.getHouseCells());
        victims.remove(start);
        victims.remove(end);
        for (Cell cell : victims) {
            if (cell.hasPotentialValue(value))
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
