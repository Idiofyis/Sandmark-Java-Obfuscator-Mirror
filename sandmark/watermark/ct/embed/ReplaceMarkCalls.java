package sandmark.watermark.ct.embed;

/*
 * MarkLocation keeps track of the locations within the
 * application where mark() calls should be replaced by
 * calls to the routines that build the watermark graph.
 * We keep a cache of open classes so that we don't
 * keep opening and closing the same class file over
 * and over; this could screw up the bytecode offsets
 * where insertion should take place.
 */
class MarkLocation {
   public sandmark.program.Class ec;
   public sandmark.program.Method mg;
   org.apache.bcel.generic.InstructionHandle ih;
   org.apache.bcel.generic.InstructionList il;
   public sandmark.util.ByteCodeLocation annoLocation;
   public int kind;
   public int embedType;
   public int localIndex;

   public static final int INT = 0;
   public static final int STRING = 1;

   static java.util.Hashtable markLocationCache;
   static java.util.Hashtable locals;
   static int varCount = 0;

   public MarkLocation(
      sandmark.program.Application app,
      sandmark.watermark.ct.embed.EmbedData embedData) throws java.lang.Exception {

      kind = embedData.kind;
      annoLocation = embedData.tracePoint.location;

      ec = app.getClass(annoLocation.getMethod().getClassName());
      mg = ec.getMethod(annoLocation.getMethod().getName(), annoLocation.getMethod().getSignature());
      il = mg.getInstructionList();
      ih = il.findHandle((int)annoLocation.getCodeIndex());

      if (embedData.kind==sandmark.watermark.ct.embed.EmbedData.VALUE) {
         if (embedData.tracePoint.value.startsWith("\"") && 
             embedData.tracePoint.value.endsWith("\"")) 
             embedType = STRING;
         else
             embedType = INT;
      }
      createLocal();
      mg.mark();
   }

   /*
    * Create a local variable to hold the argument to the mark(x) call.
    * Example:
    * <PRE>
    *    void P() {
    *       int x = ...;
    *       ...
    *       mark(x+4);
    *    }
    * </PRE>
    * Suppose that mark(x) is to be replaced by calls to
    *       Create_graph1()   
    *       Create_graph2()   
    * The resulting method would look something like this:
    * <PRE>
    *    void P() {
    *       int x = ...;
    *       ...
    *       if ((x+4)==5) Create_graph1();
    *       if ((x+4)==7) Create_graph2();  
    *    }
    * </PRE>
    * But the first time we use x+4 it will be popped off
    * the stack, and can't be reused. So, we have to do the
    * following instead:
    * <PRE>
    *    void P() {
    *       int x = ...;
    *       ...
    *       int sm$tmp1 = x+4;
    *       mark(sm$tmp1);
    *       if (sm$tmp1==5) Create_graph1();
    *       if (sm$tmp1==7) Create_graph2();  
    *    }
    * </PRE>
    * We make sure not to affect the stack height or the mark call.
    * 'mark(sm$tmp1)' will be removed later.
    */
   void createLocal() {
      if (kind == sandmark.watermark.ct.embed.EmbedData.LOCATION) return;
      java.lang.Integer Idx = (java.lang.Integer)locals.get(annoLocation);
      if (Idx != null) return;

      org.apache.bcel.generic.Type type = null;
      if (embedType == MarkLocation.STRING)
         type = org.apache.bcel.generic.Type.STRING;
      else 
         type = org.apache.bcel.generic.Type.LONG;

      org.apache.bcel.generic.LocalVariableGen lg = 
         mg.addLocalVariable("sm$tmp" + varCount++, type, null, null);
      localIndex = lg.getIndex();
      //      mg.setMaxLocals(mg.getMaxLocals()+1);      //  NEW AND UNTESTED!

      org.apache.bcel.generic.InstructionList instrs = 
          new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.Instruction dup = null;
      org.apache.bcel.generic.Instruction store = null;
      if (embedType == MarkLocation.STRING) {
          store = new org.apache.bcel.generic.ASTORE(localIndex);
          dup = new org.apache.bcel.generic.DUP();
      } else {
          store = new org.apache.bcel.generic.LSTORE(localIndex);
          dup = new org.apache.bcel.generic.DUP2();
      }
      instrs.append(dup);
      org.apache.bcel.generic.InstructionHandle start = instrs.append(store);
      lg.setStart(start);
      il.insert(ih, instrs);

      /*
      org.apache.bcel.generic.Instruction store = null;
      if (embedType == MarkLocation.STRING)
          store = new org.apache.bcel.generic.ASTORE(localIndex);
      else
          store = new org.apache.bcel.generic.LSTORE(localIndex);

      lg.setStart(il.insert(ih, store));
      */
      locals.put(annoLocation, new java.lang.Integer(localIndex));
   }

