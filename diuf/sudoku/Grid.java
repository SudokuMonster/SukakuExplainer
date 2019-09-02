/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku;

import java.util.*;

import diuf.sudoku.tools.CellSet;

/**
 * A Sudoku grid.
 * <p>
 * Contains the 9x9 array of cells, as well as methods
 * to manipulate regions (rows, columns and blocks).
 * <p>
 * Horizontal coordinates (for Cells) range from 0 (leftmost) to
 * 8 (rightmost). Vertical coordinates range from 0 (topmost) to
 * 8 (bottommost).
 */
public class Grid {

    /*
     * Cell values of the grid [0 .. 9].
     */
    private int[] cellValues = new int[81];

    /*
     * Cell values of the grid [0 .. 9].
     */
    private BitSet[] cellPotentialValues = new BitSet[81];

//    //cache for Region.getPotentialPositions(value)
//    private valueCells valueCellsCache = new valueCells();
//    private class valueCells {
//        private BitSet[][][] valuePotentialCells = new BitSet[3][9][9]; //region type, region, value
//
//        public void invalidateCellValue(int cellIndex, int value) {
//        	for(int t = 0; t < 3; t++) { //region types
//        		valuePotentialCells[t][cellRegions[cellIndex][t]][value - 1] = null;
//        	}
//        }
//        public void invalidateCell(int cellIndex) {
//        	for(int t = 0; t < 3; t++) { //region types
//        		for(int v = 0; v < 9; v++) { //values
//        			valuePotentialCells[t][cellRegions[cellIndex][t]][v] = null;
//        		}
//        	}
//        }
//        public BitSet getRegionValueCells(Region region, int value) {
//        	int regionType = region.getRegionTypeIndex();
//        	int regionIndex = region.getRegionIndex();
//        	BitSet result = valuePotentialCells[regionType][regionIndex][value - 1];
//        	if(result == null) { //build
//        		result = new BitSet(9);
//                for (int index = 0; index < 9; index++) {
//                    result.set(index, hasCellPotentialValue(region.getCell(index).getIndex(), value));
//                }
//                valuePotentialCells[regionType][regionIndex][value - 1] = result; //store to cache
//        	}
//        	return result;
//        }
//    }
     
