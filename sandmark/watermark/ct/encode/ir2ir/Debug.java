package sandmark.watermark.ct.encode.ir2ir;

public class Debug extends Transformer {

public Debug (
   sandmark.watermark.ct.encode.ir.Build p, 
   sandmark.util.ConfigProperties props) {
   super(p,props);
}

/*
 * Run through every generated method adding a 'Debug'
 * instruction. This instruction will print out a
 * message when the method is executed.
 */
public void debug(sandmark.watermark.ct.encode.ir.List M) {
   java.util.Iterator iter = M.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.Method f = (sandmark.watermark.ct.encode.ir.Method) iter.next();
      String msg = "Calling '" + f.name() + "'";
      f.ops.snoc(new sandmark.watermark.ct.encode.ir.Debug(f.graph,msg));
   };
}

public sandmark.watermark.ct.encode.ir.Build mutate() {
   debug(orig.creators);
   debug(orig.fixups);
   debug(orig.destructors);
   return orig;
}

}

