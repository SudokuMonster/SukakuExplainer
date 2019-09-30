package SudokuExplainer.solver.rules.wing;

import java.util.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.*;


/**
 * Implementation of W-Wing technique solver.
 */
public class WWing implements IndirectHintProducer {

    private boolean isWWingHeadAndTail(Cell cell1, BitSet values1, Cell cell2, BitSet values2) {
        boolean isNotSameRegion = cell1.getRowNum() != cell2.getRowNum() &&
                cell1.getColumnNum() != cell2.getColumnNum() &&
                cell1.getBlockNum() != cell2.getBlockNum();
        if (isNotSameRegion) {
            boolean isBothEmptyCells = cell1.isEmpty() && cell2.isEmpty();
            if (isBothEmptyCells) {
                boolean isBivalue = values1.cardinality() == 2 && values2.cardinality() == 2;
                if (isBivalue) {
                    BitSet temp = (BitSet)values1.clone();
                    temp.and(values2);
                    return temp.cardinality() == 2;
                }
            }
        }

        return false;
    }

    private void findWWingIn(Grid grid, int headValue, int tailValue, Cell cell1, Cell cell2,
            Class<? extends Grid.Region> regionType, int regionNumber, HintsAccumulator accu)
            throws InterruptedException {
        // Regard tailValue as link value of the chain
        BitSet potentials;
        if (regionType == Grid.Row.class) {
            potentials = grid.getRow(regionNumber).getPotentialPositions(tailValue);
        } else {
            potentials = grid.getColumn(regionNumber).getPotentialPositions(tailValue);
        }
        if (potentials.cardinality() == 2) {
            // Conjugate pair found, check if same row/column or not
            int c1 = potentials.nextSetBit(0);
            int c2 = potentials.nextSetBit(c1 + 1);
            if (regionType == Grid.Row.class) {
                if (c1 == cell1.getColumnNum() && c2 == cell2.getColumnNum()) {
                    // Found W-Wing.
                    Cell bridgeCell1 = grid.getCell(c1, regionNumber);
                    Cell bridgeCell2 = grid.getCell(c2, regionNumber);
                    WWingHint hint = createHint(grid, headValue, tailValue, cell1, cell2,
                            bridgeCell1, bridgeCell2, regionType, regionNumber);
                    if (hint.isWorth())
                        accu.add(hint);
                } // else not same column, search failed
            } else {
                if (c1 == cell1.getRowNum() && c2 == cell2.getRowNum()) {
                    // Found W-Wing.
                    Cell bridgeCell1 = grid.getCell(regionNumber, c1);
                    Cell bridgeCell2 = grid.getCell(regionNumber, c2);
                    WWingHint hint = createHint(grid, headValue, tailValue, cell1, cell2,
                            bridgeCell1, bridgeCell2, regionType, regionNumber);
                    if (hint.isWorth())
                        accu.add(hint);
                } // else not same row, search failed
            }
        } // else potential position of tailValue more or less than two, search failed
    }

    @Override
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        for (int y1 = 0; y1 < 9; y1++) {
            for (int x1 = 0; x1 < 9; x1++) {
                Cell cell1 = grid.getCell(x1, y1);
                BitSet values1 = cell1.getPotentialValues();
                for (int y2 = 0; y2 < 9; y2++) {
                    for (int x2 = 0; x2 < 9; x2++) {
                        Cell cell2 = grid.getCell(x2, y2);
                        BitSet values2 = cell2.getPotentialValues();
                        if (isWWingHeadAndTail(cell1, values1, cell2, values2)) {
                            // WWing head and tail found, check if some region has conjugate pair or not
                            int d1 = values1.nextSetBit(0);
                            int d2 = values1.nextSetBit(d1 + 1);

                            // Found conjugate pair in row
                            for (int row = 0; row < 9; row++) {
                                // Same region, don't need to find conjugate pairs
                                if (row == cell1.getRowNum() || row == cell2.getRowNum())
                                    continue;

                                // Regard d1 as head of the chain
                                findWWingIn(grid, d1, d2, cell1, cell2, Grid.Row.class, row, accu);
                                // Regard d2 as head of the chain
                                findWWingIn(grid, d2, d1, cell1, cell2, Grid.Row.class, row, accu);
                            }

                            // Found conjugate pair in column
                            for (int column = 0; column < 9; column++) {
                                if (column == cell1.getColumnNum() || column == cell2.getColumnNum())
                                    continue;

                                // Regard d1 as head of the chain
                                findWWingIn(grid, d1, d2, cell1, cell2, Grid.Column.class, column, accu);
                                // Regard d2 as head of the chain
                                findWWingIn(grid, d2, d1, cell1, cell2, Grid.Column.class, column, accu);
                            }

                            // We don't need to find conjugate pairs in block
                            // because cells are not in same block.
                        } // if isWWingHeadAndTail(cell1, values1, cell2, values2)
                    }
                }
            }
        }
    }

    private WWingHint createHint(Grid grid,
            int headValue, int bodyValue, Cell start, Cell end, Cell bridgeCell1, Cell bridgeCell2,
            Class<? extends Grid.Region> regionType, int regionNum) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
        Set<Cell> victims = new LinkedHashSet<>(start.getHouseCells());
        victims.retainAll(end.getHouseCells());
        victims.remove(start);
        victims.remove(end);
        for (Cell cell : victims) {
            if (cell.hasPotentialValue(headValue))
                removablePotentials.put(cell, SingletonBitSet.create(headValue));
        }

        Grid.Region region;
        if (regionType == Grid.Row.class) {
            region = grid.getRow(regionNum);
        } else {
            region = grid.getColumn(regionNum);
        }
        // Create hint
        return new WWingHint(this, removablePotentials,
                headValue, bodyValue, start, end, bridgeCell1, bridgeCell2, region);
    }

    @Override
    public String toString() {
        return "W-Wings";
    }
}
