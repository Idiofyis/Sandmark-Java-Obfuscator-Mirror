package sandmark.analysis.controlflowgraph;

public class MethodCFG extends sandmark.util.newgraph.MutableGraph {
   private sandmark.util.newgraph.DomTree dominator;
   private sandmark.util.newgraph.DomTree postDominator;

   sandmark.program.Method method;

   sandmark.analysis.controlflowgraph.BasicBlock source;
   sandmark.analysis.controlflowgraph.BasicBlock sink;

   int maxLocals;
   protected int mBlockCounter;

   java.util.Hashtable instr2bb;

   public MethodCFG(sandmark.program.Method method){
      this(method, true);
   }

   public MethodCFG(sandmark.program.Method method,
                    boolean exceptionsMatter) {
      this.method = method;

      instr2bb = new java.util.Hashtable(101);

      if(method.getInstructionList() == null || 
         method.getInstructionList().getLength() == 0)
         throw new EmptyMethodException();

      method.getInstructionList().setPositions();

      source = newBlock();
      sink = newBlock();

      //first we have to identify the leaders
      java.util.ArrayList leaders = identifyLeaders
         (method.getInstructionList(),method.getExceptionHandlers(),
          exceptionsMatter);
      java.util.ArrayList blockList = 
         buildBlocks(method.getInstructionList(),leaders);
      buildEdges(method.getExceptionHandlers(),blockList);

      setMaxLocals(method.getMaxLocals());

      consolidate();
   }


   /** Removes all nodes that are not reachable from the source.
    */
   public void removeUnreachable(){
      super.removeUnreachable(source);
      for (java.util.Iterator bbiter=nodes();bbiter.hasNext();){
         BasicBlock bb = (BasicBlock)bbiter.next();
         while (bb.fallthrough()!=null && !hasNode(bb.fallthrough())){
            bb.setFallthrough(bb.fallthrough().fallthrough());
         }
      }
   }

   public void graphChanged() {
      this.dominator = null;
      this.postDominator = null;
   }

   public sandmark.program.Method method(){
      return method;
   }

   class BasicBlockIterator implements java.util.Iterator {
      private java.util.Iterator it;
      private Object o;
      private boolean needNext;
      BasicBlockIterator() {
         it = nodes();
         needNext = true;
      }
      private boolean getNext() {
         if(!needNext)
            return true;
            
         while(needNext && it.hasNext()) {
            o = it.next();
            if(o != source && o != sink)
               needNext = false;
         }

         return !needNext;
      }
      public boolean hasNext() {
         return getNext();
      }
      public Object next() {
         if(!getNext())
            throw new java.util.NoSuchElementException();

         needNext = true;
         return o;
      }
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   public java.util.Iterator basicBlockIterator() {
      return new BasicBlockIterator();
   }

   public int maxLocals() {
      return maxLocals;
   }

   public void setMaxLocals(int newMaxLocals) {
      maxLocals = newMaxLocals;
   }

   public java.util.ArrayList getBlockList(){
      java.util.ArrayList al = new java.util.ArrayList();

      for(java.util.Iterator it = basicBlockIterator() ; it.hasNext() ; )
         al.add(it.next());

      return al;
   }

   /** @deprecated */
   public boolean isInScope(int checkIndex, BasicBlock bb){
      sandmark.analysis.controlflowgraph.MethodCFG mCFG = bb.graph();
      sandmark.program.Method method = mCFG.method();
      org.apache.bcel.generic.LocalVariableGen[] lvgs = 
         method.getLocalVariables();
      org.apache.bcel.generic.LocalVariableGen lvg = null;
      int i;
      for(i = 0; i < lvgs.length; i++){
         lvg = lvgs[i];
         int index = lvg.getIndex();
         if(index == checkIndex)
            break;
      }

      if(i == lvgs.length)
         return false;
         
      //org.apache.bcel.classfile.LocalVariableTable lvt =
      //method.getLocalVariableTable();
      //System.out.println("local variable table " + lvt);
      //org.apache.bcel.classfile.LocalVariable lv = 
      //lvt.getLocalVariable(index);
      org.apache.bcel.generic.InstructionHandle startHandle = lvg.getStart();
      org.apache.bcel.generic.InstructionHandle endHandle = lvg.getEnd();
      int start = startHandle.getPosition();
      int end = endHandle.getPosition();

      //int length = lv.getLength();
      org.apache.bcel.generic.InstructionHandle blockStartHandle =
         bb.getIH();
      int startHandleIndex = blockStartHandle.getPosition();
      org.apache.bcel.generic.InstructionHandle blockEndHandle =
         bb.getLastInstruction();
      int endHandleIndex = blockEndHandle.getPosition();
      if((start <= startHandleIndex) && (end >= endHandleIndex))
         return true;
      else
         return false;
   }

