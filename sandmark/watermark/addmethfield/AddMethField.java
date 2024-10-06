package sandmark.watermark.addmethfield;

/**
 * Authors: Ginger Myles and Miriam Miklofsky
 * Purpose: Implements algorithm 3.2 for CSc 620 Project 1
 */

public class AddMethField
   extends sandmark.watermark.StaticWatermarker {
    private static boolean DEBUG = false;

    /**
     *  Returns this watermarker's short name.
     */
    public String getShortName() {
        return "Add Method and Field";
    }

    /**
     *  Returns this watermarker's long name.
     */
    public String getLongName() {
        return "Embed a watermark in added methods and fields.";
    }

    /*
     *  Get the HTML codes of the About page.
     */
    public java.lang.String getAlgHTML(){
        return
            "<HTML><BODY>" +
            "AddMethField is a static watermarker which embeds the watermark by splitting it in half the first part becomes the name of a new field and the second becomes part of the name of a new method." +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href=\"mailto:mylesg@cs.arizona.edu\">Ginger Myles</a> and <a href=\"mailto:miriamm@cs.arizona.edu\">Miriam Miklofsky</a>\n" +
            "</TR></TD>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    public String getAuthor(){
       return "Ginger Myles and Miriam Miklofsky";
    }

    public String getAuthorEmail(){
       return "mylesg@cs.arizona.edu";
    }

    public String getDescription(){
       return "AddMethField is a static watermarker which embeds the watermark by splitting it in half the first part becomes the name of a new field and the second becomes part of the name of a new method.";
    }

    public sandmark.config.ModificationProperty[] getMutations(){
        sandmark.config.ModificationProperty[] properties = {
           sandmark.config.ModificationProperty.I_ADD_FIELDS, 
           sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
           sandmark.config.ModificationProperty.I_ADD_METHODS};
                return properties;
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited(){
       sandmark.config.RequisiteProperty[] properties = {
          sandmark.config.ModificationProperty.I_REMOVE_FIELDS,
          sandmark.config.ModificationProperty.I_REMOVE_METHODS,
          sandmark.config.ModificationProperty.I_CHANGE_METHOD_NAMES,
          sandmark.config.ModificationProperty.I_CHANGE_FIELD_NAMES};
       return properties;
   }

    /*
     *  Get the URL of the Help page
     */
    public java.lang.String getAlgURL(){
        return "sandmark/watermark/addmethfield/doc/help.html";
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
      throws sandmark.watermark.WatermarkingException {

   String watermark = params.watermark;
   String key = params.key;

   sandmark.util.Log.message(0,"Watermarking using Addmetdfield technique with key " + key);
   
   //find a valid class to watermark
   sandmark.program.Class cls = findValidClass(params.app);

   if(cls == null){
      sandmark.util.Log.message(0,new String("Cannot watermark this collection" +
      " of classes: all interfaces, all abstract, no methods other than " +
      " <init> or <clinit>, or a combination of these"));
      throw new sandmark.watermark.WatermarkingException("Cannot watermark" +
      " this collection of classes: all interfaces, all abstract, no methods" +
      " other than <init> or <clinit>, or a combination of these conditions"); 
   }

   //break the watermark into two parts
   int wmLength = watermark.length();
   String wmPart1 = watermark.substring(0, (wmLength/2));
   String wmPart2 = watermark.substring((wmLength/2), wmLength);

   //create a new field
   String newFieldName = "sm$" + wmPart1;
   int field_access_flags = org.apache.bcel.Constants.ACC_PRIVATE |
                            org.apache.bcel.Constants.ACC_STATIC;
   sandmark.program.LocalField fg = new sandmark.program.LocalField(cls,
      field_access_flags, org.apache.bcel.generic.Type.INT, newFieldName);

   //get a pseudorandom number to indicate which method will make a call
   //to the watermarked method.
   //we are going to seed the random number generator with the key
   long seed;
   if(key == null || key.equals("")){
      seed = 42;
   }else{
      java.math.BigInteger bigIntKey = sandmark.util.StringInt.encode(key);
      seed = bigIntKey.longValue();
   }
   java.util.Random generator = sandmark.util.Random.getRandom(); //new java.util.Random(seed);
   generator.setSeed(seed);

   //get the name of method where the watermarked method will be
   //inserted. We are using this name to tack on the second part
   //of the watermark.
   sandmark.program.Method chosenMethod = findMethodToWM(cls);
   if(chosenMethod == null){
      throw new sandmark.watermark.WatermarkingException("Cannot watermark" +
      " this collection of classes.");
   }

   String chosenMethodName = chosenMethod.getName();

   //create a new method
   String newMethodName = chosenMethodName + "$" + wmPart2;
   sandmark.program.LocalMethod mg = makeNewMethod(newMethodName, fg,
      cls);

   org.apache.bcel.generic.InstructionList il1 =
      chosenMethod.getInstructionList();
   org.apache.bcel.generic.InstructionHandle firstIH = il1.getStart();

   //append new instructions to the temporary list so they can be added
   //to the chosen method
   il1.insert(firstIH,new org.apache.bcel.generic.ICONST(2));
   il1.insert(firstIH,new org.apache.bcel.generic.ICONST(4));
   il1.insert(firstIH,
	      new org.apache.bcel.generic.InstructionFactory
	      (cls.getConstantPool()).createInvoke
	      (mg.getEnclosingClass().getName(),mg.getName(),
	       mg.getReturnType(),mg.getArgumentTypes(),
	       org.apache.bcel.Constants.INVOKESTATIC));

   //update the class gen so the changes to the method take hold
   chosenMethod.setMaxStack();
   chosenMethod.setMaxLocals();
   
   sandmark.util.Log.message(0,"Watermarking using "+this.getShortName()+" is done");

} //end embed()

   private sandmark.program.Class findValidClass(
      sandmark.program.Application app){

      java.util.Iterator classes = app.classes();
      while(classes.hasNext()){
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         if(isClassValid(cls))
            return cls;
      }
      return null;
   }

   private boolean isClassValid(sandmark.program.Class cls){
      if(cls.isAbstract() || cls.isInterface())
         return false;

      java.util.Iterator methods = cls.methods();
      while(methods.hasNext()){
         sandmark.program.Method m = (sandmark.program.Method)methods.next();
	 if(isValidMethod(m))
	     return true;
      }
      return false;
   }
    private boolean isValidMethod(sandmark.program.Method method) {
       if(method.getName().equals("<init>"))
	   return false;
       if(method.getName().equals("<clinit>"))
	   return false;
       if(method.getInstructionList() == null)
	   return false;
       return true;
    }


private sandmark.program.Method findMethodToWM(sandmark.program.Class cls){

   sandmark.program.Method[] methods = cls.getMethods();
   int slot = sandmark.util.Random.getRandom().nextInt() % methods.length;
   slot = (slot + methods.length) % methods.length;
   
   for(int fencepost = slot + methods.length ; slot < fencepost ; 
       slot = (slot + 1) % methods.length) {
       if(isValidMethod(methods[slot]))
	   return methods[slot];
   }
   return null;
}

private sandmark.program.LocalMethod makeNewMethod(String newMethodName, 
   sandmark.program.Field wmField, sandmark.program.Class cls){

   int method_access_flags = org.apache.bcel.Constants.ACC_PRIVATE |
                             org.apache.bcel.Constants.ACC_STATIC;
   org.apache.bcel.generic.Type return_type =
      org.apache.bcel.generic.Type.VOID;
   org.apache.bcel.generic.Type[] arg_types =
      {org.apache.bcel.generic.Type.INT,
      org.apache.bcel.generic.Type.INT};
   org.apache.bcel.generic.InstructionFactory factory = 
       new org.apache.bcel.generic.InstructionFactory(cls.getConstantPool());
   org.apache.bcel.generic.Instruction putstatic = 
       factory.createPutStatic
       (wmField.getEnclosingClass().getName(),wmField.getName(),wmField.getType());
   
   //create the instruction list
   //This new method takes the values of the two parameters, adds them
   //together and places the result in the watermarked field.
   org.apache.bcel.generic.InstructionList il =
      new org.apache.bcel.generic.InstructionList();
   il.append(org.apache.bcel.generic.InstructionConstants.ILOAD_0);
   il.append(org.apache.bcel.generic.InstructionConstants.ILOAD_1);
   il.append(org.apache.bcel.generic.InstructionConstants.IADD);
   il.append(org.apache.bcel.generic.InstructionConstants.ISTORE_0);
   il.append(org.apache.bcel.generic.InstructionConstants.ILOAD_0);
   il.append(putstatic);
   il.append(new org.apache.bcel.generic.RETURN());

   sandmark.program.LocalMethod mg =
      new sandmark.program.LocalMethod(cls, method_access_flags,
      return_type, arg_types, null, newMethodName, il);
   mg.setMaxStack();
   mg.setMaxLocals();

   return mg;
}

/*************************************************************************/
/*                              Recognition                              */
/*************************************************************************/

/* An iterator which generates the watermarks
 * found in the program.
 */
class Recognizer implements java.util.Iterator {
    java.util.Vector result = new java.util.Vector();
    int current = 0;

    public Recognizer(sandmark.watermark.StaticRecognizeParameters params) {
       generate(params);
    }

   public void generate(sandmark.watermark.StaticRecognizeParameters params) {

      java.util.ArrayList methodSuffices = new java.util.ArrayList();
      java.util.ArrayList fieldSuffices = new java.util.ArrayList();

      for(java.util.Iterator classes = params.app.classes() ; classes.hasNext() ; ) {
	  sandmark.program.Class cls = (sandmark.program.Class)classes.next();
	  for(java.util.Iterator methods = cls.methods() ; methods.hasNext() ; ) {
	      sandmark.program.Method method = 
		  (sandmark.program.Method)methods.next();
	      if(DEBUG)
		  System.out.println(method);
	      String name = method.getName();
	      int dollarIndex = -1;
	      while((dollarIndex = name.indexOf('$',dollarIndex + 1)) != -1)
		  methodSuffices.add(name.substring(dollarIndex + 1));
	  }
	  for(java.util.Iterator fields = cls.fields() ; fields.hasNext() ; ) {
	      sandmark.program.Field field = (sandmark.program.Field)fields.next();
	      if(DEBUG)
		  System.out.println(field);
	      String name = field.getName();
	      int dollarIndex = -1;
	      while((dollarIndex = name.indexOf('$',dollarIndex + 1)) != -1)
		  fieldSuffices.add(name.substring(dollarIndex + 1));
	  }
      }

      if(DEBUG) {
	  System.out.println(methodSuffices);
	  System.out.println(fieldSuffices);
      }

      for(java.util.Iterator ms = methodSuffices.iterator() ; ms.hasNext() ; ) {
	  String m = (String)ms.next();
	  for(java.util.Iterator fs = fieldSuffices.iterator() ; fs.hasNext() ; ) {
	      String f = (String)fs.next();
	      result.add(f + m);
	  }
      }
   }         

    public boolean hasNext() {
       return current < result.size();
    }

    public java.lang.Object next() {
       return result.get(current++);
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


} // class AddMethField

