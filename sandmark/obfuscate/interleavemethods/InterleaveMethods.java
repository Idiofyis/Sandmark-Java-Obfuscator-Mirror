package sandmark.obfuscate.interleavemethods;

/** Implementation of method interleaver.
 *  Puts 2 methods with same sig into one new one, whose execution
 *  path is based on the value of an opaque predicate
 *  input as an additional parameter to the new method.
 *
 *  Some future enhancements could be:
 *    -inline more than 2 methods at a time (variable#)
 *    -don't require same params, but add in params as
 *     needed (alternatively pass params as 
 *     java.lang.Objects)
 *    -don't require same return types. This could be
 *     done by changing all return types to java.lang.Objects
 *    
 *  15 September 2003
 *  @author Zach Heidepriem
 */
public class InterleaveMethods extends sandmark.obfuscate.AppObfuscator {
   public static org.apache.bcel.generic.InstructionHandle COOLHANDLE=null;



   private static final boolean DEBUG = false;   
   private static final String DYNAMIC_INIT = "<init>";
   private static final String STATIC_INIT = "<clinit>";

   //This obfuscator applies to one app
   private sandmark.program.Application application;
   private sandmark.analysis.classhierarchy.ClassHierarchy ch;
   private java.util.HashMap amap, bmap, orig2new, bbytes;

   /** Algorithm:
    *  1) For every method A in the app, if A is eligible for renaming
    *     but it's not a key of orig2new:
    *     For every method A' in getAllOverrides(A) (includes A),
    *     copy A' into C' where every C' has same name and signature. 
    *     Put (A',C') into orig2new.
    * 
    *  2) If we found an eligble A {
    *  a) Try to find a match for A, B s.t. B is in the same class as A,
    *     has the same sig as A, can be renamed, and isn't a key or value in
    *     orig2new.
    *     If we successfully find a pair (A,B), select a random bytes p,q.
    *     For every method A' in getAllOverrides(A) put (A', p) into amap 
    *     
    *  b) For every method B' in getAllOverrides(B) (includes B) let 
    *     A' = B'.getEnclosingClass().getMethod(A.name(), A.sig()):
    *     if A' != null then
    *       bmap.put(B', A'); orig2new.put(B',orig2new.get(A'))
    *     else
    *       (C' = B'.copy()).setName(orig2new.get(A).getName());
    *       orig2new.put(B', C');     
    *
    *  3) For each entry (M,C) in orig2new append a byte parameter P to C's 
    *     params 
    *
    *  4) For each entry (B,A) in bmap let q = bbytes.get(B), p = Amap.get(A),
    *     C = orig2new.get(A)
    *     Then C becomes { Interleaver.interleave(A,B,C,p,q) }
    *     
    *  5) Go through the entire app and update all the method calls. For
    *     every call to M:
    *     if amap.containsKey(M) then p = amap.get(M), 
    *                                 C = orig2new.get(M):
    *       C(...) --becomes--> int x  = (y != p); 
    *                           if(Pt) x = p  C(...,x);
    *     else if bmap.containsKey(M) then p = amap.get(bmap.get(M)),
    *                                      C = orig2new.get(M):
    *       C(...) --becomes--> int x = p; if(Pt) x = (y!=p) C(...,x);
    *     else if orig2new.containsKey(M) then p = randomByte(), 
    *                                     C = orig2new.get(M):
    *       C(...) --becomes--> C(...,p)  
    * 
    *  6) For every entry (M, C) in orig2new, remove M.      
    */
   public void apply(sandmark.program.Application application) 
      throws Exception {
      //Some global variables...
      this.application = application;
      ch = new sandmark.analysis.classhierarchy.ClassHierarchy(application);
      //A->predVal
      amap = new java.util.HashMap();
      //B->A
      bmap = new java.util.HashMap();
      //M->C
      orig2new = new java.util.HashMap();    
      //B->q
      bbytes = new java.util.HashMap();


      // publicize and de-finalize all methods
      new sandmark.util.Publicizer().apply(application);
      for(java.util.Iterator classes = application.classes() ; 
          classes.hasNext() ; ) {
         sandmark.program.Class clazz = 
            (sandmark.program.Class)classes.next();
         for(java.util.Iterator methods = clazz.methods() ; 
             methods.hasNext() ; ) {
            sandmark.program.Method method =
               (sandmark.program.Method)methods.next();
            method.setFinal(false);
         }
      }
          
      //Go through all the classes and  methods
      java.util.Vector pairs = new java.util.Vector();
      sandmark.program.Class[] classes = application.getClasses();
      for(int i = 0; i < classes.length; i++){
         sandmark.program.Class clazz = classes[i];                
          
         sandmark.program.Method[] methods = clazz.getMethods();
         for(int j = 0; j < methods.length; j++){
            sandmark.program.Method method = methods[j];
            if(orig2new.containsValue(method))
               continue;                               
            if(step1(method))
               step2(method);
         }
      }

      step3();                
      if(DEBUG)
         print();        
      step4();
      step5();        
      step6();        
   }   


