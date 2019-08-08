/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.chaining;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Double Forcing Chain or Contradiction Forcing Chain hint.
 * Consist of two forcing chains.
 */
public class BinaryChainingHint extends ChainingHint {

    private final Potential srcPotential;
    private final Potential dstOnPotential;
    private final Potential dstOffPotential;
    private final boolean isAbsurd;
    private final boolean isNishio;

    // Cache
    private int _complexity = -1;

    public BinaryChainingHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Potential srcPotential, Potential fromOnPotential, Potential fromOffPotential,
            boolean isAbsurd, boolean isNishio) {
        super(rule, removablePotentials, true, true);
        this.srcPotential = srcPotential;
        this.dstOnPotential = fromOnPotential;
        this.dstOffPotential = fromOffPotential;
        this.isAbsurd = isAbsurd;
        this.isNishio = isNishio;
    }

    @Override
    public int getFlatViewCount() {
        return 2;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {srcPotential.cell, dstOnPotential.cell};
    }

    private Map<Cell, BitSet> getColorPotentials(int viewNum, boolean state) {
        return getColorPotentials(
                (viewNum == 0 ? this.dstOnPotential : this.dstOffPotential), state, state);
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedGreenPotentials(viewNum);
        return getColorPotentials(viewNum, true);
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedRedPotentials(viewNum);
        return getColorPotentials(viewNum, false);
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedLinks(viewNum);
        Potential start = (viewNum == 0 ? this.dstOnPotential : this.dstOffPotential);
        return getLinks(start);
    }

    @Override
    protected Collection<Potential> getChainsTargets() {
        Collection<Potential> result = new ArrayList<Potential>(2);
        result.add(this.dstOnPotential);
        result.add(this.dstOffPotential);
        return result;
    }

    @Override
    protected Potential getChainTarget(int viewNum) {
        if (viewNum == 0)
            return this.dstOnPotential;
        else
            return this.dstOffPotential;
    }

    @Override
    public int getFlatComplexity() {
        if (_complexity < 0)
            _complexity = getAncestorCount(dstOnPotential) + getAncestorCount(dstOffPotential);
        return _complexity;
    }

    @Override
    public int getSortKey() {
        if (isAbsurd)
            return 7; // After all reductions
        else
            return 1;
    }

    public boolean isAbsurd() {
        return this.isAbsurd;
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    public double getDifficulty() {
        return getChainingRule().getDifficulty() + getLengthDifficulty();
    }

    public String getName() {
        String result;
        if (isNishio)
            result = "Forcing";
        else if (isAbsurd)
            result = "Contradiction Forcing";
        else
            result = "Double Forcing";
        return super.getNamePrefix() + result + super.getNameSuffix();
    }

    @Override
    protected Potential getResult() {
        if (isNishio || isAbsurd)
            return new Potential(srcPotential.cell, srcPotential.value, !srcPotential.isOn);
        else
            return dstOnPotential;
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " starting on the cell <b>" + srcPotential.cell.toString()
                    + "</b> with the value <b>" + srcPotential.value + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        if (isNishio)
            return "Nishio Forcing Chain: " + srcPotential.toString() + (srcPotential.isOn ? " on" : " off") +
                    " ==> " + this.dstOffPotential.toString() + " both on & off";
        else if (isAbsurd)
            return "Contradiction Forcing Chain: " + srcPotential.toString() + (srcPotential.isOn ? " on" : " off") +
                    " ==> " + this.dstOffPotential.toString() + " both on & off";
        else
            return "Double Forcing Chain: " + srcPotential.toString() + " on & off ==> "
                    + this.dstOnPotential.toString() + (dstOnPotential.isOn ? " on" : " off");
    }

    @Override
    public String toHtml() {
        String result;
        if (isNishio)
            result = HtmlLoader.loadHtml(this, "NishioHint.html");
        else if (isAbsurd)
            result = HtmlLoader.loadHtml(this, "DynamicContradictionHint.html");
        else
            result = HtmlLoader.loadHtml(this, "DynamicReductionHint.html");
        Potential srcOn = new Potential(srcPotential.cell, srcPotential.value, true);
        Potential srcOff = new Potential(srcPotential.cell, srcPotential.value, false);
        Potential srcReverse = new Potential(srcPotential.cell, srcPotential.value, !srcPotential.isOn);
        String chainOn = getHtmlChain(dstOnPotential);
        String chainOff = getHtmlChain(dstOffPotential);
        if (isAbsurd)
            result = HtmlLoader.format(result, srcPotential.toWeakString(), 
                    dstOnPotential.toStrongString(), dstOffPotential.toStrongString(),
                    srcReverse.toStrongString(), chainOn, chainOff);
        else
            result = HtmlLoader.format(result, srcOn.toWeakString(), srcOff.toWeakString(),
                    dstOnPotential.toStrongString(), chainOn, chainOff);
        return super.appendNestedChainsDetails(result);
    }

}
