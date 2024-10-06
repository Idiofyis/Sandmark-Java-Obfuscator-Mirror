package sandmark.watermark.bogusswitch;


public class Bogus_Switch  
   extends sandmark.watermark.StaticWatermarker {    
    
    /**
     *  Returns this watermarker's short name.
     */
    public String getShortName() {
	return "Add Switch";
    }
    
    /**
     *  Returns this watermarker's long name.
     */
    public String getLongName() {
	return "Embed a watermark in a bogus switch statement.";
    }

    /*
     *  Get the HTML codes of the About page.
     */
    public java.lang.String getAlgHTML(){
	return 
           "<HTML><BODY>\n" +
           "BOGUS_SWITCH is a static watermarking algorithm which" +
           " embeds the watermark in the labels of a bogus switch statement.\n" +
           "<table>\n" +
	    "<TR><TD>\n" +
	    "   Authors: <a href=\"mailto:ash@cs.arizona.edu\">Andy</a> and <a href=\"mailto:xyzhang@cs.arizona.edu\">Xiangyu</a>\n" +
	    "</TR></TD>\n" +
           "</table>\n" +
           "</BODY></HTML>\n";
    }

    /*
     *  Get the URL of the Help page
     */
    public java.lang.String getAlgURL(){
	return "sandmark/watermark/bogusswitch/doc/help.html";
    }

    public String getDescription() {
	return "Embeds a watermark in the labels of a switch statement";
    }

    public String[] getReferences() {
	return new String[] {};
    }

    public sandmark.config.ModificationProperty[] getMutations() {
       sandmark.config.ModificationProperty[] properties = {
          sandmark.config.ModificationProperty.I_ADD_METHOD_CODE};
       return properties;
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited(){
       sandmark.config.RequisiteProperty[] properties = {
          sandmark.config.ModificationProperty.I_MODIFY_METHOD_CODE,
          sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
          sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE};
       return properties;
    }

    public String getAuthor() {
	return "Andrew Huntwork and Xiangyu Zhang";
    }

    public String getAuthorEmail() {
	return "{ash,xyzhang}@cs.arizona.edu";
    }

/*************************************************************************/
/*                               Embedding                               */
/*************************************************************************/

 
public void embed(sandmark.watermark.StaticEmbedParameters params)
    throws sandmark.watermark.WatermarkingException {
   String watermark = params.watermark;
   boolean didWM = false;

   for(java.util.Iterator classes = params.app.classes(); !didWM && classes.hasNext() ; ) {
       sandmark.program.Class clazz = (sandmark.program.Class)classes.next();

       for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
           sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
           org.apache.bcel.generic.InstructionList il =
               method.getInstructionList();

           if(il == null)
               continue;

           int localNdx = method.getMaxLocals();
           int matches[] = new int[watermark.length()];
           org.apache.bcel.generic.InstructionHandle cases[] =
               new org.apache.bcel.generic.InstructionHandle[watermark.length()];
           org.apache.bcel.generic.InstructionHandle default_case = il.getStart();

           for (int j = 0 ; j < watermark.length() ; j++) {
               il.insert(new org.apache.bcel.generic.GOTO(default_case));
               il.insert(new org.apache.bcel.generic.ISTORE(localNdx));
               il.insert(new org.apache.bcel.generic.IADD());
               il.insert(new org.apache.bcel.generic.ILOAD(localNdx));
               cases[j] = il.insert(new org.apache.bcel.generic.ICONST(1));
               matches[j] = (j << 16) | watermark.charAt(j);   
           }

           org.apache.bcel.generic.SWITCH bg_switch =
               new org.apache.bcel.generic.SWITCH(matches,cases,default_case,0);
           il.insert(bg_switch);
           il.insert(new org.apache.bcel.generic.ILOAD(localNdx));
           il.insert(new org.apache.bcel.generic.ISTORE(localNdx));
           il.insert(org.apache.bcel.generic.InstructionConstants.ICONST_0);

           method.mark();

           method.setMaxStack();
           method.setMaxLocals();
           didWM = true;
           break;
       }
   }

   if(!didWM)
       throw new sandmark.watermark.WatermarkingException
           ("No methods or all abstract methods");
}

/*************************************************************************/
/*                              Recognition                              */
/*************************************************************************/

/* An iterator which generates the watermarks
 * found in the program.
 */
class Recognizer implements java.util.Iterator {
    java.util.Vector result = new java.util.Vector();
    int curndx=0;

    public Recognizer(sandmark.watermark.StaticRecognizeParameters params) {
       generate(params);
    }

    public void generate(sandmark.watermark.StaticRecognizeParameters params) {
        sandmark.program.Application app = params.app;	
        for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
            sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
            
            for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
                sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
                if(method.getInstructionList() == null)
                    continue;
                org.apache.bcel.generic.Instruction[] instrs =
                    method.getInstructionList().getInstructions();
                for(int  j = 0 ; j < instrs.length ; j++) {
                    String wm = tryGetWM(instrs[j]);
                    if(wm != null)
                        result.add(wm);
                }
            }
        }
    }

    private String tryGetWM(org.apache.bcel.generic.Instruction instr) {
        int matches[] = (instr instanceof org.apache.bcel.generic.LOOKUPSWITCH) ? 
            ((org.apache.bcel.generic.LOOKUPSWITCH)instr).getMatchs() :
            ((instr instanceof org.apache.bcel.generic.TABLESWITCH) ?
             ((org.apache.bcel.generic.TABLESWITCH)instr).getMatchs() :
             null);

        if(matches == null)
            return null;

        String wmark = "";

        for(int i = 0 ; i < matches.length ; i++) {
            if((matches[i] >> 16) != i)
                return null;
            wmark += (char)matches[i];
        }

        return wmark;
    }

    public boolean hasNext() {
       return curndx<result.size();
    }

    public java.lang.Object next() {
       return result.elementAt(curndx++);
    }

    public void remove() {}
}


/* Return an iterator which generates the watermarks
 * found in the program. The props argument
 * holds at least the following properties:
 *  <UL>
 *     <LI> Input File: The name of the file to be watermarked.
 *  </UL>
 */
public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
      throws sandmark.watermark.WatermarkingException {
   return new Recognizer(params);
}


} // class MyAlgorithm