   // returns false if the method canot be merged.
   // if true, fills in orig2new
   private boolean step1(sandmark.program.Method A) throws Exception {
      if(!isSpecial(A) && !orig2new.containsKey(A)){           
         java.util.HashSet set = new java.util.HashSet();
         getMethodsToRename(A, set);
         java.util.Iterator it = set.iterator();
         String name = null;
         if(DEBUG)
            System.out.println("The overrides for " + 
                               A.getEnclosingClass() + "." +
                               A.getName() + " are: ");
         for(java.util.Iterator methods = set.iterator() ; 
             methods.hasNext() ; ) {
            sandmark.program.Method method = 
               (sandmark.program.Method)methods.next();
            sandmark.analysis.controlflowgraph.MethodCFG cfg = null;
            try {
               cfg = method.getCFG();
            } catch(sandmark.analysis.controlflowgraph.EmptyMethodException e) {}
            if(isSpecial(method) || (cfg != null && cfg.nodeCount() != 
                                     cfg.graph().removeUnreachable(cfg.source()).nodeCount()) ||
               method.getInstructionList() == null) {
               return false;
            }
         }
         while(it.hasNext()){
            sandmark.program.Method Ap = (sandmark.program.Method)
               it.next();  
            if(DEBUG)
               System.out.println("\t" + 
                                  Ap.getEnclosingClass() + "." +
                                  Ap.getName());
            sandmark.program.Method Cp = Ap.copy();
            if(name == null)
               name = Cp.getName();
            else
               Cp.setName(name);
            orig2new.put(Ap, Cp);
         }      
         return true;
      }
      else
         return false;
   }


   private void step2(sandmark.program.Method A) throws Exception {
      //Find a match
      sandmark.program.Method B = getMatch(A);
      if(B != null){ //we found a B, so add a pair
         //2A
         byte p = randomByte();
         byte q = randomByte();
         while(q == p)
            q = randomByte();            
         java.util.HashSet set = new java.util.HashSet();
         getMethodsToRename(A, set);
         java.util.Iterator it = set.iterator();
         while(it.hasNext()){
            sandmark.program.Method Ap = (sandmark.program.Method)
               it.next();           
            amap.put(Ap, new Byte(p));
         }
         set = new java.util.HashSet();
         getMethodsToRename(B, set);
         it = set.iterator();
         while(it.hasNext()){
            sandmark.program.Method Bp = (sandmark.program.Method)
               it.next();           
            bbytes.put(Bp, new Byte(q));
         }           

         //2B
         if(DEBUG)
            System.out.println("---\nA is " + A + 
                               ", B is " + B + "\n---");

         set = new java.util.HashSet();
         getMethodsToRename(B, set);
         it = set.iterator();
         while(it.hasNext()){
            sandmark.program.Method Bp = (sandmark.program.Method)
               it.next();           
            sandmark.program.Method Ap = Bp.getEnclosingClass().getMethod
               (A.getName(), A.getSignature());
               
            if(DEBUG)
               System.out.println("A' is " + Ap +
                                  " B' is " + Bp);
            if(Ap != null && orig2new.get(Ap) != null){
               bmap.put(Bp, Ap);                    
               orig2new.put(Bp, orig2new.get(Ap));
            }
            else {                        
               sandmark.program.Method Cp = Bp.copy();
               sandmark.program.Method C = (sandmark.program.Method)
                  orig2new.get(A);
               Cp.setName(C.getName());
               orig2new.put(Bp, Cp);
            }                                          
         }                     
      }
   }

