package sandmark.watermark.ct.encode.ir2ir;

public class SaveNodes extends Transformer {

    sandmark.watermark.ct.encode.storage.GlobalStorage storage;

    public SaveNodes (
      sandmark.watermark.ct.encode.ir.Build p, 
      sandmark.watermark.ct.encode.storage.GlobalStorage storage,
      sandmark.util.ConfigProperties props) {
      super(p,props);
      this.storage = storage;
    }

/*
 * Run through the list of intermediate code instructions, looking
 * for "CreateNode"-instructions. If this node needs to be stored
 * in global storage, add a "SaveNode"-instruction.
 */
sandmark.watermark.ct.encode.ir.List storeNode(
   sandmark.watermark.ct.encode.ir.List ops) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator iter = ops.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      P.cons(f);
      if (f instanceof sandmark.watermark.ct.encode.ir.CreateNode) {
         sandmark.watermark.ct.encode.ir.CreateNode C = (sandmark.watermark.ct.encode.ir.CreateNode) f;
         sandmark.watermark.ct.encode.storage.NodeStorage s = storage.lookup(C.node);
         if (s != null)
            P.cons(new sandmark.watermark.ct.encode.ir.SaveNode(C.graph, C.subGraph, C.node, s));
      }
   }
   return P;
}

/*
 * If we haven't already loaded 'node' from global storage
 * (and this node is, in fact, stored in a global) add a 
 * 'LoadNode'-instruction to do so. 'seen' is updated to
 * indicate that this node has now been loaded.
 */
public sandmark.watermark.ct.encode.ir.List load(
   sandmark.watermark.ct.encode.ir.IR f, 
   sandmark.util.newgraph.MutableGraph subGraph,
   sandmark.util.newgraph.Node node, 
   java.util.HashSet seen) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   sandmark.watermark.ct.encode.storage.NodeStorage store = storage.lookup(node);
   if ((!seen.contains(node)) && (store != null)) {
      seen.add(node);
      P.cons(new sandmark.watermark.ct.encode.ir.LoadNode(f.graph, subGraph, node, store));
   }
   return P;
}


/*
 * Run through the list of intermediate code instructions, looking
 * for "AddEdge"- or "FollowLink"-instructions. These instructions
 * both need access to a node pointer. For each such pointer
 * add a "LoadNode"-instruction so that the node is loaded from
 * global storage.
 */
public sandmark.watermark.ct.encode.ir.List loadUsed(
   sandmark.watermark.ct.encode.ir.List ops) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.HashSet seen = new java.util.HashSet();
   java.util.Iterator iter =  ops.iterator();
   loop: while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      if (f instanceof sandmark.watermark.ct.encode.ir.AddEdge) {
         sandmark.watermark.ct.encode.ir.AddEdge F = (sandmark.watermark.ct.encode.ir.AddEdge) f;
         sandmark.util.newgraph.Node sourceNode = 
	    (sandmark.util.newgraph.Node)F.edge.sourceNode();
         P.cons(load(F, F.subGraph1, sourceNode, seen));
         sandmark.util.newgraph.Node sinkNode = 
	    (sandmark.util.newgraph.Node)F.edge.sinkNode();
         P.cons(load(F, F.subGraph2, sinkNode, seen));
      } else if (f instanceof sandmark.watermark.ct.encode.ir.FollowLink) {
          sandmark.watermark.ct.encode.ir.FollowLink F = (sandmark.watermark.ct.encode.ir.FollowLink) f;
          sandmark.util.newgraph.Node sinkNode = 
	     (sandmark.util.newgraph.Node)F.edge.sinkNode();
          sandmark.watermark.ct.encode.storage.NodeStorage store = storage.lookup(sinkNode);
          if (store != null) {
             seen.add(sinkNode);
             P.cons(new sandmark.watermark.ct.encode.ir.LoadNode(F.graph, F.subGraph, sinkNode, storage.lookup(sinkNode)));
             continue loop;
          } else {
             sandmark.util.newgraph.Node sourceNode = 
		(sandmark.util.newgraph.Node)F.edge.sourceNode();
             P.cons(load(F, F.subGraph, sourceNode, seen));
          }
      }
      P.cons(f);
   };
   return P;
}

/*
 * For every generated method add StoreNode and LoadNode instructions
 * that make sure that the nodes that need to be loaded or saved
 * to global storage in fact are.
 */
public sandmark.watermark.ct.encode.ir.Build mutate() {
   java.util.Iterator iter1 = orig.creators.iterator();
   while (iter1.hasNext()) {
      sandmark.watermark.ct.encode.ir.Create f = (sandmark.watermark.ct.encode.ir.Create) iter1.next();
      f.ops = storeNode(f.ops);
      f.ops = loadUsed(f.ops);
   };

   java.util.Iterator iter3 = orig.fixups.iterator();
   while (iter3.hasNext()) {
      sandmark.watermark.ct.encode.ir.Fixup f = (sandmark.watermark.ct.encode.ir.Fixup) iter3.next();
      f.ops = loadUsed(f.ops);
   };
   return orig;
}

  //--------------------------------------------------------------
   public static void printStorage (
      sandmark.watermark.ct.encode.storage.GlobalStorage storage, 
      String nodeName,
      sandmark.util.newgraph.Node node) {
      sandmark.watermark.ct.encode.storage.NodeStorage s = storage.lookup(node);
      System.out.print(nodeName + " is stored in: ");
      if (s == null)
          System.out.println("null");
      else
          System.out.println(s.toString());
   }
}

