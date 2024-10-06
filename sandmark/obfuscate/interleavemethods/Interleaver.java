package sandmark.obfuscate.interleavemethods;

/** This class allows you to interleave two methods.
    It returns an 
    <link>sandmark.obfuscate.interleavemethods.InterleavedMethod</link>.
    You may pass it the bytes you wish to use as the key values, or let
    the Interleave choose them itself (after which you can retrieve them
    from the returned InterleavedMethod). Note that A and B are not removed
    from the class.
*/

public class Interleaver {      
   private static boolean DEBUG = false;
   private sandmark.program.Application app;
   private sandmark.program.Class randomClass;
   private sandmark.program.Method constructor, nextInt;   
   private int random_slot, pc_slot, arg_slot, array_slot;
   private org.apache.bcel.generic.InstructionHandle switchIH;
   private org.apache.bcel.generic.ConstantPoolGen cpg;
   private org.apache.bcel.generic.InstructionFactory factory;
   private java.util.ArrayList ils1, ils2;
   private java.util.HashMap start2end, 
      old2new = new java.util.HashMap(), new2old = new java.util.HashMap();

    
   public Interleaver(){
      randomClass = sandmark.program.LibraryClass.find("java.util.Random");
      if(randomClass == null){
         System.out.println("Couldn't find \"java.util.Random\"!");
         throw new RuntimeException();
      }
      constructor = randomClass.getMethod("<init>", "(J)V");
      nextInt = randomClass.getMethod("nextInt", "()I");  
   }

   public  InterleavedMethod interleave
      (sandmark.program.Method A,
       sandmark.program.Method B){   
       
      sandmark.program.Method C = A.copy();
      addByteArg(C);
      return interleave(A, B, C);
   }
   /** Interleave A and B into existing method C 
       (All members of C will be replaced).
   */
   public  InterleavedMethod interleave
      (sandmark.program.Method A,
       sandmark.program.Method B,
       sandmark.program.Method C){

      byte byteA = randomByte();       
      return interleave(A, B, C, byteA);
   }
   /** Interleave A and B into existing method C,
       C's sig is same as A and B's, with a byte appended.
       (All members of C will be replaced). 
       @param byteA the key byte value for Method A;
       i.e., calls to A(...) should be replaced with
       calls to A(...,byteA)
   */
   public  InterleavedMethod interleave
      (sandmark.program.Method A,
       sandmark.program.Method B,
       sandmark.program.Method C,
       byte byteA){
      byte byteB = randomByte();
      return interleave(A, B, C, byteA, byteB);
   }
   /** Interleave A and B into existing method C 
       (All members of C will be replaced). 
       @param byteA the key byte value for Method A;
       i.e., calls to A(...) should be replaced with
       calls to A(...,byteA)
       @param byteB the key byte value for Method A;
       i.e., calls to B(...) should be replaced with
       calls to B(...,byteB)
   */
   public  InterleavedMethod interleave
      (sandmark.program.Method A,
       sandmark.program.Method B,
       sandmark.program.Method C,
       byte byteA,
       byte byteB){        
      /* method A(...,int V) { S1;...Sk; } 
       * method B(...) { T1;...Tm; }
       * **************becomes -->
       * method C(...,byte V)
       *    <fsm>
       */   

      if(DEBUG) {
         System.out.println("Merging " + A.getEnclosingClass() +
                            "." + A + " and " + 
                            B.getEnclosingClass() + "." + B + 
                            " into " + C.getEnclosingClass() + 
                            "." + C);                   
         System.out.println("A is \n" + A.getInstructionList());
         System.out.println("B is \n" + B.getInstructionList());

         org.apache.bcel.generic.CodeExceptionGen[] exceptions =
            A.getExceptionHandlers();
         System.out.println("exception handlers for A: ");
         for(int i = 0; i < exceptions.length; i++)
            System.out.println(exceptions[i]);             
         exceptions = B.getExceptionHandlers();
         System.out.println("exception handlers for B: ");
         for(int i = 0; i < exceptions.length; i++)
            System.out.println(exceptions[i]);                       
      }

      org.apache.bcel.generic.InstructionList AIL =
         A.getInstructionList();

      if(AIL == null || AIL.size() == 0){
         AIL = new org.apache.bcel.generic.InstructionList();
         //so we can getStart()
         AIL.insert(new org.apache.bcel.generic.NOP());
         A.setInstructionList(AIL);
      }
            
      org.apache.bcel.generic.InstructionList BIL =
         B.getInstructionList(); 
        
      if(BIL == null || BIL.size() == 0){
         BIL = new org.apache.bcel.generic.InstructionList();
         //so we can getStart()
         BIL.insert(new org.apache.bcel.generic.NOP());
         B.setInstructionList(BIL);
      }

      arg_slot = InterleaveUtil.getCount(C) - 1;
       
      cpg = C.getConstantPool();
      //We first sync the local vars so verifier finds no type mismatches 
      InterleaveUtil.syncLocalVars(A, B);
      C.setMaxLocals();
      C.removeLocalVariables();
      C.removeLineNumbers();

      //random_slot = InterleaveUtil.getSlot(A, B);
      A.setMaxLocals();
      B.setMaxLocals();
      random_slot = Math.max(A.calcMaxLocals(), B.calcMaxLocals());
      array_slot = random_slot+1;
      pc_slot = random_slot+2;
      factory = new org.apache.bcel.generic.InstructionFactory(cpg);      
        
      //set ils1 and ils2.
      getBlocks(A, B);     
      start2end = getStart2End();
     

      org.apache.bcel.generic.InstructionList randomCode =
         getRandomCode(); 

      //So we have a block that has a load w/ no store before it.
      //It wont verify because there is path which doesnt store
      //before loading. So we insert a bunch of fake store instructions
      //before the switch code.
      java.util.Set badLoads = getBadLoads(ils1, A);
      badLoads.addAll(getBadLoads(ils2, B));        

      randomCode.append(insertFakeStores(badLoads));
                
      randomCode.append(getSwitchCode(A, B, byteA, byteB));            
      randomCode.setPositions();        
       
      C.setInstructionList(randomCode);              
       
        
      C.removeExceptionHandlers();
      fixExceptions(A.getExceptionHandlers(), C);
      fixExceptions(B.getExceptionHandlers(), C);
      updateTargets(C.getInstructionList());     

      if(DEBUG){
         System.out.println(C.getInstructionList());
         System.out.println(C.getConstantPool());        
         org.apache.bcel.generic.CodeExceptionGen[]
            exceptions = C.getExceptionHandlers();
         System.out.println("exception handlers for C: ");
         for(int i = 0; i < exceptions.length; i++){
            System.out.println(exceptions[i]);
         }
      }

      return new InterleavedMethod(C, byteA, byteB);
   }

