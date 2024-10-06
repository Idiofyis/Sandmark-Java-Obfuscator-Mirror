package sandmark.program;



/**
 * Represents a modifiable method within a class or interface.
 *
 * <P> Modification methods in this class automatically call the
 * {@link sandmark.program.Object#mark() mark} method
 * to register their changes.
 *
 * @see sandmark.program.Method
 */

public class LocalMethod extends sandmark.program.Method {



   /**
    * Constructs a LocalMethod and adds it to a class.
    * The arguments mimic a BCEL MethodGen constructor.
    */
   public LocalMethod(
         sandmark.program.Class parent,
         int access_flags,
         org.apache.bcel.generic.Type return_type,
         org.apache.bcel.generic.Type[] arg_types,
         String[] arg_names, String method_name,
         org.apache.bcel.generic.InstructionList il) {

      super(parent, makeMethod(parent, access_flags,
         return_type, arg_types, arg_names, method_name, il),null);
   }

   private static org.apache.bcel.generic.MethodGen makeMethod(
         sandmark.program.Class parent,
         int access_flags,
         org.apache.bcel.generic.Type return_type,
         org.apache.bcel.generic.Type[] arg_types,
         String[] arg_names, String method_name,
         org.apache.bcel.generic.InstructionList il) {

      org.apache.bcel.generic.MethodGen mg =
         new org.apache.bcel.generic.MethodGen(
            access_flags, return_type, arg_types, arg_names, method_name,
            parent.getName(), il, parent.getConstantPool());
         mg.setMaxLocals();
         mg.setMaxStack();
         return mg;
   }



   /**
    * Constructs a LocalMethod from a BCEL Method and adds it to a class.
    */
   public LocalMethod(sandmark.program.Class parent,
         org.apache.bcel.classfile.Method method) {
      super(parent, method);
   }

   public LocalMethod(sandmark.program.Class parent,
                      org.apache.bcel.generic.MethodGen mg,
                      sandmark.program.Object orig) {
      super(parent,mg.copy(parent.getName(),parent.getConstantPool()),orig);
   }

}

