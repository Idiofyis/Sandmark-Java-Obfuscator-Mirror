package sandmark.watermark.ct.encode.ir2ir;

public class AddFields extends Transformer {

    public AddFields (
      sandmark.watermark.ct.encode.ir.Build p, 
      sandmark.util.ConfigProperties props) {
      super(p,props);
    }

sandmark.watermark.ct.encode.ir.List addStaticFields() {
   java.util.HashSet storageKinds = new java.util.HashSet();
   java.util.Iterator iter = orig.graph.nodes();
   while (iter.hasNext()) {
      sandmark.util.newgraph.Node node = 
	 (sandmark.util.newgraph.Node) iter.next();
      sandmark.watermark.ct.encode.storage.NodeStorage s = orig.storage.lookup(node);
      if (s != null) {
         sandmark.watermark.ct.encode.storage.StorageClass c = s.getStorageClass();
         if (c.getStoreLocation()==sandmark.watermark.ct.encode.storage.StorageClass.GLOBAL)
	     storageKinds.add(s.getStorageClass());
      }
   }

   sandmark.watermark.ct.encode.ir.List fields = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator siter = storageKinds.iterator();
   while (siter.hasNext()) {
      sandmark.watermark.ct.encode.storage.StorageClass s = 
          (sandmark.watermark.ct.encode.storage.StorageClass) siter.next();
      sandmark.watermark.ct.encode.ir.Field field =
             new sandmark.watermark.ct.encode.ir.Field(
                s.variableName(props), s.typeName(props), true);
      fields.cons(field);
   }

   java.util.Iterator piter =  orig.graph.nodes();
   while (piter.hasNext()) {
      sandmark.util.newgraph.Node node = 
	 (sandmark.util.newgraph.Node) piter.next();
      sandmark.watermark.ct.encode.storage.NodeStorage s = orig.storage.lookup(node);
      if (s != null) {
         sandmark.watermark.ct.encode.storage.StorageClass c = s.getStorageClass();
         if (c instanceof sandmark.watermark.ct.encode.storage.Pointer) {
            sandmark.watermark.ct.encode.storage.Pointer p = (sandmark.watermark.ct.encode.storage.Pointer) c;
            sandmark.watermark.ct.encode.ir.Field field = new sandmark.watermark.ct.encode.ir.Field(
                sandmark.watermark.ct.encode.storage.Pointer.globalName(node), p.typeName(props), true);
            fields.cons(field);
         }
      }
   }
   return fields;
}

sandmark.watermark.ct.encode.ir.List addStorageCreators() {
   java.util.HashSet storageKinds = new java.util.HashSet();
   java.util.Iterator iter = orig.graph.nodes();
   while (iter.hasNext()) {
      sandmark.util.newgraph.Node node = 
	 (sandmark.util.newgraph.Node) iter.next();
      sandmark.watermark.ct.encode.storage.NodeStorage s = orig.storage.lookup(node);
      if (s != null) {
         sandmark.watermark.ct.encode.storage.StorageClass c = s.getStorageClass();
	     storageKinds.add(s.getStorageClass());
      }
   }

   sandmark.watermark.ct.encode.ir.List creators = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator siter = storageKinds.iterator();
   while (siter.hasNext()) {
      sandmark.watermark.ct.encode.storage.StorageClass s = 
          (sandmark.watermark.ct.encode.storage.StorageClass) siter.next();
      sandmark.watermark.ct.encode.ir.CreateStorage c =
	  new sandmark.watermark.ct.encode.ir.CreateStorage(orig.graph, s, props);
      creators.cons(c);
   }

   return creators;
}

/*
 * For every generated method add the appropriate "Formal"-instructions.
 */
public sandmark.watermark.ct.encode.ir.Build mutate() {
   orig.staticFields = addStaticFields();
   orig.storageCreators = addStorageCreators();
   return orig;
}

}