   //Move all exceptions into C, with the updated IH's, of course.
   //This involves splitting any single Exc. Hand. that go across
   //blocks.
   private void fixExceptions
      (org.apache.bcel.generic.CodeExceptionGen[] exceptions,
       sandmark.program.Method C){

      for(int i = 0; i < exceptions.length; i++){
         org.apache.bcel.generic.InstructionHandle ih =
            exceptions[i].getStartPC();            
         org.apache.bcel.generic.InstructionHandle endOfException =
            (org.apache.bcel.generic.InstructionHandle)
            old2new.get(exceptions[i].getEndPC());
         if(endOfException == null)
            throw new RuntimeException("lost instruction");
         do{                
            org.apache.bcel.generic.InstructionHandle newIH =
               (org.apache.bcel.generic.InstructionHandle)old2new.get(ih);
            org.apache.bcel.generic.InstructionHandle end = newIH;
            while(end.getNext() != null && end != endOfException && 
                  new2old.containsKey(end.getNext()))
               end = end.getNext();
            org.apache.bcel.generic.InstructionHandle oldEnd = 
               (org.apache.bcel.generic.InstructionHandle)new2old.get(end);
            ih = oldEnd.getNext();
            if(!C.getInstructionList().contains
               ((org.apache.bcel.generic.InstructionHandle)
                old2new.get(exceptions[i].getHandlerPC())))
               throw new RuntimeException("Someone is losing instructions");
            C.addExceptionHandler
               (newIH,end,
                (org.apache.bcel.generic.InstructionHandle)old2new.get
                (exceptions[i].getHandlerPC()),
                exceptions[i].getCatchType());      
         }while(ih != null && ih.getPrev() != exceptions[i].getEndPC());           
      }
   }
   //If EndOfException is in same block as ih, return EndOfException.
   //else return the last instruction in the block
   private org.apache.bcel.generic.InstructionHandle getEnd
      (org.apache.bcel.generic.InstructionHandle endOfException,
       org.apache.bcel.generic.InstructionHandle ih){
      do{
         if(ih == endOfException)
            return ih;
         ih = ih.getNext();
      }while(!start2end.containsValue(ih));
      return ih;   
   }

