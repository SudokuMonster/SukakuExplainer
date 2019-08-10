/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


/**
 * Implementation of the naked sets solving techniques
 * (Naked Pair, Naked Triplet, Naked Quad).
 */
public class NakedSet implements IndirectHintProducer {

    private int degree;

    public NakedSet(int degree) {
        assert degree > 1 && degree <= 4;
        this.degree = degree;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        getHints(grid, Grid.Block.class, accu);
        getHints(grid, Grid.Column.class, accu);
        getHints(grid, Grid.Row.class, accu);
    }

    /**
     * For each regions of the given type, check if a n-tuple of values have
     * a common n-tuple of potential positions, and no other potential position.
     */
    private <T extends Grid.Region> void getHints(Grid grid, Class<T> regionType,
            HintsAccumulator accu) throws InterruptedException {
        Grid.Region[] regions = grid.getRegions(regionType);
        // Iterate on parts
        for (Grid.Region region : regions) {
            if (region.getEmptyCellCount() >= degree * 2) {
                Permutations perm = new Permutations(degree, 9);
                // Iterate on tuples of positions
                while (perm.hasNext()) {
                    int[] indexes = perm.nextBitNums();
                    assert indexes.length == degree;

                    // Build the cell tuple
                    Cell[] cells = new Cell[degree];
                    for (int i = 0; i < cells.length; i++)
                        cells[i] = region.getCell(indexes[i]);

                    // Build potential values for each position of the tuple
                    BitSet[] potentialValues = new BitSet[degree];
                    for (int i = 0; i < degree; i++)
                        potentialValues[i] = cells[i].getPotentialValues();

                    // Look for a common tuple of potential values, with same degree
                    BitSet commonPotentialValues = 
                        CommonTuples.searchCommonTuple(potentialValues, degree);
                    if (commonPotentialValues != null) {
                        // Potential hint found
                        IndirectHint hint = createValueUniquenessHint(region, cells, commonPotentialValues);
                        if (hint.isWorth())
                            accu.add(hint);
                    }
                }
            }
        }
    }

    private IndirectHint createValueUniquenessHint(Grid.Region region, Cell[] cells,
            BitSet commonPotentialValues) {
        // Build value list
        int[] values = new int[degree];
        int dstIndex = 0;
        for (int value = 1; value <= 9; value++) {
            if (commonPotentialValues.get(value))
                values[dstIndex++] = value;
        }
        // Build concerned cell potentials
        Map<Cell,BitSet> cellPValues = new LinkedHashMap<Cell,BitSet>();
        for (Cell cell : cells) {
            BitSet potentials = new BitSet(10);
            potentials.or(commonPotentialValues);
            potentials.and(cell.getPotentialValues());
            cellPValues.put(cell, potentials);
        }
        // Build removable potentials
        Map<Cell,BitSet> cellRemovePValues = new HashMap<Cell,BitSet>();
        for (int i = 0; i < 9; i++) {
            Cell otherCell = region.getCell(i);
            if (!Arrays.asList(cells).contains(otherCell)) {
                // Get removable potentials
                BitSet removablePotentials = new BitSet(10);
                for (int value = 1; value <= 9; value++) {
                    if (commonPotentialValues.get(value) && otherCell.hasPotentialValue(value))
                        removablePotentials.set(value);
                }
                if (!removablePotentials.isEmpty())
                    cellRemovePValues.put(otherCell, removablePotentials);
            }
        }
        return new NakedSetHint(this, cells, values, cellPValues, cellRemovePValues, region);
    }

    @Override
    public String toString() {
        if (degree == 2)
            return "Naked Pairs";
        else if (degree == 3)
            return "Naked Triplets";
        else if (degree == 4)
            return "Naked Quads";
        return "Naked Sets " + degree;
    }

}
