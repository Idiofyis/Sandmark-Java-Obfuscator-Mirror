package sandmark.util.newgraph.codec;

/**This class provides methods for encoding and decoding BigIntegers
 * from PPCT's. The BigInteger is first encoded into a totally
 * balanced binary sequence, which is used to construct a PPT.
 * The PPT is then converted into a PPCT. The process is reversed
 * for decoding.
 * @author Zach Heidepriem
 */
public class PlantedPlaneCubicTree extends AbstractCodec {

    private static boolean DEBUG = false;

    static class BTreeNode {
        BTreeNode right;
	BTreeNode left;
	Object node;//Used for encoding
	int index = -1; //Used for decoding
	BTreeNode(Object n) { node = n; }
	BTreeNode() { }
    }

    static class NTreeNode {
	java.util.List kids = new java.util.ArrayList();
	Object node;
	NTreeNode(Object n) { node = n; }
    }

    public int maxOutDegree() { return 2; }

    public sandmark.util.newgraph.Graph encode
	(java.math.BigInteger val,sandmark.util.newgraph.NodeFactory factory) {
	//First get a TBBS from the BigInt
	TotallyBalancedBinarySequence tbbs = new TotallyBalancedBinarySequence(val);   
        if(DEBUG)
            System.out.println("The encoded tbbs is:" + tbbs);
	
        //Now build a PPT (n-ary tree) from the BigInt
        java.util.Stack stack = new java.util.Stack();
	NTreeNode root = new NTreeNode(factory.createNode());
        stack.push(root);
        for(int i = 0; i < tbbs.size(); i++){
            //false means go down...
            if(!tbbs.get(i)){
		NTreeNode curr = (NTreeNode)stack.peek();
                NTreeNode next = new NTreeNode(factory.createNode());
		curr.kids.add(next);
                stack.push(next);                
            }
            else //true means go up
               stack.pop();            
        }

	//Convert the n-ary tree to a binary tree.
	//Drop the root as described in the reference
	BTreeNode btRoot = nToBTree(root).left;

	addLeaves(btRoot,factory);

        sandmark.util.newgraph.Graph g = 
	    sandmark.util.newgraph.Graphs.createGraph(null,null);
	g = buildGraph(g,btRoot);

	java.util.List leaves = new java.util.ArrayList();
	leavesInOrder(leaves,btRoot);
	Object realRoot = factory.createNode();
	g = g.addNode(realRoot).addEdge(realRoot,btRoot.node).
	    addEdge(realRoot,leaves.get(0)).
	    addEdge(leaves.get(leaves.size() - 1),realRoot);

	for(int i = 0 ; i < leaves.size() - 1 ; i++)
	    g = g.addEdge(leaves.get(i),leaves.get(i + 1));

	return g;
    }

    /* As described in http://citeseer.nj.nec.com/598821.html 
       Theorem 2 Remark 2 */
    private static BTreeNode nToBTree(NTreeNode root) {
	BTreeNode btRoot = new BTreeNode(root.node);
	BTreeNode prevKid = null;
	for(int i = root.kids.size() - 1 ; i >= 0 ; i--) {
	    BTreeNode kid = nToBTree((NTreeNode)root.kids.get(i));
	    kid.right = prevKid;
	    prevKid = kid;
	}
	btRoot.left = prevKid;
	return btRoot;
    }

    /* As described in http://citeseer.nj.nec.com/598821.html 
       Theorem 2 Remark 2, but reversed */
    private static NTreeNode bToNTree(BTreeNode root) {
	NTreeNode ntRoot = new NTreeNode(root.node);
	for(BTreeNode node = root.left ; node != null ; node = node.right)
	    ntRoot.kids.add(bToNTree(node));
	return ntRoot;
    }

    private static void addLeaves
	(BTreeNode root,sandmark.util.newgraph.NodeFactory factory) {
	if(root.left == null)
	    root.left = new BTreeNode(factory.createNode());
	else
	    addLeaves(root.left,factory);

	if(root.right == null)
	    root.right = new BTreeNode(factory.createNode());
	else
	    addLeaves(root.right,factory);
    }

    private static sandmark.util.newgraph.Graph buildGraph
	(sandmark.util.newgraph.Graph g,BTreeNode root) {
	if(root.left == null)
	    return g.addNode(root.node);

	return buildGraph(buildGraph(g,root.left),root.right).
	    addNode(root.node).addEdge(root.node,root.left.node).
	    addEdge(root.node,root.right.node);
    }

    private static void leavesInOrder(java.util.List list,BTreeNode root) {
	if(root.left == null)
	    list.add(root.node);
	else {
	    leavesInOrder(list,root.left);
	    leavesInOrder(list,root.right);
	}
    }  
    
    public java.math.BigInteger decode(sandmark.util.newgraph.Graph g) 
	throws DecodeFailure {
	java.util.List leaves = new java.util.ArrayList();
	Object root = findRootAndLeaves(g,leaves);
	java.util.Iterator rootSuccs = g.succs(root);
	Object realRoot = rootSuccs.next();
	if(!rootSuccs.hasNext())
	    throw new DecodeFailure("root must have 2 different successors");
	if(realRoot.equals(leaves.get(0)))
	    realRoot = rootSuccs.next();
	g = g.removeNode(root);
	for(int i = 0 ; i < leaves.size() - 1; i++)
	    g = g.removeEdge(leaves.get(i),leaves.get(i + 1));
	BTreeNode btRoot = buildBTree(g,realRoot,leaves);
	removeLeaves(btRoot);

	//Re-add the root of the tree we remove during encoding, as described
	//in the reference
	BTreeNode btRealRoot = new BTreeNode();
	btRealRoot.left = btRoot;

	NTreeNode ntRoot = bToNTree(btRealRoot);
        java.util.List sequence = new java.util.ArrayList();
        buildSequence(ntRoot, sequence);

        //Build a TBBS object from the sequence (calculate rank)
        TotallyBalancedBinarySequence tbbs = 
	    new TotallyBalancedBinarySequence(getArray(sequence));
        if(DEBUG)
            System.out.println("The decoded TBBS is: " + tbbs);
        return tbbs.getRank();
    }

