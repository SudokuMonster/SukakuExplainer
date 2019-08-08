/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.gui;

import java.io.*;
import java.util.*;

import javax.swing.*;

import diuf.sudoku.*;
import diuf.sudoku.io.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * The main class and controller. All actions performed in the gui
 * are directly redirected to method of this class, which
 * then calls the appropriate methods of the solver and other
 * tools.
 * <p>
 * This class only handles the logic of the filter for the hints tree,
 * and a few trivial tasks. All other operations are redirected to other
 * classes.
 * @see diuf.sudoku.gui.SudokuFrame
 * @see diuf.sudoku.solver.Solver
 */
public class SudokuExplainer {

    private Grid grid; // The Sudoku grid
    private Solver solver; // The Sudoku solver
    private SudokuFrame frame; // The main gui frame
    private SudokuPanel panel; // The sudoku grid panel

    private List<Hint> unfilteredHints = null; // All hints (unfiltered)
    private List<Hint> filteredHints = null; // All hints (filtered)
    private boolean isFiltered = true;
    private List<Hint> selectedHints = new ArrayList<Hint>(); // Currently selected hint

    // Cache for filter
    Set<Cell> givenCells = new HashSet<Cell>(); // Cell values already encountered
    Map<Cell, BitSet> removedPotentials = new HashMap<Cell, BitSet>(); // Removable potentials already encountered


    public SudokuExplainer() {
        grid = new Grid();
        solver = new Solver(grid);
        solver.rebuildPotentialValues();
        frame = new SudokuFrame();
        frame.setEngine(this);
        panel = frame.getSudokuPanel();
        panel.setSudokuGrid(grid);
        panel.setEngine(this);
        repaintHintsTree();
        frame.pack();
        java.awt.Dimension screenSize = frame.getToolkit().getScreenSize();
        java.awt.Insets insets = frame.getToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        java.awt.Dimension windowSize = frame.getSize();
        int screenWidth = screenSize.width - insets.left - insets.right;
        int screenHeight = screenSize.height - insets.top - insets.bottom;
        frame.setLocation((screenWidth - windowSize.width) / 2 + insets.left,
                (screenHeight - windowSize.height) / 2 + insets.top);
        frame.setVisible(true);
    }

    private void resetFilterCache() {
        givenCells = new HashSet<Cell>(); // Cell values already encountered
        removedPotentials = new HashMap<Cell, BitSet>(); // Removable potentials already encountered
    }

    /**
     * Hint filter
     */
    private boolean isWorth(Hint hint) {
        if (!isFiltered)
            return true;
        boolean isWorth; // Check if the hint yields to new outcomes
        if (hint instanceof DirectHint)
            isWorth = isWorth(givenCells, (DirectHint)hint);
        else
            isWorth = isWorth(removedPotentials, givenCells, (IndirectHint)hint);
        return isWorth;
    }

    /**
     * Test if a {@link DirectHint} allows the placement of a new cell
     * value. Returns <tt>false</tt> if the cell value given by this
     * hint has already been given by previous hints.
     * <p>
     * used for the hints tree filter
     * @param givenCells The set of cells whose value have already been given
     * @param hint the hint to test
     * @return whether the hint allows a new cell value placement
     */
    private boolean isWorth(Set<Cell> givenCells, DirectHint hint) {
        return (!givenCells.contains(hint.getCell()));
    }

    /**
     * Test if a {@link IndirectHint} allows the removal of new potentials.
     * Returns <tt>false</tt> if all the potentials remobavle with this hint
     * have already been removed by previous hints.
     * <p>
     * Used for the hints tree filter
     * @param removedPotentials the previously removed potentials
     * @param hint the hint to test
     * @return whether the hint allows the removal of new potentials
     */
    private boolean isWorth(Map<Cell,BitSet> removedPotentials, Set<Cell> givenCells,
            IndirectHint hint) {
        if (hint instanceof WarningHint)
            return true;
        Map<Cell, BitSet> removablePotentials = hint.getRemovablePotentials();
        for (Cell cell : removablePotentials.keySet()) {
            BitSet removable = removablePotentials.get(cell);
            BitSet previous = removedPotentials.get(cell);
            if (previous == null)
                return true;
            BitSet newRemove = (BitSet)removable.clone();
            newRemove.andNot(previous);
            if (!newRemove.isEmpty())
                return true;
        }
        Cell cell = hint.getCell();
        if (cell != null && !givenCells.contains(cell))
            return true;
        return false;
    }

