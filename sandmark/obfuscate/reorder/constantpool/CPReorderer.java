package sandmark.obfuscate.reorder.constantpool;

/**  This obfuscation randomly permutes the constant pool indexes
 *   of its classes. 
 */
public class CPReorderer extends sandmark.obfuscate.AppObfuscator{
   private static java.util.Hashtable getPermutation(org.apache.bcel.generic.ConstantPoolGen cpg){
      int size = cpg.getSize();
      java.util.Hashtable result = new java.util.Hashtable(233);
      java.util.Vector usable = new java.util.Vector(size);
      
      for (int i=1;i<size;i++){
         Object o = cpg.getConstant(i);
         if (o==null)
            continue;
         usable.add(new Object[]{o, new Integer(i)});
      }

      java.util.Random random = sandmark.util.Random.getRandom();
      
      int next=1;
      while(usable.size()>0){
         Object[] pair = (Object[])usable.remove(random.nextInt(usable.size()));
         
         result.put(pair[1], new Integer(next));
         next++;

         if (pair[0] instanceof org.apache.bcel.classfile.ConstantLong ||
             pair[0] instanceof org.apache.bcel.classfile.ConstantDouble){
            next++;
         }
      }
      return result;
   }

   private static int getindex(java.util.Hashtable hash, int i){
      Integer result = (Integer)hash.get(new Integer(i));
      if (result==null)
         return 0;
      return result.intValue();
   }

   
   private static void fixFields(sandmark.program.Class clazz, 
                                 java.util.Hashtable indexhash,
                                 org.apache.bcel.classfile.ConstantPool cp){
      for (java.util.Iterator fiter=clazz.fields();fiter.hasNext();){
         sandmark.program.Field field = (sandmark.program.Field)fiter.next();
         
         fixAttributes(field.getAttributes(), indexhash, cp);
      }
   }

   private static void fixMethods(sandmark.program.Class clazz, 
                                  java.util.Hashtable indexhash,
                                  org.apache.bcel.classfile.ConstantPool cpool){
      for (java.util.Iterator miter=clazz.methods();miter.hasNext();){
         sandmark.program.Method method = (sandmark.program.Method)miter.next();

         fixAttributes(method.getAttributes(), indexhash, cpool);

         // also fix instructions
         org.apache.bcel.generic.InstructionList ilist = method.getInstructionList();
         if (ilist==null)
            continue;

         for (java.util.Iterator iiter=ilist.iterator();iiter.hasNext();){
            org.apache.bcel.generic.InstructionHandle ih = 
               (org.apache.bcel.generic.InstructionHandle)iiter.next();

            if (ih.getInstruction() instanceof org.apache.bcel.generic.CPInstruction){
               org.apache.bcel.generic.CPInstruction cp = 
                  (org.apache.bcel.generic.CPInstruction)ih.getInstruction();
               cp.setIndex(getindex(indexhash, cp.getIndex()));
            }
         }
      }
   }

