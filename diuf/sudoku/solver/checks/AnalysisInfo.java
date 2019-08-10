/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.checks;

import java.text.*;
import java.util.*;

import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * A information hint produced by the {@link diuf.sudoku.solver.checks.Analyser}
 * class. Contains an approximate rating of the sudoku, and the list of hints that
 * have been used to solve it. The actual solution is not shown, and the grid
 * is not modified by applying this hint.
 * @see diuf.sudoku.solver.checks.Analyser
 */
public class AnalysisInfo extends WarningHint {

    private final Map<Rule,Integer> rules;
    private final Map<String,Integer> ruleNames;


    public AnalysisInfo(WarningHintProducer rule, Map<Rule,Integer> rules,
            Map<String,Integer> ruleNames) {
        super(rule);
        this.rules = rules;
        this.ruleNames = ruleNames;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public String toHtml() {
        double difficulty = getDifficulty();
        DecimalFormat format = new DecimalFormat("#0.0");
        StringBuilder details = new StringBuilder();
        for (String ruleName : ruleNames.keySet()) {
            int count = ruleNames.get(ruleName);
            details.append(Integer.toString(count));
            details.append(" x ");
            details.append(ruleName);
            details.append("<br>\n");
        }
        String result = HtmlLoader.loadHtml(this, "Analysis.html");
        result = HtmlLoader.format(result, format.format(difficulty), details);
        return result;
    }

    public double getDifficulty() {
        double difficulty = 0;
        for (Rule rule : rules.keySet()) {
            if (rule.getDifficulty() > difficulty)
                difficulty = rule.getDifficulty();
        }
        return difficulty;
    }

    @Override
    public String toString() {
        return "Sudoku Rating";
    }

}
