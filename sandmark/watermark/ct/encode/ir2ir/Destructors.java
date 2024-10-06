package sandmark.watermark.ct.encode.ir2ir;

public class Destructors extends Transformer {

    public  Destructors (
      sandmark.watermark.ct.encode.ir.Build p, 
      sandmark.util.ConfigProperties props) {
      super(p,props);
    }


public sandmark.watermark.ct.encode.ir.Build mutate() {
   orig.destructors = (sandmark.watermark.ct.encode.ir.List) orig.creators.copy();
   java.util.Iterator iter = orig.creators.iterator();
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.HashSet names = new java.util.HashSet();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.Create c = (sandmark.watermark.ct.encode.ir.Create) iter.next();
      sandmark.watermark.ct.encode.ir.Destroy d = new sandmark.watermark.ct.encode.ir.Destroy(
           c.graph, c.subGraph, 
           (sandmark.watermark.ct.encode.ir.List)c.ops.copy(),
           (sandmark.watermark.ct.encode.ir.List)c.formals.copy());
      if(names.contains(d.name() + d.signature()))
	  throw new Error("duplicate Destroy method name");
      names.add(d.name() + d.signature());
      P.cons(destroy(d));
   }
   orig.destructors = P;
   return orig;
}

java.lang.Object rndElmt(java.util.LinkedList L) {
  return L.get(sandmark.util.Random.getRandom().nextInt(L.size()));
}

public  sandmark.watermark.ct.encode.ir.Destroy destroy(sandmark.watermark.ct.encode.ir.Destroy p) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.LinkedList seen = new java.util.LinkedList();
   java.util.Iterator iter = p.ops.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      f = f.copy();
      if (f instanceof sandmark.watermark.ct.encode.ir.CreateNode) {
         sandmark.watermark.ct.encode.ir.CreateNode c = (sandmark.watermark.ct.encode.ir.CreateNode) f;
         seen.add(c.node);
         P.cons(c);
      } else if (f instanceof sandmark.watermark.ct.encode.ir.LoadNode) {
         sandmark.watermark.ct.encode.ir.LoadNode l = (sandmark.watermark.ct.encode.ir.LoadNode) f;
        if (sandmark.util.Random.getRandom().nextFloat() > 0.5)
            P.cons(new sandmark.watermark.ct.encode.ir.CreateNode(l.graph, l.subGraph, l.node));
         else
            P.cons(l);
         seen.add(l.node);
      } else if (f instanceof sandmark.watermark.ct.encode.ir.AddEdge) {
         sandmark.watermark.ct.encode.ir.AddEdge a = (sandmark.watermark.ct.encode.ir.AddEdge) f;
	 String edgeLabel = a.getFieldName();
         if (sandmark.util.Random.getRandom().nextFloat() > 0.5) {
	    java.lang.Object source = rndElmt(seen);
	    java.lang.Object sink = rndElmt(seen);
            int edgeNumber = 1 + sandmark.util.Random.getRandom().nextInt(a.graph.maxOutDegree());
            //int edgeClass = a.edge.edgeClass();
            a.edge = new sandmark.util.newgraph.LabeledEdge(source, sink, 
							    edgeLabel);
	 }
         if (sandmark.util.Random.getRandom().nextFloat() > 0.3) 
            P.cons(a);
       } else {
         P.cons(f);
       }
   }
   p.ops = P;
   return p;
}

   public static void main (String[] args) {
     System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
     System.out.println("+++++++++++++++++++++ ir.Destructors +++++++++++++++++++++++");
     System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
   }
}

