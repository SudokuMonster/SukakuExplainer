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
     * Cell values of the grid [0 .. 9].
     */
    private int[] cellValues = new int[81];

    /*
     * Cell values of the grid [0 .. 9].
     */
    private BitSet[] cellPotentialValues = new BitSet[81];
     
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

    // Views
    private static final Row[] rows = {new Row(0), new Row(1), new Row(2), new Row(3), new Row(4), new Row(5), new Row(6), new Row(7), new Row(8)};
    private static final Column[] columns = {new Column(0), new Column(1), new Column(2), new Column(3), new Column(4), new Column(5), new Column(6), new Column(7), new Column(8)};
    private static final Block[] blocks = {new Block(0,0), new Block(0,1), new Block(0,2), new Block(1,0), new Block(1,1), new Block(1,2), new Block(2,0), new Block(2,1), new Block(2,2)};

    //private static final List<Class<? extends Grid.Region>> _regionTypes = null;
    private static final Class<? extends Grid.Region>[] regionTypes = (Class<? extends Grid.Region>[]) new Class[] {Grid.Block.class, Grid.Row.class, Grid.Column.class};

    /**
     * Create a new 9x9 Sudoku grid. All cells are set to empty
     */
    public Grid() {
//        for (int y = 0, i = 0; y < 9; y++) {
//            for (int x = 0; x < 9; x++, i++) {
//                //cells[y][x] = new Cell(i);
//                cells[i] = new Cell(i);
//            }
//        }
        for (int i = 0; i < 81; i++) {
        	cellPotentialValues[i] = new BitSet(10);
        }
        // Build subparts views
//        for (int i = 0; i < 9; i++) {
//            rows[i] = new Row(i);
//            columns[i] = new Column(i);
//            blocks[i] = new Block(i / 3, i % 3);
//        }
    }

    /**
     * Get the cell at the given coordinates
     * @param x the x coordinate (0=leftmost, 8=rightmost)
     * @param y the y coordinate (0=topmost, 8=bottommost)
     * @return the cell at the given coordinates
     */
    public static Cell getCell(int x, int y) {
        //return this.cells[y][x];
        return cells[9 * y + x];
    }

    /**
     * Get the cell at the given index 8 .. 80
     */
    public static Cell getCell(int index) {
        return cells[index];
    }

    /**
     * Get the 9 regions of the given type
     * @param regionType the type of the regions to return. Must be one of
     * {@link Grid.Block}, {@link Grid.Row} or {@link Grid.Column}.
     * @return the 9 regions of the given type
     */
    public Region[] getRegions(Class<? extends Region> regionType) {
        if (regionType == Row.class)
            return Grid.rows;
        else if (regionType == Column.class)
            return Grid.columns;
        else
            return Grid.blocks;
    }

    /**
     * Set the value of a cell
     * @param x the horizontal coordinate of the cell
     * @param y the vertical coordinate of the cell
     * @param value the value to set the cell to. Use 0 to clear the cell.
     */
