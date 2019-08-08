/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.unique;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


/**
 * Implementation of the Bivalue Universal Grave solving technique.
 * Supports types 1 to 4.
 */
public class BivalueUniversalGrave implements IndirectHintProducer {

    private final Grid temp = new Grid();

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        grid.copyTo(temp);
        List<Cell> bugCells = new ArrayList<Cell>();
        Map<Cell, BitSet> bugValues = new HashMap<Cell, BitSet>();
        BitSet allBugValues = new BitSet(10);
        Set<Cell> commonCells = null;
        for (Class<? extends Grid.Region> regionType : grid.getRegionTypes()) {
            Grid.Region[] regions = grid.getRegions(regionType);
            for (int i = 0; i < regions.length; i++) {
                Grid.Region region = regions[i];
                for (int value = 1; value <= 9; value++) {
                    // Possible positions of a value in a region (row/column/block):
                    BitSet positions = region.getPotentialPositions(value);
                    int cardinality = positions.cardinality();
                    if (cardinality != 0 && cardinality != 2) {
                        // The value has not zero or two positions in the region
                        // Look for bug cells
                        List<Cell> newBugCells = new ArrayList<Cell>();
                        for (int index = positions.nextSetBit(0); index >= 0;
                                index = positions.nextSetBit(index + 1)) {
                            Cell cell = region.getCell(index);
                            int cellCardinality = cell.getPotentialValues().cardinality();
                            if (cellCardinality >= 3)
                                newBugCells.add(cell);
                        }
                        /*
                         * If there are two or more positions falling in a bug cell, we cannot
                         * decide which one is the buggy one. Just do nothing because another
                         * region will capture the correct cell.
                         */
                        if (newBugCells.size() == 1) {
                            // A new BUG cell has been found (BUG value = 'value')
                            Cell cell = newBugCells.get(0);
                            if (!bugCells.contains(cell))
                                bugCells.add(cell);
                            if (!bugValues.containsKey(cell))
                                bugValues.put(cell, new BitSet(10));
                            bugValues.get(cell).set(value);
                            allBugValues.set(value);
                            Cell twin = temp.getCell(cell.getX(), cell.getY());
                            twin.removePotentialValue(value);
                            if (commonCells == null)
                                commonCells = new LinkedHashSet<Cell>(cell.getHouseCells());
                            else
                                commonCells.retainAll(cell.getHouseCells());
                            commonCells.removeAll(bugCells);
                            if (bugCells.size() > 1 && allBugValues.cardinality() > 1
                                    && commonCells.isEmpty())
                                return; // None of type 1, 2 or 3
                        }
                        if (newBugCells.isEmpty())
                            // A value appear more than twice, but no cell has more
                            // than two values. => This is not a BUG pattern.
                            return;
                    }
                } // for value
            } // for i
        } // for regionType

