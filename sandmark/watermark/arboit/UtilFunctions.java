package sandmark.watermark.arboit;

/**
 * This class maintains a bunch of static methods for use in ArboitAlg and
 * DynamicAA.
 */
public class UtilFunctions {

   static boolean DEBUG = false;
   static boolean EVAL = false;
   static int USE_CONSTS = 0;
   static int USE_RANK = 1;
   
   /**
    * This method determines which methods in the application are possible
    * candidates for having an opaque predicate added.
    */
   public static java.util.ArrayList preprocess(sandmark.program.Application app) 
      throws sandmark.watermark.WatermarkingException {

      java.util.Iterator classes = app.classes();
      java.util.ArrayList candidateList = new java.util.ArrayList();
      Bundle bundleObject = null;
      String className = null;
      sandmark.program.Class cls= null;
   
      while(classes.hasNext()){
         cls = (sandmark.program.Class)classes.next();
         className = cls.getName();
      

         if(!cls.isInterface() && !cls.isAbstract()){
            
            sandmark.program.Method[] methods = cls.getMethods();
            for(int i=0; i < methods.length; i++){
               java.util.ArrayList indexList = new java.util.ArrayList();
               //scan instruction list for ifstatements
               sandmark.program.Method m = methods[i];
               if(DEBUG)System.out.println("method name: " + m.getName());
               org.apache.bcel.generic.InstructionList il =
                  m.getInstructionList();
               il.setPositions();
               org.apache.bcel.generic.InstructionHandle[] ihs =
                  il.getInstructionHandles();
               org.apache.bcel.generic.Instruction[] insts = il.getInstructions();
               for(int j=0; j < insts.length; j++){
                  org.apache.bcel.generic.Instruction inst = insts[j];
                  if(inst instanceof org.apache.bcel.generic.IfInstruction){
                     org.apache.bcel.generic.InstructionHandle ifHandle =
                        ihs[j];
                     int pos = ifHandle.getPosition();
                     if(DEBUG)System.out.println("ifPosition: " + pos);

                     Integer intObj = new Integer(pos);
                     indexList.add(intObj);
                  }//end if
               }//end for
               if(indexList.size() > 0){
                  bundleObject = new Bundle(className, m, indexList);
                  candidateList.add(bundleObject);
               }
            }//end for
         }//end if
      }//end while

      if(candidateList.size() == 0)
         throw new sandmark.watermark.WatermarkingException("There are no " +
            "suitable if statements to use for watermarking.");

      return candidateList;

   }//end preprocess
   
   private static void setSeed(String key) {
      long seed;
      if(key == null || key.equals(""))
         seed = 42;
      else{
         java.math.BigInteger bigIntKey = sandmark.util.StringInt.encode(key);
         seed = bigIntKey.longValue();
      }
      sandmark.util.Random.getRandom().setSeed(seed);
   }

   public static boolean isAppValid(sandmark.program.Application app) {

      java.util.Iterator classes = app.classes();
      if(!classes.hasNext())
         return false;
      else
         return true;

   }

   public static java.math.BigInteger wmBigIntValue(String wm){
      java.math.BigInteger bigIntWm =
         sandmark.util.StringInt.encode(wm);
      if(EVAL)System.out.println("watermark to embed: " + wm);
      return bigIntWm;
   }

   public static java.math.BigInteger[] splitWM
       (String watermark,
        sandmark.util.ConfigProperties props) {
      int maxValue;
      if(props.getProperty("Encode as constants").equals("true"))
         maxValue = 1000; //this is a random maximum value that seems to work
      else
         maxValue = 8; // this is the number of opaque predicates

      sandmark.util.splitint.CombinationSplitter mySplitter =
         new sandmark.util.splitint.CombinationSplitter(maxValue);
      //sandmark.util.splitint.PartialSumSplitter mySplitter = 
         //new sandmark.util.splitint.PartialSumSplitter(getGenerator(props));

      java.math.BigInteger[] wmValues = mySplitter.split(wmBigIntValue(watermark));
/*
      System.out.println("parts: " + props.getProperty("MIN_WM_PARTS"));
      java.math.BigInteger[] wmValues = mySplitter.split(wmBigIntValue(props),
         (new Integer(props.getProperty("MIN_WM_PARTS"))).intValue());
      if(DEBUG)System.out.println("length: " + wmValues.length);
         for(int i=0; i < wmValues.length; i++){
            if(EVAL)System.out.println(wmValues[i].intValue());
            if(wmValues[i].intValue() > 32767){
               if(DEBUG)System.out.println(wmValues[i].intValue());
               throw new sandmark.watermark.WatermarkingException("need more " +
                  "parts inorder to watermark");
            }
         }
*/
      return wmValues;
   }


