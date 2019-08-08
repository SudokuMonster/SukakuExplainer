/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.checks;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * An hint that allows the user to show two different solutions
 * of a sudoku having more than one solutions.
 * @see diuf.sudoku.solver.checks.BruteForceAnalysis
 */
public class DoubleSolutionWarning extends WarningHint {

    private Grid grid;
    private Grid solution1;
    private Grid solution2;
    private int lastViewNum = 0;

    public DoubleSolutionWarning(WarningHintProducer rule, Grid source, Grid solution1,
            Grid solution2) {
        super(rule);
        this.grid = source;
        this.solution1 = solution1;
        this.solution2 = solution2;
    }

    @Override
    public void apply() {
        if (lastViewNum == 0)
            solution1.copyTo(grid);
        else
            solution2.copyTo(grid);
        // Clear all potentials
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                grid.getCell(x, y).clearPotentialValues();
            }
        }
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Grid solution = (viewNum == 0 ? solution1 : solution2);
        Map<Cell,BitSet> result = new HashMap<Cell,BitSet>();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int value = solution.getCellValue(x, y);
                Cell cell = grid.getCell(x, y);
                result.put(cell, SingletonBitSet.create(value));
            }
        }
        lastViewNum = viewNum;
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        return getGreenPotentials(viewNum);
    }

    @Override
    public int getViewCount() {
        return 2;
    }

    @Override
    public boolean isWorth() {
        return true;
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    @Override
    public String toString() {
        return "Sudoku has multiple solutions";
    }

    @Override
    public String toHtml() {
        return HtmlLoader.loadHtml(this, "DoubleSolution.html");
    }

}
