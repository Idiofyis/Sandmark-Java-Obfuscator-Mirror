package sandmark.watermark.ct.trace.callforest;

public class Test {

public static class Stats extends sandmark.newstatistics.Stats {

   public Stats (){ super(new sandmark.program.Application()); }

    public java.util.Hashtable getByteCodeUsage(
       String className, 
       String methodName) {
      java.util.Hashtable usage = new java.util.Hashtable ();

      int offset = 0;
      if (methodName.equals("P"))
         offset = 0;
      else if (methodName.equals("Q"))
         offset = -5;
      else if (methodName.equals("main"))
         offset = -10;

      usage.put("aload",         new java.lang.Integer(java.lang.Math.max(0,29+offset)));
      usage.put("putfield",      new java.lang.Integer(java.lang.Math.max(0,10+offset)));
      usage.put("astore",        new java.lang.Integer(java.lang.Math.max(0,8+offset)));
      usage.put("new",           new java.lang.Integer(java.lang.Math.max(0,7+offset)));
      usage.put("invokespecial", new java.lang.Integer(java.lang.Math.max(0,7+offset)));
      usage.put("dup",           new java.lang.Integer(java.lang.Math.max(0,7+offset)));
      usage.put("getstatic",     new java.lang.Integer(java.lang.Math.max(0,6+offset)));
      usage.put("invokevirtual", new java.lang.Integer(java.lang.Math.max(0,6+offset)));
      usage.put("iconst",        new java.lang.Integer(java.lang.Math.max(0,6+offset)));
      usage.put("ifnull",        new java.lang.Integer(java.lang.Math.max(0,3+offset)));
      usage.put("pop",           new java.lang.Integer(java.lang.Math.max(0,2+offset)));
      usage.put("return",        new java.lang.Integer(java.lang.Math.max(0,2+offset)));
      usage.put("getfield",      new java.lang.Integer(java.lang.Math.max(0,2+offset)));
      usage.put("checkcast",     new java.lang.Integer(java.lang.Math.max(0,1+offset)));
      usage.put("goto",          new java.lang.Integer(java.lang.Math.max(0,1+offset)));
      return usage;
   }

    public java.util.List getByteCode(
       String className, 
       String methodName) {
     java.util.Vector result = new java.util.Vector();
     result.setSize(100);
     return result;
    }
}

public static class ClassHierarchy extends sandmark.analysis.classhierarchy.ClassHierarchy{
   public ClassHierarchy()  { }

   public boolean methodRenameOK (
      sandmark.util.MethodID origMethod,
      sandmark.util.MethodID newMethod) {
      if (origMethod.getName().equals("Q"))
         return false;
      return true;
   }
}

static sandmark.util.StackFrame mkFrame(
   String name,
   String sig,
   String className,
   long lineNumber,
   long codeIndex,
   long threadID,
   long frameID) {
      return new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(
            new sandmark.util.MethodID(name, sig, className), 
            lineNumber, 
            codeIndex
         ), 
         threadID, frameID
      );
}

static sandmark.watermark.ct.trace.TracePoint mkTracePoint(
   String data,
   String name,
   String sig,
   String className,
   long lineNumber,
   long codeIndex,
   sandmark.util.StackFrame[] frames) {
      return new sandmark.watermark.ct.trace.TracePoint(
         data,
         new sandmark.util.ByteCodeLocation(
            new sandmark.util.MethodID(name, sig, className), 
            lineNumber, 
            codeIndex
         ), 
         frames
      );
}

/*****************************************************************************/
/*                               Test 1                                      */
/*****************************************************************************/
/**
 * Build the dynamic call-graph for this simple method:
 * <PRE>
 * public class Simple1 {
 * 
 *    static void P() {
 *       sandmark.watermark.ct.trace.Annotator.sm$mark();
 *    }
 * 
 *    public static void main(String args[]) {
 *       P();
 *    }
 * }
 * </PRE>
 */