   public BasicBlock getBlock(org.apache.bcel.generic.InstructionHandle searchHandle){
      return (BasicBlock)instr2bb.get(searchHandle);
   }

   private java.util.ArrayList buildBlocks(org.apache.bcel.generic.InstructionList il,
                                           java.util.ArrayList leaders) {
      org.apache.bcel.generic.InstructionHandle ihs[] =
         il.getInstructionHandles();

      java.util.ArrayList blockList = new java.util.ArrayList();
      for(int i = 0 ; i < leaders.size() ; i++){
         blockList.add(newBlock());
      }

      sandmark.analysis.controlflowgraph.BasicBlock prev = null;

      for(int i = 0 ; i < leaders.size() ; i++) {
         sandmark.analysis.controlflowgraph.BasicBlock block =
            (sandmark.analysis.controlflowgraph.BasicBlock)blockList.get(i);
         int first = ((Integer)leaders.get(i)).intValue();
         // End of block is the instruction before the next leader,
         // or if there is no next leader, then the last instruction
         int next = i == leaders.size() - 1 ?
            ihs.length : ((Integer)leaders.get(i + 1)).intValue();
         for(int j = first ; j < next ; j++){
            block.addInst(ihs[j]);
         }
         if(prev != null) {
            prev.setFallthrough(block);
         }
         prev = block;
      }
      return blockList;
   }

