
/* Justin's simple obfuscation technique...
 * Much of the original code was borrowed and modified from VariableReassigner
 * or Method2RMadness
 */

package sandmark.obfuscate.varsplitter;

/* This is the VarSplitter class which splits a local variable so that
 * all assignments of value go to a new location (in addition) and some of the
 * references to the value of the variable are changed to the new location.
 * @author Justin Cappos (<a href="mailto:justin@cs.arizona.edu">justin@cs.arizona.edu</a>)
 * @version 1.0, August 1st, 2002
 */

public class VarSplitter extends sandmark.obfuscate.MethodObfuscator
{
    /* This method splits all usage of old_slot between usage of old_slot and
     * new_slot   All store and inc statements are done on each slot in order to
     * keep their contents synchronized in the order of new_slot, old_slot (this
     * avoids some nasty race conditions...)
     */

    private static boolean DEBUG = false;

    private void split_vars(org.apache.bcel.generic.InstructionList instlist,
                            int old_slot, int new_slot)
    {
        org.apache.bcel.generic.InstructionHandle curih;
        for(curih=instlist.getStart(); curih!=instlist.getEnd(); curih=curih.getNext()){

            // Is this a load instruction for the old_slot?
            if((curih.getInstruction() instanceof org.apache.bcel.generic.LoadInstruction)&&
              (((org.apache.bcel.generic.LocalVariableInstruction)
              curih.getInstruction()).getIndex()==old_slot)){
                if(Math.random()<.5){
                    ((org.apache.bcel.generic.LocalVariableInstruction)
                    curih.getInstruction()).setIndex(new_slot);
                }
            }

            // How about a store instruction for the old_slot?
            if((curih.getInstruction() instanceof org.apache.bcel.generic.StoreInstruction)&&
              (((org.apache.bcel.generic.LocalVariableInstruction)
              curih.getInstruction()).getIndex()==old_slot)){

                // Append the original instruction to itself...
                instlist.append(curih,curih.getInstruction().copy());
                ((org.apache.bcel.generic.LocalVariableInstruction)
                curih.getInstruction()).setIndex(new_slot);
                // Append xSTORE new_slot to the original instruction...
                instlist.append(curih,curih.getInstruction().copy());
                curih.setInstruction(new org.apache.bcel.generic.DUP());
                // Move the curih location past the new store inst...
                curih=curih.getNext().getNext();
            }
            // Am I an INC for the old_slot?
            if((curih.getInstruction() instanceof org.apache.bcel.generic.IINC)&&
              (((org.apache.bcel.generic.LocalVariableInstruction)
              curih.getInstruction()).getIndex()==old_slot)){

                // Append the instruction to itself to the original instruction...
                instlist.append(curih,curih.getInstruction().copy());
                ((org.apache.bcel.generic.LocalVariableInstruction)
                curih.getInstruction()).setIndex(new_slot);
                // Move the curih location past the inc insts...
                curih=curih.getNext();
            }
        }
        instlist.setPositions();
    }



    /*
     *  Performs the actual modification of the requested method...
     */

    public void apply(sandmark.program.Method methodObj) throws Exception
    {
        int i, j, slot, memloc, argsize, newloc;
        String classname = methodObj.getClassName();
        String methname = methodObj.getName();
        String signat = methodObj.getSignature();

        sandmark.program.Class classObj = (sandmark.program.Class)methodObj.getParent();
        sandmark.program.Method[] methods = classObj.getMethods();
        sandmark.program.Method mObj = null;
        org.apache.bcel.generic.ConstantPoolGen cpg = classObj.getConstantPool();

        slot=0;

        // Find the method we are going to modify
        if(methods!=null)
            for(i=0; i<methods.length; i++)
                if((methods[i].getName().equals(methname))&&(methods[i].getSignature().equals(signat))){
                    // This should NEVER happen, but just in case the file is corrupt
                    if(mObj!= null)
                        throw new java.io.IOException("Duplicate methods with identical signatures found."+
                                                       "\n"+classname+"."+methname+signat);
                    mObj = methods[i];
                    slot=i;
                }

        // Could I find the method?
        if(mObj==null)
             throw new IllegalArgumentException("No such method " +
                 classname + "." + methname + signat + " to obfuscate.");

        if((mObj.isInterface()||mObj.isAbstract())){
            // Nothing to do! (that was easy)
            return;
        }

        org.apache.bcel.generic.InstructionList il= mObj.getInstructionList();
        if(il==null)
            return;
        org.apache.bcel.generic.LocalVariableGen[] localvargens= mObj.getLocalVariables();
        if(localvargens==null)
            return;
        //System.out.println(il);
        org.apache.bcel.generic.InstructionHandle[] ihs=il.getInstructionHandles();
        if(ihs==null)
            return;

        int thissize;
        argsize=1;
        // Find the argument size...
        for(i=0; i<localvargens.length; i++){
            thissize = localvargens[i].getIndex();
            thissize += localvargens[i].getType().getSize();
            if(thissize>argsize){
                argsize = thissize;
            }
        }
        if(DEBUG)System.out.println(" argsize = "+argsize);

        int local_slots[] = new int[ihs.length*4+localvargens.length*2];
        int local_inst[] = new int[ihs.length];
        newloc=0;
        // The local variables are the arguments of the method; I want to avoid using those slots.
        for(j=0; j<ihs.length; j++){
            // If this is a store/load/inc instruction... that doesn't refer to the arguments...
            if((ihs[j].getInstruction() instanceof org.apache.bcel.generic.StoreInstruction)&&
              (((org.apache.bcel.generic.LocalVariableInstruction)
              ihs[j].getInstruction()).getIndex()>argsize)){ //>=
                int index = ((org.apache.bcel.generic.LocalVariableInstruction)
                            ihs[j].getInstruction()).getIndex();
                if(DEBUG)System.out.println(" instruction index = "+index);
                if(((ihs[j].getInstruction() instanceof org.apache.bcel.generic.ASTORE))||
                  ((ihs[j].getInstruction() instanceof org.apache.bcel.generic.DSTORE))||
                  ((ihs[j].getInstruction() instanceof org.apache.bcel.generic.LSTORE))){
                    // Don't use this one...
                    local_slots[index]=2;
                }
                else{
                    // put this one in the good list (it seems to work)...
                    // This may be changed later...
                    if(DEBUG)System.out.println(" recording  instruction index -> "+index);
                    if(local_slots[index]==0){
                        // should be > than paramter list index.

                        local_slots[index]=1;
                        local_inst[index]=j;
                    }
                }
            }
        }

        // randomly choosing a localvariable to use...
        for(i=0,memloc=0; i<local_slots.length-1; i++)
            if(local_slots[i]==1){
                local_slots[memloc++] = i;
                local_inst[memloc] = local_inst[i];
            }

        if(memloc==0){
            // Nothing to work with :(
            return;
        }

        // This nasty statement picks a memory location of a local variable that fits our criteria
        memloc = local_slots[((int)(((int)(Math.random()*memloc))/memloc))];

        mObj.setMaxLocals();
        newloc = mObj.getMaxLocals();
        if(DEBUG)System.out.println(" newloc = "+newloc+" memloc(old) = "+memloc);

        /* Ok, now memloc is assigned the old slot location and newloc is
           assigned a new available slot location of the same size */

        // calling split_vars(...) to do the real work...
        split_vars(il, memloc, newloc);

        // All done!   Put it back together...
        mObj.setInstructionList(il);
        mObj.setMaxStack();
        methods[slot] = mObj;
        methods[slot].mark();
    }


