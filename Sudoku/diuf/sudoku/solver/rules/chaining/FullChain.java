/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.chaining;

import java.util.*;

/**
 * Wraps a single chaining hint, and change the
 * behavior of <tt>equals()</tt> and <tt>hashCode</tt>
 * so that they consider the entire chains instead of
 * just the outcome.
 */
public class FullChain {

    private final ChainingHint chain;


    public FullChain(ChainingHint target) {
        this.chain = target;
    }

    public ChainingHint get() {
        return this.chain;
    }

    @Override
    public boolean equals(Object o) {
        // Maybe we could just compare chain.toString()!?
        if (o instanceof FullChain) {
            FullChain other = (FullChain)o;
            /*
             * Some returned collections may not be lists and may not implement equals correctly.
             * Wrap the content in an array list.
             */
            Collection<Potential> thisTargets = new ArrayList<Potential>(this.chain.getChainsTargets());
            Collection<Potential> otherTargets = new ArrayList<Potential>(other.chain.getChainsTargets());
            if (!thisTargets.equals(otherTargets))
                return false;
            Iterator<Potential> i1 = thisTargets.iterator();
            Iterator<Potential> i2 = otherTargets.iterator();
            while (i1.hasNext() && i2.hasNext()) {
                Potential p1 = i1.next();
                Potential p2 = i2.next();
                if (!this.chain.getChain(p1).equals(other.chain.getChain(p2)))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Potential target : this.chain.getChainsTargets()) {
            for (Potential p : this.chain.getChain(target))
                result ^= p.hashCode();
        }
        return result;
    }

}
