/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.generator;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.checks.*;


public class Generator {

    private final BruteForceAnalysis analyser = new BruteForceAnalysis(true);
    private boolean isInterrupted = false;


    /**
     * Generate a Sudoku grid matching the given parameters.
     * <p>
     * Depending on the given parameters, the generation can take very
     * long. The implementation actually repeatedly generates random
     * grids with the given symmetries until the difficulty is between
     * the given bounds.
     * @param symmetries the symmetries the resulting grid is allowed to have
     * @param minDifficulty the minimum difficulty of the grid
     * @param maxDifficulty the maximum difficulty of the grid
     * @return the generated grid
     */
    public Grid generate(List<Symmetry> symmetries, double minDifficulty, double maxDifficulty, double includeDifficulty1, double includeDifficulty2, double includeDifficulty3, double excludeDifficulty1, double excludeDifficulty2, double excludeDifficulty3, double notMaxDifficulty1, double notMaxDifficulty2, double notMaxDifficulty3, String excludeTechnique1, String excludeTechnique2, String excludeTechnique3, String includeTechnique1, String includeTechnique2, String includeTechnique3, String notMaxTechnique1, String notMaxTechnique2, String notMaxTechnique3, String getOneOfThree_1, String getOneOfThree_2, String getOneOfThree_3) {
        assert !symmetries.isEmpty() : "No symmetries specified";
        Random random = new Random();
        int symmetryIndex = random.nextInt(symmetries.size());
        while (true) {
            // Generate a random grid
            Symmetry symmetry = symmetries.get(symmetryIndex);
            symmetryIndex = (symmetryIndex + 1) % symmetries.size();
            Grid grid = generate(random, symmetry);

            if (isInterrupted)
                return null;

            // Analyse difficulty
            Grid copy = new Grid();
            grid.copyTo(copy);
            Solver solver = new Solver(copy);
            solver.rebuildPotentialValues();
            double difficulty = solver.analyseDifficulty(minDifficulty, maxDifficulty, includeDifficulty1, includeDifficulty2, includeDifficulty3, excludeDifficulty1, excludeDifficulty2, excludeDifficulty3, notMaxDifficulty1, notMaxDifficulty2, notMaxDifficulty3, excludeTechnique1, excludeTechnique2, excludeTechnique3, includeTechnique1, includeTechnique2, includeTechnique3, notMaxTechnique1, notMaxTechnique2, notMaxTechnique3, getOneOfThree_1, getOneOfThree_2, getOneOfThree_3);
            if (difficulty >= minDifficulty && difficulty <= maxDifficulty)
                return grid;

            if (isInterrupted)
                return null;

        }
    }

    /**
     * Generate a random grid with the given symmetry
     * @param rnd the random gene
     * @param symmetry the symmetry type
     * @return the generated grid
     */
    public Grid generate(Random rnd, Symmetry symmetry) {

        // Build the solution
        Grid grid = new Grid();
        Solver solver = new Solver(grid);
        solver.want = 0;
        boolean result = analyser.solveRandom(grid, rnd);
        assert result;
        Grid solution = new Grid();
        grid.copyTo(solution);

        // Build running indexes
        int[] indexes = new int[81];
        for (int i = 0; i < indexes.length; i++)
            indexes[i] = i;
        // Shuffle
        for (int i = 0; i < 81; i++) {
            int p1 = rnd.nextInt(81);
            int p2 = rnd.nextInt(81);
            int temp = indexes[p1];
            indexes[p1] = indexes[p2];
            indexes[p2] = temp;
        }

        //int attempts = 0;
        //int successes = 0;

        // Randomly remove clues
        boolean isSuccess = true;
        while (isSuccess) {
            // Choose a random cell to clear
            int index = rnd.nextInt(81);
            int countDown = 81; // Number of cells
            isSuccess = false;
            do {
                // Build symmetric points list
                int y = indexes[index] / 9;
                int x = indexes[index] % 9;
                Point[] points = symmetry.getPoints(x, y);

                // Remove cells
                boolean cellRemoved = false;
                for (Point p : points) {
                    //Cell cell = grid.getCell(p.x, p.y);
                    //if (cell.getValue() != 0) {
					grid.resetGiven(y * 9 + x);
                    if (grid.getCellValue(p.x, p.y) != 0) {
                        //cell.setValue(0);
                    	grid.setCellValue(p.x, p.y, 0);
                        cellRemoved = true;
                    }
                }
                if (cellRemoved) {
                    // Test if the Sudoku still has an unique solution
                    int state = analyser.getCountSolutions(grid);
                    if (state == 1) {
                        // Cells successfully removed: still a unique solution
                        isSuccess = true;
                        //successes += 1;
						//Following code is to handle Naturally difficult variants: 1to9only
						if (Settings.getInstance().isAntiFerz() || Settings.getInstance().isAntiKnight() || Settings.getInstance().whichNC() > 0 || Settings.getInstance().variantCount() > 1) {
							try { solver.getDifficulty(); } catch (UnsupportedOperationException ex) { solver.difficulty = solver.pearl = solver.diamond = 20.0; }
							if ( solver.difficulty == 20.0 ) { 
								for (Point p : points){
									grid.setGiven(y * 9 + x);
									grid.setCellValue(p.x, p.y, solution.getCellValue(p.x, p.y));
								}
								return grid; 
							}
						}
                    } else if (state == 0) {
                        assert false : "Invalid grid";
                    } else {
                        // Failed. Put the cells back and try with next cell
                        for (Point p : points){
                            grid.setCellValue(p.x, p.y, solution.getCellValue(p.x, p.y));
							grid.setGiven(y * 9 + x);
						}
                        //attempts += 1;
                    }
                }
                index = (index + 1) % 81; // Next index (indexing scrambled array of indexes)
                countDown--;
            } while (!isSuccess && countDown > 0);
        }
        return grid;
    }

    public void interrupt() {
        this.isInterrupted = true;
    }

}