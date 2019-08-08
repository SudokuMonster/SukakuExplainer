/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.checks;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * A hint that just shows an arbitrary warning or information message
 */
public class WarningMessage extends WarningHint {

    private final String message;
    private final String htmlFile;
    private final Object[] args;

    public WarningMessage(WarningHintProducer rule, String message,
            String htmlFile, Object... args) {
        super(rule);
        this.message = message;
        this.htmlFile = htmlFile;
        this.args = args;
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, htmlFile);
        return HtmlLoader.format(result, args);
    }

}
