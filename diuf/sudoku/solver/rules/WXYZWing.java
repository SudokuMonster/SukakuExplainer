package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Implementation of the "WXYZ-Wing" solving technique.
 * Similar to ALS-XZ with smaller ALS being a bivalue cell
 * can catch the double linked version which is similar to Sue-De-Coq
 * Larger  ALS has 3 cells in which cell candidates any size between 2-4
 */
public class WXYZWing implements IndirectHintProducer {

    /**
     * 
     * <ul>
     * <li>WXYZ-Wing</li>
     * <li>ALS-xz with a bivalue cell</li>
	 * <li>By SudokuMonster 2019</li>
     * </ul>
     */

    @Override
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
		List<WXYZWingHint> hintsFinal;
        hintsFinal = getHints(grid);
		// Sort the result
		Collections.sort(hintsFinal, new Comparator<WXYZWingHint>() {
			public int compare(WXYZWingHint h1, WXYZWingHint h2) {
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
		for (WXYZWingHint hint : hintsFinal)
			accu.add(hint);		
	}
	
    private boolean isWXYZWing(BitSet wxyzValues,BitSet wzValues, BitSet xzValues, BitSet aBit, Cell yzCell, Cell xzCell, Cell wzCell, Cell wxyzCell) {
        BitSet inter = (BitSet)aBit.clone();
		inter.and(xzValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(xzCell))
			return false;
        inter = (BitSet)aBit.clone();
		inter.and(wzValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(wzCell))
			return false;		
        inter = (BitSet)aBit.clone();
		inter.and(wxyzValues);
		if (inter.cardinality() == 1 && !yzCell.canSeeCell(wxyzCell))
			return false;			
        return true;
    }

    private List<WXYZWingHint> getHints(Grid grid) /*throws InterruptedException*/ {
		List<WXYZWingHint> result = new ArrayList<WXYZWingHint>();
		int biggestCardinality = 0;
		int biggestCardinality2 = 0;
		int biggestCardinality3 = 0;
		int wingSize;
		int w1Value = 0;
		int w2Value = 0;
		BitSet w1Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet w2Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet remCand	=  new BitSet(10);//candidates of set not in yzCell	
		for (int i = 0; i < 81; i++) {
			Cell wxyzCell = Grid.getCell(i);
			BitSet wxyzValues = grid.getCellPotentialValues(i);
			if (wxyzValues.cardinality() > 1 && wxyzValues.cardinality() < 5) {
				// Potential WXYZ cell found
				biggestCardinality = wxyzValues.cardinality();
				wingSize = wxyzValues.cardinality();
				for (int wzCellIndex : wxyzCell.getForwardVisibleCellIndexes()) {
					BitSet wzValues = grid.getCellPotentialValues(wzCellIndex);
					BitSet inter = (BitSet)wxyzValues.clone();
					inter.or(wzValues);
					if (wzValues.cardinality() > 1 && inter.cardinality() < 5) {
						// Potential XZ cell found
						Cell wzCell = Grid.getCell(wzCellIndex);
						biggestCardinality2 = biggestCardinality;
						if (wzValues.cardinality() > biggestCardinality2)
							biggestCardinality2 = wzValues.cardinality();
						wingSize = wxyzValues.cardinality() + wzValues.cardinality();
						CellSet intersection1 = new CellSet(wxyzCell.getForwardVisibleCells());
						intersection1.retainAll(wzCell.getForwardVisibleCells());
						for (Cell xzCell : intersection1) {
							int xzCellIndex = xzCell.getIndex();
							BitSet xzValues = grid.getCellPotentialValues(xzCellIndex);
							inter = (BitSet)wxyzValues.clone();
							inter.or(wzValues);
							inter.or(xzValues);
							if (xzValues.cardinality() > 1 && inter.cardinality() == 4) {
								// Potential XZ cell found
								biggestCardinality3 = biggestCardinality2;
								if (xzValues.cardinality() > biggestCardinality3)
									biggestCardinality3 = xzValues.cardinality();
								wingSize = wxyzValues.cardinality() + wzValues.cardinality() + xzValues.cardinality();
								//Restrict potential yzCell to Grid Cells that are visible by one or more of the other cells
								CellSet yzCellRange = new CellSet(wxyzCell.getVisibleCells());
								yzCellRange.addAll(wzCell.getVisibleCells());
								yzCellRange.addAll(xzCell.getVisibleCells());
								yzCellRange.remove(wxyzCell);		
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
										if (!isWXYZWing(wxyzValues, wzValues, xzValues, zBit, yzCell, xzCell, wzCell, wxyzCell))//Test if doubly linked
												doubleLink = false;
										if (isWXYZWing(wxyzValues, wzValues, xzValues, xBit, yzCell, xzCell, wzCell, wxyzCell)) {
											if (!doubleLink) {
												// Found WXYZ-Wing pattern
												WXYZWingHint hint = createHint(
														grid, wxyzCell, Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
														wzValues, xzValues, yzValues, wxyzValues, xValue, zValue, xBit, zBit, biggestCardinality3, wingSize, doubleLink, w1Value, w2Value, w1Bit, w2Bit, remCand, inter);
												if (hint.isWorth())
													result.add(hint);
											}
											else {
												// Found WXYZ-Wing doubly linked pattern
												remCand = (BitSet)inter.clone();
												remCand.xor(yzValues);
												w1Value = remCand.nextSetBit(0);
												w1Bit =  new BitSet(10);
												w1Bit.set(w1Value);
												w2Value = remCand.nextSetBit(w1Value+1);
												w2Bit =  new BitSet(10);
												w2Bit.set(w2Value);
												WXYZWingHint hint = createHint(
														grid, wxyzCell, Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
														wzValues, xzValues, yzValues, wxyzValues, xValue, zValue, xBit, zBit, biggestCardinality3, wingSize, doubleLink, w1Value, w2Value, w1Bit, w2Bit, remCand, inter);
												if (hint.isWorth())
													result.add(hint);
											}
										} // if (isWXYZWing(wxyzValues, wzValues, xzValues, xBit, yzCell, xzCell, wzCell, wxyzCell))
										else
											if (doubleLink) {
												doubleLink = false;
												WXYZWingHint hint = createHint(
														grid, wxyzCell, Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
														wzValues, xzValues, yzValues, wxyzValues, zValue, xValue, zBit, xBit, biggestCardinality3, wingSize, doubleLink, w1Value, w2Value, w1Bit, w2Bit, remCand, inter);
												if (hint.isWorth())
													result.add(hint);
											}//if (doubleLink)
									} // if (yzValues.cardinality() == 2 && union.cardinality() == 2) {
								} // for (Cell yzCell : yzCellRange)
							} // if (xzValues.cardinality() > 1 && inter.cardinality() == 4)
						} // for (Cell xzCell : intersection1)
					} // if (wzValues.cardinality() > 1 && inter.cardinality() < 5)
				} // for (int wzCellIndex : wxyzCell.getForwardVisibleCellIndexes())
			} // (wxyzValues.cardinality() > 1 && wxyzValues.cardinality() < 5) 
        } // for i
		return result;
    }

