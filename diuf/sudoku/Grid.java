/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
// For debugging: java -agentlib:jdwp=transport=dt_socket,server=y,address=8000 
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
     * Cell values of the grid [0 .. 9]. 0 = unknown.
     */
    private int[] cellValues = new int[81];

    /*
     * Cell potential values of the grid, bit 0 is unused.
     */
    private BitSet[] cellPotentialValues = new BitSet[81];
	private boolean[] isGiven = new boolean[81];

	private int isSudoku;  // 1=isSudoku (default), 0=isSukaku (set when Sukaku is loaded)
	
//    //cache for Region.getPotentialPositions(value)
//    private valueCells valueCellsCache = new valueCells();
//    private class valueCells {
//        private BitSet[][][] valuePotentialCells = new BitSet[3][9][9]; //region type, region, value
//
//        private valueCells() {
//            for(int regionType = 0; regionType < 3; regionType++) {
//                for(int region = 0; region < 9; region++) {
//    	            for(int cell = 0; cell < 9; cell++) {
//	            		valuePotentialCells[regionType][region][cell] = new BitSet(9);
//    	            }
//                }
//            }
//        }
//        public void invalidateCellValue(int cellIndex, int value) {
//        	for(int t = 0; t < 3; t++) { //region types
//        		valuePotentialCells[t][cellRegions[cellIndex][t]][value - 1].clear();
//        	}
//        }
//        public void invalidateCell(int cellIndex) {
//        	for(int t = 0; t < 3; t++) { //region types
//        		for(int v = 0; v < 9; v++) { //values
//        			valuePotentialCells[t][cellRegions[cellIndex][t]][v].clear();
//        		}
//        	}
//        }
//        public BitSet getRegionValueCells(Region region, int value) {
//        	int regionType = region.getRegionTypeIndex();
//        	int regionIndex = region.getRegionIndex();
//        	BitSet result = valuePotentialCells[regionType][regionIndex][value - 1];
//        	if(result.isEmpty()) { //build
//        		result = new BitSet(9);
//                for (int index = 0; index < 9; index++) {
//                    result.set(index, hasCellPotentialValue(region.getCell(index).getIndex(), value));
//                }
//                valuePotentialCells[regionType][regionIndex][value - 1] = result; //store to cache
//        	}
//        	return result;
//        }
//    }
    private static final Cell cells[];
    public static final int[][] regionCellIndex;
    public static final int[][] cellRegions;
    public static int[][] visibleCellIndex;
	public static int[][] forwardVisibleCellIndex;
	public static int[][] antiVisibleCellIndex;
	private static final int [][] windowsVisibleCellIndex;
	private static final int [][] DGVisibleCellIndex;
	private static final int [][] XVisibleCellIndex;
	private static final int [][] asteriskVisibleCellIndex;
	private static final int [][] girandolaVisibleCellIndex;
	private static final int [][] CDVisibleCellIndex;
	public static final int [][] ferzCellIndex;
	//private static final int [][] wazirCellIndex;
	public static final int [][] knightCellIndex;
	public static final int [][] wazirCellsRegular;
	public static final int [][] wazirCellsToroidal;
	public static final int [][] ferzCellsRegular;
	public static final int [][] ferzCellsToroidal;
    private static final Block[] blocks;
    private static final Row[] rows;
    private static final Column[] columns;
    private static final DG[] DGs;
    private static final Window[] windows;
    private static final diagonalMain[] diagonal1;
    private static final diagonalAnti[] diagonal2;
	private static final Girandola[] girandola;
	private static final Asterisk[] asterisk;
	private static final CD[] cd;
    public static final Region[][] regions;
    public static CellSet[] visibleCellsSet;
	public static CellSet[] forwardVisibleCellsSet;
	public static CellSet[] antiVisibleCellsSet;
    static {
    	cells = new Cell[] {
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
    	regionCellIndex = new int[81][10]; //[cell][getRegionTypeIndex()] //@sudokuMonster 5 is Temp
    	cellRegions = new int[81][10]; //[cell][getRegionTypeIndex()]//@sudokuMonster 5 is Temp
    	visibleCellIndex = new int[][] {
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
//@SudokuMonster: VisibleCellIndex for Windows groups
    	windowsVisibleCellIndex = new int[][] {			
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 10, 11, 12, 19, 20, 21, 28, 29, 30},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 14, 15, 16, 23, 24, 25, 32, 33, 34},
			{ 9, 18, 27, 13, 22, 31, 17, 26, 35},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{46, 47, 48, 55, 56, 57, 64, 65, 66},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{50, 51, 52, 59, 60, 61, 68, 69, 70},
			{45, 54, 63, 49, 58, 67, 53, 62, 71},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 1, 2, 3, 37, 38, 39, 73, 74, 75},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 5, 6, 7, 41, 42, 43, 77, 78, 79},
			{ 0, 4, 8, 36, 40, 44, 72, 76, 80}
			};
			
//@SudokuMonster: VisibleCellIndex for Disjoint Groups (DG)
		DGVisibleCellIndex = new int [][] {
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{0, 3,  6,  27, 30, 33, 54, 57, 60},
			{1, 4,  7,  28, 31, 34, 55, 58, 61},
			{2, 5,  8,  29, 32, 35, 56, 59, 62},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{9, 12, 15, 36, 39, 42, 63, 66, 69},
			{10,13, 16, 37, 40, 43, 64, 67, 70},
			{11,14, 17, 38, 41, 44, 65, 68, 71},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80},
			{18,21, 24, 45, 48, 51, 72, 75, 78},
			{19,22, 25, 46, 49, 52, 73, 76, 79},
			{20,23, 26, 47, 50, 53, 74, 77, 80}
			};
			
//@SudokuMonster: VisibleCellIndex for X (2 Main Diagonals) groups
		XVisibleCellIndex = new int [][] {
			{0,	10,	20,	30,	40,	50,	60,	70,	80},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{0,	10,	20,	30,	40,	50,	60,	70,	80},
			{},
			{},
			{},
			{},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{},
			{},
			{0,	10,	20,	30,	40,	50,	60,	70,	80},
			{},
			{},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{},
			{},
			{},
			{},
			{0,	10,	20,	30,	40,	50,	60,	70,	80},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{0,	8,	10,	16,	20,	24,	30,	32,	40,	48,	50,	56,	60,	64,	70,	72,	80},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{0,	10,	20,	30,	40,	50,	60,	70,	80},
			{},
			{},
			{},
			{},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{},
			{},
			{0,	10,	20,	30,	40,	50,	60,	70,	80},
			{},
			{},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{},
			{},
			{},
			{},
			{0,	10,	20,	30,	40,	50,	60,	70,	80},
			{},
			{8,	16,	24,	32,	40,	48,	56,	64,	72},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{0,	10,	20,	30,	40,	50,	60,	70,	80}
			};

//@SudokuMonster: VisibleCellIndex for Asterisk Extra group {13,20,24,37,40,43,56,60,67}
		asteriskVisibleCellIndex = new int [][] {
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{},
			{},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{},
			{},
			{},
			{},
			{13,20,24,37,40,43,56,60,67},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{}
			};

//@SudokuMonster: VisibleCellIndex for Girandola Extra group {0,8,13,37,40,43,67,72,80}
		girandolaVisibleCellIndex = new int [][] {
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{},
			{},
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{},
			{},
			{0,8,13,37,40,43,67,72,80},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{0,8,13,37,40,43,67,72,80}
			};

//@SudokuMonster: VisibleCellIndex for Center Dot (CD) Extra group {10,13,16,37,40,43,64,67,70}
		CDVisibleCellIndex = new int [][] {
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{10,13,16,37,40,43,64,67,70},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{}
			};

		/*wazirCellIndex = new int [][] {
			{1,0},
			{-1,0},
			{0,1},
			{0,-1}
		};*/

		ferzCellsToroidal = new int [][] {
			{71,73,8,10},
			{72,74,9,11},
			{73,75,10,12},
			{74,76,11,13},
			{75,77,12,14},
			{76,78,13,15},
			{77,79,14,16},
			{78,80,15,17},
			{79,0,16,18},
			{80,1,17,19},
			{0,2,18,20},
			{1,3,19,21},
			{2,4,20,22},
			{3,5,21,23},
			{4,6,22,24},
			{5,7,23,25},
			{6,8,24,26},
			{7,9,25,27},
			{8,10,26,28},
			{9,11,27,29},
			{10,12,28,30},
			{11,13,29,31},
			{12,14,30,32},
			{13,15,31,33},
			{14,16,32,34},
			{15,17,33,35},
			{16,18,34,36},
			{17,19,35,37},
			{18,20,36,38},
			{19,21,37,39},
			{20,22,38,40},
			{21,23,39,41},
			{22,24,40,42},
			{23,25,41,43},
			{24,26,42,44},
			{25,27,43,45},
			{26,28,44,46},
			{27,29,45,47},
			{28,30,46,48},
			{29,31,47,49},
			{30,32,48,50},
			{31,33,49,51},
			{32,34,50,52},
			{33,35,51,53},
			{34,36,52,54},
			{35,37,53,55},
			{36,38,54,56},
			{37,39,55,57},
			{38,40,56,58},
			{39,41,57,59},
			{40,42,58,60},
			{41,43,59,61},
			{42,44,60,62},
			{43,45,61,63},
			{44,46,62,64},
			{45,47,63,65},
			{46,48,64,66},
			{47,49,65,67},
			{48,50,66,68},
			{49,51,67,69},
			{50,52,68,70},
			{51,53,69,71},
			{52,54,70,72},
			{53,55,71,73},
			{54,56,72,74},
			{55,57,73,75},
			{56,58,74,76},
			{57,59,75,77},
			{58,60,76,78},
			{59,61,77,79},
			{60,62,78,80},
			{61,63,79,0},
			{62,64,80,1},
			{63,65,0,2},
			{64,66,1,3},
			{65,67,2,4},
			{66,68,3,5},
			{67,69,4,6},
			{68,70,5,7},
			{69,71,6,8},
			{70,72,7,9}
		};
		
		ferzCellsRegular = new int [][] {
			{10},
			{9,11},
			{10,12},
			{11,13},
			{12,14},
			{13,15},
			{14,16},
			{15,17},
			{16},
			{1,19},
			{0,2,18,20},
			{1,3,19,21},
			{2,4,20,22},
			{3,5,21,23},
			{4,6,22,24},
			{5,7,23,25},
			{6,8,24,26},
			{7,25},
			{10,28},
			{9,11,27,29},
			{10,12,28,30},
			{11,13,29,31},
			{12,14,30,32},
			{13,15,31,33},
			{14,16,32,34},
			{15,17,33,35},
			{16,34},
			{19,37},
			{18,20,36,38},
			{19,21,37,39},
			{20,22,38,40},
			{21,23,39,41},
			{22,24,40,42},
			{23,25,41,43},
			{24,26,42,44},
			{25,43},
			{28,46},
			{27,29,45,47},
			{28,30,46,48},
			{29,31,47,49},
			{30,32,48,50},
			{31,33,49,51},
			{32,34,50,52},
			{33,35,51,53},
			{34,52},
			{37,55},
			{36,38,54,56},
			{37,39,55,57},
			{38,40,56,58},
			{39,41,57,59},
			{40,42,58,60},
			{41,43,59,61},
			{42,44,60,62},
			{43,61},
			{46,64},
			{45,47,63,65},
			{46,48,64,66},
			{47,49,65,67},
			{48,50,66,68},
			{49,51,67,69},
			{50,52,68,70},
			{51,53,69,71},
			{52,70},
			{55,73},
			{54,56,72,74},
			{55,57,73,75},
			{56,58,74,76},
			{57,59,75,77},
			{58,60,76,78},
			{59,61,77,79},
			{60,62,78,80},
			{61,79},
			{64},
			{63,65},
			{64,66},
			{65,67},
			{66,68},
			{67,69},
			{68,70},
			{69,71},
			{70}
		};
		
		wazirCellsToroidal = new int [][] {
			{72,9,1,8},
			{73,10,2,0},
			{74,11,3,1},
			{75,12,4,2},
			{76,13,5,3},
			{77,14,6,4},
			{78,15,7,5},
			{79,16,8,6},
			{80,17,0,7},
			{0,18,10,17},
			{1,19,11,9},
			{2,20,12,10},
			{3,21,13,11},
			{4,22,14,12},
			{5,23,15,13},
			{6,24,16,14},
			{7,25,17,15},
			{8,26,9,16},
			{9,27,19,26},
			{10,28,20,18},
			{11,29,21,19},
			{12,30,22,20},
			{13,31,23,21},
			{14,32,24,22},
			{15,33,25,23},
			{16,34,26,24},
			{17,35,18,25},
			{18,36,28,35},
			{19,37,29,27},
			{20,38,30,28},
			{21,39,31,29},
			{22,40,32,30},
			{23,41,33,31},
			{24,42,34,32},
			{25,43,35,33},
			{26,44,27,34},
			{27,45,37,44},
			{28,46,38,36},
			{29,47,39,37},
			{30,48,40,38},
			{31,49,41,39},
			{32,50,42,40},
			{33,51,43,41},
			{34,52,44,42},
			{35,53,36,43},
			{36,54,46,53},
			{37,55,47,45},
			{38,56,48,46},
			{39,57,49,47},
			{40,58,50,48},
			{41,59,51,49},
			{42,60,52,50},
			{43,61,53,51},
			{44,62,45,52},
			{45,63,55,62},
			{46,64,56,54},
			{47,65,57,55},
			{48,66,58,56},
			{49,67,59,57},
			{50,68,60,58},
			{51,69,61,59},
			{52,70,62,60},
			{53,71,54,61},
			{54,72,64,71},
			{55,73,65,63},
			{56,74,66,64},
			{57,75,67,65},
			{58,76,68,66},
			{59,77,69,67},
			{60,78,70,68},
			{61,79,71,69},
			{62,80,63,70},
			{63,0,73,80},
			{64,1,74,72},
			{65,2,75,73},
			{66,3,76,74},
			{67,4,77,75},
			{68,5,78,76},
			{69,6,79,77},
			{70,7,80,78},
			{71,8,72,79}
		};
		
		wazirCellsRegular = new int [][] {
			{9,1},
			{10,2,0},
			{11,3,1},
			{12,4,2},
			{13,5,3},
			{14,6,4},
			{15,7,5},
			{16,8,6},
			{17,7},
			{0,18,10},
			{1,19,11,9},
			{2,20,12,10},
			{3,21,13,11},
			{4,22,14,12},
			{5,23,15,13},
			{6,24,16,14},
			{7,25,17,15},
			{8,26,16},
			{9,27,19},
			{10,28,20,18},
			{11,29,21,19},
			{12,30,22,20},
			{13,31,23,21},
			{14,32,24,22},
			{15,33,25,23},
			{16,34,26,24},
			{17,35,25},
			{18,36,28},
			{19,37,29,27},
			{20,38,30,28},
			{21,39,31,29},
			{22,40,32,30},
			{23,41,33,31},
			{24,42,34,32},
			{25,43,35,33},
			{26,44,34},
			{27,45,37},
			{28,46,38,36},
			{29,47,39,37},
			{30,48,40,38},
			{31,49,41,39},
			{32,50,42,40},
			{33,51,43,41},
			{34,52,44,42},
			{35,53,43},
			{36,54,46},
			{37,55,47,45},
			{38,56,48,46},
			{39,57,49,47},
			{40,58,50,48},
			{41,59,51,49},
			{42,60,52,50},
			{43,61,53,51},
			{44,62,52},
			{45,63,55},
			{46,64,56,54},
			{47,65,57,55},
			{48,66,58,56},
			{49,67,59,57},
			{50,68,60,58},
			{51,69,61,59},
			{52,70,62,60},
			{53,71,61},
			{54,72,64},
			{55,73,65,63},
			{56,74,66,64},
			{57,75,67,65},
			{58,76,68,66},
			{59,77,69,67},
			{60,78,70,68},
			{61,79,71,69},
			{62,80,70},
			{63,73},
			{64,74,72},
			{65,75,73},
			{66,76,74},
			{67,77,75},
			{68,78,76},
			{69,79,77},
			{70,80,78},
			{71,79}
		};
		
		ferzCellIndex = new int [][] {
			{1,1},
			{-1,1},
			{-1,-1},
			{1,-1}
		};
				
		knightCellIndex = new int [][] {
			{1,2},
			{-1,2},
			{1,-2},
			{-1,-2},
			{2,1},
			{-2,1},
			{2,-1},
			{-2,-1}
		};

