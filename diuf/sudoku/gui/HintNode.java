/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.gui;

import javax.swing.tree.*;

import diuf.sudoku.solver.*;

/**
 * A tree node representing a hint in the hints tree
 * of the user interface.
 */
public class HintNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 7857073221166387482L;

    private final Hint hint;
    private String name;

    public HintNode(Hint hint) {
        super();
        this.hint = hint;
        this.name = hint.toString();
    }

    HintNode(String name) {
        super();
        this.hint = null;
        this.name = name;
    }

    public Hint getHint() {
        return this.hint;
    }

    public String getName() {
        return this.name;
    }

    public boolean isHintNode() {
        return this.hint != null;
    }

    @Override
    public boolean getAllowsChildren() {
        return !isHintNode();
    }

    private int getCountHints() {
        if (isHintNode())
            return 1;
        else {
            int result = 0;
            for (int i = 0; i < super.getChildCount(); i++) {
                HintNode child = (HintNode)super.getChildAt(i);
                result += child.getCountHints();
            }
            return result;
        }
    }

    public HintNode getNodeFor(Hint hint) {
        if (hint == null)
            return null;
        if (hint.equals(this.hint))
            return this;
        for (int i = 0; i < getChildCount(); i++) {
            HintNode child = (HintNode)getChildAt(i);
            HintNode result = child.getNodeFor(hint);
            if (result != null)
                return result;
        }
        return null;
    }

    void appendCountChildrenToName() {
        int count = getCountHints();
        if (count <= 1)
            this.name += " (" + count + " hint)";
        else
            this.name += " (" + count + " hints)";
    }

    @Override
    public String toString() {
        return this.name;
    }

}