    /**
     * Copy all the hints from {@link #unfilteredHints} to
     * {@link #filteredHints}, applying the filter if active.
     */
    private void filterHints() {
        filteredHints = null;
        if (unfilteredHints != null) {
            filteredHints = new ArrayList<Hint>();
            if (isFiltered) {
                // Filter hints with similar outcome
                for (Hint hint : unfilteredHints) {
                    if (isWorth(hint))
                        addFilteredHintAndUpdateFilter(hint);
                }
            } else {
                // Copy "as is"
                for (Hint hint : unfilteredHints)
                    filteredHints.add(hint);
            }
        }
    }

    private void addFilteredHintAndUpdateFilter(Hint hint) {
        filteredHints.add(hint);
        if (hint instanceof DirectHint) {
            // Update given cells
            DirectHint dHint = (DirectHint)hint;
            givenCells.add(dHint.getCell());
        } else {
            // Update removable potentials (candidates)
            IndirectHint iHint = (IndirectHint)hint;
            Map<Cell, BitSet> removablePotentials = iHint.getRemovablePotentials();
            for (Cell cell : removablePotentials.keySet()) {
                BitSet removable = removablePotentials.get(cell);
                BitSet current = removedPotentials.get(cell);
                if (current == null) {
                    current = new BitSet(10);
                    removedPotentials.put(cell, current);
                }
                current.or(removable);
            }
            // Update given cells if any
            Cell cell = iHint.getCell();
            if (cell != null)
                givenCells.add(cell);
        }
    }

    /**
     * Enable or disable the filter of hints with similar outcomes
     * @param value whether the filter is enabled
     */
    public void setFiltered(boolean value) {
        resetFilterCache();
        this.isFiltered = value;
        filterHints();
        repaintAll();
    }

    /**
     * Get whether the filter of hints with similar outcomes is enabled
     * @return whether the filter is enabled
     */
    public boolean isFiltered() {
        return this.isFiltered;
    }

    private void repaintHints() {
        if (selectedHints.size() == 1)
            frame.setCurrentHint(selectedHints.get(0), true);
        else {
            frame.setCurrentHint(null, !selectedHints.isEmpty());
            if (selectedHints.size() > 1)
                paintMultipleHints(selectedHints);
        }
    }

    private void paintMultipleHints(List<Hint> hints) {
        Map<Cell, BitSet> redPotentials = new HashMap<Cell, BitSet>();
        Map<Cell, BitSet> greenPotentials = new HashMap<Cell, BitSet>();
        for (Hint hint : hints) {
            Cell cell = hint.getCell();
            if (cell != null)
                greenPotentials.put(cell, SingletonBitSet.create(hint.getValue()));
            if (hint instanceof IndirectHint) {
                IndirectHint ihint = (IndirectHint)hint;
                Map<Cell, BitSet> removable = ihint.getRemovablePotentials();
                for (Cell rCell : removable.keySet()) {
                    BitSet values = removable.get(rCell);
                    if (redPotentials.containsKey(rCell))
                        redPotentials.get(rCell).or(values);
                    else
                        redPotentials.put(rCell, (BitSet)values.clone());
                }
            }
        }

        panel.setRedPotentials(redPotentials);
        panel.setGreenPotentials(greenPotentials);
        panel.setBluePotentials(null);
        panel.setGreenCells(greenPotentials.keySet());
        panel.repaint();
        frame.setExplanations(HtmlLoader.loadHtml(this, "Multiple.html"));
    }
    
    /**
     * Invoked when the user manually types a value in a cell of
     * the sudoku grid.
     * @param cell the cell
     * @param value the value typed in the cell, or <code>0</code> if
     * the cell's value was erased.
     */
    public void cellValueTyped(Cell cell, int value) {
        int oldValue = cell.getValue();
        cell.setValue(value);
        if (value == 0 || oldValue != 0)
            solver.rebuildPotentialValues();
        else
            solver.cancelPotentialValues();
        boolean needRepaintHints = (filteredHints != null);
        clearHints0();
        this.selectedHints.clear();
        if (needRepaintHints) {
            repaintHintsTree();
            repaintHints();
        }
    }

    public void candidateTyped(Cell cell, int candidate) {
        if (cell.hasPotentialValue(candidate))
            cell.removePotentialValue(candidate);
        else
            cell.addPotentialValue(candidate);
        solver.cancelPotentialValues();
    }