   private java.util.HashMap getStart2End(){
      java.util.HashMap retVal = new java.util.HashMap();
      for(int i = 0; i < ils1.size(); i++)
         retVal.put(((org.apache.bcel.generic.InstructionList)ils1.get(i)).getStart(),
                    ((org.apache.bcel.generic.InstructionList)ils1.get(i)).getEnd());
      for(int i = 0; i < ils2.size(); i++)
         retVal.put(((org.apache.bcel.generic.InstructionList)ils2.get(i)).getStart(),
                    ((org.apache.bcel.generic.InstructionList)ils2.get(i)).getEnd());
      return retVal;
   }

   //For every InstructionList in list, see if it has a
   //load which doesnt come after a store. Also check if it stores
   //an array.  If so, add its
   //(type, index) tuple to retVal
   private java.util.Set getBadLoads(java.util.ArrayList list, sandmark.program.Method M){
      java.util.Set retVal = new java.util.HashSet();           
      sandmark.analysis.stacksimulator.StackSimulator ss =
         new sandmark.analysis.stacksimulator.StackSimulator(M);
        
        
      for(int i = 0; i < list.size(); i++){
         org.apache.bcel.generic.InstructionList il =
            (org.apache.bcel.generic.InstructionList)list.get(i);
         //         org.apache.bcel.generic.Instruction[] instrs = il.getInstructions();
         org.apache.bcel.generic.InstructionHandle[] handles = 
            il.getInstructionHandles();
         for(int j = 0; j < handles.length; j++){
            java.util.HashSet stores = new java.util.HashSet();
            if(handles[j].getInstruction() instanceof org.apache.bcel.generic.StoreInstruction){
               org.apache.bcel.generic.StoreInstruction store =
                  (org.apache.bcel.generic.StoreInstruction)handles[j].getInstruction();

               //Check if we're storing an array
               org.apache.bcel.generic.InstructionHandle origIH =
                  (org.apache.bcel.generic.InstructionHandle)new2old.get(handles[j]);
               sandmark.analysis.stacksimulator.StackData data[] =
                  ss.getInstructionContext(origIH).getStackAt(0);
               org.apache.bcel.generic.Type arrayType = null;
               for(int k = 0 ; k < data.length ; k++) {
                  if (data[k].getInstruction()==null)
                     continue;

                  org.apache.bcel.generic.Instruction pushInst =  
                     data[k].getInstruction().getInstruction();
                    
                  if(pushInst instanceof org.apache.bcel.generic.MULTIANEWARRAY ||
                     pushInst instanceof org.apache.bcel.generic.ANEWARRAY) {
                     org.apache.bcel.generic.Type t =
                        ((org.apache.bcel.generic.LoadClass)
                         pushInst).getType(M.getConstantPool());
                     if(arrayType != null && !t.equals
                        (org.apache.bcel.generic.Type.OBJECT) &&
                        !t.equals(arrayType))
                        throw new RuntimeException
                           ("this variable might be two incompatible types: " +
                            t + " and " + arrayType);
                     arrayType = t;
                  } 
               }
               if(arrayType != null) {
                  if(store.getIndex() > arg_slot)
                     retVal.add(new TypeIndex(arrayType, store.getIndex()));
                  //System.out.println("Array type/index: " + arraytype + 
                  //"/" + store.getIndex());
               }                    
               stores.add(new Integer(store.getIndex()));
            }
            if(handles[j].getInstruction() instanceof org.apache.bcel.generic.LoadInstruction){
               org.apache.bcel.generic.LoadInstruction load =
                  (org.apache.bcel.generic.LoadInstruction)handles[j].getInstruction();
               int index = load.getIndex();
               TypeIndex tuple = new TypeIndex(load.getType(cpg), index);
               if(index > arg_slot && 
                  !stores.contains(new Integer(index))){                        
                  retVal.add(tuple);                        
               }
            }
         }
      }        
      return retVal;
   }     
    
   //Take a collection of (type, index) tuples and create 
   //instructions that store null values of type 'type' in index 'index'
   private org.apache.bcel.generic.InstructionList insertFakeStores
      (java.util.Collection badLoads) {
      org.apache.bcel.generic.InstructionList il =
         new org.apache.bcel.generic.InstructionList();
      java.util.Iterator it = badLoads.iterator();
      while(it.hasNext()){
         TypeIndex tuple = (TypeIndex)it.next(); 
         if(tuple.type instanceof org.apache.bcel.generic.ArrayType){
            org.apache.bcel.generic.ArrayType arraytype = 
               (org.apache.bcel.generic.ArrayType)tuple.type;                
            short dim = (short)arraytype.getDimensions();
            for(int i = 0; i < dim; i++)
               il.append(factory.createConstant(new Integer(0)));
            il.append(factory.createNewArray(arraytype, dim));
         }
         else{
            il.append(org.apache.bcel.generic.InstructionFactory.createNull(tuple.type));
         }
         il.append(org.apache.bcel.generic.InstructionFactory.createStore(tuple.type, tuple.index));
      }
      return il;
   }
    
