package sandmark.util.opaquepredicatelib;

/**
 * RuntimeIntOPLib creates run time non-deterministic predicates
 * at the requested byte code position of a node within the
 * CFG of a method.
 * @author Ashok Purushotham Ramasamy Venkatraj (ashok@cs.arizona.edu)
 *
 */

public class IntComparePredicateGenerator extends OpaquePredicateGenerator {
   static Class compareClasses[] = {
      org.apache.bcel.generic.IF_ICMPEQ.class,
      org.apache.bcel.generic.IF_ICMPNE.class,
      org.apache.bcel.generic.IF_ICMPGT.class,
      org.apache.bcel.generic.IF_ICMPLT.class,
      org.apache.bcel.generic.IF_ICMPLE.class,
      org.apache.bcel.generic.IF_ICMPGE.class,
   };
   
   private boolean DEBUG = true;
   
   private void insertOpaque
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,
       int varnum,
       org.apache.bcel.generic.BranchInstruction comparator) {

      int val=sandmark.util.Random.getRandom().nextInt(127);
      
      org.apache.bcel.generic.InstructionList list = 
         new org.apache.bcel.generic.InstructionList();
      list.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.append(new org.apache.bcel.generic.BIPUSH((byte)val));
      list.append(new org.apache.bcel.generic.ILOAD(varnum));
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

      int lv = findGoodLocal(method,ih);
      if(DEBUG)
         System.out.println(" The Local variable "+ lv + " is selected to apply an opaque Predicate " );
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
      insertOpaque(method,ih,lv,comparator);
   }

   private static int findGoodLocal
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih) {

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
            return x;
      }
      return -1;
   }

   public boolean canInsertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,
       int valueType) {
      return findGoodLocal(method,ih) != -1;
   }

   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_INT_OP,
                                   OpaqueManager.PV_UNKNOWN);
      return sInfo;
   }
}










