/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.checks;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;

/**
 * Class that check that no value appear more than once
 * in the same row, column or block.
 * If this occur, a corresponding warning hint is produced.
 */
public class NoDoubles implements WarningHintProducer {

    public void getHints(Grid grid, HintsAccumulator accu)
            throws InterruptedException {
        // Iterate on region types
        //for (Class<? extends Grid.Region> regionType : Grid.getRegionTypes()) {
        //    Grid.Region[] regions = grid.getRegions(regionType);
		for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
        	if (!Settings.getInstance().isVLatin()) {
				if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
				if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
				if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
			}
            Grid.Region[] regions = Grid.getRegions(regionTypeIndex);

            // Iterate on occurances of a region
            for (Grid.Region region : regions) {
                BitSet values = new BitSet(10);

                // Iterate on cells of a region
                for (int j = 0; j < 9; j++) {
                    final Cell cell = region.getCell(j);
                    //int value = cell.getValue();
                    int value = grid.getCellValue(cell.getX(), cell.getY());
                    if (value != 0) {
                        if (values.get(value)) {
                            // Value appear twice in this region
                            WarningMessage message = new WarningMessage(this,
                                    "More than one \"" + value + "\" in a " + region.toString(),
                                    "DoubleValue.html", Integer.toString(value), region.toString(), Settings.getInstance().variantString + (Settings.getInstance().isBlocks() ? " Sudoku" : "")) {

                                @Override
                                public Collection<Cell> getRedCells() {
                                    Collection<Cell> result = new ArrayList<Cell>();
                                    result.add(cell);
                                    for (int i = 0; i < 9; i++) {
                                        Cell other = region.getCell(i);
                                        //if (!other.equals(cell) && cell.getValue() == other.getValue())
                                        if (!other.equals(cell) && grid.getCellValue(cell.getX(), cell.getY()) == grid.getCellValue(other.getX(), other.getY()))
                                            result.add(other);
                                    }
                                    return result;
                                }

                                @Override
                                public Region[] getRegions() {
                                    return new Region[] {region};
                                }
                            };
                            accu.add(message);
                        } else
                            values.set(value);
                    }
                }
            }
        }
    }

    public boolean isValid(Grid grid) {
        // Iterate on region types
        //for (Class<? extends Grid.Region> regionType : Grid.getRegionTypes()) {
        //    Grid.Region[] regions = grid.getRegions(regionType);
		for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
        	if (!Settings.getInstance().isVLatin()) {
				if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
				if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
				if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
			}
            Grid.Region[] regions = Grid.getRegions(regionTypeIndex);

            // Iterate on occurances of a region
            for (Grid.Region region : regions) {
                BitSet values = new BitSet(10);

                // Iterate on cells of a region
                for (int j = 0; j < 9; j++) {
                    final Cell cell = region.getCell(j);
                    //int value = cell.getValue();
                    int value = grid.getCellValue(cell.getX(), cell.getY());
                    if (value != 0) {
                        if (values.get(value)) {
                            return false;
                        } else
                            values.set(value);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Invalid " + Settings.getInstance().variantString + (Settings.getInstance().isBlocks() ? " Sudoku" : "");
    }

}