   private void buildEdges(org.apache.bcel.generic.CodeExceptionGen[] exceptions,
                           java.util.ArrayList blockList){
      addEdge(source, blockList.get(0));

      //      sandmark.analysis.classhierarchy.ClassHierarchy ch = 
      //         method.getApplication().getHierarchy();

      java.util.Iterator iter = blockList.iterator();
      
      java.util.ArrayList physSuccList = new java.util.ArrayList();

      org.apache.bcel.generic.InstructionHandle ih;
      for (ih = method.getInstructionList().getStart(); ih != null; 
           ih = ih.getNext()) {
         org.apache.bcel.generic.Instruction insn = 
            ih.getInstruction();
         if (insn instanceof org.apache.bcel.generic.JsrInstruction) {
            org.apache.bcel.generic.JsrInstruction jsr =
               (org.apache.bcel.generic.JsrInstruction)insn;
            BasicBlock succ = getBlock(jsr.physicalSuccessor());
            physSuccList.add(succ);
         }
      }

      while(iter.hasNext()){
         BasicBlock curr = (BasicBlock)iter.next();
         org.apache.bcel.generic.InstructionHandle nextHandle = null;
         java.util.ArrayList currInstList = curr.getInstList();
         int length = currInstList.size();
         org.apache.bcel.generic.InstructionHandle lastInstHandle =
            (org.apache.bcel.generic.InstructionHandle) currInstList.get(length-1);
         org.apache.bcel.generic.Instruction lastInst =
            lastInstHandle.getInstruction();

         nextHandle = lastInstHandle.getNext();

         if(lastInst instanceof org.apache.bcel.generic.IfInstruction){

            //add egde between this block and the target block
            org.apache.bcel.generic.IfInstruction iInst =
               (org.apache.bcel.generic.IfInstruction)lastInst;
            org.apache.bcel.generic.InstructionHandle thandle =
               iInst.getTarget();

            //find the block that is the target block
            BasicBlock block = getBlock(thandle);
            addEdge(curr, block);

            //add egde between this block and the next block
            block = getBlock(nextHandle);
            addEdge(new FallthroughEdge(curr, block));
            if(nextHandle == null || curr.fallthrough().getIH() != nextHandle)
               throw new RuntimeException("bad fallthrough");

         }else if(lastInst instanceof org.apache.bcel.generic.JsrInstruction){

            //add an edge to the target of the jsr
            org.apache.bcel.generic.JsrInstruction jInst =
               (org.apache.bcel.generic.JsrInstruction)lastInst;
            org.apache.bcel.generic.InstructionHandle thandle =
               jInst.getTarget();

            BasicBlock block = getBlock(thandle);
            addEdge(curr, block);
         }else if(lastInst instanceof org.apache.bcel.generic.GotoInstruction){

            //add edge to the target of the goto
            org.apache.bcel.generic.BranchInstruction gInst =
               (org.apache.bcel.generic.BranchInstruction)lastInst;
            org.apache.bcel.generic.InstructionHandle ghandle =
               gInst.getTarget();

            BasicBlock block = getBlock(ghandle);
            addEdge(curr, block);

         }else if(lastInst instanceof org.apache.bcel.generic.Select){

            //add edges to all targets of the tableswitch or lookupswitch
            org.apache.bcel.generic.Select tsInst =
               (org.apache.bcel.generic.Select)lastInst;

            //get default target and add edge
            org.apache.bcel.generic.InstructionHandle tHandle =
               tsInst.getTarget();
            BasicBlock next = getBlock(tHandle);
            addEdge(curr, next);

            //get the rest of the targets and add the edges
            org.apache.bcel.generic.InstructionHandle[] targetHandles =
               tsInst.getTargets();
            for(int i = 0; i < targetHandles.length; i++){
               org.apache.bcel.generic.InstructionHandle thisHandle =
                  targetHandles[i];
               //System.out.println("This handle: " + thisHandle);
               next = getBlock(thisHandle);
               addEdge(curr, next);
            }
         }else if(lastInst instanceof org.apache.bcel.generic.ReturnInstruction){
            //returns connect to the sink
            addEdge(curr, sink);

         }else if(lastInst instanceof org.apache.bcel.generic.RET){
            //have to search to find the physical successors of the jsr
            BasicBlock next = null;
            int listSize = physSuccList.size();
            if(listSize > 0){
               for (int i = 0; i < listSize; i++){
                  next = (BasicBlock)physSuccList.get(0);
                  physSuccList.remove(0);
                  addEdge(curr, next);
               }
            }else{
               addEdge(curr, sink);
            }
            //added 8-31-02
         }else if(lastInst instanceof org.apache.bcel.generic.ATHROW){
            // do not connect to next block!

         }else if(nextHandle != null) { //connect with the next block, if there is one
            BasicBlock next = getBlock(nextHandle);
            if(next == null)
               throw new RuntimeException("no block for " + nextHandle + 
                                          " after " + lastInstHandle);
            addEdge(new FallthroughEdge(curr, next));
         }

         if(lastInst instanceof org.apache.bcel.generic.ExceptionThrower){
            boolean outOfBounds = true;
            boolean addedEdge = false;
            int instPos = lastInstHandle.getPosition();
            
            for(int i = 0; i < exceptions.length; i++){
               org.apache.bcel.generic.CodeExceptionGen ceg = exceptions[i];
               org.apache.bcel.generic.InstructionHandle start =
                  ceg.getStartPC();
               int startPos = start.getPosition();
               if(instPos >= startPos){
                  org.apache.bcel.generic.InstructionHandle end =
                     ceg.getEndPC();
                  int endPos = end.getPosition();
                  if(instPos <= endPos){
                     outOfBounds = false;
                     org.apache.bcel.generic.InstructionHandle handler =
                        ceg.getHandlerPC();
                     BasicBlock next = getBlock(handler);
                     addEdge(new ExceptionEdge(curr, next, ceg));
                     addedEdge = true;
                  }
               }
            }

            if(lastInst instanceof org.apache.bcel.generic.ATHROW){
               addEdge(new ExceptionEdge
                       (curr, sink, 
                        (org.apache.bcel.generic.CodeExceptionGen)null));
            }
         }
      }
   }


