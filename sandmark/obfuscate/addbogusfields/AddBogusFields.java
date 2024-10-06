package sandmark.obfuscate.addbogusfields;

/**
 * The AddBogusFields obfuscator changes adds a bogus field to each class
   in an application and throughout the class makes assignments to the
   field.
        @author         Ginger Myles and Miriam Miklofsky
 */

public class AddBogusFields
   extends sandmark.obfuscate.ClassObfuscator {

    /**
     *  Constructor.
     */
   public AddBogusFields(){}

   public String getShortName() {
           return "Field Assignment";
   }

   public String getLongName() {
           return "Insert a bogus field and make assignments to this field";
   }

   public java.lang.String getAlgHTML(){
           return
            "<HTML><BODY>" +
            "The AddBogusFields obfuscator adds a bogus field to each class " +
            "in an application and throughout the class makes assignments to the field. " +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href=\"mailto:mylesg@cs.arizona.edu\">Ginger Myles</a> and <a href=\"mailto:miriamm@cs.arizona.edu\">Miriam Miklofsky</a>\n" +
            "</TD></TR>" +
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
      return "The AddBogusFields obfuscator adds a bogus field to each class in an application and throughout the class makes assignments to the field. ";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
       sandmark.config.ModificationProperty[] properties = {
           sandmark.config.ModificationProperty.I_ADD_FIELDS,
           sandmark.config.ModificationProperty.I_ADD_METHOD_CODE};
       return properties;
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/addbogusfields/doc/help.html";
   }

/*************************************************************************/
/*                               Embedding                               */
/*************************************************************************/

   public void apply(
      sandmark.program.Class cls)
      throws Exception {

      String newFieldName;
      int i;
      java.util.GregorianCalendar gc =
          new java.util.GregorianCalendar();
      java.util.Random generator =
          new java.util.Random(gc.getTime().getTime());

         if(!cls.isAbstract() && !cls.isInterface()){
            i = Math.abs(generator.nextInt());
            newFieldName = "sm$" + i;

            String className = cls.getName();


            if(!cls.isAbstract() || !cls.isInterface()){
               org.apache.bcel.generic.ConstantPoolGen cpg =
               cls.getConstantPool();

               sandmark.program.Field[] fields = cls.getFields();

               if(fields.length > 0){
                  //choose a random field
                  int fieldIndex = Math.abs(generator.nextInt()) %
                     fields.length;

                  //get info about chosen field
                  sandmark.program.Field fgen = fields[fieldIndex];
                  String fieldSig = fgen.getSignature();

                  //create the bogus field
                  int field_access_flags = org.apache.bcel.Constants.ACC_STATIC;
                  sandmark.program.LocalField fg = new sandmark.program.LocalField(cls,
                  field_access_flags, org.apache.bcel.generic.Type.INT, newFieldName); 
                  int fieldNameIndex = cpg.addFieldref(className, newFieldName,
                     fg.getSignature());

                  //get the methods 
                  sandmark.program.Method[] methods = cls.getMethods();
                  sandmark.program.Method mgen = null;

                  //for each method scan the instruction list for the chosen field
                  for(int j = 0; j < methods.length; j++){
                     mgen = methods[j];
         
                     //get the instruction list for the method
                     org.apache.bcel.generic.InstructionList il =
                        mgen.getInstructionList();

                     org.apache.bcel.generic.InstructionHandle ihs[] = null;
                     if(il != null){
                        ihs = il.getInstructionHandles();
 
                        //construct instruction list to insert
                        org.apache.bcel.generic.InstructionList insertList =
                           new org.apache.bcel.generic.InstructionList();

                        //now scan the instruction handles
                        for(int k=0; k<ihs.length; k++){
                           org.apache.bcel.generic.InstructionHandle ih = ihs[k];

                           if(ih.getInstruction() 
                              instanceof org.apache.bcel.generic.Instruction){
                              org.apache.bcel.generic.Instruction inst = 
                                 ih.getInstruction();
                              if(inst 
                                 instanceof org.apache.bcel.generic.BranchInstruction){
                                 org.apache.bcel.generic.BranchInstruction bInst =
                                    (org.apache.bcel.generic.BranchInstruction)inst;
                                 insertList.append(bInst);
                              }else{
                                 insertList.append(inst);
                              }//end if

                              //if it is an instance of a putfield then we need
                              //to add the assignment to the sister field right 
                              //after it.
                              if(inst instanceof org.apache.bcel.generic.PUTFIELD){
                                 org.apache.bcel.generic.FieldInstruction finst =
                                    (org.apache.bcel.generic.FieldInstruction)inst;
                                 String finstSig = finst.getSignature(cpg);
                                 if(fieldSig.compareTo(finstSig) == 0){
                                    insertList.append(new org.apache.bcel.generic.ICONST(2));
                                    insertList.append(
                                       new org.apache.bcel.generic.PUTFIELD(fieldNameIndex));
                                 }//end if
                              }//end if
                     
                           }//end if
                        }//end for
                        //replace old instruction List with the new instruction list
                        if(il == null){
                           mgen.setInstructionList(insertList);
                        }else{
                           //il.insert(ihs[0], insertList);
                        }

                        //update the class gen so that the changes to the method take hold
                        mgen.setMaxStack();
                        mgen.setMaxLocals();
                     }//end if
                  }//end for
               }//end if
            }//end if
         }//end if
   }//end apply

}//end class

