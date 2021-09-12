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
 * Implementation of the "Ferz NC Forcing Cell" solving technique by Tarek Maani.
 * This covers double consecutive claiming, double Middle claiming and triple consecutive claiming
 * http://jcbonsai.free.fr/sudoku/JSudokuUserGuide/relationalTechniques.html
 * http://forum.enjoysudoku.com/sudokuncexplainer-to-solve-and-rate-sudoku-non-consecutive-t36949.html#p285476
 */
public class lockedFNC implements IndirectHintProducer {
	
	public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        boolean isNCNonToroidal = Settings.getInstance().whichNC() == 3;
        for (int value = 1; value <= 9; value++) {
			for (int regionTypeIndex = 4; regionTypeIndex >= (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex--) {
				//DG doesn't have cells in proximity
				if (regionTypeIndex == 3) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				int regionsNumber = Grid.getRegions(regionTypeIndex).length;
				if (regionTypeIndex == 4) {
					regionsNumber = Math.min(regionsNumber, 4);
				}
				// Boxes and windows can have up to 5 diagonally-adjacent cells (cross sign).
				int maxCardinality = (regionTypeIndex == 0 || regionTypeIndex == 4) ? 5 : 3;
				for (int regionIndex = 0; regionIndex < regionsNumber; regionIndex++) {
					Grid.Region region = Grid.getRegions(regionTypeIndex)[regionIndex];
					BitSet ncValues = region.getPotentialPositions(grid, value);
					int ncValuesCard = ncValues.cardinality();
					if (ncValuesCard <= maxCardinality) {
						Cell[] cells = new Cell[ncValuesCard];
						int cellIndex = 0;
						for (int ncIndex = 0; ncIndex < 9; ncIndex++) {
							if (ncValues.get(ncIndex))
								cells[cellIndex++] = region.getCell(ncIndex);
						}
						int value1 = isNCNonToroidal ? value - 1 : value > 1 ? value - 1 : 9;
						int value2 = isNCNonToroidal ? (value + 1) % 10 : value < 9 ? value + 1 : 1;
						lockedFNCHint hint = createHint(grid, cells, value1, value2, region, value);
						if (hint != null && hint.isWorth())
							accu.add(hint);
					}
				} // for (int i = 0; i < regionsNumber; i++)
			} // for (int regionTypeIndex = 2; regionTypeIndex >=0; regionTypeIndex--)
		}// for (int value = 1; value <= 9; value++)
	}

    // Create a hint for NC locked pairs
	private lockedFNCHint createHint(Grid grid, Cell[] cells, int value1, int value2, Grid.Region region, int value) {
        Map<Cell,BitSet> removablePotentials = null;
		if (cells.length > 0) {
			int[][] cellsLookup = Settings.getInstance().isToroidal() ? Grid.ferzCellsToroidal : Grid.ferzCellsRegular;
			CellSet victims = new CellSet(cellsLookup[cells[0].getIndex()]);
			victims.add(cells[0]);
			for (int i = 1; i < cells.length; i++) {
				CellSet curVictims = new CellSet(cellsLookup[cells[i].getIndex()]);
				curVictims.add(cells[i]);
				victims.retainAll(curVictims);
				if (victims.isEmpty())
					return null;
			}

			removablePotentials = new HashMap<Cell,BitSet>();
			for (Cell cell : victims) {
				if (value1 != 0 && grid.hasCellPotentialValue(cell.getIndex(), value1))
					removablePotentials.put(cell, SingletonBitSet.create(value1));
				if (value2 != 0 && grid.hasCellPotentialValue(cell.getIndex(), value2))
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(value2);
					else
						removablePotentials.put(cell, SingletonBitSet.create(value2));		
			}
		}
		if (removablePotentials == null || removablePotentials.isEmpty())
			return null;

		int[] finalValues;
		if (value1 == 0)
			finalValues = new int[] {value2};
		else if (value2 == 0)
			finalValues = new int[] {value1};
		else
			finalValues = new int[] {value1, value2};
		return new lockedFNCHint(this, removablePotentials, cells, finalValues, region, value);
	}


    @Override
    public String toString() {
        return "Locked NC";
    }

}