   /**
    * Creates a new BasicBlock with the next available label.
    */
   sandmark.analysis.controlflowgraph.BasicBlock newBlock(){
      sandmark.analysis.controlflowgraph.BasicBlock block =
         new sandmark.analysis.controlflowgraph.BasicBlock(this);
      addBlock(block);
      return block;
   }

   public void addBlock(BasicBlock bb){
      addNode(bb);
   }


   public void printCFG(){
      for(java.util.Iterator it = basicBlockIterator() ; it.hasNext() ; ) {
         sandmark.analysis.controlflowgraph.BasicBlock block = 
            (sandmark.analysis.controlflowgraph.BasicBlock) it.next();
         System.out.println(block);
         System.out.println("successors: ");
         java.util.Iterator succIter = succs(block);
         while (succIter.hasNext())
            System.out.println(succIter.next());
      }

   }

   public BasicBlock source(){
      return source;
   }

   public BasicBlock sink(){
      return sink;
   }

   /**
    * Returns a list of blocks where the edge between slot 0 and slot 1 is a
    * back edge, the edge between slot 2 and slot 3 is a back edge, etc.
    */
   public java.util.ArrayList getBackedges(){
      java.util.ArrayList backedges = new java.util.ArrayList();

      for(java.util.Iterator it = basicBlockIterator() ; it.hasNext() ; ) {
         BasicBlock block = (BasicBlock)it.next();
         java.util.Iterator succIter = succs(block);
         while(succIter.hasNext()){
            BasicBlock succ = (BasicBlock)succIter.next();
            if(dominates(succ,block)){ //found a backedge
               backedges.add(block);
               backedges.add(succ);
            }
         }
      }
	
      return backedges;
   }

   private static java.util.Hashtable sClassNameToParentClassName;
   static {
      sClassNameToParentClassName = new java.util.Hashtable();
      sClassNameToParentClassName.put("java.lang.ClassCircularityError","java.lang.LinkageError");
      sClassNameToParentClassName.put("java.lang.LinkageError","java.lang.Error");
      sClassNameToParentClassName.put("java.lang.Error","java.lang.Throwable");
      sClassNameToParentClassName.put("java.lang.ClassFormatError","java.lang.LinkageError");
      sClassNameToParentClassName.put("java.lang.ExceptionInInitializerError","java.lang.LinkageError");
      sClassNameToParentClassName.put("java.lang.IncompatibleClassChangeError","java.lang.LinkageError");
      sClassNameToParentClassName.put("java.lang.AbstractMethodError","java.lang.IncompatibleClassChangeError");
      sClassNameToParentClassName.put("java.lang.IllegalAccessError","java.lang.IncompatibleClassChangeError");
      sClassNameToParentClassName.put("java.lang.InstantiationError","java.lang.IncompatibleClassChangeError");
      sClassNameToParentClassName.put("java.lang.NoSuchFieldError","java.lang.IncompatibleClassChangeError");
      sClassNameToParentClassName.put("java.lang.NoSuchMethodError","java.lang.IncompatibleClassChangeError");
      sClassNameToParentClassName.put("java.lang.NoClassDefFoundError","java.lang.LinkageError");
      sClassNameToParentClassName.put("java.lang.UnsatisfiedLinkError","java.lang.LinkageError");
      sClassNameToParentClassName.put("java.lang.VerifyError","java.lang.LinkageError");
      sClassNameToParentClassName.put("java.lang.NullPointerException","java.lang.RuntimeException");
      sClassNameToParentClassName.put("java.lang.RuntimeException","java.lang.Exception");
      sClassNameToParentClassName.put("java.lang.Exception","java.lang.Throwable");
      sClassNameToParentClassName.put("java.lang.ArrayIndexOutOfBoundsException","java.lang.IndexOutOfBoundsException");
      sClassNameToParentClassName.put("java.lang.IndexOutOfBoundsException","java.lang.RuntimeException");
      sClassNameToParentClassName.put("java.lang.ArithmeticException","java.lang.RuntimeException");
      sClassNameToParentClassName.put("java.lang.NegativeArraySizeException","java.lang.RuntimeException");
      sClassNameToParentClassName.put("java.lang.ClassCastException","java.lang.RuntimeException");
      sClassNameToParentClassName.put("java.lang.IllegalMonitorStateException","java.lang.RuntimeException");
   }

