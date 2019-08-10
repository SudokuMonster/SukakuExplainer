/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import diuf.sudoku.*;

/**
 * The panel representing the sudoku grid. Includes the legend.
 * <p>
 * All user actions are redirected to the {@link diuf.sudoku.gui.SudokuExplainer}
 * class.
 * @see diuf.sudoku.gui.SudokuFrame
 * @see diuf.sudoku.gui.SudokuExplainer
 */
public class SudokuPanel extends JPanel {

    private static final long serialVersionUID = 3709127163156966626L;

    private int CELL_OUTER_SIZE = 45;
    private int CELL_INNER_SIZE = 39;
    private int GRID_GAP_SIZE = 2;
    private int LEGEND_GAP_SIZE = 42;
    private int CELL_PAD = (CELL_OUTER_SIZE - CELL_INNER_SIZE) / 2;
    private int GRID_SIZE = CELL_OUTER_SIZE * 9;
    private String FONT_NAME = "Verdana";
    private int FONT_SIZE_SMALL = 12;
    private int FONT_SIZE_BIG = 36;
    private int FONT_SIZE_LEGEND = 24;

    private Grid grid;
    private Cell focusedCell = null;
    private Cell selectedCell = null;
    private int focusedCandidate = 0;
    private Map<Cell,BitSet> redPotentials;
    private Map<Cell,BitSet> greenPotentials;
    private Map<Cell,BitSet> bluePotentials;
    private Collection<Cell> greenCells;
    private Collection<Cell> redCells;
    private Grid.Region[] blueRegions;
    private Collection<Link> links;

    private SudokuFrame parent;
    private SudokuExplainer engine;

    private Font smallFont;
    private Font bigFont;
    private Font legendFont;


    public SudokuPanel(SudokuFrame parent) {
        super();
        this.parent = parent;
        if (getToolkit().getScreenSize().height < 750)
            rescale();
        initialize();
        super.setOpaque(false);
        smallFont = new Font(FONT_NAME, Font.BOLD, FONT_SIZE_SMALL);
        bigFont = new Font(FONT_NAME, Font.BOLD, FONT_SIZE_BIG);
        legendFont = new Font(FONT_NAME, Font.BOLD, FONT_SIZE_LEGEND);
    }

    private void rescale() {
        CELL_OUTER_SIZE = rescale(CELL_OUTER_SIZE);
        CELL_INNER_SIZE = rescale(CELL_INNER_SIZE);
        GRID_GAP_SIZE = rescale(GRID_GAP_SIZE);
        LEGEND_GAP_SIZE = rescale(LEGEND_GAP_SIZE);
        CELL_PAD = rescale(CELL_PAD);
        GRID_SIZE = rescale(GRID_SIZE);
        FONT_SIZE_SMALL = 9;
        FONT_SIZE_BIG = rescale(FONT_SIZE_BIG);
        FONT_SIZE_LEGEND = rescale(FONT_SIZE_LEGEND);
    }

    private int rescale(int value) {
        return value * 2 / 3;
    }

