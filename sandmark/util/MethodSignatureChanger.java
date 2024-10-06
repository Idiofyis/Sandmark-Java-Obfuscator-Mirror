package sandmark.util;



/**
 * Abstract super class for a number of simple method obfuscators. All these
 * obfuscators operate on specified method M (from a particular class C) and
 * change its signature in the process. If M is implemented by other classes in
 * C's inheritance tree (i.e. M is part of the interface of several classes in
 * C's hierarchy), then all the other implementations of M in these other
 * classes must also be fixed automatically.
 *
 * This base super class provides a pattern and common methods for performing
 * this kind of obfuscation. Subclasses only need to implement certain key
 * abstract methods to provide an implementation for their particular
 * obfuscation.
 *
 * @author Srinivas Visvanathan
 *
 */

abstract public class MethodSignatureChanger
{
    private static sandmark.program.Method mg;
    private static sandmark.program.Class cg;
    private static sandmark.program.Application app;
    private static sandmark.analysis.classhierarchy.ClassHierarchy ch;
    private static sandmark.util.MethodID mID;


    /** Applies the obfuscation on the given method meth. The obfuscation is
     * automatically applied on all other implementations of meth in the same
     * inheritance tree */
    final public boolean apply(sandmark.program.Method meth) throws Exception
    {
        if (meth.isNative())
            return false;

        this.mg = meth;
        cg = mg.getEnclosingClass();
        app = cg.getApplication();

        app.mark();//kheffner possible bugs in obfuscations caused by a stale ch
        ch = app.getHierarchy();
        mID = new sandmark.util.MethodID(mg);



        if(ch.overridesLibraryMethod(mID))
            return false;
                //if (ch.overridesLibraryMethod(mID))
                //    return false;


        if (!customInit(mg))
            return false;

        doIt();

        return true;
    }

    /* Subclass obfuscators must implement this method which will be called by
     * the "apply" method. They should perform custom initialization and error
     * checking in this method. If the obfuscation cannot be applied for some
     * reason (i.e. error checks fail), this method should return false. "apply"
     * will terminate. Otherwise true should be returned and "apply" will
     * continue. */
    abstract protected boolean customInit(sandmark.program.Method meth);


    /* actual obfuscation routine */
    private void doIt()
    {
        //find all classes that define/declare mg
        java.util.Set rcvrs =
            sandmark.util.MethodReceiver.findMethodReceivers(mg);

        java.util.Set rNames = new java.util.HashSet();
        java.util.Iterator it = rcvrs.iterator();
        while (it.hasNext()) {
            sandmark.program.Class cls = (sandmark.program.Class)it.next();
            rNames.add(cls.getName());
        }
        //find all classes that can invoke mg
        java.util.List callers =
            sandmark.util.MethodCaller.findMethodCallers(mg,rcvrs);

        //fix stuff
        fixMethodImplementations(rcvrs);
        fixMethodInvokations(callers,rNames);
    }

    /* Goes through set of classes that define/declate mg and fixes them */
    private void fixMethodImplementations(java.util.Set rcvrs)
    {
        java.util.Iterator it = rcvrs.iterator();
        while (it.hasNext()) {
            sandmark.program.Class cls = (sandmark.program.Class)it.next();
            sandmark.program.Method meth = cls.getMethod(mID.getName(),mID.getSignature());

            if (meth == null) continue;

            //cls declares mID, fix its signature
            fixMethodSignature(meth);

            if (meth.isAbstract()) continue;

            //also defines it, fix the code
            fixMethodCode(meth);
        }
    }

    /* Should be implemented by the subclass obfuscators. This method is invoked
     * to fix the signture (i.e. argument and return types) of the given method
     * meth */
    abstract protected void fixMethodSignature(sandmark.program.Method meth);

    /* Should be implementated by subclass obfuscators. This method is invoked
     * to fix the code of a subclass method meth */
    abstract protected void fixMethodCode(sandmark.program.Method meth);


    /* Goes through list of callers that can invoke mID and fixes the
     * invokations */
    private void fixMethodInvokations(java.util.List callers, java.util.Set rNames)
    {
        java.util.Iterator citr = callers.iterator();
        while (citr.hasNext()) {
            sandmark.program.Class cls = (sandmark.program.Class)citr.next();
            java.util.Iterator mitr = cls.methods();
            while (mitr.hasNext()) {
                sandmark.program.Method meth = (sandmark.program.Method)mitr.next();
                fixInvokations(meth,rNames);
            }
        }
    }


    /* Looks for invokations to the methods being fixed in the class meth */
    private void fixInvokations(sandmark.program.Method meth, java.util.Set rNames)
    {
        org.apache.bcel.generic.InstructionList il = meth.getInstructionList();
        if (il == null) return;
        org.apache.bcel.generic.InstructionHandle ih = il.getStart();
        org.apache.bcel.generic.ConstantPoolGen cpg = meth.getCPG();
        org.apache.bcel.generic.InstructionFactory iF =
            new org.apache.bcel.generic.InstructionFactory(cpg);

        //name and sig of method invokation we are looking for
        String mName = mID.getName();
        String mSig = mID.getSignature();

        for (; ih != null; ih = ih.getNext())
        {
            org.apache.bcel.generic.Instruction in = ih.getInstruction();

            //look for invokes only
            if ( !(in instanceof org.apache.bcel.generic.InvokeInstruction) )
                continue;

            org.apache.bcel.generic.InvokeInstruction inv =
                (org.apache.bcel.generic.InvokeInstruction)in;

            //ensure name & sig of method being invoked match mName & mSig
            if (!mName.equals(inv.getMethodName(cpg)) ||
                    !mSig.equals(inv.getSignature(cpg)))
                continue;

            //ensure class to which invoke is being sent is among the receivers
            //of mID
            if (!rNames.contains(inv.getClassName(cpg)))
                continue;

            //fix the invoke instruction
            ih = fixInvoke(ih,il,iF,cpg,meth);
        }
    }


    /* This method is called to fix an invokation to a method that's being fixed
     * in the method meth. A number of additional parameters are passed which
     * must be used while fixing the invokation:
     *
     * Parameters are:
     *  ih - handle of invoke instruction
     *  il - instruction list of meth's instructions
     *  iF - factory that should be used to create instructions
     *  cpg - constant pool of meth's class
     *  meth - method containing the invoke
     *
     *  The implementation must return the instruction handle of the last
     *  instruction it creates/modifies so that fixInvokations can look for
     *  invokes from the next instruction onwards. */
    abstract protected org.apache.bcel.generic.InstructionHandle
        fixInvoke(  org.apache.bcel.generic.InstructionHandle ih,
                    org.apache.bcel.generic.InstructionList il,
                    org.apache.bcel.generic.InstructionFactory iF,
                    org.apache.bcel.generic.ConstantPoolGen cpg,
                    sandmark.program.Method meth
                 );


    /* Useful method that computes the local variable array indices of the for
     * the argument list AT. isStatic is used to specify whether the method
     * containing these arguments is static or not. Returns an integer array
     * containing the indices of the args in AT */
    final protected int[] computeIndices(org.apache.bcel.generic.Type AT[],
                                    boolean isStatic)
    {
        int indices[] = new int[AT.length];
        int idx = isStatic ? 0 : 1;

        for (int jj = 0; jj < AT.length; jj++)
        {
            indices[jj] = idx;
            idx += AT[jj].getSize();
        }

        return indices;
    }
}