//    public void setCellValue(int x, int y, int value) {
//        this.cells[y][x].setValue(value);
//    }
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
//    public int getCellValue(int x, int y) {
//        return this.cells[y][x].getValue();
//    }

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
     * @param cell the cell
     * @return the potential values for unresolved cell, empty for resolved
     */
    public BitSet getCellPotentialValues(int cellIndex) {
        //return cells[cellIndex / 9][cellIndex % 9].getPotentialValues();
        return cellPotentialValues[cellIndex];
    }

    /**
     * Get the potential values for the given cell coordinates.
     * <p>
     * The result is returned as a bitset. Each of the
     * bit number 1 to 9 is set if the corresponding
     * value is a potential value for this cell. Bit number
     * <tt>0</tt> is not used and ignored.
     * @param cell the cell
     * @return the potential values for unresolved cell, empty for resolved
     */
    public BitSet getCellPotentialValues(int x, int y) {
        //return cells[y][x].getPotentialValues();
        return cellPotentialValues[y * 9 + x];
    }

    /**
     * Get the potential values for the given cell.
     * <p>
     * The result is returned as a bitset. Each of the
     * bit number 1 to 9 is set if the corresponding
     * value is a potential value for this cell. Bit number
     * <tt>0</tt> is not used and ignored.
     * @param cell the cell
     * @return the potential values for unresolved cell, empty for resolved
     */
    public BitSet getCellPotentialValues(Cell cell) {
        //return cell.getPotentialValues();
        return cellPotentialValues[cell.getIndex()];
    }

    /**
     * Test whether the given value is a potential
     * value for the given cell.
     * @param cell the cell to test
     * @param value the potential value to test, between 1 and 9, inclusive
     * @return whether the given value is a potential value for this cell
     */
    public boolean hasCellPotentialValue(Cell cell, int value) {
        //return cell.hasPotentialValue(value);
    	return cellPotentialValues[cell.getIndex()].get(value);
    }

    /**
     * Add the given value as a potential value for the given cell
     * @param cell the cell
     * @param value the value to add, between 1 and 9, inclusive
     */
    public void addCellPotentialValue(Cell cell, int value) {
        //cell.addPotentialValue(value);
        cellPotentialValues[cell.getIndex()].set(value);
    }

    /**
     * Remove the given value from the potential values of the given cell.
     * @param cell the cell
     * @param value the value to remove, between 1 and 9, inclusive
     */
    public void removeCellPotentialValue(Cell cell, int value) {
        //cell.removePotentialValue(value);
        cellPotentialValues[cell.getIndex()].clear(value);
    }

    /**
     * Removes at once several potential values of the given cell.
     * @param cell the cell
     * @param valuesToRemove bitset with values to remove
     */
    public void removeCellPotentialValues(Cell cell, BitSet valuesToRemove) {
    	//cell.removePotentialValues(valuesToRemove);
        cellPotentialValues[cell.getIndex()].andNot(valuesToRemove);
    }

    /**
     * Clears the potential values of the given cell.
     * @param cell the cell
     */
    public void clearCellPotentialValues(Cell cell) {
        //cell.clearPotentialValues();
        cellPotentialValues[cell.getIndex()].clear();
    }

    /**
     * Set the value of a cell
     * @param index the cell index [0..80]
     * @param value the value to set the cell to. Use 0 to clear the cell.
     */
    public void setCellPotentialValues(int index, BitSet values) {
        cellPotentialValues[index] = (BitSet)values.clone();
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

    /**
     * Get a list containing the three classes corresponding to the
     * three region types (row, column and block)
     * @return a list of the three region types. The resulting list
     * can not be modified
     */
    public static Class<? extends Grid.Region>[] getRegionTypes() {
//    public List<Class<? extends Grid.Region>> getRegionTypes() {
//        if (_regionTypes == null) {
//            _regionTypes = new ArrayList<Class<? extends Grid.Region>>(3);
//            _regionTypes.add(Grid.Block.class);
//            _regionTypes.add(Grid.Row.class);
//            _regionTypes.add(Grid.Column.class);
//            _regionTypes = Collections.unmodifiableList(_regionTypes);
//        }
        return regionTypes;
    }

    // Grid regions implementation (rows, columns, 3x3 squares)

    /**
     * Abstract class representing a region of a sudoku grid. A region
     * is either a row, a column or a 3x3 block.
     */
    public static abstract class Region {
    	protected final int[] regionCells = new int[9];

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
        public abstract int indexOf(Cell cell);

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
                if (grid.getCellValue(cell.getX(), cell.getY()) == value)
                    return true;
            }
            return false;
        }

        /**
         * Test whether this region contains the given cell.
         * @param cell the cell to check
         * @return whether this region contains the given cell
         */
        public abstract boolean contains(Cell cell);

        /**
         * Get the potential positions of the given value within this region.
         * The bits of the returned bitset correspond to indexes of cells, as
         * in {@link #getCell(int)}. Only the indexes of cells that have the given
         * value as a potential value are included in the bitset (see
         * {@link Grid#getCellPotentialValues(Cell cell)}).
         * @param grid the grid
         * @param value the value whose potential positions to get
         * @return the potential positions of the given value within this region
         */
        public BitSet getPotentialPositions(Grid grid, int value) {
            BitSet result = new BitSet(9);
            for (int index = 0; index < 9; index++) {
                result.set(index, grid.hasCellPotentialValue(getCell(index), value));
            }
            return result;
        }

//        public BitSet copyPotentialPositions(int value) {
//            return getPotentialPositions(value); // No need to clone, this is alreay hand-made
//        }
        public BitSet copyPotentialPositions(Grid grid, int value) {
            return getPotentialPositions(grid, value); // No need to clone, this is alreay hand-made
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
            }
        }

        public int getRowNum() {
            return this.rowNum;
        }

