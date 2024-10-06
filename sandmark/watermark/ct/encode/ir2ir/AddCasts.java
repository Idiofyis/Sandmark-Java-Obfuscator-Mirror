package sandmark.watermark.ct.encode.ir2ir;

public class AddCasts extends Transformer {
   java.util.Hashtable needCasts;
   public AddCasts(sandmark.watermark.ct.encode.ir.Build orig,
           sandmark.util.ConfigProperties props,
		   java.util.Hashtable needCasts) {
      super(orig,props);
      this.needCasts = needCasts;
   }
   public sandmark.watermark.ct.encode.ir.Build mutate() {
      String nodeClass = props.getProperty("Node Class");
      java.util.Stack irs = new java.util.Stack();
      java.util.HashSet visited = new java.util.HashSet();
      irs.add(orig.init); irs.add(orig.creators); irs.add(orig.fixups);
      irs.add(orig.destructors); irs.add(orig.construct); 
      irs.add(orig.destruct); irs.add(orig.staticFields);
      irs.add(orig.storageCreators); irs.add(orig.storageBuilder);
      while(!irs.empty()) {
	 sandmark.watermark.ct.encode.ir.IR ir = 
	    (sandmark.watermark.ct.encode.ir.IR)irs.pop();
	 if(visited.contains(ir))
	    continue;
	 visited.add(ir);
	 if(ir instanceof sandmark.watermark.ct.encode.ir.FieldAccessor) {
	    sandmark.watermark.ct.encode.ir.FieldAccessor fa =
	       (sandmark.watermark.ct.encode.ir.FieldAccessor)ir;
	    String fieldname = fa.getFieldName();
	    if(needCasts.containsKey(fieldname)) {
	       String fieldType = 
		  ((org.apache.bcel.generic.ObjectType)
		   needCasts.get(fieldname)).getClassName();
	       fa.setFieldType(fieldType);
	       if(fa instanceof sandmark.watermark.ct.encode.ir.FollowLink) {
		  sandmark.watermark.ct.encode.ir.FollowLink fl =
		     (sandmark.watermark.ct.encode.ir.FollowLink)ir;
		  fl.castTo(nodeClass);
	       }
	    }
	 } else if(ir instanceof sandmark.watermark.ct.encode.ir.Method) {
	    sandmark.watermark.ct.encode.ir.Method method = 
	       (sandmark.watermark.ct.encode.ir.Method)ir;
	    irs.add(method.ops);
	    irs.add(method.formals);
	 } else if(ir instanceof sandmark.watermark.ct.encode.ir.ProtectRegion) {
	    sandmark.watermark.ct.encode.ir.ProtectRegion pr =
	       (sandmark.watermark.ct.encode.ir.ProtectRegion)ir;
	    irs.add(pr.ops);
	 } else if(ir instanceof sandmark.watermark.ct.encode.ir.List) {
	    sandmark.watermark.ct.encode.ir.List li =
	       (sandmark.watermark.ct.encode.ir.List)ir;
	    for(java.util.Iterator it = li.iterator() ; it.hasNext() ; )
	       irs.add(it.next());
	 }
      }
      return orig;
   }
}
