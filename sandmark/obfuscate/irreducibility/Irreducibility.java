package sandmark.obfuscate.irreducibility;

public class Irreducibility extends sandmark.obfuscate.MethodObfuscator {
   public String getShortName() {
      return "Irreducibility";
   }

   public String getLongName() {
      return "Insert jumps into the method so that its control flow graph is irreducible";
   }

   public java.lang.String getAlgHTML(){
      return
         "<HTML><BODY>" +
         "Irreducibility is a method obfuscator." +
         " The algorithm inserts jumps into a method via opaque predicates" +
         " so that the control flow graph is irreducible. This" +
         " inhibits decompilation." +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:ecarter@cs.arizona.edu\">Edward Carter</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/irreducibility/doc/help.html";
   }

   public java.lang.String getAuthor() {
      return "Edward Carter";
   }

    public java.lang.String getAuthorEmail() {
        return "ecarter@cs.arizona.edu";
    }

   public java.lang.String getDescription() {
      return
         "Insert jumps into a method via opaque predicates " +
         "so that the control flow graph is irreducible.  This " +
         "inhibits decompilation.";
   }

   public sandmark.config.ModificationProperty[] getMutations() {
      return new sandmark.config.ModificationProperty[]{};
   }

   private static final org.apache.bcel.generic.Instruction pop =
      new org.apache.bcel.generic.POP();
   private static final org.apache.bcel.generic.Instruction pop2 =
      new org.apache.bcel.generic.POP2();
   private static final org.apache.bcel.generic.Instruction pushNull =
      new org.apache.bcel.generic.ACONST_NULL();
   private static final org.apache.bcel.generic.Instruction pushLong =
      new org.apache.bcel.generic.LCONST(0);
   private static final org.apache.bcel.generic.Instruction pushFloat =
      new org.apache.bcel.generic.FCONST(0);
   private static final org.apache.bcel.generic.Instruction pushDouble =
      new org.apache.bcel.generic.DCONST(0);
   private static final org.apache.bcel.generic.Instruction pushInt =
      new org.apache.bcel.generic.ICONST(0);

   private static org.apache.bcel.generic.Type getLVType(sandmark.analysis.stacksimulator.Context c,
                                                         int index) {
      try {
         return c.getLocalVariableAt(index)[0].getType();
      }
      catch (java.lang.NullPointerException npe) {
         return null;
      }
      catch (java.lang.IndexOutOfBoundsException iobe) {
	 return null;
      }
   }

   private static boolean goodType(org.apache.bcel.generic.Type t) {
      return !(t instanceof org.apache.bcel.verifier.structurals.UninitializedObjectType
               || t instanceof org.apache.bcel.generic.ReturnaddressType);
   }

   private static boolean okType(org.apache.bcel.generic.Type t) {
      return !(t instanceof org.apache.bcel.verifier.structurals.UninitializedObjectType);
   }

   public void apply(sandmark.program.Method meth) throws Exception {
      sandmark.analysis.controlflowgraph.MethodCFG cfg = null;
      try {
         cfg = new sandmark.analysis.controlflowgraph.MethodCFG(meth, true);
	 if (sandmark.Constants.DEBUG) {
	    String dotname = meth.getClassName()
	       + "." + meth.getName() + ".dot";
	    sandmark.util.newgraph.Graphs.dotInFile(cfg, dotname);
	 }
      }
      catch (sandmark.analysis.controlflowgraph.EmptyMethodException e) {
         return;
      }

      if(sandmark.Constants.DEBUG)System.out.println("--------------------------------------------");
      if(sandmark.Constants.DEBUG)System.out.println("meth = " + meth);
      if(sandmark.Constants.DEBUG)System.out.println("--------------------------------------------");

      sandmark.util.newgraph.Graph acyclic =
         sandmark.util.newgraph.Graphs.createGraph(null, null);
      acyclic = acyclic.addNode(cfg.source());
      java.util.LinkedList queue = new java.util.LinkedList();
      queue.add(cfg.source());
      while (!queue.isEmpty()) {
         java.lang.Object node = queue.removeFirst();
         for (java.util.Iterator i = cfg.outEdges(node); i.hasNext(); ) {
            sandmark.util.newgraph.Edge e =
               (sandmark.util.newgraph.Edge)i.next();
            if (acyclic.hasNode(e.sinkNode())) {
               if (!acyclic.reachable(e.sinkNode(), node))
                  acyclic = acyclic.addEdge(e);
            }
            else {
               queue.add(e.sinkNode());
               acyclic = acyclic.addEdge(e);
            }
         }
      }
      
      java.util.HashSet subroutineBlocks = new java.util.HashSet(),
         tmp = new java.util.HashSet();
      for(org.apache.bcel.generic.InstructionHandle ih = 
          meth.getInstructionList().getStart() ; ih != null ; 
          ih = ih.getNext()) {
         if(ih.getInstruction() instanceof 
            org.apache.bcel.generic.JsrInstruction) {
            org.apache.bcel.generic.JsrInstruction instr =
               (org.apache.bcel.generic.JsrInstruction)ih.getInstruction();
            sandmark.analysis.controlflowgraph.BasicBlock bb =
               cfg.getBlock(instr.getTarget());
            if(bb != null)
               tmp.add(bb);
         }
      }
      for(java.util.Iterator it = tmp.iterator () ; it.hasNext() ; ) {
         Object subroutineBlock = it.next();
         subroutineBlocks.add(subroutineBlock);
         sandmark.util.newgraph.Graph g = 
            cfg.graph().removeUnreachable(subroutineBlock);
         for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; )
            subroutineBlocks.add(nodes.next());
      }

