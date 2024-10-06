package sandmark.util.opaquepredicatelib;

/**
 * RuntimeStrOPLib creates run time non-deterministic predicates
 * at the requested byte code position of a node within the
 * CFG of a method.
 * @author Ashok Venkatraj (ashok@cs.arizona.edu)
 *
 */

public class StringOpPredicateGenerator extends OpaquePredicateGenerator {
   private boolean DEBUG = true;
   
   private void insertOpaque
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,
       int varnum,
       org.apache.bcel.generic.BranchInstruction comparator) {
      
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());
      int val= sandmark.util.Random.getRandom().nextInt(127);
      
      org.apache.bcel.generic.InstructionList list =
         new org.apache.bcel.generic.InstructionList();
      list.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
      list.append(new org.apache.bcel.generic.ALOAD(varnum));
      list.append
         (factory.createInvoke
          ("java.lang.String","length",org.apache.bcel.generic.Type.getReturnType("()I"),
           org.apache.bcel.generic.Type.getArgumentTypes("()I"),
           org.apache.bcel.Constants.INVOKEVIRTUAL));
      list.append(new org.apache.bcel.generic.BIPUSH((byte)val));
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
      int rand = sandmark.util.Random.getRandom().nextInt(IntComparePredicateGenerator.compareClasses.length);
      org.apache.bcel.generic.BranchInstruction comparator = null;
      try {
         Class compareClass = IntComparePredicateGenerator.compareClasses[rand];
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
      sandmark.analysis.initialized.Initialized init =
         new sandmark.analysis.initialized.Initialized(method);
      sandmark.analysis.defuse.ReachingDefs rd =
         new sandmark.analysis.defuse.ReachingDefs(method);
      sandmark.analysis.stacksimulator.StackSimulator ss =
         method.getStack();
      int locals = method.calcMaxLocals();
      for(int x = 0 ; x < locals ; x++) {
         if(!init.initializedAt(x,ih))
            continue;
         java.util.Set defs = rd.defs(x,ih);
         boolean correctType = true;
         for(java.util.Iterator it = defs.iterator() ; it.hasNext() ; ) {
            sandmark.analysis.defuse.DefWrapper def =
               (sandmark.analysis.defuse.DefWrapper)it.next();
            if(!(def.getType() instanceof org.apache.bcel.generic.ReferenceType)) {
               correctType = false;
               continue;
            }
            if(def instanceof sandmark.analysis.defuse.ThisDefWrapper) {
               if(!method.getEnclosingClass().getType().equals
                  (org.apache.bcel.generic.Type.STRING)) {
                  correctType = false;
                  continue;
               }
            } else if(def instanceof sandmark.analysis.defuse.ParamDefWrapper) {
               sandmark.analysis.defuse.ParamDefWrapper dw =
                  (sandmark.analysis.defuse.ParamDefWrapper)def;
               if(!method.getArgumentTypes()[dw.getParamListIndex()].equals
                  (org.apache.bcel.generic.Type.STRING)) {
                  correctType = false;
                  continue;
               }
            } else if(def instanceof sandmark.analysis.defuse.InstructionDefWrapper) {
               sandmark.analysis.defuse.InstructionDefWrapper dw =
                  (sandmark.analysis.defuse.InstructionDefWrapper)def;
               sandmark.analysis.stacksimulator.Context cx =
                  ss.getInstructionContext(dw.getIH());
               sandmark.analysis.stacksimulator.StackData sd[] = cx.getStackAt(0);
               for(int i = 0 ; i < sd.length ; i++)
                  if(!sd[i].getType().equals(org.apache.bcel.generic.Type.STRING)) {
                     correctType = false;
                     continue;
                  }
            } else {
               throw new Error("unknown def type " + def.getClass().getName());
            }
         }
         return x;
      }
      return -1;      
   }

   public boolean canInsertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      int lv = findGoodLocal(method,ih);
      return lv != -1;
   }

   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_STRING_OP,
                                   OpaqueManager.PV_UNKNOWN);
      return sInfo;
   }
}
