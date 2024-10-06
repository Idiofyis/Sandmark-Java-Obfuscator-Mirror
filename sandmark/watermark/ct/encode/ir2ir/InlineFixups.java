package sandmark.watermark.ct.encode.ir2ir;

//=============== Inline fixup routines in creators ===================
public class InlineFixups extends Transformer {

    public InlineFixups (
      sandmark.watermark.ct.encode.ir.Build p, 
      sandmark.util.ConfigProperties props) {
      super(p,props);
    }
    
int subGraphIndex(
   sandmark.util.newgraph.MutableGraph subGraph,
   sandmark.util.newgraph.MutableGraph[] subGraphs) {
     for(int i=0; i<subGraphs.length; i++)
	 if (subGraph == subGraphs[i])
	     return i;
     return -1;
   }

void inline(
   sandmark.watermark.ct.encode.ir.Fixup fixup,
   sandmark.util.newgraph.MutableGraph subGraph) {
   java.util.Iterator iter = orig.creators.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.Create creator = (sandmark.watermark.ct.encode.ir.Create) iter.next();
      if (creator.subGraph == subGraph) {
         creator.ops.cons(fixup.ops);
         return;
      }
   }
}

public sandmark.watermark.ct.encode.ir.Build mutate() {
   String individualFixups = props.getProperty("DWM_CT_Encode_IndividualFixups");
   if (individualFixups.equals("true"))
       return orig;

   java.util.Iterator iter = orig.fixups.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.Fixup fixup = (sandmark.watermark.ct.encode.ir.Fixup) iter.next();
      int subGraph1index = subGraphIndex(fixup.subGraph1, orig.subGraphs);
      int subGraph2index = subGraphIndex(fixup.subGraph2, orig.subGraphs);
      if (subGraph1index > subGraph2index) 
	 inline(fixup, orig.subGraphs[subGraph1index]);
      else
         inline(fixup, orig.subGraphs[subGraph2index]);
   }
   orig.fixups = new sandmark.watermark.ct.encode.ir.List();
   return orig;
}
}