   public static org.apache.bcel.generic.InstructionHandle findSliceStart(
      sandmark.program.Method method, int ifIndex){
      
      org.apache.bcel.generic.InstructionHandle ifHandle =
         findIfHandle(method, ifIndex);
      if(DEBUG)System.out.println("this if: " + ifHandle);
      if(ifHandle == null)
         return null;
      return getSlicingCrit(method, ifHandle);
   }

   public static org.apache.bcel.generic.InstructionHandle findIfHandle(
      sandmark.program.Method method, int ifIndex){

      org.apache.bcel.generic.InstructionList ilForIf =
         method.getInstructionList();
      ilForIf.setPositions();
      return ilForIf.findHandle(ifIndex);
   }

   /**
    * @return false means we are done, true means we are not done.
    */
   public static boolean updateIndexList(java.util.ArrayList candidateList,
      Bundle b, int ifIndex){

      sandmark.program.Method m = b.getMethod();
      java.util.ArrayList indexList = b.getIndexList();
      Integer ifIntIndex = new Integer(ifIndex);

      indexList.remove(ifIntIndex);

      if(indexList.size() == 0){
         candidateList.remove(b);
         if(candidateList.size() == 0)
            return false;
      }else{ //fix the remaining indices
         if(DEBUG)System.out.println("fixing index");
         fixList(indexList, m, ifIndex);
         if(indexList.size() == 0){
            candidateList.remove(b);
            if(candidateList.size() == 0)
               return false;
         }
      }
      return true;
   }

   public static void fixList(java.util.ArrayList indexList, 
      sandmark.program.Method m, int oldIndex){
      if(DEBUG)System.out.println("updating list");
      if(DEBUG)System.out.println("indexList: " + indexList.toString());
      for(int i=0; i < indexList.size(); i++){
         Integer iindex = (Integer)indexList.get(i);
         int index = iindex.intValue();
         if(oldIndex < index){
            if(DEBUG)System.out.println("old index less than index");
            org.apache.bcel.generic.InstructionList il =
               m.getInstructionList();
            il.setPositions();
            org.apache.bcel.generic.InstructionHandle ih = null;
            do{
               ih = il.findHandle(index);
               index++;
            }while(ih == null);
               
            boolean notFound = true;
            while(notFound){
               org.apache.bcel.generic.Instruction inst = ih.getInstruction();
               if(inst instanceof org.apache.bcel.generic.IfInstruction){
                  Integer newIndex = new Integer(ih.getPosition());
                  indexList.set(i, newIndex);
                  notFound = false;
               }else{
                  ih = ih.getNext();
               }
               if(ih == null){
                  indexList.remove(i);
                  notFound = false;
               }
            }
         }
      }//end for
   }


   public static java.util.ArrayList identifyUsableVars(
      sandmark.analysis.slicingtools.ForwardMethodSlice fs, 
      sandmark.program.Method m,
      int ifIndex){

      org.apache.bcel.generic.ConstantPoolGen cp = m.getCPG();
      java.util.ArrayList possibleVars = fs.getAffectedVars();

      //search the possibleVars to see if any are of type int
      //for right now this is all that can be handled
      sandmark.analysis.controlflowgraph.MethodCFG cfg = m.getCFG();
      org.apache.bcel.generic.InstructionList il = m.getInstructionList();
      il.setPositions();
      org.apache.bcel.generic.InstructionHandle ifHandle =
         il.findHandle(ifIndex);
      sandmark.analysis.controlflowgraph.BasicBlock bb = cfg.getBlock(ifHandle);
      java.util.ArrayList usableVars = new java.util.ArrayList();
      for(int i=0; i < possibleVars.size(); i++){
         org.apache.bcel.generic.LocalVariableInstruction lvi =
            (org.apache.bcel.generic.LocalVariableInstruction)possibleVars.get(i);
         org.apache.bcel.generic.Type lvt = lvi.getType(cp);
         if(lvt.equals(org.apache.bcel.generic.Type.INT)){
            //check if in scope
            int lvIndex = lvi.getIndex();
            if(cfg.isInScope(lvIndex, bb))
               usableVars.add(lvi);
         }
      }

      return usableVars;
   }

