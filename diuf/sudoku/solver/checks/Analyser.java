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
 * Analyze a sudoku grid.
 * <p>
 * This class tries to fully solve the sudoku using logical rules
 * only and then produce a single hint with the rating of
 * the sudoku, and the list of hints that have been used.
 * <p>
 * If the sudoku is not valid, an appropriate warning hint is
 * produced.
 * @see diuf.sudoku.solver.checks.AnalysisInfo
 */
public class Analyser implements WarningHintProducer {

    private final Solver solver;
    private final Asker asker;


    public Analyser(Solver solver, Asker asker) {
        this.solver = solver;
        this.asker = asker;
    }

    public void getHints(Grid grid, HintsAccumulator accu)
    throws InterruptedException {
        Map<Rule,Integer> rules = solver.solve(asker);
        Map<String,Integer> ruleNames = solver.toNamedList(rules);
        Hint hint = new AnalysisInfo(this, rules, ruleNames);
        accu.add(hint);
    }

    @Override
    public String toString() {
        return "Analysis";
    }

}
