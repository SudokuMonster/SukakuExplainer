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
 * Aligned Set Exclusion technique.
 * Very slow for degree >= 4.
 */
public class AlignedExclusion implements IndirectHintProducer {

    protected final int degree;


    public AlignedExclusion(int degree) {
        this.degree = degree;
    }

    public void getHints(Grid grid, HintsAccumulator accu)
            throws InterruptedException {

        /*
         * Search for "base" cells that can participate to a exclusion set.
         * For each candidate, collect the potentially excluding cells.
         */
        List<Cell> candidateList = new ArrayList<Cell>();
        Map<Cell, Collection<Cell>> cellExcluders = new LinkedHashMap<Cell, Collection<Cell>>();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell.getPotentialValues().cardinality() >= 2) { // 2 <-> degree
                    boolean hasNakedSingle = false;
                    // Look for potentially excluding cells (whose number of candidates <= degree)
                    Collection<Cell> excludingCells = new ArrayList<Cell>();
                    for (Cell excludingCell : cell.getHouseCells()) {
                        int exclCardinality = excludingCell.getPotentialValues().cardinality();
                        if (exclCardinality == 1)
                            hasNakedSingle = true;
                        else if (exclCardinality >= 2 && exclCardinality <= degree)
                            excludingCells.add(excludingCell);
                    }
                    // Optimization: Skip this technique if naked singles are remaining
                    if (!hasNakedSingle && !excludingCells.isEmpty()) {
                        candidateList.add(cell);
                        cellExcluders.put(cell, excludingCells);
                    }
                }
            }
        }

        assert candidateList.size() == cellExcluders.size();
        if (cellExcluders.size() < degree)
            return;

        /*
         * Iterate on all permutations of 'degree' cells among the
         * possible base cells
         */

        /*
         * To iterate over 'n' cells (n > 2), we first iterate among
         * two cells. Then we retain only the other cells that are
         * visible by at least one of these two cells (the twinArea), and we
         * continue the iteration on these remaining cells.
         */
        // First iterate on the first two cells
        Permutations cellSetPerm2 = new Permutations(2, candidateList.size());
        while (cellSetPerm2.hasNext()) {
            int[] indexes = cellSetPerm2.nextBitNums();
            // Setup the first two cells
            Cell cell0 = candidateList.get(indexes[0]);
            int card0 = cell0.getPotentialValues().cardinality();
            Cell cell1 = candidateList.get(indexes[1]);
            int card1 = cell1.getPotentialValues().cardinality();

            // Create the twinArea: set of cells visible by one of the two first cells
            Collection<Cell> twinArea = new LinkedHashSet<Cell>(cellExcluders.get(cell0));
            twinArea.addAll(cellExcluders.get(cell1));
            // Retain only other candidates
            twinArea.retainAll(candidateList);
            // Remove the two first cells
            twinArea.remove(cell0);
            twinArea.remove(cell1);

            // Check if we have enough cells in the twin Area
            if (twinArea.size() >= degree - 2) {
                List<Cell> tailCells = new ArrayList<Cell>(twinArea);
                // Iterate on remaining cells using the twinArea
                Permutations tailSetPerm = new Permutations(degree - 2, tailCells.size());
                while (tailSetPerm.hasNext()) {
                    Cell[] cells = new Cell[degree];
                    int[] cardinalities = new int[degree];
                    // Copy the first two cells
                    cells[0] = cell0;
                    cardinalities[0] = card0;
                    cells[1] = cell1;
                    cardinalities[1] = card1;

                    // Add the tail cells
                    int[] tindexes = tailSetPerm.nextBitNums();
                    assert tindexes.length == degree - 2;
                    for (int i = 0; i < tindexes.length; i++) {
                        cells[i + 2] = tailCells.get(tindexes[i]);
                        cardinalities[i + 2] = cells[i + 2].getPotentialValues().cardinality();
                    }

                    // Build the list of common excluding cells for the base cells 'cells'
                    Set<Cell> commonExcluders = new LinkedHashSet<Cell>();
                    for (int i = 0; i < degree; i++) {
                        Collection<Cell> excludingCells = cellExcluders.get(cells[i]);
                        if (i == 0)
                            commonExcluders.addAll(excludingCells);
                        else
                            commonExcluders.retainAll(excludingCells);
                    }

                    if (commonExcluders.size() >= 2) {
                        Map<Cell, BitSet> removablePotentials = new HashMap<Cell, BitSet>();

                        int[] potIndexes = new int[degree];
                        // Iterate on combinations of potentials accross the base cells
                        List<int[]> allowedPotentialCombinations = new ArrayList<int[]>();
                        Map<int[], Cell> lockedPotentialCombinations = new LinkedHashMap<int[], Cell>();
                        boolean isFinished = false;
                        do {
                            // Get next combination of potential indexes
                            int z = 0;
                            boolean rollOver = false;
                            do {
                                if (potIndexes[z] == 0) {
                                    rollOver = true;
                                    potIndexes[z] = cardinalities[z] - 1;
                                    z++;
                                } else {
                                    rollOver = false;
                                    potIndexes[z]--;
                                }
                            } while (z < degree && rollOver);

                            // Build the combination of potential values
                            int[] potentials = new int[degree];
                            for (int i = 0; i < degree; i++) {
                                BitSet values = cells[i].getPotentialValues();
                                int p = values.nextSetBit(0);
                                for (int j = 0; j < potIndexes[i]; j++)
                                    p = values.nextSetBit(p + 1);
                                assert p >= 0;
                                potentials[i] = p;
                            }

                            boolean isAllowed = true;
                            Cell lockingCell = null;
                            // Check if this potential combination is allowed, hidden single rule
                            Permutations perm = new Permutations(2, degree);
                            while (perm.hasNext()) {
                                int[] cellIndexes = perm.nextBitNums();
                                assert cellIndexes.length == 2;
                                int p1 = potentials[cellIndexes[0]];
                                int p2 = potentials[cellIndexes[1]];
                                if (p1 == p2) {
                                    /*
                                     * Hidden Single: Using the same potential value for two cells of the
                                     * set is only allowed if they do not share a region
                                     */
                                    Cell c1 = cells[cellIndexes[0]];
                                    Cell c2 = cells[cellIndexes[1]];
                                    if (c1.getHouseCells().contains(c2)) {
                                        isAllowed = false;
                                        break;
                                    }
                                }
                            }

                            // Check if this potential combination is allowed, using common excluder cells
                            if (isAllowed) {
                                for (Cell excludingCell : commonExcluders) {
                                    BitSet values = (BitSet)excludingCell.getPotentialValues().clone();
                                    for (int i = 0; i < degree; i++)
                                        values.clear(potentials[i]);
                                    if (values.isEmpty()) {
                                        lockingCell = excludingCell;
                                        isAllowed = false;
                                        break;
                                    }
                                }
                            }

                            // Store the combination in the appropriate structure
                            if (isAllowed)
                                allowedPotentialCombinations.add(potentials);
                            else
                                lockedPotentialCombinations.put(potentials, lockingCell);

                            // Check if last combination of potentials from the base cells has been reached
                            isFinished = true;
                            for (int i = 0; i < degree; i++) {
                                if (potIndexes[i] != 0)
                                    isFinished = false;
                            }
                        } while (!isFinished);

                        /*
                         * For all potentials of all base cells, test if the
                         * value is possible in at least one allowed combination
                         */
                        for (int i = 0; i < degree; i++) {
                            Cell cell = cells[i];
                            BitSet values = cell.getPotentialValues();
                            for (int p = values.nextSetBit(0); p >= 0; p = values.nextSetBit(p + 1)) {
                                boolean isValueAllowed = false;
                                for (int[] combinations : allowedPotentialCombinations) {
                                    if (combinations[i] == p) {
                                        isValueAllowed = true; // At least one allowed combination permits this value
                                    }
                                }
                                if (!isValueAllowed) {
                                    // Yeah, value 'p' can be excluded from cell 'cell'
                                    if (removablePotentials.containsKey(cell))
                                        removablePotentials.get(cell).set(p);
                                    else
                                        removablePotentials.put(cell, SingletonBitSet.create(p));
                                }
                            }
                        }

                        // Create hint
                        IndirectHint hint = new AlignedExclusionHint(this, removablePotentials, cells,
                                lockedPotentialCombinations);
                        if (hint.isWorth())
                            accu.add(hint);
                    } // if enough common excluders
                } // Iterate on cell permutations: tail cells
            }
        } // Iterate on cell permutations: first two cells
    }

    @Override
    public String toString() {
        if (degree == 2)
            return "Aligned Pair Exclusion";
        else if (degree == 3)
            return "Aligned Triplet Exclusion";
        else if (degree == 4)
            return "Aligned Quad Exclusion";
        else
            return "Aligned Set (" + degree + ") Exclusion";
    }

}
