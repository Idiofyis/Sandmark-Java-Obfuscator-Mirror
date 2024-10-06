package sandmark.birthmark.wpp;

public class Annotate{

   private sandmark.program.Application app;

   public Annotate(sandmark.program.Application app){
      this.app = app;
   }

   public void annotate() {
      //iterate through the classes adding a reference to
      //"sandmark.birthmark.wpp.Annotator"
      java.util.Iterator classes = app.classes();
      String annotatorClass = "sandmark.birthmark.wpp.Annotator";
      while(classes.hasNext()){
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         //add a reference to "sandmark.birthmark.wpp.Annotator"
         org.apache.bcel.generic.ConstantPoolGen cp = cls.getConstantPool();
         int annoMethodRef = cp.addMethodref(annotatorClass, "sm$mark", "()V"); 
         annotateCFGs(cls,annoMethodRef);
         cls.mark();
      }
   }

   public void save(java.io.File file) throws java.io.IOException{
      app.save(file);
   }

   private void annotateCFGs(sandmark.program.Class cls, int annoMethodRef){
      org.apache.bcel.generic.InstructionHandle ih = null;

      java.util.Iterator methods = cls.methods();
      while(methods.hasNext()){
         sandmark.program.Method m = (sandmark.program.Method)methods.next();
         if(m.isAbstract() || m.isInterface())
            continue;
         //System.out.println("method: " + m.getName());
         org.apache.bcel.generic.InstructionList mil = m.getInstructionList();
         sandmark.analysis.controlflowgraph.MethodCFG mCFG = m.getCFG();
         java.util.Iterator blocks = mCFG.basicBlockIterator();
         while(blocks.hasNext()){
            sandmark.analysis.controlflowgraph.BasicBlock bb =
               (sandmark.analysis.controlflowgraph.BasicBlock)blocks.next();
            //System.out.println(bb.toString());
            org.apache.bcel.generic.InstructionHandle lastIH =
               bb.getLastInstruction();
            if(lastIH.getInstruction() instanceof
               org.apache.bcel.generic.BranchInstruction){
               mil.append(lastIH, new
                  org.apache.bcel.generic.INVOKESTATIC(annoMethodRef));
               //System.out.println("appended");
               org.apache.bcel.generic.BranchInstruction binst =
                  (org.apache.bcel.generic.BranchInstruction)lastIH.getInstruction();
               org.apache.bcel.generic.InstructionHandle target =
                  binst.getTarget();
               ih = mil.insert(target, new 
                  org.apache.bcel.generic.INVOKESTATIC(annoMethodRef));
               //System.out.println("inserted");
               mil.setPositions();
               binst.setTarget(ih);
               //mil.redirectBranches(target, ih);
            }else if(!(lastIH.getInstruction() instanceof
               org.apache.bcel.generic.ReturnInstruction)){
               mil.append(lastIH, new
                  org.apache.bcel.generic.INVOKESTATIC(annoMethodRef));
               //System.out.println("appended");
            }
            mil.setPositions();
         }
         m.mark();
      }
   }
}

