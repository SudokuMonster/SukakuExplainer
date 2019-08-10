/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.gui;

import java.security.*;
import java.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.UIManager.*;
import javax.swing.tree.*;

import diuf.sudoku.*;
import static diuf.sudoku.Settings.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.checks.*;
import diuf.sudoku.tools.*;
import javax.swing.ImageIcon;

/**
 * The main window of the application.
 * All the action are redirected to the {@link SudokuExplainer} class.
 */
public class SudokuFrame extends JFrame implements Asker {

    private static final long serialVersionUID = 8247189707924329043L;

    private SudokuExplainer engine;
    private Hint currentHint = null;
    private int viewCount = 1;
    private int viewNum = 0;

    private GenerateDialog generateDialog = null;
    private TechniquesSelectDialog selectDialog = null;

    private JFrame dummyFrameKnife = null;
    private JPanel jContentPane = null;
    private SudokuPanel sudokuPanel = null;
    private JScrollPane hintDetailsPane = null;
    private JTree hintsTree = null;
    private JEditorPane hintDetailArea = null;
    private JPanel jPanel = null;
    private JPanel sudokuContainer = null;
    private JPanel hintDetailContainer = null;
    private JPanel buttonsPane = null;
    private JButton btnGetAllHints = null;
    private JButton btnApplyHintAndGet = null;
    private JButton btnQuit = null;
    private JPanel buttonsContainer = null;
    private JScrollPane hintsTreeScrollpane = null;
    private JButton btnGetNextHint = null;
    private JPanel viewSelectionPanel = null;
    private JPanel hintsTreePanel = null;
    private JCheckBox chkFilter = null;
    private JButton btnCheckValidity = null;
    private JButton btnApplyHint = null;
    private JComboBox cmbViewSelector = null;
    private JPanel hintsSouthPanel = null;
    private JPanel ratingPanel = null;
    private JLabel jLabel = null;
    private JLabel lblRating = null;
    private JLabel jLabel2 = null;
    private JMenuBar jJMenuBar = null;
    private JMenu fileMenu = null;
    private JMenuItem mitNew = null;
    private JMenuItem mitQuit = null;
    private JMenuItem mitLoad = null;
    private JMenuItem mitSave = null;
    private JMenu editMenu = null;
    private JMenuItem mitCopy = null;
    private JMenuItem mitClear = null;
    private JMenuItem mitPaste = null;
    private JMenu toolMenu = null;
    private JMenuItem mitCheckValidity = null;
    private JMenuItem mitAnalyse = null;
    private JMenuItem mitSolveStep = null;
    private JMenuItem mitGetNextHint = null;
    private JMenuItem mitApplyHint = null;
    private JMenuItem mitGetAllHints = null;
    private JMenuItem mitSolve = null;
    private JMenuItem mitResetPotentials = null;
    private JMenuItem mitClearHints = null;
    private File defaultDirectory = null;
    private JRadioButton rdbView1 = null;
    private JRadioButton rdbView2 = null;
    private JMenu optionsMenu = null;
    private JCheckBoxMenuItem mitFilter = null;
    private JRadioButtonMenuItem mitMathMode = null;
    private JRadioButtonMenuItem mitChessMode = null;
    private JCheckBoxMenuItem mitAntiAliasing = null;
    private JMenu helpMenu = null;
    private JMenuItem mitAbout = null;
    private JMenuItem mitGetSmallClue = null;
    private JMenuItem mitGetBigClue = null;
    private JMenu mitLookAndFeel = null;
    private JMenuItem mitShowWelcome = null;
    private JMenuItem mitGenerate = null;
    private JCheckBoxMenuItem mitShowCandidates = null;
    private JMenuItem mitSelectTechniques = null;
    private JPanel pnlEnabledTechniques = null;
    private JLabel lblEnabledTechniques = null;