   private void step3(){                  
      java.util.Iterator cmethods = orig2new.values().iterator();
      java.util.Vector marked = new java.util.Vector();
      while(cmethods.hasNext()){
         sandmark.program.Method c = 
            (sandmark.program.Method)cmethods.next();
         //Only add 1 byte for each C
         if(marked.contains(c))
            continue;
         else
            marked.add(c);
            
         org.apache.bcel.generic.Type[] origtypes = 
            c.getArgumentTypes();
         org.apache.bcel.generic.Type[] types =
            new org.apache.bcel.generic.Type[origtypes.length+1];
         for(int i = 0; i < origtypes.length; i++)
            types[i] = origtypes[i];
         types[origtypes.length] = 
            org.apache.bcel.generic.BasicType.BYTE;            
         c.setArgumentTypes(types);                           
      }
   }

   private void step4(){
      java.util.Iterator it = bmap.keySet().iterator();
      while(it.hasNext()){
         sandmark.program.Method B = (sandmark.program.Method)it.next();
         sandmark.program.Method A = (sandmark.program.Method)bmap.get(B);

         sandmark.program.Method C = 
            (sandmark.program.Method)orig2new.get(A);
         sandmark.program.Method D = 
            (sandmark.program.Method)orig2new.get(B);
         if(DEBUG)
            System.out.println("interleaving " + A + " and " + B + " to get " + C);
         //Assert
         if(C != D)
            if(DEBUG)
               System.out.println("Orig2new appears corrupt: " +
                                  A + "->"  + C + "\n" +
                                  B + "->" + D);
            
         byte p = ((Byte)amap.get(A)).byteValue();
         byte q = ((Byte)bbytes.get(B)).byteValue();
         new Interleaver().interleave(A, B, C, p, q);
      }
   }

   private void step5(){
      //Step 5: fix all the calls using the maps we built,   
      java.util.Iterator appInstructions =
         getAppInstructions(application);
      while(appInstructions.hasNext())
         fix((Bundle)appInstructions.next()); 
   }

   private void step6(){
      //Step 6: delete all the original methods 
      java.util.Iterator methods = orig2new.keySet().iterator();
      while(methods.hasNext())
         ((sandmark.program.Method)methods.next()).delete();
   }

        













   //***********************HELPER METHODS***************************
   private void fix(Bundle bundle){       
                      
      sandmark.program.Method method = bundle.getMethod();
      sandmark.program.Class clazz = bundle.getEnclosingClass();            
      org.apache.bcel.generic.InstructionHandle ih = bundle.getIH();         

      //If its an invoke...
      if(ih.getInstruction() instanceof 
         org.apache.bcel.generic.InvokeInstruction){
         org.apache.bcel.generic.InvokeInstruction ii =
            (org.apache.bcel.generic.InvokeInstruction)
            ih.getInstruction();  

         sandmark.program.Class invokedClass =
            application.getClass(ii.getClassName
                                 (method.getConstantPool()));
         if(invokedClass == null) //can't find the class in app, return
            return;

         String invokeName = ii.getName(method.getConstantPool());
         String invokeSig = ii.getSignature(method.getConstantPool());
         sandmark.program.Method invoke = invokedClass.getMethod
            (invokeName, invokeSig);
                                           
         int i = 0;
         sandmark.program.Class[] superclasses = 
            ch.superClasses(invokedClass);
         while(invoke == null && i < superclasses.length)                
            invoke = superclasses[i++].getMethod(invokeName, invokeSig);
            
         if(DEBUG)
            System.out.println("The invoke is: " + invokedClass + "." +
                               invoke);      
           
         sandmark.program.Method copy;
         //boolean isB;
         Byte predVal;

         //check to see if we need to change it...       
         if(amap.containsKey(invoke)){
            copy = (sandmark.program.Method)orig2new.get(invoke);
            predVal = (Byte)amap.get(invoke);
            //isB = false;
         }
         else if(bmap.containsKey(invoke)){
            sandmark.program.Method a = 
               (sandmark.program.Method)bmap.get(invoke);
            copy = (sandmark.program.Method)orig2new.get(a);
            predVal = (Byte)bbytes.get(invoke);
            //isB = true;                
         }
         //else we have a single method
         else if(orig2new.containsKey(invoke)){
            copy = (sandmark.program.Method)orig2new.get(invoke);
            //No interleaving was performed in copy. Ignore the byte
            predVal = new Byte(randomByte()); 
            //isB = false;
         }
         else //Its some regular call, dont do anything
            return;           
         updateCall(method, copy, ih, predVal);
      } //ii    
   } //fix              
 