    private void initialize() {
        this.addMouseListener(new java.awt.event.MouseAdapter() {   
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setFocusedCell(null);
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Cell target = getCellAt(e.getX(), e.getY());
                if (target == selectedCell && target != null) {
                    int value = getCandidateAt(e.getX(), e.getY());
                    if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3
                            || (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                        if (value != 0) { // Check that the cell is empty
                            // Toggle a candidate
                            engine.candidateTyped(target, value);
                            repaintCell(target);
                        }
                    } else {
                        if (target.getValue() == 0) {
                            // Set the cell's value
                            engine.cellValueTyped(target, value);
                            repaint();
                        } else {
                            // Clear the cell's value
                            engine.cellValueTyped(target, 0);
                            repaint();
                        }
                    }
                } else {
                    setFocusedCandidate(0);
                    setSelectedCell(target);
                    if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3
                            || (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                        setFocusedCandidate(getCandidateAt(e.getX(), e.getY()));
                }
                SudokuPanel.super.requestFocusInWindow();
            }
        });

        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                setFocusedCell(getCellAt(e.getX(), e.getY()));
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    setFocusedCandidate(getCandidateAt(e.getX(), e.getY()));
                else
                    setFocusedCandidate(0);
            }
        });
        this.addKeyListener(new java.awt.event.KeyAdapter() {   
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT ||
                        code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN) {
                    setFocusedCell(null);
                    int x = 4;
                    int y = 4;
                    if (selectedCell != null) {
                        x = selectedCell.getX();
                        y = selectedCell.getY();
                    }
                    if (code == KeyEvent.VK_LEFT)
                        x = (x + 8) % 9;
                    else if (code == KeyEvent.VK_RIGHT)
                        x = (x + 1) % 9;
                    else if (code == KeyEvent.VK_UP)
                        y = (y + 8) % 9;
                    else if (code == KeyEvent.VK_DOWN)
                        y = (y + 1) % 9;
                    setFocusedCandidate(0);
                    if (selectedCell == null) {
                        // Select the central cell
                        setSelectedCell(grid.getCell(4, 4));
                        x = 4;
                        y = 4;
                    } else {
                        setSelectedCell(grid.getCell(x, y));
                    }
                } else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE) {
                    if (selectedCell != null) {
                        engine.cellValueTyped(selectedCell, 0);
                        repaint();
                    }
                } else if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9) {
                    if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                        int value = (code - KeyEvent.VK_0);
                        if (selectedCell != null) {
                            engine.candidateTyped(selectedCell, value);
                            repaintCell(selectedCell);
                        }
                    }
                } else if (code == KeyEvent.VK_ESCAPE) {
                    setSelectedCell(null);
                    engine.clearHints();
                    repaint();
                }
            }
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                boolean isProcessed = false;
                if (selectedCell != null) {
                    char ch = e.getKeyChar();
                    if (ch >= '1' && ch <= '9') {
                        int value = ch - '0';
                        engine.cellValueTyped(selectedCell, value);
                        repaint();
                        isProcessed = true;
                    } else if (ch == ' ' || ch == '0') {
                        engine.cellValueTyped(selectedCell, 0);
                        selectedCell.setValue(0);
                        repaint();
                        isProcessed = true;
                    } else if (ch == '\r' || ch == '\n') {
                        setSelectedCell(null);
                        parent.getBtnApplyHintAndGet().requestFocusInWindow();
                        repaint();
                        isProcessed = true;
                    }
                }
                if (!isProcessed && e.getComponent() != SudokuPanel.this.parent) {
                    e.setSource(SudokuPanel.this.parent);
                    dispatchEvent(e);
                }
            }
        });
    }

    void setEngine(SudokuExplainer explainer) {
        this.engine = explainer;
    }

    private Cell getCellAt(int x, int y) {
        int cx = (x - LEGEND_GAP_SIZE) / CELL_OUTER_SIZE;
        int cy = (y - GRID_GAP_SIZE) / CELL_OUTER_SIZE;
        if (cx < 0 || cx >= 9 || cy < 0 || cy >= 9)
            return null;
        return grid.getCell(cx, cy);
    }

    private int getCandidateAt(int x, int y) {
        // Get cell's corner
        int cx = (x - LEGEND_GAP_SIZE) / CELL_OUTER_SIZE;
        int cy = (y - GRID_GAP_SIZE) / CELL_OUTER_SIZE;
        if (cx < 0 || cx >= 9 || cy < 0 || cy >= 9)
            return 0;
        Cell cell = grid.getCell(cx, cy);
        if (!cell.equals(this.selectedCell))
            return 0;
        if (this.selectedCell != null && this.selectedCell.getValue() != 0)
            return 0;
        // Substract cell's corner
        x = x - cx * CELL_OUTER_SIZE - LEGEND_GAP_SIZE;
        y = y - cy * CELL_OUTER_SIZE - GRID_GAP_SIZE;
        // Get candidate
        int px = (x - CELL_PAD) / (CELL_INNER_SIZE / 3);
        int py = (y - CELL_PAD) / (CELL_INNER_SIZE / 3);
        if (px < 0 || px >= 3 || py < 0 || py >= 3)
            return 0;
        return py * 3 + px + 1;
    }

    public Grid getSudokuGrid() {
        return grid;
    }

    public void setSudokuGrid(Grid sudokuGrid) {
        this.grid = sudokuGrid;
        this.selectedCell = sudokuGrid.getCell(4, 4);
    }

    public Collection<Cell> getGreenCells() {
        return greenCells;
    }

    public void setGreenCells(Collection<Cell> greenCells) {
        this.greenCells = greenCells;
    }

    public Collection<Cell> getRedCells() {
        return redCells;
    }

    public void setRedCells(Collection<Cell> redCells) {
        this.redCells = redCells;
    }

    public Map<Cell, BitSet> getGreenPotentials() {
        return greenPotentials;
    }

    public void setGreenPotentials(Map<Cell, BitSet> greenPotentials) {
        this.greenPotentials = greenPotentials;
    }

    public Map<Cell, BitSet> getRedPotentials() {
        return redPotentials;
    }

    public void setRedPotentials(Map<Cell, BitSet> redPotentials) {
        this.redPotentials = redPotentials;
    }

    public Map<Cell, BitSet> getBluePotentials() {
        return bluePotentials;
    }

    public void setBluePotentials(Map<Cell, BitSet> bluePotentials) {
        this.bluePotentials = bluePotentials;
    }

    public void setBlueRegions(Grid.Region... regions) {
        this.blueRegions = regions;
    }

    public void setLinks(Collection<Link> links) {
        this.links = links;
    }

    public void clearSelection() {
        this.focusedCandidate = 0;
        this.selectedCell = null;
        this.focusedCell = null;
    }

    public void clearFocus() {
        this.focusedCandidate = 0;
        this.focusedCell = null;
    }

    private void repaintCell(Cell cell) {
        if (cell == null)
            return;
        repaint(cell.getX() * CELL_OUTER_SIZE + LEGEND_GAP_SIZE,
                cell.getY() * CELL_OUTER_SIZE + GRID_GAP_SIZE,
                CELL_OUTER_SIZE, CELL_OUTER_SIZE);
    }

    private void setFocusedCell(Cell cell) {
        repaintCell(this.focusedCell);
        this.focusedCell = cell;
        repaintCell(this.focusedCell);
    }

    private void setSelectedCell(Cell cell) {
        repaintCell(this.selectedCell);
        this.selectedCell = cell;
        repaintCell(this.selectedCell);
    }

    private void setFocusedCandidate(int value) {
        if (this.selectedCell == null)
            return;
        this.focusedCandidate = value;
        repaintCell(this.selectedCell);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GRID_SIZE + LEGEND_GAP_SIZE + GRID_GAP_SIZE,
                GRID_SIZE + LEGEND_GAP_SIZE + GRID_GAP_SIZE);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private void drawStringCentered(Graphics g, String s, int x, int y) {
        Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
        double px = x - rect.getWidth() / 2;
        double py = y - rect.getHeight() / 2 - rect.getY();
        g.drawString(s, (int)(px + 0.5), (int)(py + 0.5));
    }

    private void drawStringCentered3D(Graphics g, String s, int x, int y) {
        Color color = g.getColor();
        g.setColor(Color.black);
        drawStringCentered(g, s, x, y + 1);
        g.setColor(Color.yellow);
        drawStringCentered(g, s, x - 1, y);
        g.setColor(color);
        drawStringCentered(g, s, x, y);
    }

    private boolean initPotentialColor(Graphics g, Cell cell, int value) {
        boolean isHighlighted = false;
        boolean isRed = false;
        Color col = Color.gray;
        if (bluePotentials != null) {
            BitSet blueValues = bluePotentials.get(cell);
            if (blueValues != null && blueValues.get(value)) {
                col = Color.blue;
                isHighlighted = true;
            }
        }
        if (redPotentials != null) {
            BitSet redValues = redPotentials.get(cell);
            if (redValues != null && redValues.get(value)) {
                col = Color.red;
                isHighlighted = true;
                isRed = true;
            }
        }
        if (greenPotentials != null) {
            BitSet greenValues = greenPotentials.get(cell);
            if (greenValues != null && greenValues.get(value)) {
                if (isRed) {
                    col = new Color(224, 128, 0);
                } else {
                    col = new Color(0, 224, 0);
                    isHighlighted = true;
                }
            }
        }
        if (cell == selectedCell)
            col = new Color(
                    (col.getRed() + Color.orange.getRed()) / 2,
                    (col.getGreen() + Color.orange.getGreen()) / 2,
                    (col.getBlue() + Color.orange.getBlue()) / 2);
        if (cell == selectedCell && value == focusedCandidate)
            col = Color.black;
        g.setColor(col);
        return isHighlighted;
    }

    private void initFillColor(Graphics g, Cell cell) {
        Color col;
        if (redCells != null && redCells.contains(cell))
            col = Color.red;
        else if (greenCells != null && greenCells.contains(cell))
            col = new Color(192, 255, 255);
        else if (cell == selectedCell)
            col = Color.orange;
        else if (cell == focusedCell)
            col = Color.yellow;
        else
            col = Color.white;
        g.setColor(col);
    }

    private void initValueColor(Graphics g, Cell cell) {
        Color col = Color.black;
        if (cell == selectedCell)
            col = new Color(
                    (col.getRed() + Color.orange.getRed()) / 2,
                    (col.getGreen() + Color.orange.getGreen()) / 2,
                    (col.getBlue() + Color.orange.getBlue()) / 2);
        g.setColor(col);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        initGraphics(g2);
        paintLegend(g);
        AffineTransform oldTransform = g2.getTransform();
        AffineTransform translate = 
            AffineTransform.getTranslateInstance(LEGEND_GAP_SIZE, GRID_GAP_SIZE);
        g2.transform(translate);
        g.clearRect(0, 0, GRID_SIZE, GRID_SIZE);
        paintSelectionAndFocus(g);
        paintGrid(g);
        paintHighlightedRegions(g);
        paintCellsValues(g);
        paintLinks(g);
        paintCellsPotentials(g);
        paintCursor();
        g2.setTransform(oldTransform);
    }

    /**
     * Set the given rectangle to match the rectangle occupied by a cell.
     * Only valid if the grpahics context is translated so that the
     * origin matches the upper left corner of the grid.
     * @param x the horizontal coordinate of the cell within the grid (0..8)
     * @param y the vertical coordinate of the cell within the grid (0..8)
     * @param target set to the rectangle occupied by the cell, in pixels
     */
    private void readCellRectangle(int x, int y, Rectangle target) {
        target.x = x * CELL_OUTER_SIZE;
        target.y = y * CELL_OUTER_SIZE;
        target.width = CELL_OUTER_SIZE;
        target.height = CELL_OUTER_SIZE;
    }

    private void initGraphics(Graphics2D g2) {
        if (Settings.getInstance().isAntialiasing()) {
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);    
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);    
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        }
    }

    private void paintLegend(Graphics g) {
        g.setFont(legendFont);
        g.setColor(new Color(0, 32, 64));
        Settings settings = Settings.getInstance();
        for (int i = 0; i < 9; i++) {
            String xLegend;
            if (settings.isRCNotation())
                xLegend = "C" + (i + 1);
            else
                xLegend = Character.toString((char)('A' + i));
            String yLegend;
            if (settings.isRCNotation())
                yLegend = "R" + (i + 1);
            else
                yLegend = Integer.toString(i + 1);
            drawStringCentered(g, yLegend,
                    LEGEND_GAP_SIZE / 2, CELL_OUTER_SIZE * i + GRID_GAP_SIZE + CELL_OUTER_SIZE / 2);
            drawStringCentered(g, xLegend,
                    LEGEND_GAP_SIZE + i * CELL_OUTER_SIZE + CELL_OUTER_SIZE / 2,
                    CELL_OUTER_SIZE * 9 + GRID_GAP_SIZE + LEGEND_GAP_SIZE / 2);
        }
    }

    private void paintSelectionAndFocus(Graphics g) {
        Rectangle clip = g.getClipBounds();
        Rectangle cellRect = new Rectangle();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                readCellRectangle(x, y, cellRect);
                if (clip.intersects(cellRect)) {
                    Cell cell = grid.getCell(x, y);
                    initFillColor(g, cell);
                    g.fillRect(x * CELL_OUTER_SIZE, y * CELL_OUTER_SIZE, CELL_OUTER_SIZE, CELL_OUTER_SIZE);
                }
            }
        }
    }

    private void paintGrid(Graphics g) {
        for (int i = 0; i <= 9; i++) {
            int lineWidth;
            if (i % 3 == 0) {
                lineWidth = 4;
                g.setColor(Color.black);
            } else {
                lineWidth = 2;
                g.setColor(Color.blue.darker());
            }
            int offset = lineWidth / 2;
            g.fillRect(i * CELL_OUTER_SIZE - offset, 0 - offset, lineWidth, GRID_SIZE + lineWidth);
            g.fillRect(0 - offset, i * CELL_OUTER_SIZE - offset, GRID_SIZE + lineWidth, lineWidth);
        }
    }

    private void paintHighlightedRegions(Graphics g) {
        if (blueRegions != null) {
            Color[] colors = new Color[] {new Color(0, 0, 192), new Color(0, 128, 0)};
            for (int rev = 0; rev < 2; rev++) {
                for (int i = 0; i < blueRegions.length; i++) {
                    int index = (rev == 0 ? i : blueRegions.length - 1 - i);
                    Grid.Region region = blueRegions[index];
                    if (region != null) {
                        int x, y, w, h; // coordinates, width, height (in cells)
                        if (region instanceof Grid.Row) {
                            Grid.Row row = (Grid.Row)region;
                            y = row.getRowNum();
                            h = 1;
                            x = 0;
                            w = 9;
                        } else if (region instanceof Grid.Column) {
                            Grid.Column column = (Grid.Column)region;
                            x = column.getColumnNum();
                            w = 1;
                            y = 0;
                            h = 9;
                        } else {
                            Grid.Block square = (Grid.Block)region;
                            x = square.getHIndex() * 3;
                            y = square.getVIndex() * 3;
                            w = 3;
                            h = 3;
                        }
                        g.setColor(colors[index % 2]);
                        for (int s = -2 + rev; s <= 2; s+= 2) {
                            g.drawRect(x * CELL_OUTER_SIZE + s, y * CELL_OUTER_SIZE + s,
                                    w * CELL_OUTER_SIZE - s * 2, h * CELL_OUTER_SIZE - s * 2);
                        }
                        if (rev == 0) {
                            Color base = colors[index % 2];
                            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 12));
                            g.fillRect(x * CELL_OUTER_SIZE + 3, y * CELL_OUTER_SIZE + 3,
                                    w * CELL_OUTER_SIZE - 6, h * CELL_OUTER_SIZE - 6);
                        }
                        index++;
                    }
                }
            }
        }
    }

    private void paintCellsPotentials(Graphics g) {
        Rectangle clip = g.getClipBounds();
        Rectangle cellRect = new Rectangle();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                readCellRectangle(x, y, cellRect);
                if (clip.intersects(cellRect)) {
                    Cell cell = grid.getCell(x, y);
                    // Paint potentials
                    int index = 0;
                    g.setFont(smallFont);
                    for (int value = 1; value <= 9; value++) {
                        boolean paintIt = Settings.getInstance().isShowingCandidates();
                        if (cell == this.selectedCell && value == this.focusedCandidate) {
                            // Paint magenta selection
                            g.setColor(Color.magenta);
                            g.fillRect(
                                    x * CELL_OUTER_SIZE + CELL_PAD + (index % 3) * (CELL_INNER_SIZE / 3),
                                    y * CELL_OUTER_SIZE + CELL_PAD + (index / 3) * (CELL_INNER_SIZE / 3),
                                    CELL_INNER_SIZE / 3, CELL_INNER_SIZE / 3);
                            paintIt = true;
                        }
                        if (cell.hasPotentialValue(value)) {
                            int cx = x * CELL_OUTER_SIZE + CELL_PAD
                            + (index % 3) * (CELL_INNER_SIZE / 3) + CELL_INNER_SIZE / 6;
                            int cy = y * CELL_OUTER_SIZE + CELL_PAD
                            + (index / 3) * (CELL_INNER_SIZE / 3) + CELL_INNER_SIZE / 6;
                            boolean isHighlighted = initPotentialColor(g, cell, value);
                            if (isHighlighted)
                                drawStringCentered3D(g, "" + value, cx, cy);
                            else if (paintIt)
                                drawStringCentered(g, "" + value, cx, cy);
                        }
                        index++;
                    }
                }
            }
        }
    }

    private void paintCellsValues(Graphics g) {
        Rectangle clip = g.getClipBounds();
        Rectangle cellRect = new Rectangle();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                readCellRectangle(x, y, cellRect);
                if (clip.intersects(cellRect)) {
                    Cell cell = grid.getCell(x, y);
                    // Paint cell value
                    if (cell.getValue() != 0) {
                        g.setFont(bigFont);
                        int cx = x * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 2;
                        int cy = y * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 2;          
                        initValueColor(g, cell);
                        drawStringCentered(g, "" + cell.getValue(), cx, cy);
                    }
                }
            }
        }
    }

    private class Line {

        public final int sx;
        public final int sy;
        public final int ex;
        public final int ey;

        public Line(int sx, int sy, int ex, int ey) {
            this.sx = sx;
            this.sy = sy;
            this.ex = ex;
            this.ey = ey;
        }

        private int distanceUnscaled(int px, int py) {
            // Vectorial product, without normalization by length
            return (px - sx) * (ey - sy) - (py - sy) * (ex - sx);
        }

        private boolean intervalOverlaps(int s1, int e1, int s2, int e2) {
            if (s1 > e1) {
                // Swap
                s1 = s1 ^ e1;
                e1 = s1 ^ e1;
                s1 = s1 ^ e1;
            }
            if (s2 > e2) {
                // Swap
                s2 = s2 ^ e2;
                e2 = s2 ^ e2;
                s2 = s2 ^ e2;
            }
            return s1 < e2 && e1 > s2;
        }

        public boolean overlaps(Line other) {
            if (distanceUnscaled(other.sx, other.sy) == 0 &&
                    distanceUnscaled(other.ex, other.ey) == 0) {
                // Both lines are on the same right
                return intervalOverlaps(this.sx, this.ex, other.sx, other.ex)
                || intervalOverlaps(this.sy, this.ey, other.sy, other.ey);
            }
            return false;
        }

    }

    private void paintLinks(Graphics g) {
        g.setColor(Color.orange);
        if (links != null) {
            Collection<Line> paintedLines = new ArrayList<Line>();
            for (Link link : links) {
                double sx = link.getSrcCell().getX() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                double sy = link.getSrcCell().getY() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                int srcValue = link.getSrcValue();
                if (srcValue > 0) {
                    sx += ((srcValue - 1) % 3) * (CELL_INNER_SIZE / 3);
                    sy += ((srcValue - 1) / 3) * (CELL_INNER_SIZE / 3);
                } else {
                    sx += CELL_INNER_SIZE / 3;
                    sy += CELL_INNER_SIZE / 3;
                }
                double ex = link.getDstCell().getX() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                double ey = link.getDstCell().getY() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                int dstValue = link.getDstValue();
                if (dstValue > 0) {
                    ex += ((dstValue - 1) % 3) * (CELL_INNER_SIZE / 3);
                    ey += ((dstValue - 1) / 3) * (CELL_INNER_SIZE / 3);
                } else {
                    ex += CELL_INNER_SIZE / 3;
                    ey += CELL_INNER_SIZE / 3;
                }
                // Get unity vector
                double length = Math.sqrt((ex - sx) * (ex - sx) + (ey - sy) * (ey - sy));
                double ux = (ex - sx) / length;
                double uy = (ey - sy) / length;
                // Build line object
                Line line = new Line((int)sx, (int)sy, (int)ex, (int)ey);
                // Count number of overlapping lines
                int countOverlap = 0;
                for (Line other : paintedLines) {
                    if (line.overlaps(other))
                        countOverlap++;
                }
                // Move the line perpendicularly to go away from overlapping lines
                double mx = (uy * ((countOverlap + 1) / 2) * 3.0);
                double my = (ux * ((countOverlap + 1) / 2) * 3.0);
                if (countOverlap % 2 == 0)
                    mx = -mx;
                else
                    my = -my;
                if (length >= CELL_INNER_SIZE / 2) {
                    // Truncate end points
                    if (srcValue > 0) {
                        sx += ux * CELL_INNER_SIZE / 6;
                        sy += uy * CELL_INNER_SIZE / 6;
                    }
                    if (dstValue > 0) {
                        ex -= ux * CELL_INNER_SIZE / 6;
                        ey -= uy * CELL_INNER_SIZE / 6;
                    }
                    if (dstValue > 0) {
                        // Draw arrow
                        double lx = ex - ux * 5 + uy * 2;
                        double ly = ey - uy * 5 - ux * 2;
                        double rx = ex - ux * 5 - uy * 2;
                        double ry = ey - uy * 5 + ux * 2;
                        g.fillPolygon(new int[] {(int)(ex + mx), (int)(rx + mx), (int)(lx + mx)},
                                new int[] {(int)(ey + my), (int)(ry + my), (int)(ly + my)}, 3);
                    }
                    paintedLines.add(line);
                }
                // Draw the line
                if (dstValue == 0 && srcValue == 0) {
                    Color oldColor = g.getColor();
                    //g.setColor(Color.magenta);
                    g.drawLine((int)(sx + mx), (int)(sy + my), (int)(ex + mx), (int)(ey + my));
                    g.setColor(oldColor);
                } else {
                    g.drawLine((int)(sx + mx), (int)(sy + my), (int)(ex + mx), (int)(ey + my));
                }
            }
        }
    }

    private void paintCursor() {
        /*
    if (selectedCell == focusedCell)
      this.setCursor(new Cursor(Cursor.TEXT_CURSOR));
    else
      this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         */
    }

}
