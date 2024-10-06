package sandmark.util.newexprtree;

public class MethodExprTree extends sandmark.analysis.controlflowgraph.MethodCFG{



	sandmark.util.newexprtree.ComputeExprTree et;
	java.util.ArrayList exprTreeBlockList;
	java.util.HashMap BlockToETB;

public MethodExprTree(sandmark.program.Method method,
		      boolean _exceptionsMatter){

      super(method, _exceptionsMatter);

      //remove blocks that are completely unreachable since stack simulator
      //gets confused and generates invalid contexts for them
      removeUnreachable(source());
      //We don't ever want to do this next line.
      //removeUnreachable(sink());

      //build the exprTrees for the basic blocks
      buildExprTrees(method);
      exprTreeBlockList = new java.util.ArrayList();
      BlockToETB = new java.util.HashMap();
      computeBlockList();
   }//end constructor




   void buildExprTrees(sandmark.program.Method method){

      et = new sandmark.util.newexprtree.ComputeExprTree(method,this);
   }


	void computeBlockList() {
	   java.util.Iterator nodeIter = nodes();
	   while (nodeIter.hasNext()) {
	      sandmark.analysis.controlflowgraph.BasicBlock bb =
		 (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next();
	      ExprTreeBlock etb = new ExprTreeBlock(this, bb);
	      exprTreeBlockList.add(etb);
	      BlockToETB.put(bb, etb);
	   }
	}


	public java.util.ArrayList getExprTreeBlocks(){
	      return exprTreeBlockList;
	 }

	public ExprTreeBlock getExprTreeBlock(sandmark.analysis.controlflowgraph.BasicBlock  block){
	      return (ExprTreeBlock)BlockToETB.get(block);
	 }


	public String toString()
	{
		return et.toString();
	}

  /* It returns the node associated with an instruction */
	public sandmark.util.newexprtree.Node  iToNode(org.apache.bcel.generic.InstructionHandle x)
    {
	 return  et.iToNode(x);

   }
	/**
       Returns the sandmark.util.newexprtree.NodeInfo associated with a sandmark.util.newexprtree.Node .
       @param tempgn a node in the expression tree whose corresponding information is desired
	*/
	public sandmark.util.newexprtree.NodeInfo nodeToInfo(sandmark.util.newexprtree.Node  tempgn)
	{
	 return et.nodeToInfo(tempgn);

	}

	/* It returns the instruction associated with a node */
	public org.apache.bcel.generic.InstructionHandle nodeToI(sandmark.util.newexprtree.Node  x)
	{
		return  et.nodeToI(x);

	}



	public java.util.ArrayList getInstList(sandmark.util.newgraph.MutableGraph dg)
	{
		return  et.getInstList(dg);
	}

} //end class

