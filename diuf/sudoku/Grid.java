/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku;

import java.util.*;

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
     * Cells of the grid. First array index is the vertical index (from top
     * to bottom), and second index is horizontal index (from left to right).
     */
    private Cell[][] cells = new Cell[9][9];

    // Views
    private Row[] rows = new Row[9];
    private Column[] columns = new Column[9];
    private Block[] blocks = new Block[9];


    /**
     * Create a new 9x9 Sudoku grid. All cells are set to empty
     */
    public Grid() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                cells[y][x] = new Cell(this, x, y);
            }
        }
        // Build subparts views
        for (int i = 0; i < 9; i++) {
            rows[i] = new Row(i);
            columns[i] = new Column(i);
            blocks[i] = new Block(i / 3, i % 3);
        }
    }

    /**
     * Get the cell at the given coordinates
     * @param x the x coordinate (0=leftmost, 8=rightmost)
     * @param y the y coordinate (0=topmost, 8=bottommost)
     * @return the cell at the given coordinates
     */
    public Cell getCell(int x, int y) {
        return this.cells[y][x];
    }

    /**
     * Get the 9 regions of the given type
     * @param regionType the type of the regions to return. Must be one of
     * {@link Grid.Block}, {@link Grid.Row} or {@link Grid.Column}.
     * @return the 9 regions of the given type
     */
    public Region[] getRegions(Class<? extends Region> regionType) {
        if (regionType == Row.class)
            return this.rows;
        else if (regionType == Column.class)
            return this.columns;
        else
            return this.blocks;
    }

    /**
     * Get the row at the given index.
     * Rows are numbered from top to bottom.
     * @param num the index of the row to get, between 0 and 8, inclusive
     * @return the row at the given index
     */
    public Row getRow(int num) {
        return this.rows[num];
    }

    /**
     * Get the column at the given index.
     * Columns are numbered from left to right.
     * @param num the index of the column to get, between 0 and 8, inclusive
     * @return the column at the given index
     */
    public Column getColumn(int num) {
        return this.columns[num];
    }

    /**
     * Get the block at the given index.
     * Blocks are numbered from left to right, top to bottom.
     * @param num the index of the block to get, between 0 and 8, inclusive
     * @return the block at the given index
     */
    public Block getBlock(int num) {
        return this.blocks[num];
    }

    /**
     * Get the block at the given location
     * @param vPos the vertical position, between 0 to 2, inclusive
     * @param hPos the horizontal position, between 0 to 2, inclusive
     * @return the block at the given location
     */
    public Block getBlock(int vPos, int hPos) {
        return this.blocks[vPos * 3 + hPos];
    }

    // Cell values

    /**
     * Set the value of a cell
     * @param x the horizontal coordinate of the cell
     * @param y the vertical coordinate of the cell
     * @param value the value to set the cell to. Use 0 to clear the cell.
     */
    public void setCellValue(int x, int y, int value) {
        this.cells[y][x].setValue(value);
    }

    /**
     * Get the value of a cell
     * @param x the horizontal coordinate of the cell
     * @param y the vertical coordinate of the cell
     * @return the value of the cell, or 0 if the cell is empty
     */
    public int getCellValue(int x, int y) {
        return this.cells[y][x].getValue();
    }

    /**
     * Get the row at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the row at the given coordinates
     */
    public Row getRowAt(int x, int y) {
        return this.rows[y];
    }

    /**
     * Get the column at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the column at the given location
     */
    public Column getColumnAt(int x, int y) {
        return this.columns[x];
    }

    /**
     * Get the 3x3 block at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the block at the given coordinates (the coordinates
     * are coordinates of a cell)
     */
    public Block getBlockAt(int x, int y) {
        return this.blocks[(y / 3) * 3 + (x / 3)];
    }

    public Grid.Region getRegionAt(Class<? extends Grid.Region> regionType, int x, int y) {
        if (regionType.equals(Grid.Row.class))
            return getRowAt(x, y);
        else if (regionType.equals(Grid.Column.class))
            return getColumnAt(x, y);
        else
            return getBlockAt(x, y);
    }

    public Grid.Region getRegionAt(Class<? extends Grid.Region> regionType, Cell cell) {
        return getRegionAt(regionType, cell.getX(), cell.getY());
    }

    private List<Class<? extends Grid.Region>> _regionTypes = null;

    /**
     * Get a list containing the three classes corresponding to the
     * three region types (row, column and block)
     * @return a list of the three region types. The resulting list
     * can not be modified
     */
    public List<Class<? extends Grid.Region>> getRegionTypes() {
        if (_regionTypes == null) {
            _regionTypes = new ArrayList<Class<? extends Grid.Region>>(3);
            _regionTypes.add(Grid.Block.class);
            _regionTypes.add(Grid.Row.class);
            _regionTypes.add(Grid.Column.class);
            _regionTypes = Collections.unmodifiableList(_regionTypes);
        }
        return _regionTypes;
    }

    // Grid regions implementation (rows, columns, 3x3 squares)

    /**
     * Abstract class representing a region of a sudoku grid. A region
     * is either a row, a column or a 3x3 block.
     */
    public abstract class Region {

        /**
         * Get a cell of this region by index. The order in which cells are
         * returned according to the index is not defined, but is guaranted
         * to be consistant accross multiple invocations of this method.
         * @param index the index of the cell to get, between 0 (inclusive)
         * and 9 (exclusive).
         * @return the cell at the given index
         */
        public abstract Cell getCell(int index);

        /**
         * Get the index of the given cell within this region.
         * <p>
         * The returned value is consistent with {@link #getCell(int)}.
         * @param cell the cell whose index to get
         * @return the index of the cell, or -1 if the cell does not belong to
         * this region.
         */
        public int indexOf(Cell cell) {
            /*
             * This code is not really used. The method is always overriden
             */
            for (int i = 0; i < 9; i++) {
                if (getCell(i).equals(cell))
                    return i;
            }
            return -1;
        }

        /**
         * Test whether this region contains the given value, that is,
         * is a cell of this region is filled with the given value.
         * @param value the value to check for
         * @return whether this region contains the given value
         */
        public boolean contains(int value) {
            for (int i = 0; i < 9; i++) {
                if (getCell(i).getValue() == value)
                    return true;
            }
            return false;
        }

        /**
         * Get the potential positions of the given value within this region.
         * The bits of the returned bitset correspond to indexes of cells, as
         * in {@link #getCell(int)}. Only the indexes of cells that have the given
         * value as a potential value are included in the bitset (see
         * {@link Cell#getPotentialValues()}).
         * @param value the value whose potential positions to get
         * @return the potential positions of the given value within this region
         * @see Cell#getPotentialValues()
         */
        public BitSet getPotentialPositions(int value) {
            BitSet result = new BitSet(9);
            for (int index = 0; index < 9; index++) {
                result.set(index, getCell(index).hasPotentialValue(value));
            }
            return result;
        }

        public BitSet copyPotentialPositions(int value) {
            return getPotentialPositions(value); // No need to clone, this is alreay hand-made
        }

        /**
         * Get the cells of this region. The iteration order of the result
         * matches the order of the cells returned by {@link #getCell(int)}.
         * @return the cells of this region.
         */
        public Set<Cell> getCellSet() {
            Set<Cell> result = new LinkedHashSet<Cell>();
            for (int i = 0; i < 9; i++)
                result.add(getCell(i));
            return result;
        }

        /**
         * Return the cells that are common to this region and the
         * given region
         * @param other the other region
         * @return the cells belonging to this region and to the other region
         */
        public Set<Cell> commonCells(Region other) {
            Set<Cell> result = this.getCellSet();
            result.retainAll(other.getCellSet());
            return result;
        }

        /**
         * Test whether thsi region crosses an other region.
         * <p>
         * A region crosses another region if they have at least one
         * common cell. In particular, any rows cross any columns.
         * @param other the other region
         * @return whether this region crosses the other region.
         */
        public boolean crosses(Region other) {
            return !commonCells(other).isEmpty();
        }

        /**
         * Get the number of cells of this region that are still empty.
         * @return the number of cells of this region that are still empty
         */
        public int getEmptyCellCount() {
            int result = 0;
            for (int i = 0; i < 9; i++)
                if (getCell(i).isEmpty())
                    result++;
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
    public class Row extends Region {

        private int rowNum;

        public Row(int rowNum) {
            this.rowNum = rowNum;
        }

        public int getRowNum() {
            return this.rowNum;
        }

        @Override
        public Cell getCell(int index) {
            return cells[rowNum][index];
        }

        @Override
        public int indexOf(Cell cell) {
            return cell.getX();
        }

        @Override
        public boolean crosses(Region other) {
            if (other instanceof Block) {
                Block square = (Block)other;
                return rowNum / 3 == square.vNum;
            } else if (other instanceof Column) {
                return true;
            } else if (other instanceof Row) {
                Row row = (Row)other;
                return this.rowNum == row.rowNum;
            } else {
                return super.crosses(other);
            }
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
     * A column a sudoku grid
     */
    public class Column extends Region {

        private int columnNum;

        public Column(int columnNum) {
            this.columnNum = columnNum;
        }

        public int getColumnNum() {
            return this.columnNum;
        }

        @Override
        public Cell getCell(int index) {
            return cells[index][columnNum];
        }

        @Override
        public int indexOf(Cell cell) {
            return cell.getY();
        }

        @Override
        public boolean crosses(Region other) {
            if (other instanceof Block) {
                Block square = (Block)other;
                return columnNum / 3 == square.hNum;
            } else if (other instanceof Row) {
                return true;
            } else if (other instanceof Column) {
                Column column = (Column)other;
                return this.columnNum == column.columnNum;
            } else {
                return super.crosses(other);
            }
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
    public class Block extends Region {

        private int vNum, hNum;

        public Block(int vNum, int hNum) {
            this.vNum = vNum;
            this.hNum = hNum;
        }

        public int getVIndex() {
            return this.vNum;
        }

        public int getHIndex() {
            return this.hNum;
        }

        @Override
        public Cell getCell(int index) {
            return cells[vNum * 3 + index / 3][hNum * 3 + index % 3];
        }

        @Override
        public int indexOf(Cell cell) {
            return (cell.getY() % 3) * 3 + (cell.getX() % 3);
        }

        @Override
        public boolean crosses(Region other) {
            if (other instanceof Row) {
                return ((Row)other).crosses(this);
            } else if (other instanceof Column) {
                return ((Column)other).crosses(this);
            } else if (other instanceof Block) {
                Block square = (Block)other;
                return this.vNum == square.vNum && this.hNum == square.hNum;
            } else {
                return super.crosses(other);
            }
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
        for (Class<? extends Region> regionType : getRegionTypes()) {
            Region region = getRegionAt(regionType, target.getX(), target.getY());
            for (int i = 0; i < 9; i++) {
                Cell cell = region.getCell(i);
                if (!cell.equals(target) && cell.getValue() == value)
                    return cell;
            }
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
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                this.cells[y][x].copyTo(other.cells[y][x]);
            }
        }
    }

    /**
     * Get the number of occurances of a given value in this grid
     * @param value the value
     * @return the number of occurances of a given value in this grid
     */
    public int getCountOccurancesOfValue(int value) {
        int result = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (cells[y][x].getValue() == value)
                    result++;
            }
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
     * Compare two grids for equality. Comparison is based on the values
     * of the cells and on the potential values of the empty cells.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Grid))
            return false;
        Grid other = (Grid)o;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell thisCell = this.getCell(x, y);
                Cell otherCell = other.getCell(x, y);
                if (thisCell.getValue() != otherCell.getValue())
                    return false;
                if (!thisCell.getPotentialValues().equals(otherCell.getPotentialValues()))
                    return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = getCell(x, y);
                result ^= cell.getValue();
                result ^= cell.getPotentialValues().hashCode();
            }
        }
        return result;
    }

}
