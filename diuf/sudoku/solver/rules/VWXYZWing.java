package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


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

 
	private boolean isVWXYZWing(BitSet vwxyzValues, BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues) {
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
        for (int i = 0; i < 81; i++) {
										
			Cell vwxyzCell = Grid.getCell(i);
			BitSet vwxyzValues = grid.getCellPotentialValues(i);
			if (vwxyzValues.cardinality() == targetCardinality) {
				// Potential VWXYZ cell found
				for (int vzCellIndex : vwxyzCell.getVisibleCellIndexes()) {
					BitSet vzValues = grid.getCellPotentialValues(vzCellIndex);
					if (vzValues.cardinality() == 2) {
						// Potential VZ Cell found
						Cell vzCell = Grid.getCell(vzCellIndex);
						for (int wzCellIndex : vwxyzCell.getVisibleCellIndexes()) {
							Cell wzCell = Grid.getCell(wzCellIndex);
							
							
							
							
							if (wzCell.getX() != vzCell.getX() || wzCell.getY() != vzCell.getY()) {
								BitSet wzValues = grid.getCellPotentialValues(wzCellIndex);
								if (wzValues.cardinality() == 2) {
									// Potential WZ cell found
									for (int xzCellIndex : vwxyzCell.getVisibleCellIndexes()) {
										Cell xzCell = Grid.getCell(xzCellIndex);
										if (!(xzCell.getX() == vzCell.getX() && xzCell.getY() == vzCell.getY()) &&
												!(xzCell.getX() == wzCell.getX() && xzCell.getY() == wzCell.getY())) {
											BitSet xzValues = grid.getCellPotentialValues(xzCellIndex);
											if (xzValues.cardinality() == 2) {
												// Potential XZ cell found
												for (int yzCellIndex : vwxyzCell.getVisibleCellIndexes()) {
													Cell yzCell = Grid.getCell(yzCellIndex);
													if (!(yzCell.getX() == vzCell.getX() && yzCell.getY() == vzCell.getY()) &&
															!(yzCell.getX() == wzCell.getX() && yzCell.getY() == wzCell.getY()) &&
															!(yzCell.getX() == xzCell.getX() && yzCell.getY() == xzCell.getY())) {
														BitSet yzValues = grid.getCellPotentialValues(yzCellIndex);
														if (yzValues.cardinality() == 2) {
															// Potential YZ cell found
															if (isVWXYZWing(vwxyzValues, vzValues, wzValues, xzValues, yzValues)) {
																// Found VWXYZ-Wing pattern
																VWXYZWingHint hint = createHint(
																		grid, vwxyzCell, Grid.getCell(vzCellIndex), Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																		vzValues, wzValues, xzValues, yzValues);
																if (hint.isWorth())
																	accu.add(hint);
															} // if isVWXYZWing(vwxyzValues, vzValues, wzValues, xzValues, yzValues)
														} // if yzValues.cardinality() == 2
													} // if yzCell != vzCell && wzCell && xzCell
												} // for Cell yzCell : vwxyzCell.getVisibleCellIndexes()
											} // if xzValues.cardinality() == 2
										} // if xzCell != vzCell && wzCell
									} // for Cell xzCell : vwxyzCell.getVisibleCellIndexes()
								} // if wzValues.cardinality() == 2
							} // if wzCell != vzCell
						} // for Cell wzCell : vwxyzCell.getVisibleCellIndexes()
					} // if vzValues.cardinality() == 2
				} // for Cell vzCell : vwxyzCell.getVisibleCellIndexes()
			} // if vwxyzValues.cardinality() == targetCardinality
        } // for i
    }

    private VWXYZWingHint createHint(
            Grid grid, Cell vwxyzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues) {
        // Get the "z" value
        BitSet inter = (BitSet)vzValues.clone();
        inter.and(wzValues);
        inter.and(xzValues);
        inter.and(yzValues);
        int zValue = inter.nextSetBit(0);

        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
        //Set<Cell> victims = new LinkedHashSet<>(vzCell.getHouseCells());
        CellSet victims = new CellSet(vzCell.getVisibleCells());
		victims.retainAll(wzCell.getVisibleCells());
        victims.retainAll(xzCell.getVisibleCells());
        victims.retainAll(yzCell.getVisibleCells());
        if (!isIncompletedPivot)
            victims.retainAll(vwxyzCell.getVisibleCells());
        victims.remove(vwxyzCell);
        victims.remove(vzCell);
        victims.remove(wzCell);
        victims.remove(xzCell);
        victims.remove(yzCell);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), zValue)) {
                removablePotentials.put(cell, SingletonBitSet.create(zValue));
            }
        }

        // Create hint
        return new VWXYZWingHint(this, removablePotentials,
                vwxyzCell, vzCell, wzCell, xzCell, yzCell, zValue, isIncompletedPivot);
    }

    @Override
    public String toString() {
        return "VWXYZ-Wings";
    }
}
