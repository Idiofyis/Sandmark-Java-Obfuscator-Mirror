package sandmark.util.newexprtree;

public class ExprTreeBlock  {

   java.util.ArrayList exprTreeList; //list of expression trees for block
   MethodExprTree met; //the MethodExprTree the block is associated with

   protected sandmark.analysis.controlflowgraph.BasicBlock bb;

	/**
    * This class contains all the expresssion trees associated within a basic
    * blocks of the method  */

   protected ExprTreeBlock(MethodExprTree met,sandmark.analysis.controlflowgraph.BasicBlock bb){

		this.met = met;
		this.bb=bb;
		exprTreeList=new java.util.ArrayList();

		java.util.ArrayList Grlist=met.et.blockToGrlist(bb);

		sandmark.util.newgraph.MutableGraph myGr;

		for(int k=0;k<Grlist.size();k++)
		{
			myGr=(sandmark.util.newgraph.MutableGraph)Grlist.get(k);
			exprTreeList.add(new sandmark.util.newexprtree.ExprTree(met,this,myGr));
		}
   }


   //public methods

   /**
    * Returns a list of the expression trees associated with this block.
    */
   public java.util.ArrayList getExprTrees(){
      return exprTreeList;
   }

   /**
    * Returns the MethodExprTree this block is associated with.
    */
   public MethodExprTree getMethodExprTree(){
      return met;
   }

   public sandmark.analysis.controlflowgraph.BasicBlock getBasicBlock(){
      return bb;
   }

	public String toString()
	{
		return met.et.toString(bb);
	}



} //end ExprTreeBlock class

