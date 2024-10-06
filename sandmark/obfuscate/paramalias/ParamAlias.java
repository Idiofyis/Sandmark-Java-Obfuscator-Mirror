package sandmark.obfuscate.paramalias;

/**
   This obfuscator adds a global field to get class to have the same memory location as a random formal parameter of a method
*/

public class ParamAlias
   extends sandmark.obfuscate.AppObfuscator{

   private static final boolean debug = false;
   private sandmark.program.Method methodToChange;
   private int paramIndex = -1;
   private org.apache.bcel.generic.Type paramType = null;
   private String paramName;

    /**
     *  Constructor.
     */
   public ParamAlias(){}

   public String getShortName() {
      return "ParamAlias";
   }

   public String getLongName() {
      return "Insert a global field and make its value that of a formal parameter";
   }

   public java.lang.String getAlgHTML(){
      return
          "<HTML><BODY>" +
          "The ParamAlias obfuscator adds a global field to each class " +
          "in an application and assigns that field to a formal parameter in a random method of the class. " +
          "<TABLE>" +
          "<TR><TD>" +
          "Author: <a href=\"mailto:mtg@cs.arizona.edu\">Mary Grabher</a>>\n" +
          "</TD></TR>" +
          "</TABLE>" +
          "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Mary Grabher";
   }

   public String getAuthorEmail(){
      return "mtg@cs.arizona.edu";
   }

   public String getDescription(){
      return "The ParamAlias obfuscator adds a global field to each class in an application " +
          "and assigns that field to a formal parameter in a random method of the class.";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {
         sandmark.config.ModificationProperty.I_ADD_FIELDS,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE};
      return properties;
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/paramalias/doc/help.html";
   }

   public void apply(sandmark.program.Application app) throws Exception{
      sandmark.program.Class[] classes = app.getClasses();
      applyEachClass(classes);
   }

    private void applyEachClass(sandmark.program.Class[] classes){
        for(int classNum = 0; classNum < classes.length; classNum++){

            sandmark.program.Class cls = classes[classNum];
            org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool();

            if(debug) System.out.println("In Param Alias of class: " + cls.getName());

            if(!cls.isAbstract() || !cls.isInterface()){

                /* methodToChange will be set after this (if successful) */
                paramIndex = findMethod(cls);

                /* no method in this class to change */
                if(paramIndex < 0){
                    if(debug) System.out.println("Could not find a suitable method in " + cls.getName());
                    continue;
                }

                /* find the index of the param and it's type*/
                String[] paramNames = methodToChange.getArgumentNames();
                paramName = paramNames[paramIndex];


                if(!getInfoAboutVar(paramName, cpg)) continue;

                String newFieldName = "sm$pa";

                /* create the new field */
                int field_access_flags = org.apache.bcel.Constants.ACC_PRIVATE;
                sandmark.program.LocalField fg = new sandmark.program.LocalField(cls, field_access_flags, paramType, newFieldName);
                /* add the new field */
                int newFieldIndex = cpg.addFieldref(cls.getName(), newFieldName, fg.getSignature());

                if(debug) System.out.println("New field index: " + newFieldIndex);

                /* create new instruction list */
                org.apache.bcel.generic.InstructionList insertList = new org.apache.bcel.generic.InstructionList();
                /* first make initial assignment from new field to parameter found*/
                /* load (this) */
                insertList.append(new org.apache.bcel.generic.ALOAD(0));
                /* load the parameter that was found */
                insertList.append(new org.apache.bcel.generic.ALOAD(paramIndex));
                /* add putfield instruction */
                insertList.append(new org.apache.bcel.generic.PUTFIELD(newFieldIndex));

                updateInstructions(newFieldIndex, cpg, insertList, methodToChange);
            }
        }
    }

    private int hasValidParam(int newFieldIndex, org.apache.bcel.generic.ConstantPoolGen cpg, sandmark.program.Method methodToCheck){
        org.apache.bcel.generic.Type types[] = methodToCheck.getArgumentTypes();

        for(int i = 0; i < types.length; i++){
            if(types[i].equals(paramType)){
                //System.out.println("ParamType: " + paramType +"\nType Check: " + types[i]);
                return i;
            }
        }

        return -1;
    }

    private boolean getInfoAboutVar(String name, org.apache.bcel.generic.ConstantPoolGen cpg){
        org.apache.bcel.generic.LocalVariableGen[] lvg = methodToChange.getLocalVariables();

        for(int i =0;i < lvg.length; i++){
            if(name.equals(lvg[i].getName())){
                org.apache.bcel.classfile.LocalVariable lv = lvg[i].getLocalVariable(cpg);
                paramIndex = i;
                paramType = lvg[i].getType();
                return true;
            }
        }

        return false;
    }


    private void updateInstructions(int newFieldIndex, org.apache.bcel.generic.ConstantPoolGen cpg,
                                    org.apache.bcel.generic.InstructionList insertList, sandmark.program.Method theMethod){

        int iSCount = 0;
        java.util.ArrayList newIndexes = new java.util.ArrayList();
        /*get the instruction list for the method */
        org.apache.bcel.generic.InstructionList il = theMethod.getInstructionList();

        /* now must go through all instructions and change reference to new field */
        org.apache.bcel.generic.InstructionHandle ihs[] = null;

        if(il != null){
            ihs = il.getInstructionHandles();

            /* insert the first instructions for the new field */
            il.insert(ihs[0], insertList);

            for(int k = 0; k < ihs.length; k++){

                org.apache.bcel.generic.InstructionHandle ih = ihs[k];
                org.apache.bcel.generic.Instruction inst = ih.getInstruction();

                if(inst instanceof org.apache.bcel.generic.ALOAD){
                    org.apache.bcel.generic.LoadInstruction li = (org.apache.bcel.generic.LoadInstruction) inst;
                    if(li.getIndex() == paramIndex){
                        insertList = new org.apache.bcel.generic.InstructionList();
                        il.insert(ihs[k], new org.apache.bcel.generic.ALOAD(0));
                        org.apache.bcel.generic.Instruction instNext1 = ihs[k+1].getInstruction();
                        org.apache.bcel.generic.Instruction instNext2 = null;
                        if(k+2 < ihs.length)
                            instNext2 = ihs[k+2].getInstruction();

                        if(instNext1 instanceof org.apache.bcel.generic.INVOKEVIRTUAL){
                            org.apache.bcel.generic.InvokeInstruction ii = (org.apache.bcel.generic.InvokeInstruction) instNext1;
                            if(ii.getType(cpg).equals(paramType))
                                il.insert(ihs[k], new org.apache.bcel.generic.ALOAD(0));
                        }else if(instNext1 instanceof org.apache.bcel.generic.ICONST && instNext2 instanceof org.apache.bcel.generic.INVOKEVIRTUAL){
                            org.apache.bcel.generic.InvokeInstruction ii = (org.apache.bcel.generic.InvokeInstruction) instNext2;
                            if(ii.getType(cpg).equals(paramType))
                                il.insert(ihs[k], new org.apache.bcel.generic.ALOAD(0));
                        }

                        il.insert(ihs[k], new org.apache.bcel.generic.GETFIELD(newFieldIndex));
                        deleteInst(il, ihs[k], ihs[k+1]);
                    }
                }else if(inst instanceof org.apache.bcel.generic.ASTORE){
                    org.apache.bcel.generic.StoreInstruction si = (org.apache.bcel.generic.StoreInstruction) inst;

                    if(si.getIndex() == paramIndex){
                        il.insert(ihs[k], new org.apache.bcel.generic.PUTFIELD(newFieldIndex));
                        deleteInst(il, ihs[k], ihs[k+1]);
                    }
                }else if(inst instanceof org.apache.bcel.generic.NEW){
                    newIndexes.add(new Integer(k));
                }else if(inst instanceof org.apache.bcel.generic.INVOKESPECIAL){
                    iSCount++;
                    org.apache.bcel.generic.INVOKESPECIAL ii = (org.apache.bcel.generic.INVOKESPECIAL) inst;

                    org.apache.bcel.generic.Instruction nextInst = ihs[k+1].getInstruction();
                    if(nextInst instanceof org.apache.bcel.generic.ASTORE){

                        org.apache.bcel.generic.StoreInstruction si = (org.apache.bcel.generic.StoreInstruction) nextInst;

                        if(si.getIndex() == paramIndex){

                            int place = ((Integer)newIndexes.get(newIndexes.size() - iSCount)).intValue();
                            insertList = new org.apache.bcel.generic.InstructionList();
                            insertList.append(new org.apache.bcel.generic.ALOAD(0));
                            il.insert(ihs[place], insertList);
                        }
                    }
                }
            }

            theMethod.setInstructionList(il);
            theMethod.mark();
            if(debug) System.out.println("New Instruction List for: " + theMethod.getName() + "\n___________________________\n" + theMethod.getInstructionList());
        }
    }

    private void deleteInst(org.apache.bcel.generic.InstructionList il, org.apache.bcel.generic.InstructionHandle ihs, org.apache.bcel.generic.InstructionHandle next){
        try{
            il.delete(ihs);
        }catch(org.apache.bcel.generic.TargetLostException e){
            org.apache.bcel.generic.InstructionHandle[] targets = e.getTargets();
            for(int i=0; i < targets.length; i++){
                org.apache.bcel.generic.InstructionTargeter[] targeters = targets[i].getTargeters();
                for(int j=0; j < targeters.length; j++){
                    targeters[j].updateTarget(targets[i], next);
                }
            }
        }
    }


   private int findParamIndex(org.apache.bcel.generic.Type[] typesArr){

       org.apache.bcel.generic.Type[] methodTypes = typesArr;

       for(int types = 0; types < methodTypes.length; types++){
           if(methodTypes[types] instanceof org.apache.bcel.generic.ObjectType){
               return types;
           }
       }
       return -1;
   }

   private int  findMethod(sandmark.program.Class cls){

      java.util.Random gen = sandmark.util.Random.getRandom();
      sandmark.program.Method[] methods = cls.getMethods();

      if(debug) System.out.println("In find Method for: " + cls.getName());
      int index = gen.nextInt(methods.length);

      int start = index;

      while(true){

         /* get a random method */
         methodToChange = methods[index % methods.length];
         if(debug) System.out.println("Search thru: " + methodToChange.getName());
         org.apache.bcel.generic.InstructionList il = methodToChange.getInstructionList();

         /* check to see if method has at least one parameter and continue to add field etc. */
         org.apache.bcel.generic.Type[] argTypes = methodToChange.getArgumentTypes();

         if(argTypes.length > 0 && !methodToChange.isStatic() && !methodToChange.getName().equals("<init>") && !methodToChange.getName().equals("<clinit>")){
            int indexParam = findParamIndex(argTypes);
            if(indexParam >= 0){
                if(debug) System.out.println("Instruction List for method: __" + methodToChange.getName() + "__\n" + il);
                return indexParam;
            }
         }
         index++;

         /* a method to change does not exist for this class */
         if(index % methods.length == start){
            methodToChange = null;
            if(debug) System.out.println("Could not find a suitable method for: " + cls.getName());
            return -1;
         }
      }
   }

   public static void main(String[] args){
      /* just a little test program */

      try{

         sandmark.program.Application app = new sandmark.program.Application(args[0]);

         sandmark.program.Class[] classes = app.getClasses();
         ParamAlias pa = new ParamAlias();
         System.out.println("About to apply");
         pa.apply(app);
         System.out.println("About to save");
         app.save(args[0] + "_CHANGED.jar");
      }catch(Exception e){
          System.out.println(e + "\n\n");
          e.printStackTrace();
      }
   }
}


