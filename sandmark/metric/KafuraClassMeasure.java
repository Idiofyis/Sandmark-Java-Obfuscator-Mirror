package sandmark.metric;

/** This class implements the henry/kafura's coupling metrics.
 *  It basically checks the amount of information flow in and out  the
 *  methods in the class.
 *  Extends from 'ClassMetric' class
 */
public class KafuraClassMeasure extends ClassMetric
{
    private static final KafuraClassMeasure singleton =
        new KafuraClassMeasure();

    public String getName(){
        return "Kafura Measure";
    }
    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 2500000;}
    public float getStdDev(){return 160715;}

    public static KafuraClassMeasure getInstance(){
        return singleton;
    }


    protected int calculateMeasure(sandmark.program.Class myClassObj){

        int complexityMeasure = 0;
        int globalDsIn = 0;  // tbi; criteria for 'global-in'
        int globalDsOut = 0; // tbi; criteria for 'global-out'

        /* note:  these are inflows and outflows for a single class */
        int outFlows = this.getLocalOutFlows(myClassObj);
        int inFlows = this.getLocalInFlows(myClassObj);

        int classFanin = inFlows + globalDsIn;
        int classFanout = outFlows + globalDsOut;

        complexityMeasure = (int)(java.lang.Math.pow( classFanin+classFanout, 2));
        return complexityMeasure;
    }


    private int getInvokedFromWithinClass(sandmark.program.Method mg, sandmark.program.Method mgens[])
    {
        int numInvokes = 0;
        String methodName = mg.getName();
        String className = mg.getClassName();

        /* take care of recursive calls also */
        for(int i=0; i<mgens.length; i++) {
            if(mgens[i]==null)
                continue;
            org.apache.bcel.generic.InstructionList instrlist = mgens[i].getInstructionList();
            if(instrlist == null)
                return 0;

            java.util.Iterator ins = instrlist.iterator();
            // org.apache.bcel.generic.Instruction ins[] = instrlist.getInstructions();
            //if(ins == null)
            //return 0;

            while(ins.hasNext()){
                org.apache.bcel.generic.Instruction instruction =
                    ((org.apache.bcel.generic.InstructionHandle)ins.next()).getInstruction();
                //for(int m=0; m<ins.length; m++) {
                //String code = ins[m].toString();
                if(instruction instanceof org.apache.bcel.generic.InvokeInstruction) {
                    org.apache.bcel.generic.InvokeInstruction invoke =
                        (org.apache.bcel.generic.InvokeInstruction)instruction;

                    String classTarget = invoke.getClassName(mgens[i].getConstantPool());
                    if(className.equals(classTarget)){
                        String methodTarget = invoke.getMethodName(mgens[i].getConstantPool());
                        /* same class name ... now check methodname */
                        if(methodName.equals(methodTarget))
                            /* method name also matches */
                            numInvokes++;
                    }
                }
            }
        }
        return(numInvokes);
    }


    private boolean returnsData(sandmark.program.Method mg)
    {
    return !mg.getReturnType().equals(org.apache.bcel.generic.Type.VOID);
    }


    /** This method calculates the localOutFlow information ie. number of method
     *  invocation done by a method +  the number of calls made to this method ( provided
     *  this method returns some value. The summation is taken over all the methods and the
     *  result returned.
     */
    private int getLocalOutFlows(sandmark.program.Class classObj)
    {
        int numInvokedwithinClass = 0;
        int numCalls = 0;

        sandmark.program.Method methodgens[] = classObj.getMethods();

        for(int k=0; k<methodgens.length; k++)
            numCalls += StatsUtil.getApplicationCallCount(methodgens[k]);

        for(int i=0; i<methodgens.length; i++){
          /* if method does not return anything , the rest part of the loop is not required */
            if( !returnsData(methodgens[i]) )
                continue;

            numInvokedwithinClass += this.getInvokedFromWithinClass(methodgens[i], methodgens);

            int numInvokes = 0;
            sandmark.program.Class[] classes = classObj.getApplication().getClasses();

            for(int k=0; k<classes.length; k++) {
                if(classObj.equals(classes[k]))
                    continue;

                sandmark.program.Method mgs[] = classes[k].getMethods();
                if(mgs==null)
                    continue;

                org.apache.bcel.generic.ConstantPoolGen cp = classes[k].getConstantPool();

                for(int q = 0; q < mgs.length; q++) {
                    if(mgs[q]==null)
                        continue;

                    org.apache.bcel.generic.InstructionList instrlist = mgs[q].getInstructionList();
                    if(instrlist==null)
                        continue;

                    java.util.Iterator ins = instrlist.iterator();
                    if(ins==null)
                        continue;

                    while(ins.hasNext()){
                        org.apache.bcel.generic.Instruction instruction =
                            ((org.apache.bcel.generic.InstructionHandle)ins.next()).getInstruction();

                        if( instruction instanceof org.apache.bcel.generic.InvokeInstruction) {
                            org.apache.bcel.generic.InvokeInstruction invoke =
                                (org.apache.bcel.generic.InvokeInstruction)instruction;

                            String targetClass = invoke.getClassName(cp);
                            String targetMethod = invoke.getMethodName(cp);

                            if(targetClass.equals(classes[k].getName()) &&
                               targetMethod.equals(methodgens[i].getName()))
                                numInvokes++;


                        }
                    }
                }//for every method int the class
            }//for every class

            numCalls += numInvokes;
        }
        return numInvokedwithinClass+numCalls;
    }

    private int getNonVoidMethodsInvoked(sandmark.program.Method mg)
    {
        int methodCount = 0;

        if(mg == null)
            return 0;

        org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
        org.apache.bcel.generic.InstructionHandle ihs[] = null;
        if(instrlist != null)
            ihs = instrlist.getInstructionHandles();
        if(ihs == null) return 0;

        sandmark.program.Class classObj = (sandmark.program.Class)mg.getParent();

        for(int k=0; k < ihs.length; k++) {
            org.apache.bcel.generic.Instruction ins = ihs[k].getInstruction();
            if( ins instanceof org.apache.bcel.generic.InvokeInstruction) {
                org.apache.bcel.generic.InvokeInstruction invoke =
                    (org.apache.bcel.generic.InvokeInstruction)ins;

                String cname = ((org.apache.bcel.generic.InvokeInstruction)ins).getClassName
                    (classObj.getConstantPool());
                sandmark.program.Class cClass = mg.getApplication().getClass(cname);
                //make sure its not a constructor or a java library call
                if(!( ins instanceof org.apache.bcel.generic.INVOKESPECIAL) ||
                   cClass instanceof sandmark.program.LibraryClass){
                    org.apache.bcel.generic.Type returnType = invoke.getReturnType(classObj.getConstantPool());
                    if(returnType != org.apache.bcel.generic.Type.VOID)
                        methodCount++;
                }
            }
        }
        return methodCount;
    }

    /** This method calculates the localInFlow information ie. number of invocation done to
     *  each method + number of invocations ( provided the other method returns some value)
     *  The summation is taken over all the methods and the result is returned.
     */
    private int getLocalInFlows(sandmark.program.Class classObj)
    {
        int numInvokes = 0;
        int numInvokesWithReturn = 0;

        sandmark.program.Method mgens[] = classObj.getMethods();

        for(int i = 0; i < mgens.length; i++) {
            int nonVoidMethods = getNonVoidMethodsInvoked(mgens[i]);

            /* check that the methods return some value atleast; then there is 'inflow' */
            numInvokesWithReturn += nonVoidMethods;

            int numInvokesTothisMethod = 0;

            sandmark.program.Class [] allClasses = classObj.getApplication().getClasses();

            for(int k = 0; k < allClasses.length; k++) {
                sandmark.program.Class currClass = allClasses[k];
                if(currClass == classObj)
                    continue;

                sandmark.program.Method mgs[] = currClass.getMethods();
                for(int q=0; q<mgs.length; q++) {
                    org.apache.bcel.generic.InstructionList instrlist =
                        mgs[q].getInstructionList();

                    if(instrlist == null)
                        continue;

                    //org.apache.bcel.generic.Instruction ins[] = instrlist.getInstructions();
                    java.util.Iterator ins = instrlist.iterator();
                    if(ins == null)
                        continue;

                    while(ins.hasNext()){
                        org.apache.bcel.generic.Instruction instruction =
                            ((org.apache.bcel.generic.InstructionHandle)ins.next()).getInstruction();
                        if(instruction instanceof org.apache.bcel.generic.InvokeInstruction) {
                            org.apache.bcel.generic.InvokeInstruction invInstruction =
                                (org.apache.bcel.generic.InvokeInstruction)instruction;

                            String targetClass = invInstruction.getClassName(currClass.getConstantPool());
                            String targetMethod = invInstruction.getMethodName(currClass.getConstantPool());
                            if(classObj.getName().equals(targetClass) &&
                               mgens[i].getName().equals(targetMethod))
                                /* method name also matches */
                                numInvokesTothisMethod++;

                        }

                    }
                }

            }
            numInvokes+= numInvokesTothisMethod;
        }
        return  numInvokes+numInvokesWithReturn;
    }

    public boolean hasProperty()
    {
    return false;
    }
    public java.util.Vector getMetricProperties()
    {
    return null;
    }

}


