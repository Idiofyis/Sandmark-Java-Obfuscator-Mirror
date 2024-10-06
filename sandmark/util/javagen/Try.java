package sandmark.util.javagen;

public class Try
   extends sandmark.util.javagen.Statement {
   sandmark.util.javagen.List body;
   String exception;
   sandmark.util.javagen.List Catch;

   public Try (
      sandmark.util.javagen.List body,
      String exception,
      sandmark.util.javagen.List Catch){
      this.body = body;
      this.exception = exception;
      this.Catch = Catch;
   }

   public String toString(String indent)  {
      String P = indent  +  "try {\n";
      P += renderStats(body, indent);
      P += indent  + "} catch ("  +  exception.toString()  +  " ex) {\n" ;
      P += renderStats(Catch, indent);
      P += indent  +  "}";
      return P;
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();

       org.apache.bcel.generic.InstructionHandle start_pc =
          il.append(new org.apache.bcel.generic.NOP());

      java.util.Iterator siter = body.iterator();
      while (siter.hasNext()) {
          sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
          s.toByteCode(cg,mg);
      };

      org.apache.bcel.generic.GOTO branch = new org.apache.bcel.generic.GOTO(null);
      org.apache.bcel.generic.InstructionHandle end_pc =
           il.append(branch);

      // The exception is on the stack when entering
      // the catch block. Pop it.
      org.apache.bcel.generic.InstructionHandle handler_pc =
          il.append(new org.apache.bcel.generic.POP());

      java.util.Iterator citer = Catch.iterator();
      while (citer.hasNext()) {
          sandmark.util.javagen.Statement c = (sandmark.util.javagen.Statement) citer.next();
          c.toByteCode(cg,mg);
      };

      org.apache.bcel.generic.InstructionHandle next_pc =
          il.append(new org.apache.bcel.generic.NOP());
      branch.setTarget(next_pc);

      org.apache.bcel.generic.ObjectType catch_type =
         new org.apache.bcel.generic.ObjectType(exception);

      org.apache.bcel.generic.CodeExceptionGen eg =
         mg.addExceptionHandler(start_pc, end_pc, handler_pc, catch_type);
   }

 	 public void toCode(
 	           sandmark.program.Class cg,
          sandmark.program.Method mg) {
         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();

          org.apache.bcel.generic.InstructionHandle start_pc =
             il.append(new org.apache.bcel.generic.NOP());

         java.util.Iterator siter = body.iterator();
         while (siter.hasNext()) {
             sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
             s.toCode(cg,mg);
         };

         org.apache.bcel.generic.GOTO branch = new org.apache.bcel.generic.GOTO(null);
         org.apache.bcel.generic.InstructionHandle end_pc =
              il.append(branch);

         // The exception is on the stack when entering
         // the catch block. Pop it.
         org.apache.bcel.generic.InstructionHandle handler_pc =
             il.append(new org.apache.bcel.generic.POP());

         java.util.Iterator citer = Catch.iterator();
         while (citer.hasNext()) {
             sandmark.util.javagen.Statement c = (sandmark.util.javagen.Statement) citer.next();
             c.toCode(cg,mg);
         };

         org.apache.bcel.generic.InstructionHandle next_pc =
             il.append(new org.apache.bcel.generic.NOP());
         branch.setTarget(next_pc);

         org.apache.bcel.generic.ObjectType catch_type =
            new org.apache.bcel.generic.ObjectType(exception);

         org.apache.bcel.generic.CodeExceptionGen eg =
            mg.addExceptionHandler(start_pc, end_pc, handler_pc, catch_type);
   }
}