   //method(...) --becomes--> if(Pt) x = predVal copy(...,x);       
   private void updateCall(sandmark.program.Method method,
                           sandmark.program.Method copy,
                           org.apache.bcel.generic.InstructionHandle ih,
                           Byte predVal) {     
        
      org.apache.bcel.generic.InstructionList il =
         method.getInstructionList();  
      //Add the class to the constant pool, if not already there         
      org.apache.bcel.generic.ConstantPoolGen cpg = 
         method.getEnclosingClass().getConstantPool();            
         
      org.apache.bcel.generic.InvokeInstruction newCall = null;            
      //create a call to A'()  
      int cpindex;
      if(copy.getEnclosingClass().isInterface())
         cpindex = cpg.addInterfaceMethodref(copy.getClassName(),
                                             copy.getName(),
                                             copy.getSignature()); 
      else     
         cpindex = cpg.addMethodref(copy.getClassName(),
                                    copy.getName(),
                                    copy.getSignature());  
      int numargs = copy.getArgumentTypes().length;
      //Create a new invoke instruction...            
      if(ih.getInstruction() instanceof 
         org.apache.bcel.generic.INVOKESTATIC)
         newCall = new org.apache.bcel.generic.INVOKESTATIC(cpindex);
      else if(ih.getInstruction() 
              instanceof org.apache.bcel.generic.INVOKEVIRTUAL)
         newCall = new org.apache.bcel.generic.INVOKEVIRTUAL(cpindex);
      else if(ih.getInstruction()
              instanceof org.apache.bcel.generic.INVOKEINTERFACE)
         newCall = 
            new org.apache.bcel.generic.
            INVOKEINTERFACE(cpindex, InterleaveUtil.getCount(copy));
      else if(ih.getInstruction()
              instanceof org.apache.bcel.generic.INVOKESPECIAL)
         {         
            if(isInit(copy)){
               if(DEBUG)
                  System.out.println("Can't update sig of init! " + 
                                     ih.getClass());
               return;
            }
            else { //its private method or call to super method
               newCall = new org.apache.bcel.generic.
                  INVOKESPECIAL(cpindex);
            }
         }  
      else //this should never happen as long as only 4 subclasses of II
         return;
          

      //Find a y != predVal
      byte y = randomByte();                     
      while(y == predVal.byteValue())
         y = randomByte();       
      //Make some instructions
      //Create local variable "im_x" to the method call   
      int loc = InterleaveUtil.getSlot(method);        
      org.apache.bcel.generic.Instruction bipush =
         new org.apache.bcel.generic.BIPUSH(y);         

      org.apache.bcel.generic.Instruction istore = 
         new org.apache.bcel.generic.ISTORE(loc);
      //First store some value y != predVal 'x = y'
      org.apache.bcel.generic.InstructionHandle predStart = 
         il.insert(ih, bipush);
      il.append(bipush, istore);
      org.apache.bcel.generic.Instruction load =
         new org.apache.bcel.generic.ILOAD(loc);
      org.apache.bcel.generic.InstructionHandle loadIH =
         il.insert(ih, load);         
         
      //now we have { .., load y; newcall }
      //Put in a true predicate (ie push a 1)      
        
      //Push 1 for now.  replace after all other changes have been made 
      //with opl predicate
      org.apache.bcel.generic.InstructionHandle pushPredicateValue =
         il.insert
         (loadIH,
          org.apache.bcel.generic.InstructionConstants.ICONST_1);
        
      //now there's an extra 1 on the stack, before load          
      //If we want to call A(), branch. Else dont branch
      org.apache.bcel.generic.BranchInstruction branch =
         new org.apache.bcel.generic.IFEQ(loadIH);          

      org.apache.bcel.generic.Instruction fakePush =
         new org.apache.bcel.generic.BIPUSH(predVal.byteValue());
      org.apache.bcel.generic.Instruction fakeStore =
         new org.apache.bcel.generic.ISTORE(loc);        
        
      //There's a 1 on the stack..
      //So { if[eq]->load; bipush(predVal); 
      //     istore(loc); load(loc); newcall }
      il.insert(loadIH, branch);
      il.append(branch, fakePush);        
      il.append(fakePush, fakeStore);                

      //and replace the old one          
      org.apache.bcel.generic.InstructionHandle newCallHandle = 
         il.append(ih, newCall);  

      //Clean everything up
      il.redirectBranches(ih, predStart);      

      
      il.redirectExceptionHandlers(method.getExceptionHandlers(),
                                   ih,
                                   newCallHandle);
      il.redirectLocalVariables(method.getLocalVariables(),
                                ih,
                                newCallHandle);
      il.update();
        
      try{
         il.delete(ih);         
      }catch(org.apache.bcel.generic.TargetLostException tle){
         //This is supposed to fix the line numbers
         org.apache.bcel.generic.InstructionHandle[] targets = 
            tle.getTargets();
         for(int i=0; i < targets.length; i++) {
            org.apache.bcel.generic.InstructionTargeter[] targeters = 
               targets[i].getTargeters();
                
            for(int j=0; j < targeters.length; j++)                    
               targeters[j].updateTarget(targets[i], newCallHandle);
         }
      }

	
      sandmark.util.opaquepredicatelib.PredicateFactory predicates[] =
         sandmark.util.opaquepredicatelib.OpaqueManager.getPredicatesByValue
         (sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE);
      java.util.HashSet badPreds = new java.util.HashSet();
      sandmark.util.opaquepredicatelib.OpaquePredicateGenerator predicate = null;
      while(predicate == null && badPreds.size() != predicates.length) {
         int which = sandmark.util.Random.getRandom().nextInt() % predicates.length;
         if(which < 0)
            which += predicates.length;
         predicate = predicates[which].createInstance();
         if(!predicate.canInsertPredicate
            (method,pushPredicateValue,
             sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE)) {
            badPreds.add(predicates[which]);
            predicate = null;
         }
      }
      predicate.insertPredicate
         (method,pushPredicateValue,
          sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE);
      pushPredicateValue.setInstruction(new org.apache.bcel.generic.NOP());
	
      il.setPositions();
      method.mark();       
   } 
 