      sandmark.util.newgraph.Graph d = cfg.dominatorTree(cfg.source());
      sandmark.analysis.stacksimulator.StackSimulator ss =
         new sandmark.analysis.stacksimulator.StackSimulator(meth);
      java.util.Vector potential = new java.util.Vector();
      int depth;

      for (java.util.Iterator i = cfg.nodes(); i.hasNext(); ) {
         sandmark.analysis.controlflowgraph.BasicBlock u =
            (sandmark.analysis.controlflowgraph.BasicBlock)i.next();
         if (!u.equals(cfg.source()) && !u.equals(cfg.sink())) {
            for (java.util.Iterator j = cfg.nodes(); j.hasNext(); ) {
               sandmark.analysis.controlflowgraph.BasicBlock v =
                  (sandmark.analysis.controlflowgraph.BasicBlock)j.next();
               if (!v.equals(cfg.source())
                   && !v.equals(cfg.sink())
                   && acyclic.reachable(v, u)
                   && !d.reachable(v, u) && !subroutineBlocks.contains(v)
                   && !subroutineBlocks.contains(u)) {
                  org.apache.bcel.generic.InstructionHandle ui = u.getIH();
                  org.apache.bcel.generic.InstructionHandle vi = v.getIH();
                  sandmark.analysis.stacksimulator.Context uc =
                     ss.getInstructionContext(ui);
                  sandmark.analysis.stacksimulator.Context vc =
                     ss.getInstructionContext(vi);

                  boolean good = true;

                  for (depth = 0; good && depth < uc.getStackSize(); depth++) {
                     org.apache.bcel.generic.Type t =
                        uc.getStackAt(depth)[0].getType();
                     good = okType(t);
                  }
                  for (depth = 0; good && depth < vc.getStackSize(); depth++) {
                     org.apache.bcel.generic.Type t =
                        vc.getStackAt(depth)[0].getType();
                     good = goodType(t);
                  }
                  for (depth = 0; good && depth < meth.getMaxLocals(); depth++) {
                     org.apache.bcel.generic.Type oldType =
                        getLVType(uc, depth);
                     org.apache.bcel.generic.Type newType =
                        getLVType(vc, depth);
                     good = okType(oldType) && goodType(newType);
                  }

                  if (good)
                     potential.add(new sandmark.util.newgraph.EdgeImpl(u, v));
               }
            }
         }
      }

      if (potential.size() == 0)
         return;

      java.util.Random r = sandmark.util.Random.getRandom();
      int index = (int)(r.nextDouble() * potential.size());

      sandmark.util.newgraph.Edge e =
         (sandmark.util.newgraph.Edge)potential.get(index);
      sandmark.analysis.controlflowgraph.BasicBlock u =
         (sandmark.analysis.controlflowgraph.BasicBlock)e.sourceNode();
      sandmark.analysis.controlflowgraph.BasicBlock v =
         (sandmark.analysis.controlflowgraph.BasicBlock)e.sinkNode();

      org.apache.bcel.generic.InstructionHandle ui = u.getIH();
      org.apache.bcel.generic.InstructionHandle vi = v.getIH();
      org.apache.bcel.generic.InstructionList stackBlock =
         new org.apache.bcel.generic.InstructionList();
      sandmark.analysis.stacksimulator.Context uc =
         ss.getInstructionContext(ui);
      sandmark.analysis.stacksimulator.Context vc =
         ss.getInstructionContext(vi);

      for (depth = 0; depth < uc.getStackSize(); depth++) {
         if (uc.getStackAt(depth)[0].getSize() == 1)
            stackBlock.append(pop);
         else
            stackBlock.append(pop2);
      }

