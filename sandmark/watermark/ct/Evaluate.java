package sandmark.watermark.ct;

/**
 *  This class contains code to evaluate the CT algorithm.
 */

public class Evaluate {

 static int edgeCount (sandmark.util.newgraph.MutableGraph graph) {
     return graph.edgeCount();
 }
   public static sandmark.program.Application doit(
        int components,
        java.math.BigInteger watermark,
        String encoding,
        boolean useCycleGraph) throws java.lang.Exception {

      sandmark.watermark.ct.CT ct = new sandmark.watermark.ct.CT();
      sandmark.util.ConfigProperties props = ct.getConfigProperties();
      props.setProperty("Storage Location","global");
      props.setProperty("Protection Method","if");
      props.setProperty("Graph Type",encoding);
      props.setProperty("Subgraph Count",java.lang.Integer.toString(components));
      props.setProperty("Inline Code","false");
      props.setProperty("Replace Watermark Class","false");
      props.setProperty("Dump Intermediate Code","");
      props.setProperty("Storage Method","array");
      sandmark.watermark.DynamicWatermarker.getProperties().setProperty
      	("Watermark",watermark.toString());
      props.setProperty("Numeric Watermark","true");
      props.setProperty("Use Cycle Graph",java.lang.Boolean.toString(useCycleGraph));

       sandmark.util.newgraph.MutableGraph graph = 
           sandmark.watermark.ct.embed.Embedder.constructGraph(watermark.toString(),props);
       sandmark.watermark.ct.encode.Encoder encoder = 
          new sandmark.watermark.ct.encode.Encoder(graph,props,
						   new java.util.Hashtable());
       encoder.encode();

      System.out.println("CODEC: " + encoding + " " + ((useCycleGraph)?"cycle":"plain"));
      System.out.println("COMPONENTS: " + components);
      System.out.println("WATERMARK: " + watermark);

      System.out.println("NODECOUNT: " + graph.nodeCount());
      System.out.println("EDGECOUNT: " + edgeCount(graph));

       sandmark.program.Application app = new sandmark.program.Application();
       sandmark.program.Class bc = encoder.getByteCode(app);
       sandmark.program.Method[] methods = bc.getMethods();
       String sizes = "";
       for(int i=0; i<methods.length; i++){
          sandmark.program.Method method = methods[i];
          String name = method.getName();
          if (name.startsWith("Create_")) {
             org.apache.bcel.generic.InstructionList ilist = method.getInstructionList(); 
	     //ilist.setPositions();
	     //if(components == 1)
	     //System.out.println(ilist);
             byte[] bytes = ilist.getByteCode();
             sizes = sizes + " " + bytes.length;
          }
      }
      System.out.println("METHODSIZES:" + sizes + "\n");
      return app;
   }  

   static void testOneCodec(String codec, boolean useCycleGraph) {
      java.math.BigInteger watermark = java.math.BigInteger.valueOf(16);
      java.math.BigInteger two       = java.math.BigInteger.valueOf(2);
      for(int w=4; w<=64; w++) {
         for (int components=1; components<6; components++) {
	    try {
	       doit(components,watermark,codec, useCycleGraph);
	    } catch(sandmark.watermark.ct.encode.Split.SplitException e) {
            } catch(Exception e) {
		e.printStackTrace();
              System.out.println(codec + " didn't work");
	      throw new Error();
            }
         }
         watermark = watermark.multiply(two);
      }
   }

   public static void main(String args[]) throws java.lang.Exception {
      java.util.Collection wrapperCodecs = 
          sandmark.util.classloading.ClassFinder.getClassesWithAncestor(
                        sandmark.util.classloading.IClassFinder.WRAPPER_CODEC);
      java.util.Collection allCodecs = 
	 sandmark.util.classloading.ClassFinder.getClassesWithAncestor(
            sandmark.util.classloading.IClassFinder.GRAPH_CODEC);
      allCodecs.removeAll(wrapperCodecs);
      String[] codecs = (String[])allCodecs.toArray(new String[0]);
      boolean[] useCycleGraph = {true,false};
      for (int codec=0; codec<codecs.length; codec++) 
         for (int cycle=0; cycle<useCycleGraph.length; cycle++) 
            testOneCodec(codecs[codec], useCycleGraph[cycle]);
   }

} // class Evaluate