   //Creates the bytecode equivalent to:
   //java.util.Random r = new java.util.Random(arg[index]);    
   // {create array code}
   private  org.apache.bcel.generic.InstructionList getRandomCode(){
      cpg.addClass("java.util.Random");
      cpg.addMethodref("java.util.Random", "<init>", "(J)V");
      cpg.addMethodref("java.util.Random", "nextInt", "()I");         

      org.apache.bcel.generic.InstructionList il =
         new org.apache.bcel.generic.InstructionList();
      //creates the code: Random r = new Random(seed)
      il.append(factory.createNew(new org.apache.bcel.generic.ObjectType
                                  ("java.util.Random")));
      il.append(org.apache.bcel.generic.InstructionFactory.createDup(1));
      il.append(org.apache.bcel.generic.InstructionFactory.createLoad
                (org.apache.bcel.generic.BasicType.INT,
                 arg_slot));
      il.append(new org.apache.bcel.generic.I2L());
       
      il.append(factory.createInvoke
                ("java.util.Random",
                 "<init>",
                 constructor.getReturnType(),
                 constructor.getArgumentTypes(),
                 org.apache.bcel.Constants.INVOKESPECIAL));
      il.append(org.apache.bcel.generic.InstructionFactory.createStore
                (org.apache.bcel.generic.BasicType.OBJECT, random_slot));
      //create an array of random numbers in array_slot
      il.append(createArray());                  
      il.setPositions();
      return il;        
   }
   /*Generates the bytecode equivalent to:
     array = new int[size];
     for(int pc = 0; pc < size; pc++)
     array[pc] = r.nextInt();
     pc = 0
   */
   private org.apache.bcel.generic.InstructionList createArray(){
      int numBlocks = ils1.size() + ils2.size();
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();
                   
      il.append(factory.createConstant(new Integer(numBlocks)));
      il.append(new org.apache.bcel.generic.NEWARRAY
                (org.apache.bcel.generic.BasicType.INT));
      il.append(org.apache.bcel.generic.InstructionFactory.createStore
                (org.apache.bcel.generic.BasicType.OBJECT,
                 array_slot));

      il.append(factory.createConstant(new Integer(0)));
      il.append(org.apache.bcel.generic.InstructionFactory.createStore
                (org.apache.bcel.generic.BasicType.INT,
                 pc_slot));
      org.apache.bcel.generic.InstructionHandle start = 
         il.append(org.apache.bcel.generic.InstructionFactory.createLoad
                   (org.apache.bcel.generic.BasicType.INT,
                    pc_slot));
      il.append(factory.createConstant(new Integer(numBlocks)));
      org.apache.bcel.generic.InstructionHandle icmp = 
         il.append(new org.apache.bcel.generic.IF_ICMPGE(null));
      il.append(org.apache.bcel.generic.InstructionFactory.createLoad
                (org.apache.bcel.generic.BasicType.OBJECT,
                 array_slot));
      il.append(org.apache.bcel.generic.InstructionFactory.createLoad
                (org.apache.bcel.generic.BasicType.INT,
                 pc_slot));
        
      il.append(org.apache.bcel.generic.InstructionFactory.createLoad
                (org.apache.bcel.generic.BasicType.OBJECT, random_slot));
      il.append(factory.createInvoke
                (nextInt.getClassName(),
                 nextInt.getName(),
                 nextInt.getReturnType(),
                 nextInt.getArgumentTypes(),
                 org.apache.bcel.Constants.INVOKEVIRTUAL));
      il.append(new org.apache.bcel.generic.IASTORE());
      il.append(new org.apache.bcel.generic.IINC(pc_slot,1));
      il.append(new org.apache.bcel.generic.GOTO(start));

      //pc = 0   
      ((org.apache.bcel.generic.BranchInstruction)icmp.getInstruction()).
         setTarget(il.append(factory.createConstant(new Integer(0))));
      il.append(org.apache.bcel.generic.InstructionFactory.createStore
                (org.apache.bcel.generic.BasicType.INT,
                 pc_slot)); 
      il.setPositions();       
      return il;
   }

