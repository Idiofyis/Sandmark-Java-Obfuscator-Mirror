package sandmark.watermark.ct.encode.storage;

public class Pointer extends sandmark.watermark.ct.encode.storage.StorageClass {

    public Pointer (int storeLocation) {
       this.storeLocation = storeLocation;
    }

    public String toString() {
        return "Pointer/" + locationToString();
    }

    public static String globalName(sandmark.util.newgraph.Node node) {
        return "sm$" + node.name();
    }

    public String typeName (sandmark.util.ConfigProperties props) {
        return props.getProperty("Node Class");
    }

    public String variableName(
            sandmark.util.ConfigProperties props){
	return "";
    }

    public sandmark.util.javagen.Statement toJavaStore(
       sandmark.util.newgraph.Node node, 
       sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");

      if (storeLocation == sandmark.watermark.ct.encode.storage.StorageClass.FORMAL)
	  return new sandmark.util.javagen.EmptyStatement();

      sandmark.util.javagen.AssignStatic stat =
          new sandmark.util.javagen.AssignStatic(
             nodeType, 
             globalName(node),  //"n"  +  nodeNumber
             nodeType,
             new sandmark.util.javagen.LocalRef(node.name(),nodeType)
          );

      return stat;
   }

    public sandmark.util.javagen.Expression toJavaLoad(
       sandmark.util.newgraph.Node node, 
       sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");

      if (storeLocation == sandmark.watermark.ct.encode.storage.StorageClass.FORMAL)
	  return null;

      sandmark.util.javagen.Expression expr =
          new sandmark.util.javagen.StaticRef(
               nodeType,
               globalName(node), 
               nodeType
            );

      return expr;
   }

    /* 
     * Doesn't work since Java doesn't have reference parameters.
     */
   public sandmark.util.javagen.Statement toJavaCreate (
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.util.ConfigProperties props) {
      return new sandmark.util.javagen.EmptyStatement();
   }

   public static sandmark.util.javagen.Java toJavaField (
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.util.newgraph.Node node,
      sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");
      String[] attributes = {"public","static"};

      sandmark.util.javagen.Field field =
	  new sandmark.util.javagen.Field(
	     globalName(node),
             nodeType,
             attributes);

      return field;
   }

    public boolean equals(java.lang.Object o) {
	sandmark.watermark.ct.encode.storage.Pointer a = (sandmark.watermark.ct.encode.storage.Pointer)o;
        return storeLocation==a.storeLocation;
    }

    public int hashCode() {
        return storeLocation + sandmark.watermark.ct.encode.storage.Pointer.class.hashCode();
    }
}   


