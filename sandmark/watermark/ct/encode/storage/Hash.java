package sandmark.watermark.ct.encode.storage;

public class Hash extends sandmark.watermark.ct.encode.storage.StorageClass {

    public Hash (int storeLocation) {
       this.storeLocation = storeLocation;
    }

    public String toString() {
        return "Hash/" + locationToString();
    }

    static final String hashName = "sm$hash";
    static final String storageType = "java.util.Hashtable";

    public String typeName (sandmark.util.ConfigProperties props) {
	return storageType;
    }

    public String variableName (sandmark.util.ConfigProperties props){
	return hashName;
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

      sandmark.util.javagen.VirtualFunCall put =
	  new sandmark.util.javagen.VirtualFunCall(
             getContainer(props),
             typeName(props), 
             "put", 
             "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", 
             new sandmark.util.javagen.New(
                "java.lang.Integer",
                new sandmark.util.javagen.LiteralInt(nodeNumber)),
             new sandmark.util.javagen.LocalRef(
                node.name(),
                nodeType));

      sandmark.util.javagen.Discard stat = 
         new sandmark.util.javagen.Discard(put);
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
             "(Ljava/lang/Object;)Ljava/lang/Object;", 
             new sandmark.util.javagen.New(
                "java.lang.Integer",
                new sandmark.util.javagen.LiteralInt(nodeNumber)));

       sandmark.util.javagen.Cast cast =
             new sandmark.util.javagen.Cast(
                nodeType,
                get);

      return cast;
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
            new sandmark.util.javagen.New(typeName(props)));

      return local;
   }

    public boolean equals(java.lang.Object o) {
	sandmark.watermark.ct.encode.storage.Hash a = (sandmark.watermark.ct.encode.storage.Hash)o;
        return storeLocation==a.storeLocation;
    }

    public int hashCode() {
        return storeLocation + sandmark.watermark.ct.encode.storage.Hash.class.hashCode();
    }

}


