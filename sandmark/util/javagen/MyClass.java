package sandmark.util.javagen;

public class MyClass extends java.lang.Object {
  public java.lang.Integer F1;
  public static java.lang.Integer F2;
  public static java.lang.Integer[] F3;
  public static java.lang.Integer F4;


  public static void M1 () {
     java.lang.Integer L1 = new java.lang.Integer(55);
     java.lang.Integer L2 = new java.lang.Integer(66);
     java.lang.Integer L3 = (java.lang.Integer)L1;
     java.lang.Integer L4 = (L1 != null)?L2:L3;
     if (L1 != null) {
        java.lang.Integer L5 = new java.lang.Integer(77);
     };
     java.lang.System.out.println(L1.toString());
     MyClass.F2 = L2;
     java.lang.System.out.println(MyClass.F2.toString());
     MyClass L7 = new MyClass();
     L7.F1 = L1;
     java.lang.System.out.println(L7.F1.toString());
     MyClass.F3 = new java.lang.Integer[10];
     MyClass.F3[3] = new java.lang.Integer(88);
     java.lang.System.out.println(MyClass.F3[3].toString());
     try {
        java.lang.System.out.println(MyClass.F3[3].toString());
        java.lang.System.out.println(MyClass.F4.toString());
     } catch (java.lang.Exception ex) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + ex );
    };
  }

   static {
     MyClass.F4 = new java.lang.Integer(77);
   }
}