   /*
    * Create a new MarkLocation object. If one already
    * exists for this location, return it from the cache.
    */
   public static MarkLocation create (
      sandmark.program.Application app,
      sandmark.watermark.ct.embed.EmbedData embedData) throws java.lang.Exception {
       MarkLocation m = (MarkLocation) markLocationCache.get(embedData.tracePoint.location);
       if (m == null) {
           m = new MarkLocation(app, embedData);
	   markLocationCache.put(embedData.tracePoint.location, m);
       }
       return m;
   }

   public static void init() {
       markLocationCache = new java.util.Hashtable();
       locals = new java.util.Hashtable();
       varCount = 0;
   }

}

//-----------------------------------------------------------
//     Routines to build up insertion point information.
//-----------------------------------------------------------
/*
 * Construct all the data necessary to insert a method call
 * at a particular point in the program. This point is
 * specified by the embedData object. We find the instruction
 * list associated with the method and the instruction handle
 * associated with the callerCodeIndex.
 */
class InsertionPoint {
   public String value;
   public long callerCodeIndex;
   public String callerSourceName;
   public String callerName;
   public String callerSig;
   public sandmark.util.MethodID[] methods;
   public String location;
   public MarkLocation markLocation;

   public InsertionPoint (
      sandmark.program.Application app,
      sandmark.watermark.ct.embed.EmbedData embedData) throws java.lang.Exception {
      value = embedData.tracePoint.value;
      callerCodeIndex = embedData.tracePoint.location.getCodeIndex();
      callerName = embedData.tracePoint.location.getMethod().getName();
      callerSig = embedData.tracePoint.location.getMethod().getSignature();
      callerSourceName = embedData.tracePoint.location.getMethod().getClassName();
      methods = embedData.methods;
      location = callerSourceName + "." + callerName + ":" + callerCodeIndex;

      markLocation = MarkLocation.create(app,embedData);
    }
}


//-----------------------------------------------------------
//-----------------------------------------------------------
/**
 * Replaces Annot.mark() calls in the bytecode with method 
 * calls to watermark class 
 *
*/

public class ReplaceMarkCalls {

