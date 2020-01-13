package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Implementation of the "UVWXYZ-Wing" solving technique.
 * Similar to ALS-XZ with smaller ALS being a bivalue cell
 * can catch the double linked version which is similar to Sue-De-Coq
 * Larger  ALS has 5 cells in which cell candidates any size between 2-6
 */
public class UVWXYZWing implements IndirectHintProducer {

    /**
     * 
     * <ul>
     * <li>UVWXYZ-Wing</li>
     * <li>ALS-xz with a bivalue cell</li>
	 * <li>By SudokuMonster 2019</li>
     * </ul>
     */

    @Override
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
		List<UVWXYZWingHint> hintsFinal;
        hintsFinal = getHints(grid);
		// Sort the result
		Collections.sort(hintsFinal, new Comparator<UVWXYZWingHint>() {
			public int compare(UVWXYZWingHint h1, UVWXYZWingHint h2) {
				double d1 = h1.getDifficulty();
				double d2 = h2.getDifficulty();
				int e1 = h1.getEliminationsTotal();
				int e2 = h2.getEliminationsTotal();
				String s1 = h1.getSuffix();
				String s2 = h2.getSuffix();
				//sort according to difficulty in ascending order
				if (d1 < d2)
					return -1;
				else if (d1 > d2)
					return 1;
				//sort according to number of eliminations in descending order
				if ((e2 - e1) != 0)
					return e2 - e1;
				//sort according to suffix in reverse lexographic order
				return s2.compareTo(s1);
			}
		});
		for (UVWXYZWingHint hint : hintsFinal)
			accu.add(hint);		
	}

    private boolean isUVWXYZWing(BitSet UVWXYZValues,BitSet uzValues,BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet aBit, Cell yzCell, Cell xzCell, Cell wzCell, Cell vzCell, Cell uzCell, Cell UVWXYZCell) {
        BitSet inter = (BitSet)aBit.clone();
		inter.and(xzValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(xzCell))
			return false;
        inter = (BitSet)aBit.clone();
		inter.and(wzValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(wzCell))
			return false;		
        inter = (BitSet)aBit.clone();
		inter.and(vzValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(vzCell))
			return false;
        inter = (BitSet)aBit.clone();
		inter.and(uzValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(uzCell))
			return false;
        inter = (BitSet)aBit.clone();
		inter.and(UVWXYZValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(UVWXYZCell))
			return false;			
        return true;
    }

    private List<UVWXYZWingHint> getHints(Grid grid) /*throws InterruptedException*/ {
		List<UVWXYZWingHint> result = new ArrayList<UVWXYZWingHint>();
		int biggestCardinality = 0;
		int biggestCardinality2 = 0;
		int biggestCardinality3 = 0;
		int biggestCardinality4 = 0;
		int biggestCardinality5 = 0;
		int wingSize;
		int w1Value = 0;
		int w2Value = 0;
		int w3Value = 0;
		int w4Value = 0;
		BitSet w1Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet w2Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet w3Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet w4Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet remCand	=  new BitSet(10);//candidates of set not in yzCell	
		for (int i = 0; i < 81; i++) {
			Cell UVWXYZCell = Grid.getCell(i);
			BitSet UVWXYZValues = grid.getCellPotentialValues(i);
			if (UVWXYZValues.cardinality() > 1 && UVWXYZValues.cardinality() < 7) {
				// Potential UVWXYZ cell found
				biggestCardinality = UVWXYZValues.cardinality();
				wingSize = UVWXYZValues.cardinality();				
				for (int uzCellIndex : UVWXYZCell.getForwardVisibleCellIndexes()) {
					BitSet uzValues = grid.getCellPotentialValues(uzCellIndex);
					BitSet inter = (BitSet)UVWXYZValues.clone();
					inter.or(uzValues);
					if (uzValues.cardinality() > 1 && inter.cardinality() < 7) {
						// Potential WZ cell found
						Cell uzCell = Grid.getCell(uzCellIndex);
						biggestCardinality2 = biggestCardinality;
						if (uzValues.cardinality() > biggestCardinality2)
							biggestCardinality2 = uzValues.cardinality();
						wingSize = UVWXYZValues.cardinality() + uzValues.cardinality();
						CellSet intersection1 = new CellSet(UVWXYZCell.getForwardVisibleCells());
						intersection1.retainAll(uzCell.getForwardVisibleCells());					
						for (Cell vzCell : intersection1) {
							int vzCellIndex = vzCell.getIndex();
							BitSet vzValues = grid.getCellPotentialValues(vzCellIndex);
							inter = (BitSet)UVWXYZValues.clone();
							inter.or(uzValues);
							inter.or(vzValues);
							if (vzValues.cardinality() > 1 && inter.cardinality() < 7) {
								// Potential XZ cell found
								biggestCardinality3 = biggestCardinality2;
								if (vzValues.cardinality() > biggestCardinality3)
									biggestCardinality3 = vzValues.cardinality();
								wingSize = UVWXYZValues.cardinality() + uzValues.cardinality() + vzValues.cardinality();
								CellSet intersection2 = new CellSet(vzCell.getForwardVisibleCells());
								intersection2.retainAll(intersection1);
								for (Cell wzCell : intersection2) {
									int wzCellIndex = wzCell.getIndex();
									BitSet wzValues = grid.getCellPotentialValues(wzCellIndex);
									inter = (BitSet)UVWXYZValues.clone();
									inter.or(uzValues);
									inter.or(vzValues);
									inter.or(wzValues);
									if (wzValues.cardinality() > 1 && inter.cardinality() < 7) {
										// Potential XZ cell found
										biggestCardinality4 = biggestCardinality3;
										if (wzValues.cardinality() > biggestCardinality4)
											biggestCardinality4 = wzValues.cardinality();
										wingSize = UVWXYZValues.cardinality() + uzValues.cardinality() + vzValues.cardinality() + wzValues.cardinality();
										CellSet intersection3 = new CellSet(wzCell.getForwardVisibleCells());
										intersection3.retainAll(intersection2);								
										for (Cell xzCell : intersection3) {
											int xzCellIndex = xzCell.getIndex();
											BitSet xzValues = grid.getCellPotentialValues(xzCellIndex);
											inter = (BitSet)UVWXYZValues.clone();
											inter.or(uzValues);									
											inter.or(vzValues);
											inter.or(wzValues);
											inter.or(xzValues);
											if (xzValues.cardinality() > 1 && inter.cardinality() == 6) {
												// Potential XZ cell found
												biggestCardinality5 = biggestCardinality4;
												if (xzValues.cardinality() > biggestCardinality5)
													biggestCardinality5 = xzValues.cardinality();
												wingSize = UVWXYZValues.cardinality() + uzValues.cardinality() + vzValues.cardinality() + wzValues.cardinality() + xzValues.cardinality();
												//Restrict potential yzCell to Grid Cells that are visible by one or more of the other cells
												CellSet yzCellRange = new CellSet(UVWXYZCell.getVisibleCells());
												yzCellRange.addAll(uzCell.getVisibleCells());
												yzCellRange.addAll(vzCell.getVisibleCells());
												yzCellRange.addAll(wzCell.getVisibleCells());
												yzCellRange.addAll(xzCell.getVisibleCells());
												yzCellRange.remove(UVWXYZCell);
												yzCellRange.remove(uzCell);			
												yzCellRange.remove(vzCell);			
												yzCellRange.remove(wzCell);
												yzCellRange.remove(xzCell);									
												for (Cell yzCell : yzCellRange) {
													int yzCellIndex = yzCell.getIndex();
													BitSet yzValues = grid.getCellPotentialValues(yzCellIndex);
													BitSet union = (BitSet)yzValues.clone();
													union.and(inter);
													if (yzValues.cardinality() == 2 && union.cardinality() == 2) {
														// Potential YZ cell found
														// Get the "z" value and the "x" value in ALS-xz
														boolean doubleLink = true;//assume doubly linked until testing
														int zValue;
														int xValue;
														BitSet zBit = new BitSet(10);
														BitSet xBit = new BitSet(10);
															xValue = yzValues.nextSetBit(0);
															xBit.set(xValue);
															zValue = yzValues.nextSetBit(xValue + 1);
															zBit.set(zValue);													
														if (!isUVWXYZWing(UVWXYZValues, uzValues, vzValues, wzValues, xzValues, zBit, yzCell, xzCell, wzCell, vzCell, uzCell, UVWXYZCell))//Test if doubly linked
																doubleLink = false;
														if (isUVWXYZWing(UVWXYZValues, uzValues, vzValues, wzValues, xzValues, xBit, yzCell, xzCell, wzCell, vzCell, uzCell, UVWXYZCell)) {
															if (!doubleLink) {
																// Found UVWXYZ-Wing pattern
																UVWXYZWingHint hint = createHint(
																		grid, UVWXYZCell, Grid.getCell(uzCellIndex), Grid.getCell(vzCellIndex), Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																		uzValues, vzValues, wzValues, xzValues, yzValues, UVWXYZValues, xValue, zValue, xBit, zBit, biggestCardinality5, wingSize, doubleLink, w1Value, w2Value, w3Value, w4Value, w1Bit, w2Bit, w3Bit, w4Bit, remCand, inter);
																if (hint.isWorth())
																	result.add(hint);
															}
															else {
																// Found UVWXYZ-Wing doubly linked pattern
																remCand = (BitSet)inter.clone();
																remCand.xor(yzValues);
																w1Value = remCand.nextSetBit(0);
																w1Bit =  new BitSet(10);
																w1Bit.set(w1Value);
																w2Value = remCand.nextSetBit(w1Value+1);
																w2Bit =  new BitSet(10);
																w2Bit.set(w2Value);
																w3Value = remCand.nextSetBit(w2Value+1);
																w3Bit =  new BitSet(10);
																w3Bit.set(w3Value);
																w4Value = remCand.nextSetBit(w3Value+1);
																w4Bit =  new BitSet(10);
																w4Bit.set(w4Value);
																UVWXYZWingHint hint = createHint(
																		grid, UVWXYZCell, Grid.getCell(uzCellIndex), Grid.getCell(vzCellIndex), Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																		uzValues, vzValues, wzValues, xzValues, yzValues, UVWXYZValues, xValue, zValue, xBit, zBit, biggestCardinality5, wingSize, doubleLink, w1Value, w2Value, w3Value, w4Value, w1Bit, w2Bit, w3Bit, w4Bit, remCand, inter);
																if (hint.isWorth())
																	result.add(hint);
															}
														} // if (isUVWXYZWing(UVWXYZValues, uzValues, vzValues, wzValues, xzValues, xBit, yzCell, xzCell, wzCell, vzCell, uzCell, UVWXYZCell))
														else
															if (doubleLink) {
																doubleLink = false;
																UVWXYZWingHint hint = createHint(
																		grid, UVWXYZCell, Grid.getCell(uzCellIndex), Grid.getCell(vzCellIndex), Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																		uzValues, vzValues, wzValues, xzValues, yzValues, UVWXYZValues, zValue, xValue, zBit, xBit, biggestCardinality5, wingSize, doubleLink, w1Value, w2Value, w3Value, w4Value, w1Bit, w2Bit, w3Bit, w4Bit, remCand, inter);
																if (hint.isWorth())
																	result.add(hint);
															}//if (doubleLink)
													} // if (yzValues.cardinality() == 2 && union.cardinality() == 2) {
												} // for (Cell yzCell : yzCellRange)
											} // if (xzValues.cardinality() > 1 && inter.cardinality() == 6)
										} // for (Cell xzCell : intersection3)
									} // if (wzValues.cardinality() > 1 && inter.cardinality() < 7)
								} // for (Cell wzCell : intersection2)
							} // if (vzValues.cardinality() > 1 && inter.cardinality() < 7)
						} // for (Cell vzCell : intersection1)
					} // if (uzValues.cardinality() > 1 && inter.cardinality() < 7)
				} // for (int uzCellIndex : UVWXYZCell.getForwardVisibleCellIndexes())
			} // (UVWXYZValues.cardinality() > 1 && UVWXYZValues.cardinality() < 7) 
        } // for i
		return result;
    }

    private UVWXYZWingHint createHint(
            Grid grid, Cell UVWXYZCell, Cell uzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet uzValues, BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues, BitSet UVWXYZValues, int xValue, int zValue, BitSet xBit, BitSet zBit, int biggestCardinality, int wingSize, boolean doubleLink, int w1Value, int w2Value, int w3Value, int w4Value, BitSet w1Bit, BitSet w2Bit, BitSet w3Bit, BitSet w4Bit, BitSet remCand, BitSet wingSet) {
        // Build list of removable potentials
		boolean weakPotentials = false;
		boolean strongPotentialsX = false;//if both remain false then proceed as normal UVWXYZ if weak is false strong is true and z is false then swap z to x
		boolean strongPotentialsZ = false;//if both remain false then proceed as normal UVWXYZ if weak is false strong is true and z is false then swap z to x        
		BitSet inter = (BitSet)zBit.clone();
		Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
		int eliminationsTotal = 0;
		CellSet victims = null;
		if (doubleLink) {//if no eliminations at all then produce Hint as regular UVWXYZ
			inter = (BitSet)w1Bit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
						victims = new CellSet (xzCell.getVisibleCells());
			inter = (BitSet)w1Bit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)w1Bit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vzCell.getVisibleCells());
					else
						victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)w1Bit.clone();
			inter.and(uzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (uzCell.getVisibleCells());
					else
						victims.retainAll(uzCell.getVisibleCells());
			inter = (BitSet)w1Bit.clone();
			inter.and(UVWXYZValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (UVWXYZCell.getVisibleCells());
					else
						victims.retainAll(UVWXYZCell.getVisibleCells());
			victims.remove(UVWXYZCell);
			victims.remove(uzCell);			
			victims.remove(vzCell);			
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w1Value)) {		
					eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(w1Value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(w1Value));
					weakPotentials = true;
				}
			}
			victims = null;
			inter = (BitSet)w2Bit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
					victims = new CellSet (xzCell.getVisibleCells());
			inter = (BitSet)w2Bit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)w2Bit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vzCell.getVisibleCells());
					else
						victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)w2Bit.clone();
			inter.and(uzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (uzCell.getVisibleCells());
					else
						victims.retainAll(uzCell.getVisibleCells());
			inter = (BitSet)w2Bit.clone();
			inter.and(UVWXYZValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (UVWXYZCell.getVisibleCells());
					else
						victims.retainAll(UVWXYZCell.getVisibleCells());
			victims.remove(UVWXYZCell);
			victims.remove(uzCell);			
			victims.remove(vzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w2Value)) {
					eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(w2Value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(w2Value));
					weakPotentials = true;
				}
			}
			victims = null;
			inter = (BitSet)w3Bit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
					victims = new CellSet (xzCell.getVisibleCells());
			inter = (BitSet)w3Bit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)w3Bit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vzCell.getVisibleCells());
					else
						victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)w3Bit.clone();
			inter.and(uzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (uzCell.getVisibleCells());
					else
						victims.retainAll(uzCell.getVisibleCells());
			inter = (BitSet)w3Bit.clone();
			inter.and(UVWXYZValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (UVWXYZCell.getVisibleCells());
					else
						victims.retainAll(UVWXYZCell.getVisibleCells());
			victims.remove(UVWXYZCell);
			victims.remove(uzCell);
			victims.remove(vzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w3Value)) {
					eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(w3Value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(w3Value));
					weakPotentials = true;
				}
			}
			victims = null;
			inter = (BitSet)w4Bit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
					victims = new CellSet (xzCell.getVisibleCells());
			inter = (BitSet)w4Bit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)w4Bit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vzCell.getVisibleCells());
					else
						victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)w4Bit.clone();
			inter.and(uzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (uzCell.getVisibleCells());
					else
						victims.retainAll(uzCell.getVisibleCells());
			inter = (BitSet)w4Bit.clone();
			inter.and(UVWXYZValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (UVWXYZCell.getVisibleCells());
					else
						victims.retainAll(UVWXYZCell.getVisibleCells());
			victims.remove(UVWXYZCell);
			victims.remove(uzCell);			
			victims.remove(vzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w4Value)) {
					eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(w4Value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(w4Value));
					weakPotentials = true;
				}
			}			
			victims = new CellSet(yzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(xzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(uzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(uzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(UVWXYZValues);
			if (inter.cardinality() == 1)
				victims.retainAll(UVWXYZCell.getVisibleCells());
			victims.remove(UVWXYZCell);
			victims.remove(uzCell);
			victims.remove(vzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), xValue)) {
					eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(xValue);
					else
						removablePotentials.put(cell, SingletonBitSet.create(xValue));
					strongPotentialsX = true;
				}
			}
		}
		victims = new CellSet(yzCell.getVisibleCells());
		inter = (BitSet)zBit.clone();
		inter.and(xzValues);
		if (inter.cardinality() == 1)
			victims.retainAll(xzCell.getVisibleCells());
		inter = (BitSet)zBit.clone();
		inter.and(wzValues);
        if (inter.cardinality() == 1)
			victims.retainAll(wzCell.getVisibleCells());
		inter = (BitSet)zBit.clone();
		inter.and(vzValues);
        if (inter.cardinality() == 1)
			victims.retainAll(vzCell.getVisibleCells());
		inter = (BitSet)zBit.clone();
		inter.and(uzValues);
        if (inter.cardinality() == 1)
			victims.retainAll(uzCell.getVisibleCells());
 		inter = (BitSet)zBit.clone();
		inter.and(UVWXYZValues);
        if (inter.cardinality() == 1)
			victims.retainAll(UVWXYZCell.getVisibleCells());
        victims.remove(UVWXYZCell);
		victims.remove(uzCell);
		victims.remove(vzCell);
        victims.remove(wzCell);
        victims.remove(xzCell);
        victims.remove(yzCell);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), zValue)) {
					eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(zValue);
					else
						removablePotentials.put(cell, SingletonBitSet.create(zValue));
					strongPotentialsZ = true;
            }
        }
		if (doubleLink)
			if (!weakPotentials)
				if (!strongPotentialsZ) {
					doubleLink = false;
					return new UVWXYZWingHint(this, removablePotentials,
						UVWXYZCell, uzCell, vzCell, wzCell, xzCell, yzCell, xValue, zValue, biggestCardinality, wingSize, doubleLink, wingSet, eliminationsTotal);					
				}
				else
					if (!strongPotentialsX)
						doubleLink = false;
        // Create hint
        return new UVWXYZWingHint(this, removablePotentials,
                UVWXYZCell, uzCell, vzCell, wzCell, xzCell, yzCell, zValue, xValue, biggestCardinality, wingSize, doubleLink, wingSet, eliminationsTotal);
    }

    @Override
    public String toString() {
        return "UVWXYZ-Wings";
    }
}
