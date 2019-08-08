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
 * Implementation of X-Wing, Swordfish and Jellyfish solving techniques.
 * The following techniques are implemented depending on the given degree:
 * <ul>
 * <li>Degree 2: X-Wing
 * <li>Degree 3: Swordfish
 * <li>Degree 4: Jellyfish
 * </ul>
 */
public class Fisherman implements IndirectHintProducer {

    private final int degree;


    public Fisherman(int degree) {
        this.degree = degree;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        getHints(grid, Grid.Column.class, Grid.Row.class, accu);
        getHints(grid, Grid.Row.class, Grid.Column.class, accu);
    }

    private <S extends Grid.Region,T extends Grid.Region> void getHints(Grid grid,
            Class<S> partType1, Class<T> partType2,
            HintsAccumulator accu) throws InterruptedException {
        assert !partType1.equals(partType2);

        // Get occurance count for each value
        int[] occurances = new int[10];
        for (int value = 1; value <= 9; value++)
            occurances[value] = grid.getCountOccurancesOfValue(value);

        Grid.Region[] parts = grid.getRegions(partType1);
        // Iterate on lines tuples
        Permutations perm = new Permutations(degree, 9);
        while (perm.hasNext()) {
            int[] indexes = perm.nextBitNums();
            assert indexes.length == degree;

            BitSet myIndexes = new BitSet(9);
            for (int i = 0; i < indexes.length; i++)
                myIndexes.set(indexes[i]);

            // Iterate on values
            for (int value = 1; value <= 9; value++) {

                // Pattern is only possible if there are at least (degree * 2) missing occurances
                // of the value.
                if (occurances[value] + degree * 2 <= 9) {

                    // Check for exactly the same positions of the value in all lines
                    BitSet[] positions = new BitSet[degree];
                    for (int i = 0; i < degree; i++)
                        positions[i] = parts[indexes[i]].getPotentialPositions(value);
                    BitSet common = CommonTuples.searchCommonTuple(positions, degree);

                    if (common != null) {
                        // Potential hint found
                        IndirectHint hint = createFishHint(grid, partType1, partType2,
                                myIndexes, common, value);
                        if (hint.isWorth())
                            accu.add(hint);
                    }
                }
            }
        }
    }

    private <S extends Grid.Region,T extends Grid.Region> IndirectHint createFishHint(
            Grid grid, Class<S> otherPartType, Class<T> myPartType, BitSet otherIndexes, 
            BitSet myIndexes, int value) {
        Grid.Region[] myParts = grid.getRegions(myPartType);
        Grid.Region[] otherParts = grid.getRegions(otherPartType);
        // Build parts
        List<Grid.Region> parts1 = new ArrayList<Grid.Region>();
        List<Grid.Region> parts2 = new ArrayList<Grid.Region>();
        for (int i = 0; i < 9; i++) {
            if (otherIndexes.get(i))
                parts1.add(otherParts[i]);
            if (myIndexes.get(i))
                parts2.add(myParts[i]);
        }
        assert parts1.size() == parts2.size();
        Grid.Region[] allParts = new Grid.Region[parts1.size() + parts2.size()];
        for (int i = 0; i < parts1.size(); i++) {
            allParts[i * 2] = parts1.get(i);
            allParts[i * 2 + 1] = parts2.get(i);
        }

        // Build highlighted potentials and cells
        List<Cell> cells = new ArrayList<Cell>();
        Map<Cell,BitSet> cellPotentials = new HashMap<Cell,BitSet>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (myIndexes.get(i) && otherIndexes.get(j)) {
                    Cell cell = myParts[i].getCell(j);
                    if (cell.hasPotentialValue(value)) {
                        cells.add(cell);
                        cellPotentials.put(cell, SingletonBitSet.create(value));
                    }
                }
            }
        }
        Cell[] allCells = new Cell[cells.size()];
        cells.toArray(allCells);

        // Build removable potentials
        Map<Cell,BitSet> cellRemovablePotentials = new HashMap<Cell,BitSet>();
        for (int i = 0; i < 9; i++) {
            if (myIndexes.get(i)) {
                // Check if value appears outside from otherIndexes
                BitSet potentialPositions = myParts[i].copyPotentialPositions(value);
                potentialPositions.andNot(otherIndexes);
                if (!potentialPositions.isEmpty()) {
                    for (int j = 0; j < 9; j++) {
                        if (potentialPositions.get(j))
                            cellRemovablePotentials.put(myParts[i].getCell(j),
                                    SingletonBitSet.create(value));
                    }
                }
            }
        }
        return new LockingHint(this, allCells, value, cellPotentials,
                cellRemovablePotentials, allParts);
    }

    @Override
    public String toString() {
        if (degree == 2)
            return "X-Wings";
        else if (degree == 3)
            return "Swordfishes";
        else if (degree == 4)
            return "Jellyfishes";
        return null;
    }

}
