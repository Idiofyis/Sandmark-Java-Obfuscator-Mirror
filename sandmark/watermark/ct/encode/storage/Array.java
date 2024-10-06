package sandmark.watermark.ct.encode.storage;

public class Array extends sandmark.watermark.ct.encode.storage.StorageClass {

    public Array (int storeLocation) {
       this.storeLocation = storeLocation;
    }

    public String toString() {
        return "Array/" + locationToString();
    }

    static final String arrayName = "sm$array";

    public String typeName (sandmark.util.ConfigProperties props) {
        String nodeType = props.getProperty("Node Class");
	return nodeType+"[]";
    }

    public String variableName (
            sandmark.util.ConfigProperties props){
	return arrayName;
    }


    sandmark.util.javagen.Expression getContainer(sandmark.util.ConfigProperties props) {
        String nodeType = props.getProperty("Node Class");
	if (storeLocation == sandmark.watermark.ct.encode.storage.StorageClass.GLOBAL)
	    return new sandmark.util.javagen.StaticRef(nodeType, variableName(props), typeName(props));
        else
	    return new sandmark.util.javagen.LocalRef(variableName(props), typeName(props));
    }

    public sandmark.util.javagen.Statement toJavaStore(
      sandmark.util.newgraph.Node node, 
      sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");
      int nodeNumber = node.nodeNumber();
      sandmark.util.javagen.AssignIndex stat =
          new sandmark.util.javagen.AssignIndex(
	     getContainer(props),
             new sandmark.util.javagen.LiteralInt(nodeNumber),
	     new sandmark.util.javagen.LocalRef(node.name(), nodeType));
      return stat;
    }

    public sandmark.util.javagen.Expression toJavaLoad(
       sandmark.util.newgraph.Node node, 
       sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");
      int nodeNumber = node.nodeNumber();
      sandmark.util.javagen.LoadIndex expr = 
	     new sandmark.util.javagen.LoadIndex(
	        getContainer(props),
                new sandmark.util.javagen.LiteralInt(nodeNumber),
                nodeType);

      return expr;
   }

   public sandmark.util.javagen.Statement toJavaCreate (
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");

      int nodeCount = graph.nodeCount();
      sandmark.util.javagen.Local local =
	  new sandmark.util.javagen.Local(
	    variableName(props),
            typeName(props),
            new sandmark.util.javagen.NewArray(nodeType,nodeCount));

      return local;
   }

    public boolean equals(java.lang.Object o) {
	sandmark.watermark.ct.encode.storage.Array a = (sandmark.watermark.ct.encode.storage.Array)o;
        return storeLocation==a.storeLocation;
    }

    public int hashCode() {
        return storeLocation + sandmark.watermark.ct.encode.storage.Array.class.hashCode();
    }
}



