package sandmark.util.outliner;


/* 
 *  The main running class of the Method Outliner utility.
 *
 *  @author Tapas R. Sahoo (<a href="mailto:tapas@cs.arizona.edu">tapas</a>), 
 *  @version 1.0, July 23, 2003
 */
 
public class Outliner
{
    private boolean DEBUG = true;
    private boolean BUG = true;

    private org.apache.bcel.generic.InstructionHandle fromIH = null;
    private org.apache.bcel.generic.InstructionHandle toIH = null;
    private sandmark.program.Method baseMethod = null;


    /** Outline the code in between 'from' and 'to' (inclusive) 
     */
    public Outliner()
    {}

    public Outliner(org.apache.bcel.generic.InstructionHandle from, 
                    org.apache.bcel.generic.InstructionHandle to, 
                    sandmark.program.Method targetMethod)
    {
        fromIH = from;
        toIH = to;
        baseMethod = targetMethod;
    }

    public void apply(sandmark.program.Application app)
    {
        java.util.Iterator classes = app.classes();

        OutlineUtil util = new OutlineUtil(app, baseMethod);

        if( !util.isValidMethod() ) {
            System.out.println(" Cannot outline code from this method. Constraints violated!");
            System.out.println(" NO OBFUSCATION DONE...");
            throw new RuntimeException();
        }

        /*  This function verifies that the fromIH and toIH satisfy the dom/postdom criteria. */

        if(fromIH==null)
            if(DEBUG)System.out.println("fromIH is null");
        if(toIH==null)
            if(DEBUG)System.out.println("toIH is null");
        if(!util.verifyOutliningPoints(fromIH, toIH)) {
            System.out.println(" Cannot outline code from this method. dom/postdom Constraints violated!");
            System.out.println(" NO OBFUSCATION DONE...");
            throw new RuntimeException();
        }
        System.out.println("outlineable");

        sandmark.analysis.interference.InterferenceGraph igraph =
            new sandmark.analysis.interference.InterferenceGraph(baseMethod);

        /* Extract an iterator over DUWeb objects in the igraph */
        java.util.Iterator nodeIter = igraph.nodes();
        java.util.ArrayList nodelist = new java.util.ArrayList();
        while(nodeIter.hasNext())
            nodelist.add(nodeIter.next());

        /* Extract set of variables (along with their types) which  have 
         * use in this range, and last def.  before 'fromIH'.  
         */
        LiveVar lv1[] = util.getPliveVars(fromIH, toIH, nodelist);

        /* Extract set of variables (along with their types) which  have 
         * def in this range, and  atleast one use after 'toIH' and no definition 
         * before this range.  
         */
        LiveVar lv2[] = util.getQliveVars(fromIH, toIH, nodelist);

        /*  Extract set of variables (along with their types) whose live range
         *  passes the entire range.  
         */
        LiveVar lv3[] = util.getRliveVars(fromIH, toIH, nodelist);

        /*  Pass the lv1 locals to the new function as intial @params. 
         *  Pushes the arguments into the top of the stack, to be consumed 
         *  by the invokation to the new function 
         */
        util.passInRanges(fromIH, lv1);

        /*  Promote and pass the lv3 locals in a Object array to pass it to the 
         *  new function as its first @param. Unpack it in that function. 
         *  this funtion returns the arrayref index and pushes the object into 
         *  the top of the stack, to be consumed by the invokation to the 
         *  new function 
         */
        int LV3index = util.passThroughRanges(fromIH, lv3);


        /*  Create the new function with the in-range instructions and 
         *  necessary unpacking instructions(for lv1) and packing instructions
         *  (for lv2) to be returned.  For lv3, not sure as of now. Check the 
         *  routines in MethodMadness obfuscator ...
         */
        sandmark.program.Method outmeth = 
            util.createOutlineFunction(fromIH, toIH, lv1, lv2, lv3);

        /*  Unpacks the locals:
         *       -      returned by the new function at the position(LV2) and
         *       -      passed as object array to the new function(LV3)
         *  at 'lowerCutHandle' and insert appropriate instructions to get the value
         *  into primitive locals in their correct slots ...
         */
        util.unpackLocals(toIH, lv2, lv3, LV3index, outmeth);

        /*  Miscelleneous modules:
         *      1. Add method Invokation 
         *      2. Remove the code range which is outlined.
		 *      3. Update ExceptionTable of target method(if needed).
         */
        util.misc(fromIH, toIH, outmeth, lv1, lv2, lv3);
        try {
            app.save("in.jar"); //to be done in maintest  invokation 
        }catch(java.io.IOException ex){
            System.out.println(" Exception: "+ex);
            return;
        }
        if(DEBUG)System.out.println("\n Outlining done successfully  ... \n");
        return;
    }
}

class OutlineFrame {
    
    static sandmark.program.Method domMethod;
    static sandmark.program.Method postdomMethod;
    static org.apache.bcel.generic.InstructionHandle domInvoke;
    static org.apache.bcel.generic.InstructionHandle postdomInvoke;
    
    OutlineFrame(sandmark.program.Method method1, 
                 sandmark.program.Method method2, 
                 org.apache.bcel.generic.InstructionHandle invokehandle1,
                 org.apache.bcel.generic.InstructionHandle invokehandle2)
    {
        domMethod = method1;
        postdomMethod = method2;
        domInvoke = invokehandle1;
        postdomInvoke = invokehandle2;
    }

    static sandmark.analysis.controlflowgraph.BasicBlock
        getdomBB(sandmark.analysis.controlflowgraph.MethodCFG mcfg)
    {
        return mcfg.getBlock(domInvoke);
    }

    static sandmark.analysis.controlflowgraph.BasicBlock
        getpostdomBB(sandmark.analysis.controlflowgraph.MethodCFG mcfg)
    {
        return mcfg.getBlock(postdomInvoke);
    }
}


