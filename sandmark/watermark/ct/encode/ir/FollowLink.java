package sandmark.watermark.ct.encode.ir;

public class FollowLink extends IR implements FieldAccessor {
   public sandmark.util.newgraph.MutableGraph subGraph;
   public sandmark.util.newgraph.Node node;
   public sandmark.util.newgraph.LabeledEdge edge;
   public String protection;
   public String mCastTo;
   public String mFieldType;

   public FollowLink (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.util.newgraph.MutableGraph subGraph, 
      sandmark.util.newgraph.Node node, 
      sandmark.util.newgraph.LabeledEdge edge, 
      String protection){
      this.graph = graph;
      this.subGraph = subGraph;
      this.node = node;
      this.edge = edge;
      this.protection = protection;
   }

   public void castTo(String className) { mCastTo = className; }
   public void setFieldType(String fieldType) { mFieldType = fieldType; }
   public String getFieldName() { return edge.getLabel(); }

   public java.lang.Object clone() throws CloneNotSupportedException {
      FollowLink fl = new FollowLink(graph, subGraph, node, edge, protection);
      fl.setFieldType(mFieldType);
      fl.castTo(mCastTo);
      return fl;
   }

   public String toString(String indent) {
      return indent + 
             node.name()  + 
             " := FollowLink("  + 
             edge.getLabel()  +  ", "  + 
             subGraph +  ", "  +
             protection  +  ")";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      String nodeType = mFieldType;
      if(nodeType == null)
	 nodeType = props.getProperty("Node Class");      
      String nodeClass = props.getProperty("Node Class");
      String edgeName = edge.getLabel();
      String nodeName = node.name();

      sandmark.util.javagen.Expression source = 
	  new sandmark.util.javagen.LocalRef(((sandmark.util.newgraph.Node)edge.sourceNode()).name(), nodeType);
      sandmark.util.javagen.Expression sink = 
	  new sandmark.util.javagen.LocalRef(((sandmark.util.newgraph.Node)edge.sinkNode()).name(), nodeType);

      sandmark.util.javagen.Expression select = 
            new sandmark.util.javagen.FieldRef(
               source,
               nodeClass,
               edgeName,
               nodeType);

      if(mCastTo != null)
	 select = new sandmark.util.javagen.Cast(mCastTo,select);

      if (protection.equals("if")) {
         select = new sandmark.util.javagen.CondNotNullExpr(
            source, 
            select, 
            new sandmark.util.javagen.Null(),
            nodeType);
      } else if (protection.equals("safe")) {
         select = new sandmark.util.javagen.CondNotNullExpr(
            source, 
            select, 
            new sandmark.util.javagen.New(nodeClass),
            nodeType);
      };

      sandmark.util.javagen.Java local =
        new sandmark.util.javagen.Local(nodeName, nodeType, select);
      local.setComment(toString());
      return local;
   }
}