//@SudokuMonster: Visible indexes >  Cell index; used to speed search and minimize repeats in some techniques (e.g WXYZ wing)
    	forwardVisibleCellIndex = new int [][] {
			{1,2,3,4,5,6,7,8,9,10,11,18,19,20,27,36,45,54,63,72},
			{2,3,4,5,6,7,8,9,10,11,18,19,20,28,37,46,55,64,73},
			{3,4,5,6,7,8,9,10,11,18,19,20,29,38,47,56,65,74},
			{4,5,6,7,8,12,13,14,21,22,23,30,39,48,57,66,75},
			{5,6,7,8,12,13,14,21,22,23,31,40,49,58,67,76},
			{6,7,8,12,13,14,21,22,23,32,41,50,59,68,77},
			{7,8,15,16,17,24,25,26,33,42,51,60,69,78},
			{8,15,16,17,24,25,26,34,43,52,61,70,79},
			{15,16,17,24,25,26,35,44,53,62,71,80},
			{10,11,12,13,14,15,16,17,18,19,20,27,36,45,54,63,72},
			{11,12,13,14,15,16,17,18,19,20,28,37,46,55,64,73},
			{12,13,14,15,16,17,18,19,20,29,38,47,56,65,74},
			{13,14,15,16,17,21,22,23,30,39,48,57,66,75},
			{14,15,16,17,21,22,23,31,40,49,58,67,76},
			{15,16,17,21,22,23,32,41,50,59,68,77},
			{16,17,24,25,26,33,42,51,60,69,78},
			{17,24,25,26,34,43,52,61,70,79},
			{24,25,26,35,44,53,62,71,80},
			{19,20,21,22,23,24,25,26,27,36,45,54,63,72},
			{20,21,22,23,24,25,26,28,37,46,55,64,73},
			{21,22,23,24,25,26,29,38,47,56,65,74},
			{22,23,24,25,26,30,39,48,57,66,75},
			{23,24,25,26,31,40,49,58,67,76},
			{24,25,26,32,41,50,59,68,77},
			{25,26,33,42,51,60,69,78},
			{26,34,43,52,61,70,79},
			{35,44,53,62,71,80},
			{28,29,30,31,32,33,34,35,36,37,38,45,46,47,54,63,72},
			{29,30,31,32,33,34,35,36,37,38,45,46,47,55,64,73},
			{30,31,32,33,34,35,36,37,38,45,46,47,56,65,74},
			{31,32,33,34,35,39,40,41,48,49,50,57,66,75},
			{32,33,34,35,39,40,41,48,49,50,58,67,76},
			{33,34,35,39,40,41,48,49,50,59,68,77},
			{34,35,42,43,44,51,52,53,60,69,78},
			{35,42,43,44,51,52,53,61,70,79},
			{42,43,44,51,52,53,62,71,80},
			{37,38,39,40,41,42,43,44,45,46,47,54,63,72},
			{38,39,40,41,42,43,44,45,46,47,55,64,73},
			{39,40,41,42,43,44,45,46,47,56,65,74},
			{40,41,42,43,44,48,49,50,57,66,75},
			{41,42,43,44,48,49,50,58,67,76},
			{42,43,44,48,49,50,59,68,77},
			{43,44,51,52,53,60,69,78},
			{44,51,52,53,61,70,79},
			{51,52,53,62,71,80},
			{46,47,48,49,50,51,52,53,54,63,72},
			{47,48,49,50,51,52,53,55,64,73},
			{48,49,50,51,52,53,56,65,74},
			{49,50,51,52,53,57,66,75},
			{50,51,52,53,58,67,76},
			{51,52,53,59,68,77},
			{52,53,60,69,78},
			{53,61,70,79},
			{62,71,80},
			{55,56,57,58,59,60,61,62,63,64,65,72,73,74},
			{56,57,58,59,60,61,62,63,64,65,72,73,74},
			{57,58,59,60,61,62,63,64,65,72,73,74},
			{58,59,60,61,62,66,67,68,75,76,77},
			{59,60,61,62,66,67,68,75,76,77},
			{60,61,62,66,67,68,75,76,77},
			{61,62,69,70,71,78,79,80},
			{62,69,70,71,78,79,80},
			{69,70,71,78,79,80},
			{64,65,66,67,68,69,70,71,72,73,74},
			{65,66,67,68,69,70,71,72,73,74},
			{66,67,68,69,70,71,72,73,74},
			{67,68,69,70,71,75,76,77},
			{68,69,70,71,75,76,77},
			{69,70,71,75,76,77},
			{70,71,78,79,80},
			{71,78,79,80},
			{78,79,80},
			{73,74,75,76,77,78,79,80},
			{74,75,76,77,78,79,80},
			{75,76,77,78,79,80},
			{76,77,78,79,80},
			{77,78,79,80},
			{78,79,80},
			{79,80},
			{80},
			{}
			};
		antiVisibleCellIndex = new int [81][0];
    	visibleCellsSet = new CellSet[] {
				new CellSet(visibleCellIndex[0]),new CellSet(visibleCellIndex[1]),new CellSet(visibleCellIndex[2]),new CellSet(visibleCellIndex[3]),new CellSet(visibleCellIndex[4]),new CellSet(visibleCellIndex[5]),new CellSet(visibleCellIndex[6]),new CellSet(visibleCellIndex[7]),new CellSet(visibleCellIndex[8]),
				new CellSet(visibleCellIndex[9]),new CellSet(visibleCellIndex[10]),new CellSet(visibleCellIndex[11]),new CellSet(visibleCellIndex[12]),new CellSet(visibleCellIndex[13]),new CellSet(visibleCellIndex[14]),new CellSet(visibleCellIndex[15]),new CellSet(visibleCellIndex[16]),new CellSet(visibleCellIndex[17]),
				new CellSet(visibleCellIndex[18]),new CellSet(visibleCellIndex[19]),new CellSet(visibleCellIndex[20]),new CellSet(visibleCellIndex[21]),new CellSet(visibleCellIndex[22]),new CellSet(visibleCellIndex[23]),new CellSet(visibleCellIndex[24]),new CellSet(visibleCellIndex[25]),new CellSet(visibleCellIndex[26]),
				new CellSet(visibleCellIndex[27]),new CellSet(visibleCellIndex[28]),new CellSet(visibleCellIndex[29]),new CellSet(visibleCellIndex[30]),new CellSet(visibleCellIndex[31]),new CellSet(visibleCellIndex[32]),new CellSet(visibleCellIndex[33]),new CellSet(visibleCellIndex[34]),new CellSet(visibleCellIndex[35]),
				new CellSet(visibleCellIndex[36]),new CellSet(visibleCellIndex[37]),new CellSet(visibleCellIndex[38]),new CellSet(visibleCellIndex[39]),new CellSet(visibleCellIndex[40]),new CellSet(visibleCellIndex[41]),new CellSet(visibleCellIndex[42]),new CellSet(visibleCellIndex[43]),new CellSet(visibleCellIndex[44]),
				new CellSet(visibleCellIndex[45]),new CellSet(visibleCellIndex[46]),new CellSet(visibleCellIndex[47]),new CellSet(visibleCellIndex[48]),new CellSet(visibleCellIndex[49]),new CellSet(visibleCellIndex[50]),new CellSet(visibleCellIndex[51]),new CellSet(visibleCellIndex[52]),new CellSet(visibleCellIndex[53]),
				new CellSet(visibleCellIndex[54]),new CellSet(visibleCellIndex[55]),new CellSet(visibleCellIndex[56]),new CellSet(visibleCellIndex[57]),new CellSet(visibleCellIndex[58]),new CellSet(visibleCellIndex[59]),new CellSet(visibleCellIndex[60]),new CellSet(visibleCellIndex[61]),new CellSet(visibleCellIndex[62]),
				new CellSet(visibleCellIndex[63]),new CellSet(visibleCellIndex[64]),new CellSet(visibleCellIndex[65]),new CellSet(visibleCellIndex[66]),new CellSet(visibleCellIndex[67]),new CellSet(visibleCellIndex[68]),new CellSet(visibleCellIndex[69]),new CellSet(visibleCellIndex[70]),new CellSet(visibleCellIndex[71]),
				new CellSet(visibleCellIndex[72]),new CellSet(visibleCellIndex[73]),new CellSet(visibleCellIndex[74]),new CellSet(visibleCellIndex[75]),new CellSet(visibleCellIndex[76]),new CellSet(visibleCellIndex[77]),new CellSet(visibleCellIndex[78]),new CellSet(visibleCellIndex[79]),new CellSet(visibleCellIndex[80])
				};
		forwardVisibleCellsSet = new CellSet[] {
				new CellSet(forwardVisibleCellIndex[0]),new CellSet(forwardVisibleCellIndex[1]),new CellSet(forwardVisibleCellIndex[2]),new CellSet(forwardVisibleCellIndex[3]),new CellSet(forwardVisibleCellIndex[4]),new CellSet(forwardVisibleCellIndex[5]),new CellSet(forwardVisibleCellIndex[6]),new CellSet(forwardVisibleCellIndex[7]),new CellSet(forwardVisibleCellIndex[8]),
				new CellSet(forwardVisibleCellIndex[9]),new CellSet(forwardVisibleCellIndex[10]),new CellSet(forwardVisibleCellIndex[11]),new CellSet(forwardVisibleCellIndex[12]),new CellSet(forwardVisibleCellIndex[13]),new CellSet(forwardVisibleCellIndex[14]),new CellSet(forwardVisibleCellIndex[15]),new CellSet(forwardVisibleCellIndex[16]),new CellSet(forwardVisibleCellIndex[17]),
				new CellSet(forwardVisibleCellIndex[18]),new CellSet(forwardVisibleCellIndex[19]),new CellSet(forwardVisibleCellIndex[20]),new CellSet(forwardVisibleCellIndex[21]),new CellSet(forwardVisibleCellIndex[22]),new CellSet(forwardVisibleCellIndex[23]),new CellSet(forwardVisibleCellIndex[24]),new CellSet(forwardVisibleCellIndex[25]),new CellSet(forwardVisibleCellIndex[26]),
				new CellSet(forwardVisibleCellIndex[27]),new CellSet(forwardVisibleCellIndex[28]),new CellSet(forwardVisibleCellIndex[29]),new CellSet(forwardVisibleCellIndex[30]),new CellSet(forwardVisibleCellIndex[31]),new CellSet(forwardVisibleCellIndex[32]),new CellSet(forwardVisibleCellIndex[33]),new CellSet(forwardVisibleCellIndex[34]),new CellSet(forwardVisibleCellIndex[35]),
				new CellSet(forwardVisibleCellIndex[36]),new CellSet(forwardVisibleCellIndex[37]),new CellSet(forwardVisibleCellIndex[38]),new CellSet(forwardVisibleCellIndex[39]),new CellSet(forwardVisibleCellIndex[40]),new CellSet(forwardVisibleCellIndex[41]),new CellSet(forwardVisibleCellIndex[42]),new CellSet(forwardVisibleCellIndex[43]),new CellSet(forwardVisibleCellIndex[44]),
				new CellSet(forwardVisibleCellIndex[45]),new CellSet(forwardVisibleCellIndex[46]),new CellSet(forwardVisibleCellIndex[47]),new CellSet(forwardVisibleCellIndex[48]),new CellSet(forwardVisibleCellIndex[49]),new CellSet(forwardVisibleCellIndex[50]),new CellSet(forwardVisibleCellIndex[51]),new CellSet(forwardVisibleCellIndex[52]),new CellSet(forwardVisibleCellIndex[53]),
				new CellSet(forwardVisibleCellIndex[54]),new CellSet(forwardVisibleCellIndex[55]),new CellSet(forwardVisibleCellIndex[56]),new CellSet(forwardVisibleCellIndex[57]),new CellSet(forwardVisibleCellIndex[58]),new CellSet(forwardVisibleCellIndex[59]),new CellSet(forwardVisibleCellIndex[60]),new CellSet(forwardVisibleCellIndex[61]),new CellSet(forwardVisibleCellIndex[62]),
				new CellSet(forwardVisibleCellIndex[63]),new CellSet(forwardVisibleCellIndex[64]),new CellSet(forwardVisibleCellIndex[65]),new CellSet(forwardVisibleCellIndex[66]),new CellSet(forwardVisibleCellIndex[67]),new CellSet(forwardVisibleCellIndex[68]),new CellSet(forwardVisibleCellIndex[69]),new CellSet(forwardVisibleCellIndex[70]),new CellSet(forwardVisibleCellIndex[71]),
				new CellSet(forwardVisibleCellIndex[72]),new CellSet(forwardVisibleCellIndex[73]),new CellSet(forwardVisibleCellIndex[74]),new CellSet(forwardVisibleCellIndex[75]),new CellSet(forwardVisibleCellIndex[76]),new CellSet(forwardVisibleCellIndex[77]),new CellSet(forwardVisibleCellIndex[78]),new CellSet(forwardVisibleCellIndex[79]),new CellSet(forwardVisibleCellIndex[80])
				};
		antiVisibleCellsSet = null;
    	blocks = new Block[] {new Block(0), new Block(1), new Block(2), new Block(3), new Block(4), new Block(5), new Block(6), new Block(7), new Block(8)};
    	rows = new Row[] {new Row(0), new Row(1), new Row(2), new Row(3), new Row(4), new Row(5), new Row(6), new Row(7), new Row(8)};
    	columns = new Column[]{new Column(0), new Column(1), new Column(2), new Column(3), new Column(4), new Column(5), new Column(6), new Column(7), new Column(8)}; 
		DGs = new DG[]{new DG(0), new DG(1), new DG(2), new DG(3), new DG(4), new DG(5), new DG(6), new DG(7), new DG(8)};
		windows = new Window[]{new Window(0), new Window(1), new Window(2), new Window(3), new Window(4), new Window(5), new Window(6), new Window(7), new Window(8)};
		girandola = new Girandola[]{new Girandola(0)};
		diagonal1 = new diagonalMain[]{new diagonalMain(0)};
		diagonal2 = new diagonalAnti[]{new diagonalAnti(0)};
		asterisk = new Asterisk[]{new Asterisk(0)};
		cd = new CD[]{new CD(0)};
		regions = new Region[][] {blocks, rows, columns, DGs, windows, diagonal1, diagonal2, girandola, asterisk, cd};
    }

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
			isGiven[i] = false;	// fix #99 - initialize to false
        }
		isSudoku = 1;
    }
    
	public int isSudoku() {
		return this.isSudoku;
	}

	public void setSukaku() {
		this.isSudoku = 0;
	}
	
    //=== Static methods ==============

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

    /**
     * Get the 9 regions of the given type
     * @param regionTypeIndex the type of the regions to return. Must be 0 for
     * {@link Grid.Block}, 1 for {@link Grid.Row}, or 2 for {@link Grid.Column}.
     * @return the 9 regions of the given type
     */
    public static Region[] getRegions(int regionTypeIndex) {
    	return regions[regionTypeIndex];
    }

    /**
     * Get the row at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the row at the given coordinates
     */
    public static Row getRowAt(int x, int y) {
        return Grid.rows[y];
    }

    /**
     * Get the column at the given location
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @return the column at the given location
     */
    public static Column getColumnAt(int x, int y) {
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

    public static Grid.Region getRegionAt(int regionTypeIndex, int cellIndex) {
        return Grid.regions[regionTypeIndex][Grid.cellRegions[cellIndex][regionTypeIndex]];
    }
    //=== Non-static methods ==============

    /**
     * Is the cell a given or not
     * @param index the cell index [0..80]
     */	
		
	private void setGiven(int index) {		// fix #99 - made private
        this.isGiven[index] = true;
    }

    private void resetGiven(int index) {	// fix #99 - made private
        this.isGiven[index] = false;
    }

	public void fixGivens() {				// fix #99 - new method
		for (int i = 0; i < 81; i++) {
			if ( getCellValue(i) != 0 ) {
				this.isGiven[i] = true;
			}
		}
	}

    public boolean isGiven(int index) {
        return this.isGiven[index];
    }

//@SudokuMonster: Static for Variant this has to be called if a variant technique relies on Visible Cells
//					until a better way to de-clutter code is found
//Change visible cells according to variant
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void changeVisibleCells() {
    	if (Settings.getInstance().isBlocks())
    	visibleCellIndex = new int[][] {
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
    	else
			visibleCellIndex = new int[][] {
				{ 1, 2, 3, 4, 5, 6, 7, 8, 9,18,27,36,45,54,63,72},
				{ 0, 2, 3, 4, 5, 6, 7, 8,10,19,28,37,46,55,64,73},
				{ 0, 1, 3, 4, 5, 6, 7, 8,11,20,29,38,47,56,65,74},
				{ 0, 1, 2, 4, 5, 6, 7, 8,12,21,30,39,48,57,66,75},
				{ 0, 1, 2, 3, 5, 6, 7, 8,13,22,31,40,49,58,67,76},
				{ 0, 1, 2, 3, 4, 6, 7, 8,14,23,32,41,50,59,68,77},
				{ 0, 1, 2, 3, 4, 5, 7, 8,15,24,33,42,51,60,69,78},
				{ 0, 1, 2, 3, 4, 5, 6, 8,16,25,34,43,52,61,70,79},
				{ 0, 1, 2, 3, 4, 5, 6, 7,17,26,35,44,53,62,71,80},
				{ 0,10,11,12,13,14,15,16,17,18,27,36,45,54,63,72},
				{ 1, 9,11,12,13,14,15,16,17,19,28,37,46,55,64,73},
				{ 2, 9,10,12,13,14,15,16,17,20,29,38,47,56,65,74},
				{ 3, 9,10,11,13,14,15,16,17,21,30,39,48,57,66,75},
				{ 4, 9,10,11,12,14,15,16,17,22,31,40,49,58,67,76},
				{ 5, 9,10,11,12,13,15,16,17,23,32,41,50,59,68,77},
				{ 6, 9,10,11,12,13,14,16,17,24,33,42,51,60,69,78},
				{ 7, 9,10,11,12,13,14,15,17,25,34,43,52,61,70,79},
				{ 8, 9,10,11,12,13,14,15,16,26,35,44,53,62,71,80},
				{ 0, 9,19,20,21,22,23,24,25,26,27,36,45,54,63,72},
				{ 1,10,18,20,21,22,23,24,25,26,28,37,46,55,64,73},
				{ 2,11,18,19,21,22,23,24,25,26,29,38,47,56,65,74},
				{ 3,12,18,19,20,22,23,24,25,26,30,39,48,57,66,75},
				{ 4,13,18,19,20,21,23,24,25,26,31,40,49,58,67,76},
				{ 5,14,18,19,20,21,22,24,25,26,32,41,50,59,68,77},
				{ 6,15,18,19,20,21,22,23,25,26,33,42,51,60,69,78},
				{ 7,16,18,19,20,21,22,23,24,26,34,43,52,61,70,79},
				{ 8,17,18,19,20,21,22,23,24,25,35,44,53,62,71,80},
				{ 0, 9,18,28,29,30,31,32,33,34,35,36,45,54,63,72},
				{ 1,10,19,27,29,30,31,32,33,34,35,37,46,55,64,73},
				{ 2,11,20,27,28,30,31,32,33,34,35,38,47,56,65,74},
				{ 3,12,21,27,28,29,31,32,33,34,35,39,48,57,66,75},
				{ 4,13,22,27,28,29,30,32,33,34,35,40,49,58,67,76},
				{ 5,14,23,27,28,29,30,31,33,34,35,41,50,59,68,77},
				{ 6,15,24,27,28,29,30,31,32,34,35,42,51,60,69,78},
				{ 7,16,25,27,28,29,30,31,32,33,35,43,52,61,70,79},
				{ 8,17,26,27,28,29,30,31,32,33,34,44,53,62,71,80},
				{ 0, 9,18,27,37,38,39,40,41,42,43,44,45,54,63,72},
				{ 1,10,19,28,36,38,39,40,41,42,43,44,46,55,64,73},
				{ 2,11,20,29,36,37,39,40,41,42,43,44,47,56,65,74},
				{ 3,12,21,30,36,37,38,40,41,42,43,44,48,57,66,75},
				{ 4,13,22,31,36,37,38,39,41,42,43,44,49,58,67,76},
				{ 5,14,23,32,36,37,38,39,40,42,43,44,50,59,68,77},
				{ 6,15,24,33,36,37,38,39,40,41,43,44,51,60,69,78},
				{ 7,16,25,34,36,37,38,39,40,41,42,44,52,61,70,79},
				{ 8,17,26,35,36,37,38,39,40,41,42,43,53,62,71,80},
				{ 0, 9,18,27,36,46,47,48,49,50,51,52,53,54,63,72},
				{ 1,10,19,28,37,45,47,48,49,50,51,52,53,55,64,73},
				{ 2,11,20,29,38,45,46,48,49,50,51,52,53,56,65,74},
				{ 3,12,21,30,39,45,46,47,49,50,51,52,53,57,66,75},
				{ 4,13,22,31,40,45,46,47,48,50,51,52,53,58,67,76},
				{ 5,14,23,32,41,45,46,47,48,49,51,52,53,59,68,77},
				{ 6,15,24,33,42,45,46,47,48,49,50,52,53,60,69,78},
				{ 7,16,25,34,43,45,46,47,48,49,50,51,53,61,70,79},
				{ 8,17,26,35,44,45,46,47,48,49,50,51,52,62,71,80},
				{ 0, 9,18,27,36,45,55,56,57,58,59,60,61,62,63,72},
				{ 1,10,19,28,37,46,54,56,57,58,59,60,61,62,64,73},
				{ 2,11,20,29,38,47,54,55,57,58,59,60,61,62,65,74},
				{ 3,12,21,30,39,48,54,55,56,58,59,60,61,62,66,75},
				{ 4,13,22,31,40,49,54,55,56,57,59,60,61,62,67,76},
				{ 5,14,23,32,41,50,54,55,56,57,58,60,61,62,68,77},
				{ 6,15,24,33,42,51,54,55,56,57,58,59,61,62,69,78},
				{ 7,16,25,34,43,52,54,55,56,57,58,59,60,62,70,79},
				{ 8,17,26,35,44,53,54,55,56,57,58,59,60,61,71,80},
				{ 0, 9,18,27,36,45,54,64,65,66,67,68,69,70,71,72},
				{ 1,10,19,28,37,46,55,63,65,66,67,68,69,70,71,73},
				{ 2,11,20,29,38,47,56,63,64,66,67,68,69,70,71,74},
				{ 3,12,21,30,39,48,57,63,64,65,67,68,69,70,71,75},
				{ 4,13,22,31,40,49,58,63,64,65,66,68,69,70,71,76},
				{ 5,14,23,32,41,50,59,63,64,65,66,67,69,70,71,77},
				{ 6,15,24,33,42,51,60,63,64,65,66,67,68,70,71,78},
				{ 7,16,25,34,43,52,61,63,64,65,66,67,68,69,71,79},
				{ 8,17,26,35,44,53,62,63,64,65,66,67,68,69,70,80},
				{ 0, 9,18,27,36,45,54,63,73,74,75,76,77,78,79,80},
				{ 1,10,19,28,37,46,55,64,72,74,75,76,77,78,79,80},
				{ 2,11,20,29,38,47,56,65,72,73,75,76,77,78,79,80},
				{ 3,12,21,30,39,48,57,66,72,73,74,76,77,78,79,80},
				{ 4,13,22,31,40,49,58,67,72,73,74,75,77,78,79,80},
				{ 5,14,23,32,41,50,59,68,72,73,74,75,76,78,79,80},
				{ 6,15,24,33,42,51,60,69,72,73,74,75,76,77,79,80},
				{ 7,16,25,34,43,52,61,70,72,73,74,75,76,77,78,80},
				{ 8,17,26,35,44,53,62,71,72,73,74,75,76,77,78,79}
			};
		if (Settings.getInstance().isBlocks())	
		forwardVisibleCellIndex = new int [][] {
			{1,2,3,4,5,6,7,8,9,10,11,18,19,20,27,36,45,54,63,72},
			{2,3,4,5,6,7,8,9,10,11,18,19,20,28,37,46,55,64,73},
			{3,4,5,6,7,8,9,10,11,18,19,20,29,38,47,56,65,74},
			{4,5,6,7,8,12,13,14,21,22,23,30,39,48,57,66,75},
			{5,6,7,8,12,13,14,21,22,23,31,40,49,58,67,76},
			{6,7,8,12,13,14,21,22,23,32,41,50,59,68,77},
			{7,8,15,16,17,24,25,26,33,42,51,60,69,78},
			{8,15,16,17,24,25,26,34,43,52,61,70,79},
			{15,16,17,24,25,26,35,44,53,62,71,80},
			{10,11,12,13,14,15,16,17,18,19,20,27,36,45,54,63,72},
			{11,12,13,14,15,16,17,18,19,20,28,37,46,55,64,73},
			{12,13,14,15,16,17,18,19,20,29,38,47,56,65,74},
			{13,14,15,16,17,21,22,23,30,39,48,57,66,75},
			{14,15,16,17,21,22,23,31,40,49,58,67,76},
			{15,16,17,21,22,23,32,41,50,59,68,77},
			{16,17,24,25,26,33,42,51,60,69,78},
			{17,24,25,26,34,43,52,61,70,79},
			{24,25,26,35,44,53,62,71,80},
			{19,20,21,22,23,24,25,26,27,36,45,54,63,72},
			{20,21,22,23,24,25,26,28,37,46,55,64,73},
			{21,22,23,24,25,26,29,38,47,56,65,74},
			{22,23,24,25,26,30,39,48,57,66,75},
			{23,24,25,26,31,40,49,58,67,76},
			{24,25,26,32,41,50,59,68,77},
			{25,26,33,42,51,60,69,78},
			{26,34,43,52,61,70,79},
			{35,44,53,62,71,80},
			{28,29,30,31,32,33,34,35,36,37,38,45,46,47,54,63,72},
			{29,30,31,32,33,34,35,36,37,38,45,46,47,55,64,73},
			{30,31,32,33,34,35,36,37,38,45,46,47,56,65,74},
			{31,32,33,34,35,39,40,41,48,49,50,57,66,75},
			{32,33,34,35,39,40,41,48,49,50,58,67,76},
			{33,34,35,39,40,41,48,49,50,59,68,77},
			{34,35,42,43,44,51,52,53,60,69,78},
			{35,42,43,44,51,52,53,61,70,79},
			{42,43,44,51,52,53,62,71,80},
			{37,38,39,40,41,42,43,44,45,46,47,54,63,72},
			{38,39,40,41,42,43,44,45,46,47,55,64,73},
			{39,40,41,42,43,44,45,46,47,56,65,74},
			{40,41,42,43,44,48,49,50,57,66,75},
			{41,42,43,44,48,49,50,58,67,76},
			{42,43,44,48,49,50,59,68,77},
			{43,44,51,52,53,60,69,78},
			{44,51,52,53,61,70,79},
			{51,52,53,62,71,80},
			{46,47,48,49,50,51,52,53,54,63,72},
			{47,48,49,50,51,52,53,55,64,73},
			{48,49,50,51,52,53,56,65,74},
			{49,50,51,52,53,57,66,75},
			{50,51,52,53,58,67,76},
			{51,52,53,59,68,77},
			{52,53,60,69,78},
			{53,61,70,79},
			{62,71,80},
			{55,56,57,58,59,60,61,62,63,64,65,72,73,74},
			{56,57,58,59,60,61,62,63,64,65,72,73,74},
			{57,58,59,60,61,62,63,64,65,72,73,74},
			{58,59,60,61,62,66,67,68,75,76,77},
			{59,60,61,62,66,67,68,75,76,77},
			{60,61,62,66,67,68,75,76,77},
			{61,62,69,70,71,78,79,80},
			{62,69,70,71,78,79,80},
			{69,70,71,78,79,80},
			{64,65,66,67,68,69,70,71,72,73,74},
			{65,66,67,68,69,70,71,72,73,74},
			{66,67,68,69,70,71,72,73,74},
			{67,68,69,70,71,75,76,77},
			{68,69,70,71,75,76,77},
			{69,70,71,75,76,77},
			{70,71,78,79,80},
			{71,78,79,80},
			{78,79,80},
			{73,74,75,76,77,78,79,80},
			{74,75,76,77,78,79,80},
			{75,76,77,78,79,80},
			{76,77,78,79,80},
			{77,78,79,80},
			{78,79,80},
			{79,80},
			{80},
			{}
			};
    	else
			forwardVisibleCellIndex = new int [][] {
				{1,2,3,4,5,6,7,8,9,18,27,36,45,54,63,72},
				{2,3,4,5,6,7,8,10,19,28,37,46,55,64,73},
				{3,4,5,6,7,8,11,20,29,38,47,56,65,74},
				{4,5,6,7,8,12,21,30,39,48,57,66,75},
				{5,6,7,8,13,22,31,40,49,58,67,76},
				{6,7,8,14,23,32,41,50,59,68,77},
				{7,8,15,24,33,42,51,60,69,78},
				{8,16,25,34,43,52,61,70,79},
				{17,26,35,44,53,62,71,80},
				{10,11,12,13,14,15,16,17,18,27,36,45,54,63,72},
				{11,12,13,14,15,16,17,19,28,37,46,55,64,73},
				{12,13,14,15,16,17,20,29,38,47,56,65,74},
				{13,14,15,16,17,21,30,39,48,57,66,75},
				{14,15,16,17,22,31,40,49,58,67,76},
				{15,16,17,23,32,41,50,59,68,77},
				{16,17,24,33,42,51,60,69,78},
				{17,25,34,43,52,61,70,79},
				{26,35,44,53,62,71,80},
				{19,20,21,22,23,24,25,26,27,36,45,54,63,72},
				{20,21,22,23,24,25,26,28,37,46,55,64,73},
				{21,22,23,24,25,26,29,38,47,56,65,74},
				{22,23,24,25,26,30,39,48,57,66,75},
				{23,24,25,26,31,40,49,58,67,76},
				{24,25,26,32,41,50,59,68,77},
				{25,26,33,42,51,60,69,78},
				{26,34,43,52,61,70,79},
				{35,44,53,62,71,80},
				{28,29,30,31,32,33,34,35,36,45,54,63,72},
				{29,30,31,32,33,34,35,37,46,55,64,73},
				{30,31,32,33,34,35,38,47,56,65,74},
				{31,32,33,34,35,39,48,57,66,75},
				{32,33,34,35,40,49,58,67,76},
				{33,34,35,41,50,59,68,77},
				{34,35,42,51,60,69,78},
				{35,43,52,61,70,79},
				{44,53,62,71,80},
				{37,38,39,40,41,42,43,44,45,54,63,72},
				{38,39,40,41,42,43,44,46,55,64,73},
				{39,40,41,42,43,44,47,56,65,74},
				{40,41,42,43,44,48,57,66,75},
				{41,42,43,44,49,58,67,76},
				{42,43,44,50,59,68,77},
				{43,44,51,60,69,78},
				{44,52,61,70,79},
				{53,62,71,80},
				{46,47,48,49,50,51,52,53,54,63,72},
				{47,48,49,50,51,52,53,55,64,73},
				{48,49,50,51,52,53,56,65,74},
				{49,50,51,52,53,57,66,75},
				{50,51,52,53,58,67,76},
				{51,52,53,59,68,77},
				{52,53,60,69,78},
				{53,61,70,79},
				{62,71,80},
				{55,56,57,58,59,60,61,62,63,72},
				{56,57,58,59,60,61,62,64,73},
				{57,58,59,60,61,62,65,74},
				{58,59,60,61,62,66,75},
				{59,60,61,62,67,76},
				{60,61,62,68,77},
				{61,62,69,78},
				{62,70,79},
				{71,80},
				{64,65,66,67,68,69,70,71,72},
				{65,66,67,68,69,70,71,73},
				{66,67,68,69,70,71,74},
				{67,68,69,70,71,75},
				{68,69,70,71,76},
				{69,70,71,77},
				{70,71,78},
				{71,79},
				{80},
				{73,74,75,76,77,78,79,80},
				{74,75,76,77,78,79,80},
				{75,76,77,78,79,80},
				{76,77,78,79,80},
				{77,78,79,80},
				{78,79,80},
				{79,80},
				{80},
				{}
			};
		//@SudokuMonster Changining Static visibleCellIndex & visibleCellsSet with variants
		//To change the least possible what @dobrichev has changed with cellSets
		//Eperimental area to change visible cell index
		//The base is either Sudoku or Latin Square already determined above
		//What remains are additions
		//Next 10 lines must be before any additions done
		int visibilityMax = 0;
		ArrayList<ArrayList<Integer>> tempList1 = new ArrayList();
		ArrayList<ArrayList<Integer>> tempList2 = new ArrayList();		
		ArrayList<ArrayList<Integer>> tempList3 = new ArrayList();		
		for (int i = 0; i < 81; i++) {
			ArrayList<Integer> list1 = new ArrayList();
			ArrayList<Integer> list2 = new ArrayList();
			ArrayList<Integer> list3 = new ArrayList();
			for (int j = 0; j < visibleCellIndex[i].length; j++)
				list1.add(visibleCellIndex[i][j]);
			for (int j = 0; j < forwardVisibleCellIndex[i].length; j++)
				list2.add(forwardVisibleCellIndex[i][j]);			
		//above 10 lines must be before any additions done	
			if (Settings.getInstance().isWindows())//Windows constraints
				for (int j = 0; j < windowsVisibleCellIndex[i].length; j++)
					if (windowsVisibleCellIndex[i][j] != i && list1.indexOf(windowsVisibleCellIndex[i][j]) < 0) {
						list1.add(windowsVisibleCellIndex[i][j]);
						if (windowsVisibleCellIndex[i][j] > i)
							list2.add(windowsVisibleCellIndex[i][j]);
					}
			if (Settings.getInstance().isDG())//DG constraints
				for (int j = 0; j < DGVisibleCellIndex[i].length; j++)
					if (DGVisibleCellIndex[i][j] != i && list1.indexOf(DGVisibleCellIndex[i][j]) < 0) {
						list1.add(DGVisibleCellIndex[i][j]);
						if (DGVisibleCellIndex[i][j] > i)
							list2.add(DGVisibleCellIndex[i][j]);
					}
			if (Settings.getInstance().isX())//X constraints
				for (int j = 0; j < XVisibleCellIndex[i].length; j++)
					if (XVisibleCellIndex[i][j] != i && list1.indexOf(XVisibleCellIndex[i][j]) < 0) {
						list1.add(XVisibleCellIndex[i][j]);
						if (XVisibleCellIndex[i][j] > i)
							list2.add(XVisibleCellIndex[i][j]);
					}
			if (Settings.getInstance().isCD())//CD constraints
				for (int j = 0; j < CDVisibleCellIndex[i].length; j++)
					if (CDVisibleCellIndex[i][j] != i && list1.indexOf(CDVisibleCellIndex[i][j]) < 0) {
						list1.add(CDVisibleCellIndex[i][j]);
						if (CDVisibleCellIndex[i][j] > i)
							list2.add(CDVisibleCellIndex[i][j]);
					}
			if (Settings.getInstance().isGirandola())//girandola constraints
				for (int j = 0; j < girandolaVisibleCellIndex[i].length; j++)
					if (girandolaVisibleCellIndex[i][j] != i && list1.indexOf(girandolaVisibleCellIndex[i][j]) < 0) {
						list1.add(girandolaVisibleCellIndex[i][j]);
						if (girandolaVisibleCellIndex[i][j] > i)
							list2.add(girandolaVisibleCellIndex[i][j]);
					}
			if (Settings.getInstance().isAsterisk())//asterisk constraints
				for (int j = 0; j < asteriskVisibleCellIndex[i].length; j++)
					if (asteriskVisibleCellIndex[i][j] != i && list1.indexOf(asteriskVisibleCellIndex[i][j]) < 0) {
						list1.add(asteriskVisibleCellIndex[i][j]);
						if (asteriskVisibleCellIndex[i][j] > i)
							list2.add(asteriskVisibleCellIndex[i][j]);
					}
			if (Settings.getInstance().isAntiFerz())//Anti Ferz (0,1)
				for (int j = 0; j < ferzCellIndex.length; j++) { 
					boolean isOutsideBoardX = (i / 9 + ferzCellIndex[j][0]) < 0 || (i / 9 + ferzCellIndex[j][0]) > 8;
					boolean isOutsideBoardY = (i % 9 + ferzCellIndex[j][1]) < 0 || (i % 9 + ferzCellIndex[j][1]) > 8;
					int correctedX = (i / 9 + ferzCellIndex[j][0]) < 0 ? 9 + (i / 9 + ferzCellIndex[j][0]): (i / 9 + ferzCellIndex[j][0]) % 9;
					int correctedY = (i % 9 + ferzCellIndex[j][1]) < 0 ? 9 + (i % 9 + ferzCellIndex[j][1]): (i % 9 + ferzCellIndex[j][1]) % 9;
					int leapCellIndex = correctedX * 9 + correctedY;
					if (Settings.getInstance().isToroidal())  {
						if (leapCellIndex != i && list1.indexOf(leapCellIndex) < 0) {
							list1.add(leapCellIndex);
							list3.add(leapCellIndex);
							if (leapCellIndex > i)
								list2.add(leapCellIndex);
						}
					}
					else
						if (!isOutsideBoardX && !isOutsideBoardY) {
							if (leapCellIndex != i && list1.indexOf(leapCellIndex) < 0) {
								list1.add(leapCellIndex);
								list3.add(leapCellIndex);
								if (leapCellIndex > i)
									list2.add(leapCellIndex);
							}		
						}	
				}
			if (Settings.getInstance().isAntiKnight())//Anti Knight (1,2)
				for (int j = 0; j < knightCellIndex.length; j++) { 
					boolean isOutsideBoardX = (i / 9 + knightCellIndex[j][0]) < 0 || (i / 9 + knightCellIndex[j][0]) > 8;
					boolean isOutsideBoardY = (i % 9 + knightCellIndex[j][1]) < 0 || (i % 9 + knightCellIndex[j][1]) > 8;
					int correctedX = (i / 9 + knightCellIndex[j][0]) < 0 ? 9 + (i / 9 + knightCellIndex[j][0]) : (i / 9 + knightCellIndex[j][0]) % 9;
					int correctedY = (i % 9 + knightCellIndex[j][1]) < 0 ? 9 + (i % 9 + knightCellIndex[j][1]) : (i % 9 + knightCellIndex[j][1]) % 9;
					int leapCellIndex = correctedX * 9 + correctedY;
					if (Settings.getInstance().isToroidal())  {
						if (leapCellIndex != i && list1.indexOf(leapCellIndex) < 0) {
							list1.add(leapCellIndex);
							list3.add(leapCellIndex);
							if (leapCellIndex > i)
								list2.add(leapCellIndex);
						}	
					}
					else
						if (!isOutsideBoardX && !isOutsideBoardY) {
							if (leapCellIndex != i && list1.indexOf(leapCellIndex) < 0) {
								list1.add(leapCellIndex);
								list3.add(leapCellIndex);
								if (leapCellIndex > i)
									list2.add(leapCellIndex);
							}
						}	
				}
		//The following 3 lines need to be at bottom of loop
			visibilityMax = visibilityMax > list1.size() ? visibilityMax : list1.size();
			tempList1.add(list1);
			tempList2.add(list2);
			if (Settings.getInstance().isAntiKnight() || Settings.getInstance().isAntiFerz())
				tempList3.add(list3);			
		}
		//@SudokuMonster: recreating array from ArrayList
				visibleCellIndex = new int[81][visibilityMax];
					for (int i = 0; i < 81; i++){
						visibleCellIndex [i] = new int[tempList1.get(i).size()];
						forwardVisibleCellIndex [i] = new int[tempList2.get(i).size()];
						if (Settings.getInstance().isAntiKnight() || Settings.getInstance().isAntiFerz())
							antiVisibleCellIndex [i] = new int[tempList3.get(i).size()];
						for (int j = 0; j < tempList1.get(i).size(); j++) {
							visibleCellIndex [i][j] = tempList1.get(i).get(j);
							if (Settings.getInstance().isAntiKnight() || Settings.getInstance().isAntiFerz())
								if (j < tempList3.get(i).size())
									antiVisibleCellIndex [i][j] = tempList3.get(i).get(j);
							if (j < tempList2.get(i).size())
								forwardVisibleCellIndex [i][j] = tempList2.get(i).get(j);
						}
					}
    	visibleCellsSet = new CellSet[] {
				new CellSet(visibleCellIndex[0]),new CellSet(visibleCellIndex[1]),new CellSet(visibleCellIndex[2]),new CellSet(visibleCellIndex[3]),new CellSet(visibleCellIndex[4]),new CellSet(visibleCellIndex[5]),new CellSet(visibleCellIndex[6]),new CellSet(visibleCellIndex[7]),new CellSet(visibleCellIndex[8]),
				new CellSet(visibleCellIndex[9]),new CellSet(visibleCellIndex[10]),new CellSet(visibleCellIndex[11]),new CellSet(visibleCellIndex[12]),new CellSet(visibleCellIndex[13]),new CellSet(visibleCellIndex[14]),new CellSet(visibleCellIndex[15]),new CellSet(visibleCellIndex[16]),new CellSet(visibleCellIndex[17]),
				new CellSet(visibleCellIndex[18]),new CellSet(visibleCellIndex[19]),new CellSet(visibleCellIndex[20]),new CellSet(visibleCellIndex[21]),new CellSet(visibleCellIndex[22]),new CellSet(visibleCellIndex[23]),new CellSet(visibleCellIndex[24]),new CellSet(visibleCellIndex[25]),new CellSet(visibleCellIndex[26]),
				new CellSet(visibleCellIndex[27]),new CellSet(visibleCellIndex[28]),new CellSet(visibleCellIndex[29]),new CellSet(visibleCellIndex[30]),new CellSet(visibleCellIndex[31]),new CellSet(visibleCellIndex[32]),new CellSet(visibleCellIndex[33]),new CellSet(visibleCellIndex[34]),new CellSet(visibleCellIndex[35]),
				new CellSet(visibleCellIndex[36]),new CellSet(visibleCellIndex[37]),new CellSet(visibleCellIndex[38]),new CellSet(visibleCellIndex[39]),new CellSet(visibleCellIndex[40]),new CellSet(visibleCellIndex[41]),new CellSet(visibleCellIndex[42]),new CellSet(visibleCellIndex[43]),new CellSet(visibleCellIndex[44]),
				new CellSet(visibleCellIndex[45]),new CellSet(visibleCellIndex[46]),new CellSet(visibleCellIndex[47]),new CellSet(visibleCellIndex[48]),new CellSet(visibleCellIndex[49]),new CellSet(visibleCellIndex[50]),new CellSet(visibleCellIndex[51]),new CellSet(visibleCellIndex[52]),new CellSet(visibleCellIndex[53]),
				new CellSet(visibleCellIndex[54]),new CellSet(visibleCellIndex[55]),new CellSet(visibleCellIndex[56]),new CellSet(visibleCellIndex[57]),new CellSet(visibleCellIndex[58]),new CellSet(visibleCellIndex[59]),new CellSet(visibleCellIndex[60]),new CellSet(visibleCellIndex[61]),new CellSet(visibleCellIndex[62]),
				new CellSet(visibleCellIndex[63]),new CellSet(visibleCellIndex[64]),new CellSet(visibleCellIndex[65]),new CellSet(visibleCellIndex[66]),new CellSet(visibleCellIndex[67]),new CellSet(visibleCellIndex[68]),new CellSet(visibleCellIndex[69]),new CellSet(visibleCellIndex[70]),new CellSet(visibleCellIndex[71]),
				new CellSet(visibleCellIndex[72]),new CellSet(visibleCellIndex[73]),new CellSet(visibleCellIndex[74]),new CellSet(visibleCellIndex[75]),new CellSet(visibleCellIndex[76]),new CellSet(visibleCellIndex[77]),new CellSet(visibleCellIndex[78]),new CellSet(visibleCellIndex[79]),new CellSet(visibleCellIndex[80])
				};
		forwardVisibleCellsSet = new CellSet[] {
				new CellSet(forwardVisibleCellIndex[0]),new CellSet(forwardVisibleCellIndex[1]),new CellSet(forwardVisibleCellIndex[2]),new CellSet(forwardVisibleCellIndex[3]),new CellSet(forwardVisibleCellIndex[4]),new CellSet(forwardVisibleCellIndex[5]),new CellSet(forwardVisibleCellIndex[6]),new CellSet(forwardVisibleCellIndex[7]),new CellSet(forwardVisibleCellIndex[8]),
				new CellSet(forwardVisibleCellIndex[9]),new CellSet(forwardVisibleCellIndex[10]),new CellSet(forwardVisibleCellIndex[11]),new CellSet(forwardVisibleCellIndex[12]),new CellSet(forwardVisibleCellIndex[13]),new CellSet(forwardVisibleCellIndex[14]),new CellSet(forwardVisibleCellIndex[15]),new CellSet(forwardVisibleCellIndex[16]),new CellSet(forwardVisibleCellIndex[17]),
				new CellSet(forwardVisibleCellIndex[18]),new CellSet(forwardVisibleCellIndex[19]),new CellSet(forwardVisibleCellIndex[20]),new CellSet(forwardVisibleCellIndex[21]),new CellSet(forwardVisibleCellIndex[22]),new CellSet(forwardVisibleCellIndex[23]),new CellSet(forwardVisibleCellIndex[24]),new CellSet(forwardVisibleCellIndex[25]),new CellSet(forwardVisibleCellIndex[26]),
				new CellSet(forwardVisibleCellIndex[27]),new CellSet(forwardVisibleCellIndex[28]),new CellSet(forwardVisibleCellIndex[29]),new CellSet(forwardVisibleCellIndex[30]),new CellSet(forwardVisibleCellIndex[31]),new CellSet(forwardVisibleCellIndex[32]),new CellSet(forwardVisibleCellIndex[33]),new CellSet(forwardVisibleCellIndex[34]),new CellSet(forwardVisibleCellIndex[35]),
				new CellSet(forwardVisibleCellIndex[36]),new CellSet(forwardVisibleCellIndex[37]),new CellSet(forwardVisibleCellIndex[38]),new CellSet(forwardVisibleCellIndex[39]),new CellSet(forwardVisibleCellIndex[40]),new CellSet(forwardVisibleCellIndex[41]),new CellSet(forwardVisibleCellIndex[42]),new CellSet(forwardVisibleCellIndex[43]),new CellSet(forwardVisibleCellIndex[44]),
				new CellSet(forwardVisibleCellIndex[45]),new CellSet(forwardVisibleCellIndex[46]),new CellSet(forwardVisibleCellIndex[47]),new CellSet(forwardVisibleCellIndex[48]),new CellSet(forwardVisibleCellIndex[49]),new CellSet(forwardVisibleCellIndex[50]),new CellSet(forwardVisibleCellIndex[51]),new CellSet(forwardVisibleCellIndex[52]),new CellSet(forwardVisibleCellIndex[53]),
				new CellSet(forwardVisibleCellIndex[54]),new CellSet(forwardVisibleCellIndex[55]),new CellSet(forwardVisibleCellIndex[56]),new CellSet(forwardVisibleCellIndex[57]),new CellSet(forwardVisibleCellIndex[58]),new CellSet(forwardVisibleCellIndex[59]),new CellSet(forwardVisibleCellIndex[60]),new CellSet(forwardVisibleCellIndex[61]),new CellSet(forwardVisibleCellIndex[62]),
				new CellSet(forwardVisibleCellIndex[63]),new CellSet(forwardVisibleCellIndex[64]),new CellSet(forwardVisibleCellIndex[65]),new CellSet(forwardVisibleCellIndex[66]),new CellSet(forwardVisibleCellIndex[67]),new CellSet(forwardVisibleCellIndex[68]),new CellSet(forwardVisibleCellIndex[69]),new CellSet(forwardVisibleCellIndex[70]),new CellSet(forwardVisibleCellIndex[71]),
				new CellSet(forwardVisibleCellIndex[72]),new CellSet(forwardVisibleCellIndex[73]),new CellSet(forwardVisibleCellIndex[74]),new CellSet(forwardVisibleCellIndex[75]),new CellSet(forwardVisibleCellIndex[76]),new CellSet(forwardVisibleCellIndex[77]),new CellSet(forwardVisibleCellIndex[78]),new CellSet(forwardVisibleCellIndex[79]),new CellSet(forwardVisibleCellIndex[80])
				};
		if (Settings.getInstance().isAntiKnight() || Settings.getInstance().isAntiFerz())
			antiVisibleCellsSet = new CellSet[] {
					new CellSet(antiVisibleCellIndex[0]),new CellSet(antiVisibleCellIndex[1]),new CellSet(antiVisibleCellIndex[2]),new CellSet(antiVisibleCellIndex[3]),new CellSet(antiVisibleCellIndex[4]),new CellSet(antiVisibleCellIndex[5]),new CellSet(antiVisibleCellIndex[6]),new CellSet(antiVisibleCellIndex[7]),new CellSet(antiVisibleCellIndex[8]),
					new CellSet(antiVisibleCellIndex[9]),new CellSet(antiVisibleCellIndex[10]),new CellSet(antiVisibleCellIndex[11]),new CellSet(antiVisibleCellIndex[12]),new CellSet(antiVisibleCellIndex[13]),new CellSet(antiVisibleCellIndex[14]),new CellSet(antiVisibleCellIndex[15]),new CellSet(antiVisibleCellIndex[16]),new CellSet(antiVisibleCellIndex[17]),
					new CellSet(antiVisibleCellIndex[18]),new CellSet(antiVisibleCellIndex[19]),new CellSet(antiVisibleCellIndex[20]),new CellSet(antiVisibleCellIndex[21]),new CellSet(antiVisibleCellIndex[22]),new CellSet(antiVisibleCellIndex[23]),new CellSet(antiVisibleCellIndex[24]),new CellSet(antiVisibleCellIndex[25]),new CellSet(antiVisibleCellIndex[26]),
					new CellSet(antiVisibleCellIndex[27]),new CellSet(antiVisibleCellIndex[28]),new CellSet(antiVisibleCellIndex[29]),new CellSet(antiVisibleCellIndex[30]),new CellSet(antiVisibleCellIndex[31]),new CellSet(antiVisibleCellIndex[32]),new CellSet(antiVisibleCellIndex[33]),new CellSet(antiVisibleCellIndex[34]),new CellSet(antiVisibleCellIndex[35]),
					new CellSet(antiVisibleCellIndex[36]),new CellSet(antiVisibleCellIndex[37]),new CellSet(antiVisibleCellIndex[38]),new CellSet(antiVisibleCellIndex[39]),new CellSet(antiVisibleCellIndex[40]),new CellSet(antiVisibleCellIndex[41]),new CellSet(antiVisibleCellIndex[42]),new CellSet(antiVisibleCellIndex[43]),new CellSet(antiVisibleCellIndex[44]),
					new CellSet(antiVisibleCellIndex[45]),new CellSet(antiVisibleCellIndex[46]),new CellSet(antiVisibleCellIndex[47]),new CellSet(antiVisibleCellIndex[48]),new CellSet(antiVisibleCellIndex[49]),new CellSet(antiVisibleCellIndex[50]),new CellSet(antiVisibleCellIndex[51]),new CellSet(antiVisibleCellIndex[52]),new CellSet(antiVisibleCellIndex[53]),
					new CellSet(antiVisibleCellIndex[54]),new CellSet(antiVisibleCellIndex[55]),new CellSet(antiVisibleCellIndex[56]),new CellSet(antiVisibleCellIndex[57]),new CellSet(antiVisibleCellIndex[58]),new CellSet(antiVisibleCellIndex[59]),new CellSet(antiVisibleCellIndex[60]),new CellSet(antiVisibleCellIndex[61]),new CellSet(antiVisibleCellIndex[62]),
					new CellSet(antiVisibleCellIndex[63]),new CellSet(antiVisibleCellIndex[64]),new CellSet(antiVisibleCellIndex[65]),new CellSet(antiVisibleCellIndex[66]),new CellSet(antiVisibleCellIndex[67]),new CellSet(antiVisibleCellIndex[68]),new CellSet(antiVisibleCellIndex[69]),new CellSet(antiVisibleCellIndex[70]),new CellSet(antiVisibleCellIndex[71]),
					new CellSet(antiVisibleCellIndex[72]),new CellSet(antiVisibleCellIndex[73]),new CellSet(antiVisibleCellIndex[74]),new CellSet(antiVisibleCellIndex[75]),new CellSet(antiVisibleCellIndex[76]),new CellSet(antiVisibleCellIndex[77]),new CellSet(antiVisibleCellIndex[78]),new CellSet(antiVisibleCellIndex[79]),new CellSet(antiVisibleCellIndex[80])
					};
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
     * @param cellIndex the cell index [0 .. 80]
     * @param value the value to add, between 1 and 9, inclusive
     */
    public void addCellPotentialValue(int cellIndex, int value) {
//        if(cellPotentialValues[cellIndex].get(value)) return; //no change (doesn't improve, 32382541 -> 32382541)
        cellPotentialValues[cellIndex].set(value);
        //valueCellsCache.invalidateCellValue(cellIndex, value);
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
        //valueCellsCache.invalidateCellValue(cellIndex, value);
        //numCellPencilmarksUpdate++;
    }

    /**
     * Removes at once several potential values of the given cell.
     * @param cellIndex the cell index [0 .. 80]
     * @param valuesToRemove bitset with values to remove
     */
    public void removeCellPotentialValues(int cellIndex, BitSet valuesToRemove) {
    	//BitSet cl = new BitSet();
    	//cl.or(cellPotentialValues[cellIndex]);
    	//cl.and(valuesToRemove);
    	//if(cl.isEmpty()) return; //no change (doesn't improve, 32380479 -> 32380479)
        cellPotentialValues[cellIndex].andNot(valuesToRemove);
        //valueCellsCache.invalidateCell(cellIndex);
        //numCellPencilmarksUpdate++;
    }

    /**
     * Clears the potential values of the given cell.
     * @param cellIndex the cell index [0 .. 80]
     */
    public void clearCellPotentialValues(int cellIndex) {
        //if(cellPotentialValues[cellIndex].isEmpty()) return; //no change (doesn't improve, 32380479 -> 32380479)
        cellPotentialValues[cellIndex].clear();
        //valueCellsCache.invalidateCell(cellIndex);
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
        int[] visible = Grid.visibleCellIndex[target.getIndex()];
        for(int i = 0; i < (Settings.getInstance().isBlocks() ? 20 : 16); i++) {
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
			if (this.isGiven(i))
				other.setGiven(i);
			else
				other.resetGiven(i);
        }
//        //clone the cache as well
//        for(int regionType = 0; regionType < 3; regionType++) {
//            for(int region = 0; region < 9; region++) {
//	            for(int cell = 0; cell < 9; cell++) {
//	            	BitSet cache = other.valueCellsCache.valuePotentialCells[regionType][region][cell];
//	            	cache.clear();
//	            	cache.or(valueCellsCache.valuePotentialCells[regionType][region][cell]);
//	            }
//            }
//        }
    }

    public boolean isSolved() {
    	for(int i = 0; i < 81; i++) {
    		if(cellValues[i] == 0) return false;
    	}
        return true;
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
     * Get a string representation of this grid81 with Jigsaw region. If No Jigsaw then Variants region (except DG & X).
	 If not then No regions at all by @SudokuMonster
     */
    
    public String toStringVariantGrid() {
		int[] regionArray = new int[81];
		if (Settings.getInstance().isWindows())
			regionArray = Arrays.copyOf(Settings.regionsWindows, 81);
		else
			if (Settings.getInstance().isX())
				regionArray = Arrays.copyOf(Settings.regionsBothDiagonals, 81);	
			else
				if (Settings.getInstance().isAsterisk())
					regionArray = Arrays.copyOf(Settings.regionsAsterisk, 81);
				else
					if (Settings.getInstance().isCD())
						regionArray = Arrays.copyOf(Settings.regionsCD, 81);
					else
						if (Settings.getInstance().isGirandola())
							regionArray = Arrays.copyOf(Settings.regionsGirandola, 81);
						else
							if (Settings.getInstance().isDG())
								regionArray = Arrays.copyOf(Settings.regionsDG, 81);
							else
								regionArray = Arrays.copyOf(Settings.regionsNoVariants, 81);
        StringBuilder result = new StringBuilder();
        int xp = 0;
		int yp = 0;
		for (int y = 0; y < 19; y++) {
			yp = y%2;
            for (int x = 0; x < 19; x++) {
				xp = x%2;
				if (y == 0) {
					if (x == 0 || x == 18 || (xp == 0 && regionArray[( y / 2 ) * 9 + x / 2 - 1] != regionArray[( y / 2 ) * 9 + x / 2]))
						result.append('+');
					else
						result.append('-');
					if (x != 18)
						result.append('-');	
				}
				else
					if (y == 18) {
						if (x == 0 || x == 18 || (xp == 0 && regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] != regionArray[(( y / 2 ) - 1 ) * 9 + x / 2]))
							result.append('+');
						else
							result.append('-');
						if (x != 18)
							result.append('-');	
					}
					else
						if (yp == 0)
							if (x == 0)
								if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] != regionArray[( y / 2 ) * 9 + x / 2]) {
									result.append('+');
									result.append('-');									
								}
								else {
									result.append('|');
									result.append(' ');
								}
							else
								if (x == 18)
									if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] != regionArray[( y / 2 ) * 9 + x / 2 - 1])
										result.append('+');
									else
										result.append('|');
								else
									if (xp == 0)
										if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] == regionArray[( y / 2 ) * 9 + x / 2] &&
													regionArray[( y / 2 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2] &&
													regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2 - 1] &&
													regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[(( y / 2 ) - 1 ) * 9 + x / 2]){
													result.append(' ');
													result.append(' ');
										}
										else
											if (regionArray[( y / 2 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2] &&
												regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[(( y / 2 ) - 1 ) * 9 + x / 2]){
												result.append('-');
												result.append('-');
											}
											else
												if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] == regionArray[( y / 2 ) * 9 + x / 2] &&
													regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2 - 1]){
													result.append('|');
													result.append(' ');
												}
												else {
													result.append('+');
													if (x < 17 &&
														regionArray[(( y / 2 ) - 1 ) * 9 + (x + 1) / 2 ] == regionArray[( y / 2 ) * 9 + (x + 1) / 2]){
														result.append(' ');
													}
													else
														result.append('-');
												}
									else
										if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] == regionArray[( y / 2 ) * 9 + x / 2]){
											result.append(' ');
											result.append(' ');
										}
										else {
											result.append('-');	
											result.append('-');	
										}
						else {
							if (xp == 1) {
								int value = getCellValue(x / 2 , y / 2);
								if (value == 0)
									result.append('.');
								else
									result.append(value);
							}
							else
								if (x == 0 || x == 18 || regionArray[( y / 2 ) * 9 + x / 2 - 1] != regionArray[( y / 2 ) * 9 + x / 2])
									result.append('|');
								else
									result.append(' ');
							result.append(' ');
						}
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
                if (Settings.getInstance().isBlocks() || i == 0) {
					s = "+";
						for (int j=0; j<3; j++ ) {
							for (int k=0; k<3; k++ ) { s += "-";
								for (int l=0; l<crd; l++ ) { s += "-";
								}
							}
							if (Settings.getInstance().isBlocks() || j == 2)
								s += "-+";
						}
						res += s + System.lineSeparator();
				}
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
						if (Settings.getInstance().isBlocks() || k == 2)
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
                if (Settings.getInstance().isBlocks() || j == 2)
					s += "-+";
            }
            res += s;
        }
        return res;
    }
 
    /**
     * Get a multi-line pencilmark-string representation of this grid with Jigsaw region. If No Jigsaw then Variants region (except DG & X).
	 If not then No regions at all by @SudokuMonster
     */
    
    public String toStringMultilinePencilmarksVariant() {
		int[] regionArray = new int[81];
		if (Settings.getInstance().isWindows())
			regionArray = Arrays.copyOf(Settings.regionsWindows, 81);
		else
			if (Settings.getInstance().isX())
				regionArray = Arrays.copyOf(Settings.regionsBothDiagonals, 81);	
			else
				if (Settings.getInstance().isAsterisk())
					regionArray = Arrays.copyOf(Settings.regionsAsterisk, 81);
				else
					if (Settings.getInstance().isCD())
						regionArray = Arrays.copyOf(Settings.regionsCD, 81);
					else
						if (Settings.getInstance().isGirandola())
							regionArray = Arrays.copyOf(Settings.regionsGirandola, 81);
						else
							if (Settings.getInstance().isDG())
								regionArray = Arrays.copyOf(Settings.regionsDG, 81);
							else
								regionArray = Arrays.copyOf(Settings.regionsNoVariants, 81);
        String s = "";
		String minuses = "";
		String spaces = "";
		int cnt = 0;
		int pad = 0;
		int xp = 0;
		int yp = 0;
        int crd = 1;
        for (int i = 0; i < 81; i++) {
            int n = getCellPotentialValues(i).cardinality();
            if ( n > crd ) { crd = n; }
        }
		for (int n = 0; n < crd; n++) {
            minuses += "-";
			spaces += " ";
        }
        if ( crd > 1 ){
			for (int y = 0; y < 19; y++) {
				yp = y%2;
				for (int x = 0; x < 19; x++) {
					xp = x%2;
					if (y == 0) {
						if (x == 0 || x == 18 || (xp == 0 && regionArray[( y / 2 ) * 9 + x / 2 - 1] != regionArray[( y / 2 ) * 9 + x / 2])){
							s +="+";
							if (x != 18)
								s +=  "-" ;
						}
						else {
							s += "-";
							if (xp == 0)
								s += "-";
							else
							 s += minuses;
						}
					}
					else
						if (y == 18) {
							if (x == 0 || x == 18 || (xp == 0 && regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] != regionArray[(( y / 2 ) - 1 ) * 9 + x / 2])){
								s +="+";
								if (x != 18)
									s += "-";
							}
							else {
								s += "-";
								if (xp == 0)
									s += "-";
								else
								 s += minuses;
							}	
						}
						else
							if (yp == 0)
								if (x == 0)
									if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] != regionArray[( y / 2 ) * 9 + x / 2]) {
										s +="+-";									
									}
									else {
										s +="| ";
									}
								else
									if (x == 18)
										if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] != regionArray[( y / 2 ) * 9 + x / 2 - 1])
											s +="+";
										else
											s +="|";
									else
										if (xp == 0)
											if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] == regionArray[( y / 2 ) * 9 + x / 2] &&
														regionArray[( y / 2 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2] &&
														regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2 - 1] &&
														regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[(( y / 2 ) - 1 ) * 9 + x / 2]){
														s +="  ";
											}
											else
												if (regionArray[( y / 2 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2] &&
													regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[(( y / 2 ) - 1 ) * 9 + x / 2]){
													s +="--";
												}
												else
													if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] == regionArray[( y / 2 ) * 9 + x / 2] &&
														regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 - 1] == regionArray[( y / 2 ) * 9 + x / 2 - 1]){
														s +="| ";
													}
													else {
														s +="+";
														if (x < 17 &&
															regionArray[(( y / 2 ) - 1 ) * 9 + (x + 1) / 2 ] == regionArray[( y / 2 ) * 9 + (x + 1) / 2]){
															s +=" ";
														}
														else
															s +="-";
													}
										else
											if (regionArray[(( y / 2 ) - 1 ) * 9 + x / 2 ] == regionArray[( y / 2 ) * 9 + x / 2]){
												s += " " + spaces;
											}
											else {
												s += "-" + minuses;	
											}
							else {
								if (xp == 1) {
									cnt = pad = 0;
									Cell cell = getCell(x / 2 , y / 2);
									int n = getCellValue(x / 2 , y / 2);
									if ( n != 0 ) {
										s += n;
										cnt += 1;
									}
									if ( n == 0 ) {
										for (int pv=1; pv<=9; pv++ ) {
											if ( hasCellPotentialValue(cell.getIndex(), pv) ) {
												s += pv;
												cnt += 1;
											}
										}
									}
									pad = crd - cnt;
									for (n = 0; n < pad; n++) {
										s += " ";
									}
								}
								else
									if (x == 0 || x == 18 || regionArray[( y / 2 ) * 9 + x / 2 - 1] != regionArray[( y / 2 ) * 9 + x / 2])
										s +="|";
									else
										s +=" ";
								s +=" ";
							}
				}
				s +="\n";
			}
		}
        return s;
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
                    addCellPotentialValue(cl, value);
                }
            }
    	}
		fixGivens();	// fix #99
    }

