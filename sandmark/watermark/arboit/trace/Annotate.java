package sandmark.watermark.arboit.trace;

public class Annotate {

   sandmark.program.Application app;
   sandmark.util.ConfigProperties props;

   /**
    * The code we are adding to each usable if statement in a method.
    * <PRE>
    * INVOKESTATIC sandmark.watermark.arboit.trace.Annotator.sm$mark() 
    * </PRE>
    */
   public static final int ANNOTATEDCODESIZE = 3;

   public Annotate(sandmark.program.Application app,
                   sandmark.util.ConfigProperties props) {
      this.props = props;
      this.app = app;
   }

   public void annotate() {
      java.util.Iterator classes = app.classes();
      String annotatorClass = props.getProperty("DWM_AA_AnnotatorClass",
         "sandmark.watermark.arboit.trace.Annotator");
      while(classes.hasNext()){
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         //add a reference to "sandmark.watermark.arboit.trace.Annotator"
         org.apache.bcel.generic.ConstantPoolGen cp = cls.getConstantPool();
         int annoMethodRef = cp.addMethodref(annotatorClass, "sm$mark", "()V"); 
         annotateClass(cls, annoMethodRef);
      }
   }

   /**
    * Save the edited classfiles under a new name.
    */  
   public void save(java.io.File appFile) throws java.io.IOException {
      System.out.println(appFile);
      app.save(appFile.toString());
   }

   public void annotateClass(sandmark.program.Class cls, int annoMethodRef){
      java.util.Iterator methods = cls.methods();
      while(methods.hasNext()){
         sandmark.program.Method m = (sandmark.program.Method)methods.next();
         annotateMethod(m, annoMethodRef);
      }
   }

   public void annotateMethod(sandmark.program.Method m, int annoMethodRef){
      //scan the instructions looking for if statements
      //when one is found insert a mark before it.
      org.apache.bcel.generic.InstructionList mil = m.getInstructionList();
      if(mil == null)
         return;
      org.apache.bcel.generic.InstructionHandle[] ihs =
         mil.getInstructionHandles();
      for(int i=0; i < ihs.length; i++){
         org.apache.bcel.generic.InstructionHandle ih = ihs[i];
         org.apache.bcel.generic.Instruction inst = ih.getInstruction();
         if(inst instanceof org.apache.bcel.generic.IfInstruction){
            org.apache.bcel.generic.InstructionList il = 
               new org.apache.bcel.generic.InstructionList();
            //insert the mark instructions before this instruction
            il.append(new org.apache.bcel.generic.INVOKESTATIC(annoMethodRef));
            mil.insert(inst, il);
         }
      }
      m.setMaxStack();
      m.mark();
   }

   

}
