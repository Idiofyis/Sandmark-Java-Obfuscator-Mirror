package sandmark.obfuscate.dynamicinliner;

public class DynamicInliner extends sandmark.optimise.AppOptimizer {
    private static final boolean DEBUG = false;
    private int index, inlineCount, firstUnusedLocal;
    private sandmark.program.Method method, invokedMethod;
    private sandmark.analysis.classhierarchy.ClassHierarchy hier;
    private org.apache.bcel.generic.InstructionHandle ihs[];
    private sandmark.util.Inliner inliner;
    private java.util.Hashtable inlinedMethods;
    private sandmark.program.Application app;
    private org.apache.bcel.generic.InvokeInstruction is;
    private sandmark.program.Class clazz;
    private boolean firstTime;

    public DynamicInliner() {}

    public void apply(sandmark.program.Application application) throws Exception {
        inlineCount = 0;
        app = application;
        new sandmark.util.Publicizer().apply(app);

        hier = new sandmark.analysis.classhierarchy.ClassHierarchy(app);

        java.util.Hashtable methodNameToMethod =
            new java.util.Hashtable();
        //Make a map
        for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
            clazz = (sandmark.program.Class)classIt.next();
            for(java.util.Iterator methodIt = clazz.methods(); methodIt.hasNext();){
                sandmark.program.Method method =
                    (sandmark.program.Method)methodIt.next();
                methodNameToMethod.put
                    (sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName
                     (method),
                     method);
            }
        }
        //Go through all the classes
        for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
            clazz = (sandmark.program.Class)classIt.next();

