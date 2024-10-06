package sandmark.util.newexprtree;

/**
   This class is used to get the information associated with each node in the expression tree.
   @author Kamlesh Kantilal (kamlesh@cs.arizona.edu)
*/

public class ExprTreeNode{
sandmark.util.newexprtree.ExprTree exprtree;
sandmark.util.newexprtree.NodeInfo ni;

ExprTreeNode(ExprTree exprtree,sandmark.util.newexprtree.NodeInfo ni)
{
	this.exprtree=exprtree;
	this.ni=ni;
}


/**
     Returns the instruction handle associated with this node in the expression tree
      If a node is outside the basic block it is set to null
*/

public org.apache.bcel.generic.InstructionHandle getIH()
{
	return ni.getIH();

}


/**
    Returns the graph associated with this node in the expression tree

*/

public sandmark.util.newgraph.MutableGraph  getGraph()
{
return ni.getGraph();
}



/**
     Returns the context provided by stack simulator for the instruction
     associated with this node in the expression tree

*/

public sandmark.analysis.stacksimulator.Context getContext()
{
	return ni.getContext();

}

  public ExprTree getExprTree(){
      return exprtree;
   }

/**
     Returns whether this node is associated with an instruction outside the basic block

*/


public boolean isOutsideBlock()
{
	return	ni.isOutsideBlock();
}

public boolean isMarked()
{
	return	ni.isMarked();
}

public void setMark()
{
	ni.setMark();
}

public void clearMark()
{
	ni.clearMark();
}


}

