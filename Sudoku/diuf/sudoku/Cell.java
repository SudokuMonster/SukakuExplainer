/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku;

import java.util.*;

/**
 * A cell of a sudoku grid.
 * <p>
 * Holds:
 * <ul>
 * <li>The grid this cell belongs to
 * <li>The x and y coordinates within the grid
 * <li>The current value, or <code>0</code> if the cell is empty
 * <li>The bitset of potential values for this cell (the candidates).
 * </ul>
 */
public class Cell {

    private final Grid grid;
    private final int x;
    private final int y;
    private int value = 0;
    private BitSet potentialValues = new BitSet(9);


    /**
     * Create a new cell
     * @param grid the grid this cell is part of
     * @param x the x coordinate of this cell (0=leftmost, 8=rightmost)
     * @param y the y coordinate of this cell (0=topmost, 8=bottommost)
     */
    public Cell(Grid grid, int x, int y) {
        this.grid = grid;
        this.x = x;
        this.y = y;
    }

    /**
     * Get the x coordinate of this cell.
     * 0 = leftmost, 8 = rightmost
     * @return the x coordinate of this cell
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get the y coordinate of this cell.
     * 0 = topmost, 8 = bottommost
     * @return the y coordinate of this cell
     */
    public int getY() {
        return this.y;
    }

    /**
     * Get the value of this cell. Returns <tt>0</tt>
     * if this cell is still empty.
     * @return the value of this cell.
     */
    public int getValue() {
        return value;
    }

    /**
     * Get whether this cell is empty
     * @return whether this cell is empty
     */
    public boolean isEmpty() {
        return (value == 0);
    }

    /**
     * Set the value of this cell.
     * @param value the value of this cell. Use <tt>0</tt> to
     * clear it.
     */
    public void setValue(int value) {
        this.value = value;
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
    public void setValueAndCancel(int value) {
        assert value != 0;
        this.value = value;
        this.potentialValues.clear();
        for (Class<? extends Grid.Region> regionType : grid.getRegionTypes()) {
            Grid.Region region = grid.getRegionAt(regionType, this.x, this.y);
            for (int i = 0; i < 9; i++) {
                Cell other = region.getCell(i);
                other.removePotentialValue(value);
            }
        }
    }

    /**
     * Get the potential values for this cell.
     * <p>
     * The result is returned as a bitset. Each of the
     * bit number 1 to 9 is set if the corresponding
     * value is a potential value for this cell. Bit number
     * <tt>0</tt> is not used and ignored.
     * @return the potential values for this cell
     */
    public BitSet getPotentialValues() {
        return this.potentialValues;
    }

    /**
     * Test whether the given value is a potential
     * value for this cell.
     * @param value the potential value to test, between 1 and 9, inclusive
     * @return whether the given value is a potential value for this cell
     */
    public boolean hasPotentialValue(int value) {
        return this.potentialValues.get(value);
    }

    /**
     * Add the given value as a potential value for this cell
     * @param value the value to add, between 1 and 9, inclusive
     */
    public void addPotentialValue(int value) {
        this.potentialValues.set(value, true);
    }

    /**
     * Remove the given value from the potential values of this cell.
     * @param value the value to remove, between 1 and 9, inclusive
     */
    public void removePotentialValue(int value) {
        this.potentialValues.set(value, false);
    }

    public void removePotentialValues(BitSet valuesToRemove) {
        this.potentialValues.andNot(valuesToRemove);
    }

    public void clearPotentialValues() {
        this.potentialValues.clear();
    }

    /**
     * Get the cells that form the "house" of this cell. The
     * "house" cells are all the cells that are in the
     * same block, row or column.
     * <p>
     * The iteration order is guaranted to be the same on each
     * invocation of this method for the same cell. (this is
     * necessary to ensure that hints of the same difficulty
     * are always returned in the same order).
     * @return the cells that are controlled by this cell
     */
    public Collection<Cell> getHouseCells() {
        // Use a set to prevent duplicates (cells in both block and row/column)
        Collection<Cell> result = new LinkedHashSet<Cell>();
        // Iterate on region types (Block, Row, Column)
        for (Class<? extends Grid.Region> regionType : grid.getRegionTypes()) {
            // Get region on which this cell is
            Grid.Region region = grid.getRegionAt(regionType, x, y);
            // Add all cell of that region
            for (int i = 0; i < 9; i++)
                result.add(region.getCell(i));
        }
        // Remove this cell
        result.remove(this);
        return result;
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
        return "Cell " + toString(x, y);
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
        return toString(x, y);
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
            builder.append(toString(cell.x, cell.y));      
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
            builder.append(toString(cell.x, cell.y));      
        }
        return builder.toString();
    }

    /**
     * Copy this cell to another one. The value and potential values
     * are copied, but the grid reference and the coordinates are not.
     * @param other the cell to copy this cell to
     */
    public void copyTo(Cell other) {
        assert this.x == other.x && this.y == other.y;
        other.value = this.value;
        other.potentialValues = (BitSet)this.potentialValues.clone();
    }

}
