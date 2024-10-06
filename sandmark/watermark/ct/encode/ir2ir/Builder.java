package sandmark.watermark.ct.encode.ir2ir;

public class Builder extends Transformer {

public Builder (
   sandmark.watermark.ct.encode.ir.Build p, 
   sandmark.util.ConfigProperties props) {
   super(p,props);
}

public sandmark.watermark.ct.encode.ir.Build mutate() {
   orig.construct = genConstructMethod();
   orig.destruct = genDestructMethod();
   return orig;
}

sandmark.watermark.ct.encode.ir.List genBuildCalls(
   sandmark.watermark.ct.encode.ir.List methods) {
   String nodeType = props.getProperty("Node Class");
   sandmark.watermark.ct.encode.ir.List stats = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator iter = methods.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.Method method = (sandmark.watermark.ct.encode.ir.Method) iter.next();
      sandmark.watermark.ct.encode.ir.List args = new sandmark.watermark.ct.encode.ir.List();
      stats.cons(new sandmark.watermark.ct.encode.ir.StaticCall(nodeType, method.name(), "()V", args));
   }
   return stats;
}

sandmark.watermark.ct.encode.ir.Method genConstructMethod() {
   sandmark.watermark.ct.encode.ir.List formals = new sandmark.watermark.ct.encode.ir.List();
   sandmark.watermark.ct.encode.ir.List calls = new sandmark.watermark.ct.encode.ir.List();
   calls.cons(genBuildCalls(orig.init));
   calls.cons(genBuildCalls(orig.creators));
   calls.cons(genBuildCalls(orig.fixups));
   return new sandmark.watermark.ct.encode.ir.Construct(orig.graph, calls, formals);
}

sandmark.watermark.ct.encode.ir.Method genDestructMethod() {
   sandmark.watermark.ct.encode.ir.List formals = new sandmark.watermark.ct.encode.ir.List();
   sandmark.watermark.ct.encode.ir.List calls = new sandmark.watermark.ct.encode.ir.List();
   calls.cons(genBuildCalls(orig.destructors));
   return  new sandmark.watermark.ct.encode.ir.Destruct(orig.graph, calls, formals);
}

}

