package sandmark.watermark.ct.encode.storage;

public class NodeStorage {
    sandmark.util.newgraph.Node node;
    sandmark.watermark.ct.encode.storage.StorageClass storage;

    public NodeStorage (
       sandmark.util.newgraph.Node node, 
       sandmark.watermark.ct.encode.storage.StorageClass storage) {
       this.node = node;
       this.storage = storage;
    }

    public String toString() {
        return node.name() + ":" + storage.toString();
    }

    public sandmark.watermark.ct.encode.storage.StorageClass getStorageClass() {
       return storage;
    }

    public sandmark.util.javagen.Statement toJavaStore(sandmark.util.ConfigProperties props) {
       return storage.toJavaStore(node, props);
    }

    public sandmark.util.javagen.Statement toJavaLoad(sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");

      sandmark.util.javagen.Expression load = storage.toJavaLoad(node, props);
      if (load == null)
	  return  new sandmark.util.javagen.EmptyStatement();

      sandmark.util.javagen.Local stat = 
         new sandmark.util.javagen.Local(
            node.name(),
            nodeType,
            load);

      return stat;
   }

}
       