   private void getBlocks(sandmark.program.Method A,
                          sandmark.program.Method B){
      //Get the basic blocks in order
      sandmark.analysis.controlflowgraph.MethodCFG cfgA =
         A.getCFG();
      sandmark.analysis.controlflowgraph.MethodCFG cfgB =
         B.getCFG();

      java.util.ArrayList blocks1 = 
         sandmark.diff.methoddiff.DMDiffAlgorithm.getBlocksInOrder(cfgA);  
      java.util.ArrayList blocks2 = 
         sandmark.diff.methoddiff.DMDiffAlgorithm.getBlocksInOrder(cfgB);              

      sandmark.analysis.stacksimulator.StackSimulator ss1 =
         new sandmark.analysis.stacksimulator.StackSimulator(A);
      // 	for(org.apache.bcel.generic.InstructionHandle ih =
      // 		B.getInstructionList().getStart() ; ih != null ; 
      // 	    ih = ih.getNext()) {
      // 	    System.out.println(ih + ":");
      // 	    org.apache.bcel.generic.InstructionTargeter targeters[] =
      // 		ih.getTargeters();
      // 	    for(int i = 0 ; targeters != null && i < targeters.length ; i++)
      // 		System.out.println("  " + targeters[i]);
      // 	}
      sandmark.analysis.stacksimulator.StackSimulator ss2 =
         new sandmark.analysis.stacksimulator.StackSimulator(B);
       
      //Not going to use bb's directly, merge s.t. every group verfifies        
      ils1 = group(A,blocks1, ss1);              
      ils2 = group(B,blocks2, ss2);
   }

   //translates methods with basic blocks Method A(a1...an), Method B(b1..bm)
   //into fsm, ie: switch(pc){ case X: {bb }... }
   private  org.apache.bcel.generic.InstructionList getSwitchCode
      (sandmark.program.Method A,
       sandmark.program.Method B,         
       byte byteA, byte byteB){
      org.apache.bcel.generic.InstructionList il =
         new org.apache.bcel.generic.InstructionList();
      //First insert the last two lines, which update PC            
      org.apache.bcel.generic.InstructionHandle end = 
         il.append(new org.apache.bcel.generic.IINC(pc_slot, 1));
              
      //Use byteA as random seed
      java.util.Random r = new java.util.Random(byteA);       
      //And map basic blocks -> random val, which will used for 'case'
      java.util.HashMap il2value = new java.util.HashMap();
      for(int i = 0; i < ils1.size(); i++) {
         org.apache.bcel.generic.InstructionList tmp =            
            (org.apache.bcel.generic.InstructionList)ils1.get(i);

         updateTargets(tmp);
         Integer rint = new Integer(r.nextInt());
         il2value.put(tmp, rint); 
         //Every instruction knows its id            
         putAll(tmp, new Integer(i));
      }      
      //same for b
      r = new java.util.Random(byteB);
       
      for(int i = 0; i < ils2.size(); i++){
         org.apache.bcel.generic.InstructionList tmp =            
            (org.apache.bcel.generic.InstructionList)ils2.get(i);
         updateTargets(tmp);
         Integer rint = new Integer(r.nextInt());
         il2value.put(tmp, rint);       
         //Every instruction knows its id
         putAll(tmp, new Integer(i));                        
      }      
      if(DEBUG)            
         System.out.println(target2value);
        
      //Now extract all the blocks from the set, which are ordered by hash,
      //and put them in the list, updating matches/targets
      java.util.HashMap match2target = new java.util.HashMap();
      java.util.Iterator it = il2value.keySet().iterator();
        
      //Put at end for now, will move to front later
      switchIH = il.append(org.apache.bcel.generic.InstructionFactory.createLoad
                           (org.apache.bcel.generic.BasicType.OBJECT,
                            array_slot));

      while(it.hasNext()){            
         org.apache.bcel.generic.InstructionList tmp = 
            (org.apache.bcel.generic.InstructionList)it.next();           

         tmp.append(new org.apache.bcel.generic.GOTO(end));            
         fixBranches(tmp);
      }        
      for(java.util.Iterator ils = il2value.keySet().iterator() ;
          ils.hasNext() ; ) {
         org.apache.bcel.generic.InstructionList tmp =
            (org.apache.bcel.generic.InstructionList)ils.next();
         il.insert(tmp);
         //for sorting
         match2target.put(il2value.get(tmp), il.getStart());
      }
      il.setPositions();    
      //Retrieve the matches and targets by match     
      //and put the switch at the beginning
      Object[][] sorted = getSortedMatches(match2target);
      int[] matches = demoteIntegers(sorted[0]);
      org.apache.bcel.generic.InstructionHandle[] targets =
         demoteIHs(sorted[1]);
                       
      il.move(switchIH, il.insert(new org.apache.bcel.generic.NOP()));
      org.apache.bcel.generic.InstructionHandle ih =             
         il.append(switchIH, org.apache.bcel.generic.InstructionFactory.createLoad
                   (org.apache.bcel.generic.BasicType.INT,
                    pc_slot));
      ih = il.append(ih, new org.apache.bcel.generic.IALOAD());        
        
      il.append(ih, new org.apache.bcel.generic.LOOKUPSWITCH
                (matches, targets, end));
      //and branch back at the end
      il.append(new org.apache.bcel.generic.GOTO(switchIH));        
      il.setPositions();         
      return il;       
   }

