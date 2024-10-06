package sandmark.util.javagen;

public class Test {

    static sandmark.util.javagen.List stats = new sandmark.util.javagen.List();
    static sandmark.util.javagen.LiteralInt Int55 = new sandmark.util.javagen.LiteralInt(55);
    static sandmark.util.javagen.LiteralInt Int66 = new sandmark.util.javagen.LiteralInt(66);
    static sandmark.util.javagen.LiteralInt Int77 = new sandmark.util.javagen.LiteralInt(77);
    static sandmark.util.javagen.LiteralInt Int88 = new sandmark.util.javagen.LiteralInt(88);
    static sandmark.util.javagen.LiteralInt Int99 = new sandmark.util.javagen.LiteralInt(99);

    static String[] dynamicAttributes = {"public"};
    static String[] staticAttributes = {"public", "static"};

    //==================================================================
    public static sandmark.util.javagen.VirtualCall printInteger(
       sandmark.util.javagen.Expression expr) {
      // java.lang.System.out.println(<expr>.toString());
      sandmark.util.javagen.VirtualCall C =
	  new sandmark.util.javagen.VirtualCall(
             new sandmark.util.javagen.StaticRef("java.lang.System", "out", "java.io.PrintStream"),
             "java.io.PrintStream", 
             "println", 
             "(Ljava/lang/String;)V", 
              new sandmark.util.javagen.VirtualFunCall(
                expr,
                "java.lang.Integer", 
                "toString", 
                "()Ljava/lang/String;")
      );
      return C;
   }

    //==================================================================
    public static void dumpClass(
      sandmark.util.javagen.List fields,
      sandmark.util.javagen.List methods) {
      sandmark.util.javagen.Class C =
	  new sandmark.util.javagen.Class("java.lang.Object","MyClass",fields,methods);

      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
      System.out.println("++++++++++++++++++++++++ java.Java ++++++++++++++++++++++++++");
      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
      System.out.println(C.toString());
      System.out.println("----------------------------------------------");
      System.out.println("Dumping to MyClass.class");
      System.out.println("Examine using: 'javap -l -c -verbose MyClass'");
      System.out.println("Execute using: 'java MyClassMain'");
      System.out.println("----------------------------------------------");

      org.apache.bcel.generic.ClassGen cg = C.toByteCode();
      try {
         cg.getJavaClass().dump("MyClass.class");
      } catch (Exception e) {
        System.out.println("Exception: " + e.toString() + ":" + e.getMessage());
      }
    }

    //==================================================================
    static void makeLocals() {
      // Local variables.
      // java.lang.Integer L1 = new java.lang.Integer(55);
      sandmark.util.javagen.Local L1 = 
         new sandmark.util.javagen.Local("L1","java.lang.Integer",
             new sandmark.util.javagen.New("java.lang.Integer",Int55));
      stats.cons(L1);

      // java.lang.Integer L2 = new java.lang.Integer(66);
      sandmark.util.javagen.Local L2 = 
         new sandmark.util.javagen.Local("L2","java.lang.Integer",
             new sandmark.util.javagen.New("java.lang.Integer",Int66));
      stats.cons(L2);

      // java.lang.Integer L3 = (java.lang.Integer)L1;
      sandmark.util.javagen.Local L3 = 
         new sandmark.util.javagen.Local("L3","java.lang.Integer",
             new sandmark.util.javagen.Cast("java.lang.Integer",
                new sandmark.util.javagen.LocalRef("L1","java.lang.Integer")));
      stats.cons(L3);
   }

    //==================================================================
    static void makeCall1() {
      // Virtual function calls.
      // java.lang.System.out.println(L1.toString());
      sandmark.util.javagen.VirtualCall C1 =
         printInteger(new sandmark.util.javagen.LocalRef("L1","java.lang.Integer"));
      stats.cons(C1);
    }

