package sandmark.program;

/**
 * Wraps the ConstantPoolGen class in order to intercept
 * method calls and mark the enclosing class as dirty.
 */

class LocalCPG extends ConstantPoolGen {

   /**
    * Constructs a Sandmark LocalCPG from a BCEL ConstantPool.
    */
   LocalCPG(sandmark.program.Class c,
         org.apache.bcel.classfile.ConstantPool cp) {
      super(c, cp);
   }

}

