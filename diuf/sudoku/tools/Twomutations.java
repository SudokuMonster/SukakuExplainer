package diuf.sudoku.tools;

/**
 * Generator of binary permutations.
 * <p>
 * Given a length <tt>countBits</tt> and a
 * degree <tt>countOnes</tt> with
 * <tt>countOnes <= countBits</tt>, this class will compute
 * all binary numbers of length <tt>countBits</tt> that have
 * exactly <tt>countOnes</tt> bits equal to <tt>1</tt>.
 * <p>
 * The binary numbers are generated in increasing order.
 * <p>
 * Example: with <tt>countBits = 5</tt> and <tt>countOnes = 3</tt>
 * the following binary numbers are generated:
 * <ul>
 * <li>00111
 * <li>01011
 * <li>01101
 * <li>01110
 * <li>10011
 * <li>10101
 * <li>10110
 * <li>11001
 * <li>11010
 * <li>11100
 * </ul>
 * Code adapted from "Hacker's Delight" by Henry S. Warren,
 * ISBN 0-201-91465-4
 */
public class Twomutations {

    private final int countBits;
    private final int countOnes;

    private int n1;
    private int n2;

    private boolean isLast;

    /**
     * Create a new binary permutations generator.
     * <p>
     * The maximum supported value for <code>countBits</code>
     * is 64. <code>countOnes</code> must be equal or less than
     * <code>countBits</code>.
     * @param countOnes the number of bits equal to one
     * @param countBits the length of the binary numbers in bits
     */
    public Twomutations(int countOnes, int countBits) {
        if (countOnes < 0)
            throw new IllegalArgumentException("countOnes < 0");
        if (countBits < 0)
            throw new IllegalArgumentException("countBits < 0");
        if (countOnes > countBits)
            throw new IllegalArgumentException("countOnes > countBits");
        if (countBits > 81)
            throw new IllegalArgumentException("countBits > 81");
        this.countBits = countBits;
        this.countOnes = countOnes;
        this.n1 = 0;
        this.n2 = 1;
        this.isLast = (countBits == n2);
    }

    /**
     * Test if there are more permutations available
     * @return whether there are more permutations available
     */
    public boolean hasNext() {
        return !isLast;
    }

    /**
     * Get the next binary permutation.
     * @return the next binary permutation
     */
    public void next() {
        n1++;
        if ( n1 == n2 )
        {
           n1 = 0;
           n2++;
        }
        isLast = (countBits == n2);
    }

    /**
     * Get the next binary permutation as an array
     * of bit indexes.
     * @return the 0-based indexes of the bits that are set
     * to one.
     */
    public int[] nextBitNums() {
        int[] result = new int[countOnes];
        result[ 0] = n1;
        result[ 1] = n2;
        next();
        return result;
    }

}