    /*
     * Cells of the grid. First array index is the vertical index (from top
     * to bottom), and second index is horizontal index (from left to right).
     */
    //private Cell[][] cells = new Cell[9][9];
    //private Cell[] cells = new Cell[81];
    private static final Cell cells[] = {
    		new Cell(0), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8),
    		new Cell(9), new Cell(10), new Cell(11), new Cell(12), new Cell(13), new Cell(14), new Cell(15), new Cell(16), new Cell(17),
    		new Cell(18), new Cell(19), new Cell(20), new Cell(21), new Cell(22), new Cell(23), new Cell(24), new Cell(25), new Cell(26),
    		new Cell(27), new Cell(28), new Cell(29), new Cell(30), new Cell(31), new Cell(32), new Cell(33), new Cell(34), new Cell(35),
    		new Cell(36), new Cell(37), new Cell(38), new Cell(39), new Cell(40), new Cell(41), new Cell(42), new Cell(43), new Cell(44),
    		new Cell(45), new Cell(46), new Cell(47), new Cell(48), new Cell(49), new Cell(50), new Cell(51), new Cell(52), new Cell(53),
    		new Cell(54), new Cell(55), new Cell(56), new Cell(57), new Cell(58), new Cell(59), new Cell(60), new Cell(61), new Cell(62),
    		new Cell(63), new Cell(64), new Cell(65), new Cell(66), new Cell(67), new Cell(68), new Cell(69), new Cell(70), new Cell(71),
    		new Cell(72), new Cell(73), new Cell(74), new Cell(75), new Cell(76), new Cell(77), new Cell(78), new Cell(79), new Cell(80)
    		};

    public static final int[][] regionCellIndex = new int[81][3]; //[cell][getRegionTypeIndex()]
    public static final int[][] cellRegions = new int[81][3]; //[cell][getRegionTypeIndex()]

    public static final int[][] visibleCellIndex = {
    		{ 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,18,19,20,27,36,45,54,63,72},
    		{ 0, 2, 3, 4, 5, 6, 7, 8, 9,10,11,18,19,20,28,37,46,55,64,73},
    		{ 0, 1, 3, 4, 5, 6, 7, 8, 9,10,11,18,19,20,29,38,47,56,65,74},
    		{ 0, 1, 2, 4, 5, 6, 7, 8,12,13,14,21,22,23,30,39,48,57,66,75},
    		{ 0, 1, 2, 3, 5, 6, 7, 8,12,13,14,21,22,23,31,40,49,58,67,76},
    		{ 0, 1, 2, 3, 4, 6, 7, 8,12,13,14,21,22,23,32,41,50,59,68,77},
    		{ 0, 1, 2, 3, 4, 5, 7, 8,15,16,17,24,25,26,33,42,51,60,69,78},
    		{ 0, 1, 2, 3, 4, 5, 6, 8,15,16,17,24,25,26,34,43,52,61,70,79},
    		{ 0, 1, 2, 3, 4, 5, 6, 7,15,16,17,24,25,26,35,44,53,62,71,80},
    		{ 0, 1, 2,10,11,12,13,14,15,16,17,18,19,20,27,36,45,54,63,72},
    		{ 0, 1, 2, 9,11,12,13,14,15,16,17,18,19,20,28,37,46,55,64,73},
    		{ 0, 1, 2, 9,10,12,13,14,15,16,17,18,19,20,29,38,47,56,65,74},
    		{ 3, 4, 5, 9,10,11,13,14,15,16,17,21,22,23,30,39,48,57,66,75},
    		{ 3, 4, 5, 9,10,11,12,14,15,16,17,21,22,23,31,40,49,58,67,76},
    		{ 3, 4, 5, 9,10,11,12,13,15,16,17,21,22,23,32,41,50,59,68,77},
    		{ 6, 7, 8, 9,10,11,12,13,14,16,17,24,25,26,33,42,51,60,69,78},
    		{ 6, 7, 8, 9,10,11,12,13,14,15,17,24,25,26,34,43,52,61,70,79},
    		{ 6, 7, 8, 9,10,11,12,13,14,15,16,24,25,26,35,44,53,62,71,80},
    		{ 0, 1, 2, 9,10,11,19,20,21,22,23,24,25,26,27,36,45,54,63,72},
    		{ 0, 1, 2, 9,10,11,18,20,21,22,23,24,25,26,28,37,46,55,64,73},
    		{ 0, 1, 2, 9,10,11,18,19,21,22,23,24,25,26,29,38,47,56,65,74},
    		{ 3, 4, 5,12,13,14,18,19,20,22,23,24,25,26,30,39,48,57,66,75},
    		{ 3, 4, 5,12,13,14,18,19,20,21,23,24,25,26,31,40,49,58,67,76},
    		{ 3, 4, 5,12,13,14,18,19,20,21,22,24,25,26,32,41,50,59,68,77},
    		{ 6, 7, 8,15,16,17,18,19,20,21,22,23,25,26,33,42,51,60,69,78},
    		{ 6, 7, 8,15,16,17,18,19,20,21,22,23,24,26,34,43,52,61,70,79},
    		{ 6, 7, 8,15,16,17,18,19,20,21,22,23,24,25,35,44,53,62,71,80},
    		{ 0, 9,18,28,29,30,31,32,33,34,35,36,37,38,45,46,47,54,63,72},
    		{ 1,10,19,27,29,30,31,32,33,34,35,36,37,38,45,46,47,55,64,73},
    		{ 2,11,20,27,28,30,31,32,33,34,35,36,37,38,45,46,47,56,65,74},
    		{ 3,12,21,27,28,29,31,32,33,34,35,39,40,41,48,49,50,57,66,75},
    		{ 4,13,22,27,28,29,30,32,33,34,35,39,40,41,48,49,50,58,67,76},
    		{ 5,14,23,27,28,29,30,31,33,34,35,39,40,41,48,49,50,59,68,77},
    		{ 6,15,24,27,28,29,30,31,32,34,35,42,43,44,51,52,53,60,69,78},
    		{ 7,16,25,27,28,29,30,31,32,33,35,42,43,44,51,52,53,61,70,79},
    		{ 8,17,26,27,28,29,30,31,32,33,34,42,43,44,51,52,53,62,71,80},
    		{ 0, 9,18,27,28,29,37,38,39,40,41,42,43,44,45,46,47,54,63,72},
    		{ 1,10,19,27,28,29,36,38,39,40,41,42,43,44,45,46,47,55,64,73},
    		{ 2,11,20,27,28,29,36,37,39,40,41,42,43,44,45,46,47,56,65,74},
    		{ 3,12,21,30,31,32,36,37,38,40,41,42,43,44,48,49,50,57,66,75},
    		{ 4,13,22,30,31,32,36,37,38,39,41,42,43,44,48,49,50,58,67,76},
    		{ 5,14,23,30,31,32,36,37,38,39,40,42,43,44,48,49,50,59,68,77},
    		{ 6,15,24,33,34,35,36,37,38,39,40,41,43,44,51,52,53,60,69,78},
    		{ 7,16,25,33,34,35,36,37,38,39,40,41,42,44,51,52,53,61,70,79},
    		{ 8,17,26,33,34,35,36,37,38,39,40,41,42,43,51,52,53,62,71,80},
    		{ 0, 9,18,27,28,29,36,37,38,46,47,48,49,50,51,52,53,54,63,72},
    		{ 1,10,19,27,28,29,36,37,38,45,47,48,49,50,51,52,53,55,64,73},
    		{ 2,11,20,27,28,29,36,37,38,45,46,48,49,50,51,52,53,56,65,74},
    		{ 3,12,21,30,31,32,39,40,41,45,46,47,49,50,51,52,53,57,66,75},
    		{ 4,13,22,30,31,32,39,40,41,45,46,47,48,50,51,52,53,58,67,76},
    		{ 5,14,23,30,31,32,39,40,41,45,46,47,48,49,51,52,53,59,68,77},
    		{ 6,15,24,33,34,35,42,43,44,45,46,47,48,49,50,52,53,60,69,78},
    		{ 7,16,25,33,34,35,42,43,44,45,46,47,48,49,50,51,53,61,70,79},
    		{ 8,17,26,33,34,35,42,43,44,45,46,47,48,49,50,51,52,62,71,80},
    		{ 0, 9,18,27,36,45,55,56,57,58,59,60,61,62,63,64,65,72,73,74},
    		{ 1,10,19,28,37,46,54,56,57,58,59,60,61,62,63,64,65,72,73,74},
    		{ 2,11,20,29,38,47,54,55,57,58,59,60,61,62,63,64,65,72,73,74},
    		{ 3,12,21,30,39,48,54,55,56,58,59,60,61,62,66,67,68,75,76,77},
    		{ 4,13,22,31,40,49,54,55,56,57,59,60,61,62,66,67,68,75,76,77},
    		{ 5,14,23,32,41,50,54,55,56,57,58,60,61,62,66,67,68,75,76,77},
    		{ 6,15,24,33,42,51,54,55,56,57,58,59,61,62,69,70,71,78,79,80},
    		{ 7,16,25,34,43,52,54,55,56,57,58,59,60,62,69,70,71,78,79,80},
    		{ 8,17,26,35,44,53,54,55,56,57,58,59,60,61,69,70,71,78,79,80},
    		{ 0, 9,18,27,36,45,54,55,56,64,65,66,67,68,69,70,71,72,73,74},
    		{ 1,10,19,28,37,46,54,55,56,63,65,66,67,68,69,70,71,72,73,74},
    		{ 2,11,20,29,38,47,54,55,56,63,64,66,67,68,69,70,71,72,73,74},
    		{ 3,12,21,30,39,48,57,58,59,63,64,65,67,68,69,70,71,75,76,77},
    		{ 4,13,22,31,40,49,57,58,59,63,64,65,66,68,69,70,71,75,76,77},
    		{ 5,14,23,32,41,50,57,58,59,63,64,65,66,67,69,70,71,75,76,77},
    		{ 6,15,24,33,42,51,60,61,62,63,64,65,66,67,68,70,71,78,79,80},
    		{ 7,16,25,34,43,52,60,61,62,63,64,65,66,67,68,69,71,78,79,80},
    		{ 8,17,26,35,44,53,60,61,62,63,64,65,66,67,68,69,70,78,79,80},
    		{ 0, 9,18,27,36,45,54,55,56,63,64,65,73,74,75,76,77,78,79,80},
    		{ 1,10,19,28,37,46,54,55,56,63,64,65,72,74,75,76,77,78,79,80},
    		{ 2,11,20,29,38,47,54,55,56,63,64,65,72,73,75,76,77,78,79,80},
    		{ 3,12,21,30,39,48,57,58,59,66,67,68,72,73,74,76,77,78,79,80},
    		{ 4,13,22,31,40,49,57,58,59,66,67,68,72,73,74,75,77,78,79,80},
    		{ 5,14,23,32,41,50,57,58,59,66,67,68,72,73,74,75,76,78,79,80},
    		{ 6,15,24,33,42,51,60,61,62,69,70,71,72,73,74,75,76,77,79,80},
    		{ 7,16,25,34,43,52,60,61,62,69,70,71,72,73,74,75,76,77,78,80},
    		{ 8,17,26,35,44,53,60,61,62,69,70,71,72,73,74,75,76,77,78,79}
			};

    // Views
    private static final Block[] blocks = {new Block(0), new Block(1), new Block(2), new Block(3), new Block(4), new Block(5), new Block(6), new Block(7), new Block(8)};
    private static final Row[] rows = {new Row(0), new Row(1), new Row(2), new Row(3), new Row(4), new Row(5), new Row(6), new Row(7), new Row(8)};
    private static final Column[] columns = {new Column(0), new Column(1), new Column(2), new Column(3), new Column(4), new Column(5), new Column(6), new Column(7), new Column(8)};
    public static final Region[][] regions = {blocks, rows, columns}; 

    //private static final Class<? extends Grid.Region>[] regionTypes = (Class<? extends Grid.Region>[]) new Class[] {Grid.Block.class, Grid.Row.class, Grid.Column.class};
    
    //temporary development/debug counters
    //public static long numCellPencilmarksUpdate = 0;
    //public static long numCellPencilmarksRead = 0;
    //public static long numGetPP = 0;
    
    /**
     * Create a new 9x9 Sudoku grid. All cells are set to empty
     */
    public Grid() {
        for (int i = 0; i < 81; i++) {
        	cellPotentialValues[i] = new BitSet(10);
        }
    }

    /**
     * Get the cell at the given coordinates
     * @param x the x coordinate (0=leftmost, 8=rightmost)
     * @param y the y coordinate (0=topmost, 8=bottommost)
     * @return the cell at the given coordinates
     */
    public static Cell getCell(int x, int y) {
        return cells[9 * y + x];
    }

    /**
     * Get the cell at the given index 8 .. 80
     */
    public static Cell getCell(int index) {
        return cells[index];
    }

