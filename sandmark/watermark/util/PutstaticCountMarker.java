package sandmark.watermark.util;

/**
 * This class embeds a one-bit value in a basic block by
 * manipulating the number of distinct static fields written
 * by the basic block.  An even number of distinct fields
 * corresponds to 0; an odd number corresponds to 1.  Basic
 * blocks can be marked with this class without destroying
 * previous marks made by an
 * {@link InsnCountMarker InsnCountMarker}.
 */

public class PutstaticCountMarker extends StaticWriteParityMarker {
    public PutstaticCountMarker(sandmark.program.Class clazz) {
	super(clazz, true);
    }

    /**
     * Returns either 0 or 1, based on whether the number of unique
     * static fields written to in the given basic block is odd or
     * even.
     */
    protected int getParity(sandmark.analysis.controlflowgraph.BasicBlock b) {
	java.util.HashSet s = new java.util.HashSet();
	java.util.Iterator i = b.getInstList().iterator();
	sandmark.program.Method method = findMethod(b);
	boolean isStatic = method.isStatic() || method.getName().equals("<init>");
	while (i.hasNext()) {
	    org.apache.bcel.generic.Instruction insn = 
		((org.apache.bcel.generic.InstructionHandle)i.next()).getInstruction();
	    if (isStatic) {
	       if (insn instanceof org.apache.bcel.generic.PUTSTATIC)
		  s.add(new Integer(((org.apache.bcel.generic.PUTSTATIC)insn).getIndex()));
	    }
	    else {
	       if (insn instanceof org.apache.bcel.generic.PUTFIELD)
		  s.add(new Integer(((org.apache.bcel.generic.PUTFIELD)insn).getIndex()));
	    }
	}

	return s.size() % 2;
    }
}

