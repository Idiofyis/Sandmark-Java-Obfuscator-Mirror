package sandmark.watermark.util;

/**
 * This class marks basic blocks with a one-bit value
 * by manipulating the number of instructions in the block.
 * An even number of instructions corresponds to 0; an odd
 * number corresponds to 1.
 *
 * @see PutstaticCountMarker
 */

public class InsnCountMarker extends StaticWriteParityMarker {
    public InsnCountMarker(sandmark.program.Class clazz) {
	super(clazz, false);
    }

    /**
     * Returns either 0 or 1, based on whether the number of instructions
     * in the given basic block is odd or even.
     */
    protected int getParity(sandmark.analysis.controlflowgraph.BasicBlock b) {
	return b.getInstList().size() % 2;
    }
}