   private void putAll(org.apache.bcel.generic.InstructionList il,
                       Object o){
      org.apache.bcel.generic.InstructionHandle[] ihs =
         il.getInstructionHandles();
      for(int i = 0; i < ihs.length; i++)
         target2value.put(ihs[i], o);        
   }

   java.util.HashMap target2value = new java.util.HashMap();

   //Branching around will break the fsm. So instead of branching,
   //we just update pc. E.g. ifge->target; break; will become:
   // ifge->+2
   // break;
   // pc += [change]
   // goto->target
   private void fixBranches(org.apache.bcel.generic.InstructionList il){       

      org.apache.bcel.generic.InstructionHandle[] instrs = 
         il.getInstructionHandles();
      //Dont want to touch last instructin, the goto (aka break)
      for(int i = 0; i < instrs.length-1; i++){
         //If we have branch, first increment pc accordingly.
         if(instrs[i].getInstruction() instanceof 
            org.apache.bcel.generic.BranchInstruction){

            java.util.HashSet targets = new java.util.HashSet();
            
            org.apache.bcel.generic.BranchInstruction bi = 
               (org.apache.bcel.generic.BranchInstruction)instrs[i].
               getInstruction();
            targets.add(bi.getTarget());

            if(bi instanceof org.apache.bcel.generic.Select) {
               org.apache.bcel.generic.Select select =
                  (org.apache.bcel.generic.Select)bi;
               org.apache.bcel.generic.InstructionHandle cases[] =
                  select.getTargets();
               for(int j = 0 ; j < cases.length ; j++)
                  targets.add(cases[j]);
            }
            // now targets contains all targets of this branch

            for(java.util.Iterator targetIt = targets.iterator() ; targetIt.hasNext() ; ) {
               org.apache.bcel.generic.InstructionHandle newTarget =
                  (org.apache.bcel.generic.InstructionHandle)targetIt.next();

               if(newTarget.getPrev() != null) {
                  // if newTarget is not the first instruction..
                  if(il.contains(newTarget))
                     continue; //Don't fixup branches inside a block
                  else
                     throw new RuntimeException
                        ("can't jump into the middle of another block " + 
                         newTarget + " " + instrs[i] + " " + newTarget.getPrev());
               }
               // newTarget is the first instruction

               int nextVal = ((Integer)target2value.get(newTarget)).intValue();
               int currVal = ((Integer)target2value.get(instrs[i])).intValue();
               int val =  nextVal - currVal;                        
               org.apache.bcel.generic.GOTO go = 
                  new org.apache.bcel.generic.GOTO(newTarget);
                  
               bi.updateTarget(newTarget,il.append
                               (new org.apache.bcel.generic.IINC(pc_slot, val)));
               il.append(go);
            }
         }
      }
   }

   // redirects all branches according to old2new
   private void updateTargets(org.apache.bcel.generic.InstructionList il){
      java.util.Iterator it = old2new.keySet().iterator();
      while(it.hasNext()){
         org.apache.bcel.generic.InstructionHandle oldIH =
            (org.apache.bcel.generic.InstructionHandle)it.next();
         org.apache.bcel.generic.InstructionHandle newIH =
            (org.apache.bcel.generic.InstructionHandle)old2new.get(oldIH);

         il.redirectBranches(oldIH, newIH);
      }            
   }

   // returns true if oldList contains a key that maps to 'o' under old2new
   private boolean newListContains(java.util.Set oldList, Object o){
      for(java.util.Iterator i = oldList.iterator(); i.hasNext(); ){
         Object updated = old2new.get(i.next());            
         if(updated != null && updated == o)
            return true;
      }
      return false;        
   }

