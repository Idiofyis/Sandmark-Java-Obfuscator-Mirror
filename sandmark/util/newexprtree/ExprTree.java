package sandmark.util.newexprtree;

public class ExprTree {

   //for the following instruction handles may work better than instructions
   java.util.ArrayList instructionList;  //list of instructions in the expr tree
   //Tree instructionTree; //tree of instructions reprsenting the expr tree
   java.util.ArrayList defs; //list of the instructions that are defs
   java.util.ArrayList uses; //list of the instructions that are uses
   sandmark.util.newgraph.MutableGraph gr;
   MethodExprTree met;

  java.util.ArrayList exprTreeNodeList;
  ExprTreeBlock etb;

	/**
	* This class contains a single expresssion tree */

  ExprTree(MethodExprTree met,ExprTreeBlock etb, sandmark.util.newgraph.MutableGraph gr){
	  this.gr=gr;
	  this.met = met;
	  this.etb=etb;
	  instructionList=met.et.getInstList(gr);
	  defs=new java.util.ArrayList();
	  uses=new java.util.ArrayList();
	  exprTreeNodeList=new java.util.ArrayList();
      computeDefs();
      computeUses();
      computeExprTreeNodeList();
   } //end constructor


   //private methods

   /**
    * Determines which instructions in the expression tree are defs.
    */
   private void computeExprTreeNodeList(){
      java.util.Iterator nodeIter = gr.nodes();
      while (nodeIter.hasNext()) {
	 sandmark.util.newexprtree.Node bb =
	    (sandmark.util.newexprtree.Node)nodeIter.next();
	 exprTreeNodeList.add(new ExprTreeNode(this,
					       met.et.nodeToInfo(bb)));
      }
   }


   /**
    * Determines which instructions in the expression tree are defs.
    */
   private void computeDefs(){

	org.apache.bcel.generic.InstructionHandle handle;
	for(int k=0;k<instructionList.size();k++)
	{
		handle=((org.apache.bcel.generic.InstructionHandle)instructionList.get(k));
		org.apache.bcel.generic.Instruction inst =
            handle.getInstruction();

           if(inst instanceof org.apache.bcel.generic.StoreInstruction ||
              inst instanceof org.apache.bcel.generic.IINC)
              defs.add(handle);

 	 }

   }

   /**
    * Determines which instructions in the expression tree are uses.
    */
   private void computeUses(){
	org.apache.bcel.generic.InstructionHandle handle;
	for(int k=0;k<instructionList.size();k++)
	{
			handle=((org.apache.bcel.generic.InstructionHandle)instructionList.get(k));
			org.apache.bcel.generic.Instruction inst =
	            handle.getInstruction();

	           if(inst instanceof org.apache.bcel.generic.LoadInstruction
	            || inst instanceof org.apache.bcel.generic.IINC )
	               uses.add(handle);

 	 }

   }


   //public methods
   /**
    * Returns the list of instruction handles associated with the expression tree.
    */
   public java.util.ArrayList getInstructionList(){
      return instructionList;
   }

	public sandmark.util.newgraph.MutableGraph getGraph(){
	 return gr;

    }

    /**
	    * @return The nodes in the graph that do not have predecessors.
    */

    public java.util.ArrayList roots(){
       java.util.ArrayList rval = new java.util.ArrayList();
       for (java.util.Iterator i = gr.roots(); i.hasNext(); )
	  rval.add(i.next());
       return rval;
   }


   /**
    * Returns a list of instruction handles that are the defs of the expression
    * tree.
    */
   public java.util.ArrayList getDefs(){
      return defs;
   }

   /**
    * Returns a list of instruction handles that are the uses of the expression
    * tree.
    */
   public java.util.ArrayList getUses(){
      return uses;
   }

	public String toString()
	{
		return met.et.toString(gr);
	}




   public ExprTreeBlock getExprTreeBlock(){
	       return etb;
	 }


	/**
    * Returns a list of the expression trees nodes associated with this tree.
    */
   public java.util.ArrayList getExprTreeNodes(){
      return exprTreeNodeList;
   }

   public int compareTo(ExprTree et){
      java.util.ArrayList firstList = this.getInstructionList();
      java.util.ArrayList secondList = et.getInstructionList();

      if(firstList.size() == secondList.size()){
         org.apache.bcel.generic.InstructionHandle firstHandle =
            (org.apache.bcel.generic.InstructionHandle)firstList.get(0);
         org.apache.bcel.generic.InstructionHandle secondHandle =
            (org.apache.bcel.generic.InstructionHandle)secondList.get(0);
         return firstHandle.getPosition() - secondHandle.getPosition();
      }else
         return -1;
   }


} //end ExprTree class

