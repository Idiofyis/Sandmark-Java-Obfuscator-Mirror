/* Justin's simple obfuscation technique...
 * Much of the original code was borrowed and modified from VariableSplitter
 */

package sandmark.obfuscate.appendboguscode;


/** This is the AppendBogusCode class which appends code after the final
 * statement of a method (this may crash some reverse engineering software).
 * @author Justin Cappos (<a href="mailto:justin@cs.arizona.edu">justin@cs.arizona.edu</a>)
 * @version 1.0, August 3rd, 2002
 */

public class AppendBogusCode extends sandmark.obfuscate.MethodObfuscator {

   /** Performs the actual modification of the requested method...
    */

   public void apply(sandmark.program.Method meth) throws Exception {
      org.apache.bcel.generic.InstructionList il= meth.getInstructionList();

      if (il==null){
         // Nothing to do! (that was easy)
         return;
      }

      org.apache.bcel.generic.InstructionHandle[] ihs=il.getInstructionHandles();

      java.util.Random random = sandmark.util.Random.getRandom();

      int rand_var;
      int rand_inst_add;

      rand_inst_add=random.nextInt(7)+3;
      for (int i=0; i<rand_inst_add; i++) {
         rand_var=random.nextInt(9);

         // Here is some nasty code that randomly adds instructions in...
         // I may change this later to sample portions of the method and copy those instead...

         switch(rand_var){
         case 0:
            il.append(new org.apache.bcel.generic.GOTO(ihs[(int) ((9999*rand_var)%ihs.length)]));
            break;
         case 1:
            il.append(new org.apache.bcel.generic.BIPUSH((byte)(((int)(rand_var*9999))%255)));
            break;
         case 2:
            if (meth.getMaxLocals()>1) {
               il.append(new org.apache.bcel.generic.RET(((int)((rand_var*9999))%(meth.getMaxLocals()-1))+1));
            }
            break;
         case 3:
            il.append(new org.apache.bcel.generic.DUP());
            break;
         case 4:
            il.append(new org.apache.bcel.generic.SWAP());
            break;
         case 5:
            if (meth.getMaxLocals()>1) {
               il.append(new org.apache.bcel.generic.ILOAD(((int)((rand_var*9999))%(meth.getMaxLocals()-1))+1));
            }
            break;
         case 6:
            il.append(new org.apache.bcel.generic.IMUL());
            break;
         case 7:
            if (meth.getMaxLocals()>1) {
               il.append(new org.apache.bcel.generic.ISTORE(((int)((rand_var*9999))%(meth.getMaxLocals()-1))+1));
            }
            break;
         case 8:
            il.append(new org.apache.bcel.generic.IADD());
            break;
         }
      }

      // All done!   Put it back together...
      meth.setMaxLocals();
      meth.setMaxStack();
   }


   /* This is just a test routine... */
   public static void main(String[] args) throws Exception {
      if (args.length < 1) {
         System.out.println("Usage: AppendBogusCode <JAR FILE>.jar");
         System.exit(1);
      }

      try {
         sandmark.program.Application app = new sandmark.program.Application(args[0]);
         sandmark.obfuscate.appendboguscode.AppendBogusCode obfuscator = new
               sandmark.obfuscate.appendboguscode.AppendBogusCode();
         java.util.Iterator itr = app.classes();
         while(itr.hasNext()){
             sandmark.program.Class cls = (sandmark.program.Class)itr.next();
             sandmark.program.Method[] methods = cls.getMethods();
             for(int m = 0;  m < methods.length; m++){
                 obfuscator.apply(methods[m]);
             }
         }

         app.save("CHANGED.jar");
      }
      catch (java.io.IOException e) {
         System.err.println("I/O Error: " + e.getMessage());
      }
   }


   /** Returns the URL at which you can find information about this obfuscator. */
   public java.lang.String getAlgURL() {
      return "sandmark/obfuscate/appendboguscode/doc/help.html";
   }


   /** Returns an HTML description of this obfuscator. */
   public java.lang.String getAlgHTML() {
      return
            "<HTML><BODY>" +
            "AppendBogusCode adds bogus code after the final statement of the"+
            "target method.   This may crash some reverse engineering software\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href =\"mailto:justin@cs.arizona.edu\">Justin Cappos</a> " +
            "</TR></TD>" +
            "</TABLE>" +
            "</BODY></HTML>";

   }

   /** Returns a long description of this obfuscator's name. */
   public java.lang.String getLongName() {
      return
            "AppendBogusCode: adds extra code to the end of the target method.";
   }


   /** Returns a short description of this obfuscator's name. */
   public java.lang.String getShortName() {
      return "Random Dead Code";
   }

   public java.lang.String getAuthor() {
      return "Justin Cappos";
   }

   public java.lang.String getAuthorEmail() {
      return "justin@cs.arizona.edu";
   }

   public java.lang.String getDescription() {
      return "Appends extra code to the end of a method";
   }

   public sandmark.config.ModificationProperty[] getMutations() {
       return new sandmark.config.ModificationProperty[]{
           sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
           sandmark.config.ModificationProperty.PERFORMANCE_DEGRADE_NONE
           //thread safe, reflection safe
       };
   }

   public sandmark.config.RequisiteProperty[] getPostprohibited() {
       return new sandmark.config.RequisiteProperty[]{
           new sandmark.config.AlgorithmProperty(this)
       };
   }


}