            sandmark.program.Method methods[] = clazz.getMethods();
            //Go through all the methods
            for(int j = 0 ; j < methods.length ; j++) {
                method = methods[j];

                if(method.getInstructionList() == null)
                    continue;

                inliner = null;
                inlinedMethods = new java.util.Hashtable();
                firstUnusedLocal = method.getMaxLocals();

                org.apache.bcel.generic.InstructionHandle[] tmp =
                    method.getInstructionList().getInstructionHandles();
                ihs = new org.apache.bcel.generic.InstructionHandle[tmp.length];
                for(int i = 0; i < tmp.length; i++)
                    ihs[i] = tmp[i];

                //Go through all the instructions
                for(index = 0 ; index < ihs.length ; index++) {
                    //Check if the instruction is an invoke
                    if(ihs[index].getInstruction() instanceof
                       org.apache.bcel.generic.InvokeInstruction){
                        is = (org.apache.bcel.generic.InvokeInstruction)
                            ihs[index].getInstruction();

                        if(DEBUG)System.out.println("invoke: " + is.toString(clazz.getConstantPool().getConstantPool()));

                        //start
                        sandmark.program.Class invokedClass =
                            app.getClass(is.getClassName(clazz.getConstantPool()));
                        if(invokedClass == null)
                            continue;

                        invokedMethod =
                            invokedClass.getMethod
                            (is.getName(clazz.getConstantPool()),
                             is.getSignature(clazz.getConstantPool()));

                        if(invokedMethod == null){
                            if(DEBUG)
                                System.out.println("invoke is null, continuing");
                            continue;
                        }

                        if(invokedMethod.getInstructionList() == null){
                            if(DEBUG)
                                System.out.println(" il is null, continuing");
                            continue;
                        }

                        if(inlinedMethods.get(invokedMethod) != null){
                            if(DEBUG)
                                System.out.println(invokedMethod +
                                                  " already inlined, " +
                                                   "continuing");
                            continue;
                        }

                        if(sandmark.obfuscate.inliner.Inliner.
                           containsBadInvokes(invokedMethod)){
                            if(DEBUG)
                                System.out.println(invokedMethod +
                                                   " contains bad invoke, " +
                                                   "continuing");
                            continue;
                        }

                        if(is instanceof org.apache.bcel.generic.INVOKEVIRTUAL)
                            inlineVirtual();
                    }
                }
                method.removeNOPs();
            } //methods
        } //classes
        if(DEBUG)
            System.out.println("methods inlined: " + inlineCount);
    } //apply()


    //Inline a virtual method call, if it is accessible
    private void inlineVirtual() {
        if(DEBUG)System.out.println("hi");
        //We dont know what method to use, put all possibles
        //into a vector
        java.util.Iterator subs = hier.depthFirst
            (invokedMethod.getEnclosingClass());
        java.util.Vector possibles = new java.util.Vector();
        while(subs.hasNext()){
            sandmark.program.Class c = (sandmark.program.Class)subs.next();
            if(c != null){
                sandmark.program.Method m =
                    c.getMethod(invokedMethod.getName(),
                                invokedMethod.getSignature());
                if(m != null) possibles.add(m);
            }
        }

        for(java.util.Iterator methods = possibles.iterator() ; methods.hasNext() ; ) {
            sandmark.program.Method method = (sandmark.program.Method)methods.next();
            if(sandmark.obfuscate.inliner.Inliner.containsBadInvokes(method))
                return;
        }

        if(DEBUG){
            System.out.println("\nCurrent Class: " + clazz.getName());
            System.out.println("Current Method: " + method.getName());
            System.out.println("Orig:\n" + method.getInstructionList());
            System.out.println("Exceptions : ");
            for(int i = 0; i < method.getExceptionHandlers().length; i++)
                System.out.println(method.getExceptionHandlers()[i]);
            System.out.println("Instruction: " + ihs[index]);
            System.out.println("Index: " + index);
            System.out.print("Invokes: " + invokedMethod.getClassName());
            System.out.println("." + invokedMethod.getName());
            System.out.println("\nPossibles: ");
            for(int k = 0; k < possibles.size(); k++){
                sandmark.program.Method m = (sandmark.program.Method)
                    possibles.get(k);
                System.out.println(m.getClassName() +
                                   "." + m.getName());
            }
        }
        //Bad things happen when an exception handler uses the invoke
        //as the endPC. Save those handlers so we can update endPC's later
        org.apache.bcel.generic.CodeExceptionGen badExceptions[] =
            getExceptions(ihs[index]);

        //Store the last inst for all branches to end up at
        org.apache.bcel.generic.InstructionHandle end =
            method.getInstructionList().append
            (ihs[index], new org.apache.bcel.generic.NOP());

        //We need to find the objref which is down the stack somewhere
        //Store that ref and generate a load inst to load it when
        //needed.
        org.apache.bcel.generic.LocalVariableInstruction load = null;
        if(possibles.size() > 1){
            load = getLoadInst(method, ihs[index],invokedMethod);
        }
        //the first method doesn't need a branch, because it will become
        //the last inlined method, i.e. the else branch
        firstTime = true;

        //Now inline a branch for each method in the vector
        for(int k = 0; k < possibles.size(); k++){
            invokedMethod =
                (sandmark.program.Method)possibles.get(k);
            if(inliner == null)
                inliner = new sandmark.util.Inliner(method);

            firstUnusedLocal =
                inlineBranch(method.getConstantPool(), load, end);

            inlinedMethods.put(invokedMethod, new Integer(0));
            inlineCount++;
        }
        //We still have the call in there, take it out
        try{
            method.getInstructionList().delete(ihs[index]);
        }catch(Exception e){
            if(DEBUG) e.printStackTrace();
            throw new RuntimeException();
        }

        //Fix the exception handlers we broke
        org.apache.bcel.generic.Instruction nop =
            new org.apache.bcel.generic.NOP();
        org.apache.bcel.generic.InstructionHandle nopih =
            method.getInstructionList().insert(end, nop);
        for(int i = 0; i < badExceptions.length; i++)
            badExceptions[i].setEndPC(nopih);

        method.getInstructionList().setPositions();

        if(DEBUG){
            System.out.println("\nFinal"+method.getInstructionList());
            System.out.println("Exceptions : ");
            for(int i = 0; i < method.getExceptionHandlers().length; i++)
                System.out.println(method.getExceptionHandlers()[i]);
        }
    }

    private org.apache.bcel.generic.CodeExceptionGen[] getExceptions
        (org.apache.bcel.generic.InstructionHandle ih){
        java.util.Vector v = new java.util.Vector();
        for(int i = 0; i < method.getExceptionHandlers().length; i++)
            if(method.getExceptionHandlers()[i].getEndPC() == ih)
                v.add(method.getExceptionHandlers()[i]);
        org.apache.bcel.generic.CodeExceptionGen[] cegs =
            new org.apache.bcel.generic.CodeExceptionGen[v.size()];
        for(int i = 0; i < v.size(); i++)
            cegs[i] = (org.apache.bcel.generic.CodeExceptionGen)
                v.get(i);
        return cegs;
    }

    private int inlineBranch(org.apache.bcel.generic.ConstantPoolGen cpg,
                             org.apache.bcel.generic.Instruction load,
                             org.apache.bcel.generic.InstructionHandle end){
        //If the invokedMethod is empty, just return
        if(invokedMethod.getInstructionList() == null ||
           invokedMethod.getInstructionList().size() == 0)
            return firstUnusedLocal;
        org.apache.bcel.generic.InstructionList list = method.getInstructionList();
        org.apache.bcel.generic.InstructionHandle curr = ihs[index];
        org.apache.bcel.generic.InstructionHandle prev =
            list.insert(curr, new org.apache.bcel.generic.NOP());
        org.apache.bcel.generic.InstructionHandle next =
            list.append(curr, new org.apache.bcel.generic.NOP());

        //Add the class to the constant pool, if not already there
        int cpindex = cpg.lookupClass(invokedMethod.getClassName());

        if(cpindex < 0)
            cpindex = cpg.addClass(invokedMethod.getClassName());

        org.apache.bcel.generic.INSTANCEOF instof =
            new org.apache.bcel.generic.INSTANCEOF(cpindex);

        org.apache.bcel.generic.IFEQ ifeq =
            new org.apache.bcel.generic.IFEQ(next);

        org.apache.bcel.generic.GOTO go =
            new org.apache.bcel.generic.GOTO(end);

        org.apache.bcel.generic.Instruction callCopy =
            curr.getInstruction().copy();

        if(DEBUG){
            System.out.println("Prev: " + prev);
            System.out.println("Curr: " + curr);
            System.out.println("Next: " + next);
        }

        //..,invoke,..->..,load objref,instof,ifeq->next,invoke,goto->end,...
        if(!firstTime){
            org.apache.bcel.generic.Instruction load2 = load.copy();
            list.insert(curr, load2);
            list.append(load2, instof);

            list.insert(curr, ifeq);
            list.append(curr, go);
        }
        //or just inline the method
        else
            firstTime = false;

        list.setPositions();
        method.mark();

        if(DEBUG){
            System.out.println("About to inline: " +invokedMethod);
            System.out.println("At: " + curr);
            System.out.println("Using LV: " + firstUnusedLocal);
            System.out.println("IL is:\n" + list);
        }
        //Now we can just replace the invoke with the method
        inliner.inline(invokedMethod, curr);

        //now add the call back so we can do it all again
        ihs[index] = list.append(prev, callCopy);
        list.setPositions();
        method.mark();
        //if(DEBUG) System.out.println(list);
        return firstUnusedLocal;
    }

    /**
     * Save the stack down to the object ref, then restore it, and return an
     * instruction that loads the variable the object ref got saved to
     */
    private org.apache.bcel.generic.LocalVariableInstruction
        getLoadInst(sandmark.program.Method callingMethod,
                    org.apache.bcel.generic.InstructionHandle callSite,
                    sandmark.program.Method calledMethod){
        callingMethod.setMaxLocals();
        org.apache.bcel.generic.Type args[] = invokedMethod.getArgumentTypes();
        org.apache.bcel.generic.InstructionList list =
            callingMethod.getInstructionList();
        int storeLoc = callingMethod.getMaxLocals();
        for(int i = args.length - 1 ; i >= 0 ;
            storeLoc += args[i].getSize(),i--)
            list.insert(callSite,
                        org.apache.bcel.generic.InstructionFactory.createStore
                        (args[i],storeLoc));
        list.insert(callSite,
                    org.apache.bcel.generic.InstructionFactory.createStore
                    (org.apache.bcel.generic.Type.OBJECT,storeLoc));
        list.insert(callSite,
                    org.apache.bcel.generic.InstructionFactory.createLoad
                    (org.apache.bcel.generic.Type.OBJECT,storeLoc));
        for(int i = args.length - 1,j = storeLoc - 1; i >= 0 ;
            j -= args[i].getSize(),i--)
            list.insert(callSite,
                        org.apache.bcel.generic.InstructionFactory.createLoad
                        (args[i],j));
        list.setPositions();
        callingMethod.mark();
        return org.apache.bcel.generic.InstructionFactory.createLoad
                    (org.apache.bcel.generic.Type.OBJECT,storeLoc);
    }

    public String getShortName() {
        return "Dynamic Inliner";
    }
    public String getLongName() {
        return "Inline static and non-static method calls";
    }
    public String getAlgHTML() {
        return
            "<HTML><BODY>" +
            "Dynamic Inliner inlines methods\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author:<A HREF =" +
            "\"mailto:zachary@cs.arizona.edu\">Zachary Heidepriem</A>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }
    public String getAlgURL() {
        return "sandmark/obfuscate/dynamicinliner/doc/help.html";
    }

    public String getAuthor(){
        return "Zachary Heidepriem";
    }

    public String getAuthorEmail(){
        return "zachary@cs.arizona.edu";
    }

    public String getDescription(){
        return "DynamicInliner inlines non-static methods, determining " +
            "which branch to use at runtime.";
    }
    public String[] getReferences() {
        return new String[] {};
    }
    public sandmark.config.ModificationProperty[] getMutations() {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
            sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
            sandmark.config.ModificationProperty.I_PUBLICIZE_FIELDS,
            sandmark.config.ModificationProperty.I_PUBLICIZE_METHODS,
        };
    }
}

