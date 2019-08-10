package diuf.sudoku.tools;

import java.util.*;

/**
 * Factory for <code>BitSet</code>s containing only
 * one element.
 */
public class SingletonBitSet {

    public static BitSet create(int value) {
        BitSet result = new BitSet(10);
        result.set(value);
        return result;
    }

}
