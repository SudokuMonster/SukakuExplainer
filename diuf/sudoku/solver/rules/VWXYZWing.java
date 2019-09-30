package SudokuExplainer.solver.rules.wing;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;

/**
 * Implementation of the "VWXYZ-Wing" and its pivot-incompleted type solving techniques.
 */
public class VWXYZWing implements IndirectHintProducer {

    /**
     * Stands for whether pivot cell is completed or not.
     * <ul>
     * <li>Completed: VWXYZ VZ WZ XZ YZ</li>
     * <li>Incompleted: VWXY VZ WZ XZ YZ</li>
     * </ul>
     */
    private final boolean isIncompletedPivot;

    public VWXYZWing(boolean isIncompletedPivot) {
        this.isIncompletedPivot = isIncompletedPivot;
    }

    private boolean isVWXYZWing(
            BitSet vwxyzValues, BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues) {
        if (vwxyzValues.cardinality() != (isIncompletedPivot ? 4 : 5) ||
                vzValues.cardinality() != 2 ||
                wzValues.cardinality() != 2 ||
                xzValues.cardinality() != 2 ||
                yzValues.cardinality() != 2)
            return false;

        BitSet union = (BitSet)vwxyzValues.clone();
        union.or(vzValues);
        union.or(wzValues);
        union.or(xzValues);
        union.or(yzValues);
        BitSet inter = (BitSet)union.clone();
        if (!isIncompletedPivot)
            inter.and(vwxyzValues);
        inter.and(vzValues);
        inter.and(wzValues);
        inter.and(xzValues);
        inter.and(yzValues);

        BitSet[] innerProduct = {
                (BitSet)vzValues.clone(),
                (BitSet)vzValues.clone(),
                (BitSet)vzValues.clone(),
                (BitSet)wzValues.clone(),
                (BitSet)wzValues.clone(),
                (BitSet)xzValues.clone()
        };
        innerProduct[0].and(wzValues);
        innerProduct[1].and(xzValues);
        innerProduct[2].and(yzValues);
        innerProduct[3].and(xzValues);
        innerProduct[4].and(yzValues);
        innerProduct[5].and(yzValues);
        BitSet[] outerProduct = {
                (BitSet)vwxyzValues.clone(),
                (BitSet)vwxyzValues.clone(),
                (BitSet)vwxyzValues.clone(),
                (BitSet)vwxyzValues.clone()
        };
        outerProduct[0].and(vzValues);
        outerProduct[1].and(wzValues);
        outerProduct[2].and(xzValues);
        outerProduct[3].and(yzValues);
        boolean hasSameBitSet = false;
        boolean hasSameDigit = true;
        for (BitSet b : innerProduct) {
            hasSameBitSet = hasSameBitSet || (b.cardinality() == 2);
        }
        for (BitSet b : outerProduct) {
            hasSameDigit = hasSameDigit && (b.cardinality() == (isIncompletedPivot ? 1 : 2));
        }

        return union.cardinality() == 5 && inter.nextSetBit(0) != -1 && !hasSameBitSet && hasSameDigit;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        final int targetCardinality = isIncompletedPivot ? 4 : 5;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell vwxyzCell = grid.getCell(x, y);
                BitSet vwxyzValues = vwxyzCell.getPotentialValues();
                if (vwxyzValues.cardinality() == targetCardinality) {
                    // Potential VWXYZ cell found
                    for (Cell vzCell : vwxyzCell.getHouseCells()) {
                        BitSet vzValues = vzCell.getPotentialValues();
                        if (vzValues.cardinality() == 2) {
                            // Potential VZ Cell found
                            for (Cell wzCell : vwxyzCell.getHouseCells()) {
                                if (wzCell.getX() != vzCell.getX() || wzCell.getY() != vzCell.getY()) {
                                    BitSet wzValues = wzCell.getPotentialValues();
                                    if (wzValues.cardinality() == 2) {
                                        // Potential WZ cell found
                                        for (Cell xzCell : vwxyzCell.getHouseCells()) {
                                            if (!(xzCell.getX() == vzCell.getX() && xzCell.getY() == vzCell.getY()) &&
                                                    !(xzCell.getX() == wzCell.getX() && xzCell.getY() == wzCell.getY())) {
                                                BitSet xzValues = xzCell.getPotentialValues();
                                                if (xzValues.cardinality() == 2) {
                                                    // Potential XZ cell found
                                                    for (Cell yzCell : vwxyzCell.getHouseCells()) {
                                                        if (!(yzCell.getX() == vzCell.getX() && yzCell.getY() == vzCell.getY()) &&
                                                                !(yzCell.getX() == wzCell.getX() && yzCell.getY() == wzCell.getY()) &&
                                                                !(yzCell.getX() == xzCell.getX() && yzCell.getY() == xzCell.getY())) {
                                                            BitSet yzValues = yzCell.getPotentialValues();
                                                            if (yzValues.cardinality() == 2) {
                                                                // Potential YZ cell found
                                                                if (isVWXYZWing(vwxyzValues, vzValues, wzValues, xzValues, yzValues)) {
                                                                    // Found VWXYZ-Wing pattern
                                                                    VWXYZWingHint hint = createHint(
                                                                            vwxyzCell, vzCell, wzCell, xzCell, yzCell,
                                                                            vzValues, wzValues, xzValues, yzValues);
                                                                    if (hint.isWorth())
                                                                        accu.add(hint);
                                                                } // if isVWXYZWing(vwxyzValues, vzValues, wzValues, xzValues, yzValues)
                                                            } // if yzValues.cardinality() == 2
                                                        } // if yzCell != vzCell && wzCell && xzCell
                                                    } // for Cell yzCell : vwxyzCell.getHouseCells()
                                                } // if xzValues.cardinality() == 2
                                            } // if xzCell != vzCell && wzCell
                                        } // for Cell xzCell : vwxyzCell.getHouseCells()
                                    } // if wzValues.cardinality() == 2
                                } // if wzCell != vzCell
                            } // for Cell wzCell : vwxyzCell.getHouseCells()
                        } // if vzValues.cardinality() == 2
                    } // for Cell vzCell : vwxyzCell.getHouseCells()
                } // if vwxyzValues.cardinality() == targetCardinality
            } // for x
        } // for y
    }

    private VWXYZWingHint createHint(
            Cell vwxyzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues) {
        // Get the "z" value
        BitSet inter = (BitSet)vzValues.clone();
        inter.and(wzValues);
        inter.and(xzValues);
        inter.and(yzValues);
        int zValue = inter.nextSetBit(0);

        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
        Set<Cell> victims = new LinkedHashSet<>(vzCell.getHouseCells());
        victims.retainAll(wzCell.getHouseCells());
        victims.retainAll(xzCell.getHouseCells());
        victims.retainAll(yzCell.getHouseCells());
        if (!isIncompletedPivot)
            victims.retainAll(vwxyzCell.getHouseCells());
        victims.remove(vwxyzCell);
        victims.remove(vzCell);
        victims.remove(wzCell);
        victims.remove(xzCell);
        victims.remove(yzCell);
        for (Cell cell : victims) {
            if (cell.hasPotentialValue(zValue)) {
                removablePotentials.put(cell, SingletonBitSet.create(zValue));
            }
        }

        // Create hint
        return new VWXYZWingHint(this, removablePotentials,
                vwxyzCell, vzCell, wzCell, xzCell, yzCell, zValue, isIncompletedPivot);
    }

    @Override
    public String toString() {
        return "VWXYZ-Wings & WXYZ-Wing Extensions";
    }
}