    /**
     * Selects the given hints. Repaint the appropriate views.
     * @param nodes the selected hint nodes
     */
    public void hintsSelected(Collection<HintNode> nodes) {
        this.selectedHints.clear();
        for (HintNode node : nodes) {
            Hint hint = node.getHint();
            if (hint != null)
                this.selectedHints.add(hint);
        }
        repaintHints();
    }

    private void repaintAll() {
        repaintHintsTree();
        repaintHints();
        panel.repaint();
    }

    public void clearGrid() {
        grid = new Grid();
        solver = new Solver(grid);
        solver.rebuildPotentialValues();
        panel.setSudokuGrid(grid);
        clearHints();
        frame.showWelcomeText();
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
        solver = new Solver(grid);
        solver.rebuildPotentialValues();
        panel.setSudokuGrid(grid);
        panel.clearSelection();
        clearHints();
        frame.setExplanations("");
    }

    public Grid getGrid() {
        return panel.getSudokuGrid();
    }

    public void clearHints() {
        unfilteredHints = null;
        resetFilterCache();
        filterHints();
        selectedHints.clear();
        panel.clearSelection();
        repaintAll();
    }

    public void clearHints0() {
        unfilteredHints = null;
        resetFilterCache();
        filterHints();
        selectedHints.clear();
        panel.clearFocus();
        repaintAll();
    }

    public void rebuildSolver() {
        this.solver = new Solver(grid);
    }

