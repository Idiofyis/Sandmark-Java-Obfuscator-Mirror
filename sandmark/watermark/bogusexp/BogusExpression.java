package sandmark.watermark.bogusexp;

// RE-IMPLEMENTED USING SANDMARK.PROGRAM.OBJECTS ... 

/*
 * This algorithm adds an 'expression' watermark to the 
 * constant pool of a random class. The expression value 
 * is assigned to a variable with 'sm$' prefix. The 
 * recognizer looks for the particular local index and 
 * corresponding instructions in the constant pool to 
 * extract the watermark .
 */

public class BogusExpression extends sandmark.watermark.StaticWatermarker 
{
    boolean DEBUG = false;    
    
    /*
     *  Returns this watermarker's short name.
     */
    public String getShortName() {
        return "Add Expression";
    }
    
    /*
     *  Returns this watermarker's long name.
     */
    public String getLongName() {
        return "Embed a watermark in a Bogus Expression";
    }

    /*
     *  Get the HTML codes of the About page.
     */
    public java.lang.String getAlgHTML(){
        return 
           "<HTML><BODY>\n" +
           " BogusExpression is a watermarking algorithm which " +
           " embeds a static watermark as a bogus expression assignment" +
           " to a new local variable.\n" +
           "<table>\n" +
           "<TR><TD>\n" +
           "Author: <a href=\"mailto:tapas@cs.arizona.edu\">Tapas R. Sahoo</a> and <a href=\"mailto:balamc@cs.arizona.edu\">Balamurgan Chirstabesan</a>\n" +
            "</TR></TD>\n" +
           "</table>\n" +
           "</BODY></HTML>\n";
    }

    /*
     *  Get the URL of the Help page
     */
    public java.lang.String getAlgURL(){
        return "sandmark/watermark/bogusexp/doc/help.html";
    }

    /*
     *  Specifies the author of this algorithm.
     */
    public java.lang.String getAuthor()
    {
        return "Balamurugan Chirtsabesan and Tapas Sahoo";
    }

    /*
     *  Specifies the author's email address.
     */
    public java.lang.String getAuthorEmail()
    {
        return "balamc@cs.arizona.edu and tapas@cs.arizona.edu";
    }

    /*
     *  Specifies what this algorithm does.
     */
    public java.lang.String getDescription()
    {
        return "This algorithm embeds a static watermark " +
               "as a bogus expression that is assigned to " +
               "a new local variable. The watermark is recognized " +
               "with the help of the local variable name";
    }

