/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.checks;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.*;

/**
 * Check if a sudoku has an unique solution using brute-force.
 * If the sudoku hasn't an unique solution, an appropriate
 * warning hint is produced. Else, no hint is produced.
 * @see diuf.sudoku.solver.checks.DoubleSolutionWarning
 */
public class BruteForceAnalysis implements WarningHintProducer {

    private final Grid grid1 = new Grid();
    private final Grid grid2 = new Grid();
    private final boolean includeSolution;


    public BruteForceAnalysis(boolean includeSolution) {
        this.includeSolution = includeSolution;
    }

    /**
     * Get hints that can be found from a brute-force analysis.
     * <p>
     * A warning/information hint is produced in one of the following case:
     * <ul>
     * <li>If the sudoku has no solution
     * <li>If the sudoku has more than one solution. The produced hint
     * contains two different solutions
     * <li>If the sudoku has exactly one solution, and <tt>true</tt> was passed
     * for <tt>includeSoluton</tt> in the constructor, a hint with the solution
     * is produced.
     * </ul>
     */
    public void getHints(Grid grid, HintsAccumulator accu)
            throws InterruptedException {
        grid.copyTo(grid1);
        boolean hasSolution = analyse(grid1, false);
        if (!hasSolution) {
            grid.copyTo(grid1);
            new Solver(grid1).rebuildPotentialValues();
            WarningMessage message;
            if (grid.equals(grid1)) {
                // All potential values correct - No solution
                message = new WarningMessage(this, "The Sudoku has no solution",
                "NoSolution.html");
            } else {
                // Some potential values missing. Check with all
                hasSolution = analyse(grid1, false);
                if (!hasSolution)
                    message = new WarningMessage(this, "The Sudoku has no solution",
                    "NoSolution.html");
                else
                    message = new WarningMessage(this, "The Sudoku has no solution",
                    "MissingCandidates.html");
            }
            accu.add(message);
            return;
        }
        grid.copyTo(grid2);
        analyse(grid2, true);
        if (!grid1.equals(grid2)) {
            WarningHint message = new DoubleSolutionWarning(this, grid, grid1, grid2);
            accu.add(message);
        } else if (this.includeSolution) {
            IndirectHint hint = new SolutionHint(this, grid, grid1);
            accu.add(hint);
        }
    }

    /**
     * Get information about the number of solutions of the given
     * sudoku grid.
     * <p>
     * The following values can be returned:
     * <ul>
     * <li><b>0</b> if the sudoku has no solution
     * <li><b>1</b> if the sudoku has exactly one solution
     * <li><b>2</b> if the sudoku has <i>more than one</i> solution
     * </ul>
     * <p>
     * Warning: you should first check that no value appear twice in the
     * same row, column or block. Else, this method may be extremely slow.
     * @param grid the sudoku grid
     * @return information about the number of solutions
     */
    public int getCountSolutions(Grid grid) {
        new Solver(grid).rebuildPotentialValues();
        grid.copyTo(grid1);
        if (!analyse(grid1, false))
            return 0; // no solution
        grid.copyTo(grid2);
        analyse(grid2, true);
        if (grid1.equals(grid2))
            return 1; // one unique solution
        else
            return 2; // more than one solution
    }