//    /**
//     * Get the 9 regions of the given type
//     * @param regionType the type of the regions to return. Must be one of
//     * {@link Grid.Block}, {@link Grid.Row} or {@link Grid.Column}.
//     * @return the 9 regions of the given type
//     */
//    public Region[] getRegions(Class<? extends Region> regionType) {
//        if (regionType == Row.class)
//            return Grid.rows;
//        else if (regionType == Column.class)
//            return Grid.columns;
//        else
//            return Grid.blocks;
//    }

    /**
     * Get the 9 regions of the given type
     * @param regionTypeIndex the type of the regions to return. Must be 0 for
     * {@link Grid.Block}, 1 for {@link Grid.Row}, or 2 for {@link Grid.Column}.
     * @return the 9 regions of the given type
     */
    public Region[] getRegions(int regionTypeIndex) {
    	return regions[regionTypeIndex];
    }

    /**
     * Set the value of a cell
     * @param x the horizontal coordinate of the cell
     * @param y the vertical coordinate of the cell
     * @param value the value to set the cell to. Use 0 to clear the cell.
     */
    public void setCellValue(int x, int y, int value) {
    	cellValues[y * 9 + x] = value;
    }

    /**
     * Set the value of a cell
     * @param index the cell index [0..80]
     * @param value the value to set the cell to. Use 0 to clear the cell.
     */
    public void setCellValue(int index, int value) {
        cellValues[index] = value;
    }

    /**
     * Get the value of a cell
     * @param x the horizontal coordinate of the cell
     * @param y the vertical coordinate of the cell
     * @return the value of the cell, or 0 if the cell is empty
     */
    public int getCellValue(int x, int y) {
        return cellValues[9 * y + x];
    }
    
    /**
     * Get the value of a cell
     * @param index the cell index [0 .. 80]
     * @return the value of the cell, or 0 if the cell is empty
     */
    public int getCellValue(int index) {
        return cellValues[index];
    }

    /**
     * Get the potential values for the given cell index.
     * <p>
     * The result is returned as a bitset. Each of the
     * bit number 1 to 9 is set if the corresponding
     * value is a potential value for this cell. Bit number
     * <tt>0</tt> is not used and ignored.
     * @param cellIndex the cell index 0 to 80
     * @return the potential values for unresolved cell, empty for resolved
     */
    public BitSet getCellPotentialValues(int cellIndex) {
        //return cells[cellIndex / 9][cellIndex % 9].getPotentialValues();
        //numCellPencilmarksRead++;
        return cellPotentialValues[cellIndex];
    }

    /**
     * Test whether the given value is a potential
     * value for the given cell.
     * @param cellIndex the cell to test
     * @param value the potential value to test, between 1 and 9, inclusive
     * @return whether the given value is a potential value for this cell
     */
    public boolean hasCellPotentialValue(int cellIndex, int value) {
        //return cell.hasPotentialValue(value);
        //numCellPencilmarksRead++;
    	return cellPotentialValues[cellIndex].get(value);
    }

    /**
     * Add the given value as a potential value for the given cell
     * @param cell the cell
     * @param value the value to add, between 1 and 9, inclusive
     */
    public void addCellPotentialValue(Cell cell, int value) {
//        if(cellPotentialValues[cell.getIndex()].get(value)) return; //no change (doesn't improve, 32382541 -> 32382541)
        cellPotentialValues[cell.getIndex()].set(value);
        //valueCellsCache.invalidateCellValue(cell.getIndex(), value);
        //numCellPencilmarksUpdate++;
    }

    /**
     * Remove the given value from the potential values of the given cell.
     * @param cell the cell
     * @param value the value to remove, between 1 and 9, inclusive
     */
    public void removeCellPotentialValue(Cell cell, int value) {
        //if(!cellPotentialValues[cell.getIndex()].get(value)) return; //no change (doesn't improve, 32382541 -> 32380479)
        cellPotentialValues[cell.getIndex()].clear(value);
        //valueCellsCache.invalidateCellValue(cell.getIndex(), value);
        //numCellPencilmarksUpdate++;
    }

    /**
     * Remove the given value from the potential values of the given cell index.
     * @param cellIndex the cell index 0 .. 80
     * @param value the value to remove, between 1 and 9, inclusive
     */
    public void removeCellPotentialValue(int cellIndex, int value) {
        //if(!cellPotentialValues[cell.getIndex()].get(value)) return; //no change (doesn't improve, 32382541 -> 32380479)
        cellPotentialValues[cellIndex].clear(value);
        //valueCellsCache.invalidateCellValue(cell.getIndex(), value);
        //numCellPencilmarksUpdate++;
    }

    /**
     * Removes at once several potential values of the given cell.
     * @param cell the cell
     * @param valuesToRemove bitset with values to remove
     */
    public void removeCellPotentialValues(Cell cell, BitSet valuesToRemove) {
    	//BitSet cl = new BitSet();
    	//cl.or(cellPotentialValues[cell.getIndex()]);
    	//cl.and(valuesToRemove);
    	//if(cl.isEmpty()) return; //no change (doesn't improve, 32380479 -> 32380479)
        cellPotentialValues[cell.getIndex()].andNot(valuesToRemove);
        //valueCellsCache.invalidateCell(cell.getIndex());
        //numCellPencilmarksUpdate++;
    }

    /**
     * Clears the potential values of the given cell.
     * @param cell the cell
     */
    public void clearCellPotentialValues(Cell cell) {
        //if(cellPotentialValues[cell.getIndex()].isEmpty()) return; //no change (doesn't improve, 32380479 -> 32380479)
        cellPotentialValues[cell.getIndex()].clear();
        //valueCellsCache.invalidateCell(cell.getIndex());
        //numCellPencilmarksUpdate++;
    }

    /**
     * Set the value of a cell
     * @param index the cell index [0..80]
     * @param value the value to set the cell to. Use 0 to clear the cell.
     */
    public void setCellPotentialValues(int index, BitSet values) {
        //cellPotentialValues[index] = (BitSet)values.clone();
    	cellPotentialValues[index].clear();
    	cellPotentialValues[index].or(values);
        //valueCellsCache.invalidateCell(index);
        //numCellPencilmarksUpdate++;
    }

    /**
     * Get the row at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the row at the given coordinates
     */
    public Row getRowAt(int x, int y) {
        return Grid.rows[y];
    }

    /**
     * Get the column at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the column at the given location
     */
    public Column getColumnAt(int x, int y) {
        return Grid.columns[x];
    }

    /**
     * Get the 3x3 block at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the block at the given coordinates (the coordinates
     * are coordinates of a cell)
     */
    public static Block getBlockAt(int x, int y) {
        return Grid.blocks[(y / 3) * 3 + (x / 3)];
    }

