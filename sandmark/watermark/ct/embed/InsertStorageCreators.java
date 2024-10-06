package sandmark.watermark.ct.embed;

/**
 * We need some kind of structure to store handles to
 * watermark graph components. We can use arrays,
 * vectors, hashtables, etc. At some point these
 * structures need to be created. This class is
 * in charge of inserting code for creating these
 * storage structures.
 */

public class InsertStorageCreators {

    sandmark.util.ConfigProperties props = null;
sandmark.program.Application app = null;
String watermarkClassName;
String[][] storageCreators;
sandmark.watermark.ct.trace.callforest.Node rootNode;
//-----------------------------------------------------------
//               Constructor and Main Routine
//-----------------------------------------------------------

/**
 * We need some kind of structure to store handles to
 * watermark graph components. We can use arrays,
 * vectors, hashtables, etc. At some point these
 * structures need to be created. This class is
 * in charge of inserting code for creating these
 * storage structures.
 */
public InsertStorageCreators(
   sandmark.program.Application app,
   sandmark.util.ConfigProperties props,
   String[][] storageCreators,
   sandmark.watermark.ct.trace.callforest.Node domNode){
   this.props = props;
   this.storageCreators = storageCreators;
   this.app = app;
   this.rootNode=domNode;
   watermarkClassName = props.getProperty("DWM_CT_Encode_ClassName");
}

/*************************************************************************/

/**
 * Return the location in the code where we should insert the calls
 * to Watermark.CreateStorage_X().
 * Rather than inserting the calls at the exact bytecode location we
 * go to location 0, the beginning of the method. This avoids the
 * unpleasant situation
 * <PRE>
 *     main() {
 *        P(10);
 *     }
 * </PRE>
 * where we might insert the calls between 'push 10' and 'call P'.
 */
sandmark.util.ByteCodeLocation getCallForestRoot() {
   //    System.out.println("rootNode======"+rootNode);
    sandmark.util.StackFrame rootFrame = rootNode.getFrame();
    sandmark.util.ByteCodeLocation rootLocation = rootFrame.getLocation();
    return new sandmark.util.ByteCodeLocation
		(rootLocation.getMethod(),rootLocation.getLineNumber(),0);
}

/**
 * Insert calls to Watermark.CreateStorage_X(). The method
 * <PRE>
 *    void main() {
 *       P();
 *    }
 * </PRE>
 * should turn into
 * <PRE>
 *    void main() {
 *       java.util.Hashtable sm$hash = Watermark.CreateStorage_sm$hash();
 *       java.util.Vector sm$vector = Watermark.CreateStorage_sm$vector();
 *       P();
 *    }
 * </PRE>
 * If we're passing storage containers in formal parameters, this will
 * eventually turn into
 * <PRE>
 *    void main() {
 *       java.util.Hashtable sm$hash = Watermark.CreateStorage_sm$hash();
 *       java.util.Vector sm$vector = Watermark.CreateStorage_sm$vector();
 *       P(sm$hash,sm$vector);
 *    }
 * storageCreators[][] is an array of quadruples:
 *   {methodName, returnType, localName, GLOBAL/FORMAL}
 * for example
 *   {"CreateStorage_sm$hash", "java.util.Hashtable", "sm$hash", "FORMAL"}
 * </PRE>
 *
 * Rather than inserting the calls at the exact bytecode location we
 * go to location 0, the beginning of the method. This avoids the
 * unpleasant situation
 * <PRE>
 *     main() {
 *        P(10);
 *     }
 * </PRE>
 * where we might insert the calls between 'push 10' and 'call P'.
 *
 * This is the main entry point to this class.
 */
public void insert() throws Exception {
    sandmark.util.ByteCodeLocation rootLocation = getCallForestRoot();
    //System.out.println("createStorage:1:rootLocation=" + rootLocation);

    sandmark.program.Class ec = app.getClass(rootLocation.getMethod().getClassName());
    sandmark.program.Method mg = ec.getMethod(
       rootLocation.getMethod().getName(),
       rootLocation.getMethod().getSignature());

    org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
    org.apache.bcel.generic.InstructionHandle ih = il.findHandle((int)rootLocation.getCodeIndex());

    org.apache.bcel.generic.InstructionList instrs =
       new org.apache.bcel.generic.InstructionList();

   for(int i=0; i<storageCreators.length; i++) {
       String methodName = storageCreators[i][0];
       String returnType = storageCreators[i][1];
       String localName = storageCreators[i][2];
       boolean isGlobal = storageCreators[i][3].equals("GLOBAL");
       org.apache.bcel.generic.InstructionList call =
           createStorageCreatorCall(methodName, returnType, localName, ec.getConstantPool(), mg);
       instrs.append(call);
   }

   il.insert(ih, instrs);
   mg.setMaxStack();
   mg.mark();
}


/**
 * Return a list of instructions that invoke the method 'methodName',
 * whose signature is "()returnType". Store the resulting storage
 * container in local variable 'localName'.
 * @param methodName name of method to be called
 * @param returnType return type of method to be called
 * @param localName  name of local to store into
 * @param cp         constant pool
 * @param mg         method to be edited
 */
org.apache.bcel.generic.InstructionList createStorageCreatorCall(
   String methodName,
   String returnType,
   String localName,
   org.apache.bcel.generic.ConstantPoolGen cp,
   org.apache.bcel.generic.MethodGen mg) {
   String sig = "()" + org.apache.bcel.classfile.Utility.getSignature(returnType);
   //   System.out.println("insertStorageCreatorCall:sig=" + sig);

   org.apache.bcel.generic.InstructionList instrs =
      new org.apache.bcel.generic.InstructionList();

   org.apache.bcel.generic.Type T =
      sandmark.util.javagen.Java.typeToByteCode(returnType);

   org.apache.bcel.generic.LocalVariableGen lg =
      mg.addLocalVariable(localName, T, null, null);
   int localIndex = lg.getIndex();
   //  mg.setMaxLocals(mg.getMaxLocals()+1);

   int methodRef = cp.addMethodref(watermarkClassName, methodName, sig);
   org.apache.bcel.generic.INVOKESTATIC call =
      new org.apache.bcel.generic.INVOKESTATIC(methodRef);

   org.apache.bcel.generic.Instruction store =
      new org.apache.bcel.generic.ASTORE(localIndex);

   instrs.append(call);
   org.apache.bcel.generic.InstructionHandle start = instrs.append(store);
   lg.setStart(start);

   return instrs;
}

org.apache.bcel.generic.InstructionList createStorageCreatorCall(
   String methodName,
   String returnType,
   String localName,
   org.apache.bcel.generic.ConstantPoolGen cp,
   sandmark.program.Method mg) {
   String sig = "()" + org.apache.bcel.classfile.Utility.getSignature(returnType);
   //System.out.println("insertStorageCreatorCall:sig=" + sig);

   org.apache.bcel.generic.InstructionList instrs =
      new org.apache.bcel.generic.InstructionList();

   org.apache.bcel.generic.Type T =
      sandmark.util.javagen.Java.typeToByteCode(returnType);
   //System.out.println("insertStorageCreatorCall:1:getMaxLocals=" + mg.getMaxLocals());
   org.apache.bcel.generic.LocalVariableGen lg =
      mg.addLocalVariable(localName, T, null, null);
   int localIndex = lg.getIndex();
   //System.out.println("insertStorageCreatorCall:2:localIndex=" + localIndex);
   //System.out.println("insertStorageCreatorCall:3:getMaxLocals=" + mg.getMaxLocals());
   //  mg.setMaxLocals(mg.getMaxLocals()+1);

   int methodRef = cp.addMethodref(watermarkClassName, methodName, sig);
   org.apache.bcel.generic.INVOKESTATIC call =
      new org.apache.bcel.generic.INVOKESTATIC(methodRef);

   org.apache.bcel.generic.Instruction store =
      new org.apache.bcel.generic.ASTORE(localIndex);

   instrs.append(call);
   org.apache.bcel.generic.InstructionHandle start = instrs.append(store);
   lg.setStart(start);

   return instrs;
}

}








