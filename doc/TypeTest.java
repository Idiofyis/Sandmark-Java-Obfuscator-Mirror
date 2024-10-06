/*
 * Compile and run like this:
 *    javac -classpath .:../../smextern/BCEL.jar TypeTest.java
 *    java -classpath .:../../smextern/BCEL.jar TypeTest
 */

public class TypeTest {
    public static org.apache.bcel.generic.Type typeToByteCode(String type) {
     if (type.equals("void")) 
        return org.apache.bcel.generic.Type.VOID;
     else {
        String S = org.apache.bcel.classfile.Utility.getSignature(type);
        return org.apache.bcel.generic.Type.getType(S);
     }

    public static void test0() {
       String S = "[Ljava/lang/Object;";
       System.out.println(S);
       org.apache.bcel.generic.Type T =
          org.apache.bcel.generic.Type.getType(S);
       System.out.println(T);
       System.out.println("-----------------------------------");
    }

    public static void test1() {
       String type = "java.lang.Object[]";
       System.out.println(type);
       String S = org.apache.bcel.classfile.Utility.getSignature(type);
       System.out.println(S);
       System.out.println("-----------------------------------");
    }

     public static void test2() {
       String S = "(Ljava/lang/String;I)V;";
       System.out.println(S);
       org.apache.bcel.generic.Type[] arg_types =
          org.apache.bcel.generic.Type.getArgumentTypes(S);
       org.apache.bcel.generic.Type return_type =
          org.apache.bcel.generic.Type.getReturnType(S);
       String M = org.apache.bcel.generic.Type.getMethodSignature(
                     return_type, arg_types);
       System.out.println(M);
       System.out.println("-----------------------------------");
    }

   public static void test3() {
       org.apache.bcel.generic.Type T =
          org.apache.bcel.generic.Type.STRINGBUFFER;
       String M = T.getSignature();
       System.out.println(M);
       System.out.println("-----------------------------------");
    }

   public static void test4() {
      org.apache.bcel.generic.Type return_type = 
         org.apache.bcel.generic.Type.VOID;
      org.apache.bcel.generic.Type[] arg_types   = 
         new org.apache.bcel.generic.Type[] { 
            new org.apache.bcel.generic.ArrayType(
               org.apache.bcel.generic.Type.STRING, 1)
         };
    }

    public static void main (String args[]) {
	test0();
	test1();
        test2();
        test3();
        test4();
   }
}