static void test1(
   ClassHierarchy classHierarchy,
   Stats stats,
   sandmark.util.ConfigProperties props) {
   String anno = "sandmark.watermark.ct.trace.Annotator";
   String sig = "(Ljava/lang/String;Ljava/lang/String;)V";
   String mainSig = "(Ljava/lang/String];)V";

   sandmark.util.MethodID Pid = new sandmark.util.MethodID("P", sig, "x");
   sandmark.util.ByteCodeLocation P = new sandmark.util.ByteCodeLocation(Pid, 4, 11);
   sandmark.util.MethodID mainID = new sandmark.util.MethodID("main", mainSig, "x");
   sandmark.util.MethodID markID = new sandmark.util.MethodID("sm$mark", "()V", anno);

   sandmark.util.StackFrame[] S1 = {
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(markID, 11, 4), 1, 2),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(Pid, 4, 11), 1, 1),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(mainID, 8, 11), 1, 0)
   };

   sandmark.watermark.ct.trace.TracePoint TP1 = 
      new sandmark.watermark.ct.trace.TracePoint("----", P, S1);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints = {TP1};

   sandmark.watermark.ct.trace.callforest.Forest f = 
      new sandmark.watermark.ct.trace.callforest.Forest(
         tracePoints, classHierarchy, stats, props);
   //f.view();
   System.out.println(f);
   try {
      System.out.println("Call-graph written to 'CallForest1.dot'");
      sandmark.util.Misc.writeToFile("CallForest1.dot", f.toDot()[0]);
   } catch (Exception ex) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + ex );
    }
   sandmark.watermark.ct.trace.callforest.PathGenerator paths = 
      new sandmark.watermark.ct.trace.callforest.PathGenerator(f.getForest(),100);
   System.out.println(paths.toString());
}

/*****************************************************************************/
/*                               Test 2                                      */
/*****************************************************************************/
/**
 * Build the dynamic call-graph for this simple method:
 * <PRE>
 * public class Simple6 {
 * 
 *    static void P() {
 *       sandmark.watermark.ct.trace.Annotator.sm$mark();
 *    }
 * 
 *    static void Q() {
 *       sandmark.watermark.ct.trace.Annotator.sm$mark();
 *    }
 * 
 *    public static void main(String args[]) {
 *        P();
 *        Q();
 *    }
 * }
 * </PRE>
 * <P>
 * @param classHierarchy the class hierarchy
 * @param stats          statistics
 * @param props          properties
 * @param okToAddParam   'false' if we should name one of the methods "Q",
 *                       meaning that its argument list cannot be changed.
 */
static void test2(
   ClassHierarchy classHierarchy,
   Stats stats,
   sandmark.util.ConfigProperties props,
   boolean okToAddParam) {

   String meth1 = "P";
   String meth2 = "Q";
   if (okToAddParam)
      meth2 = "Q2";

   String sig = "(Ljava/lang/String;Ljava/lang/String;)V";
   String anno = "sandmark.watermark.ct.trace.Annotator";
   String mainSig = "(Ljava/lang/String];)V";

   sandmark.util.MethodID meth1ID = new sandmark.util.MethodID(meth1, sig, "x");
   sandmark.util.MethodID meth2ID = new sandmark.util.MethodID(meth2, sig, "x");
   sandmark.util.MethodID mainID = new sandmark.util.MethodID("main", mainSig, "x");
   sandmark.util.MethodID markID = new sandmark.util.MethodID("sm$mark", "()V", anno);

   sandmark.util.StackFrame[] S1 = {
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(markID, 11, 4), 1, 2),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(meth1ID, 11, 4), 1, 1),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(mainID, 11, 12), 1, 0)
   };

   sandmark.util.StackFrame[] S2 = {
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(markID, 11, 4), 1, 4),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(meth2ID, 11, 8), 1, 3),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(mainID, 14, 13), 1, 0)
   };

   sandmark.watermark.ct.trace.TracePoint TP1 = 
      new sandmark.watermark.ct.trace.TracePoint("----", 
          new sandmark.util.ByteCodeLocation(meth1ID, 11, 4), S1);
   sandmark.watermark.ct.trace.TracePoint TP2 = 
      new sandmark.watermark.ct.trace.TracePoint("----", 
          new sandmark.util.ByteCodeLocation(meth2ID, 11, 8), S2);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints = {TP1, TP2};

   sandmark.watermark.ct.trace.callforest.Forest f = 
      new sandmark.watermark.ct.trace.callforest.Forest(
         tracePoints, classHierarchy, stats, props);

   //  System.out.println(f);
   try {
      String fn = "CallForest2" + okToAddParam + ".dot";
      System.out.println("Call-graph written to '" + fn + "'");
      sandmark.util.Misc.writeToFile(fn, f.toDot()[0]);
   } catch (Exception ex) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + ex );
    }
   sandmark.watermark.ct.trace.callforest.PathGenerator paths = 
      new sandmark.watermark.ct.trace.callforest.PathGenerator(f.getForest(),100);
   System.out.println(paths.toString());
}