   public static int getWatermarkType(sandmark.util.ConfigProperties props){
      String consts_or_rank = props.getProperty("Encode as constants");
      if(consts_or_rank.equals("true"))
         return USE_CONSTS;
      else
         return USE_RANK;
   }

   public static void fixTarget(org.apache.bcel.generic.InstructionList il,
      sandmark.program.Method m, int ifIndex){
      org.apache.bcel.generic.InstructionHandle target = getIfTarget(m,
         ifIndex);
      org.apache.bcel.generic.InstructionHandle lastIH = il.getEnd();
      org.apache.bcel.generic.Instruction lastInst = lastIH.getInstruction();
      org.apache.bcel.generic.IfInstruction lastIf = null;
      if(lastInst instanceof org.apache.bcel.generic.IfInstruction){
         lastIf = (org.apache.bcel.generic.IfInstruction)lastInst;
         lastIf.setTarget(target);
      }
   }
  
   public static org.apache.bcel.generic.InstructionHandle getIfTarget(
      sandmark.program.Method m, int ifIndex){

      org.apache.bcel.generic.InstructionHandle target = null;

      org.apache.bcel.generic.InstructionHandle ifHandle = 
         findIfHandle(m, ifIndex);
      org.apache.bcel.generic.Instruction ifInst =
         ifHandle.getInstruction();
      if(ifInst instanceof org.apache.bcel.generic.IfInstruction){
         org.apache.bcel.generic.IfInstruction startIf =
            (org.apache.bcel.generic.IfInstruction)ifInst;
         target = startIf.getTarget();
      }

      return target;
   }



   public static org.apache.bcel.generic.InstructionHandle getSlicingCrit(
      sandmark.program.Method m, 
      org.apache.bcel.generic.InstructionHandle ifHandle){

      if(m == null)
         return null;
      sandmark.util.newexprtree.MethodExprTree met = 
         new sandmark.util.newexprtree.MethodExprTree(m, false);
/*
      if(DEBUG){
         System.out.println("cfg");
         sandmark.analysis.controlflowgraph.MethodCFG cfg = m.getCFG();
         cfg.printCFG();
      }
*/
      org.apache.bcel.generic.InstructionHandle startHandle = null;

      if(DEBUG)System.out.println("ifHandle: " + ifHandle);

      sandmark.analysis.controlflowgraph.BasicBlock bb = met.getBlock(ifHandle);
      if(DEBUG)System.out.println("bb: " + bb);
      if(bb == null)
         return startHandle;
      sandmark.util.newexprtree.ExprTreeBlock etb = met.getExprTreeBlock(bb);
      java.util.ArrayList exprTrees = etb.getExprTrees();
      sandmark.util.newexprtree.ExprTree theET = null;
      

      FOUND_ET:
      for(int i=0; i < exprTrees.size(); i++){
         sandmark.util.newexprtree.ExprTree exprTree =
            (sandmark.util.newexprtree.ExprTree)exprTrees.get(i);
         java.util.ArrayList instList = exprTree.getInstructionList();
         if(DEBUG)System.out.println("instList size: " + instList.size());
         for(int j=0; j < instList.size(); j++){
            org.apache.bcel.generic.InstructionHandle ih = 
               (org.apache.bcel.generic.InstructionHandle)instList.get(j);
            if(ifHandle.equals(ih)){
               theET = (sandmark.util.newexprtree.ExprTree)exprTrees.get(i);
               break FOUND_ET;
            }
         }         
      }

      //once et is found get the uses of that tree
      if(theET != null){
         java.util.ArrayList uses = theET.getUses();
         if(DEBUG)System.out.println("size of uses: "  + uses.size());
         if(uses != null && uses.size() > 0)
            startHandle = (org.apache.bcel.generic.InstructionHandle)uses.get(0);
      }

      return startHandle;

   }//end getSlicingCrit

   
   public static String combineValues(java.util.ArrayList foundValues, 
           sandmark.util.ConfigProperties props){
      //sandmark.util.Random splitGenerator){
   
      int maxValue;
      if(props.getProperty("Encode as constants").equals("true"))
         maxValue = 1000; //this is a random maximum value 
      else
         maxValue = 8; // this is the number of opaque predicates
      //sandmark.util.splitint.PartialSumSplitter mySplitter =
         //new sandmark.util.splitint.PartialSumSplitter(splitGenerator);
      sandmark.util.splitint.CombinationSplitter mySplitter =
         new sandmark.util.splitint.CombinationSplitter(maxValue);
      //loop through foundValues putting them in an array. I am doing it this
      //way because to the toArray method does not seem to work.
      java.math.BigInteger[] wmValues = 
         new java.math.BigInteger[foundValues.size()]; 
      for(int i=0; i < foundValues.size(); i++){
         wmValues[i] = (java.math.BigInteger)foundValues.get(i);
      }
      java.math.BigInteger wm = mySplitter.combine(wmValues);
      return sandmark.util.StringInt.decode(wm);
      
   }

