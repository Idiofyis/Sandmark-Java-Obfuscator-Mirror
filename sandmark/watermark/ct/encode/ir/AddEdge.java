package sandmark.watermark.ct.encode.ir;

public class AddEdge extends IR implements FieldAccessor {
   public sandmark.util.newgraph.MutableGraph subGraph1;
   public sandmark.util.newgraph.MutableGraph subGraph2;
   public sandmark.util.newgraph.LabeledEdge edge;
   public String protection;
   public String mFieldType;

   public AddEdge (sandmark.util.newgraph.MutableGraph graph, 
                   sandmark.util.newgraph.MutableGraph subGraph1, 
                   sandmark.util.newgraph.MutableGraph subGraph2, 
                   sandmark.util.newgraph.LabeledEdge edge, 
                   String protection){
      this.graph = graph;
      this.subGraph1 = subGraph1;
      this.subGraph2 = subGraph2;
      this.edge = edge;
      this.protection = protection;
   }

   public void setFieldType(String fieldType) { mFieldType = fieldType; }
   public String getFieldName() { return edge.getLabel(); }

   public java.lang.Object clone() throws CloneNotSupportedException {
      AddEdge ae = new AddEdge(graph, subGraph1, subGraph2, edge, protection);
      ae.setFieldType(mFieldType);
      return ae;
   }

   public String toString(String indent) {
      return indent + "AddEdge(" + edge.getLabel()  + 
	         ", "  +  edge.sourceNode() +
	         ", "  +  edge.sinkNode() +
                 ", "  +  subGraph1  +
                 ", "  +  subGraph2  + 
                 ", "  +  protection  +  ")";

   }


   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      String nodeType = mFieldType;
      if(nodeType == null)
	 nodeType = props.getProperty("Node Class");      
      String edgeName = edge.getLabel();

      sandmark.util.javagen.Expression source = 
	  new sandmark.util.javagen.LocalRef(((sandmark.util.newgraph.Node)edge.sourceNode()).name(), nodeType);
      sandmark.util.javagen.Expression sink = 
	  new sandmark.util.javagen.LocalRef(((sandmark.util.newgraph.Node)edge.sinkNode()).name(), nodeType);

      sandmark.util.javagen.Statement stat = 
	  new sandmark.util.javagen.AssignField(
             source, 
             props.getProperty("Node Class"),
             edgeName, 
             nodeType,
             sink);
      stat.setComment(toString());

      if (protection.equals("if") || protection.equals("safe"))
         stat = new sandmark.util.javagen.IfNotNull(source, stat);
      return stat;
   }
}