   //Return an interator of Bundles, each of which store 
   //(instruction, method, class)
   private java.util.Iterator getAppInstructions
      (sandmark.program.Application app){
      java.util.List list = new java.util.ArrayList();
      java.util.Iterator classes = app.classes();        
      while(classes.hasNext()){
         sandmark.program.Class clazz = 
            (sandmark.program.Class)classes.next();
         java.util.Iterator methods = clazz.methods();
         while(methods.hasNext()){
            sandmark.program.Method method = 
               (sandmark.program.Method)methods.next();              
               
            if(method.getInstructionList() == null ||
               orig2new.containsKey(method))
               continue;
            java.util.Iterator il = method.getInstructionList().iterator();
            while(il.hasNext()){
               org.apache.bcel.generic.InstructionHandle ih = 
                  (org.apache.bcel.generic.InstructionHandle)il.next();
               list.add(new Bundle(ih, method, clazz));
            }
         }
      }
      return list.iterator();
   }   

   private void getMethodsToRename
      (sandmark.program.Method method, java.util.HashSet set)
      throws Exception {
       
      set.add(method);
      sandmark.util.MethodID[] ids = ch.getMethodsToRename
         (new sandmark.util.MethodID(method));
      for(int i = 0; i < ids.length; i++){
         sandmark.program.Class clazz =
            application.getClass(ids[i].getClassName());
         if(clazz == null){
            if(DEBUG)
               System.out.println("Couldn't find " + 
                                  ids[i].getClassName());
            continue;
         }
         sandmark.program.Method override =
            clazz.getMethod(ids[i].getName(), ids[i].getSignature());
         if(override == null)
            throw new RuntimeException();
            
         if(!set.contains(override))
            getMethodsToRename(override, set);                       
      }      

   }

   private boolean isInit(sandmark.program.Method origMethod){
      return(origMethod.getName().equals(DYNAMIC_INIT) ||
             origMethod.getName().equals(STATIC_INIT));            
   }
    
   private boolean isSpecial(sandmark.program.Method origMethod){
      //Not even going to try to interleave method if it overrides
      //a java method, or is init, or main, or toString, or equals or...
      if(origMethod == null)
         return true;
       
      sandmark.util.MethodID methID = 
         new sandmark.util.MethodID(origMethod);       
      try{
         if (ch.overridesLibraryMethod(methID) || 
             ch.isSpecialMethod(methID)) 
            return true;                         
      }catch(sandmark.analysis.classhierarchy.ClassHierarchyException che){
         che.printStackTrace();                                  
      }              
      return false;         
   }