   private static java.util.ArrayList processAnnotations(
      sandmark.watermark.arboit.trace.TracePoint annotationPoints[],
      sandmark.program.Application app){

      java.util.ArrayList candidateList = new java.util.ArrayList();
      for(int i=0; i < annotationPoints.length; i++){
         sandmark.watermark.arboit.trace.TracePoint tp = annotationPoints[i];
         sandmark.util.ByteCodeLocation location = tp.location;
         sandmark.util.MethodID mid = location.getMethod();
         long index = location.getCodeIndex();
         String mSig = mid.getSignature();
         String mName = mid.getName();
         String className = mid.getClassName();
         sandmark.program.Class cls = app.getClass(className);
         sandmark.program.Method m = cls.getMethod(mName, mSig);
         //int ifIndex = (int)index + 3;
         int ifIndex = (int)index;
         org.apache.bcel.generic.InstructionList mil = m.getInstructionList();
         mil.setPositions();
         boolean notDone = true;
         do{
            org.apache.bcel.generic.InstructionHandle ih =
               mil.findHandle(ifIndex);
            if(ih == null){
               ifIndex = ifIndex - 3;
               continue;
            }
            org.apache.bcel.generic.Instruction inst = ih.getInstruction();
            if(inst instanceof org.apache.bcel.generic.IfInstruction)
               notDone = false;
            else
               ifIndex = ifIndex - 3;
         }while(notDone);
         Integer ifIntIndex = new Integer(ifIndex);
         java.util.ArrayList indexList = new java.util.ArrayList();
         indexList.add(ifIntIndex);
         Bundle b = new Bundle(className, m, indexList);
         if(candidateList.contains(b)){
            if(DEBUG)System.out.println("contained");
            int pos = candidateList.indexOf(b);
            Bundle oldB = (Bundle)candidateList.get(pos);
            (oldB.getIndexList()).addAll(indexList);
         }else{
            if(DEBUG)System.out.println("didn't contain");
            candidateList.add(b);
         }
         
      }

      return candidateList;

   }