/*****************************************************************************/
/*                               Test 3                                      */
/*****************************************************************************/
/**
 * Build the dynamic call-graph for this simple class:
 * <PRE>
 * class TTTApplication {
 *    void mark() {
 *       sm$mark();
 *    }
 * 
 *    void move() {
 *       sm$mark();
 *       mark();
 *    }
 * 
 *    void actionPerformed () {
 *       move()
 *    }
 * } 
 * </PRE>
 * <P>
 * These are the stack frames:
 * <PRE>
 * TRACEPT[1,LOCATION[METHOD[move,(I)V,TTTApplication], LINE=98, BC=36]]
 *    FRAME[LOCATION[METHOD[sm$mark,(J)V,sandmark.watermark.ct.trace.Annotator], LINE=59, BC=6], ID=78]
 *    FRAME[LOCATION[METHOD[move,(I)V,TTTApplication], LINE=98, BC=36], ID=67]
 *    FRAME[LOCATION[METHOD[actionPerformed,(Ljava/awt/event/ActionEvent;)V,TTTApplication$1], LINE=32, BC=20], ID=66]

 * TRACEPT[8,LOCATION[METHOD[mark,(I)V,TTTApplication], LINE=131, BC=49]]
 *    FRAME[LOCATION[METHOD[sm$mark,(J)V,sandmark.watermark.ct.trace.Annotator], LINE=59, BC=6], ID=80]
 *    FRAME[LOCATION[METHOD[mark,(I)V,TTTApplication], LINE=131, BC=49], ID=79]
 *    FRAME[LOCATION[METHOD[move,(I)V,TTTApplication], LINE=99, BC=41], ID=67]
 *    FRAME[LOCATION[METHOD[actionPerformed,(Ljava/awt/event/ActionEvent;)V,TTTApplication$1], LINE=32, BC=20], ID=66]
 * </PRE>
 */

static void test3(
   ClassHierarchy classHierarchy,
   Stats stats,
   sandmark.util.ConfigProperties props) {

   String anno = "sandmark.watermark.ct.trace.Annotator";

   sandmark.util.MethodID moveID = 
      new sandmark.util.MethodID("move", "(I)V", "TTTApplication");
   sandmark.util.MethodID SMmarkID = 
      new sandmark.util.MethodID("sm$mark", "(J)V", anno);
   sandmark.util.MethodID markID = 
      new sandmark.util.MethodID("mark", "(I)V", "TTTApplication");
   sandmark.util.MethodID actionPerformedID = 
      new sandmark.util.MethodID("actionPerformed", "(Ljava/awt/event/ActionEvent;)V", "TTTApplication");

   sandmark.util.StackFrame[] S1 = {
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(SMmarkID, 59, 6), 1, 78),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(moveID, 98, 36), 1, 67),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(actionPerformedID, 32, 20), 1, 66)
   };

   sandmark.util.StackFrame[] S2 = {
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(SMmarkID, 59, 6), 1, 80),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(markID, 131, 49), 1, 79),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(moveID, 99, 41), 1, 67),
      new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(actionPerformedID, 32, 20), 1, 66)
   };

   sandmark.watermark.ct.trace.TracePoint TP1 = 
      new sandmark.watermark.ct.trace.TracePoint(
         "----", new sandmark.util.ByteCodeLocation(moveID, 98, 36), S1);
   sandmark.watermark.ct.trace.TracePoint TP2 = 
      new sandmark.watermark.ct.trace.TracePoint(
         "----", new sandmark.util.ByteCodeLocation(markID, 131, 49), S2);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints = {TP1, TP2};

   sandmark.watermark.ct.trace.callforest.Forest f = 
      new sandmark.watermark.ct.trace.callforest.Forest(
         tracePoints, classHierarchy, stats, props);

   System.out.println(f);
   try {
      System.out.println("Call-graph written to 'CallForest3.dot'");
      sandmark.util.Misc.writeToFile("CallForest3.dot", f.toDot()[0]);
   } catch (Exception ex) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + ex );
    }
   sandmark.watermark.ct.trace.callforest.PathGenerator paths = 
      new sandmark.watermark.ct.trace.callforest.PathGenerator(f.getForest(),100);
   System.out.println(paths.toString());
}

