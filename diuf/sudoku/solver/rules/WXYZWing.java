package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Implementation of the "WXYZ-Wing" and its pivot-incompleted type solving techniques.
 */
public class WXYZWing implements IndirectHintProducer {

    /**
     * Stands for whether pivot cell is completed or not.
     * <ul>
     * <li>Completed: WXYZ WZ XZ YZ</li>
     * <li>Incompleted: WXY WZ XZ YZ</li>
     * </ul>
     */
    private final boolean isIncompletedPivot;

    public WXYZWing(boolean isIncompletedPivot) {
        this.isIncompletedPivot = isIncompletedPivot;
    }

    private boolean isWXYZWing(BitSet wxyzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues) {
        if (wxyzValues.cardinality() != (isIncompletedPivot ? 3 : 4) ||
                wzValues.cardinality() != 2 ||
                xzValues.cardinality() != 2 ||
                yzValues.cardinality() != 2)
            return false;

        BitSet union = (BitSet)wxyzValues.clone();
        union.or(wzValues);
        union.or(xzValues);
        union.or(yzValues);
        BitSet inter = (BitSet)union.clone();
        if (!isIncompletedPivot)
            inter.and(wxyzValues);
        inter.and(wzValues);
        inter.and(xzValues);
        inter.and(yzValues);
        BitSet[] innerProduct = {
                (BitSet)wzValues.clone(),
                (BitSet)wzValues.clone(),
                (BitSet)xzValues.clone()
        };
        innerProduct[0].and(xzValues);
        innerProduct[1].and(yzValues);
        innerProduct[2].and(yzValues);
        BitSet[] outerProduct = {
                (BitSet)wxyzValues.clone(),
                (BitSet)wxyzValues.clone(),
                (BitSet)wxyzValues.clone()
        };
        outerProduct[0].and(wzValues);
        outerProduct[1].and(xzValues);
        outerProduct[2].and(yzValues);
        boolean hasSameBitSet = false;
        boolean hasSameDigit = true;
        for (BitSet b : innerProduct) {
            hasSameBitSet = hasSameBitSet || (b.cardinality() == 2);
        }
        for (BitSet b : outerProduct) {
            hasSameDigit = hasSameDigit && (b.cardinality() == (isIncompletedPivot ? 1 : 2));
        }

        return union.cardinality() == 4 && inter.nextSetBit(0) != -1 && !hasSameBitSet && hasSameDigit;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        final int targetCardinality = isIncompletedPivot ? 3 : 4;
        for (int i = 0; i < 81; i++) {
			Cell wxyzCell = Grid.getCell(i);
			BitSet wxyzValues = grid.getCellPotentialValues(i);
			if (wxyzValues.cardinality() == targetCardinality) {
				// Potential WXYZ cell found
				for (int wzCellIndex : wxyzCell.getVisibleCellIndexes()) {
					BitSet wzValues = grid.getCellPotentialValues(wzCellIndex);
					if (wzValues.cardinality() == 2) {
						// Potential WZ cell found
						Cell wzCell = Grid.getCell(wzCellIndex);
						for (int xzCellIndex : wxyzCell.getVisibleCellIndexes()) {
							Cell xzCell = Grid.getCell(xzCellIndex);
							if (!(xzCell.getX() == wzCell.getX() && xzCell.getY() == wzCell.getY())) {
								BitSet xzValues = grid.getCellPotentialValues(xzCellIndex);
								if (xzValues.cardinality() == 2) {
									// Potential XZ cell found
									for (int yzCellIndex : wxyzCell.getVisibleCellIndexes()) {
										Cell yzCell = Grid.getCell(yzCellIndex);
										if (!(yzCell.getX() == xzCell.getX() && yzCell.getY() == xzCell.getY()) &&
												!(yzCell.getX() == wzCell.getX() && yzCell.getY() == wzCell.getY())) {
											BitSet yzValues = grid.getCellPotentialValues(yzCellIndex);
											if (yzValues.cardinality() == 2) {
												// Potential YZ cell found
												if (isWXYZWing(wxyzValues, wzValues, xzValues, yzValues)) {
													// Found WXYZ-Wing pattern
													WXYZWingHint hint = createHint(
															grid, wxyzCell, Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
															wzValues, xzValues, yzValues);
													if (hint.isWorth())
														accu.add(hint);
												} // if isWXYZWing(wxyzValues, wzValues, xzValues, yzValues)
											} // if yzValues.cardinality() == 2
										} // if yzCellIndex.getX() != xzCellIndex.getX() && yzCellIndex.getY() != xzCellIndex.getY()
									} // for Cell yzCellIndex : wxyzCell.getVisibleCells()
								} // if xzValues.cardinality() == 2
							} // if xzCellIndex.getX() != wzCellIndex.getX() && xzCellIndex.getY() != wzCellIndex.getY()
						} // for Cell xzCellIndex : wxyzCell.getVisibleCells()
					} // if wzValues.cardinality() == 2
				} // for Cell wzCellIndex : wxyzCell.getVisibleCells()
			} // if wxyzValues.cardinality() == targetCardinality
        } // for i
    }

    private WXYZWingHint createHint(
            Grid grid, Cell wxyzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet wzValues, BitSet xzValues, BitSet yzValues) {
        // Get the "z" value
        BitSet inter = (BitSet)wzValues.clone();
        inter.and(xzValues);
        inter.and(yzValues);
        int zValue = inter.nextSetBit(0);

        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
        //Set<Cell> victims = new LinkedHashSet<>(wzCell.getHouseCells(grid));
        CellSet victims = new CellSet(wzCell.getVisibleCells());
		victims.retainAll(xzCell.getVisibleCells());
        victims.retainAll(yzCell.getVisibleCells());
        if (!isIncompletedPivot)
            victims.retainAll(wxyzCell.getVisibleCells());
        victims.remove(wxyzCell);
        victims.remove(wzCell);
        victims.remove(xzCell);
        victims.remove(yzCell);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), zValue)) {
                removablePotentials.put(cell, SingletonBitSet.create(zValue));
            }
        }

        // Create hint
        return new WXYZWingHint(this, removablePotentials,
                wxyzCell, wzCell, xzCell, yzCell, zValue, isIncompletedPivot);
    }

    @Override
    public String toString() {
        return "WXYZ-Wings & Extended XYZ-Wing";
    }
}
