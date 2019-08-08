/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import diuf.sudoku.*;

public class TechniquesSelectDialog extends JDialog {

    private static final long serialVersionUID = -7071292711961723801L;

    private final SudokuExplainer engine;

    private JPanel jPanel = null;
    private JPanel buttonPanel = null;
    private JPanel okButtonPanel = null;
    private JButton btnOk = null;
    private JPanel cancelButtonPanel = null;
    private JButton btnCancel = null;
    private JPanel textPanel = null;
    private JLabel lblExplanations = null;
    private JPanel methodsPanel = null;

    private EnumSet<SolvingTechnique> selectedTechniques;


    public TechniquesSelectDialog(JFrame parent, SudokuExplainer engine) {
        super(parent);
        this.engine = engine;
        initialize();
        fillTechniques();
    }

    private void fillTechniques() {
        selectedTechniques = Settings.getInstance().getTechniques();
        for (final SolvingTechnique technique : SolvingTechnique.class
                .getEnumConstants()) {
            final JCheckBox chkSelect = new JCheckBox();
            chkSelect.setText(technique.toString());
            chkSelect.setSelected(selectedTechniques.contains(technique));
            chkSelect.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (chkSelect.isSelected())
                        selectedTechniques.add(technique);
                    else
                        selectedTechniques.remove(technique);
                }
            });
            methodsPanel.add(chkSelect);
        }
    }

    private void initialize() {
        this.setTitle("Solving Techniques Selection");
        this.setResizable(false);
        this.setContentPane(getJPanel());
        this.setModal(true);
    }

    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getButtonPanel(), BorderLayout.SOUTH);
            jPanel.add(getTextPanel(), BorderLayout.NORTH);
            jPanel.add(getMethodsPanel(), BorderLayout.CENTER);
        }
        return jPanel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            gridLayout.setColumns(2);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(gridLayout);
            buttonPanel.add(getOkButtonPanel(), null);
            buttonPanel.add(getCancelButtonPanel(), null);
        }
        return buttonPanel;
    }

    private JPanel getOkButtonPanel() {
        if (okButtonPanel == null) {
            okButtonPanel = new JPanel();
            okButtonPanel.setLayout(new FlowLayout());
            okButtonPanel.add(getBtnOk(), null);
        }
        return okButtonPanel;
    }

    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText("OK");
            btnOk.setMnemonic(KeyEvent.VK_O);
            btnOk.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!selectedTechniques.contains(SolvingTechnique.HiddenSingle)
                            && !selectedTechniques.contains(SolvingTechnique.NakedSingle)) {
                        int result = JOptionPane
                        .showConfirmDialog(
                                TechniquesSelectDialog.this,
                                "Warning: you have disabled both Hidden Single and Naked Single.\n"
                                + "Without them, the Sudoku Explainer will fail to solve most Sudokus.\n"
                                + "Do you want to continue anyway?",
                                TechniquesSelectDialog.this.getTitle(),
                                JOptionPane.YES_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (result != JOptionPane.YES_OPTION)
                            return;
                    }
                    Settings.getInstance().setTechniques(selectedTechniques);
                    TechniquesSelectDialog.this.setVisible(false);
                    TechniquesSelectDialog.this.dispose();
                    engine.clearHints();
                }
            });
        }
        return btnOk;
    }

    private JPanel getCancelButtonPanel() {
        if (cancelButtonPanel == null) {
            cancelButtonPanel = new JPanel();
            cancelButtonPanel.setLayout(new FlowLayout());
            cancelButtonPanel.add(getBtnCancel(), null);
        }
        return cancelButtonPanel;
    }

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText("Cancel");
            btnCancel.setMnemonic(KeyEvent.VK_C);
            btnCancel.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    TechniquesSelectDialog.this.setVisible(false);
                    TechniquesSelectDialog.this.dispose();
                }
            });
        }
        return btnCancel;
    }

    private JPanel getTextPanel() {
        if (textPanel == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            lblExplanations = new JLabel();
            lblExplanations.setText("Select the solving techniques to use:");
            lblExplanations
            .setToolTipText("Solving techniques that are not selected will not be used when solving or analyzing a Sudoku");
            textPanel = new JPanel();
            textPanel.setLayout(flowLayout);
            textPanel.add(lblExplanations, null);
        }
        return textPanel;
    }

    private JPanel getMethodsPanel() {
        if (methodsPanel == null) {
            TitledBorder titledBorder = BorderFactory.createTitledBorder(null,
                    "Available solving techniques", TitledBorder.CENTER,
                    TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12),
                    new Color(51, 51, 51));
            titledBorder.setBorder(null);
            titledBorder.setTitle("");
            GridLayout gridLayout1 = new GridLayout();
            gridLayout1.setColumns(3);
            gridLayout1.setRows(0);
            methodsPanel = new JPanel();
            methodsPanel.setLayout(gridLayout1);
            methodsPanel.setBorder(titledBorder);
        }
        return methodsPanel;
    }

}