/*****************************************************************************/
/*                               Test 4                                      */
/*****************************************************************************/
/**
 * Build the dynamic call-graph for this simple class:
 * <PRE>
 * public class SimpleE {
 * 
 *     static void K() {
 *       sandmark.watermark.ct.trace.Annotator.sm$mark();
 *    }
 * 
 *     static void L() {
 *        sandmark.watermark.ct.trace.Annotator.sm$mark();
 *     }
 * 
 *     static void M() {
 *        sandmark.watermark.ct.trace.Annotator.sm$mark();
 *     }
 * 
 *     static void N() {
 *        sandmark.watermark.ct.trace.Annotator.sm$mark();
 *     }
 * 
 *     static void O() {
 *        sandmark.watermark.ct.trace.Annotator.sm$mark();
 *        N();
 *     }
 * 
 *     static void P() {
 *        sandmark.watermark.ct.trace.Annotator.sm$mark();
 *        O();
 *     }
 * 
 *     static void W() {
 *        L();
 *        sandmark.watermark.ct.trace.Annotator.sm$mark();
 *        M();
 *     }
 * 
 *     static void R() {
 *        K();
 *     }
 * 
 *     static void S() {
 *        P();
 *        W();
 *        R();
 *     }
 * 
 *     public static void main(String args[]) {
 *           S();
 *     }
 * }
 * </PRE>
 * <P>
 * These are the stack frames:
 * <PRE>
 * TRACEPT[----,LOCATION[METHOD[P,()V,SimpleE,dynamic], LINE=25, BC=0]]
 *    FRAME[LOCATION[METHOD[sm$mark,()V,sandmark.watermark.ct.trace.Annotator,dynamic], LINE=45, BC=4], THRD=1, ID=3]
 *    FRAME[LOCATION[METHOD[P,()V,SimpleE,dynamic], LINE=25, BC=0], THRD=1, ID=2]
 *    FRAME[LOCATION[METHOD[S,()V,SimpleE,dynamic], LINE=40, BC=0], THRD=1, ID=1]
 *    FRAME[LOCATION[METHOD[main,([Ljava/lang/String;)V,SimpleE,dynamic], LINE=46, BC=0], THRD=1, ID=0]
 * 
 * TRACEPT[----,LOCATION[METHOD[O,()V,SimpleE,dynamic], LINE=20, BC=0]]
 *    FRAME[LOCATION[METHOD[sm$mark,()V,sandmark.watermark.ct.trace.Annotator,dynamic], LINE=45, BC=4], THRD=1, ID=5]
 *    FRAME[LOCATION[METHOD[O,()V,SimpleE,dynamic], LINE=20, BC=0], THRD=1, ID=4]
 *    FRAME[LOCATION[METHOD[P,()V,SimpleE,dynamic], LINE=26, BC=3], THRD=1, ID=2]
 *    FRAME[LOCATION[METHOD[S,()V,SimpleE,dynamic], LINE=40, BC=0], THRD=1, ID=1]
 *    FRAME[LOCATION[METHOD[main,([Ljava/lang/String;)V,SimpleE,dynamic], LINE=46, BC=0], THRD=1, ID=0]
 * 
 * TRACEPT[----,LOCATION[METHOD[N,()V,SimpleE,dynamic], LINE=16, BC=0]]
 *    FRAME[LOCATION[METHOD[sm$mark,()V,sandmark.watermark.ct.trace.Annotator,dynamic], LINE=45, BC=4], THRD=1, ID=7]
 *    FRAME[LOCATION[METHOD[N,()V,SimpleE,dynamic], LINE=16, BC=0], THRD=1, ID=6]
 *    FRAME[LOCATION[METHOD[O,()V,SimpleE,dynamic], LINE=21, BC=3], THRD=1, ID=4]
 *    FRAME[LOCATION[METHOD[P,()V,SimpleE,dynamic], LINE=26, BC=3], THRD=1, ID=2]
 *    FRAME[LOCATION[METHOD[S,()V,SimpleE,dynamic], LINE=40, BC=0], THRD=1, ID=1]
 *    FRAME[LOCATION[METHOD[main,([Ljava/lang/String;)V,SimpleE,dynamic], LINE=46, BC=0], THRD=1, ID=0]
 * 
 * TRACEPT[----,LOCATION[METHOD[L,()V,SimpleE,dynamic], LINE=8, BC=0]]
 *    FRAME[LOCATION[METHOD[sm$mark,()V,sandmark.watermark.ct.trace.Annotator,dynamic], LINE=45, BC=4], THRD=1, ID=10]
 *    FRAME[LOCATION[METHOD[L,()V,SimpleE,dynamic], LINE=8, BC=0], THRD=1, ID=9]
 *    FRAME[LOCATION[METHOD[W,()V,SimpleE,dynamic], LINE=30, BC=0], THRD=1, ID=8]
 *    FRAME[LOCATION[METHOD[S,()V,SimpleE,dynamic], LINE=41, BC=3], THRD=1, ID=1]
 *    FRAME[LOCATION[METHOD[main,([Ljava/lang/String;)V,SimpleE,dynamic], LINE=46, BC=0], THRD=1, ID=0]
 * 
 * TRACEPT[----,LOCATION[METHOD[W,()V,SimpleE,dynamic], LINE=31, BC=3]]
 *    FRAME[LOCATION[METHOD[sm$mark,()V,sandmark.watermark.ct.trace.Annotator,dynamic], LINE=45, BC=4], THRD=1, ID=11]
 *    FRAME[LOCATION[METHOD[W,()V,SimpleE,dynamic], LINE=31, BC=3], THRD=1, ID=8]
 *    FRAME[LOCATION[METHOD[S,()V,SimpleE,dynamic], LINE=41, BC=3], THRD=1, ID=1]
 *    FRAME[LOCATION[METHOD[main,([Ljava/lang/String;)V,SimpleE,dynamic], LINE=46, BC=0], THRD=1, ID=0]
 * 
 * TRACEPT[----,LOCATION[METHOD[M,()V,SimpleE,dynamic], LINE=12, BC=0]]
 *    FRAME[LOCATION[METHOD[sm$mark,()V,sandmark.watermark.ct.trace.Annotator,dynamic], LINE=45, BC=4], THRD=1, ID=13]
 *    FRAME[LOCATION[METHOD[M,()V,SimpleE,dynamic], LINE=12, BC=0], THRD=1, ID=12]
 *    FRAME[LOCATION[METHOD[W,()V,SimpleE,dynamic], LINE=32, BC=6], THRD=1, ID=8]
 *    FRAME[LOCATION[METHOD[S,()V,SimpleE,dynamic], LINE=41, BC=3], THRD=1, ID=1]
 *    FRAME[LOCATION[METHOD[main,([Ljava/lang/String;)V,SimpleE,dynamic], LINE=46, BC=0], THRD=1, ID=0]
 * 
 * TRACEPT[----,LOCATION[METHOD[K,()V,SimpleE,dynamic], LINE=4, BC=0]]
 *    FRAME[LOCATION[METHOD[sm$mark,()V,sandmark.watermark.ct.trace.Annotator,dynamic], LINE=45, BC=4], THRD=1, ID=16]
 *    FRAME[LOCATION[METHOD[K,()V,SimpleE,dynamic], LINE=4, BC=0], THRD=1, ID=15]
 *    FRAME[LOCATION[METHOD[R,()V,SimpleE,dynamic], LINE=36, BC=0], THRD=1, ID=14]
 *    FRAME[LOCATION[METHOD[S,()V,SimpleE,dynamic], LINE=42, BC=6], THRD=1, ID=1]
 *    FRAME[LOCATION[METHOD[main,([Ljava/lang/String;)V,SimpleE,dynamic], LINE=46, BC=0], THRD=1, ID=0]
 * </PRE>
 */

