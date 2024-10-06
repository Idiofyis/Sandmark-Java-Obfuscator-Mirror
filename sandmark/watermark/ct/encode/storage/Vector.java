package sandmark.watermark.ct.encode.storage;

public class Vector extends sandmark.watermark.ct.encode.storage.StorageClass {

    public Vector (int storeLocation) {
       this.storeLocation = storeLocation;
    }

    public String toString() {
        return "Vector/" + locationToString();
    }

    static final String vectorName = "sm$vec";
    static final String storageType = "java.util.Vector";

    public String typeName (sandmark.util.ConfigProperties props) {
	return storageType;
    }

    public String variableName (sandmark.util.ConfigProperties props){
	return vectorName;
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

      sandmark.util.javagen.VirtualFunCall set =
	  new sandmark.util.javagen.VirtualFunCall(
             getContainer(props),
             typeName(props), 
             "set", 
             "(ILjava/lang/Object;)Ljava/lang/Object;", 
             new sandmark.util.javagen.LiteralInt(nodeNumber),
             new sandmark.util.javagen.LocalRef(
                node.name(),
                nodeType));

      sandmark.util.javagen.Discard stat = 
         new sandmark.util.javagen.Discard(set);
      return stat;
   }

    public sandmark.util.javagen.Expression toJavaLoad(
      sandmark.util.newgraph.Node node, 
      sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");
      int nodeNumber = node.nodeNumber();

      sandmark.util.javagen.VirtualFunCall get =
	  new sandmark.util.javagen.VirtualFunCall(
             getContainer(props),
             typeName(props), 
             "get", 
             "(I)Ljava/lang/Object;", 
             new sandmark.util.javagen.LiteralInt(nodeNumber));

       sandmark.util.javagen.Cast cast =
             new sandmark.util.javagen.Cast(
                nodeType,
                get);

      return cast;
   }

   public sandmark.util.javagen.Statement toJavaCreate (
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.util.ConfigProperties props) {

      int nodeCount = graph.nodeCount();
      sandmark.util.javagen.Local local =
	  new sandmark.util.javagen.Local(
	    variableName(props),
            typeName(props),
            new sandmark.util.javagen.New(
                typeName(props), 
                new sandmark.util.javagen.LiteralInt(nodeCount)));

      sandmark.util.javagen.VirtualCall stat =
	  new sandmark.util.javagen.VirtualCall(
             new sandmark.util.javagen.LocalRef(variableName(props), typeName(props)),
             typeName(props), 
             "setSize", 
             "(I)V", 
             new sandmark.util.javagen.LiteralInt(nodeCount));

      return new sandmark.util.javagen.Block(new sandmark.util.javagen.List(local, stat));
   }


   public static void toJavaInit (
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");

      int nodeCount = graph.nodeCount();
      sandmark.util.javagen.VirtualCall stat =
	  new sandmark.util.javagen.VirtualCall(
             new sandmark.util.javagen.StaticRef(nodeType, vectorName, storageType),
             storageType, 
             "setSize", 
             "(I)V", 
             new sandmark.util.javagen.LiteralInt(nodeCount));

      sandmark.util.javagen.Class.addStaticStat(stat);
   }

    public boolean equals(java.lang.Object o) {
	sandmark.watermark.ct.encode.storage.Vector a = (sandmark.watermark.ct.encode.storage.Vector)o;
        return storeLocation==a.storeLocation;
    }

    public int hashCode() {
        return storeLocation + sandmark.watermark.ct.encode.storage.Vector.class.hashCode();
    }
}
       



