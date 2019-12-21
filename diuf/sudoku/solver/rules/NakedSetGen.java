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
 * Implementation of the generalized naked sets solving techniques
 * subsume vanilla naked sets solving techniques 
 * subsume pointing pairs, triplet  
 * (Naked Pair, Naked Triplet, Naked Quad).
 */
public class NakedSetGen implements IndirectHintProducer {

    private int degree;

    public NakedSetGen(int degree) {
        assert degree > 1 && degree <= 5;
        this.degree = degree;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        if (Settings.getInstance().isBlocks())
			getHints(grid, 0, accu); //block
        getHints(grid, 2, accu); //column
        getHints(grid, 1, accu); //row
		if (!Settings.getInstance().isVLatin()) {
			if (Settings.getInstance().isDG())
				getHints(grid, 3, accu); //DG
			if (Settings.getInstance().isWindows())
				getHints(grid, 4, accu); //Windows
			if (Settings.getInstance().isX()) {
				getHints(grid, 5, accu); //Main diagonal
				getHints(grid, 6, accu); //Anti diagonal
			}
			if (Settings.getInstance().isGirandola())
				getHints(grid, 7, accu); //Girandola			
			if (Settings.getInstance().isAsterisk())
				getHints(grid, 8, accu); //Asterisk			
			if (Settings.getInstance().isCD())
				getHints(grid, 9, accu); //CD	
		}
    }

    /**
     * For each regions of the given type, check if a n-tuple of values have
     * a common n-tuple of potential positions, and no other potential position.
     */
    private <T extends Grid.Region> void getHints(Grid grid, int regionTypeIndex, HintsAccumulator accu) throws InterruptedException {
        Grid.Region[] regions = Grid.getRegions(regionTypeIndex);
        // Iterate on parts
        for (Grid.Region region : regions) {
            if (region.getEmptyCellCount(grid) >= degree * 2) {
                Permutations perm = new Permutations(degree, 9);
                // Iterate on tuples of positions
                while (perm.hasNext()) {
                    int[] indexes = perm.nextBitNums();
                    assert indexes.length == degree;

                    // Build the cell tuple
                    Cell[] cells = new Cell[degree];
                    for (int i = 0; i < cells.length; i++)
                        cells[i] = region.getCell(indexes[i]);

                    // Build potential values for each position of the tuple
                    BitSet[] potentialValues = new BitSet[degree];
                    for (int i = 0; i < degree; i++)
                        potentialValues[i] = grid.getCellPotentialValues(cells[i].getIndex());

                    // Look for a common tuple of potential values, with same degree
                    BitSet commonPotentialValues = 
                        CommonTuples.searchCommonTuple(potentialValues, degree);
                    if (commonPotentialValues != null) {
                        // Potential hint found
                        IndirectHint hint = createValueUniquenessGenHint(grid, region, cells, commonPotentialValues);
                        if (hint.isWorth())
                            accu.add(hint);
                    }
                }
            }
        }
    }

    private IndirectHint createValueUniquenessGenHint(Grid grid, Grid.Region region, Cell[] cells, BitSet commonPotentialValues) {
    	// Build value list
        int[] values = new int[degree];
        int dstIndex = 0;
        for (int value = 1; value <= 9; value++) {
            if (commonPotentialValues.get(value))
                values[dstIndex++] = value;
        }
        // Build concerned cell potentials
        Map<Cell,BitSet> cellPValues = new LinkedHashMap<Cell,BitSet>();
        for (Cell cell : cells) {
            BitSet potentials = new BitSet(10);
            potentials.or(commonPotentialValues);
            potentials.and(grid.getCellPotentialValues(cell.getIndex()));
            cellPValues.put(cell, potentials);
        }
        // Build removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
        //SudokuMonster: eliminationsTotal can be enabled to sort hints
		//int eliminationsTotal = 0;
		for(int i = commonPotentialValues.nextSetBit(0); i >= 0; i = commonPotentialValues.nextSetBit(i + 1)) {
			CellSet Victims = null;
			for (Cell cell : cells)
				if (grid.hasCellPotentialValue(cell.getIndex(),i))
					if (Victims == null)
						Victims = new CellSet (cell.getVisibleCells());
					else
						Victims.retainAll(cell.getVisibleCells());
			for (Cell cell : Victims)
				if (grid.hasCellPotentialValue(cell.getIndex(), i)) {		
					//eliminationsTotal++;
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(i);
					else
						removablePotentials.put(cell, SingletonBitSet.create(i));
				}
		}
        return new NakedSetGenHint(this, cells, values, cellPValues, removablePotentials, region);
    }

    @Override
    public String toString() {
        final String[] groupNames = new String[] {"Pair", "Triplet", "Quad", "Quintuplet", "Sextuplet"};
        if (degree < 7)
			return "Generalized Naked " + groupNames[degree - 2];
        return "Generalized Naked Sets " + degree;
    }

}
