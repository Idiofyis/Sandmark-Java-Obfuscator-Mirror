package sandmark.program;



/**
 * Represents a modifiable field within a class or interface.
 *
 * <P> Modification methods in this class automatically call the
 * {@link sandmark.program.Object#mark() mark} method
 * to register their changes.
 *
 * @see sandmark.program.Field
 */

public class LocalField extends sandmark.program.Field {



   /**
    * Constructs a LocalField and adds it to a class.
    * The arguments mimic a BCEL FieldGen constructor.
    */
   public LocalField(sandmark.program.Class parent, int access_flags,
         org.apache.bcel.generic.Type type, String name) {
      super(parent, new org.apache.bcel.generic.FieldGen(
         access_flags, type, name, parent.getConstantPool()).getField());
   }



   /**
    * Constructs a LocalField from a BCEL Field and adds it to a class.
    */
   public LocalField(
         sandmark.program.Class parent, org.apache.bcel.classfile.Field f) {
      super(parent, f);
   }



}