    private static boolean [] getArray(java.util.List list) {
	boolean bits[] = new boolean[list.size()];
	int i = 0;
	for(java.util.Iterator it = list.iterator() ; it.hasNext() ; i++) {
	    bits[i] = ((Boolean)it.next()).booleanValue();
	}
	return bits;
    }

    private static BTreeNode buildBTree
	(sandmark.util.newgraph.Graph g,Object root,java.util.List leaves) 
	throws DecodeFailure {
	java.util.Hashtable nodeToBTN = new java.util.Hashtable();
	for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
	    Object node = nodes.next();
	    BTreeNode btn = new BTreeNode();
	    nodeToBTN.put(node,btn);
	}
	int i = 0;
	for(java.util.Iterator leafIt = leaves.iterator() ; 
	    leafIt.hasNext() ; i++) {
	    Object leaf = leafIt.next();
	    ((BTreeNode)nodeToBTN.get(leaf)).index = i;
	    Object node = leaf;
	    while(!node.equals(root)) {
		if(g.inDegree(node) != 1)
		    throw new DecodeFailure
			("Tree nodes should have in-degree 1");
		BTreeNode nodeBTN = (BTreeNode)nodeToBTN.get(node);
		if(nodeBTN.index == -1)
		    throw new Error("should have set index from a child");
		Object parent = g.preds(node).next();
		BTreeNode parentBTN = (BTreeNode)nodeToBTN.get(parent);
		if(nodeBTN == parentBTN.left || nodeBTN == parentBTN.right)
		    break;
		if(nodeBTN.index < parentBTN.index || parentBTN.index == -1) {
		    parentBTN.right = parentBTN.left;
		    parentBTN.left = nodeBTN;
		    parentBTN.index = nodeBTN.index;
		} else
		    parentBTN.right = nodeBTN;
		node = parent;
	    }
	}
	return (BTreeNode)nodeToBTN.get(root);
    }

    private static void removeLeaves(BTreeNode root) throws DecodeFailure {
	checkNode(root);

	if(root.left == null)
	    throw new Error("Shouldn't be visiting removable nodes");

	checkNode(root.left);
	checkNode(root.right);

	if(root.left.left == null)
	    root.left = null;
	else
	    removeLeaves(root.left);

	if(root.right.right == null)
	    root.right = null;
	else
	    removeLeaves(root.right);
    }

    private static void checkNode(BTreeNode node) throws DecodeFailure {
	if((node.left == null) ^ (node.right == null))
	    throw new DecodeFailure("PPCT should be a complete binary tree");
    }

    private static Object findRootAndLeaves
	(sandmark.util.newgraph.Graph g,java.util.List leavesInOrder) 
	throws DecodeFailure {
	Object leaf = null;
	for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
	    Object node = nodes.next();
	    if(g.outDegree(node) == 1) {
		leaf = node;
		break;
	    }
	}

	if(leaf == null || g.inDegree(leaf) != 2)
	    throw new DecodeFailure("no leaf nodes (2 in, 1 out)");

	Object root = leaf;
	for( ; g.outDegree(root) == 1 ; root = g.succs(root).next())
	    ;
	if(g.inDegree(root) != 1 || g.outDegree(root) != 2)
	    throw new DecodeFailure
		("no root (1 in, 2 out, reachable through an all-leaf path");

	Object firstLeaf = null;
	for(java.util.Iterator rootSuccs = g.succs(root) ; 
	    rootSuccs.hasNext() ; ) {
	    Object succ = rootSuccs.next();
	    if(g.inDegree(succ) == 2 && g.outDegree(succ) == 1) {
		firstLeaf = succ;
		break;
	    }
	}
	if(firstLeaf == null)
	    throw new DecodeFailure("Root has no edge to a leaf (2 in, 1 out)");

	leaf = firstLeaf;
	for( ; g.inDegree(leaf) == 2 && g.outDegree(leaf) == 1 ; 
	    leavesInOrder.add(leaf),leaf = g.succs(leaf).next())
	    ;

	if(!leaf.equals(root))
	    throw new DecodeFailure
		("Root and connected leaves do not form a cycle");

	if(leavesInOrder.size() * 2 != g.nodeCount())
	    throw new DecodeFailure
		("Half of nodes should be leaves (" + leavesInOrder.size() + 
		 " leaves, " + g.nodeCount() + " nodes)");

	return root;
    }

    //Build a TBBS starting at node, appending result to sequence
    private void buildSequence(NTreeNode root, java.util.List sequence) {
	for(java.util.Iterator it = root.kids.iterator() ; it.hasNext() ; ) {
	    NTreeNode node = (NTreeNode)it.next();
	    sequence.add(Boolean.FALSE);
	    buildSequence(node,sequence);
	}
        sequence.add(Boolean.TRUE);                                
    }

    public static void main(String argv[]) throws Exception {
	new PlantedPlaneCubicTree().test(argv);
    }
}