    sandmark.util.ConfigProperties props = null;
sandmark.program.Application app = null;
String watermarkClassName;
sandmark.watermark.ct.embed.EmbedData[] embedData;

//-----------------------------------------------------------
//               Constructor and Main Routine
//-----------------------------------------------------------

public ReplaceMarkCalls(
   sandmark.program.Application app,
   sandmark.util.ConfigProperties props, 
   sandmark.watermark.ct.embed.EmbedData[] embedData){
   this.props = props;
   this.embedData = embedData;
   this.app = app;
   watermarkClassName = props.getProperty("DWM_CT_Encode_ClassName");
}

/*
 * Insert static method calls to the methods in watermarkClass.
 * embedData contains information where the calls should be
 * inserted.
 *
 * This is the main entry point to this class. 
 */
public void insert() throws Exception {
    //System.out.println("ReplaceMarkCalls:insertCalls1:1");
    MarkLocation.init();

    InsertionPoint ips[] = new InsertionPoint[embedData.length];
    for (int i=0; i<embedData.length; i++) 
	ips[i] = new InsertionPoint(app, embedData[i]);

    for (int i=0; i<ips.length; i++) {
	insertCalls(ips[i]);
        ips[i].markLocation.mg.setMaxStack();
    }
}

/*
 * Insert static method calls to ip.methods at location ip.ih.
 * The calls go to methods in the class watermarkClass.
 * ip.ih has a call to a mark() method.
 * @param ip the point in the code where to insert the call.
 */
void insertCalls (
   InsertionPoint ip) {
   //System.out.println("ReplaceMarkCalls:insertCalls2:1");

   org.apache.bcel.generic.ConstantPoolGen cp = ip.markLocation.ec.getConstantPool();

   org.apache.bcel.generic.InstructionList instrs = 
      new org.apache.bcel.generic.InstructionList();

   for(int m=0; m<ip.methods.length; m++) {
       int methodRef = 
          cp.addMethodref(watermarkClassName, 
                          ip.methods[m].getName(), 
                          ip.methods[m].getSignature()); // USED TO BE "()V"
      String graphMethod = watermarkClassName + "." + 
                           ip.methods[m].getName() + ":" +
                           ip.methods[m].getSignature();

      sandmark.util.Log.message(0,"Inserting call to " + graphMethod + " at " + ip.location);
      insertCall(cp, instrs, methodRef, ip);
   }
   ip.markLocation.il.append(ip.markLocation.ih, instrs);   
   ip.markLocation.mg.mark();
   /*
   ip.markLocation.il.insert(ip.markLocation.ih, instrs);             
   ip.markLocation.ih.setInstruction(new org.apache.bcel.generic.NOP());
   */
}



/*
 * Insert static method call to methodRef at location ip.ih.
 * localIndex is the index of the local variable which holds
 * the argument of the 'mark()' call.
 * @param cp        the constant pool
 * @param instrs    the instruction list
 * @param methodRef constant pool index of the method to insert a call to
 * @param ip        the point in the code where to insert the call.
 */
void insertCall (
   org.apache.bcel.generic.ConstantPoolGen cp,
   org.apache.bcel.generic.InstructionList instrs,
   int methodRef,
   InsertionPoint ip) {
    //System.out.println("ReplaceMarkCalls:insertCall3:1");
      if (ip.markLocation.kind == sandmark.watermark.ct.embed.EmbedData.LOCATION) {
         org.apache.bcel.generic.INVOKESTATIC call = 
             new org.apache.bcel.generic.INVOKESTATIC(methodRef);
         instrs.append(call);
      } else {
         //System.out.println("ReplaceMarkCalls:insertCall3:2");
	 if (ip.markLocation.embedType == MarkLocation.STRING) {
            //System.out.println("ReplaceMarkCalls:insertCall3:3");
            instrs.append(new org.apache.bcel.generic.ALOAD(ip.markLocation.localIndex));
            String arg = ip.value.substring(1, ip.value.length()-1);
            instrs.append(new org.apache.bcel.generic.LDC(cp.addString(arg)));
            int equalsRef = cp.addMethodref("java.lang.String", "equals", "(Ljava/lang/Object;)Z");
            instrs.append(new org.apache.bcel.generic.INVOKEVIRTUAL(equalsRef));         
         } else {
            //System.out.println("ReplaceMarkCalls:insertCall3:4");
            instrs.append(new org.apache.bcel.generic.LLOAD(ip.markLocation.localIndex));
            long arg = java.lang.Long.parseLong(ip.value);
            instrs.append(new org.apache.bcel.generic.PUSH(cp, arg));
            instrs.append(new org.apache.bcel.generic.LCMP());
         }         
         //System.out.println("ReplaceMarkCalls:insertCall3:5");
         org.apache.bcel.generic.IFNE br = new org.apache.bcel.generic.IFNE(null);
         instrs.append(br);
         instrs.append(new org.apache.bcel.generic.INVOKESTATIC(methodRef));
         org.apache.bcel.generic.NOP label = new org.apache.bcel.generic.NOP();
         org.apache.bcel.generic.InstructionHandle handle = instrs.append(label);
         br.setTarget(handle);
         //System.out.println("ReplaceMarkCalls:insertCall3:6");
      }
}


}