   private java.util.ArrayList identifyLeaders
      (org.apache.bcel.generic.InstructionList il,
       org.apache.bcel.generic.CodeExceptionGen exceptions[],
       boolean exceptionsMatter) {
      org.apache.bcel.generic.InstructionHandle ihs[] =
         il.getInstructionHandles();
      java.util.Hashtable unknownIndexTargets = new java.util.Hashtable();
      java.util.TreeSet leaders = new java.util.TreeSet();
      java.util.Hashtable handleToIndex = new java.util.Hashtable();
	
      //make the first instruction a leader
      unknownIndexTargets.put(il.getStart(),il.getStart());
      for(int i = 0 ; i < exceptions.length ; i++) {
         unknownIndexTargets.put(exceptions[i].getStartPC(),"");
         unknownIndexTargets.put(exceptions[i].getHandlerPC(),"");
         org.apache.bcel.generic.InstructionHandle ih =
            exceptions[i].getEndPC();
         if(ih.getNext() != null)
            unknownIndexTargets.put(ih.getNext(),ih.getNext());
      }

      for(int j=0; j < ihs.length; j++){
         if(unknownIndexTargets.get(ihs[j]) != null) {
            leaders.add(new Integer(j));
            unknownIndexTargets.remove(ihs[j]);
         }
         handleToIndex.put(ihs[j],new Integer(j));

         org.apache.bcel.generic.Instruction instruction =
            ihs[j].getInstruction();
	    
         if((instruction instanceof org.apache.bcel.generic.IfInstruction) ||
            (instruction instanceof org.apache.bcel.generic.GotoInstruction)){
            //get position of next instruction
            if(j + 1 < ihs.length) {
               Integer pos = new Integer(j+1);
               leaders.add(pos);
            }

            //get handle of target of if or goto instruction
            org.apache.bcel.generic.BranchInstruction instruct =
               (org.apache.bcel.generic.BranchInstruction) instruction;
            org.apache.bcel.generic.InstructionHandle handle =
               instruct.getTarget();

            //add handle to target list so that we can make all of the targets
            //into leaders at the end.
            Integer ndx;
            if((ndx = (Integer)handleToIndex.get(handle)) != null)
               leaders.add(ndx);
            else
               unknownIndexTargets.put(handle,handle);
         }

         if(instruction instanceof org.apache.bcel.generic.JsrInstruction){
            //get position of next instruction
            if(j + 1 < ihs.length) {
               Integer pos = new Integer(j+1);
               leaders.add(pos);
            }

            //get handle of target of jsr instruction
            org.apache.bcel.generic.JsrInstruction jInstruct =
               (org.apache.bcel.generic.JsrInstruction) instruction;
            org.apache.bcel.generic.InstructionHandle handle =
               jInstruct.getTarget();

            //add handle to target list so that we can make all of the targets
            //into leaders at the end.
            Integer ndx;
            if((ndx = (Integer)handleToIndex.get(handle)) != null)
               leaders.add(ndx);
            else
               unknownIndexTargets.put(handle,handle);

            //get the handle of the physical successor of the jsr instruction
            //this handle is where the jsr will return to from the RET.
            handle = jInstruct.physicalSuccessor();
            if(handle != ihs[j + 1])
               throw new RuntimeException("jsr weirdness");
            if((ndx = (Integer)handleToIndex.get(handle)) != null)
               leaders.add(ndx);
            else
               unknownIndexTargets.put(handle,handle);
         }

         if(instruction instanceof org.apache.bcel.generic.Select){
            //get position of next instruction
            if(j + 1 < ihs.length){
               Integer pos = new Integer(j+1);
               leaders.add(pos);
            }

            //get handles of target of either a tableswitch or a lookupswitch
            org.apache.bcel.generic.Select sInstruc =
               (org.apache.bcel.generic.Select) instruction;
            org.apache.bcel.generic.InstructionHandle[] handles =
               sInstruc.getTargets();

            //loop through targets adding the handler to the leaders
            for(int i = 0; i < handles.length; i++) {
               Integer ndx;
               if((ndx = (Integer)handleToIndex.get(handles[i])) != null)
                  leaders.add(ndx);
               else
                  unknownIndexTargets.put(handles[i],handles[i]);
            }

            Integer ndx = (Integer)handleToIndex.get(sInstruc.getTarget());
            if(ndx != null)
               leaders.add(ndx);
            else
               unknownIndexTargets.put(sInstruc.getTarget(),sInstruc.getTarget());
         }

         if(instruction instanceof org.apache.bcel.generic.ReturnInstruction){
            if(j != ihs.length-1){
               Integer pos = new Integer(j+1);
               leaders.add(pos);
            }
         }

         if(exceptionsMatter &&
            instruction instanceof org.apache.bcel.generic.ExceptionThrower &&
            j + 1 < ihs.length) {
            boolean mayBeCaught = true;
            /* Code in preparation for reduction in nodes and edges
               java.lang.Class thrownExceptions[] =
               ((org.apache.bcel.generic.ExceptionThrower)instruction).getExceptions();
               for(int i = 0 ; i < thrownExceptions.length ; i++)
               for(int k = 0 ; k < exceptions.length ; k++)
               if(isSuperclassOf(thrownExceptions[i].getName(),
               exceptions[k].getCatchType().getClassName()) ||
               isSuperclassOf(exceptions[k].getCatchType().getClassName(),
               thrownExceptions[i].getName()))
               mayBeCaught = true;
            */
            if(mayBeCaught) {
               Integer pos = new Integer(j+1);
               leaders.add(pos);
            }
         }
         if(instruction instanceof org.apache.bcel.generic.RET &&
            j + 1 < ihs.length){
            Integer pos = new Integer(j+1);
            leaders.add(pos);
         }
      }
      return new java.util.ArrayList(leaders);
   }
                                                                                                                                                                                                       
