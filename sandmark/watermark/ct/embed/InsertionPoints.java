package sandmark.watermark.ct.embed;
/*This class is used to determine the nodes where the calls to create_WatermarkGraph
**  could be embedded . It also calculates the node where the call to create_StorageCreators
** should be embedded
*/

class InsertionPoints{
	private static final boolean Debug=false;
	java.util.ArrayList insertionPoints;
	sandmark.watermark.ct.trace.callforest.Node DominatorNode;
	sandmark.util.MethodID[] allMeth;
	/*<p>
 	 *    @param numcomponents  The maximum number of components required
     *    @param f  			The callForest
	 */
	public InsertionPoints(int numcomponents,sandmark.watermark.ct.trace.callforest.Forest f)
	{
		insertionPoints=getNodeList(numcomponents,f);
		allMeth=allMethods(f,insertionPoints);
	}

	/*returns a list of nodes where call to create_WatermarkGraph could be embedded
	 */
	public java.util.ArrayList	getInsertionPoints()
	{
		return insertionPoints;
	}

	/*It calculates the point where the call to create_StorageCreator should be embedded
	 */
	public sandmark.watermark.ct.trace.callforest.Node	getDomNode()
	{
		return DominatorNode;
	}

	/*The methods to which formal parameters should
                           be added to pass around storage containers.
 	*/
	public sandmark.util.MethodID[] getAllMethods()
	{
		return allMeth;
	}


/*calculates the node which dominates all other mark node in the graph
**and which appears the latest
 */

sandmark.watermark.ct.trace.callforest.Node getStorageNode(sandmark.util.newgraph.MutableGraph graph) {
   int i;
   java.util.ArrayList m = new java.util.ArrayList();
   sandmark.watermark.ct.trace.callforest.Node resultNode=null;
   Object root = null;

   for(java.util.Iterator nodes = graph.nodes() ; nodes.hasNext() ; ) {
      sandmark.watermark.ct.trace.callforest.Node node =
         (sandmark.watermark.ct.trace.callforest.Node) nodes.next();
      if (node.isMarkNode() && node.isEnterNode())
      {   m.add(node);

  	 }
       if(graph.inDegree(node) == 0) {
 	  if(root != null) {
 	      System.err.println
 		  ("multiple roots " + 
 		   System.identityHashCode(root) + " and " + 
 		   System.identityHashCode(node));
 	  }
 	  root = node;
       }
   }

    if(root == null)
        throw new RuntimeException("no root");
 
 
    sandmark.util.newgraph.DomTree dom = graph.dominatorTree(root);

   for(java.util.Iterator nodes = graph.nodes() ; nodes.hasNext() ; ) {
	   sandmark.watermark.ct.trace.callforest.Node node =
         (sandmark.watermark.ct.trace.callforest.Node) nodes.next();
         if(!node.isEnterNode())
         	continue;
         if(node.isMarkNode())
         	continue;

 	 
 	 java.util.HashSet hs=new java.util.HashSet();
 	 for(java.util.Iterator dominated = dom.dominated(node); 
 	     dominated.hasNext() ; )
 	     hs.add(dominated.next());
 
 	 for(i=0;i<m.size();i++)
   		{
			if(!hs.contains(m.get(i)))
				break;
		}
		if(i==m.size())
		{
			resultNode=node;
		}

   }

   return resultNode;
}

/*determine the points where the calls to create_WatermarkGraph
**  could be embedded
*/
	private java.util.ArrayList getNodeList(int numcomponents,sandmark.watermark.ct.trace.callforest.Forest f)
	{
		int i,j,k;
		sandmark.watermark.ct.trace.callforest.Node lastNode=null;
	    sandmark.watermark.ct.trace.callforest.Node firstNode=null;
	 	java.util.ArrayList result = new java.util.ArrayList();
	 	java.util.ArrayList remaining = new java.util.ArrayList();
		java.util.Vector temp=new java.util.Vector();
		//only consider the last graph in the call forest
		sandmark.util.newgraph.MutableGraph gr=(sandmark.util.newgraph.MutableGraph)f.getCallGraph(f.size()-1);

		temp.add(gr);
		sandmark.watermark.ct.trace.callforest.PathGenerator paths =
	     new sandmark.watermark.ct.trace.callforest.PathGenerator(temp,500);
		java.util.ArrayList  orderedPaths = new java.util.ArrayList();
		int overallweight=0;


		DominatorNode=getStorageNode(gr);
		if(Debug)
			System.out.println("Dom Node= "+DominatorNode);

		//consider only those paths whose first node is reachable from DominatorNode
  		while(paths.hasNext())
  		{
			sandmark.watermark.ct.trace.callforest.Path path=(sandmark.watermark.ct.trace.callforest.Path)paths.next();
			sandmark.util.newgraph.Afs afs=new sandmark.util.newgraph.Afs(gr,DominatorNode);
			while(afs.hasNext())
			{ sandmark.util.newgraph.Path pf=(sandmark.util.newgraph.Path)afs.next();
			  if(pf.onPath(path.firstNode()))
			  {		orderedPaths.add(path);
					break;
			  }
			}
		}

	//Find the node with the largest weight. Add it to the list
	//Call it the lastnode
	for(i=0;i<orderedPaths.size();i++)
	{	sandmark.watermark.ct.trace.callforest.Path remember;
		sandmark.watermark.ct.trace.callforest.Path path=
			(sandmark.watermark.ct.trace.callforest.Path)orderedPaths.get(i);
		if(path.size()==1)
		{
			sandmark.watermark.ct.trace.callforest.Node node=
				(sandmark.watermark.ct.trace.callforest.Node)path.get(0);
			remaining.add(node);
			if(lastNode==null)
				lastNode=node;
			if(node.getWeight()>lastNode.getWeight())
			{	lastNode=node;

			}
		}

	}

	if(Debug)
		System.out.println("LastNode="+lastNode);
	result.add(lastNode);
	remaining.remove(lastNode);

	//find path that ends at last node and has the largest weight.
	//Add its fist node to the list. Call it the firstnode

	if(numcomponents>1 && remaining.size()>0)
	{


		sandmark.watermark.ct.trace.callforest.Path lastpath=null;
		for(i=0;i<orderedPaths.size();i++)
		{
			sandmark.watermark.ct.trace.callforest.Path path=
			 (sandmark.watermark.ct.trace.callforest.Path)orderedPaths.get(i);

			sandmark.watermark.ct.trace.callforest.Node last=
			         (sandmark.watermark.ct.trace.callforest.Node)path.lastNode();
			if(last==lastNode)
			{ sandmark.watermark.ct.trace.callforest.Node first=
				(sandmark.watermark.ct.trace.callforest.Node)path.firstNode();
				if(remaining.contains(first))
				{
					if(lastpath==null)
						lastpath=path;
					if(sandmark.watermark.ct.trace.callforest.Path.getWeight(path,f) >sandmark.watermark.ct.trace.callforest.Path.getWeight(lastpath,f))
					{
						lastpath=path;
					}
				}

			}

		}
		if(lastpath!=null)
		{	overallweight=sandmark.watermark.ct.trace.callforest.Path.getWeight(lastpath,f);
			firstNode=
				(sandmark.watermark.ct.trace.callforest.Node)lastpath.firstNode();
			if (Debug) System.out.println("firstNode="+firstNode);
			result.add(firstNode);
			remaining.remove(firstNode);
		}

	}

	//Keep adding nodes so that they are spread across the entire path
	// from the firstnode and the lastnode.


	for(k=3;k<=numcomponents;k++)
	{
		if(remaining.size()==0)
			break;
		int maxweight=0;
		sandmark.watermark.ct.trace.callforest.Node newnode=null;


		for(i=0;i<orderedPaths.size();i++)
		{
			sandmark.watermark.ct.trace.callforest.Path path=
			 (sandmark.watermark.ct.trace.callforest.Path)orderedPaths.get(i);

			sandmark.watermark.ct.trace.callforest.Node last=
					 (sandmark.watermark.ct.trace.callforest.Node)path.lastNode();

			sandmark.watermark.ct.trace.callforest.Node first=
							(sandmark.watermark.ct.trace.callforest.Node)path.firstNode();

			if(first!=firstNode)
					continue;

			if(!remaining.contains(last))
					continue;

			for(j=0;j<orderedPaths.size();j++)
			{
				sandmark.watermark.ct.trace.callforest.Path lowerpath=
					 (sandmark.watermark.ct.trace.callforest.Path)orderedPaths.get(j);
				if(lowerpath.firstNode()!=last)
					continue;
				if(lowerpath.lastNode()!=lastNode)
					continue;
				int totweight=sandmark.watermark.ct.trace.callforest.Path.getWeight(path,f)+sandmark.watermark.ct.trace.callforest.Path.getWeight(lowerpath,f);
				//int totweight=getWeight(path,f);
				double spreadfactor=
				1-Math.abs(((k-2)/(double)(numcomponents-1))-sandmark.watermark.ct.trace.callforest.Path.getWeight(path,f)/(double)totweight);
				int currweight=(int)(totweight*spreadfactor);
				if(currweight>maxweight)
				{
					maxweight=currweight;
					newnode=last;
				}

			}

	 	}
		if(newnode!=null)
		{
			if(Debug)
				System.out.println("newNode="+newnode);
			result.add(newnode);
			remaining.remove(newnode);
		}
		else
			break;

    }
    return result;
}

/* List of methods for which we have to modify the parameter list. This methods appear in the path
 **from the DomNode to the node where the CreateGraph() method was called.
 ** Note: It looks only the nodes in the last callforest graph
 */
sandmark.util.MethodID[] allMethods(sandmark.watermark.ct.trace.callforest.Forest f,java.util.ArrayList inlist)
{

	java.util.ArrayList res=new java.util.ArrayList();
	res.addAll(inlist);
	sandmark.util.newgraph.MutableGraph gr=(sandmark.util.newgraph.MutableGraph)f.getCallGraph(f.size()-1);
	boolean change=true;
	while(change)
	{	change=false;
		sandmark.util.newgraph.Afs afs=new sandmark.util.newgraph.Afs(gr,DominatorNode);
		while(afs.hasNext())
		{	sandmark.util.newgraph.Path path=(sandmark.util.newgraph.Path)afs.next();
			java.util.Iterator iter = path.iterator();
			sandmark.watermark.ct.trace.callforest.Node prev=null;
			while (iter.hasNext()) {
				sandmark.watermark.ct.trace.callforest.Node curr =
      			   (sandmark.watermark.ct.trace.callforest.Node) iter.next();


				if(res.contains(curr)&&(prev!=null)&&(prev.getMethod()!=DominatorNode.getMethod()))
				if((!res.contains(prev)) && (prev.isCallNode()||prev.isEnterNode()) )
				{ if(Debug)
				  	System.out.println("ADDED CURR="+curr.nodeNumber()+"PREV="+prev.nodeNumber());
				  res.add(prev);
				  change=true;
				}
				prev=curr;
			}
		}

	}
	java.util.HashSet hs = new java.util.HashSet();
	for(int i=0;i<res.size();i++)
	{	hs.add(((sandmark.watermark.ct.trace.callforest.Node)res.get(i)).getMethod());
	}

	hs.remove(DominatorNode.getMethod());
	sandmark.util.MethodID[] s = new sandmark.util.MethodID[hs.size()];
	s=(sandmark.util.MethodID[])hs.toArray(s);

	if(Debug)
	{
		for(int i=0;i<s.length;i++)
		System.out.println("MethodID="+s[i]);
	}



	return s;
}





}
