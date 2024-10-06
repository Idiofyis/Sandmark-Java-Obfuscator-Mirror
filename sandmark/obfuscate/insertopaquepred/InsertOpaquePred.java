package sandmark.obfuscate.insertopaquepred;

public class InsertOpaquePred extends sandmark.obfuscate.MethodObfuscator {

   private boolean DEBUG = false;

   public String getShortName() {
      return "Insert Opaque Predicates";
   }

   public String getLongName() {
      return "Inserts an opaque predicate in all boolean expressions.";
   }

   public java.lang.String getAlgHTML(){
      return
         "<HTML><BODY>" +
         "InsertOpaquePred is a method level obfuscator which appends" +
         " an opaque predicate, supplied by the opaque predicated library," +
         " to every boolean expression in the method." +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:mylesg@cs.arizona.edu\">Ginger Myles</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/insertopaquepred/doc/help.html";
   }

   public java.lang.String getAuthor() {
      return "Ginger Myles";
   }

    public java.lang.String getAuthorEmail() {
        return "mylesg@cs.arizona.edu";
    }

   public java.lang.String getDescription() {
      return
         "InsertOpaquePred is a method level obfuscator which appends" +
         " an opaque predicate, supplied by the opaque predicated library," +
         " to every boolean expression in the method.";
   }

   public sandmark.config.ModificationProperty[] getMutations() {
      return new sandmark.config.ModificationProperty[]{};
   }

   public void apply(sandmark.program.Method m) throws Exception {

      if(m == null || m.isAbstract() || m.isInterface())
         return;

      sandmark.util.newexprtree.MethodExprTree met = 
         new sandmark.util.newexprtree.MethodExprTree(m, false);
      java.util.ArrayList exprTreeBlocks = met.getExprTreeBlocks();
      
      for(int i=0; i < exprTreeBlocks.size(); i++){
         sandmark.util.newexprtree.ExprTreeBlock etb = 
            (sandmark.util.newexprtree.ExprTreeBlock)exprTreeBlocks.get(i);
         java.util.ArrayList exprTrees = etb.getExprTrees();
         for(int j=0; j < exprTrees.size(); j++){
            sandmark.util.newexprtree.ExprTree et =
               (sandmark.util.newexprtree.ExprTree)exprTrees.get(j);
            java.util.ArrayList insts = et.getInstructionList();
            for(int k=0; k < insts.size(); k++){
               org.apache.bcel.generic.InstructionHandle ih =
                  (org.apache.bcel.generic.InstructionHandle)insts.get(k);
               org.apache.bcel.generic.Instruction inst = ih.getInstruction();
               if(inst instanceof org.apache.bcel.generic.IFEQ ||
                  inst instanceof org.apache.bcel.generic.IFNE ||
                  inst instanceof org.apache.bcel.generic.IFLT ||
                  inst instanceof org.apache.bcel.generic.IFLE || 
                  inst instanceof org.apache.bcel.generic.IFGT ||
                  inst instanceof org.apache.bcel.generic.IFGE ||
                  inst instanceof org.apache.bcel.generic.IF_ICMPEQ ||
                  inst instanceof org.apache.bcel.generic.IF_ICMPNE ||
                  inst instanceof org.apache.bcel.generic.IF_ICMPLT ||
                  inst instanceof org.apache.bcel.generic.IF_ICMPLE ||
                  inst instanceof org.apache.bcel.generic.IF_ICMPGT ||
                  inst instanceof org.apache.bcel.generic.IF_ICMPGE){
                  org.apache.bcel.generic.InstructionList il =
                     m.getInstructionList();
                  org.apache.bcel.generic.InstructionHandle pushPred =
                     il.insert(inst, new org.apache.bcel.generic.IADD());
                  m.mark();
                  sandmark.util.opaquepredicatelib.PredicateFactory predicates[] =
                     sandmark.util.opaquepredicatelib.OpaqueManager.getPredicatesByValue(
                     sandmark.util.opaquepredicatelib.OpaqueManager.PV_FALSE);
                  java.util.HashSet badPreds = new java.util.HashSet();
                  sandmark.util.opaquepredicatelib.OpaquePredicateGenerator
                     predicate = null;
                  while(predicate == null && badPreds.size() != predicates.length){
                     int which = sandmark.util.Random.getRandom().nextInt() %
                        predicates.length;
                     if(which < 0)
                        which += predicates.length;
                     predicate = predicates[which].createInstance();
                     if(!predicate.canInsertPredicate(m, pushPred,
                        sandmark.util.opaquepredicatelib.OpaqueManager.PV_FALSE)){
                        badPreds.add(predicates[which]);
                        predicate = null;
                     }
                  }
                  if(predicate != null) {
                     if(DEBUG)System.out.println("inserting predicate in method " +
                           m.getName() + " at " + ih);
                     predicate.insertPredicate(m, pushPred,
                           sandmark.util.opaquepredicatelib.OpaqueManager.PV_FALSE);
                  } else {
                     if(DEBUG)
                        System.out.println("no predicate to insert at " + ih);
                     il.insert(pushPred,new org.apache.bcel.generic.ICONST(0));
                  }
               }
            }
         }
      }
   }
}
