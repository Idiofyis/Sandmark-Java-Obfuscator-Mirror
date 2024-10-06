package sandmark.util.exprtree;

/**
   This class is used to get the information associated with each node in the expression tree.
   @author Kamlesh Kantilal (kamlesh@cs.arizona.edu)
*/

public class NodeInfo{
org.apache.bcel.generic.InstructionHandle ih;
sandmark.analysis.stacksimulator.Context cn;
sandmark.util.newgraph.MutableGraph gr;
boolean outsideBlock;
boolean mark;




NodeInfo()
{
 ih=null;
 cn=null;
 outsideBlock=false;
 mark=false;
 gr=null;
}

void setIH(org.apache.bcel.generic.InstructionHandle ihandle)
{
ih=ihandle;
}

/**
     Returns the instruction handle associated with this node in the expression tree
      If a node is outside the basic block it is set to null
*/

public org.apache.bcel.generic.InstructionHandle getIH()
{
	return ih;

}

void setGraph(sandmark.util.newgraph.MutableGraph  grh)
{
gr=grh;
}

/**
    Returns the graph associated with this node in the expression tree

*/

public sandmark.util.newgraph.MutableGraph  getGraph()
{
return gr;
}


void setContext(sandmark.analysis.stacksimulator.Context context)
{
cn=context;
}

/**
     Returns the context provided by stack simulator for the instruction
     associated with this node in the expression tree

*/

public sandmark.analysis.stacksimulator.Context getContext()
{
	return cn;

}


void setOutsideBlock()
{
outsideBlock=true;
}

/**
     Returns whether this node is associated with an instruction outside the basic block

*/


public boolean isOutsideBlock()
{
	return	outsideBlock;
}

public boolean isMarked()
{
	return	mark;
}

public void setMark()
{
	mark=true;
}

public void clearMark()
{
	mark=false;
}


}

