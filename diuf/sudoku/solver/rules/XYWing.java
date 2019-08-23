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
 * Implementation of the "XY-Wing" and "XYZ-Wing" solving techniques.
 */
public class XYWing implements IndirectHintProducer {

    private final boolean isXYZ;


    public XYWing(boolean isXYZ) {
        this.isXYZ = isXYZ;
    }

    /**
     * Test if the potential values of three cells are forming an XY-Wing.
     * <p>
     * In an XY-Wing, the three cells must have exactly the potential values xy,
     * xz and yz, respectively, where x, y and z are different number between
     * 1 and 9.
     * <p>
     * We test that their union has three value and their intersection is empty.
     * @param xyValues the potential values of the "XY" cell
     * @param xzValues the potential values of the "XZ" cell
     * @param yzValues the potential values of the "YZ" cell
     * @return whether the three potential values set are forming an XY-Wing.
     */
    private boolean isXYWing(BitSet xyValues, BitSet xzValues, BitSet yzValues) {
        if (xyValues.cardinality() != 2 ||
                xzValues.cardinality() != 2 ||
                yzValues.cardinality() != 2)
            return false;
        BitSet union = (BitSet)xyValues.clone();
        union.or(xzValues);
        union.or(yzValues);
        BitSet inter = (BitSet)xyValues.clone();
        inter.and(xzValues);
        inter.and(yzValues);
        return union.cardinality() == 3 && inter.cardinality() == 0;
    }

    private boolean isXYZWing(BitSet xyValues, BitSet xzValues, BitSet yzValues) {
        if (xyValues.cardinality() != 3 ||
                xzValues.cardinality() != 2 ||
                yzValues.cardinality() != 2)
            return false;
        BitSet union = (BitSet)xyValues.clone();
        union.or(xzValues);
        union.or(yzValues);
        BitSet inter = (BitSet)xyValues.clone();
        inter.and(xzValues);
        inter.and(yzValues);
        return union.cardinality() == 3 && inter.cardinality() == 1;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        int targetCardinality = (isXYZ ? 3 : 2);
        for (int i = 0; i < 81; i++) {
            Cell xyCell = Grid.getCell(i);
            //BitSet xyValues = xyCell.getPotentialValues();
            BitSet xyValues = grid.getCellPotentialValues(xyCell);
            if (xyValues.cardinality() == targetCardinality) {
                // Potential XY cell found
                for (int xzCellIndex : xyCell.getVisibleCellIndexes()) {
                    //BitSet xzValues = xzCell.getPotentialValues();
                    BitSet xzValues = grid.getCellPotentialValues(xzCellIndex);
                    if (xzValues.cardinality() == 2) {
                        // Potential XZ cell found. Do small test
                        BitSet remValues = (BitSet)xyValues.clone();
                        remValues.andNot(xzValues);
                        if (remValues.cardinality() == 1) {
                            // We have found XZ cell, look for YZ cell
                            for (int yzCellIndex : xyCell.getVisibleCellIndexes()) {
                                //BitSet yzValues = yzCell.getPotentialValues();
                                BitSet yzValues = grid.getCellPotentialValues(yzCellIndex);
                                if (yzValues.cardinality() == 2) {
                                    // Potential YZ cell found
                                    if (isXYZ) {
                                        if (isXYZWing(xyValues, xzValues, yzValues)) {
                                            // Found XYZ-Wing pattern
                                            XYWingHint hint = createHint(grid, xyCell, Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
                                                    xzValues, yzValues);
                                            if (hint.isWorth())
                                                accu.add(hint);
                                        }
                                    } else {
                                        if (isXYWing(xyValues, xzValues, yzValues)) {
                                            // Found XY-Wing pattern
                                            XYWingHint hint = createHint(grid, xyCell, Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
                                                    xzValues, yzValues);
                                            if (hint.isWorth())
                                                accu.add(hint);
                                        }
                                    }
                                } // yzValues.cardinality() == 2
                            } // for yzCell
                        } // xy - xz test
                    } // xzValues.cardinality() == 2
                } // for xzCell
            } // xyValues.cardinality() == 2
    	} // for i
    }

    private XYWingHint createHint(Grid grid, Cell xyCell, Cell xzCell, Cell yzCell,
            BitSet xzValues, BitSet yzValues) {
        // Get the "z" value
        BitSet inter = (BitSet)xzValues.clone();
        inter.and(yzValues);
        int zValue = inter.nextSetBit(0);

        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
        //Set<Cell> victims = new LinkedHashSet<Cell>(xzCell.getHouseCells(grid));
        CellSet victims = new CellSet(xzCell.getHouseCells(grid));
        victims.retainAll(yzCell.getHouseCells(grid));
        if (isXYZ)
            victims.retainAll(xyCell.getHouseCells(grid));
        victims.remove(xyCell);
        victims.remove(xzCell);
        victims.remove(yzCell);
        for (Cell cell : victims) {
            //if (cell.hasPotentialValue(zValue))
            if (grid.hasCellPotentialValue(cell.getIndex(), zValue))
                removablePotentials.put(cell, SingletonBitSet.create(zValue));
        }

        // Create hint
        return new XYWingHint(this, removablePotentials, isXYZ, xyCell, xzCell, yzCell,
                zValue);
    }

    @Override
    public String toString() {
        return "XY-Wings";
    }

}
