/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.gui;

import java.util.*;

import diuf.sudoku.solver.*;

/**
 * Builder for the hints tree. This class is responsible
 * for the classification of hints in groups, and counting
 * the number of hints in groups.
 */
public class HintsTreeBuilder {

    private HintNode _root = new HintNode("Hints");
    private HintNode _directHintsNode = null;
    private HintNode _warningHintsNode = null;
    private HintNode _indirectHintsNode = null;

    private HintNode root() {
        return _root;
    }

    private HintNode directHintsNode() {
        if (_directHintsNode == null) {
            _directHintsNode = new HintNode("Sudoku Rules");
            root().insert(_directHintsNode, 0);
        }
        return _directHintsNode;
    }

    private HintNode warningHintsNode() {
        if (_warningHintsNode == null) {
            _warningHintsNode = new HintNode("Informations");
            root().add(_warningHintsNode);
        }
        return _warningHintsNode;
    }

    private HintNode indirectHintsNode() {
        if (_indirectHintsNode == null) {
            _indirectHintsNode = new HintNode("Solving Techniques");
            root().add(_indirectHintsNode);
        }
        return _indirectHintsNode;
    }

    public HintNode buildHintsTree(List<Hint> hints) {
        Map<String,HintNode> directHintsNodes = new HashMap<String,HintNode>();
        Map<String,HintNode> warningHintsNodes = new HashMap<String,HintNode>();
        Map<String,HintNode> indirectHintsNodes = new HashMap<String,HintNode>();
        for (Hint hint : hints) {
            if (hint instanceof DirectHint) {
                DirectHint directHint = (DirectHint)hint;
                String producerName = directHint.getRule().toString();
                HintNode parent = directHintsNodes.get(producerName);
                if (parent == null) {
                    parent = new HintNode(producerName);
                    directHintsNode().add(parent);
                    directHintsNodes.put(producerName, parent);
                }
                HintNode node = new HintNode(hint);
                parent.add(node);
            } else if (hint instanceof WarningHint) {
                WarningHint iHint = (WarningHint)hint;
                String producerName = iHint.getRule().toString();
                HintNode parent = warningHintsNodes.get(producerName);
                if (parent == null) {
                    parent = new HintNode(producerName);
                    warningHintsNode().add(parent);
                    warningHintsNodes.put(producerName, parent);
                }
                HintNode node = new HintNode(hint);
                parent.add(node);
            } else {
                IndirectHint iHint = (IndirectHint)hint;
                String producerName = iHint.getRule().toString();
                HintNode parent = indirectHintsNodes.get(producerName);
                if (parent == null) {
                    parent = new HintNode(producerName);
                    indirectHintsNode().add(parent);
                    indirectHintsNodes.put(producerName, parent);
                }
                HintNode node = new HintNode(hint);
                parent.add(node);
            }
        }
        if (_root != null)
            _root.appendCountChildrenToName();
        if (_directHintsNode != null)
            _directHintsNode.appendCountChildrenToName();
        if (_indirectHintsNode != null)
            _indirectHintsNode.appendCountChildrenToName();
        for (HintNode topNode : directHintsNodes.values())
            topNode.appendCountChildrenToName();
        for (HintNode topNode : indirectHintsNodes.values())
            topNode.appendCountChildrenToName();
        return _root;
    }

}
