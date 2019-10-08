package diuf.sudoku.solver.rules;

import java.util.*;
//import javax.swing.JOptionPane;//added only when alert Pane is needed
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Implementation of the "WXYZ-Wing" and its pivot-incompleted type solving techniques.
 */
public class WXYZWing implements IndirectHintProducer {

    /**
     * 
     * <ul>
     * <li>WXYZ-Wing</li>
     * <li>ALS-xz with a bivalue cell</li>
     * </ul>
     */

    private boolean isWXYZWing(BitSet wzValues, BitSet xzValues, BitSet aBit, Cell yzCell, Cell xzCell, Cell wzCell) {
        BitSet inter = (BitSet)aBit.clone();
		inter.and(xzValues);
		if (inter.cardinality() == 1 && !(yzCell.getX() == xzCell.getX() || yzCell.getY() == xzCell.getY() || yzCell.getB() == xzCell.getB()))
			return false;
        inter = (BitSet)aBit.clone();
		inter.and(wzValues);
		if (inter.cardinality() == 1 && !(yzCell.getX() == wzCell.getX() || yzCell.getY() == wzCell.getY() || yzCell.getB() == wzCell.getB()))
			return false;		
        return true;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
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
				for (int wzCellIndex : wxyzCell.getVisibleCellIndexes()) {
					BitSet wzValues = grid.getCellPotentialValues(wzCellIndex);
					BitSet inter = (BitSet)wxyzValues.clone();
					inter.or(wzValues);
					if (wzValues.cardinality() > 1 && inter.cardinality() < 5) {
						// Potential WZ cell found
						Cell wzCell = Grid.getCell(wzCellIndex);
						biggestCardinality2 = biggestCardinality;
						if (wzValues.cardinality() > biggestCardinality2)
							biggestCardinality2 = wzValues.cardinality();
						wingSize = wxyzValues.cardinality() + wzValues.cardinality();
						CellSet intersection1 = new CellSet(wxyzCell.getVisibleCells());
						intersection1.retainAll(wzCell.getVisibleCells());
						for (Cell xzCell : intersection1) {
							int xzCellIndex = xzCell.getIndex();
							//if (!(xzCell.getX() == wzCell.getX() && xzCell.getY() == wzCell.getY())) {
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
									CellSet intersection2 = new CellSet(xzCell.getVisibleCells());
									intersection2.retainAll(intersection1);
									for (int yzCellIndex : wxyzCell.getVisibleCellIndexes()) {
										Cell yzCell = Grid.getCell(yzCellIndex);
										if (!(yzCell.getX() == xzCell.getX() && yzCell.getY() == xzCell.getY()) &&
												!(yzCell.getX() == wzCell.getX() && yzCell.getY() == wzCell.getY())) {
											BitSet yzValues = grid.getCellPotentialValues(yzCellIndex);
											inter = (BitSet)wxyzValues.clone();
											inter.or(wzValues);
											inter.or(xzValues);
											inter.or(yzValues);
											BitSet union = (BitSet)yzValues.clone();
											union.and(wxyzValues);
											if (yzValues.cardinality() == 2 && inter.cardinality() == 4 && union.cardinality()>0) {
												// Potential YZ cell found
												//wingSize = wxyzValues.cardinality() + wzValues.cardinality() + xzValues.cardinality() + yzValues.cardinality(); //No need as always bbivalue
												// Get the "z" value and the "x" value in ALS-xz
												boolean doubleLink = true;//assume doubly liked until testing
												int zValue;
												int xValue;
												BitSet zBit = new BitSet(10);
												BitSet xBit = new BitSet(10);
												BitSet differ = (BitSet)yzValues.clone();
												if (union.cardinality() == 2) {
													xValue = yzValues.nextSetBit(0);
													xBit.set(xValue);
													zValue = yzValues.nextSetBit(xValue + 1);
													zBit.set(zValue);
			//JOptionPane.showMessageDialog( null, "Union Cardinality =2 xValue:" + xValue + "zValue:" + zValue );	
													
												}
												else {
													xValue = union.nextSetBit(0);
													xBit.set(xValue);
													differ.xor(wxyzValues);
													differ.and(yzValues);
													zValue = differ.nextSetBit(0);
													zBit.set(zValue);
												}
												if (!isWXYZWing(wzValues, xzValues, zBit, yzCell, xzCell, wzCell))//Test if doubly linked
														doubleLink = false;
												if (isWXYZWing(wzValues, xzValues, xBit, yzCell, xzCell, wzCell)) {
													if (!doubleLink) {
														// Found WXYZ-Wing pattern
														WXYZWingHint hint = createHint(
																grid, wxyzCell, Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																wzValues, xzValues, yzValues, wxyzValues, xValue, zValue, xBit, zBit, biggestCardinality3, wingSize, doubleLink, w1Value, w2Value, w1Bit, w2Bit, remCand, inter);
														if (hint.isWorth())
															accu.add(hint);
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
															accu.add(hint);
													}
												} // if isWXYZWing(wxyzValues, wzValues, xzValues, yzValues)
												else
													if (doubleLink) {
														doubleLink = false;
														WXYZWingHint hint = createHint(
																grid, wxyzCell, Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																wzValues, xzValues, yzValues, wxyzValues, zValue, xValue, zBit, xBit, biggestCardinality3, wingSize, doubleLink, w1Value, w2Value, w1Bit, w2Bit, remCand, inter);
														if (hint.isWorth())
															accu.add(hint);
													}//swapping the z with x as both linked to Pilot but x not linked  to x containing cells while z is liked to z containing cells
											} // if yzValues.cardinality() == 2
										} // if yzCellIndex.getX() != xzCellIndex.getX() && yzCellIndex.getY() != xzCellIndex.getY()
									} // for Cell yzCellIndex : wxyzCell.getVisibleCells()
								} // if xzValues.cardinality() == 2
							//} // if xzCellIndex.getX() != wzCellIndex.getX() && xzCellIndex.getY() != wzCellIndex.getY()
						} // for Cell xzCellIndex : wxyzCell.getVisibleCells()
					} // if wzValues.cardinality() == 2
				} // for Cell wzCellIndex : wxyzCell.getVisibleCells()
			} // if wxyzValues.cardinality() == targetCardinality
        } // for i
    }

    private WXYZWingHint createHint(
            Grid grid, Cell wxyzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet wzValues, BitSet xzValues, BitSet yzValues, BitSet wxyzValues, int xValue, int zValue, BitSet xBit, BitSet zBit, int biggestCardinality, int wingSize, boolean doubleLink, int w1Value, int w2Value, BitSet w1Bit, BitSet w2Bit, BitSet remCand, BitSet wingSet) {
        // Build list of removable potentials
		boolean weakPotentials = false;
		boolean strongPotentialsX = false;//if both remain false then proceed as normal wxyz if weak is false strong is true and z is false then swap z to x
		boolean strongPotentialsZ = false;//if both remain false then proceed as normal wxyz if weak is false strong is true and z is false then swap z to x        
		BitSet inter = (BitSet)zBit.clone();
		Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
		//Set<Cell> victims = new LinkedHashSet<>(wzCell.getHouseCells(grid));
		CellSet victims = null;
		if (doubleLink) {//if no eliminations at all then produce Hint as regular WXYZ
			inter = (BitSet)w1Bit.clone();
			inter.and(xzValues);
			//JOptionPane.showMessageDialog( null, "inter Cardinality:" + inter.cardinality() + "w1Bit Cardinality:" + w1Bit.cardinality() );	
			if (inter.cardinality() == 1)
						victims = new CellSet (xzCell.getVisibleCells());
			//JOptionPane.showMessageDialog( null, "w1Value:" + w1Value + " w2Value:" + w2Value + " xzCell:" + xzCell.getIndex() + " victims:" + victims );
			inter = (BitSet)w1Bit.clone();
			inter.and(wzValues);
			//JOptionPane.showMessageDialog( null, "inter Cardinality:" + inter.cardinality() + "w1Bit Cardinality:" + w1Bit.cardinality() );
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			//JOptionPane.showMessageDialog( null, "w1Value:" + w1Value + " w2Value:" + w2Value + " wzCell:" + wzCell.getIndex() + " victims:" + victims );
			inter = (BitSet)w1Bit.clone();
			inter.and(wxyzValues);
						//JOptionPane.showMessageDialog( null, "inter Cardinality:" + inter.cardinality() + "w1Bit Cardinality:" + w1Bit.cardinality() );
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wxyzCell.getVisibleCells());
					else
						victims.retainAll(wxyzCell.getVisibleCells());
			//JOptionPane.showMessageDialog( null, "w1Value:" + w1Value + " w2Value:" + w2Value + " wxyzCell:" + wxyzCell.getIndex() + " victims:" + victims );
			victims.remove(wxyzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w1Value)) {		
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
			//JOptionPane.showMessageDialog( null, "inter Cardinality:" + inter.cardinality() + "w2Bit Cardinality:" + w2Bit.cardinality() );	
			if (inter.cardinality() == 1)
					victims = new CellSet (xzCell.getVisibleCells());
			//JOptionPane.showMessageDialog( null, "w2Value:" + w2Value + " w2Value:" + w2Value + " xzCell:" + xzCell.getIndex() + " victims:" + victims );
			inter = (BitSet)w2Bit.clone();
			inter.and(wzValues);
			//JOptionPane.showMessageDialog( null, "inter Cardinality:" + inter.cardinality() + "w2Bit Cardinality:" + w2Bit.cardinality() );
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			//JOptionPane.showMessageDialog( null, "w2Value:" + w2Value + " w2Value:" + w2Value + " wzCell:" + wzCell.getIndex() + " victims:" + victims );
			inter = (BitSet)w2Bit.clone();
			inter.and(wxyzValues);
						//JOptionPane.showMessageDialog( null, "inter Cardinality:" + inter.cardinality() + "w2Bit Cardinality:" + w2Bit.cardinality() );
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wxyzCell.getVisibleCells());
					else
						victims.retainAll(wxyzCell.getVisibleCells());
			//JOptionPane.showMessageDialog( null, "w2Value:" + w2Value + " w2Value:" + w2Value + " wxyzCell:" + wxyzCell.getIndex() + " victims:" + victims );
			victims.remove(wxyzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w2Value)) {
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
						wxyzCell, wzCell, xzCell, yzCell, xValue, zValue, biggestCardinality, wingSize, doubleLink, wingSet);					
				}
				else
					if (!strongPotentialsX)
						doubleLink = false;
        // Create hint
        return new WXYZWingHint(this, removablePotentials,
                wxyzCell, wzCell, xzCell, yzCell, zValue, xValue, biggestCardinality, wingSize, doubleLink, wingSet);
    }

    @Override
    public String toString() {
        return "WXYZ-Wings";
    }
}