    private WXYZWingHint createHint(
            Grid grid, Cell wxyzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet wzValues, BitSet xzValues, BitSet yzValues, BitSet wxyzValues, int xValue, int zValue, BitSet xBit, BitSet zBit, int biggestCardinality, int wingSize, boolean doubleLink, int w1Value, int w2Value, BitSet w1Bit, BitSet w2Bit, BitSet remCand, BitSet wingSet) {
        // Build list of removable potentials
		boolean weakPotentials = false;
		boolean strongPotentialsX = false;//if both remain false then proceed as normal WXYZ if weak is false strong is true and z is false then swap z to x
		boolean strongPotentialsZ = false;//if both remain false then proceed as normal WXYZ if weak is false strong is true and z is false then swap z to x        
		BitSet inter = (BitSet)zBit.clone();
		Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
		int eliminationsTotal = 0;
		CellSet victims = null;
		if (doubleLink) {//if no eliminations at all then produce Hint as regular WXYZ
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
			inter.and(wxyzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wxyzCell.getVisibleCells());
					else
						victims.retainAll(wxyzCell.getVisibleCells());
			victims.remove(wxyzCell);		
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
			inter.and(wxyzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wxyzCell.getVisibleCells());
					else
						victims.retainAll(wxyzCell.getVisibleCells());
			victims.remove(wxyzCell);
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
			inter.and(wxyzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(wxyzCell.getVisibleCells());
			victims.remove(wxyzCell);
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
		inter.and(wxyzValues);
        if (inter.cardinality() == 1)
			victims.retainAll(wxyzCell.getVisibleCells());
        victims.remove(wxyzCell);
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
					return new WXYZWingHint(this, removablePotentials,
						wxyzCell, wzCell, xzCell, yzCell, xValue, zValue, biggestCardinality, wingSize, doubleLink, wingSet, eliminationsTotal);					
				}
				else
					if (!strongPotentialsX)
						doubleLink = false;
        // Create hint
        return new WXYZWingHint(this, removablePotentials,
                wxyzCell, wzCell, xzCell, yzCell, zValue, xValue, biggestCardinality, wingSize, doubleLink, wingSet, eliminationsTotal);
    }

    @Override
    public String toString() {
        return "WXYZ-Wings";
    }
}