        // When bug values have been removed, all remaining empty cells must have
        // exactly two potential values. Check it
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = temp.getCell(x, y);
                if (cell.getValue() == 0 && cell.getPotentialValues().cardinality() != 2)
                    return; // Not a BUG
            }
        }
        // When bug values have been removed, all remaining candidates must have
        // two positions in each region
        for (Class<? extends Grid.Region> regionType : temp.getRegionTypes()) {
            Grid.Region[] regions = temp.getRegions(regionType);
            for (int i = 0; i < regions.length; i++) {
                Grid.Region region = regions[i];
                for (int value = 1; value <= 9; value++) {
                    // Possible positions of a value in a region (row/column/block):
                    BitSet positions = region.getPotentialPositions(value);
                    int cardinality = positions.cardinality();
                    if (cardinality != 0 && cardinality != 2)
                        return; // Not a BUG
                }
            }
        }
        
        if (bugCells.size() == 1) {
            // Yeah, potential BUG type-1 pattern found
            addBug1Hint(accu, bugCells, allBugValues);
        } else if (allBugValues.cardinality() == 1) {
            // Yeah, potential BUG type-2 or type-4 pattern found
            addBug2Hint(accu, bugCells, allBugValues, commonCells);
            if (bugCells.size() == 2)
                // Potential BUG type-4 pattern found
                addBug4Hint(accu, bugCells, bugValues, allBugValues, commonCells, grid);
        } else if (commonCells != null && !commonCells.isEmpty()) {
            if (bugCells.size() == 2)
                // Potential BUG type-4 pattern found
                addBug4Hint(accu, bugCells, bugValues, allBugValues, commonCells, grid);
            // Yeah, potential BUG type-3 pattern found
            addBug3Hint(accu, bugCells, bugValues, allBugValues, commonCells, grid);
        }
    }

    private void addBug1Hint(HintsAccumulator accu, List<Cell> bugCells, BitSet extraValues) throws InterruptedException {
        Cell bugCell = bugCells.get(0);
        Map<Cell, BitSet> removablePotentials = new HashMap<Cell, BitSet>();
        BitSet removable = (BitSet)bugCell.getPotentialValues().clone();
        removable.andNot(extraValues);
        removablePotentials.put(bugCell, removable);
        IndirectHint hint = new Bug1Hint(this, removablePotentials, bugCell, extraValues);
        accu.add(hint);
    }

    private void addBug2Hint(HintsAccumulator accu, List<Cell> bugCells, BitSet extraValues,
            Set<Cell> commonCells) throws InterruptedException {
        int value = extraValues.nextSetBit(0);
        // Cells found ?
        if (commonCells != null && !commonCells.isEmpty()) {
            Map<Cell, BitSet> removablePotentials = new HashMap<Cell, BitSet>();
            for (Cell cell : commonCells) {
                if (cell.hasPotentialValue(value))
                    removablePotentials.put(cell, SingletonBitSet.create(value));
            }
            if (!removablePotentials.isEmpty()) {
                // Create hint
                Cell[] arrCells = new Cell[bugCells.size()];
                bugCells.toArray(arrCells);
                IndirectHint hint = new Bug2Hint(this, removablePotentials, arrCells, value);
                accu.add(hint);
            }
        }
    }

    private void addBug3Hint(HintsAccumulator accu, List<Cell> bugCells,
            Map<Cell, BitSet> extraValues, BitSet allExtraValues, Set<Cell> commonCells,
            Grid grid) throws InterruptedException {
        for (Class<? extends Grid.Region> regionType : grid.getRegionTypes()) {
            // Look for a region of this type shared by bugCells
            Grid.Region region = null;
            for (Cell cell : bugCells) {
                Grid.Region cellRegion = grid.getRegionAt(regionType, cell.getX(), cell.getY());
                if (region == null) {
                    region = cellRegion;
                } else if (!region.equals(cellRegion)) {
                    // Cells do not share a region of this type
                    region = null;
                    break;
                }
            }
            if (region != null) {
                // A shared region of type regionType has been found
                // Gather other cells of this region
                List<Cell> regionCells = new ArrayList<Cell>();
                for (Cell cell : commonCells) {
                    if (grid.getRegionAt(regionType, cell).equals(region))
                        regionCells.add(cell);
                }
                // Iterate on degree
                for (int degree = 2; degree <= 6; degree++) {
                    // Iterate on permutations of the missing (degree - 1) cells
                    if (regionCells.size() >= degree) {
                        Permutations perm = new Permutations(degree - 1, regionCells.size());
                        while (perm.hasNext()) {
                            BitSet[] potentials = new BitSet[degree];
                            Cell[] nakedCells = new Cell[degree - 1];
                            BitSet otherCommon = new BitSet(10);
                            int[] indexes = perm.nextBitNums();
                            for (int i = 0; i < indexes.length; i++) {
                                Cell cell = regionCells.get(indexes[i]);
                                // Fill array of missing naked cells
                                nakedCells[i] = cell;
                                BitSet potential = cell.getPotentialValues();
                                // Fill potential values array
                                potentials[i] = potential;
                                // Gather union of potentials
                                otherCommon.or(potential);
                            }
                            // Get potentials for bug cells
                            potentials[degree - 1] = allExtraValues;
                            // Ensure that all values of the naked set are covered by non-bug cells
                            if (otherCommon.cardinality() == degree) {
                                // Search for a naked set
                                BitSet nakedSet = CommonTuples.searchCommonTuple(potentials, degree);
                                if (nakedSet != null) {
                                    // One of bugCells form a naked set with nakedCells[]
                                    // Look for cells not part of the naked set, sharing the region
                                    Set<Cell> erasable = new HashSet<Cell>(regionCells);
                                    for (Cell cell : nakedCells)
                                        erasable.remove(cell); // exclude cells of the naked set
                                    erasable.removeAll(bugCells); // exclude bug cells
                                    if (!erasable.isEmpty()) {
                                        // Ok, some cells in a common region. Look for removable potentials
                                        Map<Cell, BitSet> removablePotentials = new HashMap<Cell, BitSet>();
                                        for (Cell cell : erasable) {
                                            BitSet removable = (BitSet)cell.getPotentialValues().clone();
                                            removable.and(nakedSet);
                                            if (!removable.isEmpty())
                                                removablePotentials.put(cell, removable);
                                        }
                                        if (!removablePotentials.isEmpty()) {
                                            // Create hint
                                            Cell[] arrCells = new Cell[bugCells.size()];
                                            bugCells.toArray(arrCells);
                                            IndirectHint hint = new Bug3Hint(this, removablePotentials, arrCells,
                                                    nakedCells, extraValues, allExtraValues, nakedSet, region);
                                            accu.add(hint);
                                        }
                                    } // if (!erasable.isEmpty())
                                } // if (nakedSet != null)
                            } // if (otherCommon.cardinality() == degree)
                        } // while (perm.hasNext())
                    } // if (regionCells.size() >= degree)
                } // for (degree)
            } // if (region != null)
        } // for (regionType)
    }

    private void addBug4Hint(HintsAccumulator accu, List<Cell> bugCells,
            Map<Cell, BitSet> extraValues, BitSet allExtraValues, Set<Cell> commonCells,
            Grid grid) throws InterruptedException {
        // Test for a common, non-bug value in both cells
        Cell c1 = bugCells.get(0);
        Cell c2 = bugCells.get(1);
        BitSet common = new BitSet(10);
        common.or(c1.getPotentialValues());
        common.and(c2.getPotentialValues());
        common.andNot(allExtraValues);
        if (common.cardinality() != 1)
            return; // No BUG type 4

        for (Class<? extends Grid.Region> regionType : grid.getRegionTypes()) {
            // Look for a region of this type shared by all bugCells
            Grid.Region region = null;
            for (Cell cell : bugCells) {
                Grid.Region cellRegion = grid.getRegionAt(regionType, cell.getX(), cell.getY());
                if (region == null) {
                    region = cellRegion;
                } else if (!region.equals(cellRegion)) {
                    // Cells do not share a region of this type
                    region = null;
                    break;
                }
            }
            if (region != null) {
                // OK, this is a BUG type 4
                assert common.cardinality() == 1;
                int value = common.nextSetBit(0);
                Map<Cell, BitSet> removablePotentials = new HashMap<Cell, BitSet>();
                BitSet b1 = (BitSet)c1.getPotentialValues().clone();
                b1.andNot(extraValues.get(c1));
                b1.clear(value);
                removablePotentials.put(c1, b1);
                BitSet b2 = (BitSet)c2.getPotentialValues().clone();
                b2.andNot(extraValues.get(c2));
                b2.clear(value);
                removablePotentials.put(c2, b2);
                IndirectHint hint = new Bug4Hint(this, removablePotentials, c1, c2, extraValues,
                        allExtraValues, value, region);
                accu.add(hint);
            }
        }
    }

    @Override
    public String toString() {
        return "Unique patterns";
    }

}
