package sandmark.watermark.ct.encode.ir2ir;

public class CleanUp extends Transformer {

    sandmark.watermark.ct.encode.storage.GlobalStorage storage;

    public CleanUp (
      sandmark.watermark.ct.encode.ir.Build p, 
      sandmark.util.ConfigProperties props) {
      super(p,props);
    }

    sandmark.util.newgraph.Node getNode(sandmark.watermark.ct.encode.ir.IR f) {
        if (f instanceof sandmark.watermark.ct.encode.ir.FollowLink) 
	    return ((sandmark.watermark.ct.encode.ir.FollowLink)f).node;
        else if (f instanceof sandmark.watermark.ct.encode.ir.LoadNode) 
	    return ((sandmark.watermark.ct.encode.ir.LoadNode)f).node;
        else if (f instanceof sandmark.watermark.ct.encode.ir.CreateNode) 
	    return ((sandmark.watermark.ct.encode.ir.CreateNode)f).node;
        return null;
    }

    sandmark.util.newgraph.Edge getEdge(sandmark.watermark.ct.encode.ir.IR f) {
        if (f instanceof sandmark.watermark.ct.encode.ir.AddEdge) 
	    return ((sandmark.watermark.ct.encode.ir.AddEdge)f).edge;
        else if (f instanceof sandmark.watermark.ct.encode.ir.FollowLink) 
	    return ((sandmark.watermark.ct.encode.ir.FollowLink)f).edge;
        return null;
    }

//================= Remove redundant loads =====================
public sandmark.watermark.ct.encode.ir.List removeUnused(sandmark.watermark.ct.encode.ir.List p) {
    sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
    java.util.HashSet needed = new java.util.HashSet();
    java.util.Iterator iter =  p.backwards();
    loop: while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR f  = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      if (((f instanceof sandmark.watermark.ct.encode.ir.FollowLink) ||
           (f instanceof sandmark.watermark.ct.encode.ir.LoadNode)) &&
	  !needed.contains(getNode(f)))
          continue loop;
      if ((f instanceof sandmark.watermark.ct.encode.ir.AddEdge) ||
          (f instanceof sandmark.watermark.ct.encode.ir.FollowLink)) {
	  needed.add(getEdge(f).sourceNode());
	  needed.add(getEdge(f).sinkNode());
      }
      P.snoc(f);
   };
   return P;
}

public sandmark.watermark.ct.encode.ir.List removeMultipleA(sandmark.watermark.ct.encode.ir.List p) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.HashSet seen = new java.util.HashSet();
   java.util.Iterator iter =  p.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR f  = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      if ((f instanceof sandmark.watermark.ct.encode.ir.CreateNode) ||
          (f instanceof sandmark.watermark.ct.encode.ir.LoadNode)) {
          P.cons(f);
          seen.add(getNode(f));
      } else if (f instanceof sandmark.watermark.ct.encode.ir.FollowLink) {
          if (!seen.contains(f)) {
             P.cons(f);
             seen.add(getNode(f));
          }
      } else
          P.cons(f);
   };
   return P;
}

public sandmark.watermark.ct.encode.ir.List removeMultipleB(sandmark.watermark.ct.encode.ir.List p) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.HashSet seen = new java.util.HashSet();
   java.util.Iterator iter =  p.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR f  = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      if ((f instanceof sandmark.watermark.ct.encode.ir.CreateNode) ||
          (f instanceof sandmark.watermark.ct.encode.ir.LoadNode) ||
          (f instanceof sandmark.watermark.ct.encode.ir.FollowLink)) {
          if (!seen.contains(getNode(f))) {
             P.cons(f);
             seen.add(getNode(f));
          }
      } else 
         P.cons(f);
   };
   return P;
}

public sandmark.watermark.ct.encode.ir.List removeMultiple(sandmark.watermark.ct.encode.ir.List p) {
   p = removeMultipleA(p);
   p = removeMultipleB(p);
   return p;
}

public void clean(sandmark.watermark.ct.encode.ir.List M) {
   java.util.Iterator iter1 = M.iterator();
   while (iter1.hasNext()) {
      sandmark.watermark.ct.encode.ir.Method f = (sandmark.watermark.ct.encode.ir.Method) iter1.next();
      f.ops = removeMultiple(f.ops);
      f.ops = removeUnused(f.ops);
   };
}

public sandmark.watermark.ct.encode.ir.Build mutate() {
   clean(orig.creators);
   clean(orig.fixups);
   clean(orig.destructors);
   return orig;
}
}

