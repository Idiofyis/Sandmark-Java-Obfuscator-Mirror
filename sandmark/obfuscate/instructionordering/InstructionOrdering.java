package sandmark.obfuscate.instructionordering;




public class InstructionOrdering extends  sandmark.obfuscate.MethodObfuscator
			    implements org.apache.bcel.Constants
{
    public org.apache.bcel.generic.ConstantPoolGen myCpg;
    public sandmark.program.Method mg;
    public sandmark.analysis.controlflowgraph.MethodCFG cfg;
    sandmark.util.newexprtree.DependencyGraph dgr;
    sandmark.util.newexprtree.MethodExprTree met;

    public org.apache.bcel.generic.InstructionList il;
    public org.apache.bcel.generic.InstructionHandle[] ih;
    org.apache.bcel.generic.CodeExceptionGen[] exceptions;
    org.apache.bcel.generic.InstructionHandle ihandle;


    


    public String getShortName() {
	return "Reorder Instructions";
    }

    public sandmark.config.ModificationProperty [] getMutations() {
	return null;
    }

    public String getLongName() {
	return "Instruction Reordering";
    }

    public java.lang.String getAlgHTML() {
	return
	    "<HTML><BODY>" +
	    "InstructionOrdering is a class obfuscator." +
       " The algorithm reorders instructions within a basic block." +
	    "<TABLE>" +
	    "<TR><TD>" +
	    "Author: <a href =\"mailto:kamlesh@cs.arizona.edu\">Kamlesh Kantilal</a>\n" +
	    "</TD></TR>" +
	    "</TABLE>" +
	    "</BODY></HTML>";
    }

    public java.lang.String getAlgURL() {
	return "sandmark/obfuscate/instructionordering/doc/help.html";
    }

    public java.lang.String getAuthor() {
	return "Kamlesh Kantilal";
    }

    public java.lang.String getAuthorEmail(){
	return "kamlesh@cs.arizona.edu";
    }

    public java.lang.String getDescription() {
	return "This algorithm reorders instruction within a basic block.";
    }

    public void apply(sandmark.program.Method meth) throws Exception {
	mg = meth;
	doMethod();
	mg.setMaxStack();
	mg.setMaxLocals();
    }
    
    public void doMethod() {
	myCpg=mg.getConstantPool();
	exceptions=mg.getExceptionHandlers();

	il=mg.getInstructionList();
	if(il==null)
	    return;

	dgr=new sandmark.util.newexprtree.DependencyGraph(mg);
	met=dgr.getExpressionTree();
	cfg = met;
	reorderinstruction();
    }

    public void reorderinstruction() {
	sandmark.analysis.controlflowgraph.BasicBlock curr;
	sandmark.util.newgraph.MutableGraph reorderGraph;
	java.util.ArrayList templist=new java.util.ArrayList();
	org.apache.bcel.generic.InstructionHandle lastih=null;
	org.apache.bcel.generic.InstructionHandle tempih;

	java.util.Iterator nodeIter = cfg.nodes();
	while (nodeIter.hasNext())
	    templist.add(nodeIter.next());

	java.util.Collections.sort(templist,new BlockComparator());


	nodeIter = templist.iterator();
	while (nodeIter.hasNext()) {
	    curr = (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next();
	    tempih=curr.getIH();

	    reorderGraph=dgr.getDependencyGraph(curr);
	    if(tempih!=null)
	    lastih=writeback(reorderGraph,tempih.getPrev());
	}

    }

    public org.apache.bcel.generic.InstructionHandle writeback
	(sandmark.util.newgraph.MutableGraph reorderGraph,
	 org.apache.bcel.generic.InstructionHandle lastih)
    {
	java.util.ArrayList parse;
	java.util.ArrayList ilist;
	sandmark.util.newexprtree.ExprTree myGr=null;
	sandmark.util.newexprtree.Node  myGn=null;
	sandmark.util.newexprtree.ExprTree tempGr;
	org.apache.bcel.generic.InstructionHandle ih1;
	org.apache.bcel.generic.InstructionHandle ih2;
	org.apache.bcel.generic.InstructionList myList;
	org.apache.bcel.generic.InstructionHandle myIh[];
	
	int j;
	java.util.HashMap NToM=new java.util.HashMap();

	java.util.Iterator nodeIter = reorderGraph.nodes();
	while (nodeIter.hasNext())
	   NToM.put(nodeIter.next(), new Boolean(false));
	while(true)
	{
	    parse=getTopological(reorderGraph,NToM);
	    if(parse.size()==0) break;

	    for(int i=0;i<parse.size();i++)
	    {
		myGn=(sandmark.util.newexprtree.Node)parse.get(i);
		myGr= dgr.getExpressionTree(myGn);
		ilist=myGr.getInstructionList();
		ih1=(org.apache.bcel.generic.InstructionHandle)ilist.get(0);

		for(j=0;j<parse.size();j++)
		{
		    tempGr=dgr.getExpressionTree((sandmark.util.newexprtree.Node)parse.get(j));
		    ilist=tempGr.getInstructionList();
		    ih2=(org.apache.bcel.generic.InstructionHandle)ilist.get(0);
		    if(ih1.getPosition()<ih2.getPosition())
			break;
		}
		if(j==parse.size())
			break;
	    }
	
	    NToM.put(myGn,new Boolean(true));
	
	    myIh=il.getInstructionHandles();
	    ilist=myGr.getInstructionList();
	    ih1=(org.apache.bcel.generic.InstructionHandle)ilist.get(0);
	    ih2=(org.apache.bcel.generic.InstructionHandle)ilist.get(ilist.size()-1);

	    if(lastih==null) {
		if(ih1==myIh[0]) {
		    lastih=ih2;
		    continue;
		}
	    }

	    if(lastih!=null && lastih.getNext()==ih1) {
		lastih=ih2;
		continue;
	    }

	    if(lastih!=null)
		il.redirectBranches(lastih.getNext(),ih1) ;
	    else
		il.redirectBranches(myIh[0],ih1);
	    
	    il.move(ih1,ih2,lastih);
	    lastih=ih2;
	    il.setPositions(true);
	    il.update();
	}

	return lastih;
    }


    public java.util.ArrayList getTopological(
	    sandmark.util.newgraph.MutableGraph reorderGraph,
	    java.util.HashMap NToM)
    {	
	int j;
	boolean flag;
	java.util.ArrayList retlist=
		 new java.util.ArrayList();
	sandmark.util.newexprtree.Node  myGn;

	java.util.Iterator nodeIter = reorderGraph.nodes();
	while (nodeIter.hasNext())
	{
	    myGn=(sandmark.util.newexprtree.Node )nodeIter.next();
	    if( ((Boolean)NToM.get(myGn)).booleanValue()==true)
		continue;

	    java.util.Iterator succIter = reorderGraph.succs(myGn);
	    flag=true;
	    while (succIter.hasNext()) {   
		if( ((Boolean)NToM.get(succIter.next())).booleanValue()==false) {
		    flag=false ; break;
		}

   	    }
	    if(flag)
		retlist.add(myGn);
	}
	return retlist;
    }


class BlockComparator implements java.util.Comparator
{
    public int compare(Object o1, Object o2) {
	int pos1,pos2;
	sandmark.analysis.controlflowgraph.BasicBlock bb
	   	 	= (sandmark.analysis.controlflowgraph.BasicBlock)o1;
	if (bb.getIH() == null)
	    pos1=10000;
	else
	    pos1=bb.getIH().getPosition();

	bb= (sandmark.analysis.controlflowgraph.BasicBlock)o2;
	if (bb.getIH() == null)
	    pos2=10000;
	else
	    pos2=bb.getIH().getPosition();

  	return pos1-pos2;
    }
}   //class BlockComparator

}   //class InstructionOrdering