static void test4(
   ClassHierarchy classHierarchy,
   Stats stats,
   sandmark.util.ConfigProperties props) {

   sandmark.util.StackFrame[] S1 = {
     mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,1,3),
     mkFrame("P","()V","E",25,0,1,2),
     mkFrame("S","()V","E",40,0,1,1),
     mkFrame("main","([Ljava/lang/String;)V","E",46,0,1,0)
  };
  sandmark.watermark.ct.trace.TracePoint TP1 = mkTracePoint("----","P","()V","E",25,0,S1);

   sandmark.util.StackFrame[] S2 = {
     mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,1,5),
     mkFrame("O","()V","E",20,0,1,4),
     mkFrame("P","()V","E",26,3,1,2),
     mkFrame("S","()V","E",40,0,1,1),
     mkFrame("main","([Ljava/lang/String;)V","E",46,0,1,0)
  };
  sandmark.watermark.ct.trace.TracePoint TP2 = mkTracePoint("----","O","()V","E",20,0,S2);
  
   sandmark.util.StackFrame[] S3 = {
     mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,1,7),
     mkFrame("N","()V","E",16,0,1,6),
     mkFrame("O","()V","E",21,3,1,4),
     mkFrame("P","()V","E",26,3,1,2),
     mkFrame("S","()V","E",40,0,1,1),
     mkFrame("main","([Ljava/lang/String;)V","E",46,0,1,0)
  };
  sandmark.watermark.ct.trace.TracePoint TP3 = mkTracePoint("----","N","()V","E",16,0,S3);
  
   sandmark.util.StackFrame[] S4 = {
     mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,1,10),
     mkFrame("L","()V","E",8,0,1,9),
     mkFrame("W","()V","E",30,0,1,8),
     mkFrame("S","()V","E",41,3,1,1),
     mkFrame("main","([Ljava/lang/String;)V","E",46,0,1,0)
  };
  sandmark.watermark.ct.trace.TracePoint TP4 = mkTracePoint("----","L","()V","E",8,0,S4);
  
   sandmark.util.StackFrame[] S5 = {
     mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,1,11),
     mkFrame("W","()V","E",31,3,1,8),
     mkFrame("S","()V","E",41,3,1,1),
     mkFrame("main","([Ljava/lang/String;)V","E",46,0,1,0)
  };
  sandmark.watermark.ct.trace.TracePoint TP5 = mkTracePoint("----","W","()V","E",31,3,S5);
  
   sandmark.util.StackFrame[] S6 = {
     mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,1,13),
     mkFrame("M","()V","E",12,0,1,12),
     mkFrame("W","()V","E",32,6,1,8),
     mkFrame("S","()V","E",41,3,1,1),
     mkFrame("main","([Ljava/lang/String;)V","E",46,0,1,0)
  };
  sandmark.watermark.ct.trace.TracePoint TP6 = mkTracePoint("----","M","()V","E",12,0,S6);
  
   sandmark.util.StackFrame[] S7 = {
     mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,1,16),
     mkFrame("K","()V","E",4,0,1,15),
     mkFrame("R","()V","E",36,0,1,14),
     mkFrame("S","()V","E",42,6,1,1),
     mkFrame("main","([Ljava/lang/String;)V","E",46,0,1,0)
  };
  sandmark.watermark.ct.trace.TracePoint TP7 = mkTracePoint("----","K","()V","E",4,0,S7);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints = {TP1, TP2, TP3, TP4, TP5, TP6, TP7};

   sandmark.watermark.ct.trace.callforest.Forest f = 
      new sandmark.watermark.ct.trace.callforest.Forest(
         tracePoints, classHierarchy, stats, props);

   System.out.println(f);
   try {
      System.out.println("Call-graph written to 'CallForest4.dot'");
      sandmark.util.Misc.writeToFile("CallForest4.dot", f.toDot()[0]);
   } catch (Exception ex) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + ex );
    }
   sandmark.watermark.ct.trace.callforest.PathGenerator paths = 
      new sandmark.watermark.ct.trace.callforest.PathGenerator(f.getForest(),100);
   System.out.println(paths.toString());
}

/*****************************************************************************/
/*                                 Main                                      */
/*****************************************************************************/
/**
 * Build the dynamic call-graph for methods test1, test2, test3, test4.
 * Call by
 * <PRE>
 *   java -classpath .:../smextern3/BCEL.jar:../smextern/bloat-1.0.jar \
                sandmark.watermark.ct.trace.callforest.Test
 * </PRE>
 */
public static void main(String args[]) {
   ClassHierarchy classHierarchy = new ClassHierarchy();
   Stats stats = new Stats();
   sandmark.util.ConfigProperties props = 
       new sandmark.util.ConfigProperties(new String[][] {
               {"Node Class","Watermark","foo",null,"S","N",},  
       },null);

   System.out.println("-----------------------------------------------------------");
   test1(classHierarchy, stats, props);
   System.out.println("-----------------------------------------------------------");
   test2(classHierarchy, stats, props, true);
   System.out.println("-----------------------------------------------------------");
   test2(classHierarchy, stats, props, false);
   System.out.println("-----------------------------------------------------------");
   test3(classHierarchy, stats, props);
   System.out.println("-----------------------------------------------------------");
   test4(classHierarchy, stats, props);
   System.out.println("-----------------------------------------------------------");
}
}

