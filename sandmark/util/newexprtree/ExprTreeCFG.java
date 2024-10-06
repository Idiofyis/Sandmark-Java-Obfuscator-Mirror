package sandmark.util.newexprtree;

/** This class is a CFG of expression trees. Each basic block 
 *  is made up of a sequential list of expression trees (Expr objects)
 *  that represent the actions of the code. The actual nodes in this
 *  graph are ArrayLists of Exprs. The edges are those used in MethodCFG
 *  (i.e. EdgeImpl, FallthroughEdge, and ExceptionEdge). This class does 
 *  not rely on InstructionHandles, so it must make copies of the 
 *  CodeExceptionGen information. These are encapsulated in the ExceptionInfo
 *  class. For each CodeExceptionGen in the original method, there will be a 
 *  corresponding ExceptionInfo instance in this graph, pointing to the target Exprs
 *  rather than the target handles. When re-creating the method (rewriteMethod),
 *  the old CodeExceptionGens are scrapped and re-created based on the ExceptionInfos.
 *  Also, the LocalVariable and LineNumber tables must be scrapped.
 *  As soon as an ExprTreeCFG instance is created, the method's instruction list
 *  may be altered. 
 */
public class ExprTreeCFG
   extends sandmark.util.newgraph.MutableGraph 
   implements org.apache.bcel.Constants{

   private sandmark.program.Method method;
   private org.apache.bcel.generic.InstructionList ilist;
   private int maxlocals;
   private BasicBlock source;
   private BasicBlock sink;
   private ExceptionInfo[] exceptionInfo;

   /** Builds an ExprTreeCFG for the given method.
    */
   public ExprTreeCFG(sandmark.program.Method _method) throws Throwable{
      method = _method;
      ilist = method.getInstructionList();

      sandmark.analysis.stacksimulator.StackSimulator stack = 
         method.getStack();

      maxlocals = method.calcMaxLocals();
      fixInstructions(stack);
      stack=null;
     
      // the list of handles making up the blocks, in top-down order.
      java.util.ArrayList[] blocks = buildBlocks();


      // copy out the handle indexes for each exception handler
      org.apache.bcel.generic.CodeExceptionGen[] handlers = 
         method.getExceptionHandlers();
      int[] exceptionStarts = new int[handlers.length];
      int[] exceptionEnds = new int[handlers.length];
      int[] exceptionHandlers = new int[handlers.length];
      for (int i=0;i<handlers.length;i++){
         for (int j=0;j<blocks.length;j++){
            if (handlers[i].getStartPC()==blocks[j].get(0)){
               exceptionStarts[i] = j;
            }
            if (handlers[i].getHandlerPC()==blocks[j].get(0)){
               exceptionHandlers[i] = j;
            }
            if (handlers[i].getEndPC()==blocks[j].get(blocks[j].size()-1)){
               exceptionEnds[i] = j;
            }
         }
      }


      java.util.ArrayList sourceList = new java.util.ArrayList();
      java.util.ArrayList sinkList = new java.util.ArrayList();

      // add the arraylist nodes to this graph, and build the edges between them.
      buildEdges(blocks, sourceList, sinkList);

      // get the Expr trees that correspond to the basic blocks.
      // (this array is parallel to 'blocks', and we make use of that fact)
      BasicBlock[] treelists = buildTrees(blocks);

      // create the ExceptionInfo instances, now that we have the trees.
      exceptionInfo = new ExceptionInfo[handlers.length];
      for (int i=0;i<handlers.length;i++){
         exceptionInfo[i] = new ExceptionInfo(handlers[i].getCatchType(),
                                              (Expr)treelists[exceptionStarts[i]].getInstList().get(0),
                                              (Expr)treelists[exceptionEnds[i]].getInstList().get
                                              (treelists[exceptionEnds[i]].getInstList().size()-1),
                                              (Expr)treelists[exceptionHandlers[i]].getInstList().get(0));
      }
      
      // right now the nodes are ArrayLists of handles...
      // replace them with ArrayLists of Exprs, leaving the edges the same.
      fixGraph(treelists, blocks, sourceList, sinkList);
   }

   /** Returns the exception handlers for this method.
    */
   public ExceptionInfo[] getExceptionHandlers(){
      return exceptionInfo;
   }


   /** This method replaces the handle list nodes with Expr list nodes, 
    *  and sets branch targets correctly.
    *  'treelists' and 'blocks' are parallel arrays.
    */
   private void fixGraph(BasicBlock[] treelists, 
                         java.util.ArrayList[] blocks,
                         java.util.ArrayList sourceList,
                         java.util.ArrayList sinkList){

      // reset BranchExpr targets
      for (int i=0;i<treelists.length;i++){
         for (int m=0;m<treelists[i].getInstList().size();m++){
            Expr expr = (Expr)treelists[i].getInstList().get(m);

            if (expr instanceof BranchExpr){
               BranchExpr branch = (BranchExpr)expr;
               org.apache.bcel.generic.InstructionHandle target = 
                  branch.getHandleTarget();
               for (int j=0;j<blocks.length;j++){
                  if (blocks[j].get(0)==target){
                     branch.setTarget((Expr)treelists[j].getInstList().get(0));
                     break;
                  }
               }

               if (branch instanceof SwitchExpr){
                  SwitchExpr select = (SwitchExpr)expr;
                  org.apache.bcel.generic.InstructionHandle[] targets = 
                     select.getHandleTargets();
                  
                  Expr[] newtargets = new Expr[targets.length];
                  for (int k=0;k<newtargets.length;k++){
                     for (int j=0;j<blocks.length;j++){
                        if (blocks[j].get(0)==targets[k]){
                           newtargets[k] = (Expr)treelists[j].getInstList().get(0);
                           break;
                        }
                     }
                  }
                  select.setTargets(newtargets);
               }
            }
         }
      }

      // now we're basically done... replace the blocks in the 
      // graph with their corresponding trees

      // save the edges (we're about to clear the graph!)
      java.util.ArrayList edgelist = new java.util.ArrayList();
      for (java.util.Iterator edgeiter=edges();edgeiter.hasNext();){
         sandmark.util.newgraph.Edge edge = 
            (sandmark.util.newgraph.Edge)edgeiter.next();
         edgelist.add(edge);
         removeEdge(edge);
      }
      
      // clear the graph!
      removeAllNodes(nodes());

      // add back in the source and sink
      addNode(source = new BasicBlock(this));
      addNode(sink = new BasicBlock(this));

      // add back in 
      for (int i=0;i<edgelist.size();i++){
         sandmark.util.newgraph.Edge edge = 
            (sandmark.util.newgraph.Edge)edgelist.get(i);
         Object sourceNode=null, sinkNode=null;

         // find the new source for this edge
         if (edge.sourceNode()==sourceList)
            sourceNode = source;
         else if (edge.sourceNode()==sinkList)
            sourceNode = sink;
         else{
            for (int j=0;j<blocks.length;j++){
               if (edge.sourceNode()==blocks[j]){
                  sourceNode = treelists[j];
                  break;
               }
            }
         }

         // find the new sink for this edge
         if (edge.sinkNode()==sourceList)
            sinkNode = source;
         else if (edge.sinkNode()==sinkList)
            sinkNode = sink;
         else{
            for (int j=0;j<blocks.length;j++){
               if (edge.sinkNode()==blocks[j]){
                  sinkNode = treelists[j];
                  break;
               }
            }
         }
         
         // replace the edge with an equivalent one
         if (edge instanceof sandmark.analysis.controlflowgraph.FallthroughEdge){
            addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge
                    (sourceNode, sinkNode));
         }else if (edge instanceof sandmark.analysis.controlflowgraph.ExceptionEdge){
            sandmark.analysis.controlflowgraph.ExceptionEdge exedge = 
               (sandmark.analysis.controlflowgraph.ExceptionEdge)edge;
            addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge
                    (sourceNode, sinkNode, exedge.exception()));
         }else{
            addEdge(sourceNode, sinkNode);
         }
      }
   }


   /** Chunks up the method into basic blocks. 
    *  We ignore some of the division that would happen inside
    *  exception handlers, after each ExceptionThrower.
    *  This must be called after fixInstructions, not before.
    */
   private java.util.ArrayList[] buildBlocks(){
      org.apache.bcel.generic.CodeExceptionGen[] exceptions = 
         method.getExceptionHandlers();
      
      // will be the set of leaders
      java.util.HashSet leaders = new java.util.HashSet();

      leaders.add(ilist.getStart());
      
      // all 'try' blocks are leaders and all 'handler' blocks are leaders
      for (int i=0;i<exceptions.length;i++){
         leaders.add(exceptions[i].getStartPC());
         leaders.add(exceptions[i].getHandlerPC());
         
         if (exceptions[i].getEndPC().getNext()!=null)
            leaders.add(exceptions[i].getEndPC().getNext());
      }
      
      // the master list of basic blocks (an ArrayList of ArrayLists of handles)
      java.util.ArrayList blocks = new java.util.ArrayList();
      
      org.apache.bcel.generic.InstructionHandle[] handles = 
         ilist.getInstructionHandles();

      for (int i=0;i<handles.length;i++){
         if (handles[i].getInstruction() instanceof org.apache.bcel.generic.BranchInstruction){
            org.apache.bcel.generic.BranchInstruction branch = 
               (org.apache.bcel.generic.BranchInstruction)handles[i].getInstruction();
            if (branch instanceof org.apache.bcel.generic.Select){
               org.apache.bcel.generic.Select select = 
                  (org.apache.bcel.generic.Select)branch;
               org.apache.bcel.generic.InstructionHandle[] targets = 
                  select.getTargets();
               for (int j=0;j<targets.length;j++)
                  leaders.add(targets[j]);
            }
            // branch targets are leaders
            leaders.add(branch.getTarget());

            // instrucions after branches are leaders
            if (handles[i].getNext()!=null)
               leaders.add(handles[i].getNext());

         }else if (handles[i].getInstruction() instanceof org.apache.bcel.generic.ATHROW ||
                   handles[i].getInstruction() instanceof org.apache.bcel.generic.ReturnInstruction ||
                   handles[i].getInstruction() instanceof org.apache.bcel.generic.RET){
            // instructions after unconditional control jumps are leaders
            if (handles[i].getNext()!=null)
               leaders.add(handles[i].getNext());
         }
      }


      // identified all leaders, now start chunking!
      java.util.ArrayList currentBlock = new java.util.ArrayList();
      for (int i=0;i<handles.length;i++){
         if (leaders.contains(handles[i])){
            if (currentBlock.size()>0){
               blocks.add(currentBlock);
               currentBlock = new java.util.ArrayList();
            }
         }
         currentBlock.add(handles[i]);
      }
      // if the last block has stuff in it, make it another block
      if (currentBlock.size()>0)
         blocks.add(currentBlock);

      return (java.util.ArrayList[])blocks.toArray(new java.util.ArrayList[0]);
   }
   

   // add the nodes and edges to this graph
   private void buildEdges(java.util.ArrayList[] blocks, 
                           java.util.ArrayList sourceList,
                           java.util.ArrayList sinkList){
      java.util.Hashtable handle2block = new java.util.Hashtable();

      ilist.setPositions(true);

      java.util.ArrayList afterJsr = new java.util.ArrayList();

      for (int i=0;i<blocks.length;i++){
         addNode(blocks[i]);
         for (int j=0;j<blocks[i].size();j++){
            handle2block.put(blocks[i].get(j), blocks[i]);
         }

         org.apache.bcel.generic.Instruction inst = 
            ((org.apache.bcel.generic.InstructionHandle)
             blocks[i].get(blocks[i].size()-1)).getInstruction();

         if (inst instanceof org.apache.bcel.generic.JsrInstruction){
            if (i<blocks.length-1)
               afterJsr.add(blocks[i+1]);
         }
      }
      addNode(sourceList);
      addNode(sinkList);
      addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge(sourceList, blocks[0]));

      for (int i=0;i<blocks.length;i++){
         org.apache.bcel.generic.InstructionHandle lastHandle = 
            (org.apache.bcel.generic.InstructionHandle)blocks[i].get(blocks[i].size()-1);

         org.apache.bcel.generic.Instruction lastInst = 
            lastHandle.getInstruction();

         if (lastInst instanceof org.apache.bcel.generic.BranchInstruction){
            if (lastInst instanceof org.apache.bcel.generic.IfInstruction){
               org.apache.bcel.generic.InstructionHandle target = 
                  ((org.apache.bcel.generic.IfInstruction)lastInst).getTarget();
               addEdge(blocks[i], handle2block.get(target));
               addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge(blocks[i], blocks[i+1]));
            }else if (lastInst instanceof org.apache.bcel.generic.GotoInstruction){
               org.apache.bcel.generic.InstructionHandle target = 
                  ((org.apache.bcel.generic.GotoInstruction)lastInst).getTarget();
               addEdge(blocks[i], handle2block.get(target));
            }else if (lastInst instanceof org.apache.bcel.generic.JsrInstruction){
               org.apache.bcel.generic.InstructionHandle target = 
                  ((org.apache.bcel.generic.JsrInstruction)lastInst).getTarget();
               addEdge(blocks[i], handle2block.get(target));
            }else{
               // Select
               org.apache.bcel.generic.InstructionHandle[] targets = 
                  ((org.apache.bcel.generic.Select)lastInst).getTargets();
               org.apache.bcel.generic.InstructionHandle target = 
                  ((org.apache.bcel.generic.Select)lastInst).getTarget();                  
               for (int j=0;j<targets.length;j++)
                  addEdge(blocks[i], handle2block.get(targets[j]));
               addEdge(blocks[i], handle2block.get(target));
            }
         }else if (lastInst instanceof org.apache.bcel.generic.ReturnInstruction){
            addEdge(blocks[i], sinkList);
         }else if (lastInst.getOpcode()==ATHROW){
            org.apache.bcel.generic.CodeExceptionGen[] exceptions = 
               method.getExceptionHandlers();
            
            for (int j=0;j<exceptions.length;j++){
               if (exceptions[j].getStartPC().getPosition()<=lastHandle.getPosition() &&
                   exceptions[j].getEndPC().getPosition()>lastHandle.getPosition()){
                  addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge
                          (blocks[i], handle2block.get(exceptions[j].getHandlerPC()), exceptions[j]));
               }else{
                  addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge
                          (blocks[i], sinkList, exceptions[j]));
               }
            }
         }else if (lastInst.getOpcode()==RET){
            // stupid version
            for (int j=0;j<afterJsr.size();j++){
               addEdge(blocks[i], afterJsr.get(j));
            }
         }else{
            // not a control-transfer instruction
            if (i<blocks.length-1)
               addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge(blocks[i], blocks[i+1]));
            else
               addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge(blocks[i], sinkList));
         }
      }

      org.apache.bcel.generic.CodeExceptionGen[] handlers = 
         method.getExceptionHandlers();
      for (int i=0;i<handlers.length;i++){
         org.apache.bcel.generic.InstructionHandle before = 
            handlers[i].getStartPC().getPrev();
         if (before==null){
            Object block2 = handle2block.get(handlers[i].getHandlerPC());
            addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge
                    (sourceList, block2, handlers[i]));
         }else{
            Object block1 = handle2block.get(before);
            Object block2 = handle2block.get(handlers[i].getHandlerPC());
            addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge
                    (block1, block2, handlers[i]));
         }
      }
   }


   // helper method, just so that buildTrees doesn't get ginormous.
   // this handles arithmetic exprs.
   private void buildArithmetic(org.apache.bcel.generic.Instruction inst,
                                java.util.ArrayList result, java.util.Stack stack){

      short code = inst.getOpcode();

      org.apache.bcel.generic.BasicType type = null;
      switch(code){
      case DADD: case DDIV: case DMUL: case DNEG: case DREM: case DSUB:
         type = org.apache.bcel.generic.Type.DOUBLE;
         break;
      case FADD: case FDIV: case FMUL: case FNEG: case FREM: case FSUB:
         type = org.apache.bcel.generic.Type.FLOAT;
         break;
      case IADD: case IAND: case IDIV: case IMUL: case INEG: case IOR:
      case IREM: case ISHL: case ISHR: case ISUB: case IUSHR: case IXOR:
         type = org.apache.bcel.generic.Type.INT;
         break;
      default:
         type = org.apache.bcel.generic.Type.LONG;
         break;
      }
      // got the type now
               
      ValueExpr left=null, right=null;
      Expr newexpr = null;

      switch(code){
      case DADD: case FADD: case IADD: case LADD:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.ADD, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case DDIV: case FDIV: case IDIV: case LDIV:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.DIV, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case DMUL: case FMUL: case IMUL: case LMUL: 
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.MUL, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case DNEG: case FNEG: case INEG: case LNEG:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         newexpr = new NegateArithmeticExpr(type, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case DREM: case FREM: case IREM: case LREM:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.REM, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case DSUB: case FSUB: case ISUB: case LSUB:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.SUB, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case IAND: case LAND:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.AND, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case IOR: case LOR:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.OR, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case ISHL: case LSHL:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr
               (org.apache.bcel.generic.Type.INT);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.SHL, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case ISHR: case LSHR:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr
               (org.apache.bcel.generic.Type.INT);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.SHR, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case IUSHR: case LUSHR:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr
               (org.apache.bcel.generic.Type.INT);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.USHR, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;

      case IXOR: case LXOR:
         if (stack.size()>=1){
            right = (ValueExpr)stack.pop();
            result.remove(right);
         }else{
            right = new PrimitiveDummyExpr(type);
         }

         if (stack.size()>=1){
            left = (ValueExpr)stack.pop();
            result.remove(left);
         }else{
            left = new PrimitiveDummyExpr(type);
         }

         newexpr = new BinaryArithmeticExpr(type, ArithmeticExpr.XOR, left, right);
         stack.push(newexpr);
         result.add(newexpr);
         break;
      }
   }


   // helper method, so that buildTrees doesn't get ginormous.
   // this one handles array instructions (load and store)
   private void buildArray(org.apache.bcel.generic.Instruction inst,
                           java.util.ArrayList result, java.util.Stack stack){
      short code = inst.getOpcode();

      org.apache.bcel.generic.Type type = null;
      switch(code){
      case AALOAD:
         type=org.apache.bcel.generic.Type.OBJECT;
      case BALOAD:
         if (type==null)
            type = org.apache.bcel.generic.Type.BYTE;
      case CALOAD:
         if (type==null)
            type = org.apache.bcel.generic.Type.CHAR;
      case DALOAD:
         if (type==null)
            type = org.apache.bcel.generic.Type.DOUBLE;
      case FALOAD:
         if (type==null)
            type = org.apache.bcel.generic.Type.FLOAT;
      case IALOAD:
         if (type==null)
            type = org.apache.bcel.generic.Type.INT;
      case LALOAD:
         if (type==null)
            type = org.apache.bcel.generic.Type.LONG;
      case SALOAD:{
         if (type==null)
            type = org.apache.bcel.generic.Type.SHORT;
                  
         ValueExpr index = null;
         ValueExpr ref = null;
         if (stack.size()>=1){
            index = (ValueExpr)stack.pop();
            result.remove(index);
         }else{
            index = new PrimitiveDummyExpr
               (org.apache.bcel.generic.Type.INT);
         }
                  
         if (stack.size()>=1){
            ref = (ValueExpr)stack.pop();
            result.remove(ref);
         }else{
            ref = new ArrayDummyExpr(type);
         }

         Expr newexpr = new ArrayLoadExpr(type, ref, index);
         stack.push(newexpr);
         result.add(newexpr);
         break;
      }

      case AASTORE:
         type = org.apache.bcel.generic.Type.OBJECT;
      case BASTORE:
         if (type==null)
            type = org.apache.bcel.generic.Type.BYTE;
      case CASTORE:
         if (type==null)
            type = org.apache.bcel.generic.Type.CHAR;
      case DASTORE:
         if (type==null)
            type = org.apache.bcel.generic.Type.DOUBLE;
      case FASTORE:
         if (type==null)
            type = org.apache.bcel.generic.Type.FLOAT;
      case IASTORE:
         if (type==null)
            type = org.apache.bcel.generic.Type.INT;
      case LASTORE:
         if (type==null)
            type = org.apache.bcel.generic.Type.LONG;
      case SASTORE:{
         if (type==null)
            type = org.apache.bcel.generic.Type.SHORT;
                
         ValueExpr value=null, index=null, ref=null;
         if (stack.size()>=1){
            value = (ValueExpr)stack.pop();
            result.remove(value);
         }else{
            if (type instanceof org.apache.bcel.generic.BasicType)
               value = new PrimitiveDummyExpr
                  ((org.apache.bcel.generic.BasicType)type);
            else
               value = new ObjectDummyExpr
                  ((org.apache.bcel.generic.ReferenceType)type);
         }

         if (stack.size()>=1){
            index = (ValueExpr)stack.pop();
            result.remove(index);
         }else{
            index = new PrimitiveDummyExpr
               (org.apache.bcel.generic.Type.INT);
         }

         if (stack.size()>=1){
            ref = (ValueExpr)stack.pop();
            result.remove(ref);
         }else{
            ref = new ArrayDummyExpr(type);
         }

         Expr newexpr = new ArrayStoreExpr(type, ref, index, value);
         result.add(newexpr);
         break;
      }
      }
   }
   

   // builds the trees for each basic block.
   // returns an array that will be parallel to 'blocks'
   private BasicBlock[] buildTrees(java.util.ArrayList[] blocks){
      java.util.HashMap handlerStarts2types = new java.util.HashMap();
      java.util.HashSet jsrTargets = new java.util.HashSet();
      
      for (int i=0;i<blocks.length;i++){
         if (blocks[i].get(blocks[i].size()-1) instanceof 
             org.apache.bcel.generic.JsrInstruction){

            jsrTargets.add(((org.apache.bcel.generic.JsrInstruction)
                            blocks[i].get(blocks[i].size()-1)).getTarget());
         }
      }

      org.apache.bcel.generic.CodeExceptionGen[] exceptions = 
         method.getExceptionHandlers();
      for (int i=0;i<exceptions.length;i++){
         handlerStarts2types.put(exceptions[i].getHandlerPC(), exceptions[i].getCatchType());
      }

      BasicBlock prev=null;
      BasicBlock[] newblocks = new BasicBlock[blocks.length];

      for (int i=0;i<blocks.length;i++){
         newblocks[i] = new BasicBlock(this);

         if (prev!=null)
            prev.setFallthrough(newblocks[i]);
         prev=newblocks[i];

         java.util.ArrayList result = newblocks[i].getInstList();

         java.util.Stack stack = new java.util.Stack();
         if (jsrTargets.contains(blocks[i].get(0))){
            stack.push(new ReturnAddressDummyExpr());
         }else if (handlerStarts2types.containsKey(blocks[i].get(0))){
            stack.push(new ExceptionDummyExpr
                       ((org.apache.bcel.generic.ObjectType)
                        handlerStarts2types.get(blocks[i].get(0))));
         }
         // if this block starts a subroutine or a handler, 
         // push the magic value into the stack


         // simulate instructions
         for (int j=0;j<blocks[i].size();j++){
            org.apache.bcel.generic.InstructionHandle handle = 
               (org.apache.bcel.generic.InstructionHandle)blocks[i].get(j);
            org.apache.bcel.generic.Instruction inst = handle.getInstruction();
            short code = inst.getOpcode();

            if (code==ACONST_NULL){
               Expr newexpr = new NullConstantExpr();
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.ArithmeticInstruction){
               buildArithmetic(inst, result, stack);

            }else if (inst instanceof org.apache.bcel.generic.ArrayInstruction){
               buildArray(inst, result, stack);

            }else if (code==ARRAYLENGTH){
               ValueExpr ref = null;
               if (stack.size()>=1){
                  ref = (ValueExpr)stack.pop();
                  result.remove(ref);
               }else{
                  ref = new ArrayDummyExpr();
               }
               Expr newexpr = new ArrayLengthExpr(ref);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==ATHROW){
               ValueExpr exexpr = null;
               if (stack.size()>=1){
                  exexpr = (ValueExpr)stack.pop();
                  result.remove(exexpr);
               }else{
                  exexpr = new ExceptionDummyExpr();
               }
               Expr newexpr = new ThrowExpr(exexpr);
               result.add(newexpr);

            }else if (code==BIPUSH){
               org.apache.bcel.generic.BIPUSH push = 
                  (org.apache.bcel.generic.BIPUSH)inst;
               Expr newexpr = new NumericConstantExpr
                  (org.apache.bcel.generic.Type.INT, push.getValue());
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.BranchInstruction){
               org.apache.bcel.generic.BranchInstruction branch = 
                  (org.apache.bcel.generic.BranchInstruction)inst;
               if (inst instanceof org.apache.bcel.generic.IfInstruction){
                  int numargs=2;
                  boolean wantint=true;
                  switch(code){
                  case IFNONNULL:
                  case IFNULL:
                     wantint=false;
                  case IFEQ:
                  case IFGE:
                  case IFGT:
                  case IFLE:
                  case IFLT:
                  case IFNE:
                     numargs=1;
                     break;
                  }

                  ValueExpr left=null, right=null;
                  if (stack.size()>=1){
                     right = (ValueExpr)stack.pop();
                     result.remove(right);
                  }else{
                     if (wantint)
                        right = new PrimitiveDummyExpr
                           (org.apache.bcel.generic.Type.INT);
                     else
                        right = new ObjectDummyExpr();
                  }

                  if (numargs==2){
                     if (stack.size()>=1){
                        left = (ValueExpr)stack.pop();
                        result.remove(left);
                     }else{
                        left = new PrimitiveDummyExpr
                           (org.apache.bcel.generic.Type.INT);
                     }

                     Expr newexpr = new IfExpr(code, left, right, branch.getTarget());
                     result.add(newexpr);
                  }else{
                     Expr newexpr = new IfExpr(code, right, branch.getTarget());
                     result.add(newexpr);
                  }

               }else if (inst instanceof org.apache.bcel.generic.GotoInstruction){
                  Expr newexpr = new GotoExpr(branch.getTarget());
                  result.add(newexpr);
               }else if (inst instanceof org.apache.bcel.generic.JsrInstruction){
                  Expr newexpr = new JsrExpr(branch.getTarget());
                  result.add(newexpr);
               }else{
                  // Select
                  ValueExpr index = null;
                  if (stack.size()>=1){
                     index = (ValueExpr)stack.pop();
                     result.remove(index);
                  }else{
                     index = new PrimitiveDummyExpr
                        (org.apache.bcel.generic.Type.INT);
                  }

                  org.apache.bcel.generic.Select select = 
                     (org.apache.bcel.generic.Select)inst;
                  
                  Expr newexpr = new SwitchExpr(index, select.getMatchs(), select.getTargets(), select.getTarget(),
                                                code==LOOKUPSWITCH);
                  result.add(newexpr);
               }

            }else if (inst instanceof org.apache.bcel.generic.ConversionInstruction){
               org.apache.bcel.generic.BasicType totype=null, fromtype=null;
               switch(code){
               case D2F:
                  totype=org.apache.bcel.generic.Type.FLOAT;
               case D2I:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.INT;
               case D2L:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.LONG;
                  fromtype=org.apache.bcel.generic.Type.DOUBLE;
                  break;

               case F2D:
                  totype=org.apache.bcel.generic.Type.DOUBLE;
               case F2I:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.INT;
               case F2L:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.LONG;
                  fromtype=org.apache.bcel.generic.Type.FLOAT;
                  break;

               case I2B:
                  totype=org.apache.bcel.generic.Type.BYTE;
               case I2C:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.CHAR;
               case I2D:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.DOUBLE;
               case I2F:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.FLOAT;
               case I2L:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.LONG;
               case I2S:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.SHORT;
                  fromtype=org.apache.bcel.generic.Type.INT;
                  break;

               case L2D:
                  totype=org.apache.bcel.generic.Type.DOUBLE;
               case L2F:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.FLOAT;
               case L2I:
                  if (totype==null)
                     totype=org.apache.bcel.generic.Type.INT;
                  fromtype=org.apache.bcel.generic.Type.LONG;
                  break;
               }

               ValueExpr value = null;
               if (stack.size()>=1){
                  value = (ValueExpr)stack.pop();
                  result.remove(value);
               }else{
                  value = new PrimitiveDummyExpr(fromtype);
               }

               Expr newexpr = new ConversionExpr(value, fromtype, totype);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==ANEWARRAY){
               ValueExpr count = null;
               if (stack.size()>=1){
                  count = (ValueExpr)stack.pop();
                  result.remove(count);
               }else{
                  count = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.INT);
               }
               org.apache.bcel.generic.ArrayType type = 
                  new org.apache.bcel.generic.ArrayType
                  (((org.apache.bcel.generic.ANEWARRAY)inst).getType(method.getConstantPool()), 1);

               Expr newexpr = new NewArrayExpr(type, new ValueExpr[]{count});
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==CHECKCAST){
               org.apache.bcel.generic.ReferenceType totype = 
                  (org.apache.bcel.generic.ReferenceType)
                  ((org.apache.bcel.generic.CHECKCAST)inst).getType(method.getConstantPool());
               ValueExpr from = null;
               if (stack.size()>=1){
                  from = (ValueExpr)stack.pop();
                  result.remove(from);
               }else{
                  from = new ObjectDummyExpr();
               }
               Expr newexpr = new ConversionExpr(from, org.apache.bcel.generic.Type.OBJECT, totype);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==GETFIELD){
               org.apache.bcel.generic.GETFIELD get = 
                  (org.apache.bcel.generic.GETFIELD)inst;

               ValueExpr ref = null;
               if (stack.size()>=1){
                  ref = (ValueExpr)stack.pop();
                  result.remove(ref);
               }else{
                  ref = new ObjectDummyExpr
                     (get.getClassType(method.getConstantPool()));
               }
               org.apache.bcel.generic.Type fieldtype = 
                  get.getFieldType(method.getConstantPool());
               String fieldname = get.getFieldName(method.getConstantPool());
               String classname = get.getClassName(method.getConstantPool());

               Expr newexpr = new GetFieldExpr(classname, fieldname, fieldtype, ref);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==GETSTATIC){
               org.apache.bcel.generic.GETSTATIC get = 
                  (org.apache.bcel.generic.GETSTATIC)inst;

               org.apache.bcel.generic.Type fieldtype = 
                  get.getFieldType(method.getConstantPool());
               String fieldname = get.getFieldName(method.getConstantPool());
               String classname = get.getClassName(method.getConstantPool());

               Expr newexpr = new GetFieldExpr(classname, fieldname, fieldtype);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==PUTFIELD){
               org.apache.bcel.generic.PUTFIELD put = 
                  (org.apache.bcel.generic.PUTFIELD)inst;

               org.apache.bcel.generic.Type fieldtype = 
                  put.getFieldType(method.getConstantPool());

               ValueExpr ref = null;
               ValueExpr value = null;
               if (stack.size()>=1){
                  value = (ValueExpr)stack.pop();
                  result.remove(value);
               }else{
                  if (fieldtype instanceof org.apache.bcel.generic.BasicType){
                     value = new PrimitiveDummyExpr
                        ((org.apache.bcel.generic.BasicType)fieldtype);
                  }else
                     value = new ObjectDummyExpr
                        ((org.apache.bcel.generic.ObjectType)fieldtype);
               }

               if (stack.size()>=1){
                  ref = (ValueExpr)stack.pop();
                  result.remove(ref);
               }else{
                  ref = new ObjectDummyExpr
                     (put.getClassType(method.getConstantPool()));
               }

               String fieldname = put.getFieldName(method.getConstantPool());
               String classname = put.getClassName(method.getConstantPool());
               Expr newexpr = new PutFieldExpr(classname, fieldname, fieldtype, value, ref);
               result.add(newexpr);

            }else if (code==PUTSTATIC){
               org.apache.bcel.generic.PUTSTATIC put = 
                  (org.apache.bcel.generic.PUTSTATIC)inst;

               org.apache.bcel.generic.Type fieldtype = 
                  put.getFieldType(method.getConstantPool());

               ValueExpr value = null;
               if (stack.size()>=1){
                  value = (ValueExpr)stack.pop();
                  result.remove(value);
               }else{
                  if (fieldtype instanceof org.apache.bcel.generic.BasicType){
                     value = new PrimitiveDummyExpr
                        ((org.apache.bcel.generic.BasicType)fieldtype);
                  }else
                     value = new ObjectDummyExpr
                        ((org.apache.bcel.generic.ObjectType)fieldtype);
               }

               String fieldname = put.getFieldName(method.getConstantPool());
               String classname = put.getClassName(method.getConstantPool());
               
               Expr newexpr = new PutFieldExpr(classname, fieldname, fieldtype, value);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.InvokeInstruction){
               org.apache.bcel.generic.InvokeInstruction invoke = 
                  (org.apache.bcel.generic.InvokeInstruction)inst;
               org.apache.bcel.generic.Type[] argtypes = 
                  invoke.getArgumentTypes(method.getConstantPool());
               String classname = invoke.getClassName(method.getConstantPool());
               String methodname = invoke.getMethodName(method.getConstantPool());
               String methodsig = invoke.getSignature(method.getConstantPool());
               org.apache.bcel.generic.Type returnType = 
                  invoke.getReturnType(method.getConstantPool());

               ValueExpr[] args = new ValueExpr[argtypes.length];
               for (int t=argtypes.length-1;t>=0;t--){
                  if (stack.size()>=1){
                     args[t] = (ValueExpr)stack.pop();
                     result.remove(args[t]);
                  }else{
                     if (argtypes[t] instanceof org.apache.bcel.generic.BasicType)
                        args[t] = new PrimitiveDummyExpr
                           ((org.apache.bcel.generic.BasicType)argtypes[t]);
                     else
                        args[t] = new ObjectDummyExpr
                           ((org.apache.bcel.generic.ObjectType)argtypes[t]);
                  }
               }

               if (code!=INVOKESTATIC){
                  ValueExpr ref = null;
                  if (stack.size()>=1){
                     ref = (ValueExpr)stack.pop();
                     result.remove(ref);
                  }else{
                     ref = new ObjectDummyExpr
                        (invoke.getClassType(method.getConstantPool()));
                  }
                  
                  Expr newexpr = new InvokeExpr(classname, methodname, methodsig, 
                                                args, ref, code);
                  if (!returnType.equals(org.apache.bcel.generic.Type.VOID))
                     stack.push(newexpr);
                  result.add(newexpr);
               }else{
                  Expr newexpr = new InvokeExpr(classname, methodname, methodsig,
                                                args);
                  if (!returnType.equals(org.apache.bcel.generic.Type.VOID))
                     stack.push(newexpr);
                  result.add(newexpr);
               }

            }else if (code==INSTANCEOF){
               ValueExpr ref = null;
               if (stack.size()>=1){
                  ref = (ValueExpr)stack.pop();
                  result.remove(ref);
               }else{
                  ref = new ObjectDummyExpr();
               }

               Expr newexpr = new InstanceofExpr
                  (ref, 
                   (org.apache.bcel.generic.ObjectType)
                   ((org.apache.bcel.generic.INSTANCEOF)inst).getType(method.getConstantPool()));
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.LDC){
               int index = ((org.apache.bcel.generic.LDC)inst).getIndex();
               org.apache.bcel.classfile.Constant c = 
                  method.getConstantPool().getConstant(index);

               Expr newexpr=null;
               if (c instanceof org.apache.bcel.classfile.ConstantString){
                  String value = ((org.apache.bcel.classfile.ConstantString)c).getBytes(method.getConstantPool().getConstantPool());
                  newexpr = new StringConstantExpr(value);
               }else if (c instanceof org.apache.bcel.classfile.ConstantInteger){
                  int value = ((org.apache.bcel.classfile.ConstantInteger)c).getBytes();
                  newexpr = new NumericConstantExpr
                     (org.apache.bcel.generic.Type.INT, new Integer(value));
               }else if (c instanceof org.apache.bcel.classfile.ConstantFloat){
                  float value = ((org.apache.bcel.classfile.ConstantFloat)c).getBytes();
                  newexpr = new NumericConstantExpr
                     (org.apache.bcel.generic.Type.FLOAT, new Float(value));
               }else{
                  throw new RuntimeException("Bad constant pool type");
               }
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==LDC2_W){
               int index = ((org.apache.bcel.generic.LDC2_W)inst).getIndex();
               org.apache.bcel.classfile.Constant c = 
                  method.getConstantPool().getConstant(index);

               Expr newexpr=null;
               if (c instanceof org.apache.bcel.classfile.ConstantLong){
                  long value = ((org.apache.bcel.classfile.ConstantLong)c).getBytes();
                  newexpr = new NumericConstantExpr
                     (org.apache.bcel.generic.Type.LONG, new Long(value));
               }else if (c instanceof org.apache.bcel.classfile.ConstantDouble){
                  double value = ((org.apache.bcel.classfile.ConstantDouble)c).getBytes();
                  newexpr = new NumericConstantExpr
                     (org.apache.bcel.generic.Type.DOUBLE, new Double(value));
               }else{
                  throw new RuntimeException("Bad constant pool type");
               }
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==MULTIANEWARRAY){
               org.apache.bcel.generic.MULTIANEWARRAY multi = 
                  (org.apache.bcel.generic.MULTIANEWARRAY)inst;
               org.apache.bcel.generic.ArrayType type = 
                  (org.apache.bcel.generic.ArrayType)
                  multi.getType(method.getConstantPool());

               ValueExpr[] dimexprs = new ValueExpr[multi.getDimensions()];
               for (int t=dimexprs.length-1;t>=0;t--){
                  if (stack.size()>=1){
                     dimexprs[t] = (ValueExpr)stack.pop();
                     result.remove(dimexprs[t]);
                  }else{
                     dimexprs[t] = new PrimitiveDummyExpr
                        (org.apache.bcel.generic.Type.INT);
                  }
               }
               
               Expr newexpr = new NewArrayExpr(type, dimexprs);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==NEW){
               org.apache.bcel.generic.ObjectType type = 
                  (org.apache.bcel.generic.ObjectType)
                  ((org.apache.bcel.generic.NEW)inst).getType(method.getConstantPool());

               Expr newexpr = new NewExpr(type);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==DCMPG ||
                      code==DCMPL){
               ValueExpr right = null;
               ValueExpr left = null;
               
               if (stack.size()>=1){
                  right = (ValueExpr)stack.pop();
                  result.remove(right);
               }else{
                  right = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.DOUBLE);
               }

               if (stack.size()>=1){
                  left = (ValueExpr)stack.pop();
                  result.remove(left);
               }else{
                  left = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.DOUBLE);
               }

               Expr newexpr = new ComparisonExpr(left, right, code);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.DCONST){
               Expr newexpr = new NumericConstantExpr
                  (org.apache.bcel.generic.Type.DOUBLE,
                   ((org.apache.bcel.generic.DCONST)inst).getValue());
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==FCMPG ||
                      code==FCMPL){

               ValueExpr right = null;
               ValueExpr left = null;
               
               if (stack.size()>=1){
                  right = (ValueExpr)stack.pop();
                  result.remove(right);
               }else{
                  right = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.FLOAT);
               }

               if (stack.size()>=1){
                  left = (ValueExpr)stack.pop();
                  result.remove(left);
               }else{
                  left = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.FLOAT);
               }

               Expr newexpr = new ComparisonExpr(left, right, code);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.FCONST){
               Expr newexpr = new NumericConstantExpr
                  (org.apache.bcel.generic.Type.FLOAT,
                   ((org.apache.bcel.generic.FCONST)inst).getValue());
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.ICONST){
               Expr newexpr = new NumericConstantExpr
                  (org.apache.bcel.generic.Type.INT,
                   ((org.apache.bcel.generic.ICONST)inst).getValue());
               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==LCMP){
               ValueExpr right = null;
               ValueExpr left = null;
               
               if (stack.size()>=1){
                  right = (ValueExpr)stack.pop();
                  result.remove(right);
               }else{
                  right = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.LONG);
               }

               if (stack.size()>=1){
                  left = (ValueExpr)stack.pop();
                  result.remove(left);
               }else{
                  left = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.LONG);
               }

               Expr newexpr = new ComparisonExpr(left, right, code);
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.LCONST){
               Expr newexpr = new NumericConstantExpr
                  (org.apache.bcel.generic.Type.LONG, 
                   ((org.apache.bcel.generic.LCONST)inst).getValue());
               stack.push(newexpr);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.LocalVariableInstruction){
               if (code==IINC){
                  org.apache.bcel.generic.IINC iinc = 
                     (org.apache.bcel.generic.IINC)inst;
                  Expr newexpr = new IncExpr(iinc.getIndex(), iinc.getIncrement());
                  result.add(newexpr);
               }else if (inst instanceof org.apache.bcel.generic.LoadInstruction){
                  org.apache.bcel.generic.LoadInstruction load = 
                     (org.apache.bcel.generic.LoadInstruction)inst;
                  Expr newexpr = new LoadExpr(load.getType(method.getConstantPool()), 
                                              load.getIndex());
                  stack.push(newexpr);
                  result.add(newexpr);
               }else{
                  // StoreInstruction
                  org.apache.bcel.generic.StoreInstruction store = 
                     (org.apache.bcel.generic.StoreInstruction)inst;

                  ValueExpr value = null;
                  if (stack.size()>=1){
                     value = (ValueExpr)stack.pop();
                     result.remove(value);
                  }else{
                     if (store instanceof org.apache.bcel.generic.ASTORE)
                        value = new ObjectDummyExpr();
                     else
                        value = new PrimitiveDummyExpr
                           ((org.apache.bcel.generic.BasicType)
                            store.getType(method.getConstantPool()));
                  }

                  Expr newexpr = new StoreExpr(store.getType(method.getConstantPool()), 
                                               store.getIndex(), value);
                  result.add(newexpr);
               }
            }else if (code==MONITORENTER){
               ValueExpr ref = null;
               if (stack.size()>=1){
                  ref = (ValueExpr)stack.pop();
                  result.remove(ref);
               }else{
                  ref = new ObjectDummyExpr();
               }

               Expr newexpr = new MonitorExpr(ref, true);
               result.add(newexpr);

            }else if (code==MONITOREXIT){
               ValueExpr ref = null;
               if (stack.size()>=1){
                  ref = (ValueExpr)stack.pop();
                  result.remove(ref);
               }else{
                  ref = new ObjectDummyExpr();
               }

               Expr newexpr = new MonitorExpr(ref, false);
               result.add(newexpr);

            }else if (code==NEWARRAY){
               ValueExpr count = null;
               if (stack.size()>=1){
                  count = (ValueExpr)stack.pop();
                  result.remove(count);
               }else{
                  count = new PrimitiveDummyExpr
                     (org.apache.bcel.generic.Type.INT);
               }

               org.apache.bcel.generic.ReferenceType type = 
                  (org.apache.bcel.generic.ReferenceType)
                  ((org.apache.bcel.generic.NEWARRAY)inst).getType();

               Expr newexpr = new NewArrayExpr
                  ((org.apache.bcel.generic.ArrayType)type, 
                   new ValueExpr[]{count});

               stack.push(newexpr);
               result.add(newexpr);

            }else if (code==NOP){
               Expr newexpr = new NopExpr();
               result.add(newexpr);

            }else if (code==RET){
               int index = ((org.apache.bcel.generic.RET)inst).getIndex();
               Expr newexpr = new RetExpr(index);
               result.add(newexpr);

            }else if (inst instanceof org.apache.bcel.generic.ReturnInstruction){
               if (code==RETURN){
                  Expr newexpr = new ReturnExpr();
                  result.add(newexpr);
               }else{
                  ValueExpr ref = null;
                  org.apache.bcel.generic.Type type = null;
                  switch(code){
                  case ARETURN:
                     type = method.getReturnType(); 
                     break;
                  case DRETURN:
                     type = org.apache.bcel.generic.Type.DOUBLE; 
                     break;
                  case FRETURN:
                     type = org.apache.bcel.generic.Type.FLOAT; 
                     break;
                  case IRETURN:
                     type = org.apache.bcel.generic.Type.INT; 
                     break;
                  case LRETURN:
                     type = org.apache.bcel.generic.Type.LONG; 
                     break;
                  }

                  if (stack.size()>=1){
                     ref = (ValueExpr)stack.pop();
                     result.remove(ref);
                  }else{
                     switch(code){
                     case ARETURN:
                        ref = new ObjectDummyExpr
                           ((org.apache.bcel.generic.ReferenceType)method.getReturnType());
                        break;
                     case DRETURN:
                        ref = new PrimitiveDummyExpr
                           (org.apache.bcel.generic.Type.DOUBLE);
                        break;
                     case FRETURN:
                        ref = new PrimitiveDummyExpr
                           (org.apache.bcel.generic.Type.FLOAT);
                        break;
                     case IRETURN:
                        ref = new PrimitiveDummyExpr
                           (org.apache.bcel.generic.Type.INT);
                        break;
                     case LRETURN:
                        ref = new PrimitiveDummyExpr
                           (org.apache.bcel.generic.Type.LONG);
                        break;
                     }
                  }
                  Expr newexpr = new ReturnExpr(ref, type);
                  result.add(newexpr);
               }

            }else if (code==SIPUSH){
               Expr newexpr = new NumericConstantExpr
                  (org.apache.bcel.generic.Type.INT, 
                   ((org.apache.bcel.generic.SIPUSH)inst).getValue());
               stack.push(newexpr);
               result.add(newexpr);

            }else{
               throw new RuntimeException("Bad instruction!");
            }
            
         }// end for each instruction
      }// end for each block

      return newblocks;
   }


   /** Recreates the bytecode and exception handlers based on the expression trees.
    */
   public void rewriteMethod(){
      ilist = new org.apache.bcel.generic.InstructionList();

      method.setInstructionList(ilist);

      org.apache.bcel.generic.InstructionHandle startHandle = 
         ilist.append(org.apache.bcel.generic.InstructionConstants.NOP);

      org.apache.bcel.generic.InstructionFactory factory = 
         new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());

      method.removeExceptionHandlers();

      java.util.Hashtable expr2firstih = new java.util.Hashtable();
      java.util.Hashtable handle2branch = new java.util.Hashtable();
      java.util.IdentityHashMap branch2expr = new java.util.IdentityHashMap();
      org.apache.bcel.generic.InstructionHandle[] startpcs = 
         new org.apache.bcel.generic.InstructionHandle[exceptionInfo.length];
      org.apache.bcel.generic.InstructionHandle[] endpcs = 
         new org.apache.bcel.generic.InstructionHandle[exceptionInfo.length];
      org.apache.bcel.generic.InstructionHandle[] handlerpcs = 
         new org.apache.bcel.generic.InstructionHandle[exceptionInfo.length];
      
      java.util.Set visitedBlocks = new java.util.HashSet();
      java.util.LinkedList queue = new java.util.LinkedList();

      /*
      java.util.IdentityHashMap block2fallthroughto = new java.util.IdentityHashMap();
      java.util.IdentityHashMap block2fallthroughfrom = new java.util.IdentityHashMap();
      for (java.util.Iterator edgeiter=edges();edgeiter.hasNext();){
         sandmark.util.newgraph.Edge edge = 
            (sandmark.util.newgraph.Edge)edgeiter.next();
         if (edge instanceof sandmark.analysis.controlflowgraph.FallthroughEdge){
            block2fallthroughto.put(edge.sourceNode(), edge.sinkNode());
            block2fallthroughfrom.put(edge.sinkNode(), edge.sourceNode());
         }
      }
      */

      queue.add(source);
      while(!queue.isEmpty()) {
         BasicBlock block = (BasicBlock)queue.removeFirst();
         if(visitedBlocks.contains(block))
            continue;

         if (block.fallthroughFrom()!=null && 
             !visitedBlocks.contains(block.fallthroughFrom())){
            queue.addLast(block.fallthroughFrom());
            queue.addLast(block);
            continue;
         }
         visitedBlocks.add(block);
    
         // foreach Expr in block
         for(int i = 0 ; i < block.getInstList().size() ; i++) {
            Expr expr = (Expr)block.getInstList().get(i);
            java.util.ArrayList instrs = expr.emitBytecode(factory);

            org.apache.bcel.generic.InstructionHandle 
               firstIH = null, 
               lastIH = null;

            for (int j=0;j<instrs.size();j++){
               org.apache.bcel.generic.Instruction inst =
                  (org.apache.bcel.generic.Instruction)instrs.get(j);

               if (inst instanceof org.apache.bcel.generic.BranchInstruction){
                  ((org.apache.bcel.generic.BranchInstruction)inst).updateTarget(null, startHandle);
                  lastIH = ilist.append((org.apache.bcel.generic.BranchInstruction)inst);
                  handle2branch.put(lastIH, inst);
                  branch2expr.put(inst, expr);
               }else{
                  lastIH = ilist.append(inst);
               }

               if (firstIH==null)
                  firstIH = lastIH;
            }
            expr2firstih.put(expr, firstIH);
            
            for (int j=0;j<exceptionInfo.length;j++){
               if (expr==exceptionInfo[j].getStartPC()){
                  startpcs[j] = firstIH;
               }
               if (expr==exceptionInfo[j].getHandlerPC()){
                  handlerpcs[j] = firstIH;
               }
               if (expr==exceptionInfo[j].getEndPC()){
                  endpcs[j] = lastIH;
               }
            }
         }

         // now find who else can be written

         if(block.fallthrough() != null)
            queue.addFirst(block.fallthrough());

         for (java.util.Iterator edgeiter = outEdges(block); edgeiter.hasNext(); ) {
            Object succ = ((sandmark.util.newgraph.Edge)edgeiter.next()).sinkNode();
            queue.addLast(succ);
         }
      }

      // fix branch targets
      for (java.util.Enumeration keyiter = handle2branch.keys();keyiter.hasMoreElements();){
         org.apache.bcel.generic.InstructionHandle branchhandle = 
            (org.apache.bcel.generic.InstructionHandle)keyiter.nextElement();

         org.apache.bcel.generic.BranchInstruction branch = 
            (org.apache.bcel.generic.BranchInstruction)handle2branch.get(branchhandle);

         BranchExpr parentExpr = (BranchExpr)branch2expr.get(branch);
         org.apache.bcel.generic.InstructionHandle target = 
            (org.apache.bcel.generic.InstructionHandle)expr2firstih.get(parentExpr.getTarget());
         branch.setTarget(target);

         if (branch instanceof org.apache.bcel.generic.Select){
            org.apache.bcel.generic.Select newbranch = 
               (org.apache.bcel.generic.Select)branch;
            SwitchExpr select = (SwitchExpr)parentExpr;
            Expr[] exprTargets = select.getTargets();
            for (int i=0;i<exprTargets.length;i++){
               newbranch.setTarget(i, (org.apache.bcel.generic.InstructionHandle)
                                   expr2firstih.get(exprTargets[i]));
            }
         }
      }

      // fix exception handlers
      for (int i=0;i<exceptionInfo.length;i++){
         method.addExceptionHandler(startpcs[i], endpcs[i], handlerpcs[i], exceptionInfo[i].getCatchType());
      }

      method.removeLineNumbers();
      method.removeLocalVariables();
      method.mark();

      ilist.setPositions(true);
   }


   // helper method for fixInstructions
   private org.apache.bcel.generic.Type mapType
      (org.apache.bcel.generic.Type type){
      if (type instanceof org.apache.bcel.generic.BasicType)
         return type;
      return org.apache.bcel.generic.Type.OBJECT;
   }


   /** This is a first pass over the instructions of a method, to do a few things:
    *  1. get rid of all stack instructions (DUP, POP, SWAP, etc).
    *  2. put store/load right after all invokes that have return values.
    *     (this will make sure that they are not reordered in the Expr tree).
    *  This cleans up the tree-making immensely.
    */
   private void fixInstructions(sandmark.analysis.stacksimulator.StackSimulator stack){
      org.apache.bcel.generic.InstructionHandle[] handles = 
         ilist.getInstructionHandles();

      java.util.HashMap handle2instlist = new java.util.HashMap();

      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());
      org.apache.bcel.generic.CodeExceptionGen[] exhandlers = 
         method.getExceptionHandlers();

      // for each nonvoid INVOKE, immediately store, then load again.
      for (int i=0;i<handles.length;i++){
         short code = handles[i].getInstruction().getOpcode();
         sandmark.analysis.stacksimulator.Context context = 
            stack.getInstructionContext(handles[i]);

         org.apache.bcel.generic.InstructionHandle ih;

         switch(code){
         case INVOKESTATIC:
         case INVOKEVIRTUAL:
         case INVOKESPECIAL:
         case INVOKEINTERFACE:{
            org.apache.bcel.generic.InvokeInstruction invoke =
               (org.apache.bcel.generic.InvokeInstruction)handles[i].getInstruction();
            org.apache.bcel.generic.Type returnType = 
               invoke.getReturnType(method.getConstantPool());
            if (!returnType.equals(org.apache.bcel.generic.Type.VOID)){
               java.util.Vector list = new java.util.Vector();
               list.add(handles[i].getInstruction());
               list.add(factory.createStore(returnType, maxlocals));
               list.add(factory.createLoad(returnType, maxlocals));
               maxlocals+=returnType.getSize();
               handle2instlist.put(handles[i], list);
            }
            break;
         }
         }
      }


      // replace DUP* and SWAP with a combination of loads and stores
      for (int i=0;i<handles.length;i++){
         short code = handles[i].getInstruction().getOpcode();
         sandmark.analysis.stacksimulator.Context context = 
            stack.getInstructionContext(handles[i]);

         org.apache.bcel.generic.InstructionHandle ih;

         switch(code){
         case DUP:{
            org.apache.bcel.generic.Type type =
               context.getStackAt(0)[0].getType();

            java.util.Vector list = new java.util.Vector();
            list.add(factory.createStore(mapType(type), maxlocals));
            list.add(factory.createLoad(mapType(type), maxlocals));
            list.add(factory.createLoad(mapType(type), maxlocals));
            maxlocals++;
            handle2instlist.put(handles[i], list);
            break;
         }

         case DUP_X1:{
            org.apache.bcel.generic.Type typeB =
               context.getStackAt(0)[0].getType();
            org.apache.bcel.generic.Type typeA =
               context.getStackAt(1)[0].getType();

            java.util.Vector list = new java.util.Vector();
            list.add(factory.createStore(mapType(typeB), maxlocals+1));
            list.add(factory.createStore(mapType(typeA), maxlocals));
            list.add(factory.createLoad(mapType(typeB), maxlocals+1));
            list.add(factory.createLoad(mapType(typeA), maxlocals));
            list.add(factory.createLoad(mapType(typeB), maxlocals+1));
            maxlocals+=2;
            handle2instlist.put(handles[i], list);
            break;
         }

         case DUP_X2:{
            org.apache.bcel.generic.Type type2 =
               context.getStackAt(0)[0].getType();
            org.apache.bcel.generic.Type type1 =
               context.getStackAt(1)[0].getType();
            
            java.util.Vector list = new java.util.Vector();
            if (type1.getSize()==2){
               list.add(factory.createStore(mapType(type2), maxlocals+2));
               list.add(factory.createStore(mapType(type1), maxlocals));
               list.add(factory.createLoad(mapType(type2), maxlocals+2));
               list.add(factory.createLoad(mapType(type1), maxlocals));
               list.add(factory.createLoad(mapType(type2), maxlocals+2));
            }else{
               org.apache.bcel.generic.Type type0 =
                  context.getStackAt(2)[0].getType();
               list.add(factory.createStore(mapType(type2), maxlocals+2));
               list.add(factory.createStore(mapType(type1), maxlocals+1));
               list.add(factory.createStore(mapType(type0), maxlocals));
               list.add(factory.createLoad(mapType(type2), maxlocals+2));
               list.add(factory.createLoad(mapType(type0), maxlocals));
               list.add(factory.createLoad(mapType(type1), maxlocals+1));
               list.add(factory.createLoad(mapType(type2), maxlocals+2));
            }
            maxlocals+=3;
            handle2instlist.put(handles[i], list);
            break;
         }

         case DUP2:{
            org.apache.bcel.generic.Type type1 = 
               context.getStackAt(0)[0].getType();

            java.util.Vector list = new java.util.Vector();
            if (type1.getSize()==2){
               list.add(factory.createStore(mapType(type1), maxlocals));
               list.add(factory.createLoad(mapType(type1), maxlocals));
               list.add(factory.createLoad(mapType(type1), maxlocals));
            }else{
               org.apache.bcel.generic.Type type0 = 
                  context.getStackAt(1)[0].getType();
               list.add(factory.createStore(mapType(type1), maxlocals+1));
               list.add(factory.createStore(mapType(type0), maxlocals));
               list.add(factory.createLoad(mapType(type0), maxlocals));
               list.add(factory.createLoad(mapType(type1), maxlocals+1));
               list.add(factory.createLoad(mapType(type0), maxlocals));
               list.add(factory.createLoad(mapType(type1), maxlocals+1));
            }
            maxlocals+=2;
            handle2instlist.put(handles[i], list);
            break;
         }

         case DUP2_X1:{
            org.apache.bcel.generic.Type type2 = 
               context.getStackAt(0)[0].getType();
            org.apache.bcel.generic.Type type1 = 
               context.getStackAt(1)[0].getType();

            java.util.Vector list = new java.util.Vector();
            if (type2.getSize()==2){
               // 1, 2
               list.add(factory.createStore(mapType(type2), maxlocals+1));
               list.add(factory.createStore(mapType(type1), maxlocals));
               list.add(factory.createLoad(mapType(type2), maxlocals+1));
               list.add(factory.createLoad(mapType(type1), maxlocals));
               list.add(factory.createLoad(mapType(type2), maxlocals+1));
            }else{
               // 1, 1, 1
               org.apache.bcel.generic.Type type0 =
                  context.getStackAt(2)[0].getType();
               list.add(factory.createStore(mapType(type2), maxlocals+2));
               list.add(factory.createStore(mapType(type1), maxlocals+1));
               list.add(factory.createStore(mapType(type0), maxlocals));
               list.add(factory.createLoad(mapType(type1), maxlocals+1));
               list.add(factory.createLoad(mapType(type2), maxlocals+2));
               list.add(factory.createLoad(mapType(type0), maxlocals));
               list.add(factory.createLoad(mapType(type1), maxlocals+1));
               list.add(factory.createLoad(mapType(type2), maxlocals+2));
            }
            maxlocals+=3;
            handle2instlist.put(handles[i], list);
            break;
         }

         case DUP2_X2:{
            org.apache.bcel.generic.Type type4 = 
               context.getStackAt(0)[0].getType();
            org.apache.bcel.generic.Type type3 = 
               context.getStackAt(1)[0].getType();

            java.util.Vector list = new java.util.Vector();
            if (type4.getSize()==2){
               // 2,2 or 1,1,2
               if (type3.getSize()==2){
                  // 2,2
                  list.add(factory.createStore(mapType(type4), maxlocals+2));
                  list.add(factory.createStore(mapType(type3), maxlocals));
                  list.add(factory.createLoad(mapType(type4), maxlocals+2));
                  list.add(factory.createLoad(mapType(type3), maxlocals));
                  list.add(factory.createLoad(mapType(type4), maxlocals+2));
               }else{
                  // 1,1,2
                  org.apache.bcel.generic.Type type2 = 
                     context.getStackAt(2)[0].getType();

                  list.add(factory.createStore(mapType(type4), maxlocals+2));
                  list.add(factory.createStore(mapType(type3), maxlocals+1));
                  list.add(factory.createStore(mapType(type2), maxlocals));
                  list.add(factory.createLoad(mapType(type4), maxlocals+2));
                  list.add(factory.createLoad(mapType(type2), maxlocals));
                  list.add(factory.createLoad(mapType(type3), maxlocals+1));
                  list.add(factory.createLoad(mapType(type4), maxlocals+2));
               }
            }else{
               // 2,1,1 or 1,1,1,1
               org.apache.bcel.generic.Type type2 = 
                  context.getStackAt(2)[0].getType();

               if (type2.getSize()==2){
                  // 2,1,1
                  list.add(factory.createStore(mapType(type4), maxlocals+3));
                  list.add(factory.createStore(mapType(type3), maxlocals+2));
                  list.add(factory.createStore(mapType(type2), maxlocals));
                  list.add(factory.createLoad(mapType(type3), maxlocals+2));
                  list.add(factory.createLoad(mapType(type4), maxlocals+3));
                  list.add(factory.createLoad(mapType(type2), maxlocals));
                  list.add(factory.createLoad(mapType(type3), maxlocals+2));
                  list.add(factory.createLoad(mapType(type4), maxlocals+3));
               }else{
                  // 1,1,1,1
                  org.apache.bcel.generic.Type type1 = 
                     context.getStackAt(3)[0].getType();

                  list.add(factory.createStore(mapType(type4), maxlocals+3));
                  list.add(factory.createStore(mapType(type3), maxlocals+2));
                  list.add(factory.createStore(mapType(type2), maxlocals+1));
                  list.add(factory.createStore(mapType(type1), maxlocals));
                  list.add(factory.createLoad(mapType(type3), maxlocals+2));
                  list.add(factory.createLoad(mapType(type4), maxlocals+3));
                  list.add(factory.createLoad(mapType(type1), maxlocals));
                  list.add(factory.createLoad(mapType(type2), maxlocals+1));
                  list.add(factory.createLoad(mapType(type3), maxlocals+2));
                  list.add(factory.createLoad(mapType(type4), maxlocals+3));
               }
            }
            maxlocals+=4;
            handle2instlist.put(handles[i], list);
            break;
         }

         case POP:{
            org.apache.bcel.generic.Type type = 
               context.getStackAt(0)[0].getType();
            

            java.util.Vector list = new java.util.Vector();
            list.add(factory.createStore(type, maxlocals));
            maxlocals++;
            handle2instlist.put(handles[i], list);
            break;
         }

         case POP2:{
            org.apache.bcel.generic.Type type1 = 
               context.getStackAt(0)[0].getType();
            
            java.util.Vector list = new java.util.Vector();
            if (type1.getSize()==2){
               // 2
               list.add(factory.createStore(type1, maxlocals));
            }else{
               // 1,1
               org.apache.bcel.generic.Type type0 = 
                  context.getStackAt(1)[0].getType();
               
               list.add(factory.createStore(type1, maxlocals));
               list.add(factory.createStore(type0, maxlocals+1));
            }
            maxlocals+=2;
            handle2instlist.put(handles[i], list);
            break;
         }

         case SWAP:{
            org.apache.bcel.generic.Type type1 = 
               context.getStackAt(0)[0].getType();
            org.apache.bcel.generic.Type type0 = 
               context.getStackAt(1)[0].getType();

            java.util.Vector list = new java.util.Vector();
            list.add(factory.createStore(mapType(type1), maxlocals+1));
            list.add(factory.createStore(mapType(type0), maxlocals));
            list.add(factory.createLoad(mapType(type1), maxlocals+1));
            list.add(factory.createLoad(mapType(type0), maxlocals));
            maxlocals+=2;
            handle2instlist.put(handles[i], list);
            break;
         }
         }
      }

      for (java.util.Iterator iter=handle2instlist.keySet().iterator();iter.hasNext();){
         org.apache.bcel.generic.InstructionHandle temp, 
            handle = (org.apache.bcel.generic.InstructionHandle)iter.next();
         java.util.Vector list = (java.util.Vector)handle2instlist.get(handle);

         handle.setInstruction((org.apache.bcel.generic.Instruction)list.get(0));
         
         temp=handle;
         for (int i=1;i<list.size();i++){
            temp = ilist.append(temp, (org.apache.bcel.generic.Instruction)list.get(i));
         }
      }
   }



   public static void main(String args[]) throws Throwable{
      if (args.length<1)
         return;

      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);

      java.util.Hashtable counts = new java.util.Hashtable(1000);

      for (java.util.Iterator citer=app.classes();citer.hasNext();){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         for (java.util.Iterator miter = clazz.methods();miter.hasNext();){
            sandmark.program.Method method = (sandmark.program.Method)miter.next();
            
            org.apache.bcel.generic.InstructionList ilist = method.getInstructionList();

            if (ilist==null)
               continue;

            System.err.println("Method "+clazz.getName()+"."+method.getName()+method.getSignature());

            ExprTreeCFG tree = null;
             
            try{
               tree = new ExprTreeCFG(method);
               tree.rewriteMethod();
            }catch(Throwable t){
               t.printStackTrace();
               continue;
            }
         }
      }

      app.save(args[0]+".out");
   }
}