   private static void fixAttributes(org.apache.bcel.classfile.Attribute[] atts, 
                                     java.util.Hashtable indexhash,
                                     org.apache.bcel.classfile.ConstantPool cp){
      for (int i=0;i<atts.length;i++){
         atts[i].setNameIndex(getindex(indexhash, atts[i].getNameIndex()));
         atts[i].setConstantPool(cp);

         if (atts[i] instanceof org.apache.bcel.classfile.Code){
            // fix exception handlers and sub-attributes
            org.apache.bcel.classfile.Code code = 
               (org.apache.bcel.classfile.Code)atts[i];
            fixAttributes(code.getAttributes(), indexhash, cp);
            org.apache.bcel.classfile.CodeException[] table = 
               code.getExceptionTable();

            for (int j=0;j<table.length;j++){
               if (table[j].getCatchType()!=0)
                  table[j].setCatchType(getindex(indexhash, table[j].getCatchType()));
            }
         }else if (atts[i] instanceof org.apache.bcel.classfile.ConstantValue){
            org.apache.bcel.classfile.ConstantValue cv = 
               (org.apache.bcel.classfile.ConstantValue)atts[i];
            cv.setConstantValueIndex(getindex(indexhash, cv.getConstantValueIndex()));
         }else if (atts[i] instanceof org.apache.bcel.classfile.Deprecated){
            // do nothing
         }else if (atts[i] instanceof org.apache.bcel.classfile.ExceptionTable){
            org.apache.bcel.classfile.ExceptionTable et = 
               (org.apache.bcel.classfile.ExceptionTable)atts[i];
            int[] table = et.getExceptionIndexTable();
            for (int j=0;j<table.length;j++){
               table[j] = getindex(indexhash, table[j]);
            }
            et.setExceptionIndexTable(table);
         }else if (atts[i] instanceof org.apache.bcel.classfile.InnerClasses){
            org.apache.bcel.classfile.InnerClasses ic = 
               (org.apache.bcel.classfile.InnerClasses)atts[i];
            org.apache.bcel.classfile.InnerClass[] classes = 
               ic.getInnerClasses();

            for (int j=0;j<classes.length;j++){
               if (classes[j].getInnerClassIndex()!=0)
                  classes[j].setInnerClassIndex(getindex(indexhash, classes[j].getInnerClassIndex()));
               if (classes[j].getInnerNameIndex()!=0)
                  classes[j].setInnerNameIndex(getindex(indexhash, classes[j].getInnerNameIndex()));
               if (classes[j].getOuterClassIndex()!=0)
                  classes[j].setOuterClassIndex(getindex(indexhash, classes[j].getOuterClassIndex()));
            }
            ic.setInnerClasses(classes);
         }else if (atts[i] instanceof org.apache.bcel.classfile.LineNumberTable){
            // do nothing
         }else if (atts[i] instanceof org.apache.bcel.classfile.LocalVariableTable){
            org.apache.bcel.classfile.LocalVariableTable lvt = 
               (org.apache.bcel.classfile.LocalVariableTable)atts[i];

            org.apache.bcel.classfile.LocalVariable[] locals = 
               lvt.getLocalVariableTable();

            for (int j=0;j<locals.length;j++){
               locals[j].setNameIndex(getindex(indexhash, locals[j].getNameIndex()));
               locals[j].setSignatureIndex(getindex(indexhash, locals[j].getSignatureIndex()));
            }
            lvt.setLocalVariableTable(locals);
         }else if (atts[i] instanceof org.apache.bcel.classfile.PMGClass){
            throw new RuntimeException("Unknown attribute type found");
         }else if (atts[i] instanceof org.apache.bcel.classfile.Signature){
            org.apache.bcel.classfile.Signature sig = 
               (org.apache.bcel.classfile.Signature)atts[i];
            sig.setSignatureIndex(getindex(indexhash, sig.getSignatureIndex()));
         }else if (atts[i] instanceof org.apache.bcel.classfile.SourceFile){
            org.apache.bcel.classfile.SourceFile sf = 
               (org.apache.bcel.classfile.SourceFile)atts[i];
            sf.setSourceFileIndex(getindex(indexhash, sf.getSourceFileIndex()));
         }else if (atts[i] instanceof org.apache.bcel.classfile.StackMap){
            // do nothing
         }else if (atts[i] instanceof org.apache.bcel.classfile.Synthetic){
            // do nothing
         }else if (atts[i] instanceof org.apache.bcel.classfile.Unknown){
            throw new RuntimeException("Unknown attribute type found");
         }
      }
   }
   