   public static void removeAnnotations(sandmark.program.Application app){

      java.util.Iterator classes = app.classes();
      while(classes.hasNext()){
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         java.util.Iterator methods = cls.methods();
         while(methods.hasNext()){
            sandmark.program.Method m = 
               (sandmark.program.Method)methods.next();
            org.apache.bcel.generic.InstructionList mil =
               m.getInstructionList();
            org.apache.bcel.generic.InstructionHandle[] ihs =
               mil.getInstructionHandles();
            for(int i=0; i < ihs.length; i++){
               org.apache.bcel.generic.InstructionHandle ih =
                  ihs[i];
               org.apache.bcel.generic.Instruction inst = ih.getInstruction();
               if(inst instanceof org.apache.bcel.generic.INVOKESTATIC){
                  org.apache.bcel.generic.INVOKESTATIC ivinst =
                     (org.apache.bcel.generic.INVOKESTATIC)inst;
                  String methodName = ivinst.getMethodName(m.getCPG());
                  if(methodName.equals("sm$mark")){
                     try{
                        mil.delete(inst);
                     }catch (org.apache.bcel.generic.TargetLostException e){
                        sandmark.util.Log.message(0, "Instruction delete " +
                           "exception ignored.");
                     }
                  }
               }
            }
            m.mark();
         }
      }

   }

   public static boolean watermark(sandmark.program.Application app,
           sandmark.watermark.DynamicEmbedParameters params, 
           sandmark.util.ConfigProperties props,
      sandmark.watermark.arboit.trace.TracePoint annotationPoints[])
      throws sandmark.watermark.WatermarkingException {

      java.util.ArrayList candidateList = processAnnotations(annotationPoints,
         app);
      if(DEBUG){
         for(int i=0; i < candidateList.size(); i++){
            System.out.println("in list: " + candidateList.get(i));
         }
      }
      java.math.BigInteger[] wmValues = splitWM(params.watermark,props);
      if(DEBUG)System.out.println("split the watermark");

      return loop(candidateList, "", wmValues, props);
   }

   public static boolean watermark(sandmark.program.Application app,
           String watermark, String key,
           sandmark.util.ConfigProperties props) 
      throws sandmark.watermark.WatermarkingException {

      java.util.ArrayList candidateList = preprocess(app);
      java.math.BigInteger[] wmValues = splitWM(watermark,props);

      return loop(candidateList, key, wmValues, props);
   }

   private static Bundle getBundle(java.util.ArrayList candidateList, 
      sandmark.util.Random gen){

      int bundleIndex = Math.abs(gen.nextInt()) % candidateList.size();
      Bundle b = (Bundle)candidateList.get(bundleIndex);
      return b;
   }

   private static int getIf(Bundle b, sandmark.util.Random gen){
      java.util.ArrayList indexList = b.getIndexList();
      int ifRandomIndex = Math.abs(gen.nextInt()) % indexList.size();
      Integer ifIntIndex = (Integer)indexList.get(ifRandomIndex);
      int ifIndex = ifIntIndex.intValue();
      
      return ifIndex;
   }

   private static java.util.ArrayList getUsableVars(
      sandmark.program.Method m, int ifIndex){
      if(DEBUG)System.out.println("in getUsableVars");
      java.util.ArrayList usableVars = new java.util.ArrayList(); //null;

      //chose a slicing criterion
      org.apache.bcel.generic.InstructionHandle startHandle = 
         UtilFunctions.findSliceStart(m, ifIndex);
      if(DEBUG)System.out.println("chose a startHandle");

      //compute slice
      if(startHandle == null)
         return usableVars;

      if(DEBUG)System.out.println("StartHandle: " + startHandle);
      sandmark.analysis.slicingtools.ForwardMethodSlice fs = 
         new sandmark.analysis.slicingtools.ForwardMethodSlice(m,
         startHandle, true);

      //identify variables for opaque predicate
      usableVars = identifyUsableVars(
         fs, m, ifIndex);

      return usableVars;
   }

