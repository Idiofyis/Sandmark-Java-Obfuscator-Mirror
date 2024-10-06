package sandmark.util.opaquepredicatelib;

/**
 * RuntimeIsNullOPLib creates run time non-deterministic predicates
 * at the requested byte code position of a node within the
 * CFG of a method.
 * @author Ashok Venkatraj (ashok@cs.arizona.edu)
 *
 */

public class ObjectComparePredicateGenerator extends OpaquePredicateGenerator {
   private boolean DEBUG = true;
   private static Class compareClasses[] = { 
      org.apache.bcel.generic.IFNONNULL.class,
      org.apache.bcel.generic.IFNULL.class,
   };
   
   private void insertOpaque
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,
       int varnum,
       org.apache.bcel.generic.BranchInstruction comparator) {
      
      org.apache.bcel.generic.InstructionList list = 
         new org.apache.bcel.generic.InstructionList();
      list.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.append(new org.apache.bcel.generic.ALOAD(varnum));
      list.append(comparator);
      list.append(org.apache.bcel.generic.InstructionConstants.POP);
      list.append(org.apache.bcel.generic.InstructionConstants.ICONST_0);

      ThreadPredicateGenerator.updateTargeters(ih,list.getStart());
      method.getInstructionList().insert(ih,list);
      comparator.setTarget(ih);
      method.mark();      
   }
   
   public void insertPredicate(sandmark.program.Method method,
                               org.apache.bcel.generic.InstructionHandle ih,
                               int valueType) {
      if (!canInsertPredicate(method, ih, valueType))
         return;

      sandmark.analysis.defuse.ReachingDefs rd =
         new sandmark.analysis.defuse.ReachingDefs(method);
      sandmark.analysis.initialized.Initialized init =
         new sandmark.analysis.initialized.Initialized(method);
      int locals = method.calcMaxLocals();
      for(int x = 0 ; x < locals ; x++) {
         if(!init.initializedAt(x,ih))
            continue;
         java.util.Set defs = rd.defs(x,ih);
         boolean correctType = true;
         for(java.util.Iterator it = defs.iterator() ; it.hasNext() ; ) {
            sandmark.analysis.defuse.DefWrapper def =
               (sandmark.analysis.defuse.DefWrapper)it.next();
            if(!(def.getType() instanceof 
                 org.apache.bcel.generic.ReferenceType)) {
               correctType = false;
               break;
            }		
         }
         if(!correctType)
            continue;

         if(x == 0) {
            sandmark.analysis.stacksimulator.StackSimulator ss =
               method.getStack();
            sandmark.analysis.stacksimulator.Context cx =
               ss.getInstructionContext(ih);
            sandmark.analysis.stacksimulator.StackData sd[] =
               cx.getLocalVariableAt(0);
            boolean goodType = true;
            for(int i = 0 ; goodType && i < sd.length ; i++)
               if(sd[i].getType() instanceof 
                  org.apache.bcel.verifier.structurals.UninitializedObjectType)
                  goodType = false;
            if(!goodType)
               continue;
         }
         
         if(DEBUG)
            System.out.println(" The Local variable "+ x + " is selected to apply an opaque Predicate " );
         int rand = sandmark.util.Random.getRandom().nextInt(compareClasses.length);
         org.apache.bcel.generic.BranchInstruction comparator = null;
         try {
            Class compareClass = compareClasses[rand];
            java.lang.reflect.Constructor constructor =
               compareClass.getConstructor(new Class[] {org.apache.bcel.generic.InstructionHandle.class});
            comparator = (org.apache.bcel.generic.BranchInstruction)
               constructor.newInstance(new Object[] {null});
         } catch(Exception e) { 
            e.printStackTrace(); 
            throw new Error("shouldn't have crashed here");
         }
         insertOpaque(method,ih,x,comparator);
      }
   }

   public boolean canInsertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      sandmark.analysis.defuse.ReachingDefs rd =
         new sandmark.analysis.defuse.ReachingDefs(method);
      sandmark.analysis.initialized.Initialized init =
         new sandmark.analysis.initialized.Initialized(method);
      int locals = method.calcMaxLocals();
      for(int x = 0 ; x < locals ; x++) {
         if(!init.initializedAt(x,ih))
            continue;
         java.util.Set defs = rd.defs(x,ih);
         boolean correctType = true;
         for(java.util.Iterator it = defs.iterator() ; it.hasNext() ; ) {
            sandmark.analysis.defuse.DefWrapper def =
               (sandmark.analysis.defuse.DefWrapper)it.next();
            if(!def.getType().equals(org.apache.bcel.generic.Type.INT)) {
               correctType = false;
               break;
            }		
         }
         if(correctType)
            return true;

         if(x == 0) {
            sandmark.analysis.stacksimulator.StackSimulator ss =
               method.getStack();
            sandmark.analysis.stacksimulator.Context cx =
               ss.getInstructionContext(ih);
            sandmark.analysis.stacksimulator.StackData sd[] =
               cx.getLocalVariableAt(0);
            boolean goodType = true;
            for(int i = 0 ; goodType && i < sd.length ; i++)
               if(sd[i].getType() instanceof 
                  org.apache.bcel.verifier.structurals.UninitializedObjectType)
                  goodType = false;
            if(goodType)
               return true;
         }
      }
      return false;
   }

   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_OBJECT_OP,
                                   OpaqueManager.PV_UNKNOWN);
      return sInfo;
   }
}










