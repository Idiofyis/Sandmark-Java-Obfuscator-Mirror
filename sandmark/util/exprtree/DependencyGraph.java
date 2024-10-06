package sandmark.util.exprtree;

public class DependencyGraph
implements org.apache.bcel.Constants{

public org.apache.bcel.generic.ConstantPoolGen myCpg;
public sandmark.analysis.controlflowgraph.MethodCFG cfg;
sandmark.util.exprtree.ExprTree et;

public org.apache.bcel.generic.InstructionList il;
public org.apache.bcel.generic.InstructionHandle[] ih;
org.apache.bcel.generic.CodeExceptionGen[] exceptions;
org.apache.bcel.generic.InstructionHandle ihandle;
java.util.HashMap BToG;
java.util.HashMap NToG;
java.util.HashMap GToN;

public DependencyGraph(sandmark.program.Method method,
					org.apache.bcel.generic.ConstantPoolGen c,
						sandmark.analysis.controlflowgraph.MethodCFG mycfg)
{
	myCpg=c;
	exceptions=method.getExceptionHandlers();

	il=method.getInstructionList();
	if(il==null)
	 return;
	cfg=mycfg;

	et=new sandmark.util.exprtree.ExprTree(method,myCpg,cfg);

	BToG=new java.util.HashMap(); //dependency Graph for each block
	NToG=new java.util.HashMap();
	GToN=new java.util.HashMap();

	//System.out.println(et);
	if(validity()==false)
	{
	//System.out.println(et);
	System.out.println("Validation Fail");

	System.exit(0);
	return;
	}

	doMethod(method);

	//createlist();
}

	public String getShortName()
    {
	return "DependencyGraph";
    }

   	public sandmark.config.ModificationProperty [] getMutations() {
	      return null;
    }

    public String getLongName() {
	return "DependencyGraph";
    }


    public sandmark.util.ConfigProperties getConfigProperties()
    {
	return null;
    }

    public java.lang.String getAlgHTML()
    {
	return
	    "<HTML><BODY>" +
	    "InstructionOrdering is a class obfuscator.\n" +
	    "<TABLE>" +
	    "<TR><TD>" +
	    "Author: <a href =\"mailto:kamlesh@cs.arizona.edu\">Kamlesh Kantilal</a>\n" +
	    "</TD></TR>" +
	    "</TABLE>" +
	    "</BODY></HTML>";
    }

    public java.lang.String getAlgURL(){
	return "sandmark/util/exprtree/doc/help.html";
    }

    public java.lang.String getAuthor(){
			return "Kamlesh Kantilal";
	}

	public java.lang.String getAuthorEmail(){
			return "kamlesh@cs.arizona.edu";
	}

	public java.lang.String getDescription(){
			return "This algorithm creates dependency graph between expression trees";

	}


////////////////////////////////////////////////////////////////////////////


public sandmark.util.newgraph.MutableGraph  getDependencyGraph(sandmark.analysis.controlflowgraph.BasicBlock tempblock)
{
	return (sandmark.util.newgraph.MutableGraph)BToG.get(tempblock);

}

public sandmark.util.newgraph.MutableGraph getExpressionTree(sandmark.util.exprtree.Node tempgn)
{
	return (sandmark.util.newgraph.MutableGraph)NToG.get(tempgn);

}

public sandmark.util.exprtree.ExprTree  getExpressionTree()
{
	return et;
}

class BlockComparator implements java.util.Comparator{


	 public int compare(Object o1, Object o2)
	 {
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


}



public void doMethod(sandmark.program.Method method)
{
		java.util.ArrayList templist;
		java.util.ArrayList sortlist=new java.util.ArrayList();
		sandmark.util.newgraph.MutableGraph myGr;
		sandmark.util.exprtree.Node lastGn;
		sandmark.analysis.controlflowgraph.BasicBlock curr;
		java.util.ArrayList Grlist;
		java.util.ArrayList i1;
		java.util.ArrayList i2;


		//java.util.ArrayList reorderlist;
		sandmark.util.newgraph.MutableGraph reorderGraph;
		java.util.ArrayList parse;
		java.util.ArrayList concat;
		org.apache.bcel.generic.InstructionList myInstList=
                    new org.apache.bcel.generic.InstructionList();
		org.apache.bcel.generic.InstructionList myList;
		org.apache.bcel.generic.InstructionHandle myIh[];
		org.apache.bcel.generic.InstructionHandle myIh2[];
		org.apache.bcel.generic.InstructionHandle lastih=null;
		boolean flag;
		String S;
		int ct=0;
		int temp;

		il=method.getInstructionList();
		myIh2=il.getInstructionHandles();
		/*for(int j=0; j<myIh2.length; j++)
		System.out.println("writingin in="+myIh2[j]);*/
		java.util.Iterator nodeIter = cfg.nodes();
		while (nodeIter.hasNext()) {
			sortlist.add(nodeIter.next());
		}
		java.util.Collections.sort(sortlist,new BlockComparator());


	    nodeIter = sortlist.iterator();


		int j = 0;
		while (nodeIter.hasNext()) {
		   curr = (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next();
		  // System.out.println(et.toString(curr));

		   Grlist=et.blockToGrlist(curr);
		   reorderGraph = new sandmark.util.newgraph.MutableGraph();
		   BToG.put(curr,reorderGraph);

		   lastGn=null;

		   for(int k=0;k<Grlist.size();k++) {
		      myGr=(sandmark.util.newgraph.MutableGraph)Grlist.get(k);
		      templist=et.getInstList(myGr);
		      if(k==0)
			 lastih=((org.apache.bcel.generic.InstructionHandle)templist.get(0)).getPrev();
		      if(k!=Grlist.size()-1)
			 lastGn=adddependence(reorderGraph,myGr,lastGn,NToG,GToN,false,j);
		      else
			 lastGn=adddependence(reorderGraph,myGr,lastGn,NToG,GToN,true,j);

		   }

		   j++;

		}
		il.setPositions();
		il.update();
		myList=il;
		method.setInstructionList(il);
		myList=method.getInstructionList();
		myIh=il.getInstructionHandles();


}

/*


public void writeback
	(sandmark.analysis.controlflowgraph.MethodCFG reorderGraph,
	 java.util.HashMap NToG,
	 org.apache.bcel.generic.InstructionHandle lastih)
{
	java.util.ArrayList parse;
	java.util.ArrayList ilist;
	sandmark.analysis.controlflowgraph.MethodCFG myGr=null;
	sandmark.analysis.controlflowgraph.BasicBlock myGn=null;
	sandmark.analysis.controlflowgraph.MethodCFG tempGr;
	org.apache.bcel.generic.InstructionHandle ih1;
	org.apache.bcel.generic.InstructionHandle ih2;
	org.apache.bcel.generic.InstructionList myList;
	org.apache.bcel.generic.InstructionHandle myIh[];
	int j;
	java.util.HashMap NToM=new java.util.HashMap();

	for(int i=0;i<reorderGraph.nodes().size();i++)
	{
			NToM.put(reorderGraph.nodes().get(i),new Boolean(false));

	}

	while(true)
	{


	parse=getTopological(reorderGraph,NToM);
	if(parse.size()==0) break;

	for(int i=0;i<parse.size();i++)
	{	myGn=(sandmark.analysis.controlflowgraph.BasicBlock)parse.get(i);
		myGr=(sandmark.analysis.controlflowgraph.MethodCFG) NToG.get(parse.get(i));
		ilist=et.getInstList(myGr);
		ih1=(org.apache.bcel.generic.InstructionHandle)ilist.get(0);
		for(j=0;j<parse.size();j++)
		{
			tempGr=(sandmark.analysis.controlflowgraph.MethodCFG) NToG.get(parse.get(j));
			ilist=et.getInstList(tempGr);
			ih2=(org.apache.bcel.generic.InstructionHandle)ilist.get(0);
			if(ih1.getPosition()<ih2.getPosition())
				break;
		}
		if(j==parse.size())
			break;

   	}
			NToM.put(myGn,new Boolean(true));


		//same
			myIh=il.getInstructionHandles();
			ilist=et.getInstList(myGr);
			ih1=(org.apache.bcel.generic.InstructionHandle)ilist.get(0);
			ih2=(org.apache.bcel.generic.InstructionHandle)ilist.get(ilist.size()-1);

			//System.out.println("lastih "+lastih);
			if(lastih==null)
			{
			if(ih1==myIh[0])
			{	lastih=ih2;
				continue;
			}
			}

			if(lastih!=null && lastih.getNext()==ih1)
			{	lastih=ih2;
				continue;
			}

			if(lastih!=null)
			{il.redirectBranches(lastih.getNext(),ih1) ;
		 	}
			else
			{il.redirectBranches(myIh[0],ih1);
			}

			il.move(ih1,ih2,lastih);
			lastih=ih2;
			il.setPositions(true);

			il.update();
	}




}


public java.util.ArrayList getTopological(
	sandmark.analysis.controlflowgraph.MethodCFG reorderGraph,
	java.util.HashMap NToM)
{	int j;
	java.util.ArrayList retlist=
		 new java.util.ArrayList();
	java.util.List succlist;
	sandmark.analysis.controlflowgraph.BasicBlock myGn;

	for(int i=0;i<reorderGraph.nodes().size();i++)
	{
		myGn=(sandmark.analysis.controlflowgraph.BasicBlock)reorderGraph.nodes().get(i);
		if( ((Boolean)NToM.get(myGn)).booleanValue()==true)
				continue;

		succlist=myGn.getSuccessors();
		for(j=0;j<succlist.size();j++)
			if( ((Boolean)NToM.get(succlist.get(j))).booleanValue()==false)
				break;
		if(j==succlist.size())
			retlist.add(myGn);

	}
	return retlist;

}

*/

public boolean issubnull(sandmark.util.newgraph.MutableGraph  myGr)
{
sandmark.util.exprtree.NodeInfo ni;
java.util.Iterator nodeIter = myGr.nodes();
while (nodeIter.hasNext()) {
   sandmark.util.exprtree.Node mygn =
      (sandmark.util.exprtree.Node)nodeIter.next();
   ni=et.nodeToInfo(mygn);

   if(ni.getIH()==null)
      return true;

}


return false;
}



public sandmark.util.exprtree.Node
		adddependence(sandmark.util.newgraph.MutableGraph reorderGraph,
						sandmark.util.newgraph.MutableGraph  myGr,
						sandmark.util.exprtree.Node lastGn,
						java.util.HashMap NToG,
						java.util.HashMap GToN,boolean last,int debug)
{	int k;
	java.util.ArrayList templist;
	org.apache.bcel.generic.Instruction currinst;
	org.apache.bcel.generic.InstructionHandle currih;
	boolean flag;
	sandmark.util.exprtree.Node  myGn =
	   new sandmark.util.exprtree.Node();
	sandmark.util.exprtree.Node tempGn;
	sandmark.util.newgraph.MutableGraph tempGr;
	NToG.put(myGn,myGr);
	GToN.put(myGr,myGn);

	reorderGraph.addNode(myGn);
	myGn.setGraph(reorderGraph);
	templist=et.getInstList(myGr);
	currih=(org.apache.bcel.generic.InstructionHandle)templist.get(templist.size()-1);

	currinst=currih.getInstruction();

	if(last)
	{
	   java.util.Iterator nodeIter = reorderGraph.nodes();
	   while (nodeIter.hasNext()) {
	      tempGn =
		 (sandmark.util.exprtree.Node)nodeIter.next();
	      if(tempGn==myGn)
		 continue;

	      reorderGraph.addEdge(myGn,tempGn);
	   }
		//lastGn=myGn;
	   return lastGn;
	}

	flag=true;
	if(currinst instanceof org.apache.bcel.generic.DSTORE ||
									   currinst instanceof org.apache.bcel.generic.FSTORE ||
									   currinst instanceof org.apache.bcel.generic.ISTORE ||
									   currinst instanceof org.apache.bcel.generic.LSTORE ||
									   currinst instanceof org.apache.bcel.generic.IINC )
									   flag=false;

	if(lastGn!=null)
	reorderGraph.addEdge(myGn,lastGn);

     //newly added
	if((et.getRootNodes(myGr).size()>1) || issubnull(myGr) || callfunc(myGr))
		flag=true;


	if(flag )
	{
				lastGn=myGn;
 	}

	java.util.Iterator nodeIter = reorderGraph.nodes();
	while (nodeIter.hasNext()) {
	   tempGn=(sandmark.util.exprtree.Node)nodeIter.next();
	   tempGr=(sandmark.util.newgraph.MutableGraph)NToG.get(tempGn);
	   if(tempGn==myGn)
	      continue;


	   if(reorderGraph.hasEdge(tempGn,myGn))
	      continue;

	   java.util.Iterator rootIter = myGr.roots();
	   int count = 0;
	   while (rootIter.hasNext()) {
	      rootIter.next();
	      count++;
	   }

	   if(dependence(tempGr,myGr)|| (flag && refoutside(tempGr)) || (count>1)) {
	      reorderGraph.addEdge(myGn,tempGn);
	   }
	}

	return lastGn;
}


public boolean callfunc(sandmark.util.newgraph.MutableGraph tempGr)
{
java.util.ArrayList ilist;
org.apache.bcel.generic.InstructionHandle ih1;
org.apache.bcel.generic.Instruction in1;

ilist=et.getInstList(tempGr);
for( int k=0; k< ilist.size();k++)
{
  ih1=(org.apache.bcel.generic.InstructionHandle) ilist.get(k);
  in1=ih1.getInstruction();

  if(in1 instanceof org.apache.bcel.generic.InvokeInstruction)
  	return true;

}
return false;
}


public boolean refoutside(sandmark.util.newgraph.MutableGraph tempGr)
{
java.util.ArrayList ilist;
org.apache.bcel.generic.InstructionHandle ih1;
org.apache.bcel.generic.Instruction in1;

ilist=et.getInstList(tempGr);
for( int k=0; k< ilist.size();k++)
{
  ih1=(org.apache.bcel.generic.InstructionHandle) ilist.get(k);
  in1=ih1.getInstruction();

  if(in1 instanceof org.apache.bcel.generic.ArrayInstruction)
  	return true;

 if(in1 instanceof org.apache.bcel.generic.ARRAYLENGTH)
   	return true;

if(in1 instanceof org.apache.bcel.generic.ATHROW)
   	return true;


 if(in1 instanceof org.apache.bcel.generic.CPInstruction)
 if(!(in1 instanceof org.apache.bcel.generic.LDC) && !(in1 instanceof org.apache.bcel.generic.LDC2_W))
  return true;

if(in1 instanceof org.apache.bcel.generic.MONITORENTER)
   	return true;

if(in1 instanceof org.apache.bcel.generic.MONITOREXIT)
   	return true;

if(in1 instanceof org.apache.bcel.generic.NEWARRAY)
   	return true;

if(in1 instanceof org.apache.bcel.generic.StackInstruction)
   	return true;

if(in1 instanceof org.apache.bcel.generic.ASTORE)
   	return true;

if(in1 instanceof org.apache.bcel.generic.ALOAD)
   	return true;

}
return false;
}


public boolean dependence(sandmark.util.newgraph.MutableGraph tempGr,
								sandmark.util.newgraph.MutableGraph myGr)
{

	java.util.ArrayList ilist;
	java.util.ArrayList templist;
	java.util.ArrayList lvtemplist=new java.util.ArrayList();
	org.apache.bcel.generic.InstructionHandle ih1;
	org.apache.bcel.generic.Instruction in1;
	org.apache.bcel.generic.InstructionHandle ih2;
	org.apache.bcel.generic.Instruction  in2;


		ilist=et.getInstList(tempGr);
		templist=et.getInstList(myGr);


	//optimization new code
	 	for(int m=0;m<templist.size();m++)
		{
			ih2=(org.apache.bcel.generic.InstructionHandle)templist.get(m);
			in2=ih2.getInstruction();

			if(!(in2 instanceof org.apache.bcel.generic.LocalVariableInstruction))
			continue;

			lvtemplist.add(ih2);
	    }
	 //end optimization  new code

		for( int k=0; k< ilist.size();k++)
		{
			ih1=(org.apache.bcel.generic.InstructionHandle) ilist.get(k);
			in1=ih1.getInstruction();
			if(!(in1 instanceof org.apache.bcel.generic.LocalVariableInstruction))
				continue;
			for(int m=0;m<lvtemplist.size();m++) //ch
			{
				ih2=(org.apache.bcel.generic.InstructionHandle)lvtemplist.get(m); //ch
				in2=ih2.getInstruction();

				if(!(in2 instanceof org.apache.bcel.generic.LocalVariableInstruction))
				continue;


				//check if needed ?
				/*if(in1 instanceof org.apache.bcel.generic.LSTORE ||
				   in1 instanceof org.apache.bcel.generic.DSTORE ||
				   in2 instanceof org.apache.bcel.generic.LSTORE ||
				   in2 instanceof org.apache.bcel.generic.DSTORE ||
				   in1 instanceof org.apache.bcel.generic.LLOAD ||
				   in1 instanceof org.apache.bcel.generic.DLOAD ||
				   in2 instanceof org.apache.bcel.generic.LLOAD ||
				   in2 instanceof org.apache.bcel.generic.DLOAD
				   )
				{
				System.out.println("args LONG");
				if( ((org.apache.bcel.generic.LocalVariableInstruction)in1).getIndex()
					!= ((org.apache.bcel.generic.LocalVariableInstruction)in2).getIndex())
				if( ((org.apache.bcel.generic.LocalVariableInstruction)in1).getIndex()+1
					!= ((org.apache.bcel.generic.LocalVariableInstruction)in2).getIndex())
				if( ((org.apache.bcel.generic.LocalVariableInstruction)in1).getIndex()-1
				 != ((org.apache.bcel.generic.LocalVariableInstruction)in2).getIndex())
				continue;

				}
				else */
				if( ((org.apache.bcel.generic.LocalVariableInstruction)in1).getIndex()
						!= ((org.apache.bcel.generic.LocalVariableInstruction)in2).getIndex())
					continue;


				if(in2 instanceof org.apache.bcel.generic.StoreInstruction
				   || in2 instanceof org.apache.bcel.generic.IINC	)
				{ //System.out.println("Dep="+in1+" & " + in2);
				return true;
				}

				if(in1 instanceof org.apache.bcel.generic.StoreInstruction
					|| in1 instanceof org.apache.bcel.generic.IINC)
				{//System.out.println("Dep="+in1+" & " + in2);
					return true;
				}

			}
		}

	return false;
}


boolean isequallist(java.util.ArrayList i1,java.util.ArrayList i2)
{	int i;
	if(i1.size()!=i2.size())
		return false;

	for(i=0;i<i1.size();i++)
		if(i1.get(i)!=i2.get(i))
				return false;

	return true;
}

public boolean validity()
{
		//String S="START of Method";
		sandmark.util.newgraph.MutableGraph myGr;
		sandmark.analysis.controlflowgraph.BasicBlock curr;
		java.util.ArrayList Grlist;
		java.util.ArrayList i1;
		java.util.ArrayList i2;
		java.util.ArrayList templist;
		java.util.Iterator nodeIter = cfg.nodes();
		while (nodeIter.hasNext()) {
		   curr = (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next();
		   i1=curr.getInstList();
		   i2=new java.util.ArrayList();
		   Grlist=et.blockToGrlist(curr);
		   for(int k=0;k<Grlist.size();k++) {
		      myGr=(sandmark.util.newgraph.MutableGraph)Grlist.get(k);
		      templist=et.getInstList(myGr);
		      i2.addAll(templist);
		   }

		  /* for(int l=0;l<i2.size();l++)
		     {
		     System.out.println(
		     "u="+(org.apache.bcel.generic.InstructionHandle)i1.get(l));

		     }
		     for(int l=0;l<i2.size();l++)
		     {
		     System.out.println("m="+(org.apache.bcel.generic.InstructionHandle)i2.get(l));

		     }*/


		   if(!isequallist(i1,i2))
		      return false;
		}

		return true;
}


}