   private static boolean loop(java.util.ArrayList candidateList,String key,
      java.math.BigInteger[] wmValues, sandmark.util.ConfigProperties props) 
      throws sandmark.watermark.WatermarkingException {

      setSeed(key);
      
      sandmark.util.Random generator = sandmark.util.Random.getRandom();

      AlgOP myOPs = new AlgOP(false);

      int wmSlot = 0;
      boolean notDone = true;
      while(notDone){
         if(DEBUG)System.out.println("looping");
         int wm = wmValues[wmSlot].intValue();
         Bundle b = getBundle(candidateList, generator);
         sandmark.program.Method m = b.getMethod();
         //System.out.println("indexlist: " + b.getIndexList().size());
         if(DEBUG)System.out.println("got the method: " + m);
         int ifIndex = getIf(b, generator);
         if(DEBUG)System.out.println("m: " + m.getName() + " index: " + ifIndex);
         java.util.ArrayList usableVars = getUsableVars(m, ifIndex);
         if(DEBUG)System.out.println("usableVars.size()" + usableVars.size());
         if(usableVars.size() == 0){
            notDone = updateIndexList(candidateList, b, ifIndex);
            continue;
         }
         if(DEBUG){
            sandmark.analysis.controlflowgraph.MethodCFG cfg = m.getCFG();
            cfg.printCFG();
         }
         boolean success = myOPs.insertOpaquePredicate(m, usableVars, ifIndex, wm, props);
         notDone = updateIndexList(candidateList, b, ifIndex);
         m.mark();

         if(DEBUG){
            System.out.println("New Method Instructions");
            System.out.println(m.getInstructionList().toString());
            System.out.println("-------------------------");
         }

         if(success)
            wmSlot++;

         if(wmSlot >= wmValues.length)
            notDone = false;
      }

      if(notDone == false && wmSlot < wmValues.length)
         return false;
      else
         return true;

   }

   public static String recover(sandmark.program.Application app,
           sandmark.util.ConfigProperties props, 
      sandmark.watermark.arboit.trace.TracePoint annotationPoints[]){

      java.util.ArrayList possibleInsertionPoints =
         processAnnotations(annotationPoints, app);
      if(DEBUG){
         for(int i=0; i < possibleInsertionPoints.size(); i++){
            System.out.println(possibleInsertionPoints.get(i));
         }
      }
      java.util.ArrayList foundValues = new java.util.ArrayList();
      for(int i=0; i < possibleInsertionPoints.size(); i++){
         Bundle b = (Bundle)possibleInsertionPoints.get(i);
         sandmark.program.Method m = b.getMethod();
         java.util.ArrayList indexList = b.getIndexList();
         org.apache.bcel.generic.InstructionList mil = m.getInstructionList();
         mil.setPositions();
         sandmark.analysis.controlflowgraph.MethodCFG cfg =
            m.getCFG(false);
         for(int j=0; j < indexList.size(); j++){
            Integer iindex = (Integer)indexList.get(j);
            int index = iindex.intValue();
            org.apache.bcel.generic.InstructionHandle ih =
               mil.findHandle(index);
            if(props.getProperty("Use opaque methods").equals("true")){
               sandmark.analysis.controlflowgraph.BasicBlock bb =
                  cfg.getBlock(ih);
               sandmark.analysis.controlflowgraph.BasicBlock fallThroughBB =
                  bb.fallthrough();
               org.apache.bcel.generic.InstructionHandle lastHandle =
                  fallThroughBB.getLastInstruction();
               org.apache.bcel.generic.Instruction lastInst =
                  lastHandle.getInstruction();
               if(lastInst instanceof org.apache.bcel.generic.IfInstruction){
                  org.apache.bcel.generic.InstructionHandle prevIH =
                     lastHandle.getPrev();
                  org.apache.bcel.generic.Instruction prevInst =
                     prevIH.getInstruction();
                  if(prevInst instanceof
                     org.apache.bcel.generic.InvokeInstruction){
                     org.apache.bcel.generic.InvokeInstruction ivinst =
                        (org.apache.bcel.generic.InvokeInstruction)prevInst;
/*
                     int value = getMethodValue(ivinst, m, props);
                     if(value != 0){
                        Integer ivalue = new Integer(value);
                        String svalue = ivalue.toString();
                        java.math.BigInteger bi = new java.math.BigInteger(svalue);
                        foundValues.add(bi);
                     }
*/
                     foundValues.addAll(getMethodValue(ivinst, m, props));
                  }
               }
            }else{
               
               sandmark.analysis.controlflowgraph.BasicBlock bb =
                  cfg.getBlock(ih);
               sandmark.analysis.controlflowgraph.BasicBlock fallThroughBB =
                  bb.fallthrough();
               java.util.ArrayList blockInsts = fallThroughBB.getInstList(); //bb.getInstList();
               AlgOP myOPs = new AlgOP(true);
               int value = myOPs.isOpaque(blockInsts, getWatermarkType(props));
               if(value != 0){
                  Integer ivalue = new Integer(value);
                  String svalue = ivalue.toString();
                  java.math.BigInteger bi = new java.math.BigInteger(svalue);
                  foundValues.add(bi);
               }
            }
         }
      }

      //return UtilFunctions.combineValues(foundValues, getGenerator(props));
      return UtilFunctions.combineValues(foundValues, props);

   }
   
