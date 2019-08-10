/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.tools;

import java.util.*;

/**
 * Heart engine for the Naked Sets, Hidden Sets and N-Fishes rules.
 */
public class CommonTuples {

    /**
     * Given an array of bitsets, and a degree, check if
     * <ul>
     * <li>All bitsets have cardinality greater than one
     * <li>The union of all bitsets has a cardinality of <code>degree</code>
     * <li>(Implied) all bitsets have a cardinality less than or equal to <code>degree</code>
     * </ul>
     * If this is the case, the union of all bitsets is returned.
     * If this is not the case, <code>null</code> is returned.
     * @param candidates the array of bitsets
     * @param degree the degree
     * @return the intersection of all bitsets, or <code>null</code>
     */
    public static BitSet searchCommonTuple(BitSet[] candidates, int degree) {
        BitSet result = new BitSet(9);
        for (BitSet candidate : candidates) {
            if (candidate.cardinality() <= 1)
                return null;
            result.or(candidate);
        }
        if (result.cardinality() == degree)
            return result;
        return null;
    }

    /**
     * Same as before, but all bitsets must only have non-zero
     * cardinality instead of grater than one.
     * (Used for Unique Loops and BUGs type 3)
     */
    public static BitSet searchCommonTupleLight(BitSet[] candidates, int degree) {
        BitSet result = new BitSet(9);
        for (BitSet candidate : candidates) {
            result.or(candidate);
            if (candidate.cardinality() == 0)
                return null;
        }
        if (result.cardinality() == degree)
            return result;
        return null;
    }

}
