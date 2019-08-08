/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.chaining;

import java.util.*;

import diuf.sudoku.*;

/**
 * A potential position for a value and an "on"/"off" flag.
 * Actually a ({@link Cell}, value, on/off) triplet.
 * <p>
 * Optionally stores a list of parent potentials, and an explanation.
 * The parents are the potentials from which this one was
 * deduced. The full chain can be found from the last item of the chain using
 * the parent references. Hence this class is also used to represent an entire chain.
 */
public class Potential {

    public enum Cause {
        NakedSingle,
        HiddenBlock,
        HiddenRow,
        HiddenColumn,
        Advanced
    }

    public final Cell cell;
    public final int value;
    public final boolean isOn;
    public final List<Potential> parents = new ArrayList<Potential>(1);
    public final String explanation;
    public final Cause cause;
    public final ChainingHint nestedChain;


    public Potential(Cell cell, int value, boolean isOn) {
        this.cell = cell;
        this.value = value;
        this.isOn = isOn;
        this.explanation = null;
        this.cause = null;
        this.nestedChain = null;
    }

    public Potential(Cell cell, int value, boolean isOn, Cause cause, String explanation) {
        this.cell = cell;
        this.value = value;
        this.isOn = isOn;
        this.cause = cause;
        this.explanation = explanation;
        this.nestedChain = null;
    }

    public Potential(Cell cell, int value, boolean isOn, Cause cause, String explanation,
            ChainingHint nestedChain) {
        this.cell = cell;
        this.value = value;
        this.isOn = isOn;
        this.cause = cause;
        this.explanation = explanation;
        this.nestedChain = nestedChain;
    }

    public Potential(Cell cell, int value, boolean isOn, Potential parent,
            Cause cause, String explanation) {
        this.cell = cell;
        this.value = value;
        this.isOn = isOn;
        this.parents.add(parent);
        this.cause = cause;
        this.explanation = explanation;
        this.nestedChain = null;
    }

    public void off() {
        this.cell.removePotentialValue(this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Potential))
            return false;
        Potential other = (Potential)o;
        return this.cell.equals(other.cell) && this.value == other.value
        && this.isOn == other.isOn;
    }

    @Override
    public int hashCode() {
        return ((cell.getY() * 9 + cell.getX()) * 9 + value) * 2 + (isOn ? 1 : 0);
    }

    @Override
    public String toString() {
        return cell.toString() + "." + value;
    }

    public String toWeakString() {
        return cell.toString() + (isOn ? " contains " : " does not contain ") +
            "the value " + value;
    }

    public String toStrongString() {
        return cell.toString() + (isOn ? " must contain " : " cannot contain ") +
            "the value " + value;
    }

}