   public static String recover(sandmark.program.Application app,
           sandmark.util.ConfigProperties props){

      java.util.ArrayList foundValues = new java.util.ArrayList();
      java.util.Iterator classes = app.classes();
      while(classes.hasNext()){
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         java.util.Iterator methods = cls.methods();
         while(methods.hasNext()){
            sandmark.program.Method m = (sandmark.program.Method)methods.next();
            if(m.getInstructionList() == null)
               continue;
            if(props.getProperty("Use opaque methods").equals("true"))
               foundValues.addAll(recoverOpaqueMethod(m, props));
            else
               foundValues.addAll(recoverOpaqueInst(m, props));
         }
      }
 
      //return UtilFunctions.combineValues(foundValues, getGenerator(props));
      return UtilFunctions.combineValues(foundValues, props);
   }

   private static java.util.ArrayList recoverOpaqueInst(
      sandmark.program.Method m, sandmark.util.ConfigProperties props){

      java.util.ArrayList foundValues = new java.util.ArrayList();
      sandmark.analysis.controlflowgraph.MethodCFG cfg = m.getCFG(false);
      java.util.ArrayList blockList = cfg.getBlockList();
      AlgOP myOPs = new AlgOP(true);
      for(int i=0; i < blockList.size(); i++){
         sandmark.analysis.controlflowgraph.BasicBlock bb =
            (sandmark.analysis.controlflowgraph.BasicBlock)blockList.get(i);
         org.apache.bcel.generic.InstructionHandle lastIH =
            bb.getLastInstruction();
         org.apache.bcel.generic.Instruction lastInst = lastIH.getInstruction();
         if(lastInst instanceof org.apache.bcel.generic.IfInstruction){
            java.util.ArrayList blockInsts = bb.getInstList();
            int value = myOPs.isOpaque(blockInsts, getWatermarkType(props));
            if(value != 0){
               Integer ivalue = new Integer(value);
               String svalue = ivalue.toString();
               java.math.BigInteger bi = new java.math.BigInteger(svalue);
               foundValues.add(bi);
            }
         }
      }
      return foundValues;
   }