   public void rewriteInstructionList() {
      org.apache.bcel.generic.InstructionList il = 
         method().getInstructionList();
      /* We insert a NOP and then delete it at the end because otherwise,
         in some cases, all the instructions in the method magically
         disappear.  */
      org.apache.bcel.generic.InstructionHandle lastGood = 
         il.insert(new org.apache.bcel.generic.NOP());
      java.util.Set visitedBlocks = new java.util.HashSet();
      java.util.ArrayList stack = new java.util.ArrayList();
      for(java.util.Iterator nodes = nodes() ; nodes.hasNext() ; ) {
         Object node = nodes.next();
         if(node != source())
            stack.add(node);
      }
      stack.add(source());
      while(!stack.isEmpty()) {
         BasicBlock block = (BasicBlock)stack.remove(stack.size() - 1);
         if(visitedBlocks.contains(block))
            continue;
         visitedBlocks.add(block);

         java.util.ArrayList instrs = block.getInstList();
         for(int i = 0 ; i < instrs.size() ; i++) {
            org.apache.bcel.generic.InstructionHandle ih =
               (org.apache.bcel.generic.InstructionHandle)instrs.get(i);
            il.move(ih,lastGood);
            lastGood = ih;
         }
         for(java.util.Iterator succIt = succs(block) ; succIt.hasNext() ; ) {
            BasicBlock succ = (BasicBlock)succIt.next();
            // Visit a block if it has not been visited and
            // it is not the fallthrough block of another block
            if(!visitedBlocks.contains(succ) && succ.fallthroughFrom() == null)
               stack.add(succ);
         }
         if(block.fallthrough() != null)
            stack.add(block.fallthrough());
      }
      try {
         il.delete(il.getStart());
      } catch(org.apache.bcel.generic.TargetLostException e) {
         throw new Error("This really shouldn't happen");
      }

      // this is a new bugfix to get rid of spurious instructions that
      // do not appear in any BB (untested! may be buggy)
      for (org.apache.bcel.generic.InstructionHandle ih=lastGood.getNext();
           ih!=null; ){
         org.apache.bcel.generic.InstructionTargeter targeters[] =
            ih.getTargeters();
         for(int i = 0 ; targeters != null && i < targeters.length ; i++)
            targeters[i].updateTarget(ih,null);
      }
      
      for(org.apache.bcel.generic.InstructionHandle tmp,ih = lastGood.getNext() ; 
          ih != null ; ) {
         tmp = ih.getNext();
         try {
            il.delete(ih);
         } catch(org.apache.bcel.generic.TargetLostException e) {
            throw new Error("This really shouldn't happen");
         }
         ih = tmp;
      }
      //// END OF FIX /////

      il.setPositions(true);
   }

