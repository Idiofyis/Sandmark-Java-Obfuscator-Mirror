package sandmark.watermark.ct.encode.storage;

/**
 * This class decides which nodes should be stored 
 * in what type of storage.
*/

public class GlobalStorage {

   java.util.Hashtable store;

/**
 * Call it like this:
 * <PRE>
 *  sandmark.watermark.ct.encode.storage.GlobalStorage storage = 
      new sandmark.watermark.ct.encode.storage.GlobalStorage (graph,subGraphs,props);
 * </PRE>
 * <P>
 *  The following properties should be defined:
 *  <BR> Storage Policy:           Which nodes should we store in global
 *                                    variables? 'root' (only sub-graph roots)
 *                                    or 'all' (all nodes).
 *  <BR> Storage Method:        In what type of structure should we store
 *                                    nodes? This property is a colon-separated 
 *                                    list of '{vector,array,pointer,hash}'
 *                                    describing where we may store global
 *                                    sub-graph pointers.
 *  <BR> Storage Location:       Where should we store sub-graph pointers?
 *                                    This property is either 'global' or
 *                                    'formal'. 'Global' means that we're storing
 *                                    pointers in some static global variables,
 *                                    'formal' means that these pointers get 
 *                                    passed around from method to method as
 *                                    arguments to these methods.
 */
    public GlobalStorage (
       sandmark.util.newgraph.MutableGraph graph, 
       sandmark.util.newgraph.MutableGraph[] subGraphs,
       sandmark.util.ConfigProperties props) {
       store = new java.util.Hashtable();
       String storeWhat = props.getProperty("Storage Policy");
       String globalStoreKinds = props.getProperty("Storage Method");
       String storeLocation = props.getProperty("Storage Location");
       selectStorage(graph, subGraphs, storeWhat, globalStoreKinds, storeLocation);
    }

    public sandmark.watermark.ct.encode.storage.NodeStorage lookup(sandmark.util.newgraph.Node node) {
       return (sandmark.watermark.ct.encode.storage.NodeStorage) store.get(node);
    }

static sandmark.watermark.ct.encode.storage.StorageClass[] storageClasses = {
   new sandmark.watermark.ct.encode.storage.Array(0), 
   new sandmark.watermark.ct.encode.storage.Hash(0),
   new sandmark.watermark.ct.encode.storage.Pointer(0), 
   new sandmark.watermark.ct.encode.storage.Vector(0)
};

public static sandmark.watermark.ct.encode.storage.StorageClass[] getStorageClasses() {
   return storageClasses;
}

//==================== Save some nodes in globals =======================

   static String[] storageKinds(String globalStoreKinds) {
     java.util.StringTokenizer S = new java.util.StringTokenizer(globalStoreKinds,":");
     int C = S.countTokens();
     String[] res = new String[C];
     for(int i=0; i<C; i++)
        res[i] = S.nextToken();
     return res;
   }

   void pickRandomStorage(String[] kinds, 
			  sandmark.util.newgraph.Node node, int location) {
      String where = kinds[sandmark.util.Random.getRandom().nextInt(kinds.length)];
      sandmark.watermark.ct.encode.storage.StorageClass S = null;
      if (where.equals("array"))
	 S = new sandmark.watermark.ct.encode.storage.Array(location);
      else if (where.equals("pointer"))
	 S = new sandmark.watermark.ct.encode.storage.Pointer(location);
      else if (where.equals("vector"))
	 S = new sandmark.watermark.ct.encode.storage.Vector(location);
      else if (where.equals("hash"))
	 S = new sandmark.watermark.ct.encode.storage.Hash(location);
      sandmark.watermark.ct.encode.storage.NodeStorage n = 
         new sandmark.watermark.ct.encode.storage.NodeStorage(node, S);
      store.put(node, n);
   }

/*
 * globalStoreKinds is a list of 'arr', 'ptr', 'vec', 'hash'.
 * storeWhat is 'all', 'root', or 'none'.
 * storeLocation is 'global', or 'formal'.
 */
void selectStorage(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph[] subGraphs, 
   String storeWhat, 
   String globalStoreKinds,
   String storeLocation) {
   int location = -1;
   if (storeLocation.equals("global"))
       location = sandmark.watermark.ct.encode.storage.StorageClass.GLOBAL;
   else
       location = sandmark.watermark.ct.encode.storage.StorageClass.FORMAL;

   String[] kinds = storageKinds(globalStoreKinds);
   if (storeWhat.equals("all")) {
      java.util.Iterator iter = graph.nodes();
      while (iter.hasNext()) {
         sandmark.util.newgraph.Node node = 
	    (sandmark.util.newgraph.Node) iter.next();
         pickRandomStorage(kinds, node, location);
      }
   } else if (storeWhat.equals("root")) {
     for(int i=0; i<subGraphs.length; i++)
        pickRandomStorage(kinds, 
			  (sandmark.util.newgraph.Node)subGraphs[i].getRoot(), 
			  location);
   };
}

  //--------------------------------------------------------------
   public String toString (String indent){
      java.util.Enumeration iter = store.keys();
      String S = "";
      while (iter.hasMoreElements()) {
         sandmark.util.newgraph.Node n = 
	    (sandmark.util.newgraph.Node) iter.nextElement();
         sandmark.watermark.ct.encode.storage.NodeStorage s = (sandmark.watermark.ct.encode.storage.NodeStorage) store.get(n);
         String StorageClass = s.getStorageClass().getClass().getName();
         S +=  indent + "   " + n.name() + " ==> " + StorageClass + "\n";
      }
      return S;
   }

   public String toString (){
      return toString("");
   }


  //--------------------------------------------------------------
   public static void printStorage (
      sandmark.watermark.ct.encode.storage.GlobalStorage storage, 
      String nodeName,
      sandmark.util.newgraph.Node node) {
      sandmark.watermark.ct.encode.storage.NodeStorage s = storage.lookup(node);
      System.out.print(nodeName + " is stored in: ");
      if (s == null)
          System.out.println("null");
      else
          System.out.println(s.toString());
   }
}

