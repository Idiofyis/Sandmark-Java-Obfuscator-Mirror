package sandmark.watermark.util;

/**
 * Subclasses of this class are used to encode integral values in
 * basic blocks.
 *
 * @see sandmark.analysis.controlflowgraph.BasicBlock
 */

public abstract class BasicBlockMarker {

    /**
     * Attempts to encode the given value in the given basic block.  The
     * basic block is modified in place.  If the number of bits in the given
     * value exceeds that of the value returned by 
     * {@link #getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)}, 
     * an {@link IllegalArgumentException} will be thrown.
     *
     * @param b basic block to encode data in
     * @param value data to encode
     * @throws IllegalArgumentException if value has too many bits
     * @see #getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)
     */
    public abstract void embed(sandmark.analysis.controlflowgraph.BasicBlock b,
			       java.math.BigInteger value);

    /**
     * Attempts to encode the given value in the given basic block.  The
     * basic block is modified in place.  If the number of bits in the given
     * value exceeds that of the value returned by 
     * {@link #getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)}, 
     * an {@link IllegalArgumentException} will be thrown.
     * <br><br>
     * This method is implemented with a call to
     * {@link #embed(sandmark.analysis.controlflowgraph.BasicBlock,
     *               java.math.BigInteger)}.
     *
     * @param b basic block to encode data in
     * @param value data to encode
     * @throws IllegalArgumentException if value has too many bits
     * @see #getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)
     */
    public final void embed(sandmark.analysis.controlflowgraph.BasicBlock b,
			    long value) {
	embed(b, java.math.BigInteger.valueOf(value));
    }

    /**
     * Returns an {@link java.util.Iterator} over all values 
     * found to be embedded in the
     * given basic block.  Only values embedded using the marking scheme
     * used by this marker will be reported.
     *
     * @param b basic block to search for marks in
     */
    public abstract java.util.Iterator recognize(sandmark.analysis.controlflowgraph.BasicBlock b);

    /**
     * Returns the number of bits that can be encoded into the given basic block.
     *
     * @param b basic block to report the bit capacity of
     * @see #embed(sandmark.analysis.controlflowgraph.BasicBlock, 
     *             java.math.BigInteger)
     * @see #embed(sandmark.analysis.controlflowgraph.BasicBlock, long)
     */
    public abstract int getCapacity(sandmark.analysis.controlflowgraph.BasicBlock b);

    /**
     * Returns true if this class returns the same capacity for any basic block,
     * including null.
     *
     * @see #getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)
     */
    boolean capacityIsConstant() {
	return false;
    }

}