   public int getPreOrderIndex(BasicBlock node){
      return getPreOrder().indexOf(node);
   }

   public int getPostOrderIndex(BasicBlock node){
      return getPostOrder().indexOf(node);
   }

   public java.util.ArrayList getPreOrder(){
      java.util.ArrayList al = new java.util.ArrayList();
      for(java.util.Iterator it = depthFirst(source) ; it.hasNext() ; )
         al.add(it.next());

      return al;
   }

   public java.util.ArrayList getPostOrder(){
      java.util.ArrayList al = new java.util.ArrayList();
      for(java.util.Iterator it = postOrder(source) ; it.hasNext() ; )
         al.add(it.next());

      return al;
   }

   private sandmark.util.newgraph.DomTree buildDominator(boolean reverse) {
      if(reverse && postDominator != null)
         return postDominator;
      else if(reverse)
         return (postDominator = graph().reverse().dominatorTree(sink));
      else if(dominator != null)
         return dominator;
      else
         return (dominator = dominatorTree(source));
   }

   public boolean dominates(BasicBlock dominator,BasicBlock dominatee) {
      return buildDominator(false).reachable(dominator,dominatee);
   }

   public boolean postDominates(BasicBlock postDominator,BasicBlock postDominatee) {
      return buildDominator(true).reachable(postDominator,postDominatee);
   }

   public BasicBlock getDominator(BasicBlock b) {
      return (BasicBlock)buildDominator(false).immediateDominator(b);
   }

   public BasicBlock getPostDominator(BasicBlock b) {
      return (BasicBlock)buildDominator(true).immediateDominator(b);
   }
    
   public java.util.HashSet getDominators(BasicBlock b) {
      java.util.HashSet dominators = new java.util.HashSet();
      for(java.util.Iterator doms = buildDominator(false).dominators(b) ; 
          doms.hasNext() ; )
         dominators.add(doms.next());
      return dominators;
   }

   public java.util.HashSet getPostDominators(BasicBlock b) {
      java.util.HashSet dominators = new java.util.HashSet();
      for(java.util.Iterator doms = buildDominator(true).dominators(b) ; 
          doms.hasNext() ; )
         dominators.add(doms.next());
      return dominators;
   }
   public boolean edgeIsFallthrough(BasicBlock src,BasicBlock sink) {
      if(!hasEdge(src,sink))
         return false;

      for(java.util.Iterator edges = outEdges(src) ; edges.hasNext() ; ) {
         sandmark.util.newgraph.Edge edge = 
            (sandmark.util.newgraph.Edge)edges.next();
         if(edge instanceof FallthroughEdge && edge.sinkNode() == sink)
            return true;
      }

      return false;
   }

   public String toString() { return ProgramCFG.fieldOrMethodName(this); }
   public int hashCode() { return toString().hashCode(); }
}