   //Return true if can verify il
   private short verify(org.apache.bcel.generic.InstructionList il,
                        org.apache.bcel.generic.ConstantPoolGen cpg,
                        sandmark.analysis.stacksimulator.StackSimulator ss,
                        sandmark.analysis.defuse.ReachingDefs rd){        
      org.apache.bcel.generic.Instruction[] instrs = il.getInstructions();
      
      org.apache.bcel.generic.InstructionHandle oldStart = 
         (org.apache.bcel.generic.InstructionHandle)new2old.get(il.getStart());
      if(ss.getInstructionContext(oldStart).getStackSize() != 0){
         if(true)            
            ;//System.out.println("STACK > 0: " + il.getStart());
         return BAD_TARGET;
      }

      org.apache.bcel.generic.InstructionHandle oldEnd =
         (org.apache.bcel.generic.InstructionHandle)new2old.get(il.getEnd());
      if(ss.getInstructionContext(oldEnd,false).getStackSize() != 0) {
         //System.out.println("bad exit stack");
         return FAILED;
      }

      for (org.apache.bcel.generic.InstructionHandle ih = il.getStart() ; 
          ih != null ; ih = ih.getNext()) {
         org.apache.bcel.generic.InstructionTargeter targeters[] =
            ((org.apache.bcel.generic.InstructionHandle)
             new2old.get(ih)).getTargeters();
         for(int i = 0 ; targeters != null && i != targeters.length ; i++) {
            if(targeters[i] instanceof 
               org.apache.bcel.generic.CodeExceptionGen &&
               !il.contains((org.apache.bcel.generic.InstructionHandle)
                            old2new.get
                            (((org.apache.bcel.generic.CodeExceptionGen)
                              targeters[i]).getEndPC()))) {
               //System.out.println("bad exception");
               return FAILED;
            }
            if(ih.getPrev() != null && targeters[i] instanceof
               org.apache.bcel.generic.Instruction &&
               !il.contains((org.apache.bcel.generic.Instruction)
                            targeters[i])) {
               //System.out.println("bad branch");
               return FAILED;
            }
         }

         if(ih.getInstruction() instanceof org.apache.bcel.generic.RET) {
            java.util.Set defs = 
               rd.defs((org.apache.bcel.generic.InstructionHandle)
                       new2old.get(ih));
            for(java.util.Iterator defIt = defs.iterator() ; 
                defIt.hasNext() ;) {
               sandmark.analysis.defuse.InstructionDefWrapper wrapper =
                  (sandmark.analysis.defuse.InstructionDefWrapper)defIt.next();
               if(!il.contains((org.apache.bcel.generic.InstructionHandle)
                               old2new.get(wrapper.getIH()))) {
                  //System.out.println("ret " + old2new.get(wrapper.getIH()) + " " +
                  //new2old.get(ih) + " " + wrapper.getIH());
                  return FAILED;
               }
            }
         }
      }

      //System.out.println("verified");
      return VERIFIED;
   }

   private final short VERIFIED = 0, FAILED = 1, 
      BAD_TARGET = 2, ILLEGAL_PEEK = 3;

