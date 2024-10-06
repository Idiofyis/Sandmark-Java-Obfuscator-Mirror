package sandmark.watermark.ct.encode.ir2ir;

public class AddFormals extends Transformer {
   String[][] storageCreators;

    public AddFormals (
      sandmark.watermark.ct.encode.ir.Build p, 
      sandmark.util.ConfigProperties props,
      String[][] storageCreators) {
      super(p,props);
      this.storageCreators = storageCreators;
    }

/*
 * Run through the list of intermediate code instructions, looking
 * for "SaveNode"- and "LoadNode"-instructions. Return the
 * appropriate list of "Formal"-instructions.
 */
sandmark.watermark.ct.encode.ir.List findFormals(
      sandmark.watermark.ct.encode.ir.Method method) {
   sandmark.watermark.ct.encode.ir.List ops = method.ops;
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   java.util.HashSet seen = new java.util.HashSet();
   java.util.Iterator iter = ops.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      //System.out.println("AddFormals.addFormals:0:formal='" +f+"'");
      if (f instanceof sandmark.watermark.ct.encode.ir.NodeStorage) {
         sandmark.watermark.ct.encode.ir.NodeStorage C = (sandmark.watermark.ct.encode.ir.NodeStorage) f;
         sandmark.watermark.ct.encode.storage.NodeStorage s = C.location;
         sandmark.watermark.ct.encode.ir.Formal formal =
             new sandmark.watermark.ct.encode.ir.Formal(
                s.getStorageClass().variableName(props), 
                s.getStorageClass().typeName(props));
         //System.out.println("AddFormals.addFormals:1:formal='" +formal+"'");
      //System.out.println("AddFormals.addFormals:2:seen='" +seen+"'");
         if (!seen.contains(formal)) {
            P.cons(formal);
            seen.add(formal);
            //System.out.println("AddFormals.addFormals:3:P='" +P+"'");
	 }
      }
   }
   return P;
}

/**
 * This method returns the list of formal parameters to add to the
 * CreateGraph methods. It is less precise than the previous one
 * in that every method gets the complete set of formals (one per
 * generated storage creator), rather than the minimal set of formals
 * that it actually needs.
 * @param method  the CreateGraph/Fixup method.
 * @param storageCreators[][] an array of quadruples: 
 *                            {methodName, returnType, localName, GLOBAL/FORMAL}
 *                            for example: 
 *                            {"CreateStorage_sm$hash", "java.util.Hashtable", "sm$hash", "FORMAL"}
 */
sandmark.watermark.ct.encode.ir.List findFormals(
   sandmark.watermark.ct.encode.ir.Method method,
   String[][] storageCreators) {
   sandmark.watermark.ct.encode.ir.List P = new sandmark.watermark.ct.encode.ir.List();
   for(int i=0; i<storageCreators.length; i++) {
      String returnType = storageCreators[i][1];
      String localName = storageCreators[i][2];
      boolean isGlobal = storageCreators[i][3].equals("GLOBAL");
      sandmark.watermark.ct.encode.ir.Formal formal =
         new sandmark.watermark.ct.encode.ir.Formal(localName, returnType);
      if (!isGlobal)
         P.cons(formal);
   }
   return P;
}

/*
 * For every generated method add the appropriate "Formal"-instructions.
 */
public sandmark.watermark.ct.encode.ir.Build mutate() {
    String location = props.getProperty("Storage Location");
    if (location.equals("global")) return orig;

   java.util.Iterator iter1 = orig.creators.iterator();
   while (iter1.hasNext()) {
      sandmark.watermark.ct.encode.ir.Create f = (sandmark.watermark.ct.encode.ir.Create) iter1.next();
      sandmark.watermark.ct.encode.ir.List newFormals = findFormals(f, storageCreators);
      f.setFormals(newFormals);
   };

  java.util.Iterator iter3 = orig.fixups.iterator();
   while (iter3.hasNext()) {
      sandmark.watermark.ct.encode.ir.Fixup f = (sandmark.watermark.ct.encode.ir.Fixup) iter3.next();
      sandmark.watermark.ct.encode.ir.List newFormals = findFormals(f, storageCreators);
      f.setFormals(newFormals);
   };
   return orig;
}

}

