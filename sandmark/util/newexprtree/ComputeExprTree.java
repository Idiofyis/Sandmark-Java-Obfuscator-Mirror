package sandmark.util.newexprtree;

/**
   ExprTree is a utility that creates expression tree for all blocks in the controlflowgraph.
   It creates expression tree by adding edges between the instruction that consumes elements
   from the stack and the instructions which produce those element on the stack.

   @author Kamlesh Kantilal (kamlesh@cs.arizona.edu)
*/

public class ComputeExprTree
    implements org.apache.bcel.Constants

{

	org.apache.bcel.generic.ConstantPoolGen myCpg;
	sandmark.analysis.controlflowgraph.MethodCFG cfg;


	org.apache.bcel.generic.InstructionList il;
	org.apache.bcel.generic.InstructionHandle[] ih;
	sandmark.analysis.controlflowgraph.BasicBlock curr;
	sandmark.analysis.controlflowgraph.BasicBlock block;
	java.util.HashMap IToN;
	java.util.HashMap NToI;
	java.util.HashMap NToInfo;
	java.util.HashMap BToG;
	java.util.HashMap BToL;
	sandmark.analysis.stacksimulator.StackSimulator st;
	org.apache.bcel.generic.InstructionHandle ihandle;
	sandmark.util.newexprtree.Node gn;
	sandmark.analysis.stacksimulator.Context cn;
	sandmark.util.newgraph.MutableGraph gr;
	java.util.ArrayList ilist;

	/**
       Constructs an expression tree for a method represented by some
       method control flow graph.
       @param m - the method to analyze
       @param cpg - the ConstantPoolGen created from the class that this method is in
       @param cfg - the controlflowgraph of that method

     */

	ComputeExprTree(sandmark.program.Method method,sandmark.analysis.controlflowgraph.MethodCFG mcfg)
	{
	myCpg=method.getConstantPool();
	//cfg=method.getCFG();
	cfg=mcfg;
	st=new sandmark.analysis.stacksimulator.StackSimulator(method);

	IToN=new java.util.HashMap();
	NToI=new java.util.HashMap();
	NToInfo=new java.util.HashMap();
	BToG=new java.util.HashMap();
	BToL=new java.util.HashMap();

	il=method.getInstructionList();
	if(il==null)
	 return;
	initialize();

	}

private void initialize()
{
	ih=il.getInstructionHandles();
	sandmark.util.newexprtree.Node  gn;
	NodeInfo	ni;
	java.util.ArrayList mygrlist;
	java.util.ArrayList newmygrlist;
	sandmark.util.newexprtree.Node mygn;
	sandmark.util.newgraph.MutableGraph mygraph;

    for(int i=0;i<ih.length;i++)
    {
	gn= new sandmark.util.newexprtree.Node();
	ni= new NodeInfo();
	ni.setIH(ih[i]);
	IToN.put(ih[i],gn);
	NToI.put(gn,ih[i]);
	NToInfo.put(gn,ni);
	}

    java.util.Iterator i = cfg.nodes();
    while (i.hasNext()) {
       curr =
	  (sandmark.analysis.controlflowgraph.BasicBlock) i.next();
       doBlock();


       // converts a single forest into multiple graph
       //Fills in BToL
       newmygrlist=new java.util.ArrayList();

       generateGraph(newmygrlist);

       BToL.put(curr,newmygrlist);
       java.util.Collections.sort(newmygrlist,new GraphComparator(NToInfo));
    }
}


private void add(int i)
{		org.apache.bcel.generic.InstructionHandle tempih;
		sandmark.analysis.stacksimulator.StackData sd[];
		java.util.ArrayList grlist;
		sandmark.util.newexprtree.Node dummynode;
		NodeInfo dummyInfo;

		boolean flaglist;
		sandmark.analysis.controlflowgraph.MethodCFG myGr=null;
		sandmark.analysis.controlflowgraph.MethodCFG tempGr;
		sandmark.util.newexprtree.Node myGn;
		org.apache.bcel.generic.Instruction inst;

		for(int j=i-1;j>=0;j--)
		{				//System.out.println("ihandle===="+ihandle+"kk"+j);
				         sd=cn.getStackAt(j);
					         tempih=sd[0].getInstruction();
					         if(ilist.contains(tempih)&& tempih!=ihandle)
					         {
								gr.addEdge(gn,IToN.get(tempih));
						 	 }
						 	 else
						 	 {
						 	 	dummynode=new sandmark.util.newexprtree.Node();
						 	 	dummyInfo=new NodeInfo();
						 	 	dummyInfo.setOutsideBlock();
						 	 	dummyInfo.setIH(null);
						 	 	NToI.put(dummynode,null);
						 	 	gr.addNode(dummynode);
						 	 	dummynode.setGraph(gr);
								gr.addEdge(gn, dummynode);
						 	 	NToInfo.put(dummynode,dummyInfo);

						 	 }

		}

}


private void doBlock()
{

	org.apache.bcel.generic.Instruction inst;
	org.apache.bcel.generic.InvokeInstruction invoke;

	NodeInfo ni;
	int args;

	sandmark.util.newexprtree.Node tempn;


    int opcode;

	//if(block.getIH()==null) return;

	ilist=curr.getInstList();

	gr = new sandmark.util.newgraph.MutableGraph();
	BToG.put(curr,gr);
	BToL.put(curr,new java.util.ArrayList());


	if(ilist!=null)
	for(int parse=0;parse<ilist.size();parse++)
	{
		ihandle=(org.apache.bcel.generic.InstructionHandle)ilist.get(parse);
		cn=st.getInstructionContext(ihandle);
		gn=(sandmark.util.newexprtree.Node )IToN.get(ihandle);
		ni=(NodeInfo)NToInfo.get(gn);
		ni.setContext(cn);
		gr.addNode(gn);
		gn.setGraph(gr);

		inst=ihandle.getInstruction();
		opcode = inst.getOpcode();

		        if(inst instanceof org.apache.bcel.generic.ACONST_NULL){

		        }
		        else if(inst instanceof org.apache.bcel.generic.ArithmeticInstruction){
		            doArithmetic(ihandle);
		        }
		        else if(inst instanceof org.apache.bcel.generic.ArrayInstruction){
		            doArray(ihandle);
		        }
		        else if(inst instanceof org.apache.bcel.generic.ARRAYLENGTH){
		        	add(1);
		        }
		        else if(inst instanceof org.apache.bcel.generic.ATHROW){
		         //   context.pop();
		         add(1);
		        }
		        else if(inst instanceof org.apache.bcel.generic.BIPUSH){

		        }
		        else if(inst instanceof org.apache.bcel.generic.BranchInstruction){
		            switch(opcode){
		            case TABLESWITCH:
		            case LOOKUPSWITCH:
		            //    context.pop();
						add(1);
		                break;
		            case GOTO:
		            case GOTO_W:
		                //do nothing
		                break;
		            case IF_ACMPEQ:
		            case IF_ACMPNE:
		            case IF_ICMPEQ:
		            case IF_ICMPGE:
		            case IF_ICMPGT:
		            case IF_ICMPLE:
		            case IF_ICMPLT:
		            case IF_ICMPNE:
	   						add(2);
	   					break;
		            case IFEQ:
		            case IFGE:
		            case IFGT:
		            case IFLE:
		            case IFLT:
		            case IFNE:
		            case IFNONNULL:
		            case IFNULL:
		               //context.pop();
		        			add(1);
		                break;
		            case JSR:
		            case JSR_W:
		                break;
		            }
		        }
		        else if(inst instanceof org.apache.bcel.generic.ConversionInstruction){
		            add(1);
		        }
		        else if(inst instanceof org.apache.bcel.generic.CPInstruction){
		            switch(opcode){
		            case INSTANCEOF:
		                //context.pop(); //remove object ref
		        			add(1);
		                break;
		            case CHECKCAST:
		            		add(1);
		                break;
		            case ANEWARRAY:
		            case MULTIANEWARRAY:
		                int dimension = opcode==ANEWARRAY?1:
		                    ((org.apache.bcel.generic.MULTIANEWARRAY)inst).getDimensions();
		                add(dimension);

		                break;
		            case NEW:
		                break;
		            case LDC:
		            case LDC2_W:
		            case LDC_W:
		                break;
		            case PUTFIELD:
							add(2);

		               //no break here on purpose!
		            	break;
		            case PUTSTATIC:
		               // context.pop(); //value for put
		        			add(1);
		               	break;
		            case GETFIELD:
		      				add(1);
		                //context.pop(); //object ref
		                //no break here on purpose!
		            	break;
		            case GETSTATIC:
		                break;
		            case INVOKEINTERFACE:
		            case INVOKESPECIAL:
		            case INVOKEVIRTUAL:
		                //context.pop(); //extra for object ref
		                //no break here on purpose
		                invoke =
						(org.apache.bcel.generic.InvokeInstruction)inst;
						args = invoke.getArgumentTypes(myCpg).length;
		                add(args+1);

		                break;
		            case INVOKESTATIC:
		                invoke =
		                    (org.apache.bcel.generic.InvokeInstruction)inst;
		                args = invoke.getArgumentTypes(myCpg).length;
		                //pop the arguments
		                add(args);

		                break;
		            default:
		                throw new IllegalArgumentException
		                    ("Did not implement instruction code: " + opcode);
		            }

		        }
		        else if(inst instanceof org.apache.bcel.generic.DCMPG){
					add(2);
		        }
		        else if(inst instanceof org.apache.bcel.generic.DCMPL){
					add(2);
		        }
		        else if(inst instanceof org.apache.bcel.generic.DCONST){

		        }
		        else if(inst instanceof org.apache.bcel.generic.FCMPG){
					add(2);
		        }
		        else if(inst instanceof org.apache.bcel.generic.FCMPL){
		    		add(2);
		        }
		        else if(inst instanceof org.apache.bcel.generic.FCONST){

		        }
		        else if(inst instanceof org.apache.bcel.generic.ICONST){

		        }
		        else if(inst instanceof org.apache.bcel.generic.LCMP){
		            add(2);
		        }
		        else if(inst instanceof org.apache.bcel.generic.LCONST){

		        }
		        else if(inst instanceof org.apache.bcel.generic.LocalVariableInstruction){
		            if(inst instanceof org.apache.bcel.generic.LoadInstruction){
		        	}
		        	else if(inst instanceof org.apache.bcel.generic.StoreInstruction){

					add(1);
					}
		        	else if(inst instanceof org.apache.bcel.generic.IINC){
		            }
		            else throw new IllegalArgumentException("Invalid local variable " +
		                                                    "instruction: " + inst);
		        }
		        else if(inst instanceof org.apache.bcel.generic.MONITORENTER){
		        	add(1);
		        }
		        else if(inst instanceof org.apache.bcel.generic.MONITOREXIT){
		        	add(1);
		        }
		        else if(inst instanceof org.apache.bcel.generic.NEWARRAY){
		            add(1);
		        }
		        else if(inst instanceof org.apache.bcel.generic.NOP){
		            /*DO NOTHING*/
		        }
		        else if(inst instanceof org.apache.bcel.generic.RET){
		            /*DO NOTHING*/
		        }
		        else if(inst instanceof org.apache.bcel.generic.ReturnInstruction){
		            if(!(inst instanceof org.apache.bcel.generic.RETURN))
		                add(1);
		        }
		        else if(inst instanceof org.apache.bcel.generic.SIPUSH){

		        }
		        else if(inst instanceof org.apache.bcel.generic.StackInstruction){
		            doStack(ihandle);
		        }
		        else{
		            throw new IllegalArgumentException(inst.getClass().getName() +
		                                               "hmm is not a valid instruction.");
        		}


	}// END of FOR
}


private void doStack(org.apache.bcel.generic.InstructionHandle instH)
    {
        org.apache.bcel.generic.Instruction inst = instH.getInstruction();
        int opcode = inst.getOpcode();
		org.apache.bcel.generic.InstructionHandle tempih;	//new code
	   	sandmark.analysis.stacksimulator.StackData sd[];	//new code

        sandmark.analysis.stacksimulator.StackData[] data;
        switch(opcode){
        case DUP:

            add(1);
            break;
        case DUP_X1:

            add(1);
            break;

        case DUP_X2:
            add(1);

            break;

        case DUP2:
            data = cn.getStackAt(0);
            if(data[0].getSize() > 1){
                add(1);
            }
            else{
                add(2);

            }
            break;

        case DUP2_X1:
            data = cn.getStackAt(0);
            if(data[0].getSize() > 1){
            	add(1);
            }
            else{
				add(2);
			}

            break;
        case DUP2_X2:
    		data = cn.getStackAt(0);
            if(data[0].getSize() > 1){
            	add(1);
            }
            else{
				add(2);
			}

            break;

        case POP:
            	add(1);
            break;
        case POP2:


		   			         sd=cn.getStackAt(0);
		   				     if(sd[0].getSize() == 1)
	       						add(2);
	       					 else
	       					 	add(1);

            break;
        case SWAP:
          add(2);
            break;

        default:
            throw new IllegalArgumentException(inst.getClass().getName() +
                                               " is not a valid instruction.");
        }
    }

 private void doArithmetic(org.apache.bcel.generic.InstructionHandle instH)
 {
        org.apache.bcel.generic.Instruction inst = instH.getInstruction();
        int opcode = inst.getOpcode();

       /* if(((org.apache.bcel.generic.StackConsumer)inst).consumeStack
           (myCpg) == 1){
            add(1);
        }
        else{
            add(2);

        }
        */
        //added newly

        if(opcode == DNEG || opcode == FNEG || opcode == INEG || opcode == LNEG)
        		add(1);
		else
			 add(2);

 }

private void doArray(org.apache.bcel.generic.InstructionHandle ih
                         )
    {
        int opcode = ih.getInstruction().getOpcode();
        switch(opcode){
        case AALOAD:
            	add(2);
            break;
        case BALOAD:
        case CALOAD:
        case IALOAD:
        case SALOAD:
            	add(2);
            break;
        case DALOAD:
            	add(2);
            break;
        case FALOAD:
            	add(2);
            break;
        case LALOAD:
            	add(2);
            break;
        case AASTORE:
        case BASTORE:
        case CASTORE:
        case DASTORE:
        case FASTORE:
        case IASTORE:
        case LASTORE:
        case SASTORE:
            	add(3);
            break;
        default:
            throw new RuntimeException("forgot to simulate opcode: " + opcode);
        }

    }

public void display()
{
  java.util.ArrayList ilist;
  sandmark.analysis.controlflowgraph.MethodCFG dg;
  java.util.ArrayList newlist;
  org.apache.bcel.generic.InstructionHandle ihandle;
  java.util.Iterator nodeIter = cfg.nodes();
  while (nodeIter.hasNext()) {
     System.out.println("START BLOCK");
     block =
	(sandmark.analysis.controlflowgraph.BasicBlock) nodeIter.next();
     System.out.println(block);
     //java.util.List succ = block.getSuccessors();
     ilist=block.getInstList();
     for(int i=0;i<ilist.size();i++) {
	ihandle=(org.apache.bcel.generic.InstructionHandle)ilist.get(i);
	System.out.println("Inst =" + ihandle);
     }
     System.out.println("END BLOCK");
  }
}



/*
public String toString()
{
	String S="START of Method";
	sandmark.analysis.controlflowgraph.MethodCFG myGr;
	java.util.ArrayList Grlist;

	for(int j=0; j<cfg.nodes().size(); j++)
	{	Grlist=(java.util.ArrayList) BToL.get(cfg.nodes().get(j));
		for(int k=0;k<Grlist.size();k++)
		{
		myGr=(sandmark.analysis.controlflowgraph.MethodCFG)Grlist.get(k);
		for(int i=0;i<getRootNodes(myGr).size();i++)
			{
					S+=doPrintPostOrder((sandmark.util.newexprtree.Node)
														getRootNodes(myGr).get(i),0,2);

			}
		S+="\nEnd Tree\n";
		}
		S+="---------End of BLk---------\n";
	}
	return S;
}
*/

/**
       Returns the String representation of the expression tree of all the blocks in the CFG.

*/

public String toString()
{
	//display();
	String S="START of Method\n";
	java.util.Iterator nodeIter = cfg.nodes();
	while (nodeIter.hasNext()) {
	   S+="---------Start of BLk---------\n";
	   S+=toString((sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next());
	   S+="---------End of BLk---------\n";
	}
	return S;
}



private String doPrintPostOrder(sandmark.util.newexprtree.Node myGn
										,int level,int up)
{	String S="";
        if(myGn.graph().numSuccs(myGn)>=2) {
	   java.util.Iterator succIter = myGn.graph().succs(myGn);
	   succIter.next();
	   while (succIter.hasNext()) {
	      sandmark.util.newexprtree.Node  bb =
		 (sandmark.util.newexprtree.Node )succIter.next();
	      S += doPrintPostOrder(bb, level+1, 1);
	   }
	}
	for(int i=0;i<level;i++)
	S+=("     ");
	if(up==1)
	S+=("v~~");
	else if(up==0)
	S+=("^~~");

	S+=(org.apache.bcel.generic.InstructionHandle)NToI.get(myGn);
	S+="\n";
	if(myGn.graph().numSuccs(myGn)>=1) {
	   java.util.Iterator succIter = myGn.graph().succs(myGn);
	   sandmark.util.newexprtree.Node succ =
	      (sandmark.util.newexprtree.Node)succIter.next();
	   S+=doPrintPostOrder(succ, level+1, 0);
	}
	return S;
}

/**
       Returns the String representation of a particular expression tree.
       @param myGr a graph representing the expression tree
*/


public String toString(sandmark.util.newgraph.MutableGraph myGr)
{
	String S="";
	for(int i=0;i<getRootNodes(myGr).size();i++)
	{
			S+=doPrintPostOrder((sandmark.util.newexprtree.Node)
											getRootNodes(myGr).get(i),0,2);

	}
	return S;

}

/**
       Returns the String representation of all expression trees within a block.
       @param bblock a basic block whose representation is needed
*/


public String toString(sandmark.analysis.controlflowgraph.BasicBlock bblock)
{
	String S="";
	java.util.ArrayList Grlist;
	sandmark.util.newgraph.MutableGraph myGr;
	Grlist=(java.util.ArrayList) BToL.get(bblock);
	for(int k=0;k<Grlist.size();k++)
	{
		myGr=(sandmark.util.newgraph.MutableGraph)Grlist.get(k);
		S+=toString(myGr);
		S+="\nEnd Tree\n";
	}
	return S;

}



/**
       Returns the instruction handle list(list of "org.apache.bcel.generic.InstructionHandle"  )
       associated with a particular expression tree.
       @param dg a graph representing the expression tree
*/


public java.util.ArrayList getInstList(sandmark.util.newgraph.MutableGraph dg)
{
	java.util.ArrayList newlist=new java.util.ArrayList();


	java.util.Iterator nodeIter = dg.nodes();
	while (nodeIter.hasNext()) {
	   java.lang.Object node = nodeIter.next();
	   org.apache.bcel.generic.InstructionHandle x =
	      (org.apache.bcel.generic.InstructionHandle)NToI.get(node);
	   if (x == null)
	      continue;
	   java.util.Iterator nodeIter2 = dg.nodes();
	   while (nodeIter2.hasNext()) {
	      java.lang.Object node2 = nodeIter2.next();
	      org.apache.bcel.generic.InstructionHandle y =
		 (org.apache.bcel.generic.InstructionHandle)NToI.get(node2);
	      if (y == null)
		 continue;
	      if ((y.getPosition()<x.getPosition() && (!newlist.contains(y))) || newlist.contains(x))
		 x=y;
	   }
	   newlist.add(x);
	}

	return newlist;
}


java.util.ArrayList	getRootNodes(sandmark.util.newgraph.MutableGraph dg)
{
	java.util.ArrayList newList =new java.util.ArrayList();
	java.util.Iterator nodeIter = dg.nodes();
	while (nodeIter.hasNext()) {
	   sandmark.util.newexprtree.Node bb =
	      (sandmark.util.newexprtree.Node)nodeIter.next();
	   if (dg.numPreds(bb) == 0)
	      newList.add(bb);
	}

	return newList;
}

class NodeComparator implements java.util.Comparator{
	java.util.HashMap NToInfo;

	NodeComparator(java.util.HashMap tt)
	{
		NToInfo=tt;
    }

	 public int compare(Object o1, Object o2)
	 {
	   	 int pos1,pos2;
	   	 NodeInfo tempni = (NodeInfo)NToInfo.get(o1);
	     if (tempni.getIH() == null)
		 		     pos1=10000;
		 else
		 	pos1=tempni.getIH().getPosition();

		 tempni = (NodeInfo)NToInfo.get(o2);
			     if (tempni.getIH() == null)
				 		     pos2=10000;
				 else
				 	pos2=tempni.getIH().getPosition();

  		  return pos1-pos2;
  	 }


}

class GraphComparator implements java.util.Comparator{
	NodeComparator nc;
	java.util.HashMap NToInfo;
	GraphComparator(java.util.HashMap tt)
	{nc=new NodeComparator(tt);
	 NToInfo=tt;
    }

	 public int compare(Object gr1, Object gr2)
	 {
	   	 java.lang.Object node1,node2;
	   	 NodeInfo tempni;
	   	 sandmark.util.newexprtree.Node tempnode=null;
	   	 sandmark.util.newgraph.MutableGraph myGr
	   	          =(sandmark.util.newgraph.MutableGraph)gr1;
		 java.util.Iterator nodeIter = myGr.nodes();

		 while (nodeIter.hasNext()) {
		 		  tempnode =
		 		      (sandmark.util.newexprtree.Node)nodeIter.next();
		 		tempni = (NodeInfo)NToInfo.get(tempnode);
		 		if (tempni.getIH() != null)
				 	break;
		 }
		 node1=tempnode;

		  myGr=(sandmark.util.newgraph.MutableGraph)gr2;
		  nodeIter = myGr.nodes();
		 	while (nodeIter.hasNext()) {
		 		 		  tempnode =
		 		 		      (sandmark.util.newexprtree.Node)nodeIter.next();
		 		 		tempni = (NodeInfo)NToInfo.get(tempnode);
		 		 		if (tempni.getIH() != null)
		 				 	break;
		   }
		  node2=tempnode;



	   	return nc.compare(node1,node2);

  	 }


}


private void generateGraph(java.util.ArrayList newmygrlist)
{	java.util.ArrayList templist = new java.util.ArrayList();
	sandmark.util.newgraph.MutableGraph myGr =
	   new sandmark.util.newgraph.MutableGraph();

	//sort based on position
	java.util.ArrayList ls=new java.util.ArrayList();
	java.util.Iterator nodeIter = gr.nodes();
		while (nodeIter.hasNext()) {
		  sandmark.util.newexprtree.Node tempnode =
		      (sandmark.util.newexprtree.Node)nodeIter.next();
		ls.add(tempnode);
	}

	java.lang.Object ob[]=ls.toArray();
	java.util.Arrays.sort(ob,new NodeComparator(NToInfo));
	java.util.List orderlist=java.util.Arrays.asList(ob);

	 nodeIter = orderlist.iterator();
	while (nodeIter.hasNext()) {
	  sandmark.util.newexprtree.Node tempnode =
	      (sandmark.util.newexprtree.Node)nodeIter.next();

	   NodeInfo tempni = (NodeInfo)NToInfo.get(tempnode);
	   if (tempni.getIH() == null)
	      continue;

	   tempni.setGraph(myGr);
	   myGr.addNode(tempnode);
	   tempnode.setGraph(myGr);

	   java.util.Iterator predIter = gr.preds(tempnode);
	   while (predIter.hasNext()) {
	      sandmark.util.newexprtree.Node debugnode =
		 (sandmark.util.newexprtree.Node)predIter.next();
	      if (!templist.contains(debugnode))
		 templist.add(debugnode);
	   }

	   java.util.Iterator succIter = gr.succs(tempnode);
	   while (succIter.hasNext()) {
	      sandmark.util.newexprtree.Node debugnode =
		 (sandmark.util.newexprtree.Node)succIter.next();
	      NodeInfo debugni = (NodeInfo)NToInfo.get(debugnode);
	      if (debugni.getIH() == null) {
		 debugni.setGraph(myGr);
		 myGr.addNode(debugnode);
		 debugnode.setGraph(myGr);

	      }
	   }

	   if(templist.contains(tempnode))
	      templist.remove(tempnode);

	   if(!newmygrlist.contains(myGr))
	      newmygrlist.add(myGr);

	   if(templist.size()==0)
	       myGr = new sandmark.util.newgraph.MutableGraph();
	}

	//TO add Edges to the new set of graph
	nodeIter = gr.nodes();
	while (nodeIter.hasNext()) {
	  sandmark.util.newexprtree.Node tempnode =
	  	      (sandmark.util.newexprtree.Node)nodeIter.next();

	       java.util.Iterator succIter = gr.succs(tempnode);
	  	   while (succIter.hasNext()) {
	   		    sandmark.util.newexprtree.Node debugnode =
				 (sandmark.util.newexprtree.Node)succIter.next();
	  			 tempnode.graph().addEdge(tempnode,debugnode);
			}
    }
}


/**
       Returns a list of sandmark.util.newgraph.MutableGraph representing the expression trees associated with a basicblock
       @param block a basicblock whose expression tree list is needed
*/

//check for il==null
public java.util.ArrayList blockToGrlist(sandmark.analysis.controlflowgraph.BasicBlock block)
{
 return (java.util.ArrayList) BToL.get(block);

}


/**
       Returns the sandmark.util.newexprtree.NodeInfo associated with a sandmark.util.newexprtree.Node .
       @param tempgn a node in the expression tree whose corresponding information is desired
*/
public sandmark.util.newexprtree.NodeInfo nodeToInfo(sandmark.util.newexprtree.Node  tempgn)
{
 return (sandmark.util.newexprtree.NodeInfo) NToInfo.get(tempgn);

}

/* It returns the node associated with an instruction */
public sandmark.util.newexprtree.Node  iToNode(org.apache.bcel.generic.InstructionHandle x)
{
	return  (sandmark.util.newexprtree.Node )IToN.get(x);

}

/* It returns the instruction associated with a node */
public org.apache.bcel.generic.InstructionHandle nodeToI(sandmark.util.newexprtree.Node  x)
{
	return  (org.apache.bcel.generic.InstructionHandle)NToI.get(x);

}



}

