package sandmark.util.newexprtree;

/** Represents a switch statement.
 *  The 'default' target of a switch statement is accessed with 
 *  getTarget and setTarget (from BranchExpr).
 *  Emits: LOOKUPSWITCH, TABLESWITCH.
 */
public class SwitchExpr extends BranchExpr{
   private ValueExpr index;
   private int[] matches;
   private org.apache.bcel.generic.InstructionHandle[] handleTargets;
   private Expr[] exprTargets;
   private boolean islookupswitch;

   /*package*/ SwitchExpr(ValueExpr _index, int[] _matches,
                          org.apache.bcel.generic.InstructionHandle[] _targets,
                          org.apache.bcel.generic.InstructionHandle _target,
                          boolean islookup){
      super(_target);
      index = _index;
      matches = _matches;
      handleTargets = _targets;
      islookupswitch=islookup;
   }

   /** Constructs a SwitchExpr.
    *  @param _index the value to switch on.
    *  @param _matches the list of cases to match.
    *  @param targets the list of targets for the cases (parallel array to matches).
    *  @param defaultExpr the 'default' target.
    *  @param islookup true iff this hould be a LOOKUPSWITCH and not a TABLESWITCH.
    */
   public SwitchExpr(ValueExpr _index, int[] _matches, 
                     Expr[] targets, Expr defaultExpr, 
                     boolean islookup){
      super(defaultExpr);
      index = _index;
      matches = _matches;
      exprTargets = targets;
      islookupswitch = islookup;
   }

   /*package*/ org.apache.bcel.generic.InstructionHandle[] getHandleTargets(){
      return handleTargets;
   }

   /** Returns true iff this is a LOOKUPSWITCH, not a TABLESWITCH.
    */
   public boolean isLookupSwitch(){
      return islookupswitch;
   }

   /** Resets the list of targets for the cases (not the default case).
    *  This list must be exactly as long as the list of cases.
    */
   public void setTargets(Expr[] targets){
      if (targets==null || targets.length!=matches.length)
         throw new IllegalArgumentException("Target list must have same length as case list");
      exprTargets = targets;
   }

   /** Sets the list of 'case' labels. This array is 
    *  parallel to the array of branch targets.
    */
   public void setMatches(int[] _matches){
      if (_matches==null || _matches.length!=exprTargets.length)
         throw new IllegalArgumentException("Match list must have same length as target list");
      matches = _matches;
   }

   /** Returns the list of targets for the cases (not the default case).
    */
   public Expr[] getTargets(){
      return exprTargets;
   }

   /** Returns the value to be switched on.
    */
   public ValueExpr getIndexValue(){
      return index;
   }

   /** Sets the expression to be switched on.
    */
   public void setIndexValue(ValueExpr _index){
      index = _index;
   }

   /** Returns the list of case labels.
    */
   public int[] getMatches(){
      return matches;
   }

   public String toString(){
      String result = "SwitchExpr["+islookupswitch+","+index+",[";
      if (exprTargets!=null){
         for (int i=0;i<matches.length;i++){
            if (i>0)result+=",";
            result+=matches[i];
         }
      }
      return result+"]]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(index.emitBytecode(factory));
      if (islookupswitch){
         org.apache.bcel.generic.InstructionHandle[] targets = 
            new org.apache.bcel.generic.InstructionHandle[matches.length];
         result.add(new org.apache.bcel.generic.LOOKUPSWITCH(matches, targets, null));
      }else{
         org.apache.bcel.generic.InstructionHandle[] targets = 
            new org.apache.bcel.generic.InstructionHandle[matches.length];
         result.add(new org.apache.bcel.generic.TABLESWITCH(matches, targets, null));
      }
      return result;
   }
}