    /**
     * Check if the grid has been solved
     * @param grid the sudoku grid
     * @return whether the grid has been solved
     */
    private static boolean isSolved(Grid grid) {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (grid.getCellValue(x, y) == 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * Try to solve the given grid.
     * <p>
     * The grid can be solved in forward or reverse direction.
     * Both direction will result in the same solution if, and only if,
     * the sudoku has exactly one solution.
     * @param grid the grid to solve
     * @param isReverse whether to solve in reverse direction
     * @return <tt>true</tt> if the grid has been solved successfully
     * (in which case the grid is filled with the solution), or <tt>false</tt>
     * if the grid has no solution.
     */
    boolean analyse(Grid grid, boolean isReverse) {
        DirectHintProducer hiddenSingle = new HiddenSingle();
        DirectHintProducer nakedSingle = new NakedSingle();
        // new Solver(grid).rebuildPotentialValues();
        return analyse(grid, isReverse, null, hiddenSingle, nakedSingle);
    }

    private boolean analyse(Grid grid, boolean isReverse, Random rnd,
            DirectHintProducer hiddenSingle, DirectHintProducer nakedSingle) {
        /*
         * Quick check if every number can be placed in every row, column and block.
         * This is not necessary in theory, but in practice, some invalid sudoku
         * may require a too huge number of iterations without this check
         */
        if (!isFillable(grid))
            return false;

        /*
         * (1) Fill all naked single and hidden single.
         * Again, not necessary in theory (pure brute-force would be enough),
         * but some 17-clued sudoku would require too many iterations without it.
         */
        Hint hint = null;
        do {
            if (hint != null)
                hint.apply();
            SingleHintAccumulator accu = new SingleHintAccumulator();
            try {
                nakedSingle.getHints(grid, accu);
                hiddenSingle.getHints(grid, accu);
            } catch (InterruptedException ex) {}
            hint = accu.getHint();
        } while (hint != null);
        if (isSolved(grid))
            return true;
        /*
         * (2) Look for the cell with the least number of potentials.
         * Seems to give the best results (in term of speed) empirically
         */
        Cell leastCell = null;
        int leastCardinality = 10;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell.getValue() == 0) {
                    int cardinality = cell.getPotentialValues().cardinality();
                    if (cardinality < leastCardinality) {
                        leastCardinality = cardinality;
                        leastCell = cell;
                    }
                }
            }
        }
        // (3) Try each possible value for that cell
        Grid savePoint = new Grid();
        int startValue = (isReverse ? 8 : 0);
        int stopValue = (isReverse ? -1 : 9);
        int delta = (isReverse ? -1 : 1);
        int firstValue = 0;
        if (rnd != null)
            firstValue = rnd.nextInt(9);
        for (int value0 = startValue; value0 != stopValue; value0 += delta) {
            int value = value0 + 1;
            if (rnd != null) // Combine with random choice if random generator given
                value = ((value0 + firstValue) % 9) + 1;
            if (leastCell.hasPotentialValue(value)) {
                grid.copyTo(savePoint);
                leastCell.setValueAndCancel(value);
                boolean result = analyse(grid, isReverse, rnd, hiddenSingle, nakedSingle);
                if (result)
                    return true;
                // Restore savepoint and continue with next value, if any
                savePoint.copyTo(grid);
            }
        }
        // Failed
        return false;
    }

    /**
     * Check that every missing values can be placed in every column,
     * row and block. The test use the current state of the grid and
     * its cells.
     * @param grid the grid to check
     * @return <tt>false</tt> if at least one value cannot be placed in
     * a region (column, row or block). Else, <tt>true</tt> is returned,
     * but it does not mean the sudoku has a solution. Only <tt>false</tt>
     * imply that the sudoku has no solution.
     */
    private boolean isFillable(Grid grid) {
        for (Class<? extends Grid.Region> regionType : grid.getRegionTypes()) {
            Grid.Region[] regions = grid.getRegions(regionType);
            for (int i = 0; i < 9; i++) {
                Grid.Region region = regions[i];
                for (int value = 1; value <= 9; value++) {
                    if (!region.contains(value) && region.getPotentialPositions(value).isEmpty())
                        return false; // No room for the value in the region
                }
            }
        }
        return true;
    }

    /**
     * Solve the given grid randomly. If the grid has multiple solutions,
     * this will solve it using a random solution. If the grid has only
     * one solution, this solution will be found.
     * <p>
     * Randomly solving an empty grid generates the solution of a random grid.
     * @param grid the grid to solve
     * @param rnd the random number generator
     * @return <tt>true</tt> if a solution has been found; <tt>false</tt> if
     * the grid has no solution.
     */
    public boolean solveRandom(Grid grid, Random rnd) {
        DirectHintProducer hiddenSingle = new HiddenSingle();
        DirectHintProducer nakedSingle = new NakedSingle();
        new Solver(grid).rebuildPotentialValues();
        return analyse(grid, false, rnd, hiddenSingle, nakedSingle);
    }

    @Override
    public String toString() {
        return "Brute force analysis";
    }

}
