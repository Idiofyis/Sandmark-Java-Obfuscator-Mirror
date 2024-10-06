package sandmark.util.outliner;

public class OutlineUtil {

    private static boolean BUG = true;
    private static boolean DEBUG = true;
    private static boolean FIXBUG = true;

    private static final int REMOVE_UPDATES = -2;
    private static final int NO_UPDATES = -1;
    private static final int ZERO_UPDATES = 0;
    private static final int TWO_UPDATES = 2;
    private static final int ALL_UPDATES = 3;

    private sandmark.program.Application app;
    private sandmark.program.Method meth;
    private sandmark.analysis.controlflowgraph.MethodCFG mcfg = null;

    /* ExceptionHandler shift information */

    // for target method shifts:
    private int exOffset=0;
    private java.util.ArrayList updateExTable = new java.util.ArrayList();
    private java.util.ArrayList updateExType = new java.util.ArrayList();

    // for outline method shifts:
    private org.apache.bcel.generic.InstructionHandle sPC[];
    private org.apache.bcel.generic.InstructionHandle ePC[];
    private org.apache.bcel.generic.InstructionHandle hPC[];
    int sOffset[], eOffset[], hOffset[];
    int sOffsetIncr[], eOffsetIncr[], hOffsetIncr[];

    private org.apache.bcel.generic.ObjectType exctype[];
    private int exMark[];

    private static final org.apache.bcel.generic.Type VOID_TYPE =
        org.apache.bcel.generic.Type.VOID;
    private static final org.apache.bcel.generic.ObjectType INTEGER_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Integer;");
    private static final org.apache.bcel.generic.ObjectType DOUBLE_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Double;");
    private static final org.apache.bcel.generic.ObjectType FLOAT_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Float;");
    private static final org.apache.bcel.generic.ObjectType LONG_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Long;");
    private static final org.apache.bcel.generic.ObjectType CHARACTER_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Character;");
    private static final org.apache.bcel.generic.ObjectType SHORT_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Short;");
    private static final org.apache.bcel.generic.ObjectType BYTE_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Byte;");
    private static final org.apache.bcel.generic.ObjectType BOOLEAN_TYPE =
        (org.apache.bcel.generic.ObjectType)
        org.apache.bcel.generic.Type.getType("Ljava/lang/Boolean;");

    java.util.HashMap typeTable = new java.util.HashMap();


    /** Constructor
     */
    OutlineUtil(sandmark.program.Application baseApp, 
                sandmark.program.Method baseMeth)
    {
        app = baseApp;
        meth = baseMeth;
        mcfg = new sandmark.analysis.controlflowgraph.MethodCFG(meth, false);
        typeTable.put(org.apache.bcel.generic.Type.BOOLEAN, BOOLEAN_TYPE);
        typeTable.put(org.apache.bcel.generic.Type.BYTE, BYTE_TYPE);
        typeTable.put(org.apache.bcel.generic.Type.CHAR, CHARACTER_TYPE);
        typeTable.put(org.apache.bcel.generic.Type.DOUBLE, DOUBLE_TYPE);
        typeTable.put(org.apache.bcel.generic.Type.FLOAT, FLOAT_TYPE);
        typeTable.put(org.apache.bcel.generic.Type.INT, INTEGER_TYPE);
        typeTable.put(org.apache.bcel.generic.Type.LONG, LONG_TYPE);
        typeTable.put(org.apache.bcel.generic.Type.SHORT, SHORT_TYPE);
    }


    /** Checks if this target method can be outlined or not.
     */
    public boolean isValidMethod()
    {
        if( meth.getName().equals("<init>") || 
            meth.getName().equals("<clinit>") || 
            meth.getName().startsWith("access$") )
            return false;
        return true;
    }