   private static java.util.ArrayList getMethodValue(
      org.apache.bcel.generic.InvokeInstruction ivinst,
      sandmark.program.Method m, sandmark.util.ConfigProperties props){
      
      java.util.ArrayList foundValues = new java.util.ArrayList();
      String methodName = ivinst.getMethodName(m.getCPG());
      String methodSig = ivinst.getSignature(m.getCPG());
      sandmark.program.Method possMeth = (m.getEnclosingClass()).getMethod(
         methodName, methodSig);
      AlgOP myOPs = new AlgOP(true);
      //int value = 0;
      if(DEBUG)System.out.println("methodSig: " + methodSig);
      if(myOPs.isPossible(methodSig) && possMeth != null){
         //System.out.println("possible");
/*
         org.apache.bcel.generic.InstructionList methodil =
            possMeth.getInstructionList();
         org.apache.bcel.generic.InstructionHandle[] ihs =
            methodil.getInstructionHandles();
         java.util.ArrayList ihList = new java.util.ArrayList();
         for(int j=0; j < ihs.length; j++)
            ihList.add(ihs[j]);
*/
         //what happens if we set exceptions matter to true for recognition?
         //we don't find any of the watermark
         sandmark.analysis.controlflowgraph.MethodCFG cfg =
            possMeth.getCFG(false);
         java.util.ArrayList blockList = cfg.getBlockList();
         for(int i=0; i < blockList.size(); i++){
            sandmark.analysis.controlflowgraph.BasicBlock bb =
               (sandmark.analysis.controlflowgraph.BasicBlock)blockList.get(i);
            org.apache.bcel.generic.InstructionHandle lastIH =
               bb.getLastInstruction();
            org.apache.bcel.generic.Instruction lastInst = lastIH.getInstruction();
            if(lastInst instanceof org.apache.bcel.generic.IfInstruction){
               java.util.ArrayList blockInsts = bb.getInstList();
               if(DEBUG)System.out.println(blockInsts.toString());
/*
         sandmark.util.newexprtree.MethodExprTree met = 
            new sandmark.util.newexprtree.MethodExprTree(m, m.getCPG(), false);
         java.util.ArrayList exprTreeBlocks = met.getExprTreeBlocks();
         for(int i=0; i < exprTreeBlocks.size(); i++){
            sandmark.util.newexprtree.ExprTreeBlock etb =
               (sandmark.util.newexprtree.ExprTreeBlock)exprTreeBlocks.get(i);
            java.util.ArrayList ets = etb.getExprTrees();
            for(int j=0; j < ets.size(); j++){
               sandmark.util.newexprtree.ExprTree et =
                  (sandmark.util.newexprtree.ExprTree)ets.get(j);
               java.util.ArrayList blockInsts = et.getInstructionList();
               System.out.println(blockInsts.toString());
               org.apache.bcel.generic.InstructionHandle lastIH =
                  (org.apache.bcel.generic.InstructionHandle)blockInsts.get(blockInsts.size()-1);
               org.apache.bcel.generic.Instruction lastInst =
                  lastIH.getInstruction();
               if(lastInst instanceof org.apache.bcel.generic.IfInstruction){
*/
                  int value = myOPs.isOpaque(blockInsts, getWatermarkType(props));
                  if(value != 0){
                     Integer ivalue = new Integer(value);
                     String svalue = ivalue.toString();
                     java.math.BigInteger bi = new java.math.BigInteger(svalue);
                     foundValues.add(bi);
                  }
               //}
            }
         }
  
         //value = myOPs.isOpaque(ihList, getWatermarkType(props));
      }
      return foundValues;
   }

   private static java.util.ArrayList recoverOpaqueMethod(sandmark.program.Method m,
           sandmark.util.ConfigProperties props){
     
      java.util.ArrayList foundValues = new java.util.ArrayList();
      org.apache.bcel.generic.InstructionList il = m.getInstructionList();
      if(il == null)
         return foundValues;
      
      org.apache.bcel.generic.Instruction[] insts = il.getInstructions();
      for(int i=0; i < insts.length; i++){
         org.apache.bcel.generic.Instruction inst = insts[i];
         if(inst instanceof org.apache.bcel.generic.InvokeInstruction){
            org.apache.bcel.generic.InvokeInstruction ivinst =
               (org.apache.bcel.generic.InvokeInstruction)inst;
/*
            String methodName = ivinst.getMethodName(m.getCPG());
            String methodSig = ivinst.getSignature(m.getCPG());
            sandmark.program.Method possMeth = (m.getEnclosingClass()).getMethod(
               methodName, methodSig);
            AlgOP myOPs = new AlgOP();
            if(myOPs.isPossible(methodSig)){
               org.apache.bcel.generic.InstructionList methodil =
                  possMeth.getInstructionList();
               org.apache.bcel.generic.InstructionHandle[] ihs =
                  methodil.getInstructionHandles();
               java.util.ArrayList ihList = new java.util.ArrayList();
               for(int j=0; j < ihs.length; j++)
                  ihList.add(ihs[j]);
               int value = myOPs.isOpaque(ihList, getWatermarkType(props));
*/
/*
               int value = getMethodValue(ivinst, m, props);
               if(value != 0){
                  Integer ivalue = new Integer(value);
                  String svalue = ivalue.toString();
                  java.math.BigInteger bi = new java.math.BigInteger(svalue);
                  foundValues.add(bi);
               }
*/
               foundValues.addAll(getMethodValue(ivinst, m, props));
            //}
         }
      }
      return foundValues;
   }
}//end class