   private static org.apache.bcel.classfile.ConstantPool fixCP
      (java.util.Hashtable indexhash, org.apache.bcel.generic.ConstantPoolGen cpg){

         org.apache.bcel.classfile.Constant[] newconstants = 
            new org.apache.bcel.classfile.Constant[cpg.getSize()];
         for (int i=1;i<newconstants.length;i++){
            Integer index = (Integer)indexhash.get(new Integer(i));
            if (index==null)
               continue;
            newconstants[index.intValue()] = cpg.getConstant(i);
         }

         for (int i=1;i<newconstants.length;i++){
            if (newconstants[i]!=null){
               if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantClass){
                  org.apache.bcel.classfile.ConstantClass c = 
                     (org.apache.bcel.classfile.ConstantClass)newconstants[i];
                  c.setNameIndex(getindex(indexhash, c.getNameIndex()));
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantCP){
                  org.apache.bcel.classfile.ConstantCP c = 
                     (org.apache.bcel.classfile.ConstantCP)newconstants[i];
                  c.setClassIndex(getindex(indexhash, c.getClassIndex()));
                  c.setNameAndTypeIndex(getindex(indexhash, c.getNameAndTypeIndex()));
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantDouble){
                  // do nothing
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantFloat){
                  // do nothing
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantInteger){
                  // do nothing
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantLong){
                  // do nothing
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantNameAndType){
                  org.apache.bcel.classfile.ConstantNameAndType c = 
                     (org.apache.bcel.classfile.ConstantNameAndType)newconstants[i];
                  c.setNameIndex(getindex(indexhash, c.getNameIndex()));
                  c.setSignatureIndex(getindex(indexhash, c.getSignatureIndex()));
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantString){
                  org.apache.bcel.classfile.ConstantString c = 
                     (org.apache.bcel.classfile.ConstantString)newconstants[i];
                  c.setStringIndex(getindex(indexhash, c.getStringIndex()));
               }else if (newconstants[i] instanceof org.apache.bcel.classfile.ConstantUtf8){
                  // do nothing
               }
            }
         }

         return new org.apache.bcel.classfile.ConstantPool(newconstants);
   }

   public void apply(sandmark.program.Application app) throws Exception{
      for (java.util.Iterator iter=app.classes();iter.hasNext();){
         sandmark.program.Class clazz = (sandmark.program.Class)iter.next();
         String[] interfaces = clazz.getInterfaceNames();
         
         for (int i=0;i<interfaces.length;i++)
            clazz.removeInterface(interfaces[i]);
         
         org.apache.bcel.generic.ConstantPoolGen cpg = clazz.getConstantPool();
         java.util.Hashtable indexhash = getPermutation(cpg);

         org.apache.bcel.classfile.ConstantPool cp = fixCP(indexhash, cpg);
         clazz.setConstantPool(cp);

         clazz.setClassNameIndex(getindex(indexhash,clazz.getClassNameIndex()));
         clazz.setSuperclassNameIndex(getindex(indexhash, clazz.getSuperclassNameIndex()));

         for (int i=0;i<interfaces.length;i++)
            clazz.addInterface(interfaces[i]);

         fixFields(clazz, indexhash, cp);
         fixMethods(clazz, indexhash, cp);
         fixAttributes(clazz.getAttributes(), indexhash, cp);
      }
   }


   //////////////////////////////////

   public sandmark.config.ModificationProperty[] getMutations()
   {
      return null;
   }

   public String getShortName()
   {
      return "Constant Pool Reorderer";
   }

   public String getDescription()
   {
      return "ConstantPool Reorderer randomly reassigns constant pool indices.";
   }

   public String getAuthor()
   {
      return "Mike Stepp, Ashok Venkatraj";
   }

   public String getAuthorEmail()
   {
      return "{ashok,steppm}@cs.arizona.edu";
   }

   public String getLongName() {
      return "ConstantPool Reorderer randomly reassigns constant pool indices";
   }

   public java.lang.String getAlgHTML()
   {
      return
         "<HTML><BODY>" +
         "ConstantPool Reorderer randomly reassigns constant pool indices.\n" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:ashok@cs.arizona.edu\">Ashok Venkatraj</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/reorder/constantpool/doc/help.html";
   }

   public static void main(String args[]) throws Throwable{
      if (args.length<1)
         return;

      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);

      new CPReorderer().apply(app);

      app.save(args[0]+".out");
   }
}