    //==================================================================
    static void makeBranches() {
      // Branches.
      // java.lang.Integer L4 = (L1 != null)?L2:L3;
      sandmark.util.javagen.Local L4 = 
          new sandmark.util.javagen.Local("L4","java.lang.Integer",
             new sandmark.util.javagen.CondNotNullExpr(
                new sandmark.util.javagen.LocalRef("L1","java.lang.Integer"), 
                new sandmark.util.javagen.LocalRef("L2","java.lang.Integer"), 
                new sandmark.util.javagen.LocalRef("L3","java.lang.Integer"), 
                "java.lang.Integer"));
      stats.cons(L4);

      // if (L1 != null) 
      //    java.lang.Integer L5 = new java.lang.Integer(77);
      sandmark.util.javagen.IfNotNull I1 =
          new sandmark.util.javagen.IfNotNull(
             new sandmark.util.javagen.LocalRef("L1","java.lang.Integer"),
             new sandmark.util.javagen.Local("L5","java.lang.Integer",
                new sandmark.util.javagen.New("java.lang.Integer",Int77)));
      stats.cons(I1);
   }

   //==================================================================
   static sandmark.util.javagen.List makeFields() {
      // Declare and reference class fields.
      //  public java.lang.Integer F1;
      //  public static java.lang.Integer F2;
      sandmark.util.javagen.Field F1 =
	  new sandmark.util.javagen.Field(
	 "F1",
         "java.lang.Integer",
         dynamicAttributes);
      sandmark.util.javagen.Field F2 =
	  new sandmark.util.javagen.Field(
	 "F2",
         "java.lang.Integer",
         staticAttributes);
      sandmark.util.javagen.Field F3 =
	  new sandmark.util.javagen.Field(
	 "F3",
         "java.lang.Integer[]",
         staticAttributes);
      sandmark.util.javagen.Field F4 =
	  new sandmark.util.javagen.Field(
	 "F4",
         "java.lang.Integer",
         staticAttributes);
      return new sandmark.util.javagen.List(F1,F2,F3,F4);
    }

   //==================================================================
   static void makeAssignStaticField() {
      // MyClass.F2 = L2;
      sandmark.util.javagen.AssignStatic A1 =
          new sandmark.util.javagen.AssignStatic(
          "MyClass", 
          "F2", 
          "java.lang.Integer",
          new sandmark.util.javagen.LocalRef("L2","java.lang.Integer"));
      stats.cons(A1);
   }

   //==================================================================
   static void makeCall2() {
      // java.lang.System.out.println(MyClass.F2.toString());
      sandmark.util.javagen.VirtualCall C2 =
         printInteger(new sandmark.util.javagen.StaticRef("MyClass","F2","java.lang.Integer"));
   }

   //==================================================================
   static void makeNew() {
      // MyClass L7 = new MyClass();
      sandmark.util.javagen.Local L7 = 
         new sandmark.util.javagen.Local("L7","MyClass",
             new sandmark.util.javagen.New("MyClass"));
      stats.cons(L7);
   }

   //==================================================================
   static void makeAssignDynamicField() {
      // L7.F1 = L1;
      sandmark.util.javagen.AssignField A =
          new sandmark.util.javagen.AssignField(
             new sandmark.util.javagen.LocalRef("L7","MyClass"),
             "MyClass",
             "F1", 
             "java.lang.Integer",
             new sandmark.util.javagen.LocalRef("L1","java.lang.Integer"));
      stats.cons(A);
   }

   //==================================================================
   static void makeCall3() {
      // java.lang.System.out.println(L7.F1.toString());
      sandmark.util.javagen.VirtualCall C =
         printInteger(
            new sandmark.util.javagen.FieldRef(
               new sandmark.util.javagen.LocalRef("L7","java.lang.Integer"),
               "MyClass",
               "F1",
               "java.lang.Integer"));
      stats.cons(C);
   }

   //==================================================================
   static void makeCall4() {
      // java.lang.System.out.println(MyClass.F2.toString());
      sandmark.util.javagen.VirtualCall C =
         printInteger(
		 new sandmark.util.javagen.StaticRef("MyClass", "F2", "java.lang.Integer"));
      stats.cons(C);
   }