//        @Override
//        public Cell getCell(int index) {
//            //return cells[rowNum][index];
//            return cells[9 * rowNum + index];
//        }

        @Override
        public int indexOf(Cell cell) {
            return cell.getX();
        }

        @Override
        public boolean contains(Cell cell) {
        	return cell.getY() == rowNum;
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
    public static class Column extends Region {

        private final int columnNum;

        public Column(int columnNum) {
            this.columnNum = columnNum;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * i + columnNum;
            }
        }

        public int getColumnNum() {
            return this.columnNum;
        }

//        @Override
//        public Cell getCell(int index) {
//            return cells[9 * index + columnNum];
//        }

        @Override
        public int indexOf(Cell cell) {
            return cell.getY();
        }

        @Override
        public boolean contains(Cell cell) {
        	return cell.getX() == columnNum;
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
    public static class Block extends Region {

        private final int vNum, hNum;

        public Block(int vNum, int hNum) {
            this.vNum = vNum;
            this.hNum = hNum;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * (vNum * 3 + i / 3) + (hNum * 3 + i % 3);
            }
        }

        public int getVIndex() {
            return this.vNum;
        }

        public int getHIndex() {
            return this.hNum;
        }

//        @Override
//        public Cell getCell(int index) {
//            //return cells[vNum * 3 + index / 3][hNum * 3 + index % 3];
//            //return cells[9 * (vNum * 3 + index / 3) + (hNum * 3 + index % 3)];
//            return cells[regionCells[index]];
//        }

        @Override
        public int indexOf(Cell cell) {
            return (cell.getY() % 3) * 3 + (cell.getX() % 3);
        }

        @Override
        public boolean contains(Cell cell) {
        	int x = cell.getX();
        	int hStart = hNum * 3;
        	if(x < hStart) return false;
        	if(x > hStart + 2) return false;
        	int y = cell.getY();
        	int vStart = vNum * 3;
        	if(y < vStart) return false;
        	if(y > vStart + 2) return false;
        	return true;
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
                //if (!cell.equals(target) && cell.getValue() == value)
                if (!cell.equals(target) && getCellValue(target.getX(), target.getY()) == value)
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
//    public void copyTo(Grid other) {
//        for (int y = 0; y < 9; y++) {
//            for (int x = 0; x < 9; x++) {
//                this.cells[y][x].copyTo(other.cells[y][x]);
//            }
//        }
//    }
    public void copyTo(Grid other) {
        for (int i = 0; i < 81; i++) {
            other.setCellValue(i, this.cellValues[i]);
            other.setCellPotentialValues(i, cellPotentialValues[i]);
        }
//		for (int y = 0; y < 9; y++) {
//			for (int x = 0; x < 9; x++) {
//				this.cells[y][x].copyTo(other.cells[y][x]);
//			}
//		}
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
                //if (cells[y][x].getValue() == value)
                if (getCellValue(y, x) == value)
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
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
            	//Cell cell = cells[y][x];
            	//int value = cell.getValue();
            	int value = getCellValue(x, y);
            	if(value == 0) {
	                //BitSet values = cell.getPotentialValues();
	                BitSet values = getCellPotentialValues(x, y);
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
        }
        return result.toString();
    }
    
    /**
     * Get a multi-line pencilmark-string representation of this grid.
     */
    //public String toStringMultilinePencilmarks() {
    public String toStringMultilinePencilmarks() {
    	String res = "";
        String s = "";

        int crd = 1;
        for (int i = 0; i < 81; i++) {
            //int n = getCell(i % 9, i / 9).getPotentialValues().cardinality();
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
                                    if ( hasCellPotentialValue(cell, pv) ) {
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
                    //cell.addPotentialValue(value);
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
        for (int i = 0; i < 81; i++) {
            Cell cell = getCell(i);
            //if ( cell.getPotentialValues().cardinality() ==  1 ) {
            BitSet values = getCellPotentialValues(i);
            if ( values.cardinality() ==  1 ) {
                int singleclue = values.nextSetBit(0);
                boolean isnakedsingle = true;
                for (Cell housecell : cell.getHouseCells(this)) {
                    if ( hasCellPotentialValue(housecell, singleclue) ) {
                        isnakedsingle = false;
                        break;
                    }
                }
                if ( isnakedsingle ) {
                    //cell.setValue(singleclue);
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
//        for (int y = 0; y < 9; y++) {
//            for (int x = 0; x < 9; x++) {
//                if (getCellValue(x, y) != other.getCellValue(x, y)) return false;
//                Cell thisCell = this.getCell(x, y);
//                Cell otherCell = other.getCell(x, y);
//                //if (!thisCell.getPotentialValues().equals(otherCell.getPotentialValues()))
//                if (!getCellPotentialValues(thisCell).equals(other.getCellPotentialValues(otherCell)))
//                    return false;
//            }
//        }
        for (int i = 0; i < 81; i++) {
            if (getCellValue(i) != other.getCellValue(i)) return false;
            if (!getCellPotentialValues(i).equals(other.getCellPotentialValues(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
//        for (int y = 0; y < 9; y++) {
//            for (int x = 0; x < 9; x++) {
//                //Cell cell = getCell(x, y);
//                //result ^= cell.getValue();
//                result ^= getCellValue(x, y);
//                //result ^= cell.getPotentialValues().hashCode();
//                result ^= getCellPotentialValues(x, y).hashCode();
//            }
//        }
        for (int i = 0; i < 81; i++) {
            result ^= getCellValue(i);
            result ^= getCellPotentialValues(i).hashCode();
        }
        return result;
    }

}