   private sandmark.program.Method getMatch(sandmark.program.Method a){
      if(DEBUG)
         System.out.print("Matching " + a.getEnclosingClass() + 
                          "." + a + "...");
      sandmark.program.Method[] methods = a.getEnclosingClass().getMethods();
      for(int i = 0; i < methods.length; i++){           
         if(methods[i] != a &&
            methods[i].getSignature().equals(a.getSignature()) &&
            (methods[i].isStatic() == a.isStatic()) &&
            !isSpecial(methods[i]) &&
            !orig2new.containsKey(methods[i]) &&
            !orig2new.containsValue(methods[i]) &&
            methods[i].getInstructionList() != null &&
            methods[i].getInstructionList().size() + 
            a.getInstructionList().size() <= 20000){
            if(DEBUG)
               System.out.println(methods[i]);
            return methods[i];
         }
      }
      if(DEBUG)
         System.out.println("not found.");
      return null;            
   }

   private byte randomByte(){
      java.util.Random random = sandmark.util.Random.getRandom();
      byte[] bytes = new byte[1];
      random.nextBytes(bytes);
      return bytes[0];
   }   

   public String getShortName() {
      return "Interleave Methods";
   }
   public String getLongName() {
      return "Interleave pairs of methods into a single method";
   }
   public String getAlgHTML() {
      return
         "<HTML><BODY>" +
         "Method interleaver combines two methods into a single " +
         "new method.\n" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author:<A HREF =" +
         "\"mailto:zachary@cs.arizona.edu\">Zachary Heidepriem</A>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }
   public String getAlgURL() {
      return "sandmark/obfuscate/interleavemethods/doc/help.html";
   }

   public String getAuthor(){
      return "Zachary Heidepriem";
   }

   public String getAuthorEmail(){
      return "zachary@cs.arizona.edu";
   }

   public String getDescription(){
      return "Simple implementation of method interleaver " +
         "combines 2 methods into one new one. The execution " +
         "path is based on the value of an opaque predicate " +
         "input as an added parameter to the new method.";            
   }
   public String[] getReferences() {
      return new String[] {};
   }
   public sandmark.config.ModificationProperty[] getMutations() {
      return new sandmark.config.ModificationProperty[]{
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
         sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
         sandmark.config.ModificationProperty.I_PUBLICIZE_FIELDS,
         sandmark.config.ModificationProperty.I_PUBLICIZE_METHODS,
      };
   }   

   private class MethodBundle {

      private sandmark.program.Method method1, method2;

      public MethodBundle(sandmark.program.Method a,
                          sandmark.program.Method b){
         method1 = a;
         method2 = b;
      }

      public sandmark.program.Method getA(){
         return method1;
      }

      public sandmark.program.Method getB(){
         return method2;
      }

      public String toString(){
         return "[" + 
            method1.getClassName() + "." +
            method1.getName() + ", " +
            method2.getClassName() + "." + 
            method2.getName() + 
            "]";
      }
   }

   private class Bundle {

      private sandmark.program.Class clazz;
      private sandmark.program.Method method;
      private org.apache.bcel.generic.InstructionHandle ih;

      public Bundle(org.apache.bcel.generic.InstructionHandle ih,
                    sandmark.program.Method method,
                    sandmark.program.Class clazz){
         this.ih = ih;
         this.method = method;
         this.clazz = clazz;        
      }

      public org.apache.bcel.generic.InstructionHandle getIH(){
         return ih;
      }

      public sandmark.program.Method getMethod(){
         return method;
      }

      public sandmark.program.Class getEnclosingClass(){
         return clazz;
      }    
   }

   private void print(){
      System.out.println("\n***** START orig2new*****");
      java.util.Iterator it = orig2new.keySet().iterator();
      while(it.hasNext()){
         sandmark.program.Method key = 
            (sandmark.program.Method)it.next();
         sandmark.program.Method value =
            (sandmark.program.Method)orig2new.get(key);
         System.out.println(key.getEnclosingClass() + "." +
                            key + "->" +
                            value.getEnclosingClass() + "." +
                            value);
      }
      System.out.println("*****END orig2new*****\n");
   }

   /**For testing purposes only
    */
   public static void main(String[] args){
      try{
         sandmark.program.Application app =
            new sandmark.program.Application(args[0]);
         new InterleaveMethods().apply(app);
         app.save(args[1]);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}

