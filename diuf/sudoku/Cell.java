/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku;

import diuf.sudoku.tools.CellSet;

public class Cell {

    private final int index;

    /**
     * Create a new cell
     * @param grid the grid this cell is part of
     * @param x the x coordinate of this cell (0=leftmost, 8=rightmost)
     * @param y the y coordinate of this cell (0=topmost, 8=bottommost)
     */
    public Cell(int index) {
        this.index = index;
    }
    
    /**
     * Get the x coordinate of this cell.
     * 0 = leftmost, 8 = rightmost
     * @return the x coordinate of this cell
     */
    public int getX() {
        return this.index % 9;
    }

    /**
     * Get the y coordinate of this cell.
     * 0 = topmost, 8 = bottommost
     * @return the y coordinate of this cell
     */
    public int getY() {
        return this.index / 9;
    }

    /**
     * Get the index of this cell.
     * 0 ..8 = top row, ... 81 = bottom right
     * @return the index of this cell
     */
    public int getIndex() {
    	return index;
    }

    /**
     * Set the value of this cell, and remove that value
     * from the potential values of all controlled cells.
     * <p>
     * This cell must be empty before this call, and the
     * given value must be non-zero.
     * @param value the value to set this cell to.
     * @see #getHouseCells()
     */
    public void setValueAndCancel(int value, Grid targetGrid) {
        assert value != 0;
        targetGrid.setCellValue(this.index, value);
        targetGrid.clearCellPotentialValues(this.index);
        for(int i = 0; i < 20; i++) {
        	targetGrid.removeCellPotentialValue(Grid.visibleCellIndex[this.index][i], value);
        }
    }

    /**
     * Get the cells that form the "house" of this cell. The
     * "house" cells are all the cells that are in the
     * same block, row or column.
     * <p>
     * The iteration order is guaranteed to be the same on each
     * invocation of this method for the same cell. (this is
     * necessary to ensure that hints of the same difficulty
     * are always returned in the same order).
     * @return the cells that are controlled by this cell
     */
    public CellSet getVisibleCells() {
        // Use a set to prevent duplicates (cells in both block and row/column)
    	//return new CellSet(Grid.visibleCellIndex[index]);
    	return Grid.visibleCellsSet[index];
    }

    /**
     * Get the cell idexes that form the "house" of this cell. The
     * "house" cells are all the cells that are in the
     * same block, row or column.
     * <p>
     * The iteration order is guaranted to be the same on each
     * invocation of this method for the same cell. (this is
     * necessary to ensure that hints of the same difficulty
     * are always returned in the same order).
     * @return array of the cell indexes that are controlled by this cell
     */
    public int[] getVisibleCellIndexes() {
        return Grid.visibleCellIndex[index];
    }

    /**
     * Get a string representation of a cell. The notation that
     * is used is defined by the {@link Settings} class.
     * @param x the horizontal coordinate of the cell (0=leftmost, 8=rightmost)
     * @param y the vertical coordinate of the cell (0=topmost, 8=bottommost)
     * @return a string representation of the cell
     */
    private static String toString(int x, int y) {
        Settings settings = Settings.getInstance();
        if (settings.isRCNotation())
            return "R" + (y + 1) + "C" + (x + 1);
        else
            return "" + (char)('A' + x) + (y + 1);
    }

    /**
     * Get a complete string representation of this cell.
     * <p>
     * Returns "Cell " followed by the result of the {@link #toString()} method.
     * @return a complete string representation of this cell.
     */
    public String toFullString() {
        return "Cell " + toString(getX(), getY());
    }

    /**
     * Get a string representation of this cell.
     * <p>
     * Returned strings are in the form "A1", "A2", "A3", ...
     * "I9".
     * @return a string representation of this cell.
     */
    @Override
    public String toString() {
        return toString(getX(), getY());
    }

    /**
     * Get a full string representation of multiple cells.
     * <p>
     * The returned string might be, for example:
     * "Cells A1, B4, C3"
     * @param cells the cells
     * @return a full string representation of the cells
     */
    public static String toFullString(Cell... cells) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cell");
        if (cells.length <= 1)
            builder.append(" ");
        else
            builder.append("s ");
        for (int i = 0; i < cells.length; i++) {
            if (i > 0)
                builder.append(",");
            Cell cell = cells[i];
            builder.append(toString(cell.getX(), cell.getY()));      
        }
        return builder.toString();
    }

    /**
     * Get a string representation of multiple cells.
     * The returned string is a concatenation of the
     * result of calling {@link #toString()} on each cell,
     * separated by ",".
     * @param cells the cells to convert to a string
     * @return a string representation of the given cells.
     */
    public static String toString(Cell... cells) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0)
                builder.append(",");
            Cell cell = cells[i];
            builder.append(toString(cell.getX(), cell.getY()));      
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cell))
            return false;
    	if(this == o) return true;
    	Cell other = (Cell)o;
    	if(index != other.getIndex()) return false;
    	
    	return true;
    }
    
    @Override
    public int hashCode() {
    	return getIndex();
    }
}