    /* This is just a test routine... */
    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.out.println("Usage: VarSplitter <JAR FILE>.jar");
            System.exit(1);
        }

        sandmark.program.Application app = new sandmark.program.Application(args[0]);
        sandmark.obfuscate.appendboguscode.AppendBogusCode obfuscator =
            new sandmark.obfuscate.appendboguscode.AppendBogusCode();
        java.util.Iterator itr = app.classes();
        while(itr.hasNext()){
            sandmark.program.Class classObj = (sandmark.program.Class)itr.next();
            sandmark.program.Method[] methods = classObj.getMethods();
            if(methods!=null){
                for(int m=0;  m<methods.length; m++)
                    obfuscator.apply(methods[m]);
            }
        }
    }

    /* Returns the URL at which you can find information about this obfuscator. */
    public java.lang.String getAlgURL() {
        return "sandmark/obfuscate/varsplitter/doc/help.html";
    }


    /* Returns an HTML description of this obfuscator. */
    public java.lang.String getAlgHTML() {
        return
            "<HTML><BODY>" +
            "VarSplitter takes a local variable in a method and creates a" +
            "companion variable which all store instructions also update\n" +
            "and which some of the load instructions now reference\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href =\"mailto:justin@cs.arizona.edu\">Justin Cappos</a> " +
            "</TR></TD>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }


    /* Returns a long description of this obfuscator's name. */
    public java.lang.String getLongName(){
        return
            "VarSplitter: takes a local variable in a method and creates a" +
            "companion variable which all store instructions also update\n" +
            "and which some of the load instructions now reference\n";
    }


    /* Returns a short description of this obfuscator's name. */
    public java.lang.String getShortName(){
        return "Duplicate Registers";
    }

    public java.lang.String getAuthor(){
        return "Justin Cappos";
    }

    public java.lang.String getAuthorEmail(){
        return "justin@cs.arizona.edu";
    }

    public java.lang.String getDescription(){
        return "Takes a local variable in a method and splits references to "+
               "it with a new variable (which stays synchronized).";
    }

    public sandmark.config.ModificationProperty[] getMutations(){
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_ADD_LOCAL_VARIABLES,
            sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
            sandmark.config.ModificationProperty.PERFORMANCE_DEGRADE_LOW
        };
    }

    public sandmark.config.RequisiteProperty[] getPresuggestions()
    {
        return new sandmark.config.RequisiteProperty[]{
            sandmark.config.ModificationProperty.I_REORDER_INSTRUCTIONS
        };
    }

    public sandmark.config.RequisiteProperty[] getPostsuggestions()
    {
        return new sandmark.config.RequisiteProperty[]{
            sandmark.config.ModificationProperty.I_REORDER_INSTRUCTIONS,
            sandmark.config.ModificationProperty.I_CHANGE_LOCAL_VARIABLES
        };
    }

    public sandmark.config.RequisiteProperty[] getPreprohibited()
    {
        return new sandmark.config.RequisiteProperty[]{
            sandmark.config.ModificationProperty.I_CHANGE_LOCAL_VARIABLES
        };
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited()
    {
        return new sandmark.config.RequisiteProperty[]{
            new sandmark.config.AlgorithmProperty(this)
        };
    }
}