    private void displayError(Throwable ex) {
        ex.printStackTrace();
        try {
            repaintAll();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        frame.setExplanations("<html><body><font color=\"red\">" +
                ex.toString().replace("\n", "<br>") +
        "</font></body></html>");
    }

    /**
     * Check the validity of the current Sudoku grid.
     * Corresponding warning are automatically added and
     * displayed.
     * @return whether the grid is valid
     */
    public boolean checkValidity() {
        selectedHints.clear();
        unfilteredHints = new ArrayList<Hint>();
        Hint hint = solver.checkValidity();
        if (hint != null) {
            unfilteredHints.add(hint);
            selectedHints.add(hint);
        }
        filterHints();
        repaintAll();
        return (hint == null);
    }

    public void resetPotentials() {
        solver.rebuildPotentialValues();
        clearHints();
    }

    private Hint getNextHintImpl() {
        if (unfilteredHints == null) {
            unfilteredHints = new ArrayList<Hint>();
            filterHints();
        }
        // Create temporary buffers for gathering all the hints again
        final List<Hint> buffer = new ArrayList<Hint>();
        final StrongReference<Hint> newHint = new StrongReference<Hint>();
        solver.gatherHints(unfilteredHints, buffer, new HintsAccumulator() {
            /*
             * Trick: gatherHints will get all the hints it can find, one after
             * the other, sorted by difficulty. It will call add() for every hint.
             * To get only the first hint, we throw an InterruptedException after the
             * first produced hint that was not filtered.
             */
            public void add(Hint hint) throws InterruptedException {
                if (!buffer.contains(hint)) {
                    buffer.add(hint);
                    boolean isNew = (buffer.size() > unfilteredHints.size());
                    if (isNew) {
                        unfilteredHints.add(hint); // This hint is new for the unfiltered list
                        if (isWorth(hint)) {
                            newHint.setValue(hint);
                            throw new InterruptedException();
                        }
                    }
                }
            }
        }, frame);
        selectedHints.clear();
        Hint hint = null;
        if (newHint.isValueSet())
            hint = newHint.getValue();
        return hint;
    }

    public void getNextHint() {
        try {
            Hint hint = getNextHintImpl();
            if (hint != null) {
                addFilteredHintAndUpdateFilter(hint);
                selectedHints.add(hint);
            }
            repaintAll();
        } catch (Throwable ex) {
            displayError(ex);
        }
    }

    public void getAllHints() {
        try {
            unfilteredHints = solver.getAllHints(frame);
            selectedHints.clear();
            resetFilterCache();
            filterHints();
            if (!filteredHints.isEmpty())
                selectedHints.add(filteredHints.get(0));
            repaintAll();
        } catch (Throwable ex) {
            displayError(ex);
        }
    }

    public void applySelectedHints() {
        for (Hint hint : selectedHints)
            hint.apply();
        clearHints();
        repaintAll();
    }

    public void applySelectedHintsAndContinue() {
        applySelectedHints();
        getNextHint();
    }

    private void repaintHintsTree() {
        if (filteredHints == null) {
            List<Hint> noHints = Collections.emptyList();
            HintNode root = new HintsTreeBuilder().buildHintsTree(noHints);
            frame.setHintsTree(root, null, false);
        } else {
            HintNode root = new HintsTreeBuilder().buildHintsTree(filteredHints);
            HintNode selected = null;
            if (root != null && selectedHints.size() == 1)
                selected = root.getNodeFor(selectedHints.get(0));
            frame.setHintsTree(root, selected, unfilteredHints.size() > 1);
        }
    }

    public void pasteGrid() {
        Grid copy = new Grid();
        this.grid.copyTo(copy);
        clearGrid();
        ErrorMessage message = SudokuIO.loadFromClipboard(grid);
        if (message == null || !message.isFatal())
            solver.rebuildPotentialValues();
        else
            copy.copyTo(grid);
        if (message != null)
            JOptionPane.showMessageDialog(frame, message.toString(), "Paste",
                    (message.isFatal() ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE));
    }

    public void copyGrid() {
        SudokuIO.saveToClipboard(grid);
    }

    public void loadGrid(File file) {
        Grid copy = new Grid();
        this.grid.copyTo(copy);
        clearGrid();
        ErrorMessage message = SudokuIO.loadFromFile(grid, file);
        if (message == null || !message.isFatal())
            solver.rebuildPotentialValues();
        else
            copy.copyTo(grid);
        if (message != null)
            JOptionPane.showMessageDialog(frame, message.toString(), "Paste",
                    (message.isFatal() ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE));
    }

    public void saveGrid(File file) {
        ErrorMessage message = SudokuIO.saveToFile(grid, file);
        if (message != null)
            JOptionPane.showMessageDialog(frame, message.toString(), "Paste",
                    JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Solve the current grid using brute-force.
     * Display a new hint showing the solution.
     */
    public void solve() {
        clearHints();
        unfilteredHints = new ArrayList<Hint>();
        Hint hint = solver.bruteForceSolve();
        if (hint != null) {
            unfilteredHints.add(hint);
            selectedHints.add(hint);
        }
        filterHints();
        repaintAll();
    }

    /**
     * Solve the current Sudoku grid using logical rules.
     * Display a new hint giving the required rules, and the
     * approximate rating of the Sudoku.
     */
    public Hint analyse() {
        try {
            clearHints();
            unfilteredHints = new ArrayList<Hint>();
            Hint hint = solver.analyse(frame);
            if (hint != null) {
                unfilteredHints.add(hint);
                selectedHints.add(hint);
            }
            filterHints();
            return hint;
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Throwable ex) {
            displayError(ex);
            return null;
        } finally {
            repaintAll();
        }
    }

    /**
     * Display a single hint.
     * @param hint the hint to display
     */
    public void showHint(Hint hint) {
        clearHints();
        unfilteredHints = new ArrayList<Hint>();
        if (hint != null) {
            unfilteredHints.add(hint);
            selectedHints.add(hint);
        }
        filterHints();
        repaintAll();
    }

    /**
     * Get a small or big clue.
     * @param isBig whether to get a big clue
     */
    public void getClue(boolean isBig) {
        clearHints();
        Hint hint = getNextHintImpl();
        if (hint != null) {
            if (hint instanceof Rule) {
                Rule rule = (Rule)hint;
                String clueFile = (isBig ? "BigClue.html" : "SmallClue.html");
                String htmlText = HtmlLoader.loadHtml(this, clueFile);
                String clueHtml = rule.getClueHtml(isBig);
                htmlText = htmlText.replace("{0}", clueHtml);
                // This is rather hacky...
                if (htmlText.indexOf("<b1>") >= 0) {
                    panel.setBlueRegions(hint.getRegions());
                }
                htmlText = HtmlLoader.formatColors(htmlText);
                frame.setExplanations(htmlText);
                unfilteredHints = null;
                resetFilterCache();
                filterHints();
            } else {
                addFilteredHintAndUpdateFilter(hint);
                selectedHints.add(hint);
                repaintAll();
            }
        }
    }

    /**
     * Start point of the application. No splash screen is
     * handled there.
     * @param args program arguments (not used)
     */
    public static void main(String[] args) {
        try {
            String lookAndFeelClassName = Settings.getInstance().getLookAndFeelClassName();
            if (lookAndFeelClassName == null)
                lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeelClassName);
        } catch(Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SudokuExplainer();
            }
        });
    }

}