//@SudokuMonster: Changes to allow for FP (NC)    
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
				if (Settings.getInstance().isForbiddenPairs() && isnakedsingle){
					int statusNC = Settings.getInstance().whichNC();
					if (statusNC > 0)
						if(Settings.getInstance().isToroidal()) {
							int j = 0;
							if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
								j = Grid.wazirCellsToroidal[i].length;
							else
								j = Grid.ferzCellsToroidal[i].length;
							for(int k = 0; k < j; k++) {
								if (statusNC == 2 || statusNC == 4 || singleclue < 9)
									if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2){
										if(hasCellPotentialValue(Grid.wazirCellsToroidal[i][k], singleclue == 9 ? 1 : singleclue + 1)){
											isnakedsingle = false;
											break;
										}
									}
									else
										if(hasCellPotentialValue(Grid.ferzCellsToroidal[i][k], singleclue == 9 ? 1 : singleclue + 1)){
											isnakedsingle = false;
											break;
										}
								if (statusNC == 2 || statusNC == 4 || singleclue > 1)
									if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2) {
										if(hasCellPotentialValue(Grid.wazirCellsToroidal[i][k], singleclue == 1 ? 9 : singleclue - 1)){
											isnakedsingle = false;
											break;
										}
									}
									else
										if(hasCellPotentialValue(Grid.ferzCellsToroidal[i][k], singleclue == 1 ? 9 : singleclue - 1)){
											isnakedsingle = false;
											break;
										}
							}
						}
						else {
							int j = 0;
							if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
								j = Grid.wazirCellsRegular[i].length;
							else
								j = Grid.ferzCellsRegular[i].length;
							for(int k = 0; k < j; k++) {
								if (statusNC == 2 || statusNC == 4 || singleclue < 9)
									if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2){
										if(hasCellPotentialValue(Grid.wazirCellsRegular[i][k], singleclue == 9 ? 1 : singleclue + 1)){
											isnakedsingle = false;
											break;
										}
									}
									else
										if(hasCellPotentialValue(Grid.ferzCellsRegular[i][k], singleclue == 9 ? 1 : singleclue + 1)){
											isnakedsingle = false;
											break;
										}

								if (statusNC == 2 || statusNC == 4 || singleclue > 1)
									if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2) {
										if(hasCellPotentialValue(Grid.wazirCellsRegular[i][k], singleclue == 1 ? 9 : singleclue - 1)){
											isnakedsingle = false;
											break;
										}
									}
									else
										if(hasCellPotentialValue(Grid.ferzCellsRegular[i][k], singleclue == 1 ? 9 : singleclue - 1)){
											isnakedsingle = false;
											break;
										}										
							}					
						}
				}
                if(isnakedsingle) {
                	setCellValue(i % 9, i / 9, singleclue);
                	clearCellPotentialValues(i);
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
        //if(!this.cellValues.equals(other.cellValues)) return false; <== incorrect
        if(!Arrays.equals(this.cellValues, other.cellValues)) return false;
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

    // Grid regions implementation (rows, columns, 3x3 squares)

    /**
     * Abstract class representing a region of a sudoku grid. A region
     * is either a row, a column or a 3x3 block.
     */
    public static abstract class Region {
    	protected final int[] regionCells = new int[9];
    	public final BitSet regionCellsBitSet = new BitSet(81);
    	
    	public abstract int getRegionTypeIndex();
    	public abstract int getRegionIndex();

		//4 Cell set corresponds to cells in block that share exactly 2 columns and 2 rowes
		private static int[][]  blocksEmptyCells = new int[][] {
			{4, 5, 7, 8},
			{3, 5, 6, 8},
			{3, 4, 6, 7},
			{1, 2, 7, 8},
			{0, 2, 6, 8},
			{0, 1, 6, 7},
			{1, 2, 4, 5},
			{0, 2, 3, 5},
			{0, 1, 3, 4},
			{6, 7, 8, -1},
			{3, 4, 5, -1},
			{0, 1, 2, -1},
			{2, 5, 8, -1},
			{1, 4, 7, -1},
			{0, 3, 6, -1}
		};
		//4 Cell set corresponds to cells in block that share exactly 1 column and 1 row or 2 lines 
		//without the cell at the intersection
		private static int[][]  blockGroupedCells = new int[][] {
			{3, 6, -1, 1, 2, -1},
			{4, 7, -1, 0, 2, -1},
			{5, 8, -1, 0, 1, -1},
			{0, 6, -1, 4, 5, -1},
			{1, 7, -1, 3, 5, -1},
			{2, 8, -1, 3, 4, -1},
			{0, 3, -1, 7, 8, -1},
			{1, 4, -1, 6, 8, -1},
			{2, 5, -1, 6, 7, -1},
			{0, 1, 2, 3, 4, 5},
			{0, 1, 2, 6, 7, 8},
			{3, 4, 5, 6, 7, 8},
			{0, 3, 6, 1, 4, 7},
			{0, 3, 6, 2, 5, 8},
			{1, 4, 7, 2, 5, 8}
		};
		private static int[][]  LineEmptyCells = new int[][] {
			{0,1,2},
			{3,4,5},
			{6,7,8},
		};			
		private static int[][]  LineGroupedCells = new int[][] {
			{3,4,5,6,7,8},
			{0,1,2,6,7,8},
			{0,1,2,3,4,5}
		};			
		//4 Cell set corresponds to cells in block that share exactly 2 columns and 2 rowes
		public BitSet Rectangle(int index) {
        	BitSet blockEmptyCellSet = new BitSet(10);
			for(int i = 0; i < 4; i++)
				if (blocksEmptyCells[index][i] >= 0)
					blockEmptyCellSet.set(blocksEmptyCells[index][i]);
			return blockEmptyCellSet;
		}

		//4 Cell set corresponds to cells in block that share exactly 1 column and 1 row 
		//without the cell at the intersection
		public BitSet Cross(int index) {
        	BitSet blockGroupedCellSet = new BitSet(10);
			for(int i = 0; i < 6; i++)
				 if (blockGroupedCells[index][i] >= 0)
					blockGroupedCellSet.set(blockGroupedCells[index][i]);
			return blockGroupedCellSet;
		}

		//2 Cell set corresponds to cells in block Cross cells that share 1 row 
		//without the cell at the intersection
		public BitSet crossBlade1(int index) {
        	BitSet blockGroupedCellSet = new BitSet(10);
			for(int i = 0; i < 3; i++)
				if (blockGroupedCells[index][i] >= 0)
					blockGroupedCellSet.set(blockGroupedCells[index][i]);
			return blockGroupedCellSet;
		}

		//2 Cell set corresponds to cells in block Cross cells that share 1 column 
		//without the cell at the intersection
		public BitSet crossBlade2(int index) {
        	BitSet blockGroupedCellSet = new BitSet(10);
			for(int i = 3; i < 6; i++)
				if (blockGroupedCells[index][i] >= 0)
					blockGroupedCellSet.set(blockGroupedCells[index][i]);
			return blockGroupedCellSet;
		}

		//1 Cell set corresponds to intersection cell in block cells that share exactly 1 column and 1 row 
		public BitSet crossHeart(int index) {
        	BitSet blockGroupedCellSet = new BitSet(10);
			blockGroupedCellSet.set(index);
			return blockGroupedCellSet;
		}


		//cellIndex of blockCell at centre of Cross Cell set 
		public int Heart(int index) {
			int[] blocksHeartCells = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
			return blocksHeartCells[index];
		}
		
		public BitSet lineEmptyCells(int index) {
			BitSet lineEmptyCellSet = new BitSet(10);
			for(int i = 0; i < 3; i++) 
				lineEmptyCellSet.set(LineEmptyCells[index][i]);
			return lineEmptyCellSet;
		}
		
		public BitSet lineBlade1(int index) {
        	BitSet lineBladeCellSet = new BitSet(10);
			for(int i = 0; i < 3; i++)
				lineBladeCellSet.set(LineGroupedCells[index][i]);
			return lineBladeCellSet;
		}

		public BitSet lineBlade2(int index) {
        	BitSet lineBladeCellSet = new BitSet(10);
			for(int i = 0; i < 3; i++)
				lineBladeCellSet.set(LineGroupedCells[index][i+3]);
			return lineBladeCellSet;
		}

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
        	return regionCellsBitSet.get(cell.getIndex());
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

//        /**
//         * Get the cells of this region. The iteration order of the result
//         * matches the order of the cells returned by {@link #getCell(int)}.
//         * @return the cells of this region.
//         */
//        public CellSet getCellSet() {
//            return new CellSet(regionCells);
//        }

        /**
         * Test whether this region crosses an other region.
         * <p>
         * A region crosses another region if they have at least one
         * common cell. In particular, any rows cross any columns.
         * @param other the other region
         * @return whether this region crosses the other region.
         */
        public boolean crosses(Region other) { //can be implemented as static table
        	return regionCellsBitSet.intersects(other.regionCellsBitSet);
        }

        /**
         * Get the number of cells of this region that are still empty.
         * @return the number of cells of this region that are still empty
         */
        public int getEmptyCellCount(Grid grid) {
            int result = 0;
            for (int i = 0; i < 9; i++) {
            	//Cell cell = getCell(i);
                //if (grid.getCellValue(cell.getX(), cell.getY()) == 0)
                //    result++;
                if(grid.getCellValue(regionCells[i]) == 0) result++;
            }
            return result;
        }
		
		public int getRegionCellIndexRow(int regionCellIndex) {
			return this.regionCells[regionCellIndex] / 9;
		}

		public int getRegionCellIndexColumn(int regionCellIndex) {
			return this.regionCells[regionCellIndex] % 9;
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

        /**
         * Get a short string representation of this region's type
         */
        public abstract String toStringShort();

        /**
         * Get a short string representation of this region
         * @return a short string representation of this region
         */
        public abstract String toFullStringShort();

        /**
         * Get a 2-digit integer representation of this regionType and RegionIndex
         * @return 2-digit integer representation of this regionType and RegionIndex
         */
        public abstract int toFullNumber();
    }

    /**
     * A row of a sudoku grid.
     */
    public static class Row extends Region {

        private final int rowNum;

        private Row(int rowNum) {
            this.rowNum = rowNum;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * rowNum + i;
            	regionCellIndex[regionCells[i]][getRegionTypeIndex()] = i;
            	regionCellsBitSet.set(regionCells[i]);
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
                return toString() + " " + (rowNum + 1);
            else
                return toString() + " " + (rowNum + 1);
        }

		@Override
        public String toStringShort() {
            return "r";
        }

        @Override
        public String toFullStringShort() {
            Settings settings = Settings.getInstance();
            if (settings.isRCNotation())
                return toStringShort() + (rowNum + 1);
            else
                return toStringShort() + (rowNum + 1);
        }

        @Override
        public int toFullNumber() {
			int result = (getRegionTypeIndex() * 10 + (rowNum + 1));
            return result;
        }
    }

    /**
     * A column of a sudoku grid
     */
    public static class Column extends Region {

        private final int columnNum;

        private Column(int columnNum) {
            this.columnNum = columnNum;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * i + columnNum;
            	regionCellIndex[regionCells[i]][getRegionTypeIndex()] = i;
            	regionCellsBitSet.set(regionCells[i]);
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
                return toString() + " " + (columnNum + 1);
            else
                return toString() + " " + (char)('A' + columnNum);
        }

		@Override
        public String toStringShort() {
            return "c";
        }

        @Override
        public String toFullStringShort() {
            Settings settings = Settings.getInstance();
            if (settings.isRCNotation())
                return toStringShort() + (columnNum + 1);
            else
                return toStringShort() + (columnNum + 1);
        }

        @Override
        public int toFullNumber() {
			int result = (getRegionTypeIndex() * 10 + (columnNum + 1));
            return result;
        }
    }

    /**
     * A 3x3 block of a sudoku grid.
     */
    public static class Block extends Region {

        private final int vNum, hNum, index;

        private Block(int index) {
        	final int[] vNums = new int[]{0,0,0,1,1,1,2,2,2};
        	final int[] hNums = new int[]{0,1,2,0,1,2,0,1,2};
            this.vNum = vNums[index];
            this.hNum = hNums[index];
            this.index = index;
            for(int i = 0; i < 9; i++) {
            	regionCells[i] = 9 * (vNum * 3 + i / 3) + (hNum * 3 + i % 3);
            	regionCellIndex[regionCells[i]][getRegionTypeIndex()] = i;
            	regionCellsBitSet.set(regionCells[i]);
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

		@Override
        public String toStringShort() {
            return "b";
        }

        @Override
        public String toFullStringShort() {
            return toStringShort() + (vNum * 3 + hNum + 1);
        }

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + (vNum * 3 + hNum + 1);
        }
    }

    /**
     * A 9 cell region of DG configuration (Block position group)
     */
    public static class DG extends Region {

        private final int vNum, hNum, index;
        private DG(int index) {
        	final int[] vNums = new int[]{0,0,0,1,1,1,2,2,2};
        	final int[] hNums = new int[]{0,1,2,0,1,2,0,1,2};
            this.vNum = vNums[index];
            this.hNum = hNums[index];
            this.index = index;
            for(int i = 0; i < 9; i++) {
				regionCells[i] = 9 * vNums[index] + hNums[index] +  3 * hNums[i] + 27 * vNums[i];
				regionCellIndex[regionCells[i]][getRegionTypeIndex()] = i;
				regionCellsBitSet.set(regionCells[i]);
				cellRegions[regionCells[i]][getRegionTypeIndex()] = index;
            }
        }

        public int getRegionTypeIndex() {
        	return 3;
        }
        
        public int getRegionIndex() {
        	return index;
        }


        @Override
        public String toString() {
            return "disjoint group";
        }

        @Override
        public String toFullString() {
            return toString() + " " + (vNum * 3 + hNum + 1);
        }

		@Override
        public String toStringShort() {
            return "p";
        }

        @Override
        public String toFullStringShort() {
            return toStringShort() + (vNum * 3 + hNum + 1);
		}

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + (vNum * 3 + hNum + 1);
        }
    }
	
    /**
     * A 9 cell region of Windows configuration
     */
    public static class Window extends Region {

        private final int /*vNum, hNum,*/ index;
		private final int[] windows = new int[]{
			8,4,4,4,8,5,5,5,8,
			6,0,0,0,6,1,1,1,6,
			6,0,0,0,6,1,1,1,6,
			6,0,0,0,6,1,1,1,6,
			8,4,4,4,8,5,5,5,8,
			7,2,2,2,7,3,3,3,7,
			7,2,2,2,7,3,3,3,7,
			7,2,2,2,7,3,3,3,7,
			8,4,4,4,8,5,5,5,8
		};
		private final int[] paths = new int[]{
			0,0,1,2,1,0,1,2,2,
			0,0,1,2,3,0,1,2,6,
			1,3,4,5,4,3,4,5,7,
			2,6,7,8,5,6,7,8,8,
			3,3,4,5,4,3,4,5,5,
			0,0,1,2,3,0,1,2,6,
			1,3,4,5,4,3,4,5,7,
			2,6,7,8,5,6,7,8,8,
			6,6,7,8,7,6,7,8,8
		};
        private Window(int index) {
        	//final int[] vNums = new int[]{0,0,0,1,1,1,2,2,2};
        	//final int[] hNums = new int[]{0,1,2,0,1,2,0,1,2};
            //this.vNum = vNums[index];
            //this.hNum = hNums[index];
            this.index = index;
            for(int i = 0; i < 81; i++) {
            	if (windows[i] == index) {
					regionCells[paths[i]] = i;
					regionCellIndex[i][getRegionTypeIndex()] = paths[i];
					regionCellsBitSet.set(regionCells[paths[i]]);
					cellRegions[regionCells[paths[i]]][getRegionTypeIndex()] = index;
				}
            }
        }

        public int getRegionTypeIndex() {
        	return 4;
        }
        
        public int getRegionIndex() {
        	return index;
        }
       
        @Override
        public String toString() {
            return "window group";
        }

        @Override
        public String toFullString() {
            return toString() + " " + (index + 1);
        }

		@Override
        public String toStringShort() {
            return "w";
        }

        @Override
		public String toFullStringShort() {
            return toStringShort() + (index + 1);
		}

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + (index + 1);
        }
    }

    /**
     * A 9 cell region of Main Diagonal configuration
     */
    public static class diagonalMain extends Region {
		private final int index;
		private final int[] paths = new int[]{
			0,	10,	20,	30,	40,	50,	60,	70,	80
		};
        private diagonalMain(int index) {
            this.index = index;
            for(int i = 0; i < 81; i++) {
				regionCellIndex[i][getRegionTypeIndex()] = -1;
				cellRegions[i][getRegionTypeIndex()] = -1;
			}
			for(int i = 0; i < 9; i++) {
						regionCells[i] = paths[i];
						regionCellIndex[paths[i]][getRegionTypeIndex()] = i;
						regionCellsBitSet.set(regionCells[i]);
						cellRegions[regionCells[i]][getRegionTypeIndex()] = 0;
			}
        }

        public int getRegionTypeIndex() {
        	return 5;
        }
        
        public int getRegionIndex() {
        	return index;
        }

        @Override
        public String toString() {
            return "Main Diagonal";
        }

        @Override
        public String toFullString() {
            return toString();
        }

		@Override
        public String toStringShort() {
            return "d\\";
        }

        @Override
        public String toFullStringShort() {
            return toStringShort();
		}

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + index;
        }
    }

    /**
     * A 9 cell region of Anti Diagonal configuration
     */
    public static class diagonalAnti extends Region {
		private final int index;
		private final int[] paths = new int[]{
			8,	16,	24,	32,	40,	48,	56,	64,	72
		};
        private diagonalAnti(int index) {
            this.index = index;
            for(int i = 0; i < 81; i++) {
				regionCellIndex[i][getRegionTypeIndex()] = -1;
				cellRegions[i][getRegionTypeIndex()] = -1;
			}
            for(int i = 0; i < 9; i++) {
						regionCells[i] = paths[i];
						regionCellIndex[paths[i]][getRegionTypeIndex()] = i;
						regionCellsBitSet.set(regionCells[i]);
						cellRegions[regionCells[i]][getRegionTypeIndex()] = 0;
			}
        }

        public int getRegionTypeIndex() {
        	return 6;
        }
        
        public int getRegionIndex() {
        	return index;
        }
       
        @Override
        public String toString() {
            return "Anti Diagonal";
        }

        @Override
        public String toFullString() {
            return toString();
        }

		@Override
        public String toStringShort() {
            return "d/";
        }

        @Override
        public String toFullStringShort() {
            return toStringShort();
		}

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + index;
        }
    }

    /**
     * A 9 cell region of Girandola configuration
     */
    public static class Girandola extends Region {
		private final int /*vNum, hNum,*/ index;
		public final static int[] regionsGirandola = 
			{1,0,0,0,0,0,0,0,1,
			 0,0,0,0,1,0,0,0,0,
			 0,0,0,0,0,0,0,0,0,
			 0,0,0,0,0,0,0,0,0,
			 0,1,0,0,1,0,0,1,0,
			 0,0,0,0,0,0,0,0,0,
			 0,0,0,0,0,0,0,0,0,
			 0,0,0,0,1,0,0,0,0,
			 1,0,0,0,0,0,0,0,1};
		private final int[] paths = new int[]{
			0, 8, 13, 37, 40, 43, 67, 72, 80
		};
        private Girandola(int index) {
        	//final int[] vNums = new int[]{0,0,0,1,1,1,2,2,2};
        	//final int[] hNums = new int[]{0,1,2,0,1,2,0,1,2};
            //this.vNum = vNums[index];
            //this.hNum = hNums[index];
            this.index = index;
            for(int i = 0; i < 81; i++) {
				regionCellIndex[i][getRegionTypeIndex()] = -1;
				cellRegions[i][getRegionTypeIndex()] = -1;
			}
            for(int i = 0; i < 9; i++) {
						regionCells[i] = paths[i];
						regionCellIndex[paths[i]][getRegionTypeIndex()] = i;
						regionCellsBitSet.set(regionCells[i]);
						cellRegions[regionCells[i]][getRegionTypeIndex()] = 0;
			}
        }

        public int getRegionTypeIndex() {
        	return 7;
        }
        
        public int getRegionIndex() {
        	return index;
        }
       
        @Override
        public String toString() {
            return "Girandola group";
        }

        @Override
        public String toFullString() {
            return toString();
        }

		@Override
        public String toStringShort() {
            return "g";
        }

        @Override
        public String toFullStringShort() {
            return toStringShort();
		}

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + index;
        }
    }

    /**
     * A 9 cell region of Asterisk configuration
     */
    public static class Asterisk extends Region {
		private final int index;
		private final int[] paths = new int[]{
			13,20,24,37,40,43,56,60,67
		};
        private Asterisk(int index) {
            this.index = index;
            for(int i = 0; i < 81; i++) {
				regionCellIndex[i][getRegionTypeIndex()] = -1;
				cellRegions[i][getRegionTypeIndex()] = -1;
			}
            for(int i = 0; i < 9; i++) {
						regionCells[i] = paths[i];
						regionCellIndex[paths[i]][getRegionTypeIndex()] = i;
						regionCellsBitSet.set(regionCells[i]);
						cellRegions[regionCells[i]][getRegionTypeIndex()] = 0;
			}
        }

        public int getRegionTypeIndex() {
        	return 8;
        }
        
        public int getRegionIndex() {
        	return index;
        }
       
        @Override
        public String toString() {
            return "Asterisk group";
        }

        @Override
        public String toFullString() {
            return toString();
		}

		@Override
        public String toStringShort() {
            return "a";
        }

        @Override
        public String toFullStringShort() {
            return toStringShort();
        }

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + index;
        }
    }
	
    /**
     * A 9 cell region of Center Dot configuration
     */
    public static class CD extends Region {
		private final int index;
		private final int[] paths = new int[]{
			10,13,16,37,40,43,64,67,70
		};
        private CD(int index) {
            this.index = index;
            for(int i = 0; i < 81; i++) {
				regionCellIndex[i][getRegionTypeIndex()] = -1;
				cellRegions[i][getRegionTypeIndex()] = -1;
			}
            for(int i = 0; i < 9; i++) {
						regionCells[i] = paths[i];
						regionCellIndex[paths[i]][getRegionTypeIndex()] = i;
						regionCellsBitSet.set(regionCells[i]);
						cellRegions[regionCells[i]][getRegionTypeIndex()] = 0;
			}
        }

        public int getRegionTypeIndex() {
        	return 9;
        }
        
        public int getRegionIndex() {
        	return index;
        }
       
        @Override
        public String toString() {
            return "Center Dot group";
        }

        @Override
        public String toFullString() {
            return toString();
        }

		@Override
        public String toStringShort() {
            return ".";
        }

        @Override
        public String toFullStringShort() {
            return toStringShort();
		}

        @Override
        public int toFullNumber() {
            return getRegionTypeIndex() * 10 + index;
        }
    }
}