   //Take an ArrrayList of in order basic blocks and return an ArrayList of
   //InstructionList objects, s.t each InstructionList can verify
   private java.util.ArrayList group
      (sandmark.program.Method method,java.util.ArrayList blocks,
       sandmark.analysis.stacksimulator.StackSimulator ss){
      java.util.ArrayList retVal = new java.util.ArrayList();
      org.apache.bcel.generic.InstructionList tmp = 
         new org.apache.bcel.generic.InstructionList();
      sandmark.analysis.defuse.ReachingDefs rd = 
         new sandmark.analysis.defuse.ReachingDefs(method);
      for(int i = 0; i < blocks.size(); i++){
         sandmark.analysis.controlflowgraph.BasicBlock bb = 
            (sandmark.analysis.controlflowgraph.BasicBlock)blocks.get(i);

         tmp.append(getInstructionList(bb));            
            
         boolean DEBUG2 = false && DEBUG;
         if(DEBUG2)
            System.out.println("Trying to verify:\n" + tmp);            
         switch(verify(tmp,cpg, ss, rd)){
         case VERIFIED: {                                   
            retVal.add(tmp);
            tmp = new org.apache.bcel.generic.InstructionList();
            if(DEBUG2) 
               System.out.println("Passed.\n");
            break;
         }                    
         case FAILED: {  
            if(DEBUG2)
               System.out.println("Failed.\n");
            break;
         }
         case BAD_TARGET:
         case ILLEGAL_PEEK: {                    
            /*This means we have an instruction that reads
              from an empty stack or il.getStart() can't start. 
              Such instructions may not lead off case statements, so attach to previous.
            */                
            if(DEBUG2)
               System.out.println("Illegal peek, appending to last");
            if(retVal.size() > 0) {
               org.apache.bcel.generic.InstructionList il =
                  (org.apache.bcel.generic.InstructionList)retVal.remove
                  (retVal.size()-1);
               il.append(tmp);
               if(verify(il,cpg,ss,rd) == VERIFIED) {
                  retVal.add(il);
                  tmp = new org.apache.bcel.generic.InstructionList();
               } else {
                  tmp = il;
               }
            } else               
               throw new RuntimeException
                  ("method starts with a block that requires " + 
                   "something on the stack");
            break;
         }            
         }            
      }
      //Have some code left over, add it            
      if(tmp.size() > 0){
         if(retVal.size() == 0) {
            if(verify(tmp,cpg,ss,rd) != VERIFIED) {
               System.out.println(retVal);
               System.out.println
                  (((sandmark.analysis.controlflowgraph.BasicBlock)
                    blocks.get(0)).graph().method().getInstructionList());
               throw new RuntimeException("adding bad list 0");
            }
            retVal.add(tmp);
         } else {
            ((org.apache.bcel.generic.InstructionList)retVal.
             get(retVal.size()-1)).append(tmp);
            while(verify((org.apache.bcel.generic.InstructionList)
                         retVal.get(retVal.size() - 1),cpg,ss,rd) !=
                  VERIFIED) {
               if(retVal.size() == 1) {
                  System.out.println(retVal);
                  System.out.println
                     (((sandmark.analysis.controlflowgraph.BasicBlock)
                       blocks.get(0)).graph().method().getInstructionList());
                  throw new RuntimeException
                     ("adding bad list 1");
               }
               ((org.apache.bcel.generic.InstructionList)retVal.get
                (retVal.size() - 2)).append
                  ((org.apache.bcel.generic.InstructionList)
                   retVal.get(retVal.size() - 1));
               retVal.remove(retVal.size() - 1);
            }
         }
      }

      return retVal;
   }

   private org.apache.bcel.generic.InstructionList getInstructionList
      (sandmark.analysis.controlflowgraph.BasicBlock bb){
      org.apache.bcel.generic.InstructionList tmp =
         new org.apache.bcel.generic.InstructionList();
      java.util.ArrayList instrs = bb.getInstList();
      //In case it's empty we can call getstart anyway            
      if(instrs.size() == 0)                
         tmp.append(new org.apache.bcel.generic.NOP());

      for(int i = 0; i < instrs.size(); i++){
         org.apache.bcel.generic.Instruction inst =
            ((org.apache.bcel.generic.InstructionHandle)
             instrs.get(i)).getInstruction();
            
         org.apache.bcel.generic.InstructionHandle newInst = null;
         if(inst instanceof org.apache.bcel.generic.BranchInstruction)
            newInst = tmp.append
               ((org.apache.bcel.generic.BranchInstruction)inst);
         else
            newInst = tmp.append(inst);
         old2new.put(instrs.get(i), newInst);            
         new2old.put(newInst, instrs.get(i));
      }
      return tmp;
   }    

   private static int[] demoteIntegers(Object[] a){
      int[] retVal = new int[a.length];
      for(int i = 0; i < a.length; i++)
         retVal[i] = ((Integer)a[i]).intValue();
      return retVal;
   }

   private static org.apache.bcel.generic.InstructionHandle[] 
      demoteIHs(Object[] a){
      org.apache.bcel.generic.InstructionHandle[] retVal = 
         new org.apache.bcel.generic.InstructionHandle[a.length];
      for(int i = 0; i < a.length; i++)
         retVal[i] = ((org.apache.bcel.generic.InstructionHandle)a[i]);
      return retVal;
   }   
    
   private static Object[][] getSortedMatches(java.util.HashMap map){
      Object[][] retVal = new Object[2][map.size()];
      int ctr = 0;
      java.util.Iterator it = new java.util.TreeSet(map.keySet()).iterator();
      while(it.hasNext()){
         Integer key = (Integer)it.next();
         retVal[0][ctr] = key;
         retVal[1][ctr++] = map.get(key);
      }        
      return retVal;
   }         

   private static void addByteArg(sandmark.program.Method c){
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

   private static byte randomByte(){
      java.util.Random random = sandmark.util.Random.getRandom();
      byte[] bytes = new byte[1];
      random.nextBytes(bytes);
      return bytes[0];
   }
}