//    public Grid.Region getRegionAt(Class<? extends Grid.Region> regionType, int x, int y) {
//        if (regionType.equals(Grid.Row.class))
//            return getRowAt(x, y);
//        else if (regionType.equals(Grid.Column.class))
//            return getColumnAt(x, y);
//        else
//            return getBlockAt(x, y);
//    }

//    public Grid.Region getRegionAt(Class<? extends Grid.Region> regionType, Cell cell) {
//        return getRegionAt(regionType, cell.getX(), cell.getY());
//    }
    
    public Grid.Region getRegionAt(int regionTypeIndex, int cellIndex) {
        return Grid.regions[regionTypeIndex][Grid.cellRegions[cellIndex][regionTypeIndex]];
    }

//    /**
//     * Get a list containing the three classes corresponding to the
//     * three region types (row, column and block)
//     * @return a list of the three region types. The resulting list
//     * can not be modified
//     */
//    public static Class<? extends Grid.Region>[] getRegionTypes() {
//        return regionTypes;
//    }

    // Grid regions implementation (rows, columns, 3x3 squares)

    /**
     * Abstract class representing a region of a sudoku grid. A region
     * is either a row, a column or a 3x3 block.
     */
    public static abstract class Region {
    	protected final int[] regionCells = new int[9];
    	protected final BitSet regionCellsSet = new BitSet(81);
    	
    	public abstract int getRegionTypeIndex();
    	public abstract int getRegionIndex();

        /**
         * Get a cell of this region by index. The order in which cells are
         * returned according to the index is not defined, but is guaranted
         * to be consistant accross multiple invocations of this method.
         * @param index the index of the cell to get, between 0 (inclusive)
         * and 9 (exclusive).
         * @return the cell at the given index
         */
        public Cell getCell(int index) {
            return cells[regionCells[index]];
        }

        /**
         * Get the index of the given cell within this region.
         * <p>
         * The returned value is consistent with {@link #getCell(int)}.
         * @param cell the cell whose index to get
         * @return the index of the cell. If the cell does not belong to
         * this region the result is undetermined.
         */
        public int indexOf(Cell cell) {
        	return regionCellIndex[cell.getIndex()][getRegionTypeIndex()];
        }

        /**
         * Test whether this region contains the given value, that is,
         * is a cell of this region is filled with the given value.
         * @param grid the grid
         * @param value the value to check for
         * @return whether this region contains the given value
         */
        public boolean contains(Grid grid, int value) {
            for (int i = 0; i < 9; i++) {
            	Cell cell = getCell(i);
                if (grid.getCellValue(cell.getIndex()) == value)
                    return true;
            }
            return false;
        }

        /**
         * Test whether this region contains the given cell.
         * @param cell the cell to check
         * @return whether this region contains the given cell
         */
        public boolean contains(Cell cell) {
        	return regionCellsSet.get(cell.getIndex());
        }

        /**
         * Get the potential positions of the given value within this region.
         * The bits of the returned bitset correspond to indexes of cells, as
         * in {@link #getCell(int)}. Only the indexes of cells that have the given
         * value as a potential value are included in the bitset (see
         * {@link Grid#getCellPotentialValues(int cellIndex)}).
         * @param grid the grid
         * @param value the value whose potential positions to get
         * @return the potential positions of the given value within this region
         */
        public BitSet getPotentialPositions(Grid grid, int value) {
            BitSet result = new BitSet(9);
            for (int index = 0; index < 9; index++) {
                result.set(index, grid.hasCellPotentialValue(getCell(index).getIndex(), value));
            }
            //result.or(grid.valueCellsCache.getRegionValueCells(this, value));
            //numGetPP++;
            return result;
        }

        public BitSet copyPotentialPositions(Grid grid, int value) {
            return getPotentialPositions(grid, value); // No need to clone, this is alreay hand-made
        }

        /**
         * Get the cells of this region. The iteration order of the result
         * matches the order of the cells returned by {@link #getCell(int)}.
         * @return the cells of this region.
         */
        public CellSet getCellSet() {
            return new CellSet(regionCells);
        }

        /**
         * Test whether this region crosses an other region.
         * <p>
         * A region crosses another region if they have at least one
         * common cell. In particular, any rows cross any columns.
         * @param other the other region
         * @return whether this region crosses the other region.
         */
        public boolean crosses(Region other) { //can be implemented as static table
        	return regionCellsSet.intersects(other.regionCellsSet);
        	//BitSet intersection = (BitSet)regionCellsSet.clone();
        	//intersection.and(other.regionCellsSet);
        	//return !intersection.isEmpty();
        }

        /**
         * Get the number of cells of this region that are still empty.
         * @return the number of cells of this region that are still empty
         */
        public int getEmptyCellCount(Grid grid) {
            int result = 0;
            for (int i = 0; i < 9; i++) {
            	Cell cell = getCell(i);
                if (grid.getCellValue(cell.getX(), cell.getY()) == 0)
                    result++;
            }
            return result;
        }

        /**
         * Get a string representation of this region's type
         */
        @Override
        public abstract String toString();

        /**
         * Get a string representation of this region
         * @return a string representation of this region
         */
        public abstract String toFullString();
    }

    /**
     * A row of a sudoku grid.
     */
    public static class Row extends Region {

        private final int rowNum;

        public Row(int rowNum) {
            this.rowNum = rowNum;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * rowNum + i;
            	regionCellIndex[regionCells[i]][getRegionTypeIndex()] = i;
            	regionCellsSet.set(regionCells[i]);
            	cellRegions[regionCells[i]][1] = rowNum;
        	}
        }
        
        public int getRegionTypeIndex() {
        	return 1;
        }

        public int getRegionIndex() {
        	return rowNum;
        }
        
        public int getRowNum() {
            return this.rowNum;
        }

        @Override
        public String toString() {
            return "row";
        }

        @Override
        public String toFullString() {
            Settings settings = Settings.getInstance();
            if (settings.isRCNotation())
                return toString() + " R" + (rowNum + 1);
            else
                return toString() + " " + (rowNum + 1);
        }
    }

    /**
     * A column of a sudoku grid
     */
    public static class Column extends Region {

        private final int columnNum;

        public Column(int columnNum) {
            this.columnNum = columnNum;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * i + columnNum;
            	regionCellIndex[regionCells[i]][getRegionTypeIndex()] = i;
            	regionCellsSet.set(regionCells[i]);
            	cellRegions[regionCells[i]][2] = columnNum;
            }
        }

        public int getRegionTypeIndex() {
        	return 2;
        }
        
        public int getRegionIndex() {
        	return columnNum;
        }
        
        public int getColumnNum() {
            return this.columnNum;
        }

        @Override
        public String toString() {
            return "column";
        }

        @Override
        public String toFullString() {
            Settings settings = Settings.getInstance();
            if (settings.isRCNotation())
                return toString() + " C" + (columnNum + 1);
            else
                return toString() + " " + (char)('A' + columnNum);
        }
    }

    /**
     * A 3x3 block of a sudoku grid.
     */
    public static class Block extends Region {

        private final int vNum, hNum, index;

        public Block(int index) {
        	final int[] vNums = new int[]{0,0,0,1,1,1,2,2,2};
        	final int[] hNums = new int[]{0,1,2,0,1,2,0,1,2};
            this.vNum = vNums[index];
            this.hNum = hNums[index];
            this.index = index;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * (vNum * 3 + i / 3) + (hNum * 3 + i % 3);
            	regionCellIndex[regionCells[i]][getRegionTypeIndex()] = i;
            	regionCellsSet.set(regionCells[i]);
            	cellRegions[regionCells[i]][0] = index;
            }
        }

        public int getRegionTypeIndex() {
        	return 0;
        }
        
        public int getRegionIndex() {
        	return index;
        }
       
        public int getVIndex() {
            return this.vNum;
        }

        public int getHIndex() {
            return this.hNum;
        }

        @Override
        public String toString() {
            return "block";
        }

        @Override
        public String toFullString() {
            return toString() + " " + (vNum * 3 + hNum + 1);
        }
    }

    /**
     * Get the first cell that cancels the given cell.
     * <p>
     * More precisely, get the first cell that:
     * <ul>
     * <li>is in the same row, column or block of the given cell
     * <li>contains the given value
     * </ul>
     * The order used for the "first" is not defined, but is guaranted to be
     * consistent accross multiple invocations.
     * @param target the cell
     * @param value the value
     * @return the first cell that share a region with the given cell, and has
     * the given value
     */
    public Cell getFirstCancellerOf(Cell target, int value) {
//        for (Class<? extends Region> regionType : getRegionTypes()) {
//            Region region = getRegionAt(regionType, target.getX(), target.getY());
//            for (int i = 0; i < 9; i++) {
//                Cell cell = region.getCell(i);
//                if (!cell.equals(target) && getCellValue(target.getX(), target.getY()) == value)
//                    return cell;
//            }
//        }
        int[] visible = Grid.visibleCellIndex[target.getIndex()];
        for(int i = 0; i < 20; i++) {
        	if(cellValues[visible[i]] == value) return Grid.getCell(visible[i]);
        }
        return null;
    }

    /**
     * Copy the content of this grid to another grid.
     * The values of the cells and their potential values
     * are copied.
     * @param other the grid to copy this grid to
     */
    public void copyTo(Grid other) {
        for (int i = 0; i < 81; i++) {
            other.setCellValue(i, this.cellValues[i]);
            other.setCellPotentialValues(i, cellPotentialValues[i]);
        }
    }

    /**
     * Get the number of occurances of a given value in this grid
     * @param value the value
     * @return the number of occurances of a given value in this grid
     */
    public int getCountOccurancesOfValue(int value) {
        int result = 0;
        for (int i = 0; i < 81; i++) {
            if (getCellValue(i) == value)
                result++;
        }
        return result;
    }

    /**
     * Get a string representation of this grid. For debugging
     * purpose only.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int value = getCellValue(x, y);
                if (value == 0)
                    result.append('.');
                else
                    result.append(value);
            }
            result.append('\n');
        }
        return result.toString();
    }
    
    /**
     * Get a single-line string representation of this grid.
     */
    public String toString81() {
        StringBuilder result = new StringBuilder(88);
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int value = getCellValue(x, y);
                if (value == 0)
                    result.append('.');
                else
                    result.append(value);
            }
        }
        return result.toString();
    }

    /**
     * Get a pencilmark-string representation of this grid.
     */
    public String toStringPencilmarks() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 81; i++) {
        	int value = getCellValue(i);
        	if(value == 0) {
                BitSet values = getCellPotentialValues(i);
                for (int v = 1; v < 10; v++) {
	                if (values.get(v))
	                    result.append(v);
	                else
	                    result.append('.');
                }
        	}
        	else {
                for (int v = 1; v < 10; v++) {
	                if (v == value)
	                    result.append(v);
	                else
	                    result.append('.');
                }
            }
        }
        return result.toString();
    }
    
    /**
     * Get a multi-line pencilmark-string representation of this grid.
     */
    public String toStringMultilinePencilmarks() {
    	String res = "";
        String s = "";

        int crd = 1;
        for (int i = 0; i < 81; i++) {
            int n = getCellPotentialValues(i).cardinality();
            if ( n > crd ) { crd = n; }
        }
        if ( crd > 1 )
        {
            for (int i=0; i<3; i++ ) {
                s = "+";
                for (int j=0; j<3; j++ ) {
                    for (int k=0; k<3; k++ ) { s += "-";
                        for (int l=0; l<crd; l++ ) { s += "-";
                        }
                    }
                    s += "-+";
                }
                res += s + System.lineSeparator();
                for (int j=0; j<3; j++ ) {
                    s = "|";
                    for (int k=0; k<3; k++ ) {
                        for (int l=0; l<3; l++ ) {
                            s += " ";
                            int cnt = 0;
                            int c = ((((i*3)+j)*3)+k)*3+l;
                            Cell cell = getCell(c % 9, c / 9);
                            //int n = cell.getValue();
                            int n = getCellValue(c % 9, c / 9);
                            if ( n != 0 ) {
                                s += n;
                                cnt += 1;
                            }
                            if ( n == 0 ) {
                                for (int pv=1; pv<=9; pv++ ) {
                                    //if ( cell.hasPotentialValue( pv) ) {
                                    if ( hasCellPotentialValue(cell.getIndex(), pv) ) {
                                        s += pv;
                                        cnt += 1;
                                    }
                                }
                            }
                            for (int pad=cnt; pad<crd; pad++ ) { s += " ";
                            }
                        }
                        s += " |";
                    }
                    res += s + System.lineSeparator();
                }
            }
            s = "+";
            for (int j=0; j<3; j++ ) {
                for (int k=0; k<3; k++ ) { s += "-";
                    for (int l=0; l<crd; l++ ) { s += "-";
                    }
                }
                s += "-+";
            }
            res += s;
        }
        return res;
    }
   
    /**
     * rebuilds grid from a string of either 81 givens or 729 pencilmarks
     * @param string a string with 0 or '.' for non-givens and positional mapping to cells/pencilmarks
     */
    public void fromString(String string) {
    	int len = string.length();
    	if(len < 81) return; //ignore
    	
    	//always perform cleanup
        for (int i = 0; i < 81; i++) {
            setCellValue(i % 9, i / 9, 0);
        }
        
    	if(len < 729) { //vanilla clues
            for (int i = 0; i < 81; i++) {
                char ch = string.charAt(i);
                if (ch >= '1' && ch <= '9') {
                    int value = (ch - '0');
                    setCellValue(i % 9, i / 9, value);
                }
            }
    	}
    	else { //pencilmarks
            for (int i = 0; i < 729; i++) {
                int cl = i / 9;  // cell
                char ch = string.charAt(i);
                if (ch >= '1' && ch <= '9') {
                    int value = (ch - '0');
                    assert value == 1 + i % 9; //exact positional mapping
                    Cell cell = getCell(cl);
                    addCellPotentialValue(cell, value);
                }
            }
    	}
    }
    
    /**
     * Applies Naked Single not causing direct eliminations.
     * For adjustment of the board immediately after Pencilmarks loading.
     */
    public void adjustPencilmarks() {
        for(int i = 0; i < 81; i++) {
            Cell cell = getCell(i);
            BitSet values = getCellPotentialValues(i);
            if(values.cardinality() == 1) {
                int singleclue = values.nextSetBit(0);
                boolean isnakedsingle = true;
                for(int cellIndex : cell.getVisibleCellIndexes()) {
                    if(hasCellPotentialValue(cellIndex, singleclue)) {
                        isnakedsingle = false;
                        break;
                    }
                }
                if(isnakedsingle) {
                	setCellValue(i, singleclue);
                	clearCellPotentialValues(cell);
                }
            }
        }               
    }

    /**
     * Compare two grids for equality. Comparison is based on the values
     * of the cells and on the potential values of the empty cells.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Grid))
            return false;
        Grid other = (Grid)o;
        if(!this.cellValues.equals(other.cellValues)) return false;
        for (int i = 0; i < 81; i++) {
            if (!getCellPotentialValues(i).equals(other.getCellPotentialValues(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < 81; i++) {
            result ^= getCellValue(i);
            result ^= getCellPotentialValues(i).hashCode();
        }
        return result;
    }
}
