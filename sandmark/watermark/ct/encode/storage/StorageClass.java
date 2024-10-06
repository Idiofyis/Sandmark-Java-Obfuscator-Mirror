package sandmark.watermark.ct.encode.storage;

/**
 * sandmark.watermark.ct.encode.storage.Array, sandmark.watermark.ct.encode.storage.Hash,
 * sandmark.watermark.ct.encode.storage.Pointer, sandmark.watermark.ct.encode.storage.Vector
 *  are classes that deal with how we keep track of
 * graph nodes in the watermarked program. In order to
 * make sure that the garbage collector does not do
 * away with the subgraphs we've built, we must store
 * (at least) the root pointer of each subgraph in some
 * kind of global storage. There are four possibilities
 * right now: we can store in a global Array, HashTable,
 * individual Pointers, or in a java Vector. 
 * <P>
 * sandmark.watermark.ct.encode.storage.Storage is the base-class for
 * these storage methods. 
 * <P>
 * sandmark.watermark.ct.encode.storage.GlobalStorage
 * decides which nodes should be stored where.
 */

abstract public class StorageClass {
   int storeLocation;

   public static final int GLOBAL = 0;
   public static final int FORMAL = 1;

   public StorageClass () {
   }

   public int getStoreLocation() {
      return storeLocation;
   }

   public String toString() {
       return null;
   }

   public String locationToString() {
       return (storeLocation==GLOBAL)?"global":"formal";
   }

   abstract public sandmark.util.javagen.Statement toJavaStore(
      sandmark.util.newgraph.Node node, 
      sandmark.util.ConfigProperties props);

   abstract public sandmark.util.javagen.Expression toJavaLoad(
      sandmark.util.newgraph.Node node, 
      sandmark.util.ConfigProperties props);

   abstract public String typeName (
           sandmark.util.ConfigProperties props);
   abstract public String variableName (
           sandmark.util.ConfigProperties props);

   abstract public sandmark.util.javagen.Statement toJavaCreate (
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.util.ConfigProperties props);

   public sandmark.util.javagen.Java toJavaFormal (
      sandmark.util.newgraph.MutableGraph graph,
      sandmark.util.ConfigProperties props) {
      String nodeType = props.getProperty("Node Class");
      sandmark.util.javagen.Formal formal =
	  new sandmark.util.javagen.Formal(variableName(props),typeName(props));
      return formal;
   }

}