   //==================================================================
   static void makeNewArray() {
      // MyClass.F3 = new java.lang.Integer[10];
      sandmark.util.javagen.AssignStatic A =
          new sandmark.util.javagen.AssignStatic(
          "MyClass", 
          "F3", 
          "java.lang.Integer[]",
          new sandmark.util.javagen.NewArray("java.lang.Integer",10));
      stats.cons(A);
   }

   //==================================================================
   static void makeAssignIndex() {
      // MyClass.F3[3] = new java.lang.Integer(88);
      sandmark.util.javagen.AssignIndex A =
          new sandmark.util.javagen.AssignIndex(
	     new sandmark.util.javagen.StaticRef("MyClass", "F3", "java.lang.Integer[]"),
             new sandmark.util.javagen.LiteralInt(3),
	     new sandmark.util.javagen.New("java.lang.Integer",Int88));
      stats.cons(A);
   }

   //==================================================================
   static void makeCall5() {
      // java.lang.System.out.println(MyClass.F3[3].toString());
      sandmark.util.javagen.VirtualCall C =
         printInteger(
	     new sandmark.util.javagen.LoadIndex(
	        new sandmark.util.javagen.StaticRef("MyClass", "F3", "java.lang.Integer[]"),
                new sandmark.util.javagen.LiteralInt(3),
                "java.lang.Integer"));
      stats.cons(C);
   }

   //==================================================================
   static void makeStaticCall1() {
      // java.lang.System.out.println(MyClass.F3[3].toString());
      sandmark.util.javagen.StaticCall C =
          new sandmark.util.javagen.StaticCall("MyClass", "M2", "()V");
      stats.cons(C);
   }

   //==================================================================
   static void makeTry() {
      //  try {
      //    java.lang.System.out.println(MyClass.F3[3].toString());
      //    java.lang.System.out.println(MyClass.F2.toString());
      //  } catch (Exception e) {
      //  }
      sandmark.util.javagen.VirtualCall C1 =
         printInteger(
	     new sandmark.util.javagen.LoadIndex(
	        new sandmark.util.javagen.StaticRef("MyClass", "F3", "java.lang.Integer[]"),
                new sandmark.util.javagen.LiteralInt(3),
                "java.lang.Integer"));
      sandmark.util.javagen.VirtualCall C2 =
         printInteger(new sandmark.util.javagen.StaticRef("MyClass","F4","java.lang.Integer"));
      sandmark.util.javagen.List body = new sandmark.util.javagen.List(C1,C2);

      sandmark.util.javagen.List Catch = new sandmark.util.javagen.List();
      sandmark.util.javagen.Try T = 
	  new sandmark.util.javagen.Try(body, "java.lang.Exception", Catch);
      stats.cons(T);
   }

   static void makeStaticInitializer() {
      sandmark.util.javagen.AssignStatic A =
          new sandmark.util.javagen.AssignStatic(
          "MyClass", 
          "F4", 
          "java.lang.Integer",
          new sandmark.util.javagen.New("java.lang.Integer",Int77)
      );
      sandmark.util.javagen.Class.addStaticStat(A);
   }

   public static void main (String[] sargs) {
      makeLocals();
      makeBranches();
      makeCall1();
      sandmark.util.javagen.List fields = makeFields();
      makeAssignStaticField();
      makeCall2();
      makeCall4();
      makeNew();
      makeAssignDynamicField();
      makeCall3();
      makeNewArray();
      makeAssignIndex();
      makeCall5();
      makeTry();
      makeStaticInitializer();
      makeStaticCall1();

      sandmark.util.javagen.List formals =  
         new sandmark.util.javagen.List();

      sandmark.util.javagen.Method M1 =
        new sandmark.util.javagen.Method ("M1", "void", 
				    staticAttributes, formals, stats);

      sandmark.util.javagen.List M2stats =  
         new sandmark.util.javagen.List(
            printInteger(
               new sandmark.util.javagen.New("java.lang.Integer",Int99)));
      sandmark.util.javagen.Method M2 =
        new sandmark.util.javagen.Method ("M2", "void", 
				    staticAttributes, formals, M2stats);

      sandmark.util.javagen.List methods = new sandmark.util.javagen.List();
      methods.cons(M1,M2);

      dumpClass(fields,methods);
   }
}