    public SudokuFrame() {
        super();
        initialize();
        repaintViews();
        AutoBusy.addFullAutoBusy(this);
        showWelcomeText();
        ImageIcon icon = createImageIcon("Sudoku.gif");
        setIconImage(icon.getImage());
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                getSudokuPanel().requestFocusInWindow();
            }
        });
    }

    public void showWelcomeText() {
        String welcomeText = HtmlLoader.loadHtml(this, "Welcome.html");
        setExplanations(welcomeText);
    }

    void setEngine(SudokuExplainer explainer) {
        this.engine = explainer;
    }

    public void setHintsTree(HintNode root, HintNode selected, boolean isFilterEnabled) {
        getHintsTree().setEnabled(false);
        DefaultTreeModel model = new DefaultTreeModel(root);
        getHintsTree().setModel(model);
        // Expand any node below the root
        if (root != null) {
            for (int i = 0; i < root.getChildCount(); i++) {
                HintNode child = (HintNode)root.getChildAt(i);
                getHintsTree().expandPath(new TreePath(child.getPath()));
            }
        }
        // Enabled/Disable filter checkbox
        chkFilter.setSelected(engine.isFiltered());
        chkFilter.setEnabled(isFilterEnabled);
        mitFilter.setSelected(chkFilter.isSelected());
        mitFilter.setEnabled(chkFilter.isEnabled());
        // Select any given selected node
        if (selected != null)
            getHintsTree().setSelectionPath(new TreePath(selected.getPath()));
        getHintsTree().setEnabled(true);
    }

    private void repaintHint() {
        Set<Cell> noCells = Collections.emptySet();
        Map<Cell, BitSet> noMap = Collections.emptyMap();
        sudokuPanel.setRedCells(noCells);
        sudokuPanel.setGreenCells(noCells);
        sudokuPanel.setRedPotentials(noMap);
        sudokuPanel.setGreenPotentials(noMap);
        // Highlight as necessary
        if (currentHint != null) {
            sudokuPanel.clearSelection();
            if (currentHint instanceof DirectHint) {
                DirectHint dHint = (DirectHint)currentHint;
                sudokuPanel.setGreenCells(Collections.singleton(dHint.getCell()));
                BitSet values = new BitSet(10);
                values.set(dHint.getValue());
                sudokuPanel.setGreenPotentials(Collections.singletonMap(
                        dHint.getCell(), values));
                getSudokuPanel().setLinks(null);
            } else if (currentHint instanceof IndirectHint) {
                IndirectHint iHint = (IndirectHint)currentHint;
                sudokuPanel.setGreenPotentials(iHint.getGreenPotentials(viewNum));
                sudokuPanel.setRedPotentials(iHint.getRedPotentials(viewNum));
                sudokuPanel.setBluePotentials(iHint.getBluePotentials(sudokuPanel.getSudokuGrid(), viewNum));
                if (iHint.getSelectedCells() != null)
                    sudokuPanel.setGreenCells(Arrays.asList(iHint.getSelectedCells()));
                if (iHint instanceof WarningHint)
                    sudokuPanel.setRedCells(((WarningHint)iHint).getRedCells());
                // Set links (rendered as arrows)
                getSudokuPanel().setLinks(iHint.getLinks(viewNum));
            }
            getSudokuPanel().setBlueRegions(currentHint.getRegions());
        }
        sudokuPanel.repaint();
    }

    public void setCurrentHint(Hint hint, boolean isApplyEnabled) {
        this.currentHint = hint;
        btnApplyHint.setEnabled(isApplyEnabled);
        mitApplyHint.setEnabled(isApplyEnabled);
        if (hint != null) {
            // Select view
            if (hint instanceof IndirectHint) {
                viewCount = ((IndirectHint)hint).getViewCount();
                if (viewNum >= viewCount)
                    viewNum = 0;
            } else {
                viewNum = 0;
                viewCount = 1;
            }
            repaintViews();
            // Set explanations
            setExplanations(hint.toHtml());
            if (hint instanceof Rule) {
                Rule rule = (Rule)hint;
                DecimalFormat format = new DecimalFormat("#0.0");
                lblRating.setText(format.format(rule.getDifficulty()));
            } else if (hint instanceof AnalysisInfo) {
                AnalysisInfo info = (AnalysisInfo)hint;
                DecimalFormat format = new DecimalFormat("#0.0");
                lblRating.setText(format.format(info.getDifficulty()));
            }
            // Set regions
        } else {
            getHintDetailArea().setText(null);
            getSudokuPanel().setBlueRegions();
            getSudokuPanel().setLinks(null);
            viewCount = 1;
            viewNum = 0;
            repaintViews();
        }
        repaintHint();
        this.repaint();
    }

    private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SudokuFrame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private String makeItem(int viewNum) {
        return "View " + (viewNum + 1);
    }

    private void repaintViews() {
        cmbViewSelector.setEnabled(false);
        cmbViewSelector.removeAllItems();
        for (int i = 0; i < viewCount; i++)
            cmbViewSelector.addItem(makeItem(i));
        cmbViewSelector.setSelectedIndex(viewNum);
        cmbViewSelector.setEnabled(viewCount >= 3);
        cmbViewSelector.setVisible(viewCount >= 3);
        rdbView1.setVisible(viewCount < 3);
        rdbView2.setVisible(viewCount < 3);
        rdbView1.setEnabled(viewCount > 1);
        rdbView2.setEnabled(viewCount > 1);
        if (viewNum == 0)
            rdbView1.setSelected(true);
        else
            rdbView2.setSelected(true);
    }

    public void setExplanations(String htmlText) {
        getHintDetailArea().setText(htmlText);
        getHintDetailArea().setCaretPosition(0);
        lblRating.setText("-");
    }

    public void refreshSolvingTechniques() {
        EnumSet<SolvingTechnique> all = EnumSet.allOf(SolvingTechnique.class);
        EnumSet<SolvingTechnique> enabled = Settings.getInstance().getTechniques();
        int disabled = all.size() - enabled.size();
        String message;
        if (disabled == 1)
            message = "1 solving technique is disabled";
        else
            message = "" + disabled + " solving" +
                    " techniques are disabled";
        lblEnabledTechniques.setText(message);
        pnlEnabledTechniques.setVisible(!Settings.getInstance().isUsingAllTechniques());
    }

    public boolean ask(String message) {
        return JOptionPane.showConfirmDialog(this, message, getTitle(),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private class HintsTreeCellRenderer implements TreeCellRenderer {

        private final DefaultTreeCellRenderer target = new DefaultTreeCellRenderer();


        public HintsTreeCellRenderer() {
            ImageIcon icon = createImageIcon("Light.gif");
            target.setLeafIcon(icon);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            if (!(value instanceof HintNode))
                return target.getTreeCellRendererComponent(tree, value, selected,
                        expanded, leaf, row, hasFocus);
            HintNode node = (HintNode)value;
            boolean isEmptyParent = (!node.isHintNode() && node.getChildCount() == 0);
            return target.getTreeCellRendererComponent(tree, value, selected,
                    expanded || isEmptyParent, leaf && !isEmptyParent, row, hasFocus);
        }

    }

    private void initialize() {
        this.setTitle("Sudoku Explainer " + VERSION + "." + REVISION + SUBREV);
        JMenuBar menuBar = getJJMenuBar();
        setupLookAndFeelMenu();
        this.setJMenuBar(menuBar);
        this.setContentPane(getJContentPane());
        try {
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (SecurityException ex) {
            // May happen in "applet" mode !
        }
        this.getSudokuPanel().requestFocusInWindow();
    }

    private void setupLookAndFeelMenu() {
        String lookAndFeelName = Settings.getInstance().getLookAndFeelClassName();
        if (lookAndFeelName == null)
            lookAndFeelName = UIManager.getSystemLookAndFeelClassName();
        ButtonGroup group = new ButtonGroup();
        for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(laf.getName());
            menuItem.setName(laf.getClassName());
            try {
                Class<?> lafClass = Class.forName(laf.getClassName());
                LookAndFeel instance = (LookAndFeel)lafClass.newInstance();
                menuItem.setToolTipText(instance.getDescription());
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            group.add(menuItem);
            getMitLookAndFeel().add(menuItem);
            if (laf.getClassName().equals(lookAndFeelName))
                menuItem.setSelected(true);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (menuItem.isSelected()) {
                        String lafClassName = menuItem.getName();
                        try {
                            UIManager.setLookAndFeel(lafClassName);
                            Settings.getInstance().setLookAndFeelClassName(lafClassName);
                            SwingUtilities.updateComponentTreeUI(SudokuFrame.this);
                            // Create renderer again to reload the correct icons:
                            hintsTree.setCellRenderer(new HintsTreeCellRenderer());
                            SudokuFrame.this.repaint();
                            if (generateDialog != null && generateDialog.isVisible()) {
                                SwingUtilities.updateComponentTreeUI(generateDialog);
                                generateDialog.pack();
                                generateDialog.repaint();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.NORTH);
            jContentPane.add(getHintDetailContainer(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getButtonsContainer(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    public SudokuPanel getSudokuPanel() {
        if (sudokuPanel == null) {
            sudokuPanel = new SudokuPanel(this);
        }
        return sudokuPanel;
    }

    private JScrollPane getHintsDetailScrollPane() {
        if (hintDetailsPane == null) {
            hintDetailsPane = new JScrollPane();
            if (getToolkit().getScreenSize().height < 800)
                hintDetailsPane.setPreferredSize(new java.awt.Dimension(700,110));
            else
                hintDetailsPane.setPreferredSize(new java.awt.Dimension(800,200));
            hintDetailsPane.setViewportView(getHintDetailArea());
        }
        return hintDetailsPane;
    }

    private JTree getHintsTree() {
        if (hintsTree == null) {
            hintsTree = new JTree();
            hintsTree.setShowsRootHandles(true);
            hintsTree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            hintsTree.setCellRenderer(new HintsTreeCellRenderer());
            hintsTree.setExpandsSelectedPaths(true);
            hintsTree
            .addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

                public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
                    if (hintsTree.isEnabled()) {
                        Collection<HintNode> selection = new ArrayList<HintNode>();
                        TreePath[] pathes = hintsTree.getSelectionPaths();
                        if (pathes != null) {
                            for (TreePath path : pathes)
                                selection.add((HintNode)path.getLastPathComponent());
                        }
                        engine.hintsSelected(selection);
                    }
                }
            });
        }
        return hintsTree;
    }

    private JEditorPane getHintDetailArea() {
        if (hintDetailArea == null) {
            hintDetailArea = new JEditorPane("text/html", null) {
                private static final long serialVersionUID = -5658720148768663350L;

                @Override
                public void paint(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    super.paint(g);
                }
            };
            hintDetailArea.setEditable(false);
        }
        return hintDetailArea;
    }

    private JScrollPane getHintsTreeScrollPane() {
        if (hintsTreeScrollpane == null) {
            hintsTreeScrollpane = new JScrollPane();
            hintsTreeScrollpane.setPreferredSize(new Dimension(100, 100));
            hintsTreeScrollpane.setViewportView(getHintsTree());
        }
        return hintsTreeScrollpane;
    }

    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getSudokuContainer(), java.awt.BorderLayout.WEST);
            jPanel.add(getHintsTreePanel(), java.awt.BorderLayout.CENTER);
        }
        return jPanel;
    }

    private JPanel getSudokuContainer() {
        if (sudokuContainer == null) {
            sudokuContainer = new JPanel();
            sudokuContainer.setLayout(new BorderLayout());
            sudokuContainer.setBorder(javax.swing.BorderFactory
                    .createTitledBorder(null, "Sudoku Grid",
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                            new java.awt.Color(51, 51, 51)));
            sudokuContainer.add(getSudokuPanel(), java.awt.BorderLayout.CENTER);
            sudokuContainer.add(getViewSelectionPanel(), java.awt.BorderLayout.SOUTH);
        }
        return sudokuContainer;
    }

    private JPanel getHintDetailContainer() {
        if (hintDetailContainer == null) {
            hintDetailContainer = new JPanel();
            hintDetailContainer.setLayout(new BorderLayout());
            hintDetailContainer.setBorder(javax.swing.BorderFactory
                    .createTitledBorder(null, "Explanations",
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                            new java.awt.Color(51, 51, 51)));
            hintDetailContainer.add(getHintsDetailScrollPane(), BorderLayout.CENTER);
        }
        return hintDetailContainer;
    }

    private JPanel getButtonsPane() {
        if (buttonsPane == null) {
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 4;
            gridBagConstraints21.weightx = 1.0D;
            gridBagConstraints21.gridy = 0;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 2;
            gridBagConstraints11.weightx = 1.0D;
            gridBagConstraints11.gridy = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.weightx = 1.0D;
            gridBagConstraints.gridy = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridy = 0;
            gridBagConstraints3.weightx = 1.0D;
            gridBagConstraints3.gridx = 6;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 5;
            gridBagConstraints2.weightx = 1.0D;
            gridBagConstraints2.gridy = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.gridy = 0;
            buttonsPane = new JPanel();
            buttonsPane.setLayout(new GridBagLayout());
            buttonsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
                    "Actions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(
                            "Dialog", java.awt.Font.BOLD, 12), null));
            buttonsPane.add(getBtnGetNextHint(), gridBagConstraints11);
            buttonsPane.add(getBtnApplyHintAndGet(), gridBagConstraints1);
            buttonsPane.add(getBtnGetAllHints(), gridBagConstraints2);
            buttonsPane.add(getBtnQuit(), gridBagConstraints3);
            buttonsPane.add(getBtnApplyHint(), gridBagConstraints21);
            buttonsPane.add(getBtnCheckValidity(), gridBagConstraints);
        }
        return buttonsPane;
    }

    private JButton getBtnGetNextHint() {
        if (btnGetNextHint == null) {
            btnGetNextHint = new JButton();
            btnGetNextHint.setText("Get next hint");
            btnGetNextHint.setToolTipText("Get another, different hint");
            btnGetNextHint.setMnemonic(java.awt.event.KeyEvent.VK_N);
            btnGetNextHint.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.getNextHint();
                }
            });
        }
        return btnGetNextHint;
    }

    private JButton getBtnGetAllHints() {
        if (btnGetAllHints == null) {
            btnGetAllHints = new JButton();
            btnGetAllHints.setText("Get all hints");
            btnGetAllHints.setToolTipText("Get all hints applicable on the current situation");
            btnGetAllHints.setMnemonic(KeyEvent.VK_A);
            btnGetAllHints.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.getAllHints();
                }
            });
        }
        return btnGetAllHints;
    }

    JButton getBtnApplyHintAndGet() {
        if (btnApplyHintAndGet == null) {
            btnApplyHintAndGet = new JButton();
            btnApplyHintAndGet.setText("Solve step");
            btnApplyHintAndGet.setMnemonic(java.awt.event.KeyEvent.VK_S);
            btnApplyHintAndGet.setToolTipText("Apply the current hint (if any is shown), and get an hint for the next step");
            btnApplyHintAndGet.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            btnApplyHintAndGet.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.applySelectedHintsAndContinue();
                }
            });
        }
        return btnApplyHintAndGet;
    }

    private JButton getBtnQuit() {
        if (btnQuit == null) {
            btnQuit = new JButton();
            btnQuit.setText("Quit");
            btnQuit.setToolTipText("Quit the application");
            btnQuit.setMnemonic(java.awt.event.KeyEvent.VK_Q);
            btnQuit.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    quit();
                }
            });
        }
        return btnQuit;
    }

    private JPanel getButtonsContainer() {
        if (buttonsContainer == null) {
            buttonsContainer = new JPanel();
            buttonsContainer.setLayout(new GridLayout(1, 1));
            buttonsContainer.add(getButtonsPane(), null);
        }
        return buttonsContainer;
    }

    private JPanel getViewSelectionPanel() {
        if (viewSelectionPanel == null) {
            viewSelectionPanel = new JPanel();
            viewSelectionPanel.setLayout(new FlowLayout());
            viewSelectionPanel.add(getRdbView1(), null);
            viewSelectionPanel.add(getCmbViewSelector(), null);
            viewSelectionPanel.add(getRdbView2(), null);
            ButtonGroup group = new ButtonGroup();
            group.add(getRdbView1());
            group.add(getRdbView2());
        }
        return viewSelectionPanel;
    }

    private JPanel getHintsTreePanel() {
        if (hintsTreePanel == null) {
            hintsTreePanel = new JPanel();
            hintsTreePanel.setLayout(new BorderLayout());
            hintsTreePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    null, "Hints classification",
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(
                            "Dialog", java.awt.Font.BOLD, 12), null));
            hintsTreePanel.add(getHintsTreeScrollPane(), java.awt.BorderLayout.CENTER);
            hintsTreePanel.add(getHintsSouthPanel(), java.awt.BorderLayout.SOUTH);
        }
        return hintsTreePanel;
    }

    private JCheckBox getChkFilter() {
        if (chkFilter == null) {
            chkFilter = new JCheckBox();
            chkFilter.setText("Filter hints with similar outcome");
            chkFilter.setMnemonic(KeyEvent.VK_I);
            chkFilter.setSelected(true);
            chkFilter.setEnabled(false);
            chkFilter.addItemListener(new java.awt.event.ItemListener() {

                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    engine.setFiltered(chkFilter.isSelected());
                }
            });
        }
        return chkFilter;
    }

    private JButton getBtnCheckValidity() {
        if (btnCheckValidity == null) {
            btnCheckValidity = new JButton();
            btnCheckValidity.setText("Check validity");
            btnCheckValidity.setToolTipText("Verify the validity of the entered Sudoku");
            btnCheckValidity.setMnemonic(java.awt.event.KeyEvent.VK_V);
            btnCheckValidity.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (engine.checkValidity())
                        setExplanations(HtmlLoader.loadHtml(this, "Valid.html"));
                }
            });
        }
        return btnCheckValidity;
    }

    private JButton getBtnApplyHint() {
        if (btnApplyHint == null) {
            btnApplyHint = new JButton();
            btnApplyHint.setText("Apply hint");
            btnApplyHint.setMnemonic(KeyEvent.VK_P);
            btnApplyHint.setToolTipText("Apply the selected hint(s)");
            btnApplyHint.setEnabled(false);
            btnApplyHint.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.applySelectedHints();
                }
            });
        }
        return btnApplyHint;
    }

    private JComboBox getCmbViewSelector() {
        if (cmbViewSelector == null) {
            cmbViewSelector = new JComboBox();
            cmbViewSelector.setToolTipText("Toggle view (only for chaining hints)");
            cmbViewSelector.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (cmbViewSelector.isEnabled()) {
                        viewNum = cmbViewSelector.getSelectedIndex();
                        repaintHint();
                    }
                }
            });
        }
        return cmbViewSelector;
    }

    private JRadioButton getRdbView1() {
        if (rdbView1 == null) {
            rdbView1 = new JRadioButton();
            rdbView1.setText("View 1");
            rdbView1.setMnemonic(KeyEvent.VK_1);
            rdbView1.setToolTipText(getCmbViewSelector().getToolTipText());
            rdbView1.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (rdbView1.isSelected()) {
                        viewNum = 0;
                        repaintHint();
                    }
                }
            });
        }
        return rdbView1;
    }

    private JRadioButton getRdbView2() {
        if (rdbView2 == null) {
            rdbView2 = new JRadioButton();
            rdbView2.setText("View 2");
            rdbView2.setMnemonic(KeyEvent.VK_2);
            rdbView2.setToolTipText(getCmbViewSelector().getToolTipText());
            rdbView2.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (rdbView2.isSelected()) {
                        viewNum = 1;
                        repaintHint();
                    }
                }
            });
        }
        return rdbView2;
    }

    private JPanel getHintsSouthPanel() {
        if (hintsSouthPanel == null) {
            hintsSouthPanel = new JPanel();
            hintsSouthPanel.setLayout(new BorderLayout());
            hintsSouthPanel.add(getPnlEnabledTechniques(), BorderLayout.NORTH);
            hintsSouthPanel.add(getChkFilter(), BorderLayout.CENTER);
            hintsSouthPanel.add(getRatingPanel(), BorderLayout.SOUTH);
        }
        return hintsSouthPanel;
    }

    private JPanel getRatingPanel() {
        if (ratingPanel == null) {
            ratingPanel = new JPanel();
            jLabel2 = new JLabel();
            //jLabel2.setText(" / 10");
            lblRating = new JLabel();
            lblRating.setText("0");
            jLabel = new JLabel();
            jLabel.setText("Hint rating: ");
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
            ratingPanel.setLayout(flowLayout);
            ratingPanel.add(jLabel, null);
            ratingPanel.add(lblRating, null);
            ratingPanel.add(jLabel2, null);
        }
        return ratingPanel;
    }

    private JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getFileMenu());
            jJMenuBar.add(getEditMenu());
            jJMenuBar.add(getToolMenu());
            jJMenuBar.add(getOptionsMenu());
            jJMenuBar.add(getHelpMenu());
        }
        return jJMenuBar;
    }

    private void setCommand(JMenuItem item, char cmd) {
        item.setAccelerator(KeyStroke.getKeyStroke(cmd, InputEvent.CTRL_MASK));
    }

    private JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu();
            fileMenu.setText("File");
            fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);
            fileMenu.add(getMitNew());
            setCommand(getMitNew(), 'N');
            fileMenu.add(getMitGenerate());
            setCommand(getMitGenerate(), 'G');
            fileMenu.addSeparator();
            fileMenu.add(getMitLoad());
            setCommand(getMitLoad(), 'O');
            fileMenu.add(getMitSave());
            setCommand(getMitSave(), 'S');
            fileMenu.addSeparator();
            fileMenu.add(getMitQuit());
            setCommand(getMitQuit(), 'Q');
        }
        return fileMenu;
    }

    private JMenuItem getMitNew() {
        if (mitNew == null) {
            mitNew = new JMenuItem();
            mitNew.setText("New");
            mitNew.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_MASK));
            mitNew.setMnemonic(java.awt.event.KeyEvent.VK_N);
            mitNew.setToolTipText("Clear the grid");
            mitNew.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.clearGrid();
                }
            });
        }
        return mitNew;
    }

    private JMenuItem getMitQuit() {
        if (mitQuit == null) {
            mitQuit = new JMenuItem();
            mitQuit.setText("Quit");
            mitQuit.setMnemonic(java.awt.event.KeyEvent.VK_Q);
            mitQuit.setToolTipText("Bye bye");
            mitQuit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    quit();
                }
            });
        }
        return mitQuit;
    }

    private void warnAccessError(AccessControlException ex) {
        JOptionPane.showMessageDialog(this,
                "Sorry, this functionality cannot be used from an applet.\n" +
                "Denied permission: " + ex.getPermission().toString() + "\n" +
                "Download the application to access this functionality.",
                "Access denied", JOptionPane.ERROR_MESSAGE);
    }

    private class TextFileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            return f.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return "Text files (*.txt)";
        }

    }

    private JMenuItem getMitLoad() {
        if (mitLoad == null) {
            mitLoad = new JMenuItem();
            mitLoad.setText("Load...");
            mitLoad.setMnemonic(java.awt.event.KeyEvent.VK_O);
            mitLoad.setToolTipText("Open the file selector to load the grid from a file");
            mitLoad.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileFilter(new TextFileFilter());
                        if (defaultDirectory != null)
                            chooser.setCurrentDirectory(defaultDirectory);
                        int result = chooser.showOpenDialog(SudokuFrame.this);
                        defaultDirectory = chooser.getCurrentDirectory();
                        if (result == JFileChooser.APPROVE_OPTION)
                            engine.loadGrid(chooser.getSelectedFile());
                    } catch (AccessControlException ex) {
                        warnAccessError(ex);
                    }
                }
            });
        }
        return mitLoad;
    }

    private JMenuItem getMitSave() {
        if (mitSave == null) {
            mitSave = new JMenuItem();
            mitSave.setText("Save...");
            mitSave.setMnemonic(java.awt.event.KeyEvent.VK_S);
            mitSave.setToolTipText("Open the file selector to save the grid to a file");
            mitSave.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileFilter(new TextFileFilter());
                        if (defaultDirectory != null)
                            chooser.setCurrentDirectory(defaultDirectory);
                        int result = chooser.showSaveDialog(SudokuFrame.this);
                        defaultDirectory = chooser.getCurrentDirectory();
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            try {
                                if (!file.getName().endsWith(".txt") &&
                                        file.getName().indexOf('.') < 0)
                                    file = new File(file.getCanonicalPath() + ".txt");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            if (file.exists()) {
                                if (JOptionPane.showConfirmDialog(SudokuFrame.this,
                                        "The file \"" + file.getName() + "\" already exists.\n" +
                                        "Do you want to replace the existing file ?",
                                        "Save", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
                                    return;
                            }
                            engine.saveGrid(file);
                        }
                    } catch (AccessControlException ex) {
                        warnAccessError(ex);
                    }
                }
            });
        }
        return mitSave;
    }

    private JMenu getEditMenu() {
        if (editMenu == null) {
            editMenu = new JMenu();
            editMenu.setText("Edit");
            editMenu.setMnemonic(java.awt.event.KeyEvent.VK_E);
            editMenu.add(getMitCopy());
            setCommand(getMitCopy(), 'C');
            editMenu.add(getMitPaste());
            setCommand(getMitPaste(), 'V');
            editMenu.addSeparator();
            editMenu.add(getMitClear());
            setCommand(getMitClear(), 'E'); 
        }
        return editMenu;
    }

    private JMenuItem getMitCopy() {
        if (mitCopy == null) {
            mitCopy = new JMenuItem();
            mitCopy.setText("Copy grid");
            mitCopy.setMnemonic(KeyEvent.VK_C);
            mitCopy.setToolTipText("Copy the grid to the clipboard as plain text");
            mitCopy.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        engine.copyGrid();
                    } catch (AccessControlException ex) {
                        warnAccessError(ex);
                    }
                }
            });
        }
        return mitCopy;
    }

    private JMenuItem getMitClear() {
        if (mitClear == null) {
            mitClear = new JMenuItem();
            mitClear.setText("Clear grid");
            mitClear.setMnemonic(KeyEvent.VK_E);
            mitClear.setToolTipText("Clear the grid");
            mitClear.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.clearGrid();
                }
            });
        }
        return mitClear;
    }

    private JMenuItem getMitPaste() {
        if (mitPaste == null) {
            mitPaste = new JMenuItem();
            mitPaste.setText("Paste grid");
            mitPaste.setMnemonic(KeyEvent.VK_P);
            mitPaste.setToolTipText("Replace the grid with the content of the clipboard");
            mitPaste.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        engine.pasteGrid();
                    } catch (AccessControlException ex) {
                        warnAccessError(ex);
                    }
                }
            });
        }
        return mitPaste;
    }

    private JMenu getToolMenu() {
        if (toolMenu == null) {
            toolMenu = new JMenu();
            toolMenu.setText("Tools");
            toolMenu.setMnemonic(java.awt.event.KeyEvent.VK_T);
            toolMenu.add(getMitResetPotentials());
            setCommand(getMitResetPotentials(), 'R');
            toolMenu.add(getMitClearHints());
            setCommand(getMitClearHints(), 'D');
            toolMenu.addSeparator();
            toolMenu.add(getMitCheckValidity());
            getMitCheckValidity().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
            toolMenu.add(getMitSolveStep());
            getMitSolveStep().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
            toolMenu.add(getMitGetNextHint());
            getMitGetNextHint().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
            toolMenu.add(getMitApplyHint());
            getMitApplyHint().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
            toolMenu.add(getMitGetAllHints());
            getMitGetAllHints().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            toolMenu.addSeparator();
            toolMenu.add(getMitGetSmallClue());
            getMitGetSmallClue().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
            toolMenu.add(getMitGetBigClue());
            getMitGetBigClue().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,
                    InputEvent.SHIFT_MASK));
            toolMenu.addSeparator();
            toolMenu.add(getMitSolve());
            getMitSolve().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
            toolMenu.add(getMitAnalyse());
            getMitAnalyse().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        }
        return toolMenu;
    }

    private JMenuItem getMitCheckValidity() {
        if (mitCheckValidity == null) {
            mitCheckValidity = new JMenuItem();
            mitCheckValidity.setText("Check validity");
            mitCheckValidity.setMnemonic(KeyEvent.VK_V);
            mitCheckValidity.setToolTipText("Check if the Sudoku has exactly one solution");
            mitCheckValidity.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (engine.checkValidity())
                        setExplanations(HtmlLoader.loadHtml(this, "Valid.html"));
                }
            });
        }
        return mitCheckValidity;
    }

    private JMenuItem getMitAnalyse() {
        if (mitAnalyse == null) {
            mitAnalyse = new JMenuItem();
            mitAnalyse.setText("Analyze");
            mitAnalyse.setMnemonic(KeyEvent.VK_Y);
            mitAnalyse.setToolTipText("List the rules required to solve the Sudoku " +
            "and get its average difficulty");
            mitAnalyse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        engine.analyse();
                    } catch (UnsupportedOperationException ex) {
                        JOptionPane.showMessageDialog(SudokuFrame.this, 
                                "The Sudoku Explainer failed to solve this Sudoku\n" +
                                "using the solving techniques that are currently enabled.",
                                "Analysis", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        return mitAnalyse;
    }

    private JMenuItem getMitSolveStep() {
        if (mitSolveStep == null) {
            mitSolveStep = new JMenuItem();
            mitSolveStep.setText("Solve step");
            mitSolveStep.setMnemonic(KeyEvent.VK_S);
            mitSolveStep.setToolTipText(getBtnApplyHintAndGet().getToolTipText());
            mitSolveStep.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.applySelectedHintsAndContinue();
                }
            });
        }
        return mitSolveStep;
    }

    private JMenuItem getMitGetNextHint() {
        if (mitGetNextHint == null) {
            mitGetNextHint = new JMenuItem();
            mitGetNextHint.setText("Get next hint");
            mitGetNextHint.setMnemonic(KeyEvent.VK_N);
            mitGetNextHint.setToolTipText(getBtnGetNextHint().getToolTipText());
            mitGetNextHint.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.getNextHint();
                }
            });
        }
        return mitGetNextHint;
    }

    private JMenuItem getMitApplyHint() {
        if (mitApplyHint == null) {
            mitApplyHint = new JMenuItem();
            mitApplyHint.setText("Apply hint");
            mitApplyHint.setEnabled(false);
            mitApplyHint.setMnemonic(KeyEvent.VK_A);
            mitApplyHint.setToolTipText(getBtnApplyHint().getToolTipText());
            mitApplyHint.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.applySelectedHints();
                }
            });
        }
        return mitApplyHint;
    }

    private JMenuItem getMitGetAllHints() {
        if (mitGetAllHints == null) {
            mitGetAllHints = new JMenuItem();
            mitGetAllHints.setText("Get all hints");
            mitGetAllHints.setMnemonic(KeyEvent.VK_H);
            mitGetAllHints.setToolTipText(getBtnGetAllHints().getToolTipText());
            mitGetAllHints.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.getAllHints();
                }
            });
        }
        return mitGetAllHints;
    }

    private JMenuItem getMitSolve() {
        if (mitSolve == null) {
            mitSolve = new JMenuItem();
            mitSolve.setText("Solve");
            mitSolve.setMnemonic(KeyEvent.VK_O);
            mitSolve.setToolTipText("Highlight the solution");
            mitSolve.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.solve();
                }
            });
        }
        return mitSolve;
    }

    private JMenuItem getMitResetPotentials() {
        if (mitResetPotentials == null) {
            mitResetPotentials = new JMenuItem();
            mitResetPotentials.setText("Reset potential values");
            mitResetPotentials.setToolTipText("Recompute the remaining possible values for the empty cells");
            mitResetPotentials.setMnemonic(java.awt.event.KeyEvent.VK_R);
            mitResetPotentials.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.resetPotentials();
                }
            });
        }
        return mitResetPotentials;
    }

    private JMenuItem getMitClearHints() {
        if (mitClearHints == null) {
            mitClearHints = new JMenuItem();
            mitClearHints.setText("Clear hint(s)");
            mitClearHints.setMnemonic(KeyEvent.VK_C);
            mitClearHints.setToolTipText("Clear the hint list");
            mitClearHints.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.clearHints();
                }
            });
        }
        return mitClearHints;
    }

    private JMenu getOptionsMenu() {
        if (optionsMenu == null) {
            optionsMenu = new JMenu();
            optionsMenu.setText("Options");
            optionsMenu.setMnemonic(java.awt.event.KeyEvent.VK_O);
            optionsMenu.add(getMitFilter());
            optionsMenu.add(getMitShowCandidates());
            optionsMenu.add(getMitSelectTechniques());
            optionsMenu.addSeparator();
            optionsMenu.add(getMitChessMode());
            optionsMenu.add(getMitMathMode());
            optionsMenu.addSeparator();
            optionsMenu.add(getMitLookAndFeel());
            optionsMenu.add(getMitAntiAliasing());
            ButtonGroup group = new ButtonGroup();
            group.add(getMitChessMode());
            group.add(getMitMathMode());
        }
        return optionsMenu;
    }

    private JCheckBoxMenuItem getMitFilter() {
        if (mitFilter == null) {
            mitFilter = new JCheckBoxMenuItem();
            mitFilter.setText("Filter hints with similar outcome");
            mitFilter.setSelected(true);
            mitFilter.setEnabled(false);
            mitFilter.setMnemonic(KeyEvent.VK_F);
            mitFilter.setToolTipText(getChkFilter().getToolTipText());
            mitFilter.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    engine.setFiltered(mitFilter.isSelected());
                }
            });
        }
        return mitFilter;
    }

    private JRadioButtonMenuItem getMitMathMode() {
        if (mitMathMode == null) {
            mitMathMode = new JRadioButtonMenuItem();
            mitMathMode.setText("R1C1 - R9C9 cell notation");
            mitMathMode.setMnemonic(KeyEvent.VK_R);
            mitMathMode.setSelected(Settings.getInstance().isRCNotation());
            mitMathMode.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (mitMathMode.isSelected()) {
                        Settings.getInstance().setRCNotation(true);
                        repaint();
                    }
                }
            });
        }
        return mitMathMode;
    }

    private JRadioButtonMenuItem getMitChessMode() {
        if (mitChessMode == null) {
            mitChessMode = new JRadioButtonMenuItem();
            mitChessMode.setText("A1 - I9 cell notation");
            mitChessMode.setMnemonic(KeyEvent.VK_A);
            mitChessMode.setSelected(!Settings.getInstance().isRCNotation());
            mitChessMode.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (mitChessMode.isSelected()) {
                        Settings.getInstance().setRCNotation(false);
                        repaint();
                    }
                }
            });
        }
        return mitChessMode;
    }

    private JCheckBoxMenuItem getMitAntiAliasing() {
        if (mitAntiAliasing == null) {
            mitAntiAliasing = new JCheckBoxMenuItem();
            mitAntiAliasing.setText("High quality rendering");
            mitAntiAliasing.setSelected(Settings.getInstance().isAntialiasing());
            mitAntiAliasing.setMnemonic(KeyEvent.VK_H);
            mitAntiAliasing.setToolTipText("Use high quality (but slow) rendering");
            mitAntiAliasing.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    Settings.getInstance().setAntialiasing(mitAntiAliasing.isSelected());
                    repaint();
                }
            });
        }
        return mitAntiAliasing;
    }

    private JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setText("Help");
            helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);
            helpMenu.add(getMitShowWelcome());
            getMitShowWelcome().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
            helpMenu.addSeparator();
            helpMenu.add(getMitAbout());
        }
        return helpMenu;
    }

    private JMenuItem getMitAbout() {
        if (mitAbout == null) {
            mitAbout = new JMenuItem();
            mitAbout.setText("About");
            mitAbout.setToolTipText("Get information about the Sudoku Explainer application");
            mitAbout.setMnemonic(java.awt.event.KeyEvent.VK_A);
            mitAbout.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (dummyFrameKnife == null) {
                        dummyFrameKnife = new JFrame();
                        ImageIcon icon = createImageIcon("Knife.gif");
                        dummyFrameKnife.setIconImage(icon.getImage());
                    }
                    AboutDialog dlg = new AboutDialog(dummyFrameKnife);
                    centerDialog(dlg);
                    dlg.setVisible(true);
                }
            });
        }
        return mitAbout;
    }

    private JMenuItem getMitGetSmallClue() {
        if (mitGetSmallClue == null) {
            mitGetSmallClue = new JMenuItem();
            mitGetSmallClue.setText("Get a small clue");
            mitGetSmallClue.setMnemonic(KeyEvent.VK_M);
            mitGetSmallClue.setToolTipText("Get some information on the next solving step");
            mitGetSmallClue.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.getClue(false);
                }
            });
        }
        return mitGetSmallClue;
    }

    private JMenuItem getMitGetBigClue() {
        if (mitGetBigClue == null) {
            mitGetBigClue = new JMenuItem();
            mitGetBigClue.setText("Get a big clue");
            mitGetBigClue.setMnemonic(KeyEvent.VK_B);
            mitGetBigClue.setToolTipText("Get more information on the next solving step");
            mitGetBigClue.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.getClue(true);
                }
            });
        }
        return mitGetBigClue;
    }

    private JMenu getMitLookAndFeel() {
        if (mitLookAndFeel == null) {
            mitLookAndFeel = new JMenu();
            mitLookAndFeel.setText("Look & Feel");
            mitLookAndFeel.setMnemonic(KeyEvent.VK_L);
            mitLookAndFeel.setToolTipText("Change the appearance of the application by choosing one of the available schemes");
        }
        return mitLookAndFeel;
    }

    private JMenuItem getMitShowWelcome() {
        if (mitShowWelcome == null) {
            mitShowWelcome = new JMenuItem();
            mitShowWelcome.setMnemonic(java.awt.event.KeyEvent.VK_W);
            mitShowWelcome.setToolTipText("Show the explanation text displayed when the application is started");
            mitShowWelcome.setText("Show welcome message");
            mitShowWelcome.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showWelcomeText();
                }
            });
        }
        return mitShowWelcome;
    }

    private JMenuItem getMitGenerate() {
        if (mitGenerate == null) {
            mitGenerate = new JMenuItem();
            mitGenerate.setText("Generate...");
            mitGenerate.setMnemonic(KeyEvent.VK_G);
            mitGenerate.setToolTipText("Open a dialog to generate a random Sudoku puzzle");
            mitGenerate.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (generateDialog == null || !generateDialog.isVisible()) {
                        generateDialog = new GenerateDialog(SudokuFrame.this, engine);
                        generateDialog.pack();
                        centerDialog(generateDialog);
                    }
                    generateDialog.setVisible(true);
                }
            });
        }
        return mitGenerate;
    }

    private void centerDialog(JDialog dlg) {
        Point frameLocation = SudokuFrame.this.getLocation();
        Dimension frameSize = SudokuFrame.this.getSize();
        Dimension windowSize = dlg.getSize();
        dlg.setLocation(
                frameLocation.x + (frameSize.width - windowSize.width) / 2,
                frameLocation.y + (frameSize.height - windowSize.height) / 3);
    }

    private JCheckBoxMenuItem getMitShowCandidates() {
        if (mitShowCandidates == null) {
            mitShowCandidates = new JCheckBoxMenuItem();
            mitShowCandidates.setText("Show candidates");
            mitShowCandidates.setToolTipText("Display all possible values as small digits in empty cells");
            mitShowCandidates.setMnemonic(KeyEvent.VK_C);
            mitShowCandidates.setSelected(Settings.getInstance().isShowingCandidates());
            mitShowCandidates.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    Settings.getInstance().setShowingCandidates(mitShowCandidates.isSelected());
                    repaint();
                }
            });
        }
        return mitShowCandidates;
    }

    private JMenuItem getMitSelectTechniques() {
        if (mitSelectTechniques == null) {
            mitSelectTechniques = new JMenuItem();
            mitSelectTechniques.setMnemonic(KeyEvent.VK_T);
            mitSelectTechniques.setToolTipText("Open a dialog window to enable and disable individual solving techniques");
            mitSelectTechniques.setText("Solving techniques...");
            mitSelectTechniques.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectTechniques();
                }
            });
        }
        return mitSelectTechniques;
    }

    private void selectTechniques() {
        if (selectDialog == null || !selectDialog.isVisible()) {
            selectDialog = new TechniquesSelectDialog(this, SudokuFrame.this.engine);
            selectDialog.pack();
            centerDialog(selectDialog);
        }
        selectDialog.setVisible(true);
        refreshSolvingTechniques();
        engine.rebuildSolver();
    }

    private JPanel getPnlEnabledTechniques() {
        if (pnlEnabledTechniques == null) {
            FlowLayout flowLayout1 = new FlowLayout();
            flowLayout1.setAlignment(FlowLayout.LEFT);
            lblEnabledTechniques = new JLabel();
            lblEnabledTechniques.setToolTipText("<html><body>Not all the available solving techniques are enabled.<br>Use the <b>Options</b>-&gt;<b>Solving techniques</b> menu to<br>enable or disable individual solving techniques.</body></html>");
            lblEnabledTechniques.setIcon(new ImageIcon(getClass().getResource("/diuf/sudoku/gui/Warning.gif")));
            lblEnabledTechniques.setText("");
            lblEnabledTechniques.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        selectTechniques();
                    }
                }
            });
            pnlEnabledTechniques = new JPanel();
            pnlEnabledTechniques.setLayout(flowLayout1);
            pnlEnabledTechniques.add(lblEnabledTechniques, null);
            pnlEnabledTechniques.setVisible(false);
        }
        return pnlEnabledTechniques;
    }

    void quit() {
        SudokuFrame.this.setVisible(false);
        SudokuFrame.this.dispose();
        if (selectDialog != null)
            selectDialog.dispose();
        if (generateDialog != null)
            generateDialog.dispose();
        if (dummyFrameKnife != null)
            dummyFrameKnife.dispose();
    }

}