      for (depth = vc.getStackSize() - 1; depth >= 0; depth--) {
         org.apache.bcel.generic.Type t =
            vc.getStackAt(depth)[0].getType();
         if (t instanceof org.apache.bcel.generic.ReferenceType) {
            stackBlock.append(pushNull);
         }
         else if (t instanceof org.apache.bcel.generic.BasicType) {
            if (t.equals(org.apache.bcel.generic.Type.LONG))
               stackBlock.append(pushLong);
            else if (t.equals(org.apache.bcel.generic.Type.FLOAT))
               stackBlock.append(pushFloat);
            else if (t.equals(org.apache.bcel.generic.Type.DOUBLE))
               stackBlock.append(pushDouble);
            else
               stackBlock.append(pushInt);
            if(sandmark.Constants.DEBUG)System.out.println("--------------------------------------------");
            if(sandmark.Constants.DEBUG)System.out.println("target = " + vi);
            if(sandmark.Constants.DEBUG)System.out.println("stack at depth " + depth + " = " + t);
            if(sandmark.Constants.DEBUG)System.out.println("--------------------------------------------");
         }
         else
            throw new java.lang.RuntimeException("unknown type");
      }

      for (int lvindex = 0; lvindex < meth.getMaxLocals(); lvindex++) {
         org.apache.bcel.generic.Type oldType =
            getLVType(uc, lvindex);
         org.apache.bcel.generic.Type newType =
            getLVType(vc, lvindex);
         if(sandmark.Constants.DEBUG)System.out.println("--------------------------------------------");
         if(sandmark.Constants.DEBUG)System.out.println("target = " + vi);
         if(sandmark.Constants.DEBUG)System.out.println("local at index " + lvindex + " = " + newType);
         if(sandmark.Constants.DEBUG)System.out.println("origin = " + ui);
         if(sandmark.Constants.DEBUG)System.out.println("local at index " + lvindex + " = " + oldType);
         if(sandmark.Constants.DEBUG)System.out.println("--------------------------------------------");
         if (newType != null
             && newType instanceof org.apache.bcel.generic.ReferenceType) {
            if (oldType == null
                || !(oldType instanceof org.apache.bcel.generic.ReferenceType)
                || !(((org.apache.bcel.generic.ReferenceType)oldType).isAssignmentCompatibleWith(newType))) {
               stackBlock.append(pushNull);
               stackBlock.append(new org.apache.bcel.generic.ASTORE(lvindex));
            }
         }
         else if (newType != null) {
            boolean replace = oldType == null;
            if (!replace) {
               try {
                  replace = !newType.equals(oldType);
               }
               catch (java.lang.NullPointerException npe) {
                  replace = true;
               }
            }
            if (replace) {
               if (newType.equals(org.apache.bcel.generic.Type.LONG)) {
                  stackBlock.append(pushLong);
                  stackBlock.append(new org.apache.bcel.generic.LSTORE(lvindex++));
               }
               else if (newType.equals(org.apache.bcel.generic.Type.FLOAT)) {
                  stackBlock.append(pushFloat);
                  stackBlock.append(new org.apache.bcel.generic.FSTORE(lvindex));
               }
               else if (newType.equals(org.apache.bcel.generic.Type.DOUBLE)) {
                  stackBlock.append(pushDouble);
                  stackBlock.append(new org.apache.bcel.generic.DSTORE(lvindex++));
               }
               else {
                  stackBlock.append(pushInt);
                  stackBlock.append(new org.apache.bcel.generic.ISTORE(lvindex));
               }
            }
         }
      }

      org.apache.bcel.generic.InstructionList il = meth.getInstructionList();

      if(sandmark.Constants.DEBUG)System.out.println(il);
      if(sandmark.Constants.DEBUG)System.out.println("ui = " + ui);
      if(sandmark.Constants.DEBUG)System.out.println("vi = " + vi);
      if(sandmark.Constants.DEBUG)System.out.println();

      sandmark.program.Class cls = meth.getEnclosingClass();
      sandmark.program.Application app = cls.getApplication();

      org.apache.bcel.generic.InstructionHandle pushPredicateValue =
	  il.insert(ui,new org.apache.bcel.generic.ICONST(1));
      il.insert(ui, new org.apache.bcel.generic.IFNE(ui));
      il.insert(ui, stackBlock);
      il.insert(ui, new org.apache.bcel.generic.GOTO_W(vi));
      il.setPositions();
      meth.mark();

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
		 (meth,pushPredicateValue,
		  sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE)) {
		  badPreds.add(predicates[which]);
		  predicate = null;
	      }
      }
      if(predicate != null) {
         //System.out.println("inserting predicate in method " + meth.getName() +
         //" at " + pushPredicateValue);
         predicate.insertPredicate
         (meth,pushPredicateValue,
               sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE);
         pushPredicateValue.setInstruction(new org.apache.bcel.generic.NOP());
      }
      if(sandmark.Constants.DEBUG)System.out.println(il);
   }
    //System.out.println("wazzup");
}

