/*
 * Compile and run like this:
 *    javac -classpath .:../../smextern/BCEL.jar:../../smextern/JustIce.jar CFGTest.java
 *    java -classpath .:../../smextern/BCEL.jar:../../smextern/JustIce.jar CFGTest
 */

public class CFGTest {

public static void test0() {
   int access_flags = org.apache.bcel.Constants.ACC_PUBLIC;
   String class_name = "MyClass";
   String file_name = "MyClass.java";
   String super_class_name = "java.lang.Object";
   String[] interfaces = {};

   org.apache.bcel.generic.ClassGen cg = 
      new org.apache.bcel.generic.ClassGen(
         class_name, super_class_name, file_name,
         access_flags, interfaces);
   org.apache.bcel.generic.ConstantPoolGen cp = cg.getConstantPool();

   int method_access_flags = 
      org.apache.bcel.Constants.ACC_PUBLIC | 
      org.apache.bcel.Constants.ACC_STATIC;

   org.apache.bcel.generic.Type return_type = 
      org.apache.bcel.generic.Type.VOID;

   org.apache.bcel.generic.Type[] arg_types = 
      org.apache.bcel.generic.Type.NO_ARGS;
   String[] arg_names = {};

   String method_name = "method1";

   org.apache.bcel.generic.InstructionList il = 
      new org.apache.bcel.generic.InstructionList();
   il.append(org.apache.bcel.generic.InstructionConstants.RETURN);

   org.apache.bcel.generic.MethodGen mg = 
      new org.apache.bcel.generic.MethodGen(
         method_access_flags, return_type,  arg_types,
         arg_names, method_name, class_name, il, cp);

   mg.setMaxStack();
   cg.addMethod(mg.getMethod());

   org.apache.bcel.verifier.structurals.ControlFlowGraph cfg =
       new org.apache.bcel.verifier.structurals.ControlFlowGraph(mg);
  
   System.out.println(cfg);
   System.out.println("-----------------------------------");
}


    public static void main (String args[]) {
	test0();
   }
}

