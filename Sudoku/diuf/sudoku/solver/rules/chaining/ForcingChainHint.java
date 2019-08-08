/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.chaining;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Forcing Chain (simple) hint.
 */
public class ForcingChainHint extends ChainingHint {

    private final Potential target;

    // Cache
    private int _complexity = -1;


    public ForcingChainHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            boolean isYChain, boolean isXChain, Potential target) {
        super(rule, removablePotentials, isYChain, isXChain);
        this.target = target;
    }

    @Override
    public int getFlatViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {target.cell};
    }

    private Map<Cell, BitSet> getColorPotentials(boolean state) {
        return getColorPotentials(target, state, state);
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedGreenPotentials(viewNum);
        Map<Cell, BitSet> result = getColorPotentials(true);
        if (!target.isOn)
            result.get(target.cell).clear(target.value); // Make target potential red
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedRedPotentials(viewNum);
        Map<Cell, BitSet> result = getColorPotentials(false);
        if (target.isOn)
            result.get(target.cell).clear(target.value); // Make target green
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedLinks(viewNum);
        Potential start = this.target;
        return getLinks(start);
    }

    @Override
    public int getFlatComplexity() {
        if (_complexity < 0)
            _complexity = getAncestorCount(target);
        return _complexity;
    }

    @Override
    protected Collection<Potential> getChainsTargets() {
        return Collections.singletonList(this.target);
    }

    @Override
    protected Potential getChainTarget(int viewNum) {
        return this.target;
    }

    @Override
    public int getSortKey() {
        if (isYChain && isXChain)
            return 4;
        else if (isYChain)
            return 3;
        else
            return 2;
    }

    public double getDifficulty() {
        double result;
        if (isYChain && isXChain)
            result = 7.0;
        else
            result = 6.6;
        return result + getLengthDifficulty();
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public Cell getCell() {
        if (target.isOn)
            return target.cell;
        return null;
    }

    @Override
    public int getValue() {
        return target.value;
    }

    public String getName() {
        if (isXChain && isYChain)
            return "Forcing Chain";
        else if (isYChain)
            return "Forcing Y-Chain";
        else {
            if (getAncestorCount(target) == 6)
                return "Turbot Fish";
            else
                return "Forcing X-Chain";
        }
    }

    @Override
    protected Potential getResult() {
        return target;
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
            " on the cell <b>" + target.cell.toString()
            + "</b> with the value <b>" + target.value + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() + ": " + target.toString()
        + (target.isOn ? " on" : " off");
    }

    @Override
    public String toHtml() {
        String fileName = (isYChain ? "ForcingChain.html" : "ForcingXChain.html");
        String result = HtmlLoader.loadHtml(this, fileName);
        Potential reverse = new Potential(target.cell, target.value, !target.isOn);
        String assumption = reverse.toWeakString();
        String consequence = target.toStrongString();
        String conclusion = target.toWeakString();
        String htmlChain = getHtmlChain(target);
        String commonName = "";
        if (isXChain && !isYChain && getAncestorCount(target) == 6) // Note that first potential occurs twice in the chain
            commonName = "(Turbot Fish)";
        result = HtmlLoader.format(result, assumption, consequence, conclusion, htmlChain,
                commonName);
        return super.appendNestedChainsDetails(result);
    }

}