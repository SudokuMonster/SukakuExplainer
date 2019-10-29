/**
 * @author Mladen Dobrichev, 2019
 *
 */
package diuf.sudoku.tools;

import java.util.BitSet;

import diuf.sudoku.Grid;

public class DigitCells {
	public BitSet[] digitCells;
	public DigitCells(Grid g) {
		digitCells = new BitSet[9];
		for(int d = 0; d < 9; d++) {
			digitCells[d] = new BitSet(81);
			for(int c = 0; c < 81; c++) {
				digitCells[d].set(c, g.getCellPotentialValues(c).get(d + 1)); //transpose g
			}
		}
	}
	public DigitCells(DigitCells old) {
		digitCells = new BitSet[9];
		for(int d = 0; d < 9; d++) {
			digitCells[d] = (BitSet)(old.digitCells[d].clone());
		}
	}
}