    public sandmark.config.ModificationProperty[] getMutations()
    {
        sandmark.config.ModificationProperty[] properties = {
           sandmark.config.ModificationProperty.I_ADD_LOCAL_VARIABLES,
           sandmark.config.ModificationProperty.I_ADD_METHOD_CODE};
        return properties;
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited()
    {
       sandmark.config.RequisiteProperty[] properties = {
          sandmark.config.ModificationProperty.I_CHANGE_LOCAL_VARIABLES,
          sandmark.config.ModificationProperty.I_CHANGE_CONSTANT_POOL};
       return properties;
    }

    public sandmark.config.RequisiteProperty[] getPostrequisities()
    {
       return null;
    }

    public sandmark.config.RequisiteProperty[] getPostsuggestions()
    {
       return null;
    }

    public sandmark.config.RequisiteProperty[] getPreprohibited()
    {
       return null;
    }

    public sandmark.config.RequisiteProperty[] getPrerequisities()
    {
       return null;
    }

    public sandmark.config.RequisiteProperty[] getPresuggestions()
    {
       return null;
    }

    public java.lang.String[] getReferences()
    {
       return null;
    }


    /*************************************************************************/
    /*                               Embedding                               */
    /*************************************************************************/

    /* Embed a watermark value into the program. The props argument
     * holds at least the following properties:
     *  <UL>
     *     <LI> Watermark: The watermark value to be embedded.
     *  </UL>
     */
    public void embed(sandmark.watermark.StaticEmbedParameters params)  
            throws sandmark.watermark.WatermarkingException 
    {
       String watermark = params.watermark;
    
       int wMark=0, wMark1, wMark2;
       try {
           wMark = Integer.parseInt(watermark);
       } catch(NumberFormatException nfe) {
           sandmark.util.Log.message(0, "Cannot embed Strings and long numbers...");
           return;
       }
       java.util.Random rdm  = sandmark.util.Random.getRandom(); //new java.util.Random();
       int xyz = rdm.nextInt();
       if(wMark!=0)
           wMark1=xyz%wMark;
       else
           wMark1=xyz%(wMark+1);
       wMark2=wMark-wMark1;
    
       java.util.Iterator classes = params.app.classes();
       if (!classes.hasNext())
          throw new sandmark.watermark.WatermarkingException("There must be at least one class to watermark!");
    
       sandmark.program.Class classObj = null;
       sandmark.program.Method mObj = null;
       
       while (classes.hasNext()) {
          classObj = (sandmark.program.Class)classes.next();
          sandmark.program.Method methods[] = classObj.getMethods();
          if(methods==null)
              continue;
          if(methods.length==0) // Incase of illegal interfaces 
              continue;
          else  {
              if(DEBUG) 
                System.out.println(" class = " + classObj.getName() + " num methods = " + methods.length);
          }

    
          int insertMethod = 0; // (default: insert it in first method)
          org.apache.bcel.generic.InstructionList iList = null;
          org.apache.bcel.generic.InstructionHandle[] iHandle = null;
          int numInstr = 0;
          int iterCount = 0;
          int nextClassFlag = 0;
    
          do{
             if(iterCount++>20) {
                 nextClassFlag=1;
                 break;
             }
             // take a random method for the class and add the watermarking expression in it...
             java.util.Random rd1 = sandmark.util.Random.getRandom(); //new java.util.Random();
             insertMethod = (rd1.nextInt()%(methods.length));
             if (insertMethod < 0)
                insertMethod = 0-insertMethod;
    
             mObj = methods[insertMethod];
             
             if((iList=mObj.getInstructionList())==null)
                 continue;
             if((iHandle=iList.getInstructionHandles())==null)
                 continue;
             numInstr = iHandle.length;
    
          }while(numInstr<=2);
       
          if (nextClassFlag == 1)
             continue;
    
          org.apache.bcel.generic.ConstantPoolGen cpg = classObj.getConstantPool();
    
          // choose a random place (between 1 and 'numInstr') to insert the instruction...
          java.util.Random rd = sandmark.util.Random.getRandom(); //new java.util.Random();
          int insertPoint = 0; // (default: insert it at start of method code)
       
          // add local variable & assign watermarking expression...
          org.apache.bcel.generic.LocalVariableGen lg = 
             mObj.addLocalVariable("sm$1", org.apache.bcel.generic.Type.INT, null, null);
          int localindex = lg.getIndex();
          org.apache.bcel.generic.Instruction store = 
             new org.apache.bcel.generic.ISTORE(localindex);
    
          while(true) {
             insertPoint = (rd.nextInt()%(numInstr));
             if(insertPoint < 0)
                insertPoint = 0-insertPoint;
             if(insertPoint < 2)
                insertPoint = 2;
    
             org.apache.bcel.generic.InstructionHandle ihInsert = iHandle[insertPoint];
    
             iList.insert(ihInsert, new org.apache.bcel.generic.PUSH(cpg, wMark1));
             iList.insert(ihInsert, new org.apache.bcel.generic.PUSH(cpg, wMark2));
             iList.insert(ihInsert, org.apache.bcel.generic.InstructionConstants.IADD);
             iList.insert(ihInsert, store);
             break;
          }
    
          if(DEBUG) System.out.println("DONE with embedding 'expr' watermark...");
          // mObj.setMaxStack();
	  mObj.setInstructionList(iList);
          mObj.mark();
          return;
       }
    }

/*************************************************************************/
/*                              Recognition                              */
/*************************************************************************/

/* An iterator which generates the watermarks found in the program.
 */
class Recognizer implements java.util.Iterator
{
    java.util.Vector result = new java.util.Vector();
    int current = 0;

    public Recognizer(sandmark.watermark.StaticRecognizeParameters params) 
    {
        generate(params);
    }

    public void generate(sandmark.watermark.StaticRecognizeParameters params)
    {

        java.util.Iterator classes = params.app.classes();
        while (classes.hasNext()) {
            sandmark.program.Class classObj = (sandmark.program.Class)classes.next();
            org.apache.bcel.generic.ConstantPoolGen cpg = classObj.getConstantPool();

            sandmark.program.Method[] methods = classObj.getMethods();
            if(methods==null)
                continue;
            sandmark.program.Method mObj = null;

	    org.apache.bcel.classfile.ConstantPool cp = cpg.getConstantPool();


            for(int i=0; i<methods.length ; i++){
                mObj = methods[i];
                org.apache.bcel.generic.LocalVariableGen locals[] = mObj.getLocalVariables();
                if(locals==null)
                    continue;

                for(int j=0; j<locals.length; j++){
                    String localName = locals[j].getName();
                
                    if( localName.startsWith("sm$")){
                        // code to extract the watermark follows
                        int lIndex = locals[j].getIndex();
                        org.apache.bcel.generic.InstructionList iList = mObj.getInstructionList();
                        if(iList==null)
                            continue;
                        org.apache.bcel.generic.InstructionHandle[] iHandle = 
                            iList.getInstructionHandles();
                        if(iHandle==null)
                            continue;

                        for(int k=0; k < iHandle.length; k++) {
                            org.apache.bcel.generic.InstructionHandle ih = iHandle[k];
                            org.apache.bcel.generic.Instruction instr = ih.getInstruction();

                            if(instr instanceof org.apache.bcel.generic.ISTORE){
                                String stringVal = instr.toString(cp); // NOTE:'cp' removed: change parsing
                                if(DEBUG) System.out.println("stringVal = "+stringVal);
                                stringVal = stringVal.substring(7,stringVal.length());
                                int localIndex = Integer.parseInt(stringVal);
                                if(localIndex == lIndex){
                                    org.apache.bcel.generic.InstructionHandle ih_prev1 = iHandle[k-1];
                                    org.apache.bcel.generic.Instruction instr_prev1 = 
                                        ih_prev1.getInstruction();
                                    stringVal = instr_prev1.toString(cp); //NOTE:'cp' removed

                                    int negFlag=0;
                                    org.apache.bcel.generic.InstructionHandle ih_prev2 = iHandle[k-2];
                                    org.apache.bcel.generic.Instruction instr_prev2 = ih_prev2.getInstruction();
                                    stringVal = instr_prev2.toString(cp); // NOTE:'cp' removed
                                    int Idx = stringVal.indexOf(' ');

                                    if(Idx==-1){
                                        Idx = stringVal.indexOf('_');
                                        if(stringVal.charAt(Idx+1)=='m'){
                                            negFlag=1;
                                            Idx++;
                                        }
                                    }
                                    stringVal = stringVal.substring(Idx+1,stringVal.length());
                                    if(negFlag==1){
                                        negFlag=0;
                                        stringVal = "-".concat(stringVal);
                                    }
                                    int val1 = Integer.parseInt(stringVal);
                                    org.apache.bcel.generic.InstructionHandle ih_prev3 = iHandle[k-3];
                                    org.apache.bcel.generic.Instruction instr_prev3 = ih_prev3.getInstruction();
                                    stringVal = instr_prev3.toString(cp); // NOTE:'cp' removed
                                    Idx = stringVal.indexOf(' ');

                                    if(Idx==-1){
                                        Idx = stringVal.indexOf('_');
                                        if(stringVal.charAt(Idx+1)=='m') {
                                            negFlag=1;
                                            Idx++;
                                        }
                                    }
                                    stringVal = stringVal.substring(Idx+1,stringVal.length());
                                    if(negFlag==1)
                                        stringVal = "-".concat(stringVal);

                                    int val2 = Integer.parseInt(stringVal);
                                    int wmark = val1 + val2;
                                    result.add(Integer.toString(wmark));
                                }// if localIndex matches
                            }// if instr ISTORE
                        }// 'for' check each instr
                    }// if 'sm$'
                }// iterate locals 
            }// iterate methods
        }// iterate classes 
    }

    public boolean hasNext() {
        return current < result.size();
    }

    public java.lang.Object next() {
        return result.get(current++);
    }

    public void remove()
    {}
}

    /* Return an iterator which generates the watermarks
     * found in the program. The props argument
     * holds at least the following properties:
     *  <UL>
     *     <LI> Input File: The name of the file to be watermarked.
     *  </UL>
     */
    public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
            throws sandmark.watermark.WatermarkingException
    {
        return new Recognizer(params);
    }
}

