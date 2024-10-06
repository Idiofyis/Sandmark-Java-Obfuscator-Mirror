package sandmark.util;

/* 
 *  Makes all fields and methods in an application public.
 *  <p>
 *  Used by Method2RMadness obfuscator.
 *
 *  @author Martin Stepp (<a href="mailto:stepp">stepp</a>)
 *  @version 1.0, May 8, 2002
 */
public class Publicizer
{
    /* Turn to true to print debugging messages. */
    private static final boolean DEBUG = false;

    /* Runs a quick test of this obfuscator. */
    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.err.println("Usage: Publicizer JAR_FILE");
            System.exit(1);
        }
        sandmark.program.Application app = new sandmark.program.Application(args[0]);
        (new sandmark.util.Publicizer()).apply(app);
        app.save("HARDCODE.jar");
    }

    /*  Makes public the fields/methods in this Application's classes. */
    public void apply(sandmark.program.Application app)
    {
        java.util.Iterator itr = app.classes();
        while(itr.hasNext())
        {
            sandmark.program.Class cls = (sandmark.program.Class) itr.next();

            // publicize this class
            cls.setPublic(true);
            cls.setPrivate(false);
            cls.setProtected(false);
            cls.setFinal(false);

            org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool();

            // publicize all methods in this class
            java.util.Iterator mit = cls.methods();
            while(mit.hasNext()) {
                sandmark.program.Method m = (sandmark.program.Method)mit.next();
                publicizeMethod(m, cls);
            }

            // publicize all fields in this class
            java.util.Iterator fit = cls.fields();
            while(fit.hasNext()) {
                sandmark.program.Field f = (sandmark.program.Field)fit.next();
                publicizeField(f, cls);
            }
        }
    }

   private void publicizeMethod(sandmark.program.Method m, sandmark.program.Class cls){
      org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool();

      // add in a fix in case of private superclass methods
      if (m.isPrivate() && !m.getName().equals("<init>") && 
	  !m.isStatic()) {
         String newname = m.getName()+'$';
         while(isInJar(newname+m.getSignature(), cls.getApplication()))
            newname += '$';

         // fix up all the method calls to this private method
         for (java.util.Iterator miter = cls.methods(); miter.hasNext(); ){
            sandmark.program.Method fixupmethod = (sandmark.program.Method)miter.next();
            if (fixupmethod.getInstructionList()!=null){
               org.apache.bcel.generic.InstructionHandle[] handles = 
                  fixupmethod.getInstructionList().getInstructionHandles();
               for (int i=0;i<handles.length;i++){
                  // if this is a method call
                  if (handles[i].getInstruction() instanceof org.apache.bcel.generic.InvokeInstruction){
                     org.apache.bcel.generic.InvokeInstruction ii = 
                        (org.apache.bcel.generic.InvokeInstruction)handles[i].getInstruction();
                               
                     // if this is the right method call
                     if ((ii.getName(cpg)+ii.getSignature(cpg)).equals(m.getName()+m.getSignature()) &&
                         ii.getClassName(cpg).equals(cls.getName())){
                                  
                        org.apache.bcel.generic.InstructionFactory factory = 
                           new org.apache.bcel.generic.InstructionFactory(cpg);
                                  
                        handles[i].setInstruction(factory.createInvoke(cls.getName(), newname, m.getReturnType(), 
                                                                       m.getArgumentTypes(), ii.getOpcode()));
                     }
                  }
               }
            }
         }
                   
         m.setName(newname);
      }

      if (!m.getName().equals("<clinit>")){
         m.setPublic(true);
         m.setPrivate(false);
         m.setProtected(false);
      }
      m.setFinal(false);
   }

   private void publicizeField(sandmark.program.Field f, sandmark.program.Class cls){
      if (cls.isInterface()) return;

      org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool();
      Object value = null;

      // check for/remove ConstantValue attributes
      org.apache.bcel.classfile.Attribute[] attrs = f.getAttributes();
      for (int i=0;i<attrs.length;i++){
         if (attrs[i] instanceof org.apache.bcel.classfile.ConstantValue){
            f.removeAttribute(attrs[i]);
            int index = ((org.apache.bcel.classfile.ConstantValue)attrs[i]).getConstantValueIndex();
            org.apache.bcel.classfile.Constant cons = cpg.getConstant(index);
            value = ((org.apache.bcel.classfile.ConstantObject)cons).getConstantValue(cpg.getConstantPool());
         }
      }
      
      // if no ConstantValue attributes, check for a FieldGen init value (same thing)
      if (value==null && f.getInitValue()!=null){
         String initValue = f.getInitValue();
         if (f.getType().equals(org.apache.bcel.generic.Type.LONG)){
            value = new Long(initValue);
         }else if (f.getType().equals(org.apache.bcel.generic.Type.DOUBLE)){
            value = new Double(initValue);
         }else if (f.getType().equals(org.apache.bcel.generic.Type.FLOAT)){
            value = new Float(initValue);
         }else if (f.getType().equals(org.apache.bcel.generic.Type.INT) || 
                   f.getType().equals(org.apache.bcel.generic.Type.SHORT) ||
                   f.getType().equals(org.apache.bcel.generic.Type.BYTE) || 
                   f.getType().equals(org.apache.bcel.generic.Type.BOOLEAN) ||
                   f.getType().equals(org.apache.bcel.generic.Type.CHAR)){
            value = new Integer(initValue);
         }else if (f.getType().equals(org.apache.bcel.generic.Type.STRING)){
            value = initValue;
         }
      }
      f.cancelInitValue();
      
      // if we found an init value and the field is nonstatic, put in the clinit assignment
      if (value!=null && f.isStatic()){
         org.apache.bcel.generic.InstructionFactory factory = 
            new org.apache.bcel.generic.InstructionFactory(cpg);
         org.apache.bcel.generic.Instruction[] instrs = 
            new org.apache.bcel.generic.Instruction[]{
               factory.createConstant(value),
               factory.createPutStatic(cls.getName(), f.getName(), f.getType())
            };
         
         sandmark.program.Method clinit = cls.getMethod("<clinit>", "()V");
         if (clinit==null){
            org.apache.bcel.generic.InstructionList ilist = 
               new org.apache.bcel.generic.InstructionList();
            ilist.append(instrs[0]);
            ilist.append(instrs[1]);
            ilist.append(new org.apache.bcel.generic.RETURN());
            
            clinit = new sandmark.program.LocalMethod(cls, 
                                                      org.apache.bcel.Constants.ACC_STATIC,
                                                      org.apache.bcel.generic.Type.VOID,
                                                      org.apache.bcel.generic.Type.NO_ARGS,
                                                      null, 
                                                      "<clinit>",
                                                      ilist);
         }else{
            org.apache.bcel.generic.InstructionList clinitlist = 
               clinit.getInstructionList();
            clinitlist.insert(instrs[1]);
            clinitlist.insert(instrs[0]);
         }
         clinit.mark();
         cls.mark();
      }
      
      f.setPublic(true);
      f.setPrivate(false);
      f.setProtected(false);
      f.setFinal(false);
   }
   
   private boolean isInJar(String newname, sandmark.program.Application app){
      for (java.util.Iterator citer=app.classes();citer.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         for (java.util.Iterator miter = clazz.methods(); miter.hasNext(); ){
            sandmark.program.Method method = (sandmark.program.Method)miter.next();
            if ((method.getName()+method.getSignature()).equals(newname))
               return true;
         }
      }
      return false;
   }
   
   
   /*  Returns the URL at which you can find information about this obfuscator. */
   public java.lang.String getAlgURL()
   {
      return "sandmark/obfuscate/methodmadness/doc/helppublicizer.html";
   }
   
   
   /*  Returns an HTML description of this obfuscator. */
   public java.lang.String getAlgHTML()
   {
      return 
         "<HTML><BODY>" +
         "Publicizer is an obfuscator that makes all fields and methods public.\n" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:stepp\">Martin Stepp</a> and " +
         "<a href = \"mailto:kheffner@cs.arizona.edu\">Kelly Heffner</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }   
   
   /*  Returns a long description of this obfuscator's name. */
   public java.lang.String getLongName()
   {
      return "Publicizer; Converts all methods, classes, and fields to " +
         "public scope.";
   }
   
   
   /*  Returns a short description of this obfuscator's name. */
   public java.lang.String getShortName()
   {
      return "Publicizer";
   }
   
   public java.lang.String getDescription()
   {
      return "Converts all methods, classes, and fields to " +
         "public scope.";
   }
   
   public sandmark.config.ModificationProperty [] getMutations()
   {
      return new sandmark.config.ModificationProperty[]{
         sandmark.config.ModificationProperty.I_CHANGE_METHOD_SCOPES,
         sandmark.config.ModificationProperty.I_CHANGE_FIELD_SCOPES,
         sandmark.config.ModificationProperty.I_CHANGE_CLASS_SCOPES
      };
   }
   
   public java.lang.String getAuthor()
   {
      return "Martin Stepp";
   }
   
   public java.lang.String getAuthorEmail()
   {
      return "stepp";
   }
}
