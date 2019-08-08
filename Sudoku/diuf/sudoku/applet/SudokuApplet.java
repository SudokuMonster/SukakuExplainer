/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.applet;

import java.applet.*;

import javax.swing.*;

import diuf.sudoku.gui.*;

/**
 * Minimal applet support for the sudoku explainer.
 */
public class SudokuApplet extends Applet {

    private static final long serialVersionUID = -1770658360372460892L;

    
    @Override
    public void init() {
        super.init();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            /*
                             * It seems that IE want to get the focus just after the applet
                             * has started, which result in bringing our main window to the
                             * back. This small delay is a hack that solves this problem.
                             */
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {}
                        SudokuExplainer.main(null);
                    }
                }.start();
            }
        });
    }

}