    /** Makes an ICONST_n or BIPUSH n for a given int. 
     */
    private static org.apache.bcel.generic.Instruction createPushInstruction(int n)
    {
        switch (n)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                   return new org.apache.bcel.generic.ICONST(n);
            default:
                   return new org.apache.bcel.generic.BIPUSH((byte)n);
        }
    }

    
    /** Returns true if ih1 dominates ih2, else returns false.
     */
    private boolean dominates(org.apache.bcel.generic.InstructionHandle ih1,
                              org.apache.bcel.generic.InstructionHandle ih2)
    {
        if(mcfg==null)
            if(DEBUG) System.out.println("mcfg is null");

        if( mcfg.getBlock(ih1)!=mcfg.getBlock(ih2) ) {
            if(mcfg.dominates( mcfg.getBlock(ih1), mcfg.getBlock(ih2) ))
                return true;
            return false;
        }

        /* Else, the handles are in the same block; so use 2nd approach */
        java.util.ArrayList ilist = mcfg.getBlock(ih1).getInstList();
        if(ilist==null) {
            if(BUG) System.out.println("BUG: ilist cannot be empty for this block! ");
            return false;
        }
        for(int k=0; k<ilist.size(); k++) {
            if(ih1==(org.apache.bcel.generic.InstructionHandle)ilist.get(k))
                return true;
            if(ih2==(org.apache.bcel.generic.InstructionHandle)ilist.get(k))
                return false;
        }
        if(BUG) System.out.println("BUG: Unreachable point in code. No match found");
        return false;
    }


    /** Returns true if ih1 postdominates ih2, else returns false.
     */
    private boolean postdominates(org.apache.bcel.generic.InstructionHandle ih1,
                                  org.apache.bcel.generic.InstructionHandle ih2)
    {
        if( mcfg.getBlock(ih1)!=mcfg.getBlock(ih2) ) {
            if(mcfg.postDominates( mcfg.getBlock(ih1), mcfg.getBlock(ih2) ))
                return true;
            return false;
        }

        /* Else, the handles are in the same block; so use 2nd approach */
        java.util.ArrayList ilist = mcfg.getBlock(ih1).getInstList();
        if(ilist==null) {
            if(BUG) System.out.println("BUG: ilist cannot be empty for this block! ");
            return false;
        }
        for(int k=0; k<ilist.size(); k++) {
            if(ih1==(org.apache.bcel.generic.InstructionHandle)ilist.get(k))
                return false;
            if(ih2==(org.apache.bcel.generic.InstructionHandle)ilist.get(k))
                return true;
        }
        if(BUG) System.out.println("BUG: Unreachable point in code. No match found");
        return false;
    }


    /** This function checks whether the 'fromIH' and 'toIH' points satisfy the 
     *  dom/postdom condition. All blocks in between then should also satisfy 
     *  this criteria so that we make sure there are no jumps into this 
     *  chunk of code. The stack context at 'fromIH' and 'toIH' should be zero.
     *  Additional condition: should not split exception handling blocks.
     */
    public boolean verifyOutliningPoints(org.apache.bcel.generic.InstructionHandle fromIH,
                                         org.apache.bcel.generic.InstructionHandle toIH)
    {
        if(DEBUG) System.out.println("\n In fn. verifyOutliningPoints ... \n");
        if( !this.dominates(fromIH, toIH) || !this.postdominates(toIH, fromIH) )
            return false;

        sandmark.analysis.controlflowgraph.BasicBlock fromBB = mcfg.getBlock(fromIH);
        sandmark.analysis.controlflowgraph.BasicBlock toBB = mcfg.getBlock(toIH);

        java.util.LinkedList worklist = new java.util.LinkedList();
        worklist.add(fromBB);
        if(fromBB!=toBB) {
            int ptr=0;
            while(ptr!=worklist.size()) {
                sandmark.analysis.controlflowgraph.BasicBlock tempBB =
                    (sandmark.analysis.controlflowgraph.BasicBlock)worklist.get(ptr);
                /* Get all the successors and include it in the linkedlist */
                java.util.Iterator succlist = mcfg.succs(tempBB);
                while(succlist.hasNext()) {
                    sandmark.analysis.controlflowgraph.BasicBlock sbb =
                        (sandmark.analysis.controlflowgraph.BasicBlock)succlist.next();
                    if(!worklist.contains(sbb) && sbb!=toBB) {
                        worklist.add(sbb);
                        if(DEBUG) {
                            System.out.println("adding block to worklist...");
                            java.util.ArrayList arrlist = sbb.getInstList();
                            for(int p=0; p<arrlist.size(); p++)
                                System.out.println((org.apache.bcel.generic.InstructionHandle)arrlist.get(p));
                        }
                    }
                }
                ptr++;
            }
        }
        if(DEBUG)System.out.println(" Worklist size = "+worklist.size());

        /* From the worklist, select verify that all BBlocks 
         * satisfy dom/postdom conditon; */
        int constraintFlag=1;
        for(int k=0; k<worklist.size(); k++) {
            sandmark.analysis.controlflowgraph.BasicBlock tempBB = 
                (sandmark.analysis.controlflowgraph.BasicBlock)worklist.get(k);
            /*if(tempBB==fromBB)
                continue;*/
            if( !mcfg.dominates(fromBB,tempBB) || !mcfg.postDominates(toBB,tempBB) ) {
                constraintFlag = 0;
                break;
            }
        }
        if(constraintFlag==0) {
            System.out.println(" Blocks within the range do not satisfy dom/postdom criteria ... ");
            return false;
        }

        /* Also make sure that the stack context size is zero at 'fromIH' and 'toIH->next' */
        sandmark.analysis.stacksimulator.StackSimulator sim = 
            new sandmark.analysis.stacksimulator.StackSimulator(meth);
        sandmark.analysis.stacksimulator.Context fromContext = sim.getInstructionContext(fromIH);
        sandmark.analysis.stacksimulator.Context toContext = sim.getInstructionContext(toIH.getNext());
        if( (fromContext.getStackSize()!=0) || (toContext.getStackSize()!=0) ) {
            if(DEBUG) System.out.println(" Stack Context Size is not zero. constraint violated ");
            return false;
        }

        /* finally, check the exception handler condition */ 
        int fromOffset = fromIH.getPosition();
        int toOffset = toIH.getPosition();

        org.apache.bcel.generic.ConstantPoolGen cpg = 
            ((sandmark.program.Class)meth.getParent()).getConstantPool();
        
        org.apache.bcel.generic.CodeExceptionGen [] ceg = meth.getExceptionHandlers();
        if(ceg!=null) {
            for(int k=0; k<ceg.length; k++) {
                org.apache.bcel.classfile.CodeException ce = ceg[k].getCodeException(cpg);
                int hEndOffset = this.getCatchEndOffset(ceg[k]);
                int retvar;
                if((retvar=this.splitsExceptionBlock(ce.getStartPC(),
                     ce.getEndPC(), ce.getHandlerPC(), hEndOffset, fromOffset, toOffset))==NO_UPDATES)
                    return false;
                else {
                    updateExTable.add(new Integer(k));
                    updateExType.add(new Integer(retvar));
                }
            }
        }
        return true;
    }


    /** Returns the offset of the last instruction in the catch block 'ceg'.
     */
    private int getCatchEndOffset(org.apache.bcel.generic.CodeExceptionGen ceg)
    {
        if(ceg.getEndPC().getNext().toString().indexOf("goto")==-1)
            if(BUG)System.out.println("BUG: Instruction other then goto @ end of try block ->"+
                        ceg.getEndPC().getNext().toString());

        org.apache.bcel.generic.BranchHandle gotoIH = 
            (org.apache.bcel.generic.BranchHandle)(ceg.getEndPC().getNext());
        org.apache.bcel.generic.InstructionHandle catchEndIH = gotoIH.getTarget().getPrev();
        return catchEndIH.getPosition();
    }
        

    /** Checks whether 'from' and 'to' *properly* split the try-catch block.
     */
    private int splitsExceptionBlock(int sOffset, int eOffset, int hOffset, int hEndOffset, int from, int to)
    {
        if(sOffset>to)
            return(ALL_UPDATES); // entire try-catch block lies below toIH
        if(hEndOffset<from)
            return(ZERO_UPDATES); // entire try-catch block lies above fromIH

        if(sOffset<from) {
            if(hOffset<from && hEndOffset>to)
                return(NO_UPDATES); // part of catch block is outlined.
            if(eOffset>to)
                return(TWO_UPDATES); // part of try block is outlined.
        }
        
        // sOffset lies between fromIH and toIH
        if(hEndOffset<=to)
            return(REMOVE_UPDATES); // entire try-catch block is outlined.
        
        return NO_UPDATES;
    }


    /** Returns the DUWeb objects associated with the slot index 'slot'.
     */
    private sandmark.analysis.defuse.DUWeb[] 
                getSlotLiveRanges(java.util.ArrayList nodelist, int slot)
    {
        if(DEBUG) System.out.println("In fn. getSlotLiveRanges ...");
        java.util.ArrayList dulist = new java.util.ArrayList();
        for(int m=0; m<nodelist.size(); m++) {
            sandmark.analysis.defuse.DUWeb dw =
                (sandmark.analysis.defuse.DUWeb)nodelist.get(m);
            java.util.Set sets = dw.defs(); // defuse.DefWrappers sets
            Object obj[]= sets.toArray();
            if(obj==null) {
                if(DEBUG) System.out.println("obj is null");
                continue;
            }
            else
               if(DEBUG) System.out.println("obj.length -> "+obj.length);
            for(int k=0; k<obj.length; k++) {
                if(((sandmark.analysis.defuse.DefWrapper)obj[k]).getIndex()==slot) {
                    dulist.add(dw);
                    break;
                }
            }
        }
        if(dulist.size()==0)
            return null;
        else
            if(DEBUG) System.out.println(" dulist.size = "+dulist.size());


        Object obj[] = dulist.toArray();
        sandmark.analysis.defuse.DUWeb retweb[] = 
            new  sandmark.analysis.defuse.DUWeb[obj.length];
        for(int k=0; k<obj.length; k++)
            retweb[k] = (sandmark.analysis.defuse.DUWeb)obj[k];

        return retweb; // (sandmark.analysis.defuse.DUWeb[])dulist.toArray();
    }


    /** Returns 'true' if index 'slot' has atleast one of its liverange intersecting the 
     *  region from 'fromBB' to 'toBB'; Else, returns false.
     */
    private boolean liverangeInterferes(int slot, 
                        sandmark.analysis.controlflowgraph.BasicBlock fromBB, 
                        sandmark.analysis.controlflowgraph.BasicBlock toBB)
    {
        if(DEBUG) System.out.println("\n In fn. liverangeInterferes... \n");
        java.util.LinkedList worklist = new java.util.LinkedList();
        worklist.add(fromBB);
        int ptr=0;
        while(ptr!=worklist.size()) {
            sandmark.analysis.controlflowgraph.BasicBlock tempBB =
                (sandmark.analysis.controlflowgraph.BasicBlock)worklist.get(ptr);
            /* Get all the successors and include it in the linkedlist */
            java.util.Iterator succlist = mcfg.succs(tempBB);
            while(succlist.hasNext()) {
                sandmark.analysis.controlflowgraph.BasicBlock sbb =
                    (sandmark.analysis.controlflowgraph.BasicBlock)succlist.next();
                if(!worklist.contains(sbb) && sbb!=toBB)
                    worklist.add(sbb);
            }
            ptr++;
        }
        worklist.add(toBB);
        if(DEBUG) System.out.println(" worklist.size() = "+worklist.size());
        for(int k=0; k<worklist.size(); k++)
            if(mcfg.isInScope(slot, (sandmark.analysis.controlflowgraph.BasicBlock)worklist.get(k)))
                return true;
        return false;
    }


    /** Returns the instruction handle of the def. associated with this DefWrapper.
     */
    private org.apache.bcel.generic.InstructionHandle 
            getDefHandleFromWrapper(sandmark.analysis.defuse.DefWrapper defwrap)
    {
        if(defwrap instanceof sandmark.analysis.defuse.InstructionDefWrapper)
            return ((sandmark.analysis.defuse.InstructionDefWrapper)defwrap).getIH();
        if(defwrap instanceof sandmark.analysis.defuse.ParamDefWrapper) {
            org.apache.bcel.generic.InstructionHandle ih[] =
                meth.getInstructionList().getInstructionHandles();
            return ih[0];
        }
        if(defwrap instanceof sandmark.analysis.defuse.ThisDefWrapper) {
            if(FIXBUG)System.out.println(defwrap.getIndex()+ " thisdefIH = "+defwrap.toString());
            return null;
        }
        if(BUG)System.out.println("BUG: In fn. getDefHandleFromWrapper():  Invalid sub-Wrapper!");
        return null;
    }


    /** Returns the LiveVar objects those have their liveranges passing into the outlined code range.
     */
    public LiveVar[] getPliveVars(org.apache.bcel.generic.InstructionHandle fromIH, 
                                  org.apache.bcel.generic.InstructionHandle toIH,
                                  java.util.ArrayList nodelist)
    {
        if(DEBUG) System.out.println("\n In fn. getPliveVars ... \n");
        if(nodelist==null) {
            if(DEBUG) System.out.println(" nodelist is null ");
            return null;
        }

        sandmark.analysis.controlflowgraph.BasicBlock fromBB = mcfg.getBlock(fromIH);
        sandmark.analysis.controlflowgraph.BasicBlock toBB = mcfg.getBlock(toIH);

        if(DEBUG) System.out.println(" maxlocals = "+meth.getMaxLocals());
        int slotarray[] = new int[meth.getMaxLocals()];
        java.util.ArrayList deflist[] = new java.util.ArrayList[meth.getMaxLocals()];
        java.util.ArrayList uselist[] = new java.util.ArrayList[meth.getMaxLocals()];
        for(int k=0; k<meth.getMaxLocals(); k++) {
            slotarray[k]=0; // init: invalid
            deflist[k] = new java.util.ArrayList();
            uselist[k] = new java.util.ArrayList();
        }

        for(int slotid=0; slotid<meth.getMaxLocals(); slotid++) {
            /*** if(!this.liverangeInterferes(slotid, fromBB, toBB)) {
                if(DEBUG) System.out.println("live range of  slot "+slotid+" not in this range"); 
                continue;
            } *****/
            if(DEBUG) System.out.println("\nslotid = "+slotid);
            sandmark.analysis.defuse.DUWeb duweb[] = 
                this.getSlotLiveRanges(nodelist, slotid);
            if(duweb==null) {
                if(DEBUG) System.out.println("duweb is null for slotid -> "+slotid);
                continue;
            }

            /* Now get all the def/use handles from the duweb[] and check that 
             * atleast one def is before 'fromIH' and all use in between 
             * 'fromIH' and 'toIH' */
            for(int k=0; k<duweb.length; k++) {
                boolean neverAdd=false;
                boolean toAdd=false;
                java.util.Set defwrappers = duweb[k].defs();
                java.util.Set useHandles = duweb[k].uses(); 
                // above 2 set sizes are not necessarily the same; not strictly non-zero either.
                Object defObj[] = defwrappers.toArray();
                Object useObj[] = useHandles.toArray();
                if(useObj==null||defObj==null) {
                    if(DEBUG) System.out.println(" useObj or defObj is null");
                    continue;
                }
                
                for(int p=0; p<useObj.length; p++) {
                    org.apache.bcel.generic.InstructionHandle useIH =
                        (org.apache.bcel.generic.InstructionHandle)useObj[p];
                    /* if USE within the range */
                    if(this.dominates(fromIH, useIH) && this.dominates(useIH, toIH)) {
                        if(DEBUG) System.out.println(" use within range ...");
                        /* if atleast one DEF above fromIH, then add */
                        for(int q=0; q<defObj.length; q++) {
                            org.apache.bcel.generic.InstructionHandle defIH =
                                this.getDefHandleFromWrapper(
                                    (sandmark.analysis.defuse.DefWrapper)defObj[q]);
                            if(this.postdominates(fromIH, defIH)) {
                                if(DEBUG) System.out.println(" def above range ...");
                                toAdd=true;
                            }
                        }
                    }
                    else {
                        /* if DEF-USE chain passes thru the entire range */
                        if(this.dominates(toIH, useIH))
                            neverAdd=true;
                    }
                }

                if(neverAdd)
                    slotarray[slotid]=-1;
                if(toAdd) {
                    if(slotarray[slotid]!=-1) {
                        slotarray[slotid]=1;
                        for(int q=0; q<defObj.length; q++) {
                            org.apache.bcel.generic.InstructionHandle defIH =
                                this.getDefHandleFromWrapper(
                                    (sandmark.analysis.defuse.DefWrapper)defObj[q]);
                            deflist[slotid].add(defIH);
                        }
                        for(int p=0; p<useObj.length; p++)
                            uselist[slotid].add((org.apache.bcel.generic.InstructionHandle)useObj[p]);
                    }
                }
            }
        }
        /* slotarray[], deflist[] and uselist[] contains the final slots satisfying 'P' criteria */

        java.util.ArrayList lvlist = new java.util.ArrayList();
        for(int slotid=0; slotid<slotarray.length; slotid++) {
            if(slotarray[slotid]!=1)
                continue;
            if(DEBUG) System.out.println("### CANDIDATE SLOT : "+slotid);
            LiveVar lvObject = new LiveVar(slotid, this.getVariableType(meth, slotid));
            lvObject.setDefList(deflist[slotid]);
            lvObject.setUseList(uselist[slotid]);
            lvlist.add(lvObject);
        }

        Object obj[] = lvlist.toArray();
        LiveVar retvar[] = new LiveVar[obj.length];
        for(int k=0; k<obj.length; k++)
            retvar[k] = (LiveVar)obj[k];

        return retvar;
    }


    /** Returns the LiveVar objects those have their liveranges passing out of the outlined code range.
     */
    public LiveVar[] getQliveVars(org.apache.bcel.generic.InstructionHandle fromIH, 
                                  org.apache.bcel.generic.InstructionHandle toIH,
                                  java.util.ArrayList nodelist)
    {
        if(DEBUG) System.out.println("\n In fn. getQliveVars ... \n");
        if(nodelist==null)
            return null;

        sandmark.analysis.controlflowgraph.BasicBlock fromBB = mcfg.getBlock(fromIH);
        sandmark.analysis.controlflowgraph.BasicBlock toBB = mcfg.getBlock(toIH);

        int slotarray[] = new int[meth.getMaxLocals()];
        java.util.ArrayList deflist[] = new java.util.ArrayList[meth.getMaxLocals()];
        java.util.ArrayList uselist[] = new java.util.ArrayList[meth.getMaxLocals()];
        for(int k=0; k<meth.getMaxLocals(); k++) {
            slotarray[k]=0; // init: invalid
            deflist[k] = new java.util.ArrayList();
            uselist[k] = new java.util.ArrayList();
        }

        for(int slotid=0; slotid<meth.getMaxLocals(); slotid++) {
            if(DEBUG) System.out.println("\nslotid = "+slotid);
            sandmark.analysis.defuse.DUWeb duweb[] = 
                this.getSlotLiveRanges(nodelist, slotid);
            if(duweb==null) {
                if(DEBUG) System.out.println("duweb is null for slotid -> "+slotid);
                continue;
            }

            /* Now get all the def/use handles from the duweb[] and check that 
             * atleast one def is in between 'fromIH' and 'toIH' and rest def/use 
             * after 'toIH' 
             */
            boolean neverAdd=false;
            boolean toAdd=false;
            boolean todefAdd=false;
            Object defObj[] = null;
            Object useObj[] = null;
            for(int k=0; k<duweb.length; k++) {
                java.util.Set defwrappers = duweb[k].defs();
                java.util.Set useHandles = duweb[k].uses(); 
                // Above 2 set sizes are not necessarily the same; not strictly non-zero either.
                defObj = defwrappers.toArray();
                useObj = useHandles.toArray();

                for(int q=0; q<defObj.length; q++) {
                    org.apache.bcel.generic.InstructionHandle defIH =
                        this.getDefHandleFromWrapper(
                                (sandmark.analysis.defuse.DefWrapper)defObj[q]);
                    if(this.dominates(defIH, fromIH)) {
                        /* no defIHs should lie above fromIH */
                        neverAdd = true;
                        if(DEBUG)System.out.println("never add");
                        break;
                    }
                    if(this.dominates(defIH, toIH)) {
                        todefAdd = true;
                        break;
                    }
                }
            }
            if(!neverAdd) {
                for(int k=0; k<duweb.length; k++) {
                    java.util.Set useHandles = duweb[k].uses(); 
                    useObj = useHandles.toArray();
                    for(int p=0; p<useObj.length; p++) {
                        org.apache.bcel.generic.InstructionHandle useIH =
                            (org.apache.bcel.generic.InstructionHandle)useObj[p];
                        if(this.dominates(toIH, useIH)) {
                            /* atleast one use below toIH */
                            if(DEBUG)System.out.println("to add");
                            toAdd = true;
                            break;
                        }
                    }
                }
            }
            else
                slotarray[slotid]=-1;

            if(toAdd) {
                if(slotarray[slotid]!=-1) {
                    slotarray[slotid]=1;
                    for(int q=0; q<defObj.length; q++) {
                        org.apache.bcel.generic.InstructionHandle defIH =
                            this.getDefHandleFromWrapper(
                                (sandmark.analysis.defuse.DefWrapper)defObj[q]);
                        deflist[slotid].add(defIH);
                    }
                    for(int p=0; p<useObj.length; p++)
                        uselist[slotid].add((org.apache.bcel.generic.InstructionHandle)useObj[p]);
                }
            }
        }
        /* slotarray[], deflist[] and uselist[] contains the final slots satisfying 'Q' criteria */

        java.util.ArrayList lvlist = new java.util.ArrayList();
        for(int slotid=0; slotid<slotarray.length; slotid++) {
            if(slotarray[slotid]!=1)
                continue;
            if(DEBUG) System.out.println("### CANDIDATE SLOT : "+slotid);
            LiveVar lvObject = new LiveVar(slotid, this.getVariableType(meth, slotid));
            lvObject.setDefList(deflist[slotid]);
            lvObject.setUseList(uselist[slotid]);
            lvlist.add(lvObject);
        }

        Object obj[] = lvlist.toArray();
        if(DEBUG) System.out.println("# objlength -> "+obj.length);
        if(obj.length==0)
            return null;
        LiveVar retvar[] = new LiveVar[obj.length];
        for(int k=0; k<obj.length; k++)
            retvar[k] = (LiveVar)obj[k];

        return retvar;
    }
    

    /** Returns the variable type associated with this 'slotid'.
     */
    private org.apache.bcel.generic.Type getVariableType(sandmark.program.Method meth, int slotid)
    {
        org.apache.bcel.generic.ConstantPoolGen cpg =
            ((sandmark.program.Class)meth.getParent()).getConstantPool();
        org.apache.bcel.generic.InstructionList ilist = meth.getInstructionList();
        org.apache.bcel.generic.InstructionHandle ih[] = ilist.getInstructionHandles();
        for(int k=0;k<ih.length;k++)
            if(ih[k].getInstruction() instanceof org.apache.bcel.generic.LocalVariableInstruction)
                if(slotid==((org.apache.bcel.generic.LocalVariableInstruction)ih[k].getInstruction()).getIndex())
                    return ((org.apache.bcel.generic.LocalVariableInstruction)ih[k].getInstruction()).getType(cpg);
        if(BUG) System.out.println("BUG: slot index not found !");
        return null;
    }
        

    /** Returns the LiveVar objects those have their liveranges passing through the 
     *  entire outlined code range.
     */
    public LiveVar[] getRliveVars(org.apache.bcel.generic.InstructionHandle fromIH, 
                                  org.apache.bcel.generic.InstructionHandle toIH,
                                  java.util.ArrayList nodelist)
    {
        if(DEBUG) System.out.println("\n In fn. getRliveVars ... \n");
        if(nodelist==null)
            return null;

        sandmark.analysis.controlflowgraph.BasicBlock fromBB = mcfg.getBlock(fromIH);
        sandmark.analysis.controlflowgraph.BasicBlock toBB = mcfg.getBlock(toIH);

        int slotarray[] = new int[meth.getMaxLocals()];
        java.util.ArrayList deflist[] = new java.util.ArrayList[meth.getMaxLocals()];
        java.util.ArrayList uselist[] = new java.util.ArrayList[meth.getMaxLocals()];
        for(int k=0; k<meth.getMaxLocals(); k++) {
            slotarray[k]=0; // init: invalid
            deflist[k] = new java.util.ArrayList();
            uselist[k] = new java.util.ArrayList();
        }

        for(int slotid=0; slotid<meth.getMaxLocals(); slotid++) {
            int topFlag=0;
            int bottomFlag=0;
            sandmark.analysis.defuse.DUWeb duweb[] = 
                this.getSlotLiveRanges(nodelist, slotid);
            if(duweb==null)
                continue;

            /* Now get all the def/use handles from the duweb[] and check that 
             * atleast one def/use is before 'fromIH' and one def/use is after 'toIH' */
            for(int k=0; k<duweb.length; k++) {
                boolean neverAdd=false;
                boolean toAdd=false;
                java.util.Set defwrappers = duweb[k].defs();
                java.util.Set useHandles = duweb[k].uses(); 
                // above 2 set sizes are not necessarily the same; not strictly non-zero either.
                Object defObj[] = defwrappers.toArray();
                Object useObj[] = useHandles.toArray();
                if(useObj==null||defObj==null)
                    continue;
                
                for(int q=0; q<defObj.length; q++) {
                    org.apache.bcel.generic.InstructionHandle defIH =
                        this.getDefHandleFromWrapper(
                                (sandmark.analysis.defuse.DefWrapper)defObj[q]);
                    if(this.dominates(defIH, fromIH)) {
                        topFlag=1;
                        continue;
                    }
                    if(this.dominates(toIH, defIH))
                        bottomFlag=1;
                }

                if((topFlag==0)||(bottomFlag==0)) {
                    for(int p=0; p<useObj.length; p++) {
                        org.apache.bcel.generic.InstructionHandle useIH =
                            (org.apache.bcel.generic.InstructionHandle)useObj[p];
                        if(this.dominates(useIH, fromIH)) {
                            topFlag=1;
                            continue;
                        }
                        if(this.dominates(toIH, useIH))
                            bottomFlag=1;
                    }
                }

                if((topFlag==0)||(bottomFlag==0))
                    continue;

                slotarray[slotid]=1;
                for(int q=0; q<defObj.length; q++) {
                    org.apache.bcel.generic.InstructionHandle defIH =
                        this.getDefHandleFromWrapper(
                            (sandmark.analysis.defuse.DefWrapper)defObj[q]);
                        deflist[slotid].add(defIH);
                }
                for(int p=0; p<useObj.length; p++)
                    uselist[slotid].add((org.apache.bcel.generic.InstructionHandle)useObj[p]);
            }
        }
        /* slotarray[], deflist[] and uselist[] contains the final slots satisfying 'R' criteria */
    

        java.util.ArrayList lvlist = new java.util.ArrayList();
        for(int slotid=0; slotid<slotarray.length; slotid++) {
            if(slotarray[slotid]!=1)
                continue;
            if(DEBUG) System.out.println("### CANDIDATE SLOT : "+slotid+" type -> "+
                            this.getVariableType(meth, slotid));
            LiveVar lvObject = new LiveVar(slotid, this.getVariableType(meth, slotid));
            lvObject.setDefList(deflist[slotid]);
            lvObject.setUseList(uselist[slotid]);
            lvlist.add(lvObject);
        }
        LiveVar retvar[] = new LiveVar[lvlist.size()];
        for(int k=0; k<lvlist.size(); k++)
            retvar[k] = (LiveVar)lvlist.get(k);

        return retvar;
    }


    /** This function pushes the set of parameters that are to be passed from target method to 
     *  outlined method, into the stack.
     */
    public void passInRanges(org.apache.bcel.generic.InstructionHandle fromIH, LiveVar lv1[])
    {
        if(DEBUG) System.out.println("\n In fn. passInRanges ... \n");
        if(lv1==null)
            return; // invoke some error routine...
        
        org.apache.bcel.generic.InstructionHandle savIH = null;

        /* Generate similar code in the loop for each incoming live range 
         *      <type>load <slotnum>
         */
        org.apache.bcel.generic.ConstantPoolGen cpg = 
            ((sandmark.program.Class)meth.getParent()).getConstantPool();
        org.apache.bcel.generic.InstructionFactory factory = 
            new org.apache.bcel.generic.InstructionFactory(cpg);
        org.apache.bcel.generic.InstructionList ilist = meth.getInstructionList();

        for(int k=0; k<lv1.length; k++) {
            org.apache.bcel.generic.Instruction ins = factory.createLoad(lv1[k].getType(), lv1[k].getSlot());
            ilist.insert(fromIH, ins);
            exOffset+=ins.getLength();
            if(DEBUG) System.out.println(" pushing into stack: "+ins+" type -> "+lv1[k].getType());
        }

        meth.mark();
        meth.setInstructionList(ilist);

        return;
    }


    /** This function promotes and packs the locals that have liveranges 
     *  through outlined code range into an Object array  and pushes this into the stack.
     */
    public int passThroughRanges(org.apache.bcel.generic.InstructionHandle fromIH, LiveVar lv3[])
    {
        if(DEBUG) System.out.println("\n In fn. passThroughRanges ... \n");
        if(lv3==null)
            return -1; // invoke some error routine ...

        org.apache.bcel.generic.InstructionHandle savIH = null;

        org.apache.bcel.generic.LocalVariableGen lg = 
            meth.addLocalVariable("LV3ARRAY", 
                            org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;"),
                            null, null);
        int arrayVarIndex = lg.getIndex();



        /* Generate similar code in the loop for each incoming liverange in lv3[] 
         *   
         *      aload arrayref;
         *
         *      dup;  ( for the main array[]ref )
         *      iconst <index>;
         *      <type> load <slot>;
         *      if(prim_type)
         *          new <Class type>;
         *          dup_x1; (or dup_x2) for double/long 
         *          swap;  (or dup_x2 / pop) for double/long
         *          invokespecial <Object Class type> <primitive type>
         *      checkcast <OBJECT> 
         *      aastore; 
         *
         *  At this point we have the object reference at the top of stack.
         */

        org.apache.bcel.generic.ConstantPoolGen cpg = 
            ((sandmark.program.Class)meth.getParent()).getConstantPool();
        org.apache.bcel.generic.InstructionFactory factory = 
            new org.apache.bcel.generic.InstructionFactory(cpg);
        org.apache.bcel.generic.InstructionList ilist = meth.getInstructionList();

        /* initialize array */
        org.apache.bcel.generic.InstructionHandle initIH[] = ilist.getInstructionHandles();
        savIH = ilist.insert(initIH[0], this.createPushInstruction(lv3.length));
        exOffset+=savIH.getInstruction().getLength();

        int cpIndex = cpg.addClass(org.apache.bcel.generic.Type.OBJECT);
        savIH = ilist.insert(initIH[0], new org.apache.bcel.generic.ANEWARRAY(cpIndex));
        exOffset+=savIH.getInstruction().getLength();

        savIH = ilist.insert(initIH[0], factory.createStore(
                                org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;"),
                                arrayVarIndex));
        exOffset+=savIH.getInstruction().getLength();

        savIH = ilist.insert(fromIH, factory.createLoad(
                                org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;"),
                                arrayVarIndex));
        exOffset+=savIH.getInstruction().getLength();

        for(int k=0; k<lv3.length; k++) {
            savIH = ilist.insert(fromIH, new org.apache.bcel.generic.DUP());
            exOffset+=savIH.getInstruction().getLength();
            savIH = ilist.insert(fromIH, this.createPushInstruction(k));
            exOffset+=savIH.getInstruction().getLength();
            if(DEBUG)System.out.println("loadtype -> "+lv3[k].getType().toString()+" slot -> "+lv3[k].getSlot());
            savIH = ilist.insert(fromIH, factory.createLoad(lv3[k].getType(), lv3[k].getSlot()));
            exOffset+=savIH.getInstruction().getLength();

            org.apache.bcel.generic.ObjectType objtype = null;
            if(lv3[k].isObjectType()) {
                if(DEBUG)System.out.println("typeT is ObjectType(String/maybe)");
            }
            else {
                org.apache.bcel.generic.Type lvtype = lv3[k].getType();
                objtype = (org.apache.bcel.generic.ObjectType)typeTable.get(lv3[k].getType());
                savIH = ilist.insert(fromIH, factory.createNew(objtype));
                exOffset+=savIH.getInstruction().getLength();

                if( typeTable.get(lvtype).equals(DOUBLE_TYPE) ||
                    typeTable.get(lvtype).equals(LONG_TYPE) ) {
                    savIH = ilist.insert(fromIH, org.apache.bcel.generic.InstructionConstants.DUP_X2);
                    exOffset+=savIH.getInstruction().getLength();
                    savIH = ilist.insert(fromIH, org.apache.bcel.generic.InstructionConstants.DUP_X2);
                    exOffset+=savIH.getInstruction().getLength();
                    savIH = ilist.insert(fromIH, new org.apache.bcel.generic.POP());
                    exOffset+=savIH.getInstruction().getLength();
                }
                else {
                    savIH = ilist.insert(fromIH, org.apache.bcel.generic.InstructionConstants.DUP_X1);
                    exOffset+=savIH.getInstruction().getLength();
                    savIH = ilist.insert(fromIH, org.apache.bcel.generic.InstructionConstants.SWAP);
                    exOffset+=savIH.getInstruction().getLength();
                }

                if(DEBUG)System.out.println("typeT -> "+typeTable.get(lvtype).toString());

                exOffset+=3; // invoke is 3 word instruction 
                if(typeTable.get(lvtype).equals(INTEGER_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Integer",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                if(typeTable.get(lvtype).equals(FLOAT_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Float",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                if(typeTable.get(lvtype).equals(BYTE_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Byte",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                if(typeTable.get(lvtype).equals(DOUBLE_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Double",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                if(typeTable.get(lvtype).equals(CHARACTER_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Character",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                if(typeTable.get(lvtype).equals(BOOLEAN_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Boolean",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                if(typeTable.get(lvtype).equals(SHORT_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Short",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                if(typeTable.get(lvtype).equals(LONG_TYPE))
                     ilist.insert(fromIH, factory.createInvoke("java/lang/Long",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
            }
            savIH = ilist.insert(fromIH, factory.createCheckCast(org.apache.bcel.generic.Type.OBJECT));
            exOffset+=savIH.getInstruction().getLength();
            savIH = ilist.insert(fromIH, new org.apache.bcel.generic.AASTORE());
            exOffset+=savIH.getInstruction().getLength();
        }

        meth.mark();
        ilist.setPositions();
        meth.setInstructionList(ilist);

        return arrayVarIndex;
    }


    /** Returns a branch instruction equivalent to the string representation.
     */
    private org.apache.bcel.generic.BranchInstruction createBranchInstruction(String instr)
    {
        if(instr.indexOf("ifeq")!=-1)
            return new org.apache.bcel.generic.IFEQ(null);
        if(instr.indexOf("ifne")!=-1)
            return new org.apache.bcel.generic.IFNE(null);
        if(instr.indexOf("ifgt")!=-1)
            return new org.apache.bcel.generic.IFGT(null);
        if(instr.indexOf("iflt")!=-1)
            return new org.apache.bcel.generic.IFLT(null);
        if(instr.indexOf("ifge")!=-1)
            return new org.apache.bcel.generic.IFGE(null);
        if(instr.indexOf("ifle")!=-1)
            return new org.apache.bcel.generic.IFLE(null);
        if(instr.indexOf("if_icmpeq")!=-1)
            return new org.apache.bcel.generic.IF_ICMPEQ(null);
        if(instr.indexOf("if_icmpne")!=-1)
            return new org.apache.bcel.generic.IF_ICMPNE(null);
        if(instr.indexOf("if_icmpgt")!=-1)
            return new org.apache.bcel.generic.IF_ICMPGT(null);
        if(instr.indexOf("if_icmplt")!=-1)
            return new org.apache.bcel.generic.IF_ICMPLT(null);
        if(instr.indexOf("if_icmpge")!=-1)
            return new org.apache.bcel.generic.IF_ICMPGE(null);
        if(instr.indexOf("if_icmple")!=-1)
            return new org.apache.bcel.generic.IF_ICMPLE(null);
        if(instr.indexOf("ifnull")!=-1)
            return new org.apache.bcel.generic.IFNULL(null);
        if(instr.indexOf("ifnonnull")!=-1)
            return new org.apache.bcel.generic.IFNONNULL(null);
        if(instr.indexOf("if_acmpeq")!=-1)
            return new org.apache.bcel.generic.IF_ACMPEQ(null);
        if(instr.indexOf("if_acmpne")!=-1)
            return new org.apache.bcel.generic.IF_ACMPNE(null);
        if(instr.indexOf("goto")!=-1)
            return new org.apache.bcel.generic.GOTO(null);
        if(BUG)System.out.println("BUG: Unknown branch instruction/ or different format");
        return null;
    }

    /** Keeps a mapping from the old exception handler offsets to the new offsets in the 
     *  outlined code.
     */
    private void saveExceptionHandler(org.apache.bcel.generic.InstructionHandle oldIH, 
                                      org.apache.bcel.generic.CodeExceptionGen ceg[],
                                      org.apache.bcel.generic.InstructionHandle newIH)
    {
        if(ceg==null)
            return;
        for(int k=0; k<ceg.length; k++){
            if(ceg[k].getStartPC().equals(oldIH)) {
                sPC[k]=newIH;
                sOffset[k]=newIH.getPosition();
                exctype[k] = ceg[k].getCatchType();
                exMark[k]=1;
                if(DEBUG)System.out.println("\n In fn. saveExceptionHandler "+sOffset[k]);
            }
            if(ceg[k].getEndPC().equals(oldIH)) {
                ePC[k]=newIH;
                eOffset[k]=newIH.getPosition();
                exctype[k] = ceg[k].getCatchType();
                exMark[k]=1;
                if(DEBUG)System.out.println("\n In fn. saveExceptionHandler "+eOffset[k]);
            }
            if(ceg[k].getHandlerPC().equals(oldIH)) {
                hPC[k]=newIH;
                hOffset[k]=newIH.getPosition();
                exctype[k] = ceg[k].getCatchType();
                exMark[k]=1;
                if(DEBUG)System.out.println("\n In fn. saveExceptionHandler "+hOffset[k]);
            }
        }
        return;
    }


    /** Returns true if 'ih' is the start instruction handle of the handler block of 
     *  exception 'ceg'
     */
    private boolean isHandlerStart(org.apache.bcel.generic.InstructionHandle ih,
                                   org.apache.bcel.generic.CodeExceptionGen ceg[])
    {
        if(ceg==null)
            return false;
        for(int k=0; k<ceg.length; k++)
            if(ceg[k].getHandlerPC().equals(ih))
                return true;
        return false;
    }

    /** Returns the length of a specific group of single word instructions.
     */
    private int getSWordGrpLen(org.apache.bcel.generic.InstructionFactory ifactory)
    {
        int len=0;
        len+= (ifactory.createNew(INTEGER_TYPE)).getLength();
        len+= (org.apache.bcel.generic.InstructionConstants.DUP_X1).getLength();
        len+= (org.apache.bcel.generic.InstructionConstants.SWAP).getLength();
        len+=3; // for invoke
        return len;
    }

    /** Returns the length of a specific group of double word instructions.
     */
    private int getDWordGrpLen(org.apache.bcel.generic.InstructionFactory ifactory)
    {
        int len=0;
        len+= (ifactory.createNew(LONG_TYPE)).getLength();
        len+= (org.apache.bcel.generic.InstructionConstants.DUP_X2).getLength();
        len+= (org.apache.bcel.generic.InstructionConstants.DUP_X2).getLength();
        len+= (new org.apache.bcel.generic.POP()).getLength();
        len+=3; // for invoke
        return len;
    }

    /** Updates the exception offsets based on 'action'
     */
    private void indexOffsetUpdate(int currOffset, int action)
    {
        if(exMark==null)
            return;
        for(int k=0; k<exMark.length; k++) {
            if(exMark[k]==0)
                continue;
            int flag=0;
            if(currOffset<=sOffset[k]) {
                if(action==1)
                    sOffsetIncr[k]--;
                else
                    sOffsetIncr[k]++;
                flag = 1;
            }
            if(currOffset<=eOffset[k]) {
                if(action==1)
                    eOffsetIncr[k]--;
                else
                    eOffsetIncr[k]++;
                flag = 1;
            }
            if(currOffset<=hOffset[k]) {
                if(action==1)
                    hOffsetIncr[k]--;
                else
                    hOffsetIncr[k]++;
                flag = 1;
            }
            if(flag==1)
                if(DEBUG)System.out.println(" Decrementing for instrIncr ");
        }
        
    }

    /** Updates the exception offset based in the length of the instruction that is 
     *  inserted or deleted.
     */
    private void updateExceptionOffset(int currOffset, int instrlen)
    {
        if(exMark==null)
            return;
        for(int k=0; k<exMark.length; k++) {
            if(exMark[k]==0)
                continue;
            int flag=0;
            if(currOffset<=sOffset[k]) {
                sOffsetIncr[k]+=instrlen;
                flag = 1;
            }
            if(currOffset<=eOffset[k]) {
                eOffsetIncr[k]+=instrlen;
                flag = 1;
            }
            if(currOffset<=hOffset[k]) {
                hOffsetIncr[k]+=instrlen;
                flag = 1;
            }
            if(flag==1)
                if(DEBUG)System.out.println(" Incrementing for instr -> len = "+instrlen);
        }
        return;
    }


    /** Implements the entire BasicBlock to Seq.Instr routines, etc.
     */
    public sandmark.program.Method createOutlineFunction(
                    org.apache.bcel.generic.InstructionHandle fromIH, 
                    org.apache.bcel.generic.InstructionHandle toIH,
                    LiveVar lv1[], LiveVar lv2[], LiveVar lv3[])
    {
        if(DEBUG) System.out.println("\n In fn. createOutlineFunction ... \n");

        /* get the exception table(if any) */
        org.apache.bcel.generic.CodeExceptionGen[] ceg = meth.getExceptionHandlers();
        if(ceg!=null) {
            sPC = new org.apache.bcel.generic.InstructionHandle[ceg.length];
            sOffset = new int[ceg.length];
            sOffsetIncr = new int[ceg.length];
            ePC = new org.apache.bcel.generic.InstructionHandle[ceg.length];
            eOffset = new int[ceg.length];
            eOffsetIncr = new int[ceg.length];
            hPC = new org.apache.bcel.generic.InstructionHandle[ceg.length];
            hOffset = new int[ceg.length];
            hOffsetIncr = new int[ceg.length];
            exctype = new org.apache.bcel.generic.ObjectType[ceg.length];
            exMark = new int[ceg.length];
            for(int p=0; p<ceg.length; p++) 
                exMark[p]=0;
        }
        else {
            sPC=ePC=hPC=null;
            exMark=null;
        }
        org.apache.bcel.generic.InstructionHandle savIH = null;

        /* Recreate bytecode sequence from the CFG within the * 'fromIH' and 'toIH' range */
        sandmark.analysis.controlflowgraph.BasicBlock fromBB = mcfg.getBlock(fromIH);
        sandmark.analysis.controlflowgraph.BasicBlock toBB = mcfg.getBlock(toIH);

        boolean equiBlk=false; 
        if(fromBB.equals(toBB)) {
            if(DEBUG)System.out.println(" SAME BLOCK ");
            equiBlk=true;
        }

        java.util.LinkedList nontarget = new java.util.LinkedList();
        java.util.LinkedList target = new java.util.LinkedList();

        sandmark.analysis.controlflowgraph.BasicBlock headerBB =
            new sandmark.analysis.controlflowgraph.BasicBlock(mcfg);

        org.apache.bcel.generic.InstructionList ilist = 
            new org.apache.bcel.generic.InstructionList();
        org.apache.bcel.generic.InstructionHandle nopIH = 
            ilist.insert(new org.apache.bcel.generic.NOP());

        /* First extract the second half of the split Block ; and create a new block*/
        org.apache.bcel.generic.InstructionHandle ih = fromIH;
        if(equiBlk)
            for(; ih!=toIH; ih=ih.getNext()) {
                if(DEBUG) System.out.println("adding to headerBB: "+ih);
                headerBB.addInst(ih);
            }
        else
            for(; ih!=fromBB.getLastInstruction(); ih=ih.getNext()) {
                if(DEBUG) System.out.println("adding to headerBB: "+ih);
                headerBB.addInst(ih);
            }
        headerBB.addInst(ih);
        if(DEBUG) System.out.println("adding to headerBB: "+ih);

        nontarget.add(headerBB);
        boolean isHeader = true;

        java.util.ArrayList uselist = new java.util.ArrayList();
        uselist.add(headerBB);

        java.util.ArrayList branchList = new java.util.ArrayList();
        java.util.ArrayList targetList = new java.util.ArrayList();
        java.util.ArrayList ftarget = new java.util.ArrayList();
        for(int t=0; t<20; t++)
            ftarget.add(null);

        while(nontarget.size()>0 || target.size()>0) {

            if(DEBUG) System.out.println("targetsize = "+target.size()+" nontargetsize = "+nontarget.size());
            sandmark.analysis.controlflowgraph.BasicBlock tempBB = null;
            if(nontarget.size()!=0) {
                if(DEBUG) System.out.println("nontarget");
                tempBB = (sandmark.analysis.controlflowgraph.BasicBlock)nontarget.removeFirst();
            }
            else {
                if(DEBUG) System.out.println("target");
                tempBB = (sandmark.analysis.controlflowgraph.BasicBlock)target.removeFirst();
            }

            if(tempBB.equals(toBB)||equiBlk) {
                if(DEBUG) System.out.println(" only append till 'toIH' and exit while loop ");
                org.apache.bcel.generic.InstructionHandle tempih= null;
                if(DEBUG) System.out.println("toIH: "+toIH.getInstruction());
                for(tempih=tempBB.getIH(); tempih!=toIH; tempih=tempih.getNext()) {
                    if(DEBUG) System.out.println("appendding : "+tempih.getInstruction());
                    int t;
                    for(t=0; t<targetList.size(); t++)
                        if( ((org.apache.bcel.generic.InstructionHandle)
                                    targetList.get(t)).equals(tempih)) {
                            if(DEBUG) System.out.println("setting ftarget @ "+t+" to --> "+tempih.getInstruction());
                            savIH = ilist.insert(nopIH, tempih.getInstruction());
                            ftarget.set(t, savIH);
                            this.saveExceptionHandler(tempih, ceg, savIH);
                            break;
                        }
                    if(t==targetList.size()) {
                        savIH = ilist.insert(nopIH, tempih.getInstruction());
                        this.saveExceptionHandler(tempih, ceg, savIH);
                    }
                }
                savIH = ilist.insert(nopIH, tempih.getInstruction());
                this.saveExceptionHandler(tempih, ceg, savIH);
                if(DEBUG) System.out.println("appendding : "+tempih.getInstruction());
                break;
            }
        
            for(org.apache.bcel.generic.InstructionHandle tempih=tempBB.getIH();
                    tempih!=tempBB.getLastInstruction(); tempih=tempih.getNext()) {
                if(DEBUG) System.out.println("appending : "+tempih.getInstruction());
                int t;
                for(t=0; t<targetList.size(); t++)
                    if( ((org.apache.bcel.generic.InstructionHandle)
                                targetList.get(t)).equals(tempih)) {
                        if(DEBUG) System.out.println("setting ftargett @ "+t+" to --> "+tempih.getInstruction());
                        savIH = ilist.insert(nopIH, tempih.getInstruction());
                        ftarget.set(t, savIH);
                        this.saveExceptionHandler(tempih, ceg, savIH);
                        break;
                    }
                if(t==targetList.size()) {
                    savIH = ilist.insert(nopIH, tempih.getInstruction());
                    this.saveExceptionHandler(tempih, ceg, savIH);
                }
            }

            org.apache.bcel.generic.BranchHandle bh =  null;
            if( (tempBB.getLastInstruction().toString()).indexOf("if")!=-1 ||
                (tempBB.getLastInstruction().toString()).indexOf("goto")!=-1 ) {
                bh = ilist.insert(nopIH, this.createBranchInstruction((tempBB.getLastInstruction().toString())));
                branchList.add(bh);
                this.saveExceptionHandler(tempBB.getLastInstruction(), ceg, bh);
                if((tempBB.getLastInstruction().toString()).indexOf("goto")!=-1) {
                    /* maybe the next instruction is exceptionhandler PC; so CFG traversal will skip it */
                    if(this.isHandlerStart(tempBB.getLastInstruction().getNext(), ceg)) {
                        org.apache.bcel.generic.InstructionHandle endCatchIH = 
                            (((org.apache.bcel.generic.BranchHandle)tempBB.getLastInstruction()).getTarget()).getPrev();
                        if(DEBUG)System.out.println(" endCatchIH -> "+endCatchIH);
                        /* insert code from 'tempBB.getLastInstruction().getNext()' to 'endCatchIH' in 'ilist' */
                        org.apache.bcel.generic.InstructionHandle exIH;
                        for(exIH=tempBB.getLastInstruction().getNext(); exIH!=endCatchIH; exIH=exIH.getNext()) {
                            savIH = ilist.insert(nopIH, exIH.getInstruction());
                            this.saveExceptionHandler(exIH, ceg, savIH);
                        }
                        savIH = ilist.insert(nopIH, exIH.getInstruction());
                        this.saveExceptionHandler(exIH, ceg, savIH);
                    }
                }
            }
            else {
                int t;
                for(t=0; t<targetList.size(); t++)
                    if( ((org.apache.bcel.generic.InstructionHandle)
                                targetList.get(t)).equals(tempBB.getLastInstruction()) ) {
                        savIH = ilist.insert(nopIH, tempBB.getLastInstruction().getInstruction());
                        ftarget.set(t, savIH);
                        this.saveExceptionHandler(tempBB.getLastInstruction(), ceg, savIH);
                        break;
                    }
                if(t==targetList.size()) {
                    savIH = ilist.insert(nopIH, tempBB.getLastInstruction().getInstruction());
                    this.saveExceptionHandler(tempBB.getLastInstruction(), ceg, savIH);
                }
            }
            if(DEBUG) System.out.println("appending : "+tempBB.getLastInstruction());


            /* Get the successors */
            java.util.Iterator succlist = null;
            if(isHeader) {
                succlist = mcfg.succs(fromBB);
                isHeader = false;
            }
            else
                succlist = mcfg.succs(tempBB);

            // TBD: BUG: this will throw exception if last statement is RETURN 
            
            org.apache.bcel.generic.InstructionHandle targetIH =  null;
            if(tempBB.getLastInstruction() instanceof org.apache.bcel.generic.BranchHandle) {
                targetIH = ((org.apache.bcel.generic.BranchHandle)tempBB.getLastInstruction()).getTarget();
                targetList.add(targetIH);
            }

            while(succlist.hasNext()) {
                sandmark.analysis.controlflowgraph.BasicBlock succBB =
                    (sandmark.analysis.controlflowgraph.BasicBlock)succlist.next();
                if(uselist.contains(succBB))
                    continue;

                uselist.add(succBB);
                if(targetIH==null) {
                    nontarget.add(succBB);
                    continue;
                }

                if(succBB.getIH().equals(targetIH))
                    target.add(succBB);
                else
                    nontarget.add(succBB);
            }
        }

        if(DEBUG) {
            System.out.println(" branchList.size() -> "+branchList.size());
            System.out.println(" targetList.size() -> "+targetList.size());
            System.out.println(" ftarget.size() -> "+ftarget.size());
        }

        for(int t=0; t<branchList.size(); t++) {
            org.apache.bcel.generic.BranchHandle bh =
                (org.apache.bcel.generic.BranchHandle)branchList.get(t);
            bh.setTarget((org.apache.bcel.generic.InstructionHandle)ftarget.get(t));
        }

        ilist.setPositions();

        if(DEBUG) {
            System.out.println(" Displaying instruction list ... \n");
            org.apache.bcel.generic.InstructionHandle tih[] = ilist.getInstructionHandles();
            for(int w=0; w<tih.length; w++) 
                System.out.println(tih[w]);
        }


        /* Better to commit the instructionlist into the Outline method over here */
        int access_flags = org.apache.bcel.Constants.ACC_PUBLIC |
                           org.apache.bcel.Constants.ACC_STATIC;
        org.apache.bcel.generic.Type return_type= 
            org.apache.bcel.generic.Type.VOID; // to be changed later;

        int paramCnt=0;
        if(lv1!=null)
            paramCnt+=lv1.length;
        if(lv3!=null)
            paramCnt++; // =lv3.length;
        org.apache.bcel.generic.Type[] arg_types;
        String[] arg_names = null;
        if(DEBUG) System.out.println("paramCnt = "+paramCnt);
        if(paramCnt==0) {
            arg_types = org.apache.bcel.generic.Type.NO_ARGS;
            arg_names = null;
        }
        else {
            arg_types = new org.apache.bcel.generic.Type[paramCnt];
            arg_names = new String[paramCnt];
        }
        int cnt=0;
        if(lv1!=null)
            for(int p=0; p<lv1.length; p++)
                arg_types[cnt++] = lv1[p].getType();
        if(lv3!=null)
            arg_types[cnt++] = org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;");
        for(int p=0; p<cnt; p++)
            arg_names[p] = "ARG"+ (new Integer(p)).toString();

        sandmark.program.LocalMethod outlinemeth =
            new sandmark.program.LocalMethod((sandmark.program.Class)meth.getParent(), access_flags, 
                            return_type, arg_types, arg_names, "OUTLINEMETHOD", ilist);
        
        // TBD: copy the ExceptionTable ranges to the method Object. - done!
        if(ceg!=null) {
            for(int w=0; w<exMark.length; w++)
                if(exMark[w]==1) {
                    sOffset[w] =  sPC[w].getPosition();
                    eOffset[w] =  ePC[w].getPosition();
                    hOffset[w] =  hPC[w].getPosition();
                    if(DEBUG)System.out.println("w = "+w);
                    if(DEBUG)System.out.println("sPC = "+sPC[w]);
                    if(DEBUG)System.out.println("sOffset = "+sOffset[w]);
                    if(DEBUG)System.out.println("ePC = "+ePC[w]);
                    if(DEBUG)System.out.println("eOffset = "+eOffset[w]);
                    if(DEBUG)System.out.println("hPC = "+hPC[w]);
                    if(DEBUG)System.out.println("hOffset = "+hOffset[w]);
                    if(DEBUG)System.out.println("exctype = "+exctype[w]);
                //  outlinemeth.addExceptionHandler(sPC[w], ePC[w], hPC[w], exctype[w]);
                }
        }
        outlinemeth.mark();
        

        /* Current 'ilist' contains just all the 'raw' instructions; 
         * Match the index of the parameter(LV1) with that of these instructions.
         * Also, extract LV3 values from objectref[] in the parameter and save in there
         * corresponding primitives. All these instructions are inserted at the start
         * of the method code. 
         */

        org.apache.bcel.generic.ConstantPoolGen cpg = 
            ((sandmark.program.Class)meth.getParent()).getConstantPool();
        org.apache.bcel.generic.InstructionFactory ifactory = 
            new org.apache.bcel.generic.InstructionFactory(cpg);

        ilist = outlinemeth.getInstructionList();
        org.apache.bcel.generic.InstructionHandle tempih[] = ilist.getInstructionHandles();

        if(DEBUG)System.out.println(" For LV1, change the access indices.......................");
        if(lv1!=null) {
            if(DEBUG) System.out.println(" lv1 is not null >> "+lv1.length);
            for(int k=0; k<lv1.length; k++) {
                for(int m=0; m<tempih.length; m++) {
                    org.apache.bcel.generic.Instruction instr = tempih[m].getInstruction();
                    if (instr instanceof org.apache.bcel.generic.LocalVariableInstruction) {
                        org.apache.bcel.generic.LocalVariableInstruction lvi =
                            (org.apache.bcel.generic.LocalVariableInstruction)instr;
                        int oldIndex=lvi.getIndex(); //'oldIndex' not required actually; for debugging
                        if(lv1[k].getSlot()==oldIndex) {
                            if(DEBUG) System.out.println("for -> "+lvi+" : shifting index from "+oldIndex+" to "+k);
                            lvi.setIndex(k);
                            /* if change is such that it shortens instr.length, adjust xOffsetIncr */
                            if((instr.toString().indexOf("load")==-1)||(instr.toString().indexOf("store")==-1))
                                if(oldIndex>3 && k<3)
                                    this.indexOffsetUpdate(tempih[m].getPosition(), 1);
                                if(oldIndex<3 && k>3) // is this case possible ?
                                    this.indexOffsetUpdate(tempih[m].getPosition(), 2);
                        }
                    }
                }
            }
        }
        else
            if(DEBUG) System.out.println(" lv1 is null >> ");
        outlinemeth.mark();
        ilist.setPositions();
        outlinemeth.setInstructionList(ilist);

        /* For LV3, include code at beginning: Object[] -> Primitive/Object , and
         * at end of each primitive store, do: Primitive/Object -> Object[] storage
         * ...........................................................................
         */
        if(lv3!=null) {
            if(DEBUG) System.out.println(" lv3 is not null >> "+lv3.length);
            java.util.ArrayList newInstrs = new java.util.ArrayList();

            /* For each store for LV3 Primitive/Object, store in corresponding Object[] index
             *          dup;     // one copy left for the actual Primitive/Object store
             *          if(prim_type)
             *              new <object_type>;
             *              dup_x1; (or dup_x2) for double/long
             *              swap;  ( or dup_x2 / pop) for double/long
             *              invokespecial <init>;
             *
             *          aload <LV3index>;
             *          swap;
             *          iconst_<arrayindex>
             *          swap;
             *          aaload;
             */
            org.apache.bcel.generic.InstructionHandle ihs[] = ilist.getInstructionHandles();
            int object_slot = 0;
            if(lv1!=null)
                object_slot += lv1.length;
            int swordGrouplen = this.getSWordGrpLen(ifactory);
            int dwordGrouplen = this.getDWordGrpLen(ifactory);
            for(int k=0; k<ihs.length; k++) {
                if(!(ihs[k].getInstruction() instanceof 
                        org.apache.bcel.generic.LocalVariableInstruction))
                    continue;
                if(ihs[k].getInstruction().toString().indexOf("store")==-1)
                    continue;

                int prim_slot = ((org.apache.bcel.generic.LocalVariableInstruction)
                                 ihs[k].getInstruction()).getIndex();
                int arrindex;
                for(arrindex=0; arrindex<lv3.length; arrindex++)
                    if(lv3[arrindex].getSlot()==prim_slot)
                        break;
                if(arrindex==lv3.length)
                    continue;

                int currOffset = ihs[k].getPosition();

                /* At this point we have 'arrindex' and its corresponding 'object_slot' */
                if(DEBUG) System.out.println(" object_slot+arrindex = "+ 
                                (object_slot+arrindex) + " <--  prim_slot = "+prim_slot);
                org.apache.bcel.generic.Type lvtype = lv3[arrindex].getType();

                ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP);
                this.updateExceptionOffset(currOffset, (org.apache.bcel.generic.InstructionConstants.DUP).getLength());
                if(typeTable.get(lvtype).equals(INTEGER_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(INTEGER_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X1);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Integer",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                
                    this.updateExceptionOffset(currOffset, swordGrouplen);
                }
                if(typeTable.get(lvtype).equals(FLOAT_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(FLOAT_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X1);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Float",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                    this.updateExceptionOffset(currOffset, swordGrouplen);
                }
                if(typeTable.get(lvtype).equals(BYTE_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(BYTE_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X1);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Byte",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                    this.updateExceptionOffset(currOffset, swordGrouplen);
                }
                if(typeTable.get(lvtype).equals(CHARACTER_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(CHARACTER_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X1);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Character",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                    this.updateExceptionOffset(currOffset, swordGrouplen);
                }
                if(typeTable.get(lvtype).equals(BOOLEAN_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(BOOLEAN_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X1);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Boolean",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                    this.updateExceptionOffset(currOffset, swordGrouplen);
                }
                if(typeTable.get(lvtype).equals(SHORT_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(SHORT_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X1);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Short",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                    this.updateExceptionOffset(currOffset, swordGrouplen);
                } 
                if(typeTable.get(lvtype).equals(LONG_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(LONG_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X2);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X2);
                     ilist.insert(ihs[k], new org.apache.bcel.generic.POP());
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Long",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                    this.updateExceptionOffset(currOffset, dwordGrouplen);
                }
                if(typeTable.get(lvtype).equals(DOUBLE_TYPE)) {
                     ilist.insert(ihs[k], ifactory.createNew(DOUBLE_TYPE));
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X2);
                     ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.DUP_X2);
                     ilist.insert(ihs[k], new org.apache.bcel.generic.POP());
                     ilist.insert(ihs[k], ifactory.createInvoke("java/lang/Double",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                    this.updateExceptionOffset(currOffset, dwordGrouplen);
                }

                org.apache.bcel.generic.Instruction savInstr;
                savInstr = ifactory.createLoad(
                     org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;"), object_slot);
                ilist.insert(ihs[k], savInstr);
                this.updateExceptionOffset(currOffset, savInstr.getLength());
                ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                this.updateExceptionOffset(currOffset, (org.apache.bcel.generic.InstructionConstants.SWAP).getLength());
                savInstr = this.createPushInstruction(arrindex);
                ilist.insert(ihs[k], savInstr);
                this.updateExceptionOffset(currOffset, savInstr.getLength());
                ilist.insert(ihs[k], org.apache.bcel.generic.InstructionConstants.SWAP);
                this.updateExceptionOffset(currOffset, (org.apache.bcel.generic.InstructionConstants.SWAP).getLength());
                ilist.insert(ihs[k], new org.apache.bcel.generic.AASTORE());
                this.updateExceptionOffset(currOffset, (new org.apache.bcel.generic.AASTORE()).getLength());
            }
            outlinemeth.mark();
            ilist.setPositions();
            outlinemeth.setInstructionList(ilist);


            /* Include the code at start of 'OUTLINEMETHOD' for 
             *  Object[]  -> Primitive/Object storage
             *
             *  aload arrayref;
             *  for(;;)
             *      dup;
             *      push <arrayindex>;
             *      aaload;
             *      checkcast <object_type>;
             *      if(prim_type) 
             *          invokevirtual .<type>Value();
             *      <type>store<prim_slot>;
             *  pop;
             *
             */
            object_slot = 0;
            if(lv1!=null) 
                object_slot += lv1.length;
            
            newInstrs.add(ifactory.createLoad(
                              org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;"), object_slot));

            for(int n=0; n<lv3.length; n++) {
                newInstrs.add(org.apache.bcel.generic.InstructionConstants.DUP);
                newInstrs.add(createPushInstruction(n));
                newInstrs.add(new org.apache.bcel.generic.AALOAD());

                int prim_slot = lv3[n].getSlot();
                org.apache.bcel.generic.Type lvtype = lv3[n].getType();

                if(lvtype instanceof org.apache.bcel.generic.ReferenceType) {
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    continue;
                }
                if(typeTable.get(lvtype).equals(INTEGER_TYPE) ||
                    lvtype.equals(INTEGER_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(INTEGER_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Integer",
                                                   "intValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(FLOAT_TYPE) ||
                    lvtype.equals(FLOAT_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(FLOAT_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Float",
                                                   "floatValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(BYTE_TYPE) ||
                    lvtype.equals(BYTE_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(BYTE_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Byte",
                                                   "byteValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(DOUBLE_TYPE) ||  
                    lvtype.equals(DOUBLE_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(DOUBLE_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Double",
                                                   "doubleValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(LONG_TYPE) ||
                    lvtype.equals(LONG_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(LONG_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Long",
                                                   "longValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(SHORT_TYPE) ||
                    lvtype.equals(SHORT_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(SHORT_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Short",
                                                   "shortValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(BOOLEAN_TYPE) ||
                    lvtype.equals(BOOLEAN_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(BOOLEAN_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Boolean",
                                                   "booleanValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(CHARACTER_TYPE) ||
                    lvtype.equals(CHARACTER_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(CHARACTER_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Character",
                                                   "characterValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
            }
            newInstrs.add(org.apache.bcel.generic.InstructionConstants.POP);
            for(int m=0; m<newInstrs.size(); m++) {
                ilist.insert(tempih[0], (org.apache.bcel.generic.Instruction)newInstrs.get(m));
                this.updateExceptionOffset(0, ((org.apache.bcel.generic.Instruction)newInstrs.get(m)).getLength());
            }

            outlinemeth.mark();
            ilist.setPositions();
            outlinemeth.setInstructionList(ilist);

        }
        else
            if(DEBUG) System.out.println(" lv3 is null >> ");


        /* For LV2, promote and pack the LV2 into Object[] and return; 
         * Do this only if(LV2.length>1)  .....................................................
         */
        if(lv2!=null) {
            if(DEBUG) System.out.println(" lv2 is not null >> "+lv2.length);

            if(lv2.length==1) {
                // No promoting-packing done ...
                org.apache.bcel.generic.Type lvtype = lv2[0].getType();
                ilist.append(ifactory.createLoad(lvtype, lv2[0].getSlot()));
                ilist.append(ifactory.createReturn(lvtype));
                outlinemeth.setReturnType(lvtype);
            }
            else {
                // Else, promote-pack ...
                int numMembers = lv2.length;
                ilist.append(createPushInstruction(numMembers));
                short dim = 1;
                ilist.append(ifactory.createNewArray(org.apache.bcel.generic.Type.OBJECT, dim));
                /*
                 * arrref at top of stack now;
                 *      dup;
                 *      push index;
                 *      if(reference_type)
                 *          <object_type>load<slot>;
                 *          AASTORE;
                 *      else
                 *          new <objtype>;
                 *          dup;
                 *          <prim_type>load<slot>;
                 *          invokespecial <init>;
                 *          AASTORE;
                 */         

                for(int k=0; k<lv2.length; k++) {
                    ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                    ilist.append(createPushInstruction(k));
                    org.apache.bcel.generic.Type lvtype = lv2[k].getType();
                    if(lvtype instanceof org.apache.bcel.generic.ReferenceType) {
                        /* No promotion required; Just add to array */
                        ilist.append(ifactory.createLoad(lvtype, lv2[k].getSlot()));
                        ilist.append(ifactory.createArrayStore(lvtype)); // org.apache.bcel.generic.Type.OBJECT) 
                    }
                    else {
                        /* Promote the primitive local first; then pack it into the array */
                        int prim_slot = lv2[k].getSlot();
                        
                        if(typeTable.get(lvtype).equals(INTEGER_TYPE)) {
                             ilist.append(ifactory.createNew(INTEGER_TYPE));
                             ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                             ilist.append(ifactory.createLoad(lvtype, prim_slot));
                             ilist.append(ifactory.createInvoke("java/lang/Integer",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                             ilist.append(ifactory.createArrayStore(INTEGER_TYPE));
                        }
                        if(typeTable.get(lvtype).equals(FLOAT_TYPE)) {
                             ilist.append(ifactory.createNew(FLOAT_TYPE));
                             ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                             ilist.append(ifactory.createLoad(lvtype, prim_slot));
                             ilist.append(ifactory.createInvoke("java/lang/Float",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                             ilist.append(ifactory.createArrayStore(FLOAT_TYPE));
                        }
                        if(typeTable.get(lvtype).equals(BYTE_TYPE)) {
                             ilist.append(ifactory.createNew(BYTE_TYPE));
                             ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                             ilist.append(ifactory.createLoad(lvtype, prim_slot));
                             ilist.append(ifactory.createInvoke("java/lang/Byte",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                             ilist.append(ifactory.createArrayStore(BYTE_TYPE));
                        }
                        if(typeTable.get(lvtype).equals(DOUBLE_TYPE)) {
                             ilist.append(ifactory.createNew(DOUBLE_TYPE));
                             ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                             ilist.append(ifactory.createLoad(lvtype, prim_slot));
                             ilist.append(ifactory.createInvoke("java/lang/Double",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                             ilist.append(ifactory.createArrayStore(DOUBLE_TYPE));
                        }
                        if(typeTable.get(lvtype).equals(CHARACTER_TYPE)) {
                             ilist.append(ifactory.createNew(CHARACTER_TYPE));
                             ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                             ilist.append(ifactory.createLoad(lvtype, prim_slot));
                             ilist.append(ifactory.createInvoke("java/lang/Character",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                             ilist.append(ifactory.createArrayStore(CHARACTER_TYPE));
                        }
                        if(typeTable.get(lvtype).equals(BOOLEAN_TYPE)) {
                             ilist.append(ifactory.createNew(BOOLEAN_TYPE));
                             ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                             ilist.append(ifactory.createLoad(lvtype, prim_slot));
                             ilist.append(ifactory.createInvoke("java/lang/Boolean",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                             ilist.append(ifactory.createArrayStore(BOOLEAN_TYPE));
                        }
                        if(typeTable.get(lvtype).equals(SHORT_TYPE)) {
                             ilist.append(ifactory.createNew(SHORT_TYPE));
                             ilist.append(org.apache.bcel.generic.InstructionConstants.DUP);
                             ilist.append(ifactory.createLoad(lvtype, prim_slot));
                             ilist.append(ifactory.createInvoke("java/lang/Short",
                                                                "<init>",
                                                                VOID_TYPE,
                                                                new org.apache.bcel.generic.Type[] {lvtype},
                                                                org.apache.bcel.Constants.INVOKESPECIAL));
                             ilist.append(ifactory.createArrayStore(SHORT_TYPE));
                        }
                    }
                }
                ilist.append(ifactory.createReturn(org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;")));
                outlinemeth.setReturnType(org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;"));
            }
        }
        else {
            if(DEBUG) System.out.println(" lv2 is null >> ");
            ilist.append(ifactory.createReturn(org.apache.bcel.generic.Type.VOID));
            outlinemeth.setReturnType(org.apache.bcel.generic.Type.VOID);
        }

        /* remove the 'nopIH' instruction @ end of list */
        org.apache.bcel.generic.InstructionHandle tih[] = ilist.getInstructionHandles();
        int t;
        for(t=0; t<tih.length;t++)
            if(tih[t].toString().indexOf("nop")!=-1)
                break;
        try {
            if(DEBUG) System.out.println(" deleted NOP :"+ nopIH.getInstruction());
            ilist.delete(tih[t]);
        }catch(org.apache.bcel.generic.TargetLostException e){
            System.out.println(" Caught exception : "+e);
        }
        outlinemeth.mark();
        ilist.setPositions();
        outlinemeth.setInstructionList(ilist);

        org.apache.bcel.generic.InstructionHandle newIH[] = ilist.getInstructionHandles();

        meth.mark();
        outlinemeth.removeExceptionHandlers();
        if(ceg!=null) {
            for(int w=0; w<exMark.length; w++) {
                if(exMark[w]==1) {
                    if(DEBUG)System.out.println("sOffsetIncr = "+sOffsetIncr[w]);
                    if(DEBUG)System.out.println("eOffsetIncr = "+eOffsetIncr[w]);
                    if(DEBUG)System.out.println("hOffsetIncr = "+hOffsetIncr[w]);
                    sOffset[w] += sOffsetIncr[w];
                    eOffset[w] += eOffsetIncr[w];
                    hOffset[w] += hOffsetIncr[w];
                    if(DEBUG)System.out.println(" Adding a new handler into Outline Method ");
                    outlinemeth.addExceptionHandler(this.offsetToIH(sOffset[w], ilist),
                            this.offsetToIH(eOffset[w], ilist), this.offsetToIH(hOffset[w], ilist), exctype[w]);
                }
            }
        }
        outlinemeth.mark();
                    //outlinemeth.addExceptionHandler(newIH[28], newIH[31], newIH[33], exctype[w]);
                    /*
                    if(DEBUG)System.out.println("w = "+w);
                    if(DEBUG)System.out.println("sPC = "+sPC[w]);
                    if(DEBUG)System.out.println("ePC = "+ePC[w]);
                    if(DEBUG)System.out.println("hPC = "+hPC[w]);
                    if(DEBUG)System.out.println("exctype = "+exctype[w]);
                    outlinemeth.addExceptionHandler(sPC[w], ePC[w], hPC[w], exctype[w]);*/
        
        if(DEBUG) {
            System.out.println(" outlinemeth.name -> "+outlinemeth.getName());
            System.out.println(" outlinemeth.classname -> "+outlinemeth.getClassName());
            System.out.println(" outlinemeth.getSignature -> "+outlinemeth.getSignature());
            System.out.println(" outlinemeth.returnType -> "+outlinemeth.getReturnType());
            System.out.println(" Displaying final outlinemeth instruction list ... \n");
            if(ilist==null) System.out.println(" ilist is null ");
            tih = ilist.getInstructionHandles();
            for(int w=0; w<tih.length; w++) 
                System.out.println(tih[w]);
        }

        return outlinemeth;
    }

    /**  Returns the instruction handle that has position 'offset' in 'ilist'
     */
    private org.apache.bcel.generic.InstructionHandle 
            offsetToIH(int offset, org.apache.bcel.generic.InstructionList ilist)
    {
        int len=0;
        if(DEBUG)System.out.println(" offset = "+offset);
        org.apache.bcel.generic.InstructionHandle[] ih = ilist.getInstructionHandles();
        for(int k=0; k<ih.length; k++) {
            //if(DEBUG)System.out.println(" len = "+len);
            if(offset==len)
                return ih[k];
            len+= ih[k].getInstruction().getLength();
        }
        if(BUG)System.out.println("BUG: offset match not found in insrtuction list ");
        return null;
    }


    /** This function unpacks all the information ie. returned from the outlined method; and also 
     *  the information that passes through the outlined code.
     */
    void unpackLocals(org.apache.bcel.generic.InstructionHandle toIH, 
                      LiveVar lv2[], 
                      LiveVar lv3[], 
                      int LV3index, 
                      sandmark.program.Method outlinemeth)
    {
        if(DEBUG) System.out.println("\n In fn. unpackLocals ... \n");
        org.apache.bcel.generic.ConstantPoolGen cpg = 
            ((sandmark.program.Class)meth.getParent()).getConstantPool();
        org.apache.bcel.generic.InstructionFactory ifactory = 
            new org.apache.bcel.generic.InstructionFactory(cpg);
        org.apache.bcel.generic.InstructionList ilist = meth.getInstructionList();

        org.apache.bcel.generic.InstructionHandle tonextIH = toIH.getNext();
        /* Unpack the returned LV2 registers */
        if(lv2!=null) {
            if(DEBUG) System.out.println(" lv2 is not null >> "+lv2.length);
            java.util.ArrayList newInstrs = new java.util.ArrayList();

            if(lv2.length==1) {
                /* 
                 * <type>store <slot> 
                 */
                newInstrs.add(ifactory.createStore(lv2[0].getType(), lv2[0].getSlot()));
            }
            else {
                /* Include the code after 'toIH'  for 
                 *  object  -> primitive storage
                 *  'object[] is in top of stack'
                 *      dup
                 *      push index
                 *      aaload 
                 *      checkcast <object_type>
                 *      invokevirtual <type>Value() .. (only for nonreferencetypes)
                 *      <type>store
                 */
                for(int n=0; n<lv2.length; n++) {
                    int prim_slot = lv2[n].getSlot();
                    int object_index = n;
                    org.apache.bcel.generic.Type lvtype = lv2[n].getType();
    
                    newInstrs.add(org.apache.bcel.generic.InstructionConstants.DUP);
    
                    if(lvtype instanceof org.apache.bcel.generic.ReferenceType) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                        continue;
                    }
                    if(typeTable.get(lvtype).equals(INTEGER_TYPE) || 
                                        lvtype.equals(INTEGER_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(INTEGER_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Integer",
                                                       "intValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                    if(typeTable.get(lvtype).equals(BOOLEAN_TYPE) || 
                                        lvtype.equals(BOOLEAN_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(BOOLEAN_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Boolean",
                                                       "booleanValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                    if(typeTable.get(lvtype).equals(BYTE_TYPE) || 
                                        lvtype.equals(BYTE_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(BYTE_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Byte",
                                                       "byteValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                    if(typeTable.get(lvtype).equals(CHARACTER_TYPE) || 
                                        lvtype.equals(CHARACTER_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(CHARACTER_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Character",
                                                       "charValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                    if(typeTable.get(lvtype).equals(DOUBLE_TYPE) || 
                                        lvtype.equals(DOUBLE_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(DOUBLE_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Double",
                                                       "doubleValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                    if(typeTable.get(lvtype).equals(LONG_TYPE) || 
                                        lvtype.equals(LONG_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(LONG_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Long",
                                                       "longValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                    if(typeTable.get(lvtype).equals(SHORT_TYPE) || 
                                        lvtype.equals(SHORT_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(SHORT_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Short",
                                                       "shortValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                    if(typeTable.get(lvtype).equals(FLOAT_TYPE) || 
                                        lvtype.equals(FLOAT_TYPE)) {
                        newInstrs.add(createPushInstruction(object_index));
                        newInstrs.add(ifactory.createArrayLoad(org.apache.bcel.generic.Type.OBJECT));
                        newInstrs.add(ifactory.createCheckCast(FLOAT_TYPE));
                        if(!(lvtype instanceof org.apache.bcel.generic.ReferenceType))
                            newInstrs.add
                                (ifactory.createInvoke("java/lang/Float",
                                                       "floatValue",
                                                       lvtype,
                                                       org.apache.bcel.generic.Type.NO_ARGS,
                                                       org.apache.bcel.Constants.INVOKEVIRTUAL));
                        newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    }
                }
                /* pop out the reference object from the stack */
                newInstrs.add(org.apache.bcel.generic.InstructionConstants.POP);
            }

            for(int m=0; m<newInstrs.size(); m++) {
                ilist.insert(tonextIH, (org.apache.bcel.generic.Instruction)newInstrs.get(m));
                exOffset+=((org.apache.bcel.generic.Instruction)newInstrs.get(m)).getLength();
            }

            meth.mark();
            ilist.setPositions();
            meth.setInstructionList(ilist);
        }


        /* Unpack for LV3: Object[] -> Object-Primitive storage 
         */
        if(lv3!=null) {
            if(DEBUG) System.out.println(" lv3 is not null >> "+lv3.length);

            java.util.ArrayList newInstrs = new java.util.ArrayList();
            /* 
             *  aload arrayref;(object reference not @ top of stack)
             *  for(;;)
             *      dup;
             *      push arrayindex;
             *      aaload;
             *      checkcast <object_type>;
             *      if(prim_type) 
             *          invokevirtual <type>Value();
             *      <type>store<prim_slot>
             *  pop;
             *
             */
            int object_slot = LV3index;
            newInstrs.add(ifactory.createLoad(
                                org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;"),
                                object_slot));

            for(int n=0; n<lv3.length; n++) {
                newInstrs.add(org.apache.bcel.generic.InstructionConstants.DUP);
                newInstrs.add(createPushInstruction(n));
                newInstrs.add(new org.apache.bcel.generic.AALOAD());

                int prim_slot = lv3[n].getSlot();
                org.apache.bcel.generic.Type lvtype = lv3[n].getType();

                if(lvtype instanceof org.apache.bcel.generic.ReferenceType) {
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                    continue;
                }
                if(typeTable.get(lvtype).equals(INTEGER_TYPE) ||
                   lvtype.equals(INTEGER_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(INTEGER_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Integer",
                                                   "intValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(FLOAT_TYPE) ||
                   lvtype.equals(FLOAT_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(FLOAT_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Float",
                                                   "floatValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(BYTE_TYPE) ||
                   lvtype.equals(BYTE_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(BYTE_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Byte",
                                                   "byteValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(DOUBLE_TYPE) ||  
                   lvtype.equals(DOUBLE_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(DOUBLE_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Double",
                                                   "doubleValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(LONG_TYPE) ||
                   lvtype.equals(LONG_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(LONG_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Long",
                                                   "longValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(SHORT_TYPE) ||
                   lvtype.equals(SHORT_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(SHORT_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Short",
                                                   "shortValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(BOOLEAN_TYPE) ||
                   lvtype.equals(BOOLEAN_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(BOOLEAN_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Boolean",
                                                   "booleanValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
                if(typeTable.get(lvtype).equals(CHARACTER_TYPE) ||
                   lvtype.equals(CHARACTER_TYPE)) {
                    newInstrs.add(ifactory.createCheckCast(CHARACTER_TYPE));
                    if(!lv3[n].isObjectType())
                        newInstrs.add
                            (ifactory.createInvoke("java/lang/Character",
                                                   "characterValue",
                                                   lvtype,
                                                   org.apache.bcel.generic.Type.NO_ARGS,
                                                   org.apache.bcel.Constants.INVOKEVIRTUAL));
                    newInstrs.add(ifactory.createStore(lvtype, prim_slot));
                }
            }
            newInstrs.add(org.apache.bcel.generic.InstructionConstants.POP);
            for(int m=0; m<newInstrs.size(); m++) {
                ilist.insert(tonextIH, (org.apache.bcel.generic.Instruction)newInstrs.get(m));
                exOffset+=((org.apache.bcel.generic.Instruction)newInstrs.get(m)).getLength();
                if(DEBUG)System.out.println("Unpacking: "+(org.apache.bcel.generic.Instruction)newInstrs.get(m)); 
            }
            // TBD: put additional condition for end of file 

            meth.mark();
            ilist.setPositions();
            meth.setInstructionList(ilist);
        }

        if(DEBUG) {
            System.out.println("\nDisplaying instruction list of baseMethod after unpacking locals ... ");
            org.apache.bcel.generic.InstructionHandle tih[] = ilist.getInstructionHandles();
            for(int w=0; w<tih.length; w++) 
                System.out.println(tih[w]);
        }

        return;
    }


    /** Performs various operations such as creating invokation for outlined method; 
     *  updating the exception table ranges of the target method, etc.
     */
    public void misc(org.apache.bcel.generic.InstructionHandle fromIH,
                     org.apache.bcel.generic.InstructionHandle toIH,
                     sandmark.program.Method outmeth, 
                     LiveVar lv1[], LiveVar lv2[], LiveVar lv3[])
    {
        if(DEBUG) System.out.println("\n In fn. misc ... \n");
        org.apache.bcel.generic.InstructionList ilist =
            meth.getInstructionList();

        int paramCnt=0;
        if(lv1!=null)
            paramCnt+=lv1.length;
        if(lv3!=null)
            paramCnt++;
        org.apache.bcel.generic.Type[] arg_types;
        if(paramCnt==0) 
            arg_types = org.apache.bcel.generic.Type.NO_ARGS;
        else
            arg_types = new org.apache.bcel.generic.Type[paramCnt];
        int cnt=0;
        if(lv1!=null)
            for(int p=0; p<lv1.length; p++)
                arg_types[cnt++] = lv1[p].getType();
        if(lv3!=null)
            arg_types[cnt++] = org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;");

        org.apache.bcel.generic.Type return_type;
        if(lv2==null)
            return_type = org.apache.bcel.generic.Type.VOID;
        else {
            switch(lv2.length) {
                case 0:
                    return_type = org.apache.bcel.generic.Type.VOID;
                    break;
                case 1:
                    return_type = lv2[0].getType();
                    break;
                default:
                    return_type = org.apache.bcel.generic.Type.getReturnType("[Ljava/lang/Object;");
            }
        }

        if(DEBUG) {
            System.out.println("invoke.return ->"+return_type.toString());
            for(int h=0; h<arg_types.length; h++)
                System.out.println("invoke.arguments ->"+arg_types[h].toString());
        }


        // insert a 'nop' instruction before 'fromIH' 
        org.apache.bcel.generic.InstructionHandle nopHandle = 
            ilist.insert(fromIH, new org.apache.bcel.generic.NOP());
        exOffset+=nopHandle.getInstruction().getLength();

        // insert the method invokation to 'outmeth'
        org.apache.bcel.generic.ConstantPoolGen cpg = 
            ((sandmark.program.Class)meth.getParent()).getConstantPool();
        org.apache.bcel.generic.InstructionFactory ifactory = 
            new org.apache.bcel.generic.InstructionFactory(cpg);
        org.apache.bcel.generic.InstructionHandle savIH;
        savIH = ilist.insert(fromIH, 
                         ifactory.createInvoke(outmeth.getClassName(),
                                               outmeth.getName(),
                                               return_type,
                                               arg_types,
                                               org.apache.bcel.Constants.INVOKESTATIC));
        exOffset+=savIH.getInstruction().getLength();

        meth.mark();
        ilist.setPositions();
        meth.setInstructionList(ilist);

        // delete the instruction range.
        for(savIH = fromIH; savIH!=toIH; savIH=savIH.getNext()) 
            exOffset -= savIH.getInstruction().getLength();
        exOffset -= savIH.getInstruction().getLength(); // for 'toIH'

        try {
            ilist.delete(fromIH, toIH);
        } catch(org.apache.bcel.generic.TargetLostException e) {
            if(DEBUG) System.out.println("TargetLostException occured; all targets should be internal");
            org.apache.bcel.generic.InstructionHandle[] targets = e.getTargets();
            if(DEBUG) System.out.println("numtargets = "+targets.length);
            for(int i=0; i < targets.length; i++) {
                org.apache.bcel.generic.InstructionTargeter[] targeters = targets[i].getTargeters();
                for(int j=0; j < targeters.length; j++)
                    targeters[j].updateTarget(targets[i], nopHandle);
            }
        }
        meth.mark();
        ilist.setPositions();
        meth.setInstructionList(ilist);

        if(DEBUG) {
            System.out.println("\nDisplaying instruction list of baseMethod after misc ops ... ");
            org.apache.bcel.generic.InstructionHandle tih[] = ilist.getInstructionHandles();
            for(int w=0; w<tih.length; w++) 
                System.out.println(tih[w]);
        }

        org.apache.bcel.generic.CodeExceptionGen [] ceg = meth.getExceptionHandlers();
        /* update the ExceptionTable ranges (if required) */
        for(int k=0; k<updateExTable.size(); k++) {
            if(((Integer)updateExType.get(k)).intValue()==ZERO_UPDATES)
                continue;
            if(((Integer)updateExType.get(k)).intValue()==REMOVE_UPDATES) {
                meth.removeExceptionHandler(ceg[k]);
                continue;
            }
            org.apache.bcel.classfile.CodeException ce = ceg[k].getCodeException(cpg);
        
            if(((Integer)updateExType.get(k)).intValue()==ALL_UPDATES) {
                ce.setStartPC(ce.getStartPC()+exOffset);
                ce.setEndPC(ce.getEndPC()+exOffset);
                ce.setHandlerPC(ce.getHandlerPC()+exOffset);
                continue;
            }
            if(((Integer)updateExType.get(k)).intValue()==ALL_UPDATES) {
                ce.setEndPC(ce.getEndPC()+exOffset);
                ce.setHandlerPC(ce.getHandlerPC()+exOffset);
                continue;
            }
            if(BUG)System.out.println("BUG: invalid updateExType ->"+
                                ((Integer)updateExType.get(k)).intValue());
        }
        
        meth.mark();
        return;
    }

    /** DEPRECATED: Identify cross-points 'm' and 'n'.
     *  'm' and 'n' should dominate/post-dominate the bottom/top of method;
     *  Also, make the cut optimal, such that live variables are minimum .
     */
    public org.apache.bcel.generic.InstructionHandle identifyCrossPoint(
                    org.apache.bcel.generic.InstructionHandle from, 
                    org.apache.bcel.generic.InstructionHandle to)
    {
        sandmark.analysis.stacksimulator.StackSimulator sim = 
            new sandmark.analysis.stacksimulator.StackSimulator(meth);
        sandmark.analysis.controlflowgraph.BasicBlock fromBB = mcfg.getBlock(from);
        sandmark.analysis.controlflowgraph.BasicBlock toBB = mcfg.getBlock(to);

        java.util.Vector ihVector = new java.util.Vector(10,1);
        if(fromBB==toBB) {
            /* Entire range is in a single block */
            
            /* Choose a point where the stackcontext is zero */
            for(org.apache.bcel.generic.InstructionHandle ih=from.getNext();
                ih!=to; ih=ih.getNext()) {
                sandmark.analysis.stacksimulator.Context context = sim.getInstructionContext(ih);
                if( (context.getStackSize()==0) && (ih.getNext()!=to) )
                    ihVector.add(ih.getNext());
            }
            int randomPick = sandmark.util.Random.getRandom().nextInt()%ihVector.size();
            return (org.apache.bcel.generic.InstructionHandle)ihVector.elementAt(randomPick);
        }

        java.util.LinkedList worklist = new java.util.LinkedList();
        sandmark.analysis.controlflowgraph.BasicBlock refBB = fromBB;
        worklist.add(fromBB);
        int ptr=0;
        while(ptr!=worklist.size()) {
            sandmark.analysis.controlflowgraph.BasicBlock tempBB =
                (sandmark.analysis.controlflowgraph.BasicBlock)worklist.get(ptr);
            /* Get all the successors and include it in the linkedlist */
            java.util.Iterator succlist = mcfg.succs(tempBB);
            while(succlist.hasNext()) {
                sandmark.analysis.controlflowgraph.BasicBlock sbb = 
                    (sandmark.analysis.controlflowgraph.BasicBlock)succlist.next();
                if(!worklist.contains(sbb) && sbb!=toBB)
                    worklist.add(sbb);
            }
            ptr++;
        }

        /* From the worklist, select all BBlocks which satisfy
         * dom/postdom conditon;  Then select the one among it which has least
         * live variables scope. And then select a random zero ## STACK CONTEXT ## ih 
         * from that BBlock
         */
        sandmark.analysis.controlflowgraph.BasicBlock selectBB = null;
        int currscopeCount = 999; // a very big number; just for initialization
        for(int k=0; k<worklist.size(); k++) {
            sandmark.analysis.controlflowgraph.BasicBlock tempBB = 
                (sandmark.analysis.controlflowgraph.BasicBlock)worklist.get(k);
            if( mcfg.dominates(fromBB,tempBB) && mcfg.postDominates(tempBB,fromBB)  &&
                mcfg.dominates(tempBB,toBB) && mcfg.postDominates(toBB,tempBB) ) {
                int scopeCount=0;
                for(int index=0; index<mcfg.method().getMaxLocals(); index++) 
                    if(mcfg.isInScope(index, tempBB))
                        scopeCount++;
                if(scopeCount<currscopeCount) {
                    /* this will be a better cut, since less number of local variables to be 
                       passed as parameter to the new function */
                    currscopeCount=scopeCount;
                    selectBB=tempBB;
                }
            }
        }
        /* Choose a point where the stackcontext is zero */
        for(org.apache.bcel.generic.InstructionHandle ih=selectBB.getIH();
            ih!=selectBB.getLastInstruction(); ih=ih.getNext()) {
            sandmark.analysis.stacksimulator.Context context = sim.getInstructionContext(ih);
            if( (context.getStackSize()==0) && (ih.getNext()!=selectBB.getLastInstruction()) )
                ihVector.add(ih.getNext());
        }
        int randomPick = sandmark.util.Random.getRandom().nextInt()%ihVector.size();
        return (org.apache.bcel.generic.InstructionHandle)ihVector.elementAt(randomPick);
    }


